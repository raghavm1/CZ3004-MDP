package ntu.mdp.group.three.sensor;
import ntu.mdp.group.three.config.Position;
import ntu.mdp.group.three.config.SensorConfig;
import ntu.mdp.group.three.config.SensorLoc;
import ntu.mdp.group.three.map.ArenaMap;

public abstract class Sensor {

	// Far sensor range = 80-150
	// Short sensor range = 10-50
	// FR, FC, FL, RB, RF, LF
	// Assuming 0 is FR, 1 is FC, 2 is FL, 3 is RB, 4 is RF and 5 is LF.
	// sensorLocation[SensorLoc][x = 0, y = 1 coordinates of the sensor];
	public static int[][] sensorLocation = new int[SensorLoc.NO_OF_SENSORS][2];

	// Assume NORTH is the direction, Represent front, left and right sensor direction.
	// If the bot is facing NORTH via the Algorithm's arena landscape orientation, 0 is front, 1 is left, 2 is right directions of the robot.
	public static int[][] sensorDirection = new int[3][2];

	public final static int FRONT = 0;
	public final static int RIGHT = 1;
	public final static int LEFT = 2;

	public abstract String[] getAllSensorsValue(int x, int y, int direction);
	public abstract void setTrueMap(ArenaMap arenaMap);
	public abstract ArenaMap getTrueMap();

	// Calculates the sensor direction and the position of all my sensors based on offset from my robot position.
	// Function simply updates the sensor location and direction with the direction argument passed in.
	public static void updateSensorDirection(int direction) {

		// Front sensor direction pointed towards the (x, y) array that represents where the robot will face.
		sensorDirection[FRONT] = SensorConfig.SENSOR_DIRECTION[direction];

		// Sensor location for FRONT RIGHT sensor.
		sensorLocation[SensorLoc.FRONT_RIGHT] = new int[] {
				sensorDirection[FRONT][Position.X] + SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][Position.X],
				sensorDirection[FRONT][Position.Y] + SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][Position.Y]
		};

		// Sensor location for FRONT CENTER sensor.
		sensorLocation[SensorLoc.FRONT_CENTER] = sensorDirection[FRONT];

		// (((Direction - 1) mod 3) + 3) mod 3;
		/* If direction:
			North 	(0) = 2 (South)
			East 	(1) = 0 (North)
			South 	(2) = 1 (East)
			West 	(3) = 2 (South)
		*/
		int notSureWhatIsThisJPEG = ((direction - 1) % SensorConfig.SENSOR_DIRECTION.length + SensorConfig.SENSOR_DIRECTION.length) % SensorConfig.SENSOR_DIRECTION.length;

		// Sensor location for FRONT LEFT sensor.
		sensorLocation[SensorLoc.FRONT_LEFT] = new int[] {
				sensorDirection[FRONT][Position.X] + SensorConfig.SENSOR_DIRECTION[notSureWhatIsThisJPEG][Position.X],
				sensorDirection[FRONT][Position.Y] + SensorConfig.SENSOR_DIRECTION[notSureWhatIsThisJPEG][Position.Y]
		};

		// Sensor location for RIGHT BACK sensor.
		sensorLocation[SensorLoc.RIGHT_BEHIND] = new int[] {
				SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][Position.X] + SensorConfig.SENSOR_DIRECTION[(direction + 2) % SensorConfig.SENSOR_DIRECTION.length][Position.X],
				SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][Position.Y] + SensorConfig.SENSOR_DIRECTION[(direction + 2) % SensorConfig.SENSOR_DIRECTION.length][Position.Y	]
		};

		// Sensor location for RIGHT FRONT sensor.
		sensorLocation[SensorLoc.RIGHT_FRONT] = new int[] {
				sensorLocation[SensorLoc.FRONT_RIGHT][Position.X],
				sensorLocation[SensorLoc.FRONT_RIGHT][Position.Y]
		};

		// Sensor location for LEFT FRONT sensor.
		sensorLocation[SensorLoc.LEFT_FRONT] = new int[] {
				sensorLocation[SensorLoc.FRONT_LEFT][Position.X],
				sensorLocation[SensorLoc.FRONT_LEFT][Position.Y]
		};

		sensorDirection[RIGHT] = new int[] {
				SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][0],
				SensorConfig.SENSOR_DIRECTION[(direction + 1) % SensorConfig.SENSOR_DIRECTION.length][1]
		};

		sensorDirection[LEFT] = new int[] {
				SensorConfig.SENSOR_DIRECTION[notSureWhatIsThisJPEG][Position.X],
				SensorConfig.SENSOR_DIRECTION[notSureWhatIsThisJPEG][Position.Y]
		};
	}
}