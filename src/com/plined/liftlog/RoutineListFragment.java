package com.plined.liftlog;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.plined.liftlog.LiftLogDBMaster.RoutineCursor;

public class RoutineListFragment extends ListFragment {

	private static String TAG = "RoutineListFragment";
	
	//Key for our SharedPreferences to get the sort method.
	private static String SHAREDPREF_KEY_SORTMETHOD = "com.plined.liftlog.routine_list_fragment.sort_method";
	
	//Key for passing routine ID to our delete confirm dialog
	public static String ROUTINE_ID = "com.perryb.liftlog.routinelistfragment.routine_id";
	
	//Request/Result for deletion dialog.
	protected static int REQUEST_DELETE_ROUTINE = 1;
	protected static int REQUEST_COPY_ROUTINE = 2;
	protected static int REQUEST_BUY_PRO = 3;
	
	protected static int RESULT_DELETE_ROUTINE = 1;
	protected static int RESULT_COPY_ROUTINE = 2;
	protected static int RESULT_BUY_PRO = 3;
	
	//The max number of routines a user can hajve in the free version.
	protected static final int MAX_ROUTINES_FREE = 2;
	
	private RoutineCursorAdapter mAdapter;
	
	private LiftLogDBAPI mDbHelper;
	
	@Override
	public void onResume() {
		//Reload our data in case we just returned from the routine fragment area.
		mAdapter.reloadData();
		super.onResume();
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
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
		getActivity().getMenuInflater().inflate(R.menu.routine_list_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		//Figure out if they hit the delete button
		switch(item.getItemId()) {
		case R.id.menu_item_delete_routine: {
			//Trying to delete.
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			int position = info.position;
			
			//Figure out which we're trying to delete
			RoutineCursorAdapter adapter = (RoutineCursorAdapter)getListAdapter();
			RoutineCursor rotCursor = (RoutineCursor) adapter.getItem(position);
			Routine rotDel = rotCursor.getRoutine();
			
			//Launch a delete confirm dialog.
			//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
			DeleteRoutineConfirmDialog fragToUse = DeleteRoutineConfirmDialog.newInstance(rotDel.getId());
			
			//Launch this fragment.
			launchDialogFragmentTarget(fragToUse, "deleteroutineconfirm", REQUEST_DELETE_ROUTINE);
			
			return true;
		}
//		//TODO: REIMPLEMENT ROUITINE COPYING
//		case R.id.menu_item_copy_routine: {
//			//Trying to copy a routine.
//			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
//			int position = info.position;
//			
//			//Figure out which we're trying to copy
//			/*
//			 * The reason we do position-1 here is because the header we added here takes up position 0. So
//			 * everything else is off by 1.
//			 */
//			RoutineCursorAdapter adapter = (RoutineCursorAdapter)getListAdapter();
//			RoutineCursor rotCursor = (RoutineCursor) adapter.getItem(position-1);
//			Routine rotCopy = rotCursor.getRoutine();
//			
//			//Launch a copy dialog.
//			//Create the fragment. We'll attach our current routine ID with it so it knows what the routine
//			//to source exercises from is.
//			CopyRoutineDialog fragToUse = CopyRoutineDialog.newInstance(rotCopy.getId());
//			
//			//Launch this fragment.
//			launchDialogFragmentTarget(fragToUse, "copyroutinenameselect", REQUEST_COPY_ROUTINE);
//			
//			return true;
//		}
		default:
			return super.onContextItemSelected(item);
		}
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
		
			/*
			 * For now this will only work on api 11+
			 */
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

	/*
	 * Processes returns from our dialogs.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DELETE_ROUTINE) {
			//Routine h as been deleted. Reload our list.
			mAdapter.reloadData();
		} else if (requestCode == REQUEST_COPY_ROUTINE) {
			//Routine has been copied. Update our data.
			mAdapter.reloadData();
		}
		else {
			//Unexpected code returned.
			throw new RuntimeException("Received unexpected request and/or result code. Request code is " + requestCode + " result code is " + resultCode);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_routine_list, parent, false);
		
		//Add the listener for new routine button
		addNewRoutineListener(v);
		
		//Make our listview context menu sensitive
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		registerForContextMenu(listView);
		
		return v;
	}
	
	/*
	 * Sets up a listener on our listview that launches the routine manager when a routine is clicked.
	 */
	private void setListViewListener() {

		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Get the routine
				RoutineCursor rotCursor = (RoutineCursor) mAdapter.getItem(position);
				
				//Launch the manager.
				launchRoutineManager(rotCursor.getRoutine().getId());
			}
			
			
			private void launchRoutineManager(int routineId) {
				Intent i = new Intent(getActivity(), RoutineFactoryActivity.class);
				//Putthe routine id in
				i.putExtra(RoutineFactoryActivity.EXTRA_ROUTINE_ID, routineId);
				
				//Start the routine factory.
				startActivity(i);
			}
			
		});
	}
	
	/*
	 * Wires up our new routine view so when clicked it launches
	 * the routine creation dialog.
	 */
	private void addNewRoutineListener(View newRoutineView) {
		//Get the botgtom button
		View bottomButton = newRoutineView.findViewById(R.id.fragment_routine_list_newbutton);
		
		bottomButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//Reason for max_routines-1 is we only want to show the add if they have 1 less than the limit (as the
				//add menu can let them add the next).
				if (mDbHelper.getActiveRoutineCount() <= (MAX_ROUTINES_FREE-1) || LicenseManager.staticHasPro(getActivity())) {
					//They either have less than two routines, or they have a pro license. Let them add a routine.
					NewRoutineDialog toShow = new NewRoutineDialog();
					launchDialogFragment(toShow, "createnewroutine");
				} else {
					PurchaseProConfirmDialog toShow = PurchaseProConfirmDialog.newInstance("The free version of LiftLog is limited to two routines.", "LiftLog Pro allows you to have an unlimited number of routines, along with several other features. Would you like to upgrade now?");
					launchDialogFragmentTarget(toShow, "purchaseproconfirm", REQUEST_BUY_PRO);
				}
				
			}
		});
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
	
	
	public static class NewRoutineDialog extends NameSelectionDialog {
		
		View mInflatedLayout;
		
		public String getDialogTitle() {
			return "Create New Routine";
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
			
			//Make sure the name isn't in use
			if (!routineNameAvailable(enteredText)) {
				showErrorText("That routine name is already in use.");
				return false;
			}
			
			//Create the routine
			new Routine(getActivity(),enteredText, null, true).insert();
			
			//Get the routine's ID
			Routine insertedRot = LiftLogDBAPI.get(getActivity()).getRoutineByName(enteredText, true, true);
			
			if (insertedRot == null) {
				//Means the routine we just created can't bef ound.
				throw new RuntimeException("Created routine with name " + enteredText + " but it can't be retrieved after creation");
			}
			
			//Start our routine view fragment
			//Create our intent
			Intent i = new Intent(getActivity(), RoutineFactoryActivity.class);
			//Putthe routine id in
			i.putExtra(RoutineFactoryActivity.EXTRA_ROUTINE_ID, insertedRot.getId());
			
			//Start the routine factory.
			startActivity(i);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Create Routine";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Choose a routine name", "");
			
			return mInflatedLayout;
		}
		
		
	}
	
	
	public static class CopyRoutineDialog extends NameSelectionDialog {
		
		View mInflatedLayout;
		
		public String getDialogTitle() {
			return "Copy Routine";
		}
		
		public void negativeCallback() {
			
		}
		
		public static CopyRoutineDialog newInstance(int routineId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_ID, routineId);
			
			CopyRoutineDialog fragment = new CopyRoutineDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		/*
		 * Returns the routine ID passed to this confirm dialog.
		 */
		private int getRoutineId() {
			//Get our arguments
			Bundle args = getArguments();
			
			//Parse out the routine ID
			int rotId = args.getInt(ROUTINE_ID, -1);
			
			//Check that we got a rotid
			if (rotId == -1) {
				//Didn't get ar otId
				throw new RuntimeException("Attempting to copy a routine with no routine ID.");
			}
			
			//Return it
			return rotId;
			
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
			
			//Make sure the name isn't in use
			if (!routineNameAvailable(enteredText)) {
				showErrorText("That routine name is already in use.");
				return false;
			}
			
			//Create the routine
			new Routine(getActivity(),enteredText, null, true).insert();
			
			//Get the routine's ID
			Routine insertedRot = LiftLogDBAPI.get(getActivity()).getRoutineByName(enteredText, true, true);
			
			if (insertedRot == null) {
				//Means the routine we just created can't bef ound.
				throw new RuntimeException("Created routine with name " + enteredText + " but it can't be retrieved after creation");
			}
			
			//Get our destination routine's ID
			int destRotId = insertedRot.getId();
			
			//Get our source routine's id
			int srcRotId = getRoutineId();
			
			//Copy the exercises
			LiftLogDBAPI.get(getActivity()).duplicateRoutineExercises(srcRotId, destRotId);
			
			//Call onactivityresult so it knows to update.
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_COPY_ROUTINE, null);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Copy Routine";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			mInflatedLayout = super.inflateLayout(inflater, R.layout.dialog_new_routine, null);
			
			//Set the instruction, and our edit text hint to nothing.
			populateTexts("Choose a name for this routine's copy", "");
			
			return mInflatedLayout;
		}
		
		
	}
	
	public static class DeleteRoutineConfirmDialog extends ConfirmationDialog {
		
		View mInflatedLayout;
		
		public static DeleteRoutineConfirmDialog newInstance(int routineId) {
			Bundle args = new Bundle();
			args.putInt(ROUTINE_ID, routineId);
			
			DeleteRoutineConfirmDialog fragment = new DeleteRoutineConfirmDialog();
			fragment.setArguments(args);
			
			return fragment;
		}
		
		public String getDialogTitle() {
			return "Delete Routine";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Get the id of the routine to delete
			int idToDel = getRoutineId();
			
			//Get the routine
			Routine toDelete = LiftLogDBAPI.get(getActivity()).getRoutineById(idToDel);
			
			//Get all of its routine exercises and delete them
			ArrayList<RoutineExercise> rotExDel = LiftLogDBAPI.get(getActivity()).getRoutineExercises(toDelete.getId());
			
			//Delete every routine exercise
			for (RoutineExercise rotEx: rotExDel) {

				rotEx.delete();
			}
			
			//Delete the routine
			toDelete.delete();
			
			//Call onactivityresult so it knows to update.
			getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_DELETE_ROUTINE, null);
			
			return true;
		}
		
		/*
		 * Returns the routine ID passed to this confirm dialog.
		 */
		private int getRoutineId() {
			//Get our arguments
			Bundle args = getArguments();
			
			//Parse out the routine ID
			int rotId = args.getInt(ROUTINE_ID, -1);
			
			//Check that we got a rotid
			if (rotId == -1) {
				//Didn't get ar otId
				throw new RuntimeException("Attempting to delete a routine with routine ID -1.");
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
			setConfirmTopText("Are you sure you want to delete this routine?");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
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
	

	
}
