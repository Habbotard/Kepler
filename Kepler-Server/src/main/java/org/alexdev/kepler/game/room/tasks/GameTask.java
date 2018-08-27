package org.alexdev.kepler.game.room.tasks;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.games.Game;
import org.alexdev.kepler.game.games.battleball.BattleballTileColour;
import org.alexdev.kepler.game.games.battleball.BattleballTileState;
import org.alexdev.kepler.game.games.player.GamePlayer;
import org.alexdev.kepler.game.games.player.GameTeam;
import org.alexdev.kepler.game.pathfinder.Position;
import org.alexdev.kepler.game.pathfinder.Rotation;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.entities.RoomEntity;
import org.alexdev.kepler.game.room.enums.StatusType;
import org.alexdev.kepler.game.room.mapping.RoomTile;
import org.alexdev.kepler.log.Log;
import org.alexdev.kepler.messages.outgoing.games.GAMESTATUS;
import org.alexdev.kepler.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTask implements Runnable {
    private final Room room;
    private final Game game;

    public GameTask(Room room, Game game) {
        this.room = room;
        this.game = game;
    }

    @Override
    public void run() {
        try {
            if (this.room.getEntities().isEmpty()) {
                return;
            }

            List<GamePlayer> players = new ArrayList<>();
            List<Position> updateTiles = new ArrayList<>();

            Map<GamePlayer, Position> movingPlayers = new HashMap<>();

            for (GameTeam gameTeam : this.game.getTeamPlayers().values()) {
                for (GamePlayer gamePlayer : gameTeam.getActivePlayers()) {
                    Player player = gamePlayer.getPlayer();

                    if (player != null
                            && player.getRoomUser().getRoom() != null
                            && player.getRoomUser().getRoom() == this.room) {

                        this.processEntity(gamePlayer, movingPlayers, updateTiles);
                        RoomEntity roomEntity = player.getRoomUser();

                        players.add(gamePlayer);

                        if (roomEntity.isNeedsUpdate()) {
                            roomEntity.setNeedsUpdate(false);
                        }
                    }
                }
            }

            this.game.send(new GAMESTATUS(this.game, this.game.getTeamPlayers().values(), players, movingPlayers, updateTiles));
        } catch (Exception ex) {
            Log.getErrorLogger().error("GameTask crashed: ", ex);
        }
    }

    /**
     * Process entity.
     */
    private void processEntity(GamePlayer gamePlayer, Map<GamePlayer, Position> movingPlayers, List<Position> updateTiles) {
        Entity entity = (Entity) gamePlayer.getPlayer();
        Game game = gamePlayer.getGame();

        RoomEntity roomEntity = entity.getRoomUser();

        Position position = roomEntity.getPosition();
        Position goal = roomEntity.getGoal();

        if (roomEntity.isWalking()) {
            // Apply next tile from the tile we removed from the list the cycle before
            if (roomEntity.getNextPosition() != null) {
                roomEntity.getPosition().setX(roomEntity.getNextPosition().getX());
                roomEntity.getPosition().setY(roomEntity.getNextPosition().getY());
                roomEntity.updateNewHeight(roomEntity.getPosition());

                // Increment tiles...
                this.incrementTile(gamePlayer, roomEntity.getNextPosition(), updateTiles);
            }

            // We still have more tiles left, so lets continue moving
            if (roomEntity.getPath().size() > 0) {
                Position next = roomEntity.getPath().pop();

                // Tile was invalid after we started walking, so lets try again!
                if (!RoomTile.isValidTile(this.room, entity, next)) {
                    entity.getRoomUser().getPath().clear();
                    roomEntity.walkTo(goal.getX(), goal.getY());
                    this.processEntity(gamePlayer, movingPlayers, updateTiles);
                    return;
                }

                RoomTile previousTile = roomEntity.getTile();
                previousTile.removeEntity(entity);

                RoomTile nextTile = roomEntity.getRoom().getMapping().getTile(next);
                nextTile.addEntity(entity);

                roomEntity.removeStatus(StatusType.LAY);
                roomEntity.removeStatus(StatusType.SIT);

                int rotation = Rotation.calculateWalkDirection(position.getX(), position.getY(), next.getX(), next.getY());
                double height = this.room.getMapping().getTile(next).getWalkingHeight();

                roomEntity.getPosition().setRotation(rotation);
                roomEntity.setStatus(StatusType.MOVE, next.getX() + "," + next.getY() + "," + StringUtil.format(height));
                roomEntity.setNextPosition(next);

                // Add next position if moving
                movingPlayers.put(gamePlayer, roomEntity.getNextPosition().copy());
            } else {
                roomEntity.stopWalking();
            }

            // If we're walking, make sure to tell the server
            roomEntity.setNeedsUpdate(true);
        }
    }

    /***
     * Increment the tile when the user steps on it
     *
     * @param gamePlayer the game player incrementing the tile
     * @param position the position of the tile
     * @param updateTiles the list for the tiles to get updated
     */
    private void incrementTile(GamePlayer gamePlayer, Position position, List<Position> updateTiles) {
        if (!gamePlayer.getGame().getTileMap().isGameTile(position.getX(), position.getY())) {
            return;
        }

        BattleballTileState state = this.game.getBattleballTileStates()[position.getX()][position.getY()];
        BattleballTileColour colour = this.game.getBattleballTileColours()[position.getX()][position.getY()];

        if (colour == BattleballTileColour.DISABLED) {
            return;
        }

        if (state != BattleballTileState.SEALED) {
            if (colour.getTileColourId() == gamePlayer.getTeamId()) {
                this.game.getBattleballTileStates()[position.getX()][position.getY()] = BattleballTileState.getStateById(state.getTileStateId() + 1);
            } else {
                this.game.getBattleballTileStates()[position.getX()][position.getY()] = BattleballTileState.TOUCHED;
                this.game.getBattleballTileColours()[position.getX()][position.getY()] = BattleballTileColour.getColourById(gamePlayer.getTeamId());
            }

            BattleballTileState newState = this.game.getBattleballTileStates()[position.getX()][position.getY()];
            BattleballTileColour newColour = this.game.getBattleballTileColours()[position.getX()][position.getY()];

            int newPoints = -1;
            boolean giveEveryonePoints = false;

            if (state != newState && newState == BattleballTileState.TOUCHED) {
                newPoints = 2;

                if (colour != newColour) {
                    newPoints = 4;
                }
            }

            if (state != newState && newState == BattleballTileState.CLICKED) {
                newPoints = 6;

                if (colour != newColour) {
                    newPoints = 8;
                }
            }

            if (state != newState && newState == BattleballTileState.PRESSED) {
                newPoints = 10;

                if (colour != newColour) {
                    newPoints = 12;
                }
            }

            if (state != newState && newState == BattleballTileState.SEALED) {
                newPoints = 14;
                giveEveryonePoints = true;
            }

            if (newPoints != -1) {
                if (!giveEveryonePoints) {
                    gamePlayer.setScore(gamePlayer.getScore() + newPoints);
                } else {
                    for (GameTeam gameTeam : this.game.getTeamPlayers().values()) {
                        for (GamePlayer p : gameTeam.getActivePlayers()) {
                            p.setScore(gamePlayer.getScore() + newPoints);
                        }
                    }
                }

                System.out.println("ayyy lmao");

                GameTeam team = this.game.getTeamPlayers().get(gamePlayer.getTeamId());
                //team.setScore(team.getScore() + 1);
            }

            updateTiles.add(position.copy());
        }
    }
}
