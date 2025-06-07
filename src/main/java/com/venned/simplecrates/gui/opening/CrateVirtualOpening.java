package com.venned.simplecrates.gui.opening;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import com.venned.simplecrates.listeners.virtual.PlayerInteractRewardListener;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.MapUtils;
import com.venned.simplecrates.utils.NameSpaceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class CrateVirtualOpening {


    public static void open(Player player, CrateVirtual crate, CrateBlock crateBlock) {

        crateBlock.removeHologram();

        Location center = crateBlock.getLocation().clone().add(-0.5, 0, 0.5);
        Material originalMaterial = crateBlock.getLocation().getBlock().getType();
        crateBlock.getLocation().getBlock().setType(Material.AIR);
        player.teleport(center);
        World world = center.getWorld();

        crateBlock.setOriginalMaterial(originalMaterial);
        crateBlock.setOpener(player.getUniqueId());


        MapUtils.playerOpenVirtual.put(player.getUniqueId(), crateBlock);

        int maxRadius = 5;

        int cy = center.getBlockY();
        for (int dx = -maxRadius; dx <= maxRadius; dx++) {
            for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) <= maxRadius) {
                    Location loc = new Location(center.getWorld(), center.getBlockX() + dx, cy - 1, center.getBlockZ() + dz);
                    crateBlock.getOriginales().put(loc, loc.getBlock().getBlockData().clone());
                }
            }
        }


        BukkitTask initial =  new BukkitRunnable() {
            private int currentRadius = 1;

            @Override
            public void run() {
                if (currentRadius > maxRadius) {
                    this.cancel();
                    startOrbitingAnimation();
                    return;
                }

                double minSQ = (currentRadius - 1) * (currentRadius - 1);
                double maxSQ = currentRadius * currentRadius;

                for (Map.Entry<Location, BlockData> entry : crateBlock.getOriginales().entrySet()) {
                    Location loc = entry.getKey();
                    int dx = loc.getBlockX() - center.getBlockX();
                    int dz = loc.getBlockZ() - center.getBlockZ();
                    double distSQ = dx * dx + dz * dz;
                    if (distSQ > minSQ && distSQ <= maxSQ) {
                        // Si el bloque actual es AIR, no hacemos nada
                        if (loc.getBlock().getType() == Material.AIR) {
                            continue;
                        }

                        // Partícula de quiebre
                        BlockData originalData = entry.getValue();
                        world.spawnParticle(
                                Particle.BLOCK_CRUMBLE,
                                loc.clone().add(0.5, 0.5, 0.5),
                                10, 0.2, 0.2, 0.2, 0,
                                originalData
                        );

                        // Si era una escalera original, la reemplazamos por escalera de cuarzo
                        if (originalData instanceof org.bukkit.block.data.type.Stairs) {
                            org.bukkit.block.data.type.Stairs oldStairs =
                                    (org.bukkit.block.data.type.Stairs) originalData;
                            org.bukkit.block.data.type.Stairs newStairs =
                                    (org.bukkit.block.data.type.Stairs) Bukkit.createBlockData(crate.getOpeningMaterialStair());

                            // Copiamos la orientación: facing, half y shape
                            newStairs.setFacing(oldStairs.getFacing());
                            newStairs.setHalf(oldStairs.getHalf());
                            newStairs.setShape(oldStairs.getShape());

                            loc.getBlock().setBlockData(newStairs);
                        } else {
                            // Cualquier otro bloque → bloque completo de cuarzo
                            loc.getBlock().setType(crate.getOpeningMaterialBlock());
                        }
                    }
                }

                currentRadius++;
            }
            // Al terminar, queremos que se ejecute la animación que antes estaba al principio (orbiting, etc.)
            private void startOrbitingAnimation() {
                List<BlockDisplay> orbitingDisplays = new ArrayList<>();

                // Generamos las entidades orbitantes inicialmente en torno al jugador
                for (int i = 0; i < 8; i++) {
                    Location playerLoc = player.getLocation();
                    double angleOffset = i * (Math.PI / 4); // 8 cofres, 360°/8 = π/4 radianes
                    Location spawnLoc = playerLoc.clone().add(
                            Math.cos(angleOffset),
                            1.0,
                            Math.sin(angleOffset)
                    );
                    BlockDisplay display = (BlockDisplay) world.spawn(spawnLoc, BlockDisplay.class);
                    display.setBlock(Bukkit.createBlockData(Material.CHEST));
                    display.setTransformation(new Transformation(
                            new Vector3f(0, 0, 0),
                            new Quaternionf(),
                            new Vector3f(0.2f, 0.2f, 0.2f),
                            new Quaternionf()
                    ));
                    display.setGravity(false);
                    orbitingDisplays.add(display);
                    crateBlock.getEntitiesVisuals().add(((CraftEntity) display).getHandle());

                }

                // Tarea que teletransporta al jugador al mismo punto para “fijarlo” en el centro de la órbita
                BukkitTask taskCenter = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
                    Location base = player.getLocation();
                    Vector playerVec = base.toVector();
                    Vector centerVec = center.toVector();

                    // Solo calculamos distancia en XZ
                    Vector diff = playerVec.clone().setY(0).subtract(centerVec.clone().setY(0));
                    double dist = diff.length();

                    // Si sale del radio 3, lo traemos de vuelta al borde del círculo
                    if (dist > 3.0) {
                        // Normalizamos el vector y lo escalamos a 3
                        Vector clamped = diff.normalize().multiply(3.0);
                        double newX = centerVec.getX() + clamped.getX();
                        double newZ = centerVec.getZ() + clamped.getZ();

                        Location toTele = new Location(
                                base.getWorld(),
                                newX,
                                center.getY(),      // altura fija en center.getY()
                                newZ,
                                base.getYaw(),
                                base.getPitch()
                        );
                        player.teleport(toTele);
                    }
                }, 10L, 10L);
                crateBlock.getTask().add(taskCenter);

                BukkitTask orbiting =  new BukkitRunnable() {
                    double angle = 0;
                    double radius = 1.0;
                    int ticks = 0;

                    @Override
                    public void run() {
                        ticks++;
                        if (ticks >= 100) {
                            this.cancel();
                            orbitingDisplays.forEach(Entity::remove);
                            spawnChests(center, originalMaterial, crateBlock, player); // o como lo llamaras
                            return;
                        }

                        angle += Math.PI / 8;
                        radius += 0.015;



                        for (int i = 0; i < orbitingDisplays.size(); i++) {
                            BlockDisplay display = orbitingDisplays.get(i);

                            double offsetAngle = angle + i * (Math.PI * 2 / orbitingDisplays.size());
                            double x = Math.cos(offsetAngle) * radius;
                            double z = Math.sin(offsetAngle) * radius;

                            // Si quieres que orbite justo a la altura del jugador + 1 bloque:
                            Vector playerPos = player.getLocation().toVector();
                            Location target = new Location(
                                    world,
                                    playerPos.getX() + x,
                                    playerPos.getY() + 0.2, // un bloque arriba de la cabeza, por ejemplo
                                    playerPos.getZ() + z
                            );

                            // Teletransporta directamente al cofre a la posición exacta
                            display.teleport(target);

                            // Partícula de “rastro” verde opcional
                            world.spawnParticle(
                                    Particle.DUST,
                                    target, 1,
                                    0, 0, 0,
                                    0,
                                    new Particle.DustOptions(Color.LIME, 1)
                            );
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0L, 1L);

                crateBlock.getTask().add(orbiting);
            }
        }.runTaskTimer(Main.getInstance(), 0L, 5L);

        crateBlock.getTask().add(initial);

    }

    private static void spawnChests(Location center, Material restore, CrateBlock crateBlock, Player player) {
        World world = center.getWorld();
        int[][] offsets = {
                {-2, 1}, {-2, -1}, // Oeste
                {2, 1}, {2, -1},   // Este
                {1, -2}, {-1, -2}, // Norte
                {1, 2}, {-1, 2}    // Sur
        };


        List<Location> chestLocations = new ArrayList<>();
        List<BlockData> originalBlockData = new ArrayList<>();

        for (int[] offset : offsets) {
            Location loc = center.clone().add(offset[0], 0, offset[1]);
            chestLocations.add(loc);
            originalBlockData.add(loc.getBlock().getBlockData().clone());

            crateBlock.getOriginalBlockData().add(loc.getBlock().getBlockData().clone());
        }

        for (int i = 0; i < chestLocations.size(); i++) {
            Location chestLoc = chestLocations.get(i);
            int delay = i * 10;

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                world.spawnParticle(Particle.EXPLOSION, chestLoc.clone().add(0.0, 0, 0.0), 30, 0.2, 0.2, 0.2, 0);
                //spawnExplosionEffect(chestLoc);

                chestLoc.getBlock().setType(Material.CHEST);
                Block block = chestLoc.getBlock();
                if (block.getBlockData() instanceof Directional directional) {
                    Vector direction = center.clone().subtract(chestLoc).toVector();
                    BlockFace face = yawToFace(getYawFromVector(direction));
                    directional.setFacing(face);
                    block.setBlockData(directional);
                }

                crateBlock.getChest().add(block.getLocation());

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    BlockState state = chestLoc.getBlock().getState();
                    if (state instanceof TileState tileState) {
                        tileState.getPersistentDataContainer().set(NameSpaceUtils.rewardVirtual, PersistentDataType.STRING, crateBlock.getCrate().getName());
                        tileState.update();
                    }
                }, 4L);

            }, delay);
        }



        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.YELLOW + "Rewards in progress...", BarColor.YELLOW, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        playerBoss.put(player.getUniqueId(), bossBar);


        BukkitTask taskCheck = new BukkitRunnable() {
            private boolean countdownStarted = false;
            private int countdown = ((CrateVirtual) crateBlock.getCrate()).getCountDown();
            private int elapsedTime = 0;
            private final int maxTime = ((CrateVirtual) crateBlock.getCrate()).getMaxTime(); // tiempo máximo permitido

            @Override
            public void run() {
                elapsedTime++;

                int currentReward = crateBlock.getCurrentReward();
                int maxReward = ((CrateVirtual) crateBlock.getCrate()).getMax_reward();

                // Actualizar BossBar
                double progress = 1.0 - (double) elapsedTime / maxTime;
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

                if (currentReward >= maxReward || elapsedTime >= maxTime) {
                    if (!countdownStarted) {
                        countdownStarted = true;

                        List<Location> unusedChests = chestLocations.stream()
                                .filter(loc ->
                                        crateBlock.getChestUsed().stream().noneMatch(used ->
                                                used.getBlockX() == loc.getBlockX() &&
                                                        used.getBlockY() == loc.getBlockY() &&
                                                        used.getBlockZ() == loc.getBlockZ()
                                        )
                                )
                                .toList();

                        for (Location chestLoc : unusedChests) {
                            List<ItemReward> rewards = crateBlock.getCrate().getRewards();
                            double totalWeight = rewards.stream().mapToDouble(ItemReward::getChance).sum();
                            Random random = new Random();

                            ItemReward itemReward = PlayerInteractRewardListener.getWeightedRandomReward(rewards, totalWeight, random);
                            if (itemReward != null) {
                                sendHologramReward(itemReward.getName(), chestLoc, crateBlock);
                                sendItemEntityToPlayer(itemReward.getItemStack(), chestLoc, crateBlock);
                                crateBlock.getChestUsed().add(chestLoc);
                            }
                        }

                        PlayerManager playerManager = Main.getInstance().getPlayerManager();
                        List<String> announce = ((CrateVirtual) crateBlock.getCrate()).getAnnouncementFinish();
                        List<ItemReward> itemRewards = crateBlock.getItemsRewards();

                        if (!announce.isEmpty()) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                PlayerData data = playerManager.getPlayerData(p);
                                if (!data.isNotifiedReward()) continue;

                                for (String line : announce) {
                                    if (line.contains("{reward}")) {
                                        for (ItemReward ir : itemRewards) {
                                            String rewardLine = "- " + ir.getName();
                                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardLine));
                                        }
                                    } else {
                                        String msg = line
                                                .replace("{player}", player.getName())
                                                .replace("{crate}", crateBlock.getCrate().getName());
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                                    }
                                }
                            }
                        }
                    }

                    if (countdown > 0) {
                        countdown--;
                        return;
                    }

                    // Fin del proceso: restaurar bloques y limpiar
                    cancel();

                    for (int i = 0; i < chestLocations.size(); i++) {
                        chestLocations.get(i).getBlock().setBlockData(originalBlockData.get(i), false);
                    }

                    crateBlock.getLocation().getBlock().setType(restore);
                    crateBlock.deleteEntitiesVisuals();

                    for (Map.Entry<Location, BlockData> entry : crateBlock.getOriginales().entrySet()) {
                        Location loc = entry.getKey();
                        loc.getBlock().setBlockData(entry.getValue(), false);
                    }

                    crateBlock.getOriginalBlockData().clear();
                    crateBlock.getOriginales().clear();
                    crateBlock.getChest().clear();
                    crateBlock.getTask().forEach(BukkitTask::cancel);
                    crateBlock.getTask().clear();
                    crateBlock.getChestUsed().clear();
                    crateBlock.generateHologram();
                    crateBlock.setCurrentReward(0);
                    crateBlock.getItemsRewards().clear();
                    crateBlock.setOpener(null);
                    MapUtils.playerOpenVirtual.remove(player.getUniqueId());

                    playerBoss.remove(player.getUniqueId());
                    bossBar.removeAll();
                }
            }
        }.runTaskTimer(Main.getInstance(), 40L, 40L);

        crateBlock.getTask().add(taskCheck);
    }

    static Map<UUID, BossBar> playerBoss = new HashMap<>();

    public static void stop(CrateBlock crateBlock, Player player) {
        for(BukkitTask task : crateBlock.getTask()){
            task.cancel();
        }

        if(playerBoss.containsKey(player.getUniqueId())) {
            BossBar bossBar = playerBoss.remove(player.getUniqueId());
            bossBar.removeAll();
        }




        List<Location> chestLocations = crateBlock.getChest();
        List<BlockData> originalBlockData = crateBlock.getOriginalBlockData();

        for (int i = 0; i < chestLocations.size(); i++) {
            chestLocations.get(i).getBlock().setBlockData(originalBlockData.get(i), false);
        }


        for (Map.Entry<Location, BlockData> entry : crateBlock.getOriginales().entrySet()) {
            Location loc = entry.getKey();
            loc.getBlock().setBlockData(entry.getValue(), false);
        }


        for (int i = 0; i < chestLocations.size(); i++) {
            chestLocations.get(i).getBlock().setBlockData(originalBlockData.get(i), false);
        }


        crateBlock.getOriginales().clear();
        crateBlock.getChest().clear();
        crateBlock.getTask().forEach(BukkitTask::cancel);
        crateBlock.getTask().clear();
        crateBlock.getChestUsed().clear();
        crateBlock.generateHologram();
        crateBlock.setCurrentReward(0);
        crateBlock.getItemsRewards().clear();
        crateBlock.setOpener(null);

        crateBlock.getLocation().getBlock().setType(crateBlock.getOriginalMaterial());
        crateBlock.setOriginalMaterial(null);


        crateBlock.deleteEntitiesVisuals();

        MapUtils.playerOpenVirtual.remove(player.getUniqueId());

    }

    private static float getYawFromVector(Vector vec) {
        return (float) Math.toDegrees(Math.atan2(-vec.getX(), vec.getZ()));
    }

    private static BlockFace yawToFace(float yaw) {
        yaw = yaw < 0 ? yaw + 360 : yaw;
        yaw %= 360;

        if (yaw < 45) return BlockFace.SOUTH;
        else if (yaw < 135) return BlockFace.WEST;
        else if (yaw < 225) return BlockFace.NORTH;
        else if (yaw < 315) return BlockFace.EAST;
        else return BlockFace.SOUTH;
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

}