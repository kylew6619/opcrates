package com.venned.simplecrates.manager.lootbox;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class LootBoxManager {

    private final Set<LootBox> lootBoxes = new HashSet<>();
    private final File lootBoxFile;
    private FileConfiguration lootBoxConfig;

    public LootBoxManager() {
        lootBoxFile = new File(Main.getInstance().getDataFolder(), "lootbox.yml");

        if (!lootBoxFile.exists()) {
            try {
                lootBoxFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lootBoxConfig = YamlConfiguration.loadConfiguration(lootBoxFile);
        loadLootBoxes();
    }

    public Set<LootBox> getLootBoxes() {
        return lootBoxes;
    }

    public void addLootBox(LootBox lootBox) {
        lootBoxes.add(lootBox);
        saveLootBoxes();
    }

    public void reloadAll(){
        lootBoxConfig = YamlConfiguration.loadConfiguration(lootBoxFile);

        loadLootBoxes();
    }

    public void refreshAll(){
        saveLootBoxes();
    }

    public void saveLootBoxes() {
        lootBoxConfig.set("lootboxes", null);

        for (LootBox lootBox : lootBoxes) {
            String path = "lootboxes." + lootBox.getName();
            lootBoxConfig.set(path + ".name", lootBox.getName());
            lootBoxConfig.set(path + ".display_name", lootBox.getDisplayName());
            lootBoxConfig.set(path + ".max_reward", lootBox.getMax_reward());
            lootBoxConfig.set(path + ".lore", lootBox.getLoreS());
            lootBoxConfig.set(path + ".titlePreview", lootBox.getTitlePreview());
            lootBoxConfig.set(path + ".announce_status", lootBox.isAnnounceStatus());
            lootBoxConfig.set(path + ".announce", lootBox.getAnnouncementFinish());
            lootBoxConfig.set(path + ".announce_start", lootBox.getAnnouncementStart());



            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemReward reward : lootBox.getRewards()) {
                Map<String, Object> rewardMap = new HashMap<>();
                rewardMap.put("name", reward.getName());
                rewardMap.put("chance", reward.getChance());
                rewardMap.put("item", serializeItemStack(reward.getItemStack()));
                rewardMap.put("commands", reward.getCommands());
                rewardMap.put("visible", reward.isVisible());

                rewardList.add(rewardMap);
            }

            lootBoxConfig.set(path + ".rewards", rewardList);
        }

        try {
            lootBoxConfig.save(lootBoxFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLootBoxes() {
        lootBoxes.clear();

        if (!lootBoxConfig.contains("lootboxes")) return;

        ConfigurationSection section = lootBoxConfig.getConfigurationSection("lootboxes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");

            String displayName = section.getString(key + ".display_name");
            if(displayName == null){
                displayName = name;
            }

            int max_reward = section.getInt(key + ".max_reward");

            String titlePreview = section.getString(key + ".titlePreview");

            List<String> lore = section.getStringList(key + ".lore");

            List<String> announce = section.getStringList(key + ".announce");

            boolean announceStatus = section.getBoolean(key + ".announce_status");

            List<String> announceStart = section.getStringList(key + ".announce_start");

            List<ItemReward> rewards = new ArrayList<>();
            List<Map<?, ?>> rewardList = section.getMapList(key + ".rewards");

            for (Map<?, ?> rewardMap : rewardList) {
                String rewardName = (String) rewardMap.get("name");
                double chance = (double) rewardMap.get("chance");

                List<String> commands = (List<String>) rewardMap.get("commands");
                ItemStack itemStack = deserializeItemStack((Map<String, Object>) rewardMap.get("item"));

                boolean visible = (boolean) rewardMap.get("visible");
                rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, new ArrayList<>()));
            }

            lootBoxes.add(new LootBox(name, rewards, displayName, max_reward, lore, titlePreview, announce, announceStart, announceStatus));
        }
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        if (item == null) return null;

        Map<String, Object> data = new HashMap<>(item.serialize()); // Convertir a HashMap para modificar

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            Map<String, Object> metaData = new HashMap<>(meta.serialize()); // Convertir a HashMap para modificar

            if (meta.hasDisplayName()) {
                metaData.put("display-name", meta.getDisplayName().replace("§", "&"));
            }

            if (meta.hasLore()) {
                List<String> formattedLore = meta.getLore().stream()
                        .map(lore -> lore.replace("§", "&"))
                        .collect(Collectors.toList());
                metaData.put("lore", formattedLore);
            }

            data.put("meta", metaData); // Reemplazar los metadatos en la estructura original
        }

        return data;
    }


    private ItemStack deserializeItemStack(Map<String, Object> data) {
        if (data == null) return null;

        ItemStack item = ItemStack.deserialize(data);

        if (data.containsKey("meta")) {
            Map<String, Object> metaData = (Map<String, Object>) data.get("meta");
            ItemMeta meta = item.getItemMeta();

            if (metaData.containsKey("display-name")) {
                meta.setDisplayName(((String) metaData.get("display-name")).replace("&", "§"));
            }

            if (metaData.containsKey("lore")) {
                List<String> formattedLore = ((List<String>) metaData.get("lore")).stream()
                        .map(lore -> lore.replace("&", "§"))
                        .collect(Collectors.toList());
                meta.setLore(formattedLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
