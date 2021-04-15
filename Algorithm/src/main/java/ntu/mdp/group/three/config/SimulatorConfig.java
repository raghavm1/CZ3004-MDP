package ntu.mdp.group.three.config;

public class SimulatorConfig {

    public final static boolean IS_RANDOM_MAP = false;

    public final static String ROBOT_N_IMAGE_PATH = "robot/n-robot-vacuum-cleaner.png";
    public final static String ROBOT_S_IMAGE_PATH = "robot/s-robot-vacuum-cleaner.png";
    public final static String ROBOT_E_IMAGE_PATH = "robot/e-robot-vacuum-cleaner.png";
    public final static String ROBOT_W_IMAGE_PATH = "robot/w-robot-vacuum-cleaner.png";


    public final static String[] ROBOT_IMAGE_PATHS = new String[] {
            ROBOT_N_IMAGE_PATH,
            ROBOT_E_IMAGE_PATH,
            ROBOT_S_IMAGE_PATH,
            ROBOT_W_IMAGE_PATH
    };

    public final static int ROBOT_HEIGHT = 100;
    public final static int ROBOT_WIDTH = 100;

    public final static int DEFAULT_SIMULATOR_X = 110;
    public final static int DEFAULT_SIMULATOR_Y = 110;

    public final static int MIN_WINDOW_WIDTH = 900;
    public final static int MIN_WINDOW_HEIGHT = 600;

    public final static String UNEXPLORED_CELL_COLOR = "#141517";
    public final static String EXPLORED_CELL_COLOR = "#a5adc0";
    public final static String OBSTACLE_CELL_COLOR = "#f64045";
    public final static String WAY_POINT_CELL_COLOR = "#1f5eff";
    public final static String START_POINT_CELL_COLOR = "#323741";
    public final static String END_POINT_CELL_COLOR = "#32604c";

    public final static String[] GRID_CELL_COLORS = new String[] {
            UNEXPLORED_CELL_COLOR,
            EXPLORED_CELL_COLOR,
            OBSTACLE_CELL_COLOR,
            WAY_POINT_CELL_COLOR,
            START_POINT_CELL_COLOR,
            END_POINT_CELL_COLOR
    };

}
