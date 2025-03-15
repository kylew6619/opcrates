package com.venned.simplecrates.listeners.crate;

import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.manager.crate.CrateBlockManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerCrateRemoveListener implements Listener {

    CrateBlockManager crateBlockManager;

    public PlayerCrateRemoveListener(CrateBlockManager crateBlockManager) {
        this.crateBlockManager = crateBlockManager;
    }

    @EventHandler
    public void onBreakBlock(final BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if(crateBlockManager.getCrateBlocks().stream().noneMatch(crateBlock -> crateBlock.getLocation().equals(location))) {
            return;
        }
        CrateBlock crateBlock = crateBlockManager.getCrateBlocks().stream().filter(c->c.getLocation().equals(location))
                .findFirst().orElse(null);
        if(crateBlock != null) {
            if(event.getPlayer().isOp() && event.getPlayer().isSneaking()){
                crateBlockManager.removeCrateBlock(location);
                return;
            }
            event.setCancelled(true);
        }
    }
}
