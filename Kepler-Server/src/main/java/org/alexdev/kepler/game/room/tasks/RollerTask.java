package org.alexdev.kepler.game.room.tasks;

import org.alexdev.kepler.dao.mysql.ItemDao;
import org.alexdev.kepler.game.GameScheduler;
import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.base.ItemBehaviour;
import org.alexdev.kepler.game.pathfinder.Pathfinder;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.managers.RoomItemManager;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.messages.outgoing.rooms.items.SLIDE_OBJECT;
import org.alexdev.kepler.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RollerTask implements Runnable {
    private final Room room;

    public RollerTask(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        List<Item> itemsToUpdate = new ArrayList<>();
        List<Object> blacklist = new ArrayList<>();

        for (Item roller : this.room.getItems()) {
            if (!roller.hasBehaviour(ItemBehaviour.ROLLER)) {
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

        if (blacklist.size() > 0) {
            this.room.flushQueued();
        }

        if (itemsToUpdate.size() > 0) {
            this.room.getMapping().regenerateCollisionMap();
            ItemDao.updateItems(itemsToUpdate);

            GameScheduler.getInstance().getSchedulerService().schedule(
                    new ItemRollingTask(itemsToUpdate, room),
                    900,
                    TimeUnit.MILLISECONDS
            );
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
        RoomTile frontTile = this.room.getMapping().getTile(front.getX(), front.getY());

        if (frontTile == null) {
            return false;
        }

        if (front.equals(this.room.getModel().getDoorLocation())) {
            return false;
        }

        if (!(RoomItemManager.containsItemBehaviour(frontTile.getItems(), ItemBehaviour.ROLLER))) {
            if (!RoomTile.isValidTile(this.room, null, front)) {
                return false; // Cancel rolling into invalid tile, if the tile isn't a roller tile.
            }

            if (frontTile.getEntities().size() > 0 && !item.isWalkable()) {
                return false; // Don't allow a non-walkable item to roll into an entity
            }
        }

        boolean subtractRollerHeight = true;
        double nextHeight = item.getPosition().getZ();

        if (frontTile.getHighestItem() != null) {
            for (Item frontItem : frontTile.getItems()) {
                if (frontItem.hasBehaviour(ItemBehaviour.ROLLER)) {
                    Position frontPosition = frontItem.getPosition().getSquareInFront();

                    // Don't roll an item into the next roller, if the next roller is facing towards the roller
                    // it just rolled from.
                    if (frontPosition.equals(item.getPosition())) {
                        if (frontTile.getItems().size() > 1) {
                            return false;
                        }
                    }
                }

                // We know the next tile contains a roller, so we don't subtract the roller height
                subtractRollerHeight = false;
            }
        }

        if (subtractRollerHeight) {
            nextHeight -= roller.getDefinition().getTopHeight();
        }

        this.room.sendQueued(new SLIDE_OBJECT(item, front, roller.getId(), nextHeight));

        item.getPosition().setX(front.getX());
        item.getPosition().setY(front.getY());
        item.getPosition().setZ(nextHeight);
        item.setRolling(true);

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
        RoomTile frontTile = this.room.getMapping().getTile(front);

        if (frontTile == null) {
            return;
        }

        if (front.equals(this.room.getModel().getDoorLocation())) {
            return;
        }

        if (!(RoomItemManager.containsItemBehaviour(frontTile.getItems(), ItemBehaviour.ROLLER))) {
            if (!RoomTile.isValidTile(this.room, entity, front)) {
                return; // Cancel rolling into invalid tile, if the tile isn't a roller tile.
            }
        }

        boolean subtractRollerHeight = true;
        double nextHeight = entity.getRoomUser().getPosition().getZ();

        if (frontTile.getHighestItem() != null) {
            for (Item frontItem : frontTile.getItems()) {
                if (frontItem.hasBehaviour(ItemBehaviour.ROLLER)) {
                    Position frontPosition = frontItem.getPosition().getSquareInFront();

                    // Don't roll an item into the next roller, if the next roller is facing towards the roller
                    // it just rolled from.
                    if (frontPosition.equals(entity.getRoomUser().getPosition())) {
                        if (frontTile.getItems().size() > 1) {
                            return;
                        }
                    }
                }

                // We know the next tile contains a roller, so we don't subtract the roller height
                subtractRollerHeight = false;
            }
        }

        if (subtractRollerHeight) {
            nextHeight -= roller.getDefinition().getTopHeight();
        }

        RoomTile previousTile = this.room.getMapping().getTile(entity.getRoomUser().getPosition().getX(), entity.getRoomUser().getPosition().getY());

        previousTile.removeEntity(entity);
        frontTile.addEntity(entity);

        double displayNextHeight = nextHeight;

        if (entity.getRoomUser().isSittingOnGround()) {
            displayNextHeight -= 0.5; // Take away sit offset because yeah, weird stuff.
        }

        if (!entity.getRoomUser().isSittingOnGround()) {
            entity.getRoomUser().invokeItem(); // Invoke the current tile item if they're not sitting on rollers.
        }

        this.room.sendQueued(new SLIDE_OBJECT(entity, front, roller.getId(), displayNextHeight));

        entity.getRoomUser().setNeedsUpdate(true);
        entity.getRoomUser().getPosition().setX(front.getX());
        entity.getRoomUser().getPosition().setY(front.getY());
        entity.getRoomUser().getPosition().setZ(nextHeight);
    }
}
