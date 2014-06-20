package com.plined.liftlog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

public abstract class MultiFragmentActivity extends FragmentActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		processAlwaysOn();
	}
	
	protected abstract int getLayoutResId();
	
	public void setFragment(Fragment toDisplay) {
		FragmentManager fm = getSupportFragmentManager();
		
		//Replace the existing fragment
		fm.beginTransaction().replace(R.id.fragmentContainer, toDisplay).commit();
	}
	
	public void processAlwaysOn() {
		//Get the shared pref
		if (isKeepAwakeEnabled()) {
			//Get rootview
			View rootView = findViewById(android.R.id.content);
			
			//Set it to always be on
			rootView.setKeepScreenOn(true);
		}
	}
	
	/*
	 * Gets our preference for whether we're always awake.
	 */
	protected boolean isKeepAwakeEnabled() {		
		return getSharedPreferences(SettingsFragment.SETTINGS_PREF, Context.MODE_PRIVATE).getBoolean(SettingsFragment.SPREF_ALWAYSON_MODE, false);
	}
	
}
