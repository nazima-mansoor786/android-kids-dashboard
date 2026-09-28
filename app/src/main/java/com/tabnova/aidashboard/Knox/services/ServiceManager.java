package com.tabnova.aidashboard.Knox.services;

import android.content.Context;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dijo.xavier on 02/11/2016.
 */
public class ServiceManager {

    public static void syncAllSettingsFromServer(final Context context) {


    }

    public static void updateSystemInfo(final Context context) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //run your service
                context.startService(new Intent(context, SystemInfoService.class));
            }
        }, 1000);

    }


    public static void logToCloud(final Context context, String log, String result) {
        Intent intent = new Intent(context, WebLoggingService.class);
        intent.putExtra("CloudLog", log );
        intent.putExtra("CloudResult", result);
        context.startService(intent);

    }

    public static void updateProvisionStatus(final Context context, String status) {
        Intent intent = new Intent(context, ProvisionService.class);
        intent.putExtra("ProvisionStatus", status );

        context.startService(intent);

    }


    public static String getFunctionName(Context context) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return "";
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(context.getClass().getName())) {
                continue;
            }

            return "[" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId()
                    + "): " + st.getFileName() + ":" + st.getMethodName() + ":" + st.getLineNumber() + "]";
        }

        return "";
    }


}
