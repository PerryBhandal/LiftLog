package com.plined.liftlog;

import android.content.Context;

public class WorkoutSamples {
	
	LiftLogDBAPI mDbHelper;
	Context mAppContext;
	
	private static final String UPPER_BODY_NAME = "Sample: Upper Body";
	private static final String LOWER_BODY_NAME = "Sample: Lower Body";
	
	private static final String FOURSET_COMMENT = 
"1 warmup set\n" +
"Rest 90 seconds\n" +
"Work set 1: 5-8 reps, to failure.\n" +
"Rest 45 seconds.\n" +
"Work set 2: 9-12 reps, to failure.\n" +
"No rest. Weight for work set 3 is half of weight used in work set 2.\n" +
"Work set 3: 5-8 reps, to failure.";
	
	private static final String FIVESET_COMMENT = 
"2 warmup sets\n" +
"Rest 90 seconds\n" +
"Work set 1: 5-8 reps, to failure.\n" +
"Rest 45 seconds.\n" +
"Work set 2: 9-12 reps, to failure.\n" +
"No rest. Weight for work set 3 is half of weight used in work set 2.\n" +
"Work set 3: 5-8 reps, to failure.";
	
	public WorkoutSamples(Context appContext) {
		mAppContext = appContext;
		mDbHelper = LiftLogDBAPI.get(mAppContext);
	}
	
	/*
	 * Creates our upper and lower body routines and inserts them into the database.
	 */
	public void createSampleRoutines() {
		createUpperBodySample();
		createLowerBodySample();
	}
	
	private void createUpperBodySample() {
		//Create our routine
		Routine upperBody = createGetRoutine(UPPER_BODY_NAME);
		
		/*
		 * /Create our routine exercises and insert them.
		 */
		createInsertExercise("Barbell Bench Press", FIVESET_COMMENT, upperBody);
		createInsertExercise("Dumbbell Row", FIVESET_COMMENT, upperBody);
		createInsertExercise("Incline Dumbbell Press", FIVESET_COMMENT, upperBody);
		createInsertExercise("Cable Pulldowns", FIVESET_COMMENT, upperBody);
		createInsertExercise("Dips", FOURSET_COMMENT, upperBody);
		createInsertExercise("Bicep Curl", FOURSET_COMMENT, upperBody);
		createInsertExercise("Dumbbell Lateral Raise", FOURSET_COMMENT, upperBody);
		
		
	}
	
	private void createLowerBodySample() {
		//Create our routine
		Routine lowerBody = createGetRoutine(LOWER_BODY_NAME);
		
		/*
		 * /Create our routine exercises and insert them.
		 */
		createInsertExercise("Squats", FIVESET_COMMENT, lowerBody);
		createInsertExercise("Dumbbell Stiff Leg Deadlift", FIVESET_COMMENT, lowerBody);
		createInsertExercise("Seated Leg Extensions", FIVESET_COMMENT, lowerBody);
		createInsertExercise("Decline Crunches", FOURSET_COMMENT, lowerBody);
		createInsertExercise("Standing Calf Raise", FOURSET_COMMENT, lowerBody);
	}
	
	/*
	 * Creates the routine with the provided details and returns its instantiated version (the
	 * version where its ID is defined). It handles checking that its returned Routine is non-null.
	 */
	private Routine createGetRoutine(String routineName) {
		//Create the routine
		new Routine(mAppContext,routineName, null, true).insert();
		
		//Get the routine's ID
		Routine insertedRot = mDbHelper.getRoutineByName(routineName, true, true);
		
		if (insertedRot == null) {
			//Means the routine we just created can't bef ound.
			throw new RuntimeException("Created routine with name " + routineName + " but it can't be retrieved after creation");
		}
		
		return insertedRot;
	}
	
	/*
	 * Adds an exercise with the provided details to the provided routine.
	 */
	private void createInsertExercise(String exerciseName, String exerciseComments, Routine parentRoutine) {
		
		//Get the exercise object for this exercise.
		Exercise nameCheck = mDbHelper.getExerciseByName(exerciseName, false);
		
		//Check if the exercise doesn't exist in our DB yet.
		if (nameCheck == null) {
			//The exercise we want to add isn't an exercise that exists. Add it.
			createExercise(exerciseName);
		}
		
		//Get the exercise object. We can be case sensitive now as it should exactly match our entry.
		Exercise toAdd = mDbHelper.getExerciseByName(exerciseName, false);
		
		//Make sure it's not null. Shouldn't be under any circumstance.
		if (toAdd == null) {
			throw new RuntimeException("Created an exercise and inserted it into the database, but immediately after" +
					"insertion the exercise can't be retrieved. Exercise name is " + exerciseName);
		}
	
		//Exercise isn't in our routine, add it.
		RoutineExercise newRoutineExercise = new RoutineExercise(mAppContext, toAdd.getId(), parentRoutine.getId(), 3, 1, exerciseComments, false);
		
		//Insert the new routine exercise.
		newRoutineExercise.insert();
	}
	
	/*
	 * Creates the exercise and inserts it into the DB.
	 */
	private void createExercise(String exerciseName) {
		new Exercise(mAppContext, exerciseName).insert();
	}
	
}
