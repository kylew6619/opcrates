package com.venned.simplecrates.commands;

import com.venned.simplecrates.manager.lootbox.LootBoxManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LootBoxTabCompleter implements TabCompleter {

    private final LootBoxManager manager;

    public LootBoxTabCompleter(LootBoxManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("addreward");
            completions.add("give");
            completions.add("edit");
            return completions;
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            completions.addAll(manager.getLootBoxes().stream()
                    .map(lootBox -> lootBox.getName())
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}

