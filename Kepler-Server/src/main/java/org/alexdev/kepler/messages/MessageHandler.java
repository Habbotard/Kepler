package org.alexdev.kepler.messages;

import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.messages.headers.Incoming;
import org.alexdev.kepler.messages.incoming.rooms.*;
import org.alexdev.kepler.messages.incoming.handshake.GENERATEKEY;
import org.alexdev.kepler.messages.incoming.handshake.INIT_CRYPTO;
import org.alexdev.kepler.messages.incoming.handshake.SSO;
import org.alexdev.kepler.messages.incoming.navigator.NAVIGATE;
import org.alexdev.kepler.messages.incoming.rooms.user.QUIT;
import org.alexdev.kepler.messages.incoming.rooms.user.WALK;
import org.alexdev.kepler.messages.incoming.user.GET_CREDITS;
import org.alexdev.kepler.messages.incoming.user.GET_INFO;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {

    private Player player;
    private ConcurrentHashMap<Integer, List<MessageEvent>> messages;

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(Player player) {
        this.player = player;
        this.messages = new ConcurrentHashMap<>();

        registerHandshakePackets();
        registerUserPackets();
        registerNavigatorPackets();
        registerRoomPackets();
        registerRoomUserPackets();

        //if (Configuration.getInstance().getServerConfig().getInteractor("Logging", "log.items.loaded", Boolean.class)) {
        //    log.info("Loaded {} message event handlers", messages.size());
        //}
    }

    /**
     * Register handshake packets.
     */
    private void registerHandshakePackets() {
        registerEvent(Incoming.INIT_CRYPTO, new INIT_CRYPTO());
        registerEvent(Incoming.GENERATEKEY, new GENERATEKEY());
        registerEvent(Incoming.SSO, new SSO());
    }

    /**
     * Unregister handshake packets.
     */
    public void unregisterHandshakePackets() {
        unregisterEvent(Incoming.INIT_CRYPTO);
        unregisterEvent(Incoming.GENERATEKEY);
        unregisterEvent(Incoming.SSO);
    }

    /**
     * Register general purpose user packets.
     */
    private void registerUserPackets() {
        registerEvent(Incoming.GET_INFO, new GET_INFO());
        registerEvent(Incoming.GET_CREDITS, new GET_CREDITS());
    }

    /**
     * Register navigator packets.
     */
    private void registerNavigatorPackets() {
        registerEvent(Incoming.NAVIGATE, new NAVIGATE());
    }

    /**
     * Register navigator packets.
     */
    private void registerRoomPackets() {
        registerEvent(Incoming.GETINTEREST, new GETINTEREST());
        registerEvent(Incoming.ROOM_DIRECTORY, new ROOM_DIRECTORY());
        registerEvent(Incoming.GETROOMAD, new GETROOMAD());
        registerEvent(Incoming.G_HMAP, new G_HMAP());
        registerEvent(Incoming.G_OBJS, new G_OBJS());
        registerEvent(Incoming.G_USRS,  new G_USRS());
        registerEvent(Incoming.G_STAT, new G_STAT());
    }

    /**
     * Register room user packets.
     */
    private void registerRoomUserPackets() {
        registerEvent(Incoming.QUIT, new QUIT());
        registerEvent(Incoming.WALK, new WALK());
    }

    /**
     * Register event.
     *
     * @param header the header
     * @param messageEvent the message event
     */
    private void registerEvent(int header, MessageEvent messageEvent) {
        if (!this.messages.containsKey(header)) {
            this.messages.put(header, new ArrayList<>());
        }

        this.messages.get(header).add(messageEvent);
    }

    /**
     * Unegister event.
     *
     * @param header the header
     */
    private void unregisterEvent(int header) {
        List<MessageEvent> events = this.messages.get(header);

        if (events != null) {
            this.messages.remove(header);
        }
    }

    /**
     * Handle request.
     *
     * @param message the message
     */
    public void handleRequest(NettyRequest message) {
        if (Configuration.getInstance().getServerConfig().get("Logging", "log.received.packets", Boolean.class)) {
            if (this.messages.containsKey(message.getHeaderId())) {
                MessageEvent event = this.messages.get(message.getHeaderId()).get(0);
                this.player.getLogger().info("Received ({}): {} / {} ", event.getClass().getSimpleName(), message.getHeaderId(), message.getMessageBody());
            } else {
                this.player.getLogger().info("Received ({}): {} / {} ", "Unknown", message.getHeaderId(), message.getMessageBody());
            }
        }

        invoke(message.getHeaderId(), message);
    }

    /**
     * Invoke the request.
     *
     * @param messageId the message id
     * @param message the message
     */
    private void invoke(int messageId, NettyRequest message) {
        if (this.messages.containsKey(messageId)) {
            for (MessageEvent event : this.messages.get(messageId)) {
                event.handle(this.player, message);
            }
        }

        message.dispose();
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public ConcurrentHashMap<Integer, List<MessageEvent>> getMessages() {
        return messages;
    }
}