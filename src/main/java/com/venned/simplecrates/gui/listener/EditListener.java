package com.venned.simplecrates.gui.listener;

import com.venned.simplecrates.Main;
import com.venned.simplecrates.build.ItemReward;
import com.venned.simplecrates.build.player.gui.PlayerEditChances;
import com.venned.simplecrates.enums.EditMode;
import com.venned.simplecrates.utils.NameSpaceUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.persistence.PersistentDataType;

public class EditListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerEditChances editChances = Main.getInstance().getEditChances().getPlayerEditChances()
                .stream().filter(p->p.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
        if(editChances == null) return;
        if(e.getClickedInventory() == null) return;

        if(e.getClickedInventory().equals(editChances.getInventory())) {
            e.setCancelled(true);

            if(e.getCurrentItem() == null) return;
            if(e.getCurrentItem() == null) return;

            String nameReward = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NameSpaceUtils.rewardName, PersistentDataType.STRING);

            ItemReward itemReward = editChances.getLootBox().getRewards()
                    .stream().filter(n->n.getName().equals(nameReward)).findFirst().orElse(null);
            if(itemReward == null) return;

            editChances.setCurrenEdit(itemReward);

            if(e.getClick() == ClickType.LEFT) {
                Main.getInstance().getEditOptions().openInventory(player, itemReward, editChances);
                return;
            } else if(e.getClick() == ClickType.RIGHT){
                player.sendMessage("§c§l(!) §cWrite §a'confirm' §cin the chat for delete item");
                editChances.setEditMode(EditMode.DELETE);
            }

            player.closeInventory();
        }
    }

    @EventHandler
    public void onClick2(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerEditChances editChances = Main.getInstance().getEditChances().getPlayerEditChances()
                .stream().filter(p->p.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
        if(editChances == null) return;
        if(e.getClickedInventory() == null) return;


        if(e.getClickedInventory().equals(editChances.getInventory())) {

            e.setCancelled(true);

            if(e.getCurrentItem() == null) return;
            if(e.getCurrentItem() == null) return;

            if(editChances.getCurrenEdit() != null) {


                switch (e.getCurrentItem().getType()) {
                    case PAPER -> {
                        player.sendMessage("§awrite chance in chat");
                        player.closeInventory();
                        editChances.setEditMode(EditMode.EDIT);
                        break;
                    }

                    case BARRIER -> {
                        player.sendMessage("§aCommand Deleted");
                        editChances.getCurrenEdit().getCommands().clear();
                        break;
                    }

                    case WRITABLE_BOOK -> {
                        player.sendMessage("§aWrite commands in chat");
                        player.closeInventory();
                        editChances.setEditMode(EditMode.COMMAND);
                        break;
                    }

                    case REDSTONE -> {
                        player.closeInventory();
                        if(editChances.getCurrenEdit().isVisible()){
                            editChances.getCurrenEdit().setVisible(false);
                        } else {
                            editChances.getCurrenEdit().setVisible(true);
                        }



                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            Main.getInstance().getEditOptions().openInventory(player, editChances.getCurrenEdit(), editChances);
                        }, 5);

                        break;

                    }

                    case CHEST -> {
                        player.closeInventory();

                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->{
                            editChances.setCurrenEdit(null);
                            Main.getInstance().getEditChances().openInventory(player, editChances.getLootBox());

                        }, 5);

                        break;
                    }




                }
            }
        }
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event){

            Player player = event.getPlayer();
            PlayerEditChances editChances = Main.getInstance().getEditChances().getPlayerEditChances()
                    .stream().filter(p -> p.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);


            if (editChances == null || editChances.getEditMode() == null) return;

            String message = event.getMessage();

            event.setCancelled(true);


            if(editChances.getEditMode() == EditMode.EDIT) {
                try {
                    double newChance = Double.parseDouble(message);
                    if (newChance < 0 || newChance > 100) { // Asegurar valores válidos (puedes ajustar el rango)
                        player.sendMessage("§cThe chance must be between 0 and 100.");
                        return;
                    }

                    editChances.getCurrenEdit().setChance(newChance);
                    player.sendMessage("§aChance updated to " + newChance);

                    editChances.setEditMode(null);

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getEditOptions().openInventory(player, editChances.getCurrenEdit(), editChances);
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage("§cYou must enter a valid number.");
                }
            } else if(editChances.getEditMode() == EditMode.DELETE){
                if(message.equalsIgnoreCase("confirm")){
                    editChances.getLootBox().getRewards().remove(editChances.getCurrenEdit());
                    player.sendMessage("Item remove success");


                    editChances.setCurrenEdit(null);
                    editChances.setEditMode(null);

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getEditChances().openInventory(player, editChances.getLootBox());
                    });
                } else {
                    player.sendMessage("§c§l(!) Cancel Remove Succcess");

                    editChances.setEditMode(null);

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getEditOptions().openInventory(player, editChances.getCurrenEdit(), editChances);
                    });

                }
            } else if(editChances.getEditMode() == EditMode.COMMAND) {
                if (message.equalsIgnoreCase("cancel")) {
                    editChances.setEditMode(null);
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getEditOptions().openInventory(player, editChances.getCurrenEdit(), editChances);
                    });
                } else {
                    player.sendMessage("§aWrite another one or write 'cancel'");
                    player.sendMessage("§aAdd Command Success " + message);
                    editChances.getCurrenEdit().getCommands().add(message);
                }


            }
    }

}
