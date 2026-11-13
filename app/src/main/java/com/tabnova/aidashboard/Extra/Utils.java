package com.tabnova.aidashboard.Extra;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.work.WorkManager;

import com.rsb.vpnpoc.VPNService;
import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.FakeActivity;
import com.tabnova.aidashboard.Activity.FirstActivity;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.startup.BeginActivity;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.R;
import com.tabnova.aidashboard.BlockApps.WatchingAccessibilityService;
import com.tabnova.screenshare.StartStopScreenShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.internal.Util;

public class Utils {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int REQUEST_CAPTURE_IMAGE = 789;
    public static final int REQUEST_CAPTURE_Gallery = 1;
    public static String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static boolean contains(JSONObject jsonObject, String key) {
        // try {
        return jsonObject != null && jsonObject.has(key) && !jsonObject.isNull(key);
    }

    public static boolean checkPermission(final Context context) {
        if (Utils.isMarshMallow()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Camera permission is necessary to take a photo.");
                    alertBuilder.setPositiveButton(context.getResources().getString(R.string.yes), (dialog, which) -> ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE));
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean checkLocationPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(context.getResources().getString(R.string.yes), (dialog, which) -> ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission_group.LOCATION}, 1));
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission_group.LOCATION}, 1);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static int isArrayOrObject(JSONObject jsonObject, String key) {
        // try {
        try {
            if (jsonObject != null && jsonObject.get(key) instanceof JSONObject && !jsonObject.isNull(key)) {
                return 1;
            } else if (jsonObject != null && jsonObject.get(key) instanceof JSONArray && !jsonObject.isNull(key))
                return 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 2;
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    /*public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }
*/
//    public static MaterialDialog progress(Context context){
//        MaterialDialog mat=new MaterialDialog.Builder(context)
//                .title("Progress")
//                .content("Please Wait")
//                .progress(true, 0)
//                .progressIndeterminateStyle(false)
//                .show();
//
//        return mat;
//    }

    public static int getScreenWidth() {


        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) CustomDashboardApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;
        return deviceWidth;
    }

    public static int getScreenHeight() {


        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) CustomDashboardApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;
        return deviceHeight;
    }

    public static int convertDipToPixels(float dips) {
        return (int) (dips * CustomDashboardApplication.getInstance().getResources().getDisplayMetrics().density + 0.5f);
    }

    public static String getFormatedTime(int hour, int min) {
        String formatam;
        if (hour == 0) {
            hour += 12;
            formatam = "AM";
        } else if (hour == 12) {
            formatam = "PM";
        } else if (hour > 12) {
            hour -= 12;
            formatam = "PM";
        } else {
            formatam = "AM";
        }
        return String.format("%02d:%02d " + formatam, hour, min);
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static void openCameraIntent(Context context, Activity act) {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(context.getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile(context);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                act.startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    public static String imageFilePath;

    public static File createImageFile(Context context) throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix  */
                storageDir      /* directory*/
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public static String saveImage(Bitmap myBitmap, Context context) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        try {
            File f = new File(new File(Environment.getExternalStorageDirectory(), "aero"), "profile/" + Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            //   File f = new File(wallpaperDirectory, );
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(context,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public static boolean isAppIsInBackground() {
        boolean isInBackground = true;
        try {
            ActivityManager am = (ActivityManager) CustomDashboardApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(CustomDashboardApplication.getInstance().getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isInBackground;
    }

    public static void showSingleButtonAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    // GENERATE RANDOM STRING
    public static String getRandomString(Context context) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(10);
        for (int i = 0; i < 12; i++) {
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        CustomDashboardApplication.setString(context, Consts.generated_serial_no, stringBuilder.toString());
        Log.e("serialNumberRandom", stringBuilder.toString());
        return stringBuilder.toString();
    }

    public static void writeToFile(Context context, String data) {
        if (context == null)
            return;
        try {
            String dir = context.getExternalFilesDir(null).toString().split("/Android")[0] + "/Dashboard";
            File path = new File(dir);
            if (!path.exists()) {
                path.mkdir();
            }
            File file = new File(path, "serial_number.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            Log.e("Exception", "File write failed: ");
            e.printStackTrace();
        }
    }

    public static String readFromFile(Context context) {
        String ret = "";
        if (context == null)
            return ret;
        try {
            String dir = context.getExternalFilesDir(null).toString().split("/Android")[0] + "/Dashboard";
            File path = new File(dir);
            File file = new File(path, "serial_number.txt");
            if (file.exists()) {
                int length = (int) file.length();

                byte[] bytes = new byte[length];

                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }

                ret = new String(bytes);
                if (!TextUtils.isEmpty(ret) && !ret.equalsIgnoreCase("null")) {
                    CustomDashboardApplication.setString(context, Consts.generated_serial_no, ret);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: ");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("login activity", "Can not read file: ");
            e.printStackTrace();
        }

        return ret;
    }

    public static void writeToDomainsFile(Context context, ArrayList<String> data) {
        if (context == null)
            return;
        try {
            String dir = context.getExternalFilesDir(null).toString().split("/Android")[0];
            File path = new File(dir);
            if (!path.exists()) {
                path.mkdir();
            }
            File file = new File(path, "host.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream stream = new FileOutputStream(file);
            StringBuilder strData = new StringBuilder();

            try {
                for (String str : data) {
                    strData.append(str).append(System.lineSeparator());
                }
                stream.write(strData.toString().getBytes());
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            Log.e("Exception", "File write failed: ");
            e.printStackTrace();
        }
    }

    public static String readFromDomainFile(Context context) {
        String ret = "";
        if (context == null)
            return ret;
        try {
            String dir = context.getExternalFilesDir(null).toString().split("/Android")[0];
            File path = new File(dir);
            File file = new File(path, "host.txt");
            if (file.exists()) {
                int length = (int) file.length();

                byte[] bytes = new byte[length];

                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }

                ret = new String(bytes);
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: ");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("login activity", "Can not read file: ");
            e.printStackTrace();
        }

        return ret;
    }

    public static boolean isMarshMallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isTiramisu() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    /**
     * PendingIntent that relaunches BeginActivity after the app restarts itself (AlarmManager).
     * Same request code, intent and flags as before.
     * <p>
     * Target SDK 35+: a PendingIntent's creator no longer lends its background-activity-start
     * privileges by default, so opt in explicitly. MODE_BACKGROUND_ACTIVITY_START_ALLOWED is
     * deprecated on API 36 in favour of ALLOW_IF_VISIBLE / ALLOW_ALWAYS; the app process has
     * exited when the alarm fires (no visible window), so ALLOW_ALWAYS is used on API 36+.
     * Below API 34 no options are passed (unchanged behaviour).
     */
    @SuppressWarnings("deprecation")
    public static PendingIntent restartPendingIntent() {
        Context baseContext = CustomDashboardApplication.getInstance().getBaseContext();
        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            ActivityOptions activityOptions = ActivityOptions.makeBasic();
            activityOptions.setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS);
            options = activityOptions.toBundle();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions activityOptions = ActivityOptions.makeBasic();
            activityOptions.setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
            options = activityOptions.toBundle();
        }
        return PendingIntent.getActivity(baseContext,
                192837, new Intent(baseContext, BeginActivity.class),
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE, options);
    }

    public static boolean isLessTiramisu() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void exitApp(final Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            ComponentName compName =
                    new ComponentName(activity.getPackageName(), activity.getPackageName() + ".Activity.FakeActivity");
            pm.setComponentEnabledSetting(compName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        WatchingAccessibilityService.hideReturnOverlay();

        SessionManager.getInstance(activity).setIsParentMode(false);
        CustomDashboardApplication.setInt(activity, Consts.is_from_dashboard, 0);
        CustomDashboardApplication.setInt(activity, Consts.DISABLE_RECENTS_BUTTON, 0);
        CustomDashboardApplication.setInt(activity, Consts.ENABLE_STATUS_BAR, 1);
        SessionManager.getInstance(activity).setIsDashboardLaunch(false);

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) activity.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar beginCal = Calendar.getInstance();
        beginCal.setTimeInMillis(CustomDashboardApplication.startAppTime);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(System.currentTimeMillis());

        final List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        ArrayList<String> allPackageNameList = new ArrayList<>();
        ArrayList<String> finalBackgroungAppList = new ArrayList<>();

        for (int i = 0; i < queryUsageStats.size(); i++) {
            allPackageNameList.add(queryUsageStats.get(i).getPackageName());
        }

        if (allPackageNameList.size() > 0) {
            ArrayList<String> appModels = new ApkInfoExtractor(activity).getAppsPackageNameList();
            for (int j = 0; j < allPackageNameList.size(); j++) {
                if (appModels.contains(allPackageNameList.get(j))) {
                    finalBackgroungAppList.add(allPackageNameList.get(j));
                }
            }
        }

        if (finalBackgroungAppList.size() > 0) {
            for (int k = 0; k < finalBackgroungAppList.size(); k++) {
                if (!finalBackgroungAppList.get(k).equals(activity.getPackageName())) {
                    /*PackageManager packageManager = activity.getPackageManager();
                    ComponentName componentName = new ComponentName(activity, FakeActivity.class);
                    packageManager.setApplicationEnabledSetting(finalBackgroungAppList.get(k), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    packageManager.setApplicationEnabledSetting(finalBackgroungAppList.get(k), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);*/
                }
            }
        }

        if (!(CustomDashboardApplication.isDeviceSamsung() && SuperLockState.getInstance(activity).isESDKLicenseActivated())) {
            try {
                activity.getPackageManager().clearPackagePreferredActivities(activity.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SessionManager.getInstance(activity).setLastExitAppTime(System.currentTimeMillis());

        if (activity instanceof MainActivity || activity instanceof CustomDashboardSettings) {
            WorkManager.getInstance(activity).cancelAllWork();
        }

        try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                //if (!activity.isFinishing() && !activity.isDestroyed()) {
                CustomDashboardApplication.exitSamsungKiosk(activity);
                SuperLockState.getInstance(activity).allowStatusBar(true);
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //if (CustomDashboardApplication.isMyLauncherDefault(activity)) {
            if (!CustomDashboardApplication.isDeviceSamsung()) {
                Utils.resetPreferredLauncherAndOpenChooser(activity);
            } else {
                if (CustomDashboardApplication.isMyLauncherDefault(activity)) {
                    Utils.resetPreferredLauncherAndOpenChooser(activity);
                }
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                StartStopScreenShare.hangup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        try {
            VPNService.Companion.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(activity, FirstActivity.class);
        intent.putExtra("EXIT", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            if (isMyLauncherDefaultstr(context).contains("oppo.launcher")) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_HOME_SETTINGS);
                context.startActivity(intent);
            } else if (isMyLauncherDefaultstr(context).contains("huawei.android.launcher")) {
                Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                context.startActivity(localObject);
            } else {
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(selector);
            }

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String isMyLauncherDefaultstr(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        //CustomDashboardApplication.setString(context, Consts.defaultLauncherName, str);
        Log.e("Package Name:", str);
        return str;
    }

    public static void adminDisable(Context context) {
        ComponentName admin = new ComponentName(context, com.tabnova.aidashboard.Knox.startup.AdminReceiver.class);
        DevicePolicyManager DPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        DPM.removeActiveAdmin(admin);
    }

    public static boolean checkIsTablet(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        return diagonalInches >= 7.0;
    }

    public static void showAccessibilityPopup(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                    CustomDashboardApplication.setInt(context, Consts.accessibility_permission_status, 0);
                    Intent intent = new Intent();
                    intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    public static void showOverlayPermissionPopup(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage("To use timer popup and screen share, Kids Dashboard must require App appear on top .")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.getPackageName()));
                    try {
                        context.startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void hideSystemBars(Activity activity) {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    public static void hideStatusBars(Activity activity) {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
        //windowInsetsController.show(WindowInsetsCompat.Type.navigationBars());
    }

    public static void hideBottomNavigationBars(Activity activity) {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
    }
}