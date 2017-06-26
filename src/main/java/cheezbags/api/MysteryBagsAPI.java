package cheezbags.api;

import java.io.File;

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
     * @param bagId id of the bag (the name of the .yml file)
     * @return the .yml File of the bag on the disc, if it exists. To convert it to readable yml form, use YamlConfiguration#loadConfiguration
     */
    public static File getBagConfig(String bagId) {
        return new File(MysteryBags.instance().getDataFolder(), bagId + ".yml");
    }

}
