package ntu.mdp.group.three.map;

import ntu.mdp.group.three.config.ArenaConfig;
import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.config.Position;

import java.util.Random;

public class ArenaMap {

	private String[][] grid = new String[ArenaConfig.ARENA_WIDTH][ArenaConfig.ARENA_HEIGHT];
	public double[][] distanceMemory = new double[ArenaConfig.ARENA_WIDTH][ArenaConfig.ARENA_HEIGHT];
	private int[] wayPoint = new int[] {-1, -1};
	private String[] MDPString = new String[3];
	private boolean updated = true;

	public static Random random = new Random();

	public ArenaMap() {
		resetMap();
	}

	public ArenaMap(String[][] grid) {
		initializeMap(grid);
	}

	public ArenaMap copy() {
		ArenaMap arenaMap = new ArenaMap(this.grid);
		arenaMap.setWayPoint(this.wayPoint[Position.X], this.wayPoint[Position.Y]);
		return arenaMap;
	}

	public void print() {
		System.out.println("The current map is: \n");
		System.out.print("  ");
		for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++)
			System.out.print(((i <= 9) ? "  " : " ") + i + "|");
		System.out.println();

		String gridType, gridTypeChar = " ";
		for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
			for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
				if (i == 0) System.out.print(j + ((j > 9) ? "" : " "));
				gridType = grid[i][j];

				if (i != ArenaConfig.ARENA_WIDTH - 1) {
					if (gridType.equals(RobotConfig.EXPLORED) ||
							gridType.equals(RobotConfig.OBSTACLE) ||
							gridType.equals(RobotConfig.WAY_POINT) ||
							gridType.equals(RobotConfig.END_POINT)) {

						switch (gridType) {
							case RobotConfig.EXPLORED:
								gridTypeChar = String.format("%3s|", " ");
								break;
							case RobotConfig.OBSTACLE:
								gridTypeChar = String.format("%3s|", "X");
								break;
							case RobotConfig.WAY_POINT:
								gridTypeChar = String.format("%3s|", "W");
								break;
							case RobotConfig.END_POINT:
								gridTypeChar = String.format("%3s|", "E");
								break;
						}
					} else {
						if(gridType.equals(RobotConfig.UNEXPLORED)) {
							gridTypeChar = String.format("%3s|", "O");
						} else if(gridType.equals(RobotConfig.START_POINT)) {
							gridTypeChar = String.format("%3s|", "S");
						}
					}
				} else {
					switch (gridType) {
						case RobotConfig.EXPLORED:
							gridTypeChar = String.format("%3s|", " ");
							break;
						case RobotConfig.OBSTACLE:
							gridTypeChar = String.format("%3s|", "X");
							break;
						case RobotConfig.WAY_POINT:
							gridTypeChar = String.format("%3s|", "W");
							break;
						case RobotConfig.END_POINT:
							gridTypeChar = String.format("%3s|", "E");
							break;
						case RobotConfig.UNEXPLORED:
							gridTypeChar = String.format("%3s|", "O");
							break;
						case RobotConfig.START_POINT:
							gridTypeChar = String.format("%3s|", "S");
							break;
					}
				}
				System.out.printf("%3s", gridTypeChar);
			}
			System.out.println();
		}
		System.out.println();
	}

	public void initializeMap(String[][] grid) {
		for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
			for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
				this.setGrid(i, j, grid[i][j]);
			}
		}
	}

	public void resetMap() {
		for (int i = 0; i< ArenaConfig.ARENA_WIDTH; i++) {
			for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
				if (i < ArenaConfig.START_POINT_WIDTH && j < ArenaConfig.START_POINT_HEIGHT) {
					this.setGrid(i, j, RobotConfig.START_POINT);
					this.setDist(i, j, 0);
				} else if (i >= ArenaConfig.ARENA_WIDTH - ArenaConfig.END_POINT_WIDTH && j >= ArenaConfig.ARENA_HEIGHT - ArenaConfig.END_POINT_HEIGHT) {
					this.setGrid(i, j, RobotConfig.END_POINT);
					this.setDist(i, j, 0);
				} else {
					this.setGrid(i, j, RobotConfig.UNEXPLORED);
					this.setDist(i, j, 999999);
				}
			}
		}
	}

	public void setDist(int x, int y, double value) {
		if ((x >= 0) && (y >= 0) &&
				(x < ArenaConfig.ARENA_WIDTH) &&
				(y < ArenaConfig.ARENA_HEIGHT)
		) distanceMemory[x][y] = value;
	}

	public void setGrid(int x, int y, String command) {
		if (x < 0 || x >= ArenaConfig.ARENA_WIDTH || y < 0 || y >= ArenaConfig.ARENA_HEIGHT) return;
		for (int i = 0; i < RobotConfig.GRID_IDENTIFIER.length; i++) {
			if (command.toUpperCase().compareTo(RobotConfig.GRID_IDENTIFIER[i].toUpperCase()) == 0) {
				updated = true;
				if (i == 3) this.setWayPoint(x, y);
				else grid[x][y] = command;
				return;
			}
		}
	}

	public void generateMap(boolean isRandom) {
		int k = 0;
		for (int i = 0; i< ArenaConfig.ARENA_WIDTH; i++) {
			for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
				if (i < ArenaConfig.START_POINT_WIDTH && j < ArenaConfig.START_POINT_HEIGHT)
					this.setGrid(i, j, RobotConfig.START_POINT);
				else if (i >= ArenaConfig.ARENA_WIDTH - ArenaConfig.END_POINT_WIDTH && j >= ArenaConfig.ARENA_HEIGHT - ArenaConfig.END_POINT_HEIGHT)
					this.setGrid(i, j, RobotConfig.END_POINT);
				else this.setGrid(i, j, RobotConfig.EXPLORED);
			}
		}

		if (isRandom) {
			while (k <= ArenaConfig.MAX_OBSTACLE_COUNT) {
				int x = random.nextInt(ArenaConfig.ARENA_WIDTH);
				int y = random.nextInt(ArenaConfig.ARENA_HEIGHT);
				if (this.getGrid(x, y).compareTo(RobotConfig.EXPLORED) == 0) {
					this.setGrid(x, y, RobotConfig.OBSTACLE);
					k++;
				}
			}
		}

	}

	public void setWayPoint(int x, int y) {
		boolean verbose = new Exception().getStackTrace()[1].getClassName().equals("robot.Robot");
		if (x >= ArenaConfig.ARENA_WIDTH - 1 || x <= 0 || y >= ArenaConfig.ARENA_HEIGHT - 1 || y <= 0 ||
				(this.getGrid(x, y) != null && this.getGrid(x, y).compareTo(RobotConfig.UNEXPLORED) != 0 &&
						this.getGrid(x, y).compareTo(RobotConfig.EXPLORED) != 0)) {
			if (!(wayPoint[0] == -1 && wayPoint[1] == -1)) {
				this.wayPoint[0] = -1;
				this.wayPoint[1] = -1;
				if (verbose) {
					System.out.println("The current waypoint is set as: " + "-1" + "," + "-1");
				}
			}
			return;
		}
		this.wayPoint[0] = x;
		this.wayPoint[1] = y;
		if (verbose) {
			System.out.println("Successfully set the waypoint: " + x + "," + y);
		}
	}

	public int[] getWayPoint() {
		return wayPoint;
	}

	public String[][] getGridMap() {
		return grid;
	}

	public double getDist(int x, int y) {
		if (x < 0 || x >= ArenaConfig.ARENA_WIDTH || y < 0 || y >= ArenaConfig.ARENA_HEIGHT) return 1;
		return distanceMemory[x][y];
	}

	public String getGrid(int x, int y) {
		if (x < 0 || x >= ArenaConfig.ARENA_WIDTH || y < 0 || y >= ArenaConfig.ARENA_HEIGHT) return RobotConfig.OBSTACLE;
		return grid[x][y];
	}

	public static boolean compareArenaMaps(ArenaMap arenaMap, ArenaMap otherArenaMap) {
		String[][] arenaMapGrid = arenaMap.getGridMap();
		String[][] otherArenaMapGridMap = otherArenaMap.getGridMap();
		for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++)
			for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++)
				if (arenaMapGrid[i][j].compareTo(otherArenaMapGridMap[i][j]) != 0) return false;

		return true;
	}

	public String[] getMDFString() {
		if (!updated) return this.MDPString;

		StringBuilder MDFBitStringPart1 = new StringBuilder();
		StringBuilder MDFBitStringPart2 = new StringBuilder();

		MDFBitStringPart1.append("11");
		String[] MDFHexString = new String[] {"","",""};

		for (int j = 0; j < ArenaConfig.ARENA_WIDTH; j++) {
			for (int i = 0; i < ArenaConfig.ARENA_HEIGHT; i++) {

				if (grid[j][i].compareTo(RobotConfig.OBSTACLE) == 0) {
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("1");

				} else if (grid[j][i].compareTo(RobotConfig.UNEXPLORED) == 0) {
					MDFBitStringPart1.append("0");
				} else {
					MDFBitStringPart1.append("1");
					MDFBitStringPart2.append("0");
				}

			}
		}
		MDFBitStringPart1.append("11");

		for (int i = 0; i < MDFBitStringPart1.length(); i += 4) {
			MDFHexString[0] += Integer.toString(Integer.parseInt(MDFBitStringPart1.substring(i, i + 4), 2), 16);
		}

		if ((MDFBitStringPart2.length() % 4) != 0) { // Only pad if the MDF Bit string is not a multiple of 4
			MDFBitStringPart2.insert(0, "0".repeat(4 - (MDFBitStringPart2.length() % 4)));
//			MDFBitStringPart2.insert(0, StringUtils.repeat("0", 4 - (MDFBitStringPart2.length() % 4)));
		}

		for (int i = 0; i < MDFBitStringPart2.length(); i += 4) {
			MDFHexString[2] += Integer.toString(Integer.parseInt(MDFBitStringPart2.substring(i, i + 4), 2), 16);
		}

		int length = 0;
		for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
			for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
				if (!grid[i][j].equals(RobotConfig.UNEXPLORED)) length++;
			}
		}

		MDFHexString[1] = Integer.toString(length);

		this.MDPString = MDFHexString;
		this.updated = false;
		return MDFHexString;
	}

	public void setGrid(String[][] grid) {
		this.grid = grid;
	}
}
