package fr.altaks.mcoapi.core.configs;

import com.google.common.collect.HashBiMap;
import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.GameManager;
import fr.altaks.mcoapi.core.configs.events.scenarios.PlayerStartsConfiguringScenariosEvent;
import fr.altaks.mcoapi.core.configs.events.scenarios.PlayerStopsConfiguringScenariosEvent;
import fr.altaks.mcoapi.core.configs.scenarios.*;
import fr.altaks.mcoapi.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScenariosConfiguration implements Listener {

    private GameManager gameManager;

    private HashMap<ScenarioTypes, Scenario> scenarioTypesToScenariosInstances = new HashMap<>();
    private HashBiMap<Integer, ScenarioTypes> inventoryPosition = HashBiMap.create();
    private HashMap<ScenarioTypes, Boolean> activationStatus = new HashMap<>();

    private Inventory overallScenariosConfigInventory = null;

    public ScenariosConfiguration(GameManager gameManager) {
        this.gameManager = gameManager;

        scenarioTypesToScenariosInstances.put(ScenarioTypes.CATEYES, new CatEyesScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.CUTCLEAN, new CutCleanScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.DIAMONDLIMIT, new DiamondLimitScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.EXPRESS, new ExpressScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.FASTSMELTING, new FastSmeltingScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.GIGADRILL, new GigaDrillScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.GOLDLIMIT, new GoldLimitScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.HASTEYBOYS, new HasteyBoysScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.HASTEYBABIES, new HasteyBabiesScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.SAFEMINERS, new SafeMinersScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.SPEEDYMINERS, new SpeedyMinersScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.STARTERTOOLS, new StarterToolsScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.TRIPLEORES, new TripleOresScenario());
        scenarioTypesToScenariosInstances.put(ScenarioTypes.DOUBLEORES, new DoubleOresScenario());

        // use of slots : 1, 3, 4, 5, 7, 11, 12, 14, 15, 19, 21, 22, 23, 25
        inventoryPosition.put(1, ScenarioTypes.CATEYES);
        inventoryPosition.put(3, ScenarioTypes.CUTCLEAN);
        inventoryPosition.put(4, ScenarioTypes.DIAMONDLIMIT);
        inventoryPosition.put(5, ScenarioTypes.EXPRESS);
        inventoryPosition.put(7, ScenarioTypes.FASTSMELTING);
        inventoryPosition.put(11, ScenarioTypes.GIGADRILL);
        inventoryPosition.put(12, ScenarioTypes.GOLDLIMIT);
        inventoryPosition.put(14, ScenarioTypes.HASTEYBOYS);
        inventoryPosition.put(15, ScenarioTypes.HASTEYBABIES);
        inventoryPosition.put(19, ScenarioTypes.SAFEMINERS);
        inventoryPosition.put(21, ScenarioTypes.SPEEDYMINERS);
        inventoryPosition.put(22, ScenarioTypes.STARTERTOOLS);
        inventoryPosition.put(23, ScenarioTypes.TRIPLEORES);
        inventoryPosition.put(25, ScenarioTypes.DOUBLEORES);

        for(ScenarioTypes scenarioTypes : ScenarioTypes.values()){
            activationStatus.put(scenarioTypes, false);
        }

    }

    public ItemStack getItemStack() {
        return new ItemManager.ItemBuilder(Material.SNOW_BALL, 1, "Configuration des scénarios").addFakeEnchant().build();
    }

    @EventHandler
    public void onPlayerStartsConfigScenariosEvent(PlayerStartsConfiguringScenariosEvent event) {
        // open a config inv with all the scenarios.
        if(overallScenariosConfigInventory == null) {
            overallScenariosConfigInventory = Bukkit.createInventory(null, 3 * 9, "Clic molette pour un scénario");
            for(ScenarioTypes scenarioTypes : scenarioTypesToScenariosInstances.keySet()){
                ItemManager.ItemBuilder itemBuilder = new ItemManager.ItemBuilder(
                        scenarioTypesToScenariosInstances
                                .get(scenarioTypes)
                                .getIcon()
                                .addItemFlags(
                                        ItemFlag.HIDE_ENCHANTS,
                                        ItemFlag.HIDE_POTION_EFFECTS
                                )
                                .build()
                );

                // on ajoute le nom et la description du scénario
                ArrayList<String> lore = new ArrayList<>();
                for(String s : scenarioTypes.getLore()) lore.add(s);

                if(scenarioTypesToScenariosInstances.get(scenarioTypes).isConfigurable()) {
                    lore.add(0, ChatColor.YELLOW + "Cliquez pour configurer ce scénario.");
                    lore.add(ChatColor.GRAY + "\u00BB" + ChatColor.STRIKETHROUGH + "------------------------------------------------" + ChatColor.RESET + "" + ChatColor.GRAY + "\u00AB");
                    for(String s : scenarioTypesToScenariosInstances.get(scenarioTypes).getScenarioConfigLore())
                        lore.add(s);
                } else {
                    lore.add(0, ChatColor.GRAY + "Ce scénario n'est pas configurable.");
                }

                itemBuilder.setLore(lore.toArray(new String[lore.size()]));
                itemBuilder.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + scenarioTypes.getName() + " " + ChatColor.RED + "Désactivé");

                overallScenariosConfigInventory.setItem(inventoryPosition.inverse().get(scenarioTypes), itemBuilder.build());
            }
        }
        event.getPlayer().openInventory(overallScenariosConfigInventory);
    }

    @EventHandler
    public void onPlayerInteractsWithScenariosConfigInvEvent(InventoryClickEvent event) {
        if(event.getInventory() == null
                || event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())
                || !event.getInventory().equals(overallScenariosConfigInventory)) return;

        // We are in the scenarios config inventory
        event.setCancelled(true);
        if(!inventoryPosition.containsKey(event.getSlot())) return;
        if(event.getClick().equals(ClickType.MIDDLE)) {
            ScenarioTypes scenarioTypes = inventoryPosition.get(event.getSlot());

            // alternate activation status
            activationStatus.put(scenarioTypes, !activationStatus.get(scenarioTypes));
            // change remove the enchantment if the scenario is not activated
            if(!activationStatus.get(scenarioTypes)) {
                event.getCurrentItem().removeEnchantment(Enchantment.DURABILITY);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + scenarioTypes.getName() + " " + ChatColor.RED + "Désactivé");
                event.getCurrentItem().setItemMeta(meta);
            } else {
                event.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + scenarioTypes.getName() + " " + ChatColor.GREEN + "Activé");
                event.getCurrentItem().setItemMeta(meta);
            }

            Main.LOG.debuglog("Player " + event.getWhoClicked().getName() + " toggled scenario " + scenarioTypes.getName() + " to " + activationStatus.get(scenarioTypes));
        } else {
            Scenario scenario = scenarioTypesToScenariosInstances.get(inventoryPosition.get(event.getSlot()));
            if(scenario.isConfigurable()) {
                event.setCancelled(true);
                scenario.processUseConfig(event);
                Main.LOG.devlog("Player " + event.getWhoClicked().getName() + " used config for scenario " + scenario.scenario().getName());
            } else {
                event.getWhoClicked().sendMessage(Main.PREFIX + ChatColor.RED + "Ce scénario n'est pas configurable.");
            }
        }


    }

    @EventHandler
    public void onPlayerStopsConfigScenariosEvent(PlayerStopsConfiguringScenariosEvent event) {
        // make the previous menu come back
    }

    public enum ScenarioTypes {
        CUTCLEAN("Cut Clean", ChatColor.WHITE + "Les minerais/nourritures sont directement cuits."),
        FASTSMELTING("Fast Smelting", ChatColor.WHITE + "Les fours cuisent plus vite les ressources."),
        HASTEYBOYS("Hastey Boys", ChatColor.WHITE + "Les outils craftés sont enchant Efficiency 3 Unbreaking 2."),
        DOUBLEORES("DoubleOres", ChatColor.WHITE + "Tous les minerais sont doublés."),
        TRIPLEORES("TriplesOres", ChatColor.WHITE + "Tous les minerais sont triplés."),
        EXPRESS("Express", ChatColor.WHITE + "Items de départ pour vite commencer."),
        DIAMONDLIMIT("Diamond Limit", ChatColor.WHITE + "Limite le nombre de diamants minables."),
        GOLDLIMIT("Gold Limit", ChatColor.WHITE + "Limite le nombre d'or minables."),
        CATEYES("Cat Eyes", ChatColor.WHITE + "Night Vision permanente."),
        SPEEDYMINERS("Speedy Miners", ChatColor.WHITE + "Vous gagnez speed 1 sous la couche 32"),
        STARTERTOOLS("Starter Tools", ChatColor.WHITE + "Vous commencez avec tout les outils efficacité 2 en fer"),
        GIGADRILL("Giga Drill", ChatColor.WHITE + "Les outils craftés sont enchant Efficiency 10 Unbreaking 10."),
        HASTEYBABIES("Hastey Babies", ChatColor.WHITE + "Les outils craftés sont enchant Efficiency 1."),
        SAFEMINERS("SafeMiners", ChatColor.WHITE + "Vous êtes protégé des dégats de feu + 50% de résistance sous la couche 32");

        public String name;
        private String[] lore;

        ScenarioTypes(String name, String... lore) {
            this.name = name;
            this.lore = lore;
        }

        public String getName() {
            return name;
        }

        public String[] getLore() {
            return lore;
        }
    }

    public interface Scenario {

        ItemManager.ItemBuilder DEFAULT_ICON_BUILDER = new ItemManager.ItemBuilder(Material.FIREWORK_CHARGE, 1).setLore("");

        ScenarioTypes scenario();

        void setup(Main main);

        boolean isConfigurable();

        void processUseConfig(InventoryClickEvent event);

        default ItemManager.ItemBuilder getIcon() {
            return DEFAULT_ICON_BUILDER;
        }

        default List<String> getScenarioConfigLore() {
            return new ArrayList<>();
        }
    }

    public void startAllEnabledScenarios() {
        for(ScenarioTypes scenarioTypes : activationStatus.keySet()){
            if(activationStatus.get(scenarioTypes)) {
                Main.LOG.debuglog("Starting scenario " + scenarioTypes.getName() + "...");
                scenarioTypesToScenariosInstances.get(scenarioTypes).setup(gameManager.getMain());
            }
        }
    }
}
