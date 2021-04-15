package com.example.mdp3_android.pagerfragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import android.app.DialogFragment;

import com.example.mdp3_android.MainActivity;
import com.example.mdp3_android.R;

public class ReconfigureFragment extends DialogFragment {
    private static final String TAG = "ReconfigureFragment";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Button saveBtn, cancelReconfigureBtn;
    EditText f1ValueEditText, f2ValueEditText;
    String f1Value, f2Value;
    View rootView;
    View commsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        showLog("Entering onCreateView");
        rootView = inflater.inflate(R.layout.activity_reconfigure, container, false);
        commsView = inflater.inflate(R.layout.activity_message_tab, container, false);

        super.onCreate(savedInstanceState);

        getDialog().setTitle("Reconfiguration");

        saveBtn = rootView.findViewById(R.id.saveBtn);
        cancelReconfigureBtn = rootView.findViewById(R.id.cancelReconfigureBtn);
        f1ValueEditText = rootView.findViewById(R.id.f1ValueEditText);
        f2ValueEditText = rootView.findViewById(R.id.f2ValueEditText);

        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("F1")) {
            f1ValueEditText.setText(sharedPreferences.getString("F1", ""));
            f1Value = sharedPreferences.getString("F1", "");
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }
        if (sharedPreferences.contains("F2")) {
            f2ValueEditText.setText(sharedPreferences.getString("F2", ""));
            f2Value = sharedPreferences.getString("F2", "");
        }

        if (savedInstanceState != null) {
            f1Value = savedInstanceState.getStringArray("F1F2 value")[0];
            f2Value = savedInstanceState.getStringArray("F1F2 value")[1];
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked saveBtn");
                editor = sharedPreferences.edit();
                editor.putString("F1", f1ValueEditText.getText().toString());
                editor.putString("F2", f2ValueEditText.getText().toString());
                editor.commit();
                if (!sharedPreferences.getString("F1", "").equals(""))
                    f1Value = f1ValueEditText.getText().toString();
                if (!sharedPreferences.getString("F2", "").equals(""))
                    f2Value = f2ValueEditText.getText().toString();
                Toast.makeText(getActivity(), "Saving values...", Toast.LENGTH_SHORT).show();

                showLog("Exiting saveBtn");
                getDialog().dismiss();
            }
        });

        cancelReconfigureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked cancel ReconfigureationBtn");
                if (sharedPreferences.contains("F1"))
                    f1ValueEditText.setText(sharedPreferences.getString("F1", ""));
                if (sharedPreferences.contains("F2"))
                    f2ValueEditText.setText(sharedPreferences.getString("F2", ""));
                showLog("Exiting cancel ReconfigureationBtn");
                getDialog().dismiss();
            }
        });
        showLog("Exiting onCreateView");
        return rootView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showLog("Entering onDismiss");
        super.onDismiss(dialog);
        if (f1Value != null && !f1Value.equals(""))
            MainActivity.getF1().setContentDescription(f1Value);
        if (f2Value != null && !f2Value.equals(""))
            MainActivity.getF2().setContentDescription(f2Value);
        f1ValueEditText.clearFocus();

        showLog("Exiting onDismiss");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        String[] value = new String[]{f1Value, f2Value};
        showLog("Exiting onSaveInstanceState");
        outState.putStringArray(TAG, value);
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }


}
