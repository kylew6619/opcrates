package com.venned.simplecrates.listeners.virtual;

import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import com.venned.simplecrates.gui.opening.CrateVirtualOpening;
import com.venned.simplecrates.utils.MapUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void executeCommand(PlayerCommandPreprocessEvent event){
        if(MapUtils.playerOpenVirtual.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(MapUtils.playerOpenVirtual.containsKey(e.getPlayer().getUniqueId())) {
            CrateBlock crateBlock = MapUtils.playerOpenVirtual.get(e.getPlayer().getUniqueId());
            if(crateBlock.getChestUsed().size() == 1 || crateBlock.getChestUsed().isEmpty()){
                CrateVirtual lootBox = (CrateVirtual) crateBlock.getCrate();
                int current = lootBox.getOwned().getOrDefault(e.getPlayer().getUniqueId(), 0);
                lootBox.getOwned().put(e.getPlayer().getUniqueId(), current + 1);
            }

            CrateVirtualOpening.stop(crateBlock, e.getPlayer());
        }
    }

}
