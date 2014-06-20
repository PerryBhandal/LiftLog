package com.plined.liftlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public abstract class UserInteractDialog extends UserDialog {
	

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Call our pre-create dialog method so it inflates our view
		View v = preCreateDialog(getActivity().getLayoutInflater());
		
		//Create our dialog
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity()).setView(v);
		
		//Set our dialog's title.
		dialogBuilder.setTitle(getDialogTitle());
		
		//Add a negative button if warranted
		if (getNegativeText() != null) {
			//Add its title.
			dialogBuilder.setNegativeButton(getNegativeText(), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//run the negative callback
					negativeCallback();
				}
			});
		}
		
		//Set background to white
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			//Sub 11, invert the background.
			dialogBuilder.setInverseBackgroundForced(true);
		}
		
		//Add a positive button
		dialogBuilder.setPositiveButton(getPositiveText(), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing here. This'll be processed in the onshow listener below.
			}
		});
		
		//Create our dialog.
		final AlertDialog mDialogBox = dialogBuilder.create();
		
		//Set up our on show listener
		mDialogBox.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(DialogInterface dialog) {

		        Button b = mDialogBox.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	if (positiveCallback()) {
		            		//Dismiss our box.
		            		mDialogBox.dismiss();
		            	}
		            }
		        });
		    }
		});
		
		//Return the made dialog box
		return mDialogBox;
	}
	
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
	
	protected String getPositiveText() {
		return "Ok";
	}
	
	protected String getNegativeText() {
		return "Cancel";
	}
	
	protected abstract String getDialogTitle();
}
