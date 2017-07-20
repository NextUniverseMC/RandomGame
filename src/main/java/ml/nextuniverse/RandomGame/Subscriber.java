package ml.nextuniverse.RandomGame;

import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Logger;

/**
 * Created by TheDiamondPicks on 20/07/2017.
 */
public class Subscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("RandomGame")) {
            if (message.equals("ShutdownConfirm"))
                Main.dispatchShutdown();
        }
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }
}
