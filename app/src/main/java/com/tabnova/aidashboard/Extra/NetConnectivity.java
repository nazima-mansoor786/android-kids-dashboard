package com.tabnova.aidashboard.Extra;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tabnova.aidashboard.CustomDashboardApplication;


public class NetConnectivity {

	public static boolean isOnline(Context context) {

		ConnectivityManager cm = (ConnectivityManager) CustomDashboardApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();

		if (netinfo != null && netinfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}
