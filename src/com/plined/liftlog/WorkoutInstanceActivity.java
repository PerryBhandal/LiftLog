package com.plined.liftlog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class WorkoutInstanceActivity extends SingleFragmentActivity {

	private static String TAG = "WorkoutInstanceActivity";
	
	public static String EXTRA_ROUTINE_INSTANCE_ID = "com.perryb.liftlog.workoutinstanceactivity.extra_routine_id";
	
	public Fragment createFragment() {
		//Get our intent
		Intent rotIntent = getIntent();
		
		//Grab the key out
		int routineInstanceId = rotIntent.getIntExtra(EXTRA_ROUTINE_INSTANCE_ID, -1);
		
		if (routineInstanceId == -1) {
			throw new RuntimeException(TAG + " received a routine instance ID of -1 from its launching activity.");
		}
		
		return WorkoutInstanceFragment.newInstance(routineInstanceId);
	}
	
}
