package ntu.mdp.group.three.astarpathfinder;


import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.config.Directions;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.exploration.Exploration;
import ntu.mdp.group.three.robot.Robot;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FastestPath {

    public FastestPath() { }

    public int[] start(Robot robot, int[] wayPoint, int[] goal, int speed, boolean onGrid, boolean move) {
        AStarPathFinder aStarPathFinder = new AStarPathFinder();
        aStarPathFinder.setDirection(robot.getDirection());
        aStarPathFinder.setFirst(true);
        int[] path, path1, path2;

        if (aStarPathFinder.isValid(robot, wayPoint)) {
            if (!move) aStarPathFinder.setFirstTurnPenalty(false);
            path = aStarPathFinder.start(robot, robot.getPosition(), wayPoint, onGrid);
            if (path != null) {
                aStarPathFinder.setFirstTurnPenalty(true);
                path1 = path;
                path2 = aStarPathFinder.start(robot, wayPoint, goal, onGrid);
                path = new int[path1.length + path2.length];
                System.arraycopy(path1, 0, path, 0, path1.length);
                System.arraycopy(path2, 0, path, path1.length, path2.length);
            }
        } else {
            aStarPathFinder.setFirstTurnPenalty(true);
            path = aStarPathFinder.start(robot, robot.getPosition(), goal, onGrid);
        }

        if ((path != null) && move) {
            if (CommunicationSocket.checkConnection() && FastestPathThread.isRunning()) fastestPathMovementConstructor(path, robot);
            else move(robot, path, speed);
        }

        System.out.println(Arrays.toString(path));
        System.out.println(Arrays.toString(robot.getWayPoint()));
        System.out.println("Finished Fastest Path");
        return path;
    }

    private void fastestPathMovementConstructor(int[] path, Robot robot) {
        StringBuilder sb = new StringBuilder();
        int instructionCount = 0;
        for (int direction : path) {
            if (direction == Directions.FORWARD) instructionCount++;
            else if (instructionCount > 0) {
                sb.append("F").append(instructionCount).append("|");
                if (direction == Directions.RIGHT) {
                    sb.append(RobotConfig.TURN_RIGHT);
                    instructionCount = 1;
                } else if (direction == Directions.LEFT) {
                    sb.append(RobotConfig.TURN_LEFT);
                    instructionCount = 1;
                } else if (direction == Directions.BACKWARD) {
                    sb.append(RobotConfig.TURN_RIGHT).append(RobotConfig.TURN_RIGHT);
                    instructionCount = 1;
                } else {
                    System.out.println("Error!");
                    return;
                }
            }
        }
        if (instructionCount > 0) sb.append("F").append(instructionCount).append("|");
        String msg = sb.toString();
        robot.displayMessage("Message sent for start real run: " + msg);
        try {
            TimeUnit.SECONDS.sleep(1);
            CommunicationSocket.getInstance().sendMessage(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void move(Robot robot, int[] path, int speed) {
        Exploration exploration = new Exploration();
        for (int direction : path) {
            if (!CommunicationSocket.checkConnection()) {
                try {
                    TimeUnit.SECONDS.sleep(speed);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            if (direction == Directions.FORWARD) {
                if (exploration.checkFrontEmpty(robot)) robot.forward(1);
                else return;
            } else if (direction == Directions.RIGHT) {
                robot.updateMap();
                robot.rotateRight();
                if (exploration.checkFrontEmpty(robot)) robot.forward(1);
                else return;
            } else if (direction == Directions.LEFT) {
                robot.updateMap();
                robot.rotateLeft();
                if (exploration.checkFrontEmpty(robot)) robot.forward(1);
                else return;
            } else if (direction == Directions.BACKWARD) {
                robot.updateMap();
                robot.rotateRight();
                robot.updateMap();
                robot.rotateRight();
                if (exploration.checkFrontEmpty(robot)) robot.forward(1);
                else return;
            } else return;
        }

        robot.updateMap();
    }
}
