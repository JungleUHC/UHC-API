package fr.altaks.mcoapi.core.configs.scenarios;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.ScenariosConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DiamondLimitScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.DIAMONDLIMIT;
    }

    private int maxDiamonds = 22;
    private HashMap<UUID, Integer> collectedDiamonds = new HashMap<>();

    @Override
    public void setup(Main main) {
        for(Player player : Bukkit.getOnlinePlayers()){
            collectedDiamonds.put(player.getUniqueId(), 0);
        }
        // register events
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onPlayerCollectsItem(PlayerPickupItemEvent event) {
        // if the item is a diamond
        if(event.getItem().getItemStack().getType() == Material.DIAMOND) {

            // get the amount of diamonds picked up and the amount of diamonds already picked up
            int amount = event.getItem().getItemStack().getAmount();
            int collected = collectedDiamonds.get(event.getPlayer().getUniqueId());

            if(collected >= maxDiamonds) {
                event.getPlayer().sendMessage(Main.PREFIX + "Vous avez atteint la limite de diamants ramassés (" + maxDiamonds + ")");
                event.setCancelled(true);
                return;
            }

            // if there's an overflow, ajust the picked up amount and the remaining amount in the entity
            if(collected + amount > maxDiamonds) {
                event.setCancelled(true);

                // get the remaining amount and the picked up amount
                int remainingAmount = (collected + amount) - maxDiamonds;
                int pickedUpAmount = amount - remainingAmount;

                // set the remaining amount in the entity and add the picked up amount to the player's inventory
                event.getItem().getItemStack().setAmount(remainingAmount);
                event.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND, pickedUpAmount));
                event.getItem().setPickupDelay(20);

                // update the collected diamonds hashmap
                collectedDiamonds.put(event.getPlayer().getUniqueId(), maxDiamonds);
            } else {

                // update the collected diamonds hashmap
                collectedDiamonds.put(event.getPlayer().getUniqueId(), collected + amount);
            }
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

        maxDiamonds += change;
        if(maxDiamonds < 0) maxDiamonds = 0;

        // change lore of this item. update 5th line from the bottom
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        lore.set(lore.size() - 5, ChatColor.YELLOW + "Limite actuelle : " + maxDiamonds);
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        meta.setLore(lore);
        event.getCurrentItem().setItemMeta(meta);
    }

    @Override
    public List<String> getScenarioConfigLore() {
        return Arrays.asList(
                ChatColor.YELLOW + "Limite actuelle : " + maxDiamonds,
                ChatColor.GRAY + "Clic gauche : +1",
                ChatColor.GRAY + "Clic droit : -1",
                ChatColor.GRAY + "Shift + clic gauche : +10",
                ChatColor.GRAY + "Shift + clic droit : -10"
        );
    }

}