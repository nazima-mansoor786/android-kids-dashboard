package com.tabnova.aidashboard.Knox.license;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.services.ServiceManager;
import com.tabnova.aidashboard.Knox.utils.UsefulUtility;

import java.lang.reflect.Type;
import java.util.List;

public class LicensesProfileService extends Service {
    private static final String LOG_TAG = LicensesProfileService.class.getSimpleName();

    private boolean isRunning;
    Thread backgroundThread;
    private String errorCode = "";
    private TokenManager tokenManager;
    private Context mContext;

    private String license = "";
    private String pass ="";

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(errorCode.contentEquals("203")) {
                UsefulUtility.showErrorDialog(mContext, errorCode, "- Your device is either Note 7 \n- Or a discontinued device device \n- Not supported please Uninstall");
            }else {
                UsefulUtility.showErrorDialog(mContext, errorCode, "- Check network connection\n- Is your phone rooted or using modifided rom?" +
                        "\n- Uninstall and re install\n- Please contact admin");
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
            getLicense();

        }
    };

    private void getLicense() {
        String token = tokenManager.getToken();
        if (token == null || token.length() <= 0) {
            releaseService();
            return;
        }

        String test = Const.DEVICE_PROFILE_LICENCES + token;

        KnoxApiClient.getInstance().getApi()
                .deviceProfileLicences(token)
                .enqueue(KnoxApiClient.callback(createSuccessListener(), createErrorListener()));
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
        return new KnoxApiClient.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Gson gson = new Gson();
                    boolean keyfound = false;
                    Type type = new TypeToken<List<LicensesProfile>>() {
                    }.getType();
                    List<LicensesProfile> licenseList = gson.fromJson(response, type);

                    int size = licenseList.size();
                    if(size <= 0) {
                        errorCode = "License not found";
                        handler.postDelayed(runnable, 100);
                        releaseService();
                    }
                    Intent intent = new Intent( mContext, LicenseManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    for (int i = 0; i < size; ++i) {
                        LicensesProfile lp = licenseList.get(i);
                        if(lp != null) {
                            intent.putExtra(lp.licence_name, lp.licence_code);
                        }
                        ServiceManager.logToCloud(mContext,"Licences received from cloud ", "OK");
                    }

                    startActivity(intent);

                    releaseService();
                } catch (Exception e) {
                    errorCode = "Network Error license";
                    handler.postDelayed(runnable, 100);
                    releaseService();
                }
            }
        };
    }


    private KnoxApiClient.ErrorListener createErrorListener() {
        return new KnoxApiClient.ErrorListener() {
            @Override
            public void onError(Throwable error) {
                errorCode = "Network Error";
                handler.postDelayed(runnable, 500);
                releaseService();

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    private boolean processLicense(LicensesProfile licensesProfile) {
        if (licensesProfile.licence_code != null && !licensesProfile.licence_code.isEmpty()) {
            if(licensesProfile.licence_name.contentEquals(Const.PDP_WEB_LICENCE)) {
                license = licensesProfile.licence_code;
            }else if(licensesProfile.licence_name.contentEquals(Const.PDP_WEB_PASS)) {
                pass  = licensesProfile.licence_code;
            }

        }
        return false;
    }

    private void releaseService() {
        tokenManager.releaseTokenRef();
        stopSelf();
    }



    private int getLicenseIndex() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt("license_index", 0);
    }

    private void setLicenseIndex(int cont) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("license_index", cont);
        editor.commit();
    }

    private void restartApp() {

        int index = getLicenseIndex();
        setLicenseIndex( ++index );
        int retrycount = getRetryCount();
        setRetryCount(++retrycount);
        try {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        releaseService();
    }

    private int getRetryCount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt("retry", 0);
    }

    private void setRetryCount(int cont) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("retry", cont);
        editor.commit();
    }


}
