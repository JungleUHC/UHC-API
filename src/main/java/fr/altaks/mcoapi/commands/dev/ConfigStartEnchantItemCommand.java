package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gameconf.ench.PlayerStartsConfigItemEnchantsEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigStartInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigStartInvEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigStartEnchantItemCommand implements CommandExecutor {

    private Main main;

    public ConfigStartEnchantItemCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configitemenchlim")){
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            Bukkit.getPluginManager().callEvent(new PlayerStartsConfigItemEnchantsEvent(player));
            return true;
        }
        return false;
    }
}