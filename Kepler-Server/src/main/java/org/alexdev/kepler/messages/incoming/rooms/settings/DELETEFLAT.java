package org.alexdev.kepler.messages.incoming.rooms.settings;

import org.alexdev.kepler.dao.mysql.RoomDao;
import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.RoomManager;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

import java.util.ArrayList;
import java.util.List;

public class DELETEFLAT implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        int roomId = Integer.parseInt(reader.contents());

        Room room = RoomManager.getInstance().getRoomById(roomId);

        if (room == null) {
            return;
        }

        if (!room.isOwner(player.getDetails().getId())) {
            return;
        }

        List<Entity> entities = new ArrayList<>(room.getEntities());

        for (Entity entity : entities) {
            room.getEntityManager().leaveRoom(entity, true);
        }

        room.dispose(true);
        RoomDao.delete(room);
    }
}
