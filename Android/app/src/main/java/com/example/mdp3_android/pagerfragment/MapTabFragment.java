package com.example.mdp3_android.pagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.R;
import com.example.mdp3_android.PageViewModel;
import com.example.mdp3_android.map.GridMap;
import com.example.mdp3_android.pagerfragment.ControlsTabFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONException;
import org.json.JSONObject;

public class MapTabFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MapFragment";

    private PageViewModel pageViewModel;

    Button resetMapBtn;
    static Button updateButton;
    ImageButton directionChangeImageBtn, exploredImageBtn, obstacleImageBtn, clearImageBtn;
    ToggleButton setStartPointToggleBtn, setWaypointToggleBtn;
//    Switch manualAutoToggleBtn;
    GridMap gridMap;
    private static boolean autoUpdate = false;
    public static boolean manualUpdateRequest = false;

    // Fragment Constructor
    public static MapTabFragment newInstance(int index) {
        MapTabFragment fragment = new MapTabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_map, container, false);

        gridMap = MainActivity.getGridMap();
        final RobotDirectionFragment robotDirectionFragment = new RobotDirectionFragment();

        // Control Button
        resetMapBtn = root.findViewById(R.id.resetMapBtn);
        setStartPointToggleBtn = root.findViewById(R.id.setStartPointToggleBtn);
        setWaypointToggleBtn = root.findViewById(R.id.setWayPointToggleBtn);
        directionChangeImageBtn = root.findViewById(R.id.changeDirectionImgBtn);
        exploredImageBtn = root.findViewById(R.id.exploredImgBtn);
        obstacleImageBtn = root.findViewById(R.id.obstaclesImgBtn);
        clearImageBtn = root.findViewById(R.id.clearImgBtn);
//        manualAutoToggleBtn = root.findViewById(R.id.manualAutoToggleBtn);
        updateButton = root.findViewById(R.id.updateMapBtn);

        // Button Listener
        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetMapBtn");
                showToast("Reseting map...");
                gridMap.resetMap();
            }
        });

        setStartPointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setStartPointToggleBtn");
                if (setStartPointToggleBtn.getText().equals("STARTING POINT"))
                    showToast("Cancelled selecting starting point");
                else if (setStartPointToggleBtn.getText().equals("CANCEL") && !gridMap.getAutoUpdate()) {
                    showToast("Please select starting point");
                    gridMap.setStartCoordStatus(true);
                    gridMap.toggleCheckedBtn("setStartPointToggleBtn");
                } else
                    showToast("Please select manual mode");
                showLog("Exiting setStartPointToggleBtn");
            }
        });

        setWaypointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setWaypointToggleBtn");
                if (setWaypointToggleBtn.getText().equals("WAYPOINT"))
                    showToast("Cancelled selecting waypoint");
                else if (setWaypointToggleBtn.getText().equals("CANCEL")) {
                    showToast("Please select waypoint");
                    gridMap.setWaypointStatus(true);
                    gridMap.toggleCheckedBtn("setWaypointToggleBtn");
                }
                else
                    showToast("Please select manual mode");
                showLog("Exiting setWaypointToggleBtn");
            }
        });

        directionChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked directionChangeImageBtn");
                robotDirectionFragment.show(getActivity().getFragmentManager(), "Direction Fragment");
                showLog("Exiting directionChangeImageBtn");
            }
        });

        exploredImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked exploredImageBtn");
                if (!gridMap.getExploredStatus()) {
                    showToast("Please check cell");
                    gridMap.setExploredStatus(true);
                    gridMap.toggleCheckedBtn("exploredImageBtn");
                }
                else if (gridMap.getExploredStatus())
                    gridMap.setSetObstacleStatus(false);
                showLog("Exiting exploredImageBtn");
            }
        });

        obstacleImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked obstacleImageBtn");
                if (!gridMap.getSetObstacleStatus()) {
                    showToast("Please plot obstacles");
                    gridMap.setSetObstacleStatus(true);
                    gridMap.toggleCheckedBtn("obstacleImageBtn");
                }
                else if (gridMap.getSetObstacleStatus())
                    gridMap.setSetObstacleStatus(false);
                showLog("Exiting obstacleImageBtn");
            }
        });

        clearImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked clearImageBtn");
                if (!gridMap.getUnSetCellStatus()) {
                    showToast("Please remove cells");
                    gridMap.setUnSetCellStatus(true);
                    gridMap.toggleCheckedBtn("clearImageBtn");
                }
                else if (gridMap.getUnSetCellStatus())
                    gridMap.setUnSetCellStatus(false);
                showLog("Exiting clearImageBtn");
            }
        });

//        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked manualAutoToggleBtn");
//                if (manualAutoToggleBtn.getText().equals("MANUAL")) {
//                    try {
//                        gridMap.setAutoUpdate(true);
//                        autoUpdate = true;
//                        gridMap.toggleCheckedBtn("None");
//                        updateButton.setClickable(false);
//                        updateButton.setTextColor(Color.GRAY);
//                        ControlsTabFragment.getCalibrateButton().setClickable(false);
//                        ControlsTabFragment.getCalibrateButton().setTextColor(Color.GRAY);
//                        manualAutoToggleBtn.setText("AUTO");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    showToast("AUTO mode");
//                }
//                else if (manualAutoToggleBtn.getText().equals("AUTO")) {
//                    try {
//                        gridMap.setAutoUpdate(false);
//                        autoUpdate = false;
//                        gridMap.toggleCheckedBtn("None");
//                        updateButton.setClickable(true);
//                        updateButton.setTextColor(Color.WHITE);
//                        ControlsTabFragment.getCalibrateButton().setClickable(true);
//                        ControlsTabFragment.getCalibrateButton().setTextColor(Color.WHITE);
//                        manualAutoToggleBtn.setText("MANUAL");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    showToast("MANUAL mode");
//                }
//                showLog("Exiting manualAutoToggleBtn");
//            }
//        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked updateButton");
                MainActivity.printMessage("sendArena");
                manualUpdateRequest = true;
                showLog("Exiting updateButton");
                try {
                    String message = "{\"map\":[{\"explored\": \"ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\",\"length\":300,\"obstacle\":\"00000000000000000706180400080010001e000400000000200044438f840000000000000080\"}]}";

                    gridMap.setReceivedJsonObject(new JSONObject(message));
                    gridMap.updateMapInformation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return root;
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static Button getUpdateButton() {
        return updateButton;
    }


}
