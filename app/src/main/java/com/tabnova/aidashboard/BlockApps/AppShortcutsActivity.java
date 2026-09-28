package com.tabnova.aidashboard.BlockApps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.SessionManager;

@TargetApi(Build.VERSION_CODES.N)
public class AppShortcutsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (WatchingAccessibilityService.getInstance() == null || !Settings.canDrawOverlays(this)) {
            SPHelper.setIsShowWindow(this, true);
            Intent intent;
            if (!SessionManager.getInstance(this).getIsParentMode()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                Log.e(AppShortcutsActivity.class.getSimpleName(), "onClick: @30");
                intent = new Intent(this, CustomDashboardSettings.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        boolean isShow = !SPHelper.isShowWindow(this);
        SPHelper.setIsShowWindow(this, isShow);
        if (!isShow) {
            //TasksWindow.dismiss(this);
            // NotificationActionReceiver.showNotification(this, true);
        } else {
            //TasksWindow.show(this, getPackageName() + "\n" + getClass().getName());
            //NotificationActionReceiver.showNotification(this, false);
        }
        sendBroadcast(new Intent(CustomDashboardApplication.ACTION_STATE_CHANGED));
        finish();
    }
}