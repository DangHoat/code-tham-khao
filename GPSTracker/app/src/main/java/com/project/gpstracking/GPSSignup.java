package com.project.gpstracking;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

public class GPSSignup extends Activity{

    private EditText textUsername;
    private EditText textPassword;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_signup);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button buttonSignup = (Button)findViewById(R.id.btnSignup);
        textUsername = (EditText)findViewById(R.id.txtUsername);
        textPassword = (EditText)findViewById(R.id.txtPassword);
        ImageButton imgButtonBack = (ImageButton)findViewById(R.id.imgBack);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = textUsername.getText().toString().trim();
                String password = textPassword.getText().toString();

                if(username.equals("") || password.equals("")) {
                    Toast.makeText(getBaseContext(), "Username or password is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                /* connect to server */
                GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_REGISTER_ACCOUNT);
                poster.addProperty("username", username, String.class);
                poster.addProperty("password", password, String.class);

                ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);

                if(message.getResponseCode() == 0) {
                    Toast.makeText(getBaseContext(), message.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
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