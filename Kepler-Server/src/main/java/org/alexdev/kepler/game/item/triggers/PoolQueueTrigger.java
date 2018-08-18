package org.alexdev.kepler.game.item.triggers;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.entity.EntityType;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.pathfinder.Rotation;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.entities.RoomEntity;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.game.room.tasks.StatusTask;
import org.alexdev.kepler.game.triggers.GenericTrigger;
import org.alexdev.kepler.messages.outgoing.user.currencies.NO_TICKETS;

public class PoolQueueTrigger extends GenericTrigger {

    @Override
    public void onEntityStep(Entity entity, RoomEntity roomEntity, Item item, Position oldPosition, Object... customArgs) {
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player)entity;

        if (player.getDetails().getTickets() == 0 || player.getDetails().getPoolFigure().isEmpty()) {
            player.getRoomUser().stopWalking();
            player.getRoomUser().warp(oldPosition, false);

            player.send(new NO_TICKETS());
            /*int rotation = (player.getRoomUser().getPosition().getRotation() % 2 == 0) ?
                    (player.getRoomUser().getPosition().getRotation()) :
                    (player.getRoomUser().getPosition().getRotation() / 2 * 2);

            Position temp = new Position(player.getRoomUser().getPosition().getX(), player.getRoomUser().getPosition().getY(), rotation);

            Position[] positionsToCheck = new Position[]{
                    //temp.getSquareInFront(),
                    temp.getSquareRight(),
                    temp.getSquareLeft(),
                    temp.getSquareBehind()
            };

            for (var nextPosition : positionsToCheck) {
                RoomTile nextTile = player.getRoomUser().getRoom().getMapping().getTile(nextPosition);

                if (nextTile == null) {
                    continue;
                }

                Position copy = nextTile.getPosition().copy();
                copy.setRotation(Rotation.calculateWalkDirection(player.getRoomUser().getPosition(), copy));

                if (nextTile.getHighestItem() == null) {
                    player.getRoomUser().warp(nextTile.getPosition(), false);
                    break;
                }
            }*/
        }
    }

    @Override
    public void onEntityStop(Entity entity, RoomEntity roomEntity, Item item, Object... customArgs) {
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player)entity;

        // When they stop walking, check if the player is on a pool lido queue and walk to the next one
        StatusTask.processPoolQueue(player);
    }

    @Override
    public void onEntityLeave(Entity entity, RoomEntity roomEntity, Item item, Object... customArgs) {

    }
}
