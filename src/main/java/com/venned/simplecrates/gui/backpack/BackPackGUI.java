package com.venned.simplecrates.gui.backpack;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.BackPackKey;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.player.CrateKeyPlayer;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.player.gui.PlayerGUIBackPack;
import com.venned.simplecrates.enums.ActionBackPack;
import com.venned.simplecrates.manager.BackPackConfig;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.CrateUtils;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BackPackGUI implements Listener {
    private final BackPackConfig backPackConfig;
    private final PlayerManager playerManager;
    private final Plugin plugin;

    Map<UUID, PlayerGUIBackPack> playerInGuiBackPack = new HashMap<>();

    public BackPackGUI(BackPackConfig backPackConfig, PlayerManager playerManager, Plugin plugin) {
        this.backPackConfig = backPackConfig;
        this.playerManager = playerManager;
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openInventory(Player player) {
        FileConfiguration config = backPackConfig.getBackPackConfig();
        String title = config.getString("gui.title", "&6&lYour Key Backpack").replace("&", "§");
        int size = config.getInt("gui.size", 54);
        Inventory inventory = Bukkit.createInventory(null, size, title);

        Map<String, Object> layout = config.getConfigurationSection("gui.layout").getValues(false);

        for (String slotKey : layout.keySet()) {
            int slot = Integer.parseInt(slotKey);
            String keyType = layout.get(slotKey).toString();
            if (config.getConfigurationSection("keys." + keyType) != null) {
                ItemStack itemStack = createKeyItem(player, keyType, config, ActionBackPack.OPEN);

                inventory.setItem(slot, itemStack);
            } else if (keyType.equalsIgnoreCase("glass")) {
                inventory.setItem(slot, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            } else if (keyType.equalsIgnoreCase("close_button")) {
                ItemStack closeButton = createConfiguredItem("close_button", config);

                /*
                ItemMeta meta = closeButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cClose");
                    meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "close");
                    closeButton.setItemMeta(meta);
                }

                 */

                inventory.setItem(slot, closeButton);
            } else if(keyType.equalsIgnoreCase("change_mode")){
                ItemStack changeButton = createConfiguredItem("change_mode", config);

                /*
                ItemMeta meta = changeButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cCHANGE MODE");
                    meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "change_mode");
                    changeButton.setItemMeta(meta);
                }

                 */

                inventory.setItem(slot, changeButton);
            }
        }
            playerInGuiBackPack.remove(player.getUniqueId());

            playerInGuiBackPack.put(player.getUniqueId(), new PlayerGUIBackPack(player, inventory, ActionBackPack.OPEN));
            player.openInventory(inventory);

    }

    public void openInventory(Player player, ActionBackPack actionBackPack, Inventory inventory, PlayerGUIBackPack playerInGuiBackPack) {
        FileConfiguration config = backPackConfig.getBackPackConfig();
        String title = config.getString("gui.title", "&6&lYour Key Backpack").replace("&", "§");
        int size = config.getInt("gui.size", 54);


        for(ItemStack itemStack : inventory.getContents()) {
            if(itemStack == null) continue;
            inventory.removeItem(itemStack);
        }

      //  Inventory inventory = Bukkit.createInventory(null, size, title);

        Map<String, Object> layout = config.getConfigurationSection("gui.layout").getValues(false);

        for (String slotKey : layout.keySet()) {
            int slot = Integer.parseInt(slotKey);
            String keyType = layout.get(slotKey).toString();
            if (config.getConfigurationSection("keys." + keyType) != null) {
                ItemStack itemStack = createKeyItem(player, keyType, config, actionBackPack);
                inventory.setItem(slot, itemStack);
            } else if (keyType.equalsIgnoreCase("glass")) {
                inventory.setItem(slot, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            } else if (keyType.equalsIgnoreCase("close_button")) {
                ItemStack closeButton = createConfiguredItem("close_button", config);

                /*
                ItemMeta meta = closeButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cClose");
                    meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "close");
                    closeButton.setItemMeta(meta);
                }

                 */
                inventory.setItem(slot, closeButton);
            } else if(keyType.equalsIgnoreCase("change_mode")){
                ItemStack changeButton = createConfiguredItem("change_mode", config);

                /*
                ItemMeta meta = changeButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§cCHANGE MODE");
                    meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "change_mode");
                    changeButton.setItemMeta(meta);
                }

                 */

                inventory.setItem(slot, changeButton);
            }
        }

        playerInGuiBackPack.setActionBackPack(actionBackPack);


    }


    @EventHandler
    public void onClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();


        if(event.getClickedInventory() == null)return;
        if(!playerInGuiBackPack.containsKey(player.getUniqueId())) return;
        if(!event.getView().getTopInventory().equals(playerInGuiBackPack.get(player.getUniqueId()).getInventory())) return;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if(item == null) return;
        if(item.getType() == Material.BARRIER){
            player.closeInventory();
            return;
        }


        if (item.getItemMeta() != null) {
            if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.backpackGUI)){
                if("change_mode".equalsIgnoreCase(item.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.backpackGUI, PersistentDataType.STRING))){
            PlayerGUIBackPack playerGUIBackPack = playerInGuiBackPack.get(player.getUniqueId());
            if (playerGUIBackPack != null) {
                //     player.closeInventory();

                // Alternar entre los modos OPEN -> WITHDRAW -> DEPOSIT -> OPEN
                ActionBackPack currentMode = playerGUIBackPack.getActionBackPack();
                ActionBackPack newMode;


                if (currentMode == ActionBackPack.OPEN) {
                    newMode = ActionBackPack.WITHDRAW;
                } else if (currentMode == ActionBackPack.WITHDRAW) {
                    newMode = ActionBackPack.DEPOSIT;
                } else {
                    newMode = ActionBackPack.OPEN;
                }

                openInventory(player, newMode, playerGUIBackPack.getInventory(), playerGUIBackPack);
                return;
            }
            }
            }

        }

        if(item.getItemMeta() != null){



            if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.backpackGUI, PersistentDataType.STRING)){



                String key = item.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.backpackGUI, PersistentDataType.STRING);
                PlayerData playerData = playerManager.getPlayerData(player);



                if(playerData != null){

                    CrateKeyPlayer crateKeyPlayer = playerData.getBackPack().getCrateKeys()
                            .stream().filter(c->c.getCrate().getName().equalsIgnoreCase(key))
                            .findFirst().orElse(null);



                    BackPackKey backPackKey = null;
                    if(crateKeyPlayer == null) {
                        Crate crate = Main.getInstance().getCrateManager().getCrateByName(key);
                        if(crate != null) {
                        List<BackPackKey> backPackKeys = backPackConfig.getKeys();
                        backPackKey = backPackKeys.stream()
                                .filter(p->p.getCrate().equals(crate)).findFirst().orElse(null);
                        if(backPackKey == null){
                              return;
                        }
                        crateKeyPlayer = new CrateKeyPlayer(crate, 1);
                        }
                    }

                    Crate crate = Main.getInstance().getCrateManager().getCrateByName(key);
                    List<BackPackKey> backPackKeys = backPackConfig.getKeys();
                    backPackKey = backPackKeys.stream()
                            .filter(p->p.getCrate().equals(crate)).findFirst().orElse(null);

                    if(backPackKey == null){
                        return;
                    }

                    if(crateKeyPlayer == null){
                        return;
                    }
                    


                    PlayerGUIBackPack playerGUIBackPack = playerInGuiBackPack.get(player.getUniqueId());
                    if(playerGUIBackPack == null) return;



                    ClickType clickType = event.getClick();

                    int maxAmount = 0;
                    boolean moveAll = false;

                    if (clickType.isLeftClick()) {
                        maxAmount = 1; // Clic izquierdo -> 1 ítem
                    } else if (clickType == ClickType.SHIFT_LEFT) {
                        maxAmount = 100; // Shift + Clic -> Hasta 100 ítems
                    }else if ( clickType == ClickType.SHIFT_RIGHT){
                        moveAll = true;
                    } else if (clickType == ClickType.RIGHT) {
                        maxAmount = 10; // Shift + Clic -> Hasta 100 ítems
                    } else {
                        return; // No hacemos nada si es otro tipo de clic
                    }


                    if (playerGUIBackPack.getActionBackPack() == ActionBackPack.WITHDRAW) {
                        if (!backPackConfig.isAllow_withdraw()) {
                            player.sendMessage(Main.getMessage("backpack-disabled-withdraw", Map.of()));
                            return;
                        }
                        int amount_withdraw;
                        if(moveAll){
                             amount_withdraw = crateKeyPlayer.getKeys();
                        } else {
                             amount_withdraw = Math.min(crateKeyPlayer.getKeys(), maxAmount); // Aplica el límite por clic
                        }
                        if (amount_withdraw <= 0) return;

                        FileConfiguration config = backPackConfig.getBackPackConfig();

                        int slot = event.getSlot();
                        ItemStack update = createKeyItem(player, crateKeyPlayer.getCrate().getName(), config, playerGUIBackPack.getActionBackPack());
                        event.getClickedInventory().setItem(slot, update);

                        ItemStack keyCrate = crateKeyPlayer.getCrate().getItemKey().clone();
                        keyCrate.setAmount(amount_withdraw);
                        player.getInventory().addItem(keyCrate);
                        crateKeyPlayer.desIncrement(amount_withdraw);

                        player.sendMessage(Main.getMessage("backpack-left-click",
                                Map.of("crate", crateKeyPlayer.getCrate().getName(), "amount", "" + amount_withdraw)));

                        openInventory(player, playerGUIBackPack.getActionBackPack(), playerInGuiBackPack.get(player.getUniqueId()).getInventory(), playerGUIBackPack);

                    } else if (playerGUIBackPack.getActionBackPack() == ActionBackPack.OPEN) {
                        int amount_open = moveAll ? crateKeyPlayer.getKeys() : Math.min(crateKeyPlayer.getKeys(), maxAmount);

                        if (amount_open <= 0) return;


                        // Revisar cuántos slots vacíos hay en el inventario del jugador
                        long emptySlots = Arrays.stream(player.getInventory().getStorageContents())
                                .filter(e-> e == null|| e.getType() == Material.AIR)
                                .count();



                        if (!hasEnoughSpace(player, crateKeyPlayer.getCrate(), amount_open)) {
                            player.sendMessage(Main.getMessage("backpack-not-enough-space", Map.of(
                                    "available", String.valueOf(emptySlots)
                            )));
                            return;
                        }



                        FileConfiguration config = backPackConfig.getBackPackConfig();

                        int slot = event.getSlot();
                        ItemStack update = createKeyItem(player, crateKeyPlayer.getCrate().getName(), config, playerGUIBackPack.getActionBackPack());
                        event.getClickedInventory().setItem(slot, update);

                        CrateUtils.openAllKeys(player, crateKeyPlayer.getCrate(), amount_open);
                        crateKeyPlayer.desIncrement(amount_open);

                        player.sendMessage(Main.getMessage("backpack-right-click",
                                Map.of("crate", crateKeyPlayer.getCrate().getName(), "amount", "" + amount_open)));

                        openInventory(player, playerGUIBackPack.getActionBackPack(), playerInGuiBackPack.get(player.getUniqueId()).getInventory(), playerGUIBackPack);

                    }else if (playerGUIBackPack.getActionBackPack() == ActionBackPack.DEPOSIT) {
                        List<ItemStack> keysPlayerInventory = new ArrayList<>();
                        int totalDepositable = 0;

                        for (ItemStack itemStack : player.getInventory().getContents()) {
                            if (itemStack != null && itemStack.isSimilar(crateKeyPlayer.getCrate().getItemKey())) {
                                keysPlayerInventory.add(itemStack);
                                totalDepositable += itemStack.getAmount();
                            }
                        }
                        int amountToDeposit;
                        if(moveAll){
                            amountToDeposit = totalDepositable;
                        } else {
                            amountToDeposit = Math.min(totalDepositable, maxAmount); // Aplica el límite por clic
                        }

                        if (amountToDeposit <= 0) return;

                        int maxDepositBackPack = backPackKey.getAmountMax(); // Máximo que puede almacenar la mochila
                        int currentlyStored = crateKeyPlayer.getKeys();

                        int availableSpace = (maxDepositBackPack == -1) ? Integer.MAX_VALUE : maxDepositBackPack - currentlyStored;

                        if (availableSpace <= 0) {
                            player.sendMessage(Main.getMessage("backpack-full", Map.of(
                                    "crate", crateKeyPlayer.getCrate().getName()
                            )));
                            return;
                        }

                        amountToDeposit = Math.min(amountToDeposit, availableSpace); // Ajustamos si excede el límite

                        // Removemos ítems del inventario del jugador
                        int remainingToDeposit = amountToDeposit;
                        for (ItemStack stack : keysPlayerInventory) {
                            if (remainingToDeposit <= 0) break;
                            int removeAmount = Math.min(stack.getAmount(), remainingToDeposit);
                            stack.setAmount(stack.getAmount() - removeAmount);
                            remainingToDeposit -= removeAmount;
                        }

                        FileConfiguration config = backPackConfig.getBackPackConfig();

                        int slot = event.getSlot();
                        ItemStack update = createKeyItem(player, crateKeyPlayer.getCrate().getName(), config, playerGUIBackPack.getActionBackPack());
                        event.getClickedInventory().setItem(slot, update);

                        crateKeyPlayer.increment(amountToDeposit);

                        player.sendMessage(Main.getMessage("backpack-deposit", Map.of(
                                "crate", crateKeyPlayer.getCrate().getName(),
                                "amount", String.valueOf(amountToDeposit))));

                        openInventory(player, playerGUIBackPack.getActionBackPack(),
                                playerInGuiBackPack.get(player.getUniqueId()).getInventory(), playerGUIBackPack);
                    }

                }
            }
        }
    }


    private ItemStack createConfiguredItem(String key, FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("items." + key);
        if (section == null) return null;

        Material material = Material.getMaterial(section.getString("material", "STONE").toUpperCase());
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("display_name", "&fItem")));
            List<String> lore = section.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList()));
            }

            // Aquí puedes añadir el PersistentData si es necesario
            if (key.equalsIgnoreCase("close_button")) {
                meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "close");
            } else if (key.equalsIgnoreCase("change_mode")) {
                meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, "change_mode");
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean hasEnoughSpace(Player player, Crate crate, int cratesToOpen) {
        Logger logger = Bukkit.getLogger();
        // Paso 1: Obtener los ítems que ya están en el inventario
        ItemStack[] contents = player.getInventory().getStorageContents();
        List<ItemStack> simulatedContents = new ArrayList<>(Arrays.asList(contents));

        // Paso 2: Obtener recompensas
        List<ItemStack> rewards = new ArrayList<>();
        for (int i = 0; i < cratesToOpen; i++) {
            List<ItemReward> crateRewards = crate.rollRewards();
            for (ItemReward reward : crateRewards) {
                rewards.add(reward.getItemStack().clone());
            }
        }

        // Paso 3: Simular agregar los ítems al inventario
        for (ItemStack reward : rewards) {
            int amountToAdd = reward.getAmount();
            Material type = reward.getType();
            ItemMeta meta = reward.getItemMeta();

            boolean inserted = false;

            // Buscar si hay stacks parcialmente llenos compatibles
            for (ItemStack item : simulatedContents) {
                if (item != null && item.getType() == type && item.getItemMeta().equals(meta)) {
                    int space = item.getMaxStackSize() - item.getAmount();
                    if (space > 0) {
                        int toAdd = Math.min(space, amountToAdd);
                        item.setAmount(item.getAmount() + toAdd);
                        amountToAdd -= toAdd;
                        if (amountToAdd <= 0) {
                            inserted = true;
                            break;
                        }
                    }
                }
            }


            // Si aún quedan ítems, buscar espacios vacíos
            while (amountToAdd > 0) {
                int toAdd = Math.min(amountToAdd, reward.getMaxStackSize());
                boolean added = false;

                for (int i = 0; i < simulatedContents.size(); i++) {
                    if (simulatedContents.get(i) == null || simulatedContents.get(i).getType() == Material.AIR) {
                        ItemStack newStack = reward.clone();
                        newStack.setAmount(toAdd);
                        simulatedContents.set(i, newStack);
                        amountToAdd -= toAdd;
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    return false; // No hay espacio
                }
            }
        }

        return true;
    }

    private ItemStack createKeyItem(Player player, String keyType, FileConfiguration config, ActionBackPack actionBackPack) {
        String path = "keys." + keyType;
        Material material = Material.valueOf(config.getString(path + ".material", "STONE"));
        String displayName = config.getString(path + ".display_name", keyType).replace("&", "§");
        int storageLimit = config.getInt(path + ".storage_limit", -1);

        int amountOwned = playerManager.getPlayerData(player).getBackPack().getCrateKeys().stream()
                .filter(c -> c.getCrate().getName().equalsIgnoreCase(keyType))
                .findFirst()
                .map(CrateKeyPlayer::getKeys)
                .orElse(0);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(NameSpaceUtils.backpackGUI, PersistentDataType.STRING, keyType);
            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();

            // Añadir lore principal del key
            for (String line : config.getStringList(path + ".lore")) {
                lore.add(line.replace("&", "§")
                        .replace("%amount%", String.valueOf(amountOwned))
                        .replace("%limit%", storageLimit == -1 ? "Unlimited" : String.valueOf(storageLimit)));
            }

            lore.add(" ");

            // Añadir el lore adicional configurable
            for (String line : config.getStringList("lore-add")) {
                lore.add(line.replace("&", "§").replace("%action%", actionBackPack.name().toLowerCase()));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}

