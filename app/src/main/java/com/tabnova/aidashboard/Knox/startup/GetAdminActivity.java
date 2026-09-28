package com.tabnova.aidashboard.Knox.startup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class GetAdminActivity extends Activity {
	private static final String TAG= "ODMGetAdminReceiver";

	private Button enable;
	static final int RESULT_ENABLE = 1;

	DevicePolicyManager deviceManger;
	ActivityManager activityManager;
	ComponentName compName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_admin);

		deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		compName = new ComponentName(this, AdminReceiver.class);
		//lock = (Button) findViewById(R.id.lock);
		//lock.setOnClickListener(this);
//		enable = (Button) findViewById(R.id.btnEnable);
//		enable.setOnClickListener(this);

		Log.d(TAG, "Attempting to enable admin");
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
	//	intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "OSP requires admin access for locking and wiping the device.");
		startActivityForResult(intent, RESULT_ENABLE);
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RESULT_ENABLE:
			if (resultCode == Activity.RESULT_OK) {
				Log.i("DeviceAdminSample", "Admin enabled!");
				//Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
				//this.startActivity(intent);
				Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else {
				Log.i("DeviceAdminSample", "Admin enable FAILED!");
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}