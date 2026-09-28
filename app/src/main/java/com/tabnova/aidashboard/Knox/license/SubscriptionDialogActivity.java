package com.tabnova.aidashboard.Knox.license;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.startup.AdminReceiver;
import com.tabnova.aidashboard.Knox.startup.BeginActivity;


public class SubscriptionDialogActivity extends Activity {

    Handler mHandler = new Handler();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_error_dialog);
        mContext = this;
        String cide_check = CustomDashboardApplication.getString(mContext, "subscription_code");
        if(cide_check.length() >1 ){
            startService(new Intent(mContext, SubscriptionService.class));
            finish();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter the code you received");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                if(m_Text != null && (m_Text.length() > 0 && m_Text.length() < 100 )) {
                    CustomDashboardApplication.setString(mContext, "subscription_code", m_Text);
                    startService(new Intent(mContext, SubscriptionService.class));
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(mContext, BeginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        SubscriptionDialogActivity.this.finish();
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
