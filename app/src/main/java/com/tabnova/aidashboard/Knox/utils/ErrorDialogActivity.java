package com.tabnova.aidashboard.Knox.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.startup.AdminReceiver;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.startup.BeginActivity;
import com.tabnova.aidashboard.R;


public class ErrorDialogActivity extends Activity {
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_error_dialog);

        String title = "Error";
        String message = "Contact administrator";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("title");
            message = extras.getString("message");
        }

        AlertDialog alertDialog = new AlertDialog.Builder(ErrorDialogActivity.this).create();
        String versionInfo = "";

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            versionInfo = "\nVersion = " + pInfo.versionName + " - Code = " + pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Setting Dialog Title
        alertDialog.setTitle(title + versionInfo);


        alertDialog.setMessage(message);


        // Setting Dialog Message


        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setCancelable(false);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry", (dialog, which) -> {
            // Write your code here to execute after dialog closed
            setLicenseIndex(0);
            setRetryCount(0);

            ErrorDialogActivity.this.finish();
            PendingIntent myActivity = Utils.restartPendingIntent();

            AlarmManager alarmManager;
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    15000, myActivity);
            System.exit(2);
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Uninstall", (dialog, which) -> {
            // Write your code here to execute after dialog closed
            // ErrorDialogActivity.this.finish();
            // System.exit(0);
            uninstallDisabler();
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        ErrorDialogActivity.this.finish();
        super.onBackPressed();
    }

    private void uninstallDisabler() {
        adminDisable();
        Uri packageURI = Uri.parse("package:" + getApplicationContext().getPackageName());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);

    }

    private void adminDisable() {
        ComponentName admin = new ComponentName(this, AdminReceiver.class);
        DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        DPM.removeActiveAdmin(admin);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 1000);
    }


    private int getLicenseIndex() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt("license_index", 0);
    }

    private void setLicenseIndex(int cont) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("license_index", cont);
        editor.commit();
    }


    private void setRetryCount(int cont) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("retry", cont);
        editor.commit();
    }
}
