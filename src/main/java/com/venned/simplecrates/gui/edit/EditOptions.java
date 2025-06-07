package com.venned.simplecrates.gui.edit;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.gui.PlayerEditChances;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EditOptions {


    public void openInventory(Player player, ItemReward itemReward, PlayerEditChances editChances) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Edit Item");

        ItemStack backHome = new ItemStack(Material.CHEST);
        ItemMeta backHomeMeta = backHome.getItemMeta();
        if (backHomeMeta != null){
            backHomeMeta.setDisplayName("§aReturn Menu");
            backHomeMeta.setLore(Arrays.asList("§7Back to Main Menu"));
            backHome.setItemMeta(backHomeMeta);
        }

        // Botón para editar chance
        ItemStack editChance = new ItemStack(Material.PAPER);
        ItemMeta chanceMeta = editChance.getItemMeta();
        if (chanceMeta != null) {
            chanceMeta.setDisplayName("§aEdit Chance");
            chanceMeta.setLore(Arrays.asList("§7Current: " + itemReward.getChance() + "%", "§eClick to Edit"));
            editChance.setItemMeta(chanceMeta);
        }

        // Botón para eliminar comandos
        ItemStack removeCommands = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeCommands.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("§cDelete All Command");
            removeMeta.setLore(Arrays.asList("§7Click to delete"));
            removeCommands.setItemMeta(removeMeta);
        }

        // Botón para agregar comandos
        ItemStack addCommands = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta addMeta = addCommands.getItemMeta();
        if (addMeta != null) {
            addMeta.setDisplayName("§eAdd Command");
            addMeta.setLore(Arrays.asList("§7Click to add command"));
            addCommands.setItemMeta(addMeta);
        }

        ItemStack removeItem = new ItemStack(Material.REDSTONE);
        ItemMeta itemMeta = removeItem.getItemMeta();
        if (itemMeta != null){
            itemMeta.setDisplayName("§cChange Visible");
            itemMeta.setLore(Arrays.asList("§7Current Visibility: " + itemReward.isVisible()));
            removeItem.setItemMeta(itemMeta);
        }

        inventory.setItem(0, editChance);
        inventory.setItem(2, removeCommands);
        inventory.setItem(4, addCommands);
        inventory.setItem(6, removeItem);
        inventory.setItem(8, backHome);

        player.openInventory(inventory);
        editChances.setInventory(inventory);

    }

}
