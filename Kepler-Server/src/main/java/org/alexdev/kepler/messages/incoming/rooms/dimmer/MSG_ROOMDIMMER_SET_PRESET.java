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

        System.out.println("preset id: " + presetId);

        Pair<Integer, ArrayList<String>> presetData = MoodlightDao.getPresets(item.getId());

        int currentPreset = presetData.getLeft();
        List<String> presets = presetData.getRight();

        presets.set(presetId - 1, backgroundState + "," + presetColour + "," + presetStrength);
        item.setCustomData("2," + presetId + "," + backgroundState + "," + presetColour + "," + presetStrength);
        item.updateStatus();

        ItemDao.updateItem(item);
        MoodlightDao.updatePresets(item.getId(), presetId, presets);
    }
}