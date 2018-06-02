package org.alexdev.kepler.game.item;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.item.base.ItemBehaviour;
import org.alexdev.kepler.game.item.base.ItemDefinition;
import org.alexdev.kepler.game.pathfinder.AffectedTile;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.RoomManager;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.game.room.mapping.RoomTileState;
import org.alexdev.kepler.messages.outgoing.rooms.items.SHOWPROGRAM;
import org.alexdev.kepler.messages.outgoing.rooms.items.UPDATE_ITEM;
import org.alexdev.kepler.server.netty.streams.NettyResponse;
import org.alexdev.kepler.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private int id;
    private int ownerId;
    private int roomId;
    private ItemDefinition definition;
    private Item itemBelow;

    private Position position;
    private String wallPosition;
    private String customData;
    private String currentProgram;
    private String currentProgramValue;

    private boolean requiresUpdate;

    public Item() {
        this.id = 0;
        this.definition = new ItemDefinition();
        this.position = new Position();
        this.customData = "";
        this.wallPosition = "";
        this.currentProgram = "";
        this.currentProgramValue = "";
        this.requiresUpdate = false;
    }

    /**
     * Broadcast item program to current room, used for the pool lift, booth, pool ladders, etc
     * for special effects like splashing, closing/open curtains, etc.
     *
     * @param value the new program value to show
     */
    public void showProgram(String value) {
        if (value != null) {
            this.currentProgramValue = value;
        }

        Room room = this.getRoom();

        if (room != null) {
            room.send(new SHOWPROGRAM(new String[]{this.currentProgram, this.currentProgramValue}));
        }
    }

    /**
     * Update user statuses on items with their old position and new position.
     * The old position is never null if the item is moved.
     *
     * @param oldPosition the old position of the item
     */
    public void updateEntities(Position oldPosition) {
        if (this.hasBehaviour(ItemBehaviour.WALL_ITEM)) {
            return;
        }

        List<Entity> entitiesToUpdate = new ArrayList<>();

        if (oldPosition != null) {
            for (Position position : AffectedTile.getAffectedTiles(this, oldPosition.getX(), oldPosition.getY(), oldPosition.getRotation())) {
                RoomTile tile = this.getRoom().getMapping().getTile(position);

                if (tile == null) {
                    continue;
                }

                entitiesToUpdate.addAll(tile.getEntities());
            }
        }

        for (Position position :  AffectedTile.getAffectedTiles(this)) {
            RoomTile tile = this.getRoom().getMapping().getTile(position);

            if (tile == null) {
                continue;
            }

            entitiesToUpdate.addAll(tile.getEntities());
        }

        for (Entity entity : entitiesToUpdate) {
            entity.getRoomUser().invokeItem();
        }
    }

    /**
     * Get the total height, which is the height of the item plus stack size.
     *
     * @return the total height
     */
    public double getTotalHeight() {
        return this.position.getZ() + this.definition.getStackHeight();
    }

    /**
     * Get whether or not the item is walkable.
     *
     * @return true, if successful.
     */
    public boolean isWalkable() {
        if (this.hasBehaviour(ItemBehaviour.CAN_SIT_ON_TOP)) {
            return true;
        }

        if (this.hasBehaviour(ItemBehaviour.CAN_LAY_ON_TOP)) {
            return true;
        }

        if (this.hasBehaviour(ItemBehaviour.CAN_STAND_ON_TOP)) {
            return true;
        }

        if (this.hasBehaviour(ItemBehaviour.DOOR)) {
            return this.customData.equals("O");
        }

        return false;
    }

    /**
     * Send status update of the item.
     */
    public void updateStatus() {
        Room room = this.getRoom();

        if (room != null) {
            room.send(new UPDATE_ITEM(this));
        }
    }

    /**
     * Serialise item function for item handling packets.
     *
     * @param response the response to serialise to
     */
    public void serialise(NettyResponse response) {
        if (this.definition.hasBehaviour(ItemBehaviour.PUBLIC_SPACE_OBJECT)) {
            response.writeDelimeter(this.customData, ' ');
            response.writeString(this.definition.getSprite());
            response.writeDelimeter(this.position.getX(), ' ');
            response.writeDelimeter(this.position.getY(), ' ');
            response.writeDelimeter((int) this.position.getZ(), ' ');
            response.write(this.position.getRotation());

            if (this.hasBehaviour(ItemBehaviour.EXTRA_PARAMETER)) {
                response.write(" 2");
            }

            response.write(Character.toString((char) 13));
        } else {
            if (this.hasBehaviour(ItemBehaviour.WALL_ITEM)) {
                response.writeDelimeter(this.id, (char) 9);
                response.writeDelimeter(this.definition.getSprite(), (char) 9);
                response.writeDelimeter(" ", (char) 9);
                response.writeDelimeter(this.wallPosition, (char) 9);

                if (this.customData.length() > 0) {
                    if (this.hasBehaviour(ItemBehaviour.POST_IT)) {
                        response.write(this.customData.substring(0, 6)); // Only show post-it colour
                    } else {
                        response.write(this.customData);
                    }
                }

                response.write(Character.toString((char) 13));
            } else {
                response.writeString(this.id);
                response.writeString(this.definition.getSprite());
                response.writeInt(this.position.getX());
                response.writeInt(this.position.getY());
                response.writeInt(this.definition.getLength());
                response.writeInt(this.definition.getWidth());
                response.writeInt(this.position.getRotation());
                response.writeString(StringUtil.format(this.position.getZ()));
                response.writeString(this.definition.getColour());
                response.writeString("");
                response.writeInt(this.hasBehaviour(ItemBehaviour.ROLLER) ? 2 : 0); // Required 2 for rollers to enable animation when rollers are used!
                response.writeString(this.customData);
            }
        }
    }

    /**
     * Check if the move is valid before moving an item. Will prevent long
     * furniture from being on top of rollers, will prevent placing rollers on top of other rollers.
     * Will prevent items being placed on closed tile states.
     *
     * @param room the room to check inside
     * @param x the new x to check
     * @param y the new y to check
     * @param rotation the new rotation to check
     * @return true, if successful
     */
    public boolean isValidMove(Item item, Room room, int x, int y, int rotation) {
        for (Position position : AffectedTile.getAffectedTiles(this, x, y, rotation)) {
            RoomTile tile = room.getMapping().getTile(position);

            if (tile == null) {
                return false;
            }

            if (room.getModel().getTileState(position.getX(), position.getY()) == RoomTileState.CLOSED) {
                return false;
            }

            for (Item tileItem : tile.getItems()) {
                if (tileItem.getId() == item.getId()) {
                    continue;
                }

                if (tileItem.hasBehaviour(ItemBehaviour.ROLLER)) {
                    if (this.hasBehaviour(ItemBehaviour.ROLLER)) {
                        return false; // Can't place rollers on top of rollers
                    }

                    if (this.definition.getLength() > 1 || this.definition.getWidth() > 1) {
                        return false; // Item is too big to place on rollers.
                    }
                }
            }
        }

        return true;
    }

    /**
     * Get the room tile this item is on.
     *
     * @return the room tile, else null
     */
    public RoomTile getTile() {
        Room room = this.getRoom();

        if (room != null) {
            return this.getRoom().getMapping().getTile(this.position);
        }

        return null;
    }

    /**
     * Get if the item has a type of behaviour.
     *
     * @param behaviour the behaviour to check
     * @return true, if successful
     */
    public boolean hasBehaviour(ItemBehaviour behaviour) {
        return this.definition.hasBehaviour(behaviour);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public ItemDefinition getDefinition() {
        return this.definition;
    }

    public void setDefinitionId(int definitionId) {
        this.definition = ItemManager.getInstance().getDefinition(definitionId);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getWallPosition() {
        return wallPosition;
    }

    public void setWallPosition(String wallPosition) {
        this.wallPosition = wallPosition;
    }

    public String getCurrentProgram() {
        return currentProgram;
    }

    public void setCurrentProgram(String currentProgram) {
        this.currentProgram = currentProgram;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public String getCurrentProgramValue() {
        return currentProgramValue;
    }

    public void setCurrentProgramValue(String currentProgramValue) {
        this.currentProgramValue = currentProgramValue;
    }

    public Room getRoom() {
        return RoomManager.getInstance().getRoomById(this.roomId);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Item getItemBelow() {
        return itemBelow;
    }

    public void setItemBelow(Item itemBelow) {
        this.itemBelow = itemBelow;
    }

    public boolean getRequiresUpdate() {
        return requiresUpdate;
    }

    public void setRequiresUpdate(boolean requiresUpdate) {
        this.requiresUpdate = requiresUpdate;
    }
}

