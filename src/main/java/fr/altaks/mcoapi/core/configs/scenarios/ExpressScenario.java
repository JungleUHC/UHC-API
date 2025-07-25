package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import fr.altaks.mcoapi.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ExpressScenario implements ScenariosConfiguration.Scenario {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.EXPRESS;
    }


    @Override
    public void setup(Main main) {
        new BukkitRunnable() {

            private ItemStack axe = new ItemManager.ItemBuilder(Material.IRON_AXE, 1).build(),
                    pickaxe = new ItemManager.ItemBuilder(Material.IRON_PICKAXE, 1).build(),
                    shovel = new ItemManager.ItemBuilder(Material.IRON_SPADE, 1).addNotSafeEnchant(Enchantment.DIG_SPEED, 3).build();

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getGameMode() == GameMode.SURVIVAL) {
                        player.getInventory().addItem(axe, pickaxe, shovel);
                    }
                }
            }

        }.runTaskLater(main, 5 * 20); // 5 sec after the starting.
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }


    @Override
    public void processUseConfig(InventoryClickEvent event) {

    }
}
