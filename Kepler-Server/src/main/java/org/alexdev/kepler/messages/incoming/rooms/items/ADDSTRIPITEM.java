package org.alexdev.kepler.messages.incoming.rooms.items;

import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class ADDSTRIPITEM implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        Room room = player.getRoomUser().getRoom();

        if (room == null) {
            return;
        }

        if (!room.isOwner(player.getDetails().getId())) {
            return;
        }

        String content = reader.contents();
        String[] data = content.split(" ");

        int itemId = Integer.parseInt(data[2]);

        Item item = room.getItemManager().getById(itemId);

        if (item == null) {
            return;
        }

        room.getMapping().removeItem(item);

        player.getInventory().getItems().add(item);
        player.getInventory().getView("last");
    }
}
