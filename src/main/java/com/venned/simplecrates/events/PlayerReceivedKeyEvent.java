package com.venned.simplecrates.events;

import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.interfaces.CrateInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerReceivedKeyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack key;
    private final int amount;
    private final CrateInterface crate;
    boolean cancellable;

    public PlayerReceivedKeyEvent(Player player, ItemStack keyType, int amount, CrateInterface crate) {
        this.player = player;
        this.key = keyType;
        this.amount = amount;
        this.crate = crate;
        this.cancellable = false;

    }

    public CrateInterface getCrate() {
        return crate;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getKey() {
        return key;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancellable;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancellable = b;
    }
}
