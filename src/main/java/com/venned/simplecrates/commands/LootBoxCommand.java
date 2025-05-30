package com.venned.simplecrates.commands;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.gui.edit.EditChances;
import com.venned.simplecrates.manager.lootbox.LootBoxManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootBoxCommand implements CommandExecutor {

    LootBoxManager manager;
    EditChances editChances;

    public LootBoxCommand(LootBoxManager manager, EditChances editChances) {
        this.manager = manager;
        this.editChances = editChances;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 0) {
                List<String> info = Arrays.asList("&c&lInfo Lootbox",
                        " ",
                        "&d&lSub Commands: ",
                        " ",
                        "&c- &7create <name> <display_name> -> create a lootbox  ",
                        "&c- &7addreward <name> <name_reward> -> add a new reward ",
                        "&c- &7give <name> -> You will get a lootbox in your inventory",
                        "&c- &7edit <name> -> You can edit the lootbox",
                        " ");

                for(String i : info){
                    i = ChatColor.translateAlternateColorCodes('&', i);
                    player.sendMessage(i);
                }

                return true;
            }

            switch (args[0]) {
                case "create" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §7Specify the name and displayName");
                        return true;
                    }
                    String name = args[1];
                    String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName

                    if(manager.getLootBoxes().stream().anyMatch(m->m.getName().equalsIgnoreCase(name))){
                        player.sendMessage("§c§l(!) §cAlready exist lootbox");
                        return true;
                    }
                    LootBox lootBox = new LootBox(name, new ArrayList<>(), displayName);
                    manager.addLootBox(lootBox);
                    player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
                    player.sendMessage("§c§l(!) §aLootBox " + name + " create");
                    break;
                }

                case "reload" -> {
                    if(player.isOp()){
                        player.sendMessage("§c§l(!)  §cReload Success");
                       manager.reloadAll();
                    }
                }
                case "addreward" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §cSpecify the name and reward name");
                        return true;
                    }
                    String name = args[1];
                    LootBox lootBox = manager.getLootBoxes().stream()
                                    .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such lootbox");
                        return true;
                    }

                    String reward = String.join(" ", Arrays.copyOfRange(args, 2, args.length)); // Unir todo el displayName


                    if(lootBox.getRewards().stream().anyMatch(r->r.getName().equalsIgnoreCase(reward))) {
                        player.sendMessage("§c§l(!) §cAlready exist reward");
                        return true;
                    }

                    ItemReward itemReward = new ItemReward(reward, player.getInventory().getItemInMainHand().clone(), 10, new ArrayList<>(), true, new ArrayList<>(), false,
                            "&aYou won ! : {item_reward}");
                    lootBox.addReward(itemReward);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                    player.sendMessage("§c§l(!) §dLootBox " + name + " item added successfully");
                    manager.refreshAll();
                    break;
                }

                case "edit" -> {
                    if(args.length < 2) {
                        player.sendMessage("§c§l(!) §cSpecifies the name of the lootbox");
                        return true;
                    }
                    String name = args[1];
                    LootBox lootBox = manager.getLootBoxes().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such lootbox");
                        return true;
                    }
                    editChances.openInventory(player, lootBox);
                    break;
                }

                case "giveall" -> {
                    if(args.length < 3) {
                        player.sendMessage("§c§l(!) §cSpecify the name and amount");
                        return true;
                    }

                    String name = args[1];
                    LootBox lootBox = manager.getLootBoxes().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such LootBox");
                        return true;
                    }

                    int amount;

                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        return true;
                    }

                    ItemStack loot = lootBox.getItem().clone();
                    loot.setAmount(amount);

                    for(Player players : Bukkit.getOnlinePlayers()){
                        players.getInventory().addItem(loot);
                    }

                }

                case "give" -> {
                    if(args.length < 4) {
                        player.sendMessage("§c§l(!) §cCorrect Usage  /lootbox give {name} {player} {amount}");
                        return true;
                    }
                    String name = args[1];
                    LootBox lootBox = manager.getLootBoxes().stream()
                            .filter(n->n.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                    if(lootBox == null) {
                        player.sendMessage("§c§l(!) §cThere is no such LootBox");
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

                    ItemStack itemStack = lootBox.getItem().clone();
                    itemStack.setAmount(amount);
                    playerFind.getInventory().addItem(itemStack);
                    playerFind.sendMessage("§c§l(!) §dLootBox " + name + " item was given to you");
                    break;
                }
            }

        }

        return false;
    }
}
