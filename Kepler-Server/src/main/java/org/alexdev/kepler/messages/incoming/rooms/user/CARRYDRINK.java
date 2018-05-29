package org.alexdev.kepler.messages.incoming.rooms.user;

import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.StringUtil;

public class CARRYDRINK implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        String contents = reader.contents();

        if (StringUtil.isNumber(contents)) {
            player.getRoomUser().carryItem(Integer.parseInt(contents), null);
        } else {
            player.getRoomUser().carryItem(-1, contents);
        }

        player.getRoomUser().resetRoomTimer();
    }
}
