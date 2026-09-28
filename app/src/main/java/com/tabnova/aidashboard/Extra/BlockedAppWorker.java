package com.tabnova.aidashboard.Extra;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tabnova.aidashboard.Activity.BlockedApps;

public class BlockedAppWorker extends Worker {

    Context cxt;
    String isFromDashBoard;

    public BlockedAppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        cxt = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        isFromDashBoard = getInputData().getString(Consts.package_name);

        // if (isFromDashBoard == 0) {

        /*Intent intent = new Intent(cxt, BlockedApps.class);
        intent.putExtra(Consts.package_name, isFromDashBoard);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        cxt.startActivity(intent);*/

        Intent it = new Intent("android.intent.action.MAIN");
        it.setComponent(new ComponentName(cxt.getPackageName(), BlockedApps.class.getName()));
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.getApplicationContext().startActivity(it);
        Toast.makeText(cxt, "App Workerrrr", Toast.LENGTH_SHORT).show();
        //  }

        return Result.success();
    }
}
