package com.venned.simplecrates.manager.crate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.ItemReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CrateManager {

    private final Set<Crate> crates = new HashSet<>();
    private final File cratesFolder;

    public CrateManager() {
        cratesFolder = new File(Main.getInstance().getDataFolder(), "crates");

        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
        }

        loadCrates();
    }

    public void loadCrates() {
        crates.clear();

        File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = file.getName().replace(".yml", "");

            String displayName = config.getString("display_name", name);
            int max_reward = config.getInt("max_reward");
            List<String> lore = config.getStringList("lore");
            List<String> hologramText = config.getStringList("textHologram");
            List<String> announce = config.getStringList("announce");
            List<String> announceStart = config.getStringList("announce_start");
            boolean announceStatus = config.getBoolean("announce_status");
            String materialName = config.getString("key.material");
            Material material = Material.matchMaterial(materialName);
            String keyName = config.getString("key.name");
            List<String> keyLore = config.getStringList("key.lore").stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
            String title = config.getString("titlePreview");

            ItemStack itemKey = new ItemStack(material);
            ItemMeta meta = itemKey.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(keyName);
                meta.setLore(keyLore);
                itemKey.setItemMeta(meta);
            }

            List<ItemReward> rewards = new ArrayList<>();
            List<Map<?, ?>> rewardList = config.getMapList("rewards");

            for (Map<?, ?> rewardMap : rewardList) {
                String rewardName = (String) rewardMap.get("name");
                double chance = (double) rewardMap.get("chance");
                List<String> commands = (List<String>) rewardMap.get("commands");
                ItemStack itemStack = deserializeItemStack((Map<String, Object>) rewardMap.get("item"));
                boolean visible = (boolean) rewardMap.get("visible");
                boolean glow = (boolean) rewardMap.get("glow");

                List<String> UUIDs = (List<String>) rewardMap.get("disabled_players");
                List<UUID> playerDisabled = UUIDs.stream().map(UUID::fromString).collect(Collectors.toList());

                String messageWin = (String) rewardMap.get("message_win");

                rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, playerDisabled, glow, messageWin));
            }

            crates.add(new Crate(name, rewards, displayName, max_reward, lore, hologramText, itemKey, announce, title, announceStart, announceStatus));
        }
    }

    public void saveCrates() {
        for (Crate crate : crates) {
            File file = new File(cratesFolder, crate.getName() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            config.set("name", crate.getName());
            config.set("display_name", crate.getDisplayName());
            config.set("max_reward", crate.getMax_reward());
            config.set("lore", crate.getLoreS());
            config.set("textHologram", crate.getHologramText());
            config.set("announce", crate.getAnnouncementFinish());
            config.set("announce_start", crate.getAnnouncementStart());
            config.set("announce_status", crate.isAnnounceStatus());
            config.set("titlePreview", crate.getPreviewTitle());
            config.set("key.material", crate.getItemKey().getType().name());
            config.set("key.name", crate.getItemKey().getItemMeta().getDisplayName());
            config.set("key.lore", crate.getItemKey().getItemMeta().getLore());

            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemReward reward : crate.getRewards()) {
                Map<String, Object> rewardMap = new HashMap<>();
                rewardMap.put("name", reward.getName());
                rewardMap.put("chance", reward.getChance());
                rewardMap.put("commands", reward.getCommands());
                rewardMap.put("item", serializeItemStack(reward.getItemStack()));
                rewardMap.put("visible", reward.isVisible());
                rewardMap.put("glow", reward.isGlow());
                rewardMap.put("message_win", reward.getMessageWon());
                rewardMap.put("disabled_players", reward.getDisabledPlayers().stream().map(UUID::toString).collect(Collectors.toList()));
                rewardList.add(rewardMap);
            }

            config.set("rewards", rewardList);

            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<Crate> getCrates() {
        return crates;
    }

    public void addCrates(Crate crate) {
        crates.add(crate);
        saveCrates();
    }

    public void refreshAll() {
        saveCrates();
    }

    public void reloadAll() {
        loadCrates();
    }

    public Crate getCrateByName(String name) {
        return crates.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
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
                List<String> formattedLore = ((List<String>) metaData.get("lore")).stream().map(l -> l.replace("&", "§")).collect(Collectors.toList());
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
