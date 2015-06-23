package com.wormz.penumbra;

import android.content.Context;

import com.wormz.penumbra.model.Run;

/**
 * Created by markanthonypanizales on 12/9/14.
 */
public class RunLoader extends DataLoader<Run> {
    private long mRunId;

    public RunLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }

    @Override
    public Object loadInBackground() {
        return RunManager.get(getContext()).getRun(mRunId);
    }
}
