package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FastSmeltingScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.FASTSMELTING;
    }

    private int smeltingMultiplierPercentage = 130;
    private ArrayList<Furnace> furnaces = new ArrayList<>();

    @Override
    public void setup(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);

        final int period = 20;

        new BukkitRunnable() {

            @Override
            public void run() {
                ArrayList<Furnace> toRemove = new ArrayList<>();
                for(Furnace furnace : furnaces){
                    if(!furnace.isPlaced() || (furnace.getBlock().getType() != Material.FURNACE && furnace.getBlock().getType() != Material.BURNING_FURNACE)) {
                        toRemove.add(furnace);
                        continue;
                    }
                    if(furnace.getCookTime() != 0) {
                        furnace.setCookTime((short) (furnace.getCookTime() + (short) (((double) period) * ((double) smeltingMultiplierPercentage / 100.0d))));
                    }
                }
                furnaces.removeAll(toRemove);
            }

        }.runTaskTimer(main, 0, 5);
    }

    @EventHandler
    public void onPlayerPlacesFurnace(BlockPlaceEvent event) {
        if(event.getBlock().getType() == Material.FURNACE) {
            furnaces.add((Furnace) event.getBlock().getState());
            Main.LOG.debuglog("Furnace placed at " + event.getBlock().getLocation().toString() + " added to the list of furnaces.");
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }


    @Override
    public void processUseConfig(InventoryClickEvent event) {
        if(!event.isLeftClick() && !event.isRightClick()) return;

        int change = 1;

        if(event.isShiftClick()) {
            change = 10;
        }

        if(event.isRightClick()) {
            change *= -1;
        }

        smeltingMultiplierPercentage += change;
        if(smeltingMultiplierPercentage < 1) smeltingMultiplierPercentage = 1;

        // change lore of this item. update 5th line from the bottom
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        lore.set(lore.size() - 5, ChatColor.YELLOW + "Taux actuel : " + smeltingMultiplierPercentage + "%");
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        meta.setLore(lore);
        event.getCurrentItem().setItemMeta(meta);
    }

    @Override
    public List<String> getScenarioConfigLore() {
        return Arrays.asList(
                ChatColor.YELLOW + "Taux actuel : " + smeltingMultiplierPercentage + "%",
                ChatColor.GRAY + "Clic gauche : +1%",
                ChatColor.GRAY + "Clic droit : -1%",
                ChatColor.GRAY + "Shift + clic gauche : +10%",
                ChatColor.GRAY + "Shift + clic droit : -10%"
        );
    }
}