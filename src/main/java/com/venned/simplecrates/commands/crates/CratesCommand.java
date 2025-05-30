package com.venned.simplecrates.commands.crates;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CratesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            if(player.isOp()){
                player.sendMessage(ChatColor.RED + "Crates: ");
                for(Crate crates : Main.getInstance().getCrateManager().getCrates()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', crates.getDisplayName()));
                }

            }
        }
        return false;
    }
}
