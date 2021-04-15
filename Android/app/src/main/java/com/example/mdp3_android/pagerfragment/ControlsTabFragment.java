package com.example.mdp3_android.pagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp3_android.R;
import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.map.Maze;
import com.example.mdp3_android.helper.Constants;

import static android.content.Context.SENSOR_SERVICE;

public class ControlsTabFragment extends Fragment implements SensorEventListener {
    SharedPreferences sharedPreferences;
    
    private PageViewModel pageViewModel; 
    private static long explorationTime, fastestpathTime;

    ToggleButton explorationBtn, fastestPathBtn;
    ImageButton forwardButton, rightButton, backButton, LeftButton, resetExplorationButton, resetfastestPathBtn;
    TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;
    Switch swithTilt;

    private Sensor sensor;
    private SensorManager sensorManager;
    private static Maze maze;
    static Handler timerHandler = new Handler();
    
    Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            long fastestPathMillis = System.currentTimeMillis() - fastestpathTime;
            int fastestPathSeconds = (int) (fastestPathMillis / Constants.ONE_THOUSAND);
            int fastestPathMinutes = fastestPathSeconds / Constants.SIXTY;
            fastestPathSeconds = fastestPathSeconds % Constants.SIXTY;

            fastestTimeTextView.setText(String.format("%02d:%02d", fastestPathMinutes, fastestPathSeconds));

            timerHandler.postDelayed(this, Constants.FIVE_HUNDRED);
        }
    };

    Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            long explorationMillis = System.currentTimeMillis() - explorationTime;
            int explorationSeconds = (int) (explorationMillis / Constants.ONE_THOUSAND);
            int explorationMinutes = explorationSeconds / Constants.SIXTY;
            explorationSeconds = explorationSeconds % Constants.SIXTY;

            exploreTimeTextView.setText(String.format("%02d:%02d", explorationMinutes, explorationSeconds));

            timerHandler.postDelayed(this, Constants.FIVE_HUNDRED);
        }
    };

    
    
    public static ControlsTabFragment newInstance(int index) {
        ControlsTabFragment fragment = new ControlsTabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(Constants.SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_controls_tab, container, false);
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        fastestpathTime = Constants.ZERO;
        explorationTime = Constants.ZERO;
       
        exploreTimeTextView = root.findViewById(R.id.explorationTimeTextView);
        fastestTimeTextView = root.findViewById(R.id.fastestPathTimeTextView);
        explorationBtn = root.findViewById(R.id.explorationToggleBtn);
        fastestPathBtn = root.findViewById(R.id.fastestPathToggleBtn);
        resetExplorationButton = root.findViewById(R.id.explorationResetImgBtn);
        resetfastestPathBtn = root.findViewById(R.id.fastestPathResetImgBtn);
        forwardButton = root.findViewById(R.id.forwardImgBtn);
        rightButton = root.findViewById(R.id.rightImgBtn);
        backButton = root.findViewById(R.id.backImgBtn);
        LeftButton = root.findViewById(R.id.leftImgBtn);
        swithTilt = root.findViewById(R.id.phoneTiltSwitch);
        robotStatusTextView = MainActivity.getRobotStatus();
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        maze = MainActivity.getMaze();
        

        explorationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton exploreToggleBtn = (ToggleButton) v;
                if (exploreToggleBtn.getText().equals("EXPLORE")) {
                    robotStatusTextView.setText("Exploration is Stopped");
                    timerHandler.removeCallbacks(timerRunnableExplore);
                }
                else if (exploreToggleBtn.getText().equals("STOP")) {
                    MainActivity.outputMessage("SE|");
                    robotStatusTextView.setText("Exploration is Started");
                    explorationTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableExplore, 0);
                }
            }
        });

        resetExplorationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotStatusTextView.setText(Constants.NOT_AVAILABLE);
                exploreTimeTextView.setText("00:00");
                if(explorationBtn.isChecked())
                    explorationBtn.toggle();
                timerHandler.removeCallbacks(timerRunnableExplore);
            }
        });

        fastestPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton fastestToggleBtn = (ToggleButton) v;
                if (fastestToggleBtn.getText().equals("FASTEST")) {
                    robotStatusTextView.setText("Fastest Path is  Stopped");
                    timerHandler.removeCallbacks(timerRunnableFastest);
                }
                else if (fastestToggleBtn.getText().equals("STOP")) {
                    MainActivity.outputMessage("SP|");
                    robotStatusTextView.setText("Fastest Path is Started");
                    fastestpathTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableFastest, 0);
                }
           }
        });

        resetfastestPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotStatusTextView.setText(Constants.NOT_AVAILABLE);
                fastestTimeTextView.setText("00:00");
                if (fastestPathBtn.isChecked())
                    fastestPathBtn.toggle();
                timerHandler.removeCallbacks(timerRunnableFastest);
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maze.getAutoUpdate())
                    showToast("Press manual button");
                else if (maze.getCanDrawRobot() && !maze.getAutoUpdate()) {
                    maze.moveRobot("forward");
                    MainActivity.refreshLabel();
                    if (maze.getValidPosition())
                        showToast("move forward");
                    else
                        showToast("Robot cannot move forward");
                    MainActivity.outputMessage("U1|");
                }
                else
                    showToast("Press start point button");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maze.getAutoUpdate())
                    showToast("Press manual button");
                else if (maze.getCanDrawRobot() && !maze.getAutoUpdate()) {
                    maze.moveRobot("back");
                    MainActivity.refreshLabel();
                    if (maze.getValidPosition())
                        showToast("move backward");
                    else
                        showToast("Robot cannot move backward");
                    MainActivity.outputMessage("B1|");
                }
                else
                    showToast("Press start point button");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maze.getAutoUpdate())
                    showToast("Press manual button");
                else if (maze.getCanDrawRobot() && !maze.getAutoUpdate()) {
                    showToast("turn right");
                    maze.moveRobot("right");
                    MainActivity.refreshLabel();
                    MainActivity.outputMessage("W1|");
                }
                else
                    showToast("Press start point button");
            }
        });


        LeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maze.getAutoUpdate())
                    showToast("Press manual button");
                else if (maze.getCanDrawRobot() && !maze.getAutoUpdate()) {
                    maze.moveRobot("left");
                    MainActivity.refreshLabel();
                    showToast("turn left");
                    MainActivity.outputMessage("L1|");
                }
            }
        });

        swithTilt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (maze.getAutoUpdate()) {
                    showToast("Press manual button");
                    swithTilt.setChecked(false);
                }
                else if (maze.getCanDrawRobot() && !maze.getAutoUpdate()) {
                    if(swithTilt.isChecked()){
                        swithTilt.setPressed(true);

                        sensorManager.registerListener(ControlsTabFragment.this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
                        sensorHandler.post(sensorDelay);
                    }else{
                        try {
                            sensorManager.unregisterListener(ControlsTabFragment.this);
                        }catch(IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        sensorHandler.removeCallbacks(sensorDelay);
                    }
                } else {
                    showToast("Press start point button");
                    swithTilt.setChecked(false);
                }
                if(swithTilt.isChecked()){
                    compoundButton.setText("TILT ON");
                }else
                {
                    compoundButton.setText("TILT OFF");
                }
            }
        });


        return root;
    }
    
    
    Handler sensorHandler = new Handler();
    boolean sensorFlag= false;

    private final Runnable sensorDelay = new Runnable() {
        @Override
        public void run() {
            sensorFlag = true;
            sensorHandler.postDelayed(this,Constants.ONE_THOUSAND);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];

        if(sensorFlag) {
            if (y < -2) {
                maze.moveRobot("forward");
                MainActivity.refreshLabel();
                MainActivity.outputMessage("U1|");
            } else if (y > 2) {
                maze.moveRobot("back");
                MainActivity.refreshLabel();
                MainActivity.outputMessage("B1|");
            } else if (x > 2) {
                maze.moveRobot("left");
                MainActivity.refreshLabel();
                MainActivity.outputMessage("L1|");
            } else if (x < -2) {
                maze.moveRobot("right");
                MainActivity.refreshLabel();
                MainActivity.outputMessage("W1|");
            }
        }
        sensorFlag = false;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            sensorManager.unregisterListener(ControlsTabFragment.this);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }

}
