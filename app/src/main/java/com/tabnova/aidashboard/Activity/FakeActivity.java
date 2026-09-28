package com.tabnova.aidashboard.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;
import java.util.List;

public class FakeActivity extends BaseActivity {

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake);
        context = this;
    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, getPackageName());

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("FakeActivity", "onResume: ");
        // Toast.makeText(context, "FakeActivity : FakeActivity", Toast.LENGTH_SHORT).show();
        if (isMyLauncherDefault()) {
            startActivity(new Intent(context, Test.class));
            finish();
        } else {
            if (!CustomDashboardApplication.isDeviceSamsungActivated())
                resetPreferredLauncherAndOpenChooser(context);
        }
    }


    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(selector);

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.e("Fake Activity", "run: @86");
            CustomDashboardApplication.redirectOnActivity(FakeActivity.this, false);
        }
    }
}
