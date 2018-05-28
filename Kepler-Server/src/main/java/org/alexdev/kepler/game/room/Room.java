package org.alexdev.kepler.game.room;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.room.enums.StatusType;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.game.room.managers.RoomEntityManager;
import org.alexdev.kepler.game.room.managers.RoomItemManager;
import org.alexdev.kepler.game.room.managers.RoomTaskManager;
import org.alexdev.kepler.game.room.mapping.RoomMapping;
import org.alexdev.kepler.game.room.models.RoomModel;
import org.alexdev.kepler.game.room.models.RoomModelManager;
import org.alexdev.kepler.messages.incoming.rooms.user.YOUAROWNER;
import org.alexdev.kepler.messages.outgoing.rooms.user.YOUARECONTROLLER;
import org.alexdev.kepler.messages.types.MessageComposer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private RoomData roomData;
    private RoomMapping roomMapping;
    private RoomEntityManager roomEntityManager;
    private RoomItemManager roomItemManager;
    private RoomTaskManager roomTaskManager;

    private List<Entity> entities;
    private List<Item> items;

    public Room() {
        this.roomData = new RoomData(this);
        this.roomEntityManager = new RoomEntityManager(this);
        this.roomItemManager = new RoomItemManager(this);
        this.roomTaskManager = new RoomTaskManager(this);
        this.roomMapping = new RoomMapping(this);
        this.entities = new CopyOnWriteArrayList<>();
        this.items = new CopyOnWriteArrayList<>();
    }

    /**
     * Send a packet to all players.
     *
     * @param composer the message composer packet
     */
    public void send(MessageComposer composer) {
        for (Player player : this.roomEntityManager.getPlayers()) {
            player.send(composer);
        }
    }

    /**
     * Checks if the user id is the owner of the room.
     *
     * @param ownerId the owner id to check for
     * @return true, if successful
     */
    public boolean isOwner(int ownerId) {
        return this.roomData.getOwnerId() == ownerId;
    }

    /**
     * Get if the player has rights.
     *
     * @param userId the user id to check if they have rights
     * @return true, if successful
     */
    public boolean hasRights(int userId) {
        if (this.isOwner(userId)) {
            return true;
        }

        if (this.roomData.allowSuperUsers()) {
            return true;
        }

        return false;
    }

    /**
     * Refresh the room rights for the user.
     *
     * @param player the player to refresh the rights for
     */
    public void refreshRights(Player player) {
        String rightsValue = "";

        if (hasRights(player.getDetails().getId())) {
            player.send(new YOUARECONTROLLER());
        }

        if (isOwner(player.getDetails().getId())) {
            player.send(new YOUAROWNER());
            rightsValue = " useradmin";
        }

        player.getRoomUser().removeStatus(StatusType.FLAT_CONTROL);

        if (hasRights(player.getDetails().getId()) || isOwner(player.getDetails().getId())) {
            player.getRoomUser().setStatus(StatusType.FLAT_CONTROL, rightsValue);
        }
    }

    /**
     * Try to dispose room, it will happen when there's no users
     * in the room.
     *
     * @return if the room was successfully disposed
     */
    public boolean tryDispose() {
        if (this.roomEntityManager.getEntitiesByClass(Player.class).size() > 0) {
            return false;
        }

        this.roomTaskManager.stopTasks();
        this.roomEntityManager.getInstanceIdCounter().set(0);
        this.items.clear();

        RoomManager.getInstance().removeRoom(this.roomData.getId());
        return true;
    }

    /**
     * Get the entity manager for this room.
     *
     * @return the entity manager
     */
    public RoomEntityManager getEntityManager() {
        return this.roomEntityManager;
    }

    /**
     * Get the item manager for this room.
     *
     * @return the item manager
     */
    public RoomItemManager getItemManager() {
        return roomItemManager;
    }

    /**
     * Get the task manager for this room.
     *
     * @return the task manager
     */
    public RoomTaskManager getTaskManager() {
        return roomTaskManager;
    }

    /**
     * Get the mapping manager for this room.
     *
     * @return the room mapping manager
     */
    public RoomMapping getMapping() {
        return roomMapping;
    }

    /**
     * Get the room data for this room.
     *
     * @return the room data
     */
    public RoomData getData() {
        return roomData;
    }

    /**
     * Get the room model instance.
     *
     * @return the room model
     */
    public RoomModel getModel() {
        return RoomModelManager.getInstance().getModel(this.roomData.getModel());
    }

    /**
     * Get the entire list of entities in the room.
     *
     * @return the list of entities
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Get the entire list of items in the room.
     *
     * @return the list of items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Get whether the room is a public room or not.
     *
     * @return true, if successful
     */
    public boolean isPublicRoom() {
        return this.roomData.getOwnerId() == 0;
    }

    /**
     * Get the room id of this room.
     */
    public int getId() {
        return this.roomData.getId();
    }
}
