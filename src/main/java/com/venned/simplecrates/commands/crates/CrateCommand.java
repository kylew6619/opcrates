package com.venned.simplecrates.commands.crates;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.events.PlayerReceivedKeyEvent;
import com.venned.simplecrates.gui.edit.EditChances;
import com.venned.simplecrates.manager.crate.CrateBlockManager;
import com.venned.simplecrates.manager.crate.CrateManager;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrateCommand implements CommandExecutor {


    CrateManager manager;
    CrateBlockManager crateBlockManager;
    EditChances editChances;
    PlayerManager playerManager;

    public CrateCommand(CrateManager manager, EditChances editChances, CrateBlockManager crateBlockManager, PlayerManager playerManager) {
        this.manager = manager;
        this.editChances = editChances;
        this.crateBlockManager = crateBlockManager;
        this.playerManager = playerManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 0) {
                List<String> info = Arrays.asList("&c&lInfo Crate",
                        " ",
                        "&d&lSub Commands: ",
                        " ",
                        "&c- &7create <name> <display_name> -> create a lootbox  ",
                        "&c- &7addreward <name> <name_reward> -> add a new reward ",
                        "&c- &7give <name> -> You will get a lootbox in your inventory",
                        "&c- &7givekey <name> -> You will get a key in your inventory",
                        "&c- &7setkey <name> -> You set key in your item main hand",
                        "&c- &7edit <name> -> You can edit the lootbox",
                        "&c- &7keyall <name> <amount> -> Key All Players",
                        " ");

                for(String i : info){
                    i = ChatColor.translateAlternateColorCodes('&', i);
                    player.sendMessage(i);
                }

                return true;
            }

            switch (args[0]) {

                case "notify" -> {
                    PlayerData playerData = playerManager.getPlayerDatas().stream()
                            .filter(c->c.getUUID().equals(player.getUniqueId()))
                            .findFirst().orElse(null);
                    if(playerData != null){

                        boolean status = playerData.isNotifiedReward();

                        if(status){
                            player.sendMessage("Status Notify Disabled");
                            playerData.setNotifiedReward(false);
                        } else {
                            player.sendMessage("Status Notify Enabled");
                            playerData.setNotifiedReward(true);
                        }

                        return true;
                    }
                }

                case "create" -> {
                    if(args.length < 3) {
                        player.sendMessage(Main.getMessage("create_crate_name", Map.of()));
                        return true;
                    }
                    String name = args[1];
                    String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName

                    if(manager.getCrates().stream().anyMatch(m->m.getName().equalsIgnoreCase(name))){
                        player.sendMessage(Main.getMessage("already_exist_crate", Map.of()));
                        return true;
                    }
                    Crate crate = new Crate(name, new ArrayList<>(), displayName);
                    manager.addCrates(crate);
                    player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
                    player.sendMessage(Main.getMessage("crate_success", Map.of("name", name)));
                    break;
                }

                case "setkey" -> {
                    if (args.length < 2) {
                        player.sendMessage(Main.getMessage("no_crate_name_key", Map.of()));
                        return true;
                    }

                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n -> n.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);

                    if (crate == null) {
                        player.sendMessage(Main.getMessage("crate_no_exist", Map.of()));
                        return true;
                    }

                    if(player.getInventory().getItemInMainHand().getType() == Material.AIR){
                        player.sendMessage(Main.getMessage("crate_reward_no_hand", Map.of()));
                        return true;
                    }

                    ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, crate.getName());
                    itemStack.setItemMeta(itemMeta);
                    itemStack.setAmount(1);
                    crate.setItemKey(itemStack);
                    player.sendMessage(Main.getMessage("crate_set_key", Map.of("crate", crate.getName())));
                    manager.refreshAll();

                    return true;
                }


                case "set" -> {
                    if (args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecify the name of the Crate");
                        return true;
                    }

                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n -> n.getName().equalsIgnoreCase(name))
                            .findFirst().orElse(null);

                    if (crate == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    Block targetBlock = player.getTargetBlockExact(5);

                    if (targetBlock == null || targetBlock.getType().isAir()) {
                        player.sendMessage("§c§l(!) §cYou must be looking at a solid block!");
                        return true;
                    }

                    Location location = targetBlock.getLocation();

                    if(crateBlockManager.getCrateBlocks().stream().anyMatch(c->c.getLocation().equals(location))){
                        player.sendMessage("§c§l(!) §cAlready exist hologram in Position");
                        return true;
                    }

                    // Crear y agregar el CrateBlock
                    CrateBlock crateBlock = new CrateBlock(location, crate);
                    crateBlockManager.getCrateBlocks().add(crateBlock);

                    // Mensaje de confirmación
                    player.sendMessage("§aCrate §e" + crate.getName() + " §ahas been set at " +
                            "§7X: " + location.getBlockX() +
                            " §7Y: " + location.getBlockY() +
                            " §7Z: " + location.getBlockZ());

                    return true;
                }

                case "reload" -> {
                    if(player.isOp()){
                        player.sendMessage("§c§l(!)  §cReload Success");
                        manager.reloadAll();
                        crateBlockManager.reloadAll();
                    }
                }




                case "addreward" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §cSpecify the name and reward name");
                        return true;
                    }
                    String name = args[1];
                    Crate crate= manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(crate == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    String reward = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName


                    if(crate.getRewards().stream().anyMatch(r->r.getName().equalsIgnoreCase(reward))) {
                        player.sendMessage("§c§l(!) §cAlready exist reward");
                        return true;
                    }

                    ItemReward itemReward = new ItemReward(reward, player.getInventory().getItemInMainHand().clone(), 10, new ArrayList<>(), true, new ArrayList<>());
                    crate.addReward(itemReward);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                    player.sendMessage("§c§l(!) §dCrate " + name + " item added successfully");
                    manager.refreshAll();
                    break;
                }

                case "edit" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecifies the name of the Crate");
                        return true;
                    }
                    String name = args[1];
                    Crate crate = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(crate== null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }
                    editChances.openInventory(player, crate);
                    break;
                }

                case "give" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecify the name");
                        return true;
                    }
                    String name = args[1];
                    Crate lootBox = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }
                    player.getInventory().addItem(lootBox.getItem());
                    player.sendMessage("§c§l(!) §dCrate " + name + " item was given to you");
                    break;
                }

                case "keyall" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §cSpecify the name and amount");
                        return true;
                    }
                    String name = args[1];
                    Crate lootBox = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    int amount;

                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        return true;
                    }

                    ItemStack itemStack = lootBox.getItemKey().clone();
                    itemStack.setAmount(amount);

                    for(Player players : Bukkit.getOnlinePlayers()){
                        PlayerReceivedKeyEvent playerReceivedKeyEvent = new PlayerReceivedKeyEvent(players, itemStack, amount, lootBox);
                        Bukkit.getServer().getPluginManager().callEvent(playerReceivedKeyEvent);
                        if(playerReceivedKeyEvent.isCancelled()){
                            continue;
                        }
                        players.getInventory().addItem(itemStack);
                    }
                    break;
                }

                case "givekey" -> {
                    if(args.length < 4) {
                        player.sendMessage("§c§l(!) §cCorrect Usage  /crate givekey {crate} {player} {amount}");
                        return true;
                    }
                    String name = args[1];
                    Crate lootBox = manager.getCrates().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such Crate");
                        return true;
                    }

                    Player playerFind = Bukkit.getPlayer(args[2]);
                    if(playerFind == null) {
                        player.sendMessage("§c§l(!) §cPlayer not found");
                        return true;
                    }

                    int amount;
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c§l(!) §cAmount no correct");
                        return true;
                    }

                    ItemStack itemStack = lootBox.getItemKey().clone();
                    itemStack.setAmount(amount);

                    PlayerReceivedKeyEvent playerReceivedKeyEvent = new PlayerReceivedKeyEvent(player, itemStack, amount, lootBox);
                    Bukkit.getServer().getPluginManager().callEvent(playerReceivedKeyEvent);
                    if(playerReceivedKeyEvent.isCancelled()){
                        return true;
                    }
                    playerFind.getInventory().addItem(itemStack);
                    playerFind.sendMessage("§c§l(!) §dCrate Key " + name + " item was given to you");
                    break;
                }
            }

        }

        return false;
    }
}
