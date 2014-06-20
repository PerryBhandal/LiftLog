package com.plined.liftlog;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.plined.liftlog.LiftLogDBMaster.ExerciseInstanceCursor;
import com.plined.liftlog.LiftLogDBMaster.RoutineInstanceCursor;
import com.plined.liftlog.WorkoutHistoryFragment.DeleteRoutineInstanceConfirmDialog;

public class ExerciseHistoryDailyFragment extends ListFragment {

	//Key to store our exercise ID in our bundle
	private static String EXERCISE_ID = "com.plined.liftlog.exercisehistoryfragment.exerciseid";
	private static String EXERCISE_INSTANCE_ID = "com.plined.liftlog.exercisehistoryfragment.exerciseinstanceid";
	
	private static String TAG = "ExerciseHistoryDailyFragment";
	
	private Exercise mExercise;
	private LiftLogDBAPI mDbHelper;
	
	private ExerciseInstanceCursorAdapter mAdapter;
	private View mInflatedLayout;
	

	public void onResume() {
		if (mAdapter != null) {
			//Relaod our data.
			mAdapter.reloadData();
		}
		super.onResume();
	}
	
	public static ExerciseHistoryDailyFragment newInstance(int exerciseId, int exerciseInstanceId) {
		//Create our bundle
		Bundle args = new Bundle();
		
		//Put the routine's ID in
		args.putInt(EXERCISE_ID, exerciseId);
		args.putInt(EXERCISE_INSTANCE_ID, exerciseInstanceId);
		
		//Create our routine fragment
		ExerciseHistoryDailyFragment toLaunch = new ExerciseHistoryDailyFragment();
		
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
			ExerciseInstanceCursor insCursor = (ExerciseInstanceCursor) new ExerciseInstanceCursorLoader(getActivity()).loadCursor();
			mAdapter = new ExerciseInstanceCursorAdapter(getActivity(), insCursor);

			
			//Make sure our listview doesn't show selections
			getListView().setSelector(android.R.color.transparent);
			
			setListAdapter(mAdapter);
		}
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		if (mInflatedLayout == null) {
			mInflatedLayout = inflater.inflate(R.layout.f_exercise_history_daily, parent, false);
			mInflatedLayout.setBackgroundResource(R.drawable.background);
			
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
	
	/*
	 * Gets our exericse instance ID from the bundle.
	 */
	private int getExerciseInstanceId() {
		Bundle args = getArguments();
		
		//Get our exid out
		int exerciseInstanceId = args.getInt(EXERCISE_INSTANCE_ID, -1) ;
		
		//Make sure it's valid
		if (exerciseInstanceId == -1) {
			throw new RuntimeException("Exercise instance ID was either not defined, or could not be retrieved.");
		}
		
		return exerciseInstanceId;
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
			ExerciseInstanceCursorAdapter adapter = (ExerciseInstanceCursorAdapter)getListAdapter();
			ExerciseInstanceCursor exInstCursor = (ExerciseInstanceCursor) adapter.getItem(position);
			ExerciseInstance exInst = exInstCursor.getExerciseInstance();
			
			Intent i = new Intent(getActivity(), WorkoutInstanceActivity.class);
			//Putthe routine id in
			i.putExtra(WorkoutInstanceActivity.EXTRA_ROUTINE_INSTANCE_ID, exInst.getRoutineInstanceParent());
			
			//Start the routine factory.
			startActivity(i);
			
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	private class ExerciseInstanceCursorLoader extends SQLiteCursorLoader {
		
		public ExerciseInstanceCursorLoader(Context context) {
			super(context);
		}
		
		@Override
		protected Cursor loadCursor() {
			//Query the list of runs
			return LiftLogDBAPI.get(getContext()).getExerciseInstancesCursorFromSup(mExercise.getId(), getExerciseInstanceId());
		}
		
	}
	
	private class ExerciseInstanceCursorAdapter extends CursorAdapter {
		
		/*
		 * Wires up the exercise title.
		 */
		private void wireTitle(TextView exTitleTv, ExerciseInstance exerciseInst, String titlePrefix) {
			//Get the date
			RoutineInstance parentRoutine = mDbHelper.getRoutineInstanceById(exerciseInst.getRoutineInstanceParent());
			
			
			exTitleTv.setText(Utilities.formatDate(parentRoutine.getDate()));
		}

		private ExerciseInstanceCursor mExIntCursor;
		
		
		public ExerciseInstanceCursorAdapter(Context context, ExerciseInstanceCursor cursor) {
			super(context, cursor, 0);
			mExIntCursor = cursor;
		}
		
		
		/*
		 * Sets the visibility of the provided exercise instance view based on its
		 * exInt's expanded state.
		 */
		private void setInstanceVisibility(View exIntView, ExerciseInstance exInt) {
			//Make everything visible excluding instruction.
			View bodyView = exIntView.findViewById(R.id.t_exercise_history_daily_container_lay_body);
			
			bodyView.setVisibility(View.VISIBLE);
		}
		
		private View populateView(View baseView, ExerciseInstance exInst) {
			
			addBody(baseView, exInst);
			
			return baseView;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getActivity().getLayoutInflater().inflate(R.layout.t_exercise_history_daily_container, parent, false);
		}
		
		public void reloadData() {
			changeCursor(new ExerciseInstanceCursorLoader(getActivity()).loadCursor());
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ExerciseInstanceCursor exIntCursor = (ExerciseInstanceCursor) cursor;
			ExerciseInstance curInst = exIntCursor.getExerciseInstance();
			
			//Empty out the view's existing children in case it's a convert.
			LinearLayout setBody = (LinearLayout) view.findViewById(R.id.t_exercise_history_daily_container_lay_setBody);
			setBody.removeAllViews();
			
			//Reset the textview for comments
			TextView instructionTV = (TextView) view.findViewById(R.id.t_exercise_history_daily_container_comments_body);
			instructionTV.setText("");
			
			//Remove the expand button
			ImageView expandButton = (ImageView) view.findViewById(R.id.t_exercise_history_daily_container_header_expand);
			expandButton.setImageResource(0);
			expandButton.setVisibility(View.GONE);
			
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
			//get an arraylist of our sets
			ArrayList<SetInstance> setList = mDbHelper.getSetInstances(exInt.getId());
			
			LinearLayout addTo = (LinearLayout)baseBody.findViewById(R.id.t_exercise_history_daily_container_lay_setBody);
			
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
			
			TextView exTitle = (TextView) baseBody.findViewById(R.id.t_exercise_history_daily_container_header_name);
			wireTitle(exTitle, exInt, "");
			
			//Populate oru comments
			if (exInt.getComment() == null || exInt.getComment().equals("")) {
				//No comment. Hide the comment body.
				baseBody.findViewById(R.id.t_exercise_history_daily_container_lay_comments).setVisibility(View.GONE);
			} else {
				//Havea comment. Make sure it's visible, then populate it.
				baseBody.findViewById(R.id.t_exercise_history_daily_container_lay_comments).setVisibility(View.VISIBLE);
				TextView commentTv = (TextView) baseBody.findViewById(R.id.t_exercise_history_daily_container_comments_body);
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
//			
//			//Set its background color based on whether it's been attempted.
//			if (weightStr.equals("-") && repsStr.equals("-")) {
//				//Neitehr is defined. Set hasn't been attempted yet.
//				added.setBackgroundColor(Color.parseColor("#ed7500"));
//			} else {
//				//At least one is defined. The set has been attempted.
//				added.setBackgroundColor(Color.parseColor("#0b8e6a"));
//			}
			
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
	
}
