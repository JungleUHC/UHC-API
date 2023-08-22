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

public class GoldLimitScenario implements ScenariosConfiguration.Scenario, Listener {

    @Override
    public ScenariosConfiguration.ScenarioTypes scenario() {
        return ScenariosConfiguration.ScenarioTypes.GOLDLIMIT;
    }

    private int maxGolds = 128;
    private HashMap<UUID, Integer> collectedGold = new HashMap<>();

    @Override
    public void setup(Main main) {
        for(Player player : Bukkit.getOnlinePlayers()){
            collectedGold.put(player.getUniqueId(), 0);
        }
        // register events
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onPlayerCollectsItem(PlayerPickupItemEvent event) {
        // if the item is gold
        Material material = event.getItem().getItemStack().getType();
        if(material == Material.GOLD_INGOT || material == Material.GOLD_ORE || material == Material.GOLD_BLOCK) {

            // get the amount of diamonds picked up and the amount of gold already picked up
            int amount = event.getItem().getItemStack().getAmount();
            int collected = collectedGold.get(event.getPlayer().getUniqueId());

            if(collected >= maxGolds) {
                event.getPlayer().sendMessage(Main.PREFIX + "Vous avez atteint la limite d'or ramassés (" + maxGolds + ")");
                event.setCancelled(true);
                return;
            }

            // if there's an overflow, ajust the picked up amount and the remaining amount in the entity
            if(collected + amount > maxGolds) {
                event.setCancelled(true);

                // get the remaining amount and the picked up amount
                int remainingAmount = (collected + amount) - maxGolds;
                int pickedUpAmount = amount - remainingAmount;

                // set the remaining amount in the entity and add the picked up amount to the player's inventory
                event.getItem().getItemStack().setAmount(remainingAmount);
                event.getPlayer().getInventory().addItem(new ItemStack(material, pickedUpAmount));
                event.getItem().setPickupDelay(20);

                // update the collected diamonds hashmap
                collectedGold.put(event.getPlayer().getUniqueId(), maxGolds);
            } else {

                // update the collected diamonds hashmap
                collectedGold.put(event.getPlayer().getUniqueId(), collected + amount);
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

        maxGolds += change;
        if(maxGolds < 0) maxGolds = 0;

        // change lore of this item. update 5th line from the bottom
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        lore.set(lore.size() - 5, ChatColor.YELLOW + "Limite actuelle : " + maxGolds);
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        meta.setLore(lore);
        event.getCurrentItem().setItemMeta(meta);
    }

    @Override
    public List<String> getScenarioConfigLore() {
        return Arrays.asList(
                ChatColor.YELLOW + "Limite actuelle : " + maxGolds,
                ChatColor.GRAY + "Clic gauche : +1",
                ChatColor.GRAY + "Clic droit : -1",
                ChatColor.GRAY + "Shift + clic gauche : +10",
                ChatColor.GRAY + "Shift + clic droit : -10"
        );
    }
}