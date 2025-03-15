package com.venned.simplecrates.gui.edit;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.build.player.PlayerEditChances;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditChances {

    Set<PlayerEditChances> playerEditChances = new HashSet<>();

    public void openInventory(Player player, Opening lootBox){
        Inventory inventory = Bukkit.createInventory(null, 54, "Edit Chances");

        for(int i = 0; i < lootBox.getRewards().size(); i++){

            ItemReward itemReward = lootBox.getRewards().get(i);

            ItemStack itemStack = itemReward.getItemStack().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if(itemMeta.getLore() == null) {
                List<String> lore = new ArrayList<String>();
                lore.add(" ");
                lore.add("§dChance " + itemReward.getChance());
                lore.add("§cCommands: ");
                lore.addAll(itemReward.getCommands());
                lore.add(" ");
                lore.add("§6Left-Click Edit Item");
                lore.add("§6Right-Click Remove Item");
                itemMeta.setLore(lore);
            } else if(itemMeta.getLore() != null || !itemMeta.getLore().isEmpty()){
                List<String> lore = itemMeta.getLore();
                lore.add(" ");
                lore.add("§dChance " + itemReward.getChance());
                lore.add("§cCommands: ");
                lore.addAll(itemReward.getCommands());
                lore.add(" ");
                lore.add("§6Left-Click Edit Item");
                lore.add("§cRight-Click Remove Item");
                itemMeta.setLore(lore);
            }
            itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());

            itemStack.setItemMeta(itemMeta);

            inventory.setItem(i, itemStack);
        }

        playerEditChances.removeIf(p->p.getPlayer().getUniqueId().equals(player.getUniqueId()));

        player.openInventory(inventory);
        PlayerEditChances playerEdit = new PlayerEditChances(player, inventory, lootBox);
        playerEditChances.add(playerEdit);
    }


    public void openInventory(Player player, LootBox lootBox){
        Inventory inventory = Bukkit.createInventory(null, 54, "Edit Chances");

        for(int i = 0; i < lootBox.getRewards().size(); i++){

            ItemReward itemReward = lootBox.getRewards().get(i);

            ItemStack itemStack = itemReward.getItemStack().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if(itemMeta.getLore() == null) {
                List<String> lore = new ArrayList<String>();
                lore.add(" ");
                lore.add("§dChance " + itemReward.getChance());
                lore.add("§cCommands: ");
                lore.addAll(itemReward.getCommands());
                lore.add(" ");
                lore.add("§6Left-Click Edit Item");
                lore.add("§6Right-Click Remove Item");
                itemMeta.setLore(lore);
            } else if(itemMeta.getLore() != null || !itemMeta.getLore().isEmpty()){
                List<String> lore = itemMeta.getLore();
                lore.add(" ");
                lore.add("§dChance " + itemReward.getChance());
                lore.add("§cCommands: ");
                lore.addAll(itemReward.getCommands());
                lore.add(" ");
                lore.add("§6Left-Click Edit Item");
                lore.add("§cRight-Click Remove Item");
                itemMeta.setLore(lore);
            }
            itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());

            itemStack.setItemMeta(itemMeta);

            inventory.setItem(i, itemStack);
        }

        playerEditChances.removeIf(p->p.getPlayer().getUniqueId().equals(player.getUniqueId()));

        player.openInventory(inventory);
        PlayerEditChances playerEdit = new PlayerEditChances(player, inventory, lootBox);
        playerEditChances.add(playerEdit);
    }

    public Set<PlayerEditChances> getPlayerEditChances() {
        return playerEditChances;
    }
}
