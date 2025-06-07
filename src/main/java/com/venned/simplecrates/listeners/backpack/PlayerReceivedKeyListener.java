package com.venned.simplecrates.listeners.backpack;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.BackPackKey;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.events.PlayerReceivedKeyEvent;
import com.venned.simplecrates.manager.BackPackConfig;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

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


        if(!backPackConfig.isAutoPickup()) return;


        List<BackPackKey> backPackKeys = backPackConfig.getKeys();

        BackPackKey backPackKey = backPackKeys.stream()
                .filter(p->p.getCrate().equals(event.getCrate())).findFirst().orElse(null);


        if(backPackKey == null){
          //  event.getPlayer().sendMessage("Crate no register in BackPack Config");
            return;
        }


        event.setCancelled(true);

        List<CrateKeyPlayer> crateKeyPlayers = playerData.getBackPack().getCrateKeys();

        CrateKeyPlayer crateKeyPlayer = crateKeyPlayers.stream()
                .filter(k -> k.getCrate().equals(event.getCrate()))
                .findFirst()
                .orElse(null);

        ItemStack clone = event.getKey().clone();
        clone.setAmount(1);


        if(!playerData.getDisabledReward().contains(clone)) {

            if (crateKeyPlayer != null) {

                int depositAmount = event.getAmount();

                int maxDepositBackPack = backPackKey.getAmountMax();
                int currentlyStored = crateKeyPlayer.getKeys();
                int availableSpace = (maxDepositBackPack == -1) ? Integer.MAX_VALUE : maxDepositBackPack - currentlyStored;

                if (availableSpace <= 0) {
                    event.getPlayer().sendMessage(Main.getMessage("backpack-full", Map.of(
                            "crate", crateKeyPlayer.getCrate().getName()
                    )));
                    return;
                }


                int amountToAdd = Math.min(depositAmount, availableSpace);
                crateKeyPlayer.increment(amountToAdd);

                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&aYou received &e" + amountToAdd + " &akey(s) for the crate '&e" + ((Crate) event.getCrate()).getDisplayName() +
                                "&a'. Now you have &e" + crateKeyPlayer.getKeys() + " &akeys."));

                if (depositAmount > amountToAdd) {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cYour key backpack is full. &7Only " + amountToAdd + " out of " + depositAmount + " were added."));

                    clone.setAmount(depositAmount - amountToAdd);
                    event.getPlayer().getInventory().addItem(clone);

                }

            } else {
                String message = "You have received your first crate key'" + ((Crate) event.getCrate()).getDisplayName() + "'.";
                crateKeyPlayers.add(new CrateKeyPlayer(((Crate) event.getCrate()), event.getAmount())); // Agregar nueva clave
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }

    }

}
