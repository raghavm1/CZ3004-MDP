package ntu.mdp.group.three.communication;


import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ntu.mdp.group.three.astarpathfinder.FastestPathThread;
import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.exploration.ExplorationThread;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.robot.ActualRobot;
import ntu.mdp.group.three.threadedTasks.ExplorationTimerTask;
import ntu.mdp.group.three.utility.MDFManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CommunicationManager extends Thread {

	private static ActualRobot actualRobot;
	private static CommunicationManager communicationManager = null;
	private CommunicationSocket communicationSocket = CommunicationSocket.getInstance();
	private static Thread thread = null;
	private static ArrayList<String> bufferList = new ArrayList<>();
	private static AtomicBoolean isRunning = new AtomicBoolean(false);
	private static String[] bufferCommand = new String[] {RobotConfig.IMAGE_ACKNOWLEDGEMENT};

	private CommunicationManager() { }

	public static CommunicationManager getInstance() {
		if (communicationManager == null) communicationManager = new CommunicationManager();
		return communicationManager;
	}

	public boolean connectToRPi(boolean simulate) {
		if (actualRobot == null) actualRobot = ActualRobot.getInstance(simulate);
		return communicationSocket.connectToRPI();
	}

	public void start(Stage primaryStage) throws IOException {
		FileChooser chooseMDFFileChooser = new FileChooser();
		chooseMDFFileChooser.setTitle("Select P2 String file");
		File selectedMDFFile = chooseMDFFileChooser.showOpenDialog(primaryStage);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(selectedMDFFile.toString()));
		MDFManager mdfManager = MDFManager.getInstance();
		StringBuilder stringBuilder = new StringBuilder();
		System.out.println("selectedMDFFile.toString(): " + selectedMDFFile.toString());
		String sCurrentLine;
		while ((sCurrentLine = bufferedReader.readLine()) != null) stringBuilder.append(sCurrentLine);
		mdfManager.setP2String(stringBuilder.toString());

		isRunning.set(true);
		while(isRunning.get()) {
			if (ExplorationThread.isRunning() || FastestPathThread.isRunning()) {
				try {
					thread.join();
				}
				catch (Exception e) {
					System.out.println("Error in start ConnectionManager");
				}
			}
			else this.waitingForMessage();
		}
	}

	public void stopConnectionManager() {
		isRunning.set(false);
	}

	public void start() {
		isRunning.set(true);
		while(isRunning.get()) {
			if (ExplorationThread.isRunning() || FastestPathThread.isRunning()) {
				try {
					thread.join();
				}
				catch (Exception e) {
					System.out.println("Error in start ConnectionManager");
				}
			}
			else this.waitingForMessage();
		}
	}

	public static ArrayList<String> getBufferList() {
		return bufferList;
	}

	public void waitingForMessage() {
		String receivedMessage;
		boolean complete = false;

		// Taking in 6 sensor values separated by | slashes.
		Pattern sensorPattern = Pattern.compile("\\d+[|]\\d+[|]\\d+[|]\\d+[|]\\d+[|]\\d+");
		MDFManager mdfManager = MDFManager.getInstance();

		while (!complete) {
			actualRobot.displayMessage("Waiting for orders");
			receivedMessage = this.communicationSocket.receiveMessage().trim();
			System.out.println("s:" + receivedMessage);
			actualRobot.displayMessage("Received message: " + receivedMessage);

			if (!ExplorationThread.isRunning() && !FastestPathThread.isRunning() && receivedMessage.contains(RobotConfig.INITIALISING)) {
				Pattern singleSingleDigitPattern = Pattern.compile(RobotConfig.INITIALISING + "[\\s]\\([1-9],[1-9],[0-3]\\)"),
						doubleSingleDigitPattern = Pattern.compile(RobotConfig.INITIALISING + "[\\s]\\([1][0-8],[1-9],[0-3]\\)"),
						singleDoubleDigitPattern  = Pattern.compile(RobotConfig.INITIALISING + "[\\s]\\([1-9],[1][0-4],[0-3]\\)"),
						doubleDoubleDigitPattern = Pattern.compile(RobotConfig.INITIALISING + "[\\s]\\([1][0-8],[1][0-4],[0-3]\\)");
				if (
						singleSingleDigitPattern.matcher(receivedMessage).matches() ||
						doubleSingleDigitPattern.matcher(receivedMessage).matches() ||
						singleDoubleDigitPattern .matcher(receivedMessage).matches() ||
						doubleDoubleDigitPattern.matcher(receivedMessage).matches()
				) {
					complete = true;
					String tmp = receivedMessage.replace(RobotConfig.INITIALISING + " (", "");
					tmp = tmp.replace(")", "");
					String[] arr = tmp.trim().split(",");
					actualRobot.initialise(Integer.parseInt(arr[1]), Integer.parseInt(arr[0]), (Integer.parseInt(arr[2]) + 1 ) % 4);
					receivedMessage = "Successfully set the robot's position: " + Integer.parseInt(arr[0]) +
						"," + Integer.parseInt(arr[1]) + "," + Integer.parseInt(arr[2]);
					actualRobot.displayMessage(receivedMessage);
				}
			}

			else if (!ExplorationThread.isRunning() && !FastestPathThread.isRunning() &&
					receivedMessage.equals(RobotConfig.START_EXPLORATION) ) {
				System.out.println("Starting exploration...");
				ExplorationTimerTask explorationTimerTask = ExplorationTimerTask.getInstance();
				explorationTimerTask.start();
				mdfManager.setExploration(true);

				thread = ExplorationThread.getInstance(actualRobot, RobotConfig.TIME, RobotConfig.PERCENTAGE, RobotConfig.SPEED, RobotConfig.USING_IMAGE_REC);
				thread.setPriority(Thread.MAX_PRIORITY);
				complete = true;

				try {
					thread.join();
				}
				catch(Exception e) {
					System.out.println("Error in start exploration in ConnectionManager");
				}
			}

			else if (!ExplorationThread.isRunning() && !FastestPathThread.isRunning() && receivedMessage.equals(RobotConfig.FASTEST_PATH) ) {
				mdfManager.setExploration(false);
				try {
					if (actualRobot != null) {
						ArenaMap arenaMap = actualRobot.getMap();
						arenaMap.setGrid(mdfManager.getGridFromMDF());
						actualRobot.setKnownMap(arenaMap);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				thread = FastestPathThread.getInstance(actualRobot, actualRobot.getWayPoint(), 1);
				thread.setPriority(Thread.MAX_PRIORITY);

				try {
					thread.join();
				}
				catch(Exception e) {
					System.out.println("Error in fastest path in ConnectionManager");
				}
				complete = true;
			}

			else if (!ExplorationThread.isRunning() && !FastestPathThread.isRunning() &&
					receivedMessage.contains(RobotConfig.HANDLE_MDF_REQUEST)) {
				communicationSocket.sendMessage(mdfManager.getMDFCommandString());
				complete = true;
			}

			else if (!ExplorationThread.isRunning() && !FastestPathThread.isRunning() &&
					receivedMessage.contains(RobotConfig.SET_WAY_POINT)) {
				Pattern singleSingleDigitWayPoint = Pattern.compile(RobotConfig.SET_WAY_POINT + " \\([1-9],[1-9]\\)"),
						doubleSingleDigitWayPoint = Pattern.compile(RobotConfig.SET_WAY_POINT + " \\([1-9],[1][0-8]\\)"),
						singleDoubleDigitWayPoint = Pattern.compile(RobotConfig.SET_WAY_POINT + " \\([1][0-4],[1-9]\\)"),
						doubleDoubleDigitWayPoint = Pattern.compile(RobotConfig.SET_WAY_POINT + " \\([1][0-4],[1][0-8]\\)");
				if (
						singleSingleDigitWayPoint.matcher(receivedMessage).matches() ||
						doubleSingleDigitWayPoint.matcher(receivedMessage).matches() ||
						singleDoubleDigitWayPoint.matcher(receivedMessage).matches() ||
						doubleDoubleDigitWayPoint.matcher(receivedMessage).matches()
					) {
					complete = true;
					String tmp = receivedMessage.replace(RobotConfig.SET_WAY_POINT + " (", "");
					tmp = tmp.replace(")", "");
					String[] arr = tmp.trim().split(",");
					actualRobot.setWayPoint(Integer.parseInt(arr[1]), Integer.parseInt(arr[0]));
					receivedMessage = "Successfully received the waypoint: " + Integer.parseInt(arr[0]) +
							"," + Integer.parseInt(arr[1]);
					actualRobot.displayMessage(receivedMessage);
				}
			}

			else if (receivedMessage.equals(RobotConfig.SEND_ARENA)) {
				String[] arr = actualRobot.getMDFString();
				communicationSocket.sendMessage("{\"map\":[{\"explored\": \"" + arr[0] + "\",\"length\":" + arr[1] + ",\"obstacle\":\"" + arr[2] +
						"\"}]}");
				actualRobot.displayMessage("{\"map\":[{\"explored\": \"" + arr[0] + "\",\"length\":" + arr[1] + ",\"obstacle\":\"" + arr[2] +
						"\"}]}");
				complete = true;
			}

			else if (Arrays.asList(bufferCommand).contains(receivedMessage) || sensorPattern.matcher(receivedMessage).matches()) {
				bufferList.add(receivedMessage);
				System.out.println("Placed command" + receivedMessage + " into buffer");
			}

			else System.out.println("Unknown command: " + receivedMessage);
		}
	}
}
