package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeLoadEvent;
import fr.altaks.mcoapi.core.configs.events.gamemode.GameModeStartConfigEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigGameModeCommand implements CommandExecutor {

    private Main main;

    public ConfigGameModeCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configgamemode")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if(args.length > 0 && args[0].equalsIgnoreCase("execute")) {
                Bukkit.getPluginManager().callEvent(new GameModeLoadEvent(player));
                return true;
            } else Bukkit.getPluginManager().callEvent(new GameModeStartConfigEvent(player));

            return true;
        }
        return false;
    }
}