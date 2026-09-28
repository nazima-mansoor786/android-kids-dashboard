package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.Activity.AppPermissionsActivity;
import com.tabnova.aidashboard.Activity.CustomDashboardSettings;
import com.tabnova.aidashboard.Activity.MainActivity;
import com.tabnova.aidashboard.CustomDashboardApplication;

public class StartScheduleTimer extends Worker {


    Context cxt;

    public StartScheduleTimer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
    }

    @NonNull
    @Override
    public Result doWork() {

/*        Intent intent = new Intent(Consts.SCHEDULE_TIMER);
        intent.putExtra(Consts.TIMER_STATUS, 1);
        LocalBroadcastManager.getInstance(cxt).sendBroadcast(intent);*/

        CustomDashboardApplication.redirectOnActivity(getApplicationContext(), false);

        CustomDashboardApplication.setInt(cxt, Consts.TIMER_STATUS, 1);

        Log.e("StartScheduleTimer", "Timer Working");

        return Result.success();
    }

}
