package fr.altaks.mcoapi.core.configs;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.GameStartEvent;
import fr.altaks.mcoapi.core.configs.events.moderation.PlayerStartsConfigModerationEvent;
import fr.altaks.mcoapi.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ModerationConfiguration implements Listener {

    private boolean chatMuted = false;
    private int groupSize = 5;

    private GameManager gameManager;

    public ModerationConfiguration(GameManager gameManager) {
        this.gameManager = gameManager;

        // register chat command
        // register chat command listener
        MuteChatCommand cmd = new MuteChatCommand();
        gameManager.getMain().getCommand("chat").setExecutor(cmd);
        gameManager.getMain().getServer().getPluginManager().registerEvents(cmd, gameManager.getMain());

        // register group size command
        gameManager.getMain().getCommand("setgroup").setExecutor(new GroupSizeCommand());

        // register bypass command
        // register bypass command listener
        BypassCommand bypassCommand = new BypassCommand();
        gameManager.getMain().getCommand("bypass").setExecutor(bypassCommand);
        gameManager.getMain().getServer().getPluginManager().registerEvents(bypassCommand, gameManager.getMain());
    }

    private Inventory spectatorPermissionsInventory = null;

    @EventHandler
    public void onPlayerOpensSpectatorPermissions(PlayerStartsConfigModerationEvent event){
        if(spectatorPermissionsInventory == null) {
            // create inventory
            spectatorPermissionsInventory = Bukkit.createInventory(null, 9, "Permissions de spectateur");

            // add items : display teams and roles
            spectatorPermissionsInventory.setItem(
                    3,
                    new ItemManager.ItemBuilder(Material.BANNER, 1, "Utiliser /displayroles")
                            .setLore(
                                    ChatColor.RED +  "Désactivé par défaut",
                                    ChatColor.GRAY + "Permet d'afficher les rôles de chaque joueur"
                            )
                            .addFakeEnchant()
                            .build()
            );

            spectatorPermissionsInventory.setItem(
                    5,
                    new ItemManager.ItemBuilder(Material.BANNER, 1, "Utiliser /displayteams")
                            .setLore(
                                    ChatColor.RED +  "Désactivé par défaut",
                                    ChatColor.GRAY + "Permet d'afficher les équipes de chaque joueur"
                            )
                            .addFakeEnchant()
                            .build()
            );
        }
        event.getPlayer().openInventory(spectatorPermissionsInventory);
    }

    private ArrayList<String> spectatorsPermissionList = new ArrayList<>();

    private void editLore(ItemStack item, int line, String newText){
        List<String> lore = item.getItemMeta().getLore();
        lore.set(line, newText);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onPlayerClicksSpectatorPermissionsInventory(InventoryClickEvent event){
        if(event.getInventory() == null || event.getClickedInventory() == null) return;
        if(!event.getClickedInventory().equals(spectatorPermissionsInventory)) return;
        if(event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
        if(event.getCurrentItem() == null) return;
        event.setCancelled(true);

        switch (event.getSlot()){
            case 3:
                // prepare adding permission "mcoapi.roles.display" to spectators
                if(spectatorsPermissionList.contains("mcoapi.roles.display")) {
                    Main.LOG.debuglog("Removing permission mcoapi.roles.display from spectators");
                    spectatorsPermissionList.remove("mcoapi.roles.display");
                    editLore(event.getCurrentItem(), 0, ChatColor.RED + "Désactivé par l'hôte");
                } else {
                    Main.LOG.debuglog("Adding permission mcoapi.roles.display to spectators");
                    spectatorsPermissionList.add("mcoapi.roles.display");
                    editLore(event.getCurrentItem(), 0, ChatColor.GREEN + "Activé par l'hôte");
                    // change lore of item
                }
                break;
            case 5:
                // prepare adding permission "mcoapi.teams.display" to spectators
                if(spectatorsPermissionList.contains("mcoapi.teams.display")) {
                    Main.LOG.debuglog("Removing permission mcoapi.teams.display from spectators");
                    spectatorsPermissionList.remove("mcoapi.teams.display");
                    editLore(event.getCurrentItem(), 0, ChatColor.RED + "Désactivé par l'hôte");
                } else {
                    Main.LOG.debuglog("Adding permission mcoapi.teams.display to spectators");
                    spectatorsPermissionList.add("mcoapi.teams.display");
                    editLore(event.getCurrentItem(), 0, ChatColor.GREEN + "Activé par l'hôte");
                }
                break;
        }
    }

    private HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    @EventHandler
    public void onGameStarts(GameStartEvent event){
        // enable scheduler to add permissions to needed players and remove them from other players that are in survival
        new BukkitRunnable(){

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getGameMode() == GameMode.SPECTATOR){
                        if(!player.isOp() && !perms.containsKey(player.getUniqueId())){
                            PermissionAttachment attachment = player.addAttachment(gameManager.getMain());
                            for(String permission : spectatorsPermissionList){
                                attachment.setPermission(permission, true);
                            }
                            perms.put(player.getUniqueId(), attachment);
                        }
                    } else {
                        if(!player.isOp() && perms.containsKey(player.getUniqueId())){
                            PermissionAttachment attachment = perms.get(player.getUniqueId());
                            for(String permission : spectatorsPermissionList){
                                attachment.unsetPermission(permission);
                            }
                        }
                    }
                }
            }

        }.runTaskTimer(gameManager.getMain(), 0, 20);
    }


    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.IRON_SWORD, 1, "Configuration de la modération").addFakeEnchant().build();
    }

    public class BypassCommand implements Listener, CommandExecutor {

        private ArrayList<Player> bypassEnabledPlayers = new ArrayList<>();

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(command.getName().equalsIgnoreCase("bypass")) {
                if(sender instanceof Player && (sender.hasPermission("mcoapi.bypass") || gameManager.getHost().equals(sender))) {
                    Player player = (Player) sender;
                    if(bypassEnabledPlayers.contains(player)) {
                        bypassEnabledPlayers.remove(player);
                        player.sendMessage(Main.PREFIX + ChatColor.YELLOW + "Vous n'êtes plus en mode bypass.");
                    } else {
                        bypassEnabledPlayers.add(player);
                        player.sendMessage(Main.PREFIX + ChatColor.YELLOW + "Vous êtes maintenant en mode bypass.");
                    }
                } else {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous devez être un joueur pour exécuter cette commande.");
                }
                return true;
            }
            return false;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerDiesEvent(PlayerDeathEvent event) {
            if(bypassEnabledPlayers.isEmpty()) return;
            String deathMsg = Main.PREFIX + "Mort de " + ChatColor.YELLOW + event.getEntity().getDisplayName() + ChatColor.WHITE + " à cause de \u00BB " + ChatColor.YELLOW;
            switch (event.getEntity().getLastDamageCause().getCause()) {
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    deathMsg += "une explosion";
                    break;
                case CONTACT:
                    deathMsg += "un cactus";
                    break;
                case CUSTOM:
                    deathMsg += "une mort imprévue";
                    break;
                case DROWNING:
                    deathMsg += "une noyade";
                    break;
                case ENTITY_ATTACK:
                case THORNS:
                    deathMsg += "un combat";
                    break;
                case FALL:
                    deathMsg += "une chute";
                    break;
                case FALLING_BLOCK:
                    deathMsg += "un bloc tombant";
                    break;
                case FIRE_TICK:
                case FIRE:
                case LAVA:
                    deathMsg += "un feu/lave";
                    break;
                case LIGHTNING:
                    deathMsg += "un éclair";
                    break;
                case MAGIC:
                case POISON:
                    deathMsg += "un sort";
                    break;
                case PROJECTILE:
                    deathMsg += "un projectile";
                    break;
                case STARVATION:
                    deathMsg += "la faim";
                    break;
                case SUFFOCATION:
                    deathMsg += "l'étouffement";
                    break;
                case VOID:
                    deathMsg += "le vide";
                    break;
                case WITHER:
                    deathMsg += "le wither";
                    break;
                default:
                    deathMsg += "une mort inconnue";
                    break;
            }
            for(Player player : bypassEnabledPlayers){
                // log the death to the player
                player.sendMessage(deathMsg);
            }
        }
    }

    public class GroupSizeCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(command.getName().equalsIgnoreCase("setgroup") && args.length > 0) {
                String textNumber = args[0];
                // Parse the number
                try {
                    int number = Integer.parseInt(textNumber);
                    if(number > 0) {
                        groupSize = number;

                        // send title to all the players
                        for(Player player : Bukkit.getOnlinePlayers()){
                            player.sendTitle(
                                    ChatColor.GOLD + "\u26A0" + ChatColor.RED + " Groupes de " + groupSize + " joueurs" + ChatColor.GOLD + " \u26A0",
                                    ChatColor.YELLOW + "La taille des groupes change !"
                            );
                            player.sendMessage(Main.PREFIX + ChatColor.YELLOW + "\u26A0 La taille des groupes a été changée à " + ChatColor.GOLD + groupSize + ChatColor.YELLOW + " ! \u26A0");
                        }

                        // send broadcast in chat using yellow and gold colors


                        return true;
                    } else {
                        sender.sendMessage(Main.PREFIX + ChatColor.RED + "Le nombre doit être supérieur à 0.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + "Le nombre doit être un nombre.");
                }
            }
            return false;
        }
    }

    public class MuteChatCommand implements Listener, CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(command.getName().equalsIgnoreCase("chat") && args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "on":
                    case "active":
                    case "enable":
                        chatMuted = false;
                        Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + "Le chat a été activé.");
                        break;
                    case "off":
                    case "unactive":
                    case "disable":
                        chatMuted = true;
                        Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + "Le chat a été désactivé.");
                        break;
                }
                return true;
            }
            return false;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void onPlayerTalksInChat(AsyncPlayerChatEvent event) {
            if(event.getPlayer().isOp() || event.getPlayer().hasPermission("mcoapi.mod.chatbypass")) return;
            if(chatMuted) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Main.PREFIX + "Le chat est actuellement désactivé.");
            }
        }
    }

}
