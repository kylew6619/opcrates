package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.ItemReward;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerOpening {

    UUID uuid;
    BukkitTask task;
    Crate crate;
    List<ItemReward> rewards;
    List<ItemReward> rewardsAlready;
    int roundLeft;

    public PlayerOpening(UUID uuid, Crate crate, List<ItemReward> rewards) {
        this.uuid = uuid;
        this.crate = crate;
        this.rewards = rewards;
        this.rewardsAlready = new ArrayList<>();
    }

    public List<ItemReward> getRewardsAlready() {
        return rewardsAlready;
    }

    public void setRoundLeft(int roundLeft) {
        this.roundLeft = roundLeft;
    }

    public int getRoundLeft() {
        return roundLeft;
    }

    public List<ItemReward> getRewards() {
        return rewards;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public BukkitTask getTask() {
        return task;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Crate getCrate() {
        return crate;
    }
}
