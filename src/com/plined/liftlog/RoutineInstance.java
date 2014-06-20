package com.plined.liftlog;

import java.util.Date;

import android.content.Context;
import android.util.Log;

public class RoutineInstance extends DBModelObject {
	
	int mId = -1;
	int mRoutineSuper;
	Date mDate;
	
	public RoutineInstance(Context appContext, int id, int routineSuper, Date date) {
		mAppContext = appContext;
		mId = id;
		mRoutineSuper = routineSuper;
		mDate = date;
	}
	
	public RoutineInstance(Context appContext, int routineSuper, Date date) {
		mAppContext = appContext;
		mRoutineSuper = routineSuper;
		mDate = date;
	}

	public int getRoutineSuper() {
		return mRoutineSuper;
	}

	public void setRoutineSuper(int routineSuper) {
		mRoutineSuper = routineSuper;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
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
