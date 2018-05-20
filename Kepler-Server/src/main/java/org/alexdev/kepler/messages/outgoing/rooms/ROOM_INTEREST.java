package org.alexdev.kepler.messages.outgoing.rooms;

import org.alexdev.kepler.messages.headers.Outgoing;
import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;

public class ROOM_INTEREST extends MessageComposer {
    @Override
    public void compose(NettyResponse response) {
        response.writeInt(0);
    }

    @Override
    public short getHeader() {
        return Outgoing.ROOM_INTEREST;
    }
}
