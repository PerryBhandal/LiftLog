package com.plined.liftlog;

import android.support.v4.app.Fragment;

public class WeightSelectionActivity extends SingleFragmentActivity {

	public static final String EXTRA_SET_ID = "com.perryb.liftlog.weightselectionactivity.set_id";
	
	@Override
	public Fragment createFragment() 
	{
		int setId = getIntent().getIntExtra(EXTRA_SET_ID, -1);
		if (setId != -1) {
			return WeightSelectionFragment.newInstance(setId);
		} else {
			throw new RuntimeException("Attempting to load weight selection activity with no set instance ID.");
		}
	}
}
