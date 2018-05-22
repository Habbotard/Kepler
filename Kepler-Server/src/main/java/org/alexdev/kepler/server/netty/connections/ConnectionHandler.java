package org.alexdev.kepler.server.netty.connections;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.log.Log;
import org.alexdev.kepler.messages.MessageHandler;
import org.alexdev.kepler.messages.outgoing.handshake.HELLO;
import org.alexdev.kepler.server.netty.NettyPlayerNetwork;
import org.alexdev.kepler.server.netty.NettyServer;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler extends SimpleChannelInboundHandler<NettyRequest> {

    final private static Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private NettyServer server;

    public ConnectionHandler(NettyServer server) {
        this.server = server;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Player player = new Player(new NettyPlayerNetwork(ctx.channel(), this.server.getConnectionIds().incrementAndGet()));
        ctx.channel().attr(Player.PLAYER_KEY).set(player);

        if (!server.getChannels().add(ctx.channel())) {
            ctx.disconnect();
            return;
        }

        player.send(new HELLO());

        if (Configuration.getInstance().getServerConfig().get("Logging", "log.connections", Boolean.class)) {
            log.info("[{}] Connection from {} ", player.getNetwork().getConnectionId(), ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0]);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.server.getConnectionIds().decrementAndGet(); // Decrement because we don't want it to reach Integer.MAX_VALUE
        this.server.getChannels().remove(ctx.channel());

        Player player = ctx.channel().attr(Player.PLAYER_KEY).get();

        if (Configuration.getInstance().getServerConfig().get("Logging", "log.connections", Boolean.class)) {
            log.info("[{}] Disonnection from {} ", player.getNetwork().getConnectionId(), ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0]);
        }

        player.dispose();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, NettyRequest message) throws Exception {
        try {
            Player player = ctx.channel().attr(Player.PLAYER_KEY).get();

            if (message == null) {
                return;
            }

            if (player != null){
                MessageHandler.getInstance().handleRequest(player, message);
            }

        } catch (Exception ex) {
            Log.getErrorLogger().error("Could not handle message: ", ex);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.getErrorLogger().error("Netty error occurred: ", cause);
    }
}