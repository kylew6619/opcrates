package com.venned.simplecrates.interfaces;

import com.venned.simplecrates.build.ItemReward;
import org.bukkit.entity.Player;

import java.util.List;

public interface CrateInterface {

    List<String> getHologramText();

    String getDisplayName();

    String getName();

    String getPreviewTitle();

    List<ItemReward> getRewards();

    void openCrate(Player player);
}
