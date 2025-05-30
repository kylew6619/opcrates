package com.venned.simplecrates.manager;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.BackPackKey;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.manager.crate.CrateManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BackPackConfig {


    private final File backPackFile;
    private FileConfiguration backPackConfig;

    CrateManager crateManager;

    boolean enabled = true;
    boolean autoPickup = true;
    boolean allow_withdraw = true;

    List<World> disabledWorlds = new ArrayList<>();


    List<BackPackKey> keys = new ArrayList<>();
    List<Crate> keysDisabled = new ArrayList<>();

    public BackPackConfig(CrateManager crateManager) {
        backPackFile = new File(Main.getInstance().getDataFolder(), "backpack.yml");
        if (!backPackFile.exists()) {
            createDefaultConfig();
        }

        this.crateManager = crateManager;

        backPackConfig = YamlConfiguration.loadConfiguration(backPackFile);

        loadConfig();
    }

    public void reloadConfig() {
        backPackConfig = YamlConfiguration.loadConfiguration(backPackFile);

        loadConfig();
    }

    public void loadConfig(){

        enabled = backPackConfig.getBoolean("key_backpacks.enabled");
        autoPickup = backPackConfig.getBoolean("key_backpacks.autoPickup");
        allow_withdraw = backPackConfig.getBoolean("key_backpacks.allow_withdraw");

        disabledWorlds.clear();
        List<String> worlds = backPackConfig.getStringList("key_backpacks.disable_in_worlds");
        for (String worldName : worlds) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                disabledWorlds.add(world);
            } else {
                Bukkit.getLogger().warning("BackPackConfig: World '" + worldName + "' not found!");
            }
        }

        keys.clear();
        if (backPackConfig.contains("keys")) {
            for (String keyName : backPackConfig.getConfigurationSection("keys").getKeys(false)) {
                String displayName = backPackConfig.getString("keys." + keyName + ".display_name", keyName);
                String materialName = backPackConfig.getString("keys." + keyName + ".material", "STONE");
                int storageLimit = backPackConfig.getInt("keys." + keyName + ".storage_limit", -1);
                List<String> lore = backPackConfig.getStringList("keys." + keyName + ".lore");
                ListIterator<String> iterator = lore.listIterator();
                while (iterator.hasNext()) {
                    String line = iterator.next();
                    line = line.replace("&", "§").replace("%limit%", "" + storageLimit);
                    iterator.set(line); // Reemplazar la línea en la lista
                }

                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    Bukkit.getLogger().warning("Invalid material for key '" + keyName + "': " + materialName);
                    continue;
                }

                ItemStack keyItem = new ItemStack(material);
                ItemMeta itemMeta = keyItem.getItemMeta();
                itemMeta.setDisplayName(displayName);
                itemMeta.setLore(lore);
                keyItem.setItemMeta(itemMeta);

                Crate crate = crateManager.getCrateByName(keyName);
                if(crate == null){
                    Bukkit.getLogger().warning("Crate '" + keyName + "' not found in BackPack Crate");
                    return;
                }

                BackPackKey backPackKey = new BackPackKey(crate, keyItem, storageLimit);
                keys.add(backPackKey);
            }
        }

        List<Crate> disabledKeys = new ArrayList<>();
        List<String> disabled_keys = backPackConfig.getStringList("backpacks.disabled_keys");
        for(String disabled_key : disabled_keys){
            for(Crate crate : crateManager.getCrates()){
                if(crate.getName().equalsIgnoreCase(disabled_key)){
                    disabledKeys.add(crate);
                }
            }
        }
        this.keysDisabled = disabledKeys;
    }


    private void createDefaultConfig() {
        backPackConfig = new YamlConfiguration();

        backPackConfig.set("key_backpacks.enabled", true);
        backPackConfig.set("key_backpacks.autoPickup", true);
        backPackConfig.set("key_backpacks.allow_withdraw", true);
        backPackConfig.set("key_backpacks.tradeable", false);
        backPackConfig.set("key_backpacks.disable_in_worlds", List.of("hub", "event"));
        backPackConfig.set("lore-add", Arrays.asList("&9Left-Click to %action% 1000 item", "&9Left-Click to %action% 1000 item", "&9Left-Click to %action% 1000 item",
                "&8Shift-Left-Click to %action% 100 item", "&9Shift-Right-Click to %action% all"));


        backPackConfig.set("gui.title", "&6&lYour Key Backpack");
        backPackConfig.set("gui.size", 54);

        Map<String, String> layout = new HashMap<>();
        layout.put("0", "glass");
        layout.put("1", "vote");
        layout.put("2", "legendary");
        layout.put("3", "mythic");
        layout.put("8", "close_button");
        layout.put("10", "change_mode");

        backPackConfig.set("gui.layout", layout);

        backPackConfig.set("items.change_mode.display_name", "&cChange Mode");
        backPackConfig.set("items.change_mode.material", "STONE");
        backPackConfig.set("items.change_mode.lore", List.of("&cClick change mode"));

        backPackConfig.set("items.close_button.display_name", "&cClose Button");
        backPackConfig.set("items.close_button.material", "BARRIER");
        backPackConfig.set("items.close_button.lore", List.of("&cClick change mode"));

        backPackConfig.set("keys.vote.display_name", "&aVote Key");
        backPackConfig.set("keys.vote.material", "TRIPWIRE_HOOK");
        backPackConfig.set("keys.vote.storage_limit", 100);
        backPackConfig.set("keys.vote.lore", List.of("&7Store &eVote Keys &7automatically!", "&7Keys Owned: &e%amount%", "&7Storage Limit: &c%limit%"));

        backPackConfig.set("keys.legendary.display_name", "&dLegendary Key");
        backPackConfig.set("keys.legendary.material", "GOLD_NUGGET");
        backPackConfig.set("keys.legendary.storage_limit", 50);
        backPackConfig.set("keys.legendary.lore", List.of("&7This key is ultra rare!", "&7Keys Owned: &e%amount%", "&7Storage Limit: &c%limit%"));

        backPackConfig.set("keys.mythic.display_name", "&bMythic Key");
        backPackConfig.set("keys.mythic.material", "DIAMOND");
        backPackConfig.set("keys.mythic.storage_limit", -1);
        backPackConfig.set("keys.mythic.lore", List.of("&7Only the best players earn these!", "&7Keys Owned: &e%amount%", "&7Storage Limit: &aUnlimited"));

        backPackConfig.set("disabled_keys", List.of("basic", "common"));

        try {
            backPackConfig.save(backPackFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoPickup() {
        return autoPickup;
    }

    public boolean isAllow_withdraw() {
        return allow_withdraw;
    }

    public List<BackPackKey> getKeys() {
        return keys;
    }

    public FileConfiguration getBackPackConfig() {
        return backPackConfig;
    }

    public File getBackPackFile() {
        return backPackFile;
    }
}
