package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CatEyesScenario implements ScenariosConfiguration.Scenario {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.CATEYES;
    }

    public static final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 1_000_000, 0, false, false);

    @Override
    public void setup(Main main) {
        new BukkitRunnable() {

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    player.addPotionEffect(NIGHT_VISION, false);
                }
            }

        }.runTaskTimer(main, 0, 20);
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }


    @Override
    public void processUseConfig(InventoryClickEvent event) {

    }
}