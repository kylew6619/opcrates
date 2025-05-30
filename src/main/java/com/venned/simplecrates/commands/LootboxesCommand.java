package com.venned.simplecrates.commands;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.build.crate.Crate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LootboxesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            if(player.isOp()){
                player.sendMessage(ChatColor.LIGHT_PURPLE + "LootBoxes: ");
                for(LootBox crates : Main.getInstance().getLootBoxManager().getLootBoxes()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', crates.getDisplayName()));
                }

            }
        }
        return false;
    }
}
