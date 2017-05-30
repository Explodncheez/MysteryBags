package cheezbags.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import cheezbags.MysteryBag;
import cheezbags.MysteryBags;
import cheezbags.Hand;
import cheezbags.events.MysteryBagDropEvent;

public class MysteryBagsListener implements Listener {
	
	public MysteryBagsListener(MysteryBags instance) {
		this.instance = instance;
	}
	
	private MysteryBags instance;
	
	@EventHandler
	public void onEntityKill(EntityDeathEvent e) {
		if (!instance.dropFromMobs)
			return;

		Player p = e.getEntity().getKiller();
		LivingEntity entity = e.getEntity();

		if (p == null && instance.requirePlayerKill)
			return;

		if (!instance.limitWorlds.isEmpty() && !instance.limitWorlds.contains(entity.getWorld().getName()))
			return;

		if (!instance.limitRegions.isEmpty() && !a(entity.getLocation(), instance.limitRegions))
			return;

		int looting = p.getInventory().getItemInMainHand() == null ? 0 : p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		double lootExtraChance = instance.lootingEffectiveness * looting;
		for (MysteryBag bag : instance.cheezBags.values()) {
			if (bag.willDrop(entity.getType(), lootExtraChance)) {
				MysteryBagDropEvent event = new MysteryBagDropEvent(p, entity, bag.getBagItem());
				instance.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled())
					return;
				
				ItemStack drop = event.getItem();
				if (instance.lootingSensitiveAmount) {
					drop.setAmount(drop.getAmount() * (1 + instance.random.nextInt(looting + 1)));
				}
				if (MysteryBags.instance().overrideDrops) {
				    entity.getWorld().dropItem(entity.getLocation(), drop);
				} else {
				    e.getDrops().add(drop);
				}
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction().toString().charAt(0) == 'R') {
			Player p = e.getPlayer();
			ItemStack i = e.getItem();
			
			if (i == null)
				return;

			Hand hand = e.getHand() == EquipmentSlot.HAND ? Hand.MAIN : Hand.OFF;
			List<String> lore = i.getItemMeta().getLore();
			if (lore != null && lore.size() > 0) {
				String id = lore.get(0).replace("§", "");
				MysteryBag bag = instance.cheezBags.get(id);
				if (bag != null) {
					e.setCancelled(true);
					if (p.hasPermission("mysterybags.open"))
						bag.open(e.getPlayer(), hand);
					else
						p.sendMessage(MysteryBags.PREFIX + "§7You do not have permission to open that.");
				}
			}
		}
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent e) {
		boolean playerInv = e.getClickedInventory() instanceof PlayerInventory;
		Player p = (Player) e.getWhoClicked();
		for (int i = playerInv ? 80 : 0; i < (playerInv ? 84 : e.getClickedInventory().getContents().length); i++) {
			ItemStack item = e.getClickedInventory().getContents()[i];
			if (b(item)) {
				e.setCancelled(true);
				p.sendMessage(MysteryBags.PREFIX + "§7You may not craft with Mystery Bags!");
				p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.4F, 1.2F);
				p.closeInventory();
				break;
			}
		}
	}
	
	@EventHandler
	public void onFurnaceSmelt(FurnaceSmeltEvent e) {
		if (b(e.getSource()) || b(((Furnace) e.getBlock().getState()).getInventory().getFuel()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onPotionBrew(BrewEvent e) {
		for (ItemStack item : e.getContents().getContents()) {
			if (b(item)) {
				e.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getPlayer().hasMetadata("cheezbags.editor")) {
			String title = e.getPlayer().getMetadata("cheezbags.editor").get(0).asString();
			MysteryBag bag = instance.cheezBags.get(title);
			if (bag != null) {
				bag.closeEditor((Player) e.getPlayer(), e.getInventory());
				e.getPlayer().sendMessage(MysteryBags.PREFIX + "§aRemember to use §e/mbags save " + title + " §ato save your changes!");
			}
			e.getPlayer().removeMetadata("cheezbags.editor", MysteryBags.instance());
		}
	}
	
	private static boolean a(Location loc, Set<String> set) {
		if (!MysteryBags.instance().worldguard || set.isEmpty())
			return true;
		
		Set<String> returned = new HashSet<String>();
		for (ProtectedRegion p : WorldGuardPlugin.inst().getRegionManager(loc.getWorld()).getApplicableRegions(loc).getRegions()) {
			returned.add(p.getId());
		}
		return returned.removeAll(set);
	}
	
	private static boolean b(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			List<String> lore = item.getItemMeta().getLore();
			if (lore != null) {
				return MysteryBags.instance().cheezBags.containsKey(lore.get(0).replace("§", ""));
			}
		}
		return false;
	}

}
