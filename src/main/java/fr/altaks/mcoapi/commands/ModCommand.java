package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class ModCommand implements CommandExecutor {

    private Main main;

    public ModCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("mod")) {
            if(sender instanceof Player) {
                if(main.getGameManager().getHost().equals((Player) sender) || sender.hasPermission("mcoapi.mod")) {
                    StringJoiner joiner = new StringJoiner(" ");
                    for(String arg : args){
                        joiner.add(arg);
                    }
                    Bukkit.broadcastMessage("§c[Host] §r" + sender.getName() + " §7» §c" + joiner.toString());
                    return true;
                }
            }
        }
        return false;
    }
}
