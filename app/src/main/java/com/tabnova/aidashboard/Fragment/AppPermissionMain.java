package com.tabnova.aidashboard.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Activity.FakeActivity;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.Adapter.AppPermissionListAdapter;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Model.AppPermissionModel;
import com.tabnova.aidashboard.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppPermissionMain extends Fragment implements AppPermissionListAdapter.OnPermissionSelectListener {

    // RECYCLER VIEW
    private RecyclerView recPermissionView;

    // ADAPTER
    private AppPermissionListAdapter appPermissionListAdapter;

    // ARRAY LIST
    private ArrayList<AppPermissionModel> appPermissionModels;

    // BOOLEAN
    private boolean launcherPermissionStatus;
    private boolean accessibilityPermissionStatus;
    public static boolean overlayViewPermissionStatus;
    private boolean runtimePermissionStatus;

    // BUTTON
    private Button btnContinue;

    // APP PERMISSIONS STRING ARRAY
    private final String[] permit = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.EXPAND_STATUS_BAR,
            Manifest.permission.WRITE_SETTINGS
    };

    // INTEGER
    public static int request = 0;
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 7890;

    // API MANAGER
    private APIManager apiManager;

    // CONTEXT
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        apiManager = new APIManager(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // VIEW
        View mainView = inflater.inflate(R.layout.app_intro_fragment, container, false);

        // INIT VIEW
        recPermissionView = mainView.findViewById(R.id.rec_permission_view);
        btnContinue = mainView.findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            if (CustomDashboardApplication.isMyLauncherDefault(context)) {
                launcherPermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 1);
            } else {
                launcherPermissionStatus = false;
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
                accessibilityPermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 1);
                CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 1);
            } else {
                accessibilityPermissionStatus = false;
                CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 0);
                CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
            }

            if (Utils.isTiramisu() &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                runtimePermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
            } else if (Utils.isMarshMallow() && Utils.isLessTiramisu() &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                runtimePermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
            } else {
                if (!Utils.isMarshMallow()) {
                    runtimePermissionStatus = true;
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
                } else {
                    runtimePermissionStatus = false;
                    CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                }
            }

            recPermissionView.setHasFixedSize(true);
            recPermissionView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            appPermissionModels = new ArrayList<>();

            //appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.usage_permission), usageStaticsPermissionStatus));
            appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.accebility_permission), accessibilityPermissionStatus));
            appPermissionModels.add(1, new AppPermissionModel(1, context.getResources().getString(R.string.runtime_permission), runtimePermissionStatus));
            appPermissionModels.add(2, new AppPermissionModel(2, "3." + context.getResources().getString(R.string.overlay_permission), overlayViewPermissionStatus));
            if (!CustomDashboardApplication.isDeviceSamsungActivated()) {
                appPermissionModels.add(3, new AppPermissionModel(3, "4. " + context.getResources().getString(R.string.defualt_home), launcherPermissionStatus));
            }

            appPermissionListAdapter = new AppPermissionListAdapter(context, appPermissionModels, AppPermissionMain.this);
            recPermissionView.setAdapter(appPermissionListAdapter);

            /* if (!usageStaticsPermissionStatus){
                Toast.makeText(context, context.getResources().getString(R.string.usage_statics_msg), Toast.LENGTH_SHORT).show();
            } else*/
            if (!accessibilityPermissionStatus) {
                Toast.makeText(context, context.getResources().getString(R.string.accessibility_msg), Toast.LENGTH_SHORT).show();
            } else {
                if (!runtimePermissionStatus) {
                    Toast.makeText(context, context.getResources().getString(R.string.storage_msg), Toast.LENGTH_SHORT).show();
                    //requestPermission();
                } else {
                    /*  if (!overlayViewPermissionStatus) {
                        Toast.makeText(context, context.getResources().getString(R.string.overlay_msg), Toast.LENGTH_SHORT).show();
                    } else if (!launcherPermissionStatus) {
                        Toast.makeText(context, context.getResources().getString(R.string.home_app_msg), Toast.LENGTH_SHORT).show();
                    } else {*/
                    Intent intent = new Intent(Consts.app_permission_done);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

                    Intent openSettings = new Intent(context, MainActivity.class);
                    if (Consts.IS_BUSINESS_APP == 1) {
                        openSettings.putExtra(Consts.open_home, 1);
                    }
                    startActivity(openSettings);

                    CustomDashboardApplication.setInt(context, Consts.is_first_time, 1);
                    //}
                }
            }
        });

        CustomDashboardApplication.setInt(context, Consts.app_permission_status, 1);

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (CustomDashboardApplication.isMyLauncherDefault(context)) {
            launcherPermissionStatus = true;
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 1);
        } else {
            launcherPermissionStatus = false;
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
        }

        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), Consts.PACKAGE_NAME);
        boolean granted = (mode == AppOpsManager.MODE_ALLOWED);

        if (granted) {
            CustomDashboardApplication.setInt(context, Consts.user_statics_permission_status, 1);
        } else {
            //btnContinue.setEnabled(true);
            btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
            CustomDashboardApplication.setInt(context, Consts.user_statics_permission_status, 0);
        }

        if (CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
            accessibilityPermissionStatus = true;
            CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 1);
            CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 1);
        } else {
            accessibilityPermissionStatus = false;
            CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
            CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 0);
        }

        if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            runtimePermissionStatus = true;
            CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
        } else if (Utils.isMarshMallow() && Utils.isLessTiramisu()
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            runtimePermissionStatus = true;
            CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
        } else {
            if (!Utils.isMarshMallow()) {
                runtimePermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 1);
            } else {
                runtimePermissionStatus = false;
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // overlayViewPermissionStatus = true;
            new Handler().postDelayed(() -> {
                if (Settings.canDrawOverlays(context)) {
                    overlayViewPermissionStatus = true;
                    CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 1);
                } else {
                    overlayViewPermissionStatus = false;
                    CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 0);
                }

                recPermissionView.setHasFixedSize(true);
                recPermissionView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

                appPermissionModels = new ArrayList<>();
                // appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.usage_permission), usageStaticsPermissionStatus));
                appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.accebility_permission), accessibilityPermissionStatus));
                appPermissionModels.add(1, new AppPermissionModel(1, context.getResources().getString(R.string.runtime_permission), runtimePermissionStatus));
                appPermissionModels.add(2, new AppPermissionModel(2, "3. " + context.getResources().getString(R.string.overlay_permission), overlayViewPermissionStatus));
                if (!CustomDashboardApplication.isDeviceSamsungActivated()) {
                    appPermissionModels.add(3, new AppPermissionModel(3, "4. " + context.getResources().getString(R.string.defualt_home), launcherPermissionStatus));
                }
                appPermissionListAdapter = new AppPermissionListAdapter(context, appPermissionModels, AppPermissionMain.this);
                recPermissionView.setAdapter(appPermissionListAdapter);
            }, 1000);
        } else {
            if (Settings.canDrawOverlays(context)) {
                overlayViewPermissionStatus = true;
                CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 1);
            } else {
                overlayViewPermissionStatus = false;
                CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 0);
            }

            recPermissionView.setHasFixedSize(true);
            recPermissionView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            appPermissionModels = new ArrayList<>();
            //appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.usage_permission), usageStaticsPermissionStatus));
            appPermissionModels.add(0, new AppPermissionModel(0, context.getResources().getString(R.string.accebility_permission), accessibilityPermissionStatus));
            appPermissionModels.add(1, new AppPermissionModel(1, context.getResources().getString(R.string.runtime_permission), runtimePermissionStatus));
            appPermissionModels.add(2, new AppPermissionModel(2, "3." + context.getResources().getString(R.string.overlay_permission), overlayViewPermissionStatus));
            if (!CustomDashboardApplication.isDeviceSamsungActivated()) {
                appPermissionModels.add(3, new AppPermissionModel(3, "4. " + context.getResources().getString(R.string.defualt_home), launcherPermissionStatus));
            }
            appPermissionListAdapter = new AppPermissionListAdapter(context, appPermissionModels, this);
            recPermissionView.setAdapter(appPermissionListAdapter);
        }

        if (!accessibilityPermissionStatus) {
            // btnContinue.setEnabled(false);
            btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
        } else {
            if (!runtimePermissionStatus) {
                //   btnContinue.setEnabled(false);
                btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
            } else {
                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.appCategoryFile);
                if (!f0.exists()) {
                    //f0.delete();
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
                    CustomDashboardApplication.setInt(context, Consts.serverAppListCount, resolveInfoList.size());
                    for (int i = 0; i < resolveInfoList.size(); i++) {
                        ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
                        apiManager.getAppsCategories(activityInfo.applicationInfo.packageName, activityInfo.applicationInfo, resolveInfoList.size());
                    }
                }

                if (!overlayViewPermissionStatus) {
                    btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
                } else {
                    btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.teal));
                }

                // btnContinue.setBackgroundColor(context.getResources().getColor(R.color.teal));
                if (!launcherPermissionStatus) {
                    //   btnContinue.setEnabled(false);
                    btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.teal));
                } else {
                    //    btnContinue.setEnabled(true);
                    btnContinue.setBackgroundColor(ContextCompat.getColor(context, R.color.teal));
                }
            }
        }
    }

    @Override
    public void OnSelectPermission(int pos, boolean isPermissionGiven) {
        if (pos == 0) {
            if (!CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
                Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission. It's helpful to track background running applications.");
            }
        } else if (pos == 1) {
            if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                requestPermission();
            } else if (Utils.isMarshMallow() && Utils.isLessTiramisu() &&
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                CustomDashboardApplication.setInt(context, Consts.storage_permission_status, 0);
                requestPermission();
            }
        } else if (pos == 2) {
            CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 0);
            Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivity(intent1);
            //startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else if (pos == 3) {
            CustomDashboardApplication.setInt(context, Consts.draw_over_lay_permission_status, 0);
            if (!CustomDashboardApplication.isMyLauncherDefault(context)) {
                CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
                if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("oppo.launcher")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_HOME_SETTINGS);
                    startActivity(intent);
                } else if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("huawei.android.launcher")) {
                    Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                    startActivity(localObject);
                } else {
                    PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeActivity.class);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    Intent selector = new Intent(Intent.ACTION_MAIN);
                    selector.addCategory(Intent.CATEGORY_HOME);
                    startActivity(selector);

                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                }
            }
        } else if (pos == 4) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else if (pos == 5) {
            if (!CustomDashboardApplication.isMyLauncherDefault(context)) {
                CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
                if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("oppo.launcher")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_HOME_SETTINGS);
                    startActivity(intent);
                } else if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("huawei.android.launcher")) {
                    Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                    startActivity(localObject);
                } else {
                    PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeActivity.class);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    Intent selector = new Intent(Intent.ACTION_MAIN);
                    selector.addCategory(Intent.CATEGORY_HOME);
                    startActivity(selector);

                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                }
            }
        }
    }

    // REQUEST PERMISSIONS
    public void requestPermission() {
        if (getActivity() != null)
            ActivityCompat.requestPermissions(getActivity(), permit, request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == request && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    runtimePermissionStatus = true;
                } else if (Utils.isMarshMallow() && Utils.isLessTiramisu()
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    runtimePermissionStatus = true;
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                runtimePermissionStatus = false;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
                // if so check once again if we have permission
                // continue here - permission was granted
                overlayViewPermissionStatus = Settings.canDrawOverlays(context);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = context.getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, context.getPackageName());

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            if (isMyLauncherDefaultStr().contains("oppo.launcher")) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            } else if (isMyLauncherDefaultStr().contains("huawei.android.launcher")) {
                Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                startActivity(localObject);
            } else {
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                startActivity(selector);
            }

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String isMyLauncherDefaultStr() {
        PackageManager localPackageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = Objects.requireNonNull(localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)).activityInfo.packageName;
        //CustomDashboardApplication.setString(context, Consts.defaultLauncherName, str);
        Log.e("Package Name:", str);
        return str;
    }
}
