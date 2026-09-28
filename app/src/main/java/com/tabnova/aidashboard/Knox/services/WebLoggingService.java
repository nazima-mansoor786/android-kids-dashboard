package com.tabnova.aidashboard.Knox.services;

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.license.Const;
import com.tabnova.aidashboard.Knox.license.EnhancedStringRequest;
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

        String requestString = Const.DEVICE_LOG_TO_CLOUD + UsefulUtility.getSerialNumber() +"/" +
                currentLog + "/" +
                currentResult +"/"
                +token;
        String sysReq  = requestString.replaceAll(" ", "%20");
        EnhancedStringRequest myReq = new EnhancedStringRequest(sysReq ,
                null,
                createSuccessListener(),
                createErrorListener());
        CustomDashboardApplication.getInstance().addToRequestQueue(myReq);
    }

    private Response.Listener<String> createSuccessListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                releaseService();
            }
        };
    }

    private Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                releaseService();
            }
        };
    }

    private void releaseService(){
        tokenManager.releaseTokenRef();
        stopSelf();
    }

}
