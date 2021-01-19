package com.project.gpstracking;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

public class GPSTrackerMonitor extends FragmentActivity implements OnMapReadyCallback {

    public final static String logcat_googlemap = "logcat_googlemap";


    private static GoogleMap mMap;

    private ImageButton btMapType;
    private ImageButton btRefresh;
    private ImageButton btBack;
    private static int mapType = GoogleMap.MAP_TYPE_NORMAL;

    private String child_name;
    private String session_id;
    private String key_id_user;
    private String seen;
    private String date;

    private GoogleApiClient client;
    private Gson gson = new Gson();
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_monitor);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* get intent */
        Intent trackerMonitor = getIntent();
        session_id = trackerMonitor.getStringExtra(GPS.KEY_SESSION_ID);
        key_id_user = trackerMonitor.getStringExtra(GPS.KEY_ID);
        child_name = trackerMonitor.getStringExtra(GPS.KEY_CHILD_NAME);
        seen = trackerMonitor.getStringExtra(GPS.KEY_SEEN);
        date = trackerMonitor.getStringExtra(GPS.KEY_DATE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(logcat_googlemap, "SupportMapFragment.getMapAsync(this) is OK");

        mMap = mapFragment.getMap();
        mMap.setMapType(mapType);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        geocoder = new Geocoder(this, Locale.getDefault());

        btMapType = (ImageButton) findViewById(R.id.btMapType);
        btMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                    mMap.setMapType(mapType);
                    btMapType.setImageResource(R.drawable.button_map_normal);
                    Log.d(logcat_googlemap, "MapType changed to MAP_TYPE_SATELLITE");
                } else {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                    mMap.setMapType(mapType);
                    btMapType.setImageResource(R.drawable.button_map_sattellite);
                    Log.d(logcat_googlemap, "MapType changed to MAP_TYPE_NORMAL");
                }
            }
        });

        btRefresh = (ImageButton)findViewById(R.id.btRefresh);
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearMap();
                getLocations("false");
            }
        });

        btBack = (ImageButton)findViewById(R.id.btBack);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(
                            marker.getPosition().latitude,marker.getPosition().longitude,5);
                }
                catch (Exception ex) {}

                Intent intent = new Intent(getBaseContext(), GPSTrackerTrackedInfo.class);
                intent.putExtra(GPS.KEY_CHILD_NAME, child_name);
                intent.putExtra(GPS.KEY_DATE, marker.getTitle());
                if(addressList != null) {
                    String adddr = "";
                    /*if(addressList.get(0).getAddressLine(0) != null)
                        adddr += addressList.get(0).getAddressLine(0) + ", ";
                    if(addressList.get(0).getSubLocality() != null)
                        adddr += addressList.get(0).getSubLocality() + ", ";
                    if(addressList.get(0).getSubAdminArea() != null)
                        adddr += addressList.get(0).getSubAdminArea() + ", ";
                    if(addressList.get(0).getAdminArea() != null)
                        adddr += addressList.get(0).getAdminArea();*/
                    for(int i = 0; i <= addressList.get(0).getMaxAddressLineIndex(); i++) {
                        if(addressList.get(0).getAddressLine(i) != null)
                            adddr += addressList.get(0).getAddressLine(i);
                        if(i < addressList.get(0).getMaxAddressLineIndex())
                            adddr += ", ";
                    }

                    intent.putExtra(GPS.KEY_ADDRESS, adddr);
                }
                startActivity(intent);

                return false;
            }
        });

        try {
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;
        //mMap.setMapType(mapType);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(fZoom));
        getLocations(seen);
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "GPSTrackerMonitor Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.project.gpstracking/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "GPSTrackerMonitor Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.project.gpstracking/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private void getLocations(String seen) {

        try {
        /* connect to server */
            GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_GET_LOCATIONS);
            poster.addProperty("session_id", session_id, String.class);
            poster.addProperty("key_id_user", key_id_user, String.class);
            poster.addProperty("seen", seen, String.class);
            poster.addProperty("date", date, String.class);
            ResponseMessage message = gson.fromJson(poster.post().toString(), ResponseMessage.class);
        /* get children successfully */
            if (message.getResponseCode() == 0) {
                ResponseLocations locations = gson.fromJson(message.getMessage(), ResponseLocations.class);
                int length = locations.toArray().length;
                for (int i = 0; i < length; i++) {
                    LatLng latLng = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
                    MarkerOptions mark = new MarkerOptions();
                    mark.position(latLng).title(String.format("%s",locations.get(i).getDateTime()));
                    mMap.addMarker(mark);
                    if(i < length - 1)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
