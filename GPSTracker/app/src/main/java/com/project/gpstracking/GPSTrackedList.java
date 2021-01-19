package com.project.gpstracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GPSTrackedList extends Activity {

    private String key_user_id;
    private String username;
    private String session_id;
    private String child_name;

    private List<String>child_names;
    private List<String>child_ids;

    private Gson gson = new Gson();

    Button buttonAddNewChild;
    TextView textUserAccount;
    ListView lvChild;
    Button buttonLogout;

    ArrayAdapter aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_list);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        session_id = intent.getStringExtra(GPS.KEY_SESSION_ID);
        username = intent.getStringExtra(GPS.KEY_USERNAME);


        buttonAddNewChild = (Button)findViewById(R.id.btnAddNewChild);
        textUserAccount = (TextView)findViewById(R.id.txtUserAccount);
        textUserAccount.setText(username);
        lvChild = (ListView)findViewById(R.id.listChildName);
        buttonLogout = (Button)findViewById(R.id.btnLogout);

        lvChild.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent trackedSetting = new Intent(getBaseContext(), GPSTrackedSetting.class);
                trackedSetting.putExtra(GPS.KEY_SESSION_ID, session_id);
                key_user_id = child_ids.get(position);
                child_name = child_names.get(position);
                trackedSetting.putExtra(GPS.KEY_ID, key_user_id);
                trackedSetting.putExtra(GPS.KEY_USERNAME, username);
                trackedSetting.putExtra(GPS.KEY_CHILD_NAME, child_name);
                startActivity(trackedSetting);
                finish();
            }
        });
        buttonAddNewChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addChild = new Intent(getBaseContext(), GPSTrackedAddChild.class);
                addChild.putExtra(GPS.KEY_SESSION_ID, session_id);
                addChild.putExtra(GPS.KEY_USERNAME, username);
                startActivity(addChild);
            }
        });
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }

    private void getChildrenFromServer() {
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
                }
            }
        }
        catch (Exception ex) { }
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

    private void loadChildren() {
        getChildrenFromServer();
        aa = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, child_names);
        lvChild.setAdapter(aa);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //logout();
    }
}
