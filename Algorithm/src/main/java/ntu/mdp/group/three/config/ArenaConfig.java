package ntu.mdp.group.three.config;

public class ArenaConfig {

    public final static int ARENA_WIDTH = 20;
    public final static int ARENA_HEIGHT = 15;

    public final static int START_POINT_WIDTH = 3;
    public final static int START_POINT_HEIGHT = 3;

    public final static int END_POINT_WIDTH = 3;
    public final static int END_POINT_HEIGHT = 3;

    public final static int GRID_WIDTH = 40;
    public final static int GRID_HEIGHT = 40;
    public final static int MARGIN_TOP = 40;
    public final static int MARGIN_LEFT = 40;

    public final static int HEIGHT = ARENA_HEIGHT * GRID_HEIGHT + MARGIN_TOP;
    public final static int WIDTH = ARENA_WIDTH * GRID_WIDTH + MARGIN_LEFT;

    public final static int MAX_OBSTACLE_COUNT = 16;

}
