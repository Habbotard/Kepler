package org.alexdev.kepler.game.room.public_rooms.walkways;

import org.alexdev.kepler.game.pathfinder.Position;

import java.util.List;

public class WalkwaysEntrance {
    private String modelFrom;
    private String modelTo;
    private List<Position> fromCoords;
    private boolean hideRoom;
    private Position destination;

    public WalkwaysEntrance(String modelFrom, String modelTo, List<Position> fromCoords, boolean hideFrom, Position destination) {
        this.modelFrom = modelFrom;
        this.modelTo = modelTo;
        this.fromCoords = fromCoords;
        this.hideRoom = hideFrom;
        this.destination = destination;
    }

    public String getModelFrom() {
        return this.modelFrom;
    }

    public String getModelTo() {
        return this.modelTo;
    }

    public List<Position> getFromCoords() {
        return this.fromCoords;
    }

    public boolean isHideRoom() {
        return this.hideRoom;
    }

    public Position getDestination() {
        return this.destination;
    }
}
