package com.project.gpstracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,GPSService.class);
        arg0.startService(intent);
        Log.d(GPS.LOG_TAG, "ServiceStartup.");
    }
}
