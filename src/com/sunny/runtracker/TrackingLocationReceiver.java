package com.sunny.runtracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class TrackingLocationReceiver extends LocationReceiver {
	
	private static final String TAG = "TrackingLocationReceiver";

	@Override
	protected void onLocationReceived(Context context, Location location) {
		Log.i(TAG, "Insert locatin into database!");
		RunManager.get(context).insertLocation(location);
	}

}
