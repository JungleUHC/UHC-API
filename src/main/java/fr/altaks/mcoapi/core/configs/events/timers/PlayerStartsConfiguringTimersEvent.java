package fr.altaks.mcoapi.core.configs.events.timers;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStartsConfiguringTimersEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;

    public PlayerStartsConfiguringTimersEvent(Player player) {
        this.player = player;
    }

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