package main;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.addons.Indicator;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.colors.ColorSkin;
import eu.hansolo.tilesfx.events.IndicatorEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ntu.mdp.group.three.astarpathfinder.FastestPathThread;
import ntu.mdp.group.three.config.*;
import ntu.mdp.group.three.communication.CommunicationManager;
import ntu.mdp.group.three.communication.CommunicationSocket;
import ntu.mdp.group.three.exploration.ExplorationThread;
import ntu.mdp.group.three.robot.RobotImage;
import ntu.mdp.group.three.map.ArenaMap;
import ntu.mdp.group.three.robot.SimulatedRobot;
import ntu.mdp.group.three.threadedTasks.DisableButtonTask;
import ntu.mdp.group.three.userInterface.NumberStringFilteredConverter;
import ntu.mdp.group.three.utility.MDFManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    public static AnimationTimer timer;
    private static long lastTimerCall;
    private ScrollPane rootWrapper;

    // Stack trace TextArea field for printing status and exception messages.
    private final static TextArea STACK_TRACE_TEXT_AREA = new TextArea();

    // Stack trace ScrollPane use for wrapping the stack trace.
    private final static ScrollPane STACK_TRACE_SCROLL_PANE = new ScrollPane();

    // List of UI components to disable when the bot is running.
    private final static List<Node> NODES_TO_DISABLE = new ArrayList<>();

    // A Map to store the grid labels mapped to the grid path.
    private final static HashMap<String, String> gridPath = new HashMap<>();

    // 2D Matrix to store the grid cells of the 15 x 20 grid.
    private final static Rectangle[][] GRID_CELLS = new Rectangle[ArenaConfig.ARENA_WIDTH][ArenaConfig.ARENA_HEIGHT];

    // A Label to store the image of the robot used for traversing the arena.
    public final static RobotImage ROBOT = new RobotImage("robot/n-robot-vacuum-cleaner.png",
            SimulatorConfig.ROBOT_WIDTH, SimulatorConfig.ROBOT_HEIGHT);

    // Static boolean variable to store the identification to if the current run is a simulated one or not.
    public static boolean IS_SIMULATED_RUN = false;

    // Local static instance of a SimulatedRobot object should it be used.
    private static SimulatedRobot simulatedRobot;

    // Local static instance of a Timer object for timing threaded jobs.
    private final static Timer LOCAL_TIMER = new Timer();

    // HashMap for storing the list of map files for future retrieval.
    private static HashMap<String, String> mapFilePath;

    // Local int array of size two to store the x and y coordinate of the chosen way point for the robot to navigate to.
    private static int[] chosenWayPoint = { -1, -1 };

    // Local constant that holds the UI GridPane component representing the arena grid.
    private final static GridPane ARENA = new GridPane();

    // Local static variable for storing the speed the robot should take in the arena.
    private static int robotSpeed = 1;

    // Local static double variable to store the exploration coverage limit in percent.
    private static int explorationCoverageLimit = 100;

    // Local static boolean variable to store the value representing if image recognition is being used.
    private static boolean isUsingImageRecognition = false;

    // Local static int variable to store the exploration time limit in seconds.
    private static int explorationTimeLimitSeconds = -1;

    // Local static GridPane variable to store the arena Grid UI component.
    private static GridPane arena = new GridPane();

    public static Tile explorationStatTile;

    public static Tile countdownTile;

    private static ComboBox<String> mapSelectComboBox;

    private static VBox stackTraceLabelWrapper = new VBox(10);

    private final static String ROBOT_VIEW_MESSAGE = "Robot's View";

    private final static String SIMULATED_MAP_MESSAGE = "Simulated Map View";

    private static Label viewToggleLabel = new Label(ROBOT_VIEW_MESSAGE);

    private static Button selectMDFButton;

    @Override
    public void init() {
        // Top level BorderPane
        rootWrapper = new ScrollPane();
        rootWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ROBOT.setPadding(new Insets(0 , 0, 0, 15));

//        rootWrapper.setFitToWidth(true); // Forces root scroll pane to fit width of child.

        BorderPane root = new BorderPane();
        root.setMinWidth(SimulatorConfig.MIN_WINDOW_WIDTH);
        root.setMinHeight(SimulatorConfig.MIN_WINDOW_HEIGHT);
        root.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * 6;
            double width = rootWrapper.getContent().getBoundsInLocal().getWidth();
            double vValue = rootWrapper.getVvalue();
            rootWrapper.setVvalue(vValue + -deltaY/width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
        });
        rootWrapper.setContent(root);

        // Creates 15 x 20 UI grid.
        arena = (GridPane) createArena();

        // This BorderPane will hold the arena grid and the stack trace scroll pane.
        BorderPane arenaWrapper = new BorderPane();
        viewToggleLabel.setId("viewLabel");

        ToggleButton toggleMenuButton = new ToggleButton("Hide Menu");
        toggleMenuButton.setAlignment(Pos.BOTTOM_CENTER);
        toggleMenuButton.setOnAction(actionEvent -> {
            root.getRight().setVisible(!toggleMenuButton.isSelected());
            toggleMenuButton.setText((toggleMenuButton.isSelected() ? "Show" : "Hide") + " Menu");
        });

        Region padLeft = new Region();
        HBox.setHgrow(padLeft, Priority.ALWAYS);

        Region padRight = new Region();
        HBox.setHgrow(padRight, Priority.ALWAYS);

        HBox rootTopChildWrapper = new HBox(20, padLeft, viewToggleLabel, toggleMenuButton, padRight);
        rootTopChildWrapper.setPadding(new Insets( 20, 0, 0, 0));

        BorderPane.setAlignment(rootTopChildWrapper, Pos.CENTER);
        arenaWrapper.setTop(rootTopChildWrapper);

        root.setCenter(arenaWrapper);

        // Set the stack trace TextArea object to the scroll pane.
        STACK_TRACE_TEXT_AREA.setId("stackTraceTextArea");
        STACK_TRACE_TEXT_AREA.setMaxHeight(160);
        STACK_TRACE_TEXT_AREA.setWrapText(true);
        STACK_TRACE_TEXT_AREA.setCursor(Cursor.DEFAULT);
        STACK_TRACE_TEXT_AREA.setEditable(false);

        // The stack trace scroll pane displays status and exception messages.
        STACK_TRACE_SCROLL_PANE.setContent(STACK_TRACE_TEXT_AREA);
        STACK_TRACE_SCROLL_PANE.setFitToWidth(true);
        STACK_TRACE_SCROLL_PANE.setPadding(new Insets(10));
        STACK_TRACE_SCROLL_PANE.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * 6;
            double width = STACK_TRACE_SCROLL_PANE.getContent().getBoundsInLocal().getWidth();
            double vValue = STACK_TRACE_SCROLL_PANE.getVvalue();
            STACK_TRACE_SCROLL_PANE.setVvalue(vValue + -deltaY/width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
        });

        arenaWrapper.setBottom(STACK_TRACE_SCROLL_PANE);

        // Create tiles and set to the right of the root border pane.
        VBox tiles = (VBox) createTiles();
        root.setRight(tiles);

        // Stack pane for grid and pane.
        StackPane arenaWrapperParent = new StackPane(arena);
        arenaWrapper.setCenter(arenaWrapperParent);
        root.setLeft(new Pane());
    }

    @Override
    public void start(Stage primaryStage) {
        CommunicationSocket.setDebugTrue();

        ButtonType yesChoiceButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noChoiceButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert choiceDialog = new Alert(
                Alert.AlertType.NONE,
                "Is this a simulated run?",
                yesChoiceButton, noChoiceButton
        );
        choiceDialog.setTitle("Simulation Choice Dialog");
        Optional<ButtonType> result = choiceDialog.showAndWait();
        if (result.get().getButtonData() == ButtonBar.ButtonData.YES) {
            IS_SIMULATED_RUN = true;
            Main.writeToTraceTextArea("We live in a simulation...");
            simulatedRobot = new SimulatedRobot(0, 0, Directions.SOUTH);
        }

        else if (result.get().getButtonData() == ButtonBar.ButtonData.NO) {
            IS_SIMULATED_RUN = false;
            Main.writeToTraceTextArea("We live in reality...");

            CommunicationManager communicationManager = CommunicationManager.getInstance();
            CommunicationSocket.setDebugTrue();

            boolean connected = false;
            while (!connected) connected = communicationManager.connectToRPi(IS_SIMULATED_RUN);

            try {
                communicationManager.start();
            } catch (Exception e) {
                communicationManager.stopConnectionManager();
                System.out.println("ConnectionManager is stopped");
            }
        }

        selectMDFButton.setOnAction(actionEvent -> {
            FileChooser chooseMDFFileChooser = new FileChooser();
            chooseMDFFileChooser.setTitle("Select P2 String file");
            File selectedMDFFile = chooseMDFFileChooser.showOpenDialog(primaryStage);
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(selectedMDFFile.toString()));
                MDFManager mdfManager = MDFManager.getInstance();
                StringBuilder stringBuilder = new StringBuilder();
                System.out.println("selectedMDFFile.toString(): " + selectedMDFFile.toString());
                String sCurrentLine;
                while ((sCurrentLine = bufferedReader.readLine()) != null) stringBuilder.append(sCurrentLine);
                mdfManager.setP2String(stringBuilder.toString());

                ArenaMap arenaMap = simulatedRobot.getMap();
                System.out.println("Loaded MDF Map:");
                arenaMap.setGrid(mdfManager.getGridFromMDF());
//                arenaMap.print();
//                simulatedRobot.setKnownMap(arenaMap);
                simulatedRobot.setTrueMap(arenaMap);

                // TODO Do something with selected MDF file.
                // TODO Do we disable select map to load combo box after loading the MDF file
                Main.writeToTraceTextArea(selectedMDFFile + " has loaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Add the root wrapper scroll pane to the top level scene.
        Scene mainScene = new Scene(rootWrapper);
        mainScene.getStylesheets().add("styles.css");
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("NTU MDP CZ3004 Group 3 Algorithm Simulator");
        primaryStage.setMinWidth(SimulatorConfig.MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(SimulatorConfig.MIN_WINDOW_HEIGHT);
        primaryStage.setMaximized(true);
        primaryStage.show();

//        timer.start();
    }

    private Parent createTiles() {
        Main.writeToTraceTextArea("Creating tiles...");

        // VBox for Tiles.
        final Color TILE_BACKGROUND_COLOR = Color.web("#22242a");
        final String TEXT_ON = "ON";
        final String TEXT_OFF = "OFF";

        HBox mapHWrapper = new HBox(10);
        HBox algorithmHWrapper = new HBox(10);
        HBox statsHWrapper = new HBox(10);

        VBox tilesWrapper = new VBox(10, mapHWrapper, algorithmHWrapper, statsHWrapper);
        tilesWrapper.setPadding(new Insets(10));
        tilesWrapper.setId("tilesWrapper");

        // Start create map tile.
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(actionEvent -> resetMapEvent(statsHWrapper));

//        Button checkMapButton = new Button("Check Map");

        Button toggleMapButton = new Button("Toggle Map");
        toggleMapButton.setOnAction(actionEvent -> toggleMap());

        NODES_TO_DISABLE.add(resetButton);
//        NODES_TO_DISABLE.add(checkMapButton);
        NODES_TO_DISABLE.add(toggleMapButton);
        selectMDFButton = new Button("Load MDF P2 File");
        NODES_TO_DISABLE.add(selectMDFButton);

        Button generateMDFStringButton = new Button("Generate Map Descriptor");
        generateMDFStringButton.setOnAction(actionEvent -> {
            String[] mdfString = simulatedRobot.getMDFString();
            Main.writeToTraceTextArea("The MDF String is: ");
            Main.writeToTraceTextArea("Part 1: "  + mdfString[0]);
            Main.writeToTraceTextArea("Part 2: " + mdfString[2]);
        });
        NODES_TO_DISABLE.add(generateMDFStringButton);

        VBox mapButtonWrapper = new VBox(10, resetButton, toggleMapButton, selectMDFButton, generateMDFStringButton);
        mapButtonWrapper.setAlignment(Pos.CENTER);
        mapButtonWrapper.setPadding(new Insets(10));

        Tile mapTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .title("Map")
                .graphic(mapButtonWrapper)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .backgroundColor(TILE_BACKGROUND_COLOR)
                .build();
        // End create map tile.

        // Start create map options tile.
        Label wayPointLabel = new Label("Set a way point");
        Label mapSelectLabel = new Label("Select map to load");

        Spinner<Integer> xWayPointSpinner = new Spinner<>(0, 14, 0, 1);
        xWayPointSpinner.setEditable(true);
        xWayPointSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            chosenWayPoint[0] = newValue;
            simulatedRobot.setWayPoint(chosenWayPoint[0], chosenWayPoint[1]);
//            if (Arrays.equals(chosenWayPoint, new int[] {-1, -1}))
//                setWayPointUI(chosenWayPoint[0], chosenWayPoint[1], oldValue, chosenWayPoint[1]);
        });
        NODES_TO_DISABLE.add(xWayPointSpinner);

        Spinner<Integer> yWayPointSpinner = new Spinner<>(0, 19, 0, 1);
        yWayPointSpinner.setEditable(true);
        yWayPointSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            chosenWayPoint[1] = newValue;
            simulatedRobot.setWayPoint(chosenWayPoint[0], chosenWayPoint[1]);
//            if (Arrays.equals(chosenWayPoint, new int[] {-1, -1}))
//                setWayPointUI(chosenWayPoint[0], chosenWayPoint[1], chosenWayPoint[0], oldValue);
        });
        NODES_TO_DISABLE.add(yWayPointSpinner);

        String[] mapSelectOptions = loadMapNames();
        mapSelectComboBox = new ComboBox<>(
                FXCollections.observableArrayList(mapSelectOptions)
        ); // TODO add actual items here in constructor.
        mapSelectComboBox.setPromptText("Select map to load");
        mapSelectComboBox.valueProperty().addListener((obs, oldItem, newItem) -> {
            ArenaMap map = getGridFromFile(newItem);
            simulatedRobot.setTrueMap(map);
//            simulatedRobot.setKnownMap(map); # TODO toggle for FP.
        });
        NODES_TO_DISABLE.add(mapSelectComboBox);

        HBox wayPointHWrapper = new HBox(10, xWayPointSpinner, yWayPointSpinner);
        VBox spinnersWrapper = new VBox(10, wayPointLabel, wayPointHWrapper);

        VBox mapSelectWrapper = new VBox(10, mapSelectLabel, mapSelectComboBox);
        mapSelectWrapper.setPadding(new Insets(0, 0, 15, 0));

        VBox mapOptionsButtonWrapper = new VBox(10, mapSelectWrapper, spinnersWrapper);
        mapOptionsButtonWrapper.setAlignment(Pos.CENTER);

        Tile mapOptionsTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .title("Map Options")
                .graphic(mapOptionsButtonWrapper)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .backgroundColor(TILE_BACKGROUND_COLOR)
                .build();
        // End create map options tile.

        // Start create Algorithms tile.
        Button explorationButton = new Button("Exploration");
        explorationButton.setOnAction(actionEvent -> startExploration(statsHWrapper));

        Button fastestPathButton = new Button("Fastest Path");
        fastestPathButton.setOnAction(actionEvent -> startFastestPath());

        Button returnToStartButton = new Button("Return");
        returnToStartButton.setOnAction(actionEvent -> simulatedRobot.resetRobotPositionOnUI());

        NODES_TO_DISABLE.add(explorationButton);
        NODES_TO_DISABLE.add(fastestPathButton);
        NODES_TO_DISABLE.add(returnToStartButton);

        VBox algorithmButtonWrapper = new VBox(10, explorationButton, fastestPathButton, returnToStartButton);
        algorithmButtonWrapper.setAlignment(Pos.CENTER);
        algorithmButtonWrapper.setPadding(new Insets(10));

        Tile algorithmOptions = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .title("Algorithms")
                .graphic(algorithmButtonWrapper)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .backgroundColor(TILE_BACKGROUND_COLOR)
                .build();
        // End create Algorithms tile.

        // Start of Algorithms options tile.
        Label explorationTimeLimitLabel = new Label("Exploration Time Limit (secs)");
        Label coverageLimitLabel = new Label("Exploration Coverage Limit (%)");
        Label speedLabel = new Label("Speed");

        TextField explorationTimeNumberField = new TextField();
        NumberStringFilteredConverter explorationTimeNumberConverter = new NumberStringFilteredConverter(NumberStringFilteredConverter.NUMBER_ONLY_FILTER);
        final TextFormatter<Number> explorationFormatter = new TextFormatter<>(
                explorationTimeNumberConverter,
                explorationTimeLimitSeconds,
                explorationTimeNumberConverter.getFilter()
        );
        explorationTimeNumberField.setTextFormatter(explorationFormatter);
        NODES_TO_DISABLE.add(explorationTimeNumberField);

        Label imageRecLabel = new Label("Image Recognition");
        imageRecLabel.setAlignment(Pos.CENTER_RIGHT);

        Label imageRecIndicatorLabel = new Label(TEXT_OFF);
        imageRecIndicatorLabel.setStyle("-fx-font-weight: bold");
        imageRecIndicatorLabel.setPadding(new Insets(0, 0, 0, 5));
        imageRecIndicatorLabel.setAlignment(Pos.CENTER_RIGHT);

        Indicator imageRecIndicator = new Indicator(Tile.BLUE, Tile.GRAY);
        imageRecIndicator.setOnMousePressed(e -> {
            imageRecIndicator.setOn(!imageRecIndicator.isOn());
            isUsingImageRecognition = imageRecIndicator.isOn();
        });
        imageRecIndicator.addEventHandler(IndicatorEvent.INDICATOR_ON, e -> imageRecIndicatorLabel.setText(TEXT_ON));
        imageRecIndicator.addEventHandler(IndicatorEvent.INDICATOR_OFF, e -> imageRecIndicatorLabel.setText(TEXT_OFF));
        NODES_TO_DISABLE.add(imageRecIndicator);

        HBox imageRecWrapper = new HBox(imageRecIndicator, imageRecLabel, imageRecIndicatorLabel);
        imageRecWrapper.setPadding(new Insets(10, 0, 10, -20));
        imageRecWrapper.setAlignment(Pos.CENTER_LEFT);

        TextField coverageNumberField = new TextField();
        NumberStringFilteredConverter coverageConverter = new NumberStringFilteredConverter(NumberStringFilteredConverter.HUNDRED_PERCENT_FILTER); // TODO For some reason the 100 percent filter isn't working..
        final TextFormatter<Number> coverageFormatter = new TextFormatter<>(
                coverageConverter,
                explorationCoverageLimit,
                coverageConverter.getFilter()
        );
        coverageNumberField.setTextFormatter(coverageFormatter);
        coverageNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            explorationCoverageLimit = Integer.parseInt(newValue);
        });
        NODES_TO_DISABLE.add(coverageNumberField);

        Spinner<Integer> speedSpinner = new Spinner<>(1, 5, 1, 1);
        speedSpinner.setEditable(true);
        speedSpinner.valueProperty().addListener((obs, oldValue, newValue) -> robotSpeed = newValue);
        NODES_TO_DISABLE.add(speedSpinner);

        VBox algorithmOptionsWrapper = new VBox(10,
                explorationTimeLimitLabel, explorationTimeNumberField,
                coverageLimitLabel, coverageNumberField,
                speedLabel, speedSpinner,
                imageRecWrapper
        );

        Tile algorithmSettingsTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .title("Algorithm Options")
                .graphic(algorithmOptionsWrapper)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .backgroundColor(TILE_BACKGROUND_COLOR)
                .build();
        // End of Algorithms options tile.

        // Start of stats tile.
        explorationStatTile = TileBuilder.create()
                .skinType(Tile.SkinType.COLOR)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .sections(
                        new Section(0.00, 0.25, ColorSkin.RED),
                        new Section(0.25, 0.50, ColorSkin.ORANGE),
                        new Section(0.50, 0.75, ColorSkin.YELLOW),
                        new Section(0.75, 1.00, ColorSkin.GREEN)
                )
                .title("Exploration Progress")
                .text("Progress of automated exploration")
                .animated(true)
                .build();

        createCountdownTile();

        explorationTimeNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            explorationTimeLimitSeconds = Integer.parseInt(newValue.replaceAll(",", ""));
        });
//        if (now > lastTimerCall + 3_500_000_000L) {
//            explorationStatTile.setValue(new Random().nextDouble() * explorationStatTile.getRange() * 1.5 + explorationStatTile.getMinValue());
//        }
        // TODO Add random animation timer.
        // End of stats tile.

        statsHWrapper.getChildren().addAll(explorationStatTile, countdownTile);
        mapHWrapper.getChildren().addAll(mapTile, mapOptionsTile);
        algorithmHWrapper.getChildren().addAll(algorithmOptions, algorithmSettingsTile);

        Main.writeToTraceTextArea("Tiles created.");
        return tilesWrapper;
    }

    private static void createCountdownTile() {
        countdownTile = TileBuilder.create()
                .skinType(Tile.SkinType.COUNTDOWN_TIMER)
                .minSize(SimulatorConfig.MIN_WINDOW_WIDTH - (15 * ArenaConfig.GRID_WIDTH),
                        UserInterfaceConstants.MIN_TILE_HEIGHT)
                .title("Countdown Timer")
                .text("Timer in seconds")
                .barColor(Bright.ORANGE_RED)
                .onAlarm(e -> {
                    timer.stop();
                    Main.writeToTraceTextArea("Exploration time limit is up!");
                })
                .build();
    }

    private Parent createArena() {
        Main.writeToTraceTextArea("Creating the arena of 15 x 20 grid...");

        // Create the map for the grid label mapped to the grid path
        for (int i = 0; i < RobotConfig.GRID_IDENTIFIER.length; i++) {
            gridPath.put(RobotConfig.GRID_IDENTIFIER[i], SimulatorConfig.GRID_CELL_COLORS[i]);
        }

        ARENA.setAlignment(Pos.CENTER);
        ARENA.setPadding(new Insets(10));
        ARENA.add(new Text(" "), 0, 0);

        final Color CELL_COLOR = Color.web(SimulatorConfig.UNEXPLORED_CELL_COLOR);
        final Color STROKE_COLOR = Color.web("#0e1314");

        for (int i = 0; i < ArenaConfig.ARENA_WIDTH + 1; i++) {
            for (int j = 0; j < ArenaConfig.ARENA_HEIGHT + 1; j++) {
                if (i > 0 && j > 0) {
                    Rectangle gridCell = new Rectangle(ArenaConfig.GRID_WIDTH, ArenaConfig.GRID_HEIGHT);

                    if (i < 4 && j < 4) gridCell.setFill(Color.web(SimulatorConfig.START_POINT_CELL_COLOR));
                    else if (j > ArenaConfig.ARENA_HEIGHT - 3 &&
                            i > ArenaConfig.ARENA_WIDTH - 3)
                        gridCell.setFill(Color.web(SimulatorConfig.END_POINT_CELL_COLOR));
                    else gridCell.setFill(CELL_COLOR);

                    gridCell.setStroke(STROKE_COLOR);
                    ARENA.add(gridCell, i, j);
                    GRID_CELLS[i - 1][j - 1] = gridCell;
                } else {
                    continue;
                }

                if (i == 1) {
                    createGridCell(ARENA, 0, j,
                            new Insets(0, -50, 0, 0)
                    );
                }

                if (j == 1) {
                    createGridCell(ARENA, i, 0,
                            new Insets(0, 0, -50, 0)
                    );
                }
            }
        }

        // Set Robot to starting position, (x, y) units away from initial initialization point of
        // cell (0, 0) of the GridPane.
        ARENA.add(ROBOT, 0, 0);
        ROBOT.setTranslateX(110);
        ROBOT.setTranslateY(110);

        Main.writeToTraceTextArea("Grid arena is created.");
        return ARENA;
    }

    // Update the Image of the Grid Image Component with the old and new map.
    public static void updateMapOnUI(ArenaMap oldMap, ArenaMap newMap) {
        String[][] oldGridValue = oldMap.getGridMap();
        String[][] newGridValue = newMap.getGridMap();
        int[] wayPoint = newMap.getWayPoint();
        int[] oldWayPoint = oldMap.getWayPoint();
        if (!Arrays.equals(wayPoint, new int[]{-1, -1})) {
            GRID_CELLS[wayPoint[0]][wayPoint[1]].setFill(
                    Color.web(gridPath.get(RobotConfig.WAY_POINT))
            );
        }
        for (int i = 0; i < ArenaConfig.ARENA_WIDTH; i++) {
            for (int j = 0; j < ArenaConfig.ARENA_HEIGHT; j++) {
                if ((oldGridValue[i][j].compareTo(newGridValue[i][j]) != 0 ||
                        Arrays.equals(oldWayPoint, new int[]{i, j}))
                        && gridPath.containsKey(newGridValue[i][j])
                        && !Arrays.equals(wayPoint, new int[]{i, j})) {
                    GRID_CELLS[i][j].setFill(
                            Color.web(gridPath.get(newGridValue[i][j]))
                    );
                }
            }
        }
    }

    private void createGridCell(GridPane arena, int colIndex, int rowIndex, Insets insets) {
        Label gridNumbering = new Label(
                String.valueOf(
                        rowIndex == 0 ? colIndex - 1 : rowIndex - 1
                )
        );
        gridNumbering.setStyle(
                "-fx-font-size: 14pt;" +
                        "-fx-text-fill: #fcfcfc;"
        );
        gridNumbering.setAlignment(Pos.CENTER);
        gridNumbering.setPadding(insets);
        arena.add(gridNumbering, colIndex, rowIndex);
        GridPane.setHalignment(gridNumbering, HPos.CENTER);
    }

    public static void disableAllActionableInputs(boolean disable) {
        Main.writeToTraceTextArea((disable ? "Disabling" : "Enabling") + " all buttons and input fields...");
        for (Node node : NODES_TO_DISABLE) {
            node.setDisable(disable);
        }
    }

    public static void writeToTraceTextArea(String message) {
        String currentStackTraceMessage = STACK_TRACE_TEXT_AREA.getText(); // TODO Text Area can only have 22 appended new line messages..
//        Main.STACK_TRACE_TEXT_AREA.setText(currentStackTraceMessage + message + "\n");
//        Main.STACK_TRACE_TEXT_AREA.positionCaret(
//                Main.STACK_TRACE_TEXT_AREA.getText().length()
//        );
//        Label newStackTraceText = new Label(message);
//        stackTraceLabelWrapper.getChildren().add(newStackTraceText); // TODO Alternative may be to add new labels to the scroll pane per new message. Try again in future.
        System.out.println(message);
    }

    private static void toggleMap() {
        Main.disableAllActionableInputs(true);

        if (viewToggleLabel.getText().equals(ROBOT_VIEW_MESSAGE)) {
            viewToggleLabel.setText(SIMULATED_MAP_MESSAGE);
        } else viewToggleLabel.setText(ROBOT_VIEW_MESSAGE);

        simulatedRobot.toggleMap();
        LOCAL_TIMER.schedule(new DisableButtonTask(false), RobotConfig.DELAY * (ArenaConfig.GRID_WIDTH + 1));
    }

    private static void startExplorationAnimation() {
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now > lastTimerCall + explorationTimeLimitSeconds) {
                    if (!countdownTile.isRunning()) {
                        countdownTile.setTimePeriod(Duration.ofSeconds(explorationTimeLimitSeconds));
                        countdownTile.setRunning(true);
                    }

                    if (explorationStatTile.isRunning()) {
//                        explorationStatTile.setValue(Exploration.percentComplete(simulatedRobot));
                    } else {
                        explorationStatTile.setRunning(true);
                    }

                    lastTimerCall = now;
                }
            }

            @Override
            public void stop() {
                super.stop();
                countdownTile.stop();
                explorationStatTile.stop();
            }
        };

        timer.start();
    }

    private static void startExploration(HBox tileWrapper) {
        if (!ExplorationThread.isRunning()) {
//            resetMapEvent(tileWrapper);
            Main.writeToTraceTextArea("Exploration Started");
            disableAllActionableInputs(true);
            startExplorationAnimation();
            ExplorationThread.getInstance(simulatedRobot, explorationTimeLimitSeconds,
                    explorationCoverageLimit, robotSpeed, isUsingImageRecognition); // TODO Forced off image rec
        }
    }

    private static void startFastestPath() {
        if (Arrays.equals(chosenWayPoint, new int[] {-1, -1})) {
            simulatedRobot.setWayPoint(chosenWayPoint[0], chosenWayPoint[1]);
        }
        int [] wayPoint = simulatedRobot.getWayPoint();
//        if (!FastestPathThread.getRunning() && ExplorationThread.isCompleted()) {
        if (!FastestPathThread.isRunning()) {
            Main.writeToTraceTextArea("Fastest Path Started");
//            System.out.println(simulatedRobot.getMap().print());
            FastestPathThread.getInstance(simulatedRobot, wayPoint, robotSpeed);
            disableAllActionableInputs(true);
            LOCAL_TIMER.schedule(new DisableButtonTask(false), 1);
        }
//        else if (!ExplorationThread.isCompleted()) {
//            Main.writeToTraceTextArea("You need to run exploration first.");
//        }
    }

    private static ArenaMap getGridFromFile(String selectedMap) {
        ArenaMap map = null;
        if (selectedMap == null || selectedMap.compareTo("Select map to load") == 0) {
            Main.writeToTraceTextArea("Unable to load map");
            return null;
        }
        disableAllActionableInputs(true);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL mapToLoadURL = classloader.getResource("maps/" + selectedMap + ".txt");
        File mapToLoad;
        try {
            if (mapToLoadURL != null) {
                mapToLoad = new File(mapToLoadURL.toURI());
                String[][] grid = new String[ArenaConfig.ARENA_WIDTH][ArenaConfig.ARENA_HEIGHT];
                FileReader fileReader = new FileReader(mapToLoad);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = bufferedReader.readLine();
                int heightCount = 0;

                while (line != null) {
                    line = line.trim().toUpperCase();
                    if (line.length() != ArenaConfig.ARENA_WIDTH)
                        throw new Exception("The format of the " + mapToLoad + " does not match the board format.");

                    for (int i = 0; i < line.length(); i++) {
                        switch(line.charAt(i)) {
                            case 'S':
                                grid[i][heightCount] = RobotConfig.START_POINT;
                                break;
                            case 'U':
                                grid[i][heightCount] = RobotConfig.EXPLORED;
                                break;
                            case 'E':
                                grid[i][heightCount] = RobotConfig.END_POINT;
                                break;
                            case 'O':
                                grid[i][heightCount] = RobotConfig.OBSTACLE;
                                break;

                            default:
                                Main.writeToTraceTextArea("There is unrecognised character symbol in " + mapToLoad + ".\n" +
                                        mapToLoad + " failed to load into the program.");
                                throw new Exception("There is unrecognised character symbol in " + mapToLoad + ".\n" +
                                        mapToLoad + " failed to load into the program.");
                        }
                    }

                    heightCount++;
                    line = bufferedReader.readLine();
                }

                if (heightCount != ArenaConfig.ARENA_HEIGHT) {
                    throw new Exception("The format of the " + mapToLoad + " does not match the board format.");
                }
                bufferedReader.close();
                Main.writeToTraceTextArea(mapToLoad + " has loaded successfully.");
                map = new ArenaMap(grid);
            } else {
                Main.writeToTraceTextArea("Resetting map...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.writeToTraceTextArea(e.getMessage());
        } finally {
            LOCAL_TIMER.schedule(new DisableButtonTask(false), RobotConfig.DELAY * (ArenaConfig.GRID_WIDTH + 1));
        }
        return map;
    }

    private static String[] loadMapNames() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL mapsFolderURL = classloader.getResource("maps");
        File folder = null;
        try {
            folder = new File(mapsFolderURL.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mapFilePath = new HashMap<>();
        mapFilePath.put("Select a map to load", null);

        for (File file: Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".txt")) {
                mapFilePath.put(file.getName().substring(0, file.getName().lastIndexOf(".txt")), file.getAbsolutePath());
            }
        }

        String[] fileNames = new String[mapFilePath.size()];
        int i = mapFilePath.size() - 1;

        for (String key : mapFilePath.keySet()) {
            fileNames[i] = key;
            i--;
        }

        Arrays.sort(fileNames);
        return fileNames;
    }

    private static void resetMapEvent(HBox tileWrapper) {
        try {
            Main.disableAllActionableInputs(true);

            ExplorationThread.stopThread();
            FastestPathThread.stopThread();

            simulatedRobot.resetRobotPositionOnUI();
            mapSelectComboBox.getSelectionModel().selectFirst();

            TimeUnit.MILLISECONDS.sleep(500);

            simulatedRobot.restartRobot();

            createCountdownTile();

            tileWrapper.getChildren().remove(1);
            tileWrapper.getChildren().add(countdownTile);

            explorationStatTile.setValue(0);
            LOCAL_TIMER.schedule(new DisableButtonTask(false), RobotConfig.DELAY * (ArenaConfig.GRID_WIDTH + 1));
            Main.writeToTraceTextArea("Robot has been reset to its original position and state.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
