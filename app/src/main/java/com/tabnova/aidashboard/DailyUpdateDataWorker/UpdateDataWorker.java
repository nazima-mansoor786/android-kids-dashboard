package com.tabnova.aidashboard.DailyUpdateDataWorker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;

import java.util.Calendar;

public class UpdateDataWorker extends Worker {

    Context cxt;
    APIManager apiManager;

    public UpdateDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
        apiManager = new APIManager(cxt);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.e("periodicWorker", "Result Done");
        Calendar currentTime  = Calendar.getInstance();
        Calendar dueTime = Calendar.getInstance();

        dueTime.set(Calendar.HOUR_OF_DAY, 24);
        dueTime.set(Calendar.MINUTE, 0);
        dueTime.set(Calendar.SECOND, 0);

        if (currentTime.getTimeInMillis() == dueTime.getTimeInMillis()) {
            dueTime.add(Calendar.HOUR_OF_DAY, 24);
            //CustomDashboardApplication.getCallLog(cxt);
           // CustomDashboardApplication.getAllSms(cxt);
            apiManager.getProfileCounter(CustomDashboardApplication.getString(cxt, Consts.token));
            //apiManager.savePhoneHistoryAll(cxt);
        }

        return Result.success();
    }
}
