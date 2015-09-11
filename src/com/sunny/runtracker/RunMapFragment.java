package com.sunny.runtracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.sunny.runtracker.RunDatabaseHelper.LocationCursor;

public class RunMapFragment extends SupportMapFragment 
	implements LoaderCallbacks<Cursor>,  BaiduMap.OnMarkerClickListener {
	
	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int LOAD_LOCATIONS = 0;
	
	private BaiduMap mBaiduMap;
	private LocationCursor mLocationCursor;
	private BitmapDescriptor startBitmap, endBitmap;
	
	public static RunMapFragment newInstance(long runId) {
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunMapFragment rf = new RunMapFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Check for a Run ID as an argument, and find the run
		Bundle args = getArguments();
		if (args != null) {
			long runId = args.getLong(ARG_RUN_ID, -1);
			if (runId != -1) {
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_LOCATIONS, args, this);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, parent, savedInstanceState);
		
		// Stash a reference to the BaiduMap
		mBaiduMap = getBaiduMap();
		// Show the user's location
		mBaiduMap.setMyLocationEnabled(true);
		
		startBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
		endBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
		
		return view;
	}
	
	@SuppressWarnings("deprecation")
	private void updateUI() {
		if (mBaiduMap == null || mLocationCursor == null) {
			return;
		}
		
		// Set up an overlay on the map for this run's locations
		// Create a polyline with all of the points
		PolylineOptions line = new PolylineOptions().width(5).color(0xAAFF0000);
		// Also create a LatngBuonds so you can zoom to fit
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		
		// Iterate over the locations
		mLocationCursor.moveToFirst();
		List<LatLng> latLngList = new ArrayList<LatLng>();
		while (!mLocationCursor.isAfterLast()) {
			Location location = mLocationCursor.getlLocation();
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			
			Resources resources = getResources();
			
			if (mLocationCursor.isFirst()) {
				// If this is the first location, add a marker for it
				String startDate = new Date(location.getTime()).toString();
				Bundle bundle = new Bundle();
				bundle.putString("Snippet", getString(R.string.run_started_at_format, startDate));
				MarkerOptions startMarkerOptions = new MarkerOptions()
					.position(latLng)
					.title(resources.getString(R.string.run_start))
					.extraInfo(bundle)
					.icon(startBitmap);
				mBaiduMap.addOverlay(startMarkerOptions);
			} else if (mLocationCursor.isLast()) {
				// If this is the last location, and not also the first, add a marker
				String endDate = new Date(location.getTime()).toString();
				Bundle bundle = new Bundle();
				bundle.putString("Snippet", getString(R.string.run_finished_at_format, endDate));
				MarkerOptions finishMarkerOptions = new MarkerOptions()
					.position(latLng)
					.title(resources.getString(R.string.run_finish))
					.extraInfo(bundle)
					.icon(endBitmap);
				mBaiduMap.addOverlay(finishMarkerOptions);
			}
			
			latLngList.add(latLng);
			latLngBuilder.include(latLng);
			mLocationCursor.moveToNext();
		}
		line.points(latLngList);
		
		// 设置点击Marker的监听器
		mBaiduMap.setOnMarkerClickListener(this);
		// Add the polyline to the map
		mBaiduMap.addOverlay(line);
		// Make the map zoom to show the track, with some padding
		// Use the size of the current display in pixels as a bounding box
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		// 把地图定位到旅途所在地
		LatLngBounds latLngBounds = latLngBuilder.build();
		MapStatus status = new MapStatus.Builder()
				.target(latLngBounds.getCenter())
				// 设置地图操作中心点在屏幕的坐标, 只有在 OnMapLoadedCallback.onMapLoaded()之后设置才生效
				.targetScreen(new Point(display.getWidth() / 2, display.getHeight() / 2))
				.zoom(13)
				.build();
		// MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLngBounds(latLngBounds);
		MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(status);
		mBaiduMap.animateMapStatus(statusUpdate);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long runId = args.getLong(ARG_RUN_ID, -1);
		return new LocationListCursorLoader(getActivity(), runId);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mLocationCursor = (LocationCursor) data;
		updateUI();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Stop using the data
		mLocationCursor.close();
		mLocationCursor = null;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			String title = marker.getTitle();
			String snippet = marker.getExtraInfo().getString("Snippet");
			new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setMessage(snippet)
				.show();
		}
		return true;
	}

	@Override
	public void onDestroy() {
		// 释放资源
		startBitmap.recycle();
		endBitmap.recycle();
		mBaiduMap = null;
		
		super.onDestroy();
	}

}
