package com.venned.simplecrates.manager.crate;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.ItemReward;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CrateManager {


    private final Set<Crate> crates = new HashSet<>();
    private final File cratesFile;
    private FileConfiguration cratesConfig;

    public CrateManager() {
        cratesFile = new File(Main.getInstance().getDataFolder(), "crates.yml");

        if (!cratesFile.exists()) {
            try {
                cratesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
        loadCrates();
    }

    public void loadCrates() {
        crates.clear();

        if (!cratesConfig.contains("crates")) return;

        ConfigurationSection section = cratesConfig.getConfigurationSection("crates");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");

            String displayName = section.getString(key + ".display_name");
            if(displayName == null){
                displayName = name;
            }

            int max_reward = section.getInt(key + ".max_reward");

            List<String> lore = section.getStringList(key + ".lore");

            List<String> hologramText = section.getStringList(key + ".textHologram");

            List<String> announce = section.getStringList(key + ".announce");

            boolean announceStatus = section.getBoolean(key + ".announce_status");

            List<String> announceStart = section.getStringList(key + ".announce_start");

            String materialName = section.getString(key + ".key.material");
            Material material = Material.matchMaterial(materialName);
            String keyName = section.getString(key + ".key.name");
            List<String> keyLore = section.getStringList(key + ".key.lore");
            keyLore = keyLore.stream().map(lore2 -> lore2.replace("&", "§")).collect(Collectors.toList());

            String title = section.getString(key + ".titlePreview");

            ItemStack itemKey = new ItemStack(material);
            ItemMeta meta = itemKey.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(keyName);
                meta.setLore(keyLore);
                itemKey.setItemMeta(meta);
            }

            List<ItemReward> rewards = new ArrayList<>();
            List<Map<?, ?>> rewardList = section.getMapList(key + ".rewards");

            for (Map<?, ?> rewardMap : rewardList) {
                String rewardName = (String) rewardMap.get("name");
                double chance = (double) rewardMap.get("chance");

                List<String> commands = (List<String>) rewardMap.get("commands");
                ItemStack itemStack = deserializeItemStack((Map<String, Object>) rewardMap.get("item"));


                boolean visible = (boolean) rewardMap.get("visible");


                List<String> UUIDs = (List<String>) rewardMap.get("disabled_players");

                List<UUID> playerDisabled = new ArrayList<>();
                for(String uuid : UUIDs){
                    playerDisabled.add(UUID.fromString(uuid));
                }

                rewards.add(new ItemReward(rewardName, itemStack, chance, commands, visible, playerDisabled));
            }

            crates.add(new Crate(name, rewards, displayName, max_reward, lore, hologramText, itemKey, announce, title, announceStart, announceStatus));
        }
    }

    public void saveCrates() {
        cratesConfig.set("crates", null);

        for (Crate crates : crates) {
            String path = "crates." + crates.getName();
            cratesConfig.set(path + ".name", crates.getName());
            cratesConfig.set(path + ".announce_status", crates.isAnnounceStatus());
            cratesConfig.set(path + ".announce", crates.getAnnouncementFinish());
            cratesConfig.set(path + ".announce_start", crates.getAnnouncementStart());
            cratesConfig.set(path + ".display_name", crates.getDisplayName());
            cratesConfig.set(path + ".max_reward", crates.getMax_reward());
            cratesConfig.set(path + ".lore", crates.getLoreS());
            cratesConfig.set(path + ".textHologram", crates.getHologramText());
            cratesConfig.set(path + ".key.material", crates.getItemKey().getType().name());
            cratesConfig.set(path + ".key.name", crates.getItemKey().getItemMeta().getDisplayName());
            cratesConfig.set(path + ".key.lore", crates.getItemKey().getItemMeta().hasLore() ? crates.getItemKey().getItemMeta().getLore() : new ArrayList<>());
            cratesConfig.set(path + ".titlePreview", crates.getPreviewTitle());

            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemReward reward : crates.getRewards()) {
                Map<String, Object> rewardMap = new HashMap<>();
                rewardMap.put("name", reward.getName());
                rewardMap.put("chance", reward.getChance());
                rewardMap.put("item", serializeItemStack(reward.getItemStack()));

                rewardMap.put("commands", reward.getCommands());
                rewardMap.put("visible", reward.isVisible());

                List<String> UUIDs = new ArrayList<>();
                for(UUID uuid : reward.getDisabledPlayers()){
                    UUIDs.add(uuid.toString());
                }
                rewardMap.put("disabled_players", UUIDs);


                rewardList.add(rewardMap);
            }
            cratesConfig.set(path + ".rewards", rewardList);
        }

        try {
            cratesConfig.save(cratesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Crate> getCrates() {
        return crates;
    }

    public void addCrates(Crate crate) {
        crates.add(crate);
        saveCrates();
    }


    public void refreshAll(){
        saveCrates();
    }

    public void reloadAll(){

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
        loadCrates();
    }

    public Crate getCrateByName(String name){
        return crates.stream().filter(c->c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
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
