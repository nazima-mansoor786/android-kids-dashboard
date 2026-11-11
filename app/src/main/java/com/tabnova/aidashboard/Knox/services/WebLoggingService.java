package com.tabnova.aidashboard.Knox.services;

import android.app.IntentService;
import android.content.Intent;

import com.tabnova.aidashboard.Knox.license.Const;
import com.tabnova.aidashboard.Knox.license.KnoxApiClient;
import com.tabnova.aidashboard.Knox.license.TokenManager;
import com.tabnova.aidashboard.Knox.utils.UsefulUtility;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WebLoggingService extends IntentService {
    private TokenManager tokenManager;
    private String currentLog = "";
    private String currentResult = "";

    public WebLoggingService() {
        super(WebLoggingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            tokenManager = TokenManager.getInstance();
            currentLog = intent.getStringExtra("CloudLog");
            if(currentLog.isEmpty()) {
                currentLog = "NA";
            }
            currentResult = intent.getStringExtra("CloudResult");
            if(currentResult.isEmpty()) {
                currentResult = "NA";
            }
            currentLog = currentLog.replace("/","-");
            currentResult = currentResult.replace("/","-");
            sendLogToCloud();

        }
    }


    private void sendLogToCloud() {
        String token = tokenManager.getToken();
        if(token == null ||token.length() <= 0) {

            return;
        }
        /*
            /setdevicelog/{device_serial_number}/{device_activity}/{activity_result}/{token}'
         */

        // Same per-value escaping as before: spaces -> %20 (previously applied to the whole URL);
        // "/" -> "-" is still done in onHandleIntent.
        KnoxApiClient.getInstance().getApi()
                .setDeviceLog(escape(UsefulUtility.getSerialNumber()), escape(currentLog), escape(currentResult), escape(token))
                .enqueue(KnoxApiClient.callback(createSuccessListener(), createErrorListener()));
    }

    private static String escape(Object value) {
        return String.valueOf(value).replaceAll(" ", "%20");
    }

    private KnoxApiClient.Listener<String> createSuccessListener() {
        return new KnoxApiClient.Listener<String>() {
            @Override
            public void onResponse(String response) {
                releaseService();
            }
        };
    }

    private KnoxApiClient.ErrorListener createErrorListener() {
        return new KnoxApiClient.ErrorListener() {
            @Override
            public void onError(Throwable error) {
                releaseService();
            }
        };
    }

    private void releaseService(){
        tokenManager.releaseTokenRef();
        stopSelf();
    }

}
