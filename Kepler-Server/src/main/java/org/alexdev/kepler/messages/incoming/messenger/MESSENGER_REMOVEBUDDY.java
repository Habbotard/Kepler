package org.alexdev.kepler.messages.incoming.messenger;

import org.alexdev.kepler.dao.mysql.MessengerDao;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.messages.outgoing.messenger.REMOVE_FRIEND;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

import java.sql.SQLException;

public class MESSENGER_REMOVEBUDDY implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws SQLException {
        reader.readInt();
        int userId = reader.readInt();

        if (!player.getMessenger().isFriend(userId)) {
            return;
        }

        MessengerDao.removeFriend(userId, player.getDetails().getId());
        MessengerDao.removeFriend(player.getDetails().getId(), userId);

        player.send(new REMOVE_FRIEND(userId));
        player.getMessenger().getFriends().remove(player.getMessenger().getFriend(userId));

        Player friend = PlayerManager.getInstance().getPlayerById(userId);


        if (friend != null) {
            friend.send(new REMOVE_FRIEND(player.getDetails().getId()));
            friend.getMessenger().getFriends().remove(friend.getMessenger().getFriend(player.getDetails().getId()));
        }
    }
}
