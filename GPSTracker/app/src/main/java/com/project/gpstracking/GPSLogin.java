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


public class GPSLogin extends Activity{
    private Gson gson = new Gson();
    private String tracking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_login);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        tracking = intent.getStringExtra(GPS.KEY_TRACKING_TYPE);

        Button buttonLogin = (Button)findViewById(R.id.btnLogin);
        final EditText textUsername = (EditText)findViewById(R.id.txtUsername);
        final EditText textPassword = (EditText)findViewById(R.id.txtPassword);
        ImageButton imgButtonBack = (ImageButton)findViewById(R.id.imgBack_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = textUsername.getText().toString().trim();
                String password = textPassword.getText().toString();
                if(username.equals("") || password.equals("")) {
                    Toast.makeText(getBaseContext(), "Username or password is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                /* connect to server */
                GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_LOGIN);
                poster.addProperty("username", username, String.class);
                poster.addProperty("password", password, String.class);
                ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);
                if(message.getResponseCode() == 0) {
                    if (tracking.equals(GPS.TYPE_TRACKED)) {
                        Intent tracked = new Intent(getBaseContext(), GPSTrackedList.class);
                        tracked.putExtra(GPS.KEY_USERNAME, username);
                        tracked.putExtra(GPS.KEY_SESSION_ID, message.getMessage());
                        startActivity(tracked);
                        finish();
                    } else {
                        Intent tracker = new Intent(getBaseContext(), GPSTracker.class);
                        tracker.putExtra(GPS.KEY_USERNAME, username);
                        tracker.putExtra(GPS.KEY_SESSION_ID, message.getMessage());
                        startActivity(tracker);
                        finish();
                    }
                }
                else {
                    Toast.makeText(getBaseContext(), message.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        imgButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
