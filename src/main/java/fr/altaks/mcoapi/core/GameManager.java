package fr.altaks.mcoapi.core;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.*;
import fr.altaks.mcoapi.core.configs.events.GameStartEvent;
import fr.altaks.mcoapi.core.configs.events.PlayerStartsConfiguringGameEvent;
import fr.altaks.mcoapi.core.configs.events.PlayerStopsConfiguringGameEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.PlayerStartConfigPartyEvent;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeLoadEvent;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeStartConfigEvent;
import fr.altaks.mcoapi.core.configs.events.moderation.PlayerStartsConfigModerationEvent;
import fr.altaks.mcoapi.core.configs.events.scenarios.PlayerStartsConfiguringScenariosEvent;
import fr.altaks.mcoapi.core.configs.events.timers.PlayerStartsConfiguringTimersEvent;
import fr.altaks.mcoapi.core.configs.events.worldconf.HostCallsMapConfigurationEvent;
import fr.altaks.mcoapi.util.FastBoard;
import fr.altaks.mcoapi.util.ItemManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameManager implements Listener {

    private UUID hostId = null;
    private List<UUID> cohosts = new ArrayList<>();
    public Player getHost() {
        return Bukkit.getPlayer(hostId);
    }

    private ItemStack hostConfigItem = new ItemManager.ItemBuilder(Material.BLAZE_ROD, 1,ChatColor.RED + "\u00BB Configuration de la partie \u00AB").build();
    private ItemStack hostStartingItem = new ItemManager.ItemBuilder(Material.BEACON, 1,ChatColor.RED + "\u00BB Démarrer la partie \u00AB").build();

    public void setHost(Player host) {
        this.hostId = host.getUniqueId();
    }

    public void addCohost(Player cohost) {
        cohosts.add(cohost.getUniqueId());
    }

    public void removeCohost(Player cohost) {
        cohosts.remove(cohost.getUniqueId());
    }

    private HashMap<UUID, FastBoard> boards = new HashMap<>();

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private GameState gameState = GameState.WAITING_FOR_HOST;

    public Main getMain() {
        return main;
    }

    private Main main;

    public GameConfiguration getGameConfiguration() {
        return gameConfiguration;
    }

    public GameModeConfiguration getGameModeConfiguration() {
        return gameModeConfiguration;
    }

    public ModerationConfiguration getModerationConfiguration() {
        return moderationConfiguration;
    }

    public ScenariosConfiguration getScenariosConfiguration() {
        return scenariosConfiguration;
    }

    public WorldConfiguration getWorldConfiguration() {
        return worldConfiguration;
    }

    private GameConfiguration gameConfiguration;
    private GameModeConfiguration gameModeConfiguration;
    private ModerationConfiguration moderationConfiguration;
    private ScenariosConfiguration scenariosConfiguration;
    private WorldConfiguration worldConfiguration;
    private TimersConfiguration timersConfiguration;

    private int waitingRoomScoreboardTaskID;

    public GameManager(Main main) {
        this.main = main;

        gameConfiguration = new GameConfiguration(this);
        gameModeConfiguration = new GameModeConfiguration(this);
        moderationConfiguration = new ModerationConfiguration(this);
        scenariosConfiguration = new ScenariosConfiguration(this);
        worldConfiguration = new WorldConfiguration(this);
        timersConfiguration = new TimersConfiguration(this);

        Bukkit.getPluginManager().registerEvents(gameConfiguration, main);
        Bukkit.getPluginManager().registerEvents(gameModeConfiguration, main);
        Bukkit.getPluginManager().registerEvents(moderationConfiguration, main);
        Bukkit.getPluginManager().registerEvents(scenariosConfiguration, main);
        Bukkit.getPluginManager().registerEvents(worldConfiguration, main);
        Bukkit.getPluginManager().registerEvents(timersConfiguration, main);

        // setting up the scoreboard on the right off the screen for the waiting stage
        for(Player player : Bukkit.getOnlinePlayers()){
            this.boards.put(player.getUniqueId(), new FastBoard(player));
        }

        waitingRoomScoreboardTaskID = new BukkitRunnable() {

            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()){

                    FastBoard board = boards.get(player.getUniqueId());

                    board.updateTitle(ChatColor.GOLD + "\u00BB UHC \u00AB");

                    board.updateLines(
                            "",
                            ChatColor.GRAY + "| " + ChatColor.WHITE + "Joueurs : " + ChatColor.RED + Bukkit.getOnlinePlayers().size() + ChatColor.WHITE + "/" + ChatColor.RED + Bukkit.getMaxPlayers(),
                            ChatColor.GRAY + "| " + ChatColor.WHITE + "Hôte : " + ChatColor.RED + (hostId == null ? "Aucun" : Bukkit.getPlayer(hostId).getName()),
                            ChatColor.GRAY + "| " + ChatColor.WHITE + "Jeu : " + ChatColor.GOLD + (gameModeConfiguration.getGameMode() == null ? "Aucun" : gameModeConfiguration.getGameMode().getLeft().getName().replace(".jar", "")),
                            "",
                            ChatColor.GOLD + "discord.gg/jungleuhc"
                    );
                }
            }

        }.runTaskTimer(main, 0, 20).getTaskId();
    }

    // on player join add a fastboard, and on player quit remove it
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.boards.put(event.getPlayer().getUniqueId(), new FastBoard(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.boards.get(event.getPlayer().getUniqueId()).delete();
        this.boards.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameStartEvent(GameStartEvent event) {
        // cancel the scoreboard task
        Bukkit.getScheduler().cancelTask(waitingRoomScoreboardTaskID);
    }

    /**
     * Inits the game managing system by setting an actual host of the party;
     *
     * @param host the players that hosts the game, usually the first player that joined the playing server
     */
    public void init(Player host) {

        this.hostId = host.getUniqueId();
        giveHostItems(host);
        this.gameState = GameState.WAITING_FOR_PLAYERS;

        host.sendMessage(Main.PREFIX + ChatColor.RED + "Vous êtes maintenant hôte de la partie");
    }

    /**
     * Changes the game host to the specified player
     * @param newHost the new host
     */
    public void changeHost(Player newHost){
        // remove stuff from the old host and give it to the new host
        Player host = Bukkit.getPlayer(hostId);
        if(host != null && this.gameState != GameState.STARTED) host.getInventory().clear();

        this.hostId = newHost.getUniqueId();
        giveHostItems(newHost);
    }

    public void giveHostItems(Player player){
        player.getInventory().addItem(this.hostConfigItem);
        player.getInventory().addItem(this.hostStartingItem);
    }

    /**
     * Resets the server game managing system in case the only preset player (the host), has left
     */
    public void reset() {
        if(this.hostId != null && Bukkit.getOfflinePlayer(hostId).isOnline()) Bukkit.getPlayer(hostId).getInventory().clear();
        this.hostId = null;
        this.gameState = GameState.WAITING_FOR_HOST;
    }

    @EventHandler
    public void onPlayerClicksWithBlazeConfigItem(PlayerInteractEvent event){
        if(event.getItem() == null) return;
        if(!ItemManager.lightCompare(event.getItem(), this.hostConfigItem)) return;

        if(event.getPlayer().getUniqueId().equals(this.hostId)){
            // the player is the host
            if(this.gameState == GameState.WAITING_FOR_PLAYERS){
                // the game is waiting for players
                // open the game mode selection inventory using the opening event
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfiguringGameEvent(event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onPlayerClicsWithBlazeConfigItem(PlayerInteractEvent event){
        if(event.getItem() == null) return;
        if(!ItemManager.lightCompare(event.getItem(), this.hostStartingItem)) return;

        if(event.getPlayer().getUniqueId().equals(this.hostId)){
            // the player is the host
            if(this.gameState == GameState.WAITING_FOR_PLAYERS){
                // make plugin enable all functionalities
                // generate world, load inventories, etc...

                if(this.gameModeConfiguration.getGameMode() == null){
                    event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Aucun mode de jeu n'a été choisi, vous ne pouvez pas lancer la partie");
                } else startGame(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onHostQuits(PlayerQuitEvent event){
        if(event.getPlayer().getUniqueId().equals(this.hostId)){
            // the player was the host, clear inventory before
            event.getPlayer().getInventory().remove(this.hostConfigItem);
            event.getPlayer().getInventory().remove(this.hostStartingItem);
        }
    }

    @EventHandler
    public void onHostTriesToPlaceBeacon(BlockPlaceEvent event){
        if(event.getPlayer().getUniqueId().equals(this.hostId)){
            // the player is the host
            if(event.getBlock().getType() == Material.BEACON){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerTriesToMoveHostBlazeRod(InventoryClickEvent event){
        if(event.getWhoClicked().getUniqueId().equals(this.hostId)){
            // the player is the host
            if(event.getCurrentItem() != null){
                if(ItemManager.lightCompare(event.getCurrentItem(), this.hostConfigItem)){
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if(Bukkit.getOnlinePlayers().size() == 1) {
            // il n'y a qu'un seul joueur
            this.main.getGameManager().reset();
            this.main.getGameManager().init(event.getPlayer());
        }

        event.getPlayer().teleport(new Location(Bukkit.getWorld("temp"), 0, 100, 0));
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if(event.getPlayer().getUniqueId().equals(this.hostId)){
            if(Bukkit.getOnlinePlayers().size() > 0){
                // give the hosting to another player
                this.main.getGameManager().changeHost(Bukkit.getOnlinePlayers().iterator().next());
                // make sure the person is not in spectator
                // teleport the player to the surface
                Bukkit.getPlayer(this.hostId).setGameMode(GameMode.SURVIVAL);
                Bukkit.getPlayer(this.hostId).teleport(new Location(Bukkit.getWorld("temp"), 0, 100, 0));
            } else if(Bukkit.getOnlinePlayers().size() == 0){
                // reset the game waiting for a host
                this.main.getGameManager().reset();
            }
        }
    }

    private ArrayList<Player> playersConfigGame = new ArrayList<>();

    public List<UUID> getCohosts() {
        return cohosts;
    }

    public TimersConfiguration getTimersConfiguration() {
        return timersConfiguration;
    }

    public enum GameState {
        WAITING_FOR_HOST, WAITING_FOR_PLAYERS, STARTED;
    }

    private Inventory gameConfigInv = null;

    /**
     * Starts the game for the whole server
     */
    public void startGame(Player starter) {

        this.gameState = GameState.STARTED;
        // enable the gamemode plugin
        // make sure there is a chosen gamemode
        if(this.gameModeConfiguration.getGameMode() == null){
            starter.sendMessage(Main.PREFIX + ChatColor.RED + "Aucun mode de jeu n'a été choisi");
            return;
        }

        // send title to all players
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Chargement ...", ChatColor.GRAY + "[Mode de jeu]");
        }
        Bukkit.getPluginManager().callEvent(new GameModeLoadEvent(Bukkit.getPlayer(this.hostId)));

        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : Bukkit.getOnlinePlayers()){
                    player.sendTitle(ChatColor.GOLD + "Chargement ...", ChatColor.GRAY + "[Génération de la map]");
                }
                worldConfiguration.regenerate("world", new Location(Bukkit.getWorld("temp"), 0, 100, 0));
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if(Bukkit.getWorld("world") != null) {
                            // le monde a été regénéré correctement et est chargé

                            // load inventories to all players thar are already in survival mode
                            for(Player player : Bukkit.getOnlinePlayers()){
                                player.sendTitle(ChatColor.GOLD + "Chargement ...", ChatColor.GRAY + "[Inventaires]");
                            }
                            for(Player player : Bukkit.getOnlinePlayers()){
                                if(player.getGameMode() != GameMode.SPECTATOR){
                                    player.getInventory().clear();
                                    if(gameConfiguration.getStartInv() != null) gameConfiguration.getStartInv().injectToPlayer(player);
                                }
                            }

                            for(Player player : Bukkit.getOnlinePlayers()){
                                player.sendTitle(ChatColor.GOLD + "Lancement de la partie", ChatColor.YELLOW + "Bon jeu !");
                            }
                            Bukkit.getPluginManager().callEvent(new GameStartEvent(Bukkit.getPlayer(hostId)));

                            cancel();
                        }
                    }

                }.runTaskTimer(main, 10 * 20, 20);

            }
        }.runTaskLater(this.main, 2 * 20);


    }

    @EventHandler
    public void onPlayerStartsConfiguringGame(PlayerStartsConfiguringGameEvent event) {
        if(!playersConfigGame.contains(event.getPlayer())) {
            playersConfigGame.add(event.getPlayer());
            if(gameConfigInv == null) {
                gameConfigInv = Bukkit.createInventory(null, 9, "Configuration de la partie");

                // command block - game config
                // watch - timers
                // leaves - worldgen
                // beacon - game modes
                // scenarios - snowball

                gameConfigInv.setItem(0, gameConfiguration.getItemStack());
                gameConfigInv.setItem(2, timersConfiguration.getItemStack());
                gameConfigInv.setItem(3, worldConfiguration.getItemStack());
                gameConfigInv.setItem(5, moderationConfiguration.getItemStack());
                gameConfigInv.setItem(6, gameModeConfiguration.getItemStack());
                gameConfigInv.setItem(8, scenariosConfiguration.getItemStack());
            }
            event.getPlayer().openInventory(gameConfigInv);
        }
    }

    @EventHandler
    public void onPlayerClicsGameConfigInv(InventoryClickEvent event) {
        if(event.getInventory() == null || event.getClickedInventory() == null) return;
        if(!event.getInventory().equals(gameConfigInv)) return;
        if(event.getClickedInventory() != event.getView().getTopInventory()) return;
        event.setCancelled(true);

        switch (event.getSlot()) {
            case 0:
                Bukkit.getPluginManager().callEvent(new PlayerStartConfigPartyEvent((Player) event.getWhoClicked()));
                break;
            case 2:
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfiguringTimersEvent((Player) event.getWhoClicked()));
                break;
            case 3:
                Bukkit.getPluginManager().callEvent(new HostCallsMapConfigurationEvent((Player) event.getWhoClicked()));
                break;
            case 5:
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfigModerationEvent((Player) event.getWhoClicked()));
                break;
            case 6:
                Bukkit.getPluginManager().callEvent(new GameModeStartConfigEvent((Player) event.getWhoClicked()));
                break;
            case 8:
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfiguringScenariosEvent((Player) event.getWhoClicked()));
                break;
            default:
                return;
        }
    }

    @EventHandler
    public void onPlayerClosesGameConfigInv(InventoryCloseEvent event) {
        if(!event.getInventory().equals(gameConfigInv)) return;
        Bukkit.getPluginManager().callEvent(new PlayerStopsConfiguringGameEvent((Player) event.getPlayer()));
    }

    @EventHandler
    public void onPlayerStopsConfiguringGame(PlayerStopsConfiguringGameEvent event) {
        if(playersConfigGame.contains(event.getPlayer())) {
            playersConfigGame.remove(event.getPlayer());
            event.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez fini de configurer la partie");
        }
    }

}
