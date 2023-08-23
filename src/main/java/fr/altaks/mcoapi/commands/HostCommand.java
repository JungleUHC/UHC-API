package fr.altaks.mcoapi.commands;

import fr.altaks.mcoapi.Main;
import fr.altaks.mcoapi.core.configs.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HostCommand implements CommandExecutor {

    private Main main;

    public HostCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("host")) return false;
        if(args.length == 0) return false;
        if(!((sender instanceof Player && sender.equals(main.getGameManager().getHost())) || sender.hasPermission("host.changehost")))
            return false;

        if(args[0].equalsIgnoreCase("sethost") && args.length == 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null) {
                sender.sendMessage(Main.PREFIX + "Le joueur " + args[1] + " n'est pas connecté");
                return true;
            }
            main.getGameManager().changeHost(player);
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez défini " + player.getName() + " comme hôte de la partie");
            Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + player.getDisplayName() + " est désormais l'hôte de la partie");
            return true;
        } else if(args[0].equalsIgnoreCase("cohost") && args.length == 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null) {
                sender.sendMessage(Main.PREFIX + "Le joueur " + args[1] + " n'est pas connecté");
                return true;
            }
            main.getGameManager().addCohost(player);
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez ajouté " + player.getName() + " comme co-hôte de la partie");
            Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + player.getDisplayName() + " est désormais co-hôte de la partie");
            return true;
        } else if(args[0].equalsIgnoreCase("setspec") && args.length == 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null) {
                sender.sendMessage(Main.PREFIX + "Le joueur " + args[1] + " n'est pas connecté");
                return true;
            }
            player.setGameMode(GameMode.SPECTATOR);
            sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez défini " + player.getName() + " comme spectateur de la partie");
            Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + player.getDisplayName() + " est désormais spectateur de la partie");
            return true;
        } else if(args[0].equalsIgnoreCase("say") && args.length > 1) {
            StringJoiner message = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++){
                message.add(args[i]);
            }
            Bukkit.broadcastMessage(ChatColor.RED + "[HOST] \u00BB " + ChatColor.WHITE + message);
            return true;
        } else if(args[0].equalsIgnoreCase("kick") && args.length > 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null) {
                sender.sendMessage(Main.PREFIX + "Le joueur " + args[1] + " n'est pas connecté");
                return true;
            }
            StringJoiner reason = new StringJoiner(" ");
            for(int i = 2; i < args.length; i++){
                reason.add(args[i]);
            }
            player.kickPlayer(ChatColor.GOLD + "Jungle" + ChatColor.YELLOW + "UHC\n\nVous avez été exclu par " + sender.getName() + " pour " + ChatColor.RED + reason);
            return true;
        } else if(args[0].equalsIgnoreCase("heal") && args.length == 2) {
            if(args[1].equalsIgnoreCase("all")){
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getGameMode() != GameMode.SPECTATOR) {
                        player.setHealth(20);
                        player.sendMessage(Main.PREFIX + "| Vous avez été soigné ");
                    }
                }
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez soigné tous les joueurs");
            } else {
                Player player = Bukkit.getPlayer(args[1]);
                if(player == null) {
                    sender.sendMessage(Main.PREFIX + "Le joueur " + args[1] + " n'est pas connecté");
                    return true;
                }
                player.setHealth(20);
                player.sendMessage(Main.PREFIX + "| Vous avez été soigné ");
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez soigné " + player.getName());
            }
            return true;
        } else if(args[0].equalsIgnoreCase("force") && args.length == 2) {
            switch (args[1].toLowerCase()) {
                case "border":
                    Bukkit.getPluginManager().callEvent(new CallForceBorderEvent());
                    Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + "La bordure a été forcée par l'hôte");
                    return true;
                case "role":
                    Bukkit.getPluginManager().callEvent(new CallForceRolesEvent());
                    Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + "Les rôles ont été forcés par l'hôte");
                    return true;
                case "pvp":
                    Bukkit.getPluginManager().callEvent(new CallForcePvPEvent());
                    Bukkit.broadcastMessage(Main.PREFIX + ChatColor.RED + "Le PvP a été forcé par l'hôte");
                    return true;
                default:
                    return false;
            }
        } else if(args[0].equalsIgnoreCase("killoffline") && args.length >= 2) {
            List<OfflinePlayer> playersToKillOffline = new ArrayList<>();
            for(int i = 1; i < args.length; i++){
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[i]);
                if(player == null || player.isOnline()) {
                    sender.sendMessage(Main.PREFIX + "Le joueur " + args[i] + " n'existe pas/est connecté...");
                    return true;
                }
                playersToKillOffline.add(player);
            }
            if(playersToKillOffline.isEmpty()) {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Aucun joueur à tuer hors-ligne");
            } else {
                Bukkit.getPluginManager().callEvent(new CallKillOfflineEvent(playersToKillOffline));
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez tué " + playersToKillOffline.size() + " joueur(s) hors-ligne, à savoir :");
                for(OfflinePlayer player : playersToKillOffline){
                    sender.sendMessage(ChatColor.RED + " - " + player.getName());
                }
            }
            return true;
        } else if(args[0].equalsIgnoreCase("revive") && args.length >= 2) {
            List<OfflinePlayer> playersToRevive = new ArrayList<>();
            for(int i = 1; i < args.length; i++){
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[i]);
                if(player == null || !player.isOnline()) {
                    sender.sendMessage(Main.PREFIX + "Le joueur " + args[i] + " n'existe pas/n'est pas connecté...");
                    return true;
                }
                playersToRevive.add(player);
            }
            if(playersToRevive.isEmpty()) {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Aucun joueur à réssusciter");
            } else {
                Bukkit.getPluginManager().callEvent(new CallRevivePlayerEvent(playersToRevive));
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez réssuscité " + playersToRevive.size() + " joueur(s), à savoir :");
                for(OfflinePlayer player : playersToRevive){
                    sender.sendMessage(ChatColor.RED + " - " + player.getName());
                    player.getPlayer().sendMessage(Main.PREFIX + ChatColor.RED + "Vous avez été réanimé par l'hôte");
                }
            }
            return true;
        }

        return false;
    }
}
