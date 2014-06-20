package com.plined.liftlog;

import java.util.ArrayList;

import com.plined.liftlog.RoutineListFragment.DeleteRoutineConfirmDialog;
import com.plined.liftlog.RoutineListFragment.NewRoutineDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

public class SettingsFragment extends Fragment {

	View mDeleteButton;
	CheckBox mDeleteCheckBox;
	CheckBox mKeepAwakeCheckBox;
	CheckBox mRestSoundCheckBox;
	
	/*
	 * Result and request keys.
	 */
	private static final int REQUEST_DELETE_DATA = 1;
	
	private static final int RESULT_DELETE_DATA = 1;
	
	public static String SPREF_ALWAYSON_MODE = "com.plined.liftlog.settingsfragment.alwaysonmode";
	public static String SPREF_EXPIRE_SOUND = "com.plined.liftlog.settingsfragment.expiresound";
	public static String SETTINGS_PREF = "llsettingspref";
	
	public static String TAG = "SettingsFragment"; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_settings, parent, false);
		
		mDeleteCheckBox = (CheckBox) v.findViewById(R.id.f_settings_deleteCheckBox);
		
		//Wire up our delete button
		mDeleteButton = v.findViewById(R.id.f_settings_confirmButton);
		
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 
				
				//See if they've entered the correct confirmation text
				if (mDeleteCheckBox.isChecked()) {
					//They've entered it correctly, take them to confirm it.
					//Create the fragment. We'll attach our current routine ID with it so it knows what to edit.
					ConfirmDeleteDialog fragToUse = new ConfirmDeleteDialog();
					
					//Launch this fragment.
					launchDialogFragmentTarget(fragToUse, "confirmdeletion", REQUEST_DELETE_DATA);
				}
			}
		});
		
		//Put color changes into dleete butt
		mDeleteCheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Check if we're now checked or not
				if (mDeleteCheckBox.isChecked()) {
					//Set to green
					mDeleteButton.setBackgroundColor(getActivity().getResources().getColor(R.color.darkRed));
				} else {
					//Set to red
					mDeleteButton.setBackgroundColor(getActivity().getResources().getColor(R.color.newTempBlueHeader));
				}
			}
		});
				
		configureAwakeButton(v);
		configureRestSoundButton(v);		
		setTutorialListener(v);
		setBackupListeners(v);
		
		
		return v;
	}
	
	private void setTutorialListener(View v) {
		View tutButton = v.findViewById(R.id.f_settings_tutorial);
		tutButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), TutorialActivity.class);
				startActivity(i);
			}
		});
	}
	
	/*
	 * Sets listeners on our backup buttons.
	 */
	private void setBackupListeners(View inflatedLayout) {
		View backupButton = inflatedLayout.findViewById(R.id.f_settings_backupDatabase);
		View restoreButton = inflatedLayout.findViewById(R.id.f_settings_restoreDatabase);
		
		backupButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Reason for max_routines-1 is we only want to show the add if they have 1 less than the limit (as the
				//add menu can let them add the next).
				if (LicenseManager.staticHasPro(getActivity())) {
					ConfirmCreateBackupDialog toShow = new ConfirmCreateBackupDialog();
					launchDialogFragment(toShow, "purchaseproconfirm");
				} else {
					PurchaseProConfirmDialog toShow = PurchaseProConfirmDialog.newInstance("This feature requires LiftLog Pro.", "Would you like to upgrade now?");
					launchDialogFragment(toShow, "purchaseproconfirm");
				}
			}
		});
		
		restoreButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Backup from live to store
				if (LicenseManager.staticHasPro(getActivity())) {
					ConfirmRestoreBackupDialog toShow = new ConfirmRestoreBackupDialog();
					launchDialogFragment(toShow, "purchaseproconfirm");
				} else {
					PurchaseProConfirmDialog toShow = PurchaseProConfirmDialog.newInstance("This feature requires LiftLog Pro.", "Would you like to upgrade now?");
					launchDialogFragment(toShow, "purchaseproconfirm");
				}
			}
		});
	}
	
	public void configureAwakeButton(View layout) {
		//Get our checkbox
		mKeepAwakeCheckBox = (CheckBox) layout.findViewById(R.id.f_settings_keepAwakeCheckBox);
		
		//Initialize its value
		mKeepAwakeCheckBox.setChecked(isKeepAwakeEnabled());
		
		//Set its listener
		mKeepAwakeCheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Check if we're now checked or not
				
				if (mKeepAwakeCheckBox.isChecked()) {
					setKeepAwakeEnabled(true);
				} else {
					setKeepAwakeEnabled(false);
				}
			}
		});
		
	}
	
	public void configureRestSoundButton(View layout) {
		//Get our checkbox
		mRestSoundCheckBox = (CheckBox) layout.findViewById(R.id.f_settings_timerSoundCheckbox);
		
		//Initialize its value
		mRestSoundCheckBox.setChecked(isRestSoundEnabled());
		
		//Set its listener
		mRestSoundCheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Check if we're now checked or not
				
				if (mRestSoundCheckBox.isChecked()) {
					setRestSoundEnabled(true);
				} else {
					setRestSoundEnabled(false);
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
	 * Processes returns from our dialogs.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DELETE_DATA) {
			//Data  has been deleted. Set the checkbox to empty
			mDeleteCheckBox.setChecked(false);
		}
		else {
			//Unexpected code returned.
			throw new RuntimeException("Received unexpected request and/or result code. Request code is " + requestCode + " result code is " + resultCode);
		}
	}
	
	/*
	 * Creates and launches a dialog fragment. This is used in cases where we do want our current
	 * fragment to be targeted.
	 */
	protected void launchDialogFragmentTarget(DialogFragment dialogToShow, String dialogTag, int targetRequest) {
		dialogToShow.setTargetFragment(this, targetRequest);
		
		launchDialogFragment(dialogToShow, dialogTag);
	}

	
	public static class ConfirmDeleteDialog extends ConfirmationDialog {
		
		View mInflatedLayout;
		
		public String getDialogTitle() {
			return "Delete User Data";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Delete the db
			getActivity().getApplicationContext().deleteDatabase(LiftLogDBMaster.DB_NAME);
			LiftLogDBAPI.get(getActivity()).resetDBMaster();
			
			//Create our sample workouts
			//new WorkoutSamples(getActivity()).createSampleRoutines();
			
			//Post a toast
			deleteDoneToast();
			
			//Call on the onactivityresult so it can clear the textview.
			getTargetFragment().onActivityResult(REQUEST_DELETE_DATA, RESULT_DELETE_DATA, null);
			
			return true;
		}
		
		/*
		 * Posts a toast that deletion is complete.
		 */
		private void deleteDoneToast() {
			Context context = getActivity().getApplicationContext();
			CharSequence text = "All user data has been deleted";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();	
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
			setConfirmTopText("Are you sure you want to delete all data and restore this application to its factory defaults? This operation is irreversible.");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
	}
	
	/*
	 * Gets our preference for whether we're always awake.
	 */
	protected boolean isKeepAwakeEnabled() {		
		return getActivity().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE).getBoolean(SettingsFragment.SPREF_ALWAYSON_MODE, false);
	}
	
	/*
	 * Sets our list sort mode value in the Shared Preferences.
	 */
	protected void setKeepAwakeEnabled(boolean setAwake) {
		getActivity().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE).edit().putBoolean(SettingsFragment.SPREF_ALWAYSON_MODE, setAwake).commit();
	}
	
	/*
	 * Gets our preference for whether we play a timer on rest expiration.
	 */
	public boolean isRestSoundEnabled() {		
		return getActivity().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE).getBoolean(SettingsFragment.SPREF_EXPIRE_SOUND, false);
	}
	
	/*
	 * Sets our list sort mode value in the Shared Preferences.
	 */
	public void setRestSoundEnabled(boolean setAwake) {
		getActivity().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE).edit().putBoolean(SettingsFragment.SPREF_EXPIRE_SOUND, setAwake).commit();
	}
	
	
	public static class ConfirmCreateBackupDialog extends ConfirmationDialog {
		
		View mInflatedLayout;
		
		public String getDialogTitle() {
			return "Backup Database";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Backup from live to store
			String dbStore = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liftlog.sqlite";
			String dbLive = "/data/data/com.plined.liftlog/databases/liftlog.sqlite";
			BackupManager.copyFile(dbLive, dbStore, getActivity());
			
			return true;
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
			setConfirmTopText("Would you like to take a backup of your current workout database? This will overwrite any existing backups you have.");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
	}
	
	public static class ConfirmRestoreBackupDialog extends ConfirmationDialog {
		
		View mInflatedLayout;
		
		public String getDialogTitle() {
			return "Restore Database";
		}
		
		public void negativeCallback() {
			
		}
		
		public boolean positiveCallback() {
			//Backup from live to store
			String dbStore = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liftlog.sqlite";
			String dbLive = "/data/data/com.plined.liftlog/databases/liftlog.sqlite";
			BackupManager.copyFile(dbStore, dbLive, getActivity());
			
			return true;
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
			setConfirmTopText("Would you like to overwrite your current database with the database you have backed up? This operation is irreversible.");
			setConfirmBottomText(null);
			
			return mInflatedLayout;
		}
		
		
	}
	

}
