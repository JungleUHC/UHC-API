package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gameconf.craft.PlayerCallsUncraftableListEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.craft.PlayerStartsConfigCraftableItems;
import fr.altaks.mcoapi.core.configs.events.gameconf.craft.PlayerStopsConfigCraftableItems;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigDeathInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigDeathInvEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigCraftableItemsCommand implements CommandExecutor {

    private Main main;

    public ConfigCraftableItemsCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configcraftitems")){
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("start")){
                    Bukkit.getPluginManager().callEvent(new PlayerStartsConfigCraftableItems(player));
                } else if(args[0].equalsIgnoreCase("end")){
                    Bukkit.getPluginManager().callEvent(new PlayerStopsConfigCraftableItems(player));
                } else if(args[0].equalsIgnoreCase("show")){
                    Bukkit.getPluginManager().callEvent(new PlayerCallsUncraftableListEvent(player));
                }
            }
            return true;
        }
        return false;
    }
}
