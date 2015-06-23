package com.wormz.penumbra.activity;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.SingleFragmentActivity;
import com.wormz.penumbra.fragments.AlarmFragment;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class AlarmActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AlarmFragment();
    }
}
