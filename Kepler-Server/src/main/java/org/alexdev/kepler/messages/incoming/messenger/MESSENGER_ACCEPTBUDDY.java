package org.alexdev.kepler.messages.incoming.messenger;

import org.alexdev.kepler.dao.mysql.MessengerDao;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.messages.outgoing.messenger.NEW_FRIEND;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class MESSENGER_ACCEPTBUDDY implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        int amount = reader.readInt();

        for (int i = 0; i < amount; i++) {
            int userId = reader.readInt();

            if (!player.getMessenger().hasRequest(userId)) {
                continue;
            }

            MessengerDao.newFriend(userId, player.getDetails().getId());

            MessengerDao.removeRequest(userId, player.getDetails().getId());
            MessengerDao.removeRequest(player.getDetails().getId(), userId);

            player.send(new NEW_FRIEND(PlayerManager.getInstance().getPlayerData(userId)));
            Player friend = PlayerManager.getInstance().getPlayerById(userId);


            if (friend != null) {
                friend.send(new NEW_FRIEND(player.getDetails()));

                // Remove request instance
                friend.getMessenger().getRequests().remove(friend.getMessenger().getRequest(userId));
            }

            // Remove request instance
            player.getMessenger().getRequests().remove(friend.getMessenger().getRequest(userId));
        }
    }
}
