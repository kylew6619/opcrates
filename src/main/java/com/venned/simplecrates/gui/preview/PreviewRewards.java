package com.venned.simplecrates.gui.preview;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.crate.Crate;
import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.player.PlayerPreview;
import com.venned.simplecrates.build.virtual.CrateVirtual;
import com.venned.simplecrates.gui.virtual.VirtualMenu;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PreviewRewards implements Listener {

    public static  enum TypePreview {

        LOOTBOX,
        CRATE

    }

    Set<PlayerPreview> previewMenu;

    public PreviewRewards(Plugin plugin) {
        this.previewMenu = new HashSet<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onPreview(Player player, List<ItemReward> rewards, String title, TypePreview typePreview, Opening opening, @Nullable CrateBlock crateBlock) {

        Inventory inventory = Bukkit.createInventory(null, 54, title);

        FileConfiguration config = Main.getInstance().getConfig();

        List<ItemReward> visibleRewards = rewards.stream()
                .filter(ItemReward::isVisible)
                .toList();

        List<ItemReward> modified = new ArrayList<>();

        for(int i = 0; i < visibleRewards.size(); i++){
            ItemReward itemReward = visibleRewards.get(i);

            ItemStack itemStack = itemReward.getItemStack().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            List<String> lore = new ArrayList<>(config.getStringList("lore-preview"));

            if (itemMeta.getLore() == null) {

                lore.add(" ");
                if(opening instanceof Crate) {

                    boolean isDisabled = itemReward.getDisabledPlayers().contains(player.getUniqueId());

                    String statusKey = isDisabled ? "DISABLED" : "ENABLED";
                    ConfigurationSection statusSection = config.getConfigurationSection("status-color." + statusKey);
                    String colorName = statusSection != null ? statusSection.getString("color", "GRAY") : "GRAY";
                    boolean isBold = statusSection != null && statusSection.getBoolean("bold", false);


                    ChatColor chatColor;
                    try {
                        chatColor = ChatColor.valueOf(colorName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        chatColor = ChatColor.GRAY;
                    }

                    String status = chatColor + (isBold ? "" + ChatColor.BOLD : "") + statusKey;



                    for (int z = 0; z < lore.size(); z++) {
                        lore.set(z, lore.get(z)
                                .replace("{chance}", String.valueOf(itemReward.getChance()))
                                .replace("{status}", status)
                                .replace("&", "§")
                        );
                    }
                } else {
                    lore.clear();
                }

                itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());

                itemMeta.setLore(lore);
                } else if (itemMeta.getLore() != null || !itemMeta.getLore().isEmpty()) {
                    List<String> loreGet = itemMeta.getLore();
                    lore.add(" ");
                if(opening instanceof Crate) {

                        for (int z = 0; z < lore.size(); z++) {
                            lore.set(z, lore.get(z)
                                    .replace("{chance}", String.valueOf(itemReward.getChance()))
                                    .replace("{status}", itemReward.getDisabledPlayers().contains(player.getUniqueId()) ? "Disabled" : "Enabled")
                                    .replace("&", "§")
                            );
                        }

                }  else {
                    lore.clear();
                    lore.addAll(loreGet);
                }

                    itemMeta.getPersistentDataContainer().set(NameSpaceUtils.rewardName, PersistentDataType.STRING, itemReward.getName());
                    itemMeta.setLore(lore);
                }
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.addAttributeModifier(Attribute.LUCK, new AttributeModifier(
                    "dummy",
                    0,
                    AttributeModifier.Operation.ADD_NUMBER
            ));

            itemMeta.setDisplayName(itemReward.getName().replace("&", "§"));

            if(itemReward.isGlow()){
                if(itemMeta.getEnchants().isEmpty()) {
                    itemMeta.addEnchant(Enchantment.LOOTING, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

                itemStack.setItemMeta(itemMeta);
                inventory.setItem(i, itemStack);

                modified.add(itemReward);

        }


        /*
        if(crateBlock != null){
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            backMeta.setDisplayName("§c<-");
            backMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            backMeta.getPersistentDataContainer().set(NameSpaceUtils.back, PersistentDataType.BOOLEAN, true);
            back.setItemMeta(backMeta);
            inventory.setItem(53, back);
        }

         */

        player.openInventory(inventory);

        previewMenu.removeIf(p->p.getUUID().equals(player.getUniqueId()));

        PlayerPreview playerPreview = new PlayerPreview(inventory, rewards, player.getUniqueId(), title, typePreview, opening);
        if(crateBlock != null){
            playerPreview.setCrateBlock(crateBlock);
        }

        previewMenu.add(playerPreview);

    }



    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();

        PlayerPreview previewRewards = previewMenu.stream().filter(c->c.getUUID().equals(event.getWhoClicked().getUniqueId())).findFirst().orElse(null);
        if(previewRewards == null) return;
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if(item == null) return;
            if(item.getItemMeta() != null){
                if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.back)){
                    VirtualMenu.open(player, (CrateVirtual) previewRewards.getOpening(), previewRewards.getCrateBlock());
                    return;
                }
                if(item.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.rewardName)){
                    String rewardName = item.getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.rewardName, PersistentDataType.STRING);
                    for(ItemReward itemReward : previewRewards.getRewardList()){
                        if(itemReward.getName().equalsIgnoreCase(rewardName)){
                            if(previewRewards.getTypePreview() == TypePreview.CRATE) {

                                String crateName = ((Crate) previewRewards.getOpening()).getName().toLowerCase(); // nombre del crate

                                int disabledCount = (int) previewRewards.getRewardList().stream()
                                        .filter(r -> r.getDisabledPlayers().contains(playerId))
                                        .count();


                                int maxAllowed = 0;
                                for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
                                    String perm = permInfo.getPermission();
                                    if (perm.toLowerCase().startsWith(crateName + ".disable.")) {
                                        try {
                                            int val = Integer.parseInt(perm.substring((crateName + ".disable.").length()));
                                            if (val > maxAllowed) maxAllowed = val;
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }

                                boolean isCurrentlyDisabled = itemReward.getDisabledPlayers().contains(playerId);

                                if (isCurrentlyDisabled) {
                                    itemReward.getDisabledPlayers().remove(playerId);

                                    player.sendMessage(Main.getMessage("disabled-reward-remove", Map.of("reward", itemReward.getName())));
                                } else {
                                    if (disabledCount < maxAllowed || player.isOp()) {
                                        itemReward.getDisabledPlayers().add(playerId);
                                        player.sendMessage(Main.getMessage("disabled-reward-add", Map.of("reward", itemReward.getName())));
                                    } else {
                                        player.sendMessage(Main.getMessage("disabled-reward-limit", Map.of("max_allowed", ""+ maxAllowed)));
                                        return;
                                    }
                                }


                                onPreview(player, previewRewards.getRewardList(), previewRewards.getTitle(), previewRewards.getTypePreview(), previewRewards.getOpening(), null);

                            }
                        }
                    }
                }
            }


    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){

        Player player = (Player) event.getPlayer();

        PlayerPreview preview = previewMenu.stream().filter(p->p.getUUID().equals(event.getPlayer().getUniqueId())).findFirst().orElse(null);
        if(preview == null) return;
        if(preview.getCrateBlock() != null){
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                VirtualMenu.open(player, (CrateVirtual) preview.getOpening(), preview.getCrateBlock());
            }, 5);

        }

        previewMenu.removeIf(p->p.getUUID().equals(event.getPlayer().getUniqueId()));
    }


}
