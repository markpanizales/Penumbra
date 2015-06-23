package com.wormz.penumbra;

import android.support.v4.app.Fragment;

import com.wormz.penumbra.fragments.MainFragment;


public class MainActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
