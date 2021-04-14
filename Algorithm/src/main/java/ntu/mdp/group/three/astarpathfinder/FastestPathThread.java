package ntu.mdp.group.three.astarpathfinder;

import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.config.Directions;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.robot.Robot;
import ntu.mdp.group.three.robot.SimulatedRobot;

import java.util.concurrent.atomic.AtomicBoolean;

public class FastestPathThread extends Thread {

    private int speed;
    private int[] wayPoint;
    private Robot robot;

    private final static AtomicBoolean isCompleted = new AtomicBoolean(false);
    private final static AtomicBoolean isRunning = new AtomicBoolean(false);
    private static FastestPathThread thread = null;

    public static FastestPathThread getInstance(Robot robot, int[] wayPoint, int speed) {
        if (thread == null) thread = new FastestPathThread(robot, wayPoint, speed);
        return thread;
    }

    private FastestPathThread(Robot robot, int[] wayPoint, int speed) {
        super("FastestPathThread");
        this.speed = speed;
        this.wayPoint = wayPoint;
        this.robot = robot;
        start();
    }

    public static boolean isCompleted() {
        return isCompleted.get();
    }

    public static boolean isRunning() {
        return isRunning.get();
    }

    public void run() {
        boolean isSimulated;
        isRunning.set(true);
        isSimulated = robot.getClass().equals(SimulatedRobot.class);
        FastestPath fastestPath = new FastestPath();
        fastestPath.start(robot, wayPoint, RobotConfig.END, speed, true, true);
        isCompleted.set(isRunning.get());
        stopThread();

		if (CommunicationSocket.checkConnection()) {
			CommunicationSocket.getInstance().sendMessage(RobotConfig.END_TOUR);
			robot.displayMessage("Sent message: " + RobotConfig.END_TOUR);
		} else {
			if (isSimulated) {
				SimulatedRobot simulatedRobot = (SimulatedRobot) robot;
				simulatedRobot.displayMessage("Fastest Path Completed");
				simulatedRobot.setDirection(Directions.NORTH);
			}
		}
    }

    public static void stopThread() {
        thread = null;
        isRunning.set(false);
    }
}
