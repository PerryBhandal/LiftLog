package com.plined.liftlog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutineFragment extends Fragment {
	
	private static String TAG = "RoutineFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
		
		ViewGroup layout = (ViewGroup) view.findViewById(R.id.main_menu_layout);
		
		//Add our begin workout button
		

		
		return view;
	}
	
	
	
}
