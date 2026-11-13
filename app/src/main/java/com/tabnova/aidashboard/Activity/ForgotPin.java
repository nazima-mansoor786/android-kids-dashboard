package com.tabnova.aidashboard.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Model.ProfileData;
import com.tabnova.aidashboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForgotPin extends BaseActivity {

    // EDIT TEXT
    private EditText edPin, edRePin;

    // BUTTON
    private Button btnUpdatePin;

    // CONTEXT
    private Context context;

    // INTEGER
    private int isVolumeEnabled;

    // CHECK BOX
    private CheckBox cbShowPwd;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // ALERT DIALOG
    private AlertDialog d;

    // API MANAGER
    private APIManager apiManager;

    @Override
    protected View getTopInsetView() {
        // Purple header (first child of the layout root; it has no id) extends behind the status bar.
        return layoutRootChild(0);
    }

    @Override
    protected boolean applyImeInsets() {
        return true;
    }

    @Override
    protected boolean useLightNavigationBarIcons() {
        // The bottom inset strip shows the (light) root background, not the purple content.
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pin);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        context = this;

        apiManager = new APIManager(context);

        dialogLoader = new DialogLoader(context);

        edPin = findViewById(R.id.ed_pin_no);
        edRePin = findViewById(R.id.ed_re_pin_no);
        btnUpdatePin = findViewById(R.id.update_pin_btn);
        cbShowPwd = findViewById(R.id.cb_show_pwd_launch);


        if (CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH) == 1) {
            cbShowPwd.setChecked(true);
        } else {
            cbShowPwd.setChecked(false);
        }


        cbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                        CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 1);
                    } else {
                        cbShowPwd.setChecked(false);
                        Toast.makeText(context, "To use this feature you have to must set PIN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 0);
                }
            }
        });


        btnUpdatePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edPin.getText().toString().trim().length() == 0){
                    edPin.setError( "Please enter new pin");
                } else if (edRePin.getText().toString().trim().length() == 0){
                    edRePin.setError( "Please re-enter new pin");
                } else if (!edRePin.getText().toString().trim().equals(edPin.getText().toString().trim())){
                    edRePin.setError( "Pin don't match");
                } else {
                    try {
                        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1){
                            //AESCrypt.encrypt(context,edPin.getText().toString().trim());
                            CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, edPin.getText().toString().trim());
                            CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                            //updatePin();
                            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                            updatePin();
                        } else {
                            //AESCrypt.encrypt(context,edPin.getText().toString().trim());
                            CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, edPin.getText().toString().trim());
                            CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1){
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
        }

        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1){
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            if (isVolumeEnabled == 1){
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

                AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
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

             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (isVolumeEnabled == 1){
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                            AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
*/
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


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back is handled by the OnBackPressedCallback registered in onCreate. Let the
            // framework track the key so it reaches the dispatcher (the catch-all below would
            // otherwise swallow it while enableOnBackInvokedCallback is false).
            return super.onKeyDown(keyCode, event);
        }

        return true;
    }

    // UPDATE PIN
    public void updatePin(){
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
                jsonObject.addProperty(Consts.pin, edPin.getText().toString().trim());
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.updatePin(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        // dialogLoader.hideProgressDialog();

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
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                                                finish();
                                                d.dismiss();
                                            }
                                        });
                                        d = dialogBuilder1.create();
                                        d.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                                                doneBtn.setAllCaps(false);
                                            }
                                        });
                                        d.show();
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
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
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
}
