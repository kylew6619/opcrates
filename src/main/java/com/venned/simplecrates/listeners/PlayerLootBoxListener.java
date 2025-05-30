package com.venned.simplecrates.listeners;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.manager.lootbox.LootBoxManager;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayerLootBoxListener implements Listener {

    LootBoxManager lootBoxManager;
    PreviewRewards previewRewards;

    public PlayerLootBoxListener(LootBoxManager lootBoxManager, PreviewRewards previewRewards){
        this.lootBoxManager = lootBoxManager;
        this.previewRewards = previewRewards;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getItem().getType() != Material.AIR) {

                    LootBox lootBox = lootBoxManager.getLootBoxes().stream()
                            .filter(n -> n.isLootBox(event.getItem()))
                            .findFirst().orElse(null);
                    if (lootBox != null) {

                        if(event.getPlayer().isSneaking()){

                            int amount_keys = event.getItem().getAmount();
                            openAllKeys(event.getPlayer(), lootBox, amount_keys);

                            event.getItem().setAmount(0);
                            return;
                        }

                        event.getPlayer().sendMessage(Main.getMessage("opening-lootbox",  Map.of("lootbox", lootBox.getDisplayName())));
                        lootBox.open(event.getPlayer());
                        event.setCancelled(true);
                    }

                }
            } else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (event.getItem().getType() != Material.AIR) {
                    LootBox lootBox = lootBoxManager.getLootBoxes().stream()
                            .filter(n -> n.isLootBox(event.getItem()))
                            .findFirst().orElse(null);
                    if (lootBox != null) {
                        previewRewards.onPreview(event.getPlayer(), lootBox.getRewards(), lootBox.getTitlePreview().replace("&", "§"), PreviewRewards.TypePreview.LOOTBOX, lootBox);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void openAllKeys(Player player, LootBox crate, int amount){

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
