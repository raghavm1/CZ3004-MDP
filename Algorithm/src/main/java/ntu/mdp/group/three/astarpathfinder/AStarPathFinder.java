package ntu.mdp.group.three.astarpathfinder;

import ntu.mdp.group.three.config.ArenaConfig;
import ntu.mdp.group.three.config.Directions;
import ntu.mdp.group.three.config.Position;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.robot.Robot;

import java.util.Arrays;

/**
 * Implementation done with reference from:
 * https://en.wikipedia.org/wiki/A*_search_algorithm
 */
public class AStarPathFinder {
    boolean first = true;
    int direction = -1;
    boolean firstPenalty = true;

    public AStarPathFinder() { }

    public int[] start(Robot robot, int[] startingPosition, int[] goalPosition, boolean onGrid) {
        Node startNode = new Node(startingPosition);
        Node currentNode;
        Node[] open = {startNode};
        Node[] closed = {};

        while (true) {
            currentNode = checkLowestCost(open);

            if (currentNode == null) {
                System.out.println("Error: open is empty");
                break;
            } else {
                open = removeNode(open, currentNode);
                closed = addNode(closed, currentNode);


                if ((!onGrid && canReach(currentNode.getPosition(), goalPosition, first)) ||
                        (onGrid && Arrays.equals(currentNode.getPosition(), goalPosition))) {
                    System.out.println("Path found!");
                    break;
                }

                open = addNeighbours(robot, open, currentNode, goalPosition);

                if (Arrays.equals(open, new Node[]{})) {
                    setFirst(false);
                    System.out.println("Error: No possible path");
                    return null;
                }
            }
        }

        int[] path = getPath(currentNode);
        System.out.println(Arrays.toString(path));
        updateDirection(path);
        System.out.println("Path Found");
        return path;
    }

    private void updateDirection(int[] path) {
        if (path != null) {
            for (int value : path) {
                switch (value) {
                    case Directions.LEFT:
                        direction = (direction + 3) % 4;
                        break;
                    case Directions.RIGHT:
                        direction = (direction + 1) % 4;
                        break;
                    case Directions.BACKWARD:
                        direction = (direction + 2) % 4;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean canReach(int[] currentNodePosition, int[] endPosition, boolean first) {
        int x = endPosition[0];
        int y = endPosition[1];
        int[][] position;

        if (first) {
            position = new int[][] {
                    {x - 1, y - 2}, {x, y - 2}, {x + 1, y - 2},
                    {x + 2, y - 1}, {x + 2, y}, {x + 2, y + 1},
                    {x + 1, y + 2}, {x, y + 2}, {x - 1, y + 2},
                    {x - 2, y + 1}, {x - 2, y}, {x - 2, y - 1}};
        } else {
            position = new int[][] {
                    {x - 1, y - 3}, {x, y - 3}, {x + 1, y - 3},
                    {x + 3, y - 1}, {x + 3, y}, {x + 3, y + 1},
                    {x + 1, y + 3}, {x, y + 3}, {x - 1, y + 3},
                    {x - 3, y + 1}, {x - 3, y}, {x - 3, y - 1}
            };
        }

        for (int[] coordinates : position) {
            if (Arrays.equals(currentNodePosition, coordinates)) {
                return true;
            }
        }
        return false;
    }

    private Node checkLowestCost(Node[] list) {
        int cost;
        if (list.length > 0) {
            int lowestCost = list[0].getCost();
            Node lowestNode = list[0];

            for (Node node : list) {
                cost = node.getCost();
                if (cost <= lowestCost) {
                    lowestCost = cost;
                    lowestNode = node;
                }
            }
            return lowestNode;
        } else return null;
    }

    private Node[] removeNode(Node[] list, Node node) {
        int index = -1;
        if (list.length < 2) return new Node[] {};
        Node[] new_list = new Node[list.length-1];
        for (int i = 0; i < list.length; i++) {
            if (list[i] == node) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            System.arraycopy(list, 0, new_list, 0, index);
            if (new_list.length - index >= 0)
                System.arraycopy(list, index + 1, new_list, index, new_list.length - index);
        }
        return new_list;
    }

    private Node[] addNode(Node[] list, Node node) {
        Node[] newList = new Node[list.length+1];
        System.arraycopy(list, 0, newList, 0, list.length);
        newList[list.length] = node;
        return newList;
    }

    private Node[] addNeighbours(Robot robot, Node[] open, Node currentNode, int[] goalPosition) {
        Node[] neighbours = new Node[4];
        int count = 0;
        int x = currentNode.getPosition()[Position.X];
        int y = currentNode.getPosition()[Position.Y];
        int[][] neighboursPosition = {
                {x, y + 1}, {x - 1, y}, {x + 1, y}, {x, y - 1}
        };

        for (int i = 0; i < 4; i++) {
            if (isValid(robot, neighboursPosition[i])) {
                Node neighbour = new Node(neighboursPosition[i]);
                neighbour.setParent(currentNode);
                neighbour.setCost(findCost(neighbour, goalPosition));
                neighbours[count] = neighbour;
                count++;
            }
        }

        for (int j = 0; j < count; j++) {
            Node node = neighbours[j];
            open = addNode(open, node);
        }

        return open;
    }

    public boolean isValid(Robot robot, int[] position) {
        if (position == null) return false;

        ArenaMap arenaMap = robot.getMap();
        int x = position[Position.X];
        int y = position[Position.Y];
        int[][] robot_pos = {
                {x - 1, y + 1}, {x, y + 1}, {x + 1, y + 1},
                {x - 1, y}, {x, y}, {x + 1, y},
                {x - 1, y - 1}, {x, y - 1}, {x + 1, y - 1}
        };

        if ((0 < x) && (x < ArenaConfig.ARENA_WIDTH - 1) && (0 < y) && (y < ArenaConfig.ARENA_HEIGHT - 1)) {
            for (int[] coordinates : robot_pos) {
                if (arenaMap.getGrid(coordinates[0], coordinates[1]).equals("Obstacle")) return false;
            }
            return true;
        } else return false;
    }

    private int findCost(Node node, int[] goalPosition) {
        node.setH(findHCost(node, goalPosition));
        node.setG(findGCost(node));
        node.updateCost();
        return node.getCost();
    }

    private int findHCost(Node currentNode, int[] goalPosition) {
        int x = Math.abs(currentNode.position[Position.X] - goalPosition[Position.X]);
        int y = Math.abs(currentNode.position[Position.Y] - goalPosition [Position.Y]);

        if (currentNode.getParent().position[Position.X] == currentNode.position[Position.X] && x == 0) return y;
        else if (currentNode.getParent().position[Position.Y] == currentNode.position[Position.Y] && y == 0) return x;
        else return x + y;
    }

    private int findGCost(Node currentNode) {
        Node previousNode = currentNode.getParent();

        if (previousNode == null) return 0;
        else if ((!firstPenalty) && (previousNode.getParent() == null)) return previousNode.getG() + 1;
        else {
            int direction = goWhere(currentNode);
            if (direction == Directions.FORWARD) return previousNode.getG() + 1;
            else if ((direction == Directions.LEFT) || (direction == Directions.RIGHT)) return previousNode.getG() + 3;
            else return previousNode.getG() + 5;
        }
    }

    private int goWhere(Node currentNode) {
        Node second = currentNode.getParent();
        if (second == null) return -1;
        Node first = second.getParent();
        int[] firstNodePosition,  secondNodePosition;

        if (first == null) {
            secondNodePosition = second.getPosition();
            if (secondNodePosition[Position.X] == currentNode.getPosition()[Position.X]) {
                if (secondNodePosition[Position.Y] > currentNode.getPosition()[Position.Y]) {
                    switch (direction) {
                        case Directions.NORTH:
                            return Directions.FORWARD;
                        case Directions.EAST:
                            return Directions.LEFT;
                        case Directions.SOUTH:
                            return Directions.BACKWARD;
                        case Directions.WEST:
                            return Directions.RIGHT;
                    }
                }
                else if (secondNodePosition[Position.Y] < currentNode.getPosition()[Position.Y]) {
                    switch (direction) {
                        case Directions.NORTH:
                            return Directions.BACKWARD;
                        case Directions.EAST:
                            return Directions.RIGHT;
                        case Directions.SOUTH:
                            return Directions.FORWARD;
                        case Directions.WEST:
                            return Directions.LEFT;
                    }
                }
            }
            else if (secondNodePosition[Position.Y] == currentNode.getPosition()[Position.Y]) {
                if (secondNodePosition[Position.X] > currentNode.getPosition()[Position.X]) {
                    switch (direction) {
                        case Directions.NORTH:
                            return Directions.LEFT;
                        case Directions.EAST:
                            return Directions.BACKWARD;
                        case Directions.SOUTH:
                            return Directions.RIGHT;
                        case Directions.WEST:
                            return Directions.FORWARD;
                    }
                } else {
                    switch (direction) {
                        case Directions.NORTH:
                            return Directions.RIGHT;
                        case Directions.EAST:
                            return Directions.FORWARD;
                        case Directions.SOUTH:
                            return Directions.LEFT;
                        case Directions.WEST:
                            return Directions.BACKWARD;
                    }
                }
            }
        }
        else {
            firstNodePosition = first.getPosition();
            secondNodePosition = second.getPosition();
            if ((firstNodePosition[Position.X] == secondNodePosition[Position.X]) && (secondNodePosition[Position.X] == currentNode.getPosition()[Position.X])) {
                if (((firstNodePosition[Position.Y] > secondNodePosition[Position.Y]) && (secondNodePosition[Position.Y] > currentNode.getPosition()[Position.Y])) ||
                        ((firstNodePosition[Position.Y] < secondNodePosition[Position.Y]) && (secondNodePosition[Position.Y] < currentNode.getPosition()[Position.Y]))) {
                    return Directions.FORWARD;
                }
                else return Directions.BACKWARD;
            }
            else if ((firstNodePosition[Position.Y] == secondNodePosition[Position.Y]) && (secondNodePosition[Position.Y] == currentNode.getPosition()[Position.Y])) {
                if (((firstNodePosition[Position.X] > secondNodePosition[Position.X]) && (secondNodePosition[Position.X] > currentNode.getPosition()[Position.X])) ||
                        ((firstNodePosition[Position.X] < secondNodePosition[Position.X]) && (secondNodePosition[Position.X] < currentNode.getPosition()[Position.X]))) {
                    return Directions.FORWARD;
                }
                else return Directions.BACKWARD;
            }
            else if (firstNodePosition[Position.X] == secondNodePosition[Position.X]) {
                if (firstNodePosition[Position.Y] < secondNodePosition[Position.Y]) {
                    if (secondNodePosition[Position.X] < currentNode.getPosition()[Position.X]) return Directions.LEFT;
                    else return Directions.RIGHT;
                } else {
                    if (secondNodePosition[Position.X] > currentNode.getPosition()[Position.X]) return Directions.LEFT;
                    else return Directions.RIGHT;
                }
            } else {
                if (firstNodePosition[Position.X] < secondNodePosition[Position.X])
                    return secondNodePosition[Position.Y] > currentNode.getPosition()[Position.Y] ? Directions.LEFT : Directions.RIGHT;
                else
                    return secondNodePosition[Position.Y] < currentNode.getPosition()[Position.Y] ? Directions.LEFT : Directions.RIGHT;
            }
        }
        return -2;
    }

    private int[] getPath(Node node) {
        int[] path = {goWhere(node)};
        Node currentNode = node.getParent();
        if (currentNode == null) return null;
        while (currentNode.getParent() != null) {
            if (goWhere(currentNode) >= 0) {
                int[] tempPath = new int[path.length + 1];
                System.arraycopy(path, 0, tempPath, 1, path.length);
                tempPath[0] = goWhere(currentNode);
                path = tempPath;
                currentNode = currentNode.getParent();
            }
        }
        return path;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setFirstTurnPenalty(boolean first_penalty) {
        this.firstPenalty = first_penalty;
    }
}