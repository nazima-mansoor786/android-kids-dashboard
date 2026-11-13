package com.tabnova.aidashboard.BlockApps;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class WatchingAccessibilityService extends AccessibilityService {

    private static WatchingAccessibilityService sInstance;

    private ArrayList<AppModel> selectedAppList;
    private ArrayList<String> packageNameList;
    private ArrayList<AppModel> tempUnlockedAppList;

    private String tempPackageName;
    private String currentPackageName;

    private int isFromDashboard;

    public static WatchingAccessibilityService getInstance() {
        return sInstance;
    }

    // ---- Return-to-dashboard overlay (replaces the former AppVisibilityService) ----
    // Shown as TYPE_ACCESSIBILITY_OVERLAY, so it needs no foreground service and no
    // SYSTEM_ALERT_WINDOW. Same layout, size, position, flags and tap behavior as before.

    private View returnOverlayView;

    /**
     * Shows the "please wait" card (if this service is running) and returns to the dashboard.
     * Idempotent. Runs on the main thread. If the accessibility service is not running,
     * no card is shown and the app is redirected directly.
     */
    public static void showReturnOverlay(final Context context) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showReturnOverlay(context));
            return;
        }
        WatchingAccessibilityService service = sInstance;
        if (service == null) {
            CustomDashboardApplication.redirectOnActivity(context.getApplicationContext(), true);
            return;
        }
        service.addReturnOverlay();
        CustomDashboardApplication.redirectOnActivity(service.getApplicationContext(), true);
    }

    /** Removes the "please wait" card if it is shown. Idempotent. Runs on the main thread. */
    public static void hideReturnOverlay() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(WatchingAccessibilityService::hideReturnOverlay);
            return;
        }
        WatchingAccessibilityService service = sInstance;
        if (service != null) {
            service.removeReturnOverlay();
        }
    }

    private void addReturnOverlay() {
        if (returnOverlayView != null) {
            return;
        }
        // TYPE_ACCESSIBILITY_OVERLAY must be added through this service's own WindowManager.
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        View floatingView = LayoutInflater.from(this).inflate(R.layout.frame_overlay_apps, null);
        LinearLayout llMain = floatingView.findViewById(R.id.ll_main);

        final WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        localLayoutParams.gravity = Gravity.CENTER;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        localLayoutParams.format = PixelFormat.TRANSLUCENT;

        llMain.setOnClickListener(v -> CustomDashboardApplication.redirectOnActivity(getApplicationContext(), true));

        try {
            windowManager.addView(floatingView, localLayoutParams);
            returnOverlayView = floatingView;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeReturnOverlay() {
        View view = returnOverlayView;
        returnOverlayView = null;
        if (view == null || !view.isAttachedToWindow()) {
            return;
        }
        try {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.removeViewImmediate(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @SuppressLint({"NewApi", "RestrictedApi"})
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //CustomDashboardApplication.isMyLauncherDefaultstr(getApplicationContext());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            try {
                String currentPackageName = event.getPackageName().toString();
                String className = String.valueOf(event.getClassName()).toLowerCase();

                Log.e("Watching Accebility", "onAccessibilityEvent: " + currentPackageName + " : Class Name : " + className);

                //if (Utils.isAppIsInBackground()) {
                ArrayList<AppModel> tempList = new ArrayList<>();

                String ser1 = SerializeObject.ReadSettings(getApplicationContext(), Consts.appHistoryFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);

                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        tempList = (ArrayList<AppModel>) obj;
                    }
                }

                boolean isAppFound = false;

                if (!TextUtils.isEmpty(currentPackageName) && !currentPackageName.equalsIgnoreCase(getPackageName()) && !tempList.isEmpty()) {
                    for (int i = 0; i < tempList.size(); i++) {
                        if ((!TextUtils.isEmpty(tempList.get(i).getPackageDetails()) &&
                                tempList.get(i).getPackageDetails().equalsIgnoreCase(currentPackageName)) ||
                                (!TextUtils.isEmpty(tempList.get(i).getAppInfo()) &&
                                        tempList.get(i).getAppInfo().equalsIgnoreCase(currentPackageName))) {
                            isAppFound = true;
                            break;
                        }
                    }
                }

                if (!isAppFound && !currentPackageName.equalsIgnoreCase(getPackageName()) &&
                        !currentPackageName.equalsIgnoreCase("com.android.systemui") &&
                        !currentPackageName.equalsIgnoreCase("android") &&
                        !currentPackageName.equalsIgnoreCase("com.android.server.am.appnotrespondingdialog") &&
                        !currentPackageName.equalsIgnoreCase("com.samsung.accessibility") &&
                        !currentPackageName.equalsIgnoreCase("com.samsung.android.app.cocktailbarservice") &&
                        !currentPackageName.equalsIgnoreCase("com.samsung.android.honeyboard")
                        && Utils.isAppIsInBackground()) {
                    ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> runningTaskInfoList = am.getRunningTasks(Integer.MAX_VALUE);

                    boolean isAppExist = false;

                    try {
                        if (!runningTaskInfoList.isEmpty()) {
                            for (int i = 0; i < runningTaskInfoList.size(); i++) {
                                if (runningTaskInfoList.get(i).topActivity.getPackageName().equalsIgnoreCase(getPackageName())) {
                                    isAppExist = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    boolean finalIsAppExist = isAppExist;
                    new Handler().postDelayed(() -> {
                        if (finalIsAppExist) {
                            try {
                                Toast.makeText(getApplicationContext(), "You have to add this app on dashboard first.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                am.killBackgroundProcesses(currentPackageName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 3000);
                }
                //}

                /*if (className.contains("recent") || className.contains("recents")) {
                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.DISABLE_RECENTS_BUTTON) == 1) {
                        Log.e("Accessibility", "onAccessibilityEvent: @57");
                        //Toast.makeText(getApplicationContext(), "onAccessibilityEvent", Toast.LENGTH_SHORT).show();
                        if (SessionManager.getInstance(getApplicationContext()).getIsDashboardLaunch()) {
                            Intent intent;
                            if (SessionManager.getInstance(getApplicationContext()).getIsParentMode()) {
                                Log.e("Accessibility", "@62");
                                intent = new Intent(getApplicationContext(), CustomDashboardSettings.class);
                            } else {
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            getApplicationContext().startActivity(intent);
                        }
                        return;
                    }
                } else {
                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.is_from_dashboard) == 0) {

                        selectedAppList = new ArrayList<>();
                        packageNameList = new ArrayList<>();
                        packageNameList.clear();
                        selectedAppList.clear();
                        String blocks = SerializeObject.ReadSettings(getApplicationContext(), Consts.blocksAppsFile);
                        if (!TextUtils.isEmpty(blocks)) {
                            Object obj = SerializeObject.stringToObject(blocks);
                            // Then cast it to your object and
                            if (obj instanceof ArrayList) {
                                // Do something
                                selectedAppList = (ArrayList<AppModel>) obj;
                            }
                        }

                        for (int k = 0; k < selectedAppList.size(); k++) {
                            packageNameList.add(selectedAppList.get(k).getPackageDetails());
                        }

                        if (packageNameList.size() > 0) {
                            if (!packageNameList.contains(event.getPackageName().toString())) {
                                if (!currentPackageName.equalsIgnoreCase(CustomDashboardApplication.getString(getApplicationContext(), Consts.lastPackageName))) {
                                    CustomDashboardApplication.setString(getApplicationContext(), Consts.lastPackageName, "");
                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.isActivityOpened, 0);

                                    ArrayList<AppModel> tempUnblockApps = new ArrayList<>();
                                    String tempBlockFile = SerializeObject.ReadSettings(getApplicationContext(), Consts.tempBlockAppFile);
                                    if (tempBlockFile != null && !tempBlockFile.equalsIgnoreCase("")) {
                                        Object obj = SerializeObject.stringToObject(tempBlockFile);
                                        // Then cast it to your object and
                                        if (obj instanceof ArrayList) {
                                            // Do something
                                            tempUnblockApps = (ArrayList<AppModel>) obj;
                                        }
                                    }

                                    if (tempUnblockApps.size() > 0) {
                                        selectedAppList.add(tempUnblockApps.get(0));
                                    }

                                    String dir = getApplicationContext().getFilesDir().getAbsolutePath();
                                    File blockApps = new File(dir, Consts.blocksAppsFile);
                                    if (blockApps.exists()) {
                                        blockApps.delete();
                                    }

                                    String newBlockAppsList = SerializeObject.objectToString(selectedAppList);
                                    if (newBlockAppsList != null && !newBlockAppsList.equalsIgnoreCase("")) {
                                        SerializeObject.WriteSettings(getApplicationContext(), newBlockAppsList, Consts.blocksAppsFile);
                                    } else {
                                        SerializeObject.WriteSettings(getApplicationContext(), "", Consts.blocksAppsFile);
                                    }

                                    String tempBlockdir = getApplicationContext().getFilesDir().getAbsolutePath();
                                    File tempBlockApps = new File(tempBlockdir, Consts.tempBlockAppFile);
                                    if (tempBlockApps.exists()) {
                                        tempBlockApps.delete();
                                    }
                                } else if (event.getPackageName().toString().contains("huawei.android.launcher")) {
                                    CustomDashboardApplication.setString(getApplicationContext(), Consts.lastPackageName, "");

                                    ArrayList<AppModel> tempUnblockApps = new ArrayList<>();
                                    String tempBlockFile = SerializeObject.ReadSettings(getApplicationContext(), Consts.tempBlockAppFile);
                                    if (tempBlockFile != null && !tempBlockFile.equalsIgnoreCase("")) {
                                        Object obj = SerializeObject.stringToObject(tempBlockFile);
                                        // Then cast it to your object and
                                        if (obj instanceof ArrayList) {
                                            // Do something
                                            tempUnblockApps = (ArrayList<AppModel>) obj;
                                        }
                                    }

                                    if (tempUnblockApps.size() > 0) {
                                        selectedAppList.add(tempUnblockApps.get(0));
                                    }

                                    String dir = getApplicationContext().getFilesDir().getAbsolutePath();
                                    File blockApps = new File(dir, Consts.blocksAppsFile);
                                    if (blockApps.exists()) {
                                        blockApps.delete();
                                    }

                                    String newBlockAppsList = SerializeObject.objectToString(selectedAppList);
                                    if (newBlockAppsList != null && !newBlockAppsList.equalsIgnoreCase("")) {
                                        SerializeObject.WriteSettings(getApplicationContext(), newBlockAppsList, Consts.blocksAppsFile);
                                    } else {
                                        SerializeObject.WriteSettings(getApplicationContext(), "", Consts.blocksAppsFile);
                                    }

                                    String tempBlockdir = getApplicationContext().getFilesDir().getAbsolutePath();
                                    File tempBlockApps = new File(tempBlockdir, Consts.tempBlockAppFile);
                                    if (tempBlockApps.exists()) {
                                        tempBlockApps.delete();
                                    }
                                }

                                //CustomDashboardApplication.setString(getApplicationContext(), Consts.lastPackageName, "");

                            } else if (selectedAppList.size() > 0) {
                                for (int i = 0; i < selectedAppList.size(); i++) {
                                    if (selectedAppList.get(i).getPackageDetails().equalsIgnoreCase(event.getPackageName().toString())) {
                                        if (!CustomDashboardApplication.getString(getApplicationContext(), Consts.lastPackageName).equalsIgnoreCase(currentPackageName)) {

                                            CustomDashboardApplication.setString(getApplicationContext(), Consts.lastPackageName, "");

                                            ArrayList<AppModel> tempUnblockApps = new ArrayList<>();
                                            String tempBlockFile = SerializeObject.ReadSettings(getApplicationContext(), Consts.tempBlockAppFile);
                                            if (tempBlockFile != null && !tempBlockFile.equalsIgnoreCase("")) {
                                                Object obj = SerializeObject.stringToObject(tempBlockFile);
                                                // Then cast it to your object and
                                                if (obj instanceof ArrayList) {
                                                    // Do something
                                                    tempUnblockApps = (ArrayList<AppModel>) obj;
                                                }
                                            }

                                            if (tempUnblockApps.size() > 0) {
                                                selectedAppList.add(tempUnblockApps.get(0));
                                            }

                                            String dir = getApplicationContext().getFilesDir().getAbsolutePath();
                                            File blockApps = new File(dir, Consts.blocksAppsFile);
                                            if (blockApps.exists()) {
                                                blockApps.delete();
                                            }

                                            String newBlockAppsList = SerializeObject.objectToString(selectedAppList);
                                            if (newBlockAppsList != null && !newBlockAppsList.equalsIgnoreCase("")) {
                                                SerializeObject.WriteSettings(getApplicationContext(), newBlockAppsList, Consts.blocksAppsFile);
                                            } else {
                                                SerializeObject.WriteSettings(getApplicationContext(), "", Consts.blocksAppsFile);
                                            }

                                            String tempBlockdir = getApplicationContext().getFilesDir().getAbsolutePath();
                                            File tempBlockApps = new File(tempBlockdir, Consts.tempBlockAppFile);
                                            if (tempBlockApps.exists()) {
                                                tempBlockApps.delete();
                                            }
                                        }

                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.isActivityOpened) == 0) {
                                            *//*WorkRequest workRequest = new OneTimeWorkRequest.Builder(BlockedAppWorker.class).setInputData(new Data.Builder().put(Consts.package_name, event.getPackageName()).build()).setInitialDelay(0, TimeUnit.SECONDS).build();
                                            WorkManager.getInstance().enqueue(workRequest);*//*
                 *//*startService(
                                                    new Intent(
                                                            getApplicationContext(),
                                                            BlockedItemService.class
                                                    )
                                            );
                                            Toast.makeText(getApplicationContext(), "App Workerrrr", Toast.LENGTH_SHORT).show();*//*
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.isActivityOpened, 1);
                                        }

                                        break;
                                    }
                                }
                                //selectedAppList.clear();
                            }
                            packageNameList.clear();
                        }
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }


    @Override
    protected void onServiceConnected() {
        //Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_SHORT).show();
        sInstance = this;
        //packageNameList = new ArrayList<>();
        // selectedAppList = new ArrayList<>();
/*        String ser = SerializeObject.ReadSettings(getApplicationContext(), Consts.blocksAppsFile);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                selectedAppList = (ArrayList<AppModel>) obj;
            }
        }

        try {
            if (selectedAppList.size() > 0) {
                for (int i = 0; i < selectedAppList.size(); i++) {
                    packageNameList.add(selectedAppList.get(i).getPackageDetails());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if (SPHelper.isShowWindow(this)) {
            //  NotificationActionReceiver.showNotification(this, false);
        }
        // sendBroadcast(new Intent(QuickSettingTileService.ACTION_UPDATE_TITLE));

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        super.onServiceConnected();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        removeReturnOverlay();
        sInstance = null;
        TasksWindow.dismiss(this);
        //NotificationActionReceiver.cancelNotification(this);
        //sendBroadcast(new Intent(QuickSettingTileService.ACTION_UPDATE_TITLE));
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        removeReturnOverlay();
        if (sInstance == this) {
            sInstance = null;
        }
        super.onDestroy();
    }
}
