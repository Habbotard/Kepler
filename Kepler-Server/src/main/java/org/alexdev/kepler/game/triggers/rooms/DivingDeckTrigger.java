package org.alexdev.kepler.game.triggers.rooms;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.entities.RoomEntity;
import org.alexdev.kepler.game.triggers.GenericTrigger;

public class DivingDeckTrigger extends GenericTrigger {
    private class DivingDeckCamera implements Runnable {

        @Override
        public void run() {

        }
    }

    @Override
    public void onRoomEntry(Player player, Room room, Object... customArgs) {

    }

    @Override
    public void onRoomLeave(Player player, Room room, Object... customArgs)  {

    }
}
