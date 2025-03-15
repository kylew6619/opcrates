package com.venned.simplecrates.task;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.CrateBlock;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Display;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftTextDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramChestTask extends BukkitRunnable {
    @Override
    public void run() {
        for(CrateBlock crateBlock : Main.getInstance().getCrateBlockManager().getCrateBlocks()) {
            for(TextDisplay textDisplay : crateBlock.getTexts()){

                Location lineLocation = textDisplay.getLocation();


                Display.TextDisplay entityDisplay = ((CraftTextDisplay) textDisplay).getHandle();

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


                for (Player player : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) player).getHandle().connection.sendPacket(spawnPacket); // Primero spawn
                    ((CraftPlayer) player).getHandle().connection.sendPacket(metadataPacket); // Luego metadata
                }
            }
        }
    }
}
