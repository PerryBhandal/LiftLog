package com.plined.liftlog;

import java.util.Date;

import android.content.Context;
import android.util.Log;

public class Routine extends DBModelObject {
	
	private static String TAG = "Routine";
	
	int mId = -1;
	String mName;
	Date mLastUsed;
	boolean mActive;
	
	public Routine(Context appContext, int id, String name, Date lastUsed, boolean active) {
		mAppContext = appContext;
		mId = id;
		mName = name;
		mLastUsed = lastUsed;
		mActive = active;
	}

	public Routine(Context appContext, String name, Date lastUsed, boolean active) {
		mAppContext = appContext;
		mName = name;
		mLastUsed = lastUsed;
		mActive = active;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
		update();
	}

	public Date getLastUsed() {
		return mLastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		mLastUsed = lastUsed;
		update();
	}

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		mActive = active;
	}

	public int getId() {
		return mId;
	}
	
	public String toString() {
		String toRet = "";
		
		toRet += "--BEGIN ROUTINE STRING--\n";
		toRet += "ID: " + mId + "\n";
		toRet += "Name: " + mId + "\n";
		toRet += "Last used: " + mLastUsed.toString() + " \n";
		toRet += "Active: " + mActive + "\n";
		toRet += "--END ROUTINE STRING--\n";
		
		return toRet;
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

		
		//Make sure it's active.
		if (!isActive()) {
			throw new RuntimeException("Attempting update operation on an inactive routine.");
		}
		
		LiftLogDBAPI.get(mAppContext).update(this);
	}
	
	public void insert() {

		
		//Make sure it's active.
		if (!isActive()) {
			throw new RuntimeException("Attempting insert operation on an inactive routine.");
		}
		
		LiftLogDBAPI.get(mAppContext).insert(this);
	}
	
}
