package org.alexdev.kepler.server.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.alexdev.kepler.log.Log;
import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;
import org.alexdev.kepler.util.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetworkEncoder extends MessageToMessageEncoder<MessageComposer> {
    final private static Logger log = LoggerFactory.getLogger(NetworkEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageComposer msg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer(); // TODO: initial capacity calculation

        NettyResponse response = new NettyResponse(msg.getHeader(), buffer);

        try {
            msg.compose(response);
        } catch (Exception ex) {
            Log.getErrorLogger().error("Error when composing (" + response.getHeader() + ") occurred: ", ex);
            return;
        }

        if (!response.isFinalised()) {
            buffer.writeByte(1);
            response.setFinalised(true);
        }

        if (ServerConfiguration.getBoolean("log.sent.packets")) {
            log.info("SENT: {} / {}", msg.getHeader(), response.getBodyString());
        }

        out.add(buffer);
    }
}