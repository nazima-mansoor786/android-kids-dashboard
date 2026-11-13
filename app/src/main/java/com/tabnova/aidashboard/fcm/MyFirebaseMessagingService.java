package com.tabnova.aidashboard.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tabnova.aidashboard.Activity.AppPermissionsActivity;
import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.Activity.ScreenShareActivity;
import com.tabnova.aidashboard.Activity.SubscriptionPlanInfoActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.R;
import com.tabnova.screenshare.MediaProjectionService;
import com.tabnova.screenshare.SignallingClient;
import com.tabnova.screenshare.StartStopScreenShare;

import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String NOTIFICATION_CHANNEL_ID = "ai_dashboard_channel";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    String IS_APPROVED = "false";
    String IS_BLOACKED = "false";
    String NAME = "";
    String chatID = "";
    Intent intent = null;

    Context context;
    SessionManager sessionManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        context = this;
        sessionManager = SessionManager.getInstance(context);
        try {

            // TODO(developer): Handle FCM messages here.
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.e(TAG, "From: " + remoteMessage.getFrom());
            //Log.d("feedid",remoteMessage.getda
            // Check if message contains a data payload.
            Log.e(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            }


            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }
            sendNotification(remoteMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        try {

            String messageBody = "";
            String isForceExit = "";
            String isDelete = "";

            if (remoteMessage.getData().containsKey(Consts.message)) {
                messageBody = remoteMessage.getData().get(Consts.message);

                if (remoteMessage.getData().containsKey(Consts.force_exit)) {
                    isForceExit = remoteMessage.getData().get(Consts.force_exit);

                    if (isForceExit.equals("1")) {
                        //android.os.Process.killProcess(android.os.Process.myPid());
                        Intent intent = new Intent(Consts.IS_FORCE_EXIT);
                        intent.putExtra(Consts.force_exit, 1);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        return;
                    }
                }

                if (remoteMessage.getData().containsKey(Consts.child_deleted)) {
                    isDelete = remoteMessage.getData().get(Consts.child_deleted);

                    if (isDelete.equals("1")) {
                        Intent intent = new Intent(Consts.IS_DELETE);
                        intent.putExtra(Consts.delete_child, 1);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        return;
                    }
                }

                if (remoteMessage.getData().containsKey(Consts.wallpaper_deleted)) {
                    isDelete = remoteMessage.getData().get(Consts.wallpaper_deleted);

                    if (isDelete.equals("1")) {
                        Intent intent = new Intent(Consts.IS_WALL_PAPER_DELETE);
                        CustomDashboardApplication.setString(context, Consts.IMG_FILE, "");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        return;
                    }
                }

                if (!TextUtils.isEmpty(messageBody)) {
                    if (messageBody.startsWith("Remote View,") && messageBody.contains(",")) {
                        String[] arr = messageBody.split(",");
                        String serialNumber = CustomDashboardApplication.getSerialNumber();
                        if (arr.length == 2) {
                            serialNumber = arr[1];
                            Log.e(TAG, "sendNotification: Serial number found in notification : " + serialNumber);
                        }
                        /*new Thread(() -> {
                            stopService(new Intent(getApplicationContext(), MediaProjectionService.class));
                            try {
                                try {
                                    if (SignallingClient.getInstance() != null) {
                                        SignallingClient.getInstance().close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                StartStopScreenShare.hangup();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();*/

                        Log.e(TAG, "sendNotification: Serial Number : " + serialNumber);

                        Intent intent = new Intent(getApplicationContext(), ScreenShareActivity.class);
                        if (!sessionManager.getIsSubscriptionEnabled()) {
                            intent = new Intent(getApplicationContext(), SubscriptionPlanInfoActivity.class);
                            intent.putExtra(Consts.is_screen_share, true);
                            intent.putExtra(Consts.is_launcher, true);
                        }

                        intent.putExtra(Consts.serial_number, serialNumber);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (messageBody.startsWith("Remote View Off,")) {
                        new Thread(() -> {
                            try {
                                /*stopService(new Intent(getApplicationContext(), MediaProjectionService.class));
                                try {
                                    if (SignallingClient.getInstance() != null) {
                                        SignallingClient.getInstance().close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/

                                StartStopScreenShare.hangup();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            } else {
                messageBody = remoteMessage.getNotification().getBody();

                if (!TextUtils.isEmpty(messageBody)) {
                    if (messageBody.startsWith("Remote View,") && messageBody.contains(",")) {
                        /*new Thread(() -> {
                            try {
                                stopService(new Intent(getApplicationContext(), MediaProjectionService.class));
                                if (SignallingClient.getInstance() != null) {
                                    SignallingClient.getInstance().close();
                                }

                                StartStopScreenShare.hangup();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();*/

                        String[] arr = messageBody.split(",");
                        String serialNumber = CustomDashboardApplication.getSerialNumber();
                        if (arr.length == 2) {
                            serialNumber = arr[1];
                            Log.e(TAG, "sendNotification: Serial number found in notification : " + serialNumber);
                        }

                        Log.e(TAG, "sendNotification: Serial Number : " + serialNumber);

                        Intent intent = new Intent(getApplicationContext(), ScreenShareActivity.class);
                        if (!sessionManager.getIsSubscriptionEnabled()) {
                            intent = new Intent(getApplicationContext(), SubscriptionPlanInfoActivity.class);
                            intent.putExtra(Consts.is_screen_share, true);
                            intent.putExtra(Consts.is_launcher, true);
                        }

                        intent.putExtra(Consts.serial_number, serialNumber);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (messageBody.startsWith("Remote View Off,")) {
                        new Thread(() -> {
                            try {
                                /*stopService(new Intent(getApplicationContext(), MediaProjectionService.class));
                                if (SignallingClient.getInstance() != null) {
                                    SignallingClient.getInstance().close();
                                }*/

                                StartStopScreenShare.hangup();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            }

            final Map<String, String> data = remoteMessage.getData();
            // intent.putExtra(Keys.badgecount,badgecount);
            if (data != null) {

            } else {
                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                    if (SessionManager.getInstance(this).getIsParentMode()) {
                        Log.e(TAG, "sendNotification: @161");
                        intent = new Intent(CustomDashboardApplication.getInstance(), CustomDashboardSettings.class);
                    } else {
                        intent = new Intent(CustomDashboardApplication.getInstance(), MainActivity.class);
                    }
                    intent.putExtra(Consts.is_launcher, true);
                } else {
                    if (CustomDashboardApplication.checkAllPermissions(getApplicationContext())) {
                        if (SessionManager.getInstance(this).getIsParentMode()) {
                            Log.e(TAG, "sendNotification: @170");
                            intent = new Intent(CustomDashboardApplication.getInstance(), CustomDashboardSettings.class);
                        } else {
                            intent = new Intent(CustomDashboardApplication.getInstance(), MainActivity.class);
                        }
                        intent.putExtra(Consts.is_launcher, true);
                    } else {
                        intent = new Intent(CustomDashboardApplication.getInstance(), AppPermissionsActivity.class);
                    }
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            if (intent == null) {
                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                    if (SessionManager.getInstance(this).getIsParentMode()) {
                        Log.e(TAG, "sendNotification: @187");
                        intent = new Intent(CustomDashboardApplication.getInstance(), CustomDashboardSettings.class);
                    } else {
                        intent = new Intent(CustomDashboardApplication.getInstance(), MainActivity.class);
                    }
                    intent.putExtra(Consts.is_launcher, true);
                } else {
                    if (CustomDashboardApplication.checkAllPermissions(getApplicationContext())) {
                        if (SessionManager.getInstance(this).getIsParentMode()) {
                            Log.e(TAG, "sendNotification: @196");
                            intent = new Intent(CustomDashboardApplication.getInstance(), CustomDashboardSettings.class);
                        } else {
                            intent = new Intent(CustomDashboardApplication.getInstance(), MainActivity.class);
                        }
                        intent.putExtra(Consts.is_launcher, true);
                    } else {
                        intent = new Intent(CustomDashboardApplication.getInstance(), AppPermissionsActivity.class);
                    }
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

            new APIManager(getApplicationContext()).getProfileCounter(CustomDashboardApplication.getString(getApplicationContext(), Consts.token));

            /*if (!Utils.isAppIsInBackground()) {
                Intent notiIntent = new Intent(Consts.isServerUpdated);
                notiIntent.putExtra(Consts.serverUpdate, 1);
                LocalBroadcastManager.getInstance(CustomDashboardApplication.getInstance()).sendBroadcast(notiIntent);
            }*/

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder;
            // int color = ContextCompat.getColor(CupidApplication.getInstance(), R.color.white);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // int color = 0xff123456;
                //  int color = getResources().getColor(R.color.my_notif_color);

                notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

            } else {

                notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
            }


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.notification_channel_name), importance);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    assert notificationManager != null;
                    notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                    //notificationManager.createNotificationChannel(notificationChannel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //notificationManager.notify(NotificationID.getID(), notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int appRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(getPackageName())) {
                return 1;
            }
        }
        return 0;
    }

    String isMyLauncherDefaultstr() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.e("Package Name:", str);
        return str;
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        CustomDashboardApplication.setString(CustomDashboardApplication.getInstance(), Consts.firebase_token, token);
    }
}

