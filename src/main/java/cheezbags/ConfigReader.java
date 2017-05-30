package cheezbags;

import org.bukkit.entity.EntityType;

public class ConfigReader {
	
	public static boolean getBoolean(String configPath) {
	    if (!MysteryBags.getconfig().isSet(configPath))
	        return false;
	    
	    String data = MysteryBags.getconfig().getString(configPath);
		switch (data.toLowerCase()) {
		case "yes":
		case "yeah":
		case "sure":
		case "true":
		case "why not":
		case "k":
		case "indubitably":
			return true;
			default:
				return false;
		}
	}
	
	public static EntityType getEntityType(String data) {
		try {
			return EntityType.valueOf(data.toUpperCase());
		} catch (Exception e) {
			switch (data.toLowerCase().replace("_", " ")) {
			case "zombie pigman":
			case "zombiepigman":
			case "pigzombie":
				return EntityType.PIG_ZOMBIE;
			case "enderdragon":
				return EntityType.ENDER_DRAGON;
			case "witherboss":
			case "wither boss":
				return EntityType.WITHER;
			case "wither skeleton":
			case "witherskeleton":
				return EntityType.SKELETON;
			case "cavespider":
				return EntityType.CAVE_SPIDER;
			case "magmacube":
			case "magma slime":
			case "magmaslime":
				return EntityType.MAGMA_CUBE;
			default:
				return null;
			}
		}
	}

}
