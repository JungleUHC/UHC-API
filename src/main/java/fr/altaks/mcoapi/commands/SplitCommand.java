package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class SplitCommand implements CommandExecutor {

    /*
     * /split - Permet de split les enchantements d’un livre.
     * (par exemple, je trouve un livre dans un coffre Efficiency IV, Sharpness III, Unbreaking III, ça va split les enchantements diviser celui-ci en 3 Livres distinct).
     * */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("split")) return false;
        if(sender instanceof Player) {
            // check if player has item in hand
            Player player = (Player) sender;
            if(player.getInventory().getItemInHand() != null && player.getInventory().getItemInHand().getType() == Material.ENCHANTED_BOOK) {
                // check if item is enchanted
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) player.getInventory().getItemInHand().getItemMeta();
                if(meta.getStoredEnchants().size() > 0) {
                    // split the enchantments
                    player.setItemInHand(null);
                    for(Map.Entry<Enchantment, Integer> enchants : meta.getStoredEnchants().entrySet()){
                        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
                        bookMeta.addStoredEnchant(enchants.getKey(), enchants.getValue(), false);
                        book.setItemMeta(bookMeta);
                        player.getInventory().addItem(book);
                    }
                    return true;
                } else {
                    player.sendMessage(Main.PREFIX + "Vous devez avoir un livre enchanté en main");
                }
            } else {
                player.sendMessage(Main.PREFIX + "Vous devez avoir un livre enchanté en main");
            }
        }
        return false;
    }
}
