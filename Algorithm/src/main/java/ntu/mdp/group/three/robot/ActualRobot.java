package ntu.mdp.group.three.robot;

import main.Main;
import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.communication.CommunicationManager;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.sensor.RealSensor;
import ntu.mdp.group.three.utility.MDFManager;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ActualRobot extends Robot {

    private CommunicationSocket communicationSocket = CommunicationSocket.getInstance();
    private static ActualRobot actualRobot = null;
    private SimulatedRobot simulatedRobot = null;

    public static ActualRobot getInstance(boolean isSimulation) {
        if (actualRobot == null) actualRobot = new ActualRobot(isSimulation);
        return actualRobot;
    }

    public ActualRobot(boolean isSimulation) {
        super();
        this.map = new ArenaMap();
        initialise(
                RobotConfig.START[Position.X],
                RobotConfig.START[Position.Y],
                Directions.SOUTH
        );
        this.sensor = new RealSensor();
        if (isSimulation) {
            simulatedRobot = new SimulatedRobot(this.x, this.y, this.getDirection());
            simulatedRobot.displayMessage("Disabled all buttons for the real run.");
        }
    }

    private boolean acknowledge() {
        Pattern intSensorPattern = Pattern.compile(SensorConfig.INT_SENSOR_PATTERN);
        Pattern floatSensorPattern = Pattern.compile(SensorConfig.FLOAT_SENSOR_PATTERN);
        String receivedMessage;
        String[] arr;

        do {
            receivedMessage = communicationSocket.receiveMessage().trim();
            if (CommunicationSocket.isDebug()) System.out.println("acknowledge: " + receivedMessage);
            if (simulatedRobot != null) simulatedRobot.displayMessage("Received Message: " + receivedMessage);
            if (intSensorPattern.matcher(receivedMessage).matches() || floatSensorPattern.matcher(receivedMessage).matches()) {
                arr = receivedMessage.split("\\|");
                break;
            }
        } while (true);

        if (Integer.parseInt(arr[SensorLoc.NO_OF_SENSORS]) == 1) {
            System.arraycopy(arr, 0, sensorValues, 0, SensorLoc.NO_OF_SENSORS);
            this.sensePosition = new int[]{x, y, this.getDirection()};
            sendMDFString();
            System.out.println("Acknowledged...");
            return true;
        }

        System.arraycopy(arr, 0, sensorValues, 0, SensorLoc.NO_OF_SENSORS);
        this.sensePosition = new int[]{x, y, this.getDirection()};
        sendMDFString();
        System.out.println("Not Acknowledged...");
        return false;
    }

    // Send MDF string every time sensor values are received.
    public void sendMDFString() {
        MDFManager mdfManager = MDFManager.getInstance();
        String[] mdfString = this.getMDFString();
        if (simulatedRobot != null) simulatedRobot.displayMessage(mdfManager.getMDFCommandString());
        else communicationSocket.sendMessage("M{\"map\":[{\"explored\": \"" + mdfString[0] +
                "\",\"length\":" + mdfString[1] + ",\"obstacle\":\"" + mdfString[2] + "\"}]}");
    }


    @Override
    protected String[] getSensorValues() {
        Pattern intSensorPattern = Pattern.compile(SensorConfig.INT_SENSOR_PATTERN);
        Pattern floatSensorPattern = Pattern.compile(SensorConfig.FLOAT_SENSOR_PATTERN);
        String receivedCommand;
        String[] arr;
        communicationSocket.sendMessage(RobotConfig.SENSE_ALL);
        if (simulatedRobot != null) simulatedRobot.displayMessage("Sent message: " + RobotConfig.SENSE_ALL);
        while (true) {
            receivedCommand = communicationSocket.receiveMessage().trim(); // Get sensor values for exploration.
            if (intSensorPattern.matcher(receivedCommand).matches() || floatSensorPattern.matcher(receivedCommand).matches()) {
                arr = receivedCommand.split("\\|");
                break;
            }
        }
        System.arraycopy(arr, 0, sensorValues, 0, SensorLoc.NO_OF_SENSORS);
        this.sensePosition[Position.X] = x;
        this.sensePosition[Position.Y] = y;
        this.sensePosition[2] = getDirection();
        return arr;
    }

    @Override
    public void forward(int stepCount) {
        communicationSocket.sendMessage("F" + stepCount + "|");
        this.x = checkInArenaX(this.x + SensorConfig.SENSOR_DIRECTION[this.getDirection()][0]);
        this.y = checkInArenaX(this.y + SensorConfig.SENSOR_DIRECTION[this.getDirection()][1]);
        if (simulatedRobot != null) {
            simulatedRobot.forward(stepCount);
            simulatedRobot.displayMessage("Sent message: F" + stepCount + "|");
        }
        makeInvalid();
        if (!acknowledge()) {
            this.x = checkInArenaX(this.x - SensorConfig.SENSOR_DIRECTION[this.getDirection()][0]);
            this.y = checkInArenaX(this.y - SensorConfig.SENSOR_DIRECTION[this.getDirection()][1]);
            if (simulatedRobot != null) simulatedRobot.backward(stepCount);
        }
    }

    @Override
    public void rotateRight() {
        communicationSocket.sendMessage(RobotConfig.TURN_RIGHT);
        setDirection((this.getDirection() + 1) % 4);
        if (simulatedRobot != null) {
            simulatedRobot.rotateRight();
            simulatedRobot.displayMessage("Sent message: " + RobotConfig.TURN_RIGHT);
        }
        acknowledge();
    }

    @Override
    public void rotateLeft() {
        communicationSocket.sendMessage(RobotConfig.TURN_LEFT);
        setDirection((this.getDirection() + 3) % 4);
        if (simulatedRobot != null) {
            simulatedRobot.rotateLeft();
            simulatedRobot.displayMessage("Sent message: " + RobotConfig.TURN_LEFT);
        }
        acknowledge();
    }

    @Override
    public boolean captureImage(int[][] imagePosition) {
        communicationSocket.sendMessage(
                "C["+ imagePosition[0][Position.Y] + "," + imagePosition[0][Position.X] +
                        "|" + imagePosition[1][Position.Y] + "," + imagePosition[1][Position.X] +
                        "|" + imagePosition[2][Position.Y] + "," + imagePosition[2][Position.X] + "]"
        );

        if (simulatedRobot != null) {
            simulatedRobot.captureImage(imagePosition);
            simulatedRobot.displayMessage("Sent message: " +
                    "C["+ imagePosition[0][Position.Y] + "," + imagePosition[0][Position.X] +
                    "|" + imagePosition[1][Position.Y] + "," + imagePosition[1][Position.X] +
                    "|" + imagePosition[2][Position.Y] + "," + imagePosition[2][Position.X] + "]"
            );
        }
        boolean completed = false;
        String receivedMessage;
        ArrayList<String> buffer = CommunicationManager.getBufferList();
        while (!completed) {
            receivedMessage = communicationSocket.receiveMessage().trim();
            completed = checkImageAcknowledge(receivedMessage);
            if (completed && receivedMessage.equals(RobotConfig.IMAGE_ACKNOWLEDGEMENT)) {
                System.out.println(receivedMessage);
                return false;
            } else if (completed && receivedMessage.equals(RobotConfig.IMAGE_STOP)) {
                System.out.println(receivedMessage);
                return true;
            } else {
                for (int i = 0; i < buffer.size(); i++) {
                    completed = checkImageAcknowledge(buffer.get(i));
                    if (completed) {
                        buffer.remove(i);
                        break;
                    }
                }
            }
            System.out.println("Received message from RPI:" + receivedMessage);
        }
        return false;
    }

    @Override
    public void rightAlign() {
        if (simulatedRobot != null) this.displayMessage("Sent message: " + RobotConfig.RIGHT_ALIGN);
        communicationSocket.sendMessage(RobotConfig.RIGHT_ALIGN);
        acknowledge();
    }

    @Override
    public void displayMessage(String message) { Main.writeToTraceTextArea(message); }

    @Override
    public int[] updateMap() {
        int[] isObstacle = super.updateMap();
        if (simulatedRobot != null) simulatedRobot.setMap(this.getMap());
        return isObstacle;
    }

    private boolean checkImageAcknowledge(String command) {
        return command.equals(RobotConfig.IMAGE_ACKNOWLEDGEMENT) || command.equals(RobotConfig.IMAGE_STOP);
    }
}
