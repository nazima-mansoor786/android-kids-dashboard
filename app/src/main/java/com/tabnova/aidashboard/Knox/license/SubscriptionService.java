package com.tabnova.aidashboard.Knox.license;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.utils.UsefulUtility;


public class SubscriptionService extends Service {

    private boolean isRunning;
    Thread backgroundThread;
    private String errorCode = "";
    private TokenManager tokenManager;
    private Context mContext;

    private String license = "";
    private String pass = "";

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (errorCode.contentEquals("203")) {
                UsefulUtility.showErrorDialog(mContext, errorCode, "- Your device is either Note 7 \n- Or a discontinued device device \n- Not supported please Uninstall");
            } else {
                UsefulUtility.showErrorDialog(mContext, errorCode, "- Check network connection\n- Is your phone rooted or using modifided rom?" +
                        "\n- Uninstall and reinstall \n- Please contact admin");
            }
        }
    };


    //private RequestQueue mRequestQueue;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        tokenManager = TokenManager.getInstance();
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }


    private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
            registerDevice();

        }
    };

    private void registerDevice() {

        String userID = UsefulUtility.getSerialNumber();
        String subscription = CustomDashboardApplication.getString(mContext, "subscription_code");
        if (subscription != null && !subscription.isEmpty()) {
            String test = Const.REGISTER + userID + "/" + UsefulUtility.md5(Const.BASIC_PASSWORD) + "/" + subscription + "/" + Const.PROFILE_ID;
            KnoxApiClient.getInstance().getApi()
                    .register(String.valueOf(userID), String.valueOf(UsefulUtility.md5(Const.BASIC_PASSWORD)), subscription)
                    .enqueue(KnoxApiClient.callback(createSuccessListener(), createErrorListener()));
        } else {
            Intent intent = new Intent(mContext, SubscriptionDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            releaseService();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }

    private KnoxApiClient.Listener<String> createSuccessListener() {
        return response -> {
            try {
                if (response.contains("success")) {
                    startService(new Intent(mContext, LoginService.class));
                } else {
                    getCode();
                }
                releaseService();
            } catch (Exception e) {
                getCode();
                releaseService();
            }
        };
    }

    private KnoxApiClient.ErrorListener createErrorListener() {
        return error -> {
            getCode();
            releaseService();
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    private void releaseService() {
        tokenManager.releaseTokenRef();
        stopSelf();
    }

    void getCode() {
//        AppController.setString(mContext, "subscription_code", "");
//        Intent intent = new Intent(mContext, SubscriptionDialogActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        // no option to enter code manually so report as error

        UsefulUtility.showErrorDialog(mContext, "Network error", "Check internet and try again or contact admin");
    }


}
