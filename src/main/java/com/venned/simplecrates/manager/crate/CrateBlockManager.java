package com.venned.simplecrates.manager.crate;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateBlockManager {

    Set<CrateBlock> crateBlocks;
    CrateManager crateManager;
    private final File cratesDataFile;
    private FileConfiguration cratesDataConfig;

    public CrateBlockManager(Plugin plugin, CrateManager crateManager) {
        crateBlocks = new HashSet<CrateBlock>();
        this.crateManager = crateManager;

        cratesDataFile = new File(Main.getInstance().getDataFolder(), "crates_data.yml");

        if (!cratesDataFile.exists()) {
            try {
                cratesDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cratesDataConfig = YamlConfiguration.loadConfiguration(cratesDataFile);

        loadCrateBlocks();
    }

    public void reloadAll(){
        saveCrates();

        cratesDataConfig = YamlConfiguration.loadConfiguration(cratesDataFile);
        loadCrateBlocks();
    }

    void loadCrateBlocks() {
        for(CrateBlock crateBlock : crateBlocks) {
            crateBlock.removeHologram();
        }
        crateBlocks.clear();

        if (!cratesDataConfig.contains("crates")) return;

        ConfigurationSection section = cratesDataConfig.getConfigurationSection("crates");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String crateName = section.getString(key + ".crate_name");
            Location location = deserializeLocation(section.getString(key + ".location"));

            Crate crate = crateManager.getCrateByName(crateName);
            if (crate == null) continue;

            crateBlocks.add(new CrateBlock(location, crate));
        }
    }

    public void removeCrateBlock(Location location) {

        for(CrateBlock crateBlock : crateBlocks){
            if(crateBlock.getLocation().equals(location)){
                crateBlock.removeHologram();
            }
        }
        crateBlocks.removeIf(c->c.getLocation().equals(location));
        saveCrates();
    }


    public void saveCrates() {
        cratesDataConfig.set("crates", null); // Reinicia la sección de crates

        int index = 1;
        for (CrateBlock crateBlock : crateBlocks) {

            crateBlock.removeHologram();

            String path = "crates." + index;

            cratesDataConfig.set(path + ".crate_name", crateBlock.getCrate().getName());
            cratesDataConfig.set(path + ".location", serializeLocation(crateBlock.getLocation()));

            index++;
        }

        try {
            cratesDataConfig.save(cratesDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<CrateBlock> getCrateBlocks() {
        return crateBlocks;
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ();
    }

    private Location deserializeLocation(String data) {
        String[] parts = data.split(",");
        return new Location(
                Main.getInstance().getServer().getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

}
