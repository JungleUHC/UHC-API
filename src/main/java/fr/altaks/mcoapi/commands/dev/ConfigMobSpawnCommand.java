package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.gameconf.mobs.PlayerStartsConfigMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigMobSpawnCommand implements CommandExecutor {

    private Main main;

    public ConfigMobSpawnCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configmobspawn")) {
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            Bukkit.getPluginManager().callEvent(new PlayerStartsConfigMobSpawnEvent(player));
            return true;
        }
        return false;
    }
}