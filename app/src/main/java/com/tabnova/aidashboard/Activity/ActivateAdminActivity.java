package com.tabnova.aidashboard.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Knox.license.LicenseManagerActivity;
import com.tabnova.aidashboard.Knox.license.LoginService;
import com.tabnova.aidashboard.Knox.startup.AdminReceiver;
import com.tabnova.aidashboard.Knox.startup.GetAdminActivity;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.R;

public class ActivateAdminActivity extends AppCompatActivity {

    private String TAG = ActivateAdminActivity.class.getSimpleName();

    //CONTEXT
    Context context;

    //BUTTON
    Button btnActivateAdmin;

    //TEXT VIEW
    TextView tvSkip;

    //CHECK BOX
    CheckBox cbTermsCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_admin);
        // Edge-to-edge: purple root, default (white) bar icons.
        Utils.applyEdgeToEdge(this, null, false, false, false, false);

        //INIT CONTEXT
        context = this;

        btnActivateAdmin = findViewById(R.id.btn_activate_admin);
        tvSkip = findViewById(R.id.tv_skip_activate_admin);
        cbTermsCondition = findViewById(R.id.cb_terms_condition);

        cbTermsCondition.setChecked(true);

        tvSkip.setOnClickListener(v -> {
            if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                Intent intent = new Intent(ActivateAdminActivity.this, AppPermissionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                CustomDashboardApplication.redirectOnActivity(context, true);
            }
        });

        btnActivateAdmin.setOnClickListener(v -> {
            if (cbTermsCondition.isChecked()) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    SuperLockState superLockState = new SuperLockState(context);
                    if (superLockState != null && superLockState.supportedActivation()) {
                        if (superLockState != null && !superLockState.isESDKLicenseActivated()) {
                            CustomDashboardApplication.setInt(context, Consts.IS_KNOX_SUPPORT, 0);
                            //For Samsung Knox
                            Log.d(TAG, "Getting admin permissions");
                            DevicePolicyManager mDPM;
                            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                            ComponentName mDeviceAdmin;
                            mDeviceAdmin = new ComponentName(ActivateAdminActivity.this, AdminReceiver.class);
                            if (mDPM.isAdminActive(mDeviceAdmin)) {
                                Log.d(TAG, "We have admin");

                                SuperLockState mSuperLockState = SuperLockState.getInstance(ActivateAdminActivity.this);

                                if (mSuperLockState != null && !mSuperLockState.isESDKLicenseActivated()) {
                                    //startService(new Intent(ActivateAdminActivity.this, LoginService.class));
                                    startActivity(new Intent(context, LicenseManagerActivity.class));
                                }
                            } else {
                                Log.d(TAG, "We need admin");
                                Intent intent = new Intent(getApplicationContext(), GetAdminActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            CustomDashboardApplication.setInt(context, Consts.IS_KNOX_SUPPORT, 1);
                        }
                    } else {
                        if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                            Intent intent = new Intent(ActivateAdminActivity.this, AppPermissionsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            CustomDashboardApplication.redirectOnActivity(context, false);
                        }
                    }
                }
            } else {
                Toast.makeText(context, R.string.please_accept_terms_conditions, Toast.LENGTH_SHORT).show();
            }
        });
    }
}