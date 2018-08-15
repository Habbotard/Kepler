package org.alexdev.kepler.messages.incoming.rooms.moderation;

import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.texts.TextsManager;
import org.alexdev.kepler.messages.outgoing.user.ALERT;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class KICK implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws Exception {
        Player target = PlayerManager.getInstance().getPlayerByName(reader.contents());

        if (target == null) {
            return;
        }

        if (target.getEntityId() == player.getEntityId()) {
            return; // Can't kick yourself!
        }

        if (target.hasFuse("fuse_kick")) {
            player.send(new ALERT(TextsManager.getInstance().getValue("modtool_rankerror")));
            return;
        }

        Room room = player.getRoomUser().getRoom();

        if (room.isOwner(player.getEntityId())
                || room.getRights().contains(player.getEntityId())
                || player.hasFuse("fuse_kick")) {

            target.getRoomUser().kick(false);
        }
    }
}
