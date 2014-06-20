package com.plined.liftlog;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Utilities {

	
	/*
	 * Configures a menu item.	 
	 */
	
	public static void configureItem(View retView, int viewId, String itemText, int iconId, String leftColor, String rightColor) {
		//Get our relative layout
		RelativeLayout layout = (RelativeLayout) retView.findViewById(viewId);
		
		//Assign its text
		TextView titleText = (TextView) layout.findViewById(R.id.menu_item_col_text);
		titleText.setText(itemText);
		
		//Set the left side icon
		ImageView leftIcon = (ImageView) layout.findViewById(R.id.menu_item_col_icon);
		leftIcon.setImageResource(iconId);
		
		//Set the background color of the left side view
		View leftView = layout.findViewById(R.id.menu_item_col_left_color);
		leftView.setBackgroundColor(Color.parseColor(leftColor));
		
		//Set the background color of the right side view
		View rightView = layout.findViewById(R.id.menu_item_col_right_color);
		rightView.setBackgroundColor(Color.parseColor(rightColor));
		
		
		
	}
	
	/*
	 * Returns a date in a string format of
	 * MM/DD/YYYY at HH:MM PM
	 */
	public static String formatDate(Date toFormat) {
		
		if (toFormat.getTime() == 0) {
			return "Never";
		}
		
		//Create our calendar object
		Calendar cal = Calendar.getInstance();
		cal.setTime(toFormat);
		
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		
		String ampm = "";
		if (cal.get(Calendar.AM_PM) == 0) {
			ampm = "AM"; 
		} else {
			ampm = "PM";
		}
		
		//Swap hour to 12 if it's 0. Handles AM/PM cases where it's 12:XX
		if (hour == 0) {
			hour = 12;
		}
		
		return String.format("%d/%d/%d at %d:%02d %s", month, day, year, hour, minute, ampm);
	}
	
	/*
	 * Returns a date in a string format of
	 * MM/DD/YYYY
	 */
	public static String formatDateOnly(Date toFormat) {
		
		if (toFormat.getTime() == 0) {
			return "Never";
		}
		
		//Create our calendar object
		Calendar cal = Calendar.getInstance();
		cal.setTime(toFormat);
		
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		return String.format("%d/%d/%d", month, day, year);
	}
	
	/*
	 * Returns a date in a string format of
	 * MM/DD/YY
	 */
	public static String formatDateOnlyTwoDigYear(Date toFormat) {
		
		if (toFormat.getTime() == 0) {
			return "Never";
		}
		
		//Create our calendar object
		Calendar cal = Calendar.getInstance();
		cal.setTime(toFormat);
		
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		return String.format("%d/%d/%d", month, day, (year%100));
	}
	
	/*
	 * Returns a date in a string format of
	 * HH:MM PM
	 */
	public static String formatTimeOnly(Date toFormat) {
		
		if (toFormat.getTime() == 0) {
			return "Never";
		}
		
		//Create our calendar object
		Calendar cal = Calendar.getInstance();
		cal.setTime(toFormat);
		
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		
		//Swap hour to 12 if it's 0. Handles AM/PM cases where it's 12:XX
		if (hour == 0) {
			hour = 12;
		}
		
		String ampm = "";
		if (cal.get(Calendar.AM_PM) == 0) {
			ampm = "AM"; 
		} else {
			ampm = "PM";
		}
		
		return String.format("%d:%02d %s", hour, minute, ampm);
	}
}
