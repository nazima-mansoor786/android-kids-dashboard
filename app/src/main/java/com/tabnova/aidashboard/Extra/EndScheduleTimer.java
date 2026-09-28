package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.Activity.TimeOutScreen;
import com.tabnova.aidashboard.CustomDashboardApplication;

public class EndScheduleTimer extends Worker {

    Context cxt;

    public EndScheduleTimer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
    }

    @NonNull
    @Override
    public Result doWork() {

    /*    Intent intent = new Intent(Consts.SCHEDULE_TIMER);
        intent.putExtra(Consts.end_time, 0);
        LocalBroadcastManager.getInstance(cxt).sendBroadcast(intent);*/

        Intent intent = new Intent(cxt, TimeOutScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.startActivity(intent);

        //CustomDashboardApplication.setInt(cxt, Consts.IS_LOCKED, 1);

        CustomDashboardApplication.setInt(cxt, Consts.TIMER_STATUS, 0);

        Log.e("EndScheduleTimer", "Timer Working");

        return  Result.success();
    }

}
