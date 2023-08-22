package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.core.configs.events.GameStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("start")) {
            Bukkit.getPluginManager().callEvent(new GameStartEvent((Player) sender));
            return true;
        }
        return false;
    }
}
