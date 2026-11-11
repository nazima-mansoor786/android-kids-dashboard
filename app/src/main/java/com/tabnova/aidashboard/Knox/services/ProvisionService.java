package com.tabnova.aidashboard.Knox.services;

import android.app.IntentService;
import android.content.Intent;

import com.tabnova.aidashboard.Knox.license.Const;
import com.tabnova.aidashboard.Knox.license.KnoxApiClient;
import com.tabnova.aidashboard.Knox.license.TokenManager;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ProvisionService extends IntentService {
    private TokenManager tokenManager;
    private String status = "";

    public ProvisionService() {
        super(ProvisionService.class.getSimpleName());
    }



    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            tokenManager = TokenManager.getInstance();
            status = intent.getStringExtra("ProvisionStatus");

            updateProvisionStatus();

        }
    }


    private void updateProvisionStatus() {
        String token = tokenManager.getToken();
        if(token == null ||token.length() <= 0) {

            return;
        }
        /*
            /setdevicelog/{device_serial_number}/{device_activity}/{activity_result}/{token}'
         */

        // Same per-value escaping as before: spaces -> %20 (previously applied to the whole URL).
        KnoxApiClient.getInstance().getApi()
                .setDeviceProvisionStatus(escape(status), escape(token))
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
