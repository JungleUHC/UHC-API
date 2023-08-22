package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CutCleanScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.CUTCLEAN;
    }

    @Override
    public void setup(Main main) {
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    private static HashMap<Material, Material> smeltableMaterial = new HashMap<>();

    static {
        smeltableMaterial.put(Material.IRON_ORE, Material.IRON_INGOT);
        smeltableMaterial.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        smeltableMaterial.put(Material.SAND, Material.GLASS);
        smeltableMaterial.put(Material.CLAY_BALL, Material.CLAY_BRICK);
        smeltableMaterial.put(Material.COBBLESTONE, Material.STONE);
        smeltableMaterial.put(Material.NETHERRACK, Material.NETHER_BRICK);
        smeltableMaterial.put(Material.QUARTZ_ORE, Material.QUARTZ);
        smeltableMaterial.put(Material.POTATO, Material.BAKED_POTATO);
        smeltableMaterial.put(Material.MUTTON, Material.COOKED_MUTTON);
        smeltableMaterial.put(Material.RAW_BEEF, Material.COOKED_BEEF);
        smeltableMaterial.put(Material.RAW_CHICKEN, Material.COOKED_CHICKEN);
        smeltableMaterial.put(Material.RAW_FISH, Material.COOKED_FISH);
        smeltableMaterial.put(Material.PORK, Material.GRILLED_PORK);
        smeltableMaterial.put(Material.RABBIT, Material.COOKED_RABBIT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerBreaksSmeltableBlock(ItemSpawnEvent event) {
        if(smeltableMaterial.containsKey(event.getEntity().getItemStack().getType())) {
            ItemStack itemStack = event.getEntity().getItemStack();
            itemStack.setType(smeltableMaterial.get(itemStack.getType()));
            event.getEntity().setItemStack(itemStack);
        }
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }


    @Override
    public void processUseConfig(InventoryClickEvent event) {

    }
}
