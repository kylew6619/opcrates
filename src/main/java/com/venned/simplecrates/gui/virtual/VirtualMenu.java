package com.venned.simplecrates.gui.virtual;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.player.PlayerData;
import com.venned.simplecrates.build.player.gui.PlayerVirtualGUI;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import com.venned.simplecrates.gui.opening.CrateVirtualOpening;
import com.venned.simplecrates.gui.preview.PreviewRewards;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.manager.player.PlayerManager;
import com.venned.simplecrates.utils.NameSpaceUtils;
import jdk.jfr.Enabled;
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

public class VirtualMenu implements Listener {

     static Map<UUID, PlayerVirtualGUI> crateVirtualMap = new HashMap<>();

    public VirtualMenu(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    public static void open(Player player, CrateVirtual crateVirtual, CrateBlock crateBlock) {
        FileConfiguration config = Main.getInstance().getConfig();

        String rawTitle = config.getString("menu-virtual.title", "&cCrate %name%");
        String title = ChatColor.translateAlternateColorCodes('&',
                rawTitle.replace("%name%", crateVirtual.getName()));
        int size = config.getInt("menu-virtual.size", 36);

        Inventory inventory = Bukkit.createInventory(null, size, title);

        ConfigurationSection slotsSection = config.getConfigurationSection("menu-virtual.slots");
        if (slotsSection != null) {
            for (String key : slotsSection.getKeys(false)) {
                int slot = Integer.parseInt(key);
                ConfigurationSection slotData = slotsSection.getConfigurationSection(key);
                if (slotData == null) continue;

                Material material = Material.getMaterial(slotData.getString("material", "STONE").toUpperCase());
                if (material == null) continue;

                String name = slotData.getString("name", "&fItem");
                List<String> lore = slotData.getStringList("lore");

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                            name.replace("%name%", crateVirtual.getName())));

                    int owned = crateVirtual.getOwned().getOrDefault(player.getUniqueId(), 0);

                    List<String> formattedLore = new ArrayList<>();
                    for (String line : lore) {
                        formattedLore.add(ChatColor.translateAlternateColorCodes('&',
                                line
                                        .replace("%name%", crateVirtual.getName())
                                        .replace("%owned%", String.valueOf(owned))
                                        .replace("%max_reward%", String.valueOf(crateVirtual.getMax_reward()))
                        ));
                    }
                    meta.setLore(formattedLore);

                }

                // Manejo de NBT si `crate: yes`
                if (slotData.getBoolean("crate", false)) {
                    meta.getPersistentDataContainer().set(NameSpaceUtils.rewardVirtual, PersistentDataType.STRING, crateVirtual.getName());
                }
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
        }

        crateVirtualMap.remove(player.getUniqueId());
        crateVirtualMap.put(player.getUniqueId(), new PlayerVirtualGUI(player.getUniqueId(), inventory, crateBlock, crateVirtual));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if(!crateVirtualMap.containsKey(player.getUniqueId())) return;
        if(event.getClickedInventory() == null) return;
        if(event.getView().getTopInventory().equals(crateVirtualMap.get(player.getUniqueId()).getInventory())) event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if(item == null) return;
        if(item.getItemMeta() == null) return;
        if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.rewardVirtual)){
            PlayerVirtualGUI playerVirtualGUI = crateVirtualMap.get(player.getUniqueId());

            int amountKey = playerVirtualGUI.getCrateVirtual().getOwned().getOrDefault(player.getUniqueId(), 0);

            if(event.getClick() == ClickType.LEFT) {
                if (amountKey <= 0) {
                    player.sendMessage("§cYou don't have keys to open this chest.");
                    return;
                }

                playerVirtualGUI.getCrateVirtual().getOwned().put(player.getUniqueId(), amountKey - 1);


                    if(playerVirtualGUI.getCrateVirtual().isAnnounce()) {
                        PlayerManager playerManager = Main.getInstance().getPlayerManager();
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData = playerManager.getPlayerData(players);
                            if (playerData.isNotifiedReward()) {
                                for (String a : playerVirtualGUI.getCrateVirtual().getAnnouncementStart()) {
                                    a = a.replace("&", "§");
                                    players.sendMessage(a.replace("{player}", player.getName()).replace("{crate}", playerVirtualGUI.getCrateVirtual().getDisplayName()));
                                }
                            }
                        }
                    }


                CrateVirtualOpening.open(player, playerVirtualGUI.getCrateVirtual(), playerVirtualGUI.getCrateBlock());
            } else if(event.getClick() == ClickType.RIGHT){
                String title = playerVirtualGUI.getCrateBlock().getCrate().getPreviewTitle().replace("&", "§");
                Main.getInstance().getPreviewRewards().onPreview(player, playerVirtualGUI.getCrateBlock().getCrate().getRewards(), title, PreviewRewards.TypePreview.CRATE, (Opening) playerVirtualGUI.getCrateBlock().getCrate(), playerVirtualGUI.getCrateBlock());
            }
        }

    }

}
