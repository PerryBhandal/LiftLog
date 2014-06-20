package com.plined.liftlog;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public abstract class NameSelectionDialog extends UserInteractDialog {
	
	protected static int MIN_ROUTINE_NAME_LENGTH = 1;
	protected Routine mRoutine;

	/*
	 * This method should be overriden and at the very least:
	 * 1) Member vars should be assigned.
	 * 2) View should be inflated and returned.
	 */
	protected abstract View preCreateDialog(LayoutInflater inflater);
	
	/*
	 * This method is called when our negative button is pressed.
	 */
	protected abstract void negativeCallback();
	
	/*
	 * This method is called when our positive button is pressed.
	 * If you return true, the dialog is dismissed. Returning false
	 * does not dismiss the box.
	 */
	protected abstract boolean positiveCallback();
	
	
	protected abstract String getDialogTitle();
	
	/*
	 * Sets the dialog's error text to errorText
	 * and makes the error text visible if it's invisible.
	 */
	protected void showErrorText(String errorText) {
		//Find the error text box
		TextView errorTextTV = (TextView) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_conflict);
		
		//Set the text
		errorTextTV.setText(errorText);
		
		//Make it visible
		errorTextTV.setVisibility(View.VISIBLE);
	}
	
	/*
	 * Checks whether the routine name is available (no active routine exists with the name).
	 * Check is case insensitive.
	 * Returns true if the name is available, false if it isn't.
	 */
	protected boolean routineNameAvailable(String routineName) {
		//Get a reference to our DB APi
		LiftLogDBAPI dbapi = LiftLogDBAPI.get(getActivity());
		
		//Grab any existring routine with that name.
		Routine rotSearch = dbapi.getRoutineByName(routineName, true, false);
		
		//If it's null, the name is available. If it's not, the name is taken.
		if (rotSearch == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Checks whether a routine name is long enough.
	 */
	protected boolean isLongEnough(String routineName, int minNameLength) {
		if (routineName.length() >= minNameLength)
			return true;
		else
			return false;
	}
	
	/*
	 * Populates our description text and our edit view hint with the provided data.
	 */
	protected void populateTexts(String instructionText, String editTextHint) {
		EditText hintBox = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
		hintBox.setHint(editTextHint);
		
		TextView instructionTv = (TextView) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_instructions);
		instructionTv.setText(instructionText);
	}

	/*
	 * Gets our routine and assigns it to mRoutine.
	 */
	protected void getRoutine() {
		//Get the ID
		int rotId = getArguments().getInt(RoutineFactoryFragment.ROUTINE_ID);
		
		//Get the routine
		Routine rotGrab = LiftLogDBAPI.get(getActivity()).getRoutineById(rotId);
		
		//Make sure our routine isn't null
		if (rotGrab == null) {
			throw new RuntimeException("Attempted to grab routine for name modification, but grabbed null from db. Routine ID is " + rotId);
		}
		
		//Set it to our member var
		mRoutine = rotGrab;
	}
	
	/*
	 * Returns the string in our edit text.
	 */
	protected String getEditTextString() {
		//Grab our edit text
		EditText editTextView = (EditText) mInflatedLayout.findViewById(R.id.fragment_new_routine_name_edit_text);
		
		return editTextView.getText().toString();
	}
	
}
