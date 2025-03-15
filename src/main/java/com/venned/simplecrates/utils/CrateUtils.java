package com.venned.simplecrates.utils;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrateUtils {

    public static void openAllKeys(Player player, Crate crate, int amount){

        for(int z = 0; z < amount; z++) {
            int roundsLeft = crate.getMax_reward();
            List<ItemReward> rewardsG = crate.getRewards();
            List<ItemReward> availableRewards = new ArrayList<>();
            for (ItemReward r : rewardsG) {
                if (!r.getDisabledPlayers().contains(player.getUniqueId())) {
                    availableRewards.add(r);
                }
            }
            List<ItemReward> rewardsWon = new ArrayList<>();

            if (!availableRewards.isEmpty() && roundsLeft > 0) {
                double totalWeight = availableRewards.stream().mapToDouble(ItemReward::getChance).sum();
                Random random = new Random();
                for (int i = 0; i < roundsLeft; i++) {
                    ItemReward reward = getWeightedRandomReward(availableRewards, totalWeight, random);
                    if (reward != null) {
                        rewardsWon.add(reward);
                        player.getInventory().addItem(reward.getItemStack());
                        for (String command : reward.getCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                        }
                    }
                }
            }


            PlayerManager playerManager = Main.getInstance().getPlayerManager();


            List<String> announce = crate.getAnnouncementFinish();
            if (!announce.isEmpty()) {
                List<String> finalMessage = new ArrayList<>();
                for (String line : announce) {
                    if (line.contains("{reward}")) {

                        for (ItemReward reward : rewardsWon) {
                            finalMessage.add(line.replace("{reward}", reward.getName()));
                        }
                    } else {
                        finalMessage.add(line);
                    }
                }


                if (crate.isAnnounceStatus()) {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        PlayerData playerData = playerManager.getPlayerData(players);
                        if (playerData.isNotifiedReward()) {
                            for (String line : finalMessage) {
                                line = line.replace("{player}", player.getName()).replace("{crate}", crate.getName());
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                            }
                        }
                    }
                }
            }
        }



    }

    public static ItemReward getWeightedRandomReward(List<ItemReward> rewards, double totalWeight, Random random) {
        double r = random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (ItemReward reward : rewards) {
            cumulative += reward.getChance();
            if (r <= cumulative) {
                return reward;
            }
        }
        return null;
    }
}
