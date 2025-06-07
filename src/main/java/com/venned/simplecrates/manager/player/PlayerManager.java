package com.venned.simplecrates.manager.player;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.BackPack;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.manager.crate.CrateManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    private Connection connection;
    private final Plugin plugin;
    private final Set<PlayerData> playerDataSet = new HashSet<>();
    private final CrateManager crateManager;

    public PlayerManager(Plugin plugin, CrateManager crateManager) {
        this.plugin = plugin;
        this.crateManager = crateManager;
        setupDatabase();
        loadAllPlayers();
    }

    private void setupDatabase() {
        try {

            File dataFolder = new File(plugin.getDataFolder(), "player_data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/player_data/data.db");
            try (Statement statement = connection.createStatement()) {

                plugin.getLogger().info("Create DataBase Player_Data");

                statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, notifiedReward BOOLEAN, backpack_data TEXT, autopickupkey BOOLEAN, disabled_rewards TEXT)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadAllPlayers() {
        try (Statement statement = getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM players")) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                boolean notifiedReward = resultSet.getBoolean("notifiedReward");
                String backpackData = resultSet.getString("backpack_data"); // Obtener mochila serializada
                boolean autoPickUp = resultSet.getBoolean("autopickupkey");
                String disabledRewardData = resultSet.getString("disabled_rewards");

                BackPack backPack = deserializeBackPack(backpackData);
                List<ItemStack> disabledRewards = deserializeDisabledRewards(disabledRewardData);

                PlayerData playerData = new PlayerData(uuid, notifiedReward,autoPickUp, disabledRewards);
                playerData.setBackPack(backPack);

                playerDataSet.add(playerData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(PlayerData playerData) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO players (uuid, notifiedReward, backpack_data, autopickupkey, disabled_rewards) VALUES (?, ?, ?, ?, ?)")) {

            statement.setString(1, playerData.getUUID().toString());
            statement.setBoolean(2, playerData.isNotifiedReward());
            statement.setString(3, serializeBackPack(playerData.getBackPack()));
            statement.setBoolean(4, playerData.isAutoPickUpKey());
            statement.setString(5, serializeDisabledRewards(playerData.getDisabledReward()));


            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAllPlayers() {
        for (PlayerData playerData : playerDataSet) {
            savePlayer(playerData);
        }
    }

    public PlayerData getPlayerData(Player player){
        return playerDataSet.stream().filter(p->p.getUUID().equals(player.getUniqueId()))
                .findFirst().orElse(null);
    }

    public Set<PlayerData> getPlayerDatas() {
        return playerDataSet;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDataFolder() + "/player_data/data.db");
        }
        return connection;
    }

    private String serializeDisabledRewards(List<ItemStack> items) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (ItemStack item : items) {
            serializedItems.add(serializeItemStack(item));
        }
        return new Gson().toJson(serializedItems);
    }

    private List<ItemStack> deserializeDisabledRewards(String data) {
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> itemDataList = new Gson().fromJson(data, type);

        List<ItemStack> items = new ArrayList<>();
        for (Map<String, Object> map : itemDataList) {

            ItemStack item = deserializeItemStack(map);

            Main.getInstance().getCrateManager().getCrates().forEach(crate -> {
                if (crate.isSimilarCrate(item)) {
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, crate.getName());
                    item.setItemMeta(meta);
                } else if (crate.isSimilarKey(item)) {
                    ItemMeta meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, crate.getName());
                    item.setItemMeta(meta);
                }
            });

            items.add(deserializeItemStack(map));
        }



        return items;
    }


    private ItemStack deserializeItemStack(Map<String, Object> data) {
        if (data == null) return null;
        data.put("v", Bukkit.getUnsafe().getDataVersion());

        int version = data.containsKey("v") ? ((Number)data.get("v")).intValue() : -1;

        Object raw;

        raw = data.get("meta");

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


    private String serializeBackPack(BackPack backPack) {
        if (backPack == null || backPack.getCrateKeys().isEmpty()) return "";

        return backPack.getCrateKeys().stream()
                .map(crateKey -> crateKey.getCrate().getName() + ":" + crateKey.getKeys())
                .collect(Collectors.joining(","));
    }

    private BackPack deserializeBackPack(String data) {
        if (data == null || data.isEmpty()) return new BackPack(new ArrayList<>());

        List<CrateKeyPlayer> crateKeyPlayers = Arrays.stream(data.split(","))
                .map(entry -> {
                    String[] parts = entry.split(":");
                    if (parts.length != 2) return null;

                    String crateName = parts[0];
                    int amount = Integer.parseInt(parts[1]);
                    Crate crate = crateManager.getCrateByName(crateName); // Método para obtener el Crate

                    return crate != null ? new CrateKeyPlayer(crate, amount) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new BackPack(crateKeyPlayers);
    }
}
