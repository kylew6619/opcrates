package com.venned.simplecrates.build.crate;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftTextDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class CrateBlock {

    private Location location;
    private Crate crate;
    private List<TextDisplay> texts;

    public CrateBlock(Location location, Crate crate) {
        this.location = location;
        this.crate = crate;
        this.texts = new ArrayList<>();
        generateHologram();
    }

    private void generateHologram() {
        List<String> hologramLines = crate.getHologramText();
        List<String> lines = new ArrayList<>(hologramLines);
        Collections.reverse(lines);

        World world = location.getWorld();

        if (world == null || lines.isEmpty()) return;


        Location holoLocation = location.clone().add(0.5, 1.5, 0.5); // Centrar y elevar


        MinecraftServer server = MinecraftServer.getServer();
        ServerLevel s = StreamSupport.stream(server.getAllLevels().spliterator(), false).filter(level -> level.getWorld().getName().equals(holoLocation.getWorld().getName())).findFirst().orElse(null);



        for (int i = 0; i < lines.size(); i++) {


            Location lineLocation = holoLocation.clone().add(0, 0.35 * i, 0); // Más espacio entre líneas


            net.minecraft.world.entity.Display.TextDisplay entityDisplay = new net.minecraft.world.entity.Display.TextDisplay(
                    net.minecraft.world.entity.EntityType.TEXT_DISPLAY, s
            );

            TextDisplay textDisplay = (TextDisplay) entityDisplay.getBukkitEntity();


            String text = ChatColor.translateAlternateColorCodes('&', lines.get(i));
            text = text.replace("%name%",  ChatColor.translateAlternateColorCodes('&' ,crate.getDisplayName()));




            textDisplay.setText(text);
            textDisplay.setSeeThrough(true);
            textDisplay.setPersistent(true);
            textDisplay.setBillboard(Display.Billboard.CENTER);




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


            ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(
                    textDisplay.getEntityId(), entityDisplay.getEntityData().getNonDefaultValues()
            );

            entityDisplay.teleportTo(lineLocation.getX(), lineLocation.getY(), lineLocation.getZ());


            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().connection.sendPacket(spawnPacket); // Primero spawn
                ((CraftPlayer) player).getHandle().connection.sendPacket(metadataPacket); // Luego metadata

            }


            texts.add(textDisplay);
        }
    }

    void reload(){
        for(TextDisplay textDisplay : texts){
            textDisplay.remove();
        }
    }

    public Location getLocation() {
        return location;
    }

    public Crate getCrate() {
        return crate;
    }

    public List<TextDisplay> getTexts() {
        return texts;
    }

    public void removeHologram() {
        for (TextDisplay text : texts) {
            net.minecraft.world.entity.Display.TextDisplay textDisplay = ((CraftTextDisplay) text).getHandle();
            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(textDisplay.getId());
            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().connection.sendPacket(removePacket);
            }
            text.remove();
        }
        texts.clear();
    }
}
