package fr.altaks.mcoapi.core.configs;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.gameconf.PlayerStartConfigPartyEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.craft.PlayerStartsConfigCraftableItems;
import fr.altaks.mcoapi.core.configs.events.gameconf.craft.PlayerStopsConfigCraftableItems;
import fr.altaks.mcoapi.core.configs.events.gameconf.ench.PlayerStartsConfigItemEnchantsEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.ench.PlayerStopsConfigItemEnchantsEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigDeathInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigStartInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigDeathInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigStartInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.mobs.PlayerStartsConfigMobSpawnEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.mobs.PlayerStopsConfigMobSpawnEvent;
import fr.altaks.mcoapi.util.ItemManager;
import javafx.util.Pair;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

import java.util.*;

import static org.bukkit.event.EventPriority.HIGH;
import static org.bukkit.event.EventPriority.LOW;

public class GameConfiguration implements Listener, CommandExecutor {

    private GameManager gameManager;

    private ArrayList<Player> playerConfiguringCraftableItems = new ArrayList<>();

    public LoadableInventory getStartInv() {
        return startInv;
    }

    public DeathInventory getDeathInv() {
        return deathInv;
    }

    public ArrayList<Enchantment> getDisabledEnchantments() {
        return disabledEnchantments;
    }

    public HashMap<Pair<Material, Enchantment>, Integer> getRestrictedEnchantements() {
        return restrictedEnchantements;
    }

    public ArrayList<ItemStack> getDisabledCraftingItems() {
        return disabledCraftingItems;
    }

    public float getXpMultiplier() {
        return xpMultiplier;
    }

    public int getGameSlots() {
        return gameSlots;
    }

    public ArrayList<UUID> getWhiteList() {
        return whiteList;
    }

    public ArrayList<EntityType> getDisabledEntities() {
        return disabledEntities;
    }

    public ArrayList<Player> getPlayersConfiguringStartInv() {
        return playersConfiguringStartInv;
    }

    public ArrayList<Player> getPlayersConfiguringDeathInv() {
        return playersConfiguringDeathInv;
    }

    // Inventory that will be copied to the player on the start of the game
    private LoadableInventory startInv;
    // Inventory that will be dropped by the player at the end of the game
    private DeathInventory deathInv;

    // List of disabled enchantments during the game
    private ArrayList<Enchantment> disabledEnchantments = new ArrayList<>();
    // List of restricted enchantments during the game, linked to the max level they can have
    private HashMap<Pair<Material, Enchantment>, Integer> restrictedEnchantements = new HashMap<>();

    // List of disabled items that players can't craft
    private ArrayList<ItemStack> disabledCraftingItems = new ArrayList<>();

    // xp multiplier on every xp gain
    private float xpMultiplier = 1.0f;

    // amount of slots of the game
    private int gameSlots;

    // list of UUID that are whitelisted by the game
    private ArrayList<UUID> whiteList;

    // list of entity types that won't spawn during the game
    private ArrayList<EntityType> disabledEntities = new ArrayList<>();

    // list of players that are configuring the starting inventory
    private ArrayList<Player> playersConfiguringStartInv = new ArrayList<>();

    // list of players that are configuring the death inventory
    private ArrayList<Player> playersConfiguringDeathInv = new ArrayList<>();
    private Inventory gameConfigInv = null;

    /* -------------< Configuration générale de la partie >------------- */

    public GameConfiguration(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerStartsConfigWholeGameEvent(PlayerStartConfigPartyEvent event) {
        if(gameConfigInv == null) {
            gameConfigInv = Bukkit.createInventory(null, 2 * 9, ChatColor.DARK_GRAY + "Configuration de la partie");

            // 0,  1,  2,  3,  4,  5,  6,  7,  8
            // 9, 10, 11, 12, 13, 14, 15, 16, 17

            // 0 : Gestion de l'inventaire de départ
            // 10 : Gestion de l'inventaire de mort
            gameConfigInv.setItem(0, new ItemManager.ItemBuilder(Material.IRON_SWORD, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage de l'inventaire de départ").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
            gameConfigInv.setItem(10, new ItemManager.ItemBuilder(Material.IRON_CHESTPLATE, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage de l'inventaire de décès").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());

            // 8 : Gestion des limites d'enchantements
            // 16 : Gestion des spawns de mobs
            gameConfigInv.setItem(8, new ItemManager.ItemBuilder(Material.ENCHANTED_BOOK, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage des enchantements").build());
            gameConfigInv.setItem(16, new ItemManager.ItemBuilder(Material.MONSTER_EGG, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage des monstres").build());

            // 3 : Gestion des interdictions de craft
            // 5 : Gestion des boosts d'xp
            // 12 : Gestion des timers / réglages des plugins
            // 14 : Gestion de la whitelist
            gameConfigInv.setItem(3, new ItemManager.ItemBuilder(Material.WORKBENCH, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage des crafts").build());

            gameConfigInv.setItem(5,
                    new ItemManager.ItemBuilder(Material.EXP_BOTTLE, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage de l'exp")
                            .setLore(
                                    ChatColor.YELLOW + "Taux actuel : " + (100 * xpMultiplier) + "%",
                                    ChatColor.GRAY + "Clic gauche : +1% d'xp",
                                    ChatColor.GRAY + "Clic droit : -1% d'xp",
                                    ChatColor.GRAY + "Shift + clic gauche : +10% d'xp",
                                    ChatColor.GRAY + "Shift + clic droit : -10% d'xp"
                            )
                            .build()
            );

            gameConfigInv.setItem(12, new ItemManager.ItemBuilder(Material.WATCH, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage des timings").build());
            gameConfigInv.setItem(14, new ItemManager.ItemBuilder(Material.NAME_TAG, 1, ChatColor.YELLOW + "" + ChatColor.BOLD + "Réglage de la whitelist").build());
        }
        event.getPlayer().openInventory(gameConfigInv);
    }

    @EventHandler
    public void onPlayerInteractsWithItemInWholeConfigInventory(InventoryClickEvent event) {
        if(event.getClickedInventory() == null || event.getClickedInventory().equals(event.getView().getBottomInventory()))
            return;
        if(!event.getInventory().equals(gameConfigInv)) return;

        if(event.getCurrentItem() == null) return;
        event.setCancelled(true);

        switch (event.getCurrentItem().getType()) {
            case IRON_CHESTPLATE:
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous allez configurer l'inventaire de décès");
                Bukkit.getPluginManager().callEvent(new PlayerStartConfigDeathInvEvent((Player) event.getWhoClicked()));
                ((Player) event.getWhoClicked()).spigot().sendMessage(buildConfigFinishComponent("Cliquez ici pour terminer la configuration", "configfinish deathinv", "Cliquez ici !"));
                event.getWhoClicked().closeInventory();
                break;
            case IRON_SWORD:
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous allez configurer l'inventaire de départ de la partie");
                Bukkit.getPluginManager().callEvent(new PlayerStartConfigStartInvEvent((Player) event.getWhoClicked()));
                ((Player) event.getWhoClicked()).spigot().sendMessage(buildConfigFinishComponent("Cliquez ici pour terminer la configuration", "configfinish startinv", "Cliquez ici !"));
                event.getWhoClicked().closeInventory();
                break;
            case ENCHANTED_BOOK:
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous allez configurer les enchantements disponibles");
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfigItemEnchantsEvent((Player) event.getWhoClicked()));
                ((Player) event.getWhoClicked()).spigot().sendMessage(buildConfigFinishComponent("Cliquez ici pour terminer la configuration", "configfinish enchants", "Cliquez ici !"));
                event.getWhoClicked().closeInventory();
                break;
            case MONSTER_EGG:
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous allez configurer les monstres/entités disponibles");
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfigMobSpawnEvent((Player) event.getWhoClicked()));
                ((Player) event.getWhoClicked()).spigot().sendMessage(buildConfigFinishComponent("Cliquez ici pour terminer la configuration", "configfinish mobs", "Cliquez ici !"));
                event.getWhoClicked().closeInventory();
                break;
            case WORKBENCH:
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous allez configurer les recettes de craft disponibles");
                Bukkit.getPluginManager().callEvent(new PlayerStartsConfigCraftableItems((Player) event.getWhoClicked()));
                ((Player) event.getWhoClicked()).spigot().sendMessage(buildConfigFinishComponent("Cliquez ici pour terminer la configuration", "configfinish craftings", "Cliquez ici !"));
                event.getWhoClicked().closeInventory();
                break;
            case EXP_BOTTLE: {
                // update xpMultiplier depending on the action type

                float change = 0.01f;
                if(event.isShiftClick()) change *= 10;

                if(event.isLeftClick())
                    xpMultiplier += change;
                else if(event.isRightClick())
                    xpMultiplier -= change;
                else return;

                // update lore / item displayed
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                List<String> lore = meta.getLore();
                lore.set(0, ChatColor.YELLOW + "Taux actuel : " + String.format("%.2f", 100 * xpMultiplier) + "%");
                meta.setLore(lore);
                event.getCurrentItem().setItemMeta(meta);

                Main.LOG.debuglog("XP Multiplier set to " + 100 * xpMultiplier);
                Main.LOG.devlog("Modified lore for item" + event.getCurrentItem());
                break;
            }
            case NAME_TAG: {
                // TODO : Je ne sais pas ce qu'il y a a faire, voir discord
            }
        }

    }

    @EventHandler
    public void onPlayerAcquiresXP(PlayerExpChangeEvent event) {
        // modify xp change depending on xpMultiplier, if it's a gain
        if(event.getAmount() > 0) {
            event.setAmount((int) (event.getAmount() * xpMultiplier));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("configfinish") && sender instanceof Player && args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "deathinv":
                    Bukkit.getPluginManager().callEvent(new PlayerEndsConfigDeathInvEvent((Player) sender));
                    break;
                case "startinv":
                    Bukkit.getPluginManager().callEvent(new PlayerEndsConfigStartInvEvent((Player) sender));
                    break;
                case "enchants":
                    Bukkit.getPluginManager().callEvent(new PlayerStopsConfigItemEnchantsEvent((Player) sender));
                    break;
                case "mobs":
                    Bukkit.getPluginManager().callEvent(new PlayerStopsConfigMobSpawnEvent((Player) sender));
                    break;
                case "craftings":
                    Bukkit.getPluginManager().callEvent(new PlayerStopsConfigCraftableItems((Player) sender));
                    break;
                default:
                    return true;

            }
            return true;
        }
        return false;
    }

    public TextComponent buildConfigFinishComponent(String text, String command, String hovertext) {
        TextComponent compo = new TextComponent(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------\n");

        TextComponent temp = new TextComponent(" " + text);
        temp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hovertext).create()));
        temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        compo.addExtra(temp);

        compo.addExtra(new TextComponent(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------"));
        return compo;
    }


    /* -------------< Configuration des items désactivés au craft >------------- */

    @EventHandler(priority = LOW)
    public void onPlayerStartsConfigCraftableItems(PlayerStartsConfigCraftableItems event) {
        this.playerConfiguringCraftableItems.add(event.getPlayer());
    }

    @EventHandler(priority = LOW)
    public void onPlayerStopsConfigCraftableItems(PlayerStopsConfigCraftableItems event){
        this.playerConfiguringCraftableItems.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwitchesCraftableItemStatus(PlayerInteractEvent event){
        if(!playerConfiguringCraftableItems.contains(event.getPlayer())) return;

        ItemStack item = event.getItem();
        ItemStack foundItem = null;

        boolean containsItem = false;
        for(ItemStack currentlyDisabledItem : disabledCraftingItems){
            if(ItemManager.lightCompare(currentlyDisabledItem, item)) {
                containsItem = true;
                foundItem = currentlyDisabledItem;
                break;
            }
        }

        if(containsItem) {
            disabledCraftingItems.remove(foundItem);
            event.getPlayer().sendMessage(Main.PREFIX + "L'item " + event.getItem().getType().name().toLowerCase() + " a été réactivé !");
        } else {
            disabledCraftingItems.add(item);
            event.getPlayer().sendMessage(Main.PREFIX + "L'item " + event.getItem().getType().name().toLowerCase() + " a été désactivé !");
        }
    }


    @EventHandler(priority = HIGH)
    public void onPlayerTriesToCraftItem(CraftItemEvent event){
        for(ItemStack currentlyDisabledCraftingItem : disabledCraftingItems){
            if(ItemManager.lightCompare(event.getRecipe().getResult(), currentlyDisabledCraftingItem)){
                Main.LOG.devlog(event.getWhoClicked().getName() + " tried to craft " + event.getRecipe().getResult());
                event.setCancelled(true);
            }
        }
    }

    private InventoryAction[] pickupActions = {
        InventoryAction.PICKUP_SOME,InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL
    };
    private ArrayList<Player> playerConfiguringMobSpawns = new ArrayList<>();
    private Inventory mobSpawnConfigInv = null;
    private EntityType[] mobsOrAnimals = {

            EntityType.BAT, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CHICKEN, EntityType.COW,
            EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GHAST, EntityType.GIANT, EntityType.HORSE, EntityType.IRON_GOLEM,
            EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PIG, EntityType.PIG_ZOMBIE, EntityType.SHEEP,
            EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SNOWMAN, EntityType.SPIDER, EntityType.SQUID,
            EntityType.VILLAGER, EntityType.WITCH, EntityType.WITHER, EntityType.WOLF, EntityType.ZOMBIE

    };

    @EventHandler
    public void onPlayerPicksUpForbiddenItem(InventoryClickEvent event){
        if(event.getClickedInventory() == null || event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
        for(InventoryAction action : pickupActions){
            if(action == event.getAction()){
                // it's a pickup action in an external inventory ...
                for(ItemStack item : disabledCraftingItems){
                    if(ItemManager.lightCompare(event.getCursor(), item)) {
                        event.setCancelled(true); // cancel pickup event :)
                        event.getInventory().setItem(event.getSlot(), null); // despawn the item
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerUsesForbiddenItem(PlayerItemConsumeEvent event) {
        for(ItemStack item : disabledCraftingItems){
            if(ItemManager.lightCompare(event.getItem(), item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Main.PREFIX + "Vous ne pouvez pas utiliser cet objet !");
            }
        }
    }

    /* -------------< Configuration de l'inventaire de décès de la partie >------------- */

    @EventHandler(priority = LOW)
    public void onPlayerStartConfigDeathInv(PlayerStartConfigDeathInvEvent event){
        this.playersConfiguringDeathInv.add(event.getPlayer());
        this.deathInv = new DeathInventory();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractsItem(PlayerInteractEvent event){
        if(this.playersConfiguringDeathInv.contains(event.getPlayer())){
            Material material = event.getPlayer().getInventory().getItemInHand().getType();
            if(this.deathInv.getMaterialsToDrop().contains(material)){
                event.getPlayer().sendMessage(Main.PREFIX + "Vous venez de désactiver " + material.name());
                deathInv.disableMaterial(material);
            } else {
                event.getPlayer().sendMessage(Main.PREFIX + "Vous venez d'activer " + material.name());
                deathInv.enableMaterial(material);
            }
        }
    }

    @EventHandler(priority = LOW)
    public void onPlayerEndsConfigDeathInv(PlayerEndsConfigDeathInvEvent event){
        this.playersConfiguringDeathInv.remove(event.getPlayer());

        StringJoiner joiner = new StringJoiner(", ");
        for(Material material : this.deathInv.materialsToDrop){
            joiner.add("Matériaux:  " + material.name());
        }

        event.getPlayer().sendMessage(
                Main.PREFIX + "Vous venez de configurer les matériaux droppés en cas de mort durant la partie\n" +
                        "Vous avez activé le drop des matériaux suivants : \n " + joiner.toString()
        );
    }

    /* -------------< Gestion du filtre de l'inventaire de décès >------------- */

    @EventHandler
    public void onPlayerDies(PlayerDeathEvent event){
        // pour tout l'inventaire du joueur, filtrer et droper
        Inventory inv = event.getEntity().getInventory();
        World world = event.getEntity().getWorld();
        for(int slot = 0; slot < inv.getSize(); slot++){
            if(inv.getItem(slot) != null && this.deathInv != null && this.deathInv.getMaterialsToDrop().contains(inv.getItem(slot).getType())){
                world.dropItem(event.getEntity().getLocation(), inv.getItem(slot));
            }
        }
    }

    public class LoadableInventory {

        private HashMap<ItemStack, Integer> itemsOfTheMainInv = new HashMap<>();
        private ItemStack helmet, chestplate, leggings, boots;
        private int mainHandSlot;

        public void loadFromPlayer(Player player){
            for(int slot = 0; slot < player.getInventory().getSize(); slot++){
                if(player.getInventory().getItem(slot) != null){
                    this.itemsOfTheMainInv.put(player.getInventory().getItem(slot), slot);
                }
            }

            if(player.getInventory().getHelmet() != null) this.helmet = player.getInventory().getHelmet();
            if(player.getInventory().getChestplate() != null) this.chestplate = player.getInventory().getChestplate();
            if(player.getInventory().getLeggings() != null) this.leggings = player.getInventory().getLeggings();
            if(player.getInventory().getBoots() != null) this.boots = player.getInventory().getBoots();

            this.mainHandSlot = player.getInventory().getHeldItemSlot();
        }

        public void injectToPlayer(Player player){

            player.getInventory().clear();

            // Inject the main inventory to the player
            for(Map.Entry<ItemStack, Integer> itemEntry : this.itemsOfTheMainInv.entrySet()){
                player.getInventory().setItem(itemEntry.getValue(), itemEntry.getKey());
            }

            // Inject the armor to the player
            if(this.helmet != null) player.getInventory().setHelmet(this.helmet);
            if(this.chestplate != null) player.getInventory().setChestplate(this.chestplate);
            if(this.leggings != null) player.getInventory().setLeggings(this.leggings);
            if(this.boots != null) player.getInventory().setBoots(this.boots);

            player.getInventory().setHeldItemSlot(this.mainHandSlot);
            player.updateInventory();
        }

    }

    private class DeathInventory {

        private ArrayList<Material> materialsToDrop = new ArrayList<>();

        public ArrayList<Material> getMaterialsToDrop() {
            return materialsToDrop;
        }

        private void enableMaterial(Material material){
            materialsToDrop.add(material);
        }

        private void disableMaterial(Material material){
            materialsToDrop.remove(material);
        }

    }

    /* -------------< Configuration des limitations d'enchantements par item >------------- */

    private HashMap<Material, Inventory> currentConfiguredInv = new HashMap<>();
    private ArrayList<Player> playerConfiguringItemEnchants = new ArrayList<>();
    private HashMap<Material, ArrayList<Enchantment>> itemDisabledEnchants = new HashMap<>();

    @EventHandler
    public void onPlayerStartsConfigItemEnchants(PlayerStartsConfigItemEnchantsEvent event){
        if(event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR){
            event.getPlayer().sendMessage(Main.PREFIX + "Veuillez placer un item dans votre main !");
            return;
        }
        Inventory inventoryToOpen;
        ItemStack item = event.getPlayer().getItemInHand();
        Material material = item.getType();
        if(currentConfiguredInv.containsKey(material)){
            inventoryToOpen = currentConfiguredInv.get(material);
        } else {
            ArrayList<Enchantment> compatibleEnchantements = new ArrayList<>();

            for(Enchantment ench : Enchantment.values()){
                if(ench.canEnchantItem(item)){
                    compatibleEnchantements.add(ench);
                }
            }

            int invSize = (compatibleEnchantements.size() % 9 == 0) ? (compatibleEnchantements.size() / 9) * 9 : (compatibleEnchantements.size() / 9) * 9 + 9;

            inventoryToOpen = Bukkit.createInventory(null, invSize, "Config. enchantementss. ");

            for(Enchantment ench : compatibleEnchantements){
                    ItemStack book = new ItemManager.EnchantedBookBuilder(Material.ENCHANTED_BOOK, 1, ChatColor.AQUA + "Config. enchantement :")
                            .addEnchant(ench, 1, false)
                            .setLore(
                                ChatColor.YELLOW + "Clic gauche : +1 niv",
                                ChatColor.YELLOW + "Clic droit : -1 niv",
                                ChatColor.RED + "Shift clic gauche : Activation globale",
                                ChatColor.RED + "Shift clic droit : Désactivation globale"
                            )
                            .build();
                    if(inventoryToOpen.firstEmpty() != -1){
                        inventoryToOpen.addItem(book);
                    }
            }
            this.currentConfiguredInv.put(material, inventoryToOpen);
        }
        event.getPlayer().openInventory(inventoryToOpen);
        playerConfiguringItemEnchants.add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractsEnchantWhileConfiguringItemEnchants(InventoryClickEvent event){
        if(playerConfiguringItemEnchants.contains((Player) event.getWhoClicked())) {
            // if this is not the top inventory, ignore it
            if(event.getClickedInventory() == null) return;
            if(!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

            // detect the item
            if(event.getCurrentItem() == null) return;

            Main.LOG.devlog("Item courant " + event.getCurrentItem());
            Main.LOG.devlog("Item curseur " + event.getCursor());

            ItemStack item = event.getCurrentItem();
            Material mat = null;

            for(Map.Entry<Material, Inventory> entry : currentConfiguredInv.entrySet())
                if(entry.getValue().equals(event.getInventory())) {
                    mat = entry.getKey();
                    break;
                }


            event.setCancelled(true);

            Main.LOG.devlog("Item courant " + event.getCurrentItem());
            Main.LOG.devlog("Item curseur " + event.getCursor());

            // apply changes on enchanted book level
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) item.getItemMeta();
            Map.Entry<Enchantment, Integer> ench = (Map.Entry<Enchantment, Integer>) bookMeta.getStoredEnchants().entrySet().toArray()[0];

            int levelIncrease = 0;
            // player is configuring an item enchant
            if(event.isLeftClick()) {
                // Ajouter un niveau
                levelIncrease = 1;
                if(disabledEnchantments.contains(ench.getKey())){
                    disabledEnchantments.remove(ench.getKey());
                    levelIncrease = 0;
                } else if(itemDisabledEnchants.containsKey(mat) && itemDisabledEnchants.get(mat).contains(ench.getKey())){
                    itemDisabledEnchants.get(mat).remove(ench.getKey());
                    levelIncrease = 0;
                }
            } else if(event.isRightClick()){
                // Retirer un niveau
                levelIncrease = -1;
            }

            int levelToSet = ench.getValue() + levelIncrease;

            if(event.isShiftClick()) {
                if(event.isRightClick() && !disabledEnchantments.contains(ench.getKey())){
                    // Desactiver l'enchantement
                    levelToSet = 1;
                    disabledEnchantments.add(ench.getKey());
                }
            }

            if(levelToSet > ench.getKey().getMaxLevel()){
                event.getWhoClicked().sendMessage(Main.PREFIX + "Vous ne pouvez pas sortir des limites des enchantments possibles !");
                return;
            } else if(levelToSet <= 0){
                // on désactive l'enchantement que sur cet item
                levelToSet = 1;
                if(!itemDisabledEnchants.containsKey(mat)){
                    itemDisabledEnchants.put(mat, new ArrayList<Enchantment>());
                }
                if(!itemDisabledEnchants.get(mat).contains(ench.getKey())){
                    itemDisabledEnchants.get(mat).add(ench.getKey());
                }
            }

            ItemManager.ItemBuilder newBook = new ItemManager.EnchantedBookBuilder(Material.ENCHANTED_BOOK, 1, ChatColor.AQUA + "Config. enchantement :")
                    .addEnchant(ench.getKey(), levelToSet, false)
                    .setLore(
                            ChatColor.YELLOW + "Clic gauche : +1 niv",
                            ChatColor.YELLOW + "Clic droit : -1 niv",
                            ChatColor.RED + "Shift clic gauche : Activation globale",
                            ChatColor.RED + "Shift clic droit : Désactivation globale"
                    );

            if(disabledEnchantments.contains(ench.getKey())){
                // l'enchantement est désactivé globalement
                newBook.addLore(ChatColor.BOLD + "" + ChatColor.DARK_RED + "Enchantement désactivé globalement");

            } else if(itemDisabledEnchants.containsKey(mat) && itemDisabledEnchants.get(mat).contains(ench.getKey())){
                // this enchant is disabled but only on this item
                newBook.addLore( ChatColor.DARK_RED + "Enchantement désactivé sur l'objet");
            }

            event.getInventory().setItem(event.getSlot(), newBook.build());
            ((Player) event.getWhoClicked()).updateInventory();
        }
    }

    @EventHandler
    public void onPlayerClosesItemEnchantInv(InventoryCloseEvent event){
        if(event.getInventory().getType() != InventoryType.CHEST) return;
        if(playerConfiguringItemEnchants.contains(event.getPlayer())){
            for(Inventory inv : currentConfiguredInv.values()){
                if(event.getInventory().getName().equals(inv.getName())){
                    Bukkit.getPluginManager().callEvent(new PlayerStopsConfigItemEnchantsEvent((Player) event.getPlayer()));
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractsWithForbiddenItem(PlayerInteractEvent event) {
        if(!event.hasItem()) return;
        for(ItemStack item : disabledCraftingItems){
            if(ItemManager.lightCompare(event.getItem(), item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Main.PREFIX + "Vous ne pouvez pas utiliser cet objet !");
            }
        }
    }

    /* ------------------< Configuration de la gestion des spawns de mobs >------------------ */

    /* -------------< Configuration de l'inventaire de départ de la partie >------------- */
    @EventHandler(priority = LOW)
    public void onPlayerStartConfigStartingInv(PlayerStartConfigStartInvEvent event) {
        this.playersConfiguringStartInv.add(event.getPlayer());
        // clear player's inv
        event.getPlayer().getInventory().clear();
        event.getPlayer().setGameMode(GameMode.CREATIVE);
        event.getPlayer().setFlying(false);
        this.startInv = new LoadableInventory();
    }

    @EventHandler(priority = LOW)
    public void onPlayerEndsConfigStartingInv(PlayerEndsConfigStartInvEvent event) {
        this.startInv.loadFromPlayer(event.getPlayer());
        // si le joueur est l'host, lui redonner le stuff de config
        event.getPlayer().getInventory().clear();
        event.getPlayer().setGameMode(GameMode.SURVIVAL);

        if(event.getPlayer().equals(gameManager.getHost())){
            gameManager.giveHostItems(event.getPlayer());
        }
        this.playersConfiguringStartInv.remove(event.getPlayer());
        event.getPlayer().sendMessage(Main.PREFIX + "Vous venez de configurer l'inventaire de départ de la partie");
    }

    @EventHandler
    public void onPlayerStopsConfigItemEnchants(PlayerStopsConfigItemEnchantsEvent event) {
        event.getPlayer().sendMessage(Main.PREFIX + "Vous venez de finir de configurer les limites de l'item " + event.getPlayer().getItemInHand().getType());
        playerConfiguringItemEnchants.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerStartsConfigMobSpawns(PlayerStartsConfigMobSpawnEvent event) {
        event.getPlayer().sendMessage(Main.PREFIX + "Vous venez de commencer à configurer les spawns de mobs");
        playerConfiguringMobSpawns.add(event.getPlayer());
        if(mobSpawnConfigInv != null) {
            event.getPlayer().openInventory(mobSpawnConfigInv);
        } else {
            mobSpawnConfigInv = Bukkit.createInventory(null, 6 * 9, ChatColor.DARK_GRAY + "Config. des spawns de mobs");

            // Placer les œufs de mobs dans l'inventaire
            for(EntityType type : mobsOrAnimals){
                ItemStack item = new ItemStack(Material.MONSTER_EGG, 1, type.getTypeId());
                SpawnEgg data = new SpawnEgg(type);
                item.setData(data);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Activation des spawns de " + type.getName());
                meta.setLore(Arrays.asList(
                        ChatColor.GREEN + "Statut : Activé"
                ));
                item.setItemMeta(meta);
                mobSpawnConfigInv.addItem(item);
            }
            event.getPlayer().openInventory(mobSpawnConfigInv);
        }
    }

    @EventHandler
    public void onPlayerSwitchesMobActivationStatus(InventoryClickEvent event) {
        if(event.getInventory() == null || event.getClickedInventory() == null || event.getInventory().getType() != InventoryType.CHEST)
            return;
        if(!event.getInventory().equals(mobSpawnConfigInv)) return;
        if(!event.getClickedInventory().equals(event.getView().getTopInventory())) return;
        if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if(event.getCurrentItem().getType() != Material.MONSTER_EGG) return;

        event.setCancelled(true);
        ItemStack monsterEgg = event.getCurrentItem();
        EntityType type = ((SpawnEgg) monsterEgg.getData()).getSpawnedType();
        if(type == null) return;

        if(disabledEntities.contains(type)) {
            disabledEntities.remove(type);
            event.getWhoClicked().sendMessage(Main.PREFIX + "Vous venez d'activer les spawns de " + type.getName());
            ItemStack newItem = monsterEgg.clone();
            ItemMeta meta = newItem.getItemMeta();
            meta.setLore(Arrays.asList(
                    ChatColor.GREEN + "Statut : Activé"
            ));
            newItem.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), newItem);
        } else {
            disabledEntities.add(type);
            event.getWhoClicked().sendMessage(Main.PREFIX + "Vous venez de désactiver les spawns de " + type.getName());
            ItemStack newItem = monsterEgg.clone();
            ItemMeta meta = newItem.getItemMeta();
            meta.setLore(Arrays.asList(
                    ChatColor.RED + "Statut : Désactivé"
            ));
            newItem.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), newItem);
        }
        ((Player) event.getWhoClicked()).updateInventory();
    }

    @EventHandler(priority = HIGH)
    public void onDisabledEntitySpawnEvent(EntitySpawnEvent event) {
        if(disabledEntities.contains(event.getEntityType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClosesInventory(InventoryCloseEvent event) {
        if(event.getInventory().getType() != InventoryType.CHEST) return;
        if(playerConfiguringMobSpawns.contains(event.getPlayer())) {
            if(event.getInventory().equals(mobSpawnConfigInv)) {
                Bukkit.getPluginManager().callEvent(new PlayerStopsConfigMobSpawnEvent((Player) event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onPlayerStopsConfigMobSpawns(PlayerStopsConfigMobSpawnEvent event) {
        event.getPlayer().sendMessage(Main.PREFIX + "Vous venez de finir de configurer les spawns de mobs");
        playerConfiguringMobSpawns.remove(event.getPlayer());
        for(World world : Bukkit.getWorlds()){
            for(Entity entity : world.getEntities()){
                if(disabledEntities.contains(entity.getType())) {
                    entity.remove();
                }
            }
        }
    }

    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.COMMAND, 1, "Configuration de la partie").addFakeEnchant().build();
    }

}
