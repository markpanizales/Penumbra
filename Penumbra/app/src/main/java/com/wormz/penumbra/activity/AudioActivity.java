package com.wormz.penumbra.activity;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.SingleFragmentActivity;
import com.wormz.penumbra.fragments.AudioFragment;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class AudioActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return new AudioFragment();
    }
}
