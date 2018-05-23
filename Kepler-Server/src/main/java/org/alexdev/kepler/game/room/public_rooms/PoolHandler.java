package org.alexdev.kepler.game.room.public_rooms;

import javafx.geometry.Pos;
import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.entity.EntityStatus;
import org.alexdev.kepler.game.entity.EntityType;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.RoomUser;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.messages.outgoing.rooms.items.SHOWPROGRAM;
import org.alexdev.kepler.messages.outgoing.rooms.pool.OPEN_UIMAKOPPI;
import org.alexdev.kepler.util.StringUtil;

public class PoolHandler {
    public static void setupRedirections(Room room, Item item) {
        if (item.getDefinition().getSprite().equals("poolBooth")) {
            if (item.getPosition().getX() == 17 && item.getPosition().getY() == 11) {
                room.getMapping().getTile(18, 11).setHighestItem(item);
            }

            if (item.getPosition().getX() == 17 && item.getPosition().getY() == 9) {
                room.getMapping().getTile(18, 9).setHighestItem(item);
            }

            if (item.getPosition().getX() == 8 && item.getPosition().getY() == 1) {
                room.getMapping().getTile(8, 0).setHighestItem(item);
            }

            if (item.getPosition().getX() == 9 && item.getPosition().getY() == 1) {
                room.getMapping().getTile(9, 0).setHighestItem(item);
            }
        }
    }

    public static void interact(Item item, Entity entity) {
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) entity;

        if (item.getDefinition().getSprite().equals("poolBooth")) {
            item.showProgram("close");
            player.getRoomUser().setWalkingAllowed(false);
            player.send(new OPEN_UIMAKOPPI());
        }

        if (item.getDefinition().getSprite().equals("poolEnter")) {
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

            if (warp != null && goal != null) {
                warpSwim(item, entity, warp, goal, false);
            }
        }
    }

    private static void warpSwim(Item item, Entity entity, Position warp, Position goal, boolean exit) {
        RoomUser roomUser = entity.getRoomUser();
        Room room = roomUser.getRoom();

        if (exit) {
            roomUser.removeStatus(EntityStatus.SWIM);
        } else {
            roomUser.setStatus(EntityStatus.SWIM, "");
        }

        System.out.println("Walk to: " + warp);

        //roomUser.setWalking(false);
        roomUser.setNextPosition(new Position(warp.getX(), warp.getY(), room.getMapping().getTile(warp.getX(), warp.getY()).getTileHeight()));
        roomUser.setWalking(true);

        item.showProgram("");
        roomUser.walkTo(goal.getX(), goal.getY());
    }

    public static void exitBooth(Player player) {
        RoomTile tile = player.getRoomUser().getTile();
        Room room = player.getRoomUser().getRoom();

        if (tile == null || tile.getHighestItem() == null || room == null) {
            return;
        }

        if (!tile.getHighestItem().getDefinition().getSprite().equals("poolBooth")) {
            return;
        }

        if (!room.getData().getModel().getModelName().equals("pool_a") &&
            !room.getData().getModel().getModelName().equals("md_a")) {
            return;
        }

        tile.getHighestItem().showProgram("open");
        player.getRoomUser().setWalkingAllowed(true);

        if (room.getData().getModel().getModelName().equals("pool_a")) {
            if (player.getRoomUser().getPosition().getY() == 11) {
                player.getRoomUser().walkTo(19, 11);
            }

            if (player.getRoomUser().getPosition().getY() == 9) {
                player.getRoomUser().walkTo(19, 9);
            }
        }

        if (room.getData().getModel().getModelName().equals("md_a")) {
            if (player.getRoomUser().getPosition().getX() == 8) {
                player.getRoomUser().walkTo(8, 2);
            }

            if (player.getRoomUser().getPosition().getX() == 9) {
                player.getRoomUser().walkTo(9, 9);
            }
        }
    }

    public static void disconnect(Player player) {
        RoomTile tile = player.getRoomUser().getTile();
        Room room = player.getRoomUser().getRoom();

        if (tile == null || tile.getHighestItem() == null || room == null) {
            return;
        }

        Item item = tile.getHighestItem();

        if (item.getDefinition().getSprite().equals("poolBooth") ||
            item.getDefinition().getSprite().equals("poolLift")) {
            item.showProgram("open");
        }
    }
}
