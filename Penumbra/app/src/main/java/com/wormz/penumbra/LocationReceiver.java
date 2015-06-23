package com.wormz.penumbra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class LocationReceiver extends BroadcastReceiver {
    private final static String TAG = "LocationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // If you got a Location extra, use it
        Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);

        if (location != null){
            onLocationReceived(context, location);
            return;
        }

        // If you get here something else happend
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)){
            boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }

    }

    protected void onLocationReceived(Context context, Location location){
        Log.d(TAG, this + "Got location from " + location.getProvider() + ":" + location.getLatitude() + ", " + location.getLongitude());

    }

    protected void onProviderEnabledChanged(boolean enabled){
        Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));

    }
}
