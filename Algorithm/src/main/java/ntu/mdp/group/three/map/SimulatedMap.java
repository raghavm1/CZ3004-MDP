package ntu.mdp.group.three.map;

import main.Main;

public class SimulatedMap {

	private ArenaMap arenaMap;
	private static SimulatedMap simulatedMap = null;

	private SimulatedMap(ArenaMap arenaMap) {
		this.arenaMap = arenaMap;
	}

	public static SimulatedMap getInstance(ArenaMap map) {
		if (simulatedMap == null) simulatedMap = new SimulatedMap(map);
		return simulatedMap;
	}

	public void setArenaMap(ArenaMap arenaMap) {
		Main.updateMapOnUI(this.arenaMap, arenaMap);
		this.arenaMap = arenaMap.copy();
	}

	public ArenaMap getArenaMap() {
		return arenaMap;
	}

}
