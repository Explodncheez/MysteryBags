package cheezbags.api;

import java.io.File;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import cheezbags.MysteryBag;
import cheezbags.MysteryBags;

public class MysteryBagsAPI {
    
    /**
     * @param id id of the bag (the name of the .yml file)
     * @return the MysteryBag object referenced by that id if it exists. Otherwise, returns null
     */
    public static MysteryBag getBagById(String id) {
        return MysteryBags.instance().cheezBags.get(id);
    }

    /**
     * @param id id of the bag (the name of the .yml file)
     * @return the MysteryBag item referenced by that id if it exists. Otherwise, returns null
     */
    public static ItemStack getBagItem(String id) {
        return getBagById(id).getBagItem();
    }
    
    /**
     * @param stack the ItemStack to check.
     * @return Whether or not the ItemStack is a Mystery Bag.
     */
    public static boolean isMysteryBag(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR)
            return false;
        
        List<String> lore = stack.getItemMeta().getLore();
        if (lore != null && lore.size() > 0) {
            String id = lore.get(0).replace("§", "");
            MysteryBag bag = MysteryBags.instance().cheezBags.get(id);
            return bag != null;
        }
        return false;
    }
    
    /**
     * @param bagId id of the bag (the name of the .yml file)
     * @return the .yml File of the bag on the disc, if it exists. To convert it to readable yml form, use YamlConfiguration#loadConfiguration
     */
    public static File getBagConfig(String bagId) {
        return new File(MysteryBags.instance().getDataFolder(), bagId + ".yml");
    }

}
