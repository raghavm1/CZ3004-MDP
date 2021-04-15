package com.example.mdp3_android.pagerfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.R;
import com.example.mdp3_android.map.Maze;
import com.example.mdp3_android.helper.Constants;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONObject;

public class MapTabFragment extends Fragment {

    public static boolean manualReq = false;
    private PageViewModel pageViewModel;
    Maze maze;

    ImageButton changeDirectionButton, explorationButton, obstacleButton, clearButton;
    ToggleButton startPointButton, wayPointButton;
    Button mapResetButton, mdfButton;
    static Button updateStaticMDFButton;


    public static MapTabFragment newInstance(int index) {
        MapTabFragment fragment = new MapTabFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_map_tab, container, false);

        maze = MainActivity.getMaze();
        final RobotDirectionFragment robotDirectionFragment = new RobotDirectionFragment();

        changeDirectionButton = root.findViewById(R.id.changeDirectionImgBtn);
        explorationButton = root.findViewById(R.id.exploredImgBtn);
        obstacleButton = root.findViewById(R.id.obstaclesImgBtn);
        clearButton = root.findViewById(R.id.clearImgBtn);
        startPointButton = root.findViewById(R.id.setStartPointToggleBtn);
        wayPointButton = root.findViewById(R.id.setWayPointToggleBtn);
        updateStaticMDFButton = root.findViewById(R.id.updateBtn);
        mdfButton = root.findViewById(R.id.mdfBtn);
        mapResetButton = root.findViewById(R.id.resetMapBtn);
        
        mapResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maze.resetMaze();
            }
        });


        explorationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!maze.getExploredStatus()) {
                    showToast("Please mark a cell");
                    maze.setExploredStatus(true);
                    maze.toggleCheckedBtn("explorationButton");
                }
                else if (maze.getExploredStatus())
                    maze.setSetObstacleStatus(false);
            }
        });

        obstacleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!maze.getSetObstacleStatus()) {
                    showToast("Please mark a obstacle");
                    maze.setSetObstacleStatus(true);
                    maze.toggleCheckedBtn("obstacleButton");
                }
                else if (maze.getSetObstacleStatus())
                    maze.setSetObstacleStatus(false);
            }
        });

        changeDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotDirectionFragment.show(getActivity().getFragmentManager(), "Direction Fragment");
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!maze.getUnSetCellStatus()) {
                    showToast("Please remove a cell");
                    maze.setUnSetCellStatus(true);
                    maze.toggleCheckedBtn("clearButton");
                }
                else if (maze.getUnSetCellStatus())
                    maze.setUnSetCellStatus(false);
            }
        });


        updateStaticMDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.outputMessage("sendArena");
                manualReq = true;
                try {
                    String message = "{\"map\":[{\"explored\": \"ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\",\"length\":304,\"obstacle\":\"000000000000010042038400000000000000030C000000000000021F84000800000000000400\"}]}";

                    maze.setReceivedJsonObject(new JSONObject(message));
                    maze.updatemazeInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.outputMessage("MDF|");
            }
        });

        startPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startPointButton.getText().equals("STARTING POINT"))
                    showToast("Cancelled");
                else if (startPointButton.getText().equals("CANCEL") && !maze.getAutoUpdate()) {
                    showToast("Select robot starting point");
                    maze.setStartCoordinateStatus(true);
                    maze.toggleCheckedBtn("startPointButton");
                } else
                    showToast("click manual mode");
            }
        });

        wayPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wayPointButton.getText().equals("WAYPOINT"))
                    showToast("Cancelled");
                else if (wayPointButton.getText().equals("CANCEL")) {
                    showToast("Select a way point");
                    maze.setWaypointStatus(true);
                    maze.toggleCheckedBtn("wayPointButton");
                }
                else
                    showToast("click manual mode");
            }
        });


        return root;
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static Button getupdateStaticMDFButton() {
        return updateStaticMDFButton;
    }


}
