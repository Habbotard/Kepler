package org.alexdev.kepler.messages.outgoing.rooms.items;

import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;
import org.alexdev.kepler.util.StringUtil;

public class SHOWPROGRAM extends MessageComposer {
    private final String[] arguments;
    public SHOWPROGRAM(String[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public void compose(NettyResponse response) {
        for (String argument : arguments) {
            if (argument.length() > 0) {
                response.writeDelimeter(argument, ' ');
            }
        }
    }

    @Override
    public short getHeader() {
        return 71; // "AG"
    }
}
