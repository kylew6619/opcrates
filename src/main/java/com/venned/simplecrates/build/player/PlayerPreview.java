package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.interfaces.Opening;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class PlayerPreview {

    Inventory inventory;
    List<ItemReward> rewardList;
    UUID uuid;
    String title;
    PreviewRewards.TypePreview typePreview;
    Opening opening;

    public PlayerPreview(Inventory inventory, List<ItemReward> rewardList, UUID uuid, String title, PreviewRewards.TypePreview typePreview, Opening opening) {
        this.inventory = inventory;
        this.rewardList = rewardList;
        this.uuid = uuid;
        this.title = title;
        this.typePreview = typePreview;
        this.opening = opening;
    }

    public Opening getOpening() {
        return opening;
    }

    public void setTypePreview(PreviewRewards.TypePreview typePreview) {
        this.typePreview = typePreview;
    }

    public PreviewRewards.TypePreview getTypePreview() {
        return typePreview;
    }

    public String getTitle() {
        return title;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<ItemReward> getRewardList() {
        return rewardList;
    }
}
