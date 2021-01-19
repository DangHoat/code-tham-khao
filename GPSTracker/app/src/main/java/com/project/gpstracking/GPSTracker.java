package com.project.gpstracking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GPSTracker extends Activity {
    private static final int id_datetime = 999;
    private static int day, month, year;
    private Calendar calendar;

    private List<String>child_names;
    private List<String>child_ids;
    private ListView lvSetting;
    private Button buttonOKSetting;
    private Button buttonDateSetting;
    private Button imgButtonBack;
    private CheckBox checkShowLocationByDate;
    private CheckBox checkShowUnreadLocation;
    private ListAdapter aa;

    private String username;
    private String child_name;
    private String session_id;
    private String key_id_user;
    private String seen;
    private String date;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        username = intent.getStringExtra(GPS.KEY_USERNAME);
        session_id = intent.getStringExtra(GPS.KEY_SESSION_ID);

        /* get current date */
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        getChildrenFromServer();

        /* listview setting */
        lvSetting = (ListView)findViewById(R.id.listChildSetting);
        lvSetting.setAdapter(aa);

        buttonOKSetting = (Button)findViewById(R.id.btnOKSetting);
        buttonOKSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOKSettingOnClick(v);
            }
        });

        buttonDateSetting = (Button)findViewById(R.id.btnChooseDate);
        buttonDateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(id_datetime);
            }
        });

        checkShowLocationByDate = (CheckBox)findViewById(R.id.checkShowLocationByDate);
        checkShowUnreadLocation = (CheckBox)findViewById(R.id.checkShowUnreadLocation);

        imgButtonBack = (Button)findViewById(R.id.imgbtnBackTracker);
        imgButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }
    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == id_datetime) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            Toast.makeText(getBaseContext(), String.format("%d-%d-%d", arg1, arg2+1, arg3), Toast.LENGTH_SHORT).show();
            date = String.format("%d-%d-%d", arg1, arg2+1, arg3);
            year = arg1; month = arg2; day = arg3;
        }
    };

    private void getChildrenFromServer() {
        aa = new ListAdapter(getBaseContext());
        child_names = new ArrayList<>();
        child_ids = new ArrayList<>();
        try {
        /* connect to server */
            GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_GET_CHILDREN);
            poster.addProperty("session_id", session_id, String.class);
            poster.addProperty("username", username, String.class);

            ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);
        /* get children successfully */
            if (message.getResponseCode() == 0) {
                ResponseChildren children = gson.fromJson(message.getMessage(), ResponseChildren.class);
                int length = children.toArray().length;
                for (int i = 0; i < length; i++) {
                    child_names.add(children.get(i).getName());
                    child_ids.add(children.get(i).getID());
                    aa.addChild(children.get(i).getName());
                }
            }
        }
        catch (Exception ex) { }
    }

    private void buttonOKSettingOnClick(View v) {
        try {
            Intent trackerMonitor = new Intent(getBaseContext(), GPSTrackerMonitor.class);
            trackerMonitor.putExtra(GPS.KEY_SESSION_ID, session_id);

            int checkedItem = aa.getCheckedItem();
            if(checkedItem < 0) {
                Toast.makeText(getBaseContext(), "Choose a child.", Toast.LENGTH_SHORT).show();
                return;
            }
            key_id_user = child_ids.get(checkedItem);
            child_name = child_names.get(checkedItem);

            trackerMonitor.putExtra(GPS.KEY_ID, key_id_user);
            trackerMonitor.putExtra(GPS.KEY_CHILD_NAME, child_name);
            seen = checkShowUnreadLocation.isChecked() == true ? "false" : "true";
            trackerMonitor.putExtra(GPS.KEY_SEEN, seen);

            if (checkShowLocationByDate.isChecked()) {
                trackerMonitor.putExtra(GPS.KEY_DATE, date);
            } else {
                date = "";
            }
            startActivity(trackerMonitor);
        }
        catch (Exception ex) { }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logout();
    }

    private void logout() {
        try {
        /* connect to server */
            GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_LOGOUT);
            poster.addProperty("session_id", session_id, String.class);

            ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);
        /* get children successfully */
            if (message.getResponseCode() == 0) {
                finish();
            }
            else {
                Toast.makeText(getBaseContext(), message.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex) {
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
