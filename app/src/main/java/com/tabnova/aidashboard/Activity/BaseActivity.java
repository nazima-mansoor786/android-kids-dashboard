package com.tabnova.aidashboard.Activity;

import static com.samsung.android.knox.application.ApplicationPolicy.PERMISSION_POLICY_STATE_GRANT;
import static com.tabnova.aidashboard.CustomDashboardApplication.mPrevLocation;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.samsung.android.knox.AppIdentity;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.application.ApplicationPolicy;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Database.DbHelper;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.license.LicenseManagerActivity;
import com.tabnova.aidashboard.Knox.startup.AdminReceiver;
import com.tabnova.aidashboard.Knox.startup.GetAdminActivity;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Knox.utils.Logger;
import com.tabnova.aidashboard.Model.LocationModel;
import com.tabnova.aidashboard.BlockApps.WatchingAccessibilityService;
import com.tabnova.screenshare.StartStopScreenShare;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener {


    APIManager apiManager;

    Context context;
    private static final String TAG = "BaseActivity";

    int isHomeApp = 0;

    // APP PERMISSIONS STRING ARRAY
    private final String[] permit = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // GOOGLE API CLIENT
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    int REQUEST_CHECK_SETTINGS_GPS = 7532;

    // INTEGER
    private final int request = 0;
    private Location mLastLocation;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private DbHelper dbHelper;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applyEdgeToEdgeInsets();
    }

    /** Immersive screens return true: only the display cutout is avoided (bars are hidden). */
    protected boolean useCutoutOnlyInsets() {
        return false;
    }

    /**
     * Header that should extend behind the status bar (gets the top inset). Called after
     * setContentView, so subclass views can be looked up. Null = the layout root gets it.
     */
    protected View getTopInsetView() {
        return null;
    }

    /** Screens with input fields low on the screen return true: bottom padding follows the keyboard. */
    protected boolean applyImeInsets() {
        return false;
    }

    /** True when the area behind the status bar is light: use dark status bar icons. */
    protected boolean useLightStatusBarIcons() {
        return false;
    }

    /** True when the area behind the navigation bar is light: use dark navigation bar icons. */
    protected boolean useLightNavigationBarIcons() {
        return false;
    }

    /** Child {@code index} of the layout root (for headers that have no id). */
    protected View layoutRootChild(int index) {
        ViewGroup content = findViewById(android.R.id.content);
        if (content != null && content.getChildCount() > 0 && content.getChildAt(0) instanceof ViewGroup) {
            ViewGroup root = (ViewGroup) content.getChildAt(0);
            if (index >= 0 && index < root.getChildCount()) {
                return root.getChildAt(index);
            }
        }
        return null;
    }

    private void applyEdgeToEdgeInsets() {
        Utils.applyEdgeToEdge(this, getTopInsetView(), useCutoutOnlyInsets(), applyImeInsets(),
                useLightStatusBarIcons(), useLightNavigationBarIcons());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kiosk: system Back is blocked on every screen by default (replaces the former empty
        // onBackPressed()). Screens that need Back register their own callback, which takes priority.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Intentionally empty: Back is blocked.
            }
        });
        context = this;
        apiManager = new APIManager(context);
        dbHelper = new DbHelper(this);

        try {
            if (Utils.isTiramisu() && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                String serialNo = Utils.readFromFile(context);

                if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                        !serialNo.equalsIgnoreCase(CustomDashboardApplication.getString(context, Consts.generated_serial_no))) {
                    CustomDashboardApplication.setString(context, Consts.generated_serial_no, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                }

                if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                        !CustomDashboardApplication.getString(context, Consts.generated_serial_no).equalsIgnoreCase("null")) {
                    Utils.writeToFile(context, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    Utils.writeToFile(context, CustomDashboardApplication.getSerialNumber());
                }
            } else if (Utils.isMarshMallow() && Utils.isLessTiramisu()) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    String serialNo = Utils.readFromFile(context);

                    if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                            !serialNo.equalsIgnoreCase(CustomDashboardApplication.getString(context, Consts.generated_serial_no))) {
                        CustomDashboardApplication.setString(context, Consts.generated_serial_no, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                    }

                    if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                            !CustomDashboardApplication.getString(context, Consts.generated_serial_no).equalsIgnoreCase("null")) {
                        Utils.writeToFile(context, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                    } else {
                        Utils.writeToFile(context, CustomDashboardApplication.getSerialNumber());
                    }
                }
            } else {
                if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                        !CustomDashboardApplication.getString(context, Consts.generated_serial_no).equalsIgnoreCase("null")) {
                    Utils.writeToFile(context, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    Utils.writeToFile(context, CustomDashboardApplication.getSerialNumber());
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        stopAppVisibilityService();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                locationResult.getLastLocation();
                mLastLocation = locationResult.getLastLocation();
                Logger.i("Location Update Called.. Latitude Is : " + locationResult.getLastLocation().getLatitude() +
                        " : Longitude Is : " + locationResult.getLastLocation().getLongitude());
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    if (mPrevLocation != null) {
                        if (locationResult.getLastLocation().distanceTo(mPrevLocation) > 250) {
                            //Toast.makeText(context, "Device Location API Call lat :" + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            if (NetConnectivity.isOnline(context)) {
                                apiManager.setDeviceLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            } else {
                                Toast.makeText(context, "Location stored offline 1", Toast.LENGTH_SHORT).show();
                                LocationModel locationModel = new LocationModel();
                                locationModel.setLatitude(String.valueOf(locationResult.getLastLocation().getLatitude()));
                                locationModel.setLongitude(String.valueOf(locationResult.getLastLocation().getLongitude()));
                                locationModel.setUpload(true);
                                dbHelper.insertLocationDetails(locationModel);
                            }
                            mPrevLocation = locationResult.getLastLocation();
                            //Toast.makeText(context, "lat : " + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            Log.e("latitude", locationResult.getLastLocation().getLatitude() + "");
                            Log.e("longitude", locationResult.getLastLocation().getLongitude() + "");
                        }
                    } else {
                        //Toast.makeText(context, "Device Location API Call lat :" + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        if (NetConnectivity.isOnline(context)) {
                            apiManager.setDeviceLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        } else {
                            Toast.makeText(context, "Location stored offline 2", Toast.LENGTH_SHORT).show();
                            LocationModel locationModel = new LocationModel();
                            locationModel.setLatitude(String.valueOf(locationResult.getLastLocation().getLatitude()));
                            locationModel.setLongitude(String.valueOf(locationResult.getLastLocation().getLongitude()));
                            locationModel.setUpload(true);
                            dbHelper.insertLocationDetails(locationModel);
                        }
                        mPrevLocation = locationResult.getLastLocation();
                        //Toast.makeText(context, "lat : " + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        Log.e("latitude", locationResult.getLastLocation().getLatitude() + "");
                        Log.e("longitude", locationResult.getLastLocation().getLongitude() + "");
                    }
                }
            }
        };

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(10000)
                .setInterval(60000)
                .setSmallestDisplacement(250f); // 10 seconds, in milliseconds

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            if (CustomDashboardApplication.googleApiClient != null && CustomDashboardApplication.googleApiClient.isConnected()) {
                googleApiClient = CustomDashboardApplication.googleApiClient;
            } else {
                buildGoogleApiClient();
            }

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setSmallestDisplacement(250.0f);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
                    != PackageManager.PERMISSION_GRANTED
            )
                requestpermission();
            else {
                try {
                    showGPSDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startLocationUpdates();
//                showGPSDialog();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void stopAppVisibilityService() {
        // The return-to-dashboard card is now owned by WatchingAccessibilityService.
        WatchingAccessibilityService.hideReturnOverlay();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    // REQUEST PERMISSIONS
    public void requestpermission() {
        ActivityCompat.requestPermissions(this, permit, request);
    }

    protected void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        CustomDashboardApplication.googleApiClient = googleApiClient;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == request) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    showGPSDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    try {
                        showGPSDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //startLocationService();
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to

                    break;
                default:
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {

                if (mPrevLocation != null) {
                    if (location.distanceTo(mPrevLocation) > 250) {
                        //Toast.makeText(context, "Device Location API Call lat :" + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        apiManager.setDeviceLocation(location.getLatitude(), location.getLongitude());
                        mPrevLocation = location;
                        //Toast.makeText(context, "lat : " + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        Log.e("latitude", location.getLatitude() + "");
                        Log.e("longitude", location.getLongitude() + "");
                    }
                } else {
                    //Toast.makeText(context, "Device Location API Call lat :" + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    apiManager.setDeviceLocation(location.getLatitude(), location.getLongitude());
                    mPrevLocation = location;
                    //Toast.makeText(context, "lat : " + location.getLatitude() + " long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.e("latitude", location.getLatitude() + "");
                    Log.e("longitude", location.getLongitude() + "");
                }

             /*   if (mLastLocation != null) {
                    mLastLocation = location;
                    mLastLocation = mPrevLocation;
                } else {*/
                // mPrevLocation = location;
                //  }
            }

        }

        startLocationUpdates();
        //stop location update
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Logger.i("BaseActivity : Resume");

        StartStopScreenShare.context = context;

        Log.e(TAG, "onCreate: Serial Number : " + CustomDashboardApplication.getString(context, Consts.generated_serial_no));

        try {
            if (CustomDashboardApplication.isDeviceSamsungActivated() && SessionManager.getInstance(this).getIsDashboardLaunch()) {
                //if (!SuperLockState.getInstance(this).isKioskModeEnabled(this)) {
                //Logger.i("BaseActivity : Enable Kiosk called");
                CustomDashboardApplication.enableSamsungKiosk(this);
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //preventStatusBarExpansion();

        if (SessionManager.getInstance(this).getIsLicenseEnabled() &&
                CustomDashboardApplication.isDeviceSamsung() && !SuperLockState.getInstance(this).isESDKLicenseActivated()) {
            isHomeApp = 1;
            SuperLockState superLockState = new SuperLockState(context);
            if (superLockState.supportedActivation()) {
                if (!superLockState.isESDKLicenseActivated()) {
                    CustomDashboardApplication.setInt(context, Consts.IS_KNOX_SUPPORT, 0);
                    //For Samsung Knox
                    Log.d(TAG, "Getting admin permissions");
                    DevicePolicyManager mDPM;
                    mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    ComponentName mDeviceAdmin;
                    mDeviceAdmin = new ComponentName(BaseActivity.this, AdminReceiver.class);
                    if (mDPM.isAdminActive(mDeviceAdmin)) {
                        Log.d(TAG, "We have admin");

                        SuperLockState mSuperLockState = SuperLockState.getInstance(BaseActivity.this);

                        if (mSuperLockState != null && !mSuperLockState.isESDKLicenseActivated()) {
                            //startService(new Intent(BaseActivity.this, LoginService.class));
                            startActivity(new Intent(context, LicenseManagerActivity.class));
                        }
                    } else {
                        Log.d(TAG, "We need admin");
                        Intent intent = new Intent(getApplicationContext(), GetAdminActivity.class);
                        startActivity(intent);
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.IS_KNOX_SUPPORT, 1);
                }
            }
        }

        try {
            try {
                if (Settings.System.canWrite(this)) {
                    Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.tabnova.aidashboard/com.tabnova.aidashboard.BaseActivity.java");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.GET_ACCOUNTS)
                                != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED)
                    setAllPermission(this);
        }

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            if (NetConnectivity.isOnline(context)) {
                List<LocationModel> locations = new ArrayList<>();
                locations.addAll(dbHelper.getAllLocations());
                if (locations.size() > 0) {
                    for (int i = 0; i < locations.size(); i++) {
                        if (locations.get(i).isUpload()) {
                            try {
                                //Toast.makeText(context, "Latitude : " + locations.get(i).getLatitude() + " : Longitude : " + locations.get(i).getLongitude(), Toast.LENGTH_SHORT).show();
                                Logger.i("Offline location upload : Latitude : " + locations.get(i).getLatitude() + " : Longitude : " + locations.get(i).getLongitude());
                                apiManager.uploadOfflineDeviceLocation(Double.parseDouble(locations.get(i).getLatitude()), Double.parseDouble(locations.get(i).getLongitude()), locations.get(i).getId());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else
                requestpermission();
        } else {
            requestpermission();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void startLocationService() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                Location location = null;
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(10000);
                locationRequest.setSmallestDisplacement(10.0f);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, request);
                    requestpermission();
                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                }

                // mLastLocation = location;
            } else {
                buildGoogleApiClient();
            }
        }
    }

    public void showGPSDialog() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = null;
            try {
                result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result != null) {
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        Status status = locationSettingsResult.getStatus();

                        if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                            startLocationUpdates();
                        } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(BaseActivity.this, REQUEST_CHECK_SETTINGS_GPS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                                // Ignore the error.
                            }
                        } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {

                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (CustomDashboardApplication.getInt(context, Consts.IS_FIRST_INSTALLED) == 1) {
            Log.e(TAG, "onUserLeaveHint: ");
            if (isHomeApp != 1 && !CustomDashboardApplication.isCustomSettingScreenCall && SessionManager.getInstance(context).getIsDashboardLaunch()) {
                Log.e(TAG, "Inside onUserLeaveHint: ");
                // Toast.makeText(context, "On UserLeave Hint", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Utils.isAppIsInBackground()) {
                            WatchingAccessibilityService.hideReturnOverlay();
                            if (!CustomDashboardApplication.isDeviceSamsung())
                                WatchingAccessibilityService.showReturnOverlay(context);
                        }
                    }
                }, 2000);
            }
        }
    }

    void setAllPermission(Context context) {
        List<String> runtimePermissions = new ArrayList<String>();
        runtimePermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        runtimePermissions.add("android.permission.ACCESS_FINE_LOCATION");
        runtimePermissions.add("android.permission.ACCESS_WIFI_STATE");
        runtimePermissions.add("android.permission.CHANGE_WIFI_STATE");
        runtimePermissions.add("android.permission.INTERNET");
        runtimePermissions.add("android.permission.ACCESS_NETWORK_STATE");
        runtimePermissions.add("android.permission.WAKE_LOCK");
        runtimePermissions.add("android.permission.VIBRATE");
        runtimePermissions.add("android.permission.GET_ACCOUNTS");
        runtimePermissions.add("com.android.browser.permission.WRITE_HISTORY_BOOKMARKS");
        runtimePermissions.add("com.google.android.c2dm.permission.RECEIVE");
        runtimePermissions.add("android.permission.sec.MDM_APP_MGMT");
        runtimePermissions.add("com.android.launcher.permission.INSTALL_SHORTCUT");
        runtimePermissions.add("com.android.launcher.permission.UNINSTALL_SHORTCUT");
        runtimePermissions.add("android.permission.WRITE_SETTINGS");
        runtimePermissions.add("android.permission.SET_WALLPAPER");
        runtimePermissions.add("android.permission.REQUEST_DELETE_PACKAGES");
        runtimePermissions.add("android.permission.SYSTEM_ALERT_WINDOW");
//        runtimePermissions.add("android.permission.READ_CONTACTS");
//        runtimePermissions.add("android.permission.WRITE_CONTACTS");
        runtimePermissions.add("android.permission.READ_PHONE_STATE");
        runtimePermissions.add("android.permission.BLUETOOTH");
        runtimePermissions.add("android.permission.BLUETOOTH_ADMIN");

        EnterpriseDeviceManager edm = EnterpriseDeviceManager.getInstance(context);
        ApplicationPolicy appPolicy = edm.getApplicationPolicy();
        AppIdentity appIdentity = new AppIdentity(getApplicationContext().getPackageName(), null);
        try {
            appPolicy.applyRuntimePermissions(appIdentity, runtimePermissions, PERMISSION_POLICY_STATE_GRANT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WindowManager manager;
    private MainActivity.customViewGroup view;

    // PREVENT STATUS BAR
    public void preventStatusBarExpansion() {
        manager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            disablePullNotificationTouch();
        }
    }

    private void disablePullNotificationTouch() {
        manager = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        } else {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (25 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;
        view = new MainActivity.customViewGroup(this);

        manager.addView(view, localLayoutParams);
    }
}