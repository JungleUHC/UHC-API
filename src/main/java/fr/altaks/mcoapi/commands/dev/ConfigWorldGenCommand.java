package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.worldconf.HostCallsMapConfigurationEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfigWorldGenCommand implements CommandExecutor {

    private Main main;

    public ConfigWorldGenCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("configworldgen")){
            if(!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("start")) {
                    Bukkit.getPluginManager().callEvent(new HostCallsMapConfigurationEvent(player));
                } else if(args[0].equalsIgnoreCase("execute") && args.length == 2) {

                    Location backupLocation = new Location(Bukkit.getWorld("temp"), 0, 100, 0);

                    this.main.getGameManager().getWorldConfiguration().regenerate(args[1], backupLocation);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(new Location(Bukkit.getWorld(args[1]), 0, 100, 0));
                        }
                    }, 120);

                } else if(args[0].equalsIgnoreCase("goto") && args.length == 2) {

                    player.teleport(new Location(Bukkit.getWorld(args[1]), 0, 100, 0));

                } else if(args[0].equalsIgnoreCase("whereami")) {
                    sender.sendMessage(player.getLocation().getWorld().getName());
                }
            }
            return true;
        }
        return false;
    }
}