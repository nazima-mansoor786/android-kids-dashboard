package com.tabnova.aidashboard.Knox.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Knox.license.Const;
import com.tabnova.aidashboard.Knox.license.EnhancedStringRequest;
import com.tabnova.aidashboard.Knox.license.TokenManager;
import com.tabnova.aidashboard.Knox.utils.PackageUtils;
import com.tabnova.aidashboard.Knox.utils.SysUtils;


public class SystemInfoService extends Service  {

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
       // Locator mLocator = new Locator(mContext);

        //mLocator.getLocation(Locator.Method.NETWORK_THEN_GPS, new LocationCallback());

        tokenManager = TokenManager.getInstance();
        this.backgroundThread = new Thread(myTask);
	}


    private final Runnable myTask = new Runnable() {
        public void run() {
            String token = tokenManager.getToken();
            if(token == null ||token.length() <= 0) {

                releaseService();
                return;
            }


            updateSystemAndFCM();


        }
    };

    private void updateSystemAndFCM() {
        String token = tokenManager.getToken();
        if(token == null ||token.length() <= 0) {

            releaseService();
            return;
        }
        /*
            /api/v1/setdevicedetails/{device_battery_level}/{device_activation_date}/{device_osp_client_version}/
            {device_operating_system}/{device_model_number}/{device_mac_address}/{device_ip_address}/
            {device_location}/{device_gateway_ip}/{device_profile_counter}/{token}
         */

        String requestString = Const.DEVICE_DEVICE_DETAILS + SysUtils.getBatteryLevel(mContext) +"/" +
                PackageUtils.getAppInstallDate(mContext, getApplicationContext().getPackageName()) + "/" +
                SysUtils.getVersionName(mContext,getApplicationContext().getPackageName()) +"/" +
                SysUtils.getOSVersion()+"/" +
                SysUtils.getDeviceModel() + "/" +
                SysUtils.getMacAddr() + "/" +
                SysUtils.getLocalIPAddress(mContext) + "/" +
                SysUtils.getLocation(mContext) + "/" +
                SysUtils.getLocalIPAddress(mContext) + "/" +
                SysUtils.getInt(mContext, "profile_counter") + "/" +
                token;
        String sysReq  = requestString.replaceAll(" ", "%20");
        EnhancedStringRequest myReq = new EnhancedStringRequest(sysReq ,
                null,
                createSuccessListener(),
                createErrorListener());
        CustomDashboardApplication.getInstance().addToRequestQueue(myReq);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }

	private Response.Listener<String> createSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
                if(response != null ) {

                    releaseService();
                }


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

	@Override
	public void onDestroy() {
		super.onDestroy();
        this.isRunning = false;
	}

    private void releaseService(){
        tokenManager.releaseTokenRef();
        stopSelf();
    }


}
