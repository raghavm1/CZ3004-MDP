package ntu.mdp.group.three.robot;

import main.Main;
import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.map.SimulatedMap;
import ntu.mdp.group.three.sensor.SimulatedSensor;
import ntu.mdp.group.three.threadedTasks.MoveRobotTask;

import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class SimulatedRobot extends Robot {

    private SimulatedMap simulatedMap;
    private final Timer timer = new Timer();

    public SimulatedRobot(int x, int y, int direction) {
        super();
        initialise(x, y, direction);
        this.map = new ArenaMap();
        simulatedMap = SimulatedMap.getInstance(map.copy());
        this.sensor = new SimulatedSensor();
    }

    public void resetRobotPositionOnUI() {
        this.x = checkInArenaX(1);
        this.y = checkInArenaY(1);
        this.makeInvalid();

        // Reset robot position in the 15 x 20 grid arena UI.
        Main.ROBOT.setTranslateX(SimulatorConfig.DEFAULT_SIMULATOR_X);
        Main.ROBOT.setTranslateY(SimulatorConfig.DEFAULT_SIMULATOR_Y);
    }

    public void restartRobot() {
        this.x = checkInArenaX(0);
        this.y = checkInArenaY(0);

        // Reset robot position in the 15 x 20 grid arena UI.
        Main.ROBOT.setTranslateX(SimulatorConfig.DEFAULT_SIMULATOR_X);
        Main.ROBOT.setTranslateY(SimulatorConfig.DEFAULT_SIMULATOR_Y);

        this.setDirection(Directions.SOUTH);
        this.sensor = new SimulatedSensor();
        this.map = new ArenaMap();
        simulatedMap.setArenaMap(map);
    }

    @Override
    protected String[] getSensorValues() {
        return sensor.getAllSensorsValue(this.x, this.y, getDirection());
    }

    public void setDirection(int direction) {
        super.setDirection(direction);
        Main.ROBOT.setImage(SimulatorConfig.ROBOT_IMAGE_PATHS[direction]);
    }

    public int[] updateMap() {
        int[] isObstacle = super.updateMap();
        simulatedMap.setArenaMap(this.map);
        return isObstacle;
    }

    public void setWayPoint(int x, int y) {
        int [] oldWayPoint = this.getWayPoint().clone();
        super.setWayPoint(x, y);
        if (!Arrays.equals(oldWayPoint, this.getWayPoint())) {
            simulatedMap.setArenaMap(this.map);
            if (Arrays.equals(new int[] {-1, -1}, this.getWayPoint())) this.displayMessage("Removed way point.");
            else this.displayMessage("Successfully set the way point to: (" + x + ", " + y + ")");
        }
    }

    public void setMap(ArenaMap arenaMap) {
        this.map = arenaMap;
        simulatedMap.setArenaMap(arenaMap);
    }

    public void toggleMap() {
        ArenaMap temp = sensor.getTrueMap();
        if (ArenaMap.compareArenaMaps(temp, simulatedMap.getArenaMap())) simulatedMap.setArenaMap(map.copy());
        else simulatedMap.setArenaMap(temp.copy());
    }

    @Override
    public void forward(int stepCount) {
        this.x = checkInArenaX(this.x + SensorConfig.SENSOR_DIRECTION[this.getDirection()][Position.X]);
        this.y = checkInArenaX(this.y + SensorConfig.SENSOR_DIRECTION[this.getDirection()][Position.Y]);
        this.startDirectionalRobotTask(stepCount);
    }

    public void backward(int step) {
        this.x = checkInArenaX(this.x + SensorConfig.SENSOR_DIRECTION[(this.getDirection() + 2) % 4][Position.X]);
        this.y = checkInArenaX(this.y + SensorConfig.SENSOR_DIRECTION[(this.getDirection() + 2) % 4][Position.Y]);
        this.startDirectionalRobotTask(step);
    }

    private void startDirectionalRobotTask(int step) {
        String direction;
        switch (super.getDirection()) {
            case Directions.NORTH:
                direction = Directions.UP_DIRECTION;
                break;
            case Directions.EAST:
                direction = Directions.RIGHT_DIRECTION;
                break;
            case Directions.SOUTH:
                direction = Directions.DOWN_DIRECTION;
                break;
            case Directions.WEST:
                direction = Directions.LEFT_DIRECTION;
                break;
            default:
                direction = "Error";
        }
        this.makeInvalid();
        for (int i = 0; i < step * ArenaConfig.GRID_WIDTH; i++) {
            timer.schedule(new MoveRobotTask(direction, 1), RobotConfig.DELAY * (i + 1));
        }
    }

    @Override
    public void rotateRight() { this.setDirection((this.getDirection() + 1) % 4); }

    @Override
    public void rotateLeft() { this.setDirection((this.getDirection() + 3) % 4); }

    @Override
    public boolean captureImage(int[][] imagePosition) {
        this.displayMessage("Capturing image at " + Arrays.toString(getPosition()) + " now");
        this.displayMessage("Capturing Image...");
        try {
            TimeUnit.SECONDS.sleep(2); // Simulate image capture process.
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void rightAlign() {
        try {
            this.displayMessage("Right Aligning");
            TimeUnit.SECONDS.sleep(1); // Simulate right alignment of the robot.
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void displayMessage(String message) {
        Main.writeToTraceTextArea(message);
    }
}
