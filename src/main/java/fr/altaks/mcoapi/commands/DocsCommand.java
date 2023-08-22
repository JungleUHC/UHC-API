package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.util.ItemManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class DocsCommand implements CommandExecutor, Listener {

    private Main main;

    // Add the entry "MCO UHC", "https://docs.nocry.dev/mco-uhc/presentation" to the hashmap

    public static final HashMap<String, String> availableDocs = new HashMap<String, String>() {{
        put("MCO UHC", "https://docs.nocry.dev/mco-uhc/presentation");
    }};

    private Inventory docsInventory = null;

    public DocsCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("docs")){
            if(sender instanceof Player){
                // open an inventory with books named "MCO UHC", "Bientôt" and so on
                if(docsInventory == null){
                    docsInventory = main.getServer().createInventory(null, 9, "Docs");
                    // add books to the inventory
                    for(Map.Entry<String, String> entry : availableDocs.entrySet()){
                        docsInventory.addItem(
                                new ItemManager.ItemBuilder(Material.BOOK, 1 ,ChatColor.RESET + "" + ChatColor.GOLD + entry.getKey())
                                        .addFakeEnchant()
                                        .setLore(ChatColor.RESET + "" +ChatColor.YELLOW + " | Cliquez ici pour ouvrir la documentation")
                                        .build()
                        );

                        // fill the inventory with "Bientôt" books
                        for(int i = 0; i < 2 && docsInventory.firstEmpty() != -1; i++) docsInventory.setItem(docsInventory.firstEmpty(),new ItemManager.ItemBuilder(Material.BOOK, 1, "Bientôt").build());
                    }
                }
                ((Player) sender).openInventory(docsInventory);
                return true;
            } else {
                // send a basic link as text to the sender
                sender.sendMessage("\n" +
                        "Documentations disponibles : \n\n" +
                        " | MCO UHC : https://docs.nocry.dev/mco-uhc/presentation\n" +
                        " | Bientôt : \n" +
                        " | Bientôt : \n\n");
                return true;
            }
        }
        return false;
    }

    // make a listener to cancel all interactions with books named "Bientôt" and for MCO UHC make it send a
    // text component with a link to the docs as Action.OPEN_URL
    @EventHandler
    public void onPlayerInteractsWithDocsInventory(InventoryClickEvent event){
        if(event.getInventory().equals(docsInventory) && event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())){
            if(event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BOOK){
                event.setCancelled(true);
                for(Map.Entry<String, String> entry : availableDocs.entrySet()){
                    if(event.getCurrentItem().getItemMeta().getDisplayName().contains(entry.getKey())){
                        TextComponent textComponent = new TextComponent(ChatColor.RESET + entry.getKey() + ChatColor.YELLOW + " | Cliquez ici pour ouvrir la documentation");
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, entry.getValue()));
                        ((Player) event.getWhoClicked()).spigot().sendMessage(textComponent);
                        event.getWhoClicked().closeInventory();
                        break;
                    }
                }
            }
        }
    }
}
