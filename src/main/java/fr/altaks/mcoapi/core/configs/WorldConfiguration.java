package fr.altaks.mcoapi.core.configs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.worldconf.HostCallsMapConfigurationEvent;
import fr.altaks.mcoapi.util.ItemManager;
import fr.altaks.mcoapi.util.worldmanip.DynamicClassFunctions;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorldConfiguration implements Listener {

    private GameManager gameManager;
    private Inventory configInv;
    private final ArrayList<Player> playersConfiguringWorldGen = new ArrayList<>();

    private static final String[] oreMultiplierLore = {
            ChatColor.YELLOW + "Multiplicateur : 1",
            ChatColor.GRAY + "Clic gauche : +0.1",
            ChatColor.GRAY + "Clic droit  : -0.1",
            ChatColor.GRAY + "Shift clic gauche : +1",
            ChatColor.GRAY + "Shift clic droit  : -1"
    }, itemMultipliersLore = {
            ChatColor.YELLOW + "Multiplicateur : 1",
            ChatColor.GRAY + "Clic gauche : +1",
            ChatColor.GRAY + "Clic droit  : -1"
    }, structActivationLore = {
            ChatColor.YELLOW + "Statut : Activé",
            ChatColor.GRAY + "Clic : Inverser le statut"
    };

    private static final HashMap<Material, String>
            structJsonPaths = new HashMap<>(),
            oreJsonPath = new HashMap<>();

    private static final HashMap<Material, Integer> defaultOreSize = new HashMap<>();

    private final HashMap<Material, Float> oreMultipliers = new HashMap<>();

    static {
        structJsonPaths.put(Material.ENDER_PORTAL_FRAME, "useStrongholds");
        structJsonPaths.put(Material.MINECART, "useMineShafts");

        oreJsonPath.put(Material.COAL_ORE, "coalSize");
        defaultOreSize.put(Material.COAL_ORE, 17);

        oreJsonPath.put(Material.IRON_ORE, "ironSize");
        defaultOreSize.put(Material.IRON_ORE, 27);

        oreJsonPath.put(Material.GOLD_ORE, "goldSize");
        defaultOreSize.put(Material.GOLD_ORE, 22);

        oreJsonPath.put(Material.LAPIS_ORE, "lapisSize");
        defaultOreSize.put(Material.LAPIS_ORE, 25);

        oreJsonPath.put(Material.DIAMOND_ORE, "diamondSize");
        defaultOreSize.put(Material.DIAMOND_ORE, 20);

        oreJsonPath.put(Material.REDSTONE_ORE, "redstoneSize");
        defaultOreSize.put(Material.REDSTONE_ORE, 19);

    }

    private final HashMap<Material, Integer> itemMultipliers = new HashMap<>();

    private final String defaultWorldGenerationParameters =
            "{\"coordinateScale\":684.412,\"heightScale\":684.412,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":80.0,\"mainNoiseScaleY\":160.0,\"mainNoiseScaleZ\":80.0,\"baseSize\":8.5,\"stretchY\":12.0,\"biomeDepthWeight\":1.0,\"biomeDepthOffset\":0.0,\"biomeScaleWeight\":1.0,\"biomeScaleOffset\":0.0,\"seaLevel\":63,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":8,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":4,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":4,\"riverSize\":4,\"dirtSize\":33,\"dirtCount\":10,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":33,\"graniteCount\":10,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,\"dioriteSize\":33,\"dioriteCount\":10,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,\"andesiteSize\":33,\"andesiteCount\":10,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,\"coalSize\":17,\"coalCount\":20,\"coalMinHeight\":0,\"coalMaxHeight\":128,\"ironSize\":9,\"ironCount\":20,\"ironMinHeight\":0,\"ironMaxHeight\":64,\"goldSize\":9,\"goldCount\":2,\"goldMinHeight\":0,\"goldMaxHeight\":32,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,\"diamondSize\":8,\"diamondCount\":1,\"diamondMinHeight\":0,\"diamondMaxHeight\":16,\"lapisSize\":7,\"lapisCount\":1,\"lapisCenterHeight\":16,\"lapisSpread\":16}";

    @SuppressWarnings("deprecated")
    @EventHandler
    public void onPlayerInteractsWorldConfig(InventoryClickEvent event){
        if(event.getInventory().equals(configInv)){
            if(event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

            // detect the material
            event.setCancelled(true);
            if(event.getCurrentItem() == null) return;

            Material material = event.getCurrentItem().getType();
            switch (material) {
                case STAINED_GLASS_PANE: // clicked on a useless item
                    break;

                case COAL_ORE:
                case IRON_ORE:
                case GOLD_ORE:
                case DIAMOND_ORE:
                case EMERALD_ORE:
                case REDSTONE_ORE:
                case LAPIS_ORE: {

                    // calculate the new multiplier
                    float multiplier = 1;
                    if(oreMultipliers.containsKey(material)) multiplier = oreMultipliers.get(material);

                    float change = event.isShiftClick() ? 1f : 0.1f;

                    if(event.isLeftClick()) {
                        multiplier += change;
                    } else if(event.isRightClick()) {
                        multiplier -= change;
                    }

                    // lock the change
                    if(multiplier <= 0) multiplier = 0; else if(multiplier >= 4) multiplier = 4;
                    oreMultipliers.put(material, multiplier);

                    ItemManager.ItemBuilder item = new ItemManager.ItemBuilder(event.getCurrentItem());
                    item.setLore(
                            ChatColor.YELLOW + "Multiplicateur : " + String.format("%.2f", multiplier),
                            ChatColor.GRAY + "Clic gauche : +0.1",
                            ChatColor.GRAY + "Clic droit  : -0.1",
                            ChatColor.GRAY + "Shift clic gauche : +1",
                            ChatColor.GRAY + "Shift clic droit  : -1"
                    );
                    event.getInventory().setItem(event.getSlot(), item.build());
                    break;
                }
                case FLINT:
                case SUGAR_CANE:
                case APPLE: {
                    // calculate the new multiplier

                    int multiplier = 1;
                    if(itemMultipliers.containsKey(material)) multiplier = itemMultipliers.get(material);

                    if(event.isLeftClick()) {
                        multiplier += 1;
                    } else if(event.isRightClick()) {
                        multiplier -= 1;
                    }

                    // lock the change
                    if(multiplier <= 0) multiplier = 0;
                    else if(multiplier >= 3) multiplier = 3;
                    itemMultipliers.put(material, multiplier);

                    ItemManager.ItemBuilder item = new ItemManager.ItemBuilder(event.getCurrentItem());
                    item.setLore(
                            ChatColor.YELLOW + "Multiplicateur : " + multiplier,
                            ChatColor.GRAY + "Clic gauche : +1",
                            ChatColor.GRAY + "Clic droit  : -1"
                    );
                    event.getInventory().setItem(event.getSlot(), item.build());
                    break;
                }
                case MINECART:
                case ENDER_PORTAL_FRAME: {
                    boolean activationStatus = true;
                    if(structActivationStatus.containsKey(material))
                        activationStatus = structActivationStatus.get(material);

                    activationStatus = !activationStatus;

                    structActivationStatus.put(material, activationStatus);

                    ItemManager.ItemBuilder item = new ItemManager.ItemBuilder(event.getCurrentItem());
                    item.setLore(
                            ChatColor.YELLOW + "Statut : " + ((activationStatus) ? "Activé" : ChatColor.RED + "Désactivé"),
                            ChatColor.GRAY + "Clic : Inverser le statut"
                    );
                    event.getInventory().setItem(event.getSlot(), item.build());
                    break;
                }
                case COMMAND:
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(Main.PREFIX + "Re-génération de la map...");
                    regenerate("world", new Location(Bukkit.getWorld("temp"), 0, 100, 0));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.gameManager.getMain(), new BukkitRunnable() {
                        @Override
                        public void run() {
                            event.getWhoClicked().sendMessage(Main.PREFIX + "La map a été re-générée !");
                        }
                    }, 120);
                    break;
                case ARROW:
                    if(Bukkit.getWorld("world") == null) {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(Main.PREFIX + "Re-génération de la map...");
                        regenerate("world", new Location(Bukkit.getWorld("temp"), 0, 100, 0));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this.gameManager.getMain(), new BukkitRunnable() {
                            @Override
                            public void run() {
                                event.getWhoClicked().sendMessage(Main.PREFIX + "La map a été re-générée !");
                                event.getWhoClicked().teleport(new Location(Bukkit.getWorld("world"), 0, 100, 0));
                            }
                        }, 120);
                    } else {
                        event.getWhoClicked().teleport(new Location(Bukkit.getWorld("world"), 0, 100, 0));
                    }
                default:
                    break;
            }

        }
    }

    private final JsonObject worldGenParameters = new JsonParser().parse(defaultWorldGenerationParameters).getAsJsonObject();

    private final HashMap<Material, Boolean> structActivationStatus = new HashMap<>();
    private final ItemStack
            mineshaftConf = new ItemManager.ItemBuilder(Material.MINECART, 1, ChatColor.AQUA + "Apparition des Mines abandonnées").setLore(structActivationLore).build(),
            strongholdConf = new ItemManager.ItemBuilder(Material.ENDER_PORTAL_FRAME, 1, ChatColor.AQUA + "Apparition des Donjons (End)").setLore(structActivationLore).build(),

            silex = new ItemManager.ItemBuilder(Material.FLINT, 1, ChatColor.YELLOW + "Multiplicateur de drop de Silex").setLore(itemMultipliersLore).build(),
            sugarCane = new ItemManager.ItemBuilder(Material.SUGAR_CANE, 1, ChatColor.YELLOW + "Multiplicateur de drop de Canne à sucre").setLore(itemMultipliersLore).build(),
            apples = new ItemManager.ItemBuilder(Material.APPLE, 1, ChatColor.YELLOW + "Multiplicateur de drop de Pommes").setLore(itemMultipliersLore).build(),

            coalOre = new ItemManager.ItemBuilder(Material.COAL_ORE, 1, ChatColor.RED + "Multiplicateur de génération de Charbon").setLore(oreMultiplierLore).build(),
            ironOre = new ItemManager.ItemBuilder(Material.IRON_ORE, 1, ChatColor.RED + "Multiplicateur de génération de Fer").setLore(oreMultiplierLore).build(),
            goldOre = new ItemManager.ItemBuilder(Material.GOLD_ORE, 1, ChatColor.RED + "Multiplicateur de génération d'Or").setLore(oreMultiplierLore).build(),
            lapisOre = new ItemManager.ItemBuilder(Material.LAPIS_ORE, 1, ChatColor.RED + "Multiplicateur de génération de Lapis-Lazuli").setLore(oreMultiplierLore).build(),
            diamondOre = new ItemManager.ItemBuilder(Material.DIAMOND_ORE, 1, ChatColor.RED + "Multiplicateur de génération de Diamant").setLore(oreMultiplierLore).build(),
            redstoneOre = new ItemManager.ItemBuilder(Material.REDSTONE_ORE, 1, ChatColor.RED + "Multiplicateur de génération de Redstone").setLore(oreMultiplierLore).build(),

            generate = new ItemManager.ItemBuilder(Material.COMMAND, 1, ChatColor.LIGHT_PURPLE + "Générer la map").build(),
            visit = new ItemManager.ItemBuilder(Material.ARROW, 1, ChatColor.LIGHT_PURPLE + "Prévisualiser la map").build();

    public WorldConfiguration(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerStartsConfiguringWorldGeneration(HostCallsMapConfigurationEvent event) {
        if(configInv == null) {
            configInv = Bukkit.createInventory(null, 6 * 9, "Configuration. gen. map");

            configInv.setItem(11, mineshaftConf);
            configInv.setItem(12, strongholdConf);

            configInv.setItem(13, silex);
            configInv.setItem(14, sugarCane);
            configInv.setItem(15, apples);

            configInv.setItem(28, coalOre);
            configInv.setItem(29, ironOre);

            configInv.setItem(31, generate);

            configInv.setItem(33, lapisOre);

            configInv.setItem(37, goldOre);
            configInv.setItem(38, diamondOre);
            configInv.setItem(43, redstoneOre);

            configInv.setItem(49, visit);

            while(configInv.firstEmpty() != -1){
                configInv.setItem(configInv.firstEmpty(), ItemManager.PrebuiltItems.inventoryFillingGlassPane);
            }
        }
        event.getPlayer().openInventory(configInv);
        playersConfiguringWorldGen.add(event.getPlayer());
    }



    @EventHandler
    public void onPlayerClosesWorldGenConfiguration(InventoryCloseEvent event) {
        if(event.getInventory().equals(configInv)) {
            playersConfiguringWorldGen.remove(event.getPlayer());

            // recalculate parameters

            // ore parameters
            for(Map.Entry<Material, Float> oreMultiplierEntry : oreMultipliers.entrySet()){

                // calculate real ore size
                float finalValue = oreMultiplierEntry.getValue() * defaultOreSize.get(oreMultiplierEntry.getKey());

                // inject into json object
                worldGenParameters.addProperty(
                        oreJsonPath.get(oreMultiplierEntry.getKey()),
                        finalValue
                );
            }

            // structures objects
            for(Map.Entry<Material, Boolean> structStatus : structActivationStatus.entrySet()){

                // inject into json object
                worldGenParameters.addProperty(
                        structJsonPaths.get(structStatus.getKey()),
                        structStatus.getValue()
                );
            }

            Main.LOG.devlog("New WorldGen Parameters : \n" + worldGenParameters.toString());
        }
    }

    @EventHandler
    public void onBlockDropsItemEvent(BlockBreakEvent event) {

        for(ItemStack item : event.getBlock().getDrops(event.getPlayer().getItemInHand())){
            if(itemMultipliers.containsKey(item.getType())) {
                item.setAmount(item.getAmount() * itemMultipliers.get(item.getType()).intValue());
            }
        }

    }

    public void regenerate(String worldname, Location backupLocationForPlayers) {

        // unload & delete specified world name
        World world = Bukkit.getWorld(worldname);
        if(world != null) {
            while(!world.getPlayers().isEmpty()) {
                for(Player player : world.getPlayers()){
                    player.teleport(backupLocationForPlayers);
                }
            }

            File worldDirectory = world.getWorldFolder();

            world.setKeepSpawnInMemory(false);
            world.setAutoSave(false);

            Main.LOG.devlog("Unloading world : " + world);

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.gameManager.getMain(), new BukkitRunnable() {
                @Override
                public void run() {
                    DynamicClassFunctions.bindRegionFiles();
                    DynamicClassFunctions.forceUnloadWorld(world, backupLocationForPlayers);
                    DynamicClassFunctions.clearWorldReference(world.getName());

                    Main.LOG.devlog("Has unloaded world {" + worldname + "}");

                    try {
                        FileUtils.deleteDirectory(worldDirectory);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }, 40);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(gameManager.getMain(), new BukkitRunnable() {
            @Override
            public void run() {
                WorldCreator creator = new WorldCreator(worldname);

                creator.environment(World.Environment.NORMAL);
                creator.type(WorldType.CUSTOMIZED);
                creator.generatorSettings(worldGenParameters.toString());
                creator.seed(4155519082845793722L);

                creator.createWorld();

                gameManager.getHost().sendTitle(
                        ChatColor.RED + "\u00BB Chargement \u00AB",
                        ChatColor.GOLD + "Génération en cours..."
                );

                gameManager.getHost().resetTitle();
            }
        }, 80);
    }

    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.LEAVES, 1, "Configuration du monde").addFakeEnchant().build();
    }

}
