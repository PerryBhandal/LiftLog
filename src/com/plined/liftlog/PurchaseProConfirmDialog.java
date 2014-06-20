package com.plined.liftlog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

	
public class PurchaseProConfirmDialog extends ConfirmationDialog {
	
	View mInflatedLayout;
	
	private static String TOPTEXT = "com.plined.liftlog.purchaseproconfirmdialog.toptext";
	private static String BOTTOMTEXT = "com.plined.liftlog.purchaseproconfirmdialog.bottomtext";
	
	
	public static PurchaseProConfirmDialog newInstance(String topText, String bottomText) {
		//Create our bundle
		Bundle args = new Bundle();
		//Put the routine's ID in
		args.putString(TOPTEXT, topText);
		args.putString(BOTTOMTEXT, bottomText);
		
		//Create our routine fragment
		PurchaseProConfirmDialog toLaunch = new PurchaseProConfirmDialog();
		
		//Attach our bundle
		toLaunch.setArguments(args);
		
		return toLaunch;
	}
	
	public String getDialogTitle() {
		return "LiftLog Pro";
	}
	
	public void negativeCallback() {
		
	}
	
	public boolean positiveCallback() {
		LicenseManager licenseMan = new LicenseManager(getActivity());
		licenseMan.bindIabHelperDoPurchase();
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
		setConfirmTopText(getArguments().getString(TOPTEXT));
		setConfirmBottomText(getArguments().getString(BOTTOMTEXT));
		
		return mInflatedLayout;
	}
	
	
}
	