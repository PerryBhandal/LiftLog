package com.plined.liftlog;
import android.content.Context;
import android.util.Log;

public class Exercise extends DBModelObject {

	private static String TAG = "Exercise";
	
	int mId = -1;
	String mName;
	
	public Exercise(Context appContext, int id, String name) {
		mAppContext = appContext;
		mId = id;
		mName = name;
	}
	
	public Exercise(Context appContext, String name) {
		mAppContext = appContext;
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
		update();
	}

	public int getId() {
		return mId;
	}
	
	/*
	 * Deletes this object from the database.
	 */
	public void delete() {

		LiftLogDBAPI.get(mAppContext).delete(this);
	}
	
	/*
	 * Updates this Routine in the database.
	 */
	protected void update() {

		LiftLogDBAPI.get(mAppContext).update(this);
	}
	
	public void insert() {

		LiftLogDBAPI.get(mAppContext).insert(this);
	}
	
}
