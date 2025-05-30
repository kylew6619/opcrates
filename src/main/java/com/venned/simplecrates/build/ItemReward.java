package com.venned.simplecrates.build;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ItemReward {

    ItemStack itemStack;
    double chance;
    List<String> commands;
    String name;
    boolean visible;
    List<UUID> disabledPlayers;
    boolean glow;
    String messageWon;

    public ItemReward(String name, ItemStack itemStack, double chance, List<String> commands, boolean visible, List<UUID> disabledPlayers, boolean glow, String messageWin) {
        this.name = name;
        this.itemStack = itemStack;
        this.chance = chance;
        this.commands = commands;
        this.visible = visible;
        this.disabledPlayers = disabledPlayers;
        this.glow = glow;
        this.messageWon = messageWin;

    }


    public void setMessageWon(String messageWon) {
        this.messageWon = messageWon;
    }

    public String getMessageWon() {
        return messageWon;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public boolean isGlow() {
        return glow;
    }

    public List<UUID> getDisabledPlayers() {
        return disabledPlayers;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<String> getCommands() {
        return commands;
    }

    public double getChance() {
        return chance;
    }
}
