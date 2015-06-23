package com.wormz.penumbra.sql;

import android.content.Context;
import android.database.Cursor;

import com.wormz.penumbra.RunManager;
import com.wormz.penumbra.SQLiteCursorLoader;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class LocationListCursorLoader extends SQLiteCursorLoader {
    private long mRunId;

    public LocationListCursorLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }

    @Override
    protected Cursor loadCursor() {
        return RunManager.get(getContext()).queryLocationsForRun(mRunId);
    }
}

