package cheezbags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandHandler implements CommandExecutor {
    
    private static final String[] HELP_TEXT = {
        "§2§l<< §aMysteryBags Commands §2§l>>",
        "§e/mbag list - §7Lists the ids of all active bags",
        "§e/mbag addbag <id> [material]:[data] - §7Create a new bag",
        "§e/mbag removebag <id> - §7Remove a bag (does not delete file). Do this before deleting file",
        "§e/mbag spawn <bag id> [amount] [player] - §7Spawns bags",
        "§e/mbag edit <bag id> - §7Open content-editor interface",
        "§e/mbag save <bag id> - §7Save editor changes to config",
        "§e/mbag reload - §7Reload config and bag files",
        "",
        "§e/mbag stack <#> - §7Set the literal amount of your currently held item. Useful for changing the weighted chance of unstackables",
        "§e/mbag command <commmand> - §7Generates an Command Book (Written Book) that can be placed in a MysteryBag and runs a command from console if received. Multiple lines can be added to the same book by holding a Command Book and running this command.",
        "     §fAllowed substitutions (case sensitive):",
        "        §7• §e%P% §7= player's name       • §e%X% %Y% %Z% §7= player's coords",
        "        §7• §e%W% §7= player's world",
        "§e/mbag setamount <# OR #-#> - §7Sets the amount of the held item if received from a MysteryBag. Does not affect chance. Use \"#-#\" for a random amount between two integers.",
        "",
        "§e/mbag setname <name> - §7Sets custom name of held item",
        "§e/mbag setlore <lore> - §7Sets custom lore of held item. Use '/' for new lines",
        "§e/mbag addlore <lore> - §7Appends text to existing lore",
        "§e/mbag removelore <#lines> - §7Removes that many lines from lore",
        "§e/mbag unbreakable - §7Toggles held item unbreakability. §cRequires Spigot",
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (MysteryBags.instance().acceptableCommands.contains(cmd.toLowerCase())) {
            if (sender.hasPermission("mysterybags.admin")) {
                if (args.length > 0) {
                switch (args[0]) {
                    case "spawn":
                        try {
                            if (args.length < 4 && !(sender instanceof Player)) {
                                sender.sendMessage(MysteryBags.PREFIX + "Get off of console to spawn mystery bags for yourself! (/mbags spawn <bag id> [amount] [player])");
                                return true;
                            }
                            Player recipient = args.length > 3 ? Bukkit.getPlayer(args[3]) : (Player) sender;
                            MysteryBag bagobject = MysteryBags.instance().cheezBags.get(args[1]);
                            
                            if (bagobject == null) {
                                sender.sendMessage(MysteryBags.PREFIX + "§7That bag does not exist or is not enabled.");
                                return true;
                            }
                            
                            ItemStack bag = bagobject.getBagItem();
                            int amount = 1;
                            if (args.length > 2)
                                amount = Integer.parseInt(args[2]);
                            bag.setAmount(amount);
                            MysteryBags.giveItem(recipient, bag);
                            sender.sendMessage(MysteryBags.PREFIX + "§eGiven " + amount + " " + args[1] + " bags to " + recipient.getName() + "!");
                        } catch (Exception e) {
                            sender.sendMessage(MysteryBags.PREFIX + "§7Something went wrong with the command! §f/mbags spawn <bag id> [amount] [player]");
                            e.printStackTrace();
                        }
                        return true;
                    case "list":
                        sender.sendMessage("§e<< §6Active MysteryBags: §e>>");
                        for (String s : MysteryBags.instance().cheezBags.keySet())
                            sender.sendMessage("§7• §f" + s);
                        return true;
                    case "edit":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            if (args.length > 1) {
                                MysteryBag bag = MysteryBags.instance().cheezBags.get(args[1]);
                                if (bag == null) {
                                    p.sendMessage(MysteryBags.PREFIX + "§7That bag does not exist.");
                                    return true;
                                }
                                bag.openEditor(p);
                            } else {
                                sender.sendMessage(MysteryBags.PREFIX + "§7Something went wrong with the command! §f/mbags edit <bag id>");
                            }
                        }
                        return true;
                    case "save":
                        if (args.length > 1) {
                            MysteryBag bag = MysteryBags.instance().cheezBags.get(args[1].toLowerCase());
                            if (bag == null) {
                                sender.sendMessage(MysteryBags.PREFIX + "§7That bag does not exist.");
                                return true;
                            }
                            bag.saveConfig();
                            sender.sendMessage(MysteryBags.PREFIX + "§aSuccessfully saved §e" + bag.getId() + "§a's contents! Use §e/mbags reload §ato put these changes into effect.");
                        } else {
                            for (MysteryBag bag : MysteryBags.instance().cheezBags.values())
                                bag.saveConfig();
                            sender.sendMessage(MysteryBags.PREFIX + "§aSuccessfully saved contents of all bags! Use §e/mbags reload §ato put these changes into effect.");
                        }
                        return true;
                    case "addbag":
                        if (args.length > 1) {
                            List<String> list = new ArrayList<String>();
                            list.add("&2Hold item and right click to open!");
                            MysteryBag bag = new MysteryBag(args[1], "CHEST:0", args.length > 2 ? args[2] : "§eTreasure Chest", list, true);
                            sender.sendMessage(MysteryBags.PREFIX + "§aSuccessfully added bag! §aUse §e/mbags edit " + bag.getId() + " §ato edit its contents!");
                        } else {
                            sender.sendMessage(MysteryBags.PREFIX + "§7Something went wrong with the command! §f/mbags addbag <bag id>");
                        }
                        return true;
                    case "removebag":
                        if (args.length > 1) {
                            MysteryBag bag = MysteryBags.instance().cheezBags.get(args[1]);
                            if (bag == null ){
                                sender.sendMessage("§cThat bag does not exist.");
                                return true;
                            }
                            MysteryBags.instance().cheezBags.remove(bag.getId());
                            sender.sendMessage(MysteryBags.PREFIX + "§aSafely removed the §e" + bag.getId() + "§a bag! You may now delete the file from the disc.");
                        } else {
                            sender.sendMessage(MysteryBags.PREFIX + "§7Something went wrong with the command! §f/mbags removebag <bag id>");
                        }
                        return true;
                    case "reload":
                        MysteryBags.instance().load();
                        sender.sendMessage(MysteryBags.PREFIX + "§aReloaded configuration from disc!");
                        return true;
                    case "setname":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            if (args.length < 2) {
                                ItemMeta meta = item.getItemMeta();
                                meta.setDisplayName(null);
                                item.setItemMeta(meta);
                                p.sendMessage(MysteryBags.PREFIX + "§aRemoved the custom name of held item!");
                                return true;
                            }
                            
                            String name = args[1] + " ";
                            for (int i = 2; i < args.length; i++) {
                                name += args[i] + " ";
                            }
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(name.substring(0, name.length() - 1).replace("&", "§"));
                            item.setItemMeta(meta);
                            p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully set the custom name of held item!");
                        }
                        return true;
                    case "setlore":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            if (args.length < 2) {
                                ItemMeta meta = item.getItemMeta();
                                meta.setLore(null);
                                item.setItemMeta(meta);
                                p.sendMessage(MysteryBags.PREFIX + "§aRemoved the custom lore of held item!");
                                return true;
                            }
                            
                            List<String> lore = new ArrayList<String>();
                            String name = args[1] + " ";
                            for (int i = 2; i < args.length; i++) {
                                name += args[i] + " ";
                            }
                            name = name.substring(0, name.length() - 1);
                            for (String part : name.split("/"))
                                lore.add(part.replace("&", "§"));
                            
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully set the custom lore of held item!");
                        }
                        return true;
                    case "addlore":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            ItemMeta meta = item.getItemMeta();
                            
                            if (args.length < 2) {
                                p.sendMessage(MysteryBags.PREFIX + "§7To remove lore from held item, use §e/mbags setlore§c.");
                                return true;
                            }
                            
                            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
                            String name = args[1] + " ";
                            for (int i = 2; i < args.length; i++) {
                                name += args[i] + " ";
                            }
                            name = name.substring(0, name.length() - 1);
                            for (String part : name.split("/"))
                                lore.add(part.replace("&", "§"));
                            
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully added lines to the custom lore of held item!");
                        }
                        return true;
                    case "removelore":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            ItemMeta meta = item.getItemMeta();
                            
                            if (!meta.hasLore()) {
                                p.sendMessage(MysteryBags.PREFIX + "§7This item has no lore.");
                                return true;
                            }
                            
                            int lines;
                            try {
                                lines = Math.max(1, Integer.parseInt(args[1]));
                            } catch (Exception e) {
                                lines = 1;
                            }
                            
                            List<String> lore = new ArrayList<String>();
                            if (lines < meta.getLore().size())
                                for (int i = 0; i < meta.getLore().size() - lines; i++)
                                    lore.add(meta.getLore().get(i));
                        
                            meta.setLore(lore.isEmpty() ? null : lore);
                            item.setItemMeta(meta);
                            p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully removed §e" + lines + "§a line" + (lines == 1 ? "" : "s") + " from the custom lore of held item!");
                        }
                        return true;
                    case "stack":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }

                            int a;
                            try {
                                a = Math.max(1, Integer.parseInt(args[1]));
                            } catch (Exception e) {
                                a = 1;
                            }
                            
                            item.setAmount(a);
                            p.sendMessage(MysteryBags.PREFIX + "§aSet literal stack size of held item to: §e" + a + "§a!");
                            return true;
                        }
                        return true;
                    case "unbreakable":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            
                            try {
                                boolean b;
                                ItemMeta meta = item.getItemMeta();
                                meta.setUnbreakable(b = !meta.isUnbreakable());
                                item.setItemMeta(meta);
                                p.sendMessage(MysteryBags.PREFIX + "§aToggled unbreakability of item to: §e" + b + "§a!");
                            } catch (Exception e) {
                                p.sendMessage(MysteryBags.PREFIX + "§7Your server must be running Spigot to access this command!");
                            }
                        }
                        return true;
                    case "command":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            String storedcmd = "";
                            if (args.length > 1) {
                                for (int c = 1; c < args.length; c++)
                                    storedcmd += args[c] + " ";
                            } else {
                                p.sendMessage(MysteryBags.PREFIX + "§7Please specify a command to store.");
                                return true;
                            }
                            
                            if (storedcmd.charAt(0) == '/')
                                storedcmd = storedcmd.substring(1);
                            
                            ItemStack item;
                            
                            if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK) {
                                ItemStack hand = p.getInventory().getItemInMainHand();
                                ItemMeta hmeta = hand.getItemMeta();
                                item = hmeta.hasLore() && hmeta.getLore().get(0).equals("§e§lRun Command:§j§j§j") ? hand : new ItemStack(Material.WRITTEN_BOOK);
                            } else
                                item = new ItemStack(Material.WRITTEN_BOOK);
                            
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = meta.hasLore() && meta.getLore().get(0).equals("§e§lRun Command:§j§j§j") ? meta.getLore() : new ArrayList<String>();
                            if (lore.isEmpty())
                                lore.add("§e§lRun Command:§j§j§j");
                            lore.add("§c/" + storedcmd.substring(0, storedcmd.length() - 1));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            
                            if (!p.getInventory().getItemInMainHand().equals(item)) {
                                p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully created a Command Book with the §e'" + storedcmd + "'§a command! Add multiple commands by holding the book and using §2/mbag command [command]§a.");
                                p.sendMessage(MysteryBags.PREFIX + "§aRenaming this item will fill the blank of the §2'You received _!' §amessage.");
                                p.getInventory().addItem(item);
                            } else
                                p.sendMessage(MysteryBags.PREFIX + "§aSuccessfully added the §e'" + storedcmd + "'§a command to held Command Book!");
                        }
                        return true;
                    case "amount":
                    case "setamount":
                        if (sender instanceof Player) {
                            Player p = (Player) sender;
                            ItemStack item = p.getInventory().getItemInMainHand();
                            if (item == null) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You aren't holding anything.");
                                return true;
                            }
                            
                            int amount, amount2 = -1;
                            if (args.length > 1) {
                                String[] split = args[1].split("-");
                                try {
                                    amount = Integer.parseInt(split[0]);
                                    if (split.length > 1)
                                        amount2 = Integer.parseInt(split[1]);
                                } catch (Exception e) {
                                    p.sendMessage(MysteryBags.PREFIX + "§7Please specify actual numbers.");
                                    e.printStackTrace();
                                    return true;
                                }
                            } else {
                                p.sendMessage(MysteryBags.PREFIX + "§7Please specify an amount.");
                                return true;
                            }
                            
                            String amountString = amount2 > -1 ? Math.min(amount, amount2) + "-" + Math.max(amount, amount2) : "" + amount;
                            
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();

                            if (MysteryBags.isCommandBook(item)) {
                                p.sendMessage(MysteryBags.PREFIX + "§7You cannot attach an amount to a Command item.");
                                return true;
                            }
                            
                            if (lore.size() > 0 && lore.get(0).startsWith("§c§lAMOUNT: §f"))
                                lore.set(0, "§c§lAMOUNT: §f" + amountString);
                            else
                                lore.add(0, "§c§lAMOUNT: §f" + amountString);
                            
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            p.sendMessage(MysteryBags.PREFIX + "§aThis item will now give §e" + amountString + "§a of it if received from a MysteryBag!");
                        }
                        return true;
                    case "help":
                        for (String s : HELP_TEXT)
                            sender.sendMessage(s);
                        return true;
                    }
                } else {
                    for (String s : HELP_TEXT)
                        sender.sendMessage(s);
                    return true;
                }
            }
        }
        return false;
    }

}
