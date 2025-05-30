package com.venned.simplecrates.utils;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HologramTextUtils {

    public static void textReward(Player player, Location location, List<ItemReward> rewards, String nameCrate) {
        FileConfiguration config = Main.getInstance().getConfig();

        MinecraftServer server = MinecraftServer.getServer();
        ServerLevel s = StreamSupport.stream(server.getAllLevels().spliterator(), false).filter(level -> level.getWorld().getName().equals(location.getWorld().getName())).findFirst().orElse(null);


        // Obtener formato de resumen desde la config.yml
        List<String> summaryLines = config.getStringList("summary-text");



        String rewardText = rewards.stream().map(reward -> {
            ItemMeta meta = reward.getItemStack().getItemMeta();
            String itemName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : reward.getName();
            return itemName + " x" + reward.getItemStack().getAmount();
        }).collect(Collectors.joining("\n"));

        // Reemplazar placeholders en el texto del resumen
        List<String> formattedLines = new ArrayList<>();
        for (String line : summaryLines) {
            formattedLines.add(line.replace("{player}", player.getName())
                    .replace("{crate}", nameCrate)
                    .replace("{rewards}", rewardText)
                    .replace("&", "§"));
        }

        // Crear holograma
        List<Entity> hologramEntities = new ArrayList<>();
        for (int i = 0; i < formattedLines.size(); i++) {

            net.minecraft.world.entity.Display.TextDisplay entityDisplay = new net.minecraft.world.entity.Display.TextDisplay(
                    net.minecraft.world.entity.EntityType.TEXT_DISPLAY, s
            );

            Location lineLocation = location.clone().add(0, i * 0.25, 0);

            TextDisplay textDisplay = (TextDisplay) entityDisplay.getBukkitEntity();

            ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                    entityDisplay.getId(),
                    entityDisplay.getUUID(),
                    lineLocation.getX(),
                    lineLocation.getY(),
                    lineLocation.getZ(),
                    entityDisplay.getXRot(),
                    entityDisplay.getYRot(),
                    entityDisplay.getType(),
                    0,
                    entityDisplay.getDeltaMovement(),
                    entityDisplay.getYHeadRot()

            );

            textDisplay.setText(formattedLines.get(i));
            textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            textDisplay.setSeeThrough(true);
            textDisplay.setPersistent(false);

            Transformation transformation = textDisplay.getTransformation();
            transformation.getScale().set(new Vector3f(0.5f, 0.5f, 0.5f));
            textDisplay.setTransformation(transformation);

            ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(
                    textDisplay.getEntityId(), entityDisplay.getEntityData().getNonDefaultValues()
            );


            for (Player players : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) players).getHandle().connection.sendPacket(spawnPacket); // Primero spawn
                ((CraftPlayer) players).getHandle().connection.sendPacket(metadataPacket); // Luego metadata
            }

            hologramEntities.add(textDisplay);
        }

        // Despawn después de 10 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : hologramEntities) {

                    if(entity != null) {
                        net.minecraft.world.entity.Entity e = ((CraftEntity) entity).getHandle();
                        ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(e.getId());
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) players).getHandle().connection.sendPacket(removePacket); // Primero spawn
                        }
                    }

                    if (entity != null && !entity.isDead()) {


                        entity.remove();
                    }
                }
            }
        }.runTaskLater(Main.getInstance(), 100L); // 200 ticks = 10 segundos
    }
}
