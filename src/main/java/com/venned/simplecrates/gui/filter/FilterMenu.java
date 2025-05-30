package com.venned.simplecrates.gui.filter;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemBuilder;
import com.venned.simplecrates.build.LootBox;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.manager.crate.CrateManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class FilterMenu implements Listener {

    static   Map<UUID, Inventory> inventoryMap = new HashMap<>();

    public FilterMenu() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public static void open(Player player) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "config.yml"));

        CrateManager crateManager = Main.getInstance().getCrateManager();
        PlayerData playerData = Main.getInstance().getPlayerManager().getPlayerData(player);

        ConfigurationSection menuSection = config.getConfigurationSection("filter-menu.slots");
        if (menuSection == null) return;

        String title = config.getString("filter-menu.title").replace("&", "§");

        Inventory inv = Bukkit.createInventory(null, 27, title); // 3 rows

        for (String key : menuSection.getKeys(false)) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            ConfigurationSection slotSection = menuSection.getConfigurationSection(key);
            if (slotSection == null) continue;

            String type = slotSection.getString("type", "item").toLowerCase();
            ItemStack item;

            switch (type) {
                case "lootbox" -> {
                    String crateNameItem = slotSection.getString("lootbox");

                    LootBox lootBox = Main.getInstance().getLootBoxManager().getLootBoxes().stream()
                            .filter(p->p.getName().equalsIgnoreCase(crateNameItem)).findFirst().orElse(null);

                    if(lootBox == null) continue;

                    ItemStack itemStack = lootBox.getItem().clone();


                    ItemMeta itemMeta = itemStack.getItemMeta();

                    boolean isDisabled = playerData.getDisabledReward().stream()
                            .anyMatch(i -> i.isSimilar(itemStack));


                    String statusKey = isDisabled ? "DISABLED" : "ENABLED";
                    ConfigurationSection statusSection = config.getConfigurationSection("status-color." + statusKey);
                    String colorName = statusSection != null ? statusSection.getString("color", "GRAY") : "GRAY";
                    boolean isBold = statusSection != null && statusSection.getBoolean("bold", false);


                    ChatColor chatColor;
                    try {
                        chatColor = ChatColor.valueOf(colorName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        chatColor = ChatColor.GRAY;
                    }

                    String status = (isBold ? ChatColor.BOLD : "") + "" + chatColor + statusKey;


                    List<String> lore = slotSection.getStringList("lore").stream()
                            .map(line -> line.replace("{status}", status))
                            .map(line -> line.replace("&", "§"))
                            .toList();


                    itemMeta.setLore(lore);
                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.lootBox, PersistentDataType.STRING, crateNameItem);
                    itemStack.setItemMeta(itemMeta);

                    item = itemStack.clone();
                    break;
                }
                case "crate-item"-> {
                    String crateNameItem = slotSection.getString("crate");
                    ItemStack itemStack = crateManager.getCrateByName(crateNameItem).getItem().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    boolean isDisabled = playerData.getDisabledReward().stream()
                            .anyMatch(i -> i.isSimilar(itemStack));


                    String statusKey = isDisabled ? "DISABLED" : "ENABLED";
                    String colorName = config.getString("status-color." + statusKey, "GRAY");
                    ChatColor chatColor;
                    try {
                        chatColor = ChatColor.valueOf(colorName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        chatColor = ChatColor.GRAY;
                    }

                    String status = chatColor + statusKey;

                    List<String> lore = slotSection.getStringList("lore").stream()
                            .map(line -> line.replace("{status}", status))
                            .map(line -> line.replace("&", "§"))
                            .toList();


                    itemMeta.setLore(lore);
                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, crateNameItem);
                    itemStack.setItemMeta(itemMeta);

                    item = itemStack.clone();
                    break;
                }
                case "crate-key"-> {
                    String crateNameKey = slotSection.getString("crate");

                    ItemStack itemStack = crateManager.getCrateByName(crateNameKey).getItemKey().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();


                    boolean isDisabled = playerData.getDisabledReward().stream()
                            .anyMatch(i -> i.isSimilar(itemStack));

                    String statusKey = isDisabled ? "DISABLED" : "ENABLED";
                    String colorName = config.getString("status-color." + statusKey, "GRAY");
                    ChatColor chatColor;
                    try {
                        chatColor = ChatColor.valueOf(colorName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        chatColor = ChatColor.GRAY;
                    }

                    String status = chatColor + statusKey;

                    List<String> lore = slotSection.getStringList("lore").stream()
                            .map(line -> line.replace("{status}", status))
                            .map(line -> line.replace("&", "§"))
                            .toList();


                    itemMeta.setLore(lore);

                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, crateNameKey);
                    itemStack.setItemMeta(itemMeta);

                    item = itemStack;
                    break;
                }
                default->{
                    String matName = slotSection.getString("material", "STONE");
                    Material material = Material.matchMaterial(matName);
                    if (material == null) material = Material.BARRIER;

                    String name = slotSection.getString("name", "");
                    List<String> lore = slotSection.getStringList("lore");

                    item = new ItemBuilder(material)
                            .setName(name)
                            .setLore(lore)
                            .toItemStack();
                    break;
                    }
            }
            inv.setItem(slot, item);
        }

        inventoryMap.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        Player player = (Player) event.getWhoClicked();
        if (!inventoryMap.containsKey(player.getUniqueId())) return;
        if (!event.getView().getTopInventory().equals(inventoryMap.get(player.getUniqueId()))) return;
        event.setCancelled(true);

        if (event.getCurrentItem() != null) {
            PlayerData playerData = Main.getInstance().getPlayerManager().getPlayerData(player);

            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.key)) {

                String nameCrate = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.key, PersistentDataType.STRING);

                ItemStack key = Main.getInstance().getCrateManager().getCrateByName(nameCrate).getItemKey().clone();

                if (playerData.getDisabledReward().contains(key)) {
                    player.sendMessage(Main.getMessage("filter-remove", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().remove(key);
                    open(player);
                } else {
                    player.sendMessage(Main.getMessage("filter-add", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().add(key);
                    open(player);
                }
            } else if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.crate)) {
                String nameCrate = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.crate, PersistentDataType.STRING);
                ItemStack key = Main.getInstance().getCrateManager().getCrateByName(nameCrate).getItem().clone();

                if (playerData.getDisabledReward().contains(key)) {
                    player.sendMessage(Main.getMessage("filter-remove", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().remove(key);
                    open(player);
                } else {
                    player.sendMessage(Main.getMessage("filter-add", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().add(key);
                    open(player);
                }
            } else if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.lootBox)) {
                String nameCrate = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.lootBox, PersistentDataType.STRING);

                LootBox lootBox = Main.getInstance().getLootBoxManager().getLootBoxes().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(nameCrate)).findFirst().orElse(null);

                if (lootBox == null) return;
                ItemStack key = lootBox.getItem().clone();


                if (playerData.getDisabledReward().contains(key)) {
                    player.sendMessage(Main.getMessage("filter-remove", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().remove(key);
                    open(player);
                } else {
                    player.sendMessage(Main.getMessage("filter-add", Map.of("name", nameCrate)));
                    playerData.getDisabledReward().add(key);
                    open(player);
                }


            }
        }
    }

}
