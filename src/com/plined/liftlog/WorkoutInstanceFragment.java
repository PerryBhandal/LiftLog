package com.plined.liftlog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.plined.liftlog.LiftLogDBMaster.ExerciseInstanceCursor;
import com.plined.liftlog.RoutineFactoryFragment.InstructionEditDialog;



public class WorkoutInstanceFragment extends ListFragment {
	
	private static String TAG = "WorkoutInstanceFragment";
	
	public static String ROUTINE_INSTANCE_ID = "com.perryb.liftlog.workoutinstancefragment.routineinstance_id";
	
	/*
	 * Result and request keys.
	 */
	private static final int REQUEST_ADD_COMMENT = 1;
	private static final int REQUEST_DELETE_EXERCISE = 2;
	private static final int REQUEST_CHANGE_SET_COUNT = 3;
	private static final int REQUEST_ADD_EXERCISE = 4;
	private static final int REQUEST_SET_INSTRUCTION = 5;
	private static final int REQUEST_SET_DATE = 6;
	
	private static final int RESULT_ADD_COMMENT = 1;
	private static final int RESULT_DELETE_EXERCISE = 2;
	private static final int RESULT_CHANGE_SET_COUNT = 3;
	private static final int RESULT_ADD_EXERCISE = 4;
	private static final int RESULT_SET_INSTRUCTION = 5;
	private static final int RESULT_SET_DATE = 6;
	
	//Key to store the exercise instance when passed to our comment dialog.
	private static String EXERCISE_INSTANCE_ID = "com.plined.liftlog.workoutinstancefragment.commenteditdialog.exerciseinstanceid";
	
	protected GUITimer mGuiTimer;
	
	//stores the routine instance of this workout.
	protected RoutineInstance mRoutineInstance;
	
	private ExerciseInstanceCursorAdapter mAdapter;
	private View mHeaderView;
	
	private View mInflatedLayout;
	
	protected LiftLogDBAPI mDbHelper;
	
	public static WorkoutInstanceFragment newInstance(int routineInstanceId) {
		Bundle args = new Bundle();
		args.putInt(ROUTINE_INSTANCE_ID, routineInstanceId);
		
		WorkoutInstanceFragment fragment = new WorkoutInstanceFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public void onResume() {
		mAdapter.reloadData();
		super.onResume();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Set this so we'll get callbacks when menu items are hit.
		setHasOptionsMenu(true);
		
		//Get a reference to our DB helper
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		//Get our routienInstanceId ID out
		int routineInstanceId = getRoutineInstanceArg(getArguments());
		
		//Get the routine instance from the db
		mRoutineInstance = mDbHelper.getRoutineInstanceById(routineInstanceId);
		
		if (mRoutineInstance == null) {
			throw new RuntimeException("Routine instance is null on retrieval.");
		}
		
		setRetainInstance(true);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (mAdapter == null) {
			/*
			 * We're starting. Initialize everything.
			 */
			//Initialize our list's adapter.
			ExerciseInstanceCursor insCursor = (ExerciseInstanceCursor) new ExerciseInstanceCursorLoader(getActivity()).loadCursor();
			mAdapter = new ExerciseInstanceCursorAdapter(getActivity(), insCursor);

			//Add the header to the listview
			
			
			mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.t_workout_instance_header, null);
			getListView().addHeaderView(mHeaderView);
			
			configureHeaderView();
			
			//Make sure our listview doesn't show selections
			getListView().setSelector(android.R.color.transparent);
			
			setListAdapter(mAdapter);
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	/*
	 * Pulls the routine_id from our fragment bundle and returns it.
	 * Returns -1 for the routineID if there were no arguments included.
	 */
	private int getRoutineInstanceArg(Bundle fragArguments) {
		if (fragArguments == null) {
			//No routine ID provided.
			return -1;
		}
		
		int rotId = fragArguments.getInt(ROUTINE_INSTANCE_ID);
		
		return rotId;
		
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Inflate the context menu
		getActivity().getMenuInflater().inflate(R.menu.workout_instance_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		//Figure out if they hit the delete button
		switch(item.getItemId()) {
		case R.id.menu_item_delete_exercise_instance: {
			ExerciseInstance exInst = getClickedExInt(item);
			
			//Launch our confirmation menu
			DeleteExerciseConfirmDialog fragToUse = DeleteExerciseConfirmDialog.newInstance(exInst.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "deleteexerciseconfirm", REQUEST_DELETE_EXERCISE);
			
			return true;
		}
		case R.id.menu_item_search_exercise_youtube: {
			/*
			 * /Wanting to search this exercise on YOuTube
			 */
			if (LicenseManager.staticHasPro(getActivity())) {
				ExerciseInstance exInst = getClickedExInt(item);
				
				//Get the exercise namoe
				Exercise exToSearch = mDbHelper.getExerciseById(exInst.getExerciseSuper());
				String exerciseName = exToSearch.getName();

				
				//Open YT
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + exerciseName)));
			} else {
				PurchaseProConfirmDialog toShow = PurchaseProConfirmDialog.newInstance("This feature requires LiftLog Pro.", "Would you like to upgrade now?");
				launchDialogFragment(toShow, "purchaseproconfirm");
			}
				
			return true;
		}
		case R.id.menu_item_change_set_count: {
			/*
			 * Want to modify set count.
			 */
			ExerciseInstance exInst = getClickedExInt(item);
			
			//Launch our confirmation menu
			ChangeSetCountDialog fragToUse = ChangeSetCountDialog.newInstance(exInst.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "changesetcount", REQUEST_CHANGE_SET_COUNT);
			
			return true;
		}
		case R.id.menu_item_edit_exercise_instruction: {
			/*
			 * Want to modify/add instructions for this exercise.
			 */
			ExerciseInstance exInst = getClickedExInt(item);
			
			if (exInst.getRoutineExerciseParent() == -1) {
				//This is a one time use. Display the notification dialog.
				SingleExerciseNoEdit fragToUse = new SingleExerciseNoEdit();
				
				//Launch this fragment.
				launchDialogFragment(fragToUse, "singleexnoedit");
				return true;
			}
			
			//Get the associated routine exercise
			RoutineExercise parentRotEx = mDbHelper.getRoutineExerciseById(exInst.getRoutineExerciseParent());
			
			if (parentRotEx == null) {
				//The parent routine exercise now null. Dsiplay a notification to that effect.
				RoutineExDeletedNoEdit fragToUse = RoutineExDeletedNoEdit.newInstance(exInst.getId());
				
				//Launch this fragment.
				launchDialogFragment(fragToUse, "rotexdeletednoedit");
			} else {
				//Launch our routine exercise instruction editor.
				//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
				InstructionEditDialog fragToUse = InstructionEditDialog.newInstance(parentRotEx.getId());
				
				//Launch this fragment.
				launchDialogFragmentTarget(fragToUse, "editsetinstruction", REQUEST_SET_INSTRUCTION);
			}
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	/*
	 * Returns the ExerciseInstance that was clicked to load our context menu.
	 */
	private ExerciseInstance getClickedExInt(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		
		//Figure out which we're trying to modify
		ExerciseInstanceCursorAdapter adapter = (ExerciseInstanceCursorAdapter)getListAdapter();
		ExerciseInstanceCursor exIntCursor = (ExerciseInstanceCursor) adapter.getItem(position-1);
		return exIntCursor.getExerciseInstance();
	}
	
	@Override
	public void onDestroy() {
		//Check if we're finishing. If we are, check if we need to delete this routine.
		if (getActivity().isFinishing()) {
			doInstanceCleanup();
		}
		
		//Get rid of any of our timers
		mGuiTimer.dispose();
		
		super.onDestroy();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_workout_instance, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		ExerciseInstanceCursor exIntCursor;
		
		switch(item.getItemId()) {
		case R.id.menu_item_expand: {
			//They hit the expand collapsae button
			
			//Get our current
			exIntCursor = (ExerciseInstanceCursor) mAdapter.getCursor();
			
			exIntCursor.moveToFirst();
			//Iterate over it and set everyone's expanded state to false
			while (!exIntCursor.isAfterLast()) {
				exIntCursor.getExerciseInstance().setExpanded(true);
			}
			
			//Update our data
			mAdapter.reloadData();
			return true;
		}
		case R.id.menu_item_collapse: {
			//They hit the collapsae button
			
			//Get our current
			exIntCursor = (ExerciseInstanceCursor) mAdapter.getCursor();
			
			exIntCursor.moveToFirst();
			//Iterate over it and set everyone's expanded state to false
			while (!exIntCursor.isAfterLast()) {
				exIntCursor.getExerciseInstance().setExpanded(false);
			}
			
			//Update our data
			mAdapter.reloadData();
			return true;
		}
		case R.id.menu_item_add_exercise: {
			//They hit the add exercise button. Launch the dialog to add exercise.
			AddExerciseDialog fragToUse = AddExerciseDialog.newInstance(mRoutineInstance.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "addexerciseinstance", REQUEST_ADD_EXERCISE);
			
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	/*
	 * This should be called by the hosting activityh when the back button is pressed. If the user presses thge back button
	 * and no workout info has been added, this deletes the workout.
	 */
	public void doInstanceCleanup() {
		/*
		 * TODO: Nothing here now., Make it so if they entered no data it just deletes it.
		 */
	}
	
	/*
	 * Returns true if the routine instance being worked on has any user entered data, be it
	 * a comment in an exercise instance, or a completed set.
	 */
	private boolean routineInstanceHasData() {
		//Get all exercise instances for this routine
		ArrayList<ExerciseInstance> exerciseInstances = mDbHelper.getExerciseInstances(mRoutineInstance.getId());
		
		for (ExerciseInstance exInt : exerciseInstances) {
			//Check if it has a comment
			if (exInt.getComment() != null && !exInt.getComment().equals("")) {
				//They've entered a comment. That's enough data.
				return true;
			}
			
			//Check if any set instances are populated.
			if (exerciseInstanceHasData(exInt)) {
				//Some set instance has data.
				return true;
			}
		}
		
		//If you get here, no exercise instance has a comment, nor does any set instance havee a rep completed.
		return false;
	}
	
	/*
	 * Returns true if any set instance in an exercise instance has a completed rep.
	 */
	private boolean exerciseInstanceHasData(ExerciseInstance exerciseInst) {
		//Get all set instances for this exercise instance
		ArrayList<SetInstance> setInstances = mDbHelper.getSetInstances(exerciseInst.getId());
		
		for (SetInstance setInst : setInstances) {
			//Check if this set has weight or reps defined.
			if (setInst.getWeight() != -1 || setInst.getNumReps() != -1) {
				//Either the set has weights or reps defined. Either way, it's data.
				return true;
			}
		}
		
		//No set has anything in it.
		return false;
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

		if (requestCode == REQUEST_ADD_COMMENT) {
			//Comment has been modified. Reload our data.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_DELETE_EXERCISE) {
			//Exercise has been removed. Reload data.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_CHANGE_SET_COUNT) {
			//Set count has been changed. Reload data.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_ADD_EXERCISE) {
			//Exercise has been added.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_SET_INSTRUCTION) {
			//Instruction has bneen modified.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_SET_DATE) {
			//Date has been updated.
			mRoutineInstance = mDbHelper.getRoutineInstanceById(mRoutineInstance.getId());
			//Update the header
			configureHeaderTextView();
		}
		else {
			//Unexpected code returned.
			throw new RuntimeException("Received unexpected request and/or result code. Request code is " + requestCode + " result code is " + resultCode);
		}
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
		LinearLayout topRow = (LinearLayout) mHeaderView.findViewById(R.id.t_workout_instance_header_rotName);
		configureLabelRow(topRow, "Routine", mDbHelper.getParentRoutine(mRoutineInstance).getName()); 
		
		LinearLayout bottomRow = (LinearLayout) mHeaderView.findViewById(R.id.t_workout_instance_header_rotDate);
		configureLabelRow(bottomRow, "Start Time", Utilities.formatDate(mRoutineInstance.getDate()));
	}
	
	private void configureHeaderOnClick() {
		LinearLayout bottomRow = (LinearLayout) mHeaderView.findViewById(R.id.t_workout_instance_header_rotDate);
		//Get the right side element
		View rightElement = bottomRow.findViewById(R.id.t_workout_instance_header_element_rightText);
		
		//Set the onclick listener
		rightElement.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//They hit the add exercise button. Launch the dialog to add exercise.
				DateTimeSelectDialog fragToUse = DateTimeSelectDialog.newInstance(mRoutineInstance.getId());
				
				//Launch this fragment.
				launchDialogFragmentTarget(fragToUse, "setroutineinstancedatetime", REQUEST_SET_DATE);
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
		if (mInflatedLayout == null) {
			mInflatedLayout = inflater.inflate(R.layout.f_workout_instance, parent, false);
			
			//Make our listview context menu sensitive
			ListView listView = (ListView) mInflatedLayout.findViewById(android.R.id.list);
			registerForContextMenu(listView);
			
			//Set our divider to be non-existent
			listView.setDividerHeight(0);
			
			//Crate our gui timer
			mGuiTimer = new GUITimer(mInflatedLayout.findViewById(R.id.t_timer_bar_lay_root), (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE), getActivity());
		} else {
			//HACK: This works... for some reason. I wish I knew why. If I did the alternative (getListView().getParent()) it was crashing.
			((ViewGroup) mInflatedLayout.getParent()).removeAllViews();
		}
		
		return mInflatedLayout;
	}
	
	

	private class ExerciseInstanceCursorLoader extends SQLiteCursorLoader {
		
		public ExerciseInstanceCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			//Query the list of runs
			return LiftLogDBAPI.get(getContext()).getExerciseInstancesCursor(mRoutineInstance.getId());
		}
		
	}
	
	private class ExerciseInstanceCursorAdapter extends CursorAdapter {
		
		/*
		 * Wires up the exercise title.
		 */
		private void wireTitle(TextView exTitleTv, ExerciseInstance exerciseInst, String titlePrefix) {
			//Get the name of the exercise that corresponds to this routineexercise.
			Exercise parentExercise = mDbHelper.getExInstSuperEx(exerciseInst);
			
			
			exTitleTv.setText(titlePrefix + parentExercise.getName());
		}

		private ExerciseInstanceCursor mExIntCursor;
		
		//ID of image to use in image view button when expanded
		private int EXPANDED_TRUE_IMG_ID = R.drawable.ic_menu_rotate;
		private int EXPANDED_FALSE_IMG_ID = R.drawable.ic_menu_more;
		
		public ExerciseInstanceCursorAdapter(Context context, ExerciseInstanceCursor cursor) {
			super(context, cursor, 0);
			mExIntCursor = cursor;
		}
		
		/*
		 * Sets the expand button's image depending on expand state.
		 */
		private void setExpandImg(ImageView buttonView, ExerciseInstance exInst) {
			//Check our state and set imageview accordingly.
			if (exInst.isExpanded()) {
				buttonView.setImageResource(EXPANDED_TRUE_IMG_ID);
			} else {
				buttonView.setImageResource(EXPANDED_FALSE_IMG_ID);
			}
		}
		
		/*
		 * Sets the onclick listener for everything that's exercise centric (does not include set rows,
		 * those are done elsewhere.
		 */
		private void setClickListeners(ExerciseInstance exInst, ImageView buttonView, View commentLayout, View historyView, View instructionView) {
			//Confnigure our on click wrapper
			ExerciseInstanceClickWrapper clickWrapper = new ExerciseInstanceClickWrapper(exInst, buttonView, commentLayout, historyView, instructionView);
			
			//Assign our listener
			buttonView.setOnClickListener(clickWrapper);
			commentLayout.setOnClickListener(clickWrapper);
			historyView.setOnClickListener(clickWrapper);
			instructionView.setOnClickListener(clickWrapper);
		}
		
		/*
		 * Sets the visibility of the provided exercise instance view based on its
		 * exInt's expanded state.
		 */
		private void setInstanceVisibility(View exIntView, ExerciseInstance exInt) {
			View bodyView = exIntView.findViewById(R.id.t_workout_exercise_container_lay_body);
			
			//Check if we need to hide
			if (exInt.isExpanded() == true) {
				//No need to do any hiding.
				bodyView.setVisibility(View.VISIBLE);
			} else {
				//Need to hide the body.
				bodyView.setVisibility(View.GONE);
			}
		}
		
		private View populateView(View baseView, ExerciseInstance exInst) {
			
			addBody(baseView, exInst);
			
			return baseView;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getActivity().getLayoutInflater().inflate(R.layout.t_workout_exercise_container, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new ExerciseInstanceCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ExerciseInstanceCursor exIntCursor = (ExerciseInstanceCursor) cursor;
			ExerciseInstance curInst = exIntCursor.getExerciseInstance();
			
			//Empty out the view's existing children in case it's a convert.
			LinearLayout setBody = (LinearLayout) view.findViewById(R.id.t_workout_exercise_container_lay_setBody);
			setBody.removeAllViews();
			
			//Reset the textview for comments
			TextView instructionTV = (TextView) view.findViewById(R.id.t_workout_exercise_container_comments_body);
			instructionTV.setText("");
			
			//Get and configure the expand button
			ImageView expandButton = (ImageView) view.findViewById(R.id.t_workout_exercise_container_header_expand);
			
			setExpandImg(expandButton, curInst);
			
			//Get our entire comment body view
			View commentBody = view.findViewById(R.id.t_workout_exercise_container_lay_comments);
			
			//get our history view
			View historyView = view.findViewById(R.id.t_workout_exercise_container_lay_exerciseHistory);
			
			//Get our instruction view
			View instructionView = view.findViewById(R.id.t_workout_exercise_container_lay_instruction);
			
			//Set up our listener
			setClickListeners(curInst, expandButton, commentBody, historyView, instructionView);
			
			//Populate the view.
			populateView(view, curInst);
			
			//Hide parts based on its expanded state.
			setInstanceVisibility(view, curInst);
		}
		
		/*
		 * Takes an exercise instance cursor and adds a body row for each setinstance
		 * of this exercise.
		 */
		private void addBody(View baseBody, ExerciseInstance exInt) {
			
			processSets(baseBody, exInt);
			
			populateComments(baseBody, exInt);
			
			handleInstructionVisibility(baseBody, exInt);
			
			
		}
		
		/*
		 * Sets the visibility of our instruction view depending on whether we have an associated instruction
		 * for this exercsie instance.
		 */
		private void handleInstructionVisibility(View baseBody, ExerciseInstance exInt) {
			if (exInt.getRoutineExerciseParent() == -1) {
				//Routine exercise parent is undefined. Means it's a one-time add, definitely invisible.
				setInstructionVisible(baseBody, false);
				return;
			}
			
			//Check if there's an assoicated routine exercise
			RoutineExercise assocRotEx = mDbHelper.getRoutineExerciseById(exInt.getRoutineExerciseParent()); 
			if (assocRotEx == null) {
				//It has no parent. Means the routine exercise it was created from has been deleted.
				setInstructionVisible(baseBody, false);
				return;
			} 
			
			//Has a routine exercise associated with it. Check if that has any instruction attached.
			if (assocRotEx.getInstruction() == null || assocRotEx.getInstruction().equals("")) {
				//Doesn't have an associated instruction.
				setInstructionVisible(baseBody, false);
			} else {
				//Does have an associated instruction.
				setInstructionVisible(baseBody, true);
			}
		}
		
		/*
		 * Sets visbility of the instruction view.
		 */
		private void setInstructionVisible(View baseBody, boolean isVisible) {
			//Get the instruction view
			View instructionView = baseBody.findViewById(R.id.t_workout_exercise_container_lay_instruction);
			
			//Set visibility.
			if (isVisible) {
				instructionView.setVisibility(View.VISIBLE);
			} else {
				instructionView.setVisibility(View.GONE);
			}
		}
		

		/**
		 * @param baseBody
		 * @param exInt
		 */
		private void processSets(View baseBody, ExerciseInstance exInt) {
			//get an arraylist of our sets
			ArrayList<SetInstance> setList = mDbHelper.getSetInstances(exInt.getId());
			
			LinearLayout addTo = (LinearLayout)baseBody.findViewById(R.id.t_workout_exercise_container_lay_setBody);
			
			//Values to store our set counts
			int numSets = setList.size();
			int doneSets = 0;
			
			//Print our sets
			for (SetInstance x : setList) {
				//Do a check and increment our completed set counter.
				if (setIsComplete(x)) {
					doneSets += 1;
				}
				
				addSetRow(addTo, x, exInt);
			}
			
			TextView exTitle = (TextView) baseBody.findViewById(R.id.t_workout_exercise_container_header_name);
			
			String prefix = String.format("[%d/%d] ", doneSets, numSets);
			wireTitle(exTitle, exInt, prefix);
		}

		private void populateComments(View baseBody, ExerciseInstance exInt) {
			//Populate oru comments
			if (exInt.getComment() == null || exInt.getComment().equals("")) {
				//No comment. Hide the comment body.
				baseBody.findViewById(R.id.t_workout_exercise_container_comments_body).setVisibility(View.GONE);
			} else {
				//Havea comment. Make sure it's visible, then populate it.
				baseBody.findViewById(R.id.t_workout_exercise_container_comments_body).setVisibility(View.VISIBLE);
				TextView commentTv = (TextView) baseBody.findViewById(R.id.t_workout_exercise_container_comments_body);
				commentTv.setText(exInt.getComment());
			}
		}
		
		private boolean setIsComplete(SetInstance setInst) {
			//If rep has a value, the set is complete.
			if (setInst.getNumReps() != -1) {
				return true;
			} else {
				return false;
			}
		}
		
		/*
		 * Adds a single set row based on the provided setinstance to the provided view.
		 */
		private View addSetRow(LinearLayout addToLayout, SetInstance setInt, ExerciseInstance exInt) {
			//Inflate our set row
			View added = getActivity().getLayoutInflater().inflate(R.layout.t_workout_exercise_bodyrow, addToLayout, false);
			
			//GEt our three textviews
			TextView setText = (TextView) added.findViewById(R.id.t_workout_exercise_bodyrow_set);
			TextView weightText = (TextView) added.findViewById(R.id.t_workout_exercise_bodyrow_weight);
			TextView repText = (TextView) added.findViewById(R.id.t_workout_exercise_bodyrow_reps);
			
			//Populate our set text
			setText.setText(setInt.getSetNum()+"");

			//Get int values for our weights and reps
			float weight = setInt.getWeight();
			int reps = setInt.getNumReps();
			
			//conver thtem to strings
			String weightStr = getSetStr(weight);
			String repsStr = getSetStr(reps);
			
			//Populate our text view
			weightText.setText(weightStr);
			repText.setText(repsStr);
			
			//Create an onclick listener for it
			added.setOnClickListener(new SetInstanceClickWrapper(setInt));
			
			//Add our setrow to the layout
			addToLayout.addView(added);
			
			return added;
		}
		
		/*
		 * Converts a weight/rep integer into a corresponding string.
		 * Returns "-" if the value provided is -1.
		 */
		private String getSetStr(float inputNum) {
			if (inputNum == -1) {
				return "-";
			} else {
				return StringFormatter.weightToString(inputNum);
			}
		}
			
	}
	
	private class SetInstanceClickWrapper implements View.OnClickListener {
		
		private SetInstance mSetInstance;
		
		private SetInstanceClickWrapper(SetInstance setInst) {
			mSetInstance = setInst;
		}
		
		public void onClick(View v) {
			launchWeightSelection();
		}
		
		
		/*
		 * Launches the weight selection activity with the set ID 
		 * of this set instance included.
		 */
		private void launchWeightSelection() {
			Intent i = new Intent(getActivity(), WeightSelectionActivity.class);
			
			//Putthe routine id in
			i.putExtra(WeightSelectionActivity.EXTRA_SET_ID, mSetInstance.getId());
			
			//Start the routine factory.
			startActivity(i);
		}
		
	}
	
	private class ExerciseInstanceClickWrapper implements View.OnClickListener {
		
		private ExerciseInstance mExerciseInstance;
		private View mExpandingImgView;
		private View mCommentView;
		private View mHistoryView;
		private View mInstructionView;
		
		private ExerciseInstanceClickWrapper(ExerciseInstance exInt, View expImgView, View commentView, View historyView, View instructionView) {
			mExerciseInstance = exInt;
			mExpandingImgView = expImgView;
			mCommentView = commentView;
			mHistoryView = historyView;
			mInstructionView = instructionView;
		}
		
		
		@Override
		public void onClick(View v) {
			if (v == mExpandingImgView) {
				//Invert our expand state
				mExerciseInstance.setExpanded(!mExerciseInstance.isExpanded());
			} else if (v == mCommentView) {
				//Launch the dialog to create a comment.
				startCommentDialog();
			} else if (v == mHistoryView) {
				launchHistory();
			} else if (v == mInstructionView) {
				viewInstruction();
			}
			else {
				throw new RuntimeException("Had onclick response from unexpected source.");
			}
			
			mAdapter.reloadData();
		}
		
		/*
		 * Launches the instruction view dialog box for this exercise instance.
		 */
		private void viewInstruction() {
			//They hit the add exercise button. Launch the dialog to add exercise.
			ViewInstructionDialog fragToUse = ViewInstructionDialog.newInstance(mExerciseInstance.getRoutineExerciseParent());
			
			//Launch this fragment.
			launchDialogFragment(fragToUse, "viewexerciseinstruction");
		}
		
		/*
		 * Starts the comment dialog box for this exercise.
		 */
		private void startCommentDialog() {
			//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
			CommentEditDialog fragToUse = CommentEditDialog.newInstance(mExerciseInstance.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "editexercisecomment", REQUEST_ADD_COMMENT);
		}
		
		
		/*
		 * Launches the history activity with this exercise as our ID.
		 */
		private void launchHistory() {
			Intent i = new Intent(getActivity(), ExerciseHistoryActivity.class);
			
			i.putExtra(ExerciseHistoryActivity.EXERCISE_ID, mExerciseInstance.getExerciseSuper());
			i.putExtra(ExerciseHistoryActivity.EXERCISE_INSTANCE_ID, mExerciseInstance.getId());
			
			startActivity(i);
		}
	}
	
	public static class CommentEditDialog extends NameSelectionDialog {
		
		ExerciseInstance mExerciseInstance;
		
		public static CommentEditDialog newInstance(int exerciseInstanceId) {
			Bundle args = new Bundle();
			args.putInt(EXERCISE_INSTANCE_ID, exerciseInstanceId);
			
			CommentEditDialog fragment = new CommentEditDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Exercise Comment";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get our instruction text
			EditText routineEditText = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
			String enteredText = routineEditText.getText().toString();
			
			//Put it into our routine
			mExerciseInstance.setComment(enteredText);
			
			//Call onactivityresult so it can reload our data
			getTargetFragment().onActivityResult(REQUEST_ADD_COMMENT, RESULT_ADD_COMMENT, null);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Save";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Enter your comments for this exercise", "");
			
			//Get our exercise instance object
			int exIntId = getArguments().getInt(EXERCISE_INSTANCE_ID);
			mExerciseInstance = LiftLogDBAPI.get(getActivity()).getExerciseInstanceById(exIntId);
			
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
			nameBox.setText(mExerciseInstance.getComment());
			
			//Set our text box to be left aligned.
			nameBox.setGravity(Gravity.LEFT);
			
			//Put our edit text cursor at the end of our string
			nameBox.setSelection(nameBox.getText().length());
		}
		
		
	}

	
	public static class DeleteExerciseConfirmDialog extends ConfirmationDialog {
		
		ExerciseInstance mExerciseInstance;
		
		public static DeleteExerciseConfirmDialog newInstance(int exerciseInstanceId) {
			Bundle args = new Bundle();
			args.putInt(EXERCISE_INSTANCE_ID, exerciseInstanceId);
			
			DeleteExerciseConfirmDialog fragment = new DeleteExerciseConfirmDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Remove Exercise";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			
			//Delete the routine
			mExerciseInstance.delete();
			
			//Call onactivityresult so it knows to update.
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_DELETE_EXERCISE, null);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Yes";
		}
		
		public String getNegativeText() {
			return "No";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			super.inflateLayout(inflater, R.layout.dialog_confirm, null);
			
			//Get our exercise instance object
			int exIntId = getArguments().getInt(EXERCISE_INSTANCE_ID);
			mExerciseInstance = LiftLogDBAPI.get(getActivity()).getExerciseInstanceById(exIntId);
			
			//Get the name of the exercise we want to delete.
			Exercise superEx = LiftLogDBAPI.get(getActivity()).getExerciseById(mExerciseInstance.getExerciseSuper());
			String toDeleteName = superEx.getName();
			
			//Popuilate our text boxes.
			setConfirmTopText(String.format("Are you sure you want to remove '%s' from this workout?", toDeleteName));
			setConfirmBottomText("Removing this exercise will only delete it from this specific workout. If you wish to remove it from all future workouts based on this routine, remove the exericse from the routine in the routine manager.");
			
			return mInflatedLayout;
		}
		
		
	}
	
	public static class ChangeSetCountDialog extends UserInteractDialog {
		
		int mNewSetCount;
		int mExIntCurrentSetCount;
		ExerciseInstance mExerciseInstance;
		LiftLogDBAPI mDbHelper;
		TextView mSetText;
		
		//Colors used for coloring our set text.
		String mEqualCountColor = "#000000"; //Used when cur set count and new set count are equal.
		String mPositiveCountColor = "#0b8e6a"; //Green. Used when new set count is higher than cur set count.
		String mNegativeCountColor = "#79292a"; //Red. Used when new set count is lower han cur set count.
		
		public static ChangeSetCountDialog newInstance(int exerciseInstanceId) {
			Bundle args = new Bundle();
			args.putInt(EXERCISE_INSTANCE_ID, exerciseInstanceId);
			
			ChangeSetCountDialog fragment = new ChangeSetCountDialog();
			fragment.setArguments(args);
			
			return fragment;
		}

		@Override
		public View preCreateDialog(LayoutInflater inflater) {
			View v = super.inflateLayout(inflater, R.layout.dialog_edit_setcount, null);
			
			//Init our DB
			mDbHelper = LiftLogDBAPI.get(getActivity());
			
			//Get our exercise instance object
			int exIntId = getArguments().getInt(EXERCISE_INSTANCE_ID);
			mExerciseInstance = mDbHelper.getExerciseInstanceById(exIntId);
			
			//Initialize current set count to the current number for this exercise.
			mExIntCurrentSetCount = mDbHelper.getSetCount(mExerciseInstance);
			
			//New set count starts as equivalent.
			mNewSetCount = mExIntCurrentSetCount;
			
			//Get the textview for the set text
			mSetText = (TextView) v.findViewById(R.id.d_edit_setcount_setCounter);
			
			//Initialize our set text
			updateSetText();
			
			wireButtons(v);
			
			return v;
		}
		
		/*
		 * Wires the onclick listener to our plus and minus buttons.
		 */
		private void wireButtons(View bodyView) {
			//Get our two buttons
			View minusButton = bodyView.findViewById(R.id.d_edit_setcount_minusSet);
			View plusButton = bodyView.findViewById(R.id.d_edit_setcount_plusSet);
			
			//Set minus listener
			minusButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mNewSetCount > 1) {
						mNewSetCount -= 1;
						updateSetText();
					}
				}
			});
			
			//Set plus listener
			plusButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mNewSetCount < 100) {
						mNewSetCount += 1;
						updateSetText();
					}
				}
			});
		}
		
		/*
		 * Updates the set text counter to the current counter.
		 */
		private void updateSetText() {
			//Update the text
			mSetText.setText(mNewSetCount+"");
			
			//Determine its color
			if (mNewSetCount == mExIntCurrentSetCount) {
				//Equal. Set to black.
				mSetText.setTextColor(Color.parseColor(mEqualCountColor));
			} else if (mNewSetCount > mExIntCurrentSetCount) {
				//New count is greater. Set to green.
				mSetText.setTextColor(Color.parseColor(mPositiveCountColor));
			} else if (mNewSetCount < mExIntCurrentSetCount) {
				//New count is lower. Set to red.
				mSetText.setTextColor(Color.parseColor(mNegativeCountColor));
			}
		}
	
		@Override
		protected String getDialogTitle() {
			return "Change Number of Sets";
		}
		
		@Override
		protected void negativeCallback() {
			
		}
		
		@Override
		protected boolean positiveCallback() {
			if (mNewSetCount > mExIntCurrentSetCount) {
				//new set count is greater.
				addSets();
			} else if (mNewSetCount < mExIntCurrentSetCount) {
				//New set count is lower.
				removeSets();
			}
			
			//Verify that we ahve the correct number of sets
			verifySetCount();
			
			//Get the onactivityresult to update
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CHANGE_SET_COUNT, null);

			return true;
		}
		
		/*
		 * Adds the sets necessary to make up the gap between the current
		 * number of sets, and the desired number of sets.
		 */
		private void addSets() {
			//Create the necessary sets
			for (int i=mExIntCurrentSetCount+1; i <= mNewSetCount; i++) {
				//Create the set i nstance and insert it.
				new SetInstance(getActivity(), i, -1, -1, mExerciseInstance.getId()).insert();
			}
			
			//Verify that our final set count is correct.
			verifySetCount();
		}
		
		/*
		 * Removes the sets necessary to get rid of sets above our desired set count.
		 */
		private void removeSets() {
			//Loop over and delete the surplus sets.
			for (int i=mExIntCurrentSetCount; i > mNewSetCount; i--) {
				//Get the set
				SetInstance toDelete = mDbHelper.getSetFromExInst(i, mExerciseInstance);
				
				//Make sure the set we want to delete exists.
				if (toDelete == null) {
					throw new RuntimeException("Retrieved set for deletion that doesn't exist. Trying to retrieve set " + i);
				}
				
				toDelete.delete();
			}
			
			//Verify that our final set count is correct.
			verifySetCount();
		}
		
		/*
		 * Verifies that an exercise's set count in the db is equal to what we intended it to be after modification.
		 */
		private void verifySetCount() {
			//Verify we have the correct number of sets
			int dbSetCount = mDbHelper.getSetCount(mExerciseInstance);
			if (dbSetCount != mNewSetCount) {
				throw new RuntimeException("Set count after modification is incorrect. Expected " + mNewSetCount + " got " + dbSetCount);
			}
		}
	}
	
	public static class ViewInstructionDialog extends UserNotificationDialog {
		
		LiftLogDBAPI mDbHelper;
		View mInflatedLayout;
		
		private static final String ROUTINE_EXERCISE_ID = "com.plined.liftlog.workoutinstancefragment.viewinstructiondialog.routineexerciseid";

		public static ViewInstructionDialog newInstance(int routineExerciseId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_EXERCISE_ID, routineExerciseId);
			
			ViewInstructionDialog fragment = new ViewInstructionDialog();
			fragment.setArguments(args);
			
			return fragment;
		}

		@Override
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_user_notification, null);
			
			//Init our DB
			mDbHelper = LiftLogDBAPI.get(getActivity());
			
			//Get our routine exercise object
			int routineExerciseId = getArguments().getInt(ROUTINE_EXERCISE_ID, -1);
			
			if (routineExerciseId == -1) {
				throw new RuntimeException("Attempting to show instruction for an undefined routine exercise id.");
			}
			
			RoutineExercise parentRotEx = mDbHelper.getRoutineExerciseById(routineExerciseId);

			//Make sure it's not null
			if (parentRotEx == null) {
				throw new RuntimeException("Could not retrieve associated routine exercise.");
			}
			
			configureTextView(parentRotEx);
			
			return mInflatedLayout;
		}
		
		/*
		 * Configures the textviews based on the details from the parent routine exercise.
		 */
		private void configureTextView(RoutineExercise parentRotEx) {
			//Set the header to gone
			mInflatedLayout.findViewById(R.id.dialog_user_notification_header).setVisibility(View.GONE);
			
			//Populate the textview with our instruction.
			TextView bodyText = (TextView) mInflatedLayout.findViewById(R.id.dialog_user_notification_body);
			bodyText.setText(parentRotEx.getInstruction());
		}
	
		@Override
		protected String getDialogTitle() {
			return "Exercise Instructions";
		}
		
	}
	
	public static class SingleExerciseNoEdit extends UserNotificationDialog {
		
		View mInflatedLayout;

		@Override
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_user_notification, null);
			
			configureTextView();
			
			return mInflatedLayout;
		}
		
		/*
		 * Configures the textviews based on the details from the parent routine exercise.
		 */
		private void configureTextView() {
			//Set the header to gone
			TextView headerText = (TextView) mInflatedLayout.findViewById(R.id.dialog_user_notification_header);
			headerText.setText("Instruction can't be modified");
			
			//Populate the textview with our instruction.
			TextView bodyText = (TextView) mInflatedLayout.findViewById(R.id.dialog_user_notification_body);
			bodyText.setText("This exercise was added for a single workout. As a result it does not belong to any routine, so it can't have an instruction added.");
		}
	
		@Override
		protected String getDialogTitle() {
			return "Exercise Instructions";
		}
		
	}
	
	public static class RoutineExDeletedNoEdit extends UserNotificationDialog {
		
		LiftLogDBAPI mDbHelper;
		View mInflatedLayout;
		
		private static final String DIALOG_EXERCISE_INSTANCE_ID = "com.plined.liftlog.workoutinstancefragment.routineexdeletednoedit.exerciseinstanceid";

		public static RoutineExDeletedNoEdit newInstance(int exerciseInstanceId) {
			Bundle args = new Bundle();
			args.putInt(DIALOG_EXERCISE_INSTANCE_ID, exerciseInstanceId);
			
			RoutineExDeletedNoEdit fragment = new RoutineExDeletedNoEdit();
			fragment.setArguments(args);
			
			return fragment;
		}

		@Override
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_user_notification, null);
			
			//Init our DB
			mDbHelper = LiftLogDBAPI.get(getActivity());
			
			ExerciseInstance exInst = getExerciseInstance();
			
			//Get the routine for this exercise instance.
			Routine rotParent = getRoutineFromExInst(exInst);

			configureTextView(rotParent);
			
			return mInflatedLayout;
		}
		
		/*
		 * Returns the Routine that exInst belongs to.
		 */
		private Routine getRoutineFromExInst(ExerciseInstance exInst) {
			//Get the parent rot instance first.
			RoutineInstance rotInstance = mDbHelper.getRoutineInstanceById(exInst.getRoutineInstanceParent());
			
			if (rotInstance == null) {
				throw new RuntimeException("Couldn't get parent rot instance.");
			}
			
			//Get the routine
			Routine parentRot = mDbHelper.getRoutineById(rotInstance.getRoutineSuper());
			
			if (parentRot == null) {
				throw new RuntimeException("Couldn't get routine super.");
			}
			
			return parentRot;
		}
		
		/*
		 * gets the exercise instance referred to in the arguments.
		 */
		private ExerciseInstance getExerciseInstance() {
			//Get our exercise instance
			int exIntId = getArguments().getInt(DIALOG_EXERCISE_INSTANCE_ID, -1);
			
			if (exIntId == -1) {
				throw new RuntimeException("Invalid exercise instance ID " + exIntId);
			}
			
			ExerciseInstance exInst = mDbHelper.getExerciseInstanceById(exIntId);
			
			if (exInst == null) {
				throw new RuntimeException("Exercise instance is null.");
			}
			
			return exInst;
		}
		
		/*
		 * Configures the textviews based on the details from the parent routine exercise.
		 */
		private void configureTextView(Routine rotParent) {
			//Set the header to gone
			TextView headerText = (TextView) mInflatedLayout.findViewById(R.id.dialog_user_notification_header);
			headerText.setText("Instruction can't be modified");
			
			//Populate the textview with our instruction.
			TextView bodyText = (TextView) mInflatedLayout.findViewById(R.id.dialog_user_notification_body);
			bodyText.setText("This exercise was removed from the routine '" + rotParent.getName() + "', as a result it can't have an instruction added or modified.");
		}
	
		@Override
		protected String getDialogTitle() {
			return "Exercise Instructions";
		}
		
	}
	
	public static class AddExerciseDialog extends UserInteractDialog {
		
		int mNewSetCount;
		RoutineInstance mRoutineInstance;
		LiftLogDBAPI mDbHelper;
		TextView mSetText;
		AutoCompleteTextView mExerciseAutoText;
		View mInflatedLayout;

		public static AddExerciseDialog newInstance(int routineInstanceId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_INSTANCE_ID, routineInstanceId);
			
			AddExerciseDialog fragment = new AddExerciseDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		/*
		 * Configures the autocompleteextview to show
		 * exercises.
		 */
		private void wireAutoComplete(View baseView) {
			//Get all exercises
			String[] exerciseNames = mDbHelper.getAllExercisesNames();
			
			ArrayAdapter<String> exAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, exerciseNames);
			
			AutoCompleteTextView textView = (AutoCompleteTextView) baseView.findViewById(R.id.d_add_single_exercise_exNameEt);
			
			textView.setAdapter(exAdapter);
		}

		@Override
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_add_single_exercise, null);
			
			//Init our DB
			mDbHelper = LiftLogDBAPI.get(getActivity());
			
			//Wire up the auto complete textv iew
			wireAutoComplete(mInflatedLayout);
			
			//Get our exercise instance object
			int routineInstanceId = getArguments().getInt(ROUTINE_INSTANCE_ID);
			mRoutineInstance = mDbHelper.getRoutineInstanceById(routineInstanceId);
						
			//New set count starts at 1.
			mNewSetCount = 1;
			
			//Get the textview for the set text
			mSetText = (TextView) mInflatedLayout.findViewById(R.id.d_add_single_exercise_setCounter);
			
			//Get the auto complet textview for the exercise name.
			mExerciseAutoText = (AutoCompleteTextView) mInflatedLayout.findViewById(R.id.d_add_single_exercise_exNameEt);
			
			//get the header text and change it
			TextView headerText = (TextView) mInflatedLayout.findViewById(R.id.d_add_single_exercise_header);
			headerText.setText("Enter the name of exercise you wish to add");
			
			//Initialize our set text
			updateSetText();
			
			wireButtons(mInflatedLayout);
			
			return mInflatedLayout;
		}
		
		/*
		 * Wires the onclick listener to our plus and minus buttons.
		 */
		private void wireButtons(View bodyView) {
			//Get our two buttons
			View minusButton = bodyView.findViewById(R.id.d_add_single_exercise_minusSet);
			View plusButton = bodyView.findViewById(R.id.d_add_single_exercise_plusSet);
			
			//Set minus listener
			minusButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mNewSetCount > 1) {
						mNewSetCount -= 1;
						updateSetText();
					}
				}
			});
			
			//Set plus listener
			plusButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mNewSetCount < 100) {
						mNewSetCount += 1;
						updateSetText();
					}
				}
			});
		}
		
		/*
		 * Updates the set text counter to the current counter.
		 */
		private void updateSetText() {
			//Update the text
			mSetText.setText(mNewSetCount+"");
		}
	
		@Override
		protected String getDialogTitle() {
			return "Add Exercise";
		}
		
		@Override
		protected void negativeCallback() {
			
		}
		
		protected boolean isLongEnough(String routineName, int minNameLength) {
			if (routineName.length() >= minNameLength)
				return true;
			else
				return false;
		}
		
		/*
		 * Populates the error text box. Sets it to visible if it's invisble.
		 */
		protected void showErrorText(String errorToShow) {
			TextView mErrorText = (TextView) mInflatedLayout.findViewById(R.id.dialog_confirm_bottom_text);
			
			//Set the errr text
			mErrorText.setText(errorToShow);
			
			//Change the color to red.
			mErrorText.setTextColor(Color.RED);
		}
		
		/*
		 * Creates the exercise and inserts it into the DB.
		 */
		private void createExercise(String exerciseName) {
			new Exercise(getActivity(), exerciseName).insert();
		}
		
		/*
		 * Checks whether the provided exercise is already in our routine instance
		 */
		private boolean exerciseInRoutineInstance(Exercise toAdd) {
			//Grab all of our exercise instances
			ArrayList<ExerciseInstance> exIntArray = getDBManager().getExerciseInstances(mRoutineInstance.getId());
			for (ExerciseInstance exInt : exIntArray) {
				//check if the exercise is in our routine already
				if (exInt.getExerciseSuper() == toAdd.getId()) {
					//Exercise is in our routine already.
					return true;
				}
			}
			
			//Exercise isn't in our routine.
			return false;
		}
		
		@Override
		protected boolean positiveCallback() {
			//Get our edit text string
			String enteredText = mExerciseAutoText.getText().toString();
			
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
			
			//Make sure it's not already in our routine instance
			if (exerciseInRoutineInstance(toAdd)) {
				showErrorText("The exercise '" + toAdd.getName() + "' is already in this workout.");
				return false;
			}
			
			//Add the exercise and insert it.
			new ExerciseInstance(getActivity(), toAdd.getId(), mRoutineInstance.getId(), null, -1, -1).insert();
			
			//Get the exercise instance back so we can get its id
			ExerciseInstance newExInt = mDbHelper.getExerciseInstance(mRoutineInstance.getId(), toAdd.getId());
			
			
			//Make sure it exists.
			if (newExInt == null) {
				throw new RuntimeException("Can't retrieve exercise instance immediately after creation.");
			}
			
			//Add the necessary set instances
			for (int i=1; i <= mNewSetCount; i++) {
				new SetInstance(getActivity(), i, -1, -1, newExInt.getId()).insert();
			}
			
			//Get the onactivityresult to update
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_ADD_EXERCISE, null);
			
			return true;
		}

	}
	
	public static class DateTimeSelectDialog extends UserInteractDialog {

		
		RoutineInstance mRoutineInstance;
		LiftLogDBAPI mDbHelper;
		View mInflatedLayout;
		Calendar mWorkingDate;
		View mDateHeader;
		View mTimeHeader;
		View mActiveHeaderView;
		View mSetCurrentView;
		DatePicker mDatePicker;
		TimePicker mTimePicker;
		
		private static final String DATETIME_ROUTINE_INSTANCE_ID = "com.plined.liftlog.datetimeselectdialog.routineinstanceid";
		
		public static DateTimeSelectDialog newInstance(int routineInstanceId) {
			Bundle args = new Bundle();
			args.putInt(DATETIME_ROUTINE_INSTANCE_ID, routineInstanceId);
			
			DateTimeSelectDialog fragment = new DateTimeSelectDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		@Override
		protected View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.d_date_time_select, null);
			
			//Init our DB
			mDbHelper = LiftLogDBAPI.get(getActivity());
			
			//Get our exercise instance object
			int routineInstanceId = getArguments().getInt(DATETIME_ROUTINE_INSTANCE_ID);
			mRoutineInstance = mDbHelper.getRoutineInstanceById(routineInstanceId);
			
			//Create the date we'll modify
			mWorkingDate = new GregorianCalendar();
			mWorkingDate.setTime(mRoutineInstance.getDate());
			
			//get our pickers
			mDatePicker = (DatePicker) mInflatedLayout.findViewById(R.id.d_date_time_select_datePicker);
			mTimePicker = (TimePicker) mInflatedLayout.findViewById(R.id.d_date_time_select_timePicker);
			
			//Populate them with initial values
			populatePickers();
			
			wireHeader(mInflatedLayout);
			
			return mInflatedLayout;
		}
		
		/*
		 * Uses the routine instance to populate the date and time pickers with the routine instance's
		 * current date and time.
		 */
		private void populatePickers() {
			//DatePicker
			mDatePicker.updateDate(mWorkingDate.get(Calendar.YEAR), mWorkingDate.get(Calendar.MONTH), mWorkingDate.get(Calendar.DAY_OF_MONTH));
			
			//time picker
			mTimePicker.setCurrentHour(mWorkingDate.get(Calendar.HOUR_OF_DAY));
			mTimePicker.setCurrentMinute(mWorkingDate.get(Calendar.MINUTE));
		}
		
		/*
		 * Wires up the header with the current date and time.
		 */
		private void wireHeader(View inflatedLayout) {
			//Get our objects
			mDateHeader = inflatedLayout.findViewById(R.id.d_date_time_select_header_date);
			mTimeHeader = inflatedLayout.findViewById(R.id.d_date_time_select_header_time);
			mSetCurrentView = inflatedLayout.findViewById(R.id.d_date_time_select_current);
			
			//configure header text views
			TextView timeHeaderText = (TextView) mTimeHeader.findViewById(R.id.t_date_time_select_dialog_headerelement_title);
			timeHeaderText.setText("Time");
			
			//Set our date to our currently active view
			setActiveHeader(mDateHeader);
			
			//Set up our onclick listeners
			wireHeaderOnClick();
		}
		
		/*
		 * Sets the onclick for our headers so we can switch between them.
		 */
		private void wireHeaderOnClick() {
			//Wire date onclick
			mDateHeader.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setActiveHeader(mDateHeader);
				}
			});
			
			mTimeHeader.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setActiveHeader(mTimeHeader);
				}
			});
			
			mSetCurrentView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//Update th etime in our calendar
					setDateTimeCurrent();
					
					//Pass it ont our pickers
					populatePickers();
				}
				
				/*
				 * Sets the date and time in our time picker to the current time.
				 */
				private void setDateTimeCurrent() {
					mWorkingDate = new GregorianCalendar();
					mWorkingDate.setTime(new Date());
				}
			});
		}
		
		/*
		 * Sets the provided view as the active header. This function
		 * - Changes the color of the header.
		 * - Sets it as mActiveView
		 * - Configures visibility of body elements.
		 */
		private void setActiveHeader(View nowActive) {
			if (nowActive != mActiveHeaderView) {
				//Gto a new active header.
				/*
				 * Set both headers to inactive.
				 */
				//Change background color.
				mDateHeader.setBackgroundColor(getActivity().getResources().getColor(R.color.darkBlue));
				mTimeHeader.setBackgroundColor(getActivity().getResources().getColor(R.color.darkBlue));
				
				/*
				 * Set both the date and time pickers to gone
				 */
				mDatePicker.setVisibility(View.GONE);
				mTimePicker.setVisibility(View.GONE);
				
				/*
				 * Set the active header to active.
				 */
				//Set its background color
				nowActive.setBackgroundColor(getActivity().getResources().getColor(R.color.lightBlue));
				
				/*
				 * Set the relevant date/time picker to visible, adn the other to invisible.
				 */
				if (nowActive == mDateHeader) {
					//Make date visible
					mDatePicker.setVisibility(View.VISIBLE);

				} else if (nowActive == mTimeHeader) {
					//Make time visible
					mTimePicker.setVisibility(View.VISIBLE);
				}
			}
		}
		
		@Override
		protected void negativeCallback() {
		}

		@Override
		protected boolean positiveCallback() {
			/*
			 * Update our calendar with current values from our date and time picker
			 */
			
			//Date
			mWorkingDate.set(Calendar.YEAR, mDatePicker.getYear());
			mWorkingDate.set(Calendar.MONTH, mDatePicker.getMonth());
			mWorkingDate.set(Calendar.DAY_OF_MONTH, mDatePicker.getDayOfMonth());

			//Time
			mWorkingDate.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
			mWorkingDate.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
			
			//Update the time to the configured time.
			mRoutineInstance.setDate(mWorkingDate.getTime());
			
			//Have our fragment update the time
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_SET_DATE, null);
			
			return true;
		}

		@Override
		protected String getDialogTitle() {
			return "Edit Date";
		}

	}
}
