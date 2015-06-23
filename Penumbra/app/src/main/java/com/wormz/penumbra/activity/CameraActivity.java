package com.wormz.penumbra.activity;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.SingleFragmentActivity;
import com.wormz.penumbra.fragments.CameraFragment;

/**
 * Created by macx-mini on 3/9/15.
 */
public class CameraActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CameraFragment();
    }
}
