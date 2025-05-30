package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.enums.EditMode;
import com.venned.simplecrates.interfaces.Opening;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PlayerEditChances {


    Inventory inventory;
    Player player;
    ItemReward currenEdit;
    EditMode editMode;
    Opening crate;

    public PlayerEditChances(Player player, Inventory inventory, Opening lootBox) {
        this.player = player;
        this.inventory = inventory;
        this.crate = lootBox;
    }

    public Opening getCrate() {
        return crate;
    }

    public void setCurrenEdit(ItemReward currenEdit) {
        this.currenEdit = currenEdit;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ItemReward getCurrenEdit() {
        return currenEdit;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Opening getLootBox() {
        return crate;
    }
}
