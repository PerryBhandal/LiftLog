package com.plined.liftlog;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.plined.liftlog.LiftLogDBMaster.RoutineExerciseCursor;


public class RoutineFactoryFragment extends ListFragment {
	
	//Stores the routine we're currently managing.
	private Routine mRoutine;
	
	private int mRoutineId;
	
	private static String TAG = "RoutineFactoryFragment";
	
	private RoutineExerciseCursorAdapter mAdapter;
	
	private LiftLogDBAPI mDbHelper;
	
	private View mHeaderView;
	
	//Key used in our bundle to store the routine's ID
	public static String ROUTINE_ID = "com.perryb.liftlog.routinefactoryfragment.routine_id";
	public static String ROUTINE_EXERCISE_ID = "com.perryb.liftlog.routinefactoryfragment.routine_exercise_id";
	
	/*
	 * Request codes
	 */
	private static int REQUEST_EDIT_ROUTINE_NAME = 2;
	private static int REQUEST_ADD_EXERCISE = 3;
	private static int REQUEST_SET_INSTRUCTION = 4;
	private static int REQUEST_DELETE_EXERCISE = 5;
	private static int REQUEST_RENAME_EXERCISE = 6;
	
	/*
	 * Result codes.
	 */
	private static int RESULT_EDIT_ROUTINE = 2;
	private static int RESULT_ADD_EXERCISE = 3;
	private static int RESULT_SET_INSTRUCTION = 4;
	private static int RESULT_DELETE_EXERCISE = 5;
	private static int RESULT_RENAME_EXERCISE = 6;
	
	/*
	 * Dialog tags
	 */
	
	private static String DIALOG_TAG_EDIT_NAME = "editroutinename"; 
	private static String DIALOG_TAG_ADD_EXERCISE = "addexercise"; 
	
	
	public static RoutineFactoryFragment newInstance(int routineId) {
		//Create our bundle
		Bundle args = new Bundle();
		//Put the routine's ID in
		args.putInt(ROUTINE_ID, routineId);
		
		//Create our routine fragment
		RoutineFactoryFragment toLaunch = new RoutineFactoryFragment();
		
		//Attach our bundle
		toLaunch.setArguments(args);
		
		return toLaunch;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get a reference to our DB manager
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		//Assign our ID
		mRoutineId = getRoutineArg(getArguments());
		
		//Set this so we'll get callbacks when menu items are hit.
		setHasOptionsMenu(true);
		
		getRoutineFromDb();
	}
	
	/*
	 * Gets our routine from the Db based on the mRoutineId we have set.
	 */
	public void getRoutineFromDb() {
		//Get our routine object
		mRoutine = mDbHelper.getRoutineById(mRoutineId);
		
		//Make sure our routine is non-null
		if (mRoutine == null) {
			throw new RuntimeException("Routine with id " + mRoutineId + " was null after retrieval.");
		}
	}
	

	
	/*
	 * Right now all of the listview initialization stuff happens in onStart().
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		RoutineExerciseCursor insCursor = (RoutineExerciseCursor) new RoutineExerciseCursorLoader(getActivity()).loadCursor();
		mAdapter = new RoutineExerciseCursorAdapter(getActivity(), insCursor);
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Inflate the context menu
		getActivity().getMenuInflater().inflate(R.menu.routine_factory_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		//Figure out if they hit the delete button
		switch(item.getItemId()) {
		case R.id.menu_item_delete_routine_exercise: {
			//Trying to delete.
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out which we're trying to delete
			RoutineExerciseCursorAdapter adapter = (RoutineExerciseCursorAdapter)getListAdapter();
			RoutineExerciseCursor rotExCursor = (RoutineExerciseCursor) adapter.getItem(position-1);
			RoutineExercise rotEx = rotExCursor.getRoutineExercise();
			
			//Launch a delete confirm dialog.
			//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
			ConfirmDeleteRotEx fragToUse = ConfirmDeleteRotEx.newInstance(rotEx.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "deleterotexconfirm", REQUEST_DELETE_EXERCISE);
			
			return true;
		}
		case R.id.menu_item_rename_routine_exercise: {
			//Trying to rename exercise.
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out which we're trying to rename.
			RoutineExerciseCursorAdapter adapter = (RoutineExerciseCursorAdapter)getListAdapter();
			RoutineExerciseCursor rotExCursor = (RoutineExerciseCursor) adapter.getItem(position-1);
			RoutineExercise rotEx = rotExCursor.getRoutineExercise();
			
			//Launch a rename dialog.
			//Create the fragment. We'll attach the exercise super id so w eknow what to edit.
			RenameExerciseDialog fragToUse = RenameExerciseDialog.newInstance(rotEx.getExerciseSuper());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "renameexercise", REQUEST_RENAME_EXERCISE);
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_routine_factory, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		RoutineExerciseCursor rotCursor;
		
		switch(item.getItemId()) {
		case R.id.menu_item_expand:
			//They hit the expand collapsae button
			
			//Get our current rotCursor
			rotCursor = (RoutineExerciseCursor) mAdapter.getCursor();
			
			rotCursor.moveToFirst();
			//Iterate over it and set everyone's expanded state to false
			while (!rotCursor.isAfterLast()) {
				rotCursor.getRoutineExercise().setExpanded(true);
			}
			
			//Update our data
			mAdapter.reloadData();
			return true;
		case R.id.menu_item_collapse:
			//They hit the collapsae button
			
			//Get our current rotCursor
			rotCursor = (RoutineExerciseCursor) mAdapter.getCursor();
			
			rotCursor.moveToFirst();
			//Iterate over it and set everyone's expanded state to true
			while (!rotCursor.isAfterLast()) {
				rotCursor.getRoutineExercise().setExpanded(false);
			}
			
			//Update our data
			mAdapter.reloadData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//Add the header to the listview
		mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.t_routine_factory_header, null);
		getListView().addHeaderView(mHeaderView);
		
		configureHeaderView();
		
		//Make sure our listview doesn't show selections
		getListView().setSelector(android.R.color.transparent);
		
		super.onActivityCreated(savedInstanceState);
	}
	
	/*
	 * Populates the text fields of the header view, and sets up its on click listeners.
	 */
	public void configureHeaderView() {
		//Set up our textviews
		configureHeaderTextView();
		
		//Set up our header onclick listeners
		configureHeaderOnClick();
	}
	
	private void configureHeaderTextView() {
		//Populate textviews in both rows.
		LinearLayout topRow = (LinearLayout) mHeaderView.findViewById(R.id.t_routine_factory_header_name);
		configureLabelRow(topRow, "Name", mRoutine.getName());
		
		LinearLayout bottomRow = (LinearLayout) mHeaderView.findViewById(R.id.t_routine_factory_header_lastUsed);
		configureLabelRow(bottomRow, "Last Used", Utilities.formatDate(mRoutine.getLastUsed()));
	}
	
	private void configureHeaderOnClick() {
		//Set an onclick listener on the name modification dialog box.
		View nameLayout = mHeaderView.findViewById(R.id.t_routine_factory_header_name);
		nameLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogFragment toShow = NameEditDialog.newInstance(mRoutineId);
				launchDialogFragmentTarget(toShow, DIALOG_TAG_EDIT_NAME, REQUEST_EDIT_ROUTINE_NAME);
			}
		});
		
		//Set an onclick listener on the exercise add dialog box.
		View exerciseLayout = mHeaderView.findViewById(R.id.t_routine_factory_header_addExercise);
		exerciseLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogFragment toShow = AddExerciseDialog.newInstance(mRoutineId);
				launchDialogFragmentTarget(toShow, DIALOG_TAG_ADD_EXERCISE, REQUEST_ADD_EXERCISE);
			}
		});
	}
	
	/*
	 * Configures a row of entries in our header.
	 */
	public void configureLabelRow(LinearLayout layoutRoot, String leftLabel, String rightLabel) {
		TextView leftTv = (TextView) layoutRoot.findViewById(R.id.t_workout_instance_header_element_leftText);
		leftTv.setText(leftLabel);
		
		TextView rightTv = (TextView) layoutRoot.findViewById(R.id.t_workout_instance_header_element_rightText);
		rightTv.setText(rightLabel);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		v.setBackgroundResource(R.color.newTempBg);
		
		//Make our listview context menu sensitive
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		registerForContextMenu(listView);
		
		//Set our divider to be non-existent
		listView.setDividerHeight(0);
		
		return v;
	}

	
	/*
	 * Pulls the routine_id from our fragment bundle and returns it.
	 * Returns -1 for the routineID if there were no arguments included.
	 */
	private int getRoutineArg(Bundle fragArguments) {
		if (fragArguments == null) {
			//No routine ID provided.
			return -1;
		}
		
		int rotId = fragArguments.getInt(ROUTINE_ID);
		
		return rotId;
		
	}
	
	/*
	 * Creates and launches a dialog fragment. This is used in cases where we don't want to set it as our target.
	 */
	protected void launchDialogFragment(DialogFragment dialogToShow, String dialogTag) {
		//Get our fragment manager
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		//Show it.
		dialogToShow.show(fm, dialogTag);
	}
	
	/*
	 * Creates and launches a dialog fragment. This is used in cases where we do want our current
	 * fragment to be targeted.
	 */
	protected void launchDialogFragmentTarget(DialogFragment dialogToShow, String dialogTag, int targetRequest) {
		dialogToShow.setTargetFragment(this, targetRequest);
		
		launchDialogFragment(dialogToShow, dialogTag);
	}
	
	/*
	 * Processes returns from our dialogs.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EDIT_ROUTINE_NAME) {
			//Routine name has been updated. Update our routine info.
			getRoutineFromDb();
			
			//Reconfigure our header
			configureHeaderTextView();
		} else if (requestCode == REQUEST_ADD_EXERCISE) {
			//Exercise has been added. Notify that our data set has changed.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_SET_INSTRUCTION) {
			//An instruction has been set on one of our routine exercises. Update our data set.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_DELETE_EXERCISE) {
			//Exercise has been removed from routine, reload data.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_RENAME_EXERCISE) {
			//Exercise has been renamaed. Reload data.
			mAdapter.reloadData();
		}
		else {
			//Unexpected code returned.
			throw new RuntimeException("Received unexpected request and/or result code. Request code is " + requestCode + " result code is " + resultCode);
		}
	}
	

	
	/*
	 * Adds an onclick listener to our name so when it's clicked we're able to edit the name of our routine.
	 * 
	 * viewToListen is the View to add the onclick listener to.
	 */
	private void addNameOnClickListener(View viewToListen) {
		//Add the onclick listener to our view
		viewToListen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * Create and launch our name dialog editing box.
				 */
				
				//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
				NameSelectionDialog fragToUse = NameEditDialog.newInstance(mRoutine.getId());
				
				//Launch this fragment.
				launchDialogFragmentTarget(fragToUse, "editroutinename", REQUEST_EDIT_ROUTINE_NAME);
			}
		});
	}

	
	public static class InstructionEditDialog extends NameSelectionDialog {
		
		RoutineExercise mRoutineExercise;
		
		public static InstructionEditDialog newInstance(int routineExerciseId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_EXERCISE_ID, routineExerciseId);
			
			InstructionEditDialog fragment = new InstructionEditDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Exercise Instructions";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get our instruction text
			EditText routineEditText = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			String enteredText = routineEditText.getText().toString();
			
			//Put it into our routine
			mRoutineExercise.setInstruction(enteredText);
			
			//Call onactivity result so it can reload our data
			getTargetFragment().onActivityResult(REQUEST_SET_INSTRUCTION, RESULT_SET_INSTRUCTION, null);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Save";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Enter your instructions for this exercise", "");
			
			//Get our routineexercise object
			int rotExId = getArguments().getInt(ROUTINE_EXERCISE_ID);
			mRoutineExercise = LiftLogDBAPI.get(getActivity()).getRoutineExerciseById(rotExId);
			
			//Populate the edit text with our existing instruction text
			setExistingText();
			
			return v;
		}
		
		/*
		 * Populates our edit text with the existing routine name.
		 */
		private void setExistingText() {
			//Get our edit text
			EditText nameBox = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			
			//Set its text to the routine name.
			nameBox.setText(mRoutineExercise.getInstruction());
			
			//Set our text box to be left aligned.
			nameBox.setGravity(Gravity.LEFT);
			
			//Put our edit text cursor at the end of our string
			nameBox.setSelection(nameBox.getText().length());
		}
		
		
	}

	
	public static class NameEditDialog extends NameSelectionDialog {
		
		public static NameSelectionDialog newInstance(int routineId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_ID, routineId);
			
			NameSelectionDialog fragment = new NameEditDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Edit Routine Name";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get our routine name
			EditText routineEditText = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			String enteredText = routineEditText.getText().toString();
			
			//Trim off leading and trailing whitespaces
			enteredText = enteredText.trim();
			
			//Make sure it's long enough
			if (!isLongEnough(enteredText, MIN_ROUTINE_NAME_LENGTH)) {
				showErrorText("Your routine name can't be empty.");
				return false;
			}
			
			//Check if it's the same as our existing routine name. That'll happen when they
			//open the edit name box, then press ok. Let's just return true in that case without actually
			//modifying our routine's name.
			if (enteredText.equals(mRoutine.getName())) {
				//Name is unchanged.
				return true;
			}
			
			//Make sure the name isn't in use
			if (!routineNameAvailable(enteredText)) {
				showErrorText("That routine name is already in use.");
				return false;
			}
			
			//Update the routine's name
			updateRoutineName(enteredText);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Edit Routine";
		}
		
		/*
		 * Updates our mRoutine with its new name, then updates
		 * it in the database.
		 */
		private void updateRoutineName(String newName) {
			//Change the name in mroutine
			mRoutine.setName(newName);
			
			//Tell teh hosting fragment we're done
			getTargetFragment().onActivityResult(REQUEST_EDIT_ROUTINE_NAME, RESULT_EDIT_ROUTINE, null);
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Choose a new routine name", "");
			
			//Grab our routine into our member var
			getRoutine();
			
			//Populate the edit text with our existing routine name.
			setExistingName();
			
			return v;
		}
		
		/*
		 * Populates our edit text with the existing routine name.
		 */
		private void setExistingName() {
			//Get our edit text
			EditText nameBox = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			
			//Set its text to the routine name.
			nameBox.setText(mRoutine.getName());
			
			//Put our edit text cursor at the end of our string
			nameBox.setSelection(nameBox.getText().length());
		}
		
		
	}

	public static class AddExerciseDialog extends NameSelectionDialog {
		
		public static AddExerciseDialog newInstance(int routineId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_ID, routineId);
			
			AddExerciseDialog fragment = new AddExerciseDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		
		public String getDialogTitle() {
			return "Add Exercise";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get our edit text string
			String enteredText = getEditTextString();
			
			//Trim the text
			enteredText = enteredText.trim();
			
			//Make sure it's at least one character long
			if (!isLongEnough(enteredText, 1)) {
				showErrorText("The exercise name can't be empty.");
				return false;
			}
			
			//Get the exercise object for this exercise.
			Exercise nameCheck = getDBManager().getExerciseByName(enteredText, false);
			
			//Check if the exercise doesn't exist in our DB yet.
			if (nameCheck == null) {
				//The exercise we want to add isn't an exercise that exists. Add it.
				createExercise(enteredText);
			} else {
				//Name check isn't null. So the exercise exists already. If the name differs
				//change it in the DB so all future instances get the new casing.
				if (!nameCheck.getName().equals(enteredText)) {
					//Casing idffers. Update it.
					nameCheck.setName(enteredText);
					nameCheck.update();
				}
			}
			
			//Get the exercise object. We can be case sensitive now as it should exactly match our entry.
			Exercise toAdd = getDBManager().getExerciseByName(enteredText, false);
			
			//Make sure it's not null. Shouldn't be under any circumstance.
			if (toAdd == null) {
				throw new RuntimeException("Created an exercise and inserted it into the database, but immediately after" +
						"insertion the exercise can't be retrieved. Exercise name is " + enteredText);
			}
			
			//See if this exercise is in our routine already
			if (exerciseInRoutine(toAdd)) {
				//Print the error text.
				showErrorText("The exercise '" + toAdd.getName() + "' is already in this routine.");
				return false;
			} 
		
			//Exercise isn't in our routine, add it.
			RoutineExercise newRoutineExercise = new RoutineExercise(getActivity().getApplicationContext(), toAdd.getId(), mRoutine.getId(), 1, 1, null, true);
			
			//Insert the new routine exercise.
			newRoutineExercise.insert();
			
			//Call our parent so it knows to update.
			getTargetFragment().onActivityResult(REQUEST_ADD_EXERCISE, RESULT_ADD_EXERCISE, null);
			
			return true;
		}
		
		/*
		 * Checks whether the provided exercise is already in our routine.
		 */
		private boolean exerciseInRoutine(Exercise toAdd) {
			//Grab all of our routineexercises
			ArrayList<RoutineExercise> rotExArray = getDBManager().getRoutineExercises(mRoutine.getId());
			for (RoutineExercise rotEx : rotExArray) {
				//check if the exercise is in our routine already
				if (rotEx.getExerciseSuper() == toAdd.getId()) {
					//Exercise is in our routine already.
					return true;
				}
			}
			
			//Exercise isn't in our routine.
			return false;
		}
		
		/*
		 * Creates the exercise and inserts it into the DB.
		 */
		private void createExercise(String exerciseName) {
			new Exercise(getActivity(), exerciseName).insert();
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_add_exercise, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Enter the name of the exercise you wish to add.", "Exercise Name");
			
			//Get all exercises
			String[] exerciseNames = LiftLogDBAPI.get(getActivity()).getAllExercisesNames();
			
			ArrayAdapter<String> exAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, exerciseNames);
			
			AutoCompleteTextView textView = (AutoCompleteTextView) v.findViewById(R.id.fragment_new_routine_name_edit_text); 
			
			textView.setAdapter(exAdapter);
			
			//Grab our routine into our member var
			getRoutine();
			
			return v;
		}
	}
	

	public static class RenameExerciseDialog extends NameSelectionDialog {
		
		private static final String EXERCISE_ID =  "com.plined.liftlog.routinefactoryfragment.renameexercisedialog.exerciseid";
		private Exercise mExercise;
		
		public static RenameExerciseDialog newInstance(int exerciseId) {
			Bundle args = new Bundle();
			args.putInt(EXERCISE_ID, exerciseId);
			
			RenameExerciseDialog fragment = new RenameExerciseDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		
		public String getDialogTitle() {
			return "Rename Exercise";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get our edit text string
			String enteredText = getEditTextString();
			
			//Trim the text
			enteredText = enteredText.trim();
			
			//Check if we're just recasing the name, or using the same name.
			if (enteredText.toLowerCase().equals(mExercise.getName().toLowerCase())) {
				//Updat eit and return. No need to check itf it's in use.
				mExercise.setName(enteredText);
				mExercise.update();
				return true;
			}
			
			//Make sure it's at least one character long
			if (!isLongEnough(enteredText, 1)) {
				showErrorText("The exercise name can't be empty.");
				return false;
			}
			
			//Make sure the name isn't in use
			if (LiftLogDBAPI.get(getActivity()).isExNameUsed(enteredText, false)) {
				//An exercise already exists with this name.
				showErrorText("Exercise name is already in use.");
				return false;
			}
			
			//Update our name
			mExercise.setName(enteredText);
			mExercise.update();
			
			//Call our parent so it knows to update.
			getTargetFragment().onActivityResult(REQUEST_RENAME_EXERCISE, RESULT_RENAME_EXERCISE, null);
			
			return true;
		}
		
		
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Enter an updated name for this exercise", "");
			
			//Get the exercise
			mExercise = getExercise();
			
			//Populate the initial name of the exercise with the current name
			setExistingName();
			
			return v;
		}
		
		/*
		 * Gets the exercise referred to in our arguments. Throws runtimeexception
		 * if the exercise doesn't exist.
		 */
		private Exercise getExercise() {
			//Get the argument
			int exerciseId = getArguments().getInt(EXERCISE_ID, -1);
			
			//Get the exercise
			Exercise toRet = LiftLogDBAPI.get(getActivity()).getExerciseById(exerciseId);
			
			if (toRet == null) {
				throw new RuntimeException("Attempting to retrieve an exercise that does not exist. Exercise id is " + exerciseId);
			}
			
			return toRet;
		}
		
		/*
		 * Populates our edit text with the existing routine name.
		 */
		private void setExistingName() {
			//Get our edit text
			EditText nameBox = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			
			//Set its text to the routine name.
			nameBox.setText(mExercise.getName());
			
			//Put our edit text cursor at the end of our string
			nameBox.setSelection(nameBox.getText().length());
		}
	}
	
	
	private class RoutineExerciseCursorLoader extends SQLiteCursorLoader {
		
		public RoutineExerciseCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			//Query the list of runs
			return LiftLogDBAPI.get(getContext()).getRoutineExercisesCursor(mRoutineId);
		}
		
	}
	
	private class RoutineExerciseCursorAdapter extends CursorAdapter {
		
		private RoutineExerciseCursor mRotExCursor;
		
		//ID of image to use in image view button when expanded
		private int EXPANDED_TRUE_IMG_ID = R.drawable.ic_menu_rotate;
		private int EXPANDED_FALSE_IMG_ID = R.drawable.ic_menu_more;
		
		
		public RoutineExerciseCursorAdapter(Context context, RoutineExerciseCursor cursor) {
			super(context, cursor, 0);
			mRotExCursor = cursor;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			//Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			return inflater.inflate(R.layout.t_routine_factory_container, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new RoutineExerciseCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//Get the run for the current row
			RoutineExercise rotEx = ((RoutineExerciseCursor)cursor).getRoutineExercise();
			
			wireTitle(view, rotEx);
			
			setVisibility(view, rotEx);
			wirePositionText(view.findViewById(R.id.t_routine_factory_container_position), rotEx);
			wireSetText(view.findViewById(R.id.t_routine_factory_container_sets), rotEx);
			wireInstructionText((TextView) view.findViewById(R.id.t_routine_factory_container_instructions_body), rotEx);
			wireListeners(view, rotEx);
		}
		
		/*
		 * Sets the text body of our instruction text (if there is any). Hides the text
		 * body portion if there's no instruction text.
		 */
		private void wireInstructionText(TextView instructionTv, RoutineExercise rotEx) {
			
			if (rotEx.getInstruction() != null && rotEx.getInstruction().length() > 0) {
				//There's an instruction.
				
				//Populate it
				instructionTv.setText(rotEx.getInstruction());
				
				//Make sure it's visible
				instructionTv.setVisibility(View.VISIBLE);
			} else {
				//There's no instruction. Make the body gone.
				instructionTv.setVisibility(View.GONE);
			}
		}
		

		/*
		 * Populates the position text labels (left and right)
		 */
		private void wirePositionText(View rotExView, RoutineExercise rotEx) {
			setLabels(rotExView, "Position", rotEx.getPosition()+"");
		}
		
		/*
		 * Populates the position text labels (left and right)
		 */
		private void wireSetText(View rotExView, RoutineExercise rotEx) {
			setLabels(rotExView, "Sets", rotEx.getNumSets()+"");
		}
		
		/*
		 * Sets label with the provided values. Both set and position can use this.
		 */
		private void setLabels(View relativeRoot, String leftLabel, String rightLabel) {
			//Get a reference to our left label
			TextView leftTv = (TextView) relativeRoot.findViewById(R.id.t_routine_factory_bodyrow_leftText);
			leftTv.setText(leftLabel);
			
			//Get a reference to our right label.
			TextView rightTv = (TextView) relativeRoot.findViewById(R.id.t_routine_factory_bodyrow_right_text);
			rightTv.setText(rightLabel);
		}
		
		/*
		 * Sets the visibility of the body and divider based on the value in our routine exercise.
		 */
		private void setVisibility(View rotExView, RoutineExercise rotEx) {
			//Get the two views we need to manipulate
			View bodyView = rotExView.findViewById(R.id.t_routine_factory_container_lay_body);
			
			//Get the iamge view of the expand button
			ImageView expandButton = (ImageView) rotExView.findViewById(R.id.t_routine_factory_container_header_expand);
			
			if (rotEx.isExpanded()) {
				//It should be visible.
				bodyView.setVisibility(View.VISIBLE);
				
				//Set our imageView
				expandButton.setImageResource(EXPANDED_TRUE_IMG_ID);
			} else {
				//It should be invisible.
				bodyView.setVisibility(View.GONE);
				
				//Set our imageView
				expandButton.setImageResource(EXPANDED_FALSE_IMG_ID);
			}
		}
		
		/*
		 * Adds onclick listeners for all four of the image views.
		 */
		private void wireListeners(View view, RoutineExercise rotEx) {
			
			//Get references to our relative layouts holding the labels.
			View positionRel = view.findViewById(R.id.t_routine_factory_container_position);
			View setRel = view.findViewById(R.id.t_routine_factory_container_sets);

			
			/*
			 * Get position image views
			 */
			
			//Get the two image views from mit
			ImageView posMinus = (ImageView) positionRel.findViewById(R.id.t_routine_factory_bodyrow_right_minus);
			ImageView posPlus = (ImageView) positionRel.findViewById(R.id.t_routine_factory_bodyrow_right_plus);
			
			/*
			 * Get set image views
			 */
			//Set view box holds the right side box that ocntains the 2 IV and 1 TV
			ImageView setMinus = (ImageView) setRel.findViewById(R.id.t_routine_factory_bodyrow_right_minus);
			ImageView setPlus = (ImageView) setRel.findViewById(R.id.t_routine_factory_bodyrow_right_plus);
			
			/*
			 * Get teh expanded image view
			 */
			ImageView setExpanded = (ImageView) view.findViewById(R.id.t_routine_factory_container_header_expand);
			
			/*
			 * Get the entire body that contains the instructions component. We'll make all of it an onclick listener.
			 */
			View instructionBox = view.findViewById(R.id.t_routine_factory_container_lay_instructions);
			
			//Construct our onclick listener object
			ExerciseClickWrapper clickWrapper = new ExerciseClickWrapper(rotEx, setMinus, setPlus, posMinus, posPlus, setExpanded, instructionBox);
			
			//Add it as our listener for every single one
			registerOnClickListener(posMinus, clickWrapper);
			registerOnClickListener(posPlus, clickWrapper);
			registerOnClickListener(setMinus, clickWrapper);
			registerOnClickListener(setPlus, clickWrapper);
			registerOnClickListener(setExpanded, clickWrapper);
			registerOnClickListener(instructionBox, clickWrapper);
		}
		
		private void registerOnClickListener(View toListen, ExerciseClickWrapper clickWrapper) {
			toListen.setOnClickListener(clickWrapper);
		}
		
		/*
		 * Wires up the exercise title.
		 */
		private void wireTitle(View rotExView, RoutineExercise rotEx) {
			//Get the name of the exercise that corresponds to this routineexercise.
			Exercise parentExercise = mDbHelper.getRotExSuperEx(rotEx);
			
			//Get the title text view.
			TextView exTitleTv = (TextView) rotExView.findViewById(R.id.t_routine_factory_container_header_name);
			
			//Set the title text view to our exercise. Basing name on whether it's expanded or not.
			if (rotEx.isExpanded()) {
				//We're expanded, don't include detialsa bout the number of sets.
				exTitleTv.setText(parentExercise.getName());
			} else {
				//Not expanded. Add detailsa bout set count.
				
				String setTerm; //Holds the pluralization of our set.
				
				if (rotEx.getNumSets() == 1) {
					setTerm = "set";
				} else {
					setTerm = "sets";
				}
				
				exTitleTv.setText(rotEx.getNumSets() + " " + setTerm + ": " + parentExercise.getName());
			}
		}
		
		
	}
	

	public static class ConfirmDeleteRotEx extends ConfirmationDialog {
		
		View mInflatedLayout;
		private static final String FRAGMENT_ROUTINE_EXERCISE_ID = "com.plined.liftlog.confirmdeleterotex.fragmentroutineid";
		
		public static ConfirmDeleteRotEx newInstance(int routineExerciseId) {
			Bundle args = new Bundle();
			args.putInt(FRAGMENT_ROUTINE_EXERCISE_ID, routineExerciseId);
			
			ConfirmDeleteRotEx fragment = new ConfirmDeleteRotEx();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Remove Exercise";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get the id of the routine exercise to delete
			int idToDel = getRoutineExerciseId();
			
			//get the associated routine exercise
			RoutineExercise toDel = LiftLogDBAPI.get(getActivity()).getRoutineExerciseById(idToDel);
			
			if (toDel == null) {
				throw new RuntimeException("Attempting to delete routine exercise, but its result is null. ID is " + idToDel);
			}
			
			//Delete it
			toDel.delete();
			
			//Call onactivityresult so it knows to update.
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_DELETE_EXERCISE, null);
			
			return true;
		}
		
		/*
		 * Returns the routine ID passed to this confirm dialog.
		 */
		private int getRoutineExerciseId() {
			//Get our arguments
			Bundle args = getArguments();
			
			//Parse out the routine ID
			int rotId = args.getInt(FRAGMENT_ROUTINE_EXERCISE_ID, -1);
			
			//Check that we got a rotid
			if (rotId == -1) {
				//Didn't get ar otId
				throw new RuntimeException("Attempting to delete a routine exercise with routine exercise ID -1.");
			}
			
			//Return it
			return rotId;
			
		}
		
		public String getPositiveText() {
			return "Yes";
		}
		
		public String getNegativeText() {
			return "No";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_confirm, null);
			
			//Popuilate our text boxes.
			setConfirmTopText("Are you sure you want to remove the selected exercise from this routine?");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
	}

	public class ExerciseClickWrapper implements View.OnClickListener {
		RoutineExercise mRoutineExercise;
		ImageView mSetMinus;
		ImageView mSetPlus;
		ImageView mPositionMinus;
		ImageView mPositionPlus;
		ImageView mSetVisibility;
		View mInstructionBox;
		
		
		public ExerciseClickWrapper(RoutineExercise routine, ImageView setMin, ImageView setPlu, ImageView posMin, ImageView posPlu, ImageView setVisibility, View instructionBox) {
			mRoutineExercise = routine;
			mSetMinus = setMin;
			mSetPlus = setPlu;
			mPositionMinus = posMin;
			mPositionPlus = posPlu;
			mSetVisibility = setVisibility;
			mInstructionBox = instructionBox;
		}
		
		@Override
		public void onClick(View v) {
			//Figure out which imageview was clicked
			if (v == mSetMinus) {
				mRoutineExercise.decrementSetCount();
			} else if (v == mSetPlus) {
				mRoutineExercise.incrementSetCount();
			} else if (v == mPositionMinus) {
				mRoutineExercise.decrementPosition();
			} else if (v == mPositionPlus) {
				mRoutineExercise.incrementPosition();
			} else if (v == mSetVisibility) {
				//Reverse the expanded state
				mRoutineExercise.setExpanded(!mRoutineExercise.isExpanded());
				//Reload our data
			} else if (v == mInstructionBox) {
				//Got an onclick listener on the instruction box.
				showInstructionDialog();
			}
			else {
				throw new RuntimeException("Had an onclick event from an unexpected source.");
			}
			
			//Force our adapter to update with new data.
			mAdapter.reloadData();
		}
		
		/*
		 * Launches an instruction dialog box.
		 */
		private void showInstructionDialog() {
			//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
			InstructionEditDialog fragToUse = InstructionEditDialog.newInstance(mRoutineExercise.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "editsetinstruction", REQUEST_SET_INSTRUCTION);
		}
	}

}

