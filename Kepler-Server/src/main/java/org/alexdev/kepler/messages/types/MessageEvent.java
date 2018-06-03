package org.alexdev.kepler.messages.types;

import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

import java.sql.SQLException;

public interface MessageEvent {
    
    /**
     * Handle the incoming client message.
     *
     * @param player the player
     * @param reader the reader
     */
    public void handle(Player player, NettyRequest reader) throws Exception;
}
