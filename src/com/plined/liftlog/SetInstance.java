package com.plined.liftlog;

import android.content.Context;
import android.util.Log;

public class SetInstance extends DBModelObject {
	
	int mId = -1;
	int mSetNum;
	int mNumReps;
	float mWeight;
	int mExerciseInstanceParent;
	
	public SetInstance(Context appContext, int id, int setNum, int numReps, float weight, int exerciseInstanceParent) {
		mAppContext = appContext;
		mId = id;
		mSetNum = setNum;
		mNumReps = numReps;
		mWeight = weight;
		mExerciseInstanceParent = exerciseInstanceParent;
	}
	
	public SetInstance(Context appContext, int setNum, int numReps, float weight, int exerciseInstanceParent) {
		mAppContext = appContext;
		mSetNum = setNum;
		mNumReps = numReps;
		mWeight = weight;
		mExerciseInstanceParent = exerciseInstanceParent;
	}

	public int getSetNum() {
		return mSetNum;
	}

	public void setSetNum(int setNum) {
		mSetNum = setNum;
	}

	public int getNumReps() {
		return mNumReps;
	}

	public void setNumReps(int numReps) {
		mNumReps = numReps;
	}

	public float getWeight() {
		return mWeight;
	}

	public void setWeight(float weight) {
		mWeight = weight;
	}

	public int getExerciseInstanceParent() {
		return mExerciseInstanceParent;
	}

	public void setExerciseInstanceParent(int exerciseInstanceParent) {
		mExerciseInstanceParent = exerciseInstanceParent;
	}

	public int getId() {
		return mId;
	}
	
	public void delete() {

		LiftLogDBAPI.get(mAppContext).delete(this);
	}
	
	protected void update() {

		LiftLogDBAPI.get(mAppContext).update(this);
	}
	
	public void insert() {

		LiftLogDBAPI.get(mAppContext).insert(this);
	}
	
}
