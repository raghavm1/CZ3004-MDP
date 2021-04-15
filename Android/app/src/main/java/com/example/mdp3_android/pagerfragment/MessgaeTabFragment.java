package com.example.mdp3_android.pagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mdp3_android.bluetooth.BluetoothService;
import com.example.mdp3_android.helper.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp3_android.R;

public class MessgaeTabFragment extends Fragment {

    private PageViewModel pageViewModel;

    SharedPreferences sharedPreferences;

    FloatingActionButton sendMessage;
    private static TextView receivedMsgTxtView;
    private EditText editTextBox;

    public static MessgaeTabFragment newInstance(int index) {
        MessgaeTabFragment fragment = new MessgaeTabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static TextView getreceivedMsgTxtView() {
        return receivedMsgTxtView;
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
        View root = inflater.inflate(R.layout.activity_message_tab, container, false);
        editTextBox =  root.findViewById(R.id.typeBoxEditText);
        receivedMsgTxtView.setMovementMethod(new ScrollingMovementMethod());

        sendMessage = root.findViewById(R.id.messageButton);

        receivedMsgTxtView =  root.findViewById(R.id.messageReceivedTextView);



        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sentText = "" + editTextBox.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("message", sharedPreferences.getString("message", "") + '\n' + sentText);
                editor.commit();
                receivedMsgTxtView.setText(sharedPreferences.getString("message", ""));
                editTextBox.setText("");

               if (BluetoothService.connStatusFlag == true) {
                  byte[] bytes = sentText.getBytes(Charset.defaultCharset());
                    BluetoothService.write(bytes);
                }
            }
        });

        return root;
    }



}
