package com.tabnova.aidashboard.Knox.license;

import static com.samsung.android.knox.application.ApplicationPolicy.PERMISSION_POLICY_STATE_GRANT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.knox.AppIdentity;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.application.ApplicationPolicy;
import com.samsung.android.knox.license.EnterpriseLicenseManager;
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.services.ProvisionService;
import com.tabnova.aidashboard.Knox.services.ServiceManager;
import com.tabnova.aidashboard.Knox.startup.BeginActivity;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;

import java.util.ArrayList;
import java.util.List;


public class LicenseManagerActivity extends Activity {
    private Context mContext;
    ProgressDialog progress;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int retryCount = getRetryCount();

            if (retryCount > 3) {
//                runOnUiThread(() -> Toast.makeText(LicenseManagerActivity.this, "License activation failed.", Toast.LENGTH_SHORT).show());
                CustomDashboardApplication.redirectOnActivity(LicenseManagerActivity.this, false);
                //UsefulUtility.showErrorDialog(mContext, errorCode, "Retry ?");
            } else {
                ServiceManager.logToCloud(mContext, "Retrying License Activation", "Error");
//                runOnUiThread(() -> Toast.makeText(LicenseManagerActivity.this, "License activation failed.", Toast.LENGTH_SHORT).show());
                int count = retryCount + 1;
                setRetryCount(count);
                //restartApp();
                activateLicense();
            }
        }
    };

    private boolean isRegistered = false;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_license_manager);
        // dialog = ProgressDialog.show(this, "", "License Activation wait ...", true);

        this.mContext = this;
        progress = new ProgressDialog(mContext);
        progress.setMessage("License Activation wait ...");
        progress.setCancelable(true);
        if (!LicenseManagerActivity.this.isFinishing() && !LicenseManagerActivity.this.isDestroyed()) {
            try {
                progress.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ServiceManager.logToCloud(mContext, "Requesting Licences activation requested ", "OK");

        handler.postDelayed(runnable, 5000);

        activateLicense();

        IntentFilter filter = new IntentFilter();
        filter.addAction(KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS);
        filter.addAction(EnterpriseLicenseManager.ACTION_LICENSE_STATUS);
        if (Utils.isTiramisu()) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
        isRegistered = true;
    }

    private void activateLicense() {
        try {
            EnterpriseLicenseManager elm = EnterpriseLicenseManager.getInstance(getApplicationContext());
//            elm.activateLicense("1443F80685DF3D8AD58154200D26A892A3AE9C6DA4663F5CF388C3CE7DACA589F15E575A433D9181EEC185F16FDA8964F49E08D37DB8D7C5BBF3DD1636B6E92E");
            //elm.activateLicense("AC2E6CFE2C2C7F9E139D9F0D9B716F1F9A35A5C37A4CD3DE14867E22BF5FC26BF3406E780A145306EC315521974BE7B4A2CDCD4841539DB6415FA4507FB7A8CE", "com.tabnova.samsung_dashboard");
        } catch (Exception e) {
            ServiceManager.logToCloud(mContext, "ESDK exception ", "Check system");
        }

        try {
            KnoxEnterpriseLicenseManager klm = KnoxEnterpriseLicenseManager.getInstance(getApplicationContext());
            klm.activateLicense(Const.KNOX_LICENSE_SDK_KEY);
            //klm.activateLicense("KLM06-0CXIY-7PTK9-J0W5E-NRJPM-APER9");
        } catch (Exception e) {
            ServiceManager.logToCloud(mContext, "Csdk exception ", "Check system");
        }
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        try {
            if (progress != null && progress.isShowing())
                progress.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isRegistered) {
            isRegistered = false;
            unregisterReceiver(mReceiver);
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(EnterpriseLicenseManager.ACTION_LICENSE_STATUS)) {
                Bundle extras = intent.getExtras();
                String status = extras.getString(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
                int err_code = extras.getInt(EnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE);
                //Toast.makeText(context, "EnterpriseLicenseManager.ACTION_LICENSE_STATUS Status : " + status + " : Error Code : " + err_code, Toast.LENGTH_SHORT).show();
                if (err_code == EnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT) {
                    setAllPermission(mContext);
                    CustomDashboardApplication.restart(context);
                    return;
                } else if (err_code != EnterpriseLicenseManager.ERROR_NONE) {
                    handler.postDelayed(runnable, 5000);
                    ServiceManager.logToCloud(mContext, "Enterprise License error code " + err_code, "failed");
                }

                startSettings(context);

            } else if (action.equals(KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS)) {
                Bundle extras = intent.getExtras();
                String status = extras.getString(KnoxEnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
                int err_code = extras.getInt(KnoxEnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE);
                //Toast.makeText(context, "KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS Status : " + status + " : Error Code : " + err_code, Toast.LENGTH_SHORT).show();
                if (err_code == 0) {
                    setAllPermission(mContext);
                } else if (err_code == KnoxEnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT) {
                    CustomDashboardApplication.restart(context);
                    return;
                } else if (err_code != KnoxEnterpriseLicenseManager.ERROR_NONE) {
                    handler.postDelayed(runnable, 5000);
                    ServiceManager.logToCloud(mContext, "CSDK Enterprise License error code " + err_code, "failed");
                }
            }

            SuperLockState superLockState = SuperLockState.getInstance(mContext);
            if (superLockState != null && superLockState.isESDKLicenseActivated()) {
                handler.removeCallbacks(runnable);
                Log.e("License Activity", "run: @106");
                Intent prointent = new Intent(context, ProvisionService.class);
                prointent.putExtra("ProvisionStatus", "Provisioned");
                context.startService(prointent);
                ServiceManager.logToCloud(mContext, "License all ok (C + E )", "OK");
                SessionManager.getInstance(mContext).setIsLicenseEnabled(true);
                CustomDashboardApplication.setInt(mContext, Consts.app_permission_status, 1);
                //Toast.makeText(context, "License Manager @207", Toast.LENGTH_SHORT).show();
                Intent i;
//                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                setRetryCount(0);
                CustomDashboardApplication.redirectOnActivity(LicenseManagerActivity.this, false);

                try {
                    if (progress != null && progress.isShowing()) {
                        progress.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 5000);
            }
        }
    };

    private int getRetryCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt("retry", 0);
    }

    private void setRetryCount(int cont) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("retry", cont);
        editor.commit();
    }

    private void restartApp() {
        if (isRegistered) {
            isRegistered = false;
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        PendingIntent myActivity = Utils.restartPendingIntent();

        AlarmManager alarmManager;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                15000, myActivity);
        System.exit(2);

    }

    void setAllPermission(Context context) {
        List<String> runtimePermissions = new ArrayList<String>();
        runtimePermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        runtimePermissions.add("android.permission.ACCESS_FINE_LOCATION");
        runtimePermissions.add("android.permission.ACCESS_WIFI_STATE");
        runtimePermissions.add("android.permission.CHANGE_WIFI_STATE");
        runtimePermissions.add("android.permission.INTERNET");
        runtimePermissions.add("android.permission.ACCESS_NETWORK_STATE");
        runtimePermissions.add("android.permission.WAKE_LOCK");
        runtimePermissions.add("android.permission.VIBRATE");
        runtimePermissions.add("android.permission.GET_ACCOUNTS");
        runtimePermissions.add("com.android.browser.permission.WRITE_HISTORY_BOOKMARKS");
        runtimePermissions.add("com.google.android.c2dm.permission.RECEIVE");
        runtimePermissions.add("android.permission.sec.MDM_APP_MGMT");
        runtimePermissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
        runtimePermissions.add("android.permission.READ_EXTERNAL_STORAGE");
        runtimePermissions.add("com.android.launcher.permission.INSTALL_SHORTCUT");
        runtimePermissions.add("com.android.launcher.permission.UNINSTALL_SHORTCUT");
        runtimePermissions.add("android.permission.WRITE_SETTINGS");
        runtimePermissions.add("android.permission.SET_WALLPAPER");
        runtimePermissions.add("android.permission.REQUEST_DELETE_PACKAGES");
        runtimePermissions.add("android.permission.SYSTEM_ALERT_WINDOW");
//        runtimePermissions.add("android.permission.READ_CONTACTS");
//        runtimePermissions.add("android.permission.WRITE_CONTACTS");
        runtimePermissions.add("android.permission.READ_PHONE_STATE");
        runtimePermissions.add("android.permission.BLUETOOTH");
        runtimePermissions.add("android.permission.BLUETOOTH_ADMIN");
        runtimePermissions.add("android.permission.CAMERA");
        runtimePermissions.add("android.permission.EXPAND_STATUS_BAR");
        runtimePermissions.add("android.permission.CAMERA");
        runtimePermissions.add("android.permission.KILL_BACKGROUND_PROCESSES");

        CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);

        EnterpriseDeviceManager edm = EnterpriseDeviceManager.getInstance(context);
        ApplicationPolicy appPolicy = edm.getApplicationPolicy();
        AppIdentity appIdentity = new AppIdentity(getApplicationContext().getPackageName(), null);
        try {
            appPolicy.applyRuntimePermissions(appIdentity, runtimePermissions, PERMISSION_POLICY_STATE_GRANT);
        } catch (Exception e) {

        }
    }

    void startSettings(Context context) {

        setAllPermission(context);

        if (CustomDashboardApplication.isDeviceSamsung()) {
            if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                CustomDashboardApplication.setInt(context, Consts.isSettingPopUp, 2);
               /* Intent intent = new Intent(Consts.app_permission_done);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
                CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

                //Toast.makeText(context, "License Manager @319", Toast.LENGTH_SHORT).show();
                Intent openSettings;
//                if (CustomDashboardApplication.checkAllPermissions(getApplicationContext())) {
                setRetryCount(0);
                CustomDashboardApplication.redirectOnActivity(context, false);
                //finish();
            }
        }
    }
}