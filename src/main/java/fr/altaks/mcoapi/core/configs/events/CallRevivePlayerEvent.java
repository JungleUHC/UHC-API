package fr.altaks.mcoapi.core.configs.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Arrays;
import java.util.List;

public class CallRevivePlayerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private List<OfflinePlayer> toRevive;

    public CallRevivePlayerEvent(OfflinePlayer... toRevive) {
        this.toRevive = Arrays.asList(toRevive);
    }

    public CallRevivePlayerEvent(List<OfflinePlayer> toRevive) {
        this.toRevive = toRevive;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public List<OfflinePlayer> getToRevive() {
        return toRevive;
    }
}