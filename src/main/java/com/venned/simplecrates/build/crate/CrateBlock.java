package com.venned.simplecrates.build.crate;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.interfaces.CrateInterface;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftTextDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.StreamSupport;

public class CrateBlock {

    private Location location;
    private CrateInterface crate;
    private List<TextDisplay> texts;

    //VIRTUAL

    UUID opener;
    Material originalMaterial;
    Map<Location, BlockData> originales = new HashMap<>();
    private final List<ItemReward> itemsRewards;
    List<BlockData> originalBlockData = new ArrayList<>();
    private final List<Location> chest;
    private final List<Location> chestUsed;
    private final List<Entity> entitiesVisuals;
    private final Set<BukkitTask> task;
    private int currentReward;

    public CrateBlock(Location location, CrateInterface crate) {
        this.location = location;
        this.crate = crate;
        this.texts = new ArrayList<>();

        //VIRTUAL
        this.chestUsed = new ArrayList<>();
        this.entitiesVisuals = new ArrayList<>();
        this.chest = new ArrayList<>();
        this.task = new HashSet<>();
        this.currentReward = 0;
        this.itemsRewards = new ArrayList<>();
        this.opener = null;

        generateHologram();
    }

    public void setOpener(UUID opener) {
        this.opener = opener;
    }

    public UUID getOpener() {
        return opener;
    }

    public List<ItemReward> getItemsRewards() {
        return itemsRewards;
    }

    public List<BlockData> getOriginalBlockData() {
        return originalBlockData;
    }

    public Map<Location, BlockData> getOriginales() {
        return originales;
    }

    public Set<BukkitTask> getTask() {
        return task;
    }

    public void setOriginalMaterial(Material originalMaterial) {
        this.originalMaterial = originalMaterial;
    }

    public Material getOriginalMaterial() {
        return originalMaterial;
    }

    public List<Location> getChestUsed(){
        return chestUsed;
    }

    public List<Location> getChest() {
        return chest;
    }

    public void deleteEntitiesVisuals(){
        for(Entity entity : entitiesVisuals){
            for (Player players : Bukkit.getOnlinePlayers()) {
                entity.remove(Entity.RemovalReason.KILLED);
                ((CraftPlayer) players).getHandle().connection.sendPacket(new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(entity.getId())); // Primero spawn
            }
        }
    }

    public void incrementCurrentReward(int amount){
        this.currentReward += amount;
    }

    public void setCurrentReward(int currentReward) {
        this.currentReward = currentReward;
    }

    public int getCurrentReward() {
        return currentReward;
    }

    public List<Entity> getEntitiesVisuals() {
        return entitiesVisuals;
    }


    private void deleteHologram(){
        for(TextDisplay display : texts){
            ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(display.getEntityId());
            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().connection.sendPacket(removeEntitiesPacket);
            }
            texts.remove(display);
        }

    }

    public void generateHologram() {
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

    public CrateInterface getCrate() {
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
