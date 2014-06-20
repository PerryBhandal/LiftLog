package com.plined.liftlog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LicenseManager {
	
	IabHelper mIabHelper;
	Context mActivityContext;
	static final String SPREF_LICENSE = "ll_license";
	static final String SPREF_KEY_PROLICENSE = "com.plined.liftlog.licensemanager.prolicense";
	static final String TAG = "LicenseManager";
	static final String SKU_PRO = "liftlog_pro_license";
	
	public LicenseManager(Context activityContext) {
		mActivityContext = activityContext;
		
	}
	
	/*
	 * Binds our iab helper to the googl eplay servers.
	 */
	public void bindIabHelperCheckLicense() {
		mIabHelper = new IabHelper(mActivityContext, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkxUjqKb0pXL8/5QBsMbAGyUdaYtfx10d+Bon4E66IMKwULSeNNEVHANL2I8GUQUaG/rvm7zFZQA1KbHY5sBKZPvXujW5F8VoP8YRpcTBE2CfJLB0pHrIQbVjJ2Bli9SI0MkA5cuwRSAJgKLWLwmAl/2uXpjIfq0wb3EKXaxOUa2x/LhNhDOqbHu0PBosvIFB0PwHat0QVexJfLfrxebYWfUz9ZBkaiwXEF6LJpjH3ClVNdCc/IgNDJ6HeeyF/CAbcCqmpNih0y2xXEIaEWygoXAlSbj0HN1WoFhpVagp7o7EqapECfsgzbqJMTCmuCzZhd09SEMbVSjeeyMWDE8TTQIDAQAB");
		mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
				} else {
					updateLicenseState();
				}
				
			}
		});
	}
	
	public void bindIabHelperDoPurchase() {
		mIabHelper = new IabHelper(mActivityContext, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkxUjqKb0pXL8/5QBsMbAGyUdaYtfx10d+Bon4E66IMKwULSeNNEVHANL2I8GUQUaG/rvm7zFZQA1KbHY5sBKZPvXujW5F8VoP8YRpcTBE2CfJLB0pHrIQbVjJ2Bli9SI0MkA5cuwRSAJgKLWLwmAl/2uXpjIfq0wb3EKXaxOUa2x/LhNhDOqbHu0PBosvIFB0PwHat0QVexJfLfrxebYWfUz9ZBkaiwXEF6LJpjH3ClVNdCc/IgNDJ6HeeyF/CAbcCqmpNih0y2xXEIaEWygoXAlSbj0HN1WoFhpVagp7o7EqapECfsgzbqJMTCmuCzZhd09SEMbVSjeeyMWDE8TTQIDAQAB");
		mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
				} else {
					beginProPurchase();
				}
				
			}
		});
	}
	
	public static boolean staticHasPro(Context appContext) {
		//Get instance of this
		LicenseManager licenseMan = new LicenseManager(appContext);
		boolean hasLicense = licenseMan.hasProLicense();
		return hasLicense;
	}
	
	/*
	 * Disposes of the license manager and its dependent connections. This should be called by in the onDestory() of any
	 * activity that has a license manager.
	 */
	public void dispose() {
		if (mIabHelper != null) {
			mIabHelper.dispose();
		}
		mIabHelper = null;
	}
	
	/*
	 * Updates all of the user's licenses from Google's servers to the spref file.
	 */
	private void updateLicenseState() {
		//Get the license state from Google
		mIabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
			
			@Override
			public void onQueryInventoryFinished(IabResult result, Inventory inv) {
				if (result.isSuccess()) {
					//Figure out if they have a pro license
					boolean hasPro = inv.hasPurchase(SKU_PRO);
					
					//Update our license state
					setHasProLicense(hasPro);
					
				} else {
				}
			}
		});
	}
	
	/*
	 * Starts the purchase flow to purchase the pro version of the app.
	 */
	private void beginProPurchase() {
		/*
		 * Start the purchase flow, once its complete (and we get a response) update our license state.
		 */
		//Create our listener for once the purchase is done
		IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
			
			@Override
			public void onIabPurchaseFinished(IabResult result, Purchase info) {
				if (result.isFailure()) {
					dispose();
					return;
				}
				
				if (info.getSku().equals(SKU_PRO)) {
					setHasProLicense(true);
					dispose();
				}
			}
		};
		
		//We cast the mActivityContext to an activity as activity derives from context.
		mIabHelper.launchPurchaseFlow(((Activity) mActivityContext), SKU_PRO, 10001, purchaseFinishedListener);
		
	}
	
	/*
	 * Gets the license value from the shared preferences.
	 */
	public boolean hasProLicense() {		
		return mActivityContext.getSharedPreferences(SPREF_LICENSE, Context.MODE_PRIVATE).getBoolean(SPREF_KEY_PROLICENSE, false);
	}
	
	/*
	 * Sets the license value in our shared preference.
	 */
	public void setHasProLicense(boolean hasProLicense) {
		mActivityContext.getSharedPreferences(SPREF_LICENSE, Context.MODE_PRIVATE).edit().putBoolean(SPREF_KEY_PROLICENSE, hasProLicense).commit();
	}
	
	/*
	 * Wraps the handleActivityResult method in our helper.
	 */
	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		return mIabHelper.handleActivityResult(requestCode, resultCode, data);
	}
}
