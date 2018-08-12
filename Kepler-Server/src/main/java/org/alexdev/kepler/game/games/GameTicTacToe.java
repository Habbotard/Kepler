package org.alexdev.kepler.game.games;

import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.triggers.GameTrigger;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.messages.outgoing.rooms.games.ITEMMSG;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameTicTacToe extends GamehallGame {
    private static class GameToken {
        private char token;
        private char winningToken;

        private GameToken(char token, char winningToken) {
            this.token = token;
            this.winningToken = winningToken;
        }

        private char getToken() {
            return token;
        }

        private char getWinningToken() {
            return winningToken;
        }
    }

    private static final int MAX_WIDTH = 23;
    private static final int MAX_LENGTH = 24;

    private static GameToken[] gameTokens = new GameToken[]{
            new GameToken('O', 'q'),
            new GameToken('X', '+')
    };


    private List<Player> playersInGame;
    private Map<Player, GameToken> playerSides;
    private char[][] gameMap;

    public GameTicTacToe(int roomId, List<int[]> chairs) {
        super(roomId, chairs);
    }

    @Override
    public void gameStart() {
        this.playersInGame = new ArrayList<>();
        this.playerSides = new HashMap<>();
        this.restartMap();
    }

    @Override
    public void gameStop() {
        this.playersInGame.clear();
        this.playerSides.clear();
        this.gameMap = null;
    }

    @Override
    public void handleCommand(Player player, Room room, Item item, String command, String[] args) {
        GameTrigger trigger = (GameTrigger) item.getItemTrigger();

        if (command.equals("CLOSE")) {
            trigger.onEntityLeave(player, player.getRoomUser(), item);
            return;
        }

        if (command.equals("CHOOSETYPE")) {
            char sideChosen = args[0].charAt(0);
            GameToken token = null;

            for (GameToken side : gameTokens) {
                if (side.getToken() == sideChosen) {
                    token = side;
                }
            }

            if (token == null) {
                return;
            }

            if (getPlayerBySide(sideChosen) != null) {
                this.sendToEveryone(new ITEMMSG(new String[]{this.getGameId(), "TYPERESERVED"}));
                return;
            }

            this.playerSides.put(player, token);
            this.playersInGame.add(player);

            String[] playerNames = this.getPlayerNames();

            player.send(new ITEMMSG(new String[]{this.getGameId(), "SELECTTYPE " + String.valueOf(token.getToken())}));
            this.sendToEveryone(new ITEMMSG(new String[]{this.getGameId(), "OPPONENTS", playerNames[0], playerNames[1]}));
        }

        if (command.equals("RESTART")) {
            this.restartMap();
            this.broadcastMap();
            return;
        }

        if (command.equals("SETSECTOR")) {
            if (this.playersInGame.size() < this.getMinimumPeopleRequired()) {
                return; // Can't place objects until other player has joined.
            }

            if (!this.playerSides.containsKey(player)) {
                return;
            }

            char side = args[0].charAt(0);

            if (this.playerSides.get(player).getToken() != side) {
                return;
            }

            int Y = Integer.parseInt(args[1]);
            int X = Integer.parseInt(args[2]);

            if (X >= MAX_WIDTH || Y >= MAX_LENGTH) {
                return;
            }

            if (this.gameMap == null) {
                return;
            }

            this.gameMap[X][Y] = this.playerSides.get(player).getToken();
            this.broadcastMap();
        }
    }

    /**
     * Reset the game map.
     */
    private void restartMap() {
        this.gameMap = new char[MAX_WIDTH][MAX_LENGTH];

        for (int X = 0; X < MAX_WIDTH; X++) {
            for (int Y = 0; Y < MAX_LENGTH; Y++) {
                this.gameMap[X][Y] = '0';
            }
        }
    }

    /**
     * Send the game map to the opponents.
     */
    public void broadcastMap() {
        StringBuilder boardData = new StringBuilder();

        for (char[] mapData : this.gameMap) {
            for (char mapLetter : mapData) {
                boardData.append(mapLetter);
            }

            boardData.append((char)13);
        }

        String[] playerNames = this.getPlayerNames();
        this.sendToEveryone(new ITEMMSG(new String[]{this.getGameId(), "BOARDDATA", playerNames[0], playerNames[1], boardData.toString()}));
    }

    /**
     * Get the names of the people currently playing, always returns an array with
     * a length of two, if the name is blank there's no player.
     *
     * @return the player names
     */
    private String[] getPlayerNames() {
        String[] playerNames = new String[]{"", ""};

        for (int i = 0; i < this.playersInGame.size(); i++) {
            Player player = this.playersInGame.get(i);
            playerNames[i] = player.getDetails().getName() + " " + this.playerSides.get(player).getToken();
        }

        return playerNames;
    }

    /**
     * Locate a player instance by the side they're playing.
     *
     * @param side the side used
     * @return the player instance, if successful
     */
    public Player getPlayerBySide(char side) {
        for (var kvp : this.playerSides.entrySet()) {
            if (kvp.getValue().getToken() == side) {
                return kvp.getKey();
            }
        }

        return null;
    }

    @Override
    public int getMaximumPeopleRequired() {
        return 2;
    }

    @Override
    public int getMinimumPeopleRequired() {
        return 2;
    }

    @Override
    public String getGameFuseType() {
        return "TicTacToe";
    }
}
