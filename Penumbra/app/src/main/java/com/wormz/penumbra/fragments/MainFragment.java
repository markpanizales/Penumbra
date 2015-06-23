package com.wormz.penumbra.fragments;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wormz.penumbra.DemoDeviceAdminReceiver;
import com.wormz.penumbra.GcmRegistrationAsyncTask;
import com.wormz.penumbra.R;
import com.wormz.penumbra.activity.AlarmActivity;
import com.wormz.penumbra.activity.AudioActivity;
import com.wormz.penumbra.activity.MapActivity;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class MainFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = "MainFragment";

    private static final int ACTIVATION_REQUEST = 47; // identifies our request id

    DevicePolicyManager mDevicePolicyManager;
    ComponentName mDemoDeviceName;


    private ToggleButton mDeviceAdmin;
    private Button mLockDevice;
    private Button mRestDevice;
    private Button mRecordAudio;
    private Button mAlarm;
    private Button mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Device Policy Manager service and our receiver class
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDemoDeviceName = new ComponentName(getActivity(), DemoDeviceAdminReceiver.class);

        // GCM registration device class
        new GcmRegistrationAsyncTask(getActivity()).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        mDeviceAdmin = (ToggleButton) view.findViewById(R.id.toggle_device_admin);
        mDeviceAdmin.setOnCheckedChangeListener(this);

        mLockDevice = (Button) view.findViewById(R.id.button_lock_device);
        mLockDevice.setOnClickListener(this);

        mRestDevice = (Button) view.findViewById(R.id.button_reset_device);
        mRestDevice.setOnClickListener(this);

        mRecordAudio = (Button) view.findViewById(R.id.button_record_audio);
        mRecordAudio.setOnClickListener(this);

        mAlarm = (Button) view.findViewById(R.id.button_alarm);
        mAlarm.setOnClickListener(this);

        mMap = (Button) view.findViewById(R.id.button_map);
        mMap.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.button_lock_device:
                // We lock the screen
                Toast.makeText(getActivity(), "Locking device...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Locking device now");
                mDevicePolicyManager.lockNow();
                break;

            case R.id.button_reset_device:
                // We reset the device - this will erase entire /data partition!
                Toast.makeText(getActivity(), "Locking device...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Reseting device now - all user data will be ERASED to factory settings");

                //Caution: this will factory reset your phone.....use it with extreme care!!!
                //mDevicePolicyManager.wipeData(ACTIVATION_REQUEST);
                break;

            case R.id.button_record_audio:
                showRecordAudio();
                break;

            case R.id.button_alarm:
                showAlarm();
                break;

            case R.id.button_map:
                showMap();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            // Activate device administration
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDemoDeviceName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.app_name));
            startActivityForResult(intent, ACTIVATION_REQUEST);
        }
        Log.d(TAG, "onCheckedChange to: " + isChecked);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_OK){
                    Log.i(TAG, "Administration enabled!");
                    mDeviceAdmin.setChecked(true);
                }else{
                    Log.i(TAG, "Administration enable FAILED!");
                    mDeviceAdmin.setChecked(false);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRecordAudio(){
        Intent i = new Intent(getActivity(), AudioActivity.class);
        startActivityForResult(i, 0);
    }

    private void showAlarm(){
        Intent i = new Intent(getActivity(), AlarmActivity.class);
        startActivityForResult(i, 0);
    }

    private void showMap(){
        Intent i = new Intent(getActivity(), MapActivity.class);
        startActivity(i);
    }
}
