package org.alexdev.kepler.game.item;

import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.server.netty.streams.NettyResponse;
import org.alexdev.kepler.util.StringUtil;

public class Item {
    private int id;
    private ItemDefinition definition;
    private Position position;
    private boolean hasExtraParameter;
    private String currentProgram;

    private String customData;

    public Item() {
        this.id = 0;
        this.definition = new ItemDefinition();
        this.position = new Position();
        this.customData = "";
    }

    public void serialise(NettyResponse response) {
        if (!this.definition.getBehaviour().isPublicSpaceObject()) {
            response.writeString(this.id);
            response.writeString(this.definition.getSprite());
            response.writeInt(this.position.getX());
            response.writeInt(this.position.getY());
            response.writeInt(this.definition.getLength());
            response.writeInt(this.definition.getWidth());
            response.writeInt(this.position.getRotation());
            response.writeString(StringUtil.format(this.position.getZ()));
            response.writeString("");
            response.writeInt(0);
            response.writeString(this.customData);
        } else {
            response.writeDelimeter(this.customData, ' ');
            response.writeString(this.definition.getSprite());
            response.writeDelimeter(this.position.getX(), ' ');
            response.writeDelimeter(this.position.getY(), ' ');
            response.writeDelimeter((int) this.position.getZ(), ' ');
            response.write(this.position.getRotation());

            if (this.hasExtraParameter) {
                response.write(" 2");
            }

            response.write(Character.toString((char) 13));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ItemDefinition getDefinition() {
        return definition;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasExtraParameter() {
        return hasExtraParameter;
    }

    public void setHasExtraParameter(boolean hasExtraParameter) {
        this.hasExtraParameter = hasExtraParameter;
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
}

