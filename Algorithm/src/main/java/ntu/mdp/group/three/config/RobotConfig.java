package ntu.mdp.group.three.config;

public class RobotConfig {

    public final static int SPEED = 1;
    public final static int PERCENTAGE = 100;
    public final static int TIME = -1;
    public final static int DELAY = 10;
    public final static boolean USING_IMAGE_REC = false;

    public final static int[][] DEFAULT_CAPTURE = new int[][] {
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1}
    };

    public final static String UNEXPLORED = "Unexplored";
    public final static String EXPLORED = "Explored";
    public final static String OBSTACLE = "Obstacle";
    public final static String WAY_POINT = "Waypoint";
    public final static String START_POINT = "Startpoint";
    public final static String END_POINT = "Endpoint";

    public final static String[] GRID_IDENTIFIER = new String[] {
            UNEXPLORED, EXPLORED, OBSTACLE, WAY_POINT, START_POINT, END_POINT
    };

    public final static int[] START = {1,1};
    public final static int[] END = {18,13};

//    public final static String RPI_IP_ADDRESS = "127.0.0.1";
    public final static String RPI_IP_ADDRESS = "192.168.3.1";
    public final static int PORT = 8080;
    public final static int BUFFER_SIZE = 512;

    public final static String FASTEST_PATH = "SP|";
    public final static String START_EXPLORATION = "SE|";
    public final static String IMAGE_STOP = "I";
    public final static String INITIALISING = "starting";
    public final static String SEND_ARENA = "SendArena";
    public final static String HANDLE_MDF_REQUEST = "MDF|";
    public final static String SENSE_ALL = "S|";
    public final static String SET_WAY_POINT = "waypoint";
    public final static String TURN_LEFT = "L|";
    public final static String TURN_RIGHT = "W|";
    public final static String RIGHT_ALIGN = "AR|";
    public final static String END_TOUR = "D";
    public final static String IMAGE_ACKNOWLEDGEMENT = "D";

}

