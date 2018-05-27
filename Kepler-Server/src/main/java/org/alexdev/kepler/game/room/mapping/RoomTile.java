package org.alexdev.kepler.game.room.mapping;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.Room;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomTile {
    private Room room;
    private Position position;
    private List<Entity> entities;
    private List<Item> items;

    private double tileHeight;
    private Item highestItem;

    public RoomTile(Room room, Position position) {
        this.room = room;
        this.position = position;
        this.entities = new CopyOnWriteArrayList<>();
        this.items = new CopyOnWriteArrayList<>();
    }

    /**
     * Gets if the tile was valid.
     *
     * @param entity the entity checking
     * @param position the position of the tile
     * @return true, if successful
     */
    public static boolean isValidTile(Room room, Entity entity, Position position) {
        if (room == null) {
            return false;
        }

        RoomTile tile = room.getMapping().getTile(position);

        if (tile == null) {
            return false;
        }

        if (tile.getHighestItem() != null && !tile.getHighestItem().isWalkable()) {
            if (entity != null) {
                return tile.getHighestItem().getPosition().equals(entity.getRoomUser().getPosition());
            } else {
                return false;
            }
        }

        if (entity != null) {
            if (!room.isPublicRoom()) {
                if (tile.getEntities().size() > 0) {
                    return tile.containsEntity(entity); // Allow walk if you exist already in the tile
                }
            }
        }

        return room.getModel().getTileState(position.getX(), position.getY()) == RoomTileState.OPEN;
    }

    /**
     * Sets the entity.
     *
     * @param entity the new entity
     */
    public void addEntity(Entity entity) {
        if (new Position(this.position.getX(), this.position.getY()).equals(this.room.getModel().getDoorLocation())) {
            return;
        }

        this.entities.add(entity);
    }

    /**
     * Contains the entity.
     *
     * @param entity the entity
     * @return true, if successful
     */
    public boolean containsEntity(Entity entity) {
        return this.entities.contains(entity);
    }

    /**
     * Removes the entity.
     *
     * @param entity the entity
     */
    public void removeEntity(Entity entity) {
        this.entities.remove(entity);
    }

    /**
     * Get the current position of the tile.
     *
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Get the current height of the tile.
     *
     * @return the tile height
     */
    public double getTileHeight() {
        return tileHeight;
    }

    /**
     * Set the tile height of this tile.
     *
     * @param tileHeight the new tile height
     */
    public void setTileHeight(double tileHeight) {
        this.tileHeight = tileHeight;
    }

    /**
     * Get the highest item in this tile.
     *
     * @return the highest item
     */
    public Item getHighestItem() {
        return highestItem;
    }

    /**
     * Set the highest item in this tile.
     *
     * @return the highest item
     */
    public void setHighestItem(Item highestItem) {
        this.highestItem = highestItem;
    }

    public List<Entity> getEntities() {
        return this.entities;
    }

    public List<Item> getItems() {
        return items;
    }
}
