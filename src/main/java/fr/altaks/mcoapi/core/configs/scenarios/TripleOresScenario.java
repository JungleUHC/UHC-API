package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripleOresScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.TRIPLEORES;
    }


    @Override
    public void setup(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    private ArrayList<Block> blockPlacedByPlayers = new ArrayList<>();
    private List<Material> ores = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.QUARTZ_ORE
    );

    @EventHandler
    public void onPlayerPlacesBlockEvent(BlockPlaceEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.SURVIVAL) blockPlacedByPlayers.add(event.getBlockPlaced());
    }

    @EventHandler
    public void onPlayerBreaksOreEvent(BlockBreakEvent event) {
        if(blockPlacedByPlayers.contains(event.getBlock())) {
            blockPlacedByPlayers.remove(event.getBlock());
            return;
        }
        // double the amount of the block if it's an ore
        for(ItemStack item : event.getBlock().getDrops(event.getPlayer().getItemInHand())){
            if(ores.contains(event.getBlock().getType())) {
                // drop the item three times
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
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