package com.venned.simplecrates.listeners.backpack;

import com.venned.simplecrates.build.BackPackKey;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.events.PlayerReceivedKeyEvent;
import com.venned.simplecrates.manager.BackPackConfig;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PlayerReceivedKeyListener implements Listener {


    PlayerManager playerManager;
    BackPackConfig backPackConfig;

    public PlayerReceivedKeyListener(PlayerManager playerManager, BackPackConfig backPackConfig) {
        this.playerManager = playerManager;
        this.backPackConfig = backPackConfig;
    }

    @EventHandler
    public void onReceived(PlayerReceivedKeyEvent event) {

        PlayerData playerData = playerManager.getPlayerData(event.getPlayer());
        if(playerData.getBackPack() == null)return;
        if(!playerData.isAutoPickUpKey()) return;

        List<BackPackKey> backPackKeys = backPackConfig.getKeys();
        BackPackKey backPackKey = backPackKeys.stream()
                .filter(p->p.getCrate().equals(event.getCrate())).findFirst().orElse(null);
        if(backPackKey == null){
            event.getPlayer().sendMessage("Crate no register in BackPack Config");
            return;
        }

        event.setCancelled(true);

        List<CrateKeyPlayer> crateKeyPlayers = playerData.getBackPack().getCrateKeys();

        CrateKeyPlayer crateKeyPlayer = crateKeyPlayers.stream()
                .filter(k -> k.getCrate().equals(event.getCrate()))
                .findFirst()
                .orElse(null);

        if (crateKeyPlayer != null) {
            crateKeyPlayer.increment(1);
            event.getPlayer().sendMessage("You have received an extra key for the crate '" + event.getCrate().getDisplayName() + "'. Now you have " + crateKeyPlayer.getKeys() + " keys.");
        } else {
            crateKeyPlayers.add(new CrateKeyPlayer(event.getCrate(), 1)); // Agregar nueva clave
            event.getPlayer().sendMessage("You have received your first crate key'" + event.getCrate().getDisplayName() + "'.");
        }

    }

}
