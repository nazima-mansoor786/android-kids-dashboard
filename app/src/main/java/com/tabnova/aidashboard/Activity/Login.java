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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import androidx.credentials.playservices.auth.GoogleIdTokenCredential;
//import androidx.credentials.playservices.auth.GetGoogleIdTokenOption;
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
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Model.ProfileListModel;
import com.tabnova.aidashboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Login extends BaseActivity {

    // CONTEXT
    private Context context;

    // EDIT TEXT
    private EditText edUserName;
    private EditText edPassword;
    private EditText edDeviceName;

    // LINEAR LAYOUT
    private LinearLayout llDeviceName;
    private LinearLayout llLogin;

    // TEXT VIEW
    private TextView txRegister;
    private TextView txDeviceName;
    private TextView txDeviceList;
    private TextView txOr;

    // STRING
    private String googleAccessToken;
    private String profileName;
    private String googleProfileName;


    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogForgotPwd;

    // ALERT DIALOG
    private AlertDialog d;
    private AppCompatDialog alertDialogProfileName;

    // INTEGER
    private int profileId;
    private int googleProfileId;
    private int ageGroupId = 0;

    // SPINNER
    private Spinner profileListSpinner;

    // ADAPTER
    private ArrayAdapter<ProfileListModel> profileListAdapter;

    // ARRAY LIST
    private ArrayList<ProfileListModel> profileListModels;
    ArrayList<ProfileListModel> kidsAgeList = new ArrayList<>();

    // APP PERMISSIONS
    private String[] permit = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Integer request = 0;

    // GOOGLE LOGIN
    private static final String TAG = Login.class.getSimpleName();
    private String emailStr;

    // API MANAGER
    //APIManager apiManager;
    private int isProfile;

    // CARD VIEW
    private CardView cvProfileList;

    //    GoogleSignInClient googleSignInClient;

    // Initialize Credential Manager
    //CredentialManager credentialManager = CredentialManager.create(context);

    private GoogleAuthManager.ConsentLauncher consentLauncher;

    //STRING
    String deviceToken = "";
    String ageGroup = "";

    //BOOLEAN
    private boolean isGoogleLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        /*if (Utils.isTiramisu()) {
            permit = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permit = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }*/

        permit = new String[]{Manifest.permission.READ_PHONE_STATE};

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    deviceToken = task.getResult();
                    CustomDashboardApplication.setString(context, Consts.firebase_token, deviceToken);
                    Log.e("Login", "Device Token : " + deviceToken);
                });

        //googleSignInClient = GoogleSignIn.getClient(this, gso);
        // Consent-screen launcher for AuthorizationClient (must be registered before the Activity is STARTED)
        consentLauncher = new GoogleAuthManager.ConsentLauncher(this);

// Create a request for credentials
        /*GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(new GetPasswordOption())
                .addCredentialOption(new GetPublicKeyCredentialOption())
                .build();*/

        /*final GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleSignInAccount != null) {
            googleSignInClient.signOut();
        }*/

        //apiManager = new APIManager(context);

        // INIT VIEWS
        dialogLoader = new DialogLoader(context);

        LinearLayout llProfileList = findViewById(R.id.ll_device_list);
        profileListSpinner = findViewById(R.id.spinner_list);
        Spinner loginKidsAgeSpinner = findViewById(R.id.spinner_kids_age_login);
        txOr = findViewById(R.id.tx_or);
        cvProfileList = findViewById(R.id.cv_list);
        txDeviceList = findViewById(R.id.tx_device_list);
        profileListModels = new ArrayList<>();

        edUserName = findViewById(R.id.ed_user_name);
        edPassword = findViewById(R.id.ed_password);
        edDeviceName = findViewById(R.id.ed_device_name);

        llDeviceName = findViewById(R.id.ll_device_name);
        llLogin = findViewById(R.id.ll_login);

        // BUTTON
        Button btnCancel = findViewById(R.id.cancle_btn);
        Button btnLogin = findViewById(R.id.login_btn);
        Button btnGoogleLogin = findViewById(R.id.google_login_btn);
        Button btnSkip = findViewById(R.id.skip_btn);
        Button btnEmailLogin = findViewById(R.id.btn_login_with_email);

        //IMAGE VIEW
        ImageView ivBack = findViewById(R.id.iv_back);

        txRegister = findViewById(R.id.tx_register_now);
        txDeviceName = findViewById(R.id.tx_device_name);
        TextView txForgotPassword = findViewById(R.id.tx_forgot_password);

        initKidsAgeList();

        btnEmailLogin.setOnClickListener(v -> llLogin.setVisibility(View.VISIBLE));

        txForgotPassword.setOnClickListener(v -> showForgotPassword());

        btnGoogleLogin.setOnClickListener(v -> {
            isHomeApp = 1;
            /*if (googleSignInAccount != null) {
                googleSignInClient.signOut();
            }*/
            //signIn();
            startGoogleSignIn();
        });

        btnSkip.setOnClickListener(v -> {
            CustomDashboardApplication.setInt(context, Consts.force_login, 1);
            finish();
        });

        // CLICK EVENTS
        btnCancel.setOnClickListener(v -> finish());

        ivBack.setOnClickListener(v -> {
            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) != 1) {
                CustomDashboardApplication.setString(context, Consts.api_auth_token, "");
                CustomDashboardApplication.setString(context, Consts.token, "");
            }
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            isGoogleLogin = false;
            if (edUserName.getText().toString().trim().length() == 0) {
                edUserName.setError(getResources().getString(R.string.name_required));
                edUserName.setFocusable(true);
                edUserName.requestFocus();
            } else if (edUserName.getText().toString().contains("@")) {
                if (!Patterns.EMAIL_ADDRESS.matcher(edUserName.getText().toString().trim()).matches()) {
                    edUserName.setError(getResources().getString(R.string.name_required));
                    edUserName.setFocusable(true);
                    edUserName.requestFocus();
                } else {
                    if (edPassword.getText().toString().trim().length() == 0) {
                        edPassword.setError(getResources().getString(R.string.pwd_required));
                        edPassword.setFocusable(true);
                        edPassword.requestFocus();
                    } /*else if (ageGroupId == 0) {
                        Toast.makeText(context, getString(R.string.select_kids_age), Toast.LENGTH_SHORT).show();
                    }*/ else if (edDeviceName.getText().toString().trim().length() == 0) {
                      /*  if (isDeviceExists == 0) {
                            edDeviceName.setVisibility(View.VISIBLE);
                            edDeviceName.setError(getResources().getString(R.string.device_name_required));
                            edDeviceName.setFocusable(true);
                            edDeviceName.requestFocus();
                        } else {*/
                        userAuthentication();
                        //}
                    } else {
                        userAuthentication();
                    }
                }
            } else {
                if (edPassword.getText().toString().trim().length() == 0) {
                    edPassword.setError(getResources().getString(R.string.pwd_required));
                    edPassword.setFocusable(true);
                    edPassword.requestFocus();
                }
                /*else if (ageGroupId == 0) {
                    Toast.makeText(context, getString(R.string.select_kids_age), Toast.LENGTH_SHORT).show();
                }*/
                else if (edDeviceName.getText().toString().trim().length() == 0) {
                    /*if (isDeviceExists == 0) {
                        edDeviceName.setVisibility(View.VISIBLE);
                        edDeviceName.setError(getResources().getString(R.string.device_name_required));
                        edDeviceName.setFocusable(true);
                        edDeviceName.requestFocus();
                    } else {*/
                    userAuthentication();
                    // }
                } else {
                    userAuthentication();
                }
            }
        });

        profileListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    profileName = profileListSpinner.getSelectedItem().toString();
                    profileId = profileListModels.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txRegister.setOnClickListener(v -> {
            isHomeApp = 1;
            Intent intent = new Intent(context, Register.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loginKidsAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    ageGroupId = kidsAgeList.get(position).getId();
                    ageGroup = kidsAgeList.get(position).getProfileName();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<ProfileListModel> kidsAgeAdapter = new ArrayAdapter<>(Login.this, R.layout.view_spinner_item, kidsAgeList);
        kidsAgeAdapter.setDropDownViewResource(R.layout.spinner_list);
        loginKidsAgeSpinner.setAdapter(kidsAgeAdapter);
    }

    /*private void signInWithGoogle() {
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(GoogleIdTokenCredential.getCredentialOptionBuilder(getString(R.string.default_web_client_id)).build())
                .build();

        signInLauncher.launch(request);
    }

    private final ActivityResultLauncher<GetCredentialRequest> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.GetCredential(), result -> {
                handleSignInResult(result);
            });*/

    // Register for activity result
    /*ActivityResultLauncher<GetCredentialRequest> getCredentialLauncher = registerForActivityResult(
            new ActivityResultContracts.GetCredential(),
            result -> {
                if (result instanceof GetCredentialResponse) {
                    // Handle successful credential retrieval
                } else if (result instanceof GetCredentialException) {
                    // Handle credential retrieval error
                }
            }
    );*/

    // GOOGLE LOGIN: Credential Manager sign-in, then AuthorizationClient access token for the backend.
    private void startGoogleSignIn() {
        GoogleAuthManager.signIn(this, new GoogleAuthManager.SignInCallback() {
            @Override
            public void onSuccess(@NonNull String email, @NonNull String idToken) {
                if (isFinishing()) {
                    return;
                }
                dialogLoader.showProgressDialog();
                GoogleAuthManager.getAccessToken(Login.this, consentLauncher, email, new GoogleAuthManager.AccessTokenCallback() {
                    @Override
                    public void onSuccess(@NonNull String accessToken) {
                        if (isFinishing()) {
                            return;
                        }
                        // Same fields the former RetrieveTokenTask path set; request body unchanged.
                        emailStr = email;
                        isGoogleLogin = true;
                        googleAccessToken = accessToken;
                        loginWithGoogle(emailStr, "");
                    }

                    @Override
                    public void onCancelled() {
                        if (isFinishing()) {
                            return;
                        }
                        dialogLoader.hideProgressDialog();
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        if (isFinishing()) {
                            return;
                        }
                        dialogLoader.hideProgressDialog();
                        Toast.makeText(context, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled() {
                // Nothing to do.
            }

            @Override
            public void onNoAccount() {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(context, "No Google account found on this device. Please add a Google account in Settings.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull String message) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(context, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
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

    // GOOGLE LOGIN INTENT
    private void signIn() {
//        Intent signInIntent = googleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isHomeApp = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                isUserExist();
            } else {
                requestpermission();
            }
        } else {
            isUserExist();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isUserExist();
        }
    }

    // REQUEST PERMISSIONS
    public void requestpermission() {
        isHomeApp = 1;
        try {
            ActivityCompat.requestPermissions(this, permit, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // CHECK USER EXISTS OR NOT API CALL
    private void isUserExist() {
        try {
            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                if (Utils.isMarshMallow()) {
                    if (Utils.isTiramisu() && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                        if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                                !CustomDashboardApplication.getString(context, Consts.generated_serial_no).equalsIgnoreCase("null")) {
                            Utils.writeToFile(context, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                        } else {
                            Utils.writeToFile(context, CustomDashboardApplication.getSerialNumber());
                        }
                    } else if (Utils.isLessTiramisu() && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    jsonObject.addProperty(Consts.serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
                } else {
                    jsonObject.addProperty(Consts.serial_number, CustomDashboardApplication.getSerialNumber());
                }

                //jsonObject.addProperty(Consts.serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.checkDeviceExists(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        dialogLoader.hideProgressDialog();

                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
                                String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {
                                        try {
                                            if (!jsonObject.getString(Consts.responseMessage).equalsIgnoreCase("Device exist.")) {
                                                txRegister.setVisibility(View.GONE);
                                           /* AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                            dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                            dialogBuilder1.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                   *//*
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
                                                txRegister.setVisibility(View.GONE);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // USER AUTHENTICATION
    private void userAuthentication() {
        try {
            if (NetConnectivity.isOnline(context)) {
                dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.email, edUserName.getText().toString().trim());
                jsonObject.addProperty(Consts.password, edPassword.getText().toString().trim());
                //jsonObject.addProperty(Consts.profile_name, "pinku");
                if (edDeviceName.getText().toString().length() > 0) {
                    jsonObject.addProperty(Consts.profile_name, edDeviceName.getText().toString().trim());
                } else {
                    jsonObject.addProperty(Consts.profile_id, profileId);
                    //jsonObject.addProperty(Consts.profile_name, profileName);
                }
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));
                //jsonObject.addProperty(Consts.device_serial_number, "R52J90FPNLD");

                if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.generated_serial_no)) &&
                        CustomDashboardApplication.getString(context, Consts.generated_serial_no).equalsIgnoreCase("null")) {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getString(context, Consts.generated_serial_no));
//                    serialNo = CustomDashboardApplication.getString(context, Consts.generated_serial_no);
                } else {
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getSerialNumber());
//                    serialNo = CustomDashboardApplication.getInstance().getSerialNumber();
                }

                jsonObject.addProperty(Consts.age_group, ageGroupId);

                //jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.checkUserAuthentication(jsonObject);
                Log.e("requestUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            if ((response.code() == 200 || response.code() == 201) && response.body() != null) {
                                final String responseString = response.body().toString();
                                Log.e("resp", response.body().toString());
                                CustomDashboardApplication.setString(context, Consts.profile_data, responseString);
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);

                                if (status == 1) {
                                    try {
                                        JSONObject dataObj = jsonObject.getJSONObject(Consts.data);
                                        CustomDashboardApplication.setString(context, Consts.api_auth_token, dataObj.getString(Consts.api_auth_token));

                                        getAuthenticate();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    dialogLoader.hideProgressDialog();
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } else {
                                try {
                                    dialogLoader.hideProgressDialog();
                                    if (response.errorBody() != null) {
                                        String resp = response.errorBody().string();
                                        Log.d("tag", "onResponse - Status : " + response.code());
                                        Log.e("regErr->", resp);
                                        Gson gson = new Gson();
                                        TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);
                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");
                                        JsonElement element = registerResponse.get(Consts.data);
                                        if (!(element instanceof JsonNull)) {
                                            JsonObject propertyToBeCopied = (JsonObject) element;

                                            JsonElement profileName = propertyToBeCopied.get(Consts.ask_profile_name);

                                            if (!(profileName instanceof JsonNull)) {
                                                if (propertyToBeCopied.get(Consts.ask_profile_name).getAsInt() == 1) {
                                                    txDeviceName.setVisibility(View.VISIBLE);
                                                    llDeviceName.setVisibility(View.VISIBLE);

                                                    JsonElement userProfiles = propertyToBeCopied.get(Consts.user_profiles);

                                                    if (!(userProfiles instanceof JsonNull)) {
                                                        isGoogleLogin = false;
                                                        showProfilePopUp(userProfiles);
                                                        profileListModels = new ArrayList<>();
                                                        profileListModels.add(0, new ProfileListModel(0, "--- Select Kid ---", ""));

                                                        if (userProfiles.getAsJsonArray().size() > 0) {
                                                            cvProfileList.setVisibility(View.VISIBLE);
                                                            txDeviceList.setVisibility(View.VISIBLE);
                                                            txOr.setVisibility(View.VISIBLE);
                                                            for (int i = 0; i < userProfiles.getAsJsonArray().size(); i++) {
                                                                JsonObject object = (JsonObject) userProfiles.getAsJsonArray().get(i);
                                                                ProfileListModel profileListModel = new ProfileListModel();
                                                                profileListModel.setId(object.get(Consts.id).getAsInt());
                                                                profileListModel.setProfileId(object.get(Consts.profile_id).getAsString());
                                                                profileListModel.setProfileName(object.get(Consts.profile_name).getAsString());

                                                                profileListModels.add(profileListModel);
                                                            }

                                                            profileListAdapter = new ArrayAdapter<>(Login.this, R.layout.view_spinner_item, profileListModels);
                                                            profileListAdapter.setDropDownViewResource(R.layout.spinner_list);
                                                            profileListSpinner.setAdapter(profileListAdapter);
                                                        } else {
                                                            cvProfileList.setVisibility(View.GONE);
                                                            txDeviceList.setVisibility(View.GONE);
                                                            txOr.setVisibility(View.GONE);
                                                        }
                                                    }
                                                } else {
                                                    txDeviceName.setVisibility(View.GONE);
                                                    llDeviceName.setVisibility(View.GONE);
                                                    cvProfileList.setVisibility(View.GONE);
                                                    txDeviceList.setVisibility(View.GONE);
                                                    txOr.setVisibility(View.GONE);
                                                    ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                                }
                                            } else {
                                                txDeviceName.setVisibility(View.GONE);
                                                llDeviceName.setVisibility(View.GONE);
                                                cvProfileList.setVisibility(View.GONE);
                                                txDeviceList.setVisibility(View.GONE);
                                                txOr.setVisibility(View.GONE);
                                                ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                            }
                                        } else {
                                            String err = String.valueOf(registerResponse.get(Consts.responseMessage));
                                            if (!TextUtils.isEmpty(err) && (err.contains("This device is not belongs to this account") ||
                                                    err.contains("Device already registered") || err.contains("We can`t find an account with this credentials."))) {
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                                showNewUserRegisterDialog();
                                            } else {
                                                ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                            }
                                        }
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
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });
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
                dialogLoader.showProgressDialog();
                String serialNumber;
                if (!CustomDashboardApplication.getString(context, Consts.generated_serial_no).equals("")
                        && CustomDashboardApplication.getString(context, Consts.generated_serial_no) != null) {
                    serialNumber = CustomDashboardApplication.getString(context, Consts.generated_serial_no);
                } else {
                    serialNumber = CustomDashboardApplication.getSerialNumber();
                }
                Call<JsonObject> calls = feedApi.getAuthenticate(Consts.BaseUrl + Consts.SERVICE_AUTHENTICATE + serialNumber + "/" + Consts.defaultPassword);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
                                String responseString = response.body().toString();
                                Log.e("respCounter", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    //int status = jsonObject.getInt(Consts.status);
                                    //   if (status == 1){
                                    try {
                                        getProfileCounter(jsonObject.getString(Consts.token));
                                        CustomDashboardApplication.setString(context, Consts.token, jsonObject.getString(Consts.token));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            dialogLoader.hideProgressDialog();
                            try {
                                if (response.errorBody() != null) {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET COUNTER
    private void getProfileCounter(String token) {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);
                dialogLoader.showProgressDialog();
                // API CALL
                Call<JsonObject> calls = feedApi.getCounter(Consts.BaseUrl + Consts.SERVICE_GET_PROFILE_COUNTER + token);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        dialogLoader.hideProgressDialog();

                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
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
                                                CustomDashboardApplication.setInt(context, Consts.device_counter, dataObj.getInt(Consts.profile_counter) - 1);
                                            }

                                            CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);
                                            CustomDashboardApplication.setInt(context, Consts.isSettingPopUp, 2);
                                            CustomDashboardApplication.setInt(context, Consts.force_login, 2);

                                            try {
                                                //finish();

                                                if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                                                        !CustomDashboardApplication.isDeviceSamsungActivated()) {
                                                    Intent intent = new Intent(context, ActivateAdminActivity.class);
                                                    startActivity(intent);
                                                } else if (!CustomDashboardApplication.isDeviceSamsungActivated() &&
                                                        !CustomDashboardApplication.checkAllPermissions(context)) {
                                                    Intent intent = new Intent(context, AppPermissionsActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.app_permission_done));
                                                    CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

                                                    isHomeApp = 1;
                                                    CustomDashboardApplication.redirectOnActivity(context, true);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    } else {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        dialogLoader.hideProgressDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW FORGOT PASSWORD DIALOG
    void showForgotPassword() {
        try {
            alertDialogForgotPwd = new AppCompatDialog(context);
            alertDialogForgotPwd.setContentView(R.layout.forgot_password_popup);

            final EditText edEmail = alertDialogForgotPwd.findViewById(R.id.ed_email);
            final Button btnDone = alertDialogForgotPwd.findViewById(R.id.done_btn);

            if (btnDone != null) {
                btnDone.setOnClickListener(v -> {
                    if (edEmail.getText().toString().trim().length() == 0) {
                        edEmail.setError(getResources().getString(R.string.email_valid));
                        edEmail.setFocusable(true);
                        edEmail.requestFocus();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.getText().toString().trim()).matches()) {
                        edEmail.setError(getResources().getString(R.string.email_valid_required));
                        edEmail.setFocusable(true);
                        edEmail.requestFocus();
                    } else {
                        forgotPassword(edEmail);
                        alertDialogForgotPwd.dismiss();
                    }

                });
            }
            if (alertDialogForgotPwd.getWindow() != null) {
                alertDialogForgotPwd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialogForgotPwd.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                alertDialogForgotPwd.getWindow().setGravity(Gravity.CENTER);
                alertDialogForgotPwd.getWindow().getDecorView().setLeft(50);
                alertDialogForgotPwd.getWindow().getDecorView().setRight(50);
            }
            alertDialogForgotPwd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FORGOT PASSWORD API
    private void forgotPassword(EditText edEmail) {
        try {
            if (NetConnectivity.isOnline(context)) {
                dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.email, edEmail.getText().toString().trim());

                // API CALL
                Call<JsonObject> calls = feedApi.forgotPassword(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
                                String responseString = response.body().toString();
                                Log.e("respCounter", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {
                                        Toast.makeText(Login.this, "Your password reset link sent to your email.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        dialogLoader.hideProgressDialog();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        dialogLoader.hideProgressDialog();
                        t.printStackTrace();
                    }
                });
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
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getSerialNumber());
                }
                //jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getInstance().getSerialNumber());
                jsonObject.addProperty(Consts.profile_type, 1);
                jsonObject.addProperty(Consts.role_id, 113);
                jsonObject.addProperty(Consts.email, email);
                if (isProfile == 1) {
                    if (profileName != null && !TextUtils.isEmpty(profileName))
                        jsonObject.addProperty(Consts.profile_name, profileName);
                    else
                        jsonObject.addProperty(Consts.profile_id, googleProfileId);
                }
                jsonObject.addProperty(Consts.google_access_token, googleAccessToken);
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));
                jsonObject.addProperty(Consts.age_group, ageGroupId);

                // API CALL
                Call<JsonObject> calls = feedApi.loginWithGoogle(jsonObject);
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
                                String responseString = response.body().toString();
                                CustomDashboardApplication.setString(context, Consts.profile_data, responseString);
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);
                                    if (status == 1) {
                                        try {
                                            try {
                                                final JSONObject dataObj = jsonObject.getJSONObject(Consts.data);

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
                                                    getAuthenticate();
                                                    CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);
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
                                                    CustomDashboardApplication.setInt(context, Consts.is_logged_in, 1);
                                                    getAuthenticate();
                                                    //finish();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        dialogLoader.hideProgressDialog();
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            dialogLoader.hideProgressDialog();
                            try {
                                if (response.errorBody() != null) {
                                    String resp = response.errorBody().string();
                                    Log.d("tag", "onResponse - Status : " + response.code());
                                    Log.e("regErr->", resp);
                                    Gson gson = new Gson();
                                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage)+"");

                                    final JsonElement element = registerResponse.get(Consts.data);
                                    if (!(element instanceof JsonNull)) {
                                        final JsonObject propertyToBeCopied = (JsonObject) element;

                                        final JsonObject dataObj = registerResponse.getAsJsonObject(Consts.data);
                                        if (dataObj.get(Consts.ask_profile_name).getAsInt() == 1) {
                                            isProfile = 1;

                                            JsonElement userProfiles = propertyToBeCopied.get(Consts.user_profiles);
                                            isGoogleLogin = true;
                                            showProfilePopUp(userProfiles);
                                        }
                                    } else {
                                        String err = String.valueOf(registerResponse.get(Consts.responseMessage));
                                        if (!TextUtils.isEmpty(err) && (err.contains("This device is not belongs to this account") ||
                                                err.contains("Device already registered"))) {
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                            showNewUserRegisterDialog();
                                        } else {
                                            ShowMessage.showError(context, String.valueOf(registerResponse.get(Consts.responseMessage)));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        dialogLoader.hideProgressDialog();
                        t.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
        Spinner kidsAgeSpinner = alertDialogProfileName.findViewById(R.id.spinner_kids_age);
        ArrayAdapter<ProfileListModel> profileListAdapter;
        ArrayAdapter<ProfileListModel> kidsAgeAdapter;

        final ArrayList<ProfileListModel> profileListModelList = new ArrayList<>();
        if (!(userProfiles instanceof JsonNull)) {

            profileListModelList.add(0, new ProfileListModel(0, "--- Select Kid ---", ""));

            if (userProfiles.getAsJsonArray().size() > 0) {
                if (cvProfileList != null) {
                    cvProfileList.setVisibility(View.VISIBLE);
                }
                if (txProfileList != null) {
                    txProfileList.setVisibility(View.VISIBLE);
                }
                if (txOr != null) {
                    txOr.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < userProfiles.getAsJsonArray().size(); i++) {
                    JsonObject object = (JsonObject) userProfiles.getAsJsonArray().get(i);
                    ProfileListModel profileListModel = new ProfileListModel();
                    profileListModel.setId(object.get(Consts.id).getAsInt());
                    profileListModel.setProfileId(object.get(Consts.profile_id).getAsString());
                    profileListModel.setProfileName(object.get(Consts.profile_name).getAsString());

                    profileListModelList.add(profileListModel);
                }

                profileListAdapter = new ArrayAdapter<>(Login.this, R.layout.view_spinner_item, profileListModelList);
                profileListAdapter.setDropDownViewResource(R.layout.spinner_list);
                if (spinnerProfile != null) {
                    spinnerProfile.setAdapter(profileListAdapter);
                }
            } else {
                if (cvProfileList != null) {
                    cvProfileList.setVisibility(View.GONE);
                }
                if (txProfileList != null) {
                    txProfileList.setVisibility(View.GONE);
                }
                if (txOr != null) {
                    txOr.setVisibility(View.GONE);
                }
            }
        }

        initKidsAgeList();
        kidsAgeAdapter = new ArrayAdapter<>(Login.this, R.layout.view_spinner_item, kidsAgeList);
        kidsAgeAdapter.setDropDownViewResource(R.layout.spinner_list);
        if (kidsAgeSpinner != null) {
            kidsAgeSpinner.setAdapter(kidsAgeAdapter);
        }

        if (spinnerProfile != null) {
            spinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        if (isGoogleLogin) {
                            googleProfileId = profileListModelList.get(position).getId();
                        } else {
                            Login.this.profileId = profileListModelList.get(position).getId();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        if (kidsAgeSpinner != null) {
            kidsAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ageGroupId = kidsAgeList.get(position).getId();
                    ageGroup = kidsAgeList.get(position).getProfileName();
                }

                @Override


                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                if (edProfileName.getText().toString().length() > 0) {
                    if (ageGroupId == 0) {
                        Toast.makeText(context, getString(R.string.select_kids_age), Toast.LENGTH_SHORT).show();
                    } else {
                        googleProfileName = edProfileName.getText().toString();

                        if (isGoogleLogin) {
                            loginWithGoogle(emailStr, googleProfileName);
                        } else {
                            edDeviceName.setText(googleProfileName);
                            userAuthentication();
                        }

                        alertDialogProfileName.dismiss();
                    }
                } else {
                    if (isGoogleLogin) {
                        if (googleProfileId > 0) {
                            loginWithGoogle(emailStr, googleProfileName);
                            alertDialogProfileName.dismiss();
                        } else {
                            Toast.makeText(context, "Select kid's profile or enter kid's details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (Login.this.profileId > 0) {
                            userAuthentication();
                        } else {
                            Toast.makeText(context, "Select kid's profile or enter kid's details", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        if (alertDialogProfileName.getWindow() != null) {
            alertDialogProfileName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogProfileName.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogProfileName.getWindow().setGravity(Gravity.CENTER);
        }
        alertDialogProfileName.show();
    }


    private void showNewUserRegisterDialog() {
        try {
            final AppCompatDialog dialog = new AppCompatDialog(context);
            dialog.setContentView(R.layout.item_register_new_device);

            TextView tvAboutUs = dialog.findViewById(R.id.tv_message);
            Button btnYes = dialog.findViewById(R.id.btn_yes);
            Button btnSkipLogin = dialog.findViewById(R.id.btn_skip_login);

            if (btnYes != null) {
                btnYes.setOnClickListener(v -> {
                    dialog.dismiss();
                    Utils.getRandomString(context);
                    if (isGoogleLogin) {
                        loginWithGoogle(emailStr, "");
                    } else {
                        CustomDashboardApplication.setString(context, Consts.generated_serial_no, "");
                        profileId = 0;
                        edDeviceName.setText("");
                        userAuthentication();
                    }
                });
            }

            if (btnSkipLogin != null) {
                btnSkipLogin.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                            !CustomDashboardApplication.isDeviceSamsungActivated()) {
                        Intent intent = new Intent(context, ActivateAdminActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                        Intent intent = new Intent(context, AppPermissionsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);
                        Log.e(LoginOrSignUpActivity.class.getSimpleName(), "onClick: Condition 3 @104");
                        CustomDashboardApplication.redirectOnActivity(context, true);
                    }
                });
            }

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().getDecorView().setLeft(20);
                dialog.getWindow().getDecorView().setRight(20);
            }
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    jsonObject.addProperty(Consts.device_serial_number, CustomDashboardApplication.getSerialNumber());
                }
                //jsonObject.addProperty(Consts.device_serial_number,  CustomDashboardApplication.getInstance().getSerialNumber());
                jsonObject.addProperty(Consts.profile_name, edDeviceName.getText().toString());
                jsonObject.addProperty(Consts.profile_type, 1); // 1 For Android
                jsonObject.addProperty(Consts.role_id, 113);
                jsonObject.addProperty(Consts.email, edUserName.getText().toString());
                jsonObject.addProperty(Consts.password, edPassword.getText().toString());
                jsonObject.addProperty(Consts.device_gcm_token, CustomDashboardApplication.getString(context, Consts.firebase_token));
                Log.e("requestObj", jsonObject.toString());

                // API CALL
                Call<JsonObject> calls = feedApi.profileDeviceRegister(jsonObject);
                Log.e("requestUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {

                            dialogLoader.hideProgressDialog();

                            if (response.code() == 200 || response.code() == 201) {
                                // dialogLoader.hideProgressDialog();
                                if (response.body() != null) {
                                    String responseString = response.body().toString();
                                    Log.e("resp", response.body().toString());
                                    final JSONObject jsonObject = new JSONObject(responseString);
                                    int status = jsonObject.getInt(Consts.status);

                                    if (status == 1) {

                                        try {
                                            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                                            dialogBuilder1.setMessage(jsonObject.getString(Consts.responseMessage));
                                            dialogBuilder1.setNeutralButton("Ok", (dialog, which) -> {
                                                //finish();
                                                dialogLoader.showProgressDialog();
                                                userAuthentication();
                                                d.dismiss();
                                            });
                                            d = dialogBuilder1.create();
                                            d.setOnShowListener(dialog -> {
                                                Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                                                doneBtn.setAllCaps(false);
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
                                }
                            } else {
                                try {
                                    // dialogLoader.hideProgressDialog();
                                    if (response.errorBody() != null) {
                                        String resp = response.errorBody().string();
                                        Log.d("tag", "onResponse - Status : " + response.code());
                                        Log.e("regErr->", resp);
                                        Gson gson = new Gson();
                                        TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                        JsonObject registerResponse = adapter.fromJson(resp);
                                        //if (Consts.isDebugMode == true) {
                                        String err = String.valueOf(registerResponse.get(Consts.responseMessage));
                                        if (!TextUtils.isEmpty(err) && err.contains("Device already registered")) {
                                            Toast.makeText(context, String.valueOf(registerResponse.get(Consts.responseMessage)), Toast.LENGTH_SHORT).show();
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
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        //  dialogLoader.hideProgressDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}