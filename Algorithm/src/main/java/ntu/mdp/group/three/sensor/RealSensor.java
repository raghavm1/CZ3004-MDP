package ntu.mdp.group.three.sensor;


import ntu.mdp.group.three.config.SensorLoc;
import ntu.mdp.group.three.map.ArenaMap;

public class RealSensor extends Sensor {

	private int x;
	private int y;
	private int direction;
	private String[] sensors = new String[SensorLoc.NO_OF_SENSORS];

	public RealSensor() {
		super();
	}

	@Override
	public String[] getAllSensorsValue(int x, int y, int direction) {
		if (this.x == x && this.y == y && this.direction == direction) {
			this.x = x;
			this.y = y;
			this.direction = y;
			return sensors;
		}
		return null;
	}

	@Override
	public ArenaMap getTrueMap() {
		return null;
	}

	@Override
	public void setTrueMap(ArenaMap arenaMap) { }

}