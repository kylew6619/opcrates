package com.venned.simplecrates.build;

import com.venned.simplecrates.build.crate.Crate;
import org.bukkit.inventory.ItemStack;

public class BackPackKey {

    Crate crate;
    ItemStack guiItem;
    int amountMax;

    public BackPackKey(Crate crate, ItemStack guiItem, int amountMax) {
        this.crate = crate;
        this.guiItem = guiItem;
        this.amountMax = amountMax;
    }

    public Crate getCrate() {
        return crate;
    }

    public int getAmountMax() {
        return amountMax;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }
}
