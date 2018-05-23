package org.alexdev.kepler.messages.incoming.rooms.settings;

import org.alexdev.kepler.dao.mysql.NavigatorDao;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.RoomManager;
import org.alexdev.kepler.game.room.models.RoomModelManager;
import org.alexdev.kepler.messages.outgoing.rooms.settings.GOTO_FLAT;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.StringUtil;

import java.lang.management.PlatformLoggingMXBean;

public class CREATEFLAT implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        String[] data = reader.contents().split("/");

        String floorSetting = data[1];
        String roomName = StringUtil.filterInput(data[2], true);
        String roomModel = data[3];
        String roomAccess = data[4];
        boolean roomShowName = Integer.parseInt(data[5]) == 1;

        if (!floorSetting.equals("first floor")) {
            return;
        }

        if (!roomModel.startsWith("model_")) {
            return;
        }

        if (RoomModelManager.getInstance().getModel(roomModel) == null) {
            return;
        }

        String modelType = roomModel.replace("model_a", "");

        if (!modelType.equals("a") &&
                !modelType.equals("b") &&
                !modelType.equals("c") &&
                !modelType.equals("d") &&
                !modelType.equals("f") &&
                !player.hasFuse("fuse_use_special_room_layouts")) {
            return; // Fuck off, scripter.
        }

        int accessType = 0;

        if (roomAccess.equals("password")) {
            accessType = 2;
        }

        if (roomAccess.equals("closed")) {
            accessType = 1;
        }

        int roomId = NavigatorDao.createRoom(player.getDetails().getId(), roomName, roomModel, roomShowName, accessType);
        RoomManager.getInstance().addRoom(roomId);

        player.send(new GOTO_FLAT(roomId, roomName));
    }
}