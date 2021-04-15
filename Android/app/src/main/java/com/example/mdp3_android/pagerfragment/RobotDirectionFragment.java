package com.example.mdp3_android.pagerfragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.R;

public class RobotDirectionFragment extends DialogFragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    Button saveButton, cancelButton;
    String robotDirection = "";
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_robotdirection, container, false);
        super.onCreate(savedInstanceState);

        getDialog().setTitle("Change robotDirection");
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        saveButton = rootView.findViewById(R.id.saveButton);
        cancelButton = rootView.findViewById(R.id.cancelButton);

        robotDirection = sharedPreferences.getString("robotDirection","");

        if (savedInstanceState != null)
            robotDirection = savedInstanceState.getString("robotDirection");


        final Spinner spinner = (Spinner) rootView.findViewById(R.id.directionDropdownSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String robotDirection = spinner.getSelectedItem().toString();
                editor.putString("robotDirection",robotDirection);
                ((MainActivity)getActivity()).refreshDirection(robotDirection);
                editor.commit();
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveButton = rootView.findViewById(R.id.saveButton);
        outState.putString("RobotrobotDirectionFragment", robotDirection);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
