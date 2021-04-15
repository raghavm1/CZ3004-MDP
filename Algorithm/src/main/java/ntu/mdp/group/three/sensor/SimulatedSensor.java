package ntu.mdp.group.three.sensor;


import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.map.ArenaMap;

public class SimulatedSensor extends Sensor {

	private ArenaMap trueMap;
	private String sensorValue;

	public SimulatedSensor() {
		trueMap = new ArenaMap();
		trueMap.generateMap(SimulatorConfig.IS_RANDOM_MAP);
	}

	private void updateSensorValues(int x, int y) {
		String [] sensorValue = new String[SensorLoc.NO_OF_SENSORS];
		for (int i = 1; i <= SensorConfig.SHORT_SENSOR_MAX_RANGE; i ++ ) {
			if (sensorValue[SensorLoc.FRONT_RIGHT] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.FRONT_RIGHT][Position.X] + i * sensorDirection[Sensor.FRONT][Position.X],
							y + sensorLocation[SensorLoc.FRONT_RIGHT][Position.Y] + i * sensorDirection[Sensor.FRONT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.FRONT_RIGHT].length) {
				sensorValue[SensorLoc.FRONT_RIGHT] = getSensorValue(SensorLoc.FRONT_RIGHT, i);
			}

			if (sensorValue[SensorLoc.FRONT_CENTER] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.FRONT_CENTER][Position.X] + i * sensorDirection[Sensor.FRONT][Position.X],
							y + sensorLocation[SensorLoc.FRONT_CENTER][Position.Y] + i * sensorDirection[Sensor.FRONT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.FRONT_CENTER].length) {
				sensorValue[SensorLoc.FRONT_CENTER] = getSensorValue(SensorLoc.FRONT_CENTER, i);
			}

			if (sensorValue[SensorLoc.FRONT_LEFT] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.FRONT_LEFT][Position.X] + i * sensorDirection[Sensor.FRONT][Position.X],
							y + sensorLocation[SensorLoc.FRONT_LEFT][Position.Y] + i * sensorDirection[Sensor.FRONT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.FRONT_LEFT].length) {
				sensorValue[SensorLoc.FRONT_LEFT] = getSensorValue(SensorLoc.FRONT_LEFT, i);
			}

			if (sensorValue[SensorLoc.RIGHT_BEHIND] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.RIGHT_BEHIND][Position.X] + i * sensorDirection[Sensor.RIGHT][Position.X],
							y + sensorLocation[SensorLoc.RIGHT_BEHIND][Position.Y] + i * sensorDirection[Sensor.RIGHT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.RIGHT_BEHIND].length) {
				sensorValue[SensorLoc.RIGHT_BEHIND] = getSensorValue(SensorLoc.RIGHT_BEHIND, i);
			}

			if (sensorValue[SensorLoc.RIGHT_FRONT] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.RIGHT_FRONT][Position.X] + i * sensorDirection[Sensor.RIGHT][Position.X],
							y + sensorLocation[SensorLoc.RIGHT_FRONT][Position.Y] + i * sensorDirection[Sensor.RIGHT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.RIGHT_FRONT].length) {
				sensorValue[SensorLoc.RIGHT_FRONT] = getSensorValue(SensorLoc.RIGHT_FRONT, i);
			}
		}

		for (int i = 1; i <= SensorConfig.FAR_SENSOR_MAX_RANGE; i ++ ) {
			if (sensorValue[SensorLoc.LEFT_FRONT] == null &&
					trueMap.getGrid(
							x + sensorLocation[SensorLoc.LEFT_FRONT][Position.X] + i * sensorDirection[Sensor.LEFT][Position.X],
							y + sensorLocation[SensorLoc.LEFT_FRONT][Position.Y] + i * sensorDirection[Sensor.LEFT][Position.Y]
					).compareTo(RobotConfig.OBSTACLE) == 0
					&& i <= SensorConfig.SENSOR_RANGE_THRESHOLD[SensorLoc.LEFT_FRONT].length) {
				sensorValue[SensorLoc.LEFT_FRONT] = getSensorValue(SensorLoc.LEFT_FRONT, i);
			}
		}

		sensorValue = padSensorValue(sensorValue);
		this.sensorValue = String.join(" ", sensorValue);
	}

	private String getSensorValue(int s, int i) { return String.valueOf(SensorConfig.SENSOR_RANGE_THRESHOLD[s][i - 1] - 1); }

	public String[] getAllSensorsValue(int x, int y, int direction) {
		updateSensorValues(x, y);
		return sensorValue.split(" ");
	}

	private String[] padSensorValue(String [] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				arr[i] = "" + (SensorConfig.FAR_SENSOR_MAX_RANGE * 10 + SensorConfig.FAR_SENSOR_OFFSET + 1) + ".0";
			}
		}
		return arr;
	}

	public ArenaMap getTrueMap() {
		return trueMap;
	}

	public void setTrueMap(ArenaMap map) {
		trueMap = map;
	}

}