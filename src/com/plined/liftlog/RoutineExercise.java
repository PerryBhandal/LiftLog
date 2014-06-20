package com.plined.liftlog;

import android.content.Context;
import android.util.Log;

public class RoutineExercise extends DBModelObject {
	
	private static String TAG = "RoutineExercise";
	
	int mId = -1;
	int mExerciseSuper;
	int mRoutineParent;
	int mNumSets;
	int mPosition;
	String mInstruction;
	boolean mExpanded;

	//Bounds for set numbers.
	int mLowerSetBound = 1;
	int mUpperSetBound = 20;
	
	public RoutineExercise(Context appContext, int exerciseSuper, int routineParent, int numSets, int position, String instruction, boolean expanded) {
		mAppContext = appContext;
		mExerciseSuper = exerciseSuper;
		mRoutineParent = routineParent;
		mNumSets = numSets;
		mPosition = -1;
		mInstruction = instruction;
		mExpanded = expanded;
	}
	
	public RoutineExercise(Context appContext, int id, int exerciseSuper, int routineParent, int numSets, int position, String instruction, boolean expanded) {
		mAppContext = appContext;
		mId = id;
		mExerciseSuper = exerciseSuper;
		mRoutineParent = routineParent;
		mNumSets = numSets;
		mPosition = position;
		mInstruction = instruction;
		mExpanded = expanded;
	}

	/*
	 * Increments our set count by 1.
	 * Abides our set bounds.
	 */
	
	public void incrementSetCount() {
		if (mNumSets < mUpperSetBound) {
			mNumSets += 1;
		}
		
		update();
	}
	
	/*
	 * Increments our set count by 1.
	 * Abides our set bounds.
	 */
	
	public void decrementSetCount() {
		if (mNumSets > mLowerSetBound) {
			mNumSets -= 1;
		}
		
		update();
	}
	
	public int getExerciseSuper() {
		return mExerciseSuper;
	}

	public void setExerciseSuper(int exerciseSuper) {
		mExerciseSuper = exerciseSuper;
	}

	public int getRoutineParent() {
		return mRoutineParent;
	}

	public void setRoutineParent(int routineParent) {
		mRoutineParent = routineParent;
	}

	public int getNumSets() {
		return mNumSets;
	}

	public void setNumSets(int numSets) {
		mNumSets = numSets;
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		mPosition = position;
		update();
	}
	
	/*
	 * This position change doesn't force an update after.
	 * It's used when we have to initially set the item's position
	 * after creation.
	 */
	public void setPositionNoUpdate(int position) {
		mPosition = position;
	}
	
	public void incrementPosition() {
		if (canIncrement()) {
			//Decrement the one above us, and increment ourselves.
			getRoutineByPosition(mPosition+1).setPosition(mPosition);
			mPosition += 1;
			update();
		}
	}
	
	public void decrementPosition() {
		if (canDecrement()) {
			//Increment the one below us, and decrement ourselves.
			getRoutineByPosition(mPosition-1).setPosition(mPosition);
			mPosition -=1;
			update();
			
		}
	}
	
	/*
	 * Wraps the getRotExByPosition function
	 * in LiftLogDBAPI. Throws a runtime exception
	 * if the routine we ask for can't be found.
	 */
	public RoutineExercise getRoutineByPosition(int position) {
		RoutineExercise toRet = getDBAPI().getRotExByPos(mRoutineParent, position);
		
		if (toRet == null) {
			throw new RuntimeException("Could not find routine exercise in position number " + position);
		}
		
		return toRet;
	}

	public String getInstruction() {
		return mInstruction;
	}

	public void setInstruction(String instruction) {
		mInstruction = instruction;
		update();
	}

	public int getId() {
		return mId;
	}
	
	public boolean isExpanded() {
		return mExpanded;
	}

	/*
	 * Used to check whether we can decrement our position.
	 */
	public boolean canDecrement() {
		if (mPosition > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Used to check whether we can increment our position.
	 */
	public boolean canIncrement() {
		if (mPosition <= 0) {
			//Our position should never be equal to <= 0 when this is run.
			throw new RuntimeException("Checking whether routine exercise can increment, but its position is <= 0");
		}
		
		if ((getDBAPI().getMaxRotExPosition(mRoutineParent)) > mPosition) {
			//Our item isn't the highest in the list.
			return true; 
		} else {
			//Our item is highest in the list.
			return false;
		}
	}
	
	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
		update();
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
