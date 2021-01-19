package com.project.gpstracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

public class GPSTrackedAddChild extends Activity{

    private Gson gson = new Gson();

    private String child_name;
    private String username;
    private String session_id;

    Button addChildButton;
    EditText textAddChildName;
    ImageButton btnBack_addChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_add_child);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        session_id = intent.getStringExtra(GPS.KEY_SESSION_ID);
        username = intent.getStringExtra(GPS.KEY_USERNAME);

        addChildButton = (Button)findViewById(R.id.btnAddChild);
        textAddChildName = (EditText)findViewById(R.id.txtChildNameAdd);
        btnBack_addChild = (ImageButton)findViewById(R.id.imgbtnBackaddChild);

        addChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* connect to server */
                GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_ADD_CHILD);
                poster.addProperty("session_id", session_id, String.class);
                poster.addProperty("username", username, String.class);
                child_name = textAddChildName.getText().toString().trim();
                poster.addProperty("child_name", child_name, String.class);
                ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);
                if(message.getResponseCode() == 0) {
                    Toast.makeText(getBaseContext(), String.format("%s added.", child_name),Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(getBaseContext(), message.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBack_addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
