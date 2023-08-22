package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.PlayerStartsConfiguringGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigWholeGameCommand implements CommandExecutor {

    private Main main;

    public ConfigWholeGameCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configgame")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            Bukkit.getPluginManager().callEvent(new PlayerStartsConfiguringGameEvent(player));
            return true;
        }
        return false;
    }
}