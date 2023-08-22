package fr.altaks.mcoapi.core.configs.events.gameconf.ench;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStopsConfigItemEnchantsEvent  extends Event {

    private Player player;

    public PlayerStopsConfigItemEnchantsEvent(Player player){
        this.player = player;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}