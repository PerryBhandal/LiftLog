package com.plined.liftlog;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LiftLogDBMaster extends SQLiteOpenHelper {

	private static final String TAG = "LiftLogDBMaster";
	
	private Context mAppContext;

	public static final String DB_NAME = "liftlog.sqlite";
	private static final int VERSION = 2;
	
	//Routine table constants
	public static final String TABLE_ROUTINE = "routine";
	public static final String COLUMN_ROUTINE_ID = "_id";
	public static final String COLUMN_ROUTINE_NAME = "name";
	public static final String COLUMN_ROUTINE_LAST_USED = "last_used";
	public static final String COLUMN_ROUTINE_ACTIVE = "active";
	
	//RoutineExercise table constants
	public static final String TABLE_ROUTINE_EXERCISE = "routine_exercise";
	public static final String COLUMN_ROUTINE_EXERCISE_ID = "_id";
	public static final String COLUMN_ROUTINE_EXERCISE_EXERCISE_SUPER = "exercise_super";
	public static final String COLUMN_ROUTINE_EXERCISE_ROUTINE_PARENT = "routine_parent";
	public static final String COLUMN_ROUTINE_EXERCISE_NUM_SETS = "num_sets";
	public static final String COLUMN_ROUTINE_EXERCISE_POSITION = "position";
	public static final String COLUMN_ROUTINE_EXERCISE_INSTRUCTION = "instruction";
	public static final String COLUMN_ROUTINE_EXERCISE_EXPANDED = "expanded";
	
	//Exercise table constants
	public static final String TABLE_EXERCISE = "exercise";
	public static final String COLUMN_EXERCISE_ID = "_id";
	public static final String COLUMN_EXERCISE_NAME = "name";
	
	//RoutineInstance table constants
	public static final String TABLE_ROUTINE_INSTANCE = "routine_instance";
	public static final String COLUMN_ROUTINE_INSTANCE_ID = "_id";
	public static final String COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER = "routine_super";
	public static final String COLUMN_ROUTINE_INSTANCE_DATE = "date";
	
	//ExerciseInstance table constants
	public static final String TABLE_EXERCISE_INSTANCE = "exercise_instance";
	public static final String COLUMN_EXERCISE_INSTANCE_ID = "_id";
	public static final String COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER = "exercise_super";
	public static final String COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT = "routine_instance_parent";
	public static final String COLUMN_EXERCISE_INSTANCE_COMMENT = "comment";
	public static final String COLUMN_EXERCISE_INSTANCE_POSITION = "position";
	public static final String COLUMN_EXERCISE_INSTANCE_EXPANDED = "expanded";
	public static final String COLUMN_EXERCISE_INSTANCE_ROUTINE_EXERCISE_PARENT = "routine_exercise_parent";
	
	//SetInstance table constants
	public static final String TABLE_SET_INSTANCE = "set_instance";
	public static final String COLUMN_SET_INSTANCE_ID = "_id";
	public static final String COLUMN_SET_INSTANCE_SET_NUM = "set_num";
	public static final String COLUMN_SET_INSTANCE_NUM_REPS = "num_reps";
	public static final String COLUMN_SET_INSTANCE_WEIGHT = "weight";
	public static final String COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT = "exercise_instance_parent";
	
	//set_instance_timestamp constants
	public static final String VIEW_SET_INSTANCE_TIMESTAMP = "set_instance_timestamp";
	
	//exercise_instance_timestamp constants
	public static final String VIEW_EXERCISE_INSTANCE_TIMESTAMP = "exercise_instance_timestamp";
	
	public LiftLogDBMaster(Context context) {
		super(context, DB_NAME, null, VERSION);
		mAppContext = context;
		runTests();
	}
	
	public void runTests() {

	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {

		executeTableCreation(db);

		executeViewCreation(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		//Used to store the current version we've upgraded to.
		int tempVersion = oldVersion;
		
		//Upgrade from db 1 to 2.
		if (tempVersion == 1) {
			//Create our exercise_instance timestamp table
			db.execSQL(getExerciseInstanceTimeViewString());
			
			//Upgrade our tempversion
			tempVersion = 2;
		}
		
		if (tempVersion != newVersion) {
			throw new RuntimeException("Database upgrade failed. Old version is " + oldVersion + " new version is " + newVersion + " and temp version is " + tempVersion);
		}
		
	}
	
	/*
	 * Executes the creation of all of our tables.
	 */
	private void executeTableCreation(SQLiteDatabase db) {
		db.execSQL(getRoutineCreateString());
		db.execSQL(getRoutineExerciseCreateString());
		db.execSQL(getExerciseCreateString());
		db.execSQL(getRoutineInstanceCreateString());
		db.execSQL(getExerciseInstanceCreateString());
		db.execSQL(getSetInstanceCreateString());
	}
	
	/*
	 * Executes the creation of all of our views.
	 */
	private void executeViewCreation(SQLiteDatabase db) {
		execSQL(db, getSetInstanceTimeViewString());
		execSQL(db, getExerciseInstanceTimeViewString());
	}
	
	/*
	 * Runs the db creation string in creationString and catches any exceptions.
	 */
	private void execSQL(SQLiteDatabase db, String creationString) {
		try {
			db.execSQL(creationString);
		} catch (SQLiteException e) {

			throw new RuntimeException("Error during DB execution: " + e.getMessage());
		}
	}
	
	/*
	 * Returns the SQL create string to create our time stamped view table for set instances.
	 */
	private String getSetInstanceTimeViewString() {
		return "CREATE view IF NOT EXISTS set_instance_timestamp AS " +
				"SELECT set_instance._id, set_instance.set_num, set_instance.num_reps, set_instance.weight, set_instance.exercise_instance_parent, routine_instance.date, exercise_instance.exercise_super " +
				"FROM exercise_instance, routine_instance, set_instance " +
				"WHERE exercise_instance.routine_instance_parent = routine_instance._id and set_instance.exercise_instance_parent = exercise_instance._id";
	}
	
	/*
	 * Returns the SQL create string to create our time stamped view table for exercise instances.
	 */
	private String getExerciseInstanceTimeViewString() {
		String stringBuffer = "";
		
		//Add the header
		stringBuffer += String.format("CREATE view IF NOT EXISTS %s AS ", 
				VIEW_EXERCISE_INSTANCE_TIMESTAMP);
		
		//Add the select clause. KEEP THE SPACE AT THE END.
		stringBuffer += String.format("SELECT %s.%s, %s.%s, %s.%s, %s.%s, %s.%s, %s.%s, %s.%s, %s.%s ", 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_ID, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_COMMENT, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_POSITION, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_EXPANDED, 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_ROUTINE_EXERCISE_PARENT, 
				TABLE_ROUTINE_INSTANCE, 
				COLUMN_ROUTINE_INSTANCE_DATE);
		
		//Add the from clause. KEEP THE SPACE AT THE END.
		stringBuffer += String.format("FROM %s, %s ", 
				TABLE_EXERCISE_INSTANCE, 
				TABLE_ROUTINE_INSTANCE);
		
		//Add the where clause. No space at end.
		//We're making sure that our exercise_instance's routine instance parent is equal to our routine instance ID for our join.
		stringBuffer += String.format("WHERE %s.%s = %s.%s", 
				TABLE_EXERCISE_INSTANCE, 
				COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT, 
				TABLE_ROUTINE_INSTANCE, 
				COLUMN_ROUTINE_INSTANCE_ID);
		
		return stringBuffer;
	}
	
	/*
	 * Returns the SQL create string for the Routine table.
	 */
	private String getRoutineCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s TEXT NOT NULL" +
				", %s INTEGER" +
				", %s INTEGER NOT NULL" +
				")"
				, TABLE_ROUTINE, COLUMN_ROUTINE_ID, COLUMN_ROUTINE_NAME, COLUMN_ROUTINE_LAST_USED, COLUMN_ROUTINE_ACTIVE);
	}
	
	/*
	 * Returns the SQL create string for the RoutineExercise table.
	 */
	private String getRoutineExerciseCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s INTEGER NOT NULL REFERENCES exercise(id)" +
				", %s INTEGER NOT NULL REFERENCES routine(id)" +
				", %s INTEGER NOT NULL" +
				", %s INTEGER NOT NULL" +
				", %s TEXT" +
				", %s INTEGER NOT NULL" +
				")"
				,TABLE_ROUTINE_EXERCISE, COLUMN_ROUTINE_EXERCISE_ID, COLUMN_ROUTINE_EXERCISE_EXERCISE_SUPER, COLUMN_ROUTINE_EXERCISE_ROUTINE_PARENT, COLUMN_ROUTINE_EXERCISE_NUM_SETS, COLUMN_ROUTINE_EXERCISE_POSITION, COLUMN_ROUTINE_EXERCISE_INSTRUCTION, COLUMN_ROUTINE_EXERCISE_EXPANDED);
	}
	
	/*
	 * Returns the SQL create string for the Exercise table.
	 */
	private String getExerciseCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s TEXT NOT NULL" +
				")"
				, TABLE_EXERCISE, COLUMN_EXERCISE_ID, COLUMN_EXERCISE_NAME);
	}
	
	/*
	 * Returns the SQL create string for the RoutineInstance table.
	 */
	private String getRoutineInstanceCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s INTEGER NOT NULL REFERENCES routine(id)" +
				", %s INTEGER NOT NULL" +
				")"
				, TABLE_ROUTINE_INSTANCE, COLUMN_ROUTINE_INSTANCE_ID, COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER, COLUMN_ROUTINE_INSTANCE_DATE);
	}
	
	/*
	 * Returns the SQL create string for the ExerciseInstance table.
	 */
	private String getExerciseInstanceCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s INTEGER NOT NULL REFERENCES exercise(id)" +
				", %s INTEGER NOT NULL REFERENCES routine_instance(id)" +
				", %s TEXT" +
				", %s INTEGER NOT NULL" +
				", %s INTEGER NOT NULL" +
				", %s INTEGER NOT NULL" +
				")"
				, TABLE_EXERCISE_INSTANCE, COLUMN_EXERCISE_INSTANCE_ID, COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER, COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT, COLUMN_EXERCISE_INSTANCE_COMMENT, COLUMN_EXERCISE_INSTANCE_POSITION, COLUMN_EXERCISE_INSTANCE_EXPANDED, COLUMN_EXERCISE_INSTANCE_ROUTINE_EXERCISE_PARENT);
	}
	
	/*
	 * Returns the SQL create string for the SetInstance table.
	 */
	private String getSetInstanceCreateString() {
		return String.format(
				"create table %s (" +
				"%s INTEGER primary key autoincrement" +
				", %s INTEGER NOT NULL" +
				", %s INTEGER" +
				", %s INTEGER" +
				", %s INTEGER NOT NULL REFERENCES exercise_instance(id)" +
				")"
				, TABLE_SET_INSTANCE, COLUMN_SET_INSTANCE_ID, COLUMN_SET_INSTANCE_SET_NUM, COLUMN_SET_INSTANCE_NUM_REPS, COLUMN_SET_INSTANCE_WEIGHT, COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT);
	}
	
	/*
	 * Queries any type of table. Simply wraps the db query call. Requires
	 * all parameters for the given query.
	 */
	public Cursor tableQuery(String tableName, String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return getReadableDatabase().query(tableName, columns, whereClause, whereArgs, group, have, order);
	}
	
	/*
	 * Query specific to the Routine table. Requires all other parameters.
	 * Returns a RoutineCursor containing the results.
	 */
	public RoutineCursor queryRoutine(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new RoutineCursor(tableQuery(TABLE_ROUTINE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the RoutineExercise table. Requires all other parameters.
	 * Returns a RoutineExerciseCursor containing the results.
	 */
	public RoutineExerciseCursor queryRoutineExercise(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new RoutineExerciseCursor(tableQuery(TABLE_ROUTINE_EXERCISE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the Exercise table. Requires all other parameters.
	 * Returns a ExerciseCursor containing the results.
	 */
	public ExerciseCursor queryExercise(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new ExerciseCursor(tableQuery(TABLE_EXERCISE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the RoutineInstance table. Requires all other parameters.
	 * Returns a RoutineInstanceCursor containing the results.
	 */
	public RoutineInstanceCursor queryRoutineInstance(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new RoutineInstanceCursor(tableQuery(TABLE_ROUTINE_INSTANCE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the ExerciseInstance table. Requires all other parameters.
	 * Returns a ExerciseInstanceCursor containing the results.
	 */
	public ExerciseInstanceCursor queryExerciseInstance(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new ExerciseInstanceCursor(tableQuery(TABLE_EXERCISE_INSTANCE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the SetInstance table. Requires all other parameters.
	 * Returns a SetInstanceCursor containing the results.
	 */
	public SetInstanceCursor querySetInstance(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new SetInstanceCursor(tableQuery(TABLE_SET_INSTANCE, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the SetInstanceTimeStamp table. Requires all other parameters.
	 * Returns a SetInstanceCursor containing the results.
	 */
	public SetInstanceCursor querySetInstanceTimeStamp(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new SetInstanceCursor(tableQuery(VIEW_SET_INSTANCE_TIMESTAMP, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Query specific to the ExerciseInstanceTimeStamp table. Requires all other parameters.
	 * Returns an ExerciseInstanceCursor containing the results.
	 */
	public ExerciseInstanceCursor queryExerciseInstanceTimeStamp(String[] columns, String whereClause, String[] whereArgs, String group, String have, String order) {
		return new ExerciseInstanceCursor(tableQuery(VIEW_EXERCISE_INSTANCE_TIMESTAMP, columns, whereClause, whereArgs, group, have, order));
	}
	
	/*
	 * Inserts a routine into the database.
	 * This should only be used for new routines.
	 */
	public long insertRoutine(Routine toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert a routine object that already has an ID defined. Routine's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = new ContentValues();
		
		//name
		String name = toInsert.getName();
		cv.put(COLUMN_ROUTINE_NAME, name);
		
		//Last used. Only add it if it's non-null.
		if (toInsert.getLastUsed() != null) {
			long lastUsed = dateToLong(toInsert.getLastUsed());
			cv.put(COLUMN_ROUTINE_LAST_USED, lastUsed);
		}
		
		
		//Active
		int active = boolToInt(toInsert.isActive());
		cv.put(COLUMN_ROUTINE_ACTIVE, active);
		
		//Commit our content value
		return doInsertion(TABLE_ROUTINE, null, cv);
	}
	
	
	/*
	 * Inserts an exercise into the database.
	 * This should only be used for new exercises.
	 */
	public long insertExercise(Exercise toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert an exercise object that already has an ID defined. Exercise's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = new ContentValues();
		
		//name
		String name = toInsert.getName();
		cv.put(COLUMN_EXERCISE_NAME, name);
		
		
		//Commit our content value
		return doInsertion(TABLE_EXERCISE, null, cv);
	}
	
	/*
	 * Inserts an routineinstance into the database.
	 * This should only be used for new routineinstances.
	 */
	public long insertRoutineInstance(RoutineInstance toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert an routineinstance object that already has an ID defined. RoutineInstance's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = new ContentValues();
		
		//RotSuper
		cv.put(COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER, toInsert.getRoutineSuper());
		
		//Date
		cv.put(COLUMN_ROUTINE_INSTANCE_DATE, dateToLong(toInsert.getDate()));
		
		//Commit our content value
		return doInsertion(TABLE_ROUTINE_INSTANCE, null, cv);
	}
	
	/*
	 * Inserts a ExerciseInstance into the database.
	 * This should only be used for new ExerciseInstance
	 */
	public long insertExerciseInstance(ExerciseInstance toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert an exerciseinstance object that already has an ID defined. ExerciseInstance's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = getExerciseInstanceCv(toInsert);
		
		//Commit our content value
		return doInsertion(TABLE_EXERCISE_INSTANCE, null, cv);
	}
	
	/*
	 * Inserts a SetInstance into the database.
	 * This should only be used for new SetInstance
	 */
	public long insertSetInstance(SetInstance toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert an exerciseinstance object that already has an ID defined. SetInstance's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = new ContentValues();
		
		cv.put(COLUMN_SET_INSTANCE_SET_NUM, toInsert.getSetNum());
		cv.put(COLUMN_SET_INSTANCE_NUM_REPS, toInsert.getNumReps());
		cv.put(COLUMN_SET_INSTANCE_WEIGHT, toInsert.getWeight());
		cv.put(COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT, toInsert.getExerciseInstanceParent());
		
		//Commit our content value
		return doInsertion(TABLE_SET_INSTANCE, null, cv);
	}
	
	/*
	 * Inserts a routine exercise into the database.
	 */
	public long insertRoutineExercise(RoutineExercise toInsert) {
		//Verify that our ID is -1. If it isn't, it's an already defined routine.
		if (toInsert.getId() != -1) {
			throw new RuntimeException("Attempting to insert a RoutineExercise object that already has an ID defined. RoutineExercise's ID is " + toInsert.getId());
		}
		
		//Pull out our values
		ContentValues cv = new ContentValues();
		
		//Exercise super
		int exSup = toInsert.getExerciseSuper();
		
		//Make sure ID isn't -1
		if (exSup == -1) {
			throw new RuntimeException("Encountered RoutineExercise with exerciseSuper that has an id of -1. Was the ExerciseSuper properly initialized?");
		}
		
		cv.put(COLUMN_ROUTINE_EXERCISE_EXERCISE_SUPER, exSup);
		
		//RoutineParent
		int rotParent = toInsert.getRoutineParent();
		
		//Make sure ID isn't -1
		if (rotParent == -1) {
			throw new RuntimeException("Encountered RoutineExercise with RoutineParent that has an id of -1. Was the RoutineParent properly initialized?");
		}
		
		cv.put(COLUMN_ROUTINE_EXERCISE_ROUTINE_PARENT, rotParent);
		
		//numsets
		int numSets = toInsert.getNumSets();
		
		
		cv.put(COLUMN_ROUTINE_EXERCISE_NUM_SETS, numSets);
		
		//position
		int position = toInsert.getPosition();

		
		//Make sure the position is defined (not -1)
		if (position == -1) {
			throw new RuntimeException("Position defined as -1. Means it wasn't configured in LiftLogDBAPI");
		}
		
		cv.put(COLUMN_ROUTINE_EXERCISE_POSITION, position);
		
		//Instruction
		String instruction = toInsert.getInstruction();
		
		if (instruction != null) {
			cv.put(COLUMN_ROUTINE_EXERCISE_INSTRUCTION, instruction);
		}
		
		int expanded = boolToInt(toInsert.isExpanded());
		
		cv.put(COLUMN_ROUTINE_EXERCISE_EXPANDED, expanded);
		
		//Commit our content value
		return doInsertion(TABLE_ROUTINE_EXERCISE, null, cv);
	}
	
	
	/*
	 * Deletes a routine exercise from the database.
	 */
	public int deleteRoutineExercise(RoutineExercise toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_ROUTINE_EXERCISE, id);
	}
	
	/*
	 * Deletes a routine from the database.
	 */
	public int deleteRoutine(Routine toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_ROUTINE, id);
	}
	
	/*
	 * Deletes an exercise from the database.
	 */
	public int deleteExercise(Exercise toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_EXERCISE, id);
	}
	
	/*
	 * Deletes a RoutineInstance from the database.
	 */
	public int deleteRoutineInstance(RoutineInstance toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_ROUTINE_INSTANCE, id);
	}
	
	/*
	 * Deletes an ExerciseInstance from the database.
	 */
	public int deleteExerciseInstance(ExerciseInstance toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_EXERCISE_INSTANCE, id);
	}
	
	/*
	 * Deletes a SetInstance from the database.
	 */
	public int deleteSetInstance(SetInstance toDelete) {
		//Get the ID to delete.
		int id = toDelete.getId();
		
		return doDelete(TABLE_SET_INSTANCE, id);
	}
	
	/*
	 * Takes an existing routine and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateRoutine(Routine toUpdate) {
		//Make sure our routine's ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update a routine with a routine ID of -1.");
		}
		
		//Extract all values other than our ID and pack it into a CV
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ROUTINE_NAME, toUpdate.getName());
		cv.put(COLUMN_ROUTINE_ACTIVE, boolToInt(toUpdate.isActive()));
		cv.put(COLUMN_ROUTINE_LAST_USED, dateToLong(toUpdate.getLastUsed()));
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_ROUTINE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	/*
	 * Takes an existing Exercise and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateExercise(Exercise toUpdate) {
		//Make sure our Exercise ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update an exercise with an ID of -1.");
		}
		
		//Extract all values other than our ID and pack it into a CV
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_EXERCISE_NAME, toUpdate.getName());
		cv.put(COLUMN_EXERCISE_ID, toUpdate.getId());
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_EXERCISE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	/*
	 * Takes an existing RoutineInstance and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateRoutineInstance(RoutineInstance toUpdate) {
		//Make sure our RoutineInstance ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update a routineinstance with an ID of -1.");
		}
		
		//Extract all values other than our ID and pack it into a CV
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ROUTINE_INSTANCE_ID, toUpdate.getId());
		cv.put(COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER, toUpdate.getRoutineSuper());
		cv.put(COLUMN_ROUTINE_INSTANCE_DATE, dateToLong(toUpdate.getDate()));
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_ROUTINE_INSTANCE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	
	/*
	 * Takes an existing ExerciseInstance and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateExerciseInstance(ExerciseInstance toUpdate) {
		//Make sure our ExerciseInstance ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update a exerciseinstance with an ID of -1.");
		}
		
		//Pull out our values
		ContentValues cv = getExerciseInstanceCv(toUpdate);
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_EXERCISE_INSTANCE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	/*
	 * Returns the assembled content values for the exercise instance from the provided
	 * exercise instance.
	 */
	private ContentValues getExerciseInstanceCv(ExerciseInstance toAssemble) {
		ContentValues cv = new ContentValues();
		
		//RotSuper
		cv.put(COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER, toAssemble.getExerciseSuper());
		cv.put(COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT, toAssemble.getRoutineInstanceParent());
		cv.put(COLUMN_EXERCISE_INSTANCE_COMMENT, toAssemble.getComment());
		cv.put(COLUMN_EXERCISE_INSTANCE_POSITION, toAssemble.getPosition());
		cv.put(COLUMN_EXERCISE_INSTANCE_EXPANDED, boolToInt(toAssemble.isExpanded()));
		cv.put(COLUMN_EXERCISE_INSTANCE_ROUTINE_EXERCISE_PARENT, toAssemble.getRoutineExerciseParent());
		
		return cv;
	}
	
	/*
	 * Takes an existing SetInstance and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateSetInstance(SetInstance toUpdate) {
		//Make sure our SetInstance ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update a exerciseinstance with an ID of -1.");
		}
		
		//Extract all values other than our ID and pack it into a CV
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SET_INSTANCE_ID, toUpdate.getId());
		cv.put(COLUMN_SET_INSTANCE_SET_NUM, toUpdate.getSetNum());
		cv.put(COLUMN_SET_INSTANCE_NUM_REPS, toUpdate.getNumReps());
		cv.put(COLUMN_SET_INSTANCE_WEIGHT, toUpdate.getWeight());
		cv.put(COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT, toUpdate.getExerciseInstanceParent());
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_SET_INSTANCE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	/*
	 * Takes an existing routineexercise and updates it in our database.
	 * Returns the number of rows affected.
	 */
	public long updateRoutineExercise(RoutineExercise toUpdate) {
		//Make sure our routine's ID is defined.
		if (toUpdate.getId() == -1) {
			throw new RuntimeException("Attempted to update a RoutineExercise with a routine ID of -1.");
		}
		
		//Extract all values other than our ID and pack it into a CV
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ROUTINE_EXERCISE_ID, toUpdate.getId());
		cv.put(COLUMN_ROUTINE_EXERCISE_EXERCISE_SUPER, toUpdate.getExerciseSuper());
		cv.put(COLUMN_ROUTINE_EXERCISE_INSTRUCTION, toUpdate.getInstruction());
		cv.put(COLUMN_ROUTINE_EXERCISE_NUM_SETS, toUpdate.getNumSets());
		cv.put(COLUMN_ROUTINE_EXERCISE_POSITION, toUpdate.getPosition());
		cv.put(COLUMN_ROUTINE_EXERCISE_ROUTINE_PARENT, toUpdate.getRoutineParent());
		cv.put(COLUMN_ROUTINE_EXERCISE_EXPANDED, boolToInt(toUpdate.isExpanded()));
		
		//Execute our update.
		return getWritableDatabase().update(TABLE_ROUTINE_EXERCISE, cv, "_id = ?", new String[] { String.valueOf(toUpdate.getId()) });
	}
	
	/*
	 * Returns true if the int is 0, false if the int is 1.
	 * Throws exception for any other value.
	 */
	public static boolean intToBool(int boolValue) {
		if (boolValue != 1 && boolValue != 0) {
			//Invalid input.
			throw new RuntimeException("Provided boolean value of " + boolValue);
		}
		
		//True if 0, false otherwise.
		return boolValue == 0 ? true : false;
	}
	
	/*
	 * Converts a boolean to an equivalent integer
	 * 0 --> true; 1 --> false.
	 */
	public static int boolToInt(boolean boolValue) {
		return boolValue == true ? 0 : 1;
	}
	
	/*
	 * Converts a long representation of a date
	 * back into a date object.
	 */
	public static Date longToDate(long dateInLong) {
		return new Date(dateInLong);
	}
	
	/*
	 * Converts a date to its long representation.
	 */
	public static long dateToLong(Date toConvert) {
		return toConvert.getTime();
	}

	/*
	 * This wraps DB insertion operations and catches SQLiteException.
	 * Currently throws RuntimeException on all SQLIteExceptions.
	 */
	private long doInsertion(String table, String nullHack, ContentValues toIns) {
		try {
			return getWritableDatabase().insert(table, nullHack, toIns);
		} catch (SQLiteException e) {

			throw new RuntimeException("Error during DB Insertion: " + e.getMessage());
		}
	}
	
	/*
	 * This wraps DB delete operations and catches SQLiteException.
	 * Currently throws RuntimeException on all SQLIteExceptions.
	 */
	private int doDelete(String table, int idToDel) {
		try {
			return getWritableDatabase().delete(table, "_id = ?", new String[] { String.valueOf(idToDel) });
		} catch (SQLiteException e) {

			throw new RuntimeException("Error during DB deletion: " + e.getMessage());
		}
	}
	
	/*
	 * RoutineCursor is used to wrap cursors from the Routine table.
	 */
	public class RoutineCursor extends CursorWrapper {
		
		public RoutineCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single Routine from the cursor.
		 */
		public Routine getRoutine() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Grab our values out
			int id = getInt(getColumnIndex(COLUMN_ROUTINE_ID));
			String name = getString(getColumnIndex(COLUMN_ROUTINE_NAME));
			Date lastUsed = longToDate(getLong(getColumnIndex(COLUMN_ROUTINE_LAST_USED)));
			boolean active = intToBool(getInt(getColumnIndex(COLUMN_ROUTINE_ACTIVE)));
			
			//Increment our row
			moveToNext();
			
			//Init our Routine object and return it.
			return new Routine(mAppContext, id, name, lastUsed, active);
		}
		
	}
	
	/*
	 * RoutineExerciseCursor is used to wrap cursors from the Routine table.
	 */
	public class RoutineExerciseCursor extends CursorWrapper {
		
		public RoutineExerciseCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single RoutineExercise from the cursor.
		 */
		public RoutineExercise getRoutineExercise() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Get its ID
			int id = getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_ID));
			int exerciseSuper = getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_EXERCISE_SUPER));
			int routineParent = getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_ROUTINE_PARENT));
			int numSets = getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_NUM_SETS));
			int position = getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_POSITION));
			String instruction = getString(getColumnIndex(COLUMN_ROUTINE_EXERCISE_INSTRUCTION));
			boolean expanded = intToBool(getInt(getColumnIndex(COLUMN_ROUTINE_EXERCISE_EXPANDED)));
			
			//Increment our row
			moveToNext();
			
			return new RoutineExercise(mAppContext, id, exerciseSuper, routineParent, numSets, position, instruction, expanded);
		}
		
		
	}
	
	
	
	/*
	 * ExerciseCursor is used to wrap cursors from the Exercise table.
	 */
	public class ExerciseCursor extends CursorWrapper {
		
		public ExerciseCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single Exercise from the cursor.
		 */
		public Exercise getExercise() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Grab our values out
			int id = getInt(getColumnIndex(COLUMN_EXERCISE_ID));
			String name = getString(getColumnIndex(COLUMN_EXERCISE_NAME));
			
			//Increment our row
			moveToNext();
			
			//Init our Routine object and return it.
			return new Exercise(mAppContext, id, name);
		}
		
	}
	
	/*
	 * RoutineInstanceCursor is used to wrap cursors from the RoutineInstance table.
	 */
	public class RoutineInstanceCursor extends CursorWrapper {
		
		public RoutineInstanceCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single RoutineInstance from the cursor.
		 */
		public RoutineInstance getRoutineInstance() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Grab our values out
			int id = getInt(getColumnIndex(COLUMN_ROUTINE_INSTANCE_ID));
			int superId = getInt(getColumnIndex(COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER));
			Date instanceDate = longToDate(getLong(getColumnIndex(COLUMN_ROUTINE_INSTANCE_DATE)));
			
			//Increment our row
			moveToNext();
			
			//Init our Routine object and return it.
			return new RoutineInstance(mAppContext, id, superId, instanceDate);
		}
		
	}
	
	/*
	 * ExerciseInstanceCursor is used to wrap cursors from the ExerciseInstance table.
	 */
	public class ExerciseInstanceCursor extends CursorWrapper {
		
		public ExerciseInstanceCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single ExerciseInstance from the cursor.
		 */
		public ExerciseInstance getExerciseInstance() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Grab our values out
			int id = getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_ID));
			int exSuper = getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_EXERCISE_SUPER));
			int rotSuper = getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_ROUTINE_INSTANCE_PARENT));
			String commentStr = getString(getColumnIndex(COLUMN_EXERCISE_INSTANCE_COMMENT));
			int position = getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_POSITION));
			boolean expanded = intToBool(getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_EXPANDED)));
			int routineExerciseParent = getInt(getColumnIndex(COLUMN_EXERCISE_INSTANCE_ROUTINE_EXERCISE_PARENT)); 
			
			//Increment our row
			moveToNext();
			
			//Init our Routine object and return it.
			return new ExerciseInstance(mAppContext, id, exSuper, rotSuper, commentStr, position, expanded, routineExerciseParent);
		}
		
	}
	
	/*
	 * SetInstanceCursor is used to wrap cursors from the SetInstance table.
	 */
	public class SetInstanceCursor extends CursorWrapper {
		
		public SetInstanceCursor(Cursor c) {
			super(c);
		}
		
		/*
		 * Returns a single SetInstance from the cursor.
		 */
		public SetInstance getSetInstance() {
			
			if (isBeforeFirst()) {
				//Move to first entry.
				moveToFirst();
			}
			
			if (isAfterLast()) {
				return null;
				
			}
			
			//Grab our values out
			int id = getInt(getColumnIndex(COLUMN_SET_INSTANCE_ID));
			int setNum = getInt(getColumnIndex(COLUMN_SET_INSTANCE_SET_NUM));
			int numReps = getInt(getColumnIndex(COLUMN_SET_INSTANCE_NUM_REPS));
			float weight = getFloat(getColumnIndex(COLUMN_SET_INSTANCE_WEIGHT));
			int exInstParent = getInt(getColumnIndex(COLUMN_SET_INSTANCE_EXERCISE_INSTANCE_PARENT));
			
			//Increment our row
			moveToNext();
			
			//Init our Routine object and return it.
			return new SetInstance(mAppContext, id, setNum, numReps, weight, exInstParent);
		}
		
	}
	
}
