package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.Activity.OverLayScreen;
import com.tabnova.aidashboard.CustomDashboardApplication;

public class LockScreenWorker extends Worker {

    Context cxt;

    public LockScreenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;

    }

    @NonNull
    @Override
    public Result doWork() {
        Intent intent = new Intent(cxt, OverLayScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.startActivity(intent);
        CustomDashboardApplication.setInt(cxt, Consts.is_screen_locked, 1);
        return Result.success();
    }
}
