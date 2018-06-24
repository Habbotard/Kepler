package org.alexdev.kepler.messages.incoming.rooms.dimmer;

import org.alexdev.kepler.dao.mysql.ItemDao;
import org.alexdev.kepler.dao.mysql.MoodlightDao;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class MSG_ROOMDIMMER_SET_PRESET implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        Room room = player.getRoomUser().getRoom();
        Item item = room.getItemManager().getMoodlight();

        if (item == null) {
            return;
        }

        if (!MoodlightDao.containsPreset(item.getId())) {
            MoodlightDao.createPresets(item.getId());
        }

        int presetId = reader.readInt();
        int backgroundState = reader.readInt();
        String presetColour = reader.readString();
        int presetStrength = reader.readInt();

        if (presetId > 3 || presetId < 1 || backgroundState > 2 || backgroundState < 1 ||
                (presetColour.equals("#74F5F5") &&
                        presetColour.equals("#0053F7") &&
                        presetColour.equals("#E759DE") &&
                        presetColour.equals("#EA4532") &&
                        presetColour.equals("#F2F851") &&
                        presetColour.equals("#82F349") &&
                        presetColour.equals("#000000")
                        || presetStrength > 255 || presetStrength < 77)) {
            return; // Nope, no scripting room dimmers allowed here!
        }

        Pair<Integer, ArrayList<String>> presetData = MoodlightDao.getPresets(item.getId());
        List<String> presets = presetData.getRight();

        presets.set(presetId - 1, backgroundState + "," + presetColour + "," + presetStrength);
        item.setCustomData("2," + presetId + "," + backgroundState + "," + presetColour + "," + presetStrength);
        item.updateStatus();

        ItemDao.updateItem(item);
        MoodlightDao.updatePresets(item.getId(), presetId, presets);
    }
}