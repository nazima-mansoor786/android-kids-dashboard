package com.tabnova.aidashboard.Knox.license;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.services.ServiceManager;
import com.tabnova.aidashboard.Knox.services.SystemInfoService;
import com.tabnova.aidashboard.Knox.startup.ConnectionDetector;
import com.tabnova.aidashboard.Knox.utils.UsefulUtility;


public class LoginService extends Service {

    private boolean isRunning;
    private Context mContext;
    private Thread backgroundThread;
    private TokenManager tokenManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        this.isRunning = false;

        tokenManager = TokenManager.getInstance();

        this.backgroundThread = new Thread(myTask);
    }


    private final Runnable myTask = () -> getLoginTocken();

    private void getLoginTocken() {
        String userID = UsefulUtility.getSerialNumber();
        String test = Const.AUTHENTICATE + userID + "/" + UsefulUtility.md5(Const.BASIC_PASSWORD);

        GsonRequest<Tocken> myReq = new GsonRequest<>(Const.AUTHENTICATE + userID + "/" + UsefulUtility.md5(Const.BASIC_PASSWORD),
                Tocken.class, null,
                createSuccessListener(),
                createErrorListener());

        int MY_SOCKET_TIMEOUT_MS = 1000 * 20;
        myReq.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        CustomDashboardApplication.getInstance().addToRequestQueue(myReq);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ConnectionDetector cd = new ConnectionDetector(mContext);
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            UsefulUtility.showErrorDialog(mContext, "No Internet Connection error", "Check internet and try again or contact admin");
            releaseService();
        }

        if (!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }

    private Response.Listener<Tocken> createSuccessListener() {
        return response -> {
            if (response != null && !response.token.isEmpty()) {
                tokenManager.setToken(response.token);

                ServiceManager.logToCloud(mContext, "Device added to system ", "OK");

                startService(new Intent(mContext, SystemInfoService.class));
                nextAction();
            }
        };
    }

    private Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //UsefulUtils.showErrorDialog(mContext, "Error ", "Check network connection , \nPlease contact @gmail.com");
                getCode();
                releaseService();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isRunning = false;
    }

    void nextAction() {
        // startService(new Intent(mContext, LicensesProfileService.class));
        Intent intent = new Intent(mContext, LicenseManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        releaseService();
    }

    private void releaseService() {
        stopSelf();
    }

    void getCode() {
        //schneider-electric_usbnet
        CustomDashboardApplication.setString(mContext, "subscription_code", Const.HARD_CODE_SUB_CODE);
        startService(new Intent(mContext, SubscriptionService.class));
        //Subscribed so no need to ask
//        Intent intent = new Intent(mContext, SubscriptionDialogActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }
}
