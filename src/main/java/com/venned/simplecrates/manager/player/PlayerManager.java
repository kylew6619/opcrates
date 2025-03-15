package com.venned.simplecrates.manager.player;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.BackPack;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.manager.crate.CrateManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
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

                statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, notifiedReward BOOLEAN, backpack_data TEXT, autopickupkey BOOLEAN)");
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

                BackPack backPack = deserializeBackPack(backpackData);
                PlayerData playerData = new PlayerData(uuid, notifiedReward,autoPickUp);
                playerData.setBackPack(backPack);

                playerDataSet.add(playerData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(PlayerData playerData) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO players (uuid, notifiedReward, backpack_data, autopickupkey) VALUES (?, ?, ?, ?)")) {

            statement.setString(1, playerData.getUUID().toString());
            statement.setBoolean(2, playerData.isNotifiedReward());
            statement.setString(3, serializeBackPack(playerData.getBackPack()));
            statement.setBoolean(4, playerData.isAutoPickUpKey());

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
