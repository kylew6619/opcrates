package com.venned.simplecrates.commands.backpack;

import com.venned.simplecrates.build.BackPack;
import com.venned.simplecrates.build.BackPackKey;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.gui.backpack.BackPackGUI;
import com.venned.simplecrates.manager.BackPackConfig;
import com.venned.simplecrates.manager.crate.CrateManager;
import com.venned.simplecrates.manager.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BackPackCommand implements CommandExecutor {

    PlayerManager playerManager;
    BackPackConfig backPackConfig;
    CrateManager crateManager;
    BackPackGUI backPackGUI;

    public BackPackCommand(PlayerManager playerManager, BackPackConfig backPackConfig, CrateManager crateManager, BackPackGUI backPackGUI) {
        this.playerManager = playerManager;
        this.backPackConfig = backPackConfig;
        this.crateManager = crateManager;
        this.backPackGUI = backPackGUI;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {

            if(args.length == 0) {
                PlayerData playerData = playerManager.getPlayerData(player);
                if(playerData == null) return true;
                if(playerData.getBackPack() == null){
                    player.sendMessage("You don't have a backpack");
                    return true;
                }
                backPackGUI.openInventory(player);
                return true;
            }

            if(!player.isOp()) return true;

            switch (args[0]){
                case "reload":
                    if(!player.isOp()) return true;
                    backPackConfig.reloadConfig();
                    return true;
                case "give":
                    if(!player.isOp()) return true;
                    if(args.length  < 2) {
                        player.sendMessage("Correct /backpack give <name>");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null) {
                        player.sendMessage("Player not found");
                        return true;
                    }

                    PlayerData playerData = playerManager.getPlayerData(target);
                    if(playerData == null) return true;
                    if(playerData.getBackPack() != null){
                        player.sendMessage("Already BackPack");
                        return true;
                    }
                    playerData.setBackPack(new BackPack(new ArrayList<>()));
                    return true;
                /*
                case "toggle":
                    PlayerData playerData15 = playerManager.getPlayerData(player);
                    if(playerData15 == null) return true;
                    if(playerData15.isAutoPickUpKey()){
                        playerData15.setAutoPickUpKey(false);
                        player.sendMessage("AutoPickUp Disabled");
                    } else {
                        playerData15.setAutoPickUpKey(true);
                        player.sendMessage("AutoPickUp Enabled");
                    }

                    return true;

                case "withdraw":
                    if(args.length < 3){
                        player.sendMessage("/backpack withdraw <type> <amount>");
                        return true;
                    }

                    PlayerData playerData5 = playerManager.getPlayerData(player);
                    if(playerData5 == null) return true;
                    if(playerData5.getBackPack() == null){
                        player.sendMessage("You no have backpack");
                        return true;
                    }

                    int amountWithdraw;

                    try {
                        amountWithdraw = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("No correct number");
                        return true;
                    }

                    String name2 = args[1];

                    Crate crate2 = crateManager.getCrateByName(name2);

                    if(crate2 == null){
                        player.sendMessage("Crate no exist");
                        return true;
                    }

                    List<BackPackKey> backPackKeys2 = backPackConfig.getKeys();
                    BackPackKey backPackKey2 = backPackKeys2.stream()
                            .filter(p->p.getCrate().equals(crate2)).findFirst().orElse(null);
                    if(backPackKey2 == null){
                        player.sendMessage("Crate no register in BackPack Config");
                        return true;
                    }
                    List<CrateKeyPlayer> crateKeyPlayers2 = playerData5.getBackPack().getCrateKeys();

                    CrateKeyPlayer crateKeyPlayer2 = crateKeyPlayers2.stream()
                            .filter(k -> k.getCrate().equals(crate2))
                            .findFirst()
                            .orElse(null);

                    if(crateKeyPlayer2 == null){
                        player.sendMessage("No contains crate in BackPack");
                        return true;
                    }

                    int current_keys = crateKeyPlayer2.getKeys();

                    if(current_keys == 0){
                        player.sendMessage("No contains crate in BackPack");
                        return true;
                    }

                    crateKeyPlayer2.desIncrement(amountWithdraw);

                    ItemStack keyGet = crateKeyPlayer2.getCrate().getItemKey().clone();
                    keyGet.setAmount(amountWithdraw);

                    player.getInventory().addItem(keyGet);

                    player.sendMessage("Withdraw Key (" + amountWithdraw + ")");



                    return true;



                case "give":
                    if(!player.isOp()) return true;
                    if(args.length < 3){
                        player.sendMessage("/backpack give <player> <type>");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);

                    if(target == null){
                        player.sendMessage("Player no online");
                        return true;
                    }

                    PlayerData playerData2 = playerManager.getPlayerData(target);
                    if(playerData2 == null) return true;
                    if(playerData2.getBackPack() == null){
                        player.sendMessage("No  hava backpack");
                        return true;
                    }

                    String name = args[2];

                    Crate crate = crateManager.getCrateByName(name);

                    if(crate == null){
                        player.sendMessage("Crate no exist");
                        return true;
                    }

                    List<BackPackKey> backPackKeys = backPackConfig.getKeys();
                    BackPackKey backPackKey = backPackKeys.stream()
                            .filter(p->p.getCrate().equals(crate)).findFirst().orElse(null);
                    if(backPackKey == null){
                        player.sendMessage("Crate no register in BackPack Config");
                        return true;
                    }
                    List<CrateKeyPlayer> crateKeyPlayers = playerData2.getBackPack().getCrateKeys();

                    CrateKeyPlayer crateKeyPlayer = crateKeyPlayers.stream()
                            .filter(k -> k.getCrate().equals(crate))
                            .findFirst()
                            .orElse(null);

                    if (crateKeyPlayer != null) {
                        crateKeyPlayer.increment(1);
                        target.sendMessage("You have received an extra key for the crate '" + name + "'. Now you have " + crateKeyPlayer.getKeys() + " keys.");
                    } else {
                        crateKeyPlayers.add(new CrateKeyPlayer(crate, 1)); // Agregar nueva clave
                        target.sendMessage("You have received your first crate key'" + name + "'.");
                    }

                    return true;

                 */


            }
        }


        return false;
    }
}
