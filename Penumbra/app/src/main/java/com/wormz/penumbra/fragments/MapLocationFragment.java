package com.wormz.penumbra.fragments;


import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wormz.penumbra.R;
import com.wormz.penumbra.sql.LocationListCursorLoader;
import com.wormz.penumbra.sql.RunDatabaseHelper;

import java.util.Date;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class MapLocationFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MapLocationFragment";
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_LOCATIONS = 0;

    private GoogleMap mGoogleMap;
    private RunDatabaseHelper.LocationCursor mLocationCursor;

    public static MapLocationFragment newInstance(long mapId){
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, mapId);
        MapLocationFragment mapLocationFragment = new MapLocationFragment();
        mapLocationFragment.setArguments(args);
        return mapLocationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for a Run Id as an argument, and find the run
        Bundle args = getArguments();
        if (args != null){
            long runId = args.getLong(ARG_RUN_ID, -1);
            Log.d(TAG, "RUN ID: " + runId);

            if (runId != -1){
                LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_LOCATIONS, args, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mGoogleMap = getMap();

        mGoogleMap.setMyLocationEnabled(true);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long runId = args.getLong(ARG_RUN_ID, -1);
        return new LocationListCursorLoader(getActivity(), runId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLocationCursor = (RunDatabaseHelper.LocationCursor) data;
        updateUI();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLocationCursor.close();
        mLocationCursor = null;
    }

    private void updateUI(){
        Log.i(TAG, "UpdateUI called");

        if (mGoogleMap == null || mLocationCursor == null)
            return;

        // Set an overlay on the map for this run locations
        // Create a polyline with all of the points
        PolylineOptions line = new PolylineOptions();
        // Also create a latlngBounds so you can zoom to fit
        LatLngBounds.Builder latLangBuilder = new LatLngBounds.Builder();
        // Iterate over the locations
        mLocationCursor.moveToFirst();
        while(!mLocationCursor.isAfterLast()){
            Location location = mLocationCursor.getLocation();

            Log.i(TAG, "Location lat: " + location.getLatitude() + ", long: " + location.getLongitude());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            Resources resources = getResources();

            // if this is the first location, add a marker for it
            if (mLocationCursor.isFirst()){
                String startDate = new Date(location.getTime()).toString();
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(resources.getString(R.string.walk_start))
                        .snippet(resources.getString(R.string.walk_started_at_format, startDate))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mGoogleMap.addMarker(startMarkerOptions);

            }else if (mLocationCursor.isLast()){
                // If this is the last location, and not also the first add a marker
                String endDate = new Date(location.getTime()).toString();
                MarkerOptions startMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(resources.getString(R.string.walk_end))
                        .snippet(resources.getString(R.string.walk_ended_at_format, endDate));
                mGoogleMap.addMarker(startMarkerOptions);
            }

            line.add(latLng);
            latLangBuilder.include(latLng);
            mLocationCursor.moveToNext();
        }
        // Add the polyline to the map
        mGoogleMap.addPolyline(line);


        // Make the map zoom to show the track, with some padding
        // Use the size of the current display in pixels as a bounding box
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        int screenHeight = displaymetrics.heightPixels;

        // Construct a movement instruction for the map camera
        LatLngBounds latLngBounds = latLangBuilder.build();
        CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBounds, screenWidth, screenHeight, 15);
        mGoogleMap.moveCamera(movement);

    }


}
