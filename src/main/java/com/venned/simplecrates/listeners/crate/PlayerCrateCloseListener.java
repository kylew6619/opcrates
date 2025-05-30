package com.venned.simplecrates.listeners.crate;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.player.PlayerOpening;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PlayerCrateCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Set<PlayerOpening> playerOpenings = MapUtils.playerOpenings;
        PlayerOpening playerOpening = playerOpenings.stream()
                .filter(p -> p.getUUID().equals(event.getPlayer().getUniqueId()))
                .findFirst().orElse(null);

        if (playerOpening != null) {
            Player player = (Player) event.getPlayer();
            BukkitTask task = playerOpening.getTask();
            if (task != null) {
                task.cancel();
            }

            int roundsLeft = playerOpening.getRoundLeft();
            List<ItemReward> availableRewards = playerOpening.getRewards();
            List<ItemReward> rewardsWon = new ArrayList<>();

            if (!availableRewards.isEmpty() && roundsLeft > 0) {
                double totalWeight = availableRewards.stream().mapToDouble(ItemReward::getChance).sum();
                Random random = new Random();
                for (int i = 0; i < roundsLeft; i++) {
                    ItemReward reward = getWeightedRandomReward(availableRewards, totalWeight, random);
                    if (reward != null) {
                        rewardsWon.add(reward);

                        PlayerData playerData = Main.getInstance().getPlayerManager().getPlayerData(player);

                        if(playerData == null) return;

                        if(!playerData.getDisabledReward().contains(reward.getItemStack())) {


                            player.getInventory().addItem(reward.getItemStack());
                            for (String command : reward.getCommands()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                            }
                        }
                    }
                }
            }


            PlayerManager playerManager = Main.getInstance().getPlayerManager();

            Crate crate = playerOpening.getCrate();

            List<String> announce = crate.getAnnouncementFinish();
            if (!announce.isEmpty()) {
                List<String> finalMessage = new ArrayList<>();
                for (String line : announce) {
                    if (line.contains("{reward}")) {

                        for (ItemReward rewardAlready : playerOpening.getRewardsAlready()) {
                            finalMessage.add(line.replace("{reward}", rewardAlready.getName()));
                        }

                        for (ItemReward reward : rewardsWon) {
                            finalMessage.add(line.replace("{reward}", reward.getName()));
                        }
                    } else {
                        finalMessage.add(line);
                    }
                }


                if(crate.isAnnounceStatus()) {
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

            PlayerCrateListener.opening.remove(player);
            playerOpenings.remove(playerOpening);
        }
    }

    private ItemReward getWeightedRandomReward(List<ItemReward> rewards, double totalWeight, Random random) {
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