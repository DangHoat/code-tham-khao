package com.project.gpstracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class AndroidGPStrackingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_tracker);

        Button buttonLogin = (Button)findViewById(R.id.btnLogin);
        Button buttonSignUp = (Button)findViewById(R.id.btnSignup);
        //RadioButton radioButtonTracker = (RadioButton)findViewById(R.id.rbtnTracker);
        final RadioButton radioButtonTracked = (RadioButton)findViewById(R.id.rbtntracked);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioButtonTracked.isChecked()){
                    Intent isTracked = new Intent(getBaseContext(), GPSLogin.class);
                    isTracked.putExtra(GPS.KEY_TRACKING_TYPE,GPS.TYPE_TRACKED);
                    startActivity(isTracked);
                }
                else {
                    Intent isTracker = new Intent(getBaseContext(), GPSLogin.class);
                    isTracker.putExtra(GPS.KEY_TRACKING_TYPE,GPS.TYPE_TRACKER
                    );
                    startActivity(isTracker);
                }
            }
        });
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(getBaseContext(),GPSSignup.class);
                startActivity(signup);
            }
        });
    }
}
