package com.example.mdp3_android.map;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.R;
import com.example.mdp3_android.helper.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Maze extends View {

    public Maze(Context c) {
        super(c);
        initMap();
    }

    SharedPreferences sharedPreferences;
    private static JSONObject receivedJsonObject = new JSONObject();
    private static JSONObject backupmazeInfo;

    private static boolean autoUpdate = false;
    private static boolean canDrawRobot = false;
    private static boolean setWaypointStatus = false;
    private static boolean startCoordStatus = false;
    private static boolean setObstacleStatus = false;
    private static boolean unSetCellStatus = false;
    private static boolean setExploredStatus = false;
    private static boolean validPosition = false;

    private Paint black = new Paint();
    private Paint obstacleColor = new Paint();
    private Paint robotColor = new Paint();
    private Paint endGoalColor = new Paint();
    private Paint startPointColor = new Paint();
    private Paint waypointCoordColor = new Paint();
    private Paint unexploredCellColor = new Paint();
    private Paint exploredCellColor = new Paint();
    private Paint arrowColor = new Paint();
    private Paint fastestPathColor = new Paint();

    private static String robotDirection = Constants.NONE;
    private static int[] startCoord = new int[]{-1, -1};
    private static int[] curCoord = new int[]{-1, -1};
    private static int[] oldCoord = new int[]{-1, -1};
    private static int[] waypointCoord = new int[]{-1, -1};
    private static ArrayList<String[]> arrowCoord = new ArrayList<>();
    private static ArrayList<int[]> obstacleCoord = new ArrayList<>();
    private Bitmap arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_error);

    private static final int COL = Constants.FIFTEEN;
    private static final int ROW = Constants.TWENTY;
    private static float cellSize;
    private static Cell[][] cells;

    private boolean mapDrawn = false;
    public static String MDFExplorationString;
    public static String MDFObstacleString;


    public Maze(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initMap();
        black.setStyle(Paint.Style.FILL_AND_STROKE);
        obstacleColor.setColor(Color.BLACK);
        robotColor.setColor(Color.GREEN);
        endGoalColor.setColor(Color.RED);
        startPointColor.setColor(Color.CYAN);
        waypointCoordColor.setColor(Color.YELLOW);
        unexploredCellColor.setColor(Color.LTGRAY);
        exploredCellColor.setColor(Color.WHITE);
        arrowColor.setColor(Color.BLACK);
        fastestPathColor.setColor(Color.MAGENTA);
        
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private void initMap() {
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mapDrawn) {
            String[] dummyArrowCoord = new String[3];
            dummyArrowCoord[0] = "1";
            dummyArrowCoord[1] = "1";
            dummyArrowCoord[2] = "dummy";
            arrowCoord.add(dummyArrowCoord);
            this.createCell();
            this.setEndCoordinate(14, 19);
            mapDrawn = true;
        }

        drawCell(canvas);
        drawHorizontalLines(canvas);
        drawVerticalLines(canvas);
        drawGridNumber(canvas);
        if (getCanDrawRobot())
            drawRobot(canvas, curCoord);
        drawArrow(canvas, arrowCoord);
    }

    private void drawCell(Canvas canvas) {
        for (int x = 1; x <= COL; x++)
            for (int y = 0; y < ROW; y++)
                for (int i = 0; i < this.getArrowCoord().size(); i++)
                    if (!cells[x][y].type.equals("image") && cells[x][y].getId() == -1) {
                        canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, cells[x][y].paint);
                    } else {
                        Paint textPaint = new Paint();
                        textPaint.setTextSize(20);
                        textPaint.setColor(Color.WHITE);
                        textPaint.setTextAlign(Paint.Align.CENTER);
                        canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, cells[x][y].paint);
                        canvas.drawText(String.valueOf(cells[x][y].getId()),(cells[x][y].startX+cells[x][y].endX)/2, cells[x][y].endY + (cells[x][y].startY-cells[x][y].endY)/4, textPaint);
                    }

    }
    

    private void drawHorizontalLines(Canvas canvas) {
        for (int y = 0; y <= ROW; y++)
            canvas.drawLine(cells[1][y].startX, cells[1][y].startY - (cellSize / 30), cells[15][y].endX, cells[15][y].startY - (cellSize / 30), black);
    }

    private void drawVerticalLines(Canvas canvas) {
        for (int x = 0; x <= COL; x++)
            canvas.drawLine(cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][0].startY - (cellSize / 30), cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][19].endY + (cellSize / 30), black);
    }
    

    private void drawRobot(Canvas canvas, int[] curCoord) {
        int androidRowCoord = this.convertRow(curCoord[1]);
        for (int y = androidRowCoord; y <= androidRowCoord + 1; y++)
            canvas.drawLine(cells[curCoord[0] - 1][y].startX, cells[curCoord[0] - 1][y].startY - (cellSize / 30), cells[curCoord[0] + 1][y].endX, cells[curCoord[0] + 1][y].startY - (cellSize / 30), robotColor);
        for (int x = curCoord[0] - 1; x < curCoord[0] + 1; x++)
            canvas.drawLine(cells[x][androidRowCoord - 1].startX - (cellSize / 30) + cellSize, cells[x][androidRowCoord - 1].startY, cells[x][androidRowCoord + 1].startX - (cellSize / 30) + cellSize, cells[x][androidRowCoord + 1].endY, robotColor);

        switch (this.getRobotDirection()) {
            case Constants.UP:
                canvas.drawLine(cells[curCoord[0] - 1][androidRowCoord + 1].startX, cells[curCoord[0] - 1][androidRowCoord + 1].endY, (cells[curCoord[0]][androidRowCoord - 1].startX + cells[curCoord[0]][androidRowCoord - 1].endX) / 2, cells[curCoord[0]][androidRowCoord - 1].startY, black);
                canvas.drawLine((cells[curCoord[0]][androidRowCoord - 1].startX + cells[curCoord[0]][androidRowCoord - 1].endX) / 2, cells[curCoord[0]][androidRowCoord - 1].startY, cells[curCoord[0] + 1][androidRowCoord + 1].endX, cells[curCoord[0] + 1][androidRowCoord + 1].endY, black);
                break;
            case Constants.DOWN:
                canvas.drawLine(cells[curCoord[0] - 1][androidRowCoord - 1].startX, cells[curCoord[0] - 1][androidRowCoord - 1].startY, (cells[curCoord[0]][androidRowCoord + 1].startX + cells[curCoord[0]][androidRowCoord + 1].endX) / 2, cells[curCoord[0]][androidRowCoord + 1].endY, black);
                canvas.drawLine((cells[curCoord[0]][androidRowCoord + 1].startX + cells[curCoord[0]][androidRowCoord + 1].endX) / 2, cells[curCoord[0]][androidRowCoord + 1].endY, cells[curCoord[0] + 1][androidRowCoord - 1].endX, cells[curCoord[0] + 1][androidRowCoord - 1].startY, black);
                break;
            case Constants.RIGHT:
                canvas.drawLine(cells[curCoord[0] - 1][androidRowCoord - 1].startX, cells[curCoord[0] - 1][androidRowCoord - 1].startY, cells[curCoord[0] + 1][androidRowCoord].endX, cells[curCoord[0] + 1][androidRowCoord - 1].endY + (cells[curCoord[0] + 1][androidRowCoord].endY - cells[curCoord[0] + 1][androidRowCoord - 1].endY) / 2, black);
                canvas.drawLine(cells[curCoord[0] + 1][androidRowCoord].endX, cells[curCoord[0] + 1][androidRowCoord - 1].endY + (cells[curCoord[0] + 1][androidRowCoord].endY - cells[curCoord[0] + 1][androidRowCoord - 1].endY) / 2, cells[curCoord[0] - 1][androidRowCoord + 1].startX, cells[curCoord[0] - 1][androidRowCoord + 1].endY, black);
                break;
            case Constants.LEFT:
                canvas.drawLine(cells[curCoord[0] + 1][androidRowCoord - 1].endX, cells[curCoord[0] + 1][androidRowCoord - 1].startY, cells[curCoord[0] - 1][androidRowCoord].startX, cells[curCoord[0] - 1][androidRowCoord - 1].endY + (cells[curCoord[0] - 1][androidRowCoord].endY - cells[curCoord[0] - 1][androidRowCoord - 1].endY) / 2, black);
                canvas.drawLine(cells[curCoord[0] - 1][androidRowCoord].startX, cells[curCoord[0] - 1][androidRowCoord - 1].endY + (cells[curCoord[0] - 1][androidRowCoord].endY - cells[curCoord[0] - 1][androidRowCoord - 1].endY) / 2, cells[curCoord[0] + 1][androidRowCoord + 1].endX, cells[curCoord[0] + 1][androidRowCoord + 1].endY, black);
                break;
            default:
                break;
        }
    }

    private void drawGridNumber(Canvas canvas) {
        for (int x = 1; x <= COL; x++) {
            if (x > 9)
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 5), cells[x][20].startY + (cellSize / 3), black);
            else
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 3), cells[x][20].startY + (cellSize / 3), black);
        }
        for (int y = 0; y < ROW; y++) {
            if ((20 - y) > 9)
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 2), cells[0][y].startY + (cellSize / 1.5f), black);
            else
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 1.5f), cells[0][y].startY + (cellSize / 1.5f), black);
        }
    }

    public void drawImageRec(int x, int y, int id) {
        cells[x+1][19-y].setType("image");
        cells[x+1][19-y].setId(id);
        this.invalidate();
    }

    private ArrayList<String[]> getArrowCoord() {
        return arrowCoord;
    }

    public String getRobotDirection() {
        return robotDirection;
    }

    public void setAutoUpdate(boolean autoUpdate) throws JSONException {
        if (!autoUpdate)
            backupmazeInfo = this.getReceivedJsonObject();
        else {
            setReceivedJsonObject(backupmazeInfo);
            backupmazeInfo = null;
            this.updatemazeInfo();
        }
        Maze.autoUpdate = autoUpdate;
    }



    public JSONObject getReceivedJsonObject() {
        return receivedJsonObject;
    }

    public void setReceivedJsonObject(JSONObject receivedJsonObject) {
        Maze.receivedJsonObject = receivedJsonObject;
        backupmazeInfo = receivedJsonObject;
    }



    private void createCell() {
        cells = new Cell[COL + 1][ROW + 1];
        this.calculateDimension();
        cellSize = this.getCellSize();

        for (int x = 0; x <= COL; x++)
            for (int y = 0; y <= ROW; y++)
                cells[x][y] = new Cell(x * cellSize + (cellSize / 30), y * cellSize + (cellSize / 30), (x + 1) * cellSize, (y + 1) * cellSize, unexploredCellColor, "unexplored");
    }

    public void setEndCoordinate(int col, int row) {
        row = this.convertRow(row);
        for (int x = col - 1; x <= col + 1; x++)
            for (int y = row - 1; y <= row + 1; y++)
                cells[x][y].setType("end");
    }

    public void setStartCoordinate(int col, int row) {
        startCoord[0] = col;
        startCoord[1] = row;
        String direction = getRobotDirection();
        if(direction.equals(Constants.NONE)) {
            direction = Constants.UP;
        }
        if (this.getStartCoordStatus())
            this.setCurCoord(col, row, direction);
    }

    private int[] getStartCoord() {
        return startCoord;
    }

    public void setCurCoord(int col, int row, String direction) {
        curCoord[0] = col;
        curCoord[1] = row;
        this.setRobotDirection(direction);
        this.updateRobotAxis(col, row, direction);

        row = this.convertRow(row);
        for (int x = col - 1; x <= col + 1; x++)
            for (int y = row - 1; y <= row + 1; y++)
                cells[x][y].setType("robot");
    }

    public int[] getCurCoord() {
        return curCoord;
    }

    private void calculateDimension() {
        this.setCellSize(getWidth()/(COL+1));
    }

    private int convertRow(int row) {
        return (20 - row);
    }

    private void setCellSize(float cellSize) {
        Maze.cellSize = cellSize;
    }

    private float getCellSize() {
        return cellSize;
    }

    private void setOldRobotCoord(int oldCol, int oldRow) {
        oldCoord[0] = oldCol;
        oldCoord[1] = oldRow;
        oldRow = this.convertRow(oldRow);
        for (int x = oldCol - 1; x <= oldCol + 1; x++)
            for (int y = oldRow - 1; y <= oldRow + 1; y++)
                cells[x][y].setType("explored");
    }

    private int[] getOldRobotCoord() {
        return oldCoord;
    }

    private void setArrowCoordinate(int col, int row, String arrowDirection) {
        int[] obstacleCoord = new int[]{col, row};
        this.getObstacleCoord().add(obstacleCoord);
        String[] arrowCoord = new String[3];
        arrowCoord[0] = String.valueOf(col);
        arrowCoord[1] = String.valueOf(row);
        arrowCoord[2] = arrowDirection;
        this.getArrowCoord().add(arrowCoord);

        row = convertRow(row);
        cells[col][row].setType("arrow");
    }

    public void setRobotDirection(String direction) {
        sharedPreferences = getContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        robotDirection = direction;
        editor.putString("direction", direction);
        editor.commit();
        this.invalidate();;
    }

    private void updateRobotAxis(int col, int row, String direction) {
        TextView xAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.xCoordTextView);
        TextView yAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.yCoordTextView);
        TextView directionAxisTextView =  ((Activity)this.getContext()).findViewById(R.id.directionTextView);

        xAxisTextView.setText(String.valueOf(col-1));
        yAxisTextView.setText(String.valueOf(row-1));
        directionAxisTextView.setText(direction);
    }

    private void setWaypointCoord(int col, int row) throws JSONException {
        waypointCoord[0] = col;
        waypointCoord[1] = row;

        row = this.convertRow(row);
        cells[col][row].setType("waypoint");

        MainActivity.outputMessage("waypoint", waypointCoord[0]-1, waypointCoord[1]-1);
    }

    private int[] getWaypointCoord() {
        return waypointCoord;
    }

    private void setObstacleCoord(int col, int row) {
        int[] obstacleCoord = new int[]{col, row};
        Maze.obstacleCoord.add(obstacleCoord);
        row = this.convertRow(row);
        cells[col][row].setType("obstacle");
    }

    private ArrayList<int[]> getObstacleCoord() {
        return obstacleCoord;
    }


    private void drawArrow(Canvas canvas, ArrayList<String[]> arrowCoord) {
        RectF rect;

        for (int i = 0; i < arrowCoord.size(); i++) {
            if (!arrowCoord.get(i)[2].equals("dummy")) {
                int col = Integer.parseInt(arrowCoord.get(i)[0]);
                int row = convertRow(Integer.parseInt(arrowCoord.get(i)[1]));
                rect = new RectF(col * cellSize, row * cellSize, (col + 1) * cellSize, (row + 1) * cellSize);
                switch (arrowCoord.get(i)[2]) {
                    case Constants.UP:
                        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_up);
                        break;
                    case Constants.DOWN:
                        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_down);
                        break;
                    case Constants.LEFT:
                        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_left);
                        break;
                    case Constants.RIGHT:
                        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_right);
                        break;
                    default:
                        break;
                }
                canvas.drawBitmap(arrowBitmap, null, rect, null);
            }
        }
    }

    private class Cell {
        float startX, startY, endX, endY;
        Paint paint;
        String type;
        int id = -1;

        private Cell(float startX, float startY, float endX, float endY, Paint paint, String type) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = paint;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
            switch (type) {
                case "obstacle":
                    this.paint = obstacleColor;
                    break;
                case "robot":
                    this.paint = robotColor;
                    break;
                case "end":
                    this.paint = endGoalColor;
                    break;
                case "start":
                    this.paint = startPointColor;
                    break;
                case "waypoint":
                    this.paint = waypointCoordColor;
                    break;
                case "unexplored":
                    this.paint = unexploredCellColor;
                    break;
                case "explored":
                    this.paint = exploredCellColor;
                    break;
                case "arrow":
                    this.paint = arrowColor;
                    break;
                case "fastestPath":
                    this.paint = fastestPathColor;
                    break;
                case "image":
                    this.paint = obstacleColor;
                default:
                    break;
            }
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && this.getAutoUpdate() == false) {
            int column = (int) (event.getX() / cellSize);
            int row = this.convertRow((int) (event.getY() / cellSize));
            ToggleButton setStartPointToggleBtn = ((Activity)this.getContext()).findViewById(R.id.setStartPointToggleBtn);
            ToggleButton setWaypointToggleBtn = ((Activity)this. getContext()).findViewById(R.id.setWayPointToggleBtn);

            if (startCoordStatus) {
                if (canDrawRobot) {
                    int[] startCoord = this.getStartCoord();
                    if (startCoord[0] >= 2 && startCoord[1] >= 2) {
                        startCoord[1] = this.convertRow(startCoord[1]);
                        for (int x = startCoord[0] - 1; x <= startCoord[0] + 1; x++)
                            for (int y = startCoord[1] - 1; y <= startCoord[1] + 1; y++)
                                cells[x][y].setType("unexplored");
                    }
                }
                else
                    canDrawRobot = true;
                this.setStartCoordinate(column, row);
                startCoordStatus = false;
                String direction = getRobotDirection();
                if(direction.equals(Constants.NONE)) {
                    direction = Constants.UP;
                }
                try {
                    int directionInt = 0;
                    if(direction.equals(Constants.UP)){
                        directionInt = 0;
                    } else if(direction.equals(Constants.LEFT)) {
                        directionInt = 3;
                    } else if(direction.equals(Constants.RIGHT)) {
                        directionInt = 1;
                    } else if(direction.equals(Constants.DOWN)) {
                        directionInt = 2;
                    }
                    MainActivity.outputMessage("starting " + "(" + String.valueOf(row-1) + "," + String.valueOf(column-1) + "," + String.valueOf(directionInt) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateRobotAxis(column, row, direction);
                if (setStartPointToggleBtn.isChecked())
                    setStartPointToggleBtn.toggle();
                this.invalidate();
                return true;
            }
            if (setWaypointStatus) {
                int[] waypointCoord = this.getWaypointCoord();
                if (waypointCoord[0] >= 1 && waypointCoord[1] >= 1)
                    cells[waypointCoord[0]][this.convertRow(waypointCoord[1])].setType("unexplored");
                setWaypointStatus = false;
                try {
                    this.setWaypointCoord(column, row);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (setWaypointToggleBtn.isChecked())
                    setWaypointToggleBtn.toggle();
                this.invalidate();
                return true;
            }
            if (setObstacleStatus) {
                this.setObstacleCoord(column, row);
                this.invalidate();
                return true;
            }
            if (setExploredStatus) {
                cells[column][20-row].setType("explored");
                this.invalidate();
                return true;
            }
            if (unSetCellStatus) {
                ArrayList<int[]> obstacleCoord = this.getObstacleCoord();
                cells[column][20-row].setType("unexplored");
                for (int i=0; i<obstacleCoord.size(); i++)
                    if (obstacleCoord.get(i)[0] == column && obstacleCoord.get(i)[1] == row)
                        obstacleCoord.remove(i);
                this.invalidate();
                return true;
            }
        }
        return false;
    }

    public void toggleCheckedBtn(String buttonName) {
        ToggleButton setStartPointToggleBtn = ((Activity)this.getContext()).findViewById(R.id.setStartPointToggleBtn);
        ToggleButton setWaypointToggleBtn = ((Activity)this.getContext()).findViewById(R.id.setWayPointToggleBtn);
        ImageButton obstacleImageBtn = ((Activity)this.getContext()).findViewById(R.id.obstaclesImgBtn);
        ImageButton exploredImageBtn = ((Activity)this.getContext()).findViewById(R.id.exploredImgBtn);
        ImageButton clearImageBtn = ((Activity)this.getContext()).findViewById(R.id.clearImgBtn);

        if (!buttonName.equals("startPointButton"))
            if (setStartPointToggleBtn.isChecked()) {
                this.setStartCoordinateStatus(false);
                setStartPointToggleBtn.toggle();
            }
        if (!buttonName.equals("wayPointButton"))
            if (setWaypointToggleBtn.isChecked()) {
                this.setWaypointStatus(false);
                setWaypointToggleBtn.toggle();
            }
        if (!buttonName.equals("explorationButton"))
            if (exploredImageBtn.isEnabled())
                this.setExploredStatus(false);
        if (!buttonName.equals("obstacleButton"))
            if (obstacleImageBtn.isEnabled())
                this.setSetObstacleStatus(false);
        if (!buttonName.equals("clearButton"))
            if (clearImageBtn.isEnabled())
                this.setUnSetCellStatus(false);
    }

    public void resetMaze() {
        TextView robotStatusTextView =  ((Activity)this.getContext()).findViewById(R.id.robotStatus);
        Switch manualAutoToggleBtn = ((Activity)this.getContext()).findViewById(R.id.manualAutoToggleBtn);
        Switch phoneTiltSwitch = ((Activity)this.getContext()).findViewById(R.id.phoneTiltSwitch);
        updateRobotAxis(1, 1, Constants.NONE);
        robotStatusTextView.setText(Constants.NOT_AVAILABLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        if (manualAutoToggleBtn.isChecked()) {
            manualAutoToggleBtn.toggle();
            manualAutoToggleBtn.setText("MANUAL");
        }
        this.toggleCheckedBtn(Constants.NONE);

        if (phoneTiltSwitch.isChecked()) {
            phoneTiltSwitch.toggle();
            phoneTiltSwitch.setText("TILT OFF");
        }

        receivedJsonObject = null;
        backupmazeInfo = null;
        startCoord = new int[]{-1, -1};
        curCoord = new int[]{-1, -1};
        oldCoord = new int[]{-1, -1};
        robotDirection = Constants.NONE;
        autoUpdate = false;
        arrowCoord = new ArrayList<>();
        obstacleCoord = new ArrayList<>();
        waypointCoord = new int[]{-1, -1};
        mapDrawn = false;
        canDrawRobot = false;
        validPosition = false;

        this.invalidate();
    }


    public void updatemazeInfo() throws JSONException {
        JSONObject mazeInfo = this.getReceivedJsonObject();
        JSONArray infoJsonArray;
        JSONObject infoJsonObject;
        String hexStringExplored, hexStringObstacle, exploredString, obstacleString;
        BigInteger hexBigIntegerExplored, hexBigIntegerObstacle;
        String message;

        if (mazeInfo == null)
            return;

        for(int i=0; i<mazeInfo.names().length(); i++) {
            message = "updatemazeInfo Default message";
            switch (mazeInfo.names().getString(i)) {
                case "map":
                    infoJsonArray = mazeInfo.getJSONArray("map");
                    infoJsonObject = infoJsonArray.getJSONObject(0);

                    hexStringExplored = infoJsonObject.getString("explored");
                    hexBigIntegerExplored = new BigInteger(hexStringExplored, 16);
                    exploredString = hexBigIntegerExplored.toString(2);

                    int x, y;
                    for (int j = 0; j < exploredString.length() - 4; j++) {
                        y = 19 - (j / 15);
                        x = 1 + j - ((19 - y) * 15);
                        if ((String.valueOf(exploredString.charAt(j + 2))).equals("1") && !cells[x][y].type.equals("robot"))
                            cells[x][y].setType("explored");
                        else if ((String.valueOf(exploredString.charAt(j + 2))).equals("0") && !cells[x][y].type.equals("robot"))
                            cells[x][y].setType("unexplored");
                    }

                    int length = infoJsonObject.getInt("length");

                    hexStringObstacle = infoJsonObject.getString("obstacle");
                    hexBigIntegerObstacle = new BigInteger(hexStringObstacle, 16);
                    obstacleString = hexBigIntegerObstacle.toString(2);
                    while (obstacleString.length() < length) {
                        obstacleString = "0" + obstacleString;
                    }
                    setMDFExplorationString(hexStringExplored);
                    setMDFObstacleString(hexStringObstacle);

                    int k = 0;
                    for (int row = ROW - 1; row >= 0; row--)
                        for (int col = 1; col <= COL; col++)
                            if ((cells[col][row].type.equals("explored") || (cells[col][row].type.equals("robot"))) && k < obstacleString.length()) {
                                if ((String.valueOf(obstacleString.charAt(k))).equals("1"))
                                    this.setObstacleCoord(col, 20 - row);
                                k++;
                            }

                    int[] waypointCoord = this.getWaypointCoord();
                    if (waypointCoord[0] >= 1 && waypointCoord[1] >= 1)
                        cells[waypointCoord[0]][20 - waypointCoord[1]].setType("waypoint");
                    break;
                case "robotPosition":
                    if (canDrawRobot)
                        this.setOldRobotCoord(curCoord[0], curCoord[1]);
                    infoJsonArray = mazeInfo.getJSONArray("robotPosition");

                    for (int row = ROW - 1; row >= 0; row--)
                        for (int col = 1; col <= COL; col++)
                            cells[col][row].setType("unexplored");

                    String direction;
                    if (infoJsonArray.getInt(2) == 90) {
                        direction = Constants.RIGHT;
                    } else if (infoJsonArray.getInt(2) == 180) {
                        direction = Constants.DOWN;
                    } else if (infoJsonArray.getInt(2) == 270) {
                        direction = Constants.LEFT;
                    } else {
                        direction = Constants.UP;
                    }
                    this.setStartCoordinate(infoJsonArray.getInt(0), infoJsonArray.getInt(1));
                    this.setCurCoord(infoJsonArray.getInt(0)+2, convertRow(infoJsonArray.getInt(1))-1, direction);
                    canDrawRobot = true;
                    break;
                case "waypoint":
                    infoJsonArray = mazeInfo.getJSONArray("waypoint");
                    infoJsonObject = infoJsonArray.getJSONObject(0);
                    this.setWaypointCoord(infoJsonObject.getInt("x"), infoJsonObject.getInt("y"));
                    setWaypointStatus = true;
                    break;
                case "obstacle":
                    infoJsonArray = mazeInfo.getJSONArray("obstacle");
                    for (int j = 0; j < infoJsonArray.length(); j++) {
                        infoJsonObject = infoJsonArray.getJSONObject(j);
                        this.setObstacleCoord(infoJsonObject.getInt("x"), infoJsonObject.getInt("y"));
                    }
                    message = "No. of Obstacle: " + String.valueOf(infoJsonArray.length());
                    break;
                case "arrow":
                    infoJsonArray = mazeInfo.getJSONArray("arrow");
                    for (int j = 0; j < infoJsonArray.length(); j++) {
                        infoJsonObject = infoJsonArray.getJSONObject(j);
                        if (!infoJsonObject.getString("face").equals("dummy")) {
                            this.setArrowCoordinate(infoJsonObject.getInt("x"), infoJsonObject.getInt("y"), infoJsonObject.getString("face"));
                            message = "Arrow:  (" + String.valueOf(infoJsonObject.getInt("x")) + "," + String.valueOf(infoJsonObject.getInt("y")) + "), face: " + infoJsonObject.getString("face");
                        }
                    }
                    break;
                case "move":
                    infoJsonArray = mazeInfo.getJSONArray("move");
                    infoJsonObject = infoJsonArray.getJSONObject(0);
                    if (canDrawRobot)
                        moveRobot(infoJsonObject.getString("direction"));
                    message = "moveDirection: " + infoJsonObject.getString("direction");
                    break;
                case "status":
                    String msg = mazeInfo.getString("status");
                    printRobotStatus(msg);
                    message = "status: " + msg;
                    break;
                default:
                    message = "Unintended default for JSONObject";
                    break;
            }
            if (!message.equals("updatemazeInfo Default message"))
                MainActivity.receiveMessage(message);
        }
        this.invalidate();
    }

    public void moveRobot(String direction) {
        setValidPosition(false);
        int[] curCoord = this.getCurCoord();
        ArrayList<int[]> obstacleCoord = this.getObstacleCoord();
        this.setOldRobotCoord(curCoord[0], curCoord[1]);
        int[] oldCoord = this.getOldRobotCoord();
        String robotDirection = getRobotDirection();
        String backupDirection = robotDirection;

        switch (robotDirection) {
            case Constants.UP:
                switch (direction) {
                    case Constants.FORWARD:
                        if (curCoord[1] != 19) {
                            curCoord[1] += 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.RIGHT:
                        robotDirection = Constants.RIGHT;
                        break;
                    case Constants.BACK:
                        if (curCoord[1] != 2) {
                            curCoord[1] -= 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.LEFT:
                        robotDirection = Constants.LEFT;
                        break;
                    default:
                        robotDirection = "error up";
                        break;
                }
                break;
            case Constants.RIGHT:
                switch (direction) {
                    case Constants.FORWARD:
                        if (curCoord[0] != 14) {
                            curCoord[0] += 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.RIGHT:
                        robotDirection = Constants.DOWN;
                        break;
                    case Constants.BACK:
                        if (curCoord[0] != 2) {
                            curCoord[0] -= 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.LEFT:
                        robotDirection = Constants.UP;
                        break;
                    default:
                        robotDirection = "error right";
                }
                break;
            case Constants.DOWN:
                switch (direction) {
                    case Constants.FORWARD:
                        if (curCoord[1] != 2) {
                            curCoord[1] -= 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.RIGHT:
                        robotDirection = Constants.LEFT;
                        break;
                    case Constants.BACK:
                        if (curCoord[1] != 19) {
                            curCoord[1] += 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.LEFT:
                        robotDirection = Constants.RIGHT;
                        break;
                    default:
                        robotDirection = "error down";
                }
                break;
            case Constants.LEFT:
                switch (direction) {
                    case Constants.FORWARD:
                        if (curCoord[0] != 2) {
                            curCoord[0] -= 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.RIGHT:
                        robotDirection = Constants.UP;
                        break;
                    case Constants.BACK:
                        if (curCoord[0] != 14) {
                            curCoord[0] += 1;
                            validPosition = true;
                        }
                        break;
                    case Constants.LEFT:
                        robotDirection = Constants.DOWN;
                        break;
                    default:
                        robotDirection = "error left";
                }
                break;
            default:
                robotDirection = "error moveCurCoord";
                break;
        }
        if (getValidPosition())
            for (int x = curCoord[0] - 1; x <= curCoord[0] + 1; x++) {
                for (int y = curCoord[1] - 1; y <= curCoord[1] + 1; y++) {
                    for (int i = 0; i < obstacleCoord.size(); i++) {
                        if (obstacleCoord.get(i)[0] != x || obstacleCoord.get(i)[1] != y)
                            setValidPosition(true);
                        else {
                            setValidPosition(false);
                            break;
                        }
                    }
                    if (!getValidPosition())
                        break;
                }
                if (!getValidPosition())
                    break;
            }
        if (getValidPosition())
            this.setCurCoord(curCoord[0], curCoord[1], robotDirection);
        else {
            if (direction.equals(Constants.FORWARD) || direction.equals(Constants.BACK))
                robotDirection = backupDirection;
            this.setCurCoord(oldCoord[0], oldCoord[1], robotDirection);
        }
        this.invalidate();
    }
    

    public void printRobotStatus(String message) {
        TextView robotStatusTextView = ((Activity)this.getContext()).findViewById(R.id.robotStatus);
        robotStatusTextView.setText(message);
    }

    public boolean getAutoUpdate() {
        return autoUpdate;
    }
    

    private void setValidPosition(boolean status) {
        validPosition = status;
    }

    public boolean getValidPosition() {
        return validPosition;
    }

    public void setUnSetCellStatus(boolean status) {
        unSetCellStatus = status;
    }

    public boolean getUnSetCellStatus() {
        return unSetCellStatus;
    }

    public void setSetObstacleStatus(boolean status) {
        setObstacleStatus = status;
    }

    public boolean getSetObstacleStatus() {
        return setObstacleStatus;
    }

    public void setExploredStatus(boolean status) {
        setExploredStatus = status;
    }

    public boolean getExploredStatus() {
        return setExploredStatus;
    }

    public void setStartCoordinateStatus(boolean status) {
        startCoordStatus = status;
    }

    private boolean getStartCoordStatus() {
        return startCoordStatus;
    }

    public void setWaypointStatus(boolean status) {
        setWaypointStatus = status;
    }

    public boolean getCanDrawRobot() {
        return canDrawRobot;
    }

    public static void setMDFExplorationString(String msg) {
        MDFExplorationString = msg;
    }

    public static void setMDFObstacleString(String msg) {
        MDFObstacleString = msg;
    }

    public static String getMDFExplorationString() {
        return MDFExplorationString;
    }

    public static String getMDFObstacleString() {
        return MDFObstacleString;
    }

}

