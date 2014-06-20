package com.plined.liftlog;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.plined.liftlog.LiftLogDBMaster.RoutineCursor;

public class WorkoutBeginFragment extends ListFragment {

	private static String TAG = "RoutineListFragment";
	
	//Key for our SharedPreferences to get the sort method.
	private static String SHAREDPREF_KEY_SORTMETHOD = "com.plined.liftlog.begin_workout_fragment.sort_method";
	
	private RoutineCursorAdapter mAdapter;
	
	private LiftLogDBAPI mDbHelper;
	
	private View mSpinnerContainer;
	
	@Override
	public void onResume() {
		//Reload our data in case we just returned from the routine fragment area.
		mAdapter.reloadData();
		
		//Update our resume button to point to our new workout.
		processResumeButton(getView());
		
		super.onResume();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get a reference to our DB manager
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		//Set this so we'll get callbacks when menu items are hit.
		setHasOptionsMenu(true);
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Inflate the context menu
		getActivity().getMenuInflater().inflate(R.menu.f_workout_begin_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.f_workout_begin_menu_editRoutine: {
			/*
			 * They want to edit this routine. Launch it into the routine manager.
			 */
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out whjich routine we're trying to edit.
			RoutineCursorAdapter adapter = (RoutineCursorAdapter)getListAdapter();
			RoutineCursor rotCursor = (RoutineCursor) adapter.getItem(position);
			Routine rotEdit = rotCursor.getRoutine();
			
			//Launch into the routine manager with its ID
			Intent i = new Intent(getActivity(), RoutineFactoryActivity.class);
			//Putthe routine id in
			i.putExtra(RoutineFactoryActivity.EXTRA_ROUTINE_ID, rotEdit.getId());
			
			//Start the routine factory.
			startActivity(i);
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	/*
	 * Coordinates the entire process of instantiating a workout from
	 * instantiating the routine instance, to filling it with the appropriate
	 * sets and exercises.
	 * 
	 * Returns the RoutineInstance of the created routine instance.
	 */
	private RoutineInstance instantiateWorkout(int routineId) {

		
		//Create our routineInstance and get its ID
		RoutineInstance rotInstance = createRoutineInstance(routineId);
		
		//Create exercsie instances for every exercise in this routine.
		createExerciseInstances(routineId, rotInstance);
		
		//Grab the routine this i s based on and update its last used time.
		setRoutineLastUsed(routineId, new Date());
		
		return rotInstance;
	}
	
	/*
	 * Sets the routine's last used time to the provided parameter.
	 */
	private void setRoutineLastUsed(int routineId, Date lastUsedTime) {
		//Get the routine
		Routine toModify = mDbHelper.getRoutineById(routineId);
		
		//Make sur eit's not null
		if (toModify == null) {
			throw new RuntimeException("Attempted to retrieve routine for date modification, but it's null.");
		}
		
		toModify.setLastUsed(lastUsedTime);
		
	}
	
	/*
	 * Creates an exerciseinstance for every exercise in this routine
	 * and assigns it to this routine instance.
	 */
	private void createExerciseInstances(int routineId, RoutineInstance routineParent) {
		//Get an arraylist of the routineexercises for this routine.
		ArrayList<RoutineExercise> sourceRoutines = mDbHelper.getRoutineExercises(routineId);
		
		int rotParentId = routineParent.getId();
		
		//Iterate through all of the exercises and create an exercise instance for each one
		for (RoutineExercise srcRotEx : sourceRoutines) {
			int exSuper = srcRotEx.getExerciseSuper();
			
			//Create our exercise instance and insert it.
			new ExerciseInstance(getActivity(), exSuper, rotParentId, null, srcRotEx.getPosition(), srcRotEx.getId()).insert();
			
			//Grab the exercise instance now that it's created. Get it based on its routine parent and its exercise super.
			ExerciseInstance insertedInstance = mDbHelper.getExerciseInstance(rotParentId, exSuper);
			
			//Make sure it was created
			if (insertedInstance == null) {
				throw new RuntimeException("Could not retrieve exercise instance immediately after creation. Rot ID is " + rotParentId + " and super is " + exSuper);
			}
			
			//Create the necessary setinstances for this routine and insert them.
			for (int i=1; i <= srcRotEx.getNumSets(); i++) {
				new SetInstance(getActivity(), i, -1, -1, insertedInstance.getId()).insert();
			}
		}
	}
	
	/*
	 * Creates a routine instance based on our ID. Doesn't handle the other tasks
	 * just does the routine instance instantiation.
	 */
	private RoutineInstance createRoutineInstance(int rotSuper) {
		//Store the creation date, we'll need this when finding the routine after.
		Date creationTime = new Date();
		
		//Create the routine instance
		RoutineInstance toInsert = new RoutineInstance(getActivity(), rotSuper, creationTime);
		
		//Inser thte routine instance
		toInsert.insert();
		
		//Find the routine instance
		RoutineInstance insertedRotInstance = mDbHelper.getRoutineInstance(rotSuper, creationTime);
		
		if (insertedRotInstance == null) {
			throw new RuntimeException("Could not retrieve routine instance immediately after creation.");
		}
		
		return insertedRotInstance;
	}
	
	/*
	 * Right now all of the listview initialization stuff happens in onStart().
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		RoutineCursor insCursor = (RoutineCursor) new RoutineCursorLoader(getActivity()).loadCursor();
		mAdapter = new RoutineCursorAdapter(getActivity(), insCursor);
		setListAdapter(mAdapter);
		
		setListViewListener();
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_routine_list, menu);
	}
	
	/*
	 * Sets the sort menu item text (sort by alpha or last useD)
	 * based on the current shared preferences value.
	 */
	protected void setSortMenuText(MenuItem item) {
		if (getSortModePref() == LiftLogDBAPI.ROUTINE_SORT_ALPHA) {
			item.setTitle("Sort by use date");
		} else {
			item.setTitle("Sort alphabetically");
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		setSortMenuText(menu.findItem(R.id.menu_item_sortmode));
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	@TargetApi(11)
	public boolean onOptionsItemSelected(MenuItem item) {			
		
		switch(item.getItemId()) {
		case R.id.menu_item_sortmode:
			//Sort mode has been changed.
			if (getSortModePref() == LiftLogDBAPI.ROUTINE_SORT_ALPHA) {
				//Currently sorting by alpha, swap to list used.
				setSortModePref(LiftLogDBAPI.ROUTINE_SORT_LAST_USED);
			} else {
				//Currently sorting by last used, swap to alpha.
				setSortModePref(LiftLogDBAPI.ROUTINE_SORT_ALPHA);
			}
		
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//	Invalid our menu
				getActivity().invalidateOptionsMenu();
			}
			
			//Reload our data so it fixes sorting.
			mAdapter.reloadData();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	/*
	 * Sets our list sort mode value in the Shared Preferences.
	 */
	protected void setSortModePref(int sortMode) {
		getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(SHAREDPREF_KEY_SORTMETHOD, sortMode).commit();
	}
	
	/*
	 * Gets our list sort mode value from the shared preferences.
	 */
	protected int getSortModePref() {
		return getActivity().getPreferences(Context.MODE_PRIVATE).getInt(SHAREDPREF_KEY_SORTMETHOD, LiftLogDBAPI.ROUTINE_SORT_ALPHA);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		//Make sure our listview doesn't show selections
		getListView().setSelector(android.R.color.transparent);
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_workout_begin, parent, false);

		
		//Make our listview context menu sensitive
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		registerForContextMenu(listView);
		
		//Get our spinenr view
		mSpinnerContainer = v.findViewById(R.id.fragment_workout_begin_progressContainer);
		
		return v;
	}

	
	/*
	 * Determines whether a resume button should be shown. If it should, sets
	 * it to be visible, populates it with the relevant data and adds an onclick
	 * listener to it to launch into the workout fragment.
	 */
	private void processResumeButton(View inflatedLayout) {
		//Get our resume button's layout
		View resumeButton = inflatedLayout.findViewById(R.id.fragment_workout_begin_resumeButton);
		
		//Grab our most recent routine instance
		RoutineInstance recentRotInst = mDbHelper.getRecentRotInst();
		
		//If it's null
		if (recentRotInst == null) {
			//No routine instances exist. Hide the button.
			resumeButton.setVisibility(View.GONE);
		}
		//Else
		else {
			//Routine instance exist. Show the button.
			resumeButton.setVisibility(View.VISIBLE);
			
			//Set its onclick listener to launch into our workout viewer.
			resumeButton.setOnClickListener(new ResumeRotInstListener(recentRotInst.getId()));
		}
	}
	
	private class ResumeRotInstListener implements View.OnClickListener {
		int mRotInstId;
		
		public ResumeRotInstListener(int rotInstanceId) {
			mRotInstId = rotInstanceId;
		}
		
		@Override
		public void onClick(View v) {
			launchWorkoutInstance(mRotInstId);
		}
		
		private void launchWorkoutInstance(int routineInstanceId) {
			Intent i = new Intent(getActivity(), WorkoutInstanceActivity.class);
			//Putthe routine id in
			i.putExtra(WorkoutInstanceActivity.EXTRA_ROUTINE_INSTANCE_ID, routineInstanceId);
			
			//Start the routine factory.
			startActivity(i);
		}
		
	}
	
	/*
	 * Sets up a listener on our listview that launches the routine manager when a routine is clicked.
	 */
	private void setListViewListener() {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "Workout count is " + mDbHelper.getWorkoutCount());
				if (mDbHelper.getWorkoutCount() < 10 || LicenseManager.staticHasPro(getActivity())) {
					//Get the routine
					RoutineCursor rotCursor = (RoutineCursor) mAdapter.getItem(position);
					
					//Intantiate the workout
					//RoutineInstance newRot = instantiateWorkout(rotCursor.getRoutine().getId());
					CreateRoutineInstanceTask task = new CreateRoutineInstanceTask();
					setSpinnerActive(true);
					task.execute(new Integer[] { rotCursor.getRoutine().getId() });
				} else {
					PurchaseProConfirmDialog toShow = PurchaseProConfirmDialog.newInstance("LiftLog's free edition is limited to 10 workouts.", "To continue, please either delete a past workout in the workout history section, or upgrade to LiftLog Pro. \n\nWould you like to upgrade to pro now?");
					launchDialogFragment(toShow, "purchaseproconfirm");
				}

			}
			
			
		});
	}
	
	/*
	 * Launches the workout with the provided ID.
	 */
	protected void launchWorkoutInstance(int routineInstanceId) {
		Intent i = new Intent(getActivity(), WorkoutInstanceActivity.class);
		//Putthe routine id in
		i.putExtra(WorkoutInstanceActivity.EXTRA_ROUTINE_INSTANCE_ID, routineInstanceId);
		
		//Start the routine factory.
		startActivity(i);
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

	private class RoutineCursorLoader extends SQLiteCursorLoader {
		
		public RoutineCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			//Query the list of runs
			return LiftLogDBAPI.get(getContext()).getAllRoutinesCursor(true, getSortModePref());
		}
		
	}
	
	private class RoutineCursorAdapter extends CursorAdapter {
		
		private RoutineCursor mRoutineCursor;
		
		public RoutineCursorAdapter(Context context, RoutineCursor cursor) {
			super(context, cursor, 0);
			mRoutineCursor = cursor;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			//Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			return inflater.inflate(R.layout.template_routine, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new RoutineCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//Get the routine for the current row
			Routine routine = ((RoutineCursor)cursor).getRoutine();
			
			//Get our textviews
			TextView nameText = (TextView) view.findViewById(R.id.template_routine_name);
			TextView lastUsedText = (TextView) view.findViewById(R.id.template_routine_lastUsed);
			
			//Populate them
			nameText.setText(routine.getName());
			lastUsedText.setText("Last used: " + Utilities.formatDate(routine.getLastUsed()));
		}
		
		
	}
	
	/*
	 * Sets our spinner (and the frame layout) as active/inactive. Used to lock out
	 * screen input while cvreating and loading into a workout.
	 */
	protected void setSpinnerActive(boolean isActive) {
		if (isActive) {
			mSpinnerContainer.setVisibility(View.VISIBLE);
		} else {
			mSpinnerContainer.setVisibility(View.INVISIBLE);
		}
		
	}
	
	private class CreateRoutineInstanceTask extends AsyncTask<Integer, Void, RoutineInstance> {
	
		@Override
		protected RoutineInstance doInBackground(Integer... routineIds) {

			return instantiateWorkout(routineIds[0]);
		}
		
		@Override
		protected void onPostExecute(RoutineInstance createdRotEx) {

			setSpinnerActive(false);
			
			if (getActivity() != null) {
				launchWorkoutInstance(createdRotEx.getId());
			} else {
				//Activity no longer exists. Don't launch the workout.
				//They likely clicked back mid workout creation.
			}
		}
	}
	
	
}
