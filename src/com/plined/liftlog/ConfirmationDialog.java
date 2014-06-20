package com.plined.liftlog;

import android.view.View;
import android.widget.TextView;

public abstract class ConfirmationDialog extends UserInteractDialog {
	
	/*
	 * Sets the content of our top text box. If the
	 * input is null, the text box is set to gone.
	 */
	protected void setConfirmTopText(String textValue) {
		//Get a reference to it
		TextView topText = (TextView) mInflatedLayout.findViewById(R.id.dialog_confirm_top_text);
		
		if (textValue != null) {
			//Set the text
			topText.setText(textValue);
		} else {
			//Make this box invisible
			topText.setVisibility(View.GONE);
		}
	}
	
	/*
	 * Sets the content of our bottom text box. If the
	 * input is null, the text box is set to gone.
	 */
	protected void setConfirmBottomText(String textValue) {
		//Get a reference to it
		TextView bottomText = (TextView) mInflatedLayout.findViewById(R.id.dialog_confirm_bottom_text);
		
		if (textValue != null) {
			//Set the text
			bottomText.setText(textValue);
		} else {
			//Make this box invisible
			bottomText.setVisibility(View.GONE);
		}
	}

}
