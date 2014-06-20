package com.plined.liftlog;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WeightSelectionFragment extends Fragment {

	private LiftLogDBAPI mDbHelper;
	private SetInstance mSetInstance;
	
	private TextView mWeightTextView;
	private TextView mRepsTextView;
	
	private TextView mNextButton;
	
	
	private static int WEIGHT_REL = 1;
	private static int REPS_REL = 2;
	
	//Colors that our relative layout takes on depending on its strate (active veruss inactive).
	private int mCurActiveRel = -1;
	
	private RelativeLayout mActiveRel;
	
	public static final String SET_ID = "com.perryb.liftlog.weightselectionfragment.set_id";
	
	private static String TAG = "WeightSelectionFragment";
	
	public static WeightSelectionFragment newInstance(int setId) {
		//Create our bundle
		Bundle args = new Bundle();
		//Put the routine's ID in
		args.putInt(SET_ID, setId);
		
		//Create our routine fragment
		WeightSelectionFragment toLaunch = new WeightSelectionFragment();
		
		//Attach our bundle
		toLaunch.setArguments(args);
		
		return toLaunch;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get a reference to our DB manager
		mDbHelper = LiftLogDBAPI.get(getActivity());
		
		//Get our setInstance
		int setId = getSetArg(getArguments());
		mSetInstance = mDbHelper.getSetInstanceById(setId);
		
		//Make sure we got a set instance
		if (mSetInstance == null) {
			throw new RuntimeException("Could not retrieve set with set instance ID " + setId);
		}
		
		//Set this so we'll get callbacks when menu items are hit.
		setHasOptionsMenu(true);

	}
	
	/*
	 * Pulls the set_id from our fragment bundle and returns it.
	 * Thrwos a runtime exception  if the fragment has no set ID argument.
	 */
	private int getSetArg(Bundle fragArguments) {
		if (fragArguments == null) {
			//No set ID provided.
			throw new RuntimeException("Fragment has no set ID argument");
		}
		
		int setId = fragArguments.getInt(SET_ID);
		
		return setId;
		
	}
	
	/*
	 * Sets our active relative layout (the relative layout
	 * we feeed numbers into).
	 */
	private void setActiveRel(View layoutView, int activeRel) {
		//Store our inactive rel so we can cahnge its color after.
		
		View inactiveRel;
		
		if (activeRel == REPS_REL) {
			mActiveRel = (RelativeLayout) layoutView.findViewById(R.id.fragment_weight_selection_repsRel);
			inactiveRel = (RelativeLayout) layoutView.findViewById(R.id.fragment_weight_selection_weightRel);
			mCurActiveRel = REPS_REL;
			mNextButton.setText("Save");
			
			//Set its color too
			mNextButton.setBackgroundColor(getActivity().getResources().getColor(R.color.darkGreen));
		} else {
			mActiveRel = (RelativeLayout) layoutView.findViewById(R.id.fragment_weight_selection_weightRel);
			inactiveRel = (RelativeLayout) layoutView.findViewById(R.id.fragment_weight_selection_repsRel);
			mCurActiveRel = WEIGHT_REL;
			mNextButton.setText("Next");
			mNextButton.setBackgroundColor(getActivity().getResources().getColor(R.color.medBlue));
		}
		
		//Set colors
		mActiveRel.setBackgroundColor(getResources().getColor(R.color.lightBlue));
		inactiveRel.setBackgroundColor(getResources().getColor(R.color.darkBlue));
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_weight_selection, menu);
	}
	
	@Override
	@TargetApi(11)
	public boolean onOptionsItemSelected(MenuItem item) {			
		
		switch(item.getItemId()) {
		case R.id.menu_item_resetfields:
			clearNumber();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Returns the currently active rel.
	 */
	private int getActiveRelId() {
		if (mCurActiveRel != -1) {
			return mCurActiveRel;
		} else {
			throw new RuntimeException("Requesting active relative layout before initialization. Currently holds -1.");
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_weight_selection, parent, false);
		
		mRepsTextView = (TextView) (v.findViewById(R.id.fragment_weight_selection_repsRel)).findViewById(R.id.fragment_weight_selection_coretext);
		mWeightTextView = (TextView) (v.findViewById(R.id.fragment_weight_selection_weightRel)).findViewById(R.id.fragment_weight_selection_coretext);

		View.OnClickListener numberButtonListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView textView = (TextView)v;
				addNumber(textView.getText().toString());
			}
		};
		
		TableLayout tableLayout = (TableLayout)v.findViewById(R.id.fragment_weight_selection_tableLayout);
		int number = 1;
		for (int i = 2; i < tableLayout.getChildCount()-1; i = i+2) {
			TableRow row = (TableRow)tableLayout.getChildAt(i);
			
			for (int j = 0; j < row.getChildCount(); j = j+2) {
				Button button = (Button)row.getChildAt(j);
				button.setText(""+number);
				button.setOnClickListener(numberButtonListener);
				number++;
			}
			
		}
		
		
		TableRow bottomRow = (TableRow)tableLayout.getChildAt(tableLayout.getChildCount() -1);
		
		Button decimalButton = (Button)bottomRow.getChildAt(0);
		decimalButton.setText(".");
		decimalButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addDecimal();
			}
		});
		
		Button zeroButton = (Button)bottomRow.getChildAt(2);
		zeroButton.setText("0");
		zeroButton.setOnClickListener(numberButtonListener);
		
		Button enterButton = (Button) bottomRow.getChildAt(4);
		enterButton.setText("Next");
		mNextButton = enterButton;
		
		enterButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCurActiveRel == WEIGHT_REL) {
					//Currently set to reps, set to weight instead.
					setActiveRel(getView(), REPS_REL);
				} else {
					//Currently set to reps. Save this.
					saveInputs();
				}
				
			}
		});
		
		//Set the active relative layout to our reps relative layout
		setActiveRel(v, WEIGHT_REL);
		configureWeightRepListener(v);
		
		return v;
	}
	
	/*
	 * Adds a decimal to our current working number.
	 */
	private void addDecimal() {
		//Check if we're doing reps, if we are add decimal should do nothing.
		if (mCurActiveRel == REPS_REL) {
			return;
		}
		
		//Get our current nubmer value
		String curValue = getActiveTextView().getText().toString();
		
		//Check if it's an initialization value
		if (curValue.equals("-")) {
			//Nothing in there, can't put a decimal.
			return;
		}
		
		//Maek sure they don't already have a decimal
		if (curValue.contains(".")) {
			return;
		}
		
		//Add a decimal in
		//Check if we're above 1000.
		float curNum = Float.parseFloat(getActiveTextView().getText().toString());
		
		if (curNum < 1000f) {
			getActiveTextView().setText(curValue + ".");
		}
	}
	
	/*
	 * Configures the onclick listeners for our weight and reps boxes so they
	 * become focused when clicked.
	 */
	public void configureWeightRepListener(View inflatedLayout) {
		
		//Rep listener
		View repLayout = inflatedLayout.findViewById(R.id.fragment_weight_selection_repsRel);
		repLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Check if rep is not currently the active box
				if (mCurActiveRel != REPS_REL) {
					//Switch the target layout.
					setActiveRel(getView(), REPS_REL);
				}
			}
		});
		
		//Weight listener
		View weightLayout = inflatedLayout.findViewById(R.id.fragment_weight_selection_weightRel);
		weightLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Check if weight is not currently the active box
				if (mCurActiveRel != WEIGHT_REL) {
					//Switch the target layout.
					setActiveRel(getView(), WEIGHT_REL);
				}
			}
		});
		
	}
	
	/*
	 * Saves the values in both text fields into our set instance then returns back to our previous activity.
	 */
	private void saveInputs() {
		//Extract our values
		int repVal;
		float weightVal;
		
		if (mRepsTextView.getText().toString().equals("-")) {
			repVal = -1;
		} else {
			repVal = Integer.parseInt(mRepsTextView.getText().toString());
		}
		
		if (mWeightTextView.getText().toString().equals("-")) {
			weightVal = -1;
		} else {
			weightVal = Float.parseFloat(mWeightTextView.getText().toString());
		}
		
		//Now save our values back
		mSetInstance.setWeight(weightVal);
		mSetInstance.setNumReps(repVal);
		
		//Force an update
		mSetInstance.update();
		
		//Finish this activity
		getActivity().finish();
	}
	
	/*
	 * Clears both textviews and refocuses the weight rel.
	 */
	private void clearNumber() {
		mRepsTextView.setText("-");
		mWeightTextView.setText("-");
		setActiveRel(getView(), WEIGHT_REL);
	}
	
	/*
	 * Returns the core textview from the currently active relative layout.
	 */
	private TextView getActiveTextView() {
		return (TextView) mActiveRel.findViewById(R.id.fragment_weight_selection_coretext);
	}
	
	/*
	 * Adds the provided number to the active textview. Handles overflow numbers.
	 */
	private void addNumber(String strNumToAdd) {
		//Convert our number to an int
		int numToAdd = Integer.parseInt(strNumToAdd);
		
		//Get our current nubmer value
		String curValue = getActiveTextView().getText().toString();
		
		//Check if it's an initialization value
		if (curValue.equals("-")) {
			//Adding first number
			getActiveTextView().setText(numToAdd+"");
			return;
		}
		
		//Check if we're above 1000.
		float curNum = Float.parseFloat(getActiveTextView().getText().toString());
		
		if (curNum < 1000f) {
			//They're below 1000, we can add the number on.
			getActiveTextView().setText(curValue + numToAdd);
		}
		
	}
	
}
