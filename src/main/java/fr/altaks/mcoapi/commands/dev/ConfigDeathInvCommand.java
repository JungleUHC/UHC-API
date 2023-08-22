package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigDeathInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigDeathInvEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigDeathInvCommand implements CommandExecutor {

    private Main main;

    public ConfigDeathInvCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configdeathinv")){
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("start")){
                    Bukkit.getPluginManager().callEvent(new PlayerStartConfigDeathInvEvent(player));
                } else if(args[0].equalsIgnoreCase("end")){
                    Bukkit.getPluginManager().callEvent(new PlayerEndsConfigDeathInvEvent(player));
                }
            }
            return true;
        }
        return false;
    }
}