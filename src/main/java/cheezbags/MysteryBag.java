package cheezbags;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import cheezbags.events.MysteryBagOpenEvent;
import net.md_5.bungee.api.ChatColor;

public class MysteryBag {
    
    public MysteryBag(String name, YamlConfiguration config) {
        this(name, config.getString("material"), config.getString("displayname"), config.getStringList("openmsg"), false);

        this.enabled = ConfigReader.getBoolean(config, "enabled");
        this.alwaysRare = ConfigReader.getBoolean(config, "always-rare");
        this.giveAll = ConfigReader.getBoolean(config, "give-all-items");
        
        if (config.isSet("allow-spawner-drop")) {
            this.allowSpawners = ConfigReader.getBoolean(config, "allow-spawner-drop");
        }
        
        if (config.isSet("allow-baby-drop")) {
            this.allowBaby = ConfigReader.getBoolean(config, "allow-baby-drop");
        }
        
        for (String s : config.getStringList("limit-mob")) {
            try {
                limitMobs.add(ConfigReader.getEntityType(s));
            } catch (Exception e) {
                MysteryBags.throwError("§e" + s + "§c is an invalid entity type in limit-mob in file: " + id + ".yml!");
            }
        }
        
        this.dropChance = config.getDouble("drop-chance");
        
        for (String s : config.getConfigurationSection("drop-chance-mobs").getKeys(false)) {
            EntityType type;
            double d;
            try {
                type = ConfigReader.getEntityType(s);
            } catch (Exception e) {
                MysteryBags.throwError("§e" + s + "§c is an invalid entity type in drop-chance-mob in file: " + id + ".yml!");
                continue;
            }
            
            try {
                d = Double.parseDouble(config.getString("drop-chance-mobs." + s));
            } catch (NumberFormatException e) {
                MysteryBags.throwError("§e" + s + "§c is an invalid entity number in drop-chance-mob in file: " + id + ".yml!");
                continue;
            }
            
            dropChanceMobs.put(type, d);
        }
        
        this.failurechance = config.getDouble("failure-chance");
        
        for (String s : config.getStringList("failure-lines"))
            failureLines.add(s.replace("&", "§"));
        
        for (ItemStack item : (List<ItemStack>) config.get("items")) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName())
                meta.setDisplayName(meta.getDisplayName().replace("&", "§"));
            if (meta.hasLore()) {
                List<String> newLore = new ArrayList<String>();
                for (String lore : meta.getLore())
                    newLore.add(lore.replace("&", "§"));
                meta.setLore(newLore);
            }
            item.setItemMeta(meta);
            this.rawContents.add(item);
        }
        loadRaw();
    }
    
    public MysteryBag(String id, String icon, String displayname, List<String> openmsg, boolean create) {
        try {
            String[] split = icon.split(":");
            try {
                this.item = new ItemStack(Material.valueOf(split[0].toUpperCase()), 1, split.length > 1 ? Short.parseShort(split[1]) : 0);
            } catch (Exception e) {
                MysteryBags.throwError(id + " has an invalid material specified!");
                this.item = new ItemStack(Material.CHEST);
            }
        } catch (Exception e) {
            try {
                this.item = new ItemStack(Material.valueOf(icon.toUpperCase()));
            } catch (Exception e2) {
                MysteryBags.throwError(id + " has an invalid material specified!");
                this.item = new ItemStack(Material.CHEST);
            }
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayname.replace("&", "§"));
        List<String> lore = new ArrayList<String>();
        lore.add(id.replaceAll("(.)", "§$1"));
        for (String string : openmsg)
            lore.add(string.replace("&", "§"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        this.id = id;
        this.openmsg = openmsg;
        this.rawContents = new ArrayList<ItemStack>();
        this.contents = new ArrayList<ItemStack>();
        
        if (create) {
            this.dropChance = 0.2;
            this.failurechance = 0.0;
            YamlConfiguration config = new YamlConfiguration();
            config.set("enabled", false);
            config.set("material", item.getType() + ":" + item.getDurability());
            config.set("give-all-items", false);
            config.set("always-rare", false);
            config.set("displayname", item.getItemMeta().getDisplayName().replace("§", "&"));
            config.set("openmsg", this.openmsg);
            config.set("limit-mob", new ArrayList<EntityType>(limitMobs));
            config.set("failure-chance", failurechance);
            config.set("failure-lines", failureLines);
            config.set("drop-chance", dropChance);
            config.set("drop-chance-mobs", dropChanceMobs);
            config.set("items", this.contents);
            try {
                config.save(new File(MysteryBags.instance().getDataFolder(), id + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveConfig();
            MysteryBags.instance().cheezBags.put(id, this);
        }
    }
    
    private boolean enabled, giveAll, alwaysRare;
    
    private ItemStack item;
    private String id;
    private double dropChance, failurechance;
    private Boolean allowBaby = null, allowSpawners = null;
    
    private Set<EntityType> limitMobs = new HashSet<EntityType>();
    private Map<EntityType, Double> dropChanceMobs = new HashMap<EntityType, Double>();
    private List<ItemStack> rawContents;
    private List<ItemStack> contents;
    
    private List<String> openmsg, failureLines = new ArrayList<String>();
    
    private static int random(int range) {
        return range == 0 ? 0 : MysteryBags.instance().random.nextInt(range + 1);
    }
    
    private static double random() {
        return MysteryBags.instance().random.nextDouble();
    }
    
    private static PotionEffectType[] bpe = {
        PotionEffectType.POISON,
        PotionEffectType.SLOW,
        PotionEffectType.CONFUSION,
        PotionEffectType.BLINDNESS
    };
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void saveConfig() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MysteryBags.instance().getDataFolder(), id + ".yml"));
        config.set("items", rawContents);
        try {
            config.save(new File(MysteryBags.instance().getDataFolder(), id + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRaw() {
        for (ItemStack item : rawContents) {
            ItemStack clone = item.clone();
            ItemMeta meta = clone.getItemMeta();
            if (meta.getLore() != null && meta.getLore().get(0).startsWith("§c§lAMOUNT: §f")) {
                String amount = ChatColor.stripColor(meta.getLore().get(0)).split(" ")[1];
                List<String> lore = meta.getLore();
                lore.remove(0);
                meta.setLore(lore);

                if (amount.contains("-")) {
                    meta.setDisplayName((meta.hasDisplayName() ? meta.getDisplayName() : "§j") + " statistic_item_amount " + amount);
                } else {
                    clone.setAmount(Integer.parseInt(amount));
                }
                clone.setItemMeta(meta);
            } else
                clone.setAmount(1);
            for (int c = 0; c < item.getAmount(); c++) {
                this.contents.add(clone);
            }
        }
    }
    
    public void openEditor(Player p) {
        p.setMetadata("cheezbags.editor", new FixedMetadataValue(MysteryBags.instance(), id));
        Inventory inv = Bukkit.createInventory(null, 54, id + " Bag");
        for (int c = 0; c < Math.min(54, rawContents.size()); c++)
            inv.setItem(c, rawContents.get(c));
        p.openInventory(inv);
    }
    
    public void closeEditor(Player p, Inventory closed) {
        rawContents.clear();
        for (ItemStack item : closed.getContents())
            if (item != null)
                rawContents.add(item);
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * @return the literal ItemStack object for the bag's item. This object should NOT be modified under normal circumstances. Use getBagItem() instead for a copy that doesn't affect the bag.
     */
    public ItemStack getRawItem() {
        return item;
    }

    /**
     * @return a copy of the bag's ItemStack. This can be modified without affecting the bag.
     */
    public ItemStack getBagItem() {
        return item.clone();
    }
    
    /**
     * @return true if both the chance check and the mob type checks pass.
     */
    public boolean willDrop(Entity entity, double chanceIncrease, boolean isBaby, boolean isSpawner) {
        if (!enabled) {
            return false;
        }

        EntityType type = entity.getType();
        if (!limitMobs.isEmpty() && !limitMobs.contains(type)) {
            return false;
        }
        
        if (isSpawner && (this.allowSpawners == null ? !MysteryBags.instance().allowSpawners : !this.allowSpawners)) {
            return false;
        }

        if (isBaby && (this.allowBaby == null ? !MysteryBags.instance().allowBaby : !this.allowBaby)) {
            return false;
        }
        
        double mod = MysteryBags.instance().lootingSensitiveChance ? chanceIncrease : 0;
        double rng = random();
        return dropChanceMobs.containsKey(type) ?
                rng < dropChanceMobs.get(type) + mod :
                rng < dropChance + mod;
    }
    
    /**
     * @param p the player who opened the bag.
     * @param hand important to tell which hand held the bag. If null, no item will be removed.
     */
    public void open(Player p, Hand hand) {
        MysteryBagOpenEvent event = new MysteryBagOpenEvent(p, hand, this, contents.get(random(contents.size() - 1)).clone(), random() < failurechance, true);
        MysteryBags.instance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        
        ItemStack item;
        if (hand != null && event.getConsumeItem())
            switch (hand) {
            case MAIN:
                item = p.getInventory().getItemInMainHand();
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else
                    p.getInventory().setItemInMainHand(null);
                break;
            case OFF:
                item = p.getInventory().getItemInOffHand();
                if (item.getAmount() > 1)
                    item.setAmount(item.getAmount() - 1);
                else
                    p.getInventory().setItemInOffHand(null);
                break;
            }
        
        if (!giveAll) {
            ItemStack loot = event.getLoot();
            if (event.getFailed()) {
                p.addPotionEffect(new PotionEffect(bpe[random(bpe.length - 1)], 160, 1));
                p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
                if (!failureLines.isEmpty())
                    p.sendMessage(failureLines.get(random(failureLines.size() - 1)));
                MysteryBags.log(p, this, null, false);
                return;
            }
            
            boolean rare = alwaysRare || MysteryBags.isRare(loot);
            if (rare) {
                String s = MysteryBags.PREFIX +  MysteryBags.getRareLootMessage(p, loot).replace("%BAG%", this.item.getItemMeta().hasDisplayName() ? this.item.getItemMeta().getDisplayName() : id);
                if (MysteryBags.instance().announceRare)
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(s);
                        if (MysteryBags.instance().rareSound)
                            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7F, 1.5F);
                    }
                if (MysteryBags.instance().rareFirework) {
                    final Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    FireworkMeta meta = fw.getFireworkMeta();
                    Builder builder = FireworkEffect.builder();
                    builder.flicker(true);
                    builder.with(Type.BALL_LARGE);
                    builder.trail(true);
                    builder.withColor(Color.fromRGB(255, 255, 1));
                    builder.withColor(Color.WHITE);
                    builder.withFade(Color.ORANGE);
                    meta.addEffect(builder.build());
                    fw.setFireworkMeta(meta);
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            fw.detonate();
                        }
                    }.runTaskLater(MysteryBags.instance(), 2L);
                }
            }
            giveLoot(p, loot);
            MysteryBags.log(p, this, loot, rare);
            p.sendMessage(MysteryBags.getOpenMessage(loot).replace("%BAG%", this.item.getItemMeta().hasDisplayName() ? this.item.getItemMeta().getDisplayName() : id));
            
            if (MysteryBags.instance().spyMessage) {
                String message = MysteryBags.PREFIX + "§6" + p.getName() + " §7opened a(n) §e" + id + " §7and got §e" + (loot.getAmount() > 1 ? loot.getAmount() + " x " : "") + loot.getType() + (loot.getItemMeta().getDisplayName() == null ? "" : " §7called §6" + loot.getItemMeta().getDisplayName()) + "§7!";
                for (OfflinePlayer op : Bukkit.getOperators())
                    if (op.isOnline() && !op.getPlayer().equals(p))
                        op.getPlayer().sendMessage(message);
            }
        } else {
            for (ItemStack i : contents) {
                giveLoot(p, i.clone());
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.8F, 0.7F);
    }
    
    private static void giveLoot(Player p, ItemStack loot) {
        if (loot.getType() == Material.WRITTEN_BOOK) {
            List<String> lore = loot.getItemMeta().getLore();
            if (lore != null && lore.get(0).equals("§e§lRun Command:§j§j§j")) {
                for (int i = 1; i < lore.size(); i++) {
                    String command = lore.get(i).substring(3);
                    Location loc = p.getLocation();
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%P%", p.getName()).replace("%W%", loc.getWorld().getName()).replace("%X%", "" + loc.getX()).replace("%Y%", "" + loc.getY()).replace("%Z", "" + loc.getZ()));
                }
                return;
            }
        }
        if (loot.getItemMeta().hasDisplayName() && loot.getItemMeta().getDisplayName().contains(" statistic_item_amount ")) {
            String[] thingy = loot.getItemMeta().getDisplayName().split(" statistic_item_amount ");
            String[] split = thingy[1].split("-");
            int min = Integer.parseInt(split[0]);
            int amount = min + random(Integer.parseInt(split[1]) - min);
            loot.setAmount(amount);
            
            ItemMeta meta = loot.getItemMeta();
            meta.setDisplayName(thingy[0].equals("§j") ? null : thingy[0]);
            loot.setItemMeta(meta);
        }
        MysteryBags.giveItem(p, loot);
    }

}
