package com.venned.simplecrates.build.player.gui;

import com.venned.simplecrates.enums.ActionBackPack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class PlayerGUIBackPack {


    UUID UUID;
    Inventory inventory;
    ActionBackPack actionBackPack;

    public PlayerGUIBackPack(Player player, Inventory inventory, ActionBackPack actionBackPack) {
        this.UUID = player.getUniqueId();
        this.inventory = inventory;
        this.actionBackPack = actionBackPack;
    }

    public void setActionBackPack(ActionBackPack actionBackPack) {
        this.actionBackPack = actionBackPack;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ActionBackPack getActionBackPack() {
        return actionBackPack;
    }
}
