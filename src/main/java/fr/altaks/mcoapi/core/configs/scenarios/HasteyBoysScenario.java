package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class HasteyBoysScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.HASTEYBOYS;
    }

    private int efficiencyLevel = 3, unbreakingLevel = 2;

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
            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, efficiencyLevel);
            item.addUnsafeEnchantment(Enchantment.DURABILITY, unbreakingLevel);
            event.getInventory().setResult(item);
            event.setCurrentItem(item);
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }


    @Override
    public void processUseConfig(InventoryClickEvent event) {
        if(!event.isLeftClick() && !event.isRightClick()) return;

        if(event.isLeftClick()) {
            if(event.isShiftClick()) {
                efficiencyLevel -= 1;
            } else efficiencyLevel += 1;
            if(efficiencyLevel < 1) {
                efficiencyLevel = 1;
            } else if(efficiencyLevel > Enchantment.DIG_SPEED.getMaxLevel()) {
                efficiencyLevel = Enchantment.DIG_SPEED.getMaxLevel();
            }
        } else {
            if(event.isShiftClick()) {
                unbreakingLevel -= 1;
            } else unbreakingLevel += 1;
            if(unbreakingLevel < 1) {
                unbreakingLevel = 1;
            } else if(unbreakingLevel > Enchantment.DURABILITY.getMaxLevel()) {
                unbreakingLevel = Enchantment.DURABILITY.getMaxLevel();
            }
        }

        // change lore of this item. update 5th line from the bottom
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        lore.set(lore.size() - 9, ChatColor.YELLOW + "Niv. Efficiency : " + efficiencyLevel);
        lore.set(lore.size() - 3, ChatColor.YELLOW + "Niv. Unbreaking : " + unbreakingLevel);
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        meta.setLore(lore);
        event.getCurrentItem().setItemMeta(meta);
    }

    @Override
    public List<String> getScenarioConfigLore() {
        return Arrays.asList(
                "",
                ChatColor.YELLOW + "Niv. Efficiency : " + efficiencyLevel,
                ChatColor.GRAY + "[Efficiency] Clic gauche : +1",
                ChatColor.GRAY + "[Efficiency] Shift + clic gauche : -1",
                "",
                ChatColor.GRAY + "\u00BB" + ChatColor.STRIKETHROUGH + "------------------------------------------------" + ChatColor.RESET + "" + ChatColor.GRAY + "\u00AB",
                "",
                ChatColor.YELLOW + "Niv. Unbreaking : " + unbreakingLevel,
                ChatColor.GRAY + "[Unbreaking] Clic droit : +1",
                ChatColor.GRAY + "[Unbreaking] Shift + clic droit : -1"
        );
    }
}