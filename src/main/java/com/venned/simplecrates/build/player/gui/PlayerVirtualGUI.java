package com.venned.simplecrates.build.player.gui;

import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class PlayerVirtualGUI {

    UUID UUID;
    Inventory inventory;
    CrateBlock crateBlock;
    CrateVirtual crateVirtual;

    public PlayerVirtualGUI(UUID UUID, Inventory inventory, CrateBlock crateBlock, CrateVirtual crateVirtual) {
        this.UUID = UUID;
        this.inventory = inventory;
        this.crateBlock = crateBlock;
        this.crateVirtual = crateVirtual;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public CrateBlock getCrateBlock() {
        return crateBlock;
    }

    public CrateVirtual getCrateVirtual() {
        return crateVirtual;
    }
}
