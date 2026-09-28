package com.tabnova.aidashboard.Extra;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class CurrentAppWorker extends Worker {

    Context cxt;

    public CurrentAppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        getRunningAppPackageName();
        Log.e("pck", getRunningAppPackageName());
        return Result.success();
    }

    private String getRunningAppPackageName()
    {

        ActivityManager activityManager = (ActivityManager)cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(10);
        ActivityManager.RunningTaskInfo p = (ActivityManager.RunningTaskInfo) tasks.get(0);

        return p.baseActivity.getPackageName();

    }

}
