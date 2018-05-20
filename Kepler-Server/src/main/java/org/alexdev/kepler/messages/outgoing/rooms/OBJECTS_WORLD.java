package org.alexdev.kepler.messages.outgoing.rooms;

import org.alexdev.kepler.messages.headers.Outgoing;
import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;

public class OBJECTS_WORLD extends MessageComposer {
    @Override
    public void compose(NettyResponse response) {

    }

    @Override
    public short getHeader() {
        return Outgoing.OBJECTS_WORLD;
    }
}
