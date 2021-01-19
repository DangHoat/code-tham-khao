package com.project.gpstracking;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
public class GPSTrackedSetting extends Activity {

    private String keyID;
    private String usrName;
    private String childName;
    private String session_id;
    private int time;//min
    private int distance;//meter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_setting);

        Intent intent = getIntent();
        keyID = intent.getStringExtra(GPS.KEY_ID);
        usrName = intent.getStringExtra(GPS.KEY_USERNAME);
        childName = intent.getStringExtra(GPS.KEY_CHILD_NAME);
        session_id = intent.getStringExtra(GPS.KEY_SESSION_ID);

        final String []spIntervalSet = {
                "1 min",
                "2 min",
                "5 min",
                "10 min",
                "20 min",
                "30 min",
                "45 min",
                "1 hour"
        };
        final String []spDistanceSet = {
                "50m",
                "100m",
                "200m",
                "500m",
                "800m",
                "1km",
                "1.5km",
                "2km"
        };

        TextView textChildName = (TextView)findViewById(R.id.txtChildName);
        textChildName.setText(childName);
        final Spinner spInterval = (Spinner)findViewById(R.id.spInterval);
        final Spinner spDistance = (Spinner)findViewById(R.id.spDistance);
        final Button buttonOKSetting = (Button)findViewById(R.id.btnOkSetting);
        ImageButton imgButtonBack = (ImageButton)findViewById(R.id.imgbtnBackSetting);

        buttonOKSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    /* get time */
                    String spInter = spInterval.getSelectedItem().toString();
                    time = 5;
                    switch (spInter) {
                        case "1 min":
                            time = 1;
                            break;
                        case "2 min":
                            time = 2;
                            break;
                        case "5 min":
                            time = 5;
                            break;
                        case "10 min":
                            time = 10;
                            break;
                        case "20 min":
                            time = 20;
                            break;
                        case "30 min":
                            time = 30;
                            break;
                        case "45 min":
                            time = 45;
                            break;
                        case "1 hour":
                            time = 60;
                            break;
                        default:
                            break;
                    }

                    /* get distance */
                    String spDistan = spDistance.getSelectedItem().toString();
                    distance = 200;
                    switch (spDistan) {
                        case "50m":
                            distance = 50;
                            break;
                        case "100m":
                            distance = 100;
                            break;
                        case "200m":
                            distance = 200;
                            break;
                        case "500m":
                            distance = 500;
                            break;
                        case "800m":
                            distance = 800;
                            break;
                        case "1km":
                            distance = 1000;
                            break;
                        case "1.5km":
                            distance = 1500;
                            break;
                        case "2km":
                            distance = 2000;
                            break;
                        default:
                            break;
                    }

                    /* start service */
                    Intent childInfo = new Intent(getApplicationContext(), GPSService.class);
                    childInfo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    /* insert data into database */
                    // clear all
                    getContentResolver().delete(GPSServiceProvider.CONTENT_URI, null, null);
                    // insert
                    ContentValues values = new ContentValues();
                    values.put(GPSServiceProvider.KEY_DISTANCE, String.valueOf(distance));
                    values.put(GPSServiceProvider.KEY_TIME, String.valueOf(time * 60));
                    values.put(GPSServiceProvider.KEY_USER_ID, keyID);
                    values.put(GPSServiceProvider.KEY_SESSION, session_id);
                    getContentResolver().insert(GPSServiceProvider.CONTENT_URI, values);

                    /* stop service if it runs */
                    try {
                        stopService(new Intent(getApplicationContext(), GPSService.class));
                    }
                    catch (Exception ex) { }
                    startService(childInfo);
                    //startActivity(new Intent(getApplicationContext(), AndroidGPStrackingActivity.class));
                    finish();
                } catch (Exception ex) {
                    String error = "Error..";
                    Log.d(error, "Please check and try again");
                }
            }
        });
        imgButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tracked = new Intent(getBaseContext(), GPSTrackedList.class);
                tracked.putExtra(GPS.KEY_USERNAME, usrName);
                tracked.putExtra(GPS.KEY_SESSION_ID, session_id);
                startActivity(tracked);
                finish();
            }
        });

        ArrayAdapter aaInt = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, spIntervalSet);
        spInterval.setAdapter(aaInt);

        ArrayAdapter aaDis = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, spDistanceSet);
        spDistance.setAdapter(aaDis);
    }
/*
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(GPS.GPS_SERVICE.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
*/
}