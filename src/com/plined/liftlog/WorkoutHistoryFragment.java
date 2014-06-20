package com.plined.liftlog;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.plined.liftlog.LiftLogDBMaster.RoutineInstanceCursor;

public class WorkoutHistoryFragment extends ListFragment {
	
	private static String TAG = "WorkoutHistoryFragment";
	
	//Key for our SharedPreferences to get the sort method.
	private static String SHAREDPREF_KEY_SORTMETHOD = "com.plined.liftlog.workouthistoryfragment.sort_method";

	//Key for passing routine instance ID to our delete confirm dialog
	public static String ROUTINE_INSTANCE_ID = "com.perryb.liftlog.workouthistoryfragment.routine_instance_id";
	
	
	//sort method keys
	private static int SORTMETHOD_DATE_ASC = 1;
	private static int SORTMETHOD_DATE_DESC = 2;
	private static int SORTMETHOD_ROUTINE_ASC = 3;
	private static int SORTMETHOD_ROUTINE_DESC = 4;
	
	//Ints that refer to the image views to be used in descending and ascending cases.
	private static int SORT_ASCENDING_IMGVIEW = R.drawable.arrowup_whitecircle;
	private static int SORT_DESCENDING_IMGVIEW = R.drawable.arrowdown_whitecircle;
	

	//Request/Result for deletion dialog.
	protected static int REQUEST_DELETE_ROUTINE_INSTANCE = 1;
	
	protected static int RESULT_DELETE_ROUTINE_INSTANCE = 1;
	
	
	private RoutineInstanceCursorAdapter mAdapter;
	
	private LiftLogDBAPI mDbHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get a reference to our DB manager
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		//Set this so we'll get callbacks when menu items are hit.
		setHasOptionsMenu(true);
	}
	
	/*
	 * Right now all of the listview initialization stuff happens in onStart().
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		RoutineInstanceCursor insCursor = (RoutineInstanceCursor) new RoutineInstanceCursorLoader(getActivity()).loadCursor();
		mAdapter = new RoutineInstanceCursorAdapter(getActivity(), insCursor);
		setListAdapter(mAdapter);
		
		setListViewListener();
		
	}
	
	/*
	 * Processes returns from our dialogs.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DELETE_ROUTINE_INSTANCE) {
			//Routine instance has been deleted. Reload data.
			mAdapter.reloadData();
		}
		else {
			//Unexpected code returned.
			throw new RuntimeException("Received unexpected request and/or result code. Request code is " + requestCode + " result code is " + resultCode);
		}
	}
	
	
	/*
	 * Sets our list sort mode value in the Shared Preferences.
	 */
	protected void setSortModePref(int sortMode) {
		getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(SHAREDPREF_KEY_SORTMETHOD, sortMode).commit();
		
		//Update our image view now that th esort mode has changed.
		updateSortImgView();
		
		//Now that the sorting method has changed, we need to update our adapter.
		mAdapter.reloadData();
	}
	
	/*
	 * Gets our list sort mode value from the shared preferences.
	 */
	protected int getSortModePref() {
		return getActivity().getPreferences(Context.MODE_PRIVATE).getInt(SHAREDPREF_KEY_SORTMETHOD, SORTMETHOD_DATE_DESC);
	}
	
	/*
	 * Updates the sorting image view based on the curent sort more preference.
	 */
	private void updateSortImgView() {
		//Get our two image views
		ImageView dateImgView = (ImageView) getView().findViewById(R.id.f_workout_history_dateImgView);
		ImageView routineImgView = (ImageView) getView().findViewById(R.id.f_workout_history_routineImgView);
		
		int curSortMode = getSortModePref();
		
		if (curSortMode == SORTMETHOD_DATE_ASC) {
			//Should have up arrow beside date.
			dateImgView.setImageResource(SORT_ASCENDING_IMGVIEW);
			routineImgView.setImageResource(0);
		} else if (curSortMode == SORTMETHOD_DATE_DESC) {
			//Should have down arrow beside date.
			dateImgView.setImageResource(SORT_DESCENDING_IMGVIEW);
			routineImgView.setImageResource(0);
		} else if (curSortMode == SORTMETHOD_ROUTINE_ASC) {
			//Should have up arrow beside routine
			dateImgView.setImageResource(0);
			routineImgView.setImageResource(SORT_ASCENDING_IMGVIEW);
		} else if (curSortMode == SORTMETHOD_ROUTINE_DESC) {
			//Should have down arrow beside routine.
			dateImgView.setImageResource(0);
			routineImgView.setImageResource(SORT_DESCENDING_IMGVIEW);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		//Make sure our listview doesn't show selections
		getListView().setSelector(android.R.color.transparent);
		
		//Update our sorting imgview
		updateSortImgView();
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_workout_history, parent, false);
		v.setBackgroundResource(R.color.newTempBg);

		
		//Make our listview context menu sensitive
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		registerForContextMenu(listView);
		
		//Create our o nclick listener on date and routine for sorting
		configSortingListener(v);
		
		return v;
	}
	
	/*
	 * Configures the onclick listeners on date and routine so they result in the sorting method being changed.
	 */
	private void configSortingListener(View inflatedLayout) {
		//Get our two relative layouts.
		View routineRel = inflatedLayout.findViewById(R.id.f_workout_history_routineRel);
		View dateRel = inflatedLayout.findViewById(R.id.f_workout_history_dateRel);
		
		routineRel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * If we're currently sorting by routine descending, we should start sorting by routine ascending.
				 * Otherwise we should start sorting by routine descending.
				 */
				int curSortMode = getSortModePref();
				
				if (curSortMode == SORTMETHOD_ROUTINE_DESC) {
					setSortModePref(SORTMETHOD_ROUTINE_ASC);
				} else {
					setSortModePref(SORTMETHOD_ROUTINE_DESC);
				}
			}
		});
		
		dateRel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * If we're currently sorting by date descending, we should start sorting by date ascending.
				 * Otherwise we should start sorting by date descending.
				 */
				int curSortMode = getSortModePref();
				
				if (curSortMode == SORTMETHOD_DATE_DESC) {
					setSortModePref(SORTMETHOD_DATE_ASC);
				} else {
					setSortModePref(SORTMETHOD_DATE_DESC);
				}
			}
		});
	}
	
	/*
	 * Sets up a listener on our listview that launches the routine manager when a routine is clicked.
	 */
	private void setListViewListener() {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Get the routine
				RoutineInstanceCursor rotInstanceCursor = (RoutineInstanceCursor) mAdapter.getItem(position);
				
				RoutineInstance toLaunch = rotInstanceCursor.getRoutineInstance();
				
				launchWorkoutInstance(toLaunch.getId());
			}
			
			
			private void launchWorkoutInstance(int routineInstanceId) {
				Intent i = new Intent(getActivity(), WorkoutInstanceActivity.class);
				//Putthe routine id in
				i.putExtra(WorkoutInstanceActivity.EXTRA_ROUTINE_INSTANCE_ID, routineInstanceId);
				
				//Start the routine factory.
				startActivity(i);
			}
			
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Inflate the context menu
		getActivity().getMenuInflater().inflate(R.menu.f_workout_history_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		//Figure out if they hit the delete button
		switch(item.getItemId()) {
		case R.id.f_workout_history_deleteWorkout: {
			//Trying to delete.
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out which Routine Instance we're trying to delete.
			RoutineInstanceCursorAdapter adapter = (RoutineInstanceCursorAdapter)getListAdapter();
			RoutineInstanceCursor rotInsCursor = (RoutineInstanceCursor) adapter.getItem(position);
			RoutineInstance rotInsDel = rotInsCursor.getRoutineInstance();
			
			//Launch a delete confirm dialog.
			//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
			DeleteRoutineInstanceConfirmDialog fragToUse = DeleteRoutineInstanceConfirmDialog.newInstance(rotInsDel.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "deleteroutineinstanceconfirm", REQUEST_DELETE_ROUTINE_INSTANCE);
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
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

	private class RoutineInstanceCursorLoader extends SQLiteCursorLoader {
		
		public RoutineInstanceCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			//Figure out what our order by is
			String orderBy;
			
			//Get our current sort mode
			int curSortMode = getSortModePref();
			
			if (curSortMode == SORTMETHOD_DATE_ASC) {
				orderBy = LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE + " ASC";
			} else if (curSortMode == SORTMETHOD_DATE_DESC) {
				orderBy = LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE + " DESC";
			} else if (curSortMode == SORTMETHOD_ROUTINE_ASC) {
				orderBy = LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER + " ASC, " + LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ID + " DESC";
			} else if (curSortMode == SORTMETHOD_ROUTINE_DESC) {
				orderBy = LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ROUTINE_SUPER + " DESC, " + LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_ID + " DESC";	
			} else {
				throw new RuntimeException("orderBy string is unexpectedly null.");
			}
			
			return LiftLogDBAPI.get(getContext()).getAllRoutineInstancesCursor(orderBy);
		}
		
	}
	
	private class RoutineInstanceCursorAdapter extends CursorAdapter {
		
		private RoutineInstanceCursor mRoutineInstanceCursor;
		
		public RoutineInstanceCursorAdapter(Context context, RoutineInstanceCursor cursor) {
			super(context, cursor, 0);
			mRoutineInstanceCursor = cursor;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			//Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			return inflater.inflate(R.layout.template_routine_instance, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new RoutineInstanceCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//Get the routine for the current row
			RoutineInstance routineInstance = ((RoutineInstanceCursor)cursor).getRoutineInstance();
			
			//Get our textviews
			TextView dateText = (TextView) view.findViewById(R.id.t_routine_instance_date);
			TextView timeText = (TextView) view.findViewById(R.id.t_routine_instance_dateTime);
			TextView rotNameText = (TextView) view.findViewById(R.id.t_routine_instance_rotName);
			
			//Get the routine super
			Routine routineSuper = mDbHelper.getRoutineById(routineInstance.getRoutineSuper());
			
			//Populate them
			dateText.setText(Utilities.formatDateOnly(routineInstance.getDate()));
			timeText.setText(Utilities.formatTimeOnly(routineInstance.getDate()));
			rotNameText.setText(routineSuper.getName());
		}
		
		
	}
	
	
	public static class DeleteRoutineInstanceConfirmDialog extends ConfirmationDialog {
		
		View mInflatedLayout;
		
		public static DeleteRoutineInstanceConfirmDialog newInstance(int routineInstanceId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_INSTANCE_ID, routineInstanceId);
			
			DeleteRoutineInstanceConfirmDialog fragment = new DeleteRoutineInstanceConfirmDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Delete Workout";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get the id of the routine instance to delete
			int idToDel = getRoutineInstanceId();
			
			LiftLogDBAPI dbHelper = LiftLogDBAPI.get(getActivity());
			
			//Get the routine instance
			RoutineInstance toDelete = dbHelper.getRoutineInstanceById(idToDel);
			
			//Get all of its exercise instances
			ArrayList<ExerciseInstance> exIntDel = dbHelper.getExerciseInstances(toDelete.getId());
			
			//For every exercsie instance, delete all of its set instances, then delete itself.
			for (ExerciseInstance exInt: exIntDel) {
				/*
				 * Get and delete set instances.
				 */
				
				ArrayList<SetInstance> setDel = dbHelper.getSetInstances(exInt.getId());
				
				for (SetInstance setInst : setDel) {
					setInst.delete();
				}
				
				/*
				 * Delete exercise instance.
				 */
				exInt.delete();
			}
			
			//Delete the routine instance
			toDelete.delete();
			
			//Call onactivityresult so it knows to update.
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_DELETE_ROUTINE_INSTANCE, null);
			
			return true;
		}
		
		/*
		 * Returns the routine instance ID passed to this confirm dialog.
		 */
		private int getRoutineInstanceId() {
			//Get our arguments
			Bundle args = getArguments();
			
			//Parse out the routine ID
			int rotId = args.getInt(ROUTINE_INSTANCE_ID, -1);
			
			//Check that we got a rotid
			if (rotId == -1) {
				//Didn't get ar otId
				throw new RuntimeException("Attempting to delete a routine instance with ID -1.");
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
			setConfirmTopText("Are you sure you want to delete this workout?");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
	}
	
}
