package com.tabnova.aidashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.tabnova.aidashboard.Activity.AppPermissionsActivity;
import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.FakeActivity;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.BlockApps.WatchingAccessibilityService;
import com.tabnova.aidashboard.DailyUpdateDataWorker.UpdateDataWorker;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.CurrentAppWorker;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.startup.BeginActivity;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Knox.utils.Logger;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.Model.CallLogModel;
import com.tabnova.aidashboard.Model.CallLogsModel;
import com.tabnova.aidashboard.Model.SmsLogModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class CustomDashboardApplication extends Application implements LifecycleObserver, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    //private static Application mInstance;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static GoogleApiClient googleApiClient;
    public static ArrayList<ApplicationTimerModel> currentApplicationModels = new ArrayList<>();
    public static final String ACTION_STATE_CHANGED = "com.tabnova.aidashboard.MainActivity.ACTION_STATE_CHANGED";
    private AppCompatDialog alertDialogRateApp;
    public static AppCompatDialog alertDialogLoggedIn;

    public static final String TAG = CustomDashboardApplication.class.getSimpleName();

    private static CustomDashboardApplication mInstance;

    public static String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // APP PERMISSIONS
    private String[] permit = new String[]{Manifest.permission.READ_PHONE_STATE};

    Integer request = 0;

    public static boolean isCustomSettingScreenCall = false;
    public static boolean isBillingLaunched = false;

    public static CustomDashboardApplication getInstance() {
        return mInstance;
    }

    public static Location mPrevLocation;

    public static long startAppTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        com.tabnova.aidashboard.Extra.BillingManager.getInstance().init(this);
        createNotificationChannels();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        buildGoogleApiClient();

        Calendar currentTime = Calendar.getInstance();
        Calendar dueTime = Calendar.getInstance();

        dueTime.set(Calendar.HOUR_OF_DAY, 24);
        dueTime.set(Calendar.MINUTE, 0);
        dueTime.set(Calendar.SECOND, 0);

        startAppTime = System.currentTimeMillis();

        // if (currentTime.getTimeInMillis() == dueTime.getTimeInMillis()) {

        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(UpdateDataWorker.class,
                        24, TimeUnit.HOURS)
                        .setConstraints(myConstraints)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork("START_SCHEDULE_TIME",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest);
        Log.e("Workerstart", "Worker Start");
        //    }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private int i = 0;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //Toast.makeText(getApplicationContext(), "Activity Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivityStarted(Activity activity) {
                //Toast.makeText(getApplicationContext(), "Activity Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivityResumed(Activity activity) {
                //Consts.CURRENT_APP_STATUS = 0;
                //Toast.makeText(getApplicationContext(), "Activity Resumed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Consts.CURRENT_APP_STATUS = 1;
                //Toast.makeText(getApplicationContext(), "Activity Paused", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivityStopped(Activity activity) {
                //Toast.makeText(getApplicationContext(), "Activity Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                //Toast.makeText(getApplicationContext(), "Activity SaveInstanceState", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                //Toast.makeText(getApplicationContext(), "Activity Destroyed", Toast.LENGTH_SHORT).show();
                //isAppIsInBackground(activity);
            }
        });
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    protected void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background

        Constraints myConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();


        PeriodicWorkRequest workRequestEnd =
                new PeriodicWorkRequest.Builder(CurrentAppWorker.class,
                        10, TimeUnit.SECONDS,
                        calculateFlexPeriodic(10), TimeUnit.MILLISECONDS)
                        .setConstraints(myConstraints)
                        .build();


        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork("TASK", ExistingPeriodicWorkPolicy.KEEP, workRequestEnd);
    }

    public static boolean isNotificationServiceRunning(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
    }


    public static boolean checkPlayServices(Activity activity) {
        try {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Log.i(TAG, "This device is not supported.");
                    //finish();
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long calculateFlexPeriodic(int second) {

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.SECOND, second);

        // Initialize a calendar with now.
        Calendar cal2 = Calendar.getInstance();

        if (cal2.getTimeInMillis() < cal1.getTimeInMillis()) {
            // Add the worker periodicity.
            cal2.setTimeInMillis(cal2.getTimeInMillis() + TimeUnit.DAYS.toMillis(second));
        }

        long delta = (cal2.getTimeInMillis() - cal1.getTimeInMillis());

        return ((delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) ? delta
                : PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS);
    }


    // REQUEST PERMISSIONS
    public void requestPermission() {
        ActivityCompat.requestPermissions((Activity) getApplicationContext(), permit, request);
    }

    // RETURNS APPS VERSION NUMBER
    public static String getAppVersion(Context context, String packageName) {
        PackageInfo pinfo = null;
        String versionName = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(packageName, 0);
            int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static String convertDate(String dateInMilliseconds, String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    public static void getCallLog(Context context) {
        ArrayList<CallLogModel> callLogModels = new ArrayList<>();
        callLogModels.clear();

        if (CustomDashboardApplication.getLong(context, Consts.lastUpdatedCallLogDate) == 0) {

            ContentResolver cr = context.getContentResolver();
            @SuppressLint("Recycle")
            Cursor curLog = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            while (curLog.moveToNext()) {
                String callNumber = curLog.getString(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                String callName = curLog.getString(curLog.getColumnIndex(CallLog.Calls.CACHED_NAME));
                String callType = null;
                String duration = null;
                String dateString = null;
                String lastUpdatedData = null;

                // if (callName == null) {
                // callName = "Unknown";

                String callDate = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.DATE));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
                dateString = formatter.format(new Date(Long.parseLong(callDate)));
                callType = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.TYPE));
                switch (callType) {
                    case "1":
                        callType = "Incoming";
                        break;
                    case "2":
                        callType = "Outgoing";
                        break;
                    case "3":
                        callType = "Missed";
                        break;
                    case "5":
                        callType = "Rejected";
                        break;
                    case "6":
                        callType = "Blocked";
                        break;
                }
                duration = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.DURATION));

                Log.e("CALLLOG", callType + " - " + callNumber + " - " + duration);
                // }
                if (callNumber != null) {
                    callLogModels.add(new CallLogModel(callName, callNumber, callType, duration, dateString, lastUpdatedData, 0));
                }
            }

            String ser = SerializeObject.objectToString(callLogModels);
            if (ser != null && !ser.equalsIgnoreCase("")) {
                SerializeObject.WriteSettings(context, ser, Consts.callLogFile);
            } else {
                SerializeObject.WriteSettings(context, "", Consts.callLogFile);
            }

            CustomDashboardApplication.setLong(context, Consts.lastUpdatedCallLogDate, Calendar.getInstance().getTimeInMillis());

        } else {
            ContentResolver cr = context.getContentResolver();
            @SuppressLint("Recycle")
            Cursor curLog = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            while (curLog.moveToNext()) {
                String callNumber = curLog.getString(curLog
                        .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                String callName = curLog.getString(curLog.getColumnIndex(CallLog.Calls.CACHED_NAME));
                String callType = null;
                String duration = null;
                String dateString = null;
                String lastUpdatedData = null;

                String callDate = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.DATE));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
                dateString = formatter.format(new Date(Long.parseLong(callDate)));
                callType = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.TYPE));
                if (callType.equals("1")) {
                    callType = "Incoming";
                } else if (callType.equals("2")) {
                    callType = "Outgoing";
                } else if (callType.equals("3")) {
                    callType = "Missed";
                } else if (callType.equals("5")) {
                    callType = "Rejected";
                } else if (callType.equals("6")) {
                    callType = "Blocked";
                }
                duration = curLog.getString(curLog.getColumnIndex(android.provider.CallLog.Calls.DURATION));

                Log.e("CALLLOG", callType + " - " + callNumber + " - " + duration);
                // }
                if (Long.parseLong(callDate) > CustomDashboardApplication.getLong(context, Consts.lastUpdatedCallLogDate)) {
                    if (callNumber != null) {
                        callLogModels.add(new CallLogModel(callName, callNumber, callType, duration, dateString, lastUpdatedData, 0));
                    }
                }
            }

            String ser = SerializeObject.objectToString(callLogModels);
            if (ser != null && !ser.equalsIgnoreCase("")) {
                SerializeObject.WriteSettings(context, ser, Consts.callLogFile);
            } else {
                SerializeObject.WriteSettings(context, "", Consts.callLogFile);
            }

            CustomDashboardApplication.setLong(context, Consts.lastUpdatedCallLogDate, Calendar.getInstance().getTimeInMillis());
        }

        // APIManager.savePhoneHistory(context);
    }

    public static void getAllSms(Context context) {
        ArrayList<SmsLogModel> smsLogModels = new ArrayList<>();
        smsLogModels.clear();

        if (CustomDashboardApplication.getLong(context, Consts.lastUpdatedSmsLogDate) == 0) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            int totalSMS = 0;
            if (c != null) {
                totalSMS = c.getCount();
                if (c.moveToFirst()) {
                    for (int j = 0; j < totalSMS; j++) {
                        String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                        String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                        String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));

                        Date dateFormat = new Date(Long.valueOf(smsDate));
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
                        String dateString = formatter.format(new Date(Long.parseLong(smsDate)));
                        String type = null;
                        String lastUpdatedData = null;
                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                type = "Inbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                                type = "Sent";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                type = "Outbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_FAILED:
                                type = "Failed";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                                type = "Queue";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                                type = "Draft";
                                break;
                            default:
                                break;
                        }

                        if (number != null) {
                            smsLogModels.add(new SmsLogModel(number, body, type, dateString, lastUpdatedData, 1));
                        }
                        Log.e("SMSLOG", "Number: " + number + " Body: " + body + " Type: " + type + " Date: " + dateString);
                        c.moveToNext();
                    }
                    String ser = SerializeObject.objectToString(smsLogModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.smsLogFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.smsLogFile);
                    }
                }
                c.close();
            } else {
                Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
            }

            CustomDashboardApplication.setLong(context, Consts.lastUpdatedSmsLogDate, Calendar.getInstance().getTimeInMillis());

        } else {

            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            int totalSMS = 0;
            if (c != null) {
                totalSMS = c.getCount();
                if (c.moveToFirst()) {
                    for (int j = 0; j < totalSMS; j++) {
                        String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                        String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                        String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));

                        Date dateFormat = new Date(Long.valueOf(smsDate));
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
                        String dateString = formatter.format(new Date(Long.parseLong(smsDate)));
                        String type = null;
                        String lastUpdatedData = null;
                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                type = "Inbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                                type = "Sent";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                type = "Outbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_FAILED:
                                type = "Failed";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                                type = "Queue";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                                type = "Draft";
                                break;
                            default:
                                break;
                        }

                        if (Long.parseLong(smsDate) > CustomDashboardApplication.getLong(context, Consts.lastUpdatedSmsLogDate)) {
                            if (number != null) {
                                smsLogModels.add(new SmsLogModel(number, body, type, dateString, lastUpdatedData, 1));
                            }
                        }
                        Log.e("SMSLOG", "Number: " + number + " Body: " + body + " Type: " + type + " Date: " + dateString);
                        c.moveToNext();
                    }
                    String ser = SerializeObject.objectToString(smsLogModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.smsLogFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.smsLogFile);
                    }
                }
                c.close();
            } else {
                Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
            }
        }


        // APIManager.savePhoneHistory(context);
    }


    public static boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    // GET SERIAL NUMBER WITH CUSTOM CONDITIONS

    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        String serialNumber = "";

        if (!TextUtils.isEmpty(CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no)) &&
                !CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no).equalsIgnoreCase("null")) {
            serialNumber = CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no);
            serialNumber = serialNumber.replace("\n", "");

            if (!TextUtils.isEmpty(serialNumber) && !serialNumber.equalsIgnoreCase("null")) {
                return serialNumber;
            }
        }


        /*if (Utils.isMarshMallow()) {
            if (ActivityCompat.checkSelfPermission(CustomDashboardApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(CustomDashboardApplication.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                serialNumber = Utils.readFromFile(CustomDashboardApplication.getInstance());
            }
        } else {
            serialNumber = Utils.readFromFile(CustomDashboardApplication.getInstance());
        }*/

        if (!TextUtils.isEmpty(serialNumber) && !serialNumber.equalsIgnoreCase("null")) {
            serialNumber = serialNumber.replace("\n", "");
            if (!TextUtils.isEmpty(CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no)) &&
                    !CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no).equalsIgnoreCase("null")) {
                String oldSrNo = CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no);
                oldSrNo = oldSrNo.replace("\n", "");
                if (!oldSrNo.equalsIgnoreCase(serialNumber)) {
                    CustomDashboardApplication.setString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no, serialNumber);
                }
            }
            if (!TextUtils.isEmpty(serialNumber))
                return serialNumber;
        }

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals("")) {
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            }
            if (serialNumber.equals("")) {
                serialNumber = (String) get.invoke(c, "ro.serialno");
            }
            if (serialNumber.equals("")) {
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            }
            if (serialNumber.equals("")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    serialNumber = android.os.Build.getSerial();
                    //serialNumber = "000000000000";
                } else {
                    serialNumber = Build.SERIAL;
                    //serialNumber = "000000000000";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }


        if (serialNumber == null || serialNumber.isEmpty() || serialNumber.contains("00000")) {
            //serialNumber = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            serialNumber = "000000000000";
        }

        if (serialNumber.contains("00000") || serialNumber == null || serialNumber.isEmpty() || serialNumber.equals("unknown")) {
            if (CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no).equals("")
                    || CustomDashboardApplication.getString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no) == null) {
                serialNumber = getRandomString();
            }
        }

        CustomDashboardApplication.setString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no, serialNumber);
        Log.e("serialNumber", serialNumber);
        return serialNumber;
    }

    // GENERATE RANDOM STRING
    public static String getRandomString() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }

        setString(CustomDashboardApplication.getInstance(), Consts.generated_serial_no, stringBuilder.toString());
        Log.e("serialNumberRandom", stringBuilder.toString());
        return stringBuilder.toString();
    }

    // CONTACT SUPPORT MESSAGE INTENT
    public static void contactSupport(Context context) {
        String body = null;
        try {

            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER +
                    "\n Device License type : " + android.os.Build.MODEL +
                    "\n Serial Number : " + CustomDashboardApplication.getInstance().getSerialNumber() +
                    "\n Device License Activated : " + ((SuperLockState.getInstance(context) != null &&
                    SuperLockState.getInstance(context).isESDKLicenseActivated()) ? "Yes" : "No");
        } catch (Exception ignored) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Consts.CONTACT_US_EMAIL_ADDRESS});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, ""));
    }

    // SHOW LOGGED IN POP UP
    public static void showLoggedInDialog(final Context context) {
        alertDialogLoggedIn = new AppCompatDialog(context);
        alertDialogLoggedIn.setContentView(R.layout.custom_popup_global_login);
        alertDialogLoggedIn.setCancelable(true);

        Button btnLogin = alertDialogLoggedIn.findViewById(R.id.login_btn);
        Button btnCancel = alertDialogLoggedIn.findViewById(R.id.cancel_btn);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   Intent intent = new Intent(context, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/
                alertDialogLoggedIn.dismiss();
            }
        });

      /*  btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CustomDashboardApplication.setInt(context, Consts.force_login, 1);
                alertDialogLoggedIn.dismiss();
            }
        });
*/

        alertDialogLoggedIn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogLoggedIn.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogLoggedIn.getWindow().setGravity(Gravity.CENTER);
        alertDialogLoggedIn.show();
    }


    //SHOW RATE APP POP UP
    public void showRateAppDialog(final Context context) {
        try {
            alertDialogRateApp = new AppCompatDialog(context);
            alertDialogRateApp.setContentView(R.layout.custom_rating_app_popup);

            TextView btnYes = alertDialogRateApp.findViewById(R.id.btn_yes);
            TextView btnNo = alertDialogRateApp.findViewById(R.id.btn_no);
            TextView btnDismiss = alertDialogRateApp.findViewById(R.id.btn_dismiss);


            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tabnova.aidashboard")));

                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                    Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "market://details?id=" + getPackageName()));
                    try {
                        //myAppLinkToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(myAppLinkToMarket);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
                    }
                    alertDialogRateApp.dismiss();
                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomDashboardApplication.setInt(context, Consts.INSTALLED_APP, 0);
                    alertDialogRateApp.dismiss();
                }
            });

            btnDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomDashboardApplication.setInt(context, Consts.INSTALLED_APP, 0);
                    alertDialogRateApp.dismiss();
                }
            });

            alertDialogRateApp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogRateApp.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogRateApp.getWindow().setGravity(Gravity.CENTER);
            alertDialogRateApp.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isDeviceSamsung() {
        boolean isSamsunDevice;
        final String manufacturer = android.os.Build.MANUFACTURER;
        isSamsunDevice = manufacturer.equals("samsung");

        return isSamsunDevice;
    }

    public static boolean isDeviceSamsungActivated() {
        boolean isSamsunDevice;
        final String manufacturer = android.os.Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase("samsung")) {
            SuperLockState superLockState = SuperLockState.getInstance(getInstance());
            isSamsunDevice = superLockState.isESDKLicenseActivated();
        } else {
            isSamsunDevice = false;
        }

        return isSamsunDevice;
    }

   /* public static Application getInstance() {
        return mInstance;
    }*/

    public static boolean isAccessGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(Consts.PACKAGE_NAME, 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode != AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        //your package /   accesibility service path/class
        final String service = "com.tabnova.aidashboard.BlockApps.WatchingAccessibilityService";

        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");

                        return true;
                    }
                }
                accessibilityFound = true;
            }
        } else {
            accessibilityFound = false;
            Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

    public static boolean isAccessibilitySettingsEnabled(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + WatchingAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.e(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.e(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.e(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.e(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }


    public static void setInt(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }

    public static void setBatteryStatus(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getBatteryStatus(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }

    public static void setShortCut(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getShortCut(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 1);
    }

    public static void setVolume(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getVolume(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }


    public static void setMarketPlace(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getMarketPlace(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }

    public static void setAllowCalls(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getAllowCalls(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }


    public static void setClockSetting(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getClockSetting(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 2);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, "");
    }

    public static void setLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    public static void setHelpEnabled(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getHelpEnabled(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }

    public static void setClockEnabled(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putInt(key, value).apply();
    }

    public static int getClockEnabled(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, 1);
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.tabnova.aidashboard.Activity.MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    /**
     * Creates every notification channel the app posts to, up front (Android 8+), with the same
     * ids, names and importance the services use, so notifications sent before a service has run
     * (e.g. FCM, which uses default_notification_channel_id) land in the right channel.
     * Creating an existing channel is a no-op, so the services' own createNotificationChannel
     * calls stay harmless.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        // FCM default channel (manifest default_notification_channel_id), as in MyFirebaseMessagingService.
        NotificationChannel fcmChannel = new NotificationChannel(getString(R.string.default_notification_channel_id),
                "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
        fcmChannel.enableLights(true);
        fcmChannel.setLightColor(Color.RED);
        fcmChannel.enableVibration(true);
        fcmChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // VPN website filter (vpnapp VPNService).
        NotificationChannel vpnChannel = new NotificationChannel("com.rsb.vpnpoc",
                "Channel One", NotificationManager.IMPORTANCE_DEFAULT);
        vpnChannel.enableLights(true);
        vpnChannel.setLightColor(Color.RED);
        vpnChannel.setShowBadge(true);
        vpnChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        // Screen share (screenshare MediaProjectionService).
        NotificationChannel screenShareChannel = new NotificationChannel("com.tabnova.screenshare",
                "Screen Share Service", NotificationManager.IMPORTANCE_DEFAULT);
        screenShareChannel.enableLights(true);
        screenShareChannel.setLightColor(Color.RED);
        screenShareChannel.setShowBadge(true);
        screenShareChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        try {
            manager.createNotificationChannels(Arrays.asList(fcmChannel, vpnChannel, screenShareChannel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restart(Context context) {
        PendingIntent myActivity = Utils.restartPendingIntent();

        AlarmManager alarmManager;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000, myActivity);
        System.exit(2);
    }

    public static void enableSamsungKiosk(Context context) {
        try {
            if (isDeviceSamsung() && isDeviceSamsungActivated()) {
                //SuperLockState.getInstance(context).enableKioskMode(Consts.PACKAGE_NAME);
                SuperLockState.getInstance(context).hideStatusBar(true, context);
                SuperLockState.getInstance(context).allowHardwareKeys(false, KeyEvent.KEYCODE_APP_SWITCH, context);
                SuperLockState.getInstance(context).allowHardwareKeys(false, KeyEvent.KEYCODE_MENU, context);
                SuperLockState.getInstance(context).allowHardwareKeys(false, KeyEvent.KEYCODE_HOME, context);
                SuperLockState.getInstance(context).allowHardwareKeys(false, KeyEvent.KEYCODE_BACK, context);
                SuperLockState.getInstance(context).allowStatusBar(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i("enableSamsungKiosk : Exception");
            Logger.i(e);
        }
    }

    public static void exitSamsungKiosk(Context context) {
        try {
            if (isDeviceSamsungActivated()) {
                SuperLockState.getInstance(context).hideStatusBar(false, context);
                SuperLockState.getInstance(context).allowHardwareKeys(true, KeyEvent.KEYCODE_APP_SWITCH, context);
                SuperLockState.getInstance(context).allowHardwareKeys(true, KeyEvent.KEYCODE_MENU, context);
                SuperLockState.getInstance(context).allowHardwareKeys(true, KeyEvent.KEYCODE_HOME, context);
                SuperLockState.getInstance(context).allowHardwareKeys(true, KeyEvent.KEYCODE_BACK, context);
                SuperLockState.getInstance(context).allowStatusBar(true);

                try {
                    if (SuperLockState.getInstance(context).isKioskModeEnabled(context))
                        SuperLockState.getInstance(context).disableKioskMode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i("exitSamsungKiosk : Exception");
            Logger.i(e);
        }

        //SuperLockState.getInstance(context).stopApp(Consts.PACKAGE_NAME);

    }

    public static void enableBackKey(Context context) {
        if (isDeviceSamsungActivated()) {
            SuperLockState.getInstance(context).allowHardwareKeys(true, KeyEvent.KEYCODE_BACK, context);
        }
    }

    public static void setIgnoreLicense(Context context, String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getIgnoreLicense(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, false);
    }

    public static boolean isMyLauncherDefault(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            String currentHomePackage = resolveInfo.activityInfo.packageName;
            final String myPackageName = context.getPackageName();
            if (myPackageName.equals(currentHomePackage)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
        /*final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = context.getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, CustomDashboardApplication.PACKAGE_NAME);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;*/
    }


    public static void forceExit(Context context) {
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

        if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0) {
            /*if (!SuperLockState.getInstance(getInstance()).isKioskModeEnabled(getInstance())) {
                CustomDashboardApplication.enableSamsungKiosk(context);
            }*/
            CustomDashboardApplication.exitSamsungKiosk(context);
        }
        //context.finish();
        System.exit(0);
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

    public static String calculateMinutes(String startTime, String endTime) {

        String minute = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss aa");
            Date date1 = format.parse(endTime);
            Date date2 = format.parse(startTime);
            long mills = date1.getTime() - date2.getTime();
           /* Log.e("Data1", ""+date1.getTime());
            Log.e("Data2", ""+date2.getTime());
            int hours = (int) (mills/(1000 * 60 * 60));
            float mins = (mills/(1000*60)) % 60;*/
            minute = String.valueOf(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return minute;
    }

    public static long calculateTimeInMillis(String startTime, String endTime) {

        long minute = 0;

        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm");
            Date date1 = format.parse(endTime);
            Date date2 = format.parse(startTime);
            long mills = date1.getTime() - date2.getTime();
           /* Log.e("Data1", ""+date1.getTime());
            Log.e("Data2", ""+date2.getTime());
            int hours = (int) (mills/(1000 * 60 * 60));
            float mins = (mills/(1000*60)) % 60;*/
            minute = mills;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return minute;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static boolean isSamsungAndActivated(Context context) {
        boolean isSamsunDevice = false;
        try {
            final String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer.equals("samsung")) {
                if (SuperLockState.edm != null) {
                    SuperLockState sl = SuperLockState.getInstance(context);
                    if (sl == null) {
                        return isSamsunDevice;
                    }
                    if (sl.isESDKLicenseActivated()) {
                        isSamsunDevice = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSamsunDevice;
    }


    // SET TODAY TIME IN LONG
    public static void setTodayLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getTodayLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    // SET YESTERDAY TIME IN LONG
    public static void setYesterdayLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getYesterdayLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    // SET EVERYDAY TIME IN LONG
    public static void setEverydayLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getEverydayLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    // SET LAST WEEK TIME IN LONG
    public static void setLastWeekLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getLastWeekLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    // SET LAST MONTH TIME IN LONG
    public static void setLastMonthLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getLastMonthLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    // SET CUSTOM TIME IN LONG
    public static void setCustomLong(Context context, String key, Long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //apply commit
        sp.edit().putLong(key, value).apply();
    }

    public static Long getCustomLong(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDeviceHasTelephonyService(Context context) {
        boolean isAviailable = false;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            isAviailable = tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isAviailable;
    }


    @SuppressLint("MissingPermission")
    public static ArrayList<CallLogsModel> getCallLogs(Context context) {
        ArrayList<CallLogsModel> callLogsModels = new ArrayList();
        try {
            ContentResolver cr = context.getContentResolver();

            @SuppressLint("Recycle")
            Cursor curLog = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            int totalLog = 0;
            if (curLog != null) {
                totalLog = curLog.getCount();
                if (curLog.moveToFirst()) {
                    for (int i = 0; i < totalLog; i++) {
                        String callId = curLog.getString(curLog.getColumnIndex(CallLog.Calls._ID));
                        String callNumber = curLog.getString(curLog.getColumnIndex(CallLog.Calls.NUMBER));
                        String callName = curLog.getString(curLog.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        String callType = "";
                        String duration = "";
                        String dateString = "";

                        String callDate = curLog.getString(curLog.getColumnIndex(CallLog.Calls.DATE));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
                        dateString = formatter.format(new Date(callDate));
                        callType = curLog.getString(curLog.getColumnIndex(CallLog.Calls.TYPE));
                        if (callType.equals("1")) {
                            callType = "Incoming";
                        } else if (callType.equals("2")) {
                            callType = "Outgoing";
                        } else if (callType.equals("3")) {
                            callType = "Missed";
                        } else if (callType.equals("5")) {
                            callType = "Rejected";
                        } else if (callType.equals("6")) {
                            callType = "Blocked";
                        }
                        duration = curLog.getString(curLog.getColumnIndex(CallLog.Calls.DURATION));
                        Log.e("CALLLOG", callType + " - " + callNumber + " - " + duration);
                        if (callNumber != null) {
                            if (callName == null) {
                                callLogsModels.add(new CallLogsModel(Integer.parseInt(callId), callNumber, callNumber, callType, duration, dateString, Long.parseLong(callDate), false));
                            } else {
                                callLogsModels.add(new CallLogsModel(Integer.parseInt(callId), callName, callNumber, callType, duration, dateString, Long.parseLong(callDate), false));
                            }

                        }
                        curLog.moveToNext();
                    }
                }

                curLog.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return callLogsModels;
    }

    public static long getFileFolderModifiedDate(String fileName) {
        long lastModifiedDate = 0;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                long fileLastModified = file.lastModified();
                lastModifiedDate = fileLastModified;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastModifiedDate;
    }

    public static boolean isDeviceSamsungAndSupportActivation() {
        boolean isSamsunDevice;
        try {
            final String manufacturer = Build.MANUFACTURER;
            if (manufacturer.equals("samsung")) {
                SuperLockState superLockState = SuperLockState.getInstance(getInstance());
                isSamsunDevice = superLockState != null && superLockState.supportedActivation();
            } else {
                isSamsunDevice = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSamsunDevice = false;
        }

        return isSamsunDevice;
    }

    public static boolean checkAllPermissions(Context context) {
        boolean isAllPermission = true;
        /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            isAllPermission = false;
        } else if (Utils.isMarshMallow() && Utils.isLessTiramisu() &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            isAllPermission = false;
        }*/

        if (Utils.isMarshMallow() &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            isAllPermission = false;
        }

        if (!CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
            isAllPermission = false;
        }

        if (!Settings.canDrawOverlays(context)) {
            isAllPermission = false;
        }

        return isAllPermission;
    }

    public static void redirectOnActivity(Context context, boolean isLauncher) {
        Intent intent;
        if (!CustomDashboardApplication.checkAllPermissions(context)) {
            intent = new Intent(context, AppPermissionsActivity.class);
        } else {
            if (SessionManager.getInstance(context).getIsParentMode()) {
                Log.e(TAG, "onClick: @1671");
                intent = new Intent(context, CustomDashboardSettings.class);
            } else {
                intent = new Intent(context, MainActivity.class);
            }
            intent.putExtra(Consts.is_launcher, isLauncher);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}