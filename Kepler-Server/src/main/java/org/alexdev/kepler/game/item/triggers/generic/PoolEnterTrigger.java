package org.alexdev.kepler.game.item.triggers.generic;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.entity.EntityType;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.triggers.ItemTrigger;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.RoomUser;
import org.alexdev.kepler.game.room.public_rooms.PoolHandler;

public class PoolEnterTrigger implements ItemTrigger {
    @Override
    public void onEntityStep(Entity entity, RoomUser roomUser, Item item, Object... customArgs) {
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }

        Position warp = null;
        Position goal = null;

        if (item.getPosition().getX() == 20 && item.getPosition().getY() == 28) {
            warp = new Position(21, 28);
            goal = new Position(22, 28);
        }

        if (item.getPosition().getX() == 17 && item.getPosition().getY() == 21) {
            warp = new Position(16, 22);
            goal = new Position(16, 23);
        }

        if (item.getPosition().getX() == 31 && item.getPosition().getY() == 10) {
            warp = new Position(30, 11);
            goal = new Position(30, 12);
        }

        if (warp != null) {
            PoolHandler.warpSwim(item, entity, warp, goal, false);
        }
    }

    @Override
    public void onEntityStop(Entity entity, RoomUser roomUser, Item item, Object... customArgs) {

    }

    @Override
    public void onEntityLeave(Entity entity, RoomUser roomUser, Item item, Object... customArgs) {

    }
}