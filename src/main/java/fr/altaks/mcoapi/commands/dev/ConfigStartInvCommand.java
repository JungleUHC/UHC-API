package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerEndsConfigStartInvEvent;
import fr.altaks.mcoapi.core.configs.events.gameconf.invs.PlayerStartConfigStartInvEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigStartInvCommand implements CommandExecutor {

    private Main main;

    public ConfigStartInvCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configstartinv")){
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("start")){
                    Bukkit.getPluginManager().callEvent(new PlayerStartConfigStartInvEvent(player));
                } else if(args[0].equalsIgnoreCase("end")){
                    Bukkit.getPluginManager().callEvent(new PlayerEndsConfigStartInvEvent(player));
                } else if(args[0].equalsIgnoreCase("load")){
                    main.getGameManager().getGameConfiguration().getStartInv().injectToPlayer(player);
                }
            }
            return true;
        }
        return false;
    }
}
