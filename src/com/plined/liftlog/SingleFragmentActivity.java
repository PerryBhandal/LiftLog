package com.plined.liftlog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;

public abstract class SingleFragmentActivity extends FragmentActivity {

	
	private static String TAG = "SingleFragmentActivity";
	protected abstract Fragment createFragment();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if (fragment == null) {
			fragment = createFragment();
			
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
		
		//Process our always on setting
		processAlwaysOn();
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
	
	protected int getLayoutResId() {
		return R.layout.activity_fragment;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		//Start analytics tracking
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//Stop analytics tracking.
		EasyTracker.getInstance().activityStop(this);
	}
	
}
