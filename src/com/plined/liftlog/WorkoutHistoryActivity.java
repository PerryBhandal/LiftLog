package com.plined.liftlog;

import android.support.v4.app.Fragment;

public class WorkoutHistoryActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new WorkoutHistoryFragment();
	}

}
