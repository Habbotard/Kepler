package org.alexdev.kepler.messages.outgoing.handshake;

import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;

public class HELLO extends MessageComposer {

    @Override
    public void compose(NettyResponse response) {

    }

    @Override
    public short getHeader() {
        return 0; // "@@"
    }
}
