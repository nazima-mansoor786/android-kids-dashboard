package com.tabnova.screenshare;

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
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.ServiceCompat;

public class MediaProjectionService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
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
        stopForeground(true);
        stopSelf();
    }
}
