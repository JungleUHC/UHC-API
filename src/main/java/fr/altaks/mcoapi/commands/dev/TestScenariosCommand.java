package fr.altaks.mcoapi.commands.dev;

import fr.altaks.mcoapi.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestScenariosCommand implements CommandExecutor {

    private Main main;

    public TestScenariosCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("testscenarios")) {
            if(!(sender instanceof Player)) return false;
            main.getGameManager().getScenariosConfiguration().startAllEnabledScenarios();
            return true;
        }
        return false;
    }
}
