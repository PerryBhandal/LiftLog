package com.plined.liftlog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.widget.Toast;

public class BackupManager {
	
	public static void copyFile(String srcPath, String destinationPath, Context callingActivity) {
		//Create our input streams
		FileInputStream dbIn = null;
		FileOutputStream dbOut = null;
		
		
		try {
			dbIn = new FileInputStream(srcPath);
			dbOut = new FileOutputStream(destinationPath);
			int c;
			
			while ((c = dbIn.read()) != -1) {
				dbOut.write(c);
			}
		} catch (IOException e) {
			postToast(callingActivity, "Backup Failed: " + e.getMessage());
			return;
		}
		finally {
			try {
				if (dbIn != null) {
					dbIn.close();
				}
				
				if (dbOut != null) {
					dbOut.close();
				}
			} catch (IOException e) {
				postToast(callingActivity, "Backup Failed: " + e.getMessage());
				return;
			}
		}
		
		//Backup worked. Post it.
		postToast(callingActivity, "Success!");
	}
	
	private static void postToast(Context callingActivity, String toPost) {
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(callingActivity, toPost, duration);
		toast.show();
	}
}
