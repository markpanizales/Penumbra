package com.wormz.penumbra;

import android.content.Context;
import android.location.Location;

/**
 * Created by markanthonypanizales on 12/10/14.
 */
public class LastLocationLoader extends DataLoader<Location> {
    private long mRunId;

    public LastLocationLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }

    @Override
    public Location loadInBackground() {
        return RunManager.get(getContext()).getLastLocationForRun(mRunId);
    }

}
