package ml.nextuniverse.RandomGame;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by TheDiamondPicks on 14/10/2017.
 */
public class CountdownStartedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
