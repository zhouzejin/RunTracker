package com.sunny.runtracker;

import com.sunny.runtracker.RunDatabaseHelper.LocationCursor;
import com.sunny.runtracker.RunDatabaseHelper.RunCursor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class RunManager {
	
	public static final String ACTION_LOCATION = 
			"com.sunny.runtracker.ACTION_LOCATION";
	
	private static final String TAG = "RunManager";
	private static final String PREFS_FILE = "runs";
	private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";
	
	private static RunManager sRunManager;
	
	private Context mAppContext;
	private LocationManager mLocationManager;
	private RunDatabaseHelper mHelper;
	private SharedPreferences mPrefs;
	private long mCurrentId;
	
	// The private constructor forces users to use RunManager.get(Context)
	private RunManager(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager) 
				mAppContext.getSystemService(Context.LOCATION_SERVICE);
		mHelper = new RunDatabaseHelper(mAppContext);
		mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		mCurrentId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
	}
	
	public static RunManager get(Context context) {
		if (sRunManager == null) {
			// Use the application context to avoid leaking activities
			sRunManager = new RunManager(context.getApplicationContext());
		}
		return sRunManager;
	}
	
	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		Log.i(TAG, "getLocationPendingIntent");
		Intent broadcast = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}
	
	public void startLocationUpdates() {
		Log.i(TAG, "startLocationUpdates");
		String provider = LocationManager.GPS_PROVIDER;
		
		// Get the last known location and broadcast it if you have one
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if (lastKnown != null) {
			// Reset the time to now
			lastKnown.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnown);
		}
		
		// Start updates from the location manager
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
	}
	
	private void broadcastLocation(Location location) {
		Log.i(TAG, "broadcastLocation");
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);
	}

	public void stopLocationUpdates() {
		Log.i(TAG, "stopLocationUpdates");
		PendingIntent pi = getLocationPendingIntent(false);
		if (pi != null) {
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	public boolean isTrackingRun() {
		return getLocationPendingIntent(false) != null;
	}
	
	public boolean isTrackingRun(Run run) {
		return run != null && run.getId() == mCurrentId;
	}
	
	public Run startNewRun() {
		// Insert a run into the db. 
		Run run = insertRun();
		// Start tracking the run
		startTrackingRun(run);
		return run;
	}

	private Run insertRun() {
		Run run = new Run();
		run.setId(mHelper.insertRun(run));
		return run;
	}
	
	public void insertLocation(Location location) {
		if (mCurrentId != -1) {
			mHelper.insertLocation(mCurrentId, location);
		} else {
			Log.e(TAG, "Location received with on tracking run; ignoring.");
		}
	}

	public void startTrackingRun(Run run) {
		// Keep the ID
		mCurrentId = run.getId();
		// Store it in shared preferences
		mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentId).commit();
		// Start location updates
		startLocationUpdates();
	}
	
	public void stopRun() {
		stopLocationUpdates();
		mCurrentId = -1;
		mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
	}
	
	public RunCursor queryRuns() {
		return mHelper.queryRuns();
	}
	
	public Run getRun(long id) {
		Run run = null;
		RunCursor cursor = mHelper.queryRun(id);
		cursor.moveToFirst();
		
		// If you got a row, get a run. 
		if (!cursor.isAfterLast())
			run = cursor.getRun();
		cursor.close();
		
		return run;
	}
	
	public Location getLastLocationForRun(long runId) {
		Location location = null;
		LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
		cursor.moveToFirst();

		// If you got a row, get a location. 
		if (!cursor.isAfterLast())
			location = cursor.getlLocation();
		cursor.close();

		return location;
	}

}
