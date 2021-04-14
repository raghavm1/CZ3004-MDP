package ntu.mdp.group.three.communication;

import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.config.SensorConfig;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Server {
	// This server basically test communication for the main system
	public static void main(String[] args) {
		ConnectionServer server = ConnectionServer.getInstance();
		String message;
		String sensorMessage;
		boolean acknowledge;
		Scanner sc = new Scanner (System.in);
		boolean exploring = false, completed = false, fastestpath = false;
		int[] pos = new int[] {1,1};
		int direction = 2, count = 0;
		while (!completed) {
			System.out.print("Enter your command: ");
			message = sc.nextLine();
			server.sendMessage(message);
			if (message.equals(RobotConfig.START_EXPLORATION)) exploring = true;
			if (message.equals(RobotConfig.FASTEST_PATH)) fastestpath = true;
			if (message.equals(RobotConfig.SEND_ARENA)) completed = true;
			while (exploring || fastestpath) {
				message = server.receiveMessage();
				acknowledge = true;
				System.out.println("Message received: " + message);
				Pattern p = Pattern.compile("W\\d+[|]");

				if (p.matcher(message).matches()) {
					pos[0] = pos[0] + SensorConfig.SENSOR_DIRECTION[direction][0];
					pos[1] = pos[1] + SensorConfig.SENSOR_DIRECTION[direction][1];
					System.out.println(pos[0] + ", " + pos[1]);
				}
				else if (message.equals(RobotConfig.TURN_LEFT)) direction = (direction + 3) % 4;
				else if (message.equals(RobotConfig.TURN_RIGHT)) direction = (direction + 1) % 4;
				else if(message.equals(RobotConfig.SENSE_ALL) ||
						message.equals(RobotConfig.RIGHT_ALIGN)) System.out.println(pos[0] + ", " + pos[1]);
 				else if (message.equals(RobotConfig.END_TOUR) || message.contains("N")) {
					exploring = false;
					fastestpath = false;
					acknowledge = false;
				}
				else {
					acknowledge = false;
					System.out.println("Error.");
				}
				
				if (acknowledge) {
					if ((pos[0] == 1 && pos[1] == 12) || (pos[0] == 17 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 2) || (pos[0] == 2 && pos[1] == 1)) {
						sensorMessage = "" + SensorConfig.SENSOR_RANGE_THRESHOLD[0][1] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[1][1] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[2][1] +
								"|" + SensorConfig.SENSOR_RANGE_THRESHOLD[3][1] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[4][1] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[5][1] + "|1";
					} else if (((pos[0] == 1 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 13) || (pos[0] == 18 && pos[1] == 1)) && count < 1) {
						sensorMessage = "" + SensorConfig.SENSOR_RANGE_THRESHOLD[0][0] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[1][0] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[2][0]
								+ "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[3][0] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[4][0] + "|" + SensorConfig.SENSOR_RANGE_THRESHOLD[5][0] + "|1";
						count++;
					} else {
						sensorMessage = "74.0|74.0|64.0|7.0|7.0|64.0|1";
						count = 0;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					server.sendMessage(sensorMessage);
				}
			}
		}
		System.out.println(server.receiveMessage());
	}
}
