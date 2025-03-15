package com.venned.simplecrates.listeners;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.manager.lootbox.LootBoxManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

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
                        previewRewards.onPreview(event.getPlayer(), lootBox.getRewards(), lootBox.getTitlePreview().replace("&", "§"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
