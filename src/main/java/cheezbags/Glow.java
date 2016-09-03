package cheezbags;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Glow {
	
	public static void add(ItemStack item) {
		item.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
	
	public static boolean has(ItemStack item) {
		return item.containsEnchantment(Enchantment.ARROW_DAMAGE) && item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);
	}
	
	public static void remove(ItemStack item) {
		item.removeEnchantment(Enchantment.ARROW_DAMAGE);
	}
 
}