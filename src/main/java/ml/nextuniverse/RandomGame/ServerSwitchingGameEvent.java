package ml.nextuniverse.RandomGame;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by TheDiamondPicks on 19/07/2017.
 */
public class ServerSwitchingGameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
