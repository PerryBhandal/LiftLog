package com.plined.liftlog;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.plined.liftlog.LiftLogDBMaster.ExerciseInstanceCursor;
import com.plined.liftlog.LiftLogDBMaster.SetInstanceCursor;

public class ExerciseHistorySortableFragment extends ListFragment {

	//Key to store our exercise ID in our bundle
	private static String EXERCISE_ID = "com.plined.liftlog.exercisehistoryfragment.exerciseid";
	
	private static String TAG = "ExerciseHistorySortableFragment";
	
	private Exercise mExercise;
	private LiftLogDBAPI mDbHelper;
	
	private SetInstanceCursorAdapter mAdapter;
	private View mInflatedLayout;
	
	//sort method keys
	private static final int SORTMETHOD_DATE_ASC = 1;
	private static final int SORTMETHOD_DATE_DESC = 2;
	private static final int SORTMETHOD_WEIGHT_ASC = 3;
	private static final int SORTMETHOD_WEIGHT_DESC = 4;
	private static final int SORTMETHOD_REPS_ASC = 5;
	private static final int SORTMETHOD_REPS_DESC = 6;
	
	//Ints that refer to the image views to be used in descending and ascending cases.
	private static int SORT_ASCENDING_IMGVIEW = R.drawable.arrowup_whitecircle;
	private static int SORT_DESCENDING_IMGVIEW = R.drawable.arrowdown_whitecircle;
	
	//Key for our SharedPreferences to get the sort method.
	private static String SHAREDPREF_KEY_SORTMETHOD = "com.plined.liftlog.exercisehistorysortablefragment.sort_method";
	
	
	public static ExerciseHistorySortableFragment newInstance(int exerciseId) {
		//Create our bundle
		Bundle args = new Bundle();
		
		//Put the routine's ID in
		args.putInt(EXERCISE_ID, exerciseId);
		
		//Create our routine fragment
		ExerciseHistorySortableFragment toLaunch = new ExerciseHistorySortableFragment();
		
		//Attach our bundle
		toLaunch.setArguments(args);
		
		return toLaunch;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get our db helper
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		mExercise = getExercise();
		
		setRetainInstance(true);

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (mAdapter == null) {
			/*
			 * We're starting. Initialize everything.
			 */
			//Initialize our list's adapter.
			SetInstanceCursor insCursor = (SetInstanceCursor) new SetInstanceCursorLoader(getActivity()).loadCursor();
			mAdapter = new SetInstanceCursorAdapter(getActivity(), insCursor);


			
			//Make sure our listview doesn't show selections
			getListView().setSelector(android.R.color.transparent);
			
			setListAdapter(mAdapter);
		}
		
		//Make sure our header has the correct image views
		updateSortImgView();
		
		super.onActivityCreated(savedInstanceState);
	}
	
	/*
	 * Sets the text values on our header to the correct values.
	 */
	private void setHeaderTitles(View inflatedLayout) {
		
		View baseLayout = inflatedLayout.findViewById(R.id.f_exercise_history_sortable_lay_header);
		
		TextView weightTv = (TextView) inflatedLayout.findViewById(R.id.t_exercise_history_sortable_header_weight).findViewById(R.id.t_exercise_history_sortable_headerelement_name);
		weightTv.setText("Weight");
		
		TextView repsTv = (TextView) inflatedLayout.findViewById(R.id.t_exercise_history_sortable_header_reps).findViewById(R.id.t_exercise_history_sortable_headerelement_name);
		repsTv.setText("Reps");
		
	}
	
	public void onResume() {
		if (mAdapter != null) {
			//Relaod our data.
			mAdapter.reloadData();
		}
		super.onResume();
	}
	
	/*
	 * Configures the onclick listeners on date and routine so they result in the sorting method being changed.
	 */
	private void configSortingListener(View inflatedLayout) {
		//Get our three relative layouts
		View dateTv = inflatedLayout.findViewById(R.id.t_exercise_history_sortable_header_date);
		View weightTv = inflatedLayout.findViewById(R.id.t_exercise_history_sortable_header_weight);
		View repsTv = inflatedLayout.findViewById(R.id.t_exercise_history_sortable_header_reps);
		
		dateTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * If we're currently sorting by routine descending, we should start sorting by routine ascending.
				 * Otherwise we should start sorting by routine descending.
				 */
				int curSortMode = getSortModePref();

				if (curSortMode == SORTMETHOD_DATE_DESC) {
					setSortModePref(SORTMETHOD_DATE_ASC);
				} else {
					setSortModePref(SORTMETHOD_DATE_DESC);
				}
			}
		});
		
		weightTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * If we're currently sorting by routine descending, we should start sorting by routine ascending.
				 * Otherwise we should start sorting by routine descending.
				 */
				int curSortMode = getSortModePref();
				
				if (curSortMode == SORTMETHOD_WEIGHT_DESC) {
					setSortModePref(SORTMETHOD_WEIGHT_ASC);
				} else {
					setSortModePref(SORTMETHOD_WEIGHT_DESC);
				}
			}
		});
		
		repsTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * If we're currently sorting by routine descending, we should start sorting by routine ascending.
				 * Otherwise we should start sorting by routine descending.
				 */
				int curSortMode = getSortModePref();
				
				if (curSortMode == SORTMETHOD_REPS_DESC) {
					setSortModePref(SORTMETHOD_REPS_ASC);
				} else {
					setSortModePref(SORTMETHOD_REPS_DESC);
				}
			}
		});
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		if (mInflatedLayout == null) {
			mInflatedLayout = inflater.inflate(R.layout.f_exercise_history_sortable, parent, false);
			mInflatedLayout.setBackgroundResource(R.drawable.background);
			
			//Set the text of our headers to teh correct values.
			setHeaderTitles(mInflatedLayout);
			
			//Create our o nclick listener on date and routine for sorting
			configSortingListener(mInflatedLayout);
			
			//Make our listview context menu sensitive
			ListView listView = (ListView) mInflatedLayout.findViewById(android.R.id.list);
			registerForContextMenu(listView);
		} else {
			//HACK: This works... for some reason. I wish I knew why. If I did the alternative (getListView().getParent()) it was crashing.
			((ViewGroup) mInflatedLayout.getParent()).removeAllViews();
		}
		
		return mInflatedLayout;
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
	 * Returns the sorting image view for the provided relative layout.
	 * Used by updateSortImgView to grab the image views for modification.
	 */
	private ImageView getSortView(View layoutRoot) {
		return (ImageView) layoutRoot.findViewById(R.id.t_exercise_history_sortable_headerelement_expand);
	}
	
	/*
	 * Updates the sorting image view based on the curent sort more preference.
	 */
	private void updateSortImgView() {
		//Get the header that all the sort labels resides in.
		View headerView = getView().findViewById(R.id.f_exercise_history_sortable_lay_header);
		
		//Get our three image views
		ImageView dateImgView = getSortView(headerView.findViewById(R.id.t_exercise_history_sortable_header_date));
		ImageView weightImgView = getSortView(headerView.findViewById(R.id.t_exercise_history_sortable_header_weight));
		ImageView repsImgView = getSortView(headerView.findViewById(R.id.t_exercise_history_sortable_header_reps));
		
		//Set them all to empty
		dateImgView.setVisibility(View.INVISIBLE);
		weightImgView.setVisibility(View.INVISIBLE);
		repsImgView.setVisibility(View.INVISIBLE);
		
		int curSortMode = getSortModePref();
		
		switch (curSortMode) {
		case SORTMETHOD_DATE_ASC:
			populateImageView(dateImgView, SORT_ASCENDING_IMGVIEW);
			break;
		case SORTMETHOD_DATE_DESC:
			populateImageView(dateImgView, SORT_DESCENDING_IMGVIEW);
			break;
		case SORTMETHOD_WEIGHT_ASC:
			populateImageView(weightImgView, SORT_ASCENDING_IMGVIEW);
			break;
		case SORTMETHOD_WEIGHT_DESC:
			populateImageView(weightImgView, SORT_DESCENDING_IMGVIEW);
			break;
		case SORTMETHOD_REPS_ASC:
			populateImageView(repsImgView, SORT_ASCENDING_IMGVIEW);
			break;
		case SORTMETHOD_REPS_DESC:
			populateImageView(repsImgView, SORT_DESCENDING_IMGVIEW);
			break;
		}
		
	}
	
	/*
	 * Sets the provided image view to be visible, and sets its source to refsReference.
	 */
	private void populateImageView(ImageView toPopulate, int resReference) {
		toPopulate.setImageResource(resReference);
		toPopulate.setVisibility(View.VISIBLE);
	}
	
	
	/*
	 * Gets our exercise id from the provided arguments, then retrieves the resulting exercise.
	 */
	private Exercise getExercise() {
		Bundle args = getArguments();
		
		//Get our exid out
		int exerciseId = args.getInt(EXERCISE_ID, -1) ;
		
		//Make sure it's valid
		if (exerciseId == -1) {
			throw new RuntimeException("Exercise ID was either not defined, or could not be retrieved.");
		}
		
		//Get the corresponding exercise
		Exercise toRet =  mDbHelper.getExerciseById(exerciseId);
		
		//Make sure we got an exercise back
		if (toRet == null) {
			throw new RuntimeException("Could not retrieve exercise with ID " + exerciseId);
		}
		
		return toRet;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Inflate the context menu
		getActivity().getMenuInflater().inflate(R.menu.exercise_history_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		//Figure out if they hit the delete button
		switch(item.getItemId()) {
		case R.id.menu_item_view_workout: {
			//Trying to delete.
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out which exercise instance we're trying to see.
			SetInstanceCursorAdapter adapter = (SetInstanceCursorAdapter)getListAdapter();
			SetInstanceCursor setInstCursor = (SetInstanceCursor) adapter.getItem(position);
			SetInstance setInst = setInstCursor.getSetInstance();
			
			//Get the exercise instance from the set instance
			ExerciseInstance setParent = mDbHelper.getExerciseInstanceById(setInst.getExerciseInstanceParent());
			
			//Make sure it's valid,.
			if (setParent == null) {
				throw new RuntimeException("Attempted to grab set instance's parent exercise, but it was null. Value is " + setInst.getExerciseInstanceParent());
			}
			
			Intent i = new Intent(getActivity(), WorkoutInstanceActivity.class);
			//Putthe routine id in
			i.putExtra(WorkoutInstanceActivity.EXTRA_ROUTINE_INSTANCE_ID, setParent.getRoutineInstanceParent());
			
			//Start the routine factory.
			startActivity(i);
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}


	

	private class SetInstanceCursorLoader extends SQLiteCursorLoader {
		
		/*
		 * Constants added onto each order type. They're the secondary sorting characteristics.
		 */
		
		public SetInstanceCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			
			//Get our sort mode hjere.
			String orderBy = null;
			int curSortMode = getSortModePref();
			
			switch (curSortMode) {
			case SORTMETHOD_DATE_ASC:
				//Order by date (lowest first), then by set number (highest first). Reason for highestr first is last set is numbered 1. 
				orderBy = String.format("%s ASC, %s DESC", LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE, LiftLogDBMaster.COLUMN_SET_INSTANCE_SET_NUM);
				
				break;
			case SORTMETHOD_DATE_DESC:
				//Order by date (highest first), then by set number (highest first). Reaosn for highest first is last set is numbered 1. 
				orderBy = String.format("%s DESC, %s DESC", LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE, LiftLogDBMaster.COLUMN_SET_INSTANCE_SET_NUM);
				break;
			case SORTMETHOD_WEIGHT_ASC:
				//Order by weight (lowest first), then by reps (lowest first), then by date (oldest first).
				orderBy = String.format("%s ASC, %s ASC, %s ASC", LiftLogDBMaster.COLUMN_SET_INSTANCE_WEIGHT, LiftLogDBMaster.COLUMN_SET_INSTANCE_NUM_REPS, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE);
				break;
			case SORTMETHOD_WEIGHT_DESC:
				//Order by weight (highest first), then by reps (most first), then by date (oldest first).
				orderBy = String.format("%s DESC, %s DESC, %s ASC", LiftLogDBMaster.COLUMN_SET_INSTANCE_WEIGHT, LiftLogDBMaster.COLUMN_SET_INSTANCE_NUM_REPS, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE);
				break;
			case SORTMETHOD_REPS_ASC:
				//Order by reps (lowest first), then by weight (highest first), then by date (oldest first)
				orderBy = String.format("%s ASC, %s DESC, %s ASC", LiftLogDBMaster.COLUMN_SET_INSTANCE_NUM_REPS, LiftLogDBMaster.COLUMN_SET_INSTANCE_WEIGHT, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE);
				break;
			case SORTMETHOD_REPS_DESC:
				//Order by reps (highest first), then by weight (highest first), then by date (oldest first)
				orderBy = String.format("%s DESC, %s DESC, %s ASC", LiftLogDBMaster.COLUMN_SET_INSTANCE_NUM_REPS, LiftLogDBMaster.COLUMN_SET_INSTANCE_WEIGHT, LiftLogDBMaster.COLUMN_ROUTINE_INSTANCE_DATE);
				break;
			}
			//Query the list of runs
			return LiftLogDBAPI.get(getContext()).getSetInstancesCursorForExercise(mExercise.getId(), orderBy);
		}
		
	}
	
	private class SetInstanceCursorAdapter extends CursorAdapter {

		private SetInstanceCursor mSetIntCursor;
		
		
		public SetInstanceCursorAdapter(Context context, SetInstanceCursor cursor) {
			super(context, cursor, 0);
			mSetIntCursor = cursor;
		}
		
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getActivity().getLayoutInflater().inflate(R.layout.t_workout_exercise_bodyrow, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new SetInstanceCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//Get our three textviews
			TextView date = (TextView) view.findViewById(R.id.t_workout_exercise_bodyrow_set);
			TextView weight = (TextView) view.findViewById(R.id.t_workout_exercise_bodyrow_weight);
			TextView reps = (TextView) view.findViewById(R.id.t_workout_exercise_bodyrow_reps);
			
			SetInstanceCursor setInstCursor = (SetInstanceCursor) cursor;
			
			SetInstance curInst = setInstCursor.getSetInstance();
			
			if (curInst.getWeight() == -1) {
				weight.setText("-");
			} else {
				weight.setText(StringFormatter.weightToString(curInst.getWeight())); 
			}
			
			if (curInst.getNumReps() == -1) {
				reps.setText("-");
			} else {
				reps.setText(curInst.getNumReps()+""); 
			}
			
			//Get the date
			Date dateOn = mDbHelper.getSetDate(curInst.getId());
			
			date.setText(Utilities.formatDateOnlyTwoDigYear(dateOn));
		}
		
			
	}
	
}
