package cheezbags.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class MysteryBagDropEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Entity killed;
    private ItemStack bag;
    private boolean cancelled;
 
    public MysteryBagDropEvent(Player player, Entity killed, ItemStack bag) {
        this.player = player;
        this.killed = killed;
        this.bag = bag;
    }
 
    public Player getPlayer() {
        return player;
    }
    
    public Entity getKilledEntity() {
        return killed;
    }
    
    public ItemStack getItem() {
        return bag;
    }
    
    public void setItem(ItemStack item) {
        bag = item;
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
