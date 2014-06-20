package com.plined.liftlog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.util.Log;

import com.plined.liftlog.LiftLogDBMaster.ExerciseCursor;
import com.plined.liftlog.LiftLogDBMaster.ExerciseInstanceCursor;
import com.plined.liftlog.LiftLogDBMaster.RoutineCursor;
import com.plined.liftlog.LiftLogDBMaster.RoutineExerciseCursor;
import com.plined.liftlog.LiftLogDBMaster.RoutineInstanceCursor;
import com.plined.liftlog.LiftLogDBMaster.SetInstanceCursor;

public class LiftLogDBAPI {

	static String TAG = "LiftLogDBAPI";
	LiftLogDBMaster mDBMaster;
	Context mAppContext;
	
	/*
	 * Sort modes used when getting a routine cursor.
	 */
	public static int ROUTINE_SORT_ALPHA = 1;
	public static int ROUTINE_SORT_LAST_USED = 2;
	
	//Static reference to our object for singleton pattern.
	static LiftLogDBAPI sLiftLogDBAPI;
	
	public LiftLogDBAPI(Context appContext) {
		mAppContext = appContext;
		
		mDBMaster = new LiftLogDBMaster(mAppContext);
	}
	
	
	/*
	 * Returns true if the exercise name is in use, false if it isn't.
	 */
	public boolean isExNameUsed(String exerciseName, boolean caseSensitive) {
		//Get our cursor
		ExerciseCursor exCursor;
		if (caseSensitive) {
			exCursor = mDBMaster.queryExercise(null, "name = ?", new String[] { exerciseName }, null, null, null);
		} else {
			exCursor = mDBMaster.queryExercise(null, "name = ? COLLATE NOCASE", new String[] { exerciseName }, null, null, null);
		}
		
		if (exCursor.getCount() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/*
	 * Finds the date the particular set instance occurred on by using the setinstanme 
	 */
	public Date getSetDate(int setInstanceId) {
		Cursor ret = mDBMaster.tableQuery(LiftLogDBMaster.VIEW_SET_INSTANCE_TIMESTAMP, new String[] {"date"}, "_id = ?", new String[] {String.valueOf(setInstanceId)}, null, null, null);
		
		ret.moveToFirst();
		
		long retDate = ret.getLong(ret.getColumnIndex("date"));
		return new Date(retDate);
	}
	
	/*
	 * Returns the number of active routines.
	 */
	public int getActiveRoutineCount() {
		return getAllRoutines(true).size();
	}
	
	/*
	 * Returns the number of active routines.
	 */
	public int getWorkoutCount() {
		return getAllRoutineInstancesCursor(null).getCount();
	}
	
	/*
	 * Singleton get.
	 */
	public static LiftLogDBAPI get(Context c) {
		//Implements singleton pattern.
		
		//check if an instance already exists
		if (sLiftLogDBAPI == null) {
			sLiftLogDBAPI = new LiftLogDBAPI(c.getApplicationContext());
		}
		
		return sLiftLogDBAPI;
	}
	
	/*
	 * Resets the DB master (forces a new LiftLogDBAPI instance to be created
	 * on the next request).
	 */
	public void resetDBMaster() {
		sLiftLogDBAPI = null;
	}

	
	/*
	 * Returns an ArrayList containing all Routines.
	 * activeOnly determines whether we'll only return routines
	 * marked as active.
	 */
	public ArrayList<Routine> getAllRoutines(boolean activeOnly) {
		//Run the query and get our cursor
		RoutineCursor cursor;
		if (activeOnly) {
			//Only return actives.
			cursor = mDBMaster.queryRoutine(null, "active = ?", new String[] { String.valueOf(LiftLogDBMaster.boolToInt(activeOnly)) } , null, null, null);
		} else {
			//Return all.
			cursor = mDBMaster.queryRoutine(null, null, null , null, null, null);
		}
		
		ArrayList<Routine> toRet = new ArrayList<Routine>();
		
		//Grab routines out.
		while (!cursor.isAfterLast()) {
			toRet.add(cursor.getRoutine());
		}
		
		return toRet;
	}
	
	/*
	 * Returns an ArrayList containing all Routines.
	 * activeOnly determines whether we'll only return routines
	 * marked as active.
	 */
	public ArrayList<SetInstance> getSetInstances(int exerciseInstanceId) {
		//Run the query and get our cursor
		SetInstanceCursor cursor = mDBMaster.querySetInstance(null, LiftLogDBMaster.COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT + " = ?", new String[] { String.valueOf(exerciseInstanceId) } , null, null, null);

		
		ArrayList<SetInstance> toRet = new ArrayList<SetInstance>();
		
		//Grab set instances out
		while (!cursor.isAfterLast()) {
			toRet.add(cursor.getSetInstance());
		}
		
		return toRet;
	}
	
	
	/*
	 * Returns the number of set instances tied to a given exercise instance.
	 */
	public int getSetCount(ExerciseInstance exInt) {
		String whereClause = String.format("%s = ?", LiftLogDBMaster.COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT);
		SetInstanceCursor setInstances = mDBMaster.querySetInstance(null, whereClause, new String[] { String.valueOf(exInt.getId()) }, null, null, null);
		return setInstances.getCount();
	}
	
	/*
	 * Returns a RoutineInstance object based on a provided
	 * routine super, and its creation time.
	 */
	public RoutineInstance getRoutineInstance(int rotSuper, Date creationTime) {
		//Create our where clause.
		String where = String.format("%s = ? AND %s = ?", LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE);
		
		//Create our args clause
		String[] args = new String[] { String.valueOf(rotSuper), String.valueOf(LiftLogDBMaster.dateToLong(creationTime))};
		
		//do oru qeury and get our cursor back
		RoutineInstanceCursor rotCursor = mDBMaster.queryRoutineInstance(null, where, args, null, null, null);
		
		//Return the first instance
		return rotCursor.getRoutineInstance();
	}
	
	/*
	 * Returns all routine instances for a given routine super.
	 */
	public ArrayList<RoutineInstance> getRoutineInstanceBySuper(int rotSuper) {
		//Create our where clause.
		String where = String.format("%s = ?", LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER);
		
		//Create our args clause
		String[] args = new String[] { String.valueOf(rotSuper) };
		
		//Order it by date descending
		String orderBy = LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE + " DESC";
		
		//do oru qeury and get our cursor back
		RoutineInstanceCursor rotCursor = mDBMaster.queryRoutineInstance(null, where, args, null, null, orderBy);
		
		ArrayList<RoutineInstance> toRet = new ArrayList<RoutineInstance>();
		
		while (!rotCursor.isAfterLast()) {
			toRet.add(rotCursor.getRoutineInstance());
		}
		
		return toRet;
	}
	
	/*
	 * Returns an ExerciseInstance object based on a provided
	 * routineinstance parent, and the exercise it is a child of.
	 */
	public ExerciseInstance getExerciseInstance(int rotInstanceParentId, int exerciseSuperId) {
		//Create our where clause.
		String where = String.format("%s = ? AND %s = ?", LiftLogDBMaster.COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT, LiftLogDBMaster.COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER);
		
		//Create our args clause
		String[] args = new String[] { String.valueOf(rotInstanceParentId), String.valueOf(exerciseSuperId)};
		
		//do oru qeury and get our cursor back
		ExerciseInstanceCursor exCursor = mDBMaster.queryExerciseInstance(null, where, args, null, null, null);
		
		//Return the first instance
		return exCursor.getExerciseInstance();
	}
	
	/*
	 * Returns the set instance for rep setToRetrieve from the provided
	 * exercise instance,
	 */
	public SetInstance getSetFromExInst(int setToRetrieve, ExerciseInstance sourceExInt) {
		String whereClause = String.format("%s = ? AND %s = ?", LiftLogDBMaster.COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT, LiftLogDBMaster.COLUMN_SET_INSTANCE_SET_NUM);
		String[] whereArgs = new String[] { String.valueOf(sourceExInt.getId()), String.valueOf(setToRetrieve)};
		return mDBMaster.querySetInstance(null, whereClause, whereArgs, null, null, null).getSetInstance();
	}
	
	/*
	 * Returns a SetInstance based on its id.
	 */
	public SetInstance getSetInstanceById(int setId) {
		return mDBMaster.querySetInstance(null, "_id = ?", new String[] { String.valueOf(setId) }, null, null, null).getSetInstance();
	}
	
	/*
	 * Returns an ExerciseInstance based on its id.
	 */
	public ExerciseInstance getExerciseInstanceById(int exIntId) {
		return mDBMaster.queryExerciseInstance(null, "_id = ?", new String[] { String.valueOf(exIntId) }, null, null, null).getExerciseInstance();
	}
	
	/*
	 * Returns the routine instance with the highest id. Returns null
	 * if no routine instances exist.
	 */
	public RoutineInstance getRecentRotInst() {
		//Get our routine instances cursor sorted with highest IDs first
		RoutineInstanceCursor rotInstCursor = getAllRoutineInstancesCursor(LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ID + " DESC");
		
		//If our routine instance cursor is empty
		if (rotInstCursor.getCount() == 0) {
			//There are no routine instances. Return null.
			return null;
		}
		//Else
		else {
			//Get the first routine instance
			RoutineInstance toRet = rotInstCursor.getRoutineInstance();
		
			//Return it
			return toRet;
		}
	}
	
	/*
	 * Returns the Routine that a RoutineInstance is based on.
	 */
	public Routine getParentRoutine(RoutineInstance rotInstance) {
		//Grab the routine instance parent ID
		int parentId = rotInstance.getRoutineSuper();
		
		//Query for it
		return getRoutineById(parentId);
	}
	
	
	/*
	 * Grabs all set instances for the provided exercise ID. Does this on the
	 * set instance timestamp view, as we need the date potentially.
	 */
	public SetInstanceCursor getSetInstancesCursorForExercise(int exerciseId, String orderBy) {
		return mDBMaster.querySetInstanceTimeStamp(null, "(num_reps != -1 OR weight != -1) AND exercise_super = ?", new String[] {String.valueOf(exerciseId) }, null, null, orderBy);
	}
	
	/*
	 * Returns a cursor containing all Routines.
	 * activeOnly determines whether we'll only return routines
	 * marked as active.
	 */
	public RoutineCursor getAllRoutinesCursor(boolean activeOnly, int sortMode) {
		
		//Create our orderby string
		String orderBy = null;
		
		if (sortMode == ROUTINE_SORT_ALPHA) {
			orderBy = LiftLogDBMaster.COLUMN_ROUTINE_NAME + " Collate NOCASE";
		} else if (sortMode == ROUTINE_SORT_LAST_USED) {
			orderBy = LiftLogDBMaster.COLUMN_ROUTINE_LAST_USED + " DESC";
		} else {
			throw new RuntimeException("Encountered invalid sort mode: " +  sortMode);
		}
		
		//Create our where clause
		String whereClause = null;
		String[] whereArgs = null;
		
		if (activeOnly) {
			whereClause = "active = ?";
			whereArgs = new String[] { String.valueOf(LiftLogDBMaster.boolToInt(activeOnly)) };
		}
		
		
		//Do our search
		return mDBMaster.queryRoutine(null, whereClause, whereArgs , null, null, orderBy);
	}
	
	/*
	 * Returns a cursor containing all Routine Instances.
	 */
	public RoutineInstanceCursor getAllRoutineInstancesCursor(String orderBy) {
		
		//Do our search
		return mDBMaster.queryRoutineInstance(null, null, null , null, null, orderBy);
	}
	
	/*
	 * Returns an ArrayList containing all exercises.
	 */
	public ArrayList<Exercise> getAllExercises() {
		//Run the query and get our cursor
		ExerciseCursor cursor = mDBMaster.queryExercise(null, null, null, null, null, null);
		
		ArrayList<Exercise> toRet = new ArrayList<Exercise>();
		
		//Grab routines out.
		while (!cursor.isAfterLast()) {
			toRet.add(cursor.getExercise());
		}
		
		return toRet;
	}
	
	/*
	 * Returns an Array containing exercise names as Strings.
	 */
	public String[] getAllExercisesNames() {
		//Get our exercise objects
		ArrayList<Exercise> exerciseObjects = getAllExercises();
		
		ArrayList<String> exerciseNames = new ArrayList<String>();
		
		for (Exercise ex : exerciseObjects) {
			exerciseNames.add(ex.getName());
		}
		
		//Create the string we'll return
		String[] toRet = new String[exerciseNames.size()];
		toRet = exerciseNames.toArray(toRet);
		
		return toRet;
	
	}
	
	/*
	 * Wraps the insert routine method in LiftLogDBMaster.
	 */
	public long insert(Routine toInsert) {
		return mDBMaster.insertRoutine(toInsert);
	}
	
	/*
	 * Wraps the insert routineinstance method in LiftLogDBMaster.
	 */
	public long insert(RoutineInstance toInsert) {
		return mDBMaster.insertRoutineInstance(toInsert);
	}
	
	/*
	 * Wraps the insert exerciseinstance method in LiftLogDBMaster.
	 */
	public long insert(ExerciseInstance toInsert) {
		return mDBMaster.insertExerciseInstance(toInsert);
	}
	
	/*
	 * Wraps the insert SetInstance method in LiftLogDBMaster.
	 */
	public long insert(SetInstance toInsert) {
		return mDBMaster.insertSetInstance(toInsert);
	}
	
	/*
	 * Wraps the insert exercise method in LiftLogDBMaster.
	 */
	public long insert(Exercise toInsert) {
		return mDBMaster.insertExercise(toInsert);
	}
	
	/*
	 * Wraps the insert RoutineExercise method in LiftLogDBMaster.
	 */
	public long insert(RoutineExercise toInsert) {
		//Get its position
		toInsert.setPositionNoUpdate(getNextPosition(toInsert.getRoutineParent()));
		
		return mDBMaster.insertRoutineExercise(toInsert);
	}

	
	/*
	 * Grabs a single routine by its ID
	 */
	public Routine getRoutineById(int rotId) {
		
		//Get our cursor
		RoutineCursor rotCursor = mDBMaster.queryRoutine(null, "_id = ?", new String[] { String.valueOf(rotId) }, null, null, null);
		
		//Get the routine out
		Routine toRet = rotCursor.getRoutine();
		
		//Make sure it's non-null
		if (toRet == null) {
			throw new RuntimeException("Grabbed routine by ID, but result was null. ID is " + rotId);
		}

		return toRet;		
	}
	
	/*
	 * Grabs a single routineinstance by its ID
	 */
	public RoutineInstance getRoutineInstanceById(int rotId) {
		
		//Get our cursor
		RoutineInstanceCursor rotCursor = mDBMaster.queryRoutineInstance(null, "_id = ?", new String[] { String.valueOf(rotId) }, null, null, null);
		
		//Get the routine out
		RoutineInstance toRet = rotCursor.getRoutineInstance();
		
		//Make sure it's non-null
		if (toRet == null) {
			throw new RuntimeException("Grabbed RoutineInstance by ID, but result was null. ID is " + rotId);
		}

		return toRet;
	}
	
	/*
	 * Grabs all routines with a given name.
	 * Does NOT error if a routine isn't foudn with the name. It will return null in that case.
	 */
	public Routine getRoutineByName(String routineName, boolean activeOnly, boolean caseSensitive) {
		
		//Create our case clause
		String caseClause = "";
		
		if (!caseSensitive) {
			//Ensures our search is case-insensitive.
			caseClause = "COLLATE NOCASE ";
		}
		
		//Get our cursor
		RoutineCursor rotCursor;
		if (activeOnly) {
			//Grab only active ones
			rotCursor = mDBMaster.queryRoutine(null, "name = ? " + caseClause + "AND active = ?", new String[] { routineName, String.valueOf(LiftLogDBMaster.boolToInt(activeOnly)) }, null, null, null);
		} else {
			//Grab active and inactive
			rotCursor = mDBMaster.queryRoutine(null, "name = ?" + caseClause, new String[] { routineName }, null, null, null);
		}
		
		//Get the routine out
		Routine toRet = rotCursor.getRoutine();
		
		return toRet;
		
	}
	
	/*
	 * Grabs the exercise that a routine exercsie points to.
	 */
	public Exercise getRotExSuperEx(RoutineExercise toCheck) {
		ExerciseCursor cursor = mDBMaster.queryExercise(null, "_id = ?", new String[] { String.valueOf(toCheck.getExerciseSuper())}, null, null, null);
		
		return cursor.getExercise();
	}
	
	/*
	 * Grabs the exercise that a exercise instance points to.
	 */
	public Exercise getExInstSuperEx(ExerciseInstance toCheck) {
		ExerciseCursor cursor = mDBMaster.queryExercise(null, "_id = ?", new String[] { String.valueOf(toCheck.getExerciseSuper())}, null, null, null);
		
		return cursor.getExercise();
	}
	
	/*
	 * Get the first available position in our list.
	 */
	public int getNextPosition(int routineId) {
		int maxPos = 0;
		
		//Get all of the routine exercises
		ArrayList<RoutineExercise> rotExArray = getRoutineExercises(routineId);
		
		for (RoutineExercise r : rotExArray) {

			if (r.getPosition() > maxPos) {
				maxPos = r.getPosition();
			}
		}
		
		return maxPos+1;
	}
	

	
	/*
	 * Returns a cursor pointing to the routine exercises for a routine
	 */
	public RoutineExerciseCursor getRoutineExercisesCursor(int routineId) {
		//Get our cursor
		return mDBMaster.queryRoutineExercise(null, "routine_parent = ?", new String[] { String.valueOf (routineId) }, null, null, LiftLogDBMaster.COLUMN_ROUTINE_EXERCISE_POSITION + " ASC");
	}
	
	/*
	 * Returns a cursor pointing to the exercise instances for a given routine instance.
	 */
	public ExerciseInstanceCursor getExerciseInstancesCursor(int routineInstanceId) {
		//Get our cursor
		return mDBMaster.queryExerciseInstance(null, "routine_instance_parent = ?", new String[] { String.valueOf (routineInstanceId) }, null, null, LiftLogDBMaster.COLUMN_ROUTINE_EXERCISE_POSITION + " ASC");
	}
	
	/*
	 * Returns a cursor pointing to the exercise instances for a given exercise
	 */
	public ExerciseInstanceCursor getExerciseInstancesCursorFromSup(int exerciseId, int exerciseInstanceId) {
		//Get our cursor
		return mDBMaster.queryExerciseInstanceTimeStamp(null, LiftLogDBMaster.COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER + " = ? AND " + LiftLogDBMaster.COLUMN_EXERCISE_INSTANCE_ID + " != ?", new String[] { String.valueOf (exerciseId), String.valueOf(exerciseInstanceId) }, null, null, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE + " DESC");
	}
	
	/*
	 * Updates a routine object in our database.
	 */
	public void update(Routine toUpdate) {
		//Update the routine.
		mDBMaster.updateRoutine(toUpdate);
	}
	
	/*
	 * Updates a routineexercise in our database.
	 */
	public long update(RoutineExercise toUpdate) {
		//Update the routine exercise
		return mDBMaster.updateRoutineExercise(toUpdate);
	}
	
	/*
	 * Updates a exercise in our database.
	 */
	public long update(Exercise toUpdate) {
		//Update the routine exercise
		return mDBMaster.updateExercise(toUpdate);
	}
	
	/*
	 * Updates a routineinstance in our database.
	 */
	public long update(RoutineInstance toUpdate) {
		//Update the routine exercise
		return mDBMaster.updateRoutineInstance(toUpdate);
	}
	
	/*
	 * Updates a ExerciseInstance in our database.
	 */
	public long update(ExerciseInstance toUpdate) {
		//Update the routine exercise
		return mDBMaster.updateExerciseInstance(toUpdate);
	}
	
	/*
	 * Updates a SetInstance in our database.
	 */
	public long update(SetInstance toUpdate) {
		//Update the routine exercise
		return mDBMaster.updateSetInstance(toUpdate);
	}
	
	/*
	 * Deletes this routine exercise instance from teh database.
	 */
	public int delete(RoutineExercise toDelete) {
		int rowsAffected = mDBMaster.deleteRoutineExercise(toDelete);

		updatePositions(toDelete.getRoutineParent());
		
		return rowsAffected;
	}
	
	/*
	 * Sets a routine to inactive. We never deltte existing routines.
	 */
	public long delete(Routine toDelete) {
		//Set it to inactive
		toDelete.setActive(false);
		
		//Update it.
		return mDBMaster.updateRoutine(toDelete);
	}
	
	/*
	 * Deletes this routine from teh database.
	 */
	public int delete(Exercise toDelete) {
		int rowsAffected = mDBMaster.deleteExercise(toDelete);
		
		return rowsAffected;
	}
	
	/*
	 * Deletes this routineinstance from teh database.
	 */
	public long delete(RoutineInstance toDelete) {
		int rowsAffected = mDBMaster.deleteRoutineInstance(toDelete);
		
		return rowsAffected;
	}
	
	/*
	 * Deletes this exerciseinstance from teh database.
	 */
	public long delete(ExerciseInstance toDelete) {
		int rowsAffected = mDBMaster.deleteExerciseInstance(toDelete);
		
		return rowsAffected;
	}
	
	/*
	 * Deletes this SetInstance from teh database.
	 */
	public long delete(SetInstance toDelete) {
		int rowsAffected = mDBMaster.deleteSetInstance(toDelete);
		
		return rowsAffected;
	}
	
	/*
	 * Returns an array containing all of the routine exercises for a routine.
	 */
	public ArrayList<RoutineExercise> getRoutineExercises(int routineId) {
		//Get our cursor
		RoutineExerciseCursor cursor = mDBMaster.queryRoutineExercise(null, "routine_parent = ?", new String[] { String.valueOf (routineId) }, null, null, LiftLogDBMaster.COLUMN_ROUTINE_EXERCISE_POSITION + " ASC");
		
		//Add all of the elements to an array
		ArrayList<RoutineExercise> toRet = new ArrayList<RoutineExercise>();
		
		while (!cursor.isAfterLast()) {
			toRet.add(cursor.getRoutineExercise());
		}
		
		//Return the array
		return toRet;
	}
	
	/*
	 * Returns an array containing all of the exercise instances for a routine instance.
	 */
	public ArrayList<ExerciseInstance> getExerciseInstances(int routineInstanceId) {
		//Get our cursor
		String whereClause = String.format("%s = ?", LiftLogDBMaster.COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT);
		ExerciseInstanceCursor cursor = mDBMaster.queryExerciseInstance(null, whereClause, new String[] { String.valueOf (routineInstanceId) }, null, null, null);
		
		//Add all of the elements to an array
		ArrayList<ExerciseInstance> toRet = new ArrayList<ExerciseInstance>();
		
		while (!cursor.isAfterLast()) {
			toRet.add(cursor.getExerciseInstance());
		}
		
		//Return the array
		return toRet;
	}
	
	public int getMaxRotExPosition(int routineId) {
		ArrayList<RoutineExercise> rotExArray = getRoutineExercises(routineId);
		
		int maxPosition = 0;
		
		for (RoutineExercise r : rotExArray) {
			if (r.getPosition() > maxPosition) {
				maxPosition = r.getPosition();
			}
		}
		
		if (maxPosition != rotExArray.size()) {
			throw new RuntimeException("Max position found for a routine's exercises isn't equal to size. Is there a gap between positions?");
		}
		
		return maxPosition;
	}
	
	/*
	 * Copies all routine exercises from srcRoutineId to destRoutineId.
	 * Makes new instance of each routine exercise, so they're
	 * independent from there on out.
	 */
	public void duplicateRoutineExercises(int srcRoutineId, int desRoutineId) {
		//Get our source routine exercises
		ArrayList<RoutineExercise> sourceRotEx = getRoutineExercises(srcRoutineId);
		
		//Iterate over each exercise and copy it
		for (RoutineExercise rotEx : sourceRotEx) {
			//Get out all of its important values
			int exerciseSuper = rotEx.getExerciseSuper();
			int numSets = rotEx.getNumSets();
			int position = rotEx.getPosition();
			boolean expanded = rotEx.isExpanded();
			String instruction = rotEx.getInstruction();
			
			//Create a new routine exercise, use our destiantion
			//routine ID as the routine parent.
			RoutineExercise toCommit = new RoutineExercise(mAppContext, exerciseSuper, desRoutineId, numSets, position, instruction, expanded);
			
			//Insert the new routine exercise
			toCommit.insert();
		}
	}
	
	/*
	 * Returns the routine exercise at a given position.
	 */
	public RoutineExercise getRotExByPos(int routineId, int position) {
		ArrayList<RoutineExercise> rotExArray = getRoutineExercises(routineId);
		
		for (RoutineExercise r : rotExArray) {
			if (r.getPosition() == position) {
				return r;
			}
		}
		
		
		//Nothing found.
		return null;
	}
	
	/*
	 * Reorders the positions for all routine exercise's
	 * belonging to routineId.
	 */
	private void updatePositions(int routineId) {
		//Get all routine exercises.
		ArrayList<RoutineExercise> rotArray = getRoutineExercises(routineId);
		
		int curPosition = 1;
		
		for (RoutineExercise r : rotArray) {
			if (curPosition < r.getPosition()) {
				r.setPosition(curPosition);
			}
			curPosition += 1;
		}
	}
	
	public Exercise getExerciseById(int exerciseId) {
		ExerciseCursor cursor = mDBMaster.queryExercise(null, "_id = ?", new String[] { String.valueOf(exerciseId) }, null, null, null);
		
		return cursor.getExercise();
	}
	
	public RoutineExercise getRoutineExerciseById(int routineExerciseId) {
		RoutineExerciseCursor cursor = mDBMaster.queryRoutineExercise(null, "_id = ?", new String[] { String.valueOf(routineExerciseId) }, null, null, null);
		
		return cursor.getRoutineExercise();
	}
	
	/*
	 * Returns exercises with the name exerciseName. If caseSensitive is true,
	 * the search is case-sensitive; if false, the search is case insensitive.
	 */
	public Exercise getExerciseByName(String exerciseName, boolean caseSensitive) {
		//Get our cursor
		ExerciseCursor exCursor;
		if (caseSensitive) {
			exCursor = mDBMaster.queryExercise(null, "name = ?", new String[] { exerciseName }, null, null, null);
		} else {
			exCursor = mDBMaster.queryExercise(null, "name = ? COLLATE NOCASE", new String[] { exerciseName }, null, null, null);
		}
		
		//Get the exercise out
		Exercise toRet = exCursor.getExercise();
		
		return toRet;
	}
	
	public static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
	    File dbFile=context.getDatabasePath(dbName);
	    return dbFile.exists();
	}
	
}
