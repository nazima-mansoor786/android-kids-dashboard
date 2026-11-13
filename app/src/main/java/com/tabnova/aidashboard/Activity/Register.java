package com.tabnova.aidashboard.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.GoogleAuthManager;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Model.ProfileListModel;
import com.tabnova.aidashboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Register extends BaseActivity {

    // CONTEXT
    private Context context;

    // EDIT TEXT
    private EditText edUserName;
    private EditText edPassword;
    private EditText edRePassword;
    private EditText edDeviceName;

    // BUTTON
    private Button btnCancel;
    private Button btnRegister;
    private Button btnGoogleLogin;

    //SPINNER
    private Spinner kidsAgeSpinner;

    //IMAGE VIEW
    ImageView ivBack;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // ALERT DIALOG
    private AlertDialog d;
    private AppCompatDialog alertDialogProfileName;

    // APP PERMISSIONS
    private String permit[] = new String[]{Manifest.permission.READ_PHONE_STATE};

    //INT
    Integer request = 0;
    private int isProfile;
    private int isRegistered = 0;
    private int googleProfileId;
    private int ageGroupId = 0;

    // GOOGLE LOGIN
    private static final String TAG = Register.class.getSimpleName();
    private String emailStr;

    // STRING
    private String googleProfileName;
    private String googleAccessToken;

    private GoogleAuthManager.ConsentLauncher consentLauncher;

    String deviceToken = "";
    String ageGroup = "";

    //ADAPTER
    ArrayAdapter<ProfileListModel> kidsAgeAdapter;

    //ARRAY LIST
    private List<ProfileListModel> kidsAgeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back closes Register (previously super.onBackPressed() did nothing).
                finish();
                if (isRegistered == 0) {
                    isHomeApp = 1;
                    Intent intent = new Intent(context, Login.class);
                    startActivity(intent);
                }
            }
        });

        context = this;

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    deviceToken = task.getResult();

                    CustomDashboardApplication.setString(context, Consts.firebase_token, deviceToken);

                    Log.d("", deviceToken);
                });

        // Consent-screen launcher for AuthorizationClient (must be registered before the Activity is STARTED)
        consentLauncher = new GoogleAuthManager.ConsentLauncher(this);

        dialogLoader = new DialogLoader(context);

        // INIT VIEWS
        edUserName = findViewById(R.id.ed_user_name);
        edPassword = findViewById(R.id.ed_pwd);
        edRePassword = findViewById(R.id.ed_re_pwd);
        edDeviceName = findViewById(R.id.ed_device_name);
        kidsAgeSpinner = findViewById(R.id.spinner_kids_age);

        ivBack = findViewById(R.id.iv_back);

        btnCancel = findViewById(R.id.cancel_btn);
        btnRegister = findViewById(R.id.register_btn);
        btnGoogleLogin = findViewById(R.id.google_login_btn);

        btnGoogleLogin.setOnClickListener(v -> {
            isHomeApp = 1;
            startGoogleSignIn();
        });

        btnCancel.setOnClickListener(v -> finish());

        ivBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            if (edUserName.getText().toString().trim().length() == 0) {
                edUserName.setError(getResources().getString(R.string.name_required));
                edUserName.setFocusable(true);
                edUserName.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(edUserName.getText().toString().trim()).matches()) {
                edUserName.setError(getResources().getString(R.string.name_valid_required));
                edUserName.setFocusable(true);
                edUserName.requestFocus();
            } else if (edPassword.getText().toString().trim().length() == 0) {
                edPassword.setError(getResources().getString(R.string.pwd_required));
                edPassword.setFocusable(true);
                edPassword.requestFocus();
            } else if (edRePassword.getText().toString().trim().length() == 0) {
                edRePassword.setError(getResources().getString(R.string.re_pwd_required));
                edRePassword.setFocusable(true);
                edRePassword.requestFocus();
            } else if (!edRePassword.getText().toString().trim().equals(edPassword.getText().toString().trim())) {
                edRePassword.setError(getResources().getString(R.string.pwd_dont_match));
                edRePassword.setFocusable(true);
                edRePassword.requestFocus();
            } else if (edDeviceName.getText().toString().trim().length() == 0) {
                edDeviceName.setError(getResources().getString(R.string.device_name_required));
                edDeviceName.setFocusable(true);
                edDeviceName.requestFocus();
            } else if (ageGroupId == 0) {
                Toast.makeText(context, getString(R.string.select_kids_age), Toast.LENGTH_SHORT).show();
            } else {
                profileDeviceAuthentication();
                isRegistered = 1;
                //userAuthentication();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                CustomDashboardApplication.getInstance().getSerialNumber();
            } else {
                requestpermission();
            }
        }

        initKidsAgeSpinner();
    }

    private void initKidsAgeList() {
        kidsAgeList = new ArrayList<>();
        kidsAgeList.add(new ProfileListModel(0, "--- Select kid's age ---"));
        kidsAgeList.add(new ProfileListModel(1, "0-3"));
        kidsAgeList.add(new ProfileListModel(2, "4-7"));
        kidsAgeList.add(new ProfileListModel(3, "8-11"));
        kidsAgeList.add(new ProfileListModel(4, "12-15"));
        kidsAgeList.add(new ProfileListModel(5, "15+"));
    }

    private void initKidsAgeSpinner() {
        initKidsAgeList();
        kidsAgeAdapter = new ArrayAdapter<>(Register.this, R.layout.view_spinner_item, kidsAgeList);
        kidsAgeAdapter.setDropDownViewResource(R.layout.spinner_list);
        kidsAgeSpinner.setAdapter(kidsAgeAdapter);

        kidsAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ageGroupId = kidsAgeList.get(i).getId();
                ageGroup = kidsAgeList.get(i).getProfileName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // GOOGLE LOGIN: Credential Manager sign-in, then AuthorizationClient access token for the backend.
    private void startGoogleSignIn() {
        GoogleAuthManager.signIn(this, new GoogleAuthManager.SignInCallback() {
            @Override
            public void onSuccess(String email, String idToken) {
                if (isFinishing()) {
                    return;
                }
                isHomeApp = 1;
                GoogleAuthManager.getAccessToken(Register.this, consentLauncher, email, new GoogleAuthManager.AccessTokenCallback() {
                    @Override
                    public void onSuccess(String accessToken) {
                        if (isFinishing()) {
                            return;
                        }
                        isHomeApp = 1;
                        // Same fields the former token task set; request body unchanged.
                        emailStr = email;
                        googleAccessToken = accessToken;
                        loginWithGoogle(emailStr, "");
                    }

                    @Override
                    public void onCancelled() {
                        if (isFinishing()) {
                            return;
                        }
                        isHomeApp = 1;
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing()) {
                            return;
                        }
                        isHomeApp = 1;
                        Toast.makeText(context, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled() {
                if (isFinishing()) {
                    return;
                }
                isHomeApp = 1;
            }

            @Override
            public void onNoAccount() {
                if (isFinishing()) {
                    return;
                }
                isHomeApp = 1;
                Toast.makeText(context, "No Google account found on this device. Please add a Google account in Settings.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                if (isFinishing()) {
                    return;
                }
                isHomeApp = 1;
                Toast.makeText(context, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // REQUEST PERMISSIONS
    public void requestpermission() {
        ActivityCompat.requestPermissions(this, permit, request);
    }

  /*  // USER AUTHENTICATION
    private void userAuthentication(){
        try {
            if (NetConnectivity.isOnline(context)){
                dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.email, edUserName.getText().toString().trim());
                jsonObject.addProperty(Consts.password, edPassword.getText().toString().trim());
                jsonObject.addProperty(Consts.name, edDeviceName.getText().toString().trim());
                jsonObject.addProperty(Consts.device_type, 1); // 1 For Android
                jsonObject.addProperty(Consts.role_id, 113);
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.checkUserAuthentication(jsonObject);
                Log.e("requestUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {

                            if (response.code() == 200){
                                String responseString=response.body().toString();
                                Log.e("resp", response.body().toString());
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);

                                if (status == 1){

                                    try {
                                        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    JSONObject dataObj = jsonObject.getJSONObject(Consts.data);
                                                    profileDeviceAuthentication();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
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
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                } else {
                                    dialogLoader.hideProgressDialog();
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }

                            } else {
                                try {
                                    dialogLoader.hideProgressDialog();
                                    String resp=response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->",resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });

            } else {
                ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
*/

    // PROFILE DEVICE AUTHENTICATION
    private void profileDeviceAuthentication() {
        try {
            if (NetConnectivity.isOnline(context)) {
                dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                //jsonObject.addProperty(Consts.device_serial_number, "R52J90FPNLD");
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                }
                //jsonObject.addProperty(Consts.device_serial_number,  CustomDashboardApplication.getInstance().getSerialNumber());
                jsonObject.addProperty(Consts.profile_name, edDeviceName.getText().toString());
                jsonObject.addProperty(Consts.profile_type, 1); // 1 For Android
                jsonObject.addProperty(Consts.role_id, 113);
                jsonObject.addProperty(Consts.email, edUserName.getText().toString());
                jsonObject.addProperty(Consts.password, edPassword.getText().toString());
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));
                jsonObject.addProperty(Consts.age_group, ageGroupId);
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.profileDeviceRegister(jsonObject);
                Log.e("requestUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {

                            dialogLoader.hideProgressDialog();

                            if (response.code() == 200) {
                                // dialogLoader.hideProgressDialog();

                                String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);

                                if (status == 1) {

                                    try {
                                        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                                dialogLoader.showProgressDialog();
                                                userAuthentication();
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
                                    // if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    //}
                                }

                            } else {
                                try {
                                    // dialogLoader.hideProgressDialog();
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        //if (Consts.isDebugMode == true) {
                                        String err = registerResponse.get(Consts.responseMessage) + "";
                                        if (!TextUtils.isEmpty(err) && err.contains("Device already registered")) {
                                            Toast.makeText(context, registerResponse.get(Consts.responseMessage) + "", Toast.LENGTH_SHORT).show();
                                            showNewUserRegisterDialog();
                                        } else {
                                            ShowMessage.showError(context, err);
                                        }
                                        //}
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //  dialogLoader.hideProgressDialog();
                    }
                });

            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // USER AUTHENTICATION
    private void userAuthentication() {
        try {
            if (NetConnectivity.isOnline(context)) {

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.email, edUserName.getText().toString().trim());
                jsonObject.addProperty(Consts.password, edPassword.getText().toString().trim());
                jsonObject.addProperty(Consts.profile_name, edDeviceName.getText().toString().trim());
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));
                //jsonObject.addProperty(Consts.device_serial_number, "R52J90FPNLD");
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                }
                //jsonObject.addProperty(Consts.device_serial_number,  CustomDashboardApplication.getInstance().getSerialNumber());
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.checkUserAuthentication(jsonObject);
                Log.e("requestUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {
                            //    dialogLoader.hideProgressDialog();
                            if (response.code() == 200) {
                                final String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);

                                if (status == 1) {

                                    try {

                                        JSONObject dataObj = jsonObject.getJSONObject(Consts.data);
                                        CustomDashboardApplication.setString(context, Consts.api_auth_token, dataObj.getString(Consts.api_auth_token));

                                        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                CustomDashboardApplication.setString(context, Consts.profile_data, responseString);
                                                dialogLoader.showProgressDialog();
                                                getAuthenticate();
                                                //getProfileCounter();
                                                // finish();
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
                                    // dialogLoader.hideProgressDialog();
                                    //if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    // }
                                }

                            } else {
                                try {
                                    //  dialogLoader.hideProgressDialog();
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    if (response.errorBody() != null) {
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        //if (Consts.isDebugMode == true) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        // }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //   dialogLoader.hideProgressDialog();
                    }
                });

            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // GET AUTHENTICATE
    private void getAuthenticate() {
        try {
            if (NetConnectivity.isOnline(context)) {
                // dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                Gson gson = new Gson();
                //ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                //String apiToken = profileData.getData().getApiAuthToken();

                String serialNumber;
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    serialNumber = CustomDashboardApplication.getString(context, Consts.generated_serial_no);
                } else {
                    serialNumber = CustomDashboardApplication.getInstance().getSerialNumber();
                }

                // API CALL
                Call<JsonObject> calls = feedApi.getAuthenticate(Consts.BaseUrl + Consts.SERVICE_AUTHENTICATE + serialNumber + "/" + Consts.defaultPassword);
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
                                //int status = jsonObject.getInt(Consts.status);
                                //   if (status == 1){
                                try {
                                    dialogLoader.showProgressDialog();
                                    getProfileCounter(jsonObject.getString(Consts.token));
                                    CustomDashboardApplication.setString(context, Consts.token, jsonObject.getString(Consts.token));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                // } else {
                                //   ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                //   }
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
                                    // if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    // }

                                   /* if (registerResponse.get(Consts.responseMessage).equals("Please enter profile name for this device.")){
                                        txDeviceName.setVisibility(View.VISIBLE);
                                        llDeviceName.setVisibility(View.VISIBLE);
                                    } else {
                                        txDeviceName.setVisibility(View.GONE);
                                        llDeviceName.setVisibility(View.GONE);
                                    }*/
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        //     dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET COUNTER
    private void getProfileCounter(String token) {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getCounter(Consts.BaseUrl + Consts.SERVICE_GET_PROFILE_COUNTER + token);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 0) {
                                    try {
                                        JSONObject dataObj = jsonObject.getJSONObject(Consts.data);
                                        CustomDashboardApplication.setInt(context, Consts.profile_counter, dataObj.getInt(Consts.profile_counter));
                                        if (dataObj.getInt(Consts.profile_counter) == 0) {
                                            CustomDashboardApplication.setInt(context, Consts.device_counter, 1);
                                        } else {
                                            CustomDashboardApplication.setInt(context, Consts.device_counter, dataObj.getInt(Consts.profile_counter));
                                        }

                                        //CustomDashboardApplication.getCallLog(context);
                                        //CustomDashboardApplication.getAllSms(context);

                                        //apiManager.savePhoneHistory(context);
                                        CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);

                                        Log.e(TAG, "run: @762");
                                        /*Intent intent = new Intent(context, MainActivity.class);
                                        startActivity(intent);
                                        finish();*/

                                        if (CustomDashboardApplication.isDeviceSamsung() &&
                                                !CustomDashboardApplication.isDeviceSamsungActivated()) {
                                            Intent intent = new Intent(context, ActivateAdminActivity.class);
                                            startActivity(intent);
                                        } else if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                                            Intent intent = new Intent(context, AppPermissionsActivity.class);
                                            startActivity(intent);
                                        } else {
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.app_permission_done));
                                            CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

                                            CustomDashboardApplication.redirectOnActivity(context, false);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    // }
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
                                    //if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                    // }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // LOGIN WITH GOOGLE API
    private void loginWithGoogle(String email, String profileName) {
        try {
            if (NetConnectivity.isOnline(context)) {
                dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                final JsonObject jsonObject = new JsonObject();
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                }
                //jsonObject.addProperty(Consts.device_serial_number,  CustomDashboardApplication.getInstance().getSerialNumber());
                jsonObject.addProperty(Consts.profile_type, 1);
                jsonObject.addProperty(Consts.role_id, 113);
                jsonObject.addProperty(Consts.email, email);
                if (isProfile == 1) {
                    if (profileName != null)
                        jsonObject.addProperty(Consts.profile_name, profileName);
                    else
                        jsonObject.addProperty(Consts.profile_id, googleProfileId);
                }
                jsonObject.addProperty(Consts.google_access_token, googleAccessToken);
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));

                // API CALL
                Call<JsonObject> calls = feedApi.loginWithGoogle(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            CustomDashboardApplication.setString(context, Consts.data, responseString);
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);

                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {

                                        try {
                                            final JSONObject dataObj = jsonObject.getJSONObject(Consts.data);
                                           /* AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                            dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                            dialogBuilder1.setNeutralButton("Done", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {*/
                                            if (isProfile == 1) {
                                                if (Utils.contains(dataObj, Consts.api_auth_token)) {
                                                    if (dataObj.isNull(Consts.api_auth_token)) {
                                                        try {
                                                            CustomDashboardApplication.setString(context, Consts.api_auth_token, dataObj.getString(Consts.api_auth_token));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                isRegistered = 1;
                                                getAuthenticate();
                                                CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);
                                                //finish();
                                            } else {
                                                if (Utils.contains(dataObj, Consts.api_auth_token)) {
                                                    if (dataObj.isNull(Consts.api_auth_token)) {
                                                        try {
                                                            CustomDashboardApplication.setString(context, Consts.api_auth_token, dataObj.getString(Consts.api_auth_token));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                getAuthenticate();
                                                CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);
                                                //finish();
                                            }
                                               /*     d.dismiss();
                                                }
                                            });
                                            d = dialogBuilder1.create();
                                            // d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                            d.setOnShowListener(new DialogInterface.OnShowListener() {
                                                @Override
                                                public void onShow(DialogInterface dialog) {
                                                    Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                                                    doneBtn.setAllCaps(false);
                                                }
                                            });
                                            d.show();*/
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //if (Consts.isDebugMode == true) {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    // }
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

                                    final JsonElement element = registerResponse.get(Consts.data);
                                    if (!(element instanceof JsonNull)) {
                                        final JsonObject propertyToBeCopied = (JsonObject) element;

                                        final JsonObject dataObj = registerResponse.getAsJsonObject(Consts.data);

                                        if (dataObj.get(Consts.ask_profile_name).getAsInt() == 1) {
                                            isProfile = 1;

                                            JsonElement userProfiles = propertyToBeCopied.get(Consts.user_profiles);
                                            showProfilePopUp(userProfiles);
                                        }


                                       /* final AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                        dialogBuilder1.setMessage(registerResponse.get(Consts.responseMessage).toString());
                                        dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // if (Utils.contains(dataObj, Consts.ask_profile_name)){

                                                // }

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
                                        d.show();*/
                                    } else {
                                        //if (Consts.isDebugMode == true) {
                                        ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        //  }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        dialogLoader.hideProgressDialog();
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


    // SHOW NOTE DIALOG
    // SHOW NOTE DIALOG
    void showProfilePopUp(JsonElement userProfiles) {
        alertDialogProfileName = new AppCompatDialog(context);
        alertDialogProfileName.setContentView(R.layout.profile_name_layout);

        final EditText edProfileName = alertDialogProfileName.findViewById(R.id.ed_profile_name);
        TextView txOr = alertDialogProfileName.findViewById(R.id.tx_or);
        TextView txProfileList = alertDialogProfileName.findViewById(R.id.tx_device_list);
        CardView cvProfileList = alertDialogProfileName.findViewById(R.id.cv_list);
        final Spinner spinnerProfile = alertDialogProfileName.findViewById(R.id.spinner_list);
        Button btnDone = alertDialogProfileName.findViewById(R.id.done_btn);
        ArrayAdapter<ProfileListModel> profileListAdapter;
        final String[] profileName = {""};
        final int[] profileId = {0};


        final ArrayList<ProfileListModel> profileListModelList = new ArrayList<>();
        if (!(userProfiles instanceof JsonNull)) {

            profileListModelList.add(0, new ProfileListModel(0, "--- Select Kid ---", ""));

            if (userProfiles.getAsJsonArray().size() > 0) {
                cvProfileList.setVisibility(View.VISIBLE);
                txProfileList.setVisibility(View.VISIBLE);
                txOr.setVisibility(View.VISIBLE);
                for (int i = 0; i < userProfiles.getAsJsonArray().size(); i++) {
                    JsonObject object = (JsonObject) userProfiles.getAsJsonArray().get(i);
                    ProfileListModel profileListModel = new ProfileListModel();
                    profileListModel.setId(object.get(Consts.id).getAsInt());
                    profileListModel.setProfileId(object.get(Consts.profile_id).getAsString());
                    profileListModel.setProfileName(object.get(Consts.profile_name).getAsString());

                    profileListModelList.add(profileListModel);
                }

                profileListAdapter = new ArrayAdapter<>(Register.this, R.layout.view_spinner_item, profileListModelList);
                profileListAdapter.setDropDownViewResource(R.layout.spinner_list);
                spinnerProfile.setAdapter(profileListAdapter);
            } else {
                cvProfileList.setVisibility(View.GONE);
                txProfileList.setVisibility(View.GONE);
                txOr.setVisibility(View.GONE);
            }
        }


        spinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    googleProfileId = profileListModelList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnDone.setOnClickListener(v -> {
            if (edProfileName.getText().toString().length() > 0) {
                googleProfileName = edProfileName.getText().toString();
            }
            loginWithGoogle(emailStr, googleProfileName);
            alertDialogProfileName.dismiss();
        });

        alertDialogProfileName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogProfileName.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogProfileName.getWindow().setGravity(Gravity.CENTER);
        alertDialogProfileName.show();
    }

    private void showNewUserRegisterDialog() {
        try {
            final AppCompatDialog dialog = new AppCompatDialog(context);
            dialog.setContentView(R.layout.item_register_new_device);

            TextView tvAboutUs = dialog.findViewById(R.id.tv_message);
            Button btnYes = dialog.findViewById(R.id.btn_yes);
            Button btnSkipLogin = dialog.findViewById(R.id.btn_skip_login);

            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Utils.getRandomString(context);
                    profileDeviceAuthentication();
                }
            });

            btnSkipLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                            !CustomDashboardApplication.isDeviceSamsungActivated()) {
                        Intent intent = new Intent(context, ActivateAdminActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                        Intent intent = new Intent(context, AppPermissionsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);
                        Log.e(LoginOrSignUpActivity.class.getSimpleName(), "onClick: Condition 3 @104");
                        CustomDashboardApplication.redirectOnActivity(context, true);
                    }
                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().getDecorView().setLeft(20);
            dialog.getWindow().getDecorView().setRight(20);
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
