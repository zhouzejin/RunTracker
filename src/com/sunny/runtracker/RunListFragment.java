package com.sunny.runtracker;

import com.sunny.runtracker.RunDatabaseHelper.RunCursor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class RunListFragment extends ListFragment {
	
	private static final int REQUEST_NEW_RUN = 0;
	
	private RunCursor mCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		//Query the list of runs
		mCursor = RunManager.get(getActivity()).queryRuns();
		
		// Create an adapter to point at this cursor
		RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mCursor);
		setListAdapter(adapter);
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		
		super.onDestroy();
	}
	
	private static class RunCursorAdapter extends CursorAdapter {
		
		private RunCursor mRunCursor;
		
		public RunCursorAdapter(Context context, RunCursor c) {
			super(context, c, 0);
			mRunCursor = c;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Use a layout inflater to get a row view. 
			LayoutInflater inflater = (LayoutInflater) 
					context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.
					inflate(android.R.layout.simple_list_item_1, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Get the run for the current row. 
			Run run = mRunCursor.getRun(); // 这里的RunCursor是CursorAdapter已经定位的cursor
			
			// Set up the start date text view. 
			TextView startDateTextView = (TextView) view;
			String cellText = 
					context.getString(R.string.cell_text, run.getStartDate());
			startDateTextView.setText(cellText);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.run_list_options, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_new_run:
			Intent intent = new Intent(getActivity(), RunActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_NEW_RUN == requestCode) {
			mCursor.requery();
			((RunCursorAdapter)getListAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// The id argument will be the Run ID; 
		// CursorAdapter gives us this for free. 
		Intent intent = new Intent(getActivity(), RunActivity.class);
		intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
		startActivity(intent);
	}

}
