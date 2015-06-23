package com.wormz.penumbra;

import android.content.Context;
import android.location.Location;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class TrackingLocationReceiver extends LocationReceiver {
    @Override
    protected void onLocationReceived(Context context, Location location) {
        RunManager.get(context).insertLocation(location);
    }
}
