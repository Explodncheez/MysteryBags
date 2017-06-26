package cheezbags.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import cheezbags.MysteryBag;
import cheezbags.Hand;

public class MysteryBagOpenEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private MysteryBag bag;
    private Hand hand;
    private ItemStack loot;
    private boolean failed, cancelled, c;
 
    /**
     * @param player the player who opened the bag
     * @param hand was the bag in the main or off hand?
     * @param bag the bag object
     * 
     */
    public MysteryBagOpenEvent(Player player, Hand hand, MysteryBag bag, ItemStack loot, boolean failed, boolean consumeItem) {
        this.player = player;
        this.hand = hand;
        this.bag = bag;
        this.loot = loot;
        this.failed = failed;
        this.c = consumeItem;
    }
    
    public ItemStack getLoot() {
        return loot;
    }
    
    public void setLoot(ItemStack newloot) {
        loot = newloot;
    }
    
    public boolean getConsumeItem() {
        return c;
    }
    
    public void setConsumeItem(boolean b) {
        c = b;
    }
    
    public boolean getFailed() {
        return failed;
    }
    
    public void setFailed(boolean b) {
        failed = b;
    }
 
    public Player getPlayer() {
        return player;
    }
    
    public MysteryBag getBag() {
        return bag;
    }
    
    public Hand getHand() {
        return hand;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
 
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
