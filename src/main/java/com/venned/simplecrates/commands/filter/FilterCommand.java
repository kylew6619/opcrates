package com.venned.simplecrates.commands.filter;

import com.venned.simplecrates.gui.filter.FilterMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FilterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player player) {
            FilterMenu.open(player);
        }

        return false;
    }
}
