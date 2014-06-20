package com.plined.liftlog;

import android.content.Context;
import android.util.Log;

public abstract class DBModelObject {

	protected static String TAG = "DBModelObject";
	Context mAppContext;
	
	protected void setAppContext(Context c) {
		mAppContext = c;
	}
	
	/*
	 * Ensures that our app has acontext defined.
	 * Throws an exception if we don't.
	 */
	protected void verifyContextExists() {
		if (mAppContext == null) {
			throw new RuntimeException("DBModelObject doesn't have application wide context defined.");
		}
	}
	
	protected LiftLogDBAPI getDBAPI() {
		verifyContextExists();
		return LiftLogDBAPI.get(mAppContext);
	}
	
	public abstract void insert();
	public abstract void delete();
	protected abstract void update();
	
	
}
