package fr.altaks.mcoapi.core.configs;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.timers.PlayerStartsConfiguringTimersEvent;
import fr.altaks.mcoapi.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class TimersConfiguration implements Listener {

    private GameManager manager;
    private Inventory timersConfigInventory = null;
    private boolean hasReadPluginTimers = false;

    private HashMap<ItemStack, String> itemToPath = new HashMap<>();
    private HashMap<String, Object> pathToValue = new HashMap<>();
    private HashMap<String, String> pathToType = new HashMap<>();

    public TimersConfiguration(GameManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerStartsConfiguringTimers(PlayerStartsConfiguringTimersEvent event) {
        if(!hasReadPluginTimers) {
            event.getPlayer().sendMessage(Main.PREFIX + "Vous devez choisir un plugin avant de configurer les timers de celui ci");
            return;
        }
        event.getPlayer().openInventory(timersConfigInventory);
    }

    @EventHandler
    public void onPlayerInteractsWithATimerEvent(InventoryClickEvent event) {
        if(event.getInventory() == null || event.getClickedInventory() == null) return;
        if(!event.getClickedInventory().equals(event.getView().getTopInventory())) return;
        if(!event.getClickedInventory().equals(timersConfigInventory)) return;

        ItemStack clickedItem = event.getCurrentItem();

        event.setCancelled(true);

        String valueType = pathToType.get(itemToPath.get(clickedItem));
        String path = itemToPath.get(clickedItem);

        Object value = pathToValue.get(itemToPath.get(clickedItem));
        switch (valueType) {
            case "seconds":
            case "minutes":
            case "int": {
                int change = 0;
                if(event.isLeftClick()) {
                    change = 1;
                } else {
                    change = -1;
                }
                if(event.isShiftClick()) change *= 10;
                value = (int) value + change;
                if((int) value < 0) value = 0;
                break;
            }
            case "percentage": {

                double change = 0.0d;
                if(event.isLeftClick()) {
                    change = 0.01d;
                } else {
                    change = -0.01d;
                }
                if(event.isShiftClick()) change *= 10.0d;
                value = (double) value + change;
                if((double) value < 0.00d) value = 0.00d;
                break;
            }
            default:
                Main.LOG.devlog("Couldn't manipulate value " + value + " without knowing it's type");
                return;
        }

        // reinput value into values and change the linked line
        Main.LOG.devlog("Player manipulated plugin timer to set it to " + value);

        ItemStack item = clickedItem.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(0, ChatColor.GRAY + "Valeur : " + ((valueType.equals("percentage") ? String.format("%.2f", ((double) value * 100.0d)) : value)));
        meta.setLore(lore);
        item.setItemMeta(meta);

        event.getInventory().setItem(event.getSlot(), item);
        ((Player) event.getWhoClicked()).updateInventory();

        // update item within hashmaps
        itemToPath.put(item, path);
        pathToValue.put(path, value);
    }

    public void readPluginTimers(FileConfiguration input) {
        hasReadPluginTimers = true;

        if(timersConfigInventory != null) {
            timersConfigInventory.clear();
        } else {
            timersConfigInventory = Bukkit.createInventory(null, 9 * 6, "Configuration des timers/taux");
        }

        for(String nonFormattedPath : input.getConfigurationSection("timers").getKeys(false)){
            String timerConfigPath = nonFormattedPath.replace("---", ".");
            String timerDescription = input.getString("timers." + nonFormattedPath + ".description");
            String unit = input.getString("timers." + nonFormattedPath + ".unit");
            Object defaultValue = null;

            String itemname = nonFormattedPath.split("---")[1].replace("-", " ");
            // capitalize first letter
            itemname = itemname.substring(0, 1).toUpperCase() + itemname.substring(1);

            // log every timer to the debug config
            Main.LOG.debuglog("timer : " + nonFormattedPath + "\n" + "  configpath :" + timerConfigPath + "\n" + "  description : " + timerDescription + "\n" + "  unit : " + unit);

            // create timers related items.
            ItemManager.ItemBuilder item = new ItemManager.ItemBuilder(Material.PAPER, 1, ChatColor.RESET + "" + ChatColor.AQUA + itemname);
            switch (unit) {
                case "seconds":
                    defaultValue = input.getInt("timers." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.GRAY + "Valeur : " + defaultValue.toString(),
                            ChatColor.GRAY + "Unité : " + ChatColor.AQUA + "secondes (s)",
                            ChatColor.GRAY + "Clic gauche : +1 seconde",
                            ChatColor.GRAY + "Clic droit : -1 seconde",
                            ChatColor.GRAY + "Shift + clic gauche : +10 secondes",
                            ChatColor.GRAY + "Shift + clic droit : -10 secondes"
                    );
                    break;
                case "minutes":
                    defaultValue = input.getInt("timers." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.GRAY + "Valeur : " + defaultValue.toString(),
                            ChatColor.GRAY + "Unité : " + ChatColor.AQUA + "minutes (min)",
                            ChatColor.GRAY + "Clic gauche : +1 minute",
                            ChatColor.GRAY + "Clic droit : -1 minute",
                            ChatColor.GRAY + "Shift + clic gauche : +10 minutes",
                            ChatColor.GRAY + "Shift + clic droit : -10 minutes"
                    );
                    break;
                case "percentage":
                    defaultValue = input.getDouble("timers." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.GRAY + "Valeur : " + String.format("%.2f", ((double) defaultValue * 100.0d)),
                            ChatColor.GRAY + "Unité : " + ChatColor.AQUA + "pourcentage (%)",
                            ChatColor.GRAY + "Clic gauche : +1%",
                            ChatColor.GRAY + "Clic droit : -1%",
                            ChatColor.GRAY + "Shift + clic gauche : +10%",
                            ChatColor.GRAY + "Shift + clic droit : -10%"
                    );
                    break;
                case "int":
                    defaultValue = input.getInt("timers." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.GRAY + "Valeur : " + defaultValue.toString(),
                            ChatColor.GRAY + "Unité : " + ChatColor.AQUA + "entier",
                            ChatColor.GRAY + "Clic gauche : +1",
                            ChatColor.GRAY + "Clic droit : -1",
                            ChatColor.GRAY + "Shift + clic gauche : +10",
                            ChatColor.GRAY + "Shift + clic droit : -10"
                    );
                    break;
                default:
                    item.setLore(
                            ChatColor.DARK_RED + "Erreur !",
                            ChatColor.DARK_RED + "Unité inconnue !"
                    );
                    break;

            }

            item.addLore(ChatColor.ITALIC + "" + ChatColor.GRAY + "Description : " + timerDescription);
            ItemStack itemstack = item.build();

            pathToValue.put(timerConfigPath, defaultValue);
            pathToType.put(timerConfigPath, unit);
            itemToPath.put(itemstack, timerConfigPath);

            timersConfigInventory.addItem(itemstack);
        }
    }

    public void writePluginTimers(FileConfiguration config) {
        for(String path : pathToValue.keySet()) {
            Object value = pathToValue.get(path);
            Main.LOG.devlog("Writing timer " + path + " with value " + value);
            config.set(path, value);
        }
    }

    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.WATCH, 1, ChatColor.RESET + "" + ChatColor.AQUA + "Configuration des timers").addFakeEnchant().build();
    }

}
