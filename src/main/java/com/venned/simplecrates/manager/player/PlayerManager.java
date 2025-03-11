package com.venned.simplecrates.manager.player;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.*;

public class PlayerManager {
    private Connection connection;
    private final Plugin plugin;
    private final Set<PlayerData> playerDataSet = new HashSet<>();

    public PlayerManager(Plugin plugin) {
        this.plugin = plugin;
        setupDatabase();
        loadAllPlayers();
    }

    private void setupDatabase() {
        try {

            File dataFolder = new File(plugin.getDataFolder(), "player_data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs(); // Crea el directorio si no existe
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/player_data/data.db");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, notifiedReward BOOLEAN)");
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
                playerDataSet.add(new PlayerData(uuid, notifiedReward));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(PlayerData playerData) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO players (uuid, notifiedReward) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET notifiedReward = ?")) {
            statement.setString(1, playerData.getUUID().toString());
            statement.setBoolean(2, playerData.isNotifiedReward());
            statement.setBoolean(3, playerData.isNotifiedReward());
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
}
