package com.tabnova.aidashboard.Knox.startup;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tabnova.aidashboard.Activity.AppPermissionsActivity;
import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Knox.license.Const;
import com.tabnova.aidashboard.Knox.license.LicenseManagerActivity;
import com.tabnova.aidashboard.R;

public class StartupActivity extends Activity {
    private AlertDialogManager alert = new AlertDialogManager();
    Context mContext;
    DevicePolicyManager mDPM;


    private static final String TAG = "ODMStartupActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        SuperLockState mSuperLockState = SuperLockState.getInstance(this);

//		if(! mSuperLockState.iseSdkSupported()) {
//			//
//			alert.showAlertDialog(StartupActivity.this, getString(R.string.devicenotsupported), getString(R.string.contactosp), false);
//			// stop executing code by return
//			return;
//		}

        Log.d(TAG, "Getting admin permissions");

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdmin;
        mDeviceAdmin = new ComponentName(this, AdminReceiver.class);
        if (mDPM.isAdminActive(mDeviceAdmin)) {
            Log.d(TAG, "We have admin");

            if (mSuperLockState != null && !mSuperLockState.isESDKLicenseActivated() && !CustomDashboardApplication.getIgnoreLicense(mContext, Const.IGNORE_KNOX_LICENSE)) {
                //startService(new Intent(this, LoginService.class));
                try {
                    Intent intent = new Intent(StartupActivity.this, LicenseManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("StartupActivity", "run: @57");
                CustomDashboardApplication.redirectOnActivity(this, true);
            }
        } else {
            Log.d(TAG, "We need admin");
            Intent intent = new Intent(getApplicationContext(), GetAdminActivity.class);
            this.startActivity(intent);
        }

        if (mSuperLockState != null && !mSuperLockState.supportedActivation()) {
            CustomDashboardApplication.setInt(StartupActivity.this, Consts.IS_KNOX_SUPPORT, 1);
            alert.showAlertDialog(StartupActivity.this, getString(R.string.deviceisold), getString(R.string.contactosp), false);
            // stop executing code by return
            return;
        }
    }
}