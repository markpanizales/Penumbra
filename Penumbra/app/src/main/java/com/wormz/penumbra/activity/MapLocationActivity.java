package com.wormz.penumbra.activity;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.SingleFragmentActivity;
import com.wormz.penumbra.fragments.MapLocationFragment;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class MapLocationActivity extends SingleFragmentActivity{
    public static final String EXTRA_RUN_ID = "com.wormz.penumbra.run_id";

    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1){
            return MapLocationFragment.newInstance(runId);
        }else{
            return new MapLocationFragment();
        }

    }
}
