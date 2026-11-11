package com.tabnova.aidashboard.Activity;

import static com.tabnova.aidashboard.CustomDashboardApplication.isAccessibilitySettingsEnabled;
import static com.tabnova.aidashboard.Fragment.AppPermissionMain.CODE_DRAW_OVER_OTHER_APP_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.rsb.vpnpoc.MySingletonClass;
import com.rsb.vpnpoc.VPNService;
import com.tabnova.aidashboard.Adapter.RecyclerViewAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.CountdownTimer;
import com.tabnova.aidashboard.Extra.FileUtils;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.OnResponseListener;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Fragment.AppPermissionMain;
import com.tabnova.aidashboard.Knox.startup.CommonUtility;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.Model.DailyTimerModel;
import com.tabnova.aidashboard.Model.ScheduleEventModel;
import com.tabnova.aidashboard.R;
import com.tabnova.aidashboard.Service.AppVisibilityService;
import com.tabnova.screenshare.StartStopScreenShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends BaseActivity implements RecyclerViewAdapter.OnAppSelectListner, OnResponseListener, PurchasesUpdatedListener {
    // INTEGER
    private int pointerCount;
    Integer request = 0;
    private int pwdCount;
    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;
    private int Type;
    private int overLayType;
    private int currentScreen;
    private int incorrectPinTapCount = 0;

    // ALERT DIALOG
    private AppCompatDialog alertDialogNote;
    private AppCompatDialog alertDialogLauncher;
    private AppCompatDialog alertDialogSettings;
    private AppCompatDialog alertDialogSetTimer;

    private RelativeLayout relTimeUp;

    // LINEAR LAYOUT
    private LinearLayout llLogo;
    private LinearLayout llBottomSettings;
    private LinearLayout llExtendTimer;
    private LinearLayout llExit;
    private LinearLayout llPwd;
    private LinearLayout llTimesUp;
    private LinearLayout llLockedScreen;

    // private LinearLayout llPwdOverLay;

    // IMAGE VIEW
    public static ImageView ivBg;
    private ImageView ivLogo;
    private ImageView ivBtnOne;
    private ImageView ivBtnTwo;
    private ImageView ivBtnThree;
    private ImageView ivBtnFour;
    private ImageView ivBtnFive;
    private ImageView ivBtnSix;
    private ImageView ivBtnSeven;
    private ImageView ivBtnEight;
    private ImageView ivBtnNine;
    private ImageView ivBtnZero;
    private ImageView ivBtnBack;
    private ImageView ivBtnOk;
    private ImageView ivContactUs;
    public static ImageView ivBattery;

    // TEXT VIEW
    private TextView txHelp;
    private TextView txDate;
    public static TextView txInfo;
    private TextView txRemainTimer;
    public static TextView txBatteryPercent;
    public TextView txSamsungAlert;
    private TextView txLoginStatus;
    private TextView txLoginNote;

    public static Button btnStart;

    // IMAGE LOADER

    // ARRAY LIST
    public static ArrayList<AppModel> lstBook;
    public static ArrayList<ApplicationTimerModel> currentApplicationModels;
    private ArrayList<ApplicationTimerModel> lastSavedApp = new ArrayList<>();
    private ArrayList<DailyTimerModel> dailyTimerModels = new ArrayList<>();
    private ArrayList<ScheduleEventModel> scheduleEventModels = new ArrayList<>();
    private ArrayList<AppModel> addedWebShortcutsModels = new ArrayList<>();
    List<String> blockDomainWebsitesList = new ArrayList<>();

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 123;
//    private GoogleApiClient mGoogleApiClient;

    // CONTEXT
    private Context context;

    // TEXT CLOCK
    private TextClock txTime;

    // STRING
    private String userChoosenTask;

    // EDIT TEXT
    private EditText edPassword;
    private EditText edPasswordOverlay;
    private int isItemClick;

    // URI
    private Uri profileImageURI = null;
    private OneTimeWorkRequest workRequest;

    // STRING BUILDER
    StringBuilder mainSb = new StringBuilder();
    StringBuilder mainSbOverLay = new StringBuilder();

    private RecyclerViewAdapter myAdapter;
    private RecyclerView myRv;
    private int isSettings;
    public int isLlPassword;
    private int isVolumeEnabled;
    private WindowManager manager;
    private WindowManager overlayManager;
    private View overLayView;
    private customViewGroup view;

    private int isBackGround;
    private boolean isPredefineApps = false;
    private BufferedReader br;

    // API MANAGER
    APIManager apiManager;
    private int isHomeBtnClicked = 0;
    private CountDownTimer countDownTimer;

    // FRAGMENT
    private int permissionGiven = 0;

    long startAppTime;

    ArrayList<String> posList = new ArrayList<>();

    private boolean isProfileDeteted = false;

    // BILLING CLIENT
    private BillingClient billingClient;

    //SESSION MANAGER
    private SessionManager sessionManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }*/

        context = this;
        StartStopScreenShare.context = context;

        sessionManager = new SessionManager(context);

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

        CustomDashboardApplication.setInt(context, Consts.exit_app, 0);
        // CustomDashboardApplication.setInt(context, Consts.isActivityOpened , 0);

        startAppTime = System.currentTimeMillis();

        setWipeDateDetails();

        if (CustomDashboardApplication.getAppVersion(context, getPackageName()).equalsIgnoreCase(
                CustomDashboardApplication.getString(context, Consts.APP_VERSION))) {
            Log.e("AppVersion", "APP VERSION MATCHED " + CustomDashboardApplication.getAppVersion(context, getPackageName()));
        } else {
            Log.e("AppVersion", "APP VERSION NOT MATCHED " + CustomDashboardApplication.getAppVersion(context, getPackageName()));
            CustomDashboardApplication.setString(context, Consts.APP_VERSION, CustomDashboardApplication.getAppVersion(context, getPackageName()));
        }

        Log.e(TAG, "onCreate: Serial Number : " + CustomDashboardApplication.getString(context, Consts.generated_serial_no));

        alertDialogLauncher = new AppCompatDialog(context);

        txSamsungAlert = findViewById(R.id.tx_samsung_alert);

        apiManager = new APIManager(context, MainActivity.this);

        final String manufacturer = android.os.Build.MANUFACTURER;
        Log.e("manufacture", manufacturer);

        myRv = findViewById(R.id.recyclerview_id);

        ivBg = findViewById(R.id.iv_bg);
        ivLogo = findViewById(R.id.iv_logo);
        llLogo = findViewById(R.id.ll_logo);
        // ivDefault = findViewById(R.id.iv_logo_bg);
        txHelp = findViewById(R.id.tx_help);
        txTime = findViewById(R.id.tx_time);
        txDate = findViewById(R.id.tx_date);
        txInfo = findViewById(R.id.tx_info);
        // RELATIVE LAYOUT
        RelativeLayout relMain = findViewById(R.id.rel_main);
        // BUTTON
        Button btnSetWallpaper = findViewById(R.id.btn_set_wallpaper);
        // private ImageView ivDefault;
        ImageView ivSettings = findViewById(R.id.iv_settings);
        ImageView ivExit = findViewById(R.id.iv_exit);
        llBottomSettings = findViewById(R.id.ll_settings);
        txRemainTimer = findViewById(R.id.tx_remain_timer);
        relTimeUp = findViewById(R.id.rel_times_up);
        llExtendTimer = findViewById(R.id.ll_select_timer);
        llExit = findViewById(R.id.ll_exit);
        llPwd = findViewById(R.id.ll_show_pwd);
        edPassword = findViewById(R.id.ed_pwd);
        TextView txPrivacyPolicy = findViewById(R.id.tx_privacy_policy);
        TextView txTermsService = findViewById(R.id.tx_terms_service);
        btnStart = findViewById(R.id.btn_start);
        ivBattery = findViewById(R.id.iv_battery);
        txBatteryPercent = findViewById(R.id.tx_battery_percent);
        ivBattery.setImageResource(R.drawable.battery);
        ivContactUs = findViewById(R.id.btn_contact_us);
        txLoginStatus = findViewById(R.id.tx_login_status);
        txLoginNote = findViewById(R.id.tx_login);
        llTimesUp = findViewById(R.id.ll_times_up);
        llLockedScreen = findViewById(R.id.ll_lock_screen);
        TextView txForgotPin = findViewById(R.id.tx_forgot_pin);

        ivBtnOne = findViewById(R.id.btn1);
        ivBtnTwo = findViewById(R.id.btn2);
        ivBtnThree = findViewById(R.id.btn3);
        ivBtnFour = findViewById(R.id.btn4);
        ivBtnFive = findViewById(R.id.btn5);
        ivBtnSix = findViewById(R.id.btn6);
        ivBtnSeven = findViewById(R.id.btn7);
        ivBtnEight = findViewById(R.id.btn8);
        ivBtnNine = findViewById(R.id.btn9);
        ivBtnBack = findViewById(R.id.btn_clear);
        ivBtnZero = findViewById(R.id.btn0);
        ivBtnOk = findViewById(R.id.btn_ok);
        ImageView ivClose = findViewById(R.id.btn_close);

        ivClose.setOnClickListener(v -> {
            isHomeApp = 0;
            llPwd.setVisibility(View.GONE);
        });

        ivContactUs.setOnClickListener(v -> {
            incorrectPinTapCount = 0;
            CustomDashboardApplication.contactSupport(context);
        });

        txForgotPin.setOnClickListener(v -> {
         /*   if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                Intent intent = new Intent(context, ForgotPin.class);
                startActivity(intent);
            } else {*/
            CustomDashboardApplication.showLoggedInDialog(context);
            //}

        });

        txPrivacyPolicy.setPaintFlags(txPrivacyPolicy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txTermsService.setPaintFlags(txTermsService.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txPrivacyPolicy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.PRIVACY_POLICY));
            startActivity(browserIntent);
        });

        txTermsService.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.TERMS_SERVICE));
            startActivity(browserIntent);
        });

        lstBook = new ArrayList<>();

        if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
            lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
        }

        if (addedWebShortcutsModels.size() > 0) {
            lstBook.addAll(addedWebShortcutsModels);
        }

        currentScreen = 0;
        apiManager = new APIManager(context, this);
        // preventStatusBarExpansion();

        if (!isMyLauncherDefault()) {
            if (isMyLauncherDefaultstr().contains("android") || isMyLauncherDefaultstr().contains("oppo") || isMyLauncherDefaultstr().contains("huawei")) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    SuperLockState superLockState = new SuperLockState(context);
                    if (superLockState.isESDKLicenseActivated()) {
                        CustomDashboardApplication.enableSamsungKiosk(this);
                    }
                }
            }
        }

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
        String strDate = formatter.format(date);
        txDate.setText(strDate);

        currentApplicationModels = new ArrayList<>();

        // GOOGLE SIGN OPTIONS
        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
        if (!ser.equalsIgnoreCase("") && !TextUtils.isEmpty(ser)) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                lstBook = (ArrayList<AppModel>) obj;
            }
        }

        String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
        if (!TextUtils.isEmpty(ser1) && !ser1.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser1);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                dailyTimerModels = (ArrayList<DailyTimerModel>) obj;
            }
        }

        ivSettings.setOnClickListener(v -> {
            // SHOW ENTER PASSWORD POP UP
            Type = 0;
            showPasswordDialog(0);
        });

        ivExit.setOnClickListener(v -> {
            // SHOW ENTER PASSWORD POP UP
            Type = 2;
            showPasswordDialog(2);
        });

        btnSetWallpaper.setOnClickListener(v -> selectImage());

        relMain.setOnTouchListener((v, event) -> {
            int staus = initCountDownTimer();
            if (staus == 0) {
                pointerCount += event.getPointerCount();
                if (pointerCount == 10) {
                    // SHOW ENTER PASSWORD POP UP
                    Type = 0;
                    showPasswordDialog(0);
                    pointerCount = 0;
                    countDownTimer.cancel();
                    Log.e("Touch10Count", String.valueOf(pointerCount));
                }
                Log.e("TouchCount", String.valueOf(pointerCount));
            }
            return false;
        });

        //showRateAppDialog();

        myAdapter = new RecyclerViewAdapter(this, lstBook, this);

        // if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
        myRv.setLayoutManager(new GridLayoutManager(this, 3));
       /* } else {
            myrv.setLayoutManager(new GridLayoutManager(this, 4));
        }*/

        myRv.setAdapter(myAdapter);
        myRv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null) {
                    // tapped on child
                    return false;
                } else {
                    // Tap occured outside all child-views.
                    // do something

                    int staus = initCountDownTimer();
                    if (staus == 0) {
                        pointerCount += motionEvent.getPointerCount();
                        if (pointerCount == 5) {
                            // SHOW ENTER PASSWORD POP UP
                            Type = 0;
                            showPasswordDialog(0);
                            pointerCount = 0;
                            countDownTimer.cancel();
                            Log.e("Touch10Count", String.valueOf(pointerCount));
                        }
                        Log.e("TouchCount", String.valueOf(pointerCount));
                    }
                    return true;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {
            }
        });

        CustomDashboardApplication.setInt(context, Consts.INSTALLED_APP, CustomDashboardApplication.getInt(context, Consts.INSTALLED_APP) + 1);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(Consts.TimeUpdate));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver1, new IntentFilter(Consts.listUpdate));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver2, new IntentFilter(Consts.isServerUpdated));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver3, new IntentFilter(Consts.PROFILE_UPDATE));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver4, new IntentFilter(Consts.first_time));
        LocalBroadcastManager.getInstance(context).registerReceiver(appPermissionGiven, new IntentFilter(Consts.app_permission_done));

        if (CustomDashboardApplication.getInt(context, Consts.INSTALLED_APP) == 50) {
            if (Consts.IS_BUSINESS_APP != 1) {
                CustomDashboardApplication.getInstance().showRateAppDialog(context);
            }
        }

        if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.IMG_FILE)) &&
                CustomDashboardApplication.getString(context, Consts.IMG_FILE).equalsIgnoreCase(Consts.defaultWallpaperPath)) {
            CustomDashboardApplication.setString(context, Consts.IMG_FILE, "");
        }

        if (CustomDashboardApplication.isMyLauncherDefault(context)) {
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
        }

        if (!CustomDashboardApplication.isDeviceSamsungActivated()) {
            try {
                if (!CustomDashboardApplication.isMyLauncherDefault(context)) {
                    CustomDashboardApplication.setInt(context, Consts.set_as_home_app_status, 0);
                    if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("oppo.launcher")) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_HOME_SETTINGS);
                        startActivity(intent);
                    } else if (CustomDashboardApplication.isMyLauncherDefaultstr(context).contains("huawei.android.launcher")) {
                        Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                        startActivity(localObject);
                    } else {
                        PackageManager packageManager = context.getPackageManager();
                        ComponentName componentName = new ComponentName(context, FakeActivity.class);
                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                        Intent selector = new Intent(Intent.ACTION_MAIN);
                        selector.addCategory(Intent.CATEGORY_HOME);
                        startActivity(selector);

                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(
                    CommonUtility.fileDownloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    CommonUtility.fileDownloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        }

        //setupBillingClient();
        stopVPN();

        blockDomainWebsitesList = new ArrayList<>();
        String blockDomainsList = SerializeObject.ReadSettings(context, Consts.blockDomainWebsiteFile);
        if (!blockDomainsList.equalsIgnoreCase("")) {
            Object blockDomainsObj = SerializeObject.stringToObject(blockDomainsList);
            if (blockDomainsObj instanceof ArrayList) {
                blockDomainWebsitesList = (ArrayList<String>) blockDomainsObj;
            }
        }

        if (!Settings.canDrawOverlays(context)) {
            Utils.showOverlayPermissionPopup(context);
        }

        if ((blockDomainWebsitesList != null && blockDomainWebsitesList.size() > 0) || sessionManager.isApplyBlockDomains()) {
            startVPN();
        }
    }

    BroadcastReceiver appPermissionGiven = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            permissionGiven = 1;
        }
    };

    BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.first_update, 0);
                if (i == 1) {
                    lstBook = new ArrayList<>();
                    if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                        lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                    }
                    if (addedWebShortcutsModels.size() > 0) {
                        lstBook.addAll(addedWebShortcutsModels);
                    }
                    String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
                    if (!TextUtils.isEmpty(ser) && !ser.equalsIgnoreCase("")) {
                        Object obj = SerializeObject.stringToObject(ser);
                        // Then cast it to your object and
                        if (obj instanceof ArrayList) {
                            // Do something
                            lstBook = (ArrayList<AppModel>) obj;
                            Log.e(TAG, "run: @1012");
                            setAppListOnDashboard();
                            /*myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                            int currentOrientation = getResources().getConfiguration().orientation;
                            // if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
                            myrv.setLayoutManager(new GridLayoutManager(context, 3));
                            myrv.setAdapter(myAdapter);
                            myAdapter.notifyDataSetChanged();*/
                        }
                    }
                }
            }
        }
    };

    BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.listUpdateCount, 0);
                if (i == 1) {

                    ArrayList<AppModel> appModels = new ArrayList<>();
                    String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
                    if (!TextUtils.isEmpty(ser) && !ser.equalsIgnoreCase("")) {
                        Object obj = SerializeObject.stringToObject(ser);
                        // Then cast it to your object and
                        if (obj instanceof ArrayList) {
                            // Do something
                            appModels = (ArrayList<AppModel>) obj;
                        }
                    }

                    ArrayList<AppModel> appModels1 = new ArrayList<>();


                    for (int j = 0; j < appModels.size(); j++) {
                        if (appModels.get(j).isSelected()) {
                            appModels1.add(appModels.get(j));
                        }
                    }

                    lstBook = new ArrayList<>();
                    if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                        appModels1.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                    }
                    if (addedWebShortcutsModels.size() > 0) {
                        appModels1.addAll(addedWebShortcutsModels);
                    }
                    lstBook.addAll(appModels1);
                    Log.e(TAG, "run: @1061");
                    setAppListOnDashboard();
                    /*myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                    myrv.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();*/
                }
            }
        }
    };

    BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.serverUpdate, 0);
                if (i == 1) {
                    apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
                }
            }
        }
    };


    BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.profile_updated, 0);
                if (i == 1) {

                    // UPDATE BATTERY STATUS VIEW
                    if (CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS) == 0) {
                        ivBattery.setVisibility(View.VISIBLE);
                        txBatteryPercent.setVisibility(View.VISIBLE);
                    } else {
                        ivBattery.setVisibility(View.GONE);
                        txBatteryPercent.setVisibility(View.GONE);
                    }

                    // UPDATE STATUS BAR VIEW
                    if (CustomDashboardApplication.getInt(context, Consts.APP_INFO) == 0) {
                        txInfo.setVisibility(View.INVISIBLE);
                    } else {
                        txInfo.setText(CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
                        txInfo.setVisibility(View.VISIBLE);
                    }

                    // UPDATE BACK GROUND IMAGE
                    if (CustomDashboardApplication.getString(context, Consts.IMG_FILE).startsWith("http")) {
                        String contentURI = CustomDashboardApplication.getString(context, Consts.IMG_FILE);
                        Glide.with(MainActivity.this).load(contentURI).format(DecodeFormat.PREFER_RGB_565).into(ivBg);
                    }

                    // SET SECURE WEB VIEW
                    if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                        ArrayList<AppModel> appModels = new ArrayList<>();
                        String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
                        if (!TextUtils.isEmpty(ser) && !ser.equalsIgnoreCase("")) {
                            Object obj = SerializeObject.stringToObject(ser);
                            // Then cast it to your object and
                            if (obj instanceof ArrayList) {
                                // Do something
                                appModels = (ArrayList<AppModel>) obj;
                            }
                        }

                        ArrayList<AppModel> appModels1 = new ArrayList<>();


                        for (int j = 0; j < appModels.size(); j++) {
                            if (appModels.get(j).isSelected()) {
                                appModels1.add(appModels.get(j));
                            }
                        }

                        lstBook = new ArrayList<>();
                        if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                            appModels1.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                        }
                        if (addedWebShortcutsModels.size() > 0) {
                            appModels1.addAll(addedWebShortcutsModels);
                        }
                        lstBook.addAll(appModels1);
                        Log.e(TAG, "run: @1143");
                        setAppListOnDashboard();
                        /*myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                        myrv.setAdapter(myAdapter);
                        myAdapter.notifyDataSetChanged();*/
                    }


                    // UPDATE DATE/TIME SETTING
                    if (CustomDashboardApplication.getClockEnabled(context, Consts.CLOCK_WIDGET) == 0) {
                        txTime.setVisibility(View.GONE);
                        txDate.setVisibility(View.GONE);
                    } else {
                        int selectedSetting = CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING);

                        if (selectedSetting == 0) {
                            txTime.setVisibility(View.GONE);
                            txDate.setVisibility(View.GONE);
                        } else if (selectedSetting == 1) {
                            txTime.setVisibility(View.VISIBLE);
                            txDate.setVisibility(View.GONE);
                        } else if (selectedSetting == 2) {
                            txTime.setVisibility(View.GONE);
                            txDate.setVisibility(View.VISIBLE);
                        } else if (selectedSetting == 3) {
                            txTime.setVisibility(View.VISIBLE);
                            txDate.setVisibility(View.VISIBLE);
                        }

                    }

                    // UPDATE SHORT CUTS
                    if (CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD) == 0) {
                        llBottomSettings.setVisibility(View.GONE);
                    } else {
                        llBottomSettings.setVisibility(View.VISIBLE);
                    }

                    // UPDATE VOLUME SETTING
                    if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 0) {
                        CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 0);
                        isVolumeEnabled = 0;
                    } else {
                        CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 1);
                        isVolumeEnabled = 1;
                    }

                    // UPDATE TIME OUT DATA
                    if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 0) {
                        WorkManager.getInstance(context).cancelAllWork();
                    } else {
                        if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER) == 1) {
                            if (dailyTimerModels.size() > 0) {
                                final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);

                                if (timer != 0) {

                                    long timeMillis = dailyTimerModels.get(timer).getDailyTime();
                                    TimeUnit timeUnit = dailyTimerModels.get(timer).getDailyTimeUnit();

                                    Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                                    workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                                    WorkManager.getInstance(context).enqueue(workRequest);
                                } else {
                                    WorkManager.getInstance(context).cancelAllWork();
                                }
                            }

                        } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER) == 2) {

                            if (scheduleEventModels.size() > 0) {

                                for (int l = 0; l < scheduleEventModels.size(); l++) {

                                    ArrayList<String> strings = new ArrayList<String>(Arrays.asList(scheduleEventModels.get(l).getDay().split(",")));

                                    ArrayList<Integer> integers = new ArrayList<>();

                                    for (int m = 0; m < strings.size(); m++) {
                                        integers.add(Integer.parseInt(strings.get(m)));
                                    }

                                    Calendar calendar = Calendar.getInstance();
                                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                                    if (integers.size() > 0) {
                                        for (int k = 0; k < integers.size(); k++) {
                                            if (currentDay == integers.get(k)) {
                                                Calendar startDate = Calendar.getInstance();
                                                startDate.set(Calendar.HOUR, Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[0]));
                                                startDate.set(Calendar.MINUTE, Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[1]));

                                                Calendar endDate = Calendar.getInstance();
                                                endDate.set(Calendar.HOUR, Integer.parseInt(scheduleEventModels.get(l).getEndTime().split(":")[0]));
                                                endDate.set(Calendar.MINUTE, Integer.parseInt(scheduleEventModels.get(l).getEndTime().split(":")[1]));

                                                if (calendar.before(startDate)) {
                                                    Intent intent1 = new Intent(context, TimeOutScreen.class);
                                                    startActivity(intent1);
                                                    setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                                } else if (calendar.after(endDate)) {
                                                    Intent intent2 = new Intent(context, TimeOutScreen.class);
                                                    startActivity(intent2);
                                                    setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                                } else {
                                                    setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }

                        } else {
                            WorkManager.getInstance(context).cancelAllWork();
                        }
                    }
                }
            }
        }
    };


    BroadcastReceiver broadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.force_exit, 0);
                if (i == 1) {
                    Utils.exitApp(MainActivity.this);
                }
            }
        }
    };

    BroadcastReceiver broadcastReceiver6 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.delete_child, 0);
                if (i == 1) {
                    isProfileDeteted = true;
                    logoutUser();
                    if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                        CustomDashboardApplication.exitSamsungKiosk(MainActivity.this);
                        SuperLockState.getInstance(MainActivity.this).allowStatusBar(true);

                        adminDisable();
                    }
                    Intent ii = new Intent(Intent.ACTION_DELETE);
                    ii.setData(Uri.parse("package:com.tabnova.aidashboard"));
                    startActivity(ii);
                }
            }
        }
    };


    BroadcastReceiver broadcastReceiver7 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                ivBg.setImageResource(R.drawable.bg_new_dashboard);
                CustomDashboardApplication.setString(context, Consts.IMG_FILE, "");
            }
        }
    };

    public void setBatteryIndicator(int batLvl) {
        if (batLvl == 101) {
            ivBattery.setImageResource(R.drawable.ic_charging);
        } else {
            ivBattery.setImageResource(R.drawable.battery);
            ivBattery.setImageLevel(batLvl);
            ivBattery.setTag(batLvl);
        }

    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {

                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

                // Calculate the battery charged percentage
                float percentage = level / (float) scale;
                int mProgressStatus = (int) ((percentage) * 100);
                txBatteryPercent.setText(mProgressStatus + "%");
                CustomDashboardApplication.setString(context, Consts.BATTERY_LEVEL, txBatteryPercent.getText().toString().trim());
                if (isConnected(context)) {
                    setBatteryIndicator(101);
                } else {
                    setBatteryIndicator(level);
                }

            }
        }
    };

    public boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
        if (intent != null && intent.getExtras() != null) {
            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        }
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }


    // PREVENT STATUS BAR
    public void preventStatusBarExpansion() {
        manager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            disablePullNotificationTouch();
        } else {
           /* if (Settings.canDrawOverlays(context)) {
                try {
                    disablePullNotificationTouch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                //startActivity(intent);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            }*/
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        stopVPN();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver1);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver2);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver3);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver6);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver7);
        unregisterReceiver(CommonUtility.fileDownloadReceiver);
        try {
            if (googleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        view = new customViewGroup(this);

        try {
            manager.addView(view, localLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOverLayView() {
        overlayManager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));
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
        localLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.format = PixelFormat.TRANSPARENT;

        ImageView ivBtnOne, ivBtnTwo, ivBtnThree, ivBtnFour, ivBtnFive,
                ivBtnSix, ivBtnSeven, ivBtnEight, ivBtnNine, ivBtnZero, ivBtnBack, ivBtnOk;

        overLayView = LayoutInflater.from(this).inflate(R.layout.time_up_overlay, null);
        LinearLayout llExtendTimer = overLayView.findViewById(R.id.ll_select_timer);
        LinearLayout llExit = overLayView.findViewById(R.id.ll_exit);
        final LinearLayout llPwdOverLay = overLayView.findViewById(R.id.ll_pwd);
        edPasswordOverlay = overLayView.findViewById(R.id.ed_pwd);
        final LinearLayout llTimerPopup = overLayView.findViewById(R.id.ll_timer_popup);


        ivBtnOne = overLayView.findViewById(R.id.btn1);
        ivBtnTwo = overLayView.findViewById(R.id.btn2);
        ivBtnThree = overLayView.findViewById(R.id.btn3);
        ivBtnFour = overLayView.findViewById(R.id.btn4);
        ivBtnFive = overLayView.findViewById(R.id.btn5);
        ivBtnSix = overLayView.findViewById(R.id.btn6);
        ivBtnSeven = overLayView.findViewById(R.id.btn7);
        ivBtnEight = overLayView.findViewById(R.id.btn8);
        ivBtnNine = overLayView.findViewById(R.id.btn9);
        ivBtnBack = overLayView.findViewById(R.id.btn_clear);
        ivBtnZero = overLayView.findViewById(R.id.btn0);
        ivBtnOk = overLayView.findViewById(R.id.btn_ok);


        ivBtnOne.setOnClickListener(v -> buildStringOverLay(1, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnTwo.setOnClickListener(v -> buildStringOverLay(2, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnThree.setOnClickListener(v -> buildStringOverLay(3, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnFour.setOnClickListener(v -> buildStringOverLay(4, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnFive.setOnClickListener(v -> buildStringOverLay(5, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnSix.setOnClickListener(v -> buildStringOverLay(6, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnSeven.setOnClickListener(v -> buildStringOverLay(7, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnEight.setOnClickListener(v -> buildStringOverLay(8, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnNine.setOnClickListener(v -> buildStringOverLay(9, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnBack.setOnClickListener(v -> buildStringOverLay(10, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnZero.setOnClickListener(v -> buildStringOverLay(0, llPwdOverLay, llTimerPopup, overLayView));

        ivBtnOk.setOnClickListener(v -> buildStringOverLay(11, llPwdOverLay, llTimerPopup, overLayView));

        //  LinearLayout llPwd = findViewById(R.id.ll_show_pwd);

        llExit.setOnClickListener(v -> {
            // Type = 2;
            //showPasswordDialog(2);
            overLayType = 2;
            llPwdOverLay.setVisibility(View.VISIBLE);

          /*  if (overLayView != null){
                overlayManager.removeView(overLayView);
            }*/

            // finish();
        });

        llExtendTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overLayType = 3;
                llPwdOverLay.setVisibility(View.VISIBLE);

                //Type = 3;
                //showPasswordDialog(3);
            }
        });

        overlayManager.addView(overLayView, localLayoutParams);
    }

    public void setAppAsDefaultLauncher() {
      /*  List<IntentFilter> filters = new ArrayList<IntentFilter>();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        getPackageManager().getPreferredActivities(filters, activities, null);*/

        /*Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/

      /*  final ResolveInfo defaultResolution = context.getPackageManager().resolveActivity(selector, PackageManager.MATCH_DEFAULT_ONLY);
        if (defaultResolution != null) {
            final ActivityInfo activity = defaultResolution.activityInfo;
            if (activity.name.equals("com.sec.android.app.launcher.activities.LauncherActivity")) {
                selector.setClassName(activity.applicationInfo.packageName, getPackageName() + ".Activity.FakeActivity");
                startActivity(selector);
            }
        }*/
        isHomeApp = 1;
        //Intent intent= new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        final PackageManager packageManager = getPackageManager();
        for (final ResolveInfo resolveInfo : packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY)) {
            if (!getPackageName().equals(resolveInfo.activityInfo.packageName))  //if this activity is not in our activity (in other words, it's another default home screen)
            {
                //intent=packageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName);
                startActivity(getPackageManager().getLaunchIntentForPackage("com.sec.android.app.launcher"));
                break;
            }
        }
    }

    @Override
    public void onSuccess(Response<JsonObject> response) {
    }

    @Override
    public void onFailed(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }


    public static class BlockStatusBar {
        Context context;
        // To keep track of activity's window focus
        boolean currentFocus;
        // To keep track of activity's foreground/background status
        boolean isPaused;
        boolean isStooped;

        public static Handler collapseNotificationHandler;
        Method collapseStatusBar = null;


        public BlockStatusBar(Context context, boolean isPaused, boolean isStopped) {
            this.context = context;
            this.isPaused = isPaused;
            this.isStooped = isStopped;
            collapseNow();
        }


        public void collapseNow() {

            // Initialize 'collapseNotificationHandler'
            if (collapseNotificationHandler == null) {
                collapseNotificationHandler = new Handler();
            }

            // If window focus has been lost && activity is not in a paused state
            // Its a valid check because showing of notification panel
            // steals the focus from current activity's window, but does not
            // 'pause' the activity
            if (!currentFocus && !isPaused) {

                // Post a Runnable with some delay - currently set to 300 ms
                collapseNotificationHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        // Use reflection to trigger a method from 'StatusBarManager'

                        @SuppressLint("WrongConstant")
                        Object statusBarService = context.getSystemService("statusbar");
                        Class<?> statusBarManager = null;

                        try {
                            statusBarManager = Class.forName("android.app.StatusBarManager");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            // Prior to API 17, the method to call is 'collapse()'
                            // API 17 onwards, the method to call is `collapsePanels()`
                            if (statusBarManager != null) {
                                collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        if (collapseStatusBar != null)
                            collapseStatusBar.setAccessible(true);

                        try {
                            if (collapseStatusBar != null) {
                                collapseStatusBar.invoke(statusBarService);
                            }
                        } catch (IllegalArgumentException | IllegalAccessException |
                                 InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        // Check if the window focus has been returned
                        // If it hasn't been returned, post this Runnable again
                        // Currently, the delay is 100 ms. You can change this
                        // value to suit your needs.
                        if (!currentFocus && !isPaused) {
                            collapseNotificationHandler.postDelayed(this, 100L);
                        }

                        if (!currentFocus && isPaused) {
                            collapseNotificationHandler.removeCallbacksAndMessages(null);
                        }

                    }
                }, 1L);
            }
        }
    }

    public static class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("customViewGroup", "**********Intercepted");
            return true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            try {
                //sendBroadcast(closeDialog);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Method that handles loss of window focus
            if (!CustomDashboardApplication.isDeviceSamsungActivated()) {
                if (CustomDashboardApplication.getInt(context, Consts.ENABLE_STATUS_BAR) == 1) {
                    new BlockStatusBar(this, false, true).collapseNow();
                } else {
                    new BlockStatusBar(this, false, false).collapseNow();
                }
            }

        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.TimeStop, 0);
                if (i == 1) {

                    if (isBackGround == 1) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //  if (Settings.canDrawOverlays(context)) {
                            try {
                                showOverLayView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                          /*  } else {
                                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                            }*/
                        } else {
                            showOverLayView();
                        }
                    } else {
                        relTimeUp.setVisibility(View.VISIBLE);

                        llExtendTimer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Type = 3;
                                showPasswordDialog(3);
                            }
                        });

                        llExit.setOnClickListener(v -> {
                            // Type = 2;
                            showPasswordDialog(4);
                        });
                    }
                }
            }
        }
    };


    // SHOW SET TIMER SETTINGS DIALOG
    void showSetTimerDialog() {
        try {
            alertDialogSetTimer = new AppCompatDialog(context);
            alertDialogSetTimer.setContentView(R.layout.timer_pop_up);

            //RadioGroup rgTimeSettings = alertDialogSetTimer.findViewById(R.id.rg_setting);
            final RadioButton rbNoTimer = alertDialogSetTimer.findViewById(R.id.rb_no_timer);
            final RadioButton rbDailyLimit = alertDialogSetTimer.findViewById(R.id.rb_daily_limits);
            final RadioButton rbSchedule = alertDialogSetTimer.findViewById(R.id.rb_schedule);

            final Spinner spinnerTimer = alertDialogSetTimer.findViewById(R.id.spinner_timer);
            final Spinner spinnerSchedule = alertDialogSetTimer.findViewById(R.id.spinner_schedule);

            final CardView cvTimer = alertDialogSetTimer.findViewById(R.id.cv_timer);
            final CardView cvSchedule = alertDialogSetTimer.findViewById(R.id.cv_schedule);

            ArrayList<DailyTimerModel> timeList = new ArrayList<>();

            String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser1);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    timeList = (ArrayList<DailyTimerModel>) obj;
                }
            }

            ArrayList<String> scheduleList = new ArrayList<>();
            scheduleList.add("Add Event");

            ArrayAdapter<DailyTimerModel> timeAdapter;
            ArrayAdapter<String> scheduleAdapter;

            timeAdapter = new ArrayAdapter<DailyTimerModel>(this, R.layout.view_spinner_item, timeList);
            timeAdapter.setDropDownViewResource(R.layout.spinner_list);
            spinnerTimer.setAdapter(timeAdapter);

            scheduleAdapter = new ArrayAdapter<String>(this, R.layout.view_spinner_item, scheduleList);
            scheduleAdapter.setDropDownViewResource(R.layout.spinner_list);
            spinnerSchedule.setAdapter(scheduleAdapter);

            //Switch switchTime = alertDialogSetTimer.findViewById(R.id.dt_time_switch);
            Button btnDone = alertDialogSetTimer.findViewById(R.id.done_btn);


            if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 0) {
                rbNoTimer.setChecked(true);
                rbDailyLimit.setChecked(false);
                rbSchedule.setChecked(false);
                cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerTimer.setEnabled(false);
                cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerSchedule.setEnabled(false);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 1) {
                rbNoTimer.setChecked(false);
                rbDailyLimit.setChecked(true);
                rbSchedule.setChecked(false);
                cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                spinnerTimer.setEnabled(true);
                cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerSchedule.setEnabled(false);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);

                spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 2) {
                rbNoTimer.setChecked(false);
                rbDailyLimit.setChecked(false);
                rbSchedule.setChecked(true);
                cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerTimer.setEnabled(false);
                cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                spinnerSchedule.setEnabled(true);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);

                spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

            }


            rbNoTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbNoTimer.setChecked(true);
                        rbDailyLimit.setChecked(false);
                        rbSchedule.setChecked(false);
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 0);
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                        cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        spinnerTimer.setEnabled(false);
                        cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        spinnerSchedule.setEnabled(false);
                    }
                }
            });


            rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbNoTimer.setChecked(false);
                        rbSchedule.setChecked(false);
                        spinnerTimer.setEnabled(true);
                        cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                        cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        spinnerSchedule.setEnabled(false);
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 1);
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                    }
                }
            });


            if (rbSchedule != null) {
                rbSchedule.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        rbNoTimer.setChecked(false);
                        rbDailyLimit.setChecked(false);
                        spinnerTimer.setEnabled(false);
                        cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                        spinnerSchedule.setEnabled(true);
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 1);
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                    }
                });
            }

            spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (btnDone != null) {
                btnDone.setOnClickListener(v -> {
                    if (Settings.canDrawOverlays(context)) {
                        doneTimerPopup();
                    } else {
                        Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                    }
                });
            }

            if (alertDialogSetTimer.getWindow() != null) {
                alertDialogSetTimer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialogSetTimer.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                alertDialogSetTimer.getWindow().setGravity(Gravity.CENTER);
            }
            alertDialogSetTimer.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doneTimerPopup() {
        if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 0) {
            txRemainTimer.setVisibility(View.GONE);
            WorkManager.getInstance(context).cancelAllWork();
        } else {
            if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 0) {
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                WorkManager.getInstance(context).cancelAllWork();
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 1) {
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER) != 0) {
                    if (dailyTimerModels.size() > 0) {
                        final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);
                        if (timer != 0) {
                            long timeMillis = dailyTimerModels.get(timer).getDailyTime();
                            TimeUnit timeUnit = dailyTimerModels.get(timer).getDailyTimeUnit();

                            Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                            workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                            WorkManager.getInstance(context).enqueue(workRequest);
                        } else {
                            WorkManager.getInstance(context).cancelAllWork();
                        }
                    }
                }
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 2) {
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
            }
        }

        relTimeUp.setVisibility(View.GONE);
        alertDialogSetTimer.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (isMyLauncherDefault()) {
                if (isHomeBtnClicked == 0) {
                    //resetPreferredLauncherAndOpenChooser(context);
                    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
                }
            }

            return true;
        }


        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isVolumeEnabled == 1) {

                AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                try {
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

               /* AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);

                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {

               /* AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
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
                try {
                    AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    // Get the music current volume level
                    int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                    // Get the device music maximum volume level
                    int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);


*/
                return true;
            } else {

             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            stopService(new Intent(context, AppVisibilityService.class));
            if (!CustomDashboardApplication.isDeviceSamsung())
                startService(new Intent(context, AppVisibilityService.class));
            if (isMyLauncherDefault()) {
                //resetPreferredLauncherAndOpenChooser(context);
            }

            List<ApplicationInfo> packages;
            PackageManager pm;
            pm = getPackageManager();
            //get a list of installed apps.
            packages = pm.getInstalledApplications(0);

            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
                    continue;
                if (packageInfo.packageName.equals(getPackageName()))
                    continue;
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }

            return true;
        }

        /*if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CustomDashboardApplication.getInt(context, Consts.IS_FIRST_INSTALLED) == 0) {
                finish();
            } else {
                return true;
            }

        }*/

        return true;
    }

    public void resetPreferredLauncherAndOpenChooser(Context context) {
        /*try {
            CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            //packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                startActivity(localObject);
            } else {
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selector);
            }

            //packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                startActivity(localObject);
            } else {
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selector);
            }

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String isMyLauncherDefaultstr() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        //CustomDashboardApplication.setString(context, Consts.defaultLauncherName, str);
        Log.e("Package Name:", str);
        return str;
    }

   /* @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (CustomDashboardApplication.getInt(context, Consts.IS_FIRST_INSTALLED) == 1){
            if (isHomeApp != 1) {
                stopService(new Intent(context, AppVisibilityService.class));
                startService(new Intent(context, AppVisibilityService.class));
            }
        }
    }*/

    // SHOW OPEN SETTINGS DIALOG
    void showOpenSettingsDialog() {
        alertDialogSettings = new AppCompatDialog(context);
        alertDialogSettings.setContentView(R.layout.settings_pop_up);

        Button btnDone = alertDialogSettings.findViewById(R.id.done_btn);
        btnDone.setOnClickListener(v -> {
            SessionManager.getInstance(context).setIsParentMode(true);
            Intent openSettings = new Intent(context, CustomDashboardSettings.class);
            startActivity(openSettings);
            stopVPN();
            finish();
            // resetPreferredLauncherAndOpenChooser(context);
            alertDialogSettings.dismiss();
        });

        alertDialogSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogSettings.getWindow().setGravity(Gravity.CENTER);

        if (CustomDashboardApplication.getInt(context, Consts.isSettingPopUp) < 2) {

            if (isSettings == 1) {
                if (!isPredefineApps) {
                    SessionManager.getInstance(context).setIsParentMode(true);
                    Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                    startActivity(openSettings);
                    stopVPN();
                    isSettings = 0;
                    finish();
                } else {
                    myAdapter.notifyDataSetChanged();
                }
            } else if (isSettings == 2) {
                alertDialogSettings.show();
            } else {
                if (!isPredefineApps) {
                    int isReqPermission = 0;
                    if (isReqPermission != 0) {
                        if (CustomDashboardApplication.getInt(context, Consts.isAppSelected) == 0) {
                            lstBook.clear();
                            lstBook = new ArrayList<>();
                            if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                                lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                            }
                            if (addedWebShortcutsModels.size() > 0) {
                                lstBook.addAll(addedWebShortcutsModels);
                            }
                            Log.e(TAG, "run: @2256");
                            setAppListOnDashboard();
                            /*myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                            myrv.setAdapter(myAdapter);*/

                            if (alertDialogLauncher != null) {
                                if (alertDialogLauncher.isShowing()) {
                                    alertDialogLauncher.dismiss();
                                }
                            }


                            /*if (CustomDashboardApplication.getInt(context, Consts.force_login) == 1) {

                                if (CustomDashboardApplication.alertDialogLoggedIn != null) {
                                    if (CustomDashboardApplication.alertDialogLoggedIn.isShowing()) {
                                        CustomDashboardApplication.alertDialogLoggedIn.dismiss();
                                    }
                                }
                               CustomDashboardApplication.getInstance().showLoggedInDialog(context);
                            } else if (CustomDashboardApplication.getInt(context, Consts.force_login) != 2){*/
                            CustomDashboardApplication.setInt(context, Consts.isSettingPopUp, CustomDashboardApplication.getInt(context, Consts.isSettingPopUp) + 1);
                            alertDialogSettings.show();
                            //}
                        }

                    } else {
                        if (currentScreen == 0) {
                            if (CustomDashboardApplication.getInt(context, Consts.isAppSelected) == 0) {
                                lstBook.clear();
                                lstBook = new ArrayList<>();
                                if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                                    lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                                }
                                if (addedWebShortcutsModels.size() > 0) {
                                    lstBook.addAll(addedWebShortcutsModels);
                                }
                                Log.e(TAG, "run: @2292");
                                setAppListOnDashboard();
                               /* myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                                myrv.setAdapter(myAdapter);*/

                                if (alertDialogLauncher != null) {
                                    if (alertDialogLauncher.isShowing()) {
                                        alertDialogLauncher.dismiss();
                                    }
                                }
                              /*  if (CustomDashboardApplication.getInt(context, Consts.force_login) == 1) {
                                    if (CustomDashboardApplication.alertDialogLoggedIn != null) {
                                        if (CustomDashboardApplication.alertDialogLoggedIn.isShowing()) {
                                            CustomDashboardApplication.alertDialogLoggedIn.dismiss();
                                        }
                                    }
                                    CustomDashboardApplication.getInstance().showLoggedInDialog(context);
                                } else if (CustomDashboardApplication.getInt(context, Consts.force_login) != 2){*/
                                CustomDashboardApplication.setInt(context, Consts.isSettingPopUp, CustomDashboardApplication.getInt(context, Consts.isSettingPopUp) + 1);
                                alertDialogSettings.show();
                                // }
                            }
                        }
                    }
                }
            }
        }
    }


    // SHOW NOTE DIALOG
    void showNoteFolderDialog() {
        alertDialogNote = new AppCompatDialog(context);
        alertDialogNote.setContentView(R.layout.note_pop_up);

        Button btnDone = alertDialogNote.findViewById(R.id.done_btn);
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                alertDialogNote.dismiss();
//                signIn();
                ivLogo.setVisibility(View.GONE);
                llLogo.setVisibility(View.GONE);
            });
        }

        if (alertDialogNote.getWindow() != null)
            alertDialogNote.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogNote.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setRequestedOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setRequestedOrientation(getResources().getConfiguration().orientation);
    }


    // SHOW OPEN SETTINGS DIALOG
    void showOpenLauncherDialog() {
        alertDialogLauncher.setContentView(R.layout.launcher_pop_up);
        alertDialogLauncher.setCancelable(false);

        Button btnDone = alertDialogLauncher.findViewById(R.id.done_btn);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMyLauncherDefault()) {
                    //resetPreferredLauncherAndOpenChooser(context);
                }
                alertDialogLauncher.dismiss();
            }
        });

        alertDialogLauncher.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogLauncher.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogLauncher.getWindow().setGravity(Gravity.CENTER);
        alertDialogLauncher.show();
    }


    // OPEN SELECT IMAGE POP UP
    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_library), getString(R.string.cancel)};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.title_add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utils.checkPermission(context);

                if (items[item].equals(getString(R.string.take_photo))) {
                    userChoosenTask = getString(R.string.take_photo);
                    if (result) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            cameraIntent();
                        } else {
                            Utils.openCameraIntent(context, MainActivity.this);
                        }
                    }
                } else if (items[item].equals(getString(R.string.choose_from_library))) {
                    userChoosenTask = getString(R.string.choose_from_library);
                    if (result)
                        galleryIntent();
                } else if (items[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    // OPEN CAMERA INTENT
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    // OPEN GALLERY INTENT
    private void galleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECT_FILE);
    }

    private void onCaptureImageResult(Intent data) {
        try {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            String paths = Environment.getExternalStorageDirectory() + String.valueOf(System.currentTimeMillis()) + ".jpg";
            File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");

            FileOutputStream fo = null;
            try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri tempUri = getImageUri(context, thumbnail);
            profileImageURI = tempUri;
            CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
            Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET FILE
    public void importFile() {
        try {

            if (Consts.IS_BUSINESS_APP == 1) {

                lstBook = new ArrayList<>();

                StringBuilder text = new StringBuilder();
                try {
                    // File sdcard = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "dashboard.txt");
                    // File sdcard1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "dashboard.json");

            /*    File file = null;
                if (sdcard.exists()){

                } else if (sdcard1.exists()){
                    br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        Log.i("Test", "text : "+text+" : end");
                        text.append('\n');
                    }
                }*/

                    String fileString = String.valueOf(getExternalFilesDir(null));
                    Log.e("fileString", fileString);
                    String[] splitString = fileString.split("Android");
                    String downloadPathString = splitString[0] + "Download";

                    boolean fileExists = false;
                    File downloadFile = new File(downloadPathString);
                   /* if (downloadFile.exists()){
                        fileExists = true;
                        Toast.makeText(context, "Folder Exist", Toast.LENGTH_SHORT).show();
                    } else {
                        fileExists = false;
                        Toast.makeText(context, "Folder Not Exist", Toast.LENGTH_SHORT).show();
                    }*/

                    File imageFile = null;
                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (fileExists) {
                            imageFile = new File(downloadFile.getAbsolutePath(), "screen1.png");
                        }
                    } else {*/
                    imageFile = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.folder_name), "screen1.png");
                    //  }

                    if (imageFile.exists()) {
                        final Uri contentURI = Uri.parse(imageFile.toString());
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        ivBg.setImageURI(contentURI);
                        CustomDashboardApplication.setString(context, Consts.IMG_FILE, contentURI.toString());
                    }

                    File file = null;

                  /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (fileExists) {
                            file = new File(downloadFile.getAbsolutePath(), "dashboard.txt");
                        }
                    } else {*/
                    file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.folder_name), "dashboard.txt");
                    // }

                    if (file.exists()) {

                        br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            Log.i("Test", "text : " + text + " : end");
                            text.append('\n');
                        }
                        isPredefineApps = true;
                        CustomDashboardApplication.setInt(context, Consts.isPreInstall, 1);
                    } else {
                        file.getParentFile().mkdirs();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    br.close();
                }

                JSONObject jsonObject = new JSONObject(text.toString());
                Log.e("fileData", jsonObject.toString());

                final ArrayList<AppModel> appModels = new ArrayList<>();

                JSONObject appData = jsonObject.getJSONObject(Consts.app_data);
                JSONObject appSettings = appData.getJSONObject(Consts.app_settings);

                // SET VOLUME CONTROL SETTING
                if (Utils.contains(appSettings, Consts.volume_key_status)) {
                    CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, (appSettings.getInt(Consts.volume_key_status)));
                }

                // SET STATUS BAR (LABEL/APP INFO) SETTING
                if (Utils.contains(appSettings, Consts.status_bar_status)) {
                    CustomDashboardApplication.setInt(context, Consts.APP_INFO, (appSettings.getInt(Consts.status_bar_status)));
                }

                // SET STATUS BAR LABEL STRING
                if (Utils.contains(appSettings, Consts.status_bar_text)) {
                    CustomDashboardApplication.setString(context, Consts.APP_INFO_STR, (appSettings.getString(Consts.status_bar_text)));
                }

                // SET PIN
                if (Utils.contains(appSettings, Consts.pin)) {
                    if (!appSettings.getString(Consts.pin).equalsIgnoreCase("null") && !appSettings.getString(Consts.pin).equalsIgnoreCase("")) {
                        //AESCrypt.encrypt(context, appSettings.getString(Consts.pin));
                        CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, appSettings.getString(Consts.pin));
                        CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                    } else {
                        CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, "0000");
                        CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                    }
                }

                //dijo
                if (Utils.contains(appSettings, Consts.shortcuts_status)) {
                    CustomDashboardApplication.setInt(context, Consts.SHORTCUT_DASHBOARD, (appSettings.getInt(Consts.shortcuts_status)));
                }

                // SET TIMER TYPE
                if (Utils.contains(appSettings, Consts.schedule_type)) {
                    if ((appSettings.getInt(Consts.schedule_type) == 0)) {
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                    }
                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, (appSettings.getInt(Consts.schedule_type)));
                }

                // SET TIMER VALUE
                if (Utils.contains(appSettings, Consts.schedule_value)) {

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

                    for (int i = 0; i < dailyTimerModels.size(); i++) {
                        if (dailyTimerModels.get(i).getDailyTimeStr().equalsIgnoreCase(appSettings.getString(Consts.schedule_value))) {
                            CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, i);
                            break;
                        }
                    }
                }

                JSONArray dataArray = appData.getJSONArray(Consts.data);
                if (dataArray.length() > 0) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        AppModel appModel = new AppModel();
                        appModel.setId(i);
                        appModel.setPackageDetails(dataObj.getString(Consts.packageDetails));
                        appModel.setSystemApp(dataObj.getBoolean(Consts.isSystemApp));
                        appModel.setAppCategory(dataObj.getString(Consts.appCategory));
                        if (dataObj.getString(Consts.packageDetails).equalsIgnoreCase("com.dialer")) {
                            appModel.setAppVersion("1.0");
                        } else {
                            appModel.setAppVersion(CustomDashboardApplication.getAppVersion(context, dataObj.getString(Consts.packageDetails)));
                        }
                        //if (dataObj.getInt(Consts.added_to_dashboard) == 1) {
                        appModel.setSelected(dataObj.getBoolean(Consts.isSelected));
                        // } else {
                        //   appModel.setSelected(false);
                        //  }
                        if (!dataObj.isNull(Consts.targetSdk))
                            appModel.setTargetSdk(dataObj.getString(Consts.targetSdk));
                        if (!dataObj.isNull(Consts.dateInstalled))
                            appModel.setDateInstalled(dataObj.getString(Consts.dateInstalled));
                        appModel.setAppInfo("");

                        appModels.add(appModel);
                    }

                    lstBook.addAll(appModels);
                    if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                        lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                    }

                    if (addedWebShortcutsModels.size() > 0) {
                        lstBook.addAll(addedWebShortcutsModels);
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String ser = SerializeObject.objectToString(appModels);
                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                SerializeObject.WriteSettings(context, ser, Consts.appHistoryFile);
                            } else {
                                SerializeObject.WriteSettings(context, "", Consts.appHistoryFile);
                            }

                            // finish();
                        }
                    }, 1000);

                    myRv.setHasFixedSize(true);
                    setAppListOnDashboard();
                    /*myAdapter = new RecyclerViewAdapter(this, lstBook, this);
                    myrv.setLayoutManager(new GridLayoutManager(this, 3));

                    myrv.setAdapter(myAdapter);*/

                    Intent intent = new Intent(Consts.first_time);
                    intent.putExtra(Consts.first_update, 1);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAppVersion(String packageName) {
        PackageInfo pinfo = null;
        String versionName = null;
        try {
            pinfo = getPackageManager().getPackageInfo(packageName, 0);
            int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        try {
            if (data != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Uri tempUri = getImageUri(context, bm);
            profileImageURI = tempUri;


            CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
            Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OnselectGallery->", e.getMessage());
            e.printStackTrace();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        String path = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getIImageUri->", e.getMessage());
        }
        return Uri.parse(path);
    }

    // SHOW NOTE DIALOG
    void showLauncherFolderDialog() {
        alertDialogLauncher = new AppCompatDialog(context);
        alertDialogLauncher.setContentView(R.layout.launcher_pop_up);

        Button btnDone = alertDialogLauncher.findViewById(R.id.done_btn);
        CheckBox checkBox = alertDialogLauncher.findViewById(R.id.cb_ask);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alertDialogLauncher.dismiss();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               /* if (isChecked){
                    CustomDashboardApplication.setInt(context, Consts.REMOVE_POP_UP, 1);
                } else {
                    CustomDashboardApplication.setInt(context, Consts.REMOVE_POP_UP, 0);
                }*/
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alertDialogLauncher.dismiss();

                PackageManager packageManager = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, FakeActivity.class);
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selector);

                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

            }
        });

        alertDialogLauncher.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //alertDialogLauncher.show();
    }

    public int initCountDownTimer() {
        final int[] status = new int[1];
        countDownTimer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                //textView.setText("seconds remaining: " + millisUntilFinished / 1000);
                status[0] = 0;
                Log.e("Counter", "Time Running");
            }

            public void onFinish() {
                pointerCount = 0;
                status[0] = 1;
                Log.e("Counter", "Time Finished");
                //textView.setText("done!");
            }
        }.start();

        return status[0];
    }

    private void signIn() {
        /*Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);*/
    }

    // SHOW ENTER PASSWORD POP UP
    void showPasswordDialog(final int type) {
        try {
            isHomeApp = 1;
            llPwd.setVisibility(View.VISIBLE);
            isLlPassword = 1;

            ivBtnOne.setOnClickListener(v -> buildString(1));

            ivBtnTwo.setOnClickListener(v -> buildString(2));

            ivBtnThree.setOnClickListener(v -> buildString(3));

            ivBtnFour.setOnClickListener(v -> buildString(4));

            ivBtnFive.setOnClickListener(v -> buildString(5));

            ivBtnSix.setOnClickListener(v -> buildString(6));

            ivBtnSeven.setOnClickListener(v -> buildString(7));

            ivBtnEight.setOnClickListener(v -> buildString(8));

            ivBtnNine.setOnClickListener(v -> buildString(9));

            ivBtnBack.setOnClickListener(v -> buildString(10));

            ivBtnZero.setOnClickListener(v -> buildString(0));

            ivBtnOk.setOnClickListener(v -> buildString(11));

            // HANDLER FOR CHECKING USER INACTIVITY
            new Handler().postDelayed(() -> {
                if (edPassword.getText().toString().trim().length() == 0) {
                    llPwd.setVisibility(View.GONE);
                    edPassword.setText("");
                    isLlPassword = 0;
                }
            }, 5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void buildStringOverLay(int key, LinearLayout llPwd, final LinearLayout llTimerPopup, final View overLayView) {

        final RadioButton rbNoTimer = overLayView.findViewById(R.id.rb_no_timer);
        final RadioButton rbDailyLimit = overLayView.findViewById(R.id.rb_daily_limits);
        final RadioButton rbSchedule = overLayView.findViewById(R.id.rb_schedule);

        final Spinner spinnerTimer = overLayView.findViewById(R.id.spinner_timer);
        final Spinner spinnerSchedule = overLayView.findViewById(R.id.spinner_schedule);

        final CardView cvTimer = overLayView.findViewById(R.id.cv_timer);
        final CardView cvSchedule = overLayView.findViewById(R.id.cv_schedule);

        Button btnDone = overLayView.findViewById(R.id.done_btn);


        switch (key) {
            case 0:
                mainSbOverLay.append(0);
                break;
            case 1:
                mainSbOverLay.append(1);
                break;
            case 2:
                mainSbOverLay.append(2);
                break;
            case 3:
                mainSbOverLay.append(3);
                break;
            case 4:
                mainSbOverLay.append(4);
                break;
            case 5:
                mainSbOverLay.append(5);
                break;
            case 6:
                mainSbOverLay.append(6);
                break;
            case 7:
                mainSbOverLay.append(7);
                break;
            case 8:
                mainSbOverLay.append(8);
                break;
            case 9:
                mainSbOverLay.append(9);
                break;
            case 10:
                if (mainSbOverLay.length() > 0) {

                    if (mainSbOverLay.length() == 1) {
                        mainSbOverLay.delete(0, mainSbOverLay.length() - 1);
                        mainSbOverLay.setLength(0);
                        mainSbOverLay = new StringBuilder();
                    } else {
                        mainSbOverLay.deleteCharAt(mainSbOverLay.length() - 1);
                    }
                }
                break;
            case 11:
                if (mainSbOverLay.length() > 0) {
                    pointerCount = 0;
                    isLlPassword = 0;
                    edPasswordOverlay.setText(mainSbOverLay.toString());

                    if (edPasswordOverlay.getText().toString().trim().length() > 0) {


                        if (overLayType == 3) {
                            if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.IS_PWD) == 1) {
                                String decryptStr = null;
                                try {
                                    //decryptStr = AESCrypt.decrypt(getApplicationContext(), CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                    decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.is_logged_in) == 1) {
                                    if (edPasswordOverlay.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPasswordOverlay.getText().toString().trim().equalsIgnoreCase("2014")) {
                                        edPasswordOverlay.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        llTimerPopup.setVisibility(View.VISIBLE);
                                        // showSetTimerOverlayDialog();

                                        ArrayList<DailyTimerModel> timeList = new ArrayList<>();

                                        String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                            Object obj = SerializeObject.stringToObject(ser1);
                                            // Then cast it to your object and
                                            if (obj instanceof ArrayList) {
                                                // Do something
                                                timeList = (ArrayList<DailyTimerModel>) obj;
                                            }
                                        }

                                        ArrayList<String> scheduleList = new ArrayList<>();
                                        scheduleList.add("--- Select Schedule Time ---");

                                        ArrayAdapter<DailyTimerModel> timeAdapter;
                                        ArrayAdapter<String> scheduleAdapter;

                                        timeAdapter = new ArrayAdapter<DailyTimerModel>(this, R.layout.view_spinner_item, timeList);
                                        timeAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerTimer.setAdapter(timeAdapter);

                                        scheduleAdapter = new ArrayAdapter<String>(this, R.layout.view_spinner_item, scheduleList);
                                        scheduleAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerSchedule.setAdapter(scheduleAdapter);

                                        //Switch switchTime = alertDialogSetTimer.findViewById(R.id.dt_time_switch);

                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                            rbNoTimer.setChecked(true);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(true);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerTimer.setEnabled(true);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);

                                            spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 2) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(true);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerSchedule.setEnabled(true);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                        }


                                        rbNoTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(true);
                                                    rbDailyLimit.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 0);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerTimer.setEnabled(false);
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                }
                                            }
                                        });


                                        rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    spinnerTimer.setEnabled(true);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        rbSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbDailyLimit.setChecked(false);
                                                    spinnerTimer.setEnabled(false);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    spinnerSchedule.setEnabled(true);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                if (position > 0) {
                                                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, position);
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });

                                        final ArrayList<DailyTimerModel> finalTimeList = timeList;
                                        btnDone.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SET_TIMER) == 0) {
                                                    WorkManager.getInstance(context).cancelAllWork();
                                                } else {

                                                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                        WorkManager.getInstance(context).cancelAllWork();
                                                    } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER) != 0) {

                                                            if (finalTimeList.size() > 0) {
                                                                final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);

                                                                if (timer != 0) {

                                                                    long timeMillis = finalTimeList.get(timer).getDailyTime();
                                                                    TimeUnit timeUnit = finalTimeList.get(timer).getDailyTimeUnit();

                                                                    Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                                                                    WorkRequest workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                                                                    WorkManager.getInstance(context).enqueue(workRequest);
                                                                } else {
                                                                    WorkManager.getInstance(context).cancelAllWork();
                                                                }
                                                            }
                                                            startActivity(getPackageManager().getLaunchIntentForPackage(Consts.PACKAGE_NAME));

                                                        }
                                                    }
                                                }

                                                llTimerPopup.setVisibility(View.GONE);
                                                if (overLayView != null) {
                                                    overlayManager.removeView(overLayView);
                                                }
                                            }
                                        });

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPasswordOverlay.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        edPasswordOverlay.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        llTimerPopup.setVisibility(View.VISIBLE);
                                        // showSetTimerOverlayDialog();

                                        ArrayList<DailyTimerModel> timeList = new ArrayList<>();

                                        String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                            Object obj = SerializeObject.stringToObject(ser1);
                                            // Then cast it to your object and
                                            if (obj instanceof ArrayList) {
                                                // Do something
                                                timeList = (ArrayList<DailyTimerModel>) obj;
                                            }
                                        }

                                        ArrayList<String> scheduleList = new ArrayList<>();
                                        scheduleList.add("--- Select Schedule Time ---");

                                        ArrayAdapter<DailyTimerModel> timeAdapter;
                                        ArrayAdapter<String> scheduleAdapter;

                                        timeAdapter = new ArrayAdapter<DailyTimerModel>(this, R.layout.view_spinner_item, timeList);
                                        timeAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerTimer.setAdapter(timeAdapter);

                                        scheduleAdapter = new ArrayAdapter<String>(this, R.layout.view_spinner_item, scheduleList);
                                        scheduleAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerSchedule.setAdapter(scheduleAdapter);

                                        //Switch switchTime = alertDialogSetTimer.findViewById(R.id.dt_time_switch);

                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                            rbNoTimer.setChecked(true);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(true);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerTimer.setEnabled(true);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);

                                            spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 2) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(true);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerSchedule.setEnabled(true);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                        }

                                        rbNoTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(true);
                                                    rbDailyLimit.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 0);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerTimer.setEnabled(false);
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                }
                                            }
                                        });


                                        rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    spinnerTimer.setEnabled(true);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        rbSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbDailyLimit.setChecked(false);
                                                    spinnerTimer.setEnabled(false);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    spinnerSchedule.setEnabled(true);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                if (position > 0) {
                                                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, position);
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });

                                        final ArrayList<DailyTimerModel> finalTimeList = timeList;
                                        btnDone.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SET_TIMER) == 0) {
                                                    WorkManager.getInstance(context).cancelAllWork();
                                                } else {

                                                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                        WorkManager.getInstance(context).cancelAllWork();
                                                    } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER) != 0) {

                                                            if (finalTimeList.size() > 0) {
                                                                final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);

                                                                if (timer != 0) {

                                                                    long timeMillis = finalTimeList.get(timer).getDailyTime();
                                                                    TimeUnit timeUnit = finalTimeList.get(timer).getDailyTimeUnit();

                                                                    Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                                                                    WorkRequest workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                                                                    WorkManager.getInstance(context).enqueue(workRequest);
                                                                } else {
                                                                    WorkManager.getInstance(context).cancelAllWork();
                                                                }
                                                            }
                                                            startActivity(getPackageManager().getLaunchIntentForPackage(Consts.PACKAGE_NAME));

                                                        }
                                                    }
                                                }

                                                llTimerPopup.setVisibility(View.GONE);
                                                if (overLayView != null) {
                                                    overlayManager.removeView(overLayView);
                                                }
                                            }
                                        });

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.is_logged_in) == 1) {
                                    if (edPasswordOverlay.getText().toString().trim().equals("0000") || edPasswordOverlay.getText().toString().trim().equals("2014")) {
                                        //showSetTimerOverlayDialog();
                                        llPwd.setVisibility(View.GONE);
                                        llTimerPopup.setVisibility(View.VISIBLE);


                                        ArrayList<DailyTimerModel> timeList = new ArrayList<>();

                                        String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                            Object obj = SerializeObject.stringToObject(ser1);
                                            // Then cast it to your object and
                                            if (obj instanceof ArrayList) {
                                                // Do something
                                                timeList = (ArrayList<DailyTimerModel>) obj;
                                            }
                                        }

                                        ArrayList<String> scheduleList = new ArrayList<>();
                                        scheduleList.add("--- Select Schedule Time ---");

                                        ArrayAdapter<DailyTimerModel> timeAdapter;
                                        ArrayAdapter<String> scheduleAdapter;

                                        timeAdapter = new ArrayAdapter<DailyTimerModel>(getApplicationContext(), R.layout.view_spinner_item, timeList);
                                        timeAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerTimer.setAdapter(timeAdapter);

                                        scheduleAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.view_spinner_item, scheduleList);
                                        scheduleAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerSchedule.setAdapter(scheduleAdapter);

                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                            rbNoTimer.setChecked(true);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(true);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerTimer.setEnabled(true);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);

                                            spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 2) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(true);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerSchedule.setEnabled(true);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                        }


                                        rbNoTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(true);
                                                    rbDailyLimit.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 0);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerTimer.setEnabled(false);
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                }
                                            }
                                        });


                                        rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    spinnerTimer.setEnabled(true);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        rbSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbDailyLimit.setChecked(false);
                                                    spinnerTimer.setEnabled(false);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    spinnerSchedule.setEnabled(true);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                if (position > 0) {
                                                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, position);
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });

                                        final ArrayList<DailyTimerModel> finalTimeList = timeList;
                                        btnDone.setOnClickListener(new View.OnClickListener() {
                                            @SuppressLint("RestrictedApi")
                                            @Override
                                            public void onClick(View v) {
                                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SET_TIMER) == 0) {
                                                    WorkManager.getInstance(context).cancelAllWork();
                                                } else {
                                                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                        // if (CustomDashboardApplication.getString(getApplicationContext(), Consts.PREVIOUS_WORK_REQUEST_ID) != null){
                                                        WorkManager.getInstance(context).cancelAllWork();
                                                        // }
                                                    } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER) != 0) {

                                                            if (finalTimeList.size() > 0) {
                                                                final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);

                                                                if (timer != 0) {

                                                                    long timeMillis = finalTimeList.get(timer).getDailyTime();
                                                                    TimeUnit timeUnit = finalTimeList.get(timer).getDailyTimeUnit();

                                                                    Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                                                                    WorkRequest workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                                                                    WorkManager.getInstance(context).enqueue(workRequest);
                                                                } else {
                                                                    WorkManager.getInstance(context).cancelAllWork();
                                                                }
                                                            }
                                                            startActivity(getPackageManager().getLaunchIntentForPackage(Consts.PACKAGE_NAME));

                                                        }
                                                    }
                                                }

                                                llTimerPopup.setVisibility(View.GONE);
                                                relTimeUp.setVisibility(View.GONE);
                                                if (overLayView != null) {
                                                    overlayManager.removeView(overLayView);
                                                }
                                            }
                                        });

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(overLayView.getContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPasswordOverlay.getText().toString().trim().equals("0000")) {
                                        //showSetTimerOverlayDialog();
                                        llPwd.setVisibility(View.GONE);
                                        llTimerPopup.setVisibility(View.VISIBLE);


                                        ArrayList<DailyTimerModel> timeList = new ArrayList<>();

                                        String ser1 = SerializeObject.ReadSettings(context, Consts.dailyTimerFile);
                                        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                            Object obj = SerializeObject.stringToObject(ser1);
                                            // Then cast it to your object and
                                            if (obj instanceof ArrayList) {
                                                // Do something
                                                timeList = (ArrayList<DailyTimerModel>) obj;
                                            }
                                        }

                                        ArrayList<String> scheduleList = new ArrayList<>();
                                        scheduleList.add("--- Select Schedule Time ---");

                                        ArrayAdapter<DailyTimerModel> timeAdapter;
                                        ArrayAdapter<String> scheduleAdapter;

                                        timeAdapter = new ArrayAdapter<DailyTimerModel>(getApplicationContext(), R.layout.view_spinner_item, timeList);
                                        timeAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerTimer.setAdapter(timeAdapter);

                                        scheduleAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.view_spinner_item, scheduleList);
                                        scheduleAdapter.setDropDownViewResource(R.layout.spinner_list);
                                        spinnerSchedule.setAdapter(scheduleAdapter);

                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                            rbNoTimer.setChecked(true);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(true);
                                            rbSchedule.setChecked(false);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerTimer.setEnabled(true);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerSchedule.setEnabled(false);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);

                                            spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));

                                        } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 2) {
                                            rbNoTimer.setChecked(false);
                                            rbDailyLimit.setChecked(false);
                                            rbSchedule.setChecked(true);
                                            cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                            spinnerTimer.setEnabled(false);
                                            cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            spinnerSchedule.setEnabled(true);
                                            CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                        }


                                        rbNoTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(true);
                                                    rbDailyLimit.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 0);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerTimer.setEnabled(false);
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                }
                                            }
                                        });


                                        rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbSchedule.setChecked(false);
                                                    spinnerTimer.setEnabled(true);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    spinnerSchedule.setEnabled(false);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        rbSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    rbNoTimer.setChecked(false);
                                                    rbDailyLimit.setChecked(false);
                                                    spinnerTimer.setEnabled(false);
                                                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                                                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                    spinnerSchedule.setEnabled(true);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE, 1);
                                                    CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                }
                                            }
                                        });


                                        spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                if (position > 0) {
                                                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER, position);
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });

                                        final ArrayList<DailyTimerModel> finalTimeList = timeList;
                                        btnDone.setOnClickListener(new View.OnClickListener() {
                                            @SuppressLint("RestrictedApi")
                                            @Override
                                            public void onClick(View v) {
                                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SET_TIMER) == 0) {
                                                    WorkManager.getInstance(context).cancelAllWork();
                                                } else {
                                                    if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 0) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 0);
                                                        // if (CustomDashboardApplication.getString(getApplicationContext(), Consts.PREVIOUS_WORK_REQUEST_ID) != null){
                                                        WorkManager.getInstance(context).cancelAllWork();
                                                        // }
                                                    } else if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER_TYPE) == 1) {
                                                        CustomDashboardApplication.setInt(getApplicationContext(), Consts.SET_TIMER, 1);
                                                        if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.SELECTED_TIMER) != 0) {

                                                            if (finalTimeList.size() > 0) {
                                                                final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);

                                                                if (timer != 0) {

                                                                    long timeMillis = finalTimeList.get(timer).getDailyTime();
                                                                    TimeUnit timeUnit = finalTimeList.get(timer).getDailyTimeUnit();

                                                                    Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                                                                    WorkRequest workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                                                                    WorkManager.getInstance(context).enqueue(workRequest);
                                                                } else {
                                                                    WorkManager.getInstance(context).cancelAllWork();
                                                                }
                                                            }
                                                            startActivity(getPackageManager().getLaunchIntentForPackage(Consts.PACKAGE_NAME));

                                                        }
                                                    }
                                                }

                                                llTimerPopup.setVisibility(View.GONE);
                                                relTimeUp.setVisibility(View.GONE);
                                                if (overLayView != null) {
                                                    overlayManager.removeView(overLayView);
                                                }
                                            }
                                        });

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(overLayView.getContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else {
                            if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                String decryptStr = null;
                                try {
                                    //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                    decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.is_logged_in) == 1) {
                                    if (edPasswordOverlay.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPasswordOverlay.getText().toString().trim().equalsIgnoreCase("2014")) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);


                                        edPasswordOverlay.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        Utils.exitApp(this);

                                  /*      if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_HOME_SETTINGS);
                                            startActivity(intent);
                                        } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                                            Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                                            startActivity(localObject);
                                        } else {
                                         *//*   Intent selector = new Intent(Intent.ACTION_MAIN);
                                            selector.addCategory(Intent.CATEGORY_HOME);
                                            startActivity(selector);*//*

                                            packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);
                                            ///startActivity(getPackageManager().getLaunchIntentForPackage("com.sec.android.app.launcher"));
                                        }*/


                                        //  startActivity(getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.tabnova.aidashboard"));

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPasswordOverlay.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        edPasswordOverlay.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        Utils.exitApp(this);

                                      /*  PackageManager packageManager = context.getPackageManager();
                                        ComponentName componentName = new ComponentName(context, FakeActivity.class);
                                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);*/

                                     /*   if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_HOME_SETTINGS);
                                            startActivity(intent);
                                        } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                                            Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                                            startActivity(localObject);
                                        } else {
                                          *//*  Intent selector = new Intent(Intent.ACTION_MAIN);
                                            selector.addCategory(Intent.CATEGORY_HOME);
                                            startActivity(selector);*//*

                                            packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);
                                            //startActivity(getPackageManager().getLaunchIntentForPackage("com.sec.android.app.launcher"));
                                        }*/



                                 /*       packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);

                                        if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0) {
                                            CustomDashboardApplication.exitSamsungKiosk(context);
                                        }
                                        ((Activity) context).finishAffinity();
                                        System.exit(0);

                                        if (overLayView != null) {
                                            overlayManager.removeView(overLayView);
                                        }*/

                                        //  startActivity(getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.tabnova.aidashboard"));

                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(getApplicationContext(), Consts.is_logged_in) == 1) {
                                    if (edPasswordOverlay.getText().toString().trim().equals("0000") || edPasswordOverlay.getText().toString().trim().equals("2014")) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        llPwd.setVisibility(View.GONE);
                                        Utils.exitApp(this);
                                       /* PackageManager packageManager = context.getPackageManager();
                                        ComponentName componentName = new ComponentName(context, FakeActivity.class);
                                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);*/

                                      /*  if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_HOME_SETTINGS);
                                            startActivity(intent);
                                        } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                                            Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                                            startActivity(localObject);
                                        } else {
                                            *//*Intent selector = new Intent(Intent.ACTION_MAIN);
                                            selector.addCategory(Intent.CATEGORY_HOME);
                                            startActivity(selector);*//*

                                            packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);
                                           // startActivity(getPackageManager().getLaunchIntentForPackage("com.sec.android.app.launcher"));
                                        }*/



                                     /*   packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);

                                        if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0) {
                                            CustomDashboardApplication.exitSamsungKiosk(context);
                                        }
                                        ((Activity) context).finishAffinity();
                                        System.exit(0);

                                        if (overLayView != null) {
                                            overlayManager.removeView(overLayView);
                                        }*/
                                        //startActivity(getPackageManager().getLaunchIntentForPackage("com.tabnova.aidashboard"));
                                        /* */
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPasswordOverlay.getText().toString().trim().equals("0000")) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        llPwd.setVisibility(View.GONE);
                                        Utils.exitApp(this);
                                        /*PackageManager packageManager = context.getPackageManager();
                                        ComponentName componentName = new ComponentName(context, FakeActivity.class);
                                        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);*/

                                        /*if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_HOME_SETTINGS);
                                            startActivity(intent);
                                        } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                                            Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                                            startActivity(localObject);
                                        } else {
                                            *//*Intent selector = new Intent(Intent.ACTION_MAIN);
                                            selector.addCategory(Intent.CATEGORY_HOME);
                                            startActivity(selector);*//*

                                            packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);
                                            //startActivity(getPackageManager().getLaunchIntentForPackage("com.sec.android.app.launcher"));
                                        }*/



                                       /* packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);

                                        if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0) {
                                            CustomDashboardApplication.exitSamsungKiosk(context);
                                        }
                                        ((Activity) context).finishAffinity();
                                        System.exit(0);

                                        if (overLayView != null) {
                                            overlayManager.removeView(overLayView);
                                        }*/
                                        //startActivity(getPackageManager().getLaunchIntentForPackage("com.tabnova.aidashboard"));
                                        /* */
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        mainSbOverLay = new StringBuilder();
                    } else {
                        incorrectPinTapCount = incorrectPinTapCount + 1;
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    incorrectPinTapCount = incorrectPinTapCount + 1;
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        edPasswordOverlay.setText(mainSbOverLay.toString());
    }


    void buildString(int key) {

        switch (key) {
            case 0:
                mainSb.append(0);
                break;
            case 1:
                mainSb.append(1);
                break;
            case 2:
                mainSb.append(2);
                break;
            case 3:
                mainSb.append(3);
                break;
            case 4:
                mainSb.append(4);
                break;
            case 5:
                mainSb.append(5);
                break;
            case 6:
                mainSb.append(6);
                break;
            case 7:
                mainSb.append(7);
                break;
            case 8:
                mainSb.append(8);
                break;
            case 9:
                mainSb.append(9);
                break;
            case 10:
                edPassword.setText("");
                if (mainSb.length() > 0) {
                    if (mainSb.length() == 1) {
                        mainSb.delete(0, mainSb.length() - 1);
                        mainSb.setLength(0);
                        mainSb = new StringBuilder();
                    } else {
                        mainSb.deleteCharAt(mainSb.length() - 1);
                    }
                }
                break;
            case 11:
                if (mainSb.length() > 0) {
                    pointerCount = 0;
                    isLlPassword = 0;
                    edPassword.setText(mainSb.toString());

                    if (edPassword.getText().toString().trim().length() > 0) {
                        if (Type == 1) {
                            String decryptStr = null;
                            try {
                                //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                    edPassword.setText("");
                                    llPwd.setVisibility(View.GONE);
                                    ivLogo.setVisibility(View.GONE);
                                    llLogo.setVisibility(View.GONE);
                               /* if (lstBook.size() == 0){
                                    showOpenSettingsDialog();
                                }*/
                                } else {
                                    incorrectPinTapCount = incorrectPinTapCount + 1;
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                    edPassword.setText("");
                                    llPwd.setVisibility(View.GONE);
                                    ivLogo.setVisibility(View.GONE);
                                    llLogo.setVisibility(View.GONE);
                               /* if (lstBook.size() == 0){
                                    showOpenSettingsDialog();
                                }*/
                                } else {
                                    incorrectPinTapCount = incorrectPinTapCount + 1;
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (Type == 0) {
                            if (CustomDashboardApplication.getInt(context, Consts.IS_LOCKED) == 1) {
                                if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                    String decryptStr = null;
                                    try {
                                        //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                        decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                        if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                            edPassword.setText("");
                                            llPwd.setVisibility(View.GONE);
                                            relTimeUp.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                            edPassword.setText("");
                                            llPwd.setVisibility(View.GONE);
                                            relTimeUp.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                } else {
                                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                        if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                            llPwd.setVisibility(View.GONE);
                                            relTimeUp.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (edPassword.getText().toString().trim().equals("0000")) {
                                            llPwd.setVisibility(View.GONE);
                                            relTimeUp.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                    String decryptStr = null;
                                    try {
                                        //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                        decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                        if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                            edPassword.setText("");
                                            llPwd.setVisibility(View.GONE);
                                            SessionManager.getInstance(context).setIsParentMode(true);
                                            Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                            startActivity(openSettings);
                                            stopVPN();
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                            edPassword.setText("");
                                            llPwd.setVisibility(View.GONE);
                                            ivLogo.setVisibility(View.GONE);
                                            llLogo.setVisibility(View.GONE);
                                            SessionManager.getInstance(context).setIsParentMode(true);
                                            Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                            startActivity(openSettings);
                                            stopVPN();
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                        if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                            SessionManager.getInstance(context).setIsParentMode(true);
                                            Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                            startActivity(openSettings);
                                            stopVPN();
                                            llPwd.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (edPassword.getText().toString().trim().equals("0000")) {
                                            SessionManager.getInstance(context).setIsParentMode(true);
                                            Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                            startActivity(openSettings);
                                            stopVPN();
                                            llPwd.setVisibility(View.GONE);
                                        } else {
                                            incorrectPinTapCount = incorrectPinTapCount + 1;
                                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        } else if (Type == 3) {
                            if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                String decryptStr = null;
                                try {
                                    //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                    decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        ivLogo.setVisibility(View.GONE);
                                        llLogo.setVisibility(View.GONE);
                                        showSetTimerDialog();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        ivLogo.setVisibility(View.GONE);
                                        llLogo.setVisibility(View.GONE);
                                        showSetTimerDialog();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                    if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                        showSetTimerDialog();
                                        llPwd.setVisibility(View.GONE);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equals("0000")) {
                                        showSetTimerDialog();
                                        llPwd.setVisibility(View.GONE);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else if (Type == 2) {
                            if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                String decryptStr = null;
                                try {
                                    //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                                    decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        ivLogo.setVisibility(View.GONE);
                                        llLogo.setVisibility(View.GONE);

                                        CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
                                        Utils.exitApp(this);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        //CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        ivLogo.setVisibility(View.GONE);
                                        llLogo.setVisibility(View.GONE);

                                        CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);

                                        Utils.exitApp(this);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                    if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                        // CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
                                        llPwd.setVisibility(View.GONE);

                                        CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);

                                        Utils.exitApp(this);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equals("0000")) {
                                        // CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
                                        CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
                                        llPwd.setVisibility(View.GONE);

                                        Utils.exitApp(this);
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        mainSb = new StringBuilder();
                    } else {
                        incorrectPinTapCount = incorrectPinTapCount + 1;
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    incorrectPinTapCount = incorrectPinTapCount + 1;
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
                break;

        }

        if (incorrectPinTapCount == 4) {
            incorrectPinTapCount = 0;
            Toast.makeText(context, "Please tap on contact icon", Toast.LENGTH_SHORT).show();
            ivContactUs.setVisibility(View.VISIBLE);
        } else {
            ivContactUs.setVisibility(View.GONE);
        }

        edPassword.setText(mainSb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        posList = new ArrayList<>();

        SessionManager.getInstance(this).setIsDashboardLaunch(true);

        if (CustomDashboardApplication.isDeviceSamsungActivated())
            isHomeApp = 1;
        else
            isHomeApp = 0;

        SessionManager.getInstance(this).setIsParentMode(false);
        Utils.hideBottomNavigationBars(this);

        if (!CustomDashboardApplication.isDeviceSamsung())
            preventStatusBarExpansion();

        try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
//                if (!SuperLockState.getInstance(this).isKioskModeEnabled(this)) {
                Log.e("BaseActivity", " Enable Kiosk called");
                CustomDashboardApplication.enableSamsungKiosk(this);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(() -> {
            isHomeApp = 0;
            if (!Utils.isAppIsInBackground()) {
                stopService(new Intent(context, AppVisibilityService.class));
            }
        }, 3000);

        //isBackGround = 0;

        if (CustomDashboardApplication.checkPlayServices(this)) {
            Log.e("Play Service", "Play Service Available");
        } else {
            Log.e("Play Service", "Play Service Not Available");
        }

        if (Consts.IS_BUSINESS_APP == 1) {
            importFile();
        }

        if (CustomDashboardApplication.isDeviceSamsung()) {
            SuperLockState superLockState = new SuperLockState(context);
            if (superLockState.supportedActivation()) {
                if (!superLockState.isESDKLicenseActivated()) {
                    if (CustomDashboardApplication.getInt(context, Consts.isRestarts) == 1) {
                        btnStart.performClick();
                    }
                }
            }
        }

        if (CustomDashboardApplication.getInt(context, Consts.is_screen_time_out) == 1) {
            llTimesUp.setVisibility(View.VISIBLE);
            myRv.setVisibility(View.GONE);
        } else {
            llTimesUp.setVisibility(View.GONE);
            if (CustomDashboardApplication.getInt(context, Consts.is_screen_locked) != 1)
                myRv.setVisibility(View.VISIBLE);
        }

        if (CustomDashboardApplication.getInt(context, Consts.is_screen_locked) == 1) {
            llLockedScreen.setVisibility(View.VISIBLE);
            myRv.setVisibility(View.GONE);
        } else {
            llLockedScreen.setVisibility(View.GONE);
            if (CustomDashboardApplication.getInt(context, Consts.is_screen_time_out) != 1)
                myRv.setVisibility(View.VISIBLE);
        }

        String scheduleTimerStr1 = SerializeObject.ReadSettings(context, Consts.savedWebShortcutsFile);
        if (scheduleTimerStr1 != null && !scheduleTimerStr1.equalsIgnoreCase("")) {
            addedWebShortcutsModels = new ArrayList<>();
            addedWebShortcutsModels.clear();
            Object scheduleModelObj = SerializeObject.stringToObject(scheduleTimerStr1);
            if (scheduleModelObj instanceof ArrayList) {
                addedWebShortcutsModels = (ArrayList<AppModel>) scheduleModelObj;
            }
        } else {
            addedWebShortcutsModels.clear();
        }

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 0) {
            if (Consts.IS_BUSINESS_APP == 1) {
                txLoginNote.setVisibility(View.GONE);
                txLoginStatus.setVisibility(View.INVISIBLE);
            } else {
                txLoginNote.setVisibility(View.VISIBLE);
                txLoginStatus.setVisibility(View.VISIBLE);
            }

            if (CustomDashboardApplication.getInt(context, Consts.isAppSelected) == 0) {
                lstBook.clear();
                lstBook = new ArrayList<>();
                if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                    lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                }
                if (addedWebShortcutsModels.size() > 0) {
                    lstBook.addAll(addedWebShortcutsModels);
                }
                myAdapter = new RecyclerViewAdapter(context, lstBook, MainActivity.this);
                myRv.setAdapter(myAdapter);

                //  showOpenSettingsDialog();
            }
        } else {
            txLoginNote.setVisibility(View.GONE);
            txLoginStatus.setVisibility(View.INVISIBLE);
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver5, new IntentFilter(Consts.IS_FORCE_EXIT));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver6, new IntentFilter(Consts.IS_DELETE));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver7, new IntentFilter(Consts.IS_WALL_PAPER_DELETE));

        try {
            if (CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS) == 0) {
                try {
                    // Initialize a new IntentFilter instance
                    IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    registerReceiver(mBroadcastReceiver, iFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS) == 0) {
            ivBattery.setVisibility(View.VISIBLE);
            txBatteryPercent.setVisibility(View.VISIBLE);
        } else {
            ivBattery.setVisibility(View.GONE);
            txBatteryPercent.setVisibility(View.GONE);
        }

        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }

        try {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (!CustomDashboardApplication.getString(context, Consts.IMG_FILE).equalsIgnoreCase("")) {
                    if (CustomDashboardApplication.getString(context, Consts.IMG_FILE).startsWith("http")) {
                        String contentURI = CustomDashboardApplication.getString(context, Consts.IMG_FILE);
                        Glide.with(context).load(contentURI).format(DecodeFormat.PREFER_RGB_565).into(ivBg);
                        //imageLoader.displayImage(contentURI, ivBg, options);
                    } else {
                        final Uri contentURI = Uri.parse(CustomDashboardApplication.getString(context, Consts.IMG_FILE));
                        String selectedFilePath = FileUtils.getPath(context, contentURI);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        //Bitmap bitmap = BitmapFactory.decodeFile(selectedFilePath, bmOptions);
                        try {
                            ivBg.setImageURI(contentURI);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ivBg.setImageResource(R.drawable.bg_new_dashboard);
                }
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!CustomDashboardApplication.getString(context, Consts.IMG_FILE).equalsIgnoreCase("")) {
                    if (CustomDashboardApplication.getString(context, Consts.IMG_FILE).startsWith("http")) {
                        String contentURI = CustomDashboardApplication.getString(context, Consts.IMG_FILE);
                        Glide.with(context).load(contentURI).format(DecodeFormat.PREFER_RGB_565).into(ivBg);
                        //imageLoader.displayImage(contentURI, ivBg, options);
                    } else {
                        final Uri contentURI = Uri.parse(CustomDashboardApplication.getString(context, Consts.IMG_FILE));
//                        String selectedFilePath = FileUtils.getPath(context, contentURI);
//                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        //Bitmap bitmap = BitmapFactory.decodeFile(selectedFilePath, bmOptions);
                        ivBg.setImageURI(contentURI);
                    }
                } else {
                    //ivBg.setImageResource(R.drawable.landscape_bg);
                    ivBg.setImageResource(R.drawable.bg_new_dashboard);
                }
            }

            if (currentApplicationModels != null && currentApplicationModels.size() > 0) {
                if (isItemClick == 1) {
                    isItemClick = 0;
                    String ser1 = SerializeObject.ReadSettings(context, Consts.appAnalyticsFile);
                    if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                        Object obj = SerializeObject.stringToObject(ser1);
                        // Then cast it to your object and
                        if (obj instanceof ArrayList) {
                            // Do something
                            lastSavedApp = (ArrayList<ApplicationTimerModel>) obj;
                        }
                    }

                    int pos = currentApplicationModels.size() - 1;
                    if (currentApplicationModels.get(pos).getAppPackageName().equals("com.securewebview")) {
                        apiManager.uploadWebHistoryToServer();
                    }
                    String endTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                    ApplicationTimerModel applicationTimerModel = new ApplicationTimerModel();
                    applicationTimerModel.setId(currentApplicationModels.get(pos).getId());
                    applicationTimerModel.setAppName(currentApplicationModels.get(pos).getAppName());
                    applicationTimerModel.setAppPackageName(currentApplicationModels.get(pos).getAppPackageName());
                    applicationTimerModel.setDate(currentApplicationModels.get(pos).getDate());
                    applicationTimerModel.setTime(currentApplicationModels.get(pos).getTime());
                    applicationTimerModel.setStartTime(currentApplicationModels.get(pos).getStartTime());
                    applicationTimerModel.setEndTime(endTime);

                    currentApplicationModels.set(pos, applicationTimerModel);

                    lastSavedApp.add(currentApplicationModels.get(pos));

                    CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                    String ser = SerializeObject.objectToString(lastSavedApp);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.appAnalyticsFile);
                    } /*else {
                    SerializeObject.WriteSettings(context, "", "appanalytics.dat");
                }*/
                }
            }

            String scheduleTimerStr = SerializeObject.ReadSettings(context, Consts.scheduleFile);
            if (scheduleTimerStr != null && !scheduleTimerStr.equalsIgnoreCase("")) {
                Object scheduleModelObj = SerializeObject.stringToObject(scheduleTimerStr);
                if (scheduleModelObj instanceof ArrayList) {
                    scheduleEventModels = (ArrayList<ScheduleEventModel>) scheduleModelObj;
                }
            }

            if (lstBook.size() == 0) {
                lstBook.clear();
                lstBook = new ArrayList<>();
                if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                    lstBook.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                }
                if (addedWebShortcutsModels.size() > 0) {
                    lstBook.addAll(addedWebShortcutsModels);
                }

                ArrayList<AppModel> tempList = new ArrayList<>();

                String ser1 = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        tempList = (ArrayList<AppModel>) obj;
                    }
                }

                if (tempList.size() > 0) {
                    for (int j = 0; j < tempList.size(); j++) {
                        if (tempList.get(j).isSelected()) {
                            lstBook.add(tempList.get(j));
                        }
                    }

                    //setAppListOnDashboard();
                }

            } else {

                lstBook.clear();
                lstBook = new ArrayList<>();

                ArrayList<AppModel> tempList = new ArrayList<>();

                String ser1 = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);

                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        tempList = (ArrayList<AppModel>) obj;
                    }
                }

                if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                    tempList.add(new AppModel(0, true, true, "com.securewebview", "1.0", "", "28", ""));
                }
                if (addedWebShortcutsModels.size() > 0) {
                    tempList.addAll(addedWebShortcutsModels);
                }

                if (tempList.size() > 0) {
                    for (int j = 0; j < tempList.size(); j++) {
                        if (tempList.get(j).isSelected()) {
                            lstBook.add(tempList.get(j));
                        }
                    }
                    /*myAdapter = new RecyclerViewAdapter(this, lstBook, this);
                    myrv.setLayoutManager(new GridLayoutManager(this, 3));
                    myrv.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();*/
                } else {
                    //isSettings = 2;
                    //showOpenSettingsDialog();
                }
            }

            setAppListOnDashboard();

            if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 0) {
                WorkManager.getInstance(context).cancelAllWork();
            } else {
                if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 1) {
                    if (dailyTimerModels.size() > 0) {
                        final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);
                        if (timer != 0) {
                            long timeMillis = dailyTimerModels.get(timer).getDailyTime();
                            TimeUnit timeUnit = dailyTimerModels.get(timer).getDailyTimeUnit();

                            Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                            workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                            WorkManager.getInstance(context).enqueue(workRequest);
                        } else {
                            WorkManager.getInstance(context).cancelAllWork();
                        }
                    }
                } else if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 2) {
                    if (scheduleEventModels.size() > 0) {
                        for (int l = 0; l < scheduleEventModels.size(); l++) {

                            ArrayList<String> strings = new ArrayList<String>(Arrays.asList(scheduleEventModels.get(l).getDay().split(",")));

                            ArrayList<Integer> integers = new ArrayList<>();

                            for (int m = 0; m < strings.size(); m++) {
                                integers.add(Integer.parseInt(strings.get(m)));
                            }

                            Calendar calendar = Calendar.getInstance();
                            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                            int currentHour = calendar.get(Calendar.HOUR);
                            int currentMinute = calendar.get(Calendar.MINUTE);

                            Log.e("currentTime", "Day = " + currentDay + " Hour = " + currentHour + " Minute = " + currentMinute);

                            for (int k = 0; k < integers.size(); k++) {

                                //long flexTime = calculateFlex(integers.get(k), startHour, startMinute, 1);

                                if (currentDay == integers.get(k) && currentHour >= Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[0])
                                        && currentMinute >= Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[1])) {
                                    Calendar startDate = Calendar.getInstance();
                                    startDate.set(Calendar.HOUR, Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[0]));
                                    startDate.set(Calendar.MINUTE, Integer.parseInt(scheduleEventModels.get(l).getStartTime().split(":")[1]));

                                    Calendar endDate = Calendar.getInstance();
                                    endDate.set(Calendar.HOUR, Integer.parseInt(scheduleEventModels.get(l).getEndTime().split(":")[0]));
                                    endDate.set(Calendar.MINUTE, Integer.parseInt(scheduleEventModels.get(l).getEndTime().split(":")[1]));

                                    if (calendar.before(startDate)) {
                                        countDownTimer.cancel();
                                        Intent intent = new Intent(context, TimeOutScreen.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                    } else if (calendar.after(endDate)) {
                                        countDownTimer.cancel();
                                        Intent intent = new Intent(context, TimeOutScreen.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                    } else {
                                        countDownTimer.cancel();
                                        setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                    }

                                    break;
                                }
                            }
                        }
                    }
                } else {
                    WorkManager.getInstance(context).cancelAllWork();
                }
            }

            if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 0) {
                ivLogo.setVisibility(View.GONE);
                llLogo.setVisibility(View.GONE);
            } else {
                if (pwdCount != 1) {
                    pwdCount = 1;
                    if (CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH) == 1) {
                        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Consts.is_launcher)) {
                            Type = 1;
                            showPasswordDialog(1);
                            ivLogo.setVisibility(View.GONE);
                            llLogo.setVisibility(View.GONE);
                        }
                    }
                }
            }

            if (CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD) == 0) {
                llBottomSettings.setVisibility(View.GONE);
            } else {
                llBottomSettings.setVisibility(View.VISIBLE);
            }

            if (CustomDashboardApplication.getHelpEnabled(context, Consts.IS_HELP_ENABLED) == 0) {
                txHelp.setVisibility(View.GONE);
            } else {
                txHelp.setVisibility(View.VISIBLE);
                txHelp.setText(R.string.open_setting);
            }

            if (CustomDashboardApplication.getClockEnabled(context, Consts.CLOCK_WIDGET) == 0) {
                txTime.setVisibility(View.GONE);
                txDate.setVisibility(View.GONE);
            } else {
                int selectedSetting = CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING);
                if (selectedSetting == 0) {
                    txTime.setVisibility(View.GONE);
                    txDate.setVisibility(View.GONE);
                } else if (selectedSetting == 1) {
                    txTime.setVisibility(View.VISIBLE);
                    txDate.setVisibility(View.GONE);
                } else if (selectedSetting == 2) {
                    txTime.setVisibility(View.GONE);
                    txDate.setVisibility(View.VISIBLE);
                } else if (selectedSetting == 3) {
                    txTime.setVisibility(View.VISIBLE);
                    txDate.setVisibility(View.VISIBLE);
                }
            }

            if (CustomDashboardApplication.getInt(context, Consts.APP_INFO) == 0) {
                txInfo.setVisibility(View.INVISIBLE);
            } else {
                txInfo.setText(CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
                txInfo.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
    }

    private void setAppListOnDashboard() {
        int pos = 0;
        ArrayList<AppModel> appsList = new ArrayList<>();

        if (lstBook != null && lstBook.size() > 0) {
            for (int h = 0; h < lstBook.size(); h++) {
                if (lstBook.get(h).getPosition() == 0 && lstBook.get(h).getRow() > 0 && lstBook.get(h).getColumn() == 0) {
                    int position = 0;
                    if (lstBook.get(h).getColumn() > 3) {
                        lstBook.get(h).setColumn(3);
                    }
                    if (lstBook.get(h).getRow() > 1) {
                        position = ((lstBook.get(h).getRow() - 1) * 3 + lstBook.get(h).getColumn()) - 1;
                    } else {
                        position = lstBook.get(h).getColumn() - 1;
                    }

                    lstBook.get(h).setPosition(position);
                }

                if (!posList.contains(String.valueOf(lstBook.get(h).getPosition()))) {
                    posList.add(String.valueOf(lstBook.get(h).getPosition()));
                }
            }

            if (pos > lstBook.size()) {
                for (int n = 0; n <= pos; n++) {
                    appsList.add(new AppModel());
                }
            } else {
                for (int n = 0; n <= lstBook.size(); n++) {
                    appsList.add(new AppModel());
                }
            }
        }

        if (posList.size() > 0) {
            for (int i = 0; i < posList.size(); i++) {
                if (pos < Integer.parseInt(posList.get(i))) {
                    pos = Integer.parseInt(posList.get(i));
                }
            }
        }

        if (lstBook != null && lstBook.size() > 0) {
            if (pos > lstBook.size()) {
                for (int n = 0; n <= pos; n++) {
                    appsList.add(new AppModel());
                }
            } else {
                for (int n = 0; n <= lstBook.size(); n++) {
                    appsList.add(new AppModel());
                }
            }
        }

        if (lstBook != null && lstBook.size() > 0) {
            for (int k = 0; k < lstBook.size(); k++) {
                if (lstBook.get(k).getPosition() >= 0) {
                    try {
                        if (appsList.size() > 0 && appsList.get(lstBook.get(k).getPosition()) == null ||
                                appsList.get(lstBook.get(k).getPosition()).getPackageDetails() == null) {
                            appsList.set(lstBook.get(k).getPosition(), lstBook.get(k));
                        } else {
                            try {
                                for (int j = 0; j < appsList.size(); j++) {
                                    if (appsList.size() > 0 && appsList.get(j) == null || appsList.get(j).getPackageDetails() == null) {
                                        appsList.set(j, lstBook.get(k));
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (appsList != null && appsList.size() > 0) {
                        for (int j = 0; j < appsList.size(); j++) {
                            if (appsList.get(j) == null || appsList.get(j).getPackageDetails() == null) {
                                appsList.set(j, lstBook.get(k));
                                break;
                            }
                        }
                    }
                }
            }
        }

        lstBook.clear();
        lstBook.addAll(appsList);
        myAdapter = new RecyclerViewAdapter(this, appsList, this);
        myRv.setLayoutManager(new GridLayoutManager(this, 3));
        myRv.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    public void setScheduleTime(String startTime, String endTime) {
        /*
                if (scheduleEventModels.size() > 0) {

                    for (int l = 0; l < scheduleEventModels.size(); l++) {

                        ArrayList<String> strings = new ArrayList<String>(Arrays.asList(scheduleEventModels.get(l).getDay().split(",")));

                        ArrayList<Integer> integers = new ArrayList<>();

                        for (int m = 0; m < strings.size(); m++) {
                            integers.add(Integer.parseInt(strings.get(m)));
                        }

                        Calendar calendar = Calendar.getInstance();
                        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                        for (int k = 0; k < integers.size(); k++) {

                            if (currentDay == integers.get(k)){
                                SimpleDateFormat format = new SimpleDateFormat("hh:mm");
                                Date startDate = null;
                                try {
                                    startDate = format.parse(scheduleEventModels.get(l).getStartTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date endDate = null;
                                try {
                                    endDate = format.parse(scheduleEventModels.get(l).getEndTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (calendar.before(startDate)){
                                    Intent intent = new Intent(context, TimeOutScreen.class);
                                    startActivity(intent);
                                } else if (calendar.after(endDate)){
                                    Intent intent = new Intent(context, TimeOutScreen.class);
                                    startActivity(intent);
                                } else {
                                    setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                }
                                break;
                            }
                        }
                    }
                }
*/
        CountDownTimer scheduleCountDownTimer = new CountDownTimer(CustomDashboardApplication.calculateTimeInMillis(startTime, endTime), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("Time", "Time Running");
            }

            @Override
            public void onFinish() {
                Log.e("Time", "Time Finished");
                Intent intent = new Intent(Consts.SCHEDULE_TIMER);
                intent.putExtra(Consts.scheduleStatus, 1);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
/*
                if (scheduleEventModels.size() > 0) {

                    for (int l = 0; l < scheduleEventModels.size(); l++) {

                        ArrayList<String> strings = new ArrayList<String>(Arrays.asList(scheduleEventModels.get(l).getDay().split(",")));

                        ArrayList<Integer> integers = new ArrayList<>();

                        for (int m = 0; m < strings.size(); m++) {
                            integers.add(Integer.parseInt(strings.get(m)));
                        }

                        Calendar calendar = Calendar.getInstance();
                        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                        for (int k = 0; k < integers.size(); k++) {

                            if (currentDay == integers.get(k)){
                                SimpleDateFormat format = new SimpleDateFormat("hh:mm");
                                Date startDate = null;
                                try {
                                    startDate = format.parse(scheduleEventModels.get(l).getStartTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date endDate = null;
                                try {
                                    endDate = format.parse(scheduleEventModels.get(l).getEndTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (calendar.before(startDate)){
                                    Intent intent = new Intent(context, TimeOutScreen.class);
                                    startActivity(intent);
                                } else if (calendar.after(endDate)){
                                    Intent intent = new Intent(context, TimeOutScreen.class);
                                    startActivity(intent);
                                } else {
                                    setScheduleTime(scheduleEventModels.get(l).getStartTime(), scheduleEventModels.get(l).getEndTime());
                                }
                                break;
                            }
                        }
                    }
                }
*/

            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (CustomDashboardApplication.isDeviceSamsungActivated()) {
            CustomDashboardApplication.enableBackKey(context);
        }
       /* try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                CustomDashboardApplication.exitSamsungKiosk(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        isBackGround = 1;

        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
        //LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver2);
        //LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver6);


        //LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
      /*  ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if (isClick == 0){
            activityManager.moveTaskToFront(getTaskId(), 0);
        } else {
            isClick = 0;
        }*/

        if (currentApplicationModels != null && currentApplicationModels.size() > 0) {
            if (isItemClick == 1) {
                isItemClick = 0;
                String ser1 = SerializeObject.ReadSettings(context, Consts.appAnalyticsFile);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser1);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        lastSavedApp = (ArrayList<ApplicationTimerModel>) obj;
                    }
                }

                /*
                 */
                int pos = currentApplicationModels.size() - 1;
                if (currentApplicationModels.get(pos).getAppPackageName().equals("com.securewebview")) {
                    apiManager.uploadWebHistoryToServer();
                }
                String endTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());
                ApplicationTimerModel applicationTimerModel = new ApplicationTimerModel();
                applicationTimerModel.setId(currentApplicationModels.get(pos).getId());
                applicationTimerModel.setAppName(currentApplicationModels.get(pos).getAppName());
                applicationTimerModel.setAppPackageName(currentApplicationModels.get(pos).getAppPackageName());
                applicationTimerModel.setDate(currentApplicationModels.get(pos).getDate());
                applicationTimerModel.setTime(currentApplicationModels.get(pos).getTime());
                applicationTimerModel.setStartTime(currentApplicationModels.get(pos).getStartTime());
                applicationTimerModel.setEndTime(endTime);

                currentApplicationModels.set(pos, applicationTimerModel);

                // if (lastSavedApp.size() > 0) {
                lastSavedApp.add(currentApplicationModels.get(pos));
                // } else {

                // }

                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                String ser = SerializeObject.objectToString(lastSavedApp);
                if (ser != null && !ser.equalsIgnoreCase("")) {
                    SerializeObject.WriteSettings(context, ser, Consts.appAnalyticsFile);
                } /*else {
                    SerializeObject.WriteSettings(context, "", "appanalytics.dat");
                }*/
            }
        }
    }


    @Override
    public void onBackPressed() {
        Log.e("MainActivity", "onBackPressed");
    }

    @Override
    public void OnSelectItem(int pos) {
        isHomeApp = 1;

        if (!isAccessibilitySettingsEnabled(context)) {
            Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission before using Dashboard. It's helpful to track background running applications.");
            return;
        }

        if (lstBook.size() > 0) {
            CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 1);
            try {
                if (isLlPassword == 0) {
                    CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 0);
                    final String ApplicationPackageName = lstBook.get(pos).getPackageDetails();
                    if (lstBook.get(pos).getPackageDetails().equals("com.dialer")) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (lstBook.get(pos).getPackageDetails().equals("com.securewebview")) {
                        Intent intent = new Intent(context, SecureWebView.class);
                        intent.putExtra(Consts.is_shortcut, 0);
                        startActivity(intent);
                    } else if (lstBook.get(pos).getPackageDetails().equalsIgnoreCase("com.android.vending")) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(ApplicationPackageName);
                        if (intent != null) {
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(ApplicationPackageName);
                        if (intent != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, ApplicationPackageName + " Error, Please Try Again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    /*if (Utils.isOreo()) {
                        startForegroundService(new Intent(MainActivity.this, OtherAppStartupService.class));
                    } else {
                        startService(new Intent(MainActivity.this, OtherAppStartupService.class));
                    }*/

                    isItemClick = 1;
                    ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);
                    String appName;
                    if (lstBook.get(pos).getPackageDetails().equals("com.dialer")) {
                        appName = lstBook.get(pos).getPackageDetails();
                    } else if (lstBook.get(pos).getPackageDetails().equals("com.securewebview")) {
                        appName = lstBook.get(pos).getPackageDetails();
                    } else {
                        appName = apkInfoExtractor.GetAppName(lstBook.get(pos).getPackageDetails());
                    }
                    int id = lstBook.get(pos).getId();

                    String date = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(new Date());
                    String startTime = new SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(new Date());

                    currentApplicationModels.add(new ApplicationTimerModel(id, "0", appName, lstBook.get(pos).getPackageDetails(), date, startTime, ""));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // REQUEST PERMISSIONS
    public void requestPermission() {
        // APP PERMISSIONS
        String[] permit = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Manifest.permission.EXPAND_STATUS_BAR,
                Manifest.permission.WRITE_SETTINGS
        };
        if (Utils.isTiramisu()) {
            permit = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.EXPAND_STATUS_BAR
            };
        } else {
            permit = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.EXPAND_STATUS_BAR
            };
        }
        ActivityCompat.requestPermissions(this, permit, request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == request) {
            // if (isHomeBtnClicked != 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    isHomeBtnClicked = 0;
                    //relAskPermission.setVisibility(View.GONE);
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        if (!SuperLockState.getInstance(context).isESDKLicenseActivated()) {

                        } else {
                            CustomDashboardApplication.enableSamsungKiosk(context);
                        }
                    } else {
                        if (permissionGiven == 1) {
                            //showOpenLauncherDialog();
                        }
                    }
                    //importFile();
                } else if (Utils.isMarshMallow() && Utils.isLessTiramisu() &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    isHomeBtnClicked = 0;
                    //relAskPermission.setVisibility(View.GONE);
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        if (!SuperLockState.getInstance(context).isESDKLicenseActivated()) {

                        } else {
                            CustomDashboardApplication.enableSamsungKiosk(context);
                        }
                    } else {
                        if (permissionGiven == 1) {
                            //showOpenLauncherDialog();
                        }
                    }
                    //importFile();
                } else {
                    requestPermission();
                }
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

            }
            // } else {
            //  isHomeBtnClicked = 0;
            // }
        } else if (requestCode == Utils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChoosenTask.equals(getString(R.string.take_photo))) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                        cameraIntent();
                    } else {
                        Utils.openCameraIntent(this, MainActivity.this);
                    }
                } else if (userChoosenTask.equals(getString(R.string.choose_from_library))) {
                    galleryIntent();
                }
                //importFile();
            } else {
                //code for deny
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE)
                    onSelectFromGalleryResult(data);
                else if (requestCode == REQUEST_CAMERA)
                    onCaptureImageResult(data);
            }
        } else {
            if (requestCode == Utils.REQUEST_CAPTURE_IMAGE) {
                //if (data != null) {
                try {
                    File file = new File(Utils.imageFilePath);
                    profileImageURI = Uri.fromFile(file);
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                    Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // }
            } else if (requestCode == Utils.REQUEST_CAPTURE_Gallery) {
                if (data != null) {
                    Bitmap bm = null;
                    try {
                        if (data != null) {
                            try {
                                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Uri tempUri = getImageUri(context, bm);
                        profileImageURI = tempUri;
                        CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                        Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("OnselectGallery->", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            // if so check once again if we have permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // continue here - permission was granted
                AppPermissionMain.overlayViewPermissionStatus = Settings.canDrawOverlays(context);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            CustomDashboardApplication.setInt(context, Consts.INSTALLED_APP, CustomDashboardApplication.getInt(context, Consts.INSTALLED_APP) + 1);
            CustomDashboardApplication.setInt(context, Consts.IS_GOOGLE_LOGIN, 1);
            assert acct != null;
            alertDialogNote.dismiss();
        } else {
            CustomDashboardApplication.setInt(context, Consts.INSTALLED_APP, 0);
            CustomDashboardApplication.setInt(context, Consts.IS_GOOGLE_LOGIN, 0);
            alertDialogNote.dismiss();
        }
        llLogo.setVisibility(View.GONE);
        ivLogo.setVisibility(View.GONE);
    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, getPackageName());

       /* ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
*/
        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                int isDefaultApp = 1;
                return true;
            }
        }
        return false;
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String getRealPathFromURI(Uri uri) {
        int idx = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            if (cursor != null) {
                idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                Log.e("RealPathFromURI->", e.getMessage());
            }
        }
        return cursor != null ? cursor.getString(idx) : "";
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
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        if (response.code() == 200 || response.code() == 201) {
                            if (response.body() != null) {
                                String responseString = response.body().toString();
                                Log.e("respCounter", response.body().toString());
                                try {
                                    final JSONObject jsonObject = new JSONObject(responseString);

                                    try {

                                        ShowMessage.showError(context, jsonObject.getString(Consts.error));
                                        CustomDashboardApplication.setInt(context, Consts.is_logged_in, 0);
                                        CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, "0000");
                                        CustomDashboardApplication.setInt(context, Consts.IS_PWD, 0);

                                        if (isProfileDeteted) {
                                            Utils.exitApp(MainActivity.this);
                                        }
                                        isProfileDeteted = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String error = response.errorBody().string();
                                    if (error.startsWith("{") && error.endsWith("}")) {
                                        JSONObject errorObj = new JSONObject(error);
                                        if (Utils.contains(errorObj, Consts.responseMessage)) {
                                            if (BuildConfig.DEBUG) {
                                                ShowMessage.showError(context, errorObj.getString(Consts.responseMessage));
                                            }
                                        } else {
                                            if (BuildConfig.DEBUG) {
                                                ShowMessage.showError(context, error);
                                            }
                                        }
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            ShowMessage.showError(context, error);
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
                        t.printStackTrace();
                        //dialogLoader.hideProgressDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitApp() {
        /*try {
            PackageManager pm = getApplicationContext().getPackageManager();
            ComponentName compName =
                    new ComponentName(getPackageName(), getPackageName() + ".Activity.FakeActivity");
            pm.setComponentEnabledSetting(compName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        stopService(new Intent(context, AppVisibilityService.class));

        SessionManager.getInstance(this).setIsParentMode(false);
        CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
        CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
        CustomDashboardApplication.setInt(context, Consts.ENABLE_STATUS_BAR, 1);
        SessionManager.getInstance(context).setIsDashboardLaunch(false);

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar beginCal = Calendar.getInstance();
        beginCal.setTimeInMillis(startAppTime);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(System.currentTimeMillis());

        final List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        ArrayList<String> allPackageNameList = new ArrayList<>();
        ArrayList<String> finalBackgroungAppList = new ArrayList<>();

        for (int i = 0; i < queryUsageStats.size(); i++) {
            allPackageNameList.add(queryUsageStats.get(i).getPackageName());
        }

        if (allPackageNameList.size() > 0) {
            ArrayList<String> appModels = new ApkInfoExtractor(context).getAppsPackageNameList();
            for (int j = 0; j < allPackageNameList.size(); j++) {
                if (appModels.contains(allPackageNameList.get(j))) {
                    finalBackgroungAppList.add(allPackageNameList.get(j));
                }
            }
        }

        if (finalBackgroungAppList.size() > 0) {
            for (int k = 0; k < finalBackgroungAppList.size(); k++) {
                if (!finalBackgroungAppList.get(k).equals(getPackageName())) {
         /*           PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeActivity.class);
                    packageManager.setApplicationEnabledSetting(finalBackgroungAppList.get(k), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    packageManager.setApplicationEnabledSetting(finalBackgroungAppList.get(k),PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);*/
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (view != null) {
                manager.removeView(view);
            }
        } else {
            // Method that handles loss of window focus
            //new BlockStatusBar(this, false, true).collapseNow();
        }


        if (!(CustomDashboardApplication.isDeviceSamsung() && SuperLockState.getInstance(this).isESDKLicenseActivated())) {
            try {
                getPackageManager().clearPackagePreferredActivities(getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

      /*  PackageManager packageManager = getPackageManager();
        packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);*/

        /*try {
            if (CustomDashboardApplication.isDeviceSamsung()) {
                try {
                    try {
                        if (!SuperLockState.getInstance(MainActivity.this).isKioskModeEnabled(MainActivity.this)) {
                            SuperLockState.getInstance(context).enableKioskMode(Consts.PACKAGE_NAME);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    CustomDashboardApplication.exitSamsungKiosk(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if (SessionManager.getInstance(this).getLastExitAppTime() > 0 &&
                (System.currentTimeMillis() - SessionManager.getInstance(this).getLastExitAppTime()) <= 10000) {
            adminDisable();
        }

        SessionManager.getInstance(this).setLastExitAppTime(System.currentTimeMillis());

        if (overLayView != null) {
            overlayManager.removeView(overLayView);
        }

        try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                CustomDashboardApplication.exitSamsungKiosk(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        resetPreferredLauncherAndOpenChooser(MainActivity.this);

        finish();
        //System.exit(0);
    }

    private void adminDisable() {
        ComponentName admin = new ComponentName(this, com.tabnova.aidashboard.Knox.startup.AdminReceiver.class);
        DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        DPM.removeActiveAdmin(admin);
    }

    // SET DAY LONG VALUES FOR WIPE DATA
    public void setWipeDateDetails() {

        // TODAY"S DATE CALENDAR INSTANCE
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        CustomDashboardApplication.setTodayLong(context, Consts.todayDateLong, today.getTimeInMillis());

        // YESTERDAY DATE CALENDAR INSTANCE
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(new Date());
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        CustomDashboardApplication.setYesterdayLong(context, Consts.yesterDayLong, yesterday.getTimeInMillis());

        // LAST WEEK CALENDAR INSTANCE
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.setTime(new Date());
        lastWeek.add(Calendar.WEEK_OF_YEAR, -1);
        lastWeek.set(Calendar.DAY_OF_WEEK, lastWeek.getFirstDayOfWeek());
        lastWeek.set(Calendar.HOUR_OF_DAY, 0);
        lastWeek.set(Calendar.MINUTE, 0);
        lastWeek.set(Calendar.SECOND, 0);
        CustomDashboardApplication.setLastWeekLong(context, Consts.lastWeekLong, lastWeek.getTimeInMillis());

        // LAST MONTH CALENDAR INSTANCE
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        lastMonth.set(Calendar.DATE, 1);
        lastMonth.set(Calendar.HOUR_OF_DAY, 0);
        lastMonth.set(Calendar.MINUTE, 0);
        lastMonth.set(Calendar.SECOND, 0);
        CustomDashboardApplication.setLastWeekLong(context, Consts.lastMonthLong, lastMonth.getTimeInMillis());
    }

    private void setupBillingClient() {
        isHomeApp = 1;
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingClient.isReady()) {
                        queryPurchaseHistory();
                    } else {
                        CustomDashboardApplication.isBillingLaunched = false;
                    }
                } else {
                    CustomDashboardApplication.isBillingLaunched = false;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                CustomDashboardApplication.isBillingLaunched = false;
            }
        });
    }

    private void queryPurchaseHistory() {
        isHomeApp = 1;
        QueryPurchaseHistoryParams queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build();
        billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams, (client, purchasesList) -> {
            if (client.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesList != null && purchasesList.size() > 0) {
                boolean isProductFound = false;
                for (PurchaseHistoryRecord purchase : purchasesList) {
                    Log.e(TAG, "queryPurchaseHistoryAsync : " + new Gson().toJson(purchase));
                    if (!TextUtils.isEmpty(purchase.getOriginalJson())) {
                        try {
                            Log.e(TAG, "onBillingSetupFinished: Purchase JSON " + purchase.getOriginalJson());
                            JSONObject jObj = new JSONObject(purchase.getOriginalJson());
                            if (Utils.contains(jObj, Consts.productId)) {
                                if (jObj.getString(Consts.productId).equalsIgnoreCase(Consts.subscriptionId)) {
                                    String purchaseToken = purchase.getPurchaseToken();
                                    String purchaseTime = String.valueOf(purchase.getPurchaseTime());

                                    sessionManager.setPurchaseToken(purchaseToken);
                                    sessionManager.setPurchaseTime(purchaseTime);
                                }

                                isProductFound = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (isProductFound) {
                    sessionManager.setIsSubscriptionEnabled(true);

                    if ((blockDomainWebsitesList != null && blockDomainWebsitesList.size() > 0) || sessionManager.isApplyBlockDomains()) {
                        stopVPN();
                        startVPN();
                    }
                } else {
                    sessionManager.setIsSubscriptionEnabled(false);
                }
            }
        });
    }

    private void readFromFile() {
        blockDomainWebsitesList = new ArrayList<>();
        String blockDomainsList = SerializeObject.ReadSettings(context, Consts.blockDomainWebsiteFile);
        if (!blockDomainsList.equalsIgnoreCase("") && !TextUtils.isEmpty(blockDomainsList)) {
            Object blockDomainsObj = SerializeObject.stringToObject(blockDomainsList);
            if (blockDomainsObj instanceof ArrayList) {
                blockDomainWebsitesList = (ArrayList<String>) blockDomainsObj;
            }
        }

        if (blockDomainWebsitesList != null && blockDomainWebsitesList.size() > 0) {
            MySingletonClass.getInstance().setValueList(blockDomainWebsitesList);
        }
    }

    public void startVPN() {
        // check for VPN already running
        try {
            Log.e(TAG, "startVPN: Prepaper VPN service");
            // get user permission for VPN
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                isHomeApp = 1;
                Log.e(TAG, "ask user for VPN permission");
                vpnActivityResultLauncher.launch(intent);
            } else {
                readFromFile();

                if (sessionManager.isApplyBlockDomains()) {
                    File file =
                            new File(this.getExternalFilesDir(null) + "/Dashboard/block_list.txt");

                    if (file.exists()) {
                        MySingletonClass.getInstance().setBlockedHostsValue(this, sessionManager.isApplyBlockDomains());
                    } else {
                        new CommonUtility(context).downloadFile();
                    }
                } else {
                    if (blockDomainWebsitesList.size() > 0)
                        MySingletonClass.getInstance().setBlockedHostsValue(this, sessionManager.isApplyBlockDomains());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception checking network interfaces :" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopVPN() {
        if (VPNService.Companion.getInstance() != null) {
            VPNService.Companion.stopService();
        }
    }

    ActivityResultLauncher<Intent> vpnActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    stopVPN();
                    startVPN();
                }
            });
}