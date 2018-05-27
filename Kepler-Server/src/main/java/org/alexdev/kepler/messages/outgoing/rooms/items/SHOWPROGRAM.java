package org.alexdev.kepler.messages.outgoing.rooms.items;

import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;
import org.alexdev.kepler.util.StringUtil;

public class SHOWPROGRAM extends MessageComposer {
    private final String[] arguments;
    /*private final String currentProgramValue;
    private final String currentProgram;*/

    public SHOWPROGRAM(String currentProgram, String currentProgramValue) {
        this(new String[] { currentProgram, currentProgramValue });
    }

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
       /* response.write(this.currentProgram);

        if (this.currentProgramValue.length() > 0) {
            response.write(" ");
            response.write(this.currentProgramValue);
        }*/
    }

    @Override
    public short getHeader() {
        return 71; // "AG"
    }
}
