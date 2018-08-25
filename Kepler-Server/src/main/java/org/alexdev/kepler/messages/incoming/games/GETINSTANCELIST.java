package org.alexdev.kepler.messages.incoming.games;

import com.github.bhlangonijr.chesslib.game.Game;
import org.alexdev.kepler.game.games.GameManager;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.models.RoomModelTriggerType;
import org.alexdev.kepler.game.triggers.GameLobbyTrigger;
import org.alexdev.kepler.messages.outgoing.games.INSTANCELIST;
import org.alexdev.kepler.messages.outgoing.games.JOINFAILED;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;

public class GETINSTANCELIST implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws Exception {
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        Room room = player.getRoomUser().getRoom();

        if (!(room.getModel().getModelTrigger() instanceof GameLobbyTrigger)) {
            return;
        }

        GameLobbyTrigger gameLobbyTrigger = (GameLobbyTrigger) room.getModel().getModelTrigger();
        player.send(new INSTANCELIST(GameManager.getInstance().getGamesByType(gameLobbyTrigger.getGameType())));
    }
}
