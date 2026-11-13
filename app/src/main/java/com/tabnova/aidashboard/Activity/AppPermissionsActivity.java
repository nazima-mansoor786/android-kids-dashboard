package com.tabnova.aidashboard.Activity;

import static com.tabnova.aidashboard.CustomDashboardApplication.isAccessibilitySettingsOn;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.R;

public class AppPermissionsActivity extends AppCompatActivity {

    //CONTEXT
    Context context;

    //BUTTON
    Button btnContinue;

    // INTEGER
    public static int request = 0;

    private boolean isContinue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permissions);
        // Edge-to-edge: purple root, default (white) bar icons.
        Utils.applyEdgeToEdge(this, null, false, false, false, false);

        //INIT CONTEXT
        context = this;

        btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            isContinue = true;
            if (!CustomDashboardApplication.checkAllPermissions(AppPermissionsActivity.this)) {
                /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                    requestPermissions();
                } else*/ if (Utils.isMarshMallow() && (/*ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ||*/ ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                    requestPermissions();
                } else if (!isAccessibilitySettingsOn(context)) {
                    Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission. It's helpful to track background running applications.");
                } else {
                    if (!Settings.canDrawOverlays(context)) {
                        Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        try {
                            startActivity(intent1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        redirectSettingScreen();
                    }
                }
            } else {
                redirectSettingScreen();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isContinue) {
            if (!CustomDashboardApplication.checkAllPermissions(this)) {
                /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                    requestPermissions();
                } else*/ if (Utils.isMarshMallow() && (/*ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ||*/ ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                    requestPermissions();
                } else if (!isAccessibilitySettingsOn(context)) {
                    Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission. It's helpful to track background running applications.");
                } else {
                    if (!Settings.canDrawOverlays(context)) {
                        Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        try {
                            startActivity(intent1);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                redirectSettingScreen();
            }
        }
    }

    private void redirectSettingScreen() {
        if (CustomDashboardApplication.isMyLauncherDefault(context)) {
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
        }

        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), Consts.PACKAGE_NAME);
        boolean granted = (mode == AppOpsManager.MODE_ALLOWED);

        if (granted) {
            CustomDashboardApplication.setInt(context, Consts.user_statics_permission_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.user_statics_permission_status, 0);
        }

        if (CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
            CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 1);
            CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
            CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 0);
        }

        /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
        } else*/ if (Utils.isMarshMallow() && /*ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&*/ ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
        }

        // overlayViewPermissionStatus = true;
        if (Settings.canDrawOverlays(context)) {
            CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 0);
        }

        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.app_permission_done));
        CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

        //Toast.makeText(context, "App Permission redirect", Toast.LENGTH_SHORT).show();
        CustomDashboardApplication.redirectOnActivity(context, false);
    }

    // REQUEST PERMISSIONS
    public void requestPermissions() {
        String[] permit;
        /*if (Utils.isTiramisu()) {
            permit = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.EXPAND_STATUS_BAR,
                    Manifest.permission.WRITE_SETTINGS
            };
        } else {
            permit = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.EXPAND_STATUS_BAR,
                    Manifest.permission.WRITE_SETTINGS
            };
        }*/

        permit = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.EXPAND_STATUS_BAR,
                Manifest.permission.WRITE_SETTINGS
        };
        ActivityCompat.requestPermissions(this, permit, request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == request && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        if (!SuperLockState.getInstance(context).isESDKLicenseActivated()) {

                        } else {
                            //CustomDashboardApplication.enableSamsungKiosk(context);
                        }
                    }
                } else*/ if (Utils.isMarshMallow() && /*ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&*/ ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        if (!SuperLockState.getInstance(context).isESDKLicenseActivated()) {

                        } else {
                            //CustomDashboardApplication.enableSamsungKiosk(context);
                        }
                    }
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

            }
        }
    }
}