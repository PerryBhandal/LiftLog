package com.plined.liftlog;

import com.plined.liftlog.WorkoutInstanceFragment.DeleteExerciseConfirmDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainMenuFragment extends Fragment {
	
	private static String TAG = "MainMenuFragment";
	private static String SPREF_SEENTUTORIAL = "com.plined.liftlog.mainmenufragmnet.seentutorial";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Force our dB to be created to run tests
		new LiftLogDBMaster(getActivity().getApplicationContext());
		
		//Check if user has a license
		LicenseManager licenseMan = new LicenseManager(getActivity());
		if (!licenseMan.hasProLicense()) {
			//Update their license
			licenseMan.bindIabHelperCheckLicense();
		} else {
		}
		
		
		doInitialSetup();
	}
	
	
	
	/*
	 * 
	 */
	private void showUpgradeButton(View v) {
		View upgradeButton = v.findViewById(R.id.main_menu_buypro);
		upgradeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new LicenseManager(getActivity()).bindIabHelperDoPurchase();
			}
		});
		upgradeButton.setVisibility(View.VISIBLE);
	}

	
	/*
	 * Checks whether our DB exists. If it doesn't, it creates our sample workouts.
	 */
	private void doInitialSetup() {
		if (!LiftLogDBAPI.doesDatabaseExist(getActivity(), LiftLogDBMaster.DB_NAME)) {
			//DB doesn't exisst. Create sample workouts.
			//No more sample workouts.
			//new WorkoutSamples(getActivity()).createSampleRoutines();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
		
		//Set up our menu items
		populateMenuItems(view);
		
		//Add our onclick listeners
		registerClickListeners(view);
		
		if (!LicenseManager.staticHasPro(getActivity())) {
			showUpgradeButton(view);
		}
		
		//check if we should show them the tutorial dialog
		if (getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(SPREF_SEENTUTORIAL, false) == false) {
			//Set the pref to not show it again.
			getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(SPREF_SEENTUTORIAL, true).commit();
			
			//Prompt them with the dialog
			ViewTutorialDialog fragment = new ViewTutorialDialog();
			launchDialogFragment(fragment, "viewtutorial");
		}
		
		return view;
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
	

	public static class ViewTutorialDialog extends ConfirmationDialog {
		
		
		public String getDialogTitle() {
			return "LiftLog Tutorial";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			Intent i = new Intent(getActivity(), TutorialActivity.class);
			startActivity(i);
			
			return true;
		}
		
		public String getPositiveText() {
			return "Yes";
		}
		
		public String getNegativeText() {
			return "No";
		}
		
		public View preCreateDialog(LayoutInflater inflater) {
			super.inflateLayout(inflater, R.layout.dialog_confirm, null);
			
			
			//Popuilate our text boxes.
			setConfirmTopText(String.format("Would you like to view a quick tutorial on how to use LiftLog?"));
			setConfirmBottomText("If you press no you can still view the tutorial at any time by going into the settings section and clicking 'View Tutorial'");
			
			return mInflatedLayout;
		}
		
		
	}
	
	
	/*
	 * Configures our menu items.
	 */
	private void populateMenuItems(View retView) {
		//Begin workout
		Utilities.configureItem(retView, R.id.main_menu_begin_workout, "Begin Workout", R.drawable.ic_media_play, "#3d6ccc", "#4f82e9");
		Utilities.configureItem(retView, R.id.main_menu_workout_history, "Workout History", R.drawable.ic_media_rew, "#3d6ccc", "#4f82e9");
		Utilities.configureItem(retView, R.id.main_menu_routines, "Routines", R.drawable.ic_dialog_map, "#3d6ccc", "#4f82e9");
		Utilities.configureItem(retView, R.id.main_menu_settings, "Settings", R.drawable.ic_dialog_dialer, "#3d6ccc", "#4f82e9");
		Utilities.configureItem(retView, R.id.main_menu_buypro, "Upgrade To Pro", R.drawable.ic_dialog_info, "#3d6ccc", "#4f82e9");
		
	}
	
	/*
	 * Configures the onclick listeners for our menu elements
	 */
	private void registerClickListeners(View v) {
		/*
		 * Register routine's on click listener
		 */
		//Grab the view
		View routine = v.findViewById(R.id.main_menu_routines);
		
		//Create its on click listener
		routine.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Create our intent
				Intent i = new Intent(getActivity(), RoutineListActivity.class);
				//Start the routine factory.
				startActivity(i);
				
			}
		});
		
		/*
		 * Register begin workout's listener
		 */
		//Grab the view
		View beginWorkout = v.findViewById(R.id.main_menu_begin_workout);
		
		//Create its on click listener
		beginWorkout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Create our intent
				Intent i = new Intent(getActivity(), WorkoutBeginActivity.class);
				//Start the routine factory.
				startActivity(i);
			}
		});
		
		/*
		 * Register workout history listener.
		 */
		//Grab the view
		View workoutHistory = v.findViewById(R.id.main_menu_workout_history);
		
		//Create its on click listener
		workoutHistory.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Create our intent
				Intent i = new Intent(getActivity(), WorkoutHistoryActivity.class);
				//Start the routine factory.
				startActivity(i);
			}
		});
		
		/*
		 * Register settings listener.
		 */
		//Grab the view
		View settings = v.findViewById(R.id.main_menu_settings);
		
		//Create its on click listener
		settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), SettingsActivity.class);
				//Start the routine factory.
				startActivity(i);
			}
		});
		
	}
	

	

	
}
