package org.alexdev.kepler.game.room.tasks;

import org.alexdev.kepler.dao.mysql.ItemDao;
import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.pathfinder.Pathfinder;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.messages.outgoing.rooms.items.SLIDE_OBJECT;
import org.alexdev.kepler.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ProcessRollerTask implements Runnable {
    private final Room room;

    public ProcessRollerTask(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        List<Item> itemsToUpdate = new ArrayList<>();
        List<Object> blacklist = new ArrayList<>();

        for (Item roller : this.room.getItems()) {
            if (!roller.getBehaviour().isRoller()) {
                continue;
            }

            List<Entity> entities = roller.getTile().getEntities();
            List<Item> items = roller.getTile().getItems();

            // Process items on rollers
            for (Item item : items) {
                if (blacklist.contains(item)) {
                    continue;
                }

                if (this.processItem(roller, item)) {
                    itemsToUpdate.add(item);
                    blacklist.add(item);
                }
            }

            // Process entities on rollers
            for (Entity entity : entities) {
                if (blacklist.contains(entity)) {
                    continue;
                }

                this.processEntity(roller, entity);
                blacklist.add(entity);
            }
        }

        if (itemsToUpdate.size() > 0) {
            this.room.getMapping().regenerateCollisionMap();
            ItemDao.updateItems(itemsToUpdate);
        }
    }

    /**
     * Process rolling item on rollers.
     *
     * @param roller the roller being used
     * @param item the item being rolled
     * @return true, if rolled
     */
    private boolean processItem(Item roller, Item item) {
        if (roller == null) {
            return false;
        }

        if (item.getId() == roller.getId()) {
            return false;
        }

        if (item.getPosition().getZ() < roller.getPosition().getZ()) {
            return false;
        }

        Position front = roller.getPosition().getSquareInFront();

        if (!RoomTile.isValidTile(this.room, null, front)) {
            return false;
        }

        RoomTile frontTile = this.room.getMapping().getTile(front.getX(), front.getY());
        double nextHeight = frontTile.getTileHeight();

        if (frontTile.getHighestItem() != null) {
            if (!frontTile.getHighestItem().getBehaviour().isRoller()) {
                if (item.getBehaviour().isCanStackOnTop() && item.getDefinition().getStackHeight() == frontTile.getHighestItem().getDefinition().getStackHeight()) {
                    nextHeight -= item.getDefinition().getStackHeight();
                }
            }
        }

        // If this item is stacked, we maintain its stack height
        if (item.getItemBelow() != null) {
            if (!item.getItemBelow().getBehaviour().isRoller()) {
                nextHeight = item.getPosition().getZ();

                // If the next tile/front tile is not a roller, we need to adjust the sliding so the stacked items
                // don't float, so we subtract the stack height of the roller
                boolean subtractRollerHeight = false;

                if (frontTile.getHighestItem() != null) {
                    if (!frontTile.getHighestItem().getBehaviour().isRoller()) {
                        subtractRollerHeight = true;
                    }
                } else {
                    subtractRollerHeight = true;
                }

                if (subtractRollerHeight) {
                    nextHeight -= roller.getDefinition().getStackHeight();
                }
            }
        }

        this.room.send(new SLIDE_OBJECT(item, front, roller.getId(), nextHeight));

        item.getPosition().setX(front.getX());
        item.getPosition().setY(front.getY());
        item.getPosition().setZ(nextHeight);

        return true;
    }

    /**
     * Process entity on roller.
     *
     * @param roller the roller being used
     * @param entity the entity being rolled
     */
    private void processEntity(Item roller, Entity entity) {
        if (entity.getRoomUser().isWalking()) {
            return; // Don't roll user if they're working.
        }

        if (!entity.getRoomUser().getPosition().equals(roller.getPosition())) {
            return; // Don't roll users who aren't on this tile.
        }

        if (entity.getRoomUser().getPosition().getZ() < roller.getPosition().getZ()) {
            return; // Don't roll user if they're below the roller
        }

        Position front = roller.getPosition().getSquareInFront();

        if (!Pathfinder.isValidStep(this.room, entity, entity.getRoomUser().getPosition(), front, false)) {
            return;
        }

        RoomTile nextTile = this.room.getMapping().getTile(front.getX(), front.getY());
        RoomTile previousTile = this.room.getMapping().getTile(entity.getRoomUser().getPosition().getX(), entity.getRoomUser().getPosition().getY());

        previousTile.removeEntity(entity);
        nextTile.addEntity(entity);

        double nextHeight = nextTile.getTileHeight();
        double displayNextHeight = nextHeight;

        if (entity.getRoomUser().isSittingOnGround()) {
            displayNextHeight -= 0.5; // Take away sit offset because yeah, weird stuff.
        }

        this.room.send(new SLIDE_OBJECT(entity, front, roller.getId(), displayNextHeight));

        if (!entity.getRoomUser().isSittingOnGround()) {
            entity.getRoomUser().invokeItem(); // Invoke the current tile item if they're not sitting on rollers.
        }

        entity.getRoomUser().setNeedsUpdate(true);
        entity.getRoomUser().getPosition().setX(front.getX());
        entity.getRoomUser().getPosition().setY(front.getY());
        entity.getRoomUser().getPosition().setZ(nextHeight);
    }
}
