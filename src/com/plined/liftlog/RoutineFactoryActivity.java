package com.plined.liftlog;

import android.support.v4.app.Fragment;

public class RoutineFactoryActivity extends SingleFragmentActivity { 

	public static final String EXTRA_ROUTINE_ID = "com.perryb.liftlog.routine_id";
	
	@Override
	public Fragment createFragment() 
	{
		int routineId = getIntent().getIntExtra(EXTRA_ROUTINE_ID, -1);
		if (routineId != -1) {
			return RoutineFactoryFragment.newInstance(routineId);
		} else {
			return new RoutineFactoryFragment();
		}
	}

}
