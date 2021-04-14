package ntu.mdp.group.three.robot;

import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.sensor.Sensor;

public abstract class Robot {

	private boolean isValidObstacleValue;
	private int[] isObstacle = new int[SensorLoc.NO_OF_SENSORS];
	private int direction;

	protected Sensor sensor;
	protected int x, y;
	protected ArenaMap map;
	protected String[] sensorValues = new String[SensorLoc.NO_OF_SENSORS];
	protected int[] sensePosition = new int[] {-1, -1, -1};

	public Robot() { }

	public void initialise(int x, int y, int direction) {
		this.x = checkInArenaX(x);
		this.y = checkInArenaY(y);
		this.direction = direction;
		this.isValidObstacleValue = false;
	}

	public void setDirection(int direction) {
		this.direction = direction;
		this.makeInvalid();
	}

	protected int checkInArenaX(int x) {
		if (x >= ArenaConfig.ARENA_WIDTH - 1) x = ArenaConfig.ARENA_WIDTH - 2;
		if (x <= 0) x = 1;
		return x;
	}

	protected int checkInArenaY(int y) {
		if (y >= ArenaConfig.ARENA_HEIGHT - 1) y = ArenaConfig.ARENA_HEIGHT - 2;
		if (y <= 0) y = 1;
		return y;
	}

	public int getDirection() { return direction; }

	public int[] getPosition() { return new int[] { x, y }; }

	public int[] updateMap() { // This is where the robot decides where to move next based on sensor readings.
		if (isValidObstacleValue) return this.isObstacle;
		ArenaMap arenaMap = this.map;
		Sensor.updateSensorDirection(this.getDirection());

		if (!(sensePosition[Position.X] == x && sensePosition[Position.Y] == y && sensePosition[2] == direction) ||
				!CommunicationSocket.checkConnection()) {
			this.sensorValues = getSensorValues(); // THIS VALUES IS BY CM (GRID * 10)
		}

		int [][] sensorLocation = Sensor.sensorLocation;
		int [][] sensorDirection = Sensor.sensorDirection;
		int sensorDirectionValueX, sensorDirectionValueY;
		int[] obstacleSensed = new int[] {-1, -1, -1, -1, -1, -1};

		if (CommunicationSocket.isDebug()) {
			System.out.print("The SensorValues are: \n");
			for (int i = 0; i < SensorLoc.NO_OF_SENSORS; i ++) {
				System.out.print(sensorValues[i]);
				if (i != sensorValues.length - 1) System.out.print(" ");
			}
			System.out.println("\n");
		}

		this.setGridDist(arenaMap);

		for (int sensorPosition = 0; sensorPosition < SensorLoc.NO_OF_SENSORS; sensorPosition++) {
			double value = Double.parseDouble(sensorValues[sensorPosition]);
			switch(sensorPosition) {
				case SensorLoc.FRONT_RIGHT:
				case SensorLoc.FRONT_CENTER:
				case SensorLoc.FRONT_LEFT:
					sensorDirectionValueX = sensorDirection[Sensor.FRONT][Position.X];
					sensorDirectionValueY = sensorDirection[Sensor.FRONT][Position.Y];
					break;

				case SensorLoc.RIGHT_BEHIND:
				case SensorLoc.RIGHT_FRONT:
					sensorDirectionValueX = sensorDirection[Sensor.RIGHT][Position.X];
					sensorDirectionValueY = sensorDirection[Sensor.RIGHT][Position.Y];
					break;
				case SensorLoc.LEFT_FRONT:
					sensorDirectionValueX = sensorDirection[Sensor.LEFT][Position.X];
					sensorDirectionValueY = sensorDirection[Sensor.LEFT][Position.Y];
					break;
				default:
					if (sensorPosition < sensorValues.length - 1) {
						sensorDirectionValueX = sensorDirection[Sensor.RIGHT][Position.X];
						sensorDirectionValueY = sensorDirection[Sensor.RIGHT][Position.Y];
					} else {
						sensorDirectionValueX = sensorDirection[Sensor.LEFT][Position.X];
						sensorDirectionValueY = sensorDirection[Sensor.LEFT][Position.Y];
					}

			}

			// Get the current sensor's threshold.
			double[] sensorThreshold = SensorConfig.SENSOR_RANGE_THRESHOLD[sensorPosition];

			// If h = 2 means that we are sensing two cells ahead. Hence if h = n then we are sensing n cells ahead.
			for (int cellsAhead = 0; cellsAhead < sensorThreshold.length; cellsAhead++) { // The value of h determines how many obstacles are to be processed.
				int g = cellsAhead + 1; // Current sensor value pointer + 1. (g min value is 1)

				// Update the sensorLocation offset from x position and the grid in the direction of the sensor
				// Obstacle x position: Origin x + sensor location +
				// (sensor direction [this value represents where the sensor is facing (x = 1 means facing EAST, -1 means facing WEST)] * how many cells ahead)
				int x = this.x + sensorLocation[sensorPosition][Position.X] + (sensorDirectionValueX * g); // i is the current sensor[x]

				// Obstacle y position: Origin y + sensor location +
				// (sensor direction [this value represents where the sensor is facing (y = 1 means facing SOUTH, -1 means facing NORTH)] * how many cells ahead)
				int y = this.y + sensorLocation[sensorPosition][Position.Y] + (sensorDirectionValueY * g); // current sensor[y]

				// Get the old distance of the grid being updated
				double oldDistance = arenaMap.getDist(x, y);

				// Detected an obstacle (i.e. if current sensor value is less than or equal to current sensor threshold).
				if (value <= sensorThreshold[cellsAhead]) {
					if (sensorPosition == SensorLoc.LEFT_FRONT) { // For LEFT LONG RANGE sensor case.
						if (isSensorValueMoreAccurate(g + 2, oldDistance)) { // is L < R (new_dist < old_dist).
							System.out.println("LR new_dist: " + (g + 2) + " old_dist: " + oldDistance);
							System.out.println("Current bot pos (" + this.x + ", " + this.y + ") and direction of " + direction);
							System.out.println("Obstacle x: " + x + " y: " + y);
							arenaMap.setGrid(x, y, RobotConfig.OBSTACLE); // Set grid cell to an obstacle
							arenaMap.setDist(x, y, g+2);
						}
					} else { // All other NON LEFT LONG RANGE sensor case.
						if (isSensorValueMoreAccurate(g, oldDistance)) { // is L < R (new_dist < old_dist).
							System.out.println("Sensor " + sensorPosition + " new_dist: " + g + " old_dist: " + oldDistance);
							System.out.println("Current bot pos (" + this.x + ", " + this.y + ") and direction of " + direction);
							System.out.println("Obstacle x: " + x + " y: " + y);
							arenaMap.setGrid(x, y, RobotConfig.OBSTACLE); // Set grid cell to an obstacle
							arenaMap.setDist(x, y, g);
						}
					}

					obstacleSensed[sensorPosition] = g;
					break;
				}

				else {
					if (sensorPosition == SensorLoc.LEFT_FRONT) {
						if (isSensorValueMoreAccurate(g + 1, oldDistance)) { // is L < R (new_dist < old_dist).
							arenaMap.setGrid(x, y, RobotConfig.EXPLORED);
							arenaMap.setDist(x, y, g+1);
						}
					} else {
						if (isSensorValueMoreAccurate(g, oldDistance)) { // is L < R (new_dist < old_dist).
							arenaMap.setGrid(x, y, RobotConfig.EXPLORED);
							arenaMap.setDist(x, y, g);
						}
					}
				}
			}
		}
		arenaMap.print();
		System.arraycopy(obstacleSensed, 0, this.isObstacle, 0, obstacleSensed.length);
		isValidObstacleValue = true;
		return obstacleSensed; // int[] array whose value each represent how far away each obstacle are away from the robot + 1.
	}

	private void setGridDist(ArenaMap map) {
		int distX, distY;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				distX = i + x;
				distY = j + y;
				map.setDist(distX, distY, -1);
				if (!(map.getGrid(distX, distY).equals(RobotConfig.START_POINT) ||
						map.getGrid(distX, distY).equals(RobotConfig.END_POINT))) {
					map.setGrid(distX, distY, RobotConfig.EXPLORED);
				}
			}
		}
	}
	private boolean isSensorValueMoreAccurate(double newDistance, double oldDistance) { return newDistance <= oldDistance; }

	public ArenaMap getMap() { return map; }

	public void setWayPoint(int x, int y) { this.map.setWayPoint(x, y); }

	public int[] getWayPoint() { return map.getWayPoint(); }

	public void setTrueMap(ArenaMap map) { this.sensor.setTrueMap(map); }

	public void setKnownMap(ArenaMap map) { this.map = map; }

	public String[] getMDFString() { return map.getMDFString(); }

	protected void makeInvalid() { isValidObstacleValue = false; }

	protected abstract String[] getSensorValues();
	public abstract void forward(int stepCount);
	public abstract void rotateRight();
	public abstract void rotateLeft();
	public abstract void rightAlign();
	public abstract void displayMessage(String message);
	public abstract boolean captureImage(int[][] imagePosition);
}
