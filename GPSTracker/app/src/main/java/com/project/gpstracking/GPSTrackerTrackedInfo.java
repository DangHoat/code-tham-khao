package com.project.gpstracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class GPSTrackerTrackedInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_info);

        Intent intent = getIntent();
        String chiName =
                intent.getStringExtra(GPS.KEY_CHILD_NAME) == null ? "" : intent.getStringExtra(GPS.KEY_CHILD_NAME);
        String date_time =
                intent.getStringExtra(GPS.KEY_DATE) == null ? "" : intent.getStringExtra(GPS.KEY_DATE);
        String address =
                intent.getStringExtra(GPS.KEY_ADDRESS) == null ? "" : intent.getStringExtra(GPS.KEY_ADDRESS);

        TextView textChilName = (TextView)findViewById(R.id.txtChildName);
        TextView textTime = (TextView)findViewById(R.id.txtTime);
        TextView textDate = (TextView)findViewById(R.id.textDate);
        TextView textAddress = (TextView)findViewById(R.id.textAddress);
        int index = date_time.indexOf(".");
        if(index > 0)
            date_time = date_time.substring(0, index);
        String[] dateStrings = date_time.split(" ");
        if(dateStrings.length == 2) {
            textTime.setText(dateStrings[1]);
            textDate.setText(dateStrings[0]);
        }

        textChilName.setText(chiName);
        textAddress.setText(address);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
