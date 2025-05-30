package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.BackPack;
import com.venned.simplecrates.build.crate.Crate;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    UUID uuid;
    boolean notifiedReward;
    BackPack backPack;
    boolean autoPickUpKey;

    List<ItemStack> disabledReward;

    public PlayerData(UUID uuid, boolean notifiedReward, boolean autoPickUpKey, List<ItemStack> disabledReward) {
        this.uuid = uuid;
        this.notifiedReward = notifiedReward;
        this.autoPickUpKey = autoPickUpKey;
        this.disabledReward = disabledReward;
    }

    public List<ItemStack> getDisabledReward() {
        return disabledReward;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setAutoPickUpKey(boolean autoPickUpKey) {
        this.autoPickUpKey = autoPickUpKey;
    }

    public void setBackPack(BackPack backPack) {
        this.backPack = backPack;
    }

    public BackPack getBackPack() {
        return backPack;
    }

    public boolean isAutoPickUpKey() {
        return autoPickUpKey;
    }

    public void setNotifiedReward(boolean notifiedReward) {
        this.notifiedReward = notifiedReward;
    }

    public boolean isNotifiedReward() {
        return notifiedReward;
    }
}
