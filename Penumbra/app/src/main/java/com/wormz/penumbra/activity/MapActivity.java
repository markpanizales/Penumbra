package com.wormz.penumbra.activity;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.SingleFragmentActivity;
import com.wormz.penumbra.fragments.MapFragment;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class MapActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new MapFragment();
    }
}
