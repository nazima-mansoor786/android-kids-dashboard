package com.tabnova.aidashboard.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.R;


public class AppVisibilityService extends Service {

    // WINDOW MANAGER
    public static WindowManager mWindowManager;

    // VIEW
    public static View mFloatingView;

    // FRAME LAYOUT
    public FrameLayout flMain;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startOreoForeground();
        else
            startForeground(5, new Notification());

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.frame_overlay_apps, null);

        LinearLayout llMain = mFloatingView.findViewById(R.id.ll_main);

        mWindowManager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));
        final WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        localLayoutParams.gravity = Gravity.CENTER;
     /*   localLayoutParams.x = 0;
        localLayoutParams.y = 100;*/
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        localLayoutParams.format = PixelFormat.TRANSLUCENT;

        llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("AppVisisbilityService", "run: @86");
                CustomDashboardApplication.redirectOnActivity(getApplicationContext(), true);
            }
        });

        try {
            if (mWindowManager != null && mFloatingView != null)
                mWindowManager.addView(mFloatingView, localLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("AppVisisbilityService", "run: @94");
        CustomDashboardApplication.redirectOnActivity(getApplicationContext(), true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopSelf(5);
        stopForeground(true);
        stopSelf();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startOreoForeground() {
        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getPackageName();
        String channelName = "App Visibility Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

}
