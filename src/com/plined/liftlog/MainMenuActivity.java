package com.plined.liftlog;

import android.support.v4.app.Fragment;

public class MainMenuActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new MainMenuFragment();
	}

}
