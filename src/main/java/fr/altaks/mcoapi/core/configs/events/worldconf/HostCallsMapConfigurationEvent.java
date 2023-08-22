package fr.altaks.mcoapi.core.configs.events.worldconf;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HostCallsMapConfigurationEvent extends Event {

    private Player player;

    public HostCallsMapConfigurationEvent(Player player){
        this.player = player;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }
}
