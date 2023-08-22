package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SafeMinersScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.SAFEMINERS;
    }

    private float damageReduction = 50.0f;

    @Override
    public void setup(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onPlayerTakesDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && event.getEntity().getLocation().getY() <= 32) {
            // if damage cause is fire, cancel damage, else reduce by 50%
            if(event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
            } else {
                event.setDamage(event.getDamage() * (damageReduction / 100.0f));
            }
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