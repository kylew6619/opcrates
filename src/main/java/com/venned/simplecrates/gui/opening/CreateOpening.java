package com.venned.simplecrates.gui.opening;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.player.PlayerOpening;
import com.venned.simplecrates.listeners.crate.PlayerCrateListener;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateOpening {

    private static final Material[] GLASS_PANELS = {
            Material.RED_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE
    };

    public static void open(Player player, Crate crate) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&6Opening " + crate.getName()));
        player.openInventory(inventory);

        MapUtils.playerOpenings.removeIf(p->p.getUUID().equals(player.getUniqueId()));


        List<ItemReward> rewardsG = crate.getRewards();
        List<ItemReward> rewards = new ArrayList<>();
        for(ItemReward r : rewardsG){
            if(!r.getDisabledPlayers().contains(player.getUniqueId())){
                rewards.add(r);
            }
        }

        PlayerOpening playerOpening = new PlayerOpening(player.getUniqueId(), crate, rewards);
        playerOpening.setRoundLeft(crate.getMax_reward());
        MapUtils.playerOpenings.add(playerOpening);

        int duration = Main.getInstance().getConfig().getInt("duration-crate-opening");

        List<ItemReward> rewardsWin = new ArrayList<>();

        double totalWeight = rewards.stream().mapToDouble(ItemReward::getChance).sum();
        Random random = new Random();

        BukkitTask task =  new BukkitRunnable() {
            int spinsLeft = duration * 20;  // Siempre 10 giros por cada recompensa
            int roundsLeft = crate.getMax_reward(); // Veces que se repite el proceso

            @Override
            public void run() {

                if (!player.isOnline()) {
                    PlayerCrateListener.opening.remove(player);
                    cancel();
                    return;
                }

                if (spinsLeft <= 0) {

                    ItemStack wonItem = inventory.getItem(13);
                    ItemReward itemReward = rewards.stream()
                            .filter(p->p.getItemStack().isSimilar(wonItem))
                            .findFirst().orElse(null);

                    if (wonItem != null && wonItem.getType() != Material.AIR) {
                        String itemName;

                        if (wonItem.getItemMeta() != null && wonItem.getItemMeta().hasDisplayName()) {
                            itemName = wonItem.getItemMeta().getDisplayName();
                        } else {
                            itemName = wonItem.getType().toString().replace("_", " ").toLowerCase();
                        }

                        player.getInventory().addItem(wonItem);
                        player.sendMessage(Main.getMessage("crate_win_item", Map.of("item_reward", itemReward.getName())));
                        rewardsWin.add(itemReward);
                        playerOpening.getRewardsAlready().add(itemReward);

                        if (!itemReward.getCommands().isEmpty()) {
                            for (String command : itemReward.getCommands()) {
                                command = command.replace("{player}", player.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        }
                    }

                    roundsLeft--;
                    playerOpening.setRoundLeft(roundsLeft);

                    if (roundsLeft <= 0) {
                        PlayerCrateListener.opening.remove(player);
                        MapUtils.playerOpenings.removeIf(p->p.getUUID().equals(player.getUniqueId()));
                        player.closeInventory();
                        cancel();


                        List<String> announce = crate.getAnnouncementFinish();
                        if (!announce.isEmpty()) {
                            List<String> finalMessage = new ArrayList<>();
                            for (String line : announce) {
                                if (line.contains("{reward}")) {
                                    for (ItemReward reward : rewardsWin) {
                                        finalMessage.add(line.replace("{reward}", reward.getName()));
                                    }
                                } else {
                                    finalMessage.add(line);
                                }
                            }

                            PlayerManager playerManager = Main.getInstance().getPlayerManager();

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
                        return;
                    }
                    spinsLeft = duration * 20;
                }

                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, getRandomGlassPanel(random));
                    inventory.setItem(i + 18, getRandomGlassPanel(random));
                }

                // Poner ítems aleatorios en la fila del medio, con probabilidad basada en su chance
                for (int i = 9; i < 18; i++) {
                    inventory.setItem(i, getWeightedRandomItem(rewards, totalWeight, random));
                }

                // Reproducir sonido en cada giro
                player.playSound(player.getLocation(), Sound.UI_LOOM_TAKE_RESULT, 0.8f, 1.2f);

                spinsLeft--; // Reducir cantidad de giros restantes en esta ronda
            }
        }.runTaskTimer(Main.getInstance(), 0L, 2L);

        playerOpening.setTask(task);
    }

    private static ItemStack getWeightedRandomItem(List<ItemReward> rewards, double totalWeight, Random random) {
        double r = random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (ItemReward reward : rewards) {
            cumulative += reward.getChance();
            if (r <= cumulative) {
                return reward.getItemStack();
            }
        }
        return new ItemStack(Material.BARRIER); // Fallback en caso de error
    }

    private static ItemStack getRandomGlassPanel(Random random) {
        return new ItemStack(GLASS_PANELS[random.nextInt(GLASS_PANELS.length)]);
    }
}