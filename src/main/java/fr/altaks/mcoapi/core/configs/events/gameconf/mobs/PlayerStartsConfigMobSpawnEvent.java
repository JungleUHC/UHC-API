package fr.altaks.mcoapi.core.configs.events.gameconf.mobs;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStartsConfigMobSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;

    public PlayerStartsConfigMobSpawnEvent(Player player) {
        this.player = player;
    }

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
