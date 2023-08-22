package fr.altaks.mcoapi.core.configs.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class CallKillOfflineEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private OfflinePlayer[] tokill;

    public CallKillOfflineEvent(OfflinePlayer... tokill) {
        this.tokill = tokill;
    }

    public CallKillOfflineEvent(List<OfflinePlayer> tokill) {
        this.tokill = tokill.toArray(new OfflinePlayer[tokill.size()]);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public OfflinePlayer[] getTokill() {
        return tokill;
    }
}