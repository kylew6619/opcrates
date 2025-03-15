package com.venned.simplecrates.listeners.data;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerManager manager = Main.getInstance().getPlayerManager();
        if(manager.getPlayerDatas().stream().noneMatch(u->u.getUUID().equals(event.getPlayer().getUniqueId()))) {
            PlayerData playerData = new PlayerData(event.getPlayer().getUniqueId(), true, false);
            manager.getPlayerDatas().add(playerData);
        }
    }
}
