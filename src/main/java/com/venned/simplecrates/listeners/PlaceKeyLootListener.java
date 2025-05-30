package com.venned.simplecrates.listeners;

import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceKeyLootListener implements Listener {


    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if(item.getItemMeta() == null) return;
        if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.key)){
            event.setCancelled(true);
        } else if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.crate)){
            event.setCancelled(true);
        }
    }

}
