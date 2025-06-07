package com.venned.simplecrates.build.virtual;

import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.interfaces.CrateInterface;
import com.venned.simplecrates.interfaces.Opening;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CrateVirtual implements Opening, CrateInterface {


    String name;
    String previewTitle;
    String displayName;
    List<ItemReward> rewards;
    ItemStack item;
    int max_reward;
    List<String> loreS;
    List<String> hologramText;
    ItemStack itemKey;
    List<String> announcementFinish;
    List<String> announcementStart;
    boolean announce;

    Material openingMaterialBlock;
    Material openingMaterialStair;

    int countDown;
    int maxTime;

    Map<UUID, Integer> owned;

    public CrateVirtual(String name, List<ItemReward> rewards, String displayName, int max_reward, List<String> lore, List<String> hologramText, ItemStack itemKeyG, List<String> announcement, String previewTitle, List<String> announcementStart, boolean announce, Map<UUID, Integer> owned, int countDown, Material openingMaterialBlock, Material openingMaterialStair, int maxTime) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = max_reward;
        this.loreS = lore;
        this.hologramText = hologramText;
        this.previewTitle = previewTitle;
        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> newLore = new ArrayList<>();
        for(String s : lore){
            s = ChatColor.translateAlternateColorCodes('&', s);
            newLore.add(s);
        }
        itemMeta.setLore(newLore);

        if(itemKeyG.getItemMeta() != null){
            if(!itemKeyG.getItemMeta().getPersistentDataContainer().has(NameSpaceUtils.key)){
                ItemMeta itemMeta2 = itemKeyG.getItemMeta();
                itemMeta2.getPersistentDataContainer().set(NameSpaceUtils.key, PersistentDataType.STRING, name);
                itemKeyG.setItemMeta(itemMeta2);
            }
        }
        itemKey = itemKeyG;


        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
        this.announcementFinish = announcement;
        this.announcementStart = announcementStart;
        this.announce = announce;

        this.owned = owned;

        this.countDown = countDown;

        this.openingMaterialStair = openingMaterialStair;
        this.openingMaterialBlock = openingMaterialBlock;

        this.maxTime = maxTime;
    }

    public CrateVirtual(String name, List<ItemReward> rewards, String displayName) {
        this.name = name;
        this.rewards = rewards;
        this.displayName = displayName;
        this.max_reward = 3;

        ItemStack itemKey = new ItemStack(Material.PAPER);
        ItemMeta itemMetaKey = itemKey.getItemMeta();

        itemMetaKey.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName) + " Key");
        itemKey.setItemMeta(itemMetaKey);

        this.itemKey = itemKey;


        ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> loreTest = Arrays.asList( "%name% Crate" , "&aSet the lore in your config and reload the Crates", "&aUse the reload command" , "/virtualcrate reload");
        List<String> loreA = new ArrayList<>();
        for(String lore : loreTest){
            lore = ChatColor.translateAlternateColorCodes('&', lore);
            loreA.add(lore);
        }
        itemMeta.setLore(loreA);

        this.previewTitle = "Preview " + getDisplayName();
        this.loreS = loreTest;
        this.hologramText = loreTest;
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.getPersistentDataContainer().set(NameSpaceUtils.crate, PersistentDataType.STRING, name);
        itemStack.setItemMeta(itemMeta);
        this.item = itemStack;
        List<String> announce = new ArrayList<>();
        announce.add("&dRewards Crate " + this.getDisplayName());
        announce.add("{reward}");
        this.announcementFinish = announce;

        List<String> announceStart = new ArrayList<>();
        announceStart.add("&dOpening Crate " + this.getDisplayName());
        announceStart.add("{player}");

        this.announcementStart = announceStart;
        this.announce = true;

        this.countDown = 10;

        this.openingMaterialBlock = Material.QUARTZ_BLOCK;
        this.openingMaterialStair = Material.QUARTZ_STAIRS;

        this.owned = new HashMap<>();

        this.maxTime = 10;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public Map<UUID, Integer> getOwned() {
        return owned;
    }

    public String getName() {
        return name;
    }

    public int getCountDown() {
        return countDown;
    }

    public Material getOpeningMaterialBlock() {
        return openingMaterialBlock;
    }

    public Material getOpeningMaterialStair() {
        return openingMaterialStair;
    }

    public void addReward(ItemReward item) {
        rewards.add(item);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviewTitle() {
        return previewTitle;
    }

    public void setPreviewTitle(String previewTitle) {
        this.previewTitle = previewTitle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<ItemReward> getRewards() {
        return rewards;
    }

    @Override
    public void openCrate(Player player) {

    }

    public void setRewards(List<ItemReward> rewards) {
        this.rewards = rewards;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getMax_reward() {
        return max_reward;
    }

    public void setMax_reward(int max_reward) {
        this.max_reward = max_reward;
    }

    public List<String> getLoreS() {
        return loreS;
    }

    public void setLoreS(List<String> loreS) {
        this.loreS = loreS;
    }

    public List<String> getHologramText() {
        return hologramText;
    }

    public void setHologramText(List<String> hologramText) {
        this.hologramText = hologramText;
    }

    public ItemStack getItemKey() {
        return itemKey;
    }

    public void setItemKey(ItemStack itemKey) {
        this.itemKey = itemKey;
    }

    public List<String> getAnnouncementFinish() {
        return announcementFinish;
    }

    public void setAnnouncementFinish(List<String> announcementFinish) {
        this.announcementFinish = announcementFinish;
    }

    public List<String> getAnnouncementStart() {
        return announcementStart;
    }

    public void setAnnouncementStart(List<String> announcementStart) {
        this.announcementStart = announcementStart;
    }

    public boolean isAnnounce() {
        return announce;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }
}
