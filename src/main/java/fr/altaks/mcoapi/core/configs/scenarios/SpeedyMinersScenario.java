package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpeedyMinersScenario implements ScenariosConfiguration.Scenario {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.SPEEDYMINERS;
    }


    @Override
    public void setup(Main main) {
        new BukkitRunnable() {

            private PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 25, 0);

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getGameMode() == GameMode.SURVIVAL && player.getLocation().getBlockY() <= 32) {
                        player.addPotionEffect(speed, false);
                    }
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