package org.alexdev.kepler.game.room.mapping;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.models.RoomModel;
import org.alexdev.kepler.game.room.public_rooms.PoolHandler;

import java.util.ArrayList;
import java.util.List;

public class RoomMapping {
    private Room room;
    private RoomModel roomModel;
    private RoomTile roomMap[][];

    public RoomMapping(Room room) {
        this.room = room;
    }

    /**
     * Regenerate the entire collision map used for
     * furniture detection.
     */
    public void regenerateCollisionMap() {
        this.roomModel = this.room.getData().getModel();
        this.roomMap = new RoomTile[this.roomModel.getMapSizeX()][this.roomModel.getMapSizeY()];

        for (int x = 0; x < this.roomModel.getMapSizeX(); x++) {
            for (int y = 0; y < this.roomModel.getMapSizeY(); y++) {
                this.roomMap[x][y] = new RoomTile(new Position(x, y));
                this.roomMap[x][y].setTileHeight(this.roomModel.getTileHeight(x, y));
            }
        }

        synchronized (this.room.getItems()) {
            List<Item> items = new ArrayList<>(this.room.getItems());
            items.sort((item1, item2) -> Double.compare(item1.getPosition().getZ(), item2.getPosition().getZ()));

            for (Item item : items) {
                if (item.getDefinition().getBehaviour().isWallItem()) {
                    continue;
                }

                RoomTile tile = getTile(item.getPosition().getX(), item.getPosition().getY());

                if (tile == null) {
                    continue;
                }

                if (tile.getTileHeight() < item.getTotalHeight() || item.getDefinition().getBehaviour().isPublicSpaceObject()) {
                    tile.setItemBelow(tile.getHighestItem());
                    tile.setTileHeight(item.getTotalHeight());
                    tile.setHighestItem(item);

                    if (item.getDefinition().getBehaviour().isPublicSpaceObject()) {
                        PoolHandler.setupRedirections(this.room, item);
                    }
                }
            }
        }
    }

    /**
     * Method for the pathfinder to check if the tile next to the current tile is a valid step.
     *
     * @param entity the entity walking
     * @param current the current tile
     * @param tmp the temporary tile around the current tile to check
     * @param isFinalMove if the move was final
     * @return true, if a valid step
     */
    public boolean isValidStep(Entity entity, Position current, Position tmp, boolean isFinalMove) {
        if (!this.isValidTile(entity, new Position(current.getX(), current.getY()))) {
            return false;
        }

        if (!this.isValidTile(entity, new Position(tmp.getX(), tmp.getY()))) {
            return false;
        }

        RoomTile fromTile = this.getTile(current.getX(), current.getY());
        RoomTile toTile = this.getTile(tmp.getX(), tmp.getY());

        double oldHeight = fromTile.getTileHeight();
        double newHeight = toTile.getTileHeight();

        if (oldHeight - 4 >= newHeight) {
            return false;
        }

        if (oldHeight + 1.5 <= newHeight) {
            return false;
        }

        Item fromItem = fromTile.getHighestItem();
        Item toItem = toTile.getHighestItem();

        if (fromItem != null) {
            if (fromItem.getDefinition().getSprite().equals("poolEnter") ||
                fromItem.getDefinition().getSprite().equals("poolLeave")) {
                return entity.getDetails().getPoolFigure().length() > 0;
            }

            if (toItem != null) {
                if (entity.getRoomUser().getRoom().getData().getModelId().equals("pool_b")) {
                    if (fromItem.getDefinition().getSprite().equals("queue_tile2") &&
                        toItem.getDefinition().getSprite().equals("queue_tile2")) {
                        return true;
                    }
                }
            }
        }

        if (toItem != null) {
            if (toItem.getDefinition().getSprite().equals("poolBooth") ||
                toItem.getDefinition().getSprite().equals("poolLift")) {

                if (toItem.getCurrentProgramValue().equals("close")) {
                    return false;
                } else {
                    return !toItem.getDefinition().getSprite().equals("poolLift") || entity.getDetails().getPoolFigure().length() > 0;
                }
            }

            if (entity.getRoomUser().getRoom().getData().getModel().getModelName().equals("pool_b") &&
                toItem.getDefinition().getSprite().equals("queue_tile2")) {

                if (toItem.getPosition().getX() == 21 && toItem.getPosition().getY() == 9) {
                    return entity.getDetails().getTickets() > 0 && entity.getDetails().getPoolFigure().length() > 0;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets if the tile was valid.
     *
     * @param entity the entity checking
     * @param position the position of the tile
     * @return true, if successful
     */
    public boolean isValidTile(Entity entity, Position position) {
        RoomTile tile = getTile(position.getX(), position.getY());

        if (tile == null) {
            return false;
        }

        if (tile.getHighestItem() != null) {
            return tile.getHighestItem().isWalkable();
        }

        return this.roomModel.getTileState(position.getX(), position.getY()) == RoomTileState.OPEN;
    }

    /**
     * Get the tile by specified coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the tile, found, else null
     */
    public RoomTile getTile(int x, int y) {
        if (x < 0 || y < 0) {
            return null;
        }

        if (x >= this.roomModel.getMapSizeX() || y >= this.roomModel.getMapSizeY()) {
            return null;
        }

        return this.roomMap[x][y];
    }
}