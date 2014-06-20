package com.plined.liftlog;

import android.support.v4.app.Fragment;

public class WorkoutBeginActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new WorkoutBeginFragment();
	}

}
