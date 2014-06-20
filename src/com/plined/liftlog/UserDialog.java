package com.plined.liftlog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class UserDialog extends DialogFragment {

	protected View mInflatedLayout;
	
	/*
	 * Inflates our layout and stores it in member var mInflatedLayout.
	 */
	protected View inflateLayout(LayoutInflater inflater, int layoutId, ViewGroup root) {
		mInflatedLayout = inflater.inflate(layoutId, root);
		return mInflatedLayout;
	}
	
	public abstract Dialog onCreateDialog(Bundle savedInstanceState);
	
	protected abstract String getDialogTitle();
	
	/*
	 * Gets an instnace of our database manager.
	 */
	protected LiftLogDBAPI getDBManager() {
		return LiftLogDBAPI.get(getActivity().getApplicationContext());
	}
	
}
