package com.tabnova.aidashboard.Activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.tabnova.aidashboard.Adapter.ApplicationTimerAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApplicationAnalytics extends BaseActivity {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<ApplicationTimerModel> userModels;
    ArrayList<ApplicationTimerModel> updatedAppModels;

    // ADAPTER
    ApplicationTimerAdapter applicationTimerAdapter;

    // RECYCLER VIEW
    RecyclerView applicationAnalyticsRecView;

    // IMAGE VIEW
    ImageView clearAnalyticsBtn;
    private ImageView ivBack;

    // TEXT VIEW
    TextView emptyTx;

    // INTEGER
    private int isVolumeEnabled;

    // BUTTON
    private Button btnUploadToServer;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // APP PERMISSIONS
    private final String[] permit = new String[]{Manifest.permission.READ_PHONE_STATE};

    // API MANAGER
    APIManager apiManager;

    Integer request = 0;

    @Override
    protected View getTopInsetView() {
        // Purple Toolbar (first child of the layout root; it has no id) extends behind the status bar.
        return layoutRootChild(0);
    }

    @Override
    protected boolean useLightNavigationBarIcons() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_analytics);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        context = this;

        dialogLoader = new DialogLoader(context);
        apiManager = new APIManager(context);

        emptyTx = findViewById(R.id.tx_empty);
        clearAnalyticsBtn = findViewById(R.id.iv_delete);
        btnUploadToServer = findViewById(R.id.select_btn);
        ivBack = findViewById(R.id.iv_back);
        applicationAnalyticsRecView = findViewById(R.id.recycler_view);
        applicationAnalyticsRecView.setHasFixedSize(true);
        applicationAnalyticsRecView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        userModels = new ArrayList<>();

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

        updatedAppModels = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                CustomDashboardApplication.getSerialNumber();
            } else {
                requestpermission();
            }
        }

        if (userModels.size() > 0) {
            applicationAnalyticsRecView.setVisibility(View.VISIBLE);
            emptyTx.setVisibility(View.GONE);

            JSONObject jResult = new JSONObject();// main object
            JSONArray jArray = new JSONArray();// /ItemDetail jsonArray

            for (int i = 0; i < userModels.size(); i++) {
                JSONObject jGroup = new JSONObject();// /sub Object

                try {
                    jGroup.put(Consts.APP_ID, userModels.get(i).getId());
                    jGroup.put(Consts.TOTAL_TIME, userModels.get(i).getTime());
                    jGroup.put(Consts.APP_NAME, userModels.get(i).getAppName());
                    jGroup.put(Consts.APP_PACKAGE_NAME, userModels.get(i).getAppPackageName());
                    jGroup.put(Consts.DATE, userModels.get(i).getDate());
                    jGroup.put(Consts.START_TIME, userModels.get(i).getStartTime());
                    jGroup.put(Consts.END_TIME, userModels.get(i).getEndTime());

                    jArray.put(jGroup);

                    // /itemDetail Name is JsonArray Name
                    jResult.put("itemDetail", jArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            applicationAnalyticsRecView.setVisibility(View.GONE);
            emptyTx.setVisibility(View.VISIBLE);
        }

        applicationTimerAdapter = new ApplicationTimerAdapter(context, userModels);
        applicationAnalyticsRecView.setAdapter(applicationTimerAdapter);

        clearAnalyticsBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (userModels.size() > 0) {
                    userModels.clear();
                    if (CustomDashboardApplication.currentApplicationModels.size() > 0) {
                        CustomDashboardApplication.currentApplicationModels.clear();
                        CustomDashboardApplication.currentApplicationModels = new ArrayList<>();
                    }

                    applicationAnalyticsRecView.setVisibility(View.GONE);
                    emptyTx.setVisibility(View.VISIBLE);
                    String dir = getFilesDir().getAbsolutePath();
                    File f0 = new File(dir, Consts.appAnalyticsFile);
                    if (f0.exists()) {
                        f0.delete();
                    }
                }
            }
        });

        btnUploadToServer.setOnClickListener(v -> {
            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                uploadAnalyticsDataToServer();
            }
            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
        });

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            //getApplicationList();
            btnUploadToServer.setVisibility(View.VISIBLE);
        } else {
            btnUploadToServer.setVisibility(View.GONE);
        }

        ivBack.setOnClickListener(view -> finish());
    }

    // REQUEST PERMISSIONS
    public void requestpermission() {
        isHomeApp = 1;
        ActivityCompat.requestPermissions(this, permit, request);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
        }
        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isVolumeEnabled == 1) {
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

                AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);

              /*  AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {
               /* final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.ADJUST_MUTE);*/

            /*    AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isVolumeEnabled == 1) {
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

                AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);

               /* AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {
               /* final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.ADJUST_MUTE);*/

             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back is handled by the OnBackPressedCallback registered in onCreate. Let the
            // framework track the key so it reaches the dispatcher (the catch-all below would
            // otherwise swallow it while enableOnBackInvokedCallback is false).
            return super.onKeyDown(keyCode, event);
        }

        return true;
    }


    // UPLOAD ANALYTICS DATA TO SERVER
    private void uploadAnalyticsDataToServer() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // APPLICATIONS JSON ARRAY
                JSONArray appsArray = new JSONArray();

                userModels = new ArrayList<>();

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

                if (userModels.size() > 0) {
                    for (int i = 0; i < userModels.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Consts.package_name, userModels.get(i).getAppPackageName());
                        if (!TextUtils.isEmpty(userModels.get(i).getAppName()))
                            jsonObject.put(Consts.app_name_str, userModels.get(i).getAppName());
                        else
                            jsonObject.put(Consts.app_name_str, userModels.get(i).getAppPackageName());
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
                }

                if (appsArray == null || appsArray.length() == 0) {
                    return;
                }

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
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getSerialNumber());
                }
                jsonObject.add(Consts.applications, appData);

                // API CALL
                Call<JsonObject> calls = feedApi.uploadAppsAnalyticsData(jsonObject);

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        // dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        if (userModels.size() > 0) {
                                            userModels.clear();
                                            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                if (CustomDashboardApplication.currentApplicationModels != null &&
                                                        CustomDashboardApplication.currentApplicationModels.size() > 0) {
                                                    CustomDashboardApplication.currentApplicationModels.clear();
                                                    CustomDashboardApplication.currentApplicationModels = new ArrayList<>();
                                                }
                                            } else {
                                                if (CustomDashboardApplication.currentApplicationModels != null &&
                                                        CustomDashboardApplication.currentApplicationModels.size() > 0) {
                                                    CustomDashboardApplication.currentApplicationModels.clear();
                                                    CustomDashboardApplication.currentApplicationModels = new ArrayList<>();
                                                }
                                            }

                                            applicationAnalyticsRecView.setVisibility(View.GONE);
                                            emptyTx.setVisibility(View.VISIBLE);
                                            String dir = getFilesDir().getAbsolutePath();
                                            File f0 = new File(dir, Consts.appAnalyticsFile);
                                            if (f0.exists()) {
                                                f0.delete();
                                            }
                                        }
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
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    //if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                    //}
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


    // GET ANALYTICS APPLICATIONS LIST
    public void getApplicationList() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getAppsAnalyticsData(CustomDashboardApplication.getString(context, Consts.token));

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //  dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        JSONArray dataArray = jsonObject.getJSONArray(Consts.data);
                                        if (dataArray.length() > 0) {
                                            updatedAppModels.clear();
                                            updatedAppModels = new ArrayList<>();
                                            for (int i = 0; i < dataArray.length(); i++) {
                                                JSONObject dataObj = dataArray.getJSONObject(i);
                                                ApplicationTimerModel applicationTimerModel = new ApplicationTimerModel();
                                                applicationTimerModel.setId(dataObj.getInt(Consts.id));
                                                applicationTimerModel.setAppName(dataObj.getString(Consts.app_name_str));
                                                applicationTimerModel.setAppPackageName(dataObj.getString(Consts.package_name));

                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                                Date serverDate = null;
                                                try {
                                                    serverDate = formatter.parse(dataObj.getString(Consts.open_date));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                formatter = new SimpleDateFormat("dd-MMMM-yyyy");
                                                String finalDate = formatter.format(serverDate);
                                                applicationTimerModel.setDate(finalDate);
                                                applicationTimerModel.setTime(dataObj.getString(Consts.usage_time));
                                                applicationTimerModel.setStartTime(dataObj.getString(Consts.start_time));
                                                applicationTimerModel.setEndTime(dataObj.getString(Consts.end_time));
                                                updatedAppModels.add(applicationTimerModel);
                                            }

                                            userModels.addAll(updatedAppModels);
                                            applicationTimerAdapter = new ApplicationTimerAdapter(context, userModels);
                                            applicationAnalyticsRecView.setAdapter(applicationTimerAdapter);
                                            applicationAnalyticsRecView.setVisibility(View.VISIBLE);
                                            emptyTx.setVisibility(View.GONE);

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
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    if (BuildConfig.DEBUG) {
                                        ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        //   dialogLoader.hideProgressDialog();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isHomeApp = 0;
    }
}
