package com.sunny.runtracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RunFragment extends Fragment {
	
	private Button mStartButton, mStopButton;
	private TextView mStartedtTextView, mLatitudeTextView, 
		mLongitudeTextView, mAltitudeTextView, mDurationTextView;
	
	private RunManager mRunManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		mRunManager = RunManager.get(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_run, container, false);
		
		mStartedtTextView = (TextView) view.findViewById(R.id.run_startedTextView);
		mLatitudeTextView = (TextView) view.findViewById(R.id.run_latitudeTextView);
		mLongitudeTextView = (TextView) view.findViewById(R.id.run_longitudeTextView);
		mAltitudeTextView = (TextView) view.findViewById(R.id.run_altitudeTextView);
		mDurationTextView = (TextView) view.findViewById(R.id.run_durationTextView);
		
		mStartButton = (Button) view.findViewById(R.id.run_startButton);
		mStartButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mRunManager.startLocationUpdates();
				updateUI();
			}
		});
		
		mStopButton = (Button) view.findViewById(R.id.run_stopButton);
		mStopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mRunManager.stopLocationUpdates();
				updateUI();
			}
		});
		
		return view;
	}

	private void updateUI() {
		boolean started = mRunManager.isTrackingRun();
		
		mStartButton.setEnabled(!started);
		mStopButton.setEnabled(started);
	}

}
