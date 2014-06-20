package com.plined.liftlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public abstract class UserNotificationDialog extends UserDialog {

	protected abstract View preCreateDialog(LayoutInflater inflater);
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Call our pre-create dialog method so it inflates our view
		View v = preCreateDialog(getActivity().getLayoutInflater());
		
		//Create our dialog
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity()).setView(v);
		
		//Set our dialog's title.
		dialogBuilder.setTitle(getDialogTitle());
		
		//Add a positive button
		dialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			
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
		            	mDialogBox.dismiss();
		            }
		        });
		    }
		});
		
		//Return the made dialog box
		return mDialogBox;
	}

	@Override
	protected abstract String getDialogTitle();

}
