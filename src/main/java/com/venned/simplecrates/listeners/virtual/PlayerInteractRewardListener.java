package com.venned.simplecrates.listeners.virtual;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.StreamSupport;

public class PlayerInteractRewardListener implements Listener {


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        Block block = event.getClickedBlock();
        if (block.getState() instanceof TileState tileState) {
            if (tileState.getPersistentDataContainer().has(NameSpaceUtils.rewardVirtual, PersistentDataType.STRING)) {

                event.setCancelled(true); // prevenimos la apertura del cofre


                CrateBlock crateBlock = Main.getInstance().getCrateBlockManager().getCrateBlocks()
                        .stream().filter(p->p.getChest().contains(event.getClickedBlock().getLocation()) && !p.getChestUsed().contains(event.getClickedBlock().getLocation()))
                        .findFirst().orElse(null);
                if(crateBlock == null) return;

                if(crateBlock.getOpener() == null) return;
                if(!crateBlock.getOpener().equals(event.getPlayer().getUniqueId())) return;


                int max_reward = ((CrateVirtual) crateBlock.getCrate()).getMax_reward();
                int currentReward = crateBlock.getCurrentReward();

                if (currentReward >= max_reward) {
                    event.setCancelled(true);
                    return;
                }

                event.setCancelled(true);

                crateBlock.incrementCurrentReward(1);

                String crateName = tileState.getPersistentDataContainer().get(NameSpaceUtils.rewardVirtual, PersistentDataType.STRING);
                CrateVirtual crate = Main.getInstance().getCrateVirtualManager().getCrateByName(crateName);
                Player player = event.getPlayer();

                List<ItemReward> rewards = crate.getRewards();
                List<ItemReward> availableRewards = new ArrayList<>();
                for (ItemReward r : rewards) {
                    if (!r.getDisabledPlayers().contains(player.getUniqueId())) {
                        availableRewards.add(r);
                    }
                }

                if (availableRewards.isEmpty()) return;

                double totalWeight = availableRewards.stream().mapToDouble(ItemReward::getChance).sum();
                Random random = new Random();
                ItemReward reward = getWeightedRandomReward(availableRewards, totalWeight, random);

                if (reward != null) {
                //    player.getInventory().addItem(reward.getItemStack());

                    for (String command : reward.getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                    }

                    sendHologramReward(reward.getName(), block.getLocation(), crateBlock);
                    sendItemEntityToPlayer(reward.getItemStack(), block.getLocation(), crateBlock);
                    spawnFirework(block.getLocation());

                    crateBlock.getChestUsed().add(event.getClickedBlock().getLocation());


                    crateBlock.getItemsRewards().add(reward);


                    /*
                    for(Player players: Bukkit.getOnlinePlayers()){
                        players.sendMessage(Main.getMessageItem(reward.getMessageWon(), Map.of("item_reward", reward.getName())));
                    }

                     */



                }
            }
        }
    }

    public static void sendHologramReward( String text, Location original, CrateBlock crateBlock) {
        MinecraftServer server = MinecraftServer.getServer();
        ServerLevel s = StreamSupport.stream(server.getAllLevels().spliterator(), false).filter(level -> level.getWorld().getName().equals(original.getWorld().getName())).findFirst().orElse(null);

        net.minecraft.world.entity.Display.TextDisplay entityDisplay = new net.minecraft.world.entity.Display.TextDisplay(
                net.minecraft.world.entity.EntityType.TEXT_DISPLAY, s
        );

        crateBlock.getEntitiesVisuals().add(entityDisplay);


        TextDisplay textDisplay = (TextDisplay) entityDisplay.getBukkitEntity();

        double x = original.getBlockX() + 0.5;
        double y = original.getBlockY() + 2.0; // un poco más alto para que no se "pegue"
        double z = original.getBlockZ() + 0.5;

        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                entityDisplay.getId(),
                entityDisplay.getUUID(),
                x,
                y,
                z,
                entityDisplay.getXRot(),
                entityDisplay.getYRot(),
                entityDisplay.getType(),
                0,
                entityDisplay.getDeltaMovement(),
                entityDisplay.getYHeadRot()

        );

        textDisplay.setText(text.replace("&", "§"));
        textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
        textDisplay.setSeeThrough(true);
        textDisplay.setPersistent(false);

        Transformation transformation = textDisplay.getTransformation();

        float sizeX = (float) Main.getInstance().getConfig().getDouble("hologram-reward-size.x");
        float sizeY = (float) Main.getInstance().getConfig().getDouble("hologram-reward-size.y");
        float sizeZ = (float) Main.getInstance().getConfig().getDouble("hologram-reward-size.z");


        transformation.getScale().set(new Vector3f(sizeX, sizeY, sizeZ));
        textDisplay.setTransformation(transformation);

        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(
                textDisplay.getEntityId(), entityDisplay.getEntityData().getNonDefaultValues()
        );

        for (Player players : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) players).getHandle().connection.sendPacket(spawnPacket); // Primero spawn
            ((CraftPlayer) players).getHandle().connection.sendPacket(metadataPacket); // Luego metadata
        }

    }

    public static void sendItemEntityToPlayer(org.bukkit.inventory.ItemStack bukkitItem, Location original, CrateBlock crateBlock) {
        ServerLevel serverWorld = ((CraftWorld) original.getWorld()).getHandle();

        org.bukkit.inventory.ItemStack clone = bukkitItem.clone();
        clone.setAmount(1);
        ItemStack nmsStack = CraftItemStack.asNMSCopy(clone);

        BlockPos pos = new BlockPos(original.getBlockX(), original.getBlockY(), original.getBlockZ());


        ItemEntity itemEntity = new ItemEntity(serverWorld, original.getX(), original.getY(), original.getZ(), nmsStack);
        itemEntity.setNoPickUpDelay(); // para que no se pueda recoger
        itemEntity.setUnlimitedLifetime(); // no desaparece automáticamente
        itemEntity.setGlowingTag(true); // opcional

        int entityId = itemEntity.getId();

        crateBlock.getEntitiesVisuals().add(itemEntity);

        double x = original.getBlockX() + 0.5;
        double y = original.getBlockY() + 1.3; // un poco más alto para que no se "pegue"
        double z = original.getBlockZ() + 0.5;


        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                itemEntity.getId(),
                itemEntity.getUUID(),
                x,
                y,
                z,
                itemEntity.getXRot(),
                itemEntity.getYRot(),
                itemEntity.getType(),
                0,
                itemEntity.getDeltaMovement(),
                itemEntity.getYHeadRot()

        );

        Packet<?> dataPacket = new ClientboundSetEntityDataPacket(entityId, itemEntity.getEntityData().getNonDefaultValues());

        for (Player players : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) players).getHandle().connection.sendPacket(spawnPacket); // Primero spawn
            ((CraftPlayer) players).getHandle().connection.sendPacket(dataPacket); // Luego metadata
        }

        int action = 1; // Chest action
        int data = 1;

       BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), ()-> {
            Packet<?> packet = new ClientboundBlockEventPacket(pos, net.minecraft.world.level.block.Blocks.CHEST, action, data);
           for (Player players : Bukkit.getOnlinePlayers()) {
               ((CraftPlayer) players).getHandle().connection.sendPacket(packet); // Primero spawn
           }
        }, 10, 10);

       crateBlock.getTask().add(task);

    }

    public static ItemReward getWeightedRandomReward(List<ItemReward> rewards, double totalWeight, Random random) {
        double r = random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (ItemReward reward : rewards) {
            cumulative += reward.getChance();
            if (r <= cumulative) {
                return reward;
            }
        }
        return null;
    }


    private void spawnFirework(Location location) {
        Firework firework = location.getWorld().spawn(location.clone().add(0.5, 1, 0.5), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .withColor(Color.ORANGE)
                .withFade(Color.YELLOW)
                .with(FireworkEffect.Type.BURST)
                .build();

        meta.addEffect(effect);
        meta.setPower(1);

        firework.setFireworkMeta(meta);
        firework.detonate(); // detona instantáneamente
    }
}
