package com.venned.simplecrates.manager.lootbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.LootBox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class LootBoxManager {

    private final Set<LootBox> lootBoxes = new HashSet<>();
    private final File lootBoxFolder;

    public LootBoxManager() {
        lootBoxFolder = new File(Main.getInstance().getDataFolder(), "lootboxes");

        if (!lootBoxFolder.exists()) {
            lootBoxFolder.mkdirs();
        }

        loadLootBoxes();
    }

    public Set<LootBox> getLootBoxes() {
        return lootBoxes;
    }

    public void addLootBox(LootBox lootBox) {
        lootBoxes.add(lootBox);
        saveLootBoxToFile(lootBox);
    }

    public void reloadAll() {
        lootBoxes.clear();
        loadLootBoxes();
    }
    public void refreshAll(){
        saveAll();
    }

    public void saveAll() {
        for (LootBox lootBox : lootBoxes) {
            saveLootBoxToFile(lootBox);
        }
    }

    private void saveLootBoxToFile(LootBox lootBox) {
        File file = new File(lootBoxFolder, lootBox.getName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("name", lootBox.getName());
        config.set("material", lootBox.getMaterial().name());
        config.set("display_name", lootBox.getDisplayName());
        config.set("max_reward", lootBox.getMax_reward());
        config.set("lore", lootBox.getLoreS());
        config.set("titlePreview", lootBox.getTitlePreview());
        config.set("announce_status", lootBox.isAnnounceStatus());
        config.set("announce", lootBox.getAnnouncementFinish());
        config.set("announce_start", lootBox.getAnnouncementStart());

        List<Map<String, Object>> rewardList = new ArrayList<>();
        for (ItemReward reward : lootBox.getRewards()) {
            Map<String, Object> rewardMap = new HashMap<>();
            rewardMap.put("name", reward.getName());
            rewardMap.put("chance", reward.getChance());
            rewardMap.put("item", serializeItemStack(reward.getItemStack()));
            rewardMap.put("commands", reward.getCommands());
            rewardMap.put("visible", reward.isVisible());
            rewardMap.put("glow", reward.isGlow());
            rewardMap.put("message_win", reward.getMessageWon());

            rewardList.add(rewardMap);
        }

        config.set("rewards", rewardList);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLootBoxes() {
        if (!lootBoxFolder.exists()) return;

        File[] files = lootBoxFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            try {
                String name = config.getString("name");
                String displayName = config.getString("display_name", name);
                int maxReward = config.getInt("max_reward");
                Material material = Material.valueOf(config.getString("material", "CHEST"));
                String titlePreview = config.getString("titlePreview", "");
                List<String> lore = config.getStringList("lore");
                List<String> announce = config.getStringList("announce");
                List<String> announceStart = config.getStringList("announce_start");
                boolean announceStatus = config.getBoolean("announce_status");

                List<ItemReward> rewards = new ArrayList<>();
                List<Map<?, ?>> rewardList = config.getMapList("rewards");

                for (Map<?, ?> rewardMap : rewardList) {
                    String rewardName = (String) rewardMap.get("name");
                    double chance = (double) rewardMap.get("chance");
                    List<String> commands = (List<String>) rewardMap.get("commands");
                    ItemStack itemStack = deserializeItemStack((Map<String, Object>) rewardMap.get("item"));
                    boolean visible = (boolean) rewardMap.get("visible");
                    boolean glow = (boolean) rewardMap.get("glow");
                    String messageWin = (String) rewardMap.get("message_win");

                    rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, new ArrayList<>(), glow, messageWin));
                }

                lootBoxes.add(new LootBox(name, rewards, displayName, maxReward, lore, titlePreview, announce, announceStart, announceStatus, material));

            } catch (Exception e) {
                System.err.println("Failed to load lootbox file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        if (item == null) return null;
        Map<String, Object> data = new HashMap<>(item.serialize());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            Map<String, Object> metaData = new HashMap<>(meta.serialize());

            if (meta.hasDisplayName()) {
                metaData.put("display-name", meta.getDisplayName().replace("§", "&"));
            }

            if (meta.hasLore()) {
                List<String> formattedLore = meta.getLore().stream().map(l -> l.replace("§", "&")).collect(Collectors.toList());
                metaData.put("lore", formattedLore);
            }

            data.put("meta", metaData);

        }

        data.remove("v");

        return data;
    }

    private ItemStack deserializeItemStack(Map<String, Object> data) {
        if (data == null) return null;


        data.put("v", Bukkit.getUnsafe().getDataVersion());

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


            if (metaData.containsKey("PublicBukkitValues")) {
                Object publicBukkitValuesRaw = metaData.get("PublicBukkitValues");
                if (publicBukkitValuesRaw instanceof String) {
                    try {
                        JsonObject publicBukkitJson = JsonParser.parseString((String) publicBukkitValuesRaw).getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : publicBukkitJson.entrySet()) {
                            String keyRaw = entry.getKey(); // e.g. "simplecrates:key"
                            JsonElement valueElement = entry.getValue();

                            String[] namespaceSplit = keyRaw.split(":");
                            if (namespaceSplit.length == 2) {
                                System.out.println("Colocndo meta " + namespaceSplit[0]  + namespaceSplit[1]);
                                NamespacedKey key = new NamespacedKey(namespaceSplit[0], namespaceSplit[1]);
                                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, valueElement.getAsString());
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Error parsing PublicBukkitValues: " + publicBukkitValuesRaw);
                    }
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}