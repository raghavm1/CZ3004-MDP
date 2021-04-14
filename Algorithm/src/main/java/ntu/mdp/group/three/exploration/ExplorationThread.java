package ntu.mdp.group.three.exploration;


import main.Main;
import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.robot.Robot;
import ntu.mdp.group.three.robot.SimulatedRobot;
import ntu.mdp.group.three.threadedTasks.DisableButtonTask;
import ntu.mdp.group.three.utility.MDFManager;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExplorationThread extends Thread {

	// Robot abstract class.
	private Robot robot;

	// Variables to store robot runtime properties.
	private int time, percentage, speed;
	private boolean isImageRecognition;

	// A boolean value that may be updated atomically. See the VarHandle specification for descriptions of the properties
	// of atomic accesses. An AtomicBoolean is used in applications such as atomically updated flags,
	// and cannot be used as a replacement for a Boolean.
	private final static AtomicBoolean isRunning = new AtomicBoolean(false);
	private final static AtomicBoolean isComplete = new AtomicBoolean(false);
	private static ExplorationThread thread = null;

	private ExplorationThread(Robot robot, int time, int percentage, int speed, boolean image_recognition) {
		super("ExplorationThread");
		this.robot = robot;
		this.time = time;
		this.percentage = percentage;
		this.speed = speed;
		this.isImageRecognition = image_recognition;
		start();
	}

	@Override
	public void run() {
		MDFManager mdfManager = MDFManager.getInstance();
		mdfManager.setExploration(true);
		isRunning.set(true);

		boolean isSimulated = robot.getClass().equals(SimulatedRobot.class);
		if (!isSimulated) CommunicationSocket.getInstance().sendMessage(mdfManager.getMDFCommandString());

		new Exploration(robot, time, percentage, speed, isImageRecognition);

		if (isRunning.get()) {
			isComplete.set(true);
			Main.writeToTraceTextArea("Time taken to explore: " + Main.countdownTile.getDuration());
			Main.timer.stop();
			new Timer().schedule(new DisableButtonTask(false), 1);
		} else isComplete.set(false);

		stopThread();

		if (CommunicationSocket.checkConnection()) {
			// Send the MDF String at the end when it is completed.
			// MDF is the Map Descriptor Format, i.e. the Map String that will be fed to us prior the execution of SPF.
			robot.getMDFString();
			CommunicationSocket.getInstance().sendMessage(mdfManager.getMDFCommandString());
			CommunicationSocket.getInstance().sendMessage(RobotConfig.END_TOUR);
		}
		else {
			// Display onto the User Interface, it is completed when it is the simulator
			if (isSimulated) {
				SimulatedRobot simulatedRobot = (SimulatedRobot) robot;
                simulatedRobot.displayMessage("Exploration Completed");
			}
		}
	}

	// Get ExplorationThread singleton instance.
	public static ExplorationThread getInstance(Robot r, int time, int percentage, int speed, boolean image_recognition) {
		if (thread == null) {
			thread = new ExplorationThread(r, time, percentage, speed, image_recognition);
		}
		return thread;
	}

	// Getter for the AtomicBoolean variable running.
	public static boolean isRunning() {
		return isRunning.get();
	}

	// Stops the exploration thread by setting the AtomicBoolean variable of running to false and nulling the thread.
	public static void stopThread() {
		isRunning.set(false);
		thread = null;
	}

}
