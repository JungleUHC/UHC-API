package fr.altaks.mcoapi;

import fr.altaks.mcoapi.commands.*;
import fr.altaks.mcoapi.commands.dev.*;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.util.worldmanip.DynamicClassFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public static boolean debugMode = false, devMode = false;

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.AQUA + "UHCAPI" + ChatColor.GRAY + "] " + ChatColor.RESET + "\u00BB" + ChatColor.RESET + " ";
    public static final Logger LOGGER = Bukkit.getLogger();

    public GameManager getGameManager() {
        return gameManager;
    }

    private GameManager gameManager;

    @Override
    public void onLoad() {

        super.onLoad();
        boolean foundAnyRemainingArtifact = false;

        File gameModesDirectory = new File(this.getConfig().getString("gamemodes-path"));

        for(File gamemodeFile : gameModesDirectory.listFiles()){
            for(File localPluginFile : new File(this.getDataFolder().getParent()).listFiles()){
                if(gamemodeFile.getName().equals(localPluginFile.getName())) {
                    // a gamemode has been found into the game folder
                    System.out.println(LOG.DEBUG_PREFIX + "A remaining gamemode plugin file has been found during server load. Please delete " + localPluginFile.getPath() + " then restart the server");

                }
            }
        }

    }

    @Override
    public void onEnable() {

        // Adding the default config to the file system if it hasn't been done
        saveDefaultConfig();

        // Setup debug and dev mode variables
        debugMode = getConfig().getBoolean("launch-mode.debug", false);
        devMode = getConfig().getBoolean("launch-mode.dev", false);

        LOG.devlog("Server has been loaded ! Plugin is enabling...");

        // Create temp world if not existing
        if(Bukkit.getWorld("temp") == null) {
            WorldCreator tempCreator = new WorldCreator("temp");
            tempCreator.environment(World.Environment.NORMAL);
            tempCreator.createWorld();
        }

        getCommand("configstartinv").setExecutor(new ConfigStartInvCommand(this));
        getCommand("configdeathinv").setExecutor(new ConfigDeathInvCommand(this));
        getCommand("configcraftitems").setExecutor(new ConfigCraftableItemsCommand(this));

        getCommand("configitemenchlim").setExecutor(new ConfigStartEnchantItemCommand(this));
        getCommand("configworldgen").setExecutor(new ConfigWorldGenCommand(this));
        getCommand("configmobspawn").setExecutor(new ConfigMobSpawnCommand(this));

        getCommand("configgame").setExecutor(new ConfigWholeGameCommand(this));
        getCommand("mod").setExecutor(new ModCommand(this));
        getCommand("host").setExecutor(new HostCommand(this));
        getCommand("split").setExecutor(new SplitCommand());
        /*
        getCommand("mumble").setExecutor(new MumbleCommand(this));

        mumble:
            description:        Permet de se connecter au mumble
            usage:              /mumble
            permission-message: Vous n'avez pas la permission d'utiliser cette commande

         */
        getCommand("configgamemode").setExecutor(new ConfigGameModeCommand(this));
        getCommand("testscenarios").setExecutor(new TestScenariosCommand(this));
        getCommand("start").setExecutor(new StartCommand());

        DocsCommand docsCommand = new DocsCommand(this);
        getCommand("docs").setExecutor(docsCommand);
        Bukkit.getPluginManager().registerEvents(docsCommand, this);

        gameManager = new GameManager(this);
        getCommand("configfinish").setExecutor(gameManager.getGameConfiguration());
        Bukkit.getPluginManager().registerEvents(gameManager, this);

        if(Bukkit.getServer().getOnlinePlayers().size() == 1) {
            this.gameManager.init((Player) Bukkit.getOnlinePlayers().toArray()[0]);
        } else if(Bukkit.getServer().getOnlinePlayers().size() > 0){
            // choose a random player
            int randomPlayerIndex = (int) (Math.random() * Bukkit.getServer().getOnlinePlayers().size());
            this.gameManager.init((Player) Bukkit.getOnlinePlayers().toArray()[randomPlayerIndex]);
        }

        if(!DynamicClassFunctions.setPackages()) {
            LOGGER.log(Level.WARNING, "NMS/OBC package could not be detected, using " + DynamicClassFunctions.nmsPackage + " and " + DynamicClassFunctions.obcPackage);
        }
        DynamicClassFunctions.setClasses();
        DynamicClassFunctions.setMethods();
        DynamicClassFunctions.setFields();


        // setting up the waiting score board on the right


    }

    @Override
    public void onDisable() {
        // remove all stuff from all the players
        for(Player player : Bukkit.getOnlinePlayers()){
            player.getInventory().clear();
        }
    }

    /**
     * {@link java.util.logging.Logger} <br>
     * This class allows you to log information on the console / logs
     * depending on werther or not these logging channels are on
     */
    public static class LOG {

        public static final String DEBUG_PREFIX = ChatColor.GRAY + "[" + ChatColor.RED         + "DEBUG" + ChatColor.GRAY + "]" + ChatColor.RESET + " \u00BB" + ChatColor.RESET + " ";
        public static final String DEVLG_PREFIX = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "DEVLG" + ChatColor.GRAY + "]" + ChatColor.RESET + " \u00BB" + ChatColor.RESET + " ";

        public static void devlog(String... text) {
            if(Main.devMode) for(String line : text) LOGGER.info(DEVLG_PREFIX + line);
        }

        public static void debuglog(String... text){
            if(Main.debugMode) for(String line : text) LOGGER.info(DEBUG_PREFIX + line);
        }

    }

}
