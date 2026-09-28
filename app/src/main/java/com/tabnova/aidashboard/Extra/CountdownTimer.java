package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.Activity.TimeOutScreen;
import com.tabnova.aidashboard.CustomDashboardApplication;

import java.util.concurrent.CountDownLatch;

public class CountdownTimer extends Worker {

    CountDownTimer cTimer = null;
    Context cxt;
    private Data data;

    CountDownLatch countDownLatch = null;

    public CountdownTimer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        Intent i = new Intent(Consts.stopTimerScreen);
        LocalBroadcastManager.getInstance(cxt).sendBroadcast(i);

        //if (!Utils.isAppIsInBackground()) {
        if (CustomDashboardApplication.getInt(cxt, Consts.is_screen_time_out) != 1) {
            CustomDashboardApplication.setInt(cxt, Consts.is_screen_time_out, 1);

            Intent intent = new Intent(cxt, TimeOutScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cxt.startActivity(intent);

            Log.e("Timer", "Timer Working");
        }
        // }

        return Result.success();
    }
}