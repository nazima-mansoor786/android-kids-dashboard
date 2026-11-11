package com.tabnova.screenshare;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.ServiceCompat;
import androidx.core.content.IntentCompat;

/**
 * Foreground service (type mediaProjection) for one screen-share session.
 * Start it only AFTER the user granted consent, passing the consent result as extras.
 * It enters the foreground first, then starts capture itself, so capture never runs
 * before the service is in the foreground.
 */
public class MediaProjectionService extends Service {

    public static final String EXTRA_RESULT_CODE = "com.tabnova.screenshare.extra.RESULT_CODE";
    public static final String EXTRA_RESULT_DATA = "com.tabnova.screenshare.extra.RESULT_DATA";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int resultCode = intent != null
                ? intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                : Activity.RESULT_CANCELED;
        final Intent resultData = intent != null
                ? IntentCompat.getParcelableExtra(intent, EXTRA_RESULT_DATA, Intent.class)
                : null;

        if (resultCode != Activity.RESULT_OK || resultData == null) {
            // No fresh consent: never capture, never stay running.
            StartStopScreenShare.clearConsent();
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            startAsForeground();
        } catch (RuntimeException e) {
            e.printStackTrace();
            StartStopScreenShare.clearConsent();
            stopSelf();
            return START_NOT_STICKY;
        }

        // The service is now in the foreground (startForeground is synchronous), so capture may start.
        StartStopScreenShare.context = this;
        if (StartStopScreenShare.isSessionActive()) {
            // A previous session is still open: close it off the main thread (signalling close
            // blocks), then start the new session on the main thread.
            new Thread(() -> {
                StartStopScreenShare.closeSession();
                mainHandler.post(() -> StartStopScreenShare.start(resultCode, resultData));
            }).start();
        } else {
            StartStopScreenShare.start(resultCode, resultData);
        }
        return START_NOT_STICKY;
    }

    private void startAsForeground() {
        String CHANNEL_ONE_ID = "com.tabnova.screenshare";
        String CHANNEL_ONE_NAME = "Screen Share Service";
        NotificationChannel notificationChannel = null;

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Kids Dashboard")
                .setContentText("Screen Share Service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notification.setChannelId(CHANNEL_ONE_ID);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        ServiceCompat.startForeground(this, 100, notification.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Full session close if the service is stopped while a session is open (no-op after hangup()).
        // Off the main thread because signalling close blocks.
        new Thread(StartStopScreenShare::closeSession).start();
        stopForeground(true);
        stopSelf();
    }
}
