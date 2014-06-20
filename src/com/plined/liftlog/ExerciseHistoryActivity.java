package com.plined.liftlog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ExerciseHistoryActivity extends MultiFragmentActivity {

	public static String EXERCISE_ID = "com.plined.liftlog.exercisehistoryactivity.exerciseid";
	public static String EXERCISE_INSTANCE_ID = "com.plined.liftlog.exercisehistoryactivity.exerciseinstanceid";
	
	public static String TAG = "ExerciseHistoryActivity";
	
	/*
	 * Keys for the fragment we're in.
	 */
	private static int VIEWMODE_DAILY = 1;
	private static int VIEWMODE_SORTABLE = 2;
	private static String SPREF_KEY_VIEWMODE = "com.plined.liftlog.exercisehistorydailyfragment.viewmode";
	
	
	protected Fragment createDailyFragment() {
		//Get the exercise Id from the intent
		int exId = getExerciseId();
		
		//Get the exercise instance ID from the intent
		int exInstId = getExerciseInstanceId();
		
		if (exInstId == -1 || exId == -1) {
			throw new RuntimeException("Fragment started with non-existent exercise instance ID or non-existent exercise ID. Ex inst ID is " + exInstId + " and ex ID is " + exId);
		}
		
		//Create our fragment
		return ExerciseHistoryDailyFragment.newInstance(exId, exInstId);
	}
	
	protected Fragment createSortableFragment() {
		//Get the exercise Id from the intent
		int exId = getExerciseId();
		
		//Make sure it's valid (at least minimally)
		if (exId == -1) {
			throw new RuntimeException("Fragment started with non-existent exercise ID.");
		}
		
		//Create our fragment
		return ExerciseHistorySortableFragment.newInstance(exId);
	}
	
	/*
	 * Returns the exercise ID from the intent.
	 */
	private int getExerciseId() {
		return getIntent().getExtras().getInt(EXERCISE_ID, -1);
	}
	
	/*
	 * Returns the exercise instance ID from the intent.
	 */
	private int getExerciseInstanceId() {
		return getIntent().getExtras().getInt(EXERCISE_INSTANCE_ID, -1);
	}
	
	@Override
	public int getLayoutResId() {
		return R.layout.a_exercise_history;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getViewModePref() == VIEWMODE_DAILY) {
			setFragment(createDailyFragment());
		} else {
			setFragment(createSortableFragment());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.f_exercise_history_daily, menu);
		return true;
	}
	
	/*
	 * Sets the sort menu item text (View Daily History) or (View Sortable History)
	 */
	protected void setSortMenuText(MenuItem item) {
		if (getViewModePref() == VIEWMODE_DAILY) {
			item.setTitle("View Sortable History");
		} else {
			item.setTitle("View Daily History");
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		setSortMenuText(menu.findItem(R.id.menu_item_sortmode));
		return true;
	}
	
	@Override
	@TargetApi(11)
	public boolean onOptionsItemSelected(MenuItem item) {			
		
		switch(item.getItemId()) {
		case R.id.menu_item_sortmode:
			//Sort mode has been changed.
			if (getViewModePref() == VIEWMODE_DAILY) {
				//Currently sorting by daily, swap to sortabler.
				setViewModePref(VIEWMODE_SORTABLE);
				setFragment(createSortableFragment());
			} else {
				//Currently sorting by sortable, swap to daily.
				setViewModePref(VIEWMODE_DAILY);
				setFragment(createDailyFragment());
			}
		
			/*
			 * For now this will only work on api 11+
			 */
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//	Invalid our menu
				invalidateOptionsMenu();
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Sets our list View mode value in the Shared Preferences.
	 */
	protected void setViewModePref(int viewMode) {
		getPreferences(Context.MODE_PRIVATE).edit().putInt(SPREF_KEY_VIEWMODE, viewMode).commit();
	}
	
	/*
	 * Gets our list View mode value from the shared preferences.
	 */
	protected int getViewModePref() {
		return getPreferences(Context.MODE_PRIVATE).getInt(SPREF_KEY_VIEWMODE, VIEWMODE_DAILY);
	}
	
	
}
