package org.alexdev.kepler.messages.incoming.rooms.user;

import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class GOAWAY implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        Room room = player.getRoomUser().getRoom();

        var curPos = player.getRoomUser().getPosition();
        var doorPos = room.getModel().getDoorLocation();

        // If we're standing in the door, immediately leave room
        if (curPos.equals(doorPos)) {
            room.getEntityManager().leaveRoom(player, true);
            return;
        }

        // Attempt to walk to the door
        player.getRoomUser().walkTo(doorPos.getX(), doorPos.getY());

        // If user isn't walking, leave immediately
        if (!player.getRoomUser().isWalking()) {
            player.getRoomUser().getRoom().getEntityManager().leaveRoom(player, true);
        }
    }
}
