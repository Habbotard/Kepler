package org.alexdev.kepler.messages.incoming.rooms.user;

import io.netty.util.internal.ThreadLocalRandom;
import org.alexdev.kepler.game.commands.CommandManager;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.messages.outgoing.rooms.user.CHAT_MESSAGE;
import org.alexdev.kepler.messages.outgoing.rooms.user.TYPING_STATUS;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.StringUtil;

import java.util.concurrent.ConcurrentHashMap;

public class CHAT implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) {
        Room room = player.getRoom();

        if (room == null) {
            return;
        }

        String message = StringUtil.filterInput(reader.readString(), true);

        player.getRoomUser().setTyping(false);
        room.send(new TYPING_STATUS(player.getRoomUser().getInstanceId(), player.getRoomUser().isTyping()));

        if (CommandManager.getInstance().hasCommand(player, message)) {
            CommandManager.getInstance().invokeCommand(player, message);
            return;
        }

        player.getRoomUser().showChat(message, false);
        player.getRoomUser().resetRoomTimer();

        // Make chat hard to read for long distance in public rooms
        if (room.isPublicRoom()) {
            int sourceX = player.getRoomUser().getPosition().getX();
            int sourceY = player.getRoomUser().getPosition().getY();

            for (Player roomPlayer : room.getEntityManager().getPlayers()) {
                int distX = Math.abs(sourceX - roomPlayer.getRoomUser().getPosition().getX()) - 1;
                int distY = Math.abs(sourceY - roomPlayer.getRoomUser().getPosition().getY()) - 1;

                if (distX < 9 && distY < 9) {// User can hear
                    if (distX <= 6 && distY <= 6) {// User can hear full message
                        roomPlayer.send(new CHAT_MESSAGE(CHAT_MESSAGE.type.CHAT, player.getRoomUser().getInstanceId(), message));
                    } else {
                        int garbleIntensity = distX;

                        if (distY < distX) {
                            garbleIntensity = distY;
                        }

                        garbleIntensity -= 4;
                        char[] garbleMessage = message.toCharArray();

                        for (int pos = 0; pos < garbleMessage.length; pos++) {
                            int intensity = ThreadLocalRandom.current().nextInt(garbleIntensity, 6);

                            if (intensity > 3 &&
                                    garbleMessage[pos] != ' ' &&
                                    garbleMessage[pos] != ',' &&
                                    garbleMessage[pos] != '?' &&
                                    garbleMessage[pos] != '!') {
                                garbleMessage[pos] = '.';
                            }
                        }

                        roomPlayer.send(new CHAT_MESSAGE(CHAT_MESSAGE.type.CHAT, player.getRoomUser().getInstanceId(), new String(garbleMessage)));
                    }
                }
            }
        } else {
            room.send(new CHAT_MESSAGE(CHAT_MESSAGE.type.CHAT, player.getRoomUser().getInstanceId(), message));
        }
    }
}
