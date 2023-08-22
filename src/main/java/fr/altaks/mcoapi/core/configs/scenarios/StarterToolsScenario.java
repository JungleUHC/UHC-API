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

public class StarterToolsScenario implements ScenariosConfiguration.Scenario {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.STARTERTOOLS;
    }


    @Override
    public void setup(Main main) {
        new BukkitRunnable() {

            private ItemStack
                    axe = new ItemManager.ItemBuilder(Material.IRON_AXE, 1).addSafeEnchant(Enchantment.DIG_SPEED, 2).build(),
                    pickaxe = new ItemManager.ItemBuilder(Material.IRON_PICKAXE, 1).addSafeEnchant(Enchantment.DIG_SPEED, 2).build(),
                    shovel = new ItemManager.ItemBuilder(Material.IRON_SPADE, 1).addSafeEnchant(Enchantment.DIG_SPEED, 2).build(),
                    water_bucket = new ItemManager.ItemBuilder(Material.WATER_BUCKET, 1).build(),
                    feathers = new ItemManager.ItemBuilder(Material.FEATHER, 16).build();

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getGameMode() == GameMode.SURVIVAL) {
                        player.getInventory().addItem(axe, pickaxe, shovel, water_bucket, feathers);
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