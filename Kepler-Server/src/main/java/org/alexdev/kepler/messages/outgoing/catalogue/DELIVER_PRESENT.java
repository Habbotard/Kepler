package org.alexdev.kepler.messages.outgoing.catalogue;

import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.base.ItemBehaviour;
import org.alexdev.kepler.messages.types.MessageComposer;
import org.alexdev.kepler.server.netty.streams.NettyResponse;

public class DELIVER_PRESENT extends MessageComposer {
    private final Item present;

    public DELIVER_PRESENT(Item present) {
        this.present = present;
    }

    @Override
    public void compose(NettyResponse response) {
        response.writeDelimeter(this.present.getDefinition().getSprite(), (char)13);
        response.writeDelimeter(this.present.getDefinition().getSprite(), (char)13);

        if (!this.present.getDefinition().hasBehaviour(ItemBehaviour.WALL_ITEM)) {
            response.writeDelimeter(this.present.getDefinition().getLength(), (char)30);
            response.writeDelimeter(this.present.getDefinition().getWidth(), (char)30);
            response.writeDelimeter(this.present.getDefinition().getColour(), (char)30);
        }
    }

    @Override
    public short getHeader() {
        return 129; // "BA"
    }
}
