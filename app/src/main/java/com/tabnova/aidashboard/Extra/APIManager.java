package com.tabnova.aidashboard.Extra;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Database.DbHelper;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Knox.utils.PackageUtils;
import com.tabnova.aidashboard.Knox.utils.SysUtils;
import com.tabnova.aidashboard.Model.AddedWebShortcutModel;
import com.tabnova.aidashboard.Model.AdvanceSettingsModel;
import com.tabnova.aidashboard.Model.AppCategoryModel;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.Model.CallLogModel;
import com.tabnova.aidashboard.Model.DailyTimerModel;
import com.tabnova.aidashboard.Model.ProfileData;
import com.tabnova.aidashboard.Model.SaveCallHistoryModel;
import com.tabnova.aidashboard.Model.ScheduleEventModel;
import com.tabnova.aidashboard.Model.SmsLogModel;
import com.tabnova.aidashboard.Model.UploadAppsServerModel;
import com.tabnova.aidashboard.Model.WebHistoryModel;
import com.tabnova.aidashboard.Model.WipeButtonsModel;
import com.tabnova.aidashboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class APIManager {

    // CONTEXT
    Context context;

    // SUCCESS LISTENER
    OnResponseListener onResponseListener;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // APP PERMISSIONS
    private String permit[] = new String[]{Manifest.permission.READ_PHONE_STATE};

    Integer request = 0;

    public APIManager(Context context) {
        this.context = context;
//        dialogLoader = new DialogLoader(context);
       /* if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    getSerialNumber();
                } else {
                    requestpermission();
                }
            }
        }*/
    }

    public APIManager(Context context, OnResponseListener onResponseListener) {
        this.context = context;
        this.onResponseListener = onResponseListener;
        dialogLoader = new DialogLoader(context);
       /* if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    getSerialNumber();
                } else {
                    requestpermission();
                }
            }
        }*/
    }

    // UPDATE DEVICE DATE ON SERVER FOR SERVER SIDE FILTRATION
    public void updateDataOnServer() {
        try {
            if (NetConnectivity.isOnline(context)) {

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String appInstalledDate;

                if (CustomDashboardApplication.isDeviceSamsung()) {
                    if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                        appInstalledDate = "Knox Activated " + PackageUtils.getAppInstallDate(context, context.getApplicationContext().getPackageName());
                    } else {
                        appInstalledDate = "Knox Not Activated " + PackageUtils.getAppInstallDate(context, context.getApplicationContext().getPackageName());
                    }
                } else {
                    appInstalledDate = PackageUtils.getAppInstallDate(context, context.getApplicationContext().getPackageName());
                }

                // API CALL
                Call<JsonObject> calls = feedApi.updateDataOnServer(Consts.BaseUrl + Consts.DEVICE_DEVICE_DETAILS + SysUtils.getBatteryLevel(context) + "/" +
                        appInstalledDate + "/" +
                        SysUtils.getVersionName(context, context.getApplicationContext().getPackageName()) + "/" +
                        SysUtils.getOSVersion() + "/" +
                        SysUtils.getDeviceModel() + "/" +
                        SysUtils.getMacAddr() + "/" +
                        SysUtils.getLocalIPAddress(context) + "/" +
                        SysUtils.getLocation(context) + "/" +
                        SysUtils.getLocalIPAddress(context) + "/" +
                        SysUtils.getInt(context, "profile_counter") + "/" +
                        CustomDashboardApplication.getString(context, Consts.token));

                Log.e("serverReq", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //
    public void uploadWebHistoryToServer() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // APPLICATIONS JSON ARRAY
                JSONArray appsArray = new JSONArray();

                ArrayList<WebHistoryModel> webHistoryModels = new ArrayList<>();

                String ser = SerializeObject.ReadSettings(context, Consts.webHistoryFile);
                if (ser != null && !ser.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something

                        Collections.sort((ArrayList<WebHistoryModel>) obj, new Comparator<WebHistoryModel>() {
                            @Override
                            public int compare(WebHistoryModel lhs, WebHistoryModel rhs) {
                                return lhs.getEndTime().compareTo(rhs.getEndTime());
                            }
                        });

                        webHistoryModels = (ArrayList<WebHistoryModel>) obj;
                    }
                }

                if (webHistoryModels.size() > 0) {
                    for (int i = 0; i < webHistoryModels.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Consts.domain_url, webHistoryModels.get(i).getLinkStr());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yyyy");
                        Date serverDate = null;
                        try {
                            serverDate = formatter.parse(webHistoryModels.get(i).getDate());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String finalDate = formatter.format(serverDate);

                        jsonObject.put(Consts.open_date, finalDate);
                        jsonObject.put(Consts.start_time, webHistoryModels.get(i).getStartTime());
                        jsonObject.put(Consts.end_time, webHistoryModels.get(i).getEndTime());
                        jsonObject.put(Consts.usage_time, CustomDashboardApplication.calculateMinutes(webHistoryModels.get(i).getStartTime(), webHistoryModels.get(i).getEndTime()));
                        appsArray.put(jsonObject);
                    }

                    Log.e("appsArray", appsArray.toString());
                }

                JsonArray appData;
                JsonParser jsonParser = new JsonParser();
                appData = (JsonArray) jsonParser.parse(appsArray.toString());

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.add(Consts.applications, appData);
                Log.e("reqObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.addWebHistory(jsonObject);
                Log.e("url", calls.request().toString());

                final ArrayList<WebHistoryModel> finalWebHistoryModels = webHistoryModels;
                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        if (finalWebHistoryModels.size() > 0) {
                                            finalWebHistoryModels.clear();
                                            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);

                                            /*String dir = context.getFilesDir().getAbsolutePath();
                                            File f0 = new File(dir, "webHistory.dat");
                                            if (f0.exists()) {
                                                f0.delete();
                                            }*/
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        // dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // UPDATE ADVANCE SETTINGS API
    public void updateAdvanceSettings() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                ArrayList<AdvanceSettingsModel> advanceSettingsModels = new ArrayList<>();
                String advanceSettingsSer = SerializeObject.ReadSettings(context, Consts.advanceSettingsFile);
                if (advanceSettingsSer != null && !advanceSettingsSer.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(advanceSettingsSer);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        advanceSettingsModels = (ArrayList<AdvanceSettingsModel>) obj;
                    }
                }

                JsonObject reqObj = new JsonObject();
                reqObj.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                reqObj.addProperty(Consts.profile_id, profileId);
                reqObj.addProperty(Consts.factory_reset_status, advanceSettingsModels.get(0).getStatus());
                reqObj.addProperty(Consts.home_key_status, advanceSettingsModels.get(1).getStatus());
                reqObj.addProperty(Consts.volume_down_key_status, advanceSettingsModels.get(2).getStatus());
                reqObj.addProperty(Consts.status_bar_status, advanceSettingsModels.get(3).getStatus());
                reqObj.addProperty(Consts.s_voice_status, advanceSettingsModels.get(4).getStatus());
                reqObj.addProperty(Consts.over_the_air_status, advanceSettingsModels.get(5).getStatus());
                reqObj.addProperty(Consts.back_key_status, advanceSettingsModels.get(6).getStatus());
                reqObj.addProperty(Consts.power_key_status, advanceSettingsModels.get(7).getStatus());
                reqObj.addProperty(Consts.cardboard_apps_on_gear_vr_status, advanceSettingsModels.get(8).getStatus());
                reqObj.addProperty(Consts.recents_key_status, advanceSettingsModels.get(9).getStatus());
                reqObj.addProperty(Consts.volume_up_key_status, advanceSettingsModels.get(10).getStatus());


                // API CALL
                Call<JsonObject> calls = feedApi.updateAdvanceSettings(reqObj);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE WIPE APPLICATION API
    public void updateWipeDetails() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                ArrayList<WipeButtonsModel> wipeModels = new ArrayList<>();
                String wipeDetailsSer = SerializeObject.ReadSettings(context, Consts.wipeDataFile);
                if (wipeDetailsSer != null && !wipeDetailsSer.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(wipeDetailsSer);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        wipeModels = (ArrayList<WipeButtonsModel>) obj;
                    }
                }

                JsonObject reqObj = new JsonObject();
                reqObj.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                reqObj.addProperty(Consts.profile_id, profileId);
                reqObj.addProperty(Consts.uninstall_apps, wipeModels.get(0).getStatus());
                reqObj.addProperty(Consts.clean_app_data, wipeModels.get(1).getStatus());
                reqObj.addProperty(Consts.file_path, wipeModels.get(2).getStatus());
                reqObj.addProperty(Consts.remove_call_logs, wipeModels.get(3).getStatus());
                reqObj.addProperty(Consts.remove_sms_logs, wipeModels.get(4).getStatus());
                reqObj.addProperty(Consts.clean_web_cookies, wipeModels.get(5).getStatus());
                reqObj.addProperty(Consts.from_date, "");
                reqObj.addProperty(Consts.to_date, "");


                // API CALL
                Call<JsonObject> calls = feedApi.updateWipeDetails(reqObj);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // CHECK PAYMENT STATUS
    public void checkPaymentStatus() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();


                // API CALL
                Call<JsonObject> calls = feedApi.checkPaymentStatus(CustomDashboardApplication.getString(context, Consts.token), String.valueOf(profileId));
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());

                            try {
                                JSONObject respObj = new JSONObject(responseString);
                                int status = respObj.getInt(Consts.status);
                                if (status == 1) {
                                    updateAdvanceSettings();
                                    updateWipeDetails();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // UPDATE PROFILE SETTINGS
    public void updateProfileSettings() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.profile_id, profileId);
                jsonObject.addProperty(Consts.status_bar_status, CustomDashboardApplication.getInt(context, Consts.APP_INFO));
                jsonObject.addProperty(Consts.volume_key_status, CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME));
                jsonObject.addProperty(Consts.status_bar_text, CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
                jsonObject.addProperty(Consts.battery_status, CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS));
                jsonObject.addProperty(Consts.battery_level, CustomDashboardApplication.getString(context, Consts.BATTERY_LEVEL));
                jsonObject.addProperty(Consts.date_time_setting, CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING));
                jsonObject.addProperty(Consts.shortcuts_status, CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD));
                jsonObject.addProperty(Consts.secure_web_browser, CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE));
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.updateProfileSettings(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();
//                        onResponseListener.onSuccess(response);

                       /* if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                            try {
                                AlertDialog d = null;
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {

                                    try {

                                        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                        final AlertDialog finalD = d;
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                                finalD.dismiss();
                                            }
                                        });
                                        d = dialogBuilder1.create();
                                        final AlertDialog finalD1 = d;
                                        d.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                Button doneBtn = finalD1.getButton(DialogInterface.BUTTON_NEUTRAL);
                                                doneBtn.setAllCaps(false);
                                            }
                                        });
                                        //  d.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }*/
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        // onResponseListener.onFailed(t);
                        // dialogLoader.hideProgressDialog();
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    ArrayList<AppCategoryModel> appCategoryModels = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void getAppsCategories(String packageName, final ApplicationInfo applicationInfo, int listSize) {


    /*    int currentCount = 0;

        if (currentCount == 0){
            dialogLoader.showProgressDialog();
        }*/

        new AsyncTask<String, String, String>() {
            String g;
            String category;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //dialogLoader.showProgressDialog();
            }

            @Override
            protected String doInBackground(String... voids) {
                //PackageManager pm;

                //pm = context.getPackageManager();
                //List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                //for (final ApplicationInfo packageInfo : packages) {
                g = voids[0];
                final String query_url = Consts.GOOGLE_APP_CATEGORY_PREFIX_URL + g;  //GOOGLE_URL + packageInfo.packageName;
                category = getCategory(query_url, applicationInfo);
                Log.e("CATEGORY", g + ": " + category);
                //   }


                return category;
            }

            @Override
            protected void onPostExecute(final String s) {
                super.onPostExecute(s);
                //dialogLoader.showProgressDialog();
                AppCategoryModel appCategoryModel = new AppCategoryModel();
                appCategoryModel.setPackageName(g);
                if (s == null) {
                    appCategoryModel.setAppCategory("");
                } else {
                    appCategoryModel.setAppCategory(s);
                }
                //appCategoryModel.setAppCategory(s);
                appCategoryModels.add(appCategoryModel);

                Intent intent = new Intent(Consts.app_category);
                intent.putExtra(Consts.local_apps_count, appCategoryModels.size());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String dir = context.getFilesDir().getAbsolutePath();
                        File f0 = new File(dir, Consts.appCategoryFile);
                        if (f0.exists()) {
                            f0.delete();
                        }

                        String ser1 = SerializeObject.objectToString(appCategoryModels);
                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, ser1, Consts.appCategoryFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.appCategoryFile);
                        }
                    }
                }, 2000);


            }
        }.execute(packageName);

    }

    private String getCategory(String query_url, ApplicationInfo applicationInfo) {

        String category = null;
        try {
            Document doc = Jsoup.connect(query_url).get();
            Elements link = doc.select("a[class=\"hrTbp R8zArc\"]");
            //Elements link = doc.select("span[itemprop=genre]");
            category = link.last().text();
            Log.e("ValidCategory", category);
            return category;
        } catch (Exception e) {
            if (isSystemPackage(applicationInfo)) {
                category = "System";
            } else {
                category = "Others";
            }
            Log.e("OthersCategory", category);
        }
        return category;
    }

    public boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    // UPLOAD APPS DATA TO SERVER
    public void uploadAppsToServer() {
        ArrayList<AppModel> systemAppsModels = new ApkInfoExtractor(context).GetAllInstalledApkInfo();

        JSONArray jsonArray = new JSONArray();

        ArrayList<UploadAppsServerModel> uploadAppsServerModels = new ArrayList<>();
        ArrayList<AppModel> selectedApps = new ArrayList<>();

        String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
        if (!ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                selectedApps = (ArrayList<AppModel>) obj;
            }
        }

        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

        for (int i = 0; i < systemAppsModels.size(); i++) {
            UploadAppsServerModel uploadAppsServerModel = new UploadAppsServerModel();
            uploadAppsServerModel.setId(i);
            uploadAppsServerModel.setPackageName(systemAppsModels.get(i).getPackageDetails());
            uploadAppsServerModel.setGrid_row(systemAppsModels.get(i).getRow());
            uploadAppsServerModel.setGrid_column(systemAppsModels.get(i).getColumn());
            uploadAppsServerModel.setPosition(systemAppsModels.get(i).getPosition());
            uploadAppsServerModel.setLocation(systemAppsModels.get(i).getLocation());
            if (systemAppsModels.get(i).getPackageDetails().equals("com.dialer")) {
                uploadAppsServerModel.setAppVersion("1.0");
                uploadAppsServerModel.setAppVersionCode(1);
                uploadAppsServerModel.setTargetSdk("");
                uploadAppsServerModel.setDateInstalled("");
                uploadAppsServerModel.setAppName("Dialer");
                uploadAppsServerModel.setAppInfo("");
                uploadAppsServerModel.setLocked(systemAppsModels.get(i).isLocked());
                uploadAppsServerModel.setAppCategory(systemAppsModels.get(i).getAppCategory());
            } else {
                String appVersionName = CustomDashboardApplication.getAppVersion(context, systemAppsModels.get(i).getPackageDetails());
                if (appVersionName != null && !TextUtils.isEmpty(appVersionName))
                    uploadAppsServerModel.setAppVersion(appVersionName);
                else {
                    uploadAppsServerModel.setAppVersion("-1");
                }
                uploadAppsServerModel.setAppVersionCode(getAppVersionCode(systemAppsModels.get(i).getPackageDetails()));
                uploadAppsServerModel.setTargetSdk(systemAppsModels.get(i).getTargetSdk());
                uploadAppsServerModel.setDateInstalled(systemAppsModels.get(i).getDateInstalled());
                uploadAppsServerModel.setAppName(apkInfoExtractor.GetAppName(systemAppsModels.get(i).getPackageDetails()));
                uploadAppsServerModel.setAppInfo("");
                uploadAppsServerModel.setLocked(systemAppsModels.get(i).isLocked());
                uploadAppsServerModel.setAppCategory(systemAppsModels.get(i).getAppCategory());
            }

            for (int k = 0; k < selectedApps.size(); k++) {
                if (selectedApps.get(k).getPackageDetails().contains(systemAppsModels.get(i).getPackageDetails())) {
                    if (selectedApps.get(k).getColumn() > 0 && selectedApps.get(k).getRow() > 0) {
                        int position = 0;
                        if (selectedApps.get(k).getColumn() > 3) {
                            selectedApps.get(k).setColumn(3);
                        }
                        if (selectedApps.get(k).getRow() > 1) {
                            position = ((selectedApps.get(k).getRow() - 1) * 3 + selectedApps.get(k).getColumn()) - 1;
                        } else {
                            position = selectedApps.get(k).getColumn() - 1;
                        }

                        selectedApps.get(k).setPosition(position);
                        selectedApps.get(k).setLocation(selectedApps.get(k).getRow() + "," + selectedApps.get(k).getColumn());
                        uploadAppsServerModel.setGrid_row(selectedApps.get(k).getRow());
                        uploadAppsServerModel.setGrid_column(selectedApps.get(k).getColumn());
                        uploadAppsServerModel.setPosition(selectedApps.get(k).getPosition());
                        uploadAppsServerModel.setLocation(selectedApps.get(k).getLocation());
                    }
                    if (selectedApps.get(k).isSelected()) {
                        uploadAppsServerModel.setIsSelected(1);
                    } else {
                        uploadAppsServerModel.setIsSelected(0);
                    }
                }
            }

            if (!systemAppsModels.get(i).getPackageDetails().equalsIgnoreCase("com.securewebview"))
                uploadAppsServerModels.add(uploadAppsServerModel);
            //  }
        }

        for (int j = 0; j < uploadAppsServerModels.size(); j++) {
            try {
                JSONObject dataObj = new JSONObject();

                dataObj.put(Consts.package_name, uploadAppsServerModels.get(j).getPackageName());
                dataObj.put(Consts.app_name, uploadAppsServerModels.get(j).getAppName());
                dataObj.put(Consts.version_number, uploadAppsServerModels.get(j).getAppVersion());
                dataObj.put(Consts.sdk_version, uploadAppsServerModels.get(j).getTargetSdk());
                dataObj.put(Consts.version_code, uploadAppsServerModels.get(j).getAppVersionCode());
                dataObj.put(Consts.application_size, 0);
                dataObj.put(Consts.device_application_installed_dates, uploadAppsServerModels.get(j).getDateInstalled());
                dataObj.put(Consts.application_type, 1);
                dataObj.put(Consts.device_application_install_status, 1);
                dataObj.put(Consts.action_type, 1);
                dataObj.put(Consts.device_application_active_status, 0);
                dataObj.put(Consts.additional_info, uploadAppsServerModels.get(j).getAppInfo());
                dataObj.put(Consts.added_to_dashboard, uploadAppsServerModels.get(j).getIsSelected());
                dataObj.put(Consts.category_name, uploadAppsServerModels.get(j).getAppCategory());
                dataObj.put(Consts.grid_row, uploadAppsServerModels.get(j).getGrid_row());
                dataObj.put(Consts.grid_column, uploadAppsServerModels.get(j).getGrid_column());
                dataObj.put(Consts.position, uploadAppsServerModels.get(j).getPosition());
                dataObj.put(Consts.location, uploadAppsServerModels.get(j).getLocation());
                if (uploadAppsServerModels.get(j).isLocked()) {
                    dataObj.put(Consts.is_app_locked, 1);
                } else {
                    dataObj.put(Consts.is_app_locked, 0);
                }
                jsonArray.put(dataObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e("apps", jsonArray.toString());

        JsonArray appData;
        JsonParser jsonParser = new JsonParser();
        appData = (JsonArray) jsonParser.parse(jsonArray.toString());

        JsonObject reqObj = new JsonObject();
        reqObj.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
        reqObj.add(Consts.applications, appData);
        Log.e("reqObj", reqObj.toString());

        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.uploadDeviceApplication(reqObj);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // REQUEST PERMISSIONS
    public void requestpermission() {
        ActivityCompat.requestPermissions((Activity) context, permit, request);
    }

    // DELETE WALLPAPER
    public void deleteWallpaper() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();


                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.profile_id, profileId);

                Log.e("reqObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.deleteWallpaper(jsonObject);
                Log.e("url", calls.request().toString());


                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        // dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        // dialogLoader.hideProgressDialog();
                    }
                });
            } //else {
            //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            //  }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDeviceLocation(double lat, double lng) {
        try {
            if (NetConnectivity.isOnline(context)) {
                // dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);


                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(lng);
                jsonObject.addProperty(Consts.latlong, latitude + "," + longitude);
                Log.e("reqObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.setDeviceLocation(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //  dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    //  ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        // dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadOfflineDeviceLocation(double lat, double lng, final int id) {
        try {
            if (NetConnectivity.isOnline(context)) {
                // dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(lng);
                jsonObject.addProperty(Consts.latlong, latitude + "," + longitude);
                Log.e("reqObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.setDeviceLocation(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        //  dialogLoader.hideProgressDialog();
                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    if (id > 0) {
                                        DbHelper.getInstance(context).deleteLocationData(id);
                                    }
                                    try {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    //  ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        // dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPLOAD ANALYTICS DATA TO SERVER
    public void uploadAnalyticsDataToServer() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                ArrayList<ApplicationTimerModel> userModels = new ArrayList<>();

                String ser = SerializeObject.ReadSettings(context, Consts.appAnalyticsFile);
                if (ser != null && !ser.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something

                        Collections.sort((ArrayList<ApplicationTimerModel>) obj, new Comparator<ApplicationTimerModel>() {
                            @Override
                            public int compare(ApplicationTimerModel lhs, ApplicationTimerModel rhs) {
                                return lhs.getEndTime().compareTo(rhs.getEndTime());
                            }
                        });

                        userModels = (ArrayList<ApplicationTimerModel>) obj;
                    }
                }


                // APPLICATIONS JSON ARRAY
                JSONArray appsArray = new JSONArray();

                if (userModels.size() > 0) {
                    for (int i = 0; i < userModels.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Consts.package_name, userModels.get(i).getAppPackageName());
                        jsonObject.put(Consts.app_name_str, userModels.get(i).getAppName());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yyyy");
                        Date serverDate = null;
                        try {
                            serverDate = formatter.parse(userModels.get(i).getDate());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String finalDate = formatter.format(serverDate);

                        jsonObject.put(Consts.open_date, finalDate);
                        jsonObject.put(Consts.start_time, userModels.get(i).getStartTime());
                        jsonObject.put(Consts.end_time, userModels.get(i).getEndTime());
                        jsonObject.put(Consts.usage_time, calculateMinutes(userModels.get(i).getStartTime(), userModels.get(i).getEndTime()));
                        appsArray.put(jsonObject);
                    }

                    Log.e("appsArray", appsArray.toString());

                    JsonArray appData;
                    JsonParser jsonParser = new JsonParser();
                    appData = (JsonArray) jsonParser.parse(appsArray.toString());

                    // REQUEST OBJECT
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                    //jsonObject.addProperty(Consts.device_serial_number, "R52J90FPNLD");
                    if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                            && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                        jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                    } else {
                        jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                    }
                    jsonObject.add(Consts.applications, appData);
                    Log.e("reqObj", jsonObject.toString());

                    // API CALL
                    Call<JsonObject> calls = feedApi.uploadAppsAnalyticsData(jsonObject);
                    Log.e("url", calls.request().toString());

                    final ArrayList<ApplicationTimerModel> finalUserModels = userModels;
                    calls.enqueue(new Callback<JsonObject>() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            //dialogLoader.hideProgressDialog();

                            if (response.code() == 200) {
                                String responseString = response.body().toString();
                                Log.e("respCounter", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {
                                        try {
                                            //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                            if (finalUserModels.size() > 0) {
                                                finalUserModels.clear();
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    if (CustomDashboardApplication.currentApplicationModels != null &&
                                                            CustomDashboardApplication.currentApplicationModels.size() > 0) {
                                                        CustomDashboardApplication.currentApplicationModels.clear();
                                                        CustomDashboardApplication.currentApplicationModels = new ArrayList<>();
                                                    }
                                                } else {
                                                    if (CustomDashboardApplication.currentApplicationModels.size() > 0) {
                                                        CustomDashboardApplication.currentApplicationModels.clear();
                                                        CustomDashboardApplication.currentApplicationModels = new ArrayList<>();
                                                    }
                                                }

                                                String dir = context.getFilesDir().getAbsolutePath();
                                                File f0 = new File(dir, Consts.appAnalyticsFile);
                                                if (f0.exists()) {
                                                    f0.delete();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                try {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                            //dialogLoader.hideProgressDialog();
                        }
                    });
                }


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPLOAD WEB HISTORY TO SERVER

    // UPDATE PIN
    public void updatePin(String password) {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.profile_id, profileId);
                jsonObject.addProperty(Consts.pin, password);
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.updatePin(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

//                        onResponseListener.onSuccess(response);

                        /*AlertDialog d = null;
                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {

                                    try {
                                        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                        final AlertDialog finalD = d;
                                        *//*dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ((Activity)context).finish();
                                               // d.dismiss();
                                            }
                                        });*//*
                                        d = dialogBuilder1.create();
                                        final AlertDialog finalD1 = d;
                                        d.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                Button doneBtn = finalD1.getButton(DialogInterface.BUTTON_NEUTRAL);
                                                doneBtn.setAllCaps(false);
                                            }
                                        });
                                        d.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }*/
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        /* t.printStackTrace();*/
                        // onResponseListener.onFailed(t);
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // GET PROFILE DATA
    public void getProfile() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                ArrayList<DailyTimerModel> dailyTimerModels = new ArrayList<>();
                String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        dailyTimerModels = (ArrayList<DailyTimerModel>) obj;
                    }
                }

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                String apiAuthToken = profileData.getData().getApiAuthToken();
                String profileId = profileData.getData().getUserProfiles().get(0).getId().toString();

                // API CALL
                Call<JsonObject> calls = feedApi.getProfile(Consts.BaseUrl + Consts.SERVICE_GET_PROFILE_LIST + apiAuthToken, profileId);
                Log.e("url", calls.request().toString());

                final ArrayList<DailyTimerModel> finalDailyTimerModels = dailyTimerModels;
                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {

                                    try {

                                        JSONObject dataObj = jsonObject.getJSONObject(Consts.data);


                                        // SET VOLUME CONTROL SETTING
                                        if (Utils.contains(dataObj, Consts.volume_key_status)) {
                                            CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, (dataObj.getInt(Consts.volume_key_status)));
                                        }

                                        // SET STATUS BAR (LABEL/APP INFO) SETTING
                                        if (Utils.contains(dataObj, Consts.status_bar_status)) {
                                            CustomDashboardApplication.setInt(context, Consts.APP_INFO, (dataObj.getInt(Consts.status_bar_status)));
                                        }

                                        // SET STATUS BAR LABEL STRING
                                        if (Utils.contains(dataObj, Consts.status_bar_text)) {
                                            CustomDashboardApplication.setString(context, Consts.APP_INFO_STR, (dataObj.getString(Consts.status_bar_text)));
                                           /* if (dataObj.getInt(Consts.status_bar_status) == 1){
                                                MainActivity.txInfo.setVisibility(View.VISIBLE);
                                                MainActivity.txInfo.setText(dataObj.getString(Consts.status_bar_text));
                                            } else {
                                                MainActivity.txInfo.setVisibility(View.GONE);
                                            }*/
                                        }

                                        // SET SECURE WEB VIEW
                                        if (Utils.contains(dataObj, Consts.secure_web_browser)) {
                                            CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, dataObj.getInt(Consts.secure_web_browser));
                                        }

                                        // SET WALLPAPER
                                        if (Utils.contains(dataObj, Consts.wallpaper)) {
                                            if (!dataObj.getString(Consts.wallpaper).equals("null") &&
                                                    !dataObj.getString(Consts.wallpaper).equalsIgnoreCase(Consts.defaultWallpaperPath)) {
                                                CustomDashboardApplication.setString(context, Consts.IMG_FILE, (dataObj.getString(Consts.wallpaper)));
                                            }
                                        }

                                        // SET PIN
                                        if (Utils.contains(dataObj, Consts.pin)) {
                                            if (!dataObj.getString(Consts.pin).equalsIgnoreCase("null")) {
                                                //AESCrypt.encrypt(context,dataObj.getString(Consts.pin));
                                                CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, dataObj.getString(Consts.pin));
                                                CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                                            }
                                        }

                                        // SET TIMER TYPE
                                        if (Utils.contains(dataObj, Consts.schedule_type)) {
                                            if (dataObj.get(Consts.schedule_type) != null) {
                                                if ((dataObj.getInt(Consts.schedule_type) == 0)) {
                                                    CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                                                } else {
                                                    CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                                                }
                                                CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, (dataObj.getInt(Consts.schedule_type)));

                                                // SET TIMER VALUE
                                                if (dataObj.getInt(Consts.schedule_type) == 1) {
                                                    if (Utils.contains(dataObj, Consts.schedule_value)) {
                                                        for (int i = 0; i < finalDailyTimerModels.size(); i++) {
                                                            if (finalDailyTimerModels.get(i).getDailyTimeStr().equalsIgnoreCase(dataObj.getString(Consts.schedule_value))) {
                                                                CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, i);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                } else if (dataObj.getInt(Consts.schedule_type) == 2) {
                                                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, 2);
                                                    getScheduleEventList();
                                                }
                                            }
                                        }

                                        // SET DATE/TIME SETTINGS
                                        if (Utils.contains(dataObj, Consts.date_time_setting)) {
                                            CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, dataObj.getInt(Consts.date_time_setting));
                                            if (dataObj.getInt(Consts.date_time_setting) != 0) {
                                                CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
                                            } else {
                                                CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 0);
                                            }
                                        }

                                        // SET SHORTCUTS
                                        if (Utils.contains(dataObj, Consts.shortcuts_status)) {
                                            CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, dataObj.getInt(Consts.shortcuts_status));
                                        }

                                        // SET BATTERY STATUS
                                        if (Utils.contains(dataObj, Consts.battery_status)) {
                                            if (dataObj.getInt(Consts.battery_status) == 1) {
                                                CustomDashboardApplication.setBatteryStatus(context, Consts.BATTERY_STATUS, 0);
                                               /* MainActivity.ivBattery.setVisibility(View.VISIBLE);
                                                MainActivity.txBatteryPercent.setVisibility(View.VISIBLE);*/
                                            } else {
                                                CustomDashboardApplication.setBatteryStatus(context, Consts.BATTERY_STATUS, 1);
                                              /*  MainActivity.ivBattery.setVisibility(View.GONE);
                                                MainActivity.txBatteryPercent.setVisibility(View.GONE);*/
                                            }
                                        }

                                        // SET LOCK
                                        WorkRequest workRequest = null;
                                        if (Utils.contains(dataObj, Consts.lock_application)) {
                                            if (dataObj.getInt(Consts.lock_application) == 1) {
                                                CustomDashboardApplication.setInt(context, Consts.IS_LOCKED, 1);
                                                workRequest = new OneTimeWorkRequest.Builder(LockScreenWorker.class).setInitialDelay(2, TimeUnit.SECONDS).build();
                                                WorkManager.getInstance(context).enqueue(workRequest);
                                                return;
                                            } else {
                                                CustomDashboardApplication.setInt(context, Consts.IS_LOCKED, 0);
                                                CustomDashboardApplication.setInt(context, Consts.is_screen_locked, 0);
                                                Intent intent1 = new Intent(Consts.LOCKED_MODE);
                                                intent1.putExtra(Consts.IS_LOCKED, 0);
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                                                //assert workRequest != null;
                                                if (workRequest != null && workRequest.getId() != null)
                                                    WorkManager.getInstance(context).cancelWorkById(workRequest.getId());
                                            }
                                        }

                                        // ADVANCE SETTINGS STATUS
                                        if (Consts.IS_BUSINESS_APP == 1) {
                                            if (CustomDashboardApplication.isDeviceSamsung()) {
                                                SuperLockState superLockState = SuperLockState.getInstance(context);
                                                if (superLockState.supportedActivation()) {
                                                    if (superLockState.isESDKLicenseActivated()) {
                                                        ArrayList<AdvanceSettingsModel> advanceSettingsModels = new ArrayList<>();
                                                        String advanceSettingsSer = SerializeObject.ReadSettings(context, Consts.advanceSettingsFile);
                                                        File advanceSettingFile = new File(advanceSettingsSer);
                                                        if (advanceSettingFile.exists()) {
                                                            advanceSettingFile.delete();
                                                        }

                                                        // DIJO HERE SUPER LOCK STATE SET FOR KNOX ACTIVATED DEVICES
                                                        boolean factoryResetStatus;
                                                        if (dataObj.getInt(Consts.factory_reset_status) == 1) {
                                                            factoryResetStatus = true;
                                                            superLockState.allowFactoryReset(false);
                                                        } else {
                                                            factoryResetStatus = false;
                                                            superLockState.allowFactoryReset(true);
                                                        }

                                                        boolean homeKeyStatus;
                                                        if (dataObj.getInt(Consts.home_key_status) == 1) {
                                                            homeKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_HOME, context);
                                                        } else {
                                                            homeKeyStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_HOME, context);
                                                        }

                                                        boolean volumeDownKeyStatus;
                                                        if (dataObj.getInt(Consts.volume_down_key_status) == 1) {
                                                            volumeDownKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_VOLUME_DOWN, context);
                                                        } else {
                                                            volumeDownKeyStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_VOLUME_DOWN, context);
                                                        }

                                                        boolean statusBarStatus;
                                                        if (dataObj.getInt(Consts.status_bar_status) == 1) {
                                                            statusBarStatus = true;
                                                            superLockState.allowStatusBar(false);
                                                        } else {
                                                            statusBarStatus = false;
                                                            superLockState.allowStatusBar(true);
                                                        }

                                                       /* boolean svoiceStatus;
                                                        if (dataObj.getInt(Consts.s_voice_status) == 1) {
                                                            svoiceStatus = true;
                                                            superLockState.allowSVoice(false);
                                                        } else {
                                                            svoiceStatus = false;
                                                            superLockState.allowSVoice(true);
                                                        }*/

                                                        boolean overTheAirStatus;
                                                        if (dataObj.getInt(Consts.over_the_air_status) == 1) {
                                                            overTheAirStatus = true;
                                                            superLockState.allowOTAUpgrade(true);
                                                        } else {
                                                            overTheAirStatus = false;
                                                            superLockState.allowOTAUpgrade(false);
                                                        }

                                                        boolean backKeyStatus;
                                                        if (dataObj.getInt(Consts.back_key_status) == 1) {
                                                            backKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_BACK, context);
                                                        } else {
                                                            backKeyStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_BACK, context);
                                                        }

                                                        boolean powerKeyStatus;
                                                        if (dataObj.getInt(Consts.power_key_status) == 1) {
                                                            powerKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_POWER, context);
                                                        } else {
                                                            powerKeyStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_POWER, context);
                                                        }

                                                       /* boolean cardboardVrStatus;
                                                        if (dataObj.getInt(Consts.cardboard_apps_on_gear_vr_status) == 1) {
                                                            cardboardVrStatus = true;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_HOME, context);
                                                        } else {
                                                            cardboardVrStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_HOME, context);
                                                        }*/

                                                        boolean recentsKeyStatus;
                                                        if (dataObj.getInt(Consts.recents_key_status) == 1) {
                                                            recentsKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_MENU, context);
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_APP_SWITCH, context);
                                                        } else {
                                                            recentsKeyStatus = false;
//                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_MENU, context);
//                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_APP_SWITCH, context);
                                                        }

                                                        boolean volumeUpKeyStatus;
                                                        if (dataObj.getInt(Consts.volume_up_key_status) == 1) {
                                                            volumeUpKeyStatus = true;
                                                            superLockState.allowHardwareKeys(false, KeyEvent.KEYCODE_VOLUME_UP, context);
                                                        } else {
                                                            volumeUpKeyStatus = false;
                                                            superLockState.allowHardwareKeys(true, KeyEvent.KEYCODE_VOLUME_UP, context);
                                                        }

                                                        advanceSettingsModels.add(0, new AdvanceSettingsModel(0, dataObj.getInt(Consts.factory_reset_status), context.getResources().getString(R.string.factory_reset_status), factoryResetStatus));
                                                        advanceSettingsModels.add(1, new AdvanceSettingsModel(1, dataObj.getInt(Consts.home_key_status), context.getResources().getString(R.string.disable_home_key), homeKeyStatus));
                                                        advanceSettingsModels.add(2, new AdvanceSettingsModel(2, dataObj.getInt(Consts.volume_down_key_status), context.getResources().getString(R.string.disable_volume_down_key), volumeDownKeyStatus));
                                                        advanceSettingsModels.add(3, new AdvanceSettingsModel(3, dataObj.getInt(Consts.status_bar_status), context.getResources().getString(R.string.disable_status_bar), statusBarStatus));
                                                        //advanceSettingsModels.add(4, new AdvanceSettingsModel(4, dataObj.getInt(Consts.s_voice_status), context.getResources().getString(R.string.disable_s_voice), svoiceStatus));
                                                        advanceSettingsModels.add(4, new AdvanceSettingsModel(4, dataObj.getInt(Consts.over_the_air_status), context.getResources().getString(R.string.disable_over_the_air), overTheAirStatus));
                                                        advanceSettingsModels.add(5, new AdvanceSettingsModel(5, dataObj.getInt(Consts.back_key_status), context.getResources().getString(R.string.disable_back_key), backKeyStatus));
                                                        advanceSettingsModels.add(6, new AdvanceSettingsModel(6, dataObj.getInt(Consts.power_key_status), context.getResources().getString(R.string.disable_power_key), powerKeyStatus));
                                                        //advanceSettingsModels.add(8, new AdvanceSettingsModel(8, dataObj.getInt(Consts.cardboard_apps_on_gear_vr_status), context.getResources().getString(R.string.disable_cardboard_on_gear_vr), cardboardVrStatus));
                                                        advanceSettingsModels.add(7, new AdvanceSettingsModel(7, dataObj.getInt(Consts.recents_key_status), context.getResources().getString(R.string.disable_recents_key), recentsKeyStatus));
                                                        advanceSettingsModels.add(8, new AdvanceSettingsModel(8, dataObj.getInt(Consts.volume_up_key_status), context.getResources().getString(R.string.disable_volume_up_key), volumeUpKeyStatus));

                                                        String advanceSettingsStr = SerializeObject.objectToString(advanceSettingsModels);
                                                        if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                                                            SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.advanceSettingsFile);
                                                        } else {
                                                            SerializeObject.WriteSettings(context, "", Consts.advanceSettingsFile);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Intent intent = new Intent(Consts.PROFILE_UPDATE);
                                        intent.putExtra(Consts.profile_updated, 1);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                        updateProfileSchedule();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET APPLICATION LIST
    public void getApplicationList() {

        try {

            final ArrayList<String> packageNameList = new ArrayList<>();
            ArrayList<AppModel> disableAppsModels = new ArrayList<>();
            final String currentCategory;

            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();

                String disableApps = SerializeObject.ReadSettings(context, Consts.disableAppsFile);
                if (!disableApps.equalsIgnoreCase("")) {
                    Object blockObj = SerializeObject.stringToObject(disableApps);
                    if (blockObj instanceof ArrayList) {
                        disableAppsModels = (ArrayList<AppModel>) blockObj;
                    }
                }

                if (!disableAppsModels.isEmpty()) {
                    for (int i = 0; i < disableAppsModels.size(); i++) {
                        packageNameList.add(disableAppsModels.get(i).getPackageDetails());
                    }
                }

                //Retrofit retrofit = RetrofitExtra.getInstance();
                Retrofit retrofit = RetrofitExtra.getInstanceV2();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getAllApplicationList(CustomDashboardApplication.getString(context, Consts.token));
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();

                        Log.e("APIManager", "onResponse: getApplicationList : " + call.request());

                        if (response.body() != null) {
                            if (response.code() == 200 || response.code() == 201) {
                                final String responseString = response.body().toString();
                                Log.e("APIManager", "getApplicationList : GET All Apps : resp" + response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {
                                        try {
                                            ArrayList<AppModel> tempList = new ArrayList<>();

                                            ArrayList<AppModel> lastBlockApps = new ArrayList<>();

                                            String ser2 = SerializeObject.ReadSettings(context, Consts.blocksAppsFile);
                                            if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                                                Object obj = SerializeObject.stringToObject(ser2);
                                                // Then cast it to your object and
                                                if (obj instanceof ArrayList) {
                                                    // Do something
                                                    lastBlockApps = (ArrayList<AppModel>) obj;
                                                }
                                            }

                                            if (Utils.contains(jsonObject, Consts.data)) {
                                                if (jsonObject.get(Consts.data) instanceof JSONObject) {
                                                    JSONObject dataObject = jsonObject.getJSONObject(Consts.data);
                                                    if (Utils.contains(dataObject, Consts.categories)) {
                                                        JSONArray categoryArray = dataObject.getJSONArray(Consts.categories);

                                                        //CustomDashboardApplication.setInt(context, Consts.serverAppListCount, dataArray.length());
                                                        if (categoryArray.length() > 0) {
                                                            for (int i = 0; i < categoryArray.length(); i++) {
                                                                JSONObject dataObj = categoryArray.getJSONObject(i);

                                                                JSONArray dataArray = dataObj.getJSONArray(Consts.data);

                                                                if (dataArray.length() > 0) {
                                                                    for (int k = 0; k < dataArray.length(); k++) {
                                                                        JSONObject appObj = dataArray.getJSONObject(k);
                                                                        AppModel appModel = new AppModel();
                                                                        appModel.setId(k);
                                                                        //appModel.setViewType(0);
                                                                        appModel.setAppCategory(dataObj.getString(Consts.name));

                                                               /* if (appCategoryModels.contains( dataObj.getString(Consts.name))){

                                                                } else {
                                                                    appCategoryModels.add(dataObj.getString(Consts.name));
                                                                    appModel.setViewType(0);
                                                                    tempList.add(appModel);
                                                                }*/

                                                                        appModel.setAppInfo(appObj.getString(Consts.device_application_additional_info));
                                                                        appModel.setDateInstalled(appObj.getString(Consts.device_application_date_installed));
                                                                        appModel.setTargetSdk(appObj.getString(Consts.device_application_sdk_version));
                                                                        appModel.setAppVersion(appObj.getString(Consts.device_application_version));
                                                                        if (appObj.getInt(Consts.device_application_type) == 1) {
                                                                            appModel.setSystemApp(true);
                                                                        }
                                                                        appModel.setPackageDetails(appObj.getString(Consts.device_application_package_name));

                                                                        if (appObj.getInt(Consts.added_to_dashboard) == 1) {
                                                                            appModel.setSelected(true);
                                                                        } else {
                                                                            appModel.setSelected(false);
                                                                        }

                                                                        if (Utils.contains(appObj, Consts.grid_row)) {
                                                                            if (!TextUtils.isEmpty(appObj.getString(Consts.grid_row))) {
                                                                                try {
                                                                                    appModel.setRow(Integer.parseInt(appObj.getString(Consts.grid_row)));
                                                                                } catch (
                                                                                        NumberFormatException e) {
                                                                                    e.printStackTrace();
                                                                                    appModel.setRow(1);
                                                                                }
                                                                            } else {
                                                                                appModel.setRow(1);
                                                                            }
                                                                        } else {
                                                                            appModel.setRow(1);
                                                                        }

                                                                        if (Utils.contains(appObj, Consts.grid_column)) {
                                                                            if (!TextUtils.isEmpty(appObj.getString(Consts.grid_column))) {
                                                                                try {
                                                                                    appModel.setColumn(Integer.parseInt(appObj.getString(Consts.grid_column)));
                                                                                } catch (
                                                                                        NumberFormatException e) {
                                                                                    e.printStackTrace();
                                                                                    appModel.setColumn(1);
                                                                                }
                                                                            } else {
                                                                                appModel.setColumn(1);
                                                                            }
                                                                        } else {
                                                                            appModel.setColumn(1);
                                                                        }

                                                                        if (appModel.getRow() > 0 && appModel.getColumn() > 0) {
                                                                            int position = 0;
                                                                            if (appModel.getColumn() > 3) {
                                                                                appModel.setColumn(3);
                                                                            }
                                                                            if (appModel.getRow() > 1) {
                                                                                position = ((appModel.getRow() - 1) * 3 + appModel.getColumn()) - 1;
                                                                            } else {
                                                                                position = appModel.getColumn() - 1;
                                                                            }

                                                                            appModel.setPosition(position);
                                                                        } else {
                                                                            appModel.setPosition(0);
                                                                        }

                                                                        appModel.setLocation(appModel.getRow() + "," + appModel.getColumn());

                                                                /*if (appObj.getString(Consts.is_app_locked).equalsIgnoreCase("1")) {
                                                                    appModel.setLocked(true);
                                                                } else {
                                                                    appModel.setLocked(false);
                                                                }*/

                                                                        if (Utils.contains(appObj, Consts.is_app_locked)) {
                                                                            if (appObj.getInt(Consts.is_app_locked) == 2) {
                                                                                Log.e(APIManager.class.getSimpleName(), "onResponse: " + appObj.getString(Consts.device_application_package_name) + " : " + appObj.getInt(Consts.is_app_locked));
                                                                                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                                                                                    SuperLockState superLockState = SuperLockState.getInstance(context);
                                                                                    superLockState.setDisableApplication(appObj.getString(Consts.device_application_package_name));
                                                                                }
                                                                                appModel.setLocked(true);
                                                                            } else {
                                                                                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                                                                                    SuperLockState superLockState = SuperLockState.getInstance(context);
                                                                                    if (!packageNameList.contains(appObj.getString(Consts.device_application_package_name))) {
                                                                                        superLockState.setEnableApplication(appObj.getString(Consts.device_application_package_name));
                                                                                    }
                                                                                }
                                                                                appModel.setLocked(false);

                                                                        /*boolean isAppExist = false;
                                                                        if (lastBlockApps != null && lastBlockApps.size() > 0) {
                                                                            for (int n = 0; n < lastBlockApps.size(); n++) {
                                                                                if (lastBlockApps.get(n).getPackageDetails().equalsIgnoreCase(appObj.getString(Consts.device_application_package_name))) {
                                                                                    isAppExist = true;
                                                                                    lastBlockApps.get(n).setLocked(false);
                                                                                    break;
                                                                                }
                                                                            }

                                                                            if (!isAppExist) {
                                                                                lastBlockApps.add(appModel);
                                                                            }
                                                                        } else {
                                                                            lastBlockApps.add(appModel);
                                                                        }*/
                                                                            }
                                                                        }
                                                                        tempList.add(appModel);
                                                                    }
                                                                }
                                                            }

                                                            String ser = SerializeObject.objectToString(tempList);
                                                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                                                SerializeObject.WriteSettings(context, ser, Consts.appHistoryFile);
                                                            } else {
                                                                SerializeObject.WriteSettings(context, "", Consts.appHistoryFile);
                                                            }

                                                    /*String ser1 = SerializeObject.objectToString(lastBlockApps);
                                                    if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                                        SerializeObject.WriteSettings(context, ser1, Consts.blocksAppsFile);
                                                    } else {
                                                        SerializeObject.WriteSettings(context, "", Consts.blocksAppsFile);
                                                    }*/

                                                            Intent intent = new Intent(Consts.listUpdate);
                                                            intent.putExtra(Consts.listUpdateCount, 1);
                                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    String error = response.errorBody().string();
                                    Log.e("APIManager", "onResponse: getApplicationList : Error : " + error);
                                } catch (IOException e) {

                                }
                            }
                            /*try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*/
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void savePhoneHistory(final Context context, final int syncType) {

        try {

            ArrayList<CallLogModel> callLogModels = new ArrayList<>();
            ArrayList<SmsLogModel> smsLogModels = new ArrayList<>();
            ArrayList<SaveCallHistoryModel> saveCallHistoryModels = new ArrayList<>();

            if (syncType == 0) {

                String callLogStr = SerializeObject.ReadSettings(context, Consts.callLogFile);
                if (callLogStr != null && !callLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(callLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        callLogModels = (ArrayList<CallLogModel>) obj;
                    }
                }

                if (callLogModels.size() > 0) {
                    for (int i = 0; i < callLogModels.size(); i++) {
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(callLogModels.get(i).getName());
                        saveCallHistoryModel.setPhoneNumber(callLogModels.get(i).getPhoneNumber());
                        saveCallHistoryModel.setCallDuration(callLogModels.get(i).getCallDuration());
                        saveCallHistoryModel.setCallType(callLogModels.get(i).getCallType());
                        saveCallHistoryModel.setMakeOn(callLogModels.get(i).getDateStr());
                        saveCallHistoryModel.setMessage("");
                        saveCallHistoryModel.setType("Call");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }
            }

            if (syncType == 1) {

                String smsLogStr = SerializeObject.ReadSettings(context, Consts.smsLogFile);
                if (smsLogStr != null && !smsLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(smsLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        smsLogModels = (ArrayList<SmsLogModel>) obj;
                    }
                }
                if (smsLogModels.size() > 0) {
                    for (int j = 0; j < smsLogModels.size(); j++) {
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setPhoneNumber(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setCallDuration("");
                        saveCallHistoryModel.setCallType(smsLogModels.get(j).getType());
                        saveCallHistoryModel.setMakeOn(smsLogModels.get(j).getDateStr());
                        saveCallHistoryModel.setMessage(smsLogModels.get(j).getMessage());
                        saveCallHistoryModel.setType("Message");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }
            }

/*
            if (syncType == 2){

                String callLogStr = SerializeObject.ReadSettings(context, Consts.callLogFile);
                if (callLogStr != null && !callLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(callLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        callLogModels = (ArrayList<CallLogModel>) obj;
                    }
                }

                if (callLogModels.size() > 0){
                    for (int i = 0; i < callLogModels.size(); i++){
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(callLogModels.get(i).getName());
                        saveCallHistoryModel.setPhoneNumber(callLogModels.get(i).getPhoneNumber());
                        saveCallHistoryModel.setCallDuration(callLogModels.get(i).getCallDuration());
                        saveCallHistoryModel.setCallType(callLogModels.get(i).getCallType());
                        saveCallHistoryModel.setMakeOn(callLogModels.get(i).getDateStr());
                        saveCallHistoryModel.setMessage("");
                        saveCallHistoryModel.setType("Call");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }

                String smsLogStr = SerializeObject.ReadSettings(context, Consts.smsLogFile);
                if (smsLogStr != null && !smsLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(smsLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        smsLogModels = (ArrayList<SmsLogModel>) obj;
                    }
                }
                if (smsLogModels.size() > 0){
                    for (int j = 0; j < smsLogModels.size(); j++){
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setPhoneNumber(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setCallDuration("");
                        saveCallHistoryModel.setCallType(smsLogModels.get(j).getType());
                        saveCallHistoryModel.setMakeOn(smsLogModels.get(j).getDateStr());
                        saveCallHistoryModel.setMessage(smsLogModels.get(j).getMessage());
                        saveCallHistoryModel.setType("Message");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }
            }
*/


            // CALL LOGS JSON ARRAY
            JSONArray callLogArray = new JSONArray();

            JsonArray callLogData = null;
            if (saveCallHistoryModels.size() > 0) {
                for (int i = 0; i < saveCallHistoryModels.size(); i++) {
                    JSONObject jsonObject = new JSONObject();

                    if (saveCallHistoryModels.get(i).getName() == null) {
                        jsonObject.put(Consts.name, "");
                    } else {
                        jsonObject.put(Consts.name, saveCallHistoryModels.get(i).getName());
                    }

                    if (saveCallHistoryModels.get(i).getPhoneNumber() == null) {
                        jsonObject.put(Consts.phone_number, "No Number");
                    } else {
                        jsonObject.put(Consts.phone_number, saveCallHistoryModels.get(i).getPhoneNumber());
                    }

                    if (saveCallHistoryModels.get(i).getCallType() == null) {
                        jsonObject.put(Consts.call_type, "");
                    } else {
                        jsonObject.put(Consts.call_type, saveCallHistoryModels.get(i).getCallType());
                    }

                    if (saveCallHistoryModels.get(i).getCallDuration() == null) {
                        jsonObject.put(Consts.call_duration, "");
                    } else {
                        jsonObject.put(Consts.call_duration, saveCallHistoryModels.get(i).getCallDuration());
                    }

                    if (saveCallHistoryModels.get(i).getMessage() == null) {
                        jsonObject.put(Consts.message, "");
                    } else {
                        jsonObject.put(Consts.message, saveCallHistoryModels.get(i).getMessage());
                    }

                    if (saveCallHistoryModels.get(i).getMakeOn() == null) {
                        jsonObject.put(Consts.make_on, "");
                    } else {
                        jsonObject.put(Consts.make_on, saveCallHistoryModels.get(i).getMakeOn());
                    }

                    if (saveCallHistoryModels.get(i).getType() == null) {
                        jsonObject.put(Consts.type, "");
                    } else {
                        jsonObject.put(Consts.type, saveCallHistoryModels.get(i).getType());
                    }

                    callLogArray.put(jsonObject);
                }

                Log.e("callLogsArray", callLogArray.toString());

                JsonParser jsonParser = new JsonParser();
                callLogData = (JsonArray) jsonParser.parse(callLogArray.toString());

            }

            assert callLogData != null;
            if (callLogData.size() > 0) {
                if (NetConnectivity.isOnline(context)) {
                    //  dialogLoader.showProgressDialog();

                    //Retrofit retrofit = RetrofitExtra.getInstance();
                    Retrofit retrofit = RetrofitExtra.getInstanceV2();
                    RetrofitService feedApi = retrofit.create(RetrofitService.class);

                    // REQUEST OBJECT
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                    jsonObject.add(Consts.call_history, callLogData);
                    Log.e("requestObj", jsonObject.toString());

                    // API CALL
                    Call<JsonObject> calls = feedApi.savePhoneHistory(jsonObject);
                    Log.e("url", calls.request().toString());

                    calls.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            //dialogLoader.hideProgressDialog();

                            if (response.code() == 200) {
                                final String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {

                                        try {

                                            if (syncType == 0) {
                                                String dir = context.getFilesDir().getAbsolutePath();
                                                File f0 = new File(dir, Consts.callLogFile);
                                                if (f0.exists()) {
                                                    f0.delete();
                                                }
                                            } else if (syncType == 1) {
                                                String dir = context.getFilesDir().getAbsolutePath();
                                                File f0 = new File(dir, Consts.smsLogFile);
                                                if (f0.exists()) {
                                                    f0.delete();
                                                }
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                try {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });


                } else {
                    //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void savePhoneHistoryAll(final Context context) {

        try {

            ArrayList<CallLogModel> callLogModels = new ArrayList<>();
            ArrayList<SmsLogModel> smsLogModels = new ArrayList<>();
            ArrayList<SaveCallHistoryModel> saveCallHistoryModels = new ArrayList<>();


            String callLogStr = SerializeObject.ReadSettings(context, Consts.callLogFile);
            if (callLogStr != null && !callLogStr.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(callLogStr);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    callLogModels = (ArrayList<CallLogModel>) obj;
                }
            }

            if (callLogModels.size() > 0) {
                for (int i = 0; i < callLogModels.size(); i++) {
                    SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                    saveCallHistoryModel.setName(callLogModels.get(i).getName());
                    saveCallHistoryModel.setPhoneNumber(callLogModels.get(i).getPhoneNumber());
                    saveCallHistoryModel.setCallDuration(callLogModels.get(i).getCallDuration());
                    saveCallHistoryModel.setCallType(callLogModels.get(i).getCallType());
                    saveCallHistoryModel.setMakeOn(callLogModels.get(i).getDateStr());
                    saveCallHistoryModel.setMessage("");
                    saveCallHistoryModel.setType("Call");
                    saveCallHistoryModels.add(saveCallHistoryModel);
                }
            }


            String smsLogStr = SerializeObject.ReadSettings(context, Consts.smsLogFile);
            if (smsLogStr != null && !smsLogStr.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(smsLogStr);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    smsLogModels = (ArrayList<SmsLogModel>) obj;
                }
            }
            if (smsLogModels.size() > 0) {
                for (int j = 0; j < smsLogModels.size(); j++) {
                    SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                    saveCallHistoryModel.setName(smsLogModels.get(j).getNumber());
                    saveCallHistoryModel.setPhoneNumber(smsLogModels.get(j).getNumber());
                    saveCallHistoryModel.setCallDuration("");
                    saveCallHistoryModel.setCallType(smsLogModels.get(j).getType());
                    saveCallHistoryModel.setMakeOn(smsLogModels.get(j).getDateStr());
                    saveCallHistoryModel.setMessage(smsLogModels.get(j).getMessage());
                    saveCallHistoryModel.setType("Message");
                    saveCallHistoryModels.add(saveCallHistoryModel);
                }
            }


/*
            if (syncType == 2){

                String callLogStr = SerializeObject.ReadSettings(context, Consts.callLogFile);
                if (callLogStr != null && !callLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(callLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        callLogModels = (ArrayList<CallLogModel>) obj;
                    }
                }

                if (callLogModels.size() > 0){
                    for (int i = 0; i < callLogModels.size(); i++){
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(callLogModels.get(i).getName());
                        saveCallHistoryModel.setPhoneNumber(callLogModels.get(i).getPhoneNumber());
                        saveCallHistoryModel.setCallDuration(callLogModels.get(i).getCallDuration());
                        saveCallHistoryModel.setCallType(callLogModels.get(i).getCallType());
                        saveCallHistoryModel.setMakeOn(callLogModels.get(i).getDateStr());
                        saveCallHistoryModel.setMessage("");
                        saveCallHistoryModel.setType("Call");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }

                String smsLogStr = SerializeObject.ReadSettings(context, Consts.smsLogFile);
                if (smsLogStr != null && !smsLogStr.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(smsLogStr);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        smsLogModels = (ArrayList<SmsLogModel>) obj;
                    }
                }
                if (smsLogModels.size() > 0){
                    for (int j = 0; j < smsLogModels.size(); j++){
                        SaveCallHistoryModel saveCallHistoryModel = new SaveCallHistoryModel();
                        saveCallHistoryModel.setName(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setPhoneNumber(smsLogModels.get(j).getNumber());
                        saveCallHistoryModel.setCallDuration("");
                        saveCallHistoryModel.setCallType(smsLogModels.get(j).getType());
                        saveCallHistoryModel.setMakeOn(smsLogModels.get(j).getDateStr());
                        saveCallHistoryModel.setMessage(smsLogModels.get(j).getMessage());
                        saveCallHistoryModel.setType("Message");
                        saveCallHistoryModels.add(saveCallHistoryModel);
                    }
                }
            }
*/


            // CALL LOGS JSON ARRAY
            JSONArray callLogArray = new JSONArray();

            JsonArray callLogData = null;
            if (saveCallHistoryModels.size() > 0) {
                for (int i = 0; i < saveCallHistoryModels.size(); i++) {
                    JSONObject jsonObject = new JSONObject();

                    if (saveCallHistoryModels.get(i).getName() == null) {
                        jsonObject.put(Consts.name, "");
                    } else {
                        jsonObject.put(Consts.name, saveCallHistoryModels.get(i).getName());
                    }

                    if (saveCallHistoryModels.get(i).getPhoneNumber() == null) {
                        jsonObject.put(Consts.phone_number, "No Number");
                    } else {
                        jsonObject.put(Consts.phone_number, saveCallHistoryModels.get(i).getPhoneNumber());
                    }

                    if (saveCallHistoryModels.get(i).getCallType() == null) {
                        jsonObject.put(Consts.call_type, "");
                    } else {
                        jsonObject.put(Consts.call_type, saveCallHistoryModels.get(i).getCallType());
                    }

                    if (saveCallHistoryModels.get(i).getCallDuration() == null) {
                        jsonObject.put(Consts.call_duration, "");
                    } else {
                        jsonObject.put(Consts.call_duration, saveCallHistoryModels.get(i).getCallDuration());
                    }

                    if (saveCallHistoryModels.get(i).getMessage() == null) {
                        jsonObject.put(Consts.message, "");
                    } else {
                        jsonObject.put(Consts.message, saveCallHistoryModels.get(i).getMessage());
                    }

                    if (saveCallHistoryModels.get(i).getMakeOn() == null) {
                        jsonObject.put(Consts.make_on, "");
                    } else {
                        jsonObject.put(Consts.make_on, saveCallHistoryModels.get(i).getMakeOn());
                    }

                    if (saveCallHistoryModels.get(i).getType() == null) {
                        jsonObject.put(Consts.type, "");
                    } else {
                        jsonObject.put(Consts.type, saveCallHistoryModels.get(i).getType());
                    }

                    callLogArray.put(jsonObject);
                }

                Log.e("callLogsArray", callLogArray.toString());

                JsonParser jsonParser = new JsonParser();
                callLogData = (JsonArray) jsonParser.parse(callLogArray.toString());

            }

            assert callLogData != null;
            if (callLogData.size() > 0) {
                if (NetConnectivity.isOnline(context)) {
                    //  dialogLoader.showProgressDialog();

                    //Retrofit retrofit = RetrofitExtra.getInstance();
                    Retrofit retrofit = RetrofitExtra.getInstanceV2();
                    RetrofitService feedApi = retrofit.create(RetrofitService.class);

                    // REQUEST OBJECT
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                    jsonObject.add(Consts.call_history, callLogData);
                    Log.e("requestObj", jsonObject.toString());

                    // API CALL
                    Call<JsonObject> calls = feedApi.savePhoneHistory(jsonObject);
                    Log.e("url", calls.request().toString());

                    calls.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            //dialogLoader.hideProgressDialog();

                            if (response.code() == 200) {
                                final String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {

                                        try {


                                            String dir = context.getFilesDir().getAbsolutePath();
                                            File f0 = new File(dir, Consts.callLogFile);
                                            if (f0.exists()) {
                                                f0.delete();
                                            }

                                            String dir1 = context.getFilesDir().getAbsolutePath();
                                            File f01 = new File(dir1, Consts.smsLogFile);
                                            if (f01.exists()) {
                                                f01.delete();
                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                try {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });


                } else {
                    //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // UPDATE WALL PAPER
    public void updateProfileWallpaper(String profileImageString) {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // API CALL
                Call<JsonObject> calls = feedApi.updateProfile(prepareFilePart("file", profileImageString),
                        MultipartBody.Part.createFormData(Consts.profile_id, String.valueOf(profileId)),
                        MultipartBody.Part.createFormData(Consts.token, CustomDashboardApplication.getString(context, Consts.token)));

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response != null && response.body() != null)
                            Log.e("img_resp", response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE PROFILE SCHEDULE
    public void updateProfileSchedule() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();

                ArrayList<DailyTimerModel> dailyTimerModels = new ArrayList<>();

                String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        dailyTimerModels = (ArrayList<DailyTimerModel>) obj;
                    }
                }

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.profile_id, profileId);
                jsonObject.addProperty(Consts.type, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE));
                if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 2) {
                    jsonObject.addProperty(Consts.daily_limit, "5 Min");
                } else {
                    try {
                        jsonObject.addProperty(Consts.daily_limit, dailyTimerModels.get(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER)).getDailyTimeStr());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.updateProfileSchedule(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {

                                    try {

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // FOR UPLOADING IMAGE
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        File file = new File(fileUri);
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }


    // ADD SCHEDULE EVENT
    public void addScheduleEvent(JsonObject reqObj) {
        try {
            if (NetConnectivity.isOnline(context)) {
                // dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                Log.e("requestObj", reqObj.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.addScheduleEvent(reqObj);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        //  dialogLoader.hideProgressDialog();
                        onResponseListener.onSuccess(response);
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //  dialogLoader.hideProgressDialog();
                        onResponseListener.onFailed(t);
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getAppVersion(String packageName) {
        PackageInfo pinfo = null;
        String versionName = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(packageName, 0);
            int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }


    public int getAppVersionCode(String packageName) {
        PackageInfo pinfo = null;
        int versionNumber = 0;
        try {
            pinfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionNumber = pinfo.versionCode;
            // versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionNumber;
    }

    public String calculateMinutes(String startTime, String endTime) {

        String minute = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss aa");
            Date date1 = format.parse(endTime);
            Date date2 = format.parse(startTime);
            long mills = date1.getTime() - date2.getTime();
           /* Log.e("Data1", ""+date1.getTime());
            Log.e("Data2", ""+date2.getTime());
            int hours = (int) (mills/(1000 * 60 * 60));
            float mins = (mills/(1000*60)) % 60;*/
            minute = String.valueOf(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return minute;
    }

    public String getRealPathFromURI(Uri uri) {
        int idx = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RealPathFromURI->", e.getMessage());
        }
        return cursor.getString(idx);
    }


    // GET COUNTER
    public void getProfileCounter(String token) {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getCounter(Consts.BaseUrl + Consts.SERVICE_GET_PROFILE_COUNTER + token);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 0) {
                                    try {
                                        JSONObject dataObj = jsonObject.getJSONObject(Consts.data);

                                        updateDataOnServer();

                                        if (dataObj.getInt(Consts.profile_counter) == (CustomDashboardApplication.getInt(context, Consts.device_counter))) {
                                            uploadAppsToServer();
                                            //savePhoneHistory(context, 2);
                                        } else if (dataObj.getInt(Consts.profile_counter) <= CustomDashboardApplication.getInt(context, Consts.device_counter)) {
                                            //savePhoneHistory(context, 2);
                                            uploadAppsToServer();
                                            updateProfileSettings();
                                            saveWebShortcuts();
                                            String decryptStr = null;
                                            try {
                                                //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                                decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (decryptStr != null) {
                                                updatePin(decryptStr);
                                            }

                                            if (!CustomDashboardApplication.getString(context, Consts.IMG_FILE).equalsIgnoreCase("")) {
                                                final Uri contentURI = Uri.parse(CustomDashboardApplication.getString(context, Consts.IMG_FILE));
                                                String path = getRealPathFromURI(contentURI);
                                                updateProfileWallpaper(path);
                                            }

                                            updateProfileSchedule();
                                            uploadAnalyticsDataToServer();
                                        } else if (dataObj.getInt(Consts.profile_counter) >= CustomDashboardApplication.getInt(context, Consts.device_counter)) {
                                            getProfile();
                                            getApplicationList();
                                            getWebShortCuts();
                                        }
                                        //finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET SCHEDULE EVENT LIST
    public void getScheduleEventList() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.profile_id, profileId);

                // API CALL
                Call<JsonObject> calls = feedApi.getScheduleEventList(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        JSONArray dataArray = jsonObject.getJSONArray(Consts.data);
                                        if (dataArray.length() > 0) {

                                            ArrayList<ScheduleEventModel> scheduleEventModels = new ArrayList<>();
                                            for (int i = 0; i < dataArray.length(); i++) {
                                                JSONObject dataObj = dataArray.getJSONObject(i);
                                                ScheduleEventModel scheduleEventModel = new ScheduleEventModel();
                                                scheduleEventModel.setId(dataObj.getInt(Consts.id));
                                                scheduleEventModel.setProfileId(dataObj.getString(Consts.profile_id));
                                                scheduleEventModel.setEventName(dataObj.getString(Consts.event_name));
                                                scheduleEventModel.setDays(dataObj.getInt(Consts.days));
                                                scheduleEventModel.setStartTime(dataObj.getString(Consts.start_time));
                                                scheduleEventModel.setEndTime(dataObj.getString(Consts.end_time));
                                                scheduleEventModel.setGroupId(dataObj.getString(Consts.group_id));
                                                scheduleEventModel.setDay(dataObj.getString(Consts.day));

                                                String checkedDays = dataObj.getString(Consts.day);

                                                scheduleEventModels.add(scheduleEventModel);

                                            }


                                            String ser = SerializeObject.objectToString(scheduleEventModels);
                                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                                SerializeObject.WriteSettings(context, ser, Consts.scheduleFile);
                                            }

                                        } else {

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SAVE WEB SHORTCUTS
    public void saveWebShortcuts() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                ArrayList<AddedWebShortcutModel> addedWebShortCutsModels = new ArrayList<>();
                String scheduleTimerStr = SerializeObject.ReadSettings(context, Consts.addedWebShortcutsFile);
                if (scheduleTimerStr != null && !scheduleTimerStr.equalsIgnoreCase("")) {
                    Object scheduleModelObj = SerializeObject.stringToObject(scheduleTimerStr);
                    if (scheduleModelObj instanceof ArrayList) {
                        addedWebShortCutsModels = (ArrayList<AddedWebShortcutModel>) scheduleModelObj;
                    }
                }

                // APPLICATIONS JSON ARRAY
                JSONArray webShortCutsArray = new JSONArray();

                if (addedWebShortCutsModels.size() > 0) {
                    for (int i = 0; i < addedWebShortCutsModels.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Consts.website_name, addedWebShortCutsModels.get(i).getWebSiteName());
                        jsonObject.put(Consts.website_url, addedWebShortCutsModels.get(i).getWebSiteUrl());
                        jsonObject.put(Consts.website_icon, addedWebShortCutsModels.get(i).getWebsiteIcon());
                        jsonObject.put(Consts.grid_row, addedWebShortCutsModels.get(i).getRow());
                        jsonObject.put(Consts.grid_column, addedWebShortCutsModels.get(i).getColumn());
                        webShortCutsArray.put(jsonObject);
                    }
                }

                Log.e("webShortCutsArray", webShortCutsArray.toString());

                JsonArray appData;
                JsonParser jsonParser = new JsonParser();
                appData = (JsonArray) jsonParser.parse(webShortCutsArray.toString());

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                }
                jsonObject.add(Consts.applications, appData);

                // REQUEST API CALL
                Call<JsonObject> calls = feedApi.saveWebShortcuts(jsonObject);
                Log.e("saveWebShortCutUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.code() == 200) {
                            Log.e("webShortcutResp", response.body().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET WEB SHORTCUTS
    public void getWebShortCuts() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getWebShortcuts(CustomDashboardApplication.getString(context, Consts.token));

                Log.e("serverReq", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.body() != null) {
                            Log.e("serverReq", response.body().toString());
                            try {
                                JSONObject responseObj = new JSONObject(response.body().toString());

                                Object dataObj = responseObj.get(Consts.data);
                                if (dataObj instanceof JSONArray) {
                                    JSONArray dataArray = responseObj.getJSONArray(Consts.data);
                                    ArrayList<AppModel> appModels = new ArrayList<>();
                                    if (dataArray.length() > 0) {
                                        ArrayList<AddedWebShortcutModel> addedWebShortCutsModels = new ArrayList<>();
                                        addedWebShortCutsModels.clear();
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject jsonObject = dataArray.getJSONObject(i);
                                            AddedWebShortcutModel addedWebShortcutModel = new AddedWebShortcutModel();
                                            addedWebShortcutModel.setId(jsonObject.getInt(Consts.id));
                                            addedWebShortcutModel.setWebSiteName(jsonObject.getString(Consts.website_name));
                                            addedWebShortcutModel.setWebSiteUrl(jsonObject.getString(Consts.website_url));
                                            addedWebShortcutModel.setWebsiteIcon(jsonObject.getString(Consts.website_icon));
                                            addedWebShortcutModel.setSelected(true);
                                            if (Utils.contains(jsonObject, Consts.grid_row)) {
                                                if (!TextUtils.isEmpty(jsonObject.getString(Consts.grid_row))) {
                                                    try {
                                                        addedWebShortcutModel.setRow(Integer.parseInt(jsonObject.getString(Consts.grid_row)));
                                                    } catch (NumberFormatException e) {
                                                        e.printStackTrace();
                                                        addedWebShortcutModel.setRow(1);
                                                    }
                                                } else {
                                                    addedWebShortcutModel.setRow(1);
                                                }
                                            } else {
                                                addedWebShortcutModel.setRow(1);
                                            }

                                            if (Utils.contains(jsonObject, Consts.grid_column)) {
                                                if (!TextUtils.isEmpty(jsonObject.getString(Consts.grid_column))) {
                                                    try {
                                                        addedWebShortcutModel.setColumn(Integer.parseInt(jsonObject.getString(Consts.grid_column)));
                                                    } catch (NumberFormatException e) {
                                                        e.printStackTrace();
                                                        addedWebShortcutModel.setColumn(1);
                                                    }
                                                } else {
                                                    addedWebShortcutModel.setColumn(1);
                                                }
                                            } else {
                                                addedWebShortcutModel.setColumn(1);
                                            }

                                            if (addedWebShortcutModel.getRow() > 0 && addedWebShortcutModel.getColumn() > 0) {
                                                int position = 0;
                                                if (addedWebShortcutModel.getColumn() > 3) {
                                                    addedWebShortcutModel.setColumn(3);
                                                }
                                                if (addedWebShortcutModel.getRow() > 1) {
                                                    position = ((addedWebShortcutModel.getRow() - 1) * 3 + addedWebShortcutModel.getColumn()) - 1;
                                                } else {
                                                    position = addedWebShortcutModel.getColumn() - 1;
                                                }

                                                addedWebShortcutModel.setPosition(position);
                                            } else {
                                                addedWebShortcutModel.setPosition(0);
                                            }

                                            addedWebShortcutModel.setLocation(addedWebShortcutModel.getRow() + "," + addedWebShortcutModel.getColumn());
                                            addedWebShortCutsModels.add(addedWebShortcutModel);
                                        }

                                        String dir = context.getFilesDir().getAbsolutePath();
                                        File f0 = new File(dir, Consts.addedWebShortcutsFile);
                                        if (f0.exists()) {
                                            f0.delete();
                                        }

                                        String dir1 = context.getFilesDir().getAbsolutePath();
                                        File f01 = new File(dir1, Consts.savedWebShortcutsFile);
                                        if (f01.exists()) {
                                            f01.delete();
                                        }

                                        String ser = SerializeObject.objectToString(addedWebShortCutsModels);
                                        if (ser != null && !ser.equalsIgnoreCase("")) {
                                            SerializeObject.WriteSettings(context, ser, Consts.addedWebShortcutsFile);
                                        }

                                        if (addedWebShortCutsModels.size() > 0) {
                                            for (int i = 0; i < addedWebShortCutsModels.size(); i++) {
                                                appModels.add(i, new AppModel(i, addedWebShortCutsModels.get(i).isSelected(), true, true,
                                                        addedWebShortCutsModels.get(i).getWebSiteName(), "1.0", addedWebShortCutsModels.get(i).getWebSiteUrl(), "28", "",
                                                        addedWebShortCutsModels.get(i).getRow(), addedWebShortCutsModels.get(i).getColumn(),
                                                        addedWebShortCutsModels.get(i).getPosition(), addedWebShortCutsModels.get(i).getLocation()));
                                            }

                                            String ser1 = SerializeObject.objectToString(appModels);
                                            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                                SerializeObject.WriteSettings(context, ser1, Consts.savedWebShortcutsFile);
                                            } else {
                                                SerializeObject.WriteSettings(context, "", Consts.savedWebShortcutsFile);
                                            }
                                        }

                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // LOGOUT USER
    public void logoutUser() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.logoutUser(Consts.BaseUrl + Consts.SERVICE_LOGOUT + CustomDashboardApplication.getString(context, Consts.token));
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);

                                try {
                                    AlertDialog d = null;
                                    AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                    dialogBuilder1.setMessage(jsonObject.getString(Consts.error));
                                    final AlertDialog finalD = d;
                                    dialogBuilder1.setNeutralButton("Done", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CustomDashboardApplication.setInt(context, Consts.is_logged_in, 0);
                                            CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, "0000");
                                            CustomDashboardApplication.setInt(context, Consts.IS_PWD, 0);
                                            Utils.clearLoginSession(context);
                                            finalD.dismiss();
                                        }
                                    });
                                    d = dialogBuilder1.create();
                                    // d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                    final AlertDialog finalD1 = d;
                                    d.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            Button doneBtn = finalD1.getButton(DialogInterface.BUTTON_NEUTRAL);
                                            doneBtn.setAllCaps(false);
                                        }
                                    });
                                    d.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //ShowMessage.showError(context, jsonObject.getString(Consts.error));
                                //btnLogin.setText(getResources().getString(R.string.login));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
                ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
