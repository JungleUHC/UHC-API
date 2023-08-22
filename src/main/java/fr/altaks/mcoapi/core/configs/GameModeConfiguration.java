package fr.altaks.mcoapi.core.configs;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeLoadEvent;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeStartConfigEvent;
import fr.altaks.mcoapi.util.ItemManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

public class GameModeConfiguration implements Listener {

    private GameManager gameManager;

    public GameModeConfiguration(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.BEACON, 1, "Configuration du mode de jeu").addFakeEnchant().build();
    }

    private Inventory gameModeChoice = null;
    private Inventory presetsConfiguration = null;

    private ArrayList<Pair<File, Pair<FileConfiguration, FileConfiguration>>> gameModes = new ArrayList<>();
    private ArrayList<FileConfiguration> presets = new ArrayList<>();

    private Pair<File, Pair<FileConfiguration, FileConfiguration>> chosenGameMode = null;
    private Plugin chosenPlugin;
    private FileConfiguration chosenPreset = null;

    private HashMap<ItemStack, Pair<File, Pair<FileConfiguration, FileConfiguration>>> gameModeItems = new HashMap<>();
    private HashMap<ItemStack, Pair<String, String>> itemToTypeAndPath = new HashMap<>();
    private HashMap<ItemStack, Object> itemToValue = new HashMap<>();

    public Pair<File, Pair<FileConfiguration, FileConfiguration>> getGameMode() {
        return chosenGameMode;
    }

    @EventHandler
    public void onPlayerStartsConfiguringGameMode(GameModeStartConfigEvent event) {

        // Query available game modes within the config specified folder
        if(gameModeChoice == null) {
            File gameModesDirectory = new File(gameManager.getMain().getConfig().getString("gamemodes-path"));
            Main.LOG.debuglog("GameModes directory: " + gameModesDirectory.getAbsolutePath() + " (exists: " + gameModesDirectory.exists() + ")" + (gameModesDirectory.exists() ? " (isDirectory: " + gameModesDirectory.isDirectory() + ")" : ""));
            // Log every file in the directory if is a .jar file
            if(gameModesDirectory.exists() && gameModesDirectory.isDirectory()) {
                HashMap<String, File> files = new HashMap<>();
                HashMap<String, FileConfiguration> descriptions = new HashMap<>();
                HashMap<String, FileConfiguration> defaultconfigs = new HashMap<>();

                for(File file : gameModesDirectory.listFiles()){
                    if(file.getName().endsWith(".jar")) {
                        files.put(file.getName().replace(".jar", ""), file);
                        Main.LOG.debuglog("Found game mode: " + file.getName());
                    } else if(file.getName().endsWith(".conf.yml")) {
                        FileConfiguration defaultconfig = YamlConfiguration.loadConfiguration(file);
                        defaultconfigs.put(file.getName().replace(".conf.yml", ""), defaultconfig);
                        Main.LOG.debuglog("Found game mode default config: " + file.getName());
                    } else if(file.getName().endsWith(".yml")) {
                        FileConfiguration description = YamlConfiguration.loadConfiguration(file);
                        descriptions.put(file.getName().replace(".yml", ""), description);
                        Main.LOG.debuglog("Found game mode description: " + file.getName());
                    }
                }
                // loop over files and descriptions to create a pair and add it to the available gamemodes
                for(String filename : files.keySet()){
                    if(descriptions.containsKey(filename) && defaultconfigs.containsKey(filename)) {
                        gameModes.add(Pair.of(files.get(filename), Pair.of(descriptions.get(filename), defaultconfigs.get(filename))));
                    }
                }

                Main.LOG.debuglog("Found " + gameModes.size() + " game modes in total");
            } else {
                Main.LOG.debuglog("GameModes directory does not exist or is not a directory");
            }

            // calculate size of inv as a multiple of 9
            int size = 9;
            if(gameModes.size() % 9 == 0) {
                size = gameModes.size();
            } else {
                while(size < gameModes.size() && size < 6 * 9) {
                    size += 9;
                }
            }

            gameModeChoice = Bukkit.createInventory(null, size, "Choix du mode de jeu");
            for(Pair<File, Pair<FileConfiguration, FileConfiguration>> entry : gameModes){

                File file = entry.getLeft();
                FileConfiguration description = entry.getRight().getLeft();

                String gamemodeDescription = description.getString("description");
                String author = description.getString("author");
                String version = description.getString("version");

                if(gameModeChoice.firstEmpty() != -1) {
                    String itemname = ChatColor.RESET + "" + ChatColor.AQUA + file.getName().replace(".jar", "");
                    ItemStack gamemodeItem = new ItemManager.ItemBuilder(Material.PAPER, 1, itemname)
                            .setLore(
                                    ChatColor.YELLOW + "Description : " + gamemodeDescription,
                                    ChatColor.GRAY + "Développé par : " + author,
                                    ChatColor.GRAY + "Version : " + version)
                            .build();
                    gameModeChoice.addItem(gamemodeItem);
                    gameModeItems.put(gamemodeItem, entry);
                } else break;
            }

        }
        if(presetsConfiguration == null) {

            File presetsDirectory = new File(gameManager.getMain().getConfig().getString("presets-path"));
            Main.LOG.debuglog("Presets directory: " + presetsDirectory.getAbsolutePath() + " (exists: " + presetsDirectory.exists() + ")" + (presetsDirectory.exists() ? " (isDirectory: " + presetsDirectory.isDirectory() + ")" : ""));

            // Log every file in the directory if is a .yml file
            if(presetsDirectory.exists() && presetsDirectory.isDirectory()) {
                for(File file : presetsDirectory.listFiles()){
                    if(file.getName().endsWith(".yml")) {
                        presets.add(YamlConfiguration.loadConfiguration(file));
                        Main.LOG.debuglog("Found preset: " + file.getName());
                    }
                }
                Main.LOG.debuglog("Found " + presets.size() + " presets in total");
            } else {
                Main.LOG.debuglog("Presets directory does not exist or is not a directory");
            }

        }

        event.getPlayer().openInventory(gameModeChoice);
    }

    @EventHandler
    public void onPlayerClicksOnGameModeItem(InventoryClickEvent event) {
        if(event.getInventory() == null || event.getClickedInventory() == null) return;
        if(!event.getClickedInventory().equals(gameModeChoice)) return;
        if(event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
        if(!gameModeItems.containsKey(event.getCurrentItem())) return;
        event.setCancelled(true);

        // set chosen game mode
        if(chosenGameMode == null || (chosenGameMode != null && !chosenGameMode.equals(gameModeItems.get(event.getCurrentItem())))){
            chosenGameMode = gameModeItems.get(event.getCurrentItem());
            event.getWhoClicked().sendMessage(
                    Main.PREFIX + ChatColor.GREEN + "Vous avez choisi le mode de jeu " + ChatColor.AQUA + chosenGameMode.getLeft().getName().replace(".jar", "")
            );

            readPluginParameters(chosenGameMode.getRight().getLeft()); // use of plugin description file to get parameters and linked items
            gameManager.getTimersConfiguration().readPluginTimers(chosenGameMode.getRight().getLeft()); // use of plugin description file to get timers

            int size = 9;
            if(itemToTypeAndPath.size() % 9 == 0 && itemToTypeAndPath.size() <= 6 * 9) {
                size = itemToTypeAndPath.size();
            } else {
                while(size < itemToTypeAndPath.size() && size < 6 * 9) {
                    size += 9;
                }
            }

            presetsConfiguration = Bukkit.createInventory(null, size, "Configuration du mode de jeu");

            // fill the presets inventory
            for(ItemStack item : itemToTypeAndPath.keySet()){
                if(presetsConfiguration.firstEmpty() != -1) {
                    presetsConfiguration.addItem(item);
                } else break;
            }
        }

        event.getWhoClicked().openInventory(presetsConfiguration);
    }

    @EventHandler
    public void onPlayerClosesInventory(InventoryCloseEvent event) {
        if(event.getInventory() == null) return;
        if(event.getInventory().equals(gameModeChoice) && chosenGameMode == null) {
            event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Vous n'avez pas choisi de mode de jeu ! Vous ne pourrez pas lancer de partie sans !");
            return;
        } else if(event.getInventory().equals(presetsConfiguration)) {
            Bukkit.getScheduler().runTaskLater(gameManager.getMain(), () -> {
                Bukkit.getPluginManager().callEvent(new GameModeStartConfigEvent((Player) event.getPlayer()));
            }, 1L);
            return;
        }
    }

    @EventHandler
    public void onGameModeLoadEvent(GameModeLoadEvent event) {
        if(chosenGameMode == null) {
            event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Vous n'avez pas choisi de mode de jeu ! Vous ne pourrez pas lancer de partie sans !");
            return;
        }

        // Copy the needed files into the directory : .jar and default config : .conf.yml as config.yml in the plugin's folder
        File gameModeFile = chosenGameMode.getLeft();

        File pluginsFolder = gameManager.getMain().getDataFolder().getParentFile(); // /plugins/ folder
        // Copy the .jar file
        try {
            FileUtils.copyFile(gameModeFile, new File(pluginsFolder, gameModeFile.getName()));
            // enable the plugin
            File gamemode = new File(pluginsFolder, gameModeFile.getName());
            chosenPlugin = Bukkit.getPluginManager().loadPlugin(gamemode);
            // try to have the default config without the plugin being enabled to overwrite it before enabling it
            chosenPlugin.saveDefaultConfig();
            Main.LOG.devlog("Default config has been created " + chosenPlugin.getConfig());

            for(ItemStack item : itemToTypeAndPath.keySet()){
                Pair<String, String> typeAndPath = itemToTypeAndPath.get(item);
                String path = typeAndPath.getRight();
                Object value = itemToValue.get(item);
                chosenPlugin.getConfig().set(path, value);
                Main.LOG.devlog("Parameter " + path + " re-written to " + value);
            }

            // call timers rewrite
            gameManager.getTimersConfiguration().writePluginTimers(chosenPlugin.getConfig());

            Main.LOG.devlog("Config has been re-written by the API. Saving it...");
            chosenPlugin.saveConfig();

            Bukkit.getPluginManager().enablePlugin(chosenPlugin);

        } catch (IOException e) {
            event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Impossible de copier le fichier du mode de jeu dans le dossier du plugin ! Veuillez contacter les administrateurs !");
            Main.LOG.devlog("Error while copying the game mode file " + gameModeFile.getName() + " to the plugin's folder");
            e.printStackTrace();
        } catch (InvalidDescriptionException | InvalidPluginException e) {
            event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Impossible de charger le fichier du mode de jeu ! Veuillez contacter les administrateurs !");
            Main.LOG.devlog("Error while loading the game mode file " + gameModeFile.getName());
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onPlayerChangesValueOfPluginParameter(InventoryClickEvent event) {
        // Verifier que l'input est dans le bon inventaire
        // Récupérer l'item concerné
        // Récupérer la valeur concernée et son type,
        // Si c'est un boolean, inverser la valeur
        // Si c'est un int, ajouter ou retirer 1
        // Si c'est un double, ajouter ou retirer 0.1 ou si c'est shift 1
        // Si c'est un string, attendre une valeur dans le tchat et la mettre, via un runnable

        if(event.getInventory() == null || event.getClickedInventory() == null) return;
        if(!event.getClickedInventory().equals(event.getView().getTopInventory())) return;
        if(!event.getClickedInventory().equals(presetsConfiguration)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        Main.LOG.devlog("Clicked item : " + clickedItem);

        String valueType = itemToTypeAndPath.get(clickedItem).getLeft();
        Object value = itemToValue.get(clickedItem);
        switch (valueType) {
            case "boolean":
                value = !((boolean) value);
                break;
            case "integer":
                if(event.isLeftClick()) value = (int) value + 1;
                if(event.isRightClick()) value = (int) value - 1;
                if((int) value < 0) value = 0;
                break;
            case "double":
                double change = 1.0d;
                if(event.isLeftClick()) change = 1.0d;
                if(event.isRightClick()) change = -1.0d;
                if(event.isShiftClick()) change *= 10.0d;
                value = (double) value + change;
                if((double) value < 0.0d) value = 0.0d;
                break;
            default:
                Main.LOG.devlog("Couldn't manipulate value " + value + " without knowing it's type");
                return;
        }

        // reinput value into values and change the linked line
        Main.LOG.devlog("Player manipulated plugin parameter to set it to " + value);

        ItemStack item = clickedItem.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(0, ChatColor.YELLOW + "Valeur : " + value);
        meta.setLore(lore);
        item.setItemMeta(meta);

        event.getInventory().setItem(event.getSlot(), item);
        ((Player) event.getWhoClicked()).updateInventory();

        this.itemToValue.remove(clickedItem);
        this.itemToValue.put(item, value);

        Pair<String, String> typeAndPath = itemToTypeAndPath.get(clickedItem);

        this.itemToTypeAndPath.remove(clickedItem);
        this.itemToTypeAndPath.put(item, typeAndPath);
    }

    private void readPluginParameters(FileConfiguration input) {
        for(String nonFormattedPath : input.getConfigurationSection("parameters").getKeys(false)){
            String formattedPath = nonFormattedPath.replace("---", ".");

            String valueDescription = input.getString("parameters." + nonFormattedPath + ".description");
            String type = input.getString("parameters." + nonFormattedPath + ".type");

            String[] namefragments = nonFormattedPath.split("---")[1].replace("-", " : ").replace("_", " ").split(" ");
            StringJoiner builder = new StringJoiner(" ");
            for(String fragment : namefragments){
                builder.add(fragment.substring(0, 1).toUpperCase() + fragment.substring(1));
            }
            String itemname = builder.toString();
            Object defaultValue = null;

            ItemManager.ItemBuilder item = new ItemManager.ItemBuilder(Material.PAPER, 1, ChatColor.RESET + "" + ChatColor.AQUA + itemname);
            switch (type) {
                case "boolean":
                    defaultValue = input.getBoolean("parameters." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.YELLOW + "Valeur : " + defaultValue,
                            ChatColor.GRAY + "Cliquez pour changer la valeur"
                    );
                    break;
                case "integer":
                    defaultValue = input.getInt("parameters." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.YELLOW + "Valeur : " + defaultValue,
                            ChatColor.GRAY + "Clic gauche pour augmenter la valeur de 1",
                            ChatColor.GRAY + "Clic droit pour réduire la valeur de 1"
                    );
                    break;
                case "double":
                    defaultValue = input.getDouble("parameters." + nonFormattedPath + ".value");
                    item.setLore(
                            ChatColor.YELLOW + "Valeur : " + defaultValue,
                            ChatColor.GRAY + "Clic gauche pour augmenter la valeur de 0.1",
                            ChatColor.GRAY + "Clic droit pour réduire la valeur de 0.1",
                            ChatColor.GRAY + "Shift + Clic gauche pour augmenter la valeur de 1",
                            ChatColor.GRAY + "Shift + Clic droit pour réduire la valeur de 1"
                    );
                    break;
                default:
                    item.setLore(
                            ChatColor.DARK_RED + "Erreur !",
                            ChatColor.DARK_RED + "Type de valeur inconnu !"
                    );
                    break;

            }

            item.addLore(ChatColor.ITALIC + "" + ChatColor.GRAY + "Description : " + valueDescription);

            itemToTypeAndPath.put(item.build(), Pair.of(type, formattedPath));
            if(defaultValue != null) {
                itemToValue.put(item.build(), defaultValue);
            } else {
                Main.LOG.debuglog("Error while reading the default value of the parameter " + formattedPath + " : the default type or value aren't matching");
            }
            Main.LOG.devlog("Added item " + item.build().getItemMeta().getDisplayName() + " to the list of parameters items");

        }
    }
}
