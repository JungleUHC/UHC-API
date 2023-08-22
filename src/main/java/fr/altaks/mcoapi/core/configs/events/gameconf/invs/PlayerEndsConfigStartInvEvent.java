package fr.altaks.mcoapi.core.configs.events.gameconf.invs;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEndsConfigStartInvEvent extends Event {

    public Player getPlayer() {
        return player;
    }

    private Player player;

    public PlayerEndsConfigStartInvEvent(Player player){
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

}
