package fr.altaks.mcoapi.core.configs.events.gamemode;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameModeLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;

    public GameModeLoadEvent(Player player) {
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