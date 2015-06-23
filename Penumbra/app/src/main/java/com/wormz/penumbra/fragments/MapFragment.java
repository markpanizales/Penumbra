package com.wormz.penumbra.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wormz.penumbra.LastLocationLoader;
import com.wormz.penumbra.LocationReceiver;
import com.wormz.penumbra.R;
import com.wormz.penumbra.RunLoader;
import com.wormz.penumbra.RunManager;
import com.wormz.penumbra.activity.MapLocationActivity;
import com.wormz.penumbra.model.Run;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class MapFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "MapFragment";
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_RUN = 0;
    public static final int LOAD_LOCATION = 1;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver(){
        @Override
        protected void onLocationReceived(Context context, Location location) {
            if (!mRunManager.isTrackingRun(mRun))
                return;

            mLocation = location;
            if (isVisible())
                updateUI();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
//            int toastText = enabled ? R.string.gender: R.string.edit_gender;
//            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };


    private Run mRun;
    private Location mLocation;
    private RunManager mRunManager;

    private Button mTrack;
    private Button mMap;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRunManager = RunManager.get(getActivity());

        // If the map activity press the back arrow.
        if (mRunManager.isTrackingRun()){
            mRun = mRunManager.getLastRun();

            long runId = mRun.getId();
            Bundle args = new Bundle();
            args.putLong(ARG_RUN_ID, runId);

            //setArguments(args);

            if (runId != -1){
                //mRun = mRunManager.getRun(runId);
                //mLocation = mRunManager.getLastLocationForRun(runId);
                LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_RUN, args, new RunLoaderCallBacks());
                lm.initLoader(LOAD_LOCATION, args, new LocationLoaderCallbacks());
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mTrack = (Button) view.findViewById(R.id.button_track);
        mTrack.setOnClickListener(this);

        mMap = (Button) view.findViewById(R.id.button_map);
        mMap.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.button_track:
                showTrack();
                break;
            case R.id.button_map:
                showMap();
                break;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart");
        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    private void showTrack(){
        if (!mRunManager.isTrackingRun()){
            mTrack.setText(getResources().getString(R.string.stop_tracking));

            if (mRun == null){
                mRun = mRunManager.startNewRun();
            }else{
                mRunManager.startTrackingRun(mRun);
            }

            updateUI();

        }else{
            mTrack.setText(getResources().getString(R.string.track));
            mRunManager.stopRun();

        }
    }

    private void showMap(){
        if (mRun != null){
            Log.i(TAG, "ID: " + mRun.getId());

            Intent i = new Intent(getActivity(), MapLocationActivity.class);
            i.putExtra(MapLocationActivity.EXTRA_RUN_ID, mRun.getId());
            startActivity(i);
        }

    }

    private void updateUI(){

        if (mRun != null && mLocation != null){
            Log.i(TAG, "Lat: " + mLocation.getLatitude() + ", long: " + mLocation.getLongitude() + ", alt: " + mLocation.getAltitude());
            //showDoctorMap();

        }
    }


    private class RunLoaderCallBacks implements LoaderManager.LoaderCallbacks<Run>{

        @Override
        public Loader<Run> onCreateLoader(int id, Bundle args) {
            return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(Loader<Run> loader, Run data) {
            mRun = data;
            updateUI();

        }

        @Override
        public void onLoaderReset(Loader<Run> loader) {
            // Do nothing
        }
    }

    private class LocationLoaderCallbacks implements LoaderManager.LoaderCallbacks<Location> {

        @Override
        public Loader<Location> onCreateLoader(int id, Bundle args) {
            return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(Loader<Location> loader, Location location) {
            mLocation = location;
            updateUI();

        }

        @Override
        public void onLoaderReset(Loader<Location> arg0) {
            // Do nothing

        }

    }
}
