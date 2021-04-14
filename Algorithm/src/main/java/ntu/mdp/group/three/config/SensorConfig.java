package ntu.mdp.group.three.config;

public class SensorConfig {

    // Tested from regexr.com
    public final static String INT_SENSOR_PATTERN = "\\d+[|]\\d+[|]\\d+[|]\\d+[|]\\d+[|]\\d+[|]\\d+";
    public final static String FLOAT_SENSOR_PATTERN = "\\d+[.]\\d+[|]\\d+[.]\\d+[|]\\d+[.]\\d+[|]\\d+[.]\\d+[|]\\d+[.]\\d+[|]\\d+[.]\\d+[|]\\d+";

    public final static double[][] SENSOR_RANGE_THRESHOLD = {
            {10.5, 19},               // Front Right
            {11, 19},               // Front Center
            {11, 20.5},               // Front Left
            {10.5, 20.5},               // Right Behind
            {10.5, 20.5},               // Right Front
            {14.5, 23.25, 33.5, 44, 52.25}    // Left LR
    };

    // Sensor constants used only in Simulator
    public final static int SHORT_SENSOR_MAX_RANGE = 3; // This is in number of grid.
    public final static int FAR_SENSOR_MAX_RANGE = 7; // This is in number of grid.
    public final static int FAR_SENSOR_OFFSET = 13; // This is in cm.

    public final static int[][] SENSOR_DIRECTION = new int [][]{
            {0, -1}, // NORTH where x doesn't change and y is -1, hence facing NORTH of Algorithm map orientation.
            {1, 0},  // EAST where x is 1, hence facing the EAST side of the Algorithm map data structure/orientation.
            {0, 1},  // SOUTH where y is 1, hence facing SOUTH of the Algorithm map data structure/orientation.
            {-1, 0}  // WEST where x is -1, hence facing WEST of the Algorithm map data structure/orientation.
    };

}
