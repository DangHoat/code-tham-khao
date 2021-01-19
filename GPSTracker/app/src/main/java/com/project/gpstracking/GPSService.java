package com.project.gpstracking;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class GPSService extends Service{

    private GPSLocationGetter locationGetter;
    private static Location last_location = null;

    private static String keyID;
    // static String usrName;
    //private static String childName;
    private static String session_id;
    private static int time;//min
    private static int distance;//meter
    private static boolean stop = false;

    private boolean hasGPS = false;
    private Gson gson = new Gson();
    private ContentResolver cr;

    DateFormat utcFormat;
    DateFormat pstFormat;

    protected LocationManager locationManager;

    public GPSService() {
        utcFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        pstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //pstFormat.setTimeZone(TimeZone.getTimeZone("PST"));
        Log.d(GPS.LOG_TAG, "GPSService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationGetter = new GPSLocationGetter(this.getBaseContext());
        cr = this.getContentResolver();
        registerReceiver(networkStateReceiver, new IntentFilter(GPS.GPS_STATE));

        try {
            Cursor cursor = getContentResolver().query(GPSServiceProvider.CONTENT_URI, null, null, null, null);
            if (cursor.moveToNext()) {
                session_id = cursor.getString(cursor.getColumnIndexOrThrow(GPSServiceProvider.KEY_SESSION));
                keyID = cursor.getString(cursor.getColumnIndexOrThrow(GPSServiceProvider.KEY_USER_ID));
                time = Integer.parseInt(
                        cursor.getString(cursor.getColumnIndexOrThrow(GPSServiceProvider.KEY_TIME)));
                distance = Integer.parseInt(
                        cursor.getString(cursor.getColumnIndexOrThrow(GPSServiceProvider.KEY_DISTANCE)));

                Log.d(GPS.LOG_TAG, "GPSService onCreate() - OK");
            }
            new GPSTask().start();
        }
        catch (Exception ex) {
            Log.d(GPS.LOG_TAG, "GPSService onCreate() - ERROR. " + ex.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /* try to turn on GPS */
    private void turnOnGPS(){
        try {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.sendBroadcast(poke);

            Log.d(GPS.LOG_TAG, "GPSService onStartCommand() - OK");
        }
        catch (Exception ex) {
            Log.d(GPS.LOG_TAG, "GPSService onStartCommand() - ERROR. " + ex.getMessage());
        }
    }

    /* try to turn off GPS */
    private void turnOffGPS(){
        try {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.sendBroadcast(poke);

            Log.d(GPS.LOG_TAG, "turnOffGPS() - OK");
        }
        catch (Exception ex) {
            Log.d(GPS.LOG_TAG, "turnOffGPS() - ERROR. " + ex.getMessage());
        }
    }

    /* check network on or off */
    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception ex) { }
        return false;
    }

    protected ResponseMessage postDataToServer(String session_id, String key_id_user, String latitude, String longitude, String date) {
        try {
                /* connect to server */
            GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_POST_LOCATION);
            poster.addProperty("session_id", session_id, String.class);
            poster.addProperty("key_id_user", key_id_user, String.class);
            poster.addProperty("longitude", longitude, Double.class);
            poster.addProperty("latitude", latitude, Double.class);
            poster.addProperty("date", date, String.class);
            ResponseMessage msg = gson.fromJson(poster.post().toString(), ResponseMessage.class);
            Log.d(GPS.LOG_TAG, "postDataToServer() - OK");
            return msg;
        }
        catch (Exception ex) {
            Log.d(GPS.LOG_TAG, "postDataToServer() - ERROR. " + ex.getMessage());
            return null;
        }
    }

    private class GPSLocationGetter implements LocationListener {

        private Context context;
        //private Location location;

        public GPSLocationGetter(Context context) {
            this.context = context;
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Log.d(GPS.LOG_TAG, "GPSLocationGetter()");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) {
            if(hasGPS) {
                try {
                    if (locationGetter.isGPSEnabled()) {
                        hasGPS = true;
                        Location l = locationGetter.getLocation();
                        if (l != null) {
                            if (last_location == null) {
                                last_location = l;
                                insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                Log.d(GPS.LOG_TAG, "onProviderEnabled() get location - OK");
                            } else {
                                if (l.distanceTo(last_location) >= (float) distance)
                                    last_location = l;
                                insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                Log.d(GPS.LOG_TAG, "onProviderEnabled() get location - OK");
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    Log.d(GPS.LOG_TAG, "onProviderEnabled() - ERROR. " + ex.getMessage());
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onLocationChanged(Location location) { }

        public Location getLocation() {
            try {
                if(isGPSEnabled()) {
                    Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (l == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        Log.d(GPS.LOG_TAG, "getLocation() - OK. " + l.toString());
                    }
                    Log.d(GPS.LOG_TAG, "getLocation() - FAIL");
                    return l;
                }
            }
            catch (Exception e) {
                Log.d(GPS.LOG_TAG, "getLocation() - ERROR. " + e.getMessage());
            }
            return null;
        }

        public boolean isGPSEnabled() {
            try {
                if (locationManager != null)
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        return true;
            }
            catch (Exception ex) { }
            return false;
        }

        public boolean isNetworkEnabled() {
            try {
                if (locationManager != null)
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        return true;
            }
            catch (Exception ex) { }
            return false;
        }
    }

    private class GPSTask extends Thread {

        @Override
        public void run() {
            super.run();
            while (!stop) {
                Log.d(GPS.LOG_TAG, "GPSTask run - OK");
                try {
                    if (!stop) {
                /* gps is enable */
                        if (locationGetter.isGPSEnabled()) {
                            hasGPS = true;
                            Location l = locationGetter.getLocation();
                            if (l != null) {
                                if (last_location == null) {
                                    last_location = l;
                                    insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                    Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                } else {
                                    if (l.distanceTo(last_location) >= (float) distance) {
                                        last_location = l;
                                        insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                        Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                    }
                                }
                            }
                            // l == null
                            else {
                                // try to turn on gps
                                turnOnGPS();
                                if (locationGetter.isGPSEnabled()) {
                                    hasGPS = true;
                                    l = locationGetter.getLocation();
                                    if (l != null) {
                                        if (last_location == null) {
                                            last_location = l;
                                            insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                            Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                        } else {
                                            if (l.distanceTo(last_location) >= (float) distance)
                                                last_location = l;
                                            insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                            Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                        }
                                        turnOffGPS();
                                    } else {
                                        hasGPS = false;
                                    }
                                }
                            }
                        }
                        /* gps is not enable */
                        else {
                            // try to turn on gps
                            turnOnGPS();
                            if (locationGetter.isGPSEnabled()) {
                                hasGPS = true;
                                Location l = locationGetter.getLocation();
                                if (l != null) {
                                    if (last_location == null) {
                                        last_location = l;
                                        insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                        Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                    } else {
                                        if (l.distanceTo(last_location) >= (float) distance)
                                            last_location = l;
                                        insertIntoDatabase(l.getLatitude(), l.getLongitude());
                                        Log.d(GPS.LOG_TAG, "GPSTask gets location - OK");
                                    }
                                    turnOffGPS();
                                } else {
                                    hasGPS = false;
                                }
                            }
                        }

                /* send to server */
                /* network is enable */
                        if (isNetworkAvailable()) {
                            while (true) {
                                Cursor cursor = cr.query(GPSContentProvider.CONTENT_URI, null, null, null, null);
                                if (cursor.moveToNext()) {
                                    String id = cursor.getString(
                                            cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_ID));
                                    String latitude = cursor.getString(
                                            cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_LATITUDE));
                                    String longitude = cursor.getString(
                                            cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_LONGITUDE));
                                    String str_date = cursor.getString(
                                            cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_TIME));
                            Date date = null;
                            try {
                                date = utcFormat.parse(str_date);
                                str_date = pstFormat.format(date);
                            }
                            catch (Exception ex) { }
                                    ResponseMessage msg = postDataToServer(session_id, keyID, latitude, longitude, str_date);
                                    if (msg.getResponseCode() == 0) {
                                        cr.delete(GPSContentProvider.CONTENT_URI,
                                                String.format("%s=%s", GPSContentProvider.KEY_ID, id), null);
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    Thread.sleep(time * 1000);
                }
                catch (Exception ex) { }
            }
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                    // network connected
                    while (true) {
                        Cursor cursor = cr.query(GPSContentProvider.CONTENT_URI, null, null, null, null);
                        if (cursor.moveToNext()) {
                            String id = cursor.getString(
                                    cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_ID));
                            String latitude = cursor.getString(
                                    cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_LATITUDE));
                            String longitude = cursor.getString(
                                    cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_LONGITUDE));
                            String str_date = cursor.getString(
                                    cursor.getColumnIndexOrThrow(GPSContentProvider.KEY_TIME));
                            Date date;
                            try {
                                date = utcFormat.parse(str_date);
                                str_date = pstFormat.format(date);
                            } catch (Exception ex) { }
                            ResponseMessage msg = postDataToServer(session_id, keyID, latitude, longitude, str_date);
                            if (msg.getResponseCode() == 0) {
                                cr.delete(GPSContentProvider.CONTENT_URI,
                                        String.format("%s=%s", GPSContentProvider.KEY_ID, id), null);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    };

    protected void insertIntoDatabase(double latitude, double longitude) {
        try {
            ContentValues values = new ContentValues();
            values.put(GPSContentProvider.KEY_LATITUDE, latitude);
            values.put(GPSContentProvider.KEY_LONGITUDE, longitude);
            cr.insert(GPSContentProvider.CONTENT_URI, values);
        }
        catch (Exception ex) { }
    }

    private void logout() {
        try {
        /* connect to server */
            GPSDataPoster poster = new GPSDataPoster(GPSDataPoster.METHOD_LOGOUT);
            poster.addProperty("session_id", session_id, String.class);

            gson.fromJson(poster.post().toString(), ResponseMessage.class);
        }
        catch (Exception ex) { }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //logout();
    }
}