package com.plined.liftlog;

import android.content.Context;
import android.util.Log;

public class ExerciseInstance extends DBModelObject {
	
	int mId = -1;
	int mExerciseSuper;
	int mRoutineInstanceParent;
	String mComment;
	int mPosition;
	boolean mExpanded;
	int mRoutineExerciseParent;
	
	public ExerciseInstance(Context appContext, int id, int exerciseSuper, int routineInstanceParent, String comment, int position, boolean expanded, int routineExerciseParent) {
		mAppContext = appContext;
		mId = id;
		mExerciseSuper = exerciseSuper;
		mRoutineInstanceParent = routineInstanceParent;
		mComment = comment;
		mPosition = position;
		mExpanded = expanded;
		mRoutineExerciseParent = routineExerciseParent;
	}
	
	public ExerciseInstance(Context appContext, int exerciseSuper, int routineInstanceParent, String comment, int position, int routineExerciseParent) {
		mAppContext = appContext;
		mExerciseSuper = exerciseSuper;
		mRoutineInstanceParent = routineInstanceParent;
		mComment = comment;
		mPosition = position;
		mExpanded = false;
		mRoutineExerciseParent = routineExerciseParent;
	}

	public int getExerciseSuper() {
		return mExerciseSuper;
	}

	public void setExerciseSuper(int exerciseSuper) {
		mExerciseSuper = exerciseSuper;
	}

	public int getRoutineInstanceParent() {
		return mRoutineInstanceParent;
	}

	public void setRoutineInstanceParent(int routineInstanceParent) {
		mRoutineInstanceParent = routineInstanceParent;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
		update();
	}

	public int getId() {
		return mId;
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		mPosition = position;
	}
	
	public boolean isExpanded() {
		return mExpanded;
	}

	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
		update();
	}

	public int getRoutineExerciseParent() {
		return mRoutineExerciseParent;
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
