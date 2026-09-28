package com.tabnova.aidashboard.Knox.startup;


import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class AdminReceiver extends DeviceAdminReceiver {
	
	 void showToast(Context context, String msg) {
	       String status = "Super Kiosk";
	      //Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
	    }

	    @Override
	    public void onEnabled(Context context, Intent intent) {

            showToast(context, "Super Kiosk");
	    }

	    @Override
	    public CharSequence onDisableRequested(Context context, Intent intent) {
	        return "disable warning"; //context.getString(R.string.admin_receiver_status_disable_warning);
	    }

	    @Override
	    public void onDisabled(Context context, Intent intent) {

            showToast(context, "Super Kiosk");
	    }

	    @Override
	    public void onPasswordChanged(Context context, Intent intent) {
	        showToast(context, "Super Kiosk");
	    }


}
