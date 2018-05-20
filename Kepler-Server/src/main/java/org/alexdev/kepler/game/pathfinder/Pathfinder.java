package org.alexdev.kepler.game.pathfinder;

import java.util.Collections;
import java.util.LinkedList;

import org.alexdev.kepler.game.entity.Entity;

public class Pathfinder {

    public static final Position[] DIAGONAL_MOVE_POINTS = new Position[]{
            new Position(0, -1, 0),
            new Position(0, 1, 0),
            new Position(1, 0, 0),
            new Position(-1, 0, 0),
            new Position(1, -1, 0),
            new Position(-1, 1, 0),
            new Position(1, 1, 0),
            new Position(-1, -1, 0)
    };

    private static final Position[] MOVE_POINTS = new Position[]{
            new Position(0, -1),
            new Position(1, 0),
            new Position(0, 1),
            new Position(-1, 0)
    };
    
    /**
     * Make path.
     *
     * @param entity the entity
     * @return the linked list
     */
    public static LinkedList<Position> makePath(Entity entity) {
        /*int X = entity.getRoomUser().getGoal().getX();
        int Y = entity.getRoomUser().getGoal().getY();
        return makePath(entity, X, Y);*/
        return null;
    }

    /**
     * Make path with specified last coordinates
     *
     * @param entity the entity to move
     * @param X the xcoord to move from
     * @param Y the y coord to move from
     * @return the linked list
     */
    public static LinkedList<Position> makePath(Entity entity, int X, int Y) {
        /* (entity.getRoom().getModelName().isOutsideBounds(X, Y)) {
            return new LinkedList<>();
        }

        if (entity.getRoom().getModelName().isBlocked(X, Y)) {
            return new LinkedList<>();
        }

        if (!entity.getRoom().getMapping().isTileWalkable(X, Y, entity)) {
            return new LinkedList<>();
        }

        if (entity.getRoomUser().getPosition().equals(new Position(X, Y))) {
            return new LinkedList<>();
        }*/

        LinkedList<Position> squares = new LinkedList<>();

        PathfinderNode nodes = makePathReversed(entity, X, Y);

        if (nodes != null) {
            while (nodes.getNextNode() != null) {
                squares.add(new Position(nodes.getPosition().getX(), nodes.getPosition().getY()));
                nodes = nodes.getNextNode();
            }
        }

        Collections.reverse(squares);
        return squares;

    }

    /**
     * Make path reversed.
     *
     * @param entity the entity
     * @return the pathfinder node
     */
    private static PathfinderNode makePathReversed(Entity entity, int X, int Y) {
        LinkedList<PathfinderNode> openList = new LinkedList<>();

        PathfinderNode[][] map = null;//new PathfinderNode[entity.getRoom().getModelName().getMapSizeX()][entity.getRoom().getModelName().getMapSizeY()];
        PathfinderNode node;
        Position tmp;

        int cost;
        int diff;

        PathfinderNode current = new PathfinderNode(null);//entity.getRoomUser().getPosition());
        current.setCost(0);

        Position end = new Position(X, Y);
        PathfinderNode finish = new PathfinderNode(end);

        map[current.getPosition().getX()][current.getPosition().getY()] = current;
        openList.add(current);

        Position[] POINTS = DIAGONAL_MOVE_POINTS;

        while (openList.size() > 0) {
            current = openList.pollFirst();
            current.setInClosed(true);

            for (int i = 0; i < POINTS.length; i++) {
                tmp = current.getPosition().add(POINTS[i]);

                boolean isFinalMove = (tmp.getX() == end.getX() && tmp.getY() == end.getY());

                if (false) {//if (entity.getRoomUser().getRoom().getMapping().isValidStep(entity, new Position(current.getPosition().getX(), current.getPosition().getY(), current.getPosition().getZ()), tmp, isFinalMove)) {
                    if (map[tmp.getX()][tmp.getY()] == null) {
                        node = new PathfinderNode(tmp);
                        map[tmp.getX()][tmp.getY()] = node;
                    } else {
                        node = map[tmp.getX()][tmp.getY()];
                    }

                    if (!node.isInClosed()) {
                        diff = 0;

                        if (current.getPosition().getX() != node.getPosition().getX()) {
                            diff += 1;
                        }

                        if (current.getPosition().getY() != node.getPosition().getY()) {
                            diff += 1;
                        }

                        cost = current.getCost() + diff + node.getPosition().getDistanceSquared(end);

                        if (cost < node.getCost()) {
                            node.setCost(cost);
                            node.setNextNode(current);
                        }

                        if (!node.isInOpen()) {
                            if (node.getPosition().getX() == finish.getPosition().getX() && node.getPosition().getY() == finish.getPosition().getY()) {
                                node.setNextNode(current);
                                return node;
                            }

                            node.setInOpen(true);
                            openList.add(node);
                        }
                    }
                }
            }
        }

        return null;
    }
}