package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MumbleCommand implements CommandExecutor {

    private String storedMumbleLink = "";

    private Main main;

    public MumbleCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("mumble")) return false;
        if(sender instanceof Player && args.length == 0) {
            if(storedMumbleLink.isEmpty()) {
                sender.sendMessage("§cAucun lien mumble n'a été défini");
            } else {
                TextComponent message = new TextComponent(ChatColor.GRAY + "\u21B3" + ChatColor.BLUE + " Cliquez ici pour vous connecter au mumble " + ChatColor.GRAY + "\u21B2");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, storedMumbleLink));
                ((Player) sender).spigot().sendMessage(message);
            }
            return true;
        } else if(args[0].equalsIgnoreCase("add") && args.length == 2 && (sender.isOp() || sender.equals(main.getGameManager().getHost()))) {
            storedMumbleLink = args[1];
            sender.sendMessage("§aLe lien mumble a été défini");
            return true;
        }
        return false;
    }
}
