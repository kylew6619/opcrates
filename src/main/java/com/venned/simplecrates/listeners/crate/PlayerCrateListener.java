package com.venned.simplecrates.listeners.crate;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.manager.crate.CrateBlockManager;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PlayerCrateListener implements Listener {


    CrateBlockManager crateBlockManager;
    PreviewRewards previewRewards;

    static public Set<Player> opening = new HashSet<Player>();

    public PlayerCrateListener(CrateBlockManager crateBlockManager, PreviewRewards previewRewards) {
        this.crateBlockManager = crateBlockManager;
        this.previewRewards = previewRewards;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null) return;
        Location location = event.getClickedBlock().getLocation();
        if(crateBlockManager.getCrateBlocks().stream().noneMatch(crateBlock -> crateBlock.getLocation().equals(location))) {
            return;
        }
        CrateBlock crateBlock = crateBlockManager.getCrateBlocks().stream().filter(c->c.getLocation().equals(location))
                .findFirst().orElse(null);

        if(crateBlock != null) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
                event.setCancelled(true);
                String title = crateBlock.getCrate().getPreviewTitle().replace("&", "§");

                previewRewards.onPreview(event.getPlayer(), crateBlock.getCrate().getRewards(), title);
            } else if (event.getHand() == EquipmentSlot.HAND) {
                if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if(itemMeta != null) {
                        if (itemMeta.getPersistentDataContainer().has(NameSpaceUtils.key)) {
                            String key = itemMeta.getPersistentDataContainer().get(NameSpaceUtils.key, PersistentDataType.STRING);
                            if(key.equalsIgnoreCase(crateBlock.getCrate().getName())){

                                if(event.getPlayer().isSneaking()){

                                    int amount_keys = itemStack.getAmount();
                                    openAllKeys(event.getPlayer(), crateBlock.getCrate(), amount_keys);

                                    itemStack.setAmount(0);
                                    return;
                                }

                                crateBlock.getCrate().openCrate(event.getPlayer());

                                itemStack.setAmount(itemStack.getAmount() - 1);

                                opening.add(event.getPlayer());
                                return;
                            } else {
                                event.getPlayer().sendMessage(Main.getMessage("no_key_crate", Map.of("crate", crateBlock.getCrate().getDisplayName())));
                                return;
                            }
                        }
                    }
                    event.getPlayer().sendMessage(Main.getMessage("no_key_crate", Map.of("crate", crateBlock.getCrate().getDisplayName())));
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;
        if(opening.contains((Player) event.getWhoClicked())) event.setCancelled(true);
    }

    public void openAllKeys(Player player, Crate crate, int amount){

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
