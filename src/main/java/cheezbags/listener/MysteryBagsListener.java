package cheezbags.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import cheezbags.Hand;
import cheezbags.MysteryBag;
import cheezbags.MysteryBags;
import cheezbags.events.MysteryBagDropEvent;

public class MysteryBagsListener implements Listener {
    
    public MysteryBagsListener(MysteryBags instance) {
        this.instance = instance;
    }
    
    private MysteryBags instance;
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == SpawnReason.SPAWNER)
            e.getEntity().setMetadata("isSpawnerMob", new FixedMetadataValue(instance, true));
    }
    
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
        
        boolean isBaby = entity instanceof Animals && !((Animals) entity).isAdult();
        boolean isSpawner = entity.hasMetadata("isSpawnerMob");
        int looting = p.getInventory().getItemInMainHand() == null ? 0 : p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        double lootExtraChance = instance.lootingEffectiveness * looting;
        for (MysteryBag bag : instance.cheezBags.values()) {
            if (bag.willDrop(entity, lootExtraChance, isBaby, isSpawner)) {
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
            
            if (i == null || i.getType() == Material.AIR || i.getItemMeta() == null)
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

    @EventHandler(priority = EventPriority.MONITOR)
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
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().equals(event.getView().getBottomInventory()) && !event.getClick().isShiftClick() && !event.getClick().isKeyboardClick())
            return;
            
        ItemStack item = event.getClickedInventory().equals(event.getView().getTopInventory()) && !event.getClick().isKeyboardClick() ? event.getCursor() : event.getCurrentItem();
        if (check(event.getInventory().getType(), item))
            event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryDragEvent(InventoryDragEvent e) {
        if (e.getInventory() == null)
            return;
        
        if (check(e.getInventory().getType(), e.getOldCursor()))
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryMove(InventoryMoveItemEvent e) {
        if (check(e.getDestination().getType(), e.getItem()))
            e.setCancelled(true);
    }
    
    private static boolean check(InventoryType type, ItemStack item) {
           switch (type) {
           case WORKBENCH:
           case CRAFTING:
           case BEACON:
           case BREWING:
           case ENCHANTING:
           case FURNACE:
           case MERCHANT:
               return b(item);
           default:
               return false;
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
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet aset = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        for (ProtectedRegion p : aset.getRegions()) {
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
