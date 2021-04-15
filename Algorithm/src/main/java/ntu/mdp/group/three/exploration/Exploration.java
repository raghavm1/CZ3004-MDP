package ntu.mdp.group.three.exploration;

import ntu.mdp.group.three.astarpathfinder.FastestPath;
import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.robot.Robot;
import ntu.mdp.group.three.threadedTasks.SenseTask;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Exploration {
    private FastestPath fastestPath = new FastestPath();
    private ArenaMap arenaMap;
    private boolean isStoppingForImageCapture;

    public Exploration() { }

    public Exploration(Robot robot, int time, int percentage, int speed, boolean image_recognition) {
        arenaMap = robot.getMap();

        // Start Sensor polling thread.
        SenseTask senseTask = SenseTask.getInstance();
        senseTask.start();

        if (speed == 1 && time == -1 && percentage == 100) {
            if (image_recognition) {
                isStoppingForImageCapture = false;
                imageRecognitionExploration(robot);
            } else {
                normalExploration(robot);
                System.out.println("dist: ");
                for (int i = 0; i < arenaMap.distanceMemory.length; i++) {
                    for (int j = 0; j < arenaMap.distanceMemory[i].length; j++) {
                        System.out.print(arenaMap.distanceMemory[i][j] + " ");
                    }
                    System.out.println();
                }
            }
        } else  limitedExploration(robot, time, percentage, speed);
        cornerCalibration(robot);

        int[] fastestPath = this.fastestPath.start(robot, robot.getWayPoint(), RobotConfig.END, 1, true, false);

        switch (fastestPath[0]) {
            case Directions.LEFT:
                robot.rotateLeft();
                robot.rightAlign();
                break;
            case Directions.BACKWARD:
                robot.rotateLeft();
                robot.rightAlign();
                robot.rotateLeft();
                break;
            case Directions.RIGHT:
                robot.rotateRight();
                break;
            default:
                break;
        }
    }

    private void limitedExploration(Robot robot, int time, int percentage, int speed) {
        System.out.println("Starting limited exploration...");
        ExplorationTimer explorationTimer = new ExplorationTimer();
        explorationTimer.start();
        robot.setDirection(Directions.SOUTH);

        do {
            // Stop exploration when time is up.
            if (time != -1 && (int) explorationTimer.getElapsedTime() >= time) return;

            // Stop exploration when completed to the percentage defined.
            if (percentage != 100 && percentComplete(robot) >= percentage) return;

            System.out.println("Exploration Phase");
            move(robot, speed, null);
            cornerCalibration(robot);
        } while (notAtPosition(robot, RobotConfig.START));

        int[] unexplored = getUnexploredArea(robot, robot.getPosition());
        while (unexplored != null) {
            if (time != -1) {
                int time_taken = (int) explorationTimer.getElapsedTime();
                if (time_taken >= time) return;
            }

            if (percentage != 100 && percentComplete(robot) >= percentage) return;
            System.out.println("Exploration FP Phase 1");
            int[] path = fastestPath.start(robot, null, unexplored, speed, false, true);
            if ((path == null) || (arenaMap.getGrid(unexplored[0], unexplored[1]).equals(RobotConfig.UNEXPLORED))) {
                arenaMap.setGrid(unexplored[0], unexplored[1], RobotConfig.OBSTACLE);
            }
            unexplored = getUnexploredArea(robot, robot.getPosition());
            robot.updateMap();
        }

        if (notAtPosition(robot, RobotConfig.START)) {
            System.out.println("Exploration FP Phase 2");
            System.out.println(Arrays.toString(robot.getPosition()));
            fastestPath.start(robot, null, RobotConfig.START, speed, true, true);
        }

        explorationTimer.stop();
        System.out.println("Exploration Complete!");
    }

    private void normalExploration(Robot robot) {
        System.out.println("Starting normal exploration...");
        robot.setDirection(Directions.SOUTH);

        do move(robot, 1, null);
        while (notAtPosition(robot, RobotConfig.START));

        int[] unexploredArea = getUnexploredArea(robot, robot.getPosition());
        while (unexploredArea != null) {
            System.out.println("Exploration Phase");
            int[] path = fastestPath.start(robot, null, unexploredArea, 1, false, true);
            if (path == null || arenaMap.getGrid(unexploredArea[0], unexploredArea[1]).equals(RobotConfig.UNEXPLORED)) {
                arenaMap.setGrid(unexploredArea[0], unexploredArea[1], RobotConfig.OBSTACLE);
            }

            unexploredArea = getUnexploredArea(robot, robot.getPosition());
            robot.updateMap();
        }

        if (notAtPosition(robot, RobotConfig.START)) {
            System.out.println("Exploration FP Phase");
            System.out.println("Robot at: " + Arrays.toString(robot.getPosition()));
            fastestPath.start(robot, null, RobotConfig.START, 1, true, true);
        }
        System.out.println("Exploration Complete!");
    }

    private void imageRecognitionExploration(Robot robot) {
        System.out.println("Starting image rec exploration...");
        robot.setDirection(Directions.SOUTH);
        int[][] checkedObstacles = {{0}};
        boolean unexplored = false;
        int[] needToTakeImage = null;
        int[] goToCoordinates = null;
        boolean move = false;

        do {
            checkedObstacles = move(robot, 1, checkedObstacles);
            System.out.println(Arrays.deepToString(checkedObstacles));
            cornerCalibration(robot);
        } while (notAtPosition(robot, RobotConfig.START));

        cornerCalibration(robot);
        if (!this.isStoppingForImageCapture) this.isStoppingForImageCapture = robot.captureImage(RobotConfig.DEFAULT_CAPTURE);
        if (!this.isStoppingForImageCapture) {
            needToTakeImage = picture_taken(robot, robot.getPosition(), checkedObstacles);
            goToCoordinates = nextToObstacle(robot, needToTakeImage);
        }

        if (goToCoordinates == null) {
            unexplored = true;
            needToTakeImage = null;
            goToCoordinates = getUnexploredArea(robot, robot.getPosition());
        }

        while ((goToCoordinates != null) && !(this.isStoppingForImageCapture)) {
            System.out.println("Exploration Phase 2");
            int[] path = fastestPath.start(robot, null, goToCoordinates, 1, true, true);
            if ((unexplored) && ((path == null) || (arenaMap.getGrid(goToCoordinates[0], goToCoordinates[1]).equals(RobotConfig.UNEXPLORED)))) {
                arenaMap.setGrid(goToCoordinates[0], goToCoordinates[1], RobotConfig.OBSTACLE);
                int[][] temp = new int[checkedObstacles.length + 1][3];
                System.arraycopy(checkedObstacles, 0, temp, 0, checkedObstacles.length);
                temp[checkedObstacles.length] = goToCoordinates;
                checkedObstacles = temp;
            } else move = obstacleOnRight(robot, needToTakeImage);

            if (path != null && move) {
                do {
                    checkedObstacles = move(robot, 1, checkedObstacles);
                    System.out.println(Arrays.deepToString(checkedObstacles));
                } while (notAtPosition(robot, goToCoordinates) && !isStoppingForImageCapture);
            }

            imageRecognition(robot, checkedObstacles);

            unexplored = false;
            needToTakeImage = picture_taken(robot, robot.getPosition(), checkedObstacles);
            goToCoordinates = nextToObstacle(robot, needToTakeImage);

            if (goToCoordinates == null) {
                unexplored = true;
                needToTakeImage = null;
                goToCoordinates = getUnexploredArea(robot, robot.getPosition());
            }

            if (goToCoordinates == null) {
                fastestPath.start(robot, null, RobotConfig.START, 1, true, true);
                cornerCalibration(robot);
            } else {
                fastestPath.start(robot, null, getNearestCorner(robot), 1, true, true);
                cornerCalibration(robot);
                if (!this.isStoppingForImageCapture) {
                    this.isStoppingForImageCapture = robot.captureImage(RobotConfig.DEFAULT_CAPTURE);
                }
            }

            robot.updateMap();
        }

        goToCoordinates = getUnexploredArea(robot, robot.getPosition());
        while ((goToCoordinates != null) && this.isStoppingForImageCapture) {
            System.out.println("Exploration Phase 3");
            System.out.println(Arrays.toString(robot.getPosition()));

            int[] path = fastestPath.start(robot, null, goToCoordinates, 1, false, true);
            if ((path == null) || (arenaMap.getGrid(goToCoordinates[0], goToCoordinates[1]).equals(RobotConfig.UNEXPLORED))) {
                arenaMap.setGrid(goToCoordinates[0], goToCoordinates[1], RobotConfig.OBSTACLE);
            }

            goToCoordinates = getUnexploredArea(robot, robot.getPosition());
            robot.updateMap();
        }

        if (notAtPosition(robot, RobotConfig.START)) {
            System.out.println("Phase 4");
            System.out.println(Arrays.toString(robot.getPosition()));
            fastestPath.start(robot, null, RobotConfig.START, 1, true, true);
        }

        System.out.println("Exploration Complete!");
    }

    private boolean[] checkObstacles(int[] obstaclesOrNot) {
        boolean[] areObstacles = new boolean[SensorLoc.NO_OF_SENSORS];
        for (int i = 0; i < areObstacles.length; i++) areObstacles[i] = obstaclesOrNot[i] == 1;
        return areObstacles;
    }

    // Checked obstacles arguments are for image recognition.
    // Function returns null if its not image recognition.
    // print lines are for debugging purposes.
    private int[][] move(Robot robot, int speed, int[][] obstaclesToCheck) {
        System.out.println("Robot at: " + Arrays.toString(robot.getPosition()));
        int[][] checked = obstaclesToCheck;
        robot.updateMap();

        if (!CommunicationSocket.checkConnection()) {
            try {
                TimeUnit.SECONDS.sleep(speed);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        if (checkRightEmpty(robot)) {
            System.out.println("Right is empty...");
            robot.rotateRight();
            if (checkFrontEmpty(robot)) {
                System.out.println("Right is empty and front is empty...");
                robot.forward(1);
                return checked;
            } else {
                System.out.println("Right is empty and front is not empty...");
                robot.rotateLeft();
                if ((checked != null) && (rightWall(robot))) {
                    checked = imageRecognition(robot, checked);
                }
            }
        } else if ((checked != null) && (rightWall(robot))) {
            checked = imageRecognition(robot, checked);
        }
        if (checkFrontEmpty(robot)) {
            System.out.println("Front is empty...");
            robot.forward(1);
            return checked;
        } else {
            System.out.println("Front is not empty...");
            robot.rotateLeft();
            if ((checked != null) && (rightWall(robot))) {
                checked = imageRecognition(robot, checked);
            }
        }
        if (checkFrontEmpty(robot)) {
            System.out.println("Front is empty 2...");
            robot.forward(1);
            return checked;
        } else {
            System.out.println("Front is not empty 2...");
            robot.rotateLeft();
            if ((checked != null) && (rightWall(robot))) {
                checked = imageRecognition(robot, checked);
            }
        }
        if (checkFrontEmpty(robot)) {
            System.out.println("Front is empty 3...");
            robot.forward(1);
        } else {
            System.out.println("Error during exploration phase 1. All 4 sides blocked.");
        }
        System.out.println();
        return checked;
    }

    private int[][] imageRecognition(Robot robot, int[][] checkedObstacles) {
        System.out.println("Recognizing image...");
        if (this.isStoppingForImageCapture) return checkedObstacles;

        int x = robot.getPosition()[Position.X];
        int y = robot.getPosition()[Position.Y];
        int[] defaultPosition = new int[] {-1, -1, -1};
        int[][] obstaclePosition = new int[][] {defaultPosition, defaultPosition, defaultPosition};
        boolean toCaptureImage = false;
        int direction = robot.getDirection();

        switch (direction) {
            case Directions.EAST:
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        if (Arrays.equals(obstaclePosition[i], defaultPosition) &&
                                arenaMap.getGrid(x + 2 + j, y - 1 + i).equals(RobotConfig.OBSTACLE))
                            obstaclePosition[i] = new int[] {x + 2 + j, y - 1 + i, Directions.EAST};
                break;
            case Directions.WEST:
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        if (Arrays.equals(obstaclePosition[i], defaultPosition) &&
                                arenaMap.getGrid(x - 2 - j, y + 1 - i).equals(RobotConfig.OBSTACLE))
                            obstaclePosition[i] = new int[] {x - 2 - j, y + 1 - i, Directions.WEST};
                break;
            case Directions.SOUTH:
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        if (Arrays.equals(obstaclePosition[i], defaultPosition) &&
                                arenaMap.getGrid(x + 1 - i, y + 2 + j).equals(RobotConfig.OBSTACLE))
                            obstaclePosition[i] = new int[] {x + 1 - i, y + 2 + j, Directions.SOUTH};
                break;
            case Directions.NORTH:
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        if (Arrays.equals(obstaclePosition[i], defaultPosition) &&
                                arenaMap.getGrid(x - 1 + i, y - 2 - j).equals(RobotConfig.OBSTACLE))
                            obstaclePosition[i] = new int[] {x - 1 + i, y - 2 - j, Directions.NORTH};
                break;
        }

        for (int k = 0; k < 3; k++)
            if (!withinMap(obstaclePosition[k][0], obstaclePosition[k][1])) obstaclePosition[k] = defaultPosition;
            else
                for (int[] obstacles : checkedObstacles)
                    if (Arrays.equals(obstacles, obstaclePosition[k])) {
                        obstaclePosition[k] = defaultPosition;
                        break;
                    }


        for (int m = 0; m < 3; m++) {
            if (!(Arrays.equals(obstaclePosition[m], defaultPosition))) {
                if (checkFrontEmpty(robot)) checkedObstacles[0][0] = m + 1;
                if ((checkedObstacles[0][0] > 2) || (!checkFrontEmpty(robot))) toCaptureImage = true;
            }
        }

        if (toCaptureImage) {
            for (int[] obs : obstaclePosition) {
                if (!(Arrays.equals(obs, defaultPosition))) {
                    int len = checkedObstacles.length;
                    int[][] temp = new int[len + 1][3];
                    System.arraycopy(checkedObstacles, 0, temp, 0, len);
                    temp[len] = obs;
                    checkedObstacles = temp;
                }
            }
            checkedObstacles[0][0] = 0;
            this.isStoppingForImageCapture = robot.captureImage(obstaclePosition);
        }

        return checkedObstacles;
    }

    private boolean withinMap(int x, int y) {
        return (x < ArenaConfig.ARENA_WIDTH) && (x >= 0) && (y < ArenaConfig.ARENA_HEIGHT) && (y >= 0);
    }

    private boolean rightWall(Robot robot) {
        int direction = robot.getDirection();
        int[] robotPosition = robot.getPosition();
        switch (direction) {
            case Directions.EAST: return robotPosition[Position.X] != 18;
            case Directions.WEST: return robotPosition[Position.X] != 1;
            case Directions.SOUTH: return robotPosition[Position.Y] != 13;
            case Directions.NORTH: return robotPosition[Position.Y] != 1;
            default: return false;
        }
    }

    private boolean checkRightEmpty(Robot robot) {
        boolean[] obstacles = checkObstacles(robot.updateMap());
        int[] robotPosition = robot.getPosition();
        int direction = robot.getDirection();
        ArenaMap map = robot.getMap();

        switch (direction) {
            case Directions.SOUTH:
                robotPosition[Position.Y] += 2; // Set robot position to origin + 2 in the y-axis
                break;
            case Directions.NORTH:
                robotPosition[Position.Y] -= 2; // Set robot position to origin - 2 in the y-axis
                break;
            case Directions.WEST:
                robotPosition[Position.X] -= 2; // Set robot position to origin - 2 in the x-axis
                break;
            case Directions.EAST:
                robotPosition[Position.X] += 2; // Set robot position to origin + 2 in the y-axis
                break;
        }

        return (!obstacles[SensorLoc.RIGHT_BEHIND]) &&
                (!obstacles[SensorLoc.RIGHT_FRONT]) && // If Sensor RB and RF does not sense a obstacle AND
                (map.getGrid(robotPosition[Position.X], robotPosition[Position.Y]).equals(RobotConfig.EXPLORED) ||
                        map.getGrid(robotPosition[Position.X], robotPosition[Position.Y]).equals(RobotConfig.START_POINT) ||
                        map.getGrid(robotPosition[Position.X], robotPosition[Position.Y]).equals(RobotConfig.END_POINT)
                ); // if current robot position is in an explored cell or start point cell or end point cell
    }

    public boolean checkFrontEmpty(Robot robot) {
        boolean[] obstacles = checkObstacles(robot.updateMap());
        return (!obstacles[SensorLoc.FRONT_RIGHT]) && (!obstacles[SensorLoc.FRONT_CENTER]) && (!obstacles[SensorLoc.FRONT_LEFT]);
    };

    private void cornerCalibration(Robot robot) {
        int[] position= robot.getPosition();
        if (!(( position[Position.X] == 1 || position[Position.X] == 18) && ( position[Position.Y] == 1 ||  position[Position.Y] == 13))) return;
        robot.updateMap();
        int direction = robot.getDirection();
        if (( position[Position.X] == 1) && ( position[Position.Y] == 13)) {
            switch (direction) {
                case Directions.EAST:
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                case Directions.SOUTH:
                    robot.rotateRight();
                    break;
                case Directions.NORTH:
                    robot.rotateLeft();
                    break;
                default:
                    break;
            }
        } else if (( position[Position.X] == 18) && ( position[Position.Y] == 13)) {
            switch (direction) {
                case Directions.NORTH:
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                case Directions.EAST:
                    robot.rotateRight();
                    break;
                case Directions.WEST:
                    robot.rotateLeft();
                    break;
                default:
                    break;
            }
        } else if (( position[Position.X] == 18) && ( position[Position.Y] == 1)) {
            switch (direction) {
                case Directions.WEST:
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                case Directions.NORTH:
                    robot.rotateRight();
                    break;
                case Directions.SOUTH:
                    robot.rotateLeft();
                    break;
                default:
                    break;
            }
        } else if (( position[Position.X] == 1) && ( position[Position.Y] == 1)) {
            switch (direction) {
                case Directions.SOUTH:
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                case Directions.WEST:
                    robot.rotateRight();
                    break;
                case Directions.EAST:
                    robot.rotateLeft();
                    break;
                default:
                    break;
            }
        }
        int newDirection = robot.getDirection();

        switch(Math.abs(direction - newDirection + 4) % 4) {
            case Directions.EAST:
                robot.rotateRight();
                break;
            case Directions.SOUTH:
                robot.rotateRight();
                robot.rotateRight();
                break;
            case Directions.WEST:
                robot.rotateLeft();
                break;
        }
    }

    private int[] getNearestCorner(Robot robot) {
        int[] robotPosition = robot.getPosition();
        int[][] arenaCornersCoordinates = new int[][] {
                {1,1}, {1, 13}, {18, 1}, {18, 13}
        };
        int[] costs = new int[4];
        int cheapestIndex = 0;

        for (int i = 0; i < 4; i++) {
            boolean valid = true;
            int x = arenaCornersCoordinates[i][0];
            int y = arenaCornersCoordinates[i][1];
            ArenaMap map = robot.getMap();
            int[][] grid = new int[][] {
                    {x - 1, y - 1}, {x, y - 1}, {x + 1, y - 1}, {x - 1, y},
                    {x, y}, {x + 1, y}, {x - 1, y + 1}, {x, y + 1}, {x + 1, y + 1}
            };
            for (int[] grids : grid) {
                if (!map.getGrid(grids[0], grids[1]).equals(RobotConfig.EXPLORED)) {
                    valid = false;
                }
            }
            if (valid) {
                costs[i] = Math.abs(robotPosition[Position.X] - arenaCornersCoordinates[i][Position.X]) +
                        Math.abs(robotPosition[Position.Y] - arenaCornersCoordinates[i][Position.Y]);
                if (costs[i] < costs[cheapestIndex]) {
                    cheapestIndex = i;
                }
            }
        }
        return arenaCornersCoordinates[cheapestIndex];
    }

    private boolean notAtPosition(Robot robot, int[] goal) {
        return (!Arrays.equals(robot.getPosition(), goal));
    }

    private int[] getUnexploredArea(Robot robot, int[] start) {
        ArenaMap map = robot.getMap();
        int lowestCost = 9999;
        int[] cheapestPos = null;
        for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
            for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
                if (map.getGrid(i, j).equals(RobotConfig.UNEXPLORED)) {
                    int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                    if (cost < lowestCost) {
                        cheapestPos = new int[] {i, j};
                        lowestCost = cost;
                    }
                }
            }
        }
        System.out.println("getUnexploredArea: " + Arrays.toString(cheapestPos));
        return cheapestPos;
    }

    private int[] nextToObstacle(Robot robot, int[] next) {
        if (next == null) return null;
        int x = next[0];
        int y = next[1];
        int[][] order = new int[][] {
                {x - 1, y - 2}, {x, y - 2}, {x + 1, y - 2},
                {x + 2, y - 1}, {x + 2, y}, {x + 2, y + 1},
                {x + 1, y + 2}, {x, y + 2}, {x - 1, y + 2},
                {x - 2, y + 1}, {x - 2, y}, {x - 2, y - 1}
        };
        ArenaMap arenaMap = robot.getMap();
        for (int[] position: order) {
            if (withinMap( position[Position.X],  position[Position.Y]) &&
                    arenaMap.getGrid( position[Position.X],  position[Position.Y]).equals(RobotConfig.EXPLORED))
                return position;
        }
        return null;
    }

    private int[] picture_taken(Robot robot, int[] start, int[][] checkedObstacles) {
        ArenaMap map = robot.getMap();
        int minimumCost = 9999;
        int[] cheapestPosition = null;

        for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
            for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
                if (map.getGrid(i, j).equals(RobotConfig.OBSTACLE)) {
                    boolean notInside = true;
                    for (int k = 1; k < checkedObstacles.length; k++) {
                        int[] oPosition = {checkedObstacles[k][0], checkedObstacles[k][1]};
                        int[] currentPosition = {i, j};
                        if (Arrays.equals(oPosition, currentPosition)) {
                            notInside = false;
                            break;
                        }
                    }
                    if (notInside) {
                        int cost = Math.abs(start[0] - i) + Math.abs(start[1] - j);
                        if (cost < minimumCost) {
                            cheapestPosition = new int[]{i, j};
                            minimumCost = cost;
                        }
                    }
                }
            }
        }
        return cheapestPosition;
    }

    private boolean obstacleOnRight(Robot robot, int[] obstacle) {
        if (obstacle == null) {
            return false;
        }
        int direction = robot.getDirection();
        int[] robotPosition = robot.getPosition();

        switch (direction) {
            case Directions.EAST:
                if (obstacle[Position.X] == (robotPosition[Position.X] - 2)) {
                    break;
                } else if (obstacle[Position.Y] == (robotPosition[Position.Y] + 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[Position.Y] == (robotPosition[Position.Y] - 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Directions.WEST:
                if (obstacle[Position.X] == (robotPosition[Position.X] + 2)) {
                    break;
                } else if (obstacle[Position.Y] == (robotPosition[Position.Y] - 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[Position.Y] == (robotPosition[Position.Y] + 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Directions.SOUTH:
                if (obstacle[Position.Y] == (robotPosition[Position.Y] + 2)) {
                    break;
                } else if (obstacle[Position.X] == (robotPosition[Position.X] - 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[Position.X] == (robotPosition[Position.X] + 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
            case Directions.NORTH:
                if (obstacle[Position.Y] == (robotPosition[Position.Y] + 2)) {
                    break;
                } else if (obstacle[Position.X] == (robotPosition[Position.X] + 2)) {
                    robot.rotateRight();
                    break;
                } else if (obstacle[Position.X] == (robotPosition[Position.X] - 2)) {
                    robot.rotateLeft();
                    break;
                } else {
                    robot.rotateRight();
                    robot.rotateRight();
                    break;
                }
        }
        return true;
    }

    private int percentComplete(Robot robot) {
        ArenaMap map = robot.getMap();
        int unexplored = 0;
        for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
            for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
                if (map.getGrid(i, j).equals("Unexplored")) unexplored ++;
            }
        }
        return ((ArenaConfig.ARENA_HEIGHT * ArenaConfig.ARENA_WIDTH) - unexplored) / 3;
    }
}