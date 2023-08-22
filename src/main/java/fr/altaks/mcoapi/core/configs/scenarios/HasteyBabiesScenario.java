package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class HasteyBabiesScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.HASTEYBABIES;
    }


    @Override
    public void setup(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    private String[] toolstags = {
            "axe", "pickaxe", "spade", "hoe", "flint", "fishing_rod", "shears"
    };

    @EventHandler
    public void onPlayerCraftsTools(CraftItemEvent event) {
        Material resultType = event.getRecipe().getResult().getType();
        String name = resultType.name();
        if(Arrays.stream(toolstags).parallel().anyMatch(name.toLowerCase()::contains)) {

            // enchant the item efficiency 10 and unbreaking 10
            ItemStack item = event.getRecipe().getResult();
            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            event.getInventory().setResult(item);
            event.setCurrentItem(item);
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