package com.tabnova.aidashboard.Activity;

import static com.tabnova.aidashboard.CustomDashboardApplication.isAccessibilitySettingsEnabled;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.rsb.vpnpoc.VPNService;
import com.tabnova.aidashboard.Adapter.AddedWebShortcutsAdapter;
import com.tabnova.aidashboard.Adapter.CallLogsListAdapter;
import com.tabnova.aidashboard.Adapter.ClearAppsDataListAdapter;
import com.tabnova.aidashboard.Adapter.DeleteFileFolderListAdapter;
import com.tabnova.aidashboard.Adapter.DeletePredefineFileFolderListAdapter;
import com.tabnova.aidashboard.Adapter.FileModelListAdapter;
import com.tabnova.aidashboard.Adapter.PreDefineListAdapter;
import com.tabnova.aidashboard.Adapter.ScheduleAdapter;
import com.tabnova.aidashboard.Adapter.ScheduleEventListAdapter;
import com.tabnova.aidashboard.Adapter.SettingsAdapter;
import com.tabnova.aidashboard.Adapter.SettingsAppsAdapter;
import com.tabnova.aidashboard.Adapter.UninstallAppsListAdapter;
import com.tabnova.aidashboard.Adapter.WebCookiesListAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.BillingManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.GoogleAuthManager;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.OnResponseListener;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Knox.utils.SysUtils;
import com.tabnova.aidashboard.Model.AddedWebShortcutModel;
import com.tabnova.aidashboard.Model.AdvanceSettingsModel;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.Model.CallLogsModel;
import com.tabnova.aidashboard.Model.DailyTimerModel;
import com.tabnova.aidashboard.Model.DateSelectionModel;
import com.tabnova.aidashboard.Model.FileModel;
import com.tabnova.aidashboard.Model.PredefineFoldersModel;
import com.tabnova.aidashboard.Model.ProfileData;
import com.tabnova.aidashboard.Model.ScheduleDaysModel;
import com.tabnova.aidashboard.Model.ScheduleEventModel;
import com.tabnova.aidashboard.Model.SelectedFileFolderModel;
import com.tabnova.aidashboard.Model.SettingsAppsModel;
import com.tabnova.aidashboard.Model.SettingsModel;
import com.tabnova.aidashboard.Model.SubSelectedFileFolderModel;
import com.tabnova.aidashboard.Model.WipeButtonsModel;
import com.tabnova.aidashboard.R;
import com.tabnova.aidashboard.BlockApps.WatchingAccessibilityService;
import com.tabnova.screenshare.MediaProjectionService;
import com.tabnova.screenshare.SignallingClient;
import com.tabnova.screenshare.StartStopScreenShare;
import com.tabnova.aidashboard.Extra.TooltipHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CustomDashboardSettings extends BaseActivity implements SettingsAppsAdapter.OnSettingSelectListner, SettingsAdapter.OnSettingSelectListner,
        UninstallAppsListAdapter.OnAppSelectListener, ClearAppsDataListAdapter.OnClearAppSelectListener,
        OnResponseListener, ScheduleAdapter.OnScheduleSelectListner, ScheduleEventListAdapter.OnEventSelectListner,
        AddedWebShortcutsAdapter.OnShortcutsSelectListener, CallLogsListAdapter.OnCallLogSelectListener, WebCookiesListAdapter.OnWebCookieSelectListener,
        DeletePredefineFileFolderListAdapter.OnDeletePreFileFolderListener, DeleteFileFolderListAdapter.OnDeleteFileFolderListener,
        PreDefineListAdapter.OnPreDefineSelectListener, FileModelListAdapter.OnSelectListener {

    private static final String TAG = "CustomDashboardSettings";

    // ALERT DIALOG
    private AlertDialog alertExitDialog;
    private AlertDialog alertLogoutDialog;
    private AlertDialog alertMessage;
    AlertDialog.Builder alertMessageDialog;

    // TEXT VIEW
    private TextView txAppVersion;
    private TextView txLoginStr;
    private TextView txCurrentPath;
    private TextView txParentMode;
    private TextView txDashboard;
    private TextView txExit;

    // URL REGEX
    private final String URL_REGEX = "((http?|https|ftp|file)://)?(([Ww]){3}.)?[a-zA-Z0-9]+\\.[a-zA-Z]+";

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogPwd;
    private AppCompatDialog alertDialogAppInfo;
    private AppCompatDialog alertDialogClockSetting;
    private AppCompatDialog alertDialogImportExport;
    private AppCompatDialog alertDialogSetTimer;
    private AppCompatDialog alertDialogShowShortCut;
    private AppCompatDialog alertDialogAllowCalls;
    private AppCompatDialog alertDialogPassword;
    private AppCompatDialog alertDialogLabels;
    private AppCompatDialog alertDialogMarketPlace;
    private AppCompatDialog alertDialogDisableRecentsButton;
    private AppCompatDialog alertDialogCallSmsLogs;
    private AppCompatDialog alertVolumeControl;
    private AppCompatDialog alertSelectImage;
    private AppCompatDialog alertDialogSettings;
    private AppCompatDialog alertDialogAddEvent;
    private AppCompatDialog alertDialogAddWebShortcut;
    private AppCompatDialog alertDialogSecureBrowser;
    private AppCompatDialog alertDialogLoggedIn;
    private AppCompatDialog alertDialogGoogleLoggedIn;
    private AppCompatDialog alertDialogAdvanceSettings;
    private AppCompatDialog alertDialogUninstall;
    private AppCompatDialog alertClearAppsData;
    private AppCompatDialog alertCallLogsDialog;
    private AppCompatDialog alertClearWebCookies;
    private AppCompatDialog alertDeleteFIleFolderDialog;
    private AppCompatDialog alertDeleteFileDetailsDialog;
    private AppCompatDialog alertFileManagerDialog;
    private AppCompatDialog errorDialog;

    // IMAGE VIEW
    ImageView ivUninstallSelectAll;
    ImageView ivClearAppsDataSelectAll;
    ImageView ivCallLogsSelectAll;
    ImageView ivWebCookieSelectAll;
    ImageView ivDeleteFileSelectAll;
    ImageView ivUpFolder;
    ImageView ivFileManagerSelectAll;
    ImageView ivOptions;
    private ImageView ivBtnOne, ivBtnTwo, ivBtnThree, ivBtnFour, ivBtnFive,
            ivBtnSix, ivBtnSeven, ivBtnEight, ivBtnNine, ivBtnZero, ivBtnBack, ivBtnOk, ivClose;

    // BOOLEAN
    boolean isShowAllSelected = false;
    boolean isSelectAllSelected = true;
    boolean isShowAllClearDataSelected = false;
    boolean isSelectAllClearDataSelected = true;
    boolean isSelectAllCallLogsSelected = false;
    boolean isSelectAllWebCookiesSelected = false;
    boolean isSelectAllDeleteFileSelected = false;
    boolean isSelectAllFileManagerSelected = false;
    boolean isProfileDeteted = false;

    // REQUEST PERMISSION
    private static final int PICKFILE_REQUEST_CODE = 100;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    // RECYCLER VIEW
    public RecyclerView recViewSettings;
    private RecyclerView recScheduleEvent;
    private RecyclerView recUninstallAppsView;
    private RecyclerView recClearAppsDataView;
    private RecyclerView recCallLogsView;
    private RecyclerView recWebCookieView;
    private RecyclerView recDeleteFileFolderView;
    private RecyclerView recPredefineDeleteFileFolderView;
    private RecyclerView recPreFileRecView;
    private RecyclerView recFileListRecView;

    //LINEAR LAYOUT
    private LinearLayout llDeleteFileDetails;
    LinearLayout llParentMode;
    LinearLayout llDashBoard;
    LinearLayout llExit;
    private LinearLayout llPwd;

    // ADAPTER
    public SettingsAdapter settingsAdapter;
    public ScheduleEventListAdapter scheduleEventListAdapter;
    public UninstallAppsListAdapter uninstallAppsListAdapter;
    public ClearAppsDataListAdapter clearAppsDataListAdapter;
    public CallLogsListAdapter callLogsListAdapter;
    public WebCookiesListAdapter webCookiesListAdapter;
    public DeleteFileFolderListAdapter deleteFileFolderListAdapter;
    public DeletePredefineFileFolderListAdapter preDeleteFileFolderListAdapter;
    public PreDefineListAdapter preDefineListAdapter;
    public FileModelListAdapter fileModelListAdapter;

    // ARRAY LIST
    public ArrayList<SettingsModel> settingsModels;
    private ArrayList<AppModel> systemAppsModels;
    private ArrayList<DailyTimerModel> dailyTimerModels;
    private ArrayList<ScheduleEventModel> scheduleEventModels;
    ArrayList<AppModel> appModels;
    List<ResolveInfo> resolveInfoList;
    private ArrayList<AppModel> uninstallAppList;
    private ArrayList<AppModel> clearAppsDataList;
    private ArrayList<DateSelectionModel> selectedTypeList;
    private ArrayList<CallLogsModel> callLogsModels;
    private ArrayList<AppModel> webCookiesModels;
    ArrayList<SelectedFileFolderModel> preDeleteFileFolderModels;
    ArrayList<SelectedFileFolderModel> selectedDeleteFilesModels;
    ArrayList<SelectedFileFolderModel> deleteFileFolderModels;
    ArrayList<PredefineFoldersModel> predefineFoldersModels;
    ArrayList<FileModel> fileManagerFilesList;
    ArrayList<SelectedFileFolderModel> selectedFilePaths;

    // INTEGER
    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;
    private int selectedMethod;
    private int isExportType;
    private int isVolumeEnabled;
    private int selectedOption;
    private int currentWipeSelection;
    private int deleteFileSelection = 0;
    private int incorrectPinTapCount = 0;
    public int isllPassword;

    // STRING
    private String userChoosenTask;
    private String selectedPath;
    private String profileImageString;
    private String dirPath = "";
    private String parentDirPath = "";

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // EDIT TEXT
    private EditText edPath;
    private EditText edPassword;

    // URI
    private Uri profileImageURI = null;

    // BUTTON
    private Button btnDashBoard;
    private Button btnLogin;

    // LINEAR LAYOUT
    private LinearLayout llFetchingApps;

    // CONTEXT
    private Context context;

    //SESSION MANAGER
    SessionManager sessionManager;

    /*private String readLogpermit[] = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS};*/

    Integer request = 0;
    int isHomeApp = 1;

    // ALERT DIALOG
    private AlertDialog d;

    // BUFFER READER
    private BufferedReader br;

    // API MANAGER
    APIManager apiManager;
    private ArrayList<ScheduleDaysModel> scheduleDaysModels;
    private ArrayList<AddedWebShortcutModel> addedWebShortCutsModels;
    private ScheduleAdapter scheduleAdapter;
    private ArrayList<Integer> selectedDays;
    private JsonArray selectedDaysArray = new JsonArray();
    private AddedWebShortcutsAdapter addedWebSiteShortcutAdapter;
    private RecyclerView recAddedShortcuts;
    private final int count = 0;
    private ArrayList<AppModel> tempList;

    // LONG
    long todayTime = 0;
    long yesterdayTime = 0;
    long lastWeekTime = 0;
    long lastMonthTime = 0;

    File dir;

    //BOOLEAN
    boolean isSpotLightShown = false;
    boolean isPause = false;

    //TOOL TIP
    TooltipHelper loginToolTip;
    TooltipHelper applicationToolTip;

    // STRING BUILDER
    StringBuilder mainSb = new StringBuilder();

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    @Override
    protected boolean useLightStatusBarIcons() {
        return true;
    }

    @Override
    protected boolean useLightNavigationBarIcons() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_dashboard_settings);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back closes Settings only when the kiosk dashboard is not launched;
                // otherwise it stays blocked (same condition as before).
                if (!sessionManager.getIsDashboardLaunch()) {
                    finish();
                }
            }
        });

        Log.e("CustomDashboard", "onCreate: CustomDashboardSettings");

        context = this;


        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),uri -> {
            if (uri != null) {
                Log.d(TAG, "Selected URI: " + uri.toString());
                // TODO: Load the image with Glide, Picasso, etc.
            } else {
                Log.d(TAG, "No media selected");
            }
        });

        /*if (Utils.isTiramisu() && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            requestpermission();
        } else if (Utils.isLessTiramisu() && Utils.isMarshMallow() &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            requestpermission();
        }*/

        if (Utils.isMarshMallow() && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestpermission();
        }

        Log.e(TAG, "onCreate: " + CustomDashboardSettings.class.getSimpleName());
        sessionManager = new SessionManager(this);

        todayTime = CustomDashboardApplication.getLong(context, Consts.todayDateLong);
        yesterdayTime = CustomDashboardApplication.getLong(context, Consts.yesterDayLong);
        lastWeekTime = CustomDashboardApplication.getLong(context, Consts.lastWeekLong);
        lastMonthTime = CustomDashboardApplication.getLong(context, Consts.lastMonthLong);

        apiManager = new APIManager(context, this);

        Log.e(TAG, "onCreate: Serial Number : " + CustomDashboardApplication.getString(context, Consts.generated_serial_no));

        //Toast.makeText(context, "Setting screen called", Toast.LENGTH_SHORT).show();
        /*if (CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.isBillingLaunched)
            setupBillingClient();*/

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            apiManager.getApplicationList();
            ArrayList<AppModel> systemAppsModels = new ApkInfoExtractor(context).GetAllInstalledApkInfo();

            //if (systemAppsModels.size() != CustomDashboardApplication.getInt(context, Consts.serverAppListCount)){
            String dir = context.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.appCategoryFile);
            if (!f0.exists()) {

            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
            for (int i = 1; i < resolveInfoList.size(); i++) {
                ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
                apiManager.getAppsCategories(activityInfo.applicationInfo.packageName, activityInfo.applicationInfo, resolveInfoList.size());
            }
            //}
        } else {
           /* String dir = context.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.appCategoryFile);
            if (f0.exists()) {
                f0.delete();
            }

           Intent intent = new Intent(Intent.ACTION_MAIN,null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );

            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent,0);
            for (int i = 1; i < resolveInfoList.size(); i++) {
                ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
                apiManager.getAppsCategories(activityInfo.applicationInfo.packageName, activityInfo.applicationInfo);
            }*/
        }

        dialogLoader = new DialogLoader(context);

        // INIT VIEWS
        recViewSettings = findViewById(R.id.rec_view_settings);
        recViewSettings.setHasFixedSize(true);

        txAppVersion = findViewById(R.id.tx_app_version);
        txLoginStr = findViewById(R.id.tx_login_str);
        txParentMode = findViewById(R.id.tv_parent_mode);
        txDashboard = findViewById(R.id.tv_dashboard);
        txExit = findViewById(R.id.tv_exit);
        llParentMode = findViewById(R.id.ll_parent_mode);
        llDashBoard = findViewById(R.id.ll_dashboard);
        llExit = findViewById(R.id.ll_exit);
        ivOptions = findViewById(R.id.iv_options);

        //INIT PASSWORD VIEWS
        edPassword = findViewById(R.id.ed_pwd);
        llPwd = findViewById(R.id.ll_show_pwd);
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
        ivClose = findViewById(R.id.btn_close);

        ivClose.setOnClickListener(v -> {
            isHomeApp = 0;
            llPwd.setVisibility(View.GONE);
        });

        ivOptions.setOnClickListener(v -> {
            if (isllPassword == 0) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                PopupWindow popupWindowObj = showPopupMenu();
                //popupWindowObj.showAsDropDown(viewHolder.itemView, -150, -32)
                popupWindowObj.showAsDropDown(ivOptions, 0, 0);
            }
        });

        llExit.setOnClickListener(v -> {
            if (isllPassword == 0) {
                sessionManager.setIsParentMode(true);
                Utils.exitApp(CustomDashboardSettings.this);

                try {
                    resetViews();
                    txExit.setTextColor(getResources().getColor(R.color.white));
                    llExit.setBackground(getResources().getDrawable(R.drawable.bg_circle_primary_white_border));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        llParentMode.setOnClickListener(v -> {
            resetViews();
            txParentMode.setTextColor(getResources().getColor(R.color.white));
            llParentMode.setBackground(getResources().getDrawable(R.drawable.bg_circle_primary_white_border));
            sessionManager.setIsParentMode(true);
        });

        llDashBoard.setOnClickListener(v -> {
            if (isllPassword == 0) {
                if (!isAccessibilitySettingsEnabled(context)) {
                    Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission before using Dashboard. It's helpful to track background running applications.");
                } else {
                    isHomeApp = 1;
                    resetViews();
                    txDashboard.setTextColor(ContextCompat.getColor(context, R.color.white));
                    llDashBoard.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_circle_primary_white_border));
                    sessionManager.setIsParentMode(false);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    try {
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        txAppVersion.setText("App Version: " + SysUtils.getVersionName(context, context.getApplicationContext().getPackageName()));

        btnLogin = findViewById(R.id.login_btn);

        btnLogin.setOnClickListener(v -> {
            isHomeApp = 1;
            if (btnLogin.getText().toString().contains("Login")) {
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            } else {
                AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                dialogBuilder1.setMessage(getResources().getString(R.string.log_out_msg));
                dialogBuilder1.setPositiveButton("Yes", (dialog, which) -> {
                    logoutUser();
                    CustomDashboardApplication.setInt(context, Consts.is_logged_in, 0);
                    d.dismiss();
                });
                dialogBuilder1.setNegativeButton("No", (dialog, which) -> d.dismiss());
                d = dialogBuilder1.create();
                d.setOnShowListener(dialog -> {
                    Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                    doneBtn.setAllCaps(false);
                });
                d.show();
            }
        });

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean(Consts.is_launcher)) {
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    if (CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH) == 1) {
                        showPasswordDialog(1);
                    }
                }
            }
        }

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        CustomDashboardApplication.setInt(context, Consts.isSettings, count + 1);

        // BUTTON DASHBOARD
        btnDashBoard = findViewById(R.id.btn_dash_board);
        btnDashBoard.setOnClickListener(v -> {

            if (isMyLauncherDefault()) {
                //resetPreferredLauncherAndOpenChooser(context);
                finish();
            } else {
                //showOpenSettingsDialog();
                finish();

            }

            // resetPreferredLauncherAndOpenChooser(context);
            //finish();
        });

        // ARRAY LIST
        settingsModels = new ArrayList<>();
        ArrayList<SettingsAppsModel> controlsModels = new ArrayList<>();
        controlsModels.add(0, new SettingsAppsModel(0, R.drawable.ic_new_applications_white, getResources().getString(R.string.applications)));
        controlsModels.add(1, new SettingsAppsModel(1, R.drawable.ic_setting_wallpaper, getResources().getString(R.string.wallpaper)));
        controlsModels.add(2, new SettingsAppsModel(2, R.drawable.ic_lock, getResources().getString(R.string.password)));
        //controlsModels.add(3, new SettingsAppsModel(3, R.drawable.ic_label, getResources().getString(R.string.labels)));
        controlsModels.add(3, new SettingsAppsModel(3, R.drawable.ic_setting_timer, getResources().getString(R.string.schedule)));
        controlsModels.add(4, new SettingsAppsModel(4, R.drawable.ic_settings_web_shortcut, getResources().getString(R.string.add_web_shortcut)));
        controlsModels.add(5, new SettingsAppsModel(5, R.drawable.ic_new_settings, getResources().getString(R.string.settings)));
        if (CustomDashboardApplication.isDeviceSamsung()) {
            if (Consts.IS_BUSINESS_APP == 1) {
                if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                    controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_new_settings, getResources().getString(R.string.open_settings_str)));
                    controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_setting_file_folders, getResources().getString(R.string.open_file_manager)));
                    //controlsModels.add(8, new SettingsAppsModel(8, R.drawable.ic_advance_settings, getResources().getString(R.string.advance_settings)));
                    controlsModels.add(8, new SettingsAppsModel(8, R.drawable.ic_setting_uninstall, getResources().getString(R.string.uninstall)));
                    controlsModels.add(9, new SettingsAppsModel(9, R.drawable.ic_disable_recent_button, getString(R.string.website_block)));
                    controlsModels.add(10, new SettingsAppsModel(10, R.drawable.ic_new_applications_white, getString(R.string.app_block)));
                } else {
                    //controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_advance_settings, getResources().getString(R.string.advance_settings)));
                    controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_setting_uninstall, getResources().getString(R.string.uninstall)));
                    controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_disable_recent_button, getString(R.string.website_block)));
                    controlsModels.add(8, new SettingsAppsModel(8, R.drawable.ic_new_applications_white, getString(R.string.app_block)));
                }
            } else {
                // controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_advance_settings, getResources().getString(R.string.advance_settings)));
                controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_setting_uninstall, getResources().getString(R.string.uninstall)));
                //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                //controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_disable_recent_button, getResources().getString(R.string.block_domain_website)));
                controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_disable_recent_button, getString(R.string.website_block)));
                controlsModels.add(8, new SettingsAppsModel(8, R.drawable.ic_new_applications_white, getString(R.string.app_block)));
                //}
            }
        } else {
            //controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_disable_recent_button, getResources().getString(R.string.block_domain_website)));
            controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_disable_recent_button, getString(R.string.website_block)));
            controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_new_applications_white, getString(R.string.app_block)));
        }

        /*controlsModels.add(5, new SettingsAppsModel(5, R.drawable.ic_shortcuts, getResources().getString(R.string.short_cut)));
        controlsModels.add(6, new SettingsAppsModel(6, R.drawable.ic_volume_setting, getResources().getString(R.string.volume_control)));
        controlsModels.add(7, new SettingsAppsModel(7, R.drawable.ic_secure_browser, getResources().getString(R.string.secure_browser)));
        controlsModels.add(8, new SettingsAppsModel(8, R.drawable.ic_web_shortcut, getResources().getString(R.string.add_web_shortcut)));
        controlsModels.add(9, new SettingsAppsModel(9, R.drawable.ic_recent_block, getResources().getString(R.string.disable_recents_button)));*/


        ArrayList<SettingsAppsModel> dataModels = new ArrayList<>();
        //dataModels.add(0, new SettingsAppsModel(0,R.drawable.ic_screen_time,getResources().getString(R.string.screen_timer)));

        dataModels.add(0, new SettingsAppsModel(0, R.drawable.ic_setting_application_analytics, getResources().getString(R.string.app_analytics)));
        //dataModels.add(1, new SettingsAppsModel(1, R.drawable.ic_web_shortcut, "Share Screen"));
        //dataModels.add(1, new SettingsAppsModel(1, R.drawable.ic_youtube_history, context.getResources().getString(R.string.youtube_str)));
        if (Consts.IS_BUSINESS_APP == 1) {
            dataModels.add(1, new SettingsAppsModel(1, R.drawable.ic_imports_exports, getResources().getString(R.string.import_export)));
//            if (CustomDashboardApplication.isDeviceSamsung()) {
            //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
            //dataModels.add(2, new SettingsAppsModel(2, R.drawable.ic_advance_settings, getResources().getString(R.string.advance_settings)));
            // }
//            }
        } else {
//            if (CustomDashboardApplication.isDeviceSamsung()) {
            //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
            //dataModels.add(1, new SettingsAppsModel(1, R.drawable.ic_advance_settings, getResources().getString(R.string.advance_settings)));
            // }
//            }
        }

        ArrayList<SettingsAppsModel> generalModels = new ArrayList<>();
        generalModels.add(0, new SettingsAppsModel(0, R.drawable.ic_setting_contact_us, getResources().getString(R.string.contact_us)));
        generalModels.add(1, new SettingsAppsModel(1, R.drawable.ic_setting_rate_us, getResources().getString(R.string.rate_us)));
        generalModels.add(2, new SettingsAppsModel(2, R.drawable.ic_setting_share, getResources().getString(R.string.share)));
//        if (CustomDashboardApplication.isDeviceSamsungActivated())
        generalModels.add(3, new SettingsAppsModel(3, R.drawable.ic_shopping, "My Plans"));

        //generalModels.add(3, new SettingsAppsModel(3, R.drawable.ic_exits, getResources().getString(R.string.exit)));

        settingsModels.add(0, new SettingsModel(getResources().getString(R.string.controls), controlsModels));
        settingsModels.add(1, new SettingsModel(getResources().getString(R.string.data_analytics), dataModels));
        settingsModels.add(2, new SettingsModel(getResources().getString(R.string.genral), generalModels));

        // LAYOUT MANAGER
        recViewSettings.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        dailyTimerModels = new ArrayList<>();

        // ADAPTER
        settingsAdapter = new SettingsAdapter(context, settingsModels, this);
        recViewSettings.setAdapter(settingsAdapter);

        //if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) != 1) {
        dailyTimerModels = new ArrayList<>();
        dailyTimerModels.add(0, new DailyTimerModel(0, "--- Select Time ---", TimeUnit.SECONDS, 0));
        dailyTimerModels.add(1, new DailyTimerModel(1, "5 Min", TimeUnit.MINUTES, 5));
        dailyTimerModels.add(2, new DailyTimerModel(2, "10 Min", TimeUnit.MINUTES, 10));
        dailyTimerModels.add(3, new DailyTimerModel(3, "15 Min", TimeUnit.MINUTES, 15));
        dailyTimerModels.add(4, new DailyTimerModel(4, "30 Min", TimeUnit.MINUTES, 30));
        dailyTimerModels.add(5, new DailyTimerModel(5, "1 Hours", TimeUnit.HOURS, 1));
        dailyTimerModels.add(6, new DailyTimerModel(6, "2 Hours", TimeUnit.HOURS, 2));
        dailyTimerModels.add(7, new DailyTimerModel(7, "4 Hours", TimeUnit.HOURS, 4));
        dailyTimerModels.add(8, new DailyTimerModel(8, "8 Hours", TimeUnit.HOURS, 8));
        dailyTimerModels.add(9, new DailyTimerModel(9, "12 Hours", TimeUnit.HOURS, 12));
        dailyTimerModels.add(10, new DailyTimerModel(10, "16 Hours", TimeUnit.HOURS, 16));
        dailyTimerModels.add(11, new DailyTimerModel(11, "20 Hours", TimeUnit.HOURS, 20));
        dailyTimerModels.add(12, new DailyTimerModel(12, "24 Hours", TimeUnit.HOURS, 24));

        String ser = SerializeObject.objectToString(dailyTimerModels);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            SerializeObject.WriteSettings(context, ser, Consts.dailyTimerFile);
        } else {
            SerializeObject.WriteSettings(context, "", Consts.dailyTimerFile);
        }

        // }

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            btnLogin.setText(getResources().getString(R.string.log_out));
        } else {
            btnLogin.setText(getResources().getString(R.string.login));
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver2, new IntentFilter(Consts.isServerUpdated));
        // LocalBroadcastManager.getInstance(context).registerReceiver(fetchAppDetailsReceiver, new IntentFilter(Consts.app_category));

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
                    try {
                        startActivity(selector);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                }
            }
        }

        try {
            VPNService.Companion.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> appUsageDatesList = sessionManager.getAppUsageDates();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        if (appUsageDatesList != null && appUsageDatesList.size() > 0) {
            if (appUsageDatesList.size() >= 4) {
                Intent intent = new Intent(context, SubscriptionPlanInfoActivity.class);
                intent.putExtra(Consts.from_settings, true);
                intent.putExtra(Consts.is_launcher, false);
                startActivity(intent);
                sessionManager.setAppUsageDatesList("");
            } else {
                if (!appUsageDatesList.contains(currentDate)) {
                    appUsageDatesList.add(currentDate);
                    sessionManager.setAppUsageDatesList(new Gson().toJson(appUsageDatesList));
                }
            }
        } else {
            appUsageDatesList.add(currentDate);
            sessionManager.setAppUsageDatesList(new Gson().toJson(appUsageDatesList));
        }

        if (!Settings.canDrawOverlays(context)) {
            Utils.showOverlayPermissionPopup(context);
        }

        CustomDashboardApplication.enableBackKey(context);
    }

    BroadcastReceiver fetchAppDetailsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int count = intent.getIntExtra(Consts.local_apps_count, 0);
                if (count == resolveInfoList.size()) {
                    dialogLoader.hideProgressDialog();
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

    private void initWithMessageDialog() {
        alertMessageDialog = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_progress_message_dialog, null);
        alertMessageDialog.setView(dialogView);
        alertMessageDialog.setCancelable(false);

        ProgressBar dialog_progressBar = dialogView.findViewById(R.id.dialog_progressBar);
        dialog_progressBar.setIndeterminate(true);

        alertMessage = alertMessageDialog.create();
        alertMessage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertMessage.show();
    }


    // SHOW OPEN SETTINGS DIALOG
    void showOpenSettingsDialog() {
        alertDialogSettings = new AppCompatDialog(context);
        alertDialogSettings.setContentView(R.layout.launcher_pop_up);

        Button btnDone = alertDialogSettings.findViewById(R.id.done_btn);
        btnDone.setOnClickListener(v -> {
       /*     if (isMyLauncherDefault()) {
                finish();
            } else {
                resetPreferredLauncherAndOpenChooser(context);
            }*/
            finish();
            alertDialogSettings.dismiss();
        });

        alertDialogSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogSettings.getWindow().setGravity(Gravity.CENTER);
        alertDialogSettings.show();
    }

    // SHOW SECURE BROWSER DIALOG
    void showSecureBrowserDialog() {
        alertDialogSecureBrowser = new AppCompatDialog(context);
        alertDialogSecureBrowser.setContentView(R.layout.item_secure_browser);

        CheckBox cbSecBrowser = alertDialogSecureBrowser.findViewById(R.id.sec_browser_switch);
        Button btnDone = alertDialogSecureBrowser.findViewById(R.id.done_btn);

        if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
            CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 1);
            cbSecBrowser.setChecked(true);
        } else {
            CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 0);
            cbSecBrowser.setChecked(false);
        }

        cbSecBrowser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 1);
            } else {
                CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 0);
            }
        });

        btnDone.setOnClickListener(v -> {
            alertDialogSecureBrowser.dismiss();
            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                apiManager.updateProfileSettings();
            }
        });

        alertDialogSecureBrowser.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogSecureBrowser.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogSecureBrowser.getWindow().setGravity(Gravity.CENTER);
        alertDialogSecureBrowser.show();
    }

    // SHOW LOGGED IN POP UP
    void showLoggedInDialog() {
        alertDialogLoggedIn = new AppCompatDialog(context);
        alertDialogLoggedIn.setContentView(R.layout.custom_popup_global);

        Button btnLogin = alertDialogLoggedIn.findViewById(R.id.login_btn);
        Button btnCancel = alertDialogLoggedIn.findViewById(R.id.cancel_btn);

        btnLogin.setOnClickListener(v -> {
            alertDialogLoggedIn.dismiss();
            isHomeApp = 1;
            Intent intent = new Intent(context, Login.class);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> alertDialogLoggedIn.dismiss());

        alertDialogLoggedIn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogLoggedIn.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogLoggedIn.getWindow().setGravity(Gravity.CENTER);
        alertDialogLoggedIn.show();
    }

    void showGoogleLoggedInDialog() {
        alertDialogGoogleLoggedIn = new AppCompatDialog(context);
        alertDialogGoogleLoggedIn.setContentView(R.layout.custom_google_login_popup);

        Button btnLogin = alertDialogGoogleLoggedIn.findViewById(R.id.login_btn);

        btnLogin.setOnClickListener(v -> alertDialogGoogleLoggedIn.dismiss());

        alertDialogGoogleLoggedIn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogGoogleLoggedIn.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogGoogleLoggedIn.getWindow().setGravity(Gravity.CENTER);
        alertDialogGoogleLoggedIn.show();
    }

    void showAdvanceSettingsDialog() {
        try {
            alertDialogAdvanceSettings = new AppCompatDialog(context);
            alertDialogAdvanceSettings.setContentView(R.layout.advance_settings_pop_up);

            final LinearLayout llAdvanceSettings = alertDialogAdvanceSettings.findViewById(R.id.ll_advance_settings);
            LinearLayout llAdvSettingsView = alertDialogAdvanceSettings.findViewById(R.id.ll_system_settings);
            final LinearLayout llWipe = alertDialogAdvanceSettings.findViewById(R.id.ll_wipe);
            LinearLayout llWipeView = alertDialogAdvanceSettings.findViewById(R.id.ll_data_wipe);
            final View advSettingsView = alertDialogAdvanceSettings.findViewById(R.id.system_settings_view);
            final View wipeView = alertDialogAdvanceSettings.findViewById(R.id.data_wipe_view);
            final Button btnAction = alertDialogAdvanceSettings.findViewById(R.id.btn_action);

            selectedOption = 0;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            llAdvSettingsView.setOnClickListener(view -> {
                selectedOption = 0;
                llAdvanceSettings.setVisibility(View.VISIBLE);
                advSettingsView.setVisibility(View.VISIBLE);
                llWipe.setVisibility(View.GONE);
                wipeView.setVisibility(View.INVISIBLE);
                btnAction.setText(context.getResources().getString(R.string.apply_settings));
            });

            llWipeView.setOnClickListener(view -> {
                selectedOption = 1;
                llAdvanceSettings.setVisibility(View.GONE);
                advSettingsView.setVisibility(View.INVISIBLE);
                llWipe.setVisibility(View.VISIBLE);
                wipeView.setVisibility(View.VISIBLE);
                btnAction.setText(context.getResources().getString(R.string.wipe));
            });

            if (CustomDashboardApplication.isDeviceSamsung()) {
                llAdvSettingsView.performClick();
                llAdvSettingsView.setVisibility(View.VISIBLE);
            } else {
                llWipeView.performClick();
                llAdvSettingsView.setVisibility(View.GONE);
                llWipeView.setLayoutParams(params);
            }

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

            if (advanceSettingsModels.size() > 0) {

            } else {
                advanceSettingsModels.add(0, new AdvanceSettingsModel(0, 0, context.getResources().getString(R.string.factory_reset_status), false));
                advanceSettingsModels.add(1, new AdvanceSettingsModel(1, 0, context.getResources().getString(R.string.disable_home_key), false));
                advanceSettingsModels.add(2, new AdvanceSettingsModel(2, 0, context.getResources().getString(R.string.disable_volume_down_key), false));
                advanceSettingsModels.add(3, new AdvanceSettingsModel(3, 0, context.getResources().getString(R.string.disable_status_bar), false));
                //advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0,context.getResources().getString(R.string.disable_s_voice), false));
                advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0, context.getResources().getString(R.string.disable_over_the_air), false));
                advanceSettingsModels.add(5, new AdvanceSettingsModel(5, 0, context.getResources().getString(R.string.disable_back_key), false));
                advanceSettingsModels.add(6, new AdvanceSettingsModel(6, 0, context.getResources().getString(R.string.disable_power_key), false));
                //advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0,context.getResources().getString(R.string.disable_cardboard_on_gear_vr), false));
                advanceSettingsModels.add(7, new AdvanceSettingsModel(7, 0, context.getResources().getString(R.string.disable_recents_key), false));
                advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0, context.getResources().getString(R.string.disable_volume_up_key), false));
            }

            ArrayList<WipeButtonsModel> wipeButtonsModels = new ArrayList<>();
            String wipeDataSer = SerializeObject.ReadSettings(context, Consts.wipeDataFile);
            if (wipeDataSer != null && !wipeDataSer.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(wipeDataSer);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    wipeButtonsModels = (ArrayList<WipeButtonsModel>) obj;
                }
            }

            if (wipeButtonsModels.size() > 0) {

            } else {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.uninstall_apps), false));
                    wipeButtonsModels.add(1, new WipeButtonsModel(1, 0, context.getResources().getString(R.string.clean_apps_data), false));
                    wipeButtonsModels.add(2, new WipeButtonsModel(2, 0, context.getResources().getString(R.string.delete_file_folder), false));
                    /*if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                        wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.remove_call_logs), false));
                        wipeButtonsModels.add(4, new WipeButtonsModel(4, 0, context.getResources().getString(R.string.clean_web_cookies), false));
                    } else {*/
                    wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.clean_web_cookies), false));
//                    }
                } else {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.delete_file_folder), false));
                }
            }

            llAdvanceSettings.removeAllViews();
            for (int i = 0; i < advanceSettingsModels.size(); i++) {
                View dynamicView = LayoutInflater.from(context).inflate(R.layout.item_advance_settings, llAdvanceSettings, false);

                TextView txSettingsName = dynamicView.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = dynamicView.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(advanceSettingsModels.get(i).getAdvanceSettingName());

                cbSettings.setChecked(advanceSettingsModels.get(i).isChecked());


                final int finalI = i;
                final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels = advanceSettingsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
//                        if (sessionManager.getIsSubscriptionEnabled()) {
                    if (b) {
                        finalAdvanceSettingsModels.get(finalI).setChecked(true);
                        finalAdvanceSettingsModels.get(finalI).setStatus(1);
                    } else {
                        finalAdvanceSettingsModels.get(finalI).setChecked(false);
                        finalAdvanceSettingsModels.get(finalI).setStatus(0);
                    }
                    /*} else {
                        cbSettings.setChecked(false);
                        showSubscriptionAlert(CustomDashboardSettings.this);
                    }*/
                });

                llAdvanceSettings.addView(dynamicView);
            }

            llWipe.removeAllViews();
            for (int j = 0; j < wipeButtonsModels.size(); j++) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_data_wipe, llWipe, false);

                TextView txSettingsName = view.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = view.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(wipeButtonsModels.get(j).getSettingName());

                cbSettings.setChecked(wipeButtonsModels.get(j).isSelected());

                final int finalI1 = j;
                view.setOnClickListener(view1 -> {
                    if (finalI1 == 0) {
                        currentWipeSelection = 0;
                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            showUnInstallDialog();
                        } else {
                            showDeleteFileFolderDialog();
                        }
                    } else if (finalI1 == 1) {
                        currentWipeSelection = 1;
                        showClearAppsDataDialog();
                    } else if (finalI1 == 2) {
                        currentWipeSelection = 2;
                        showDeleteFileFolderDialog();
                    } else if (finalI1 == 3) {
                        currentWipeSelection = 3;
                       /* if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                            showCallLogsDialog();
                        } else {*/
                        showWebCookieDialog();
                        //}
                    } else if (finalI1 == 4) {
                        currentWipeSelection = 4;
                        showWebCookieDialog();
                    }
                });

                final int finalJ = j;
                final ArrayList<WipeButtonsModel> finalWipeButtonsModels = wipeButtonsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        finalWipeButtonsModels.get(finalJ).setSelected(true);
                        finalWipeButtonsModels.get(finalJ).setStatus(1);
                    } else {
                        finalWipeButtonsModels.get(finalJ).setSelected(false);
                        finalWipeButtonsModels.get(finalJ).setStatus(0);
                    }
                });

                llWipe.addView(view);
            }

            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels1 = advanceSettingsModels;
            final ArrayList<WipeButtonsModel> finalWipeButtonsModels1 = wipeButtonsModels;
            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels2 = advanceSettingsModels;
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (selectedOption == 0) {
                        if (Consts.IS_BUSINESS_APP == 1) {

                            // DIJO HERE SUPER LOCK STATE SET FOR KNOX ACTIVATED DEVICES

                            if (CustomDashboardApplication.isDeviceSamsung()) {
                                SuperLockState superLockState = SuperLockState.getInstance(context);
                                if (superLockState.supportedActivation()) {
                                    if (superLockState.isESDKLicenseActivated()) {
                                        // SET FACTORY RESET STATUS
                                        superLockState.allowFactoryReset(!finalAdvanceSettingsModels2.get(0).isChecked());

                                        // SET HOME KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(1).isChecked(), KeyEvent.KEYCODE_HOME, context);

                                        // SET VOLUME DOWN KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(2).isChecked(), KeyEvent.KEYCODE_VOLUME_DOWN, context);

                                        // SET STATUS BAR STATUS
                                        superLockState.allowStatusBar(!finalAdvanceSettingsModels2.get(3).isChecked());

                                        // SET S-VOICE STATUS
                                        //superLockState.allowSVoice(!finalAdvanceSettingsModels2.get(4).isChecked());

                                        // SET OS UPDATES STATUS
                                        superLockState.allowOTAUpgrade(!finalAdvanceSettingsModels2.get(4).isChecked());

                                        // SET BACK KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(5).isChecked(), KeyEvent.KEYCODE_BACK, context);

                                        // SET POWER KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(6).isChecked(), KeyEvent.KEYCODE_POWER, context);

                                        // SET CARDBOARD VR STATUS

                                        //SET DISABLE KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(7).isChecked(), KeyEvent.KEYCODE_MENU, context);

                                        // SET VOLUME UP KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(8).isChecked(), KeyEvent.KEYCODE_VOLUME_UP, context);
                                    }
                                }
                            }
                        }
                        for (int i = 0; i < finalAdvanceSettingsModels1.size(); i++) {
                            if (finalAdvanceSettingsModels1.get(i).isChecked()) {
                                Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                            } else {
                                Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                            }
                        }

                        String advanceSettingsStr = SerializeObject.objectToString(finalAdvanceSettingsModels1);
                        if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.advanceSettingsFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.advanceSettingsFile);
                        }
                        apiManager.checkPaymentStatus();
                    } else if (selectedOption == 1) {
                        for (int j = 0; j < finalWipeButtonsModels1.size(); j++) {
                            if (finalWipeButtonsModels1.get(j).isSelected()) {
                                Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                            } else {
                                Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                            }
                        }

                        String advanceSettingsStr = SerializeObject.objectToString(finalWipeButtonsModels1);
                        if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.wipeDataFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.wipeDataFile);
                        }
                        apiManager.checkPaymentStatus();
                    }
                    alertDialogAdvanceSettings.dismiss();
                }
            });

            alertDialogAdvanceSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAdvanceSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogAdvanceSettings.getWindow().setGravity(Gravity.CENTER);
            alertDialogAdvanceSettings.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showHardwareSettingsDialog() {
        try {
            alertDialogAdvanceSettings = new AppCompatDialog(context);
            alertDialogAdvanceSettings.setContentView(R.layout.advance_settings_pop_up);

            TextView tvTitle = alertDialogAdvanceSettings.findViewById(R.id.tv_title);
            final LinearLayout llAdvanceSettings = alertDialogAdvanceSettings.findViewById(R.id.ll_advance_settings);
            LinearLayout llAdvSettingsView = alertDialogAdvanceSettings.findViewById(R.id.ll_system_settings);
            final LinearLayout llWipe = alertDialogAdvanceSettings.findViewById(R.id.ll_wipe);
            LinearLayout llWipeView = alertDialogAdvanceSettings.findViewById(R.id.ll_data_wipe);
            final View advSettingsView = alertDialogAdvanceSettings.findViewById(R.id.system_settings_view);
            final View wipeView = alertDialogAdvanceSettings.findViewById(R.id.data_wipe_view);
            final Button btnAction = alertDialogAdvanceSettings.findViewById(R.id.btn_action);

            tvTitle.setText(getString(R.string.hardware_settings));

            selectedOption = 0;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            llAdvSettingsView.setOnClickListener(view -> {
                selectedOption = 0;
                llAdvanceSettings.setVisibility(View.VISIBLE);
                advSettingsView.setVisibility(View.VISIBLE);
                llWipe.setVisibility(View.GONE);
                wipeView.setVisibility(View.INVISIBLE);
                btnAction.setText(context.getResources().getString(R.string.apply_settings));
            });

            llWipeView.setOnClickListener(view -> {
                selectedOption = 1;
                llAdvanceSettings.setVisibility(View.GONE);
                advSettingsView.setVisibility(View.INVISIBLE);
                llWipe.setVisibility(View.VISIBLE);
                wipeView.setVisibility(View.VISIBLE);
                btnAction.setText(context.getResources().getString(R.string.wipe));
            });

            //if (CustomDashboardApplication.isDeviceSamsung()) {
            llAdvSettingsView.performClick();
            llAdvSettingsView.setVisibility(View.VISIBLE);
            llWipeView.setVisibility(View.GONE);
            llAdvSettingsView.setLayoutParams(params);
            /*} else {
                llWipeView.performClick();
                llAdvSettingsView.setVisibility(View.GONE);
                llWipeView.setLayoutParams(params);
            }*/

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

            if (advanceSettingsModels.size() > 0) {

            } else {
                advanceSettingsModels.add(0, new AdvanceSettingsModel(0, 0, context.getResources().getString(R.string.factory_reset_status), false));
                advanceSettingsModels.add(1, new AdvanceSettingsModel(1, 0, context.getResources().getString(R.string.disable_home_key), false));
                advanceSettingsModels.add(2, new AdvanceSettingsModel(2, 0, context.getResources().getString(R.string.disable_volume_down_key), false));
                advanceSettingsModels.add(3, new AdvanceSettingsModel(3, 0, context.getResources().getString(R.string.disable_status_bar), false));
                //advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0,context.getResources().getString(R.string.disable_s_voice), false));
                advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0, context.getResources().getString(R.string.disable_over_the_air), false));
                advanceSettingsModels.add(5, new AdvanceSettingsModel(5, 0, context.getResources().getString(R.string.disable_back_key), false));
                advanceSettingsModels.add(6, new AdvanceSettingsModel(6, 0, context.getResources().getString(R.string.disable_power_key), false));
                //advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0,context.getResources().getString(R.string.disable_cardboard_on_gear_vr), false));
                advanceSettingsModels.add(7, new AdvanceSettingsModel(7, 0, context.getResources().getString(R.string.disable_recents_key), false));
                advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0, context.getResources().getString(R.string.disable_volume_up_key), false));
            }

            ArrayList<WipeButtonsModel> wipeButtonsModels = new ArrayList<>();
            String wipeDataSer = SerializeObject.ReadSettings(context, Consts.wipeDataFile);
            if (wipeDataSer != null && !wipeDataSer.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(wipeDataSer);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    wipeButtonsModels = (ArrayList<WipeButtonsModel>) obj;
                }
            }

            if (wipeButtonsModels.size() > 0) {

            } else {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.uninstall_apps), false));
                    wipeButtonsModels.add(1, new WipeButtonsModel(1, 0, context.getResources().getString(R.string.clean_apps_data), false));
                    wipeButtonsModels.add(2, new WipeButtonsModel(2, 0, context.getResources().getString(R.string.delete_file_folder), false));
                    /*if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                        wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.remove_call_logs), false));
                        wipeButtonsModels.add(4, new WipeButtonsModel(4, 0, context.getResources().getString(R.string.clean_web_cookies), false));
                    } else {*/
                    wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.clean_web_cookies), false));
//                    }
                } else {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.delete_file_folder), false));
                }
            }

            llAdvanceSettings.removeAllViews();
            for (int i = 0; i < advanceSettingsModels.size(); i++) {
                View dynamicView = LayoutInflater.from(context).inflate(R.layout.item_advance_settings, llAdvanceSettings, false);

                TextView txSettingsName = dynamicView.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = dynamicView.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(advanceSettingsModels.get(i).getAdvanceSettingName());

                cbSettings.setChecked(advanceSettingsModels.get(i).isChecked());

                final int finalI = i;
                final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels = advanceSettingsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
//                        if (sessionManager.getIsSubscriptionEnabled()) {
                    if (b) {
                        finalAdvanceSettingsModels.get(finalI).setChecked(true);
                        finalAdvanceSettingsModels.get(finalI).setStatus(1);
                    } else {
                        finalAdvanceSettingsModels.get(finalI).setChecked(false);
                        finalAdvanceSettingsModels.get(finalI).setStatus(0);
                    }
                    /*} else {
                        cbSettings.setChecked(false);
                        showSubscriptionAlert(CustomDashboardSettings.this);
                    }*/
                });

                llAdvanceSettings.addView(dynamicView);
            }

            llWipe.removeAllViews();
            for (int j = 0; j < wipeButtonsModels.size(); j++) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_data_wipe, llWipe, false);

                TextView txSettingsName = view.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = view.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(wipeButtonsModels.get(j).getSettingName());

                cbSettings.setChecked(wipeButtonsModels.get(j).isSelected());

                final int finalI1 = j;
                view.setOnClickListener(view1 -> {
                    if (finalI1 == 0) {
                        currentWipeSelection = 0;
                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            showUnInstallDialog();
                        } else {
                            showDeleteFileFolderDialog();
                        }
                    } else if (finalI1 == 1) {
                        currentWipeSelection = 1;
                        showClearAppsDataDialog();
                    } else if (finalI1 == 2) {
                        currentWipeSelection = 2;
                        showDeleteFileFolderDialog();
                    } else if (finalI1 == 3) {
                        currentWipeSelection = 3;
                       /* if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                            showCallLogsDialog();
                        } else {*/
                        showWebCookieDialog();
                        //}
                    } else if (finalI1 == 4) {
                        currentWipeSelection = 4;
                        showWebCookieDialog();
                    }
                });

                final int finalJ = j;
                final ArrayList<WipeButtonsModel> finalWipeButtonsModels = wipeButtonsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        finalWipeButtonsModels.get(finalJ).setSelected(true);
                        finalWipeButtonsModels.get(finalJ).setStatus(1);
                    } else {
                        finalWipeButtonsModels.get(finalJ).setSelected(false);
                        finalWipeButtonsModels.get(finalJ).setStatus(0);
                    }
                });

                llWipe.addView(view);
            }

            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels1 = advanceSettingsModels;
            final ArrayList<WipeButtonsModel> finalWipeButtonsModels1 = wipeButtonsModels;
            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels2 = advanceSettingsModels;
            btnAction.setOnClickListener(view -> {

                if (selectedOption == 0) {
                    if (Consts.IS_BUSINESS_APP == 1) {

                        // DIJO HERE SUPER LOCK STATE SET FOR KNOX ACTIVATED DEVICES

                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            SuperLockState superLockState = SuperLockState.getInstance(context);
                            if (superLockState.supportedActivation()) {
                                if (superLockState.isESDKLicenseActivated()) {
                                    // SET FACTORY RESET STATUS
                                    superLockState.allowFactoryReset(!finalAdvanceSettingsModels2.get(0).isChecked());

                                    // SET HOME KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(1).isChecked(), KeyEvent.KEYCODE_HOME, context);

                                    // SET VOLUME DOWN KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(2).isChecked(), KeyEvent.KEYCODE_VOLUME_DOWN, context);

                                    // SET STATUS BAR STATUS
                                    superLockState.allowStatusBar(!finalAdvanceSettingsModels2.get(3).isChecked());

                                    // SET S-VOICE STATUS
                                    //superLockState.allowSVoice(!finalAdvanceSettingsModels2.get(4).isChecked());

                                    // SET OS UPDATES STATUS
                                    superLockState.allowOTAUpgrade(!finalAdvanceSettingsModels2.get(4).isChecked());

                                    // SET BACK KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(5).isChecked(), KeyEvent.KEYCODE_BACK, context);

                                    // SET POWER KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(6).isChecked(), KeyEvent.KEYCODE_POWER, context);

                                    // SET CARDBOARD VR STATUS

                                    //SET DISABLE KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(7).isChecked(), KeyEvent.KEYCODE_MENU, context);

                                    // SET VOLUME UP KEY STATUS
                                    superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(8).isChecked(), KeyEvent.KEYCODE_VOLUME_UP, context);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < finalAdvanceSettingsModels1.size(); i++) {
                        if (finalAdvanceSettingsModels1.get(i).isChecked()) {
                            Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                        } else {
                            Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                        }
                    }

                    String advanceSettingsStr = SerializeObject.objectToString(finalAdvanceSettingsModels1);
                    if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.advanceSettingsFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.advanceSettingsFile);
                    }
                    apiManager.checkPaymentStatus();
                } else if (selectedOption == 1) {
                    for (int j = 0; j < finalWipeButtonsModels1.size(); j++) {
                        if (finalWipeButtonsModels1.get(j).isSelected()) {
                            Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                        } else {
                            Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                        }
                    }

                    String advanceSettingsStr = SerializeObject.objectToString(finalWipeButtonsModels1);
                    if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.wipeDataFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.wipeDataFile);
                    }
                    apiManager.checkPaymentStatus();
                }
                alertDialogAdvanceSettings.dismiss();
            });

            alertDialogAdvanceSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAdvanceSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogAdvanceSettings.getWindow().setGravity(Gravity.CENTER);
            alertDialogAdvanceSettings.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showWipeSettingsDialog() {
        try {
            alertDialogAdvanceSettings = new AppCompatDialog(context);
            alertDialogAdvanceSettings.setContentView(R.layout.advance_settings_pop_up);

            TextView tvTitle = alertDialogAdvanceSettings.findViewById(R.id.tv_title);
            final LinearLayout llAdvanceSettings = alertDialogAdvanceSettings.findViewById(R.id.ll_advance_settings);
            LinearLayout llAdvSettingsView = alertDialogAdvanceSettings.findViewById(R.id.ll_system_settings);
            final LinearLayout llWipe = alertDialogAdvanceSettings.findViewById(R.id.ll_wipe);
            LinearLayout llWipeView = alertDialogAdvanceSettings.findViewById(R.id.ll_data_wipe);
            final View advSettingsView = alertDialogAdvanceSettings.findViewById(R.id.system_settings_view);
            final View wipeView = alertDialogAdvanceSettings.findViewById(R.id.data_wipe_view);
            final Button btnAction = alertDialogAdvanceSettings.findViewById(R.id.btn_action);

            tvTitle.setText("Wipe Data");
            selectedOption = 0;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            llWipeView.setOnClickListener(view -> {
                selectedOption = 1;
                llAdvanceSettings.setVisibility(View.GONE);
                advSettingsView.setVisibility(View.INVISIBLE);
                llWipe.setVisibility(View.VISIBLE);
                wipeView.setVisibility(View.VISIBLE);
                btnAction.setText(context.getResources().getString(R.string.wipe));
            });


            llWipeView.performClick();
            llAdvSettingsView.setVisibility(View.GONE);
            llWipeView.setLayoutParams(params);

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

            if (advanceSettingsModels.size() > 0) {

            } else {
                advanceSettingsModels.add(0, new AdvanceSettingsModel(0, 0, context.getResources().getString(R.string.factory_reset_status), false));
                advanceSettingsModels.add(1, new AdvanceSettingsModel(1, 0, context.getResources().getString(R.string.disable_home_key), false));
                advanceSettingsModels.add(2, new AdvanceSettingsModel(2, 0, context.getResources().getString(R.string.disable_volume_down_key), false));
                advanceSettingsModels.add(3, new AdvanceSettingsModel(3, 0, context.getResources().getString(R.string.disable_status_bar), false));
                //advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0,context.getResources().getString(R.string.disable_s_voice), false));
                advanceSettingsModels.add(4, new AdvanceSettingsModel(4, 0, context.getResources().getString(R.string.disable_over_the_air), false));
                advanceSettingsModels.add(5, new AdvanceSettingsModel(5, 0, context.getResources().getString(R.string.disable_back_key), false));
                advanceSettingsModels.add(6, new AdvanceSettingsModel(6, 0, context.getResources().getString(R.string.disable_power_key), false));
                //advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0,context.getResources().getString(R.string.disable_cardboard_on_gear_vr), false));
                advanceSettingsModels.add(7, new AdvanceSettingsModel(7, 0, context.getResources().getString(R.string.disable_recents_key), false));
                advanceSettingsModels.add(8, new AdvanceSettingsModel(8, 0, context.getResources().getString(R.string.disable_volume_up_key), false));
            }

            ArrayList<WipeButtonsModel> wipeButtonsModels = new ArrayList<>();
            String wipeDataSer = SerializeObject.ReadSettings(context, Consts.wipeDataFile);
            if (wipeDataSer != null && !wipeDataSer.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(wipeDataSer);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    wipeButtonsModels = (ArrayList<WipeButtonsModel>) obj;
                }
            }

            if (wipeButtonsModels.size() > 0) {

            } else {
                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.uninstall_apps), false));
                    wipeButtonsModels.add(1, new WipeButtonsModel(1, 0, context.getResources().getString(R.string.clean_apps_data), false));
                    wipeButtonsModels.add(2, new WipeButtonsModel(2, 0, context.getResources().getString(R.string.delete_file_folder), false));
                    /*if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                        wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.remove_call_logs), false));
                        wipeButtonsModels.add(4, new WipeButtonsModel(4, 0, context.getResources().getString(R.string.clean_web_cookies), false));
                    } else {*/
                    wipeButtonsModels.add(3, new WipeButtonsModel(3, 0, context.getResources().getString(R.string.clean_web_cookies), false));
//                    }
                } else {
                    wipeButtonsModels.add(0, new WipeButtonsModel(0, 0, context.getResources().getString(R.string.delete_file_folder), false));
                }
            }

            llAdvanceSettings.removeAllViews();
            for (int i = 0; i < advanceSettingsModels.size(); i++) {
                View dynamicView = LayoutInflater.from(context).inflate(R.layout.item_advance_settings, llAdvanceSettings, false);

                TextView txSettingsName = dynamicView.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = dynamicView.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(advanceSettingsModels.get(i).getAdvanceSettingName());

                cbSettings.setChecked(advanceSettingsModels.get(i).isChecked());


                final int finalI = i;
                final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels = advanceSettingsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
//                        if (sessionManager.getIsSubscriptionEnabled()) {
                    if (b) {
                        finalAdvanceSettingsModels.get(finalI).setChecked(true);
                        finalAdvanceSettingsModels.get(finalI).setStatus(1);
                    } else {
                        finalAdvanceSettingsModels.get(finalI).setChecked(false);
                        finalAdvanceSettingsModels.get(finalI).setStatus(0);
                    }
                    /*} else {
                        cbSettings.setChecked(false);
                        showSubscriptionAlert(CustomDashboardSettings.this);
                    }*/
                });

                llAdvanceSettings.addView(dynamicView);
            }

            llWipe.removeAllViews();
            for (int j = 0; j < wipeButtonsModels.size(); j++) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_data_wipe, llWipe, false);

                TextView txSettingsName = view.findViewById(R.id.tx_advance_settings);
                final CheckBox cbSettings = view.findViewById(R.id.iv_advance_settings);

                txSettingsName.setText(wipeButtonsModels.get(j).getSettingName());

                cbSettings.setChecked(wipeButtonsModels.get(j).isSelected());

                final int finalI1 = j;
                view.setOnClickListener(view1 -> {
                    if (finalI1 == 0) {
                        currentWipeSelection = 0;
                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            showUnInstallDialog();
                        } else {
                            showDeleteFileFolderDialog();
                        }
                    } else if (finalI1 == 1) {
                        currentWipeSelection = 1;
                        showClearAppsDataDialog();
                    } else if (finalI1 == 2) {
                        currentWipeSelection = 2;
                        showDeleteFileFolderDialog();
                    } else if (finalI1 == 3) {
                        currentWipeSelection = 3;
                       /* if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                            showCallLogsDialog();
                        } else {*/
                        showWebCookieDialog();
                        //}
                    } else if (finalI1 == 4) {
                        currentWipeSelection = 4;
                        showWebCookieDialog();
                    }
                });

                final int finalJ = j;
                final ArrayList<WipeButtonsModel> finalWipeButtonsModels = wipeButtonsModels;
                cbSettings.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        finalWipeButtonsModels.get(finalJ).setSelected(true);
                        finalWipeButtonsModels.get(finalJ).setStatus(1);
                    } else {
                        finalWipeButtonsModels.get(finalJ).setSelected(false);
                        finalWipeButtonsModels.get(finalJ).setStatus(0);
                    }
                });

                llWipe.addView(view);
            }

            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels1 = advanceSettingsModels;
            final ArrayList<WipeButtonsModel> finalWipeButtonsModels1 = wipeButtonsModels;
            final ArrayList<AdvanceSettingsModel> finalAdvanceSettingsModels2 = advanceSettingsModels;
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (selectedOption == 0) {
                        if (Consts.IS_BUSINESS_APP == 1) {

                            // DIJO HERE SUPER LOCK STATE SET FOR KNOX ACTIVATED DEVICES

                            if (CustomDashboardApplication.isDeviceSamsung()) {
                                SuperLockState superLockState = SuperLockState.getInstance(context);
                                if (superLockState.supportedActivation()) {
                                    if (superLockState.isESDKLicenseActivated()) {
                                        // SET FACTORY RESET STATUS
                                        superLockState.allowFactoryReset(!finalAdvanceSettingsModels2.get(0).isChecked());

                                        // SET HOME KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(1).isChecked(), KeyEvent.KEYCODE_HOME, context);

                                        // SET VOLUME DOWN KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(2).isChecked(), KeyEvent.KEYCODE_VOLUME_DOWN, context);

                                        // SET STATUS BAR STATUS
                                        superLockState.allowStatusBar(!finalAdvanceSettingsModels2.get(3).isChecked());

                                        // SET S-VOICE STATUS
                                        //superLockState.allowSVoice(!finalAdvanceSettingsModels2.get(4).isChecked());

                                        // SET OS UPDATES STATUS
                                        superLockState.allowOTAUpgrade(!finalAdvanceSettingsModels2.get(4).isChecked());

                                        // SET BACK KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(5).isChecked(), KeyEvent.KEYCODE_BACK, context);

                                        // SET POWER KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(6).isChecked(), KeyEvent.KEYCODE_POWER, context);

                                        // SET CARDBOARD VR STATUS

                                        //SET DISABLE KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(7).isChecked(), KeyEvent.KEYCODE_MENU, context);

                                        // SET VOLUME UP KEY STATUS
                                        superLockState.allowHardwareKeys(!finalAdvanceSettingsModels2.get(8).isChecked(), KeyEvent.KEYCODE_VOLUME_UP, context);
                                    }
                                }
                            }
                        }
                        for (int i = 0; i < finalAdvanceSettingsModels1.size(); i++) {
                            if (finalAdvanceSettingsModels1.get(i).isChecked()) {
                                Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                            } else {
                                Log.e("AdvanceSettings", finalAdvanceSettingsModels1.get(i).getAdvanceSettingName() + ":" + finalAdvanceSettingsModels1.get(i).isChecked());
                            }
                        }

                        String advanceSettingsStr = SerializeObject.objectToString(finalAdvanceSettingsModels1);
                        if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.advanceSettingsFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.advanceSettingsFile);
                        }
                        apiManager.checkPaymentStatus();
                    } else if (selectedOption == 1) {
                        for (int j = 0; j < finalWipeButtonsModels1.size(); j++) {
                            if (finalWipeButtonsModels1.get(j).isSelected()) {
                                Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                            } else {
                                Log.e("WipeData", finalWipeButtonsModels1.get(j).getSettingName() + ":" + finalWipeButtonsModels1.get(j).isSelected());
                            }
                        }

                        String advanceSettingsStr = SerializeObject.objectToString(finalWipeButtonsModels1);
                        if (advanceSettingsStr != null && !advanceSettingsStr.equalsIgnoreCase("")) {
                            SerializeObject.WriteSettings(context, advanceSettingsStr, Consts.wipeDataFile);
                        } else {
                            SerializeObject.WriteSettings(context, "", Consts.wipeDataFile);
                        }
                        apiManager.checkPaymentStatus();
                    }
                    alertDialogAdvanceSettings.dismiss();
                }
            });

            alertDialogAdvanceSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAdvanceSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogAdvanceSettings.getWindow().setGravity(Gravity.CENTER);
            alertDialogAdvanceSettings.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, getPackageName());

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            CustomDashboardApplication.setInt(context, Consts.IS_EXIT, 1);
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
                startActivity(selector);
            }

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // REQUEST PERMISSIONS
    public void requestpermission() {
        // APP PERMISSIONS
        String[] permit;
        /*if (Utils.isTiramisu()) {
            permit = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.CAMERA};
        } else {
            permit = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
        }*/
        permit = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permit, request);
    }

    /*public void requestReadLogsPermission() {
        ActivityCompat.requestPermissions(this, readLogpermit, readLogsRequest);
    }*/

    @Override
    public void OnSelectItem(int pos) {

        int currentPos = pos;

    }

    // SHOW ENTER PASSWORD DIALOG
    void showImportExportDialog() {
        try {
            alertDialogImportExport = new AppCompatDialog(context);
            alertDialogImportExport.setContentView(R.layout.import_export_file);

            RadioGroup rgImportExport = alertDialogImportExport.findViewById(R.id.rg_setting);
            RadioGroup rgInternalExternal = alertDialogImportExport.findViewById(R.id.rg_setting_folder);
            edPath = alertDialogImportExport.findViewById(R.id.ed_path);
            final EditText edFileName = alertDialogImportExport.findViewById(R.id.ed_file);
            ImageView ivFolder = alertDialogImportExport.findViewById(R.id.iv_folder);
            final LinearLayout llImportFile = alertDialogImportExport.findViewById(R.id.ll_import_file);
            //final LinearLayout lLExportFile = alertDialogImportExport.findViewById(R.id.ll_export_file);
            final LinearLayout llExport = alertDialogImportExport.findViewById(R.id.ll_export);
            final Button btnDone = alertDialogImportExport.findViewById(R.id.done_btn);


            rgImportExport.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_import) {
                    selectedMethod = 0;
                    llImportFile.setVisibility(View.VISIBLE);
                    //lLExportFile.setVisibility(View.GONE);
                    llExport.setVisibility(View.GONE);
                    btnDone.setText(R.string.import_file);
                } else if (checkedId == R.id.rb_external) {
                    selectedMethod = 1;
                    llImportFile.setVisibility(View.GONE);
                    //lLExportFile.setVisibility(View.VISIBLE);
                    llExport.setVisibility(View.VISIBLE);
                    btnDone.setText(R.string.export_file);
                }
            });

            rgInternalExternal.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_internal_storage) {
                    isExportType = 0;
                    //exportInternalStorage();
                } else if (checkedId == R.id.rb_external_storage) {
                    isExportType = 1;
                    //exportExternalStorage();
                }
            });

            ivFolder.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            });


            btnDone.setOnClickListener(v -> {
                if (selectedMethod == 0) {
                    importFile();
                    alertDialogImportExport.dismiss();
                } else if (selectedMethod == 1) {
                    if (edFileName.getText().toString().trim().length() == 0) {
                        edFileName.setError(getResources().getString(R.string.enter_file_name));
                        edFileName.setFocusable(true);
                    } else {
                        if (isExportType == 0) {
                            exportExternalStorage(edFileName.getText().toString());
                        } else if (isExportType == 1) {
                            exportExternalStorage(edFileName.getText().toString());
                        }
                        alertDialogImportExport.dismiss();
                    }
                }

            });
            alertDialogImportExport.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogImportExport.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogImportExport.getWindow().setGravity(Gravity.CENTER);
            alertDialogImportExport.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // GET FILE
    public void importFile() {
        try {
            String path = selectedPath;
            String f = null;
            try {
                f = new File(path).getCanonicalFile().getName();

                String pathFile = Environment.getExternalStorageDirectory().toString() + "/" + path;
                File directory = new File(pathFile);
                File[] list = directory.listFiles();
                if (list == null) {

                }

                List<String> fileNams = new ArrayList<>();
                for (int i = 0; i < list.length; ++i) {
                    if (list[i].isFile()) {
                        String name = list[i].getPath();
                        if (!name.isEmpty()) {
                            String extension = name.substring(name.lastIndexOf("."));
                            if (!extension.isEmpty()) {
                                if (extension.contentEquals(".json") || extension.contentEquals(".txt")) {
                                    fileNams.add(name);
                                    importFileData(selectedPath);
                                    break;
                                }
                            }
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // EXPORT IN EXTERNAL STORAGE
    public void exportExternalStorage(String fileName) {

        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        if (isSDSupportedDevice && isSDPresent) {
            // SD-card is present

          /*  File directory = new File(Environment.getExternalStorageDirectory(),
                    getResources().getString(R.string.app_name) +"/"+getResources().getString(R.string.app_analytics));*/
            File directory = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.folder_name));
            if (!directory.exists())
                directory.getParentFile().mkdirs();

            Gson gson = new GsonBuilder().create();

            /*String ser = SerializeObject.ReadSettings(context, "appanalytics.dat");
            if (ser != null && !ser.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    appModels = (ArrayList<ApplicationTimerModel>) obj;
                }
            }*/

            ArrayList<AppModel> appModels = new ArrayList<>();

            String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
            if (ser != null && !ser.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something

                    appModels = (ArrayList<AppModel>) obj;
                }
            }

            // APPLICATIONS JSON ARRAY
            JSONArray appsArray = new JSONArray();

            ArrayList<ApplicationTimerModel> userModels = new ArrayList<>();

            String serAnalytics = SerializeObject.ReadSettings(context, Consts.appAnalyticsFile);
            if (serAnalytics != null && !serAnalytics.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(serAnalytics);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something

                    Collections.sort((ArrayList<ApplicationTimerModel>) obj, (lhs, rhs) -> lhs.getEndTime().compareTo(rhs.getEndTime()));

                    userModels = (ArrayList<ApplicationTimerModel>) obj;
                }
            }

            if (userModels.size() > 0) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JsonArray appDataAna;
            JsonParser jsonParser = new JsonParser();
            appDataAna = (JsonArray) jsonParser.parse(appsArray.toString());

            if (appModels != null) {
                if (appModels.size() > 0) {

                    for (int n = 0; n < appModels.size(); n++) {
                        int position = 0;
                        if (appModels.get(n).getColumn() > 3) {
                            appModels.get(n).setColumn(3);
                        }
                        if (appModels.get(n).getRow() > 1) {
                            position = ((appModels.get(n).getRow() - 1) * 3 + appModels.get(n).getColumn()) - 1;
                        } else {
                            position = appModels.get(n).getColumn() - 1;
                        }

                        appModels.get(n).setPosition(position);
                        appModels.get(n).setLocation(appModels.get(n).getRow() + "," + appModels.get(n).getColumn());
                    }

                    JsonArray myCustomArray = gson.toJsonTree(appModels).getAsJsonArray();

                    JsonObject settingObj = new JsonObject();
                    settingObj.addProperty(Consts.status_bar_status, CustomDashboardApplication.getInt(context, Consts.APP_INFO));
                    settingObj.addProperty(Consts.volume_key_status, CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME));
                    settingObj.addProperty(Consts.status_bar_text, CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
                    settingObj.addProperty(Consts.schedule_type, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE));
                    settingObj.addProperty(Consts.schedule_value, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));
                    settingObj.addProperty(Consts.battery_status, CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS));
                    settingObj.addProperty(Consts.battery_level, CustomDashboardApplication.getString(context, Consts.BATTERY_LEVEL));
                    settingObj.addProperty(Consts.date_time_setting, CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING));
                    settingObj.addProperty(Consts.shortcuts_status, CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD));
                    try {
                        //settingObj.addProperty(Consts.pin, AESCrypt.decrypt(getApplicationContext(), CustomDashboardApplication.getString(context, Consts.PWD_ENCRY)));
                        settingObj.addProperty(Consts.pin, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    JsonObject detailsObj = new JsonObject();
                    detailsObj.add(Consts.data, myCustomArray);
                    detailsObj.add(Consts.app_settings, settingObj);
                    detailsObj.add(Consts.apps_analytics_data, appDataAna);

                    JsonObject appData = new JsonObject();
                    appData.add(Consts.app_data, detailsObj);

                    String str = appData.toString();
                    File file;
                    try {
                        file = new File(directory, fileName);

                        FileWriter fileWriter = new FileWriter(file);
                  /*  BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("{}");
                    bufferedWriter.close();*/
                        fileWriter.write(str);
                        fileWriter.flush();
                        fileWriter.close();

                    /*outputStream = new FileOutputStream(file);
                    outputStream.write(str.getBytes());
                    outputStream.close();*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {

            //Toast.makeText(context, getResources().getString(R.string.external_error), Toast.LENGTH_SHORT).show();

           /* File directory = new File(Environment.getExternalStorageDirectory(),  getResources().getString(R.string.app_name) +"/"+getResources().getString(R.string.app_analytics));
            if (!directory.exists())
                directory.mkdirs();


            Gson gson = new GsonBuilder().create();

            String ser = SerializeObject.ReadSettings(context, "appanalytics.dat");
            if (ser != null && !ser.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something

                   *//* Collections.sort((ArrayList<ApplicationTimerModel>) obj, new Comparator<ApplicationTimerModel>() {
                        @Override
                        public int compare(ApplicationTimerModel lhs, ApplicationTimerModel rhs) {
                            return lhs.getEndTime().compareTo(rhs.getEndTime());
                        }
                    });*//*

                    appModels = (ArrayList<ApplicationTimerModel>) obj;
                }
            }
            if (appModels != null) {
                if (appModels.size() > 0) {

                    JsonArray myCustomArray = gson.toJsonTree(appModels).getAsJsonArray();

                    JsonObject settingObj = new JsonObject();
                    settingObj.addProperty(Consts.IS_GOOGLE_LOGIN, CustomDashboardApplication.getInt(context, Consts.IS_GOOGLE_LOGIN));
                    settingObj.addProperty(Consts.IS_HELP_ENABLED, CustomDashboardApplication.getHelpEnabled(context, Consts.IS_HELP_ENABLED));
                    settingObj.addProperty(Consts.IS_PWD, CustomDashboardApplication.getInt(context, Consts.IS_PWD));
                    settingObj.addProperty(Consts.PASSWORD_AT_LAUNCH, CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH));
                    settingObj.addProperty(Consts.APP_INFO, CustomDashboardApplication.getInt(context, Consts.APP_INFO));
                    settingObj.addProperty(Consts.CLOCK_WIDGET, CustomDashboardApplication.getInt(context, Consts.CLOCK_WIDGET));


                    JsonObject detailsObj = new JsonObject();
                    detailsObj.add(Consts.DEVICE_SETTINGS, settingObj);
                    detailsObj.add(Consts.APP_DATA, myCustomArray);
                    String str = detailsObj.toString();
                    File file;
                    try {
                        file = new File(directory, fileName + ".txt");

                        FileWriter fileWriter = new FileWriter(file);
                  *//*  BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("{}");
                    bufferedWriter.close();*//*
                        fileWriter.write(str);
                        fileWriter.flush();
                        fileWriter.close();

                    *//*outputStream = new FileOutputStream(file);
                    outputStream.write(str.getBytes());
                    outputStream.close();*//*
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            File directory = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.folder_name));
            if (!directory.exists())
                directory.getParentFile().mkdirs();


            Gson gson = new GsonBuilder().create();

            /*String ser = SerializeObject.ReadSettings(context, "appanalytics.dat");
            if (ser != null && !ser.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something
                    appModels = (ArrayList<ApplicationTimerModel>) obj;
                }
            }*/

            ArrayList<AppModel> appModels = new ArrayList<>();


            // APPLICATIONS JSON ARRAY
            JSONArray appsArray = new JSONArray();

            ArrayList<ApplicationTimerModel> userModels = new ArrayList<>();

            String serAnalytics = SerializeObject.ReadSettings(context, Consts.appAnalyticsFile);
            if (serAnalytics != null && !serAnalytics.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(serAnalytics);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something

                    Collections.sort((ArrayList<ApplicationTimerModel>) obj, (lhs, rhs) -> lhs.getEndTime().compareTo(rhs.getEndTime()));

                    userModels = (ArrayList<ApplicationTimerModel>) obj;
                }
            }


            if (userModels.size() > 0) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JsonArray appDataAna;
            JsonParser jsonParser = new JsonParser();
            appDataAna = (JsonArray) jsonParser.parse(appsArray.toString());


            String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
            if (ser != null && !ser.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {
                    // Do something

                    appModels = (ArrayList<AppModel>) obj;
                }
            }

            if (appModels != null) {
                if (appModels.size() > 0) {
                    ArrayList<AppModel> lstBook = new ArrayList<>();
                    for (int j = 0; j < appModels.size(); j++) {
                        if (appModels.get(j).getColumn() > 0 && appModels.get(j).getRow() > 0) {
                            int position = 0;
                            if (appModels.get(j).getColumn() > 3) {
                                appModels.get(j).setColumn(3);
                            }
                            if (appModels.get(j).getRow() > 1) {
                                position = ((appModels.get(j).getRow() - 1) * 3 + appModels.get(j).getColumn()) - 1;
                            } else {
                                position = appModels.get(j).getColumn() - 1;
                            }

                            appModels.get(j).setPosition(position);
                            appModels.get(j).setLocation(appModels.get(j).getRow() + "," + appModels.get(j).getColumn());
                        }
                        if (appModels.get(j).isSelected()) {
                            lstBook.add(appModels.get(j));
                        }
                    }

                    JsonArray myCustomArray = gson.toJsonTree(lstBook).getAsJsonArray();

                    JsonObject settingObj = new JsonObject();
                    settingObj.addProperty(Consts.status_bar_status, CustomDashboardApplication.getInt(context, Consts.APP_INFO));
                    settingObj.addProperty(Consts.volume_key_status, CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME));
                    settingObj.addProperty(Consts.status_bar_text, CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
                    settingObj.addProperty(Consts.schedule_type, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE));
                    settingObj.addProperty(Consts.schedule_value, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));
                    settingObj.addProperty(Consts.battery_status, CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS));
                    settingObj.addProperty(Consts.battery_level, CustomDashboardApplication.getString(context, Consts.BATTERY_LEVEL));
                    settingObj.addProperty(Consts.date_time_setting, CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING));
                    settingObj.addProperty(Consts.shortcuts_status, CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD));
                    try {
                        //settingObj.addProperty(Consts.pin, AESCrypt.decrypt(getApplicationContext(), CustomDashboardApplication.getString(context, Consts.PWD_ENCRY)));
                        settingObj.addProperty(Consts.pin, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    JsonObject detailsObj = new JsonObject();
                    detailsObj.add(Consts.data, myCustomArray);
                    detailsObj.add(Consts.app_settings, settingObj);
                    detailsObj.add(Consts.apps_analytics_data, appDataAna);

                    JsonObject appData = new JsonObject();
                    appData.add(Consts.app_data, detailsObj);

                    String str = appData.toString();
                    File file;
                    try {
                        file = new File(directory, fileName + ".txt");

                        FileWriter fileWriter = new FileWriter(file);
                  /*  BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("{}");
                    bufferedWriter.close();*/
                        fileWriter.write(str);
                        fileWriter.flush();
                        fileWriter.close();

                    /*outputStream = new FileOutputStream(file);
                    outputStream.write(str.getBytes());
                    outputStream.close();*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String calculateMinutes(String startTime, String endTime) {

        String minute = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss aa");
            Date date1 = format.parse(endTime);
            Date date2 = format.parse(startTime);
            long mills = date1.getTime() - date2.getTime();
            minute = String.valueOf(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return minute;
    }

    // SHOW ENTER PASSWORD DIALOG
    void showPasswordSettingDialog() {
        alertDialogPassword = new AppCompatDialog(context);
        alertDialogPassword.setContentView(R.layout.setting_password_pop_up);

        TextView txUpdatePassword = alertDialogPassword.findViewById(R.id.tx_update_password);
        LinearLayout llShowPwd = alertDialogPassword.findViewById(R.id.ll_show_pwd);
        CheckBox switchPwd = alertDialogPassword.findViewById(R.id.pwd_switch);

        switchPwd.setChecked(CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH) == 1);

        txUpdatePassword.setOnClickListener(v -> {
            //showPasswordDialog();
            Intent intent = new Intent(context, ResetPin.class);
            startActivity(intent);
            alertDialogPassword.dismiss();
        });

        switchPwd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                    CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 1);
                } else {
                    Toast.makeText(context, "To use this feature you have to must set PIN", Toast.LENGTH_SHORT).show();
                }
            } else {
                CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 0);
            }
        });


        alertDialogPassword.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogPassword.show();
    }

    // SHOW ENTER PASSWORD DIALOG
    void showPasswordDialog() {
        alertDialogPwd = new AppCompatDialog(context);
        alertDialogPwd.setContentView(R.layout.reset_password_pop_up);

        final EditText edNewPassword = alertDialogPwd.findViewById(R.id.ed_new_password);
        Button btnDone = alertDialogPwd.findViewById(R.id.done_btn);

        btnDone.setOnClickListener(v -> {
            if (edNewPassword.getText().toString().trim().length() == 0) {
                edNewPassword.setError("Please enter PIN");
                edNewPassword.setFocusable(false);
            } else {
                try {
                    //AESCrypt.encrypt(context,edNewPassword.getText().toString().trim());
                    CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, edNewPassword.getText().toString().trim());
                    CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                alertDialogPwd.dismiss();
            }


        });
        alertDialogPwd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogPwd.show();
    }

    // SHOW ENTER PASSWORD DIALOG
    void showLabelsDialog() {
        alertDialogLabels = new AppCompatDialog(context);
        alertDialogLabels.setContentView(R.layout.setting_labels_pop_up);

        TextView txAddInfo = alertDialogLabels.findViewById(R.id.tx_add_info);
        TextView txDateTime = alertDialogLabels.findViewById(R.id.tx_date_time);

        txAddInfo.setOnClickListener(v -> {
            //showAddInfoDialog();
        });

        txDateTime.setOnClickListener(v -> showDateTimeSettingsDialog());

        alertDialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogLabels.show();
    }


    // SHOW ADD INFO DIALOG
    void showAddInfoDialog() {
        try {
            alertDialogAppInfo = new AppCompatDialog(context);
            alertDialogAppInfo.setContentView(R.layout.add_info_pop_up);

            final EditText edInfo = alertDialogAppInfo.findViewById(R.id.ed_info);
            CheckBox switchInfo = alertDialogAppInfo.findViewById(R.id.info_switch);
            Button btnDone = alertDialogAppInfo.findViewById(R.id.done_btn);

            if (!CustomDashboardApplication.getString(context, Consts.APP_INFO_STR).equalsIgnoreCase("")) {
                edInfo.setText(CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
            }

            switchInfo.setChecked(CustomDashboardApplication.getInt(context, Consts.APP_INFO) == 1);

            switchInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setInt(context, Consts.APP_INFO, 1);
                } else {
                    CustomDashboardApplication.setInt(context, Consts.APP_INFO, 0);
                }
            });

            btnDone.setOnClickListener(v -> {
                if (edInfo.getText().toString().trim().length() == 0) {
                    edInfo.setError("Please enter text/phone number");
                    edInfo.setFocusable(false);
                } else {
                    CustomDashboardApplication.setString(context, Consts.APP_INFO_STR, edInfo.getText().toString());
                    alertDialogAppInfo.dismiss();
                }


            });
            alertDialogAppInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAppInfo.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW DATE & TIME SETTINGS DIALOG
    void showDateTimeSettingsDialog() {
        try {
            alertDialogClockSetting = new AppCompatDialog(context);
            alertDialogClockSetting.setContentView(R.layout.clock_settings_pop_up);

            RadioGroup rgTimeSettings = alertDialogClockSetting.findViewById(R.id.rg_setting);
            RadioButton rbDate = alertDialogClockSetting.findViewById(R.id.rb_date);
            RadioButton rbTime = alertDialogClockSetting.findViewById(R.id.rb_time);
            RadioButton rbDateTime = alertDialogClockSetting.findViewById(R.id.rb_date_time);
            CheckBox switchTime = alertDialogClockSetting.findViewById(R.id.dt_time_switch);
            Button btnDone = alertDialogClockSetting.findViewById(R.id.done_btn);

            int selectedSetting = CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING);

            if (selectedSetting == 0) {
                rgTimeSettings.check(R.id.rb_time);
            } else if (selectedSetting == 1) {
                rgTimeSettings.check(R.id.rb_date);
            } else if (selectedSetting == 2) {
                rgTimeSettings.check(R.id.rb_date_time);
            }

            switchTime.setChecked(CustomDashboardApplication.getClockEnabled(context, Consts.CLOCK_WIDGET) == 1);

            switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
                } else {
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 0);
                }
            });

            rgTimeSettings.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_time) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 0);
                } else if (checkedId == R.id.rb_date) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 1);
                } else if (checkedId == R.id.rb_date_time) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 2);
                }
            });


            btnDone.setOnClickListener(v -> alertDialogClockSetting.dismiss());

            alertDialogClockSetting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogClockSetting.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // SHOW ADD EVENT DIALOG
    void showAddEventDialog() {
        try {
            alertDialogAddEvent = new AppCompatDialog(context);
            alertDialogAddEvent.setContentView(R.layout.schedule_add_event_pop_up);

            final EditText edEventName = alertDialogAddEvent.findViewById(R.id.ed_event_name);
            final EditText edStartTimeOne = alertDialogAddEvent.findViewById(R.id.ed_start_time_one);
            final EditText edStartTimeTwo = alertDialogAddEvent.findViewById(R.id.ed_start_time_two);
            final EditText edEndTimeOne = alertDialogAddEvent.findViewById(R.id.ed_end_time_one);
            final EditText edEndTimeTwo = alertDialogAddEvent.findViewById(R.id.ed_end_time_two);

            Button btnDone = alertDialogAddEvent.findViewById(R.id.done_btn);

            selectedDays = new ArrayList<>();

            edEndTimeOne.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 2) {
                        int hour = Integer.parseInt(s.toString());
                        if (hour > 23) {
                            Toast.makeText(context, "please enter correct hour", Toast.LENGTH_SHORT).show();
                        }
                        edEndTimeTwo.requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            edStartTimeOne.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 2) {
                        int hour = Integer.parseInt(s.toString());
                        if (hour > 23) {
                            Toast.makeText(context, "please enter correct hour", Toast.LENGTH_SHORT).show();
                        }
                        edStartTimeTwo.requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            edStartTimeOne.setOnClickListener(v -> startTimePicker(edStartTimeOne));

            edEndTimeOne.setOnClickListener(v -> endTimePicker(edEndTimeOne));

            final RecyclerView recDays = alertDialogAddEvent.findViewById(R.id.rec_days);

            recDays.setHasFixedSize(true);
            recDays.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

            scheduleDaysModels = new ArrayList<>();
            scheduleDaysModels.add(0, new ScheduleDaysModel(1, "Sunday", false));
            scheduleDaysModels.add(1, new ScheduleDaysModel(2, "Monday", false));
            scheduleDaysModels.add(2, new ScheduleDaysModel(3, "Tuesday", false));
            scheduleDaysModels.add(3, new ScheduleDaysModel(4, "Wednesday", false));
            scheduleDaysModels.add(4, new ScheduleDaysModel(5, "Thursday", false));
            scheduleDaysModels.add(5, new ScheduleDaysModel(6, "Friday", false));
            scheduleDaysModels.add(6, new ScheduleDaysModel(7, "Saturday", false));

            scheduleAdapter = new ScheduleAdapter(context, scheduleDaysModels, this);
            recDays.setAdapter(scheduleAdapter);

            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edEventName.getText().toString().trim().length() == 0) {
                        edEventName.setError(getResources().getString(R.string.enter_event_name_valid));
                        edEventName.requestFocus();
                    } else if (edStartTimeOne.getText().toString().trim().length() == 0) {
                        /*edStartTimeOne.setError(getResources().getString(R.string.enter_start_time_valid));
                        edStartTimeOne.requestFocus();*/

                        Toast.makeText(context, getResources().getString(R.string.enter_start_time_valid), Toast.LENGTH_SHORT).show();
                    } else if (edEndTimeOne.getText().toString().trim().length() == 0) {
                        /*edStartTimeOne.setError(getResources().getString(R.string.enter_end_time_valid));
                        edStartTimeOne.requestFocus();*/
                        Toast.makeText(context, getResources().getString(R.string.enter_end_time_valid), Toast.LENGTH_SHORT).show();
                    } else if (selectedDays.size() == 0) {
                        Toast.makeText(context, "Please select weekday from below", Toast.LENGTH_SHORT).show();
                    } else {

                        if (!TextUtils.isEmpty(edStartTimeOne.getText().toString().trim()) &&
                                !TextUtils.isEmpty(edEndTimeOne.getText().toString().trim())) {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            long startTime = 0;
                            long endTIme = 0;
                            try {
                                startTime = sdf.parse(edStartTimeOne.getText().toString().trim()).getTime();
                                endTIme = sdf.parse(edEndTimeOne.getText().toString().trim()).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (startTime == endTIme || startTime > endTIme) {
                                Toast.makeText(context, "Please select end time greater than start time.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (selectedDays.size() > 0) {
                            Gson gson = new GsonBuilder().create();
                            selectedDaysArray = gson.toJsonTree(selectedDays).getAsJsonArray();
                        }

                        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {

                            String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                            Gson gson = new Gson();
                            ProfileData profileData = gson.fromJson(profileDataStr, ProfileData.class);
                            int profileId = profileData.getData().getUserProfiles().get(0).getId();

                            // REQUEST OBJECT
                            JsonObject reqObj = new JsonObject();
                            reqObj.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                            reqObj.addProperty(Consts.profile_id, profileId);
                            reqObj.add(Consts.days, selectedDaysArray);
                            reqObj.addProperty(Consts.event_name, edEventName.getText().toString());
                            reqObj.addProperty(Consts.start_time, edStartTimeOne.getText().toString());
                            reqObj.addProperty(Consts.end_time, edEndTimeOne.getText().toString());

                            apiManager.addScheduleEvent(reqObj);
                            alertDialogAddEvent.dismiss();
                        }
                    }
                }
            });

            alertDialogAddEvent.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAddEvent.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogAddEvent.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void startTimePicker(final EditText etStartTime) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            String hour1 = "" + selectedHour;
            String minute1 = "" + selectedMinute;

            if (selectedHour < 10) {
                hour1 = "0" + selectedHour;
            }

            if (selectedMinute < 10) {
                minute1 = "0" + selectedMinute;
            }

            etStartTime.setText(hour1 + ":" + minute1);
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    void endTimePicker(final EditText etEndTime) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(CustomDashboardSettings.this, (timePicker, selectedHour, selectedMinute) -> {
            String hour1 = "" + selectedHour;
            String minute1 = "" + selectedMinute;

            if (selectedHour < 10) {
                hour1 = "0" + selectedHour;
            }

            if (selectedMinute < 10) {
                minute1 = "0" + selectedMinute;
            }
            etEndTime.setText(hour1 + ":" + minute1);
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    // SHOW SET TIMER SETTINGS DIALOG
    void showSetTimerDialog() {
        try {
            alertDialogSetTimer = new AppCompatDialog(context);
            alertDialogSetTimer.setContentView(R.layout.timer_pop_up);

            final RadioButton rbNoTimer = alertDialogSetTimer.findViewById(R.id.rb_no_timer);
            final RadioButton rbDailyLimit = alertDialogSetTimer.findViewById(R.id.rb_daily_limits);
            final RadioButton rbSchedule = alertDialogSetTimer.findViewById(R.id.rb_schedule);

            final Spinner spinnerTimer = alertDialogSetTimer.findViewById(R.id.spinner_timer);
            final Spinner spinnerSchedule = alertDialogSetTimer.findViewById(R.id.spinner_schedule);

            final CardView cvTimer = alertDialogSetTimer.findViewById(R.id.cv_timer);
            final CardView cvSchedule = alertDialogSetTimer.findViewById(R.id.cv_schedule);

            final LinearLayout llDays = alertDialogSetTimer.findViewById(R.id.ll_days);
            final LinearLayout llSchedule = alertDialogSetTimer.findViewById(R.id.ll_schedule);

            final TextView txAddEvent = alertDialogSetTimer.findViewById(R.id.tx_add_event);

            recScheduleEvent = alertDialogSetTimer.findViewById(R.id.rec_schedule_list);
            recScheduleEvent.setHasFixedSize(true);

            scheduleEventModels = new ArrayList<>();

            recScheduleEvent.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            scheduleEventListAdapter = new ScheduleEventListAdapter(context, scheduleEventModels, this);
            recScheduleEvent.setAdapter(scheduleEventListAdapter);

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

            timeAdapter = new ArrayAdapter<>(this, R.layout.view_spinner_item, timeList);
            timeAdapter.setDropDownViewResource(R.layout.spinner_list);
            spinnerTimer.setAdapter(timeAdapter);

            scheduleAdapter = new ArrayAdapter<>(this, R.layout.view_spinner_item, scheduleList);
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
                txAddEvent.setEnabled(false);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 1) {
                rbNoTimer.setChecked(false);
                rbDailyLimit.setChecked(true);
                rbSchedule.setChecked(false);
                cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                spinnerTimer.setEnabled(true);
                cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerSchedule.setEnabled(false);
                txAddEvent.setEnabled(false);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                spinnerTimer.setSelection(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER));
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 2) {
                rbNoTimer.setChecked(false);
                rbDailyLimit.setChecked(false);
                rbSchedule.setChecked(true);
                cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                spinnerTimer.setEnabled(false);
                cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                //spinnerSchedule.setEnabled(true);
                txAddEvent.setEnabled(true);
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                    getScheduleEventList();
            }

            rbNoTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    rbNoTimer.setChecked(true);
                    rbDailyLimit.setChecked(false);
                    rbSchedule.setChecked(false);
                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 0);
                    CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                    spinnerTimer.setEnabled(false);
                    txAddEvent.setEnabled(false);
                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                    spinnerSchedule.setEnabled(false);
                    CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                }
            });

            rbDailyLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    rbNoTimer.setChecked(false);
                    rbDailyLimit.setChecked(true);
                    rbSchedule.setChecked(false);
                    spinnerTimer.setEnabled(true);
                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.white));
                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                    //spinnerSchedule.setEnabled(false);
                    txAddEvent.setEnabled(false);
                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 1);
                    CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                    CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                }
            });

            rbSchedule.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    rbNoTimer.setChecked(false);
                    rbDailyLimit.setChecked(false);
                    rbSchedule.setChecked(true);
                    spinnerTimer.setEnabled(false);
                    cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                    cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                    //spinnerSchedule.setEnabled(true);
                    txAddEvent.setEnabled(true);
                    CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 2);
                    CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                        getScheduleEventList();
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

            txAddEvent.setOnClickListener(v -> {
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    showAddEventDialog();
                } else {
                    Intent intent = new Intent(context, Login.class);
                    startActivity(intent);
                }
            });

            btnDone.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(context)) {
                        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                            apiManager.updateProfileSchedule();
                        }
                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                        alertDialogSetTimer.dismiss();
                    } else {
                        Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                    }
                } else {
                    apiManager.updateProfileSchedule();
                    CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                    alertDialogSetTimer.dismiss();
                }
            });

            alertDialogSetTimer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogSetTimer.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogSetTimer.getWindow().setGravity(Gravity.CENTER);
            alertDialogSetTimer.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // SHOW SET SHORT CUT SETTING DIALOG
    void showSetShortCutDialog() {
        try {
            alertDialogShowShortCut = new AppCompatDialog(context);
            alertDialogShowShortCut.setContentView(R.layout.item_short_cuts);

            CheckBox switchTime = alertDialogShowShortCut.findViewById(R.id.dt_time_switch);

            switchTime.setChecked(CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD) == 1);

            switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 1);
                } else {
                    CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 0);
                }
            });

            alertDialogShowShortCut.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogShowShortCut.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // SHOW SET ALLOW?BLOCK CALLS SETTING DIALOG
    void showSetCallsDialog() {
        try {
            alertDialogAllowCalls = new AppCompatDialog(context);
            alertDialogAllowCalls.setContentView(R.layout.item_calls);

            CheckBox switchTime = alertDialogAllowCalls.findViewById(R.id.dt_time_switch);

            switchTime.setChecked(CustomDashboardApplication.getShortCut(context, Consts.ALLOWCALLS) == 1);


            switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setShortCut(context, Consts.ALLOWCALLS, 1);
                } else {
                    CustomDashboardApplication.setShortCut(context, Consts.ALLOWCALLS, 0);
                }
            });

            alertDialogAllowCalls.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAllowCalls.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        CustomDashboardApplication.isCustomSettingScreenCall = false;
        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }
        // LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver2);
        //LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver6);
    }

    BroadcastReceiver broadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int i = intent.getIntExtra(Consts.force_exit, 0);
                if (i == 1) {
                    Utils.exitApp(CustomDashboardSettings.this);

                    //Toast.makeText(context, "CustomDashboardSettings : broadcastReceiver5", Toast.LENGTH_SHORT).show();
                    /*PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeActivity.class);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                        Intent intent1 = new Intent();
                        intent1.setAction(Settings.ACTION_HOME_SETTINGS);
                        startActivity(intent1);
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

                    if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0 && CustomDashboardApplication.isDeviceSamsung()) {
                        try {
                            SuperLockState superLockState = SuperLockState.getInstance(CustomDashboardSettings.this);
                            if (superLockState != null && !superLockState.isKioskModeEnabled(CustomDashboardSettings.this)) {
                                superLockState.enableKioskMode(Consts.PACKAGE_NAME);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        CustomDashboardApplication.exitSamsungKiosk(context);
                    }

                    finish();
                    System.exit(0);*/
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
                        CustomDashboardApplication.exitSamsungKiosk(CustomDashboardSettings.this);
                        SuperLockState.getInstance(CustomDashboardSettings.this).allowStatusBar(true);
                        adminDisable();
                    }
                    Intent ii = new Intent(Intent.ACTION_DELETE);
                    ii.setData(Uri.parse("package:com.tabnova.aidashboard"));
                    startActivity(ii);
                }
            }
        }
    };

    private void resetViews() {
        txParentMode.setTextColor(getResources().getColor(R.color.black));
        txDashboard.setTextColor(getResources().getColor(R.color.black));
        txExit.setTextColor(getResources().getColor(R.color.black));

        llParentMode.setBackground(getResources().getDrawable(R.drawable.bg_circle_white_grey_border));
        llDashBoard.setBackground(getResources().getDrawable(R.drawable.bg_circle_white_grey_border));
        llExit.setBackground(getResources().getDrawable(R.drawable.bg_circle_white_grey_border));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CustomDashboardApplication.isDeviceSamsungActivated())
            isHomeApp = 1;
        else
            isHomeApp = 0;
        isPause = false;

        Utils.hideBottomNavigationBars(this);
        CustomDashboardApplication.isCustomSettingScreenCall = true;
        /*try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                CustomDashboardApplication.exitSamsungKiosk(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        sessionManager.setIsParentMode(true);

        resetViews();
        txParentMode.setTextColor(getResources().getColor(R.color.white));
        llParentMode.setBackground(getResources().getDrawable(R.drawable.bg_circle_primary_white_border));

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver5, new IntentFilter(Consts.IS_FORCE_EXIT));
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver6, new IntentFilter(Consts.IS_DELETE));

        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }

        new Handler().postDelayed(() -> {
            if (!Utils.isAppIsInBackground()) {
                WatchingAccessibilityService.hideReturnOverlay();
            }
        }, 3000);

        CustomDashboardApplication.enableBackKey(context);

/*
        if (CustomDashboardApplication.getInt(context, Consts.is_first_time) == 1) {
            //DialogLoader dialogLoader = new DialogLoader(context, 1);
            //dialogLoader.showProgressDialog();
            String dir = context.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.appCategoryFile);
            if (f0.exists()) {
                f0.delete();
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
            CustomDashboardApplication.setInt(context, Consts.serverAppListCount, resolveInfoList.size());
            for (int i = 0; i < resolveInfoList.size(); i++) {
                currentPositon = i;
               // llFetchingApps.setVisibility(View.VISIBLE);
                ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
                apiManager.getAppsCategories(activityInfo.applicationInfo.packageName, activityInfo.applicationInfo, resolveInfoList.size());
            }
          //  CustomDashboardApplication.setInt(context, Consts.is_first_time,0);
        }
*/
        recViewSettings.post(() -> {
            View viewItem = recViewSettings.getLayoutManager().findViewByPosition(0);
            View icon = null;
            if (viewItem != null) {
                icon = viewItem.findViewById(R.id.iv_icon);
            }

            if (CustomDashboardApplication.getInt(context, Consts.isAppClick) == 1) {
                /*Log.e(TAG, "run: @2497");
                if (!isLoginToolTip) {
                    if (loginToolTip != null && loginToolTip.isShowing())
                        loginToolTip.dismiss();
                    showLoginToolTip();
                    isLoginToolTip = true;
                }*/
            } else {
                Log.e(TAG, "run: @2519");
                if (icon != null && !isSpotLightShown && CustomDashboardApplication.getInt(context, Consts.isAppClick) != 2) {
                    applicationToolTip = new TooltipHelper.Builder(icon)
                            .setText("Select Applications From Application List You Want To Add On Dashboard")
                            .setTextColor(getResources().getColor(R.color.white))
                            .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                            .setPadding(8)
                            .setCancelable(true)
                            .setDismissOnClick(true)
                            .setOnDismissListener(() -> {
                                isSpotLightShown = false;
                                CustomDashboardApplication.setInt(context, Consts.isAppClick, 1);
                                if (!isPause) {
                                    if (loginToolTip != null && loginToolTip.isShowing())
                                        loginToolTip.dismiss();
                                    showLoginToolTip();
                                }
                            })
                            .show();
                    isSpotLightShown = true;
                }
            }
        });

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            txLoginStr.setVisibility(View.VISIBLE);
            txLoginStr.setText(Html.fromHtml("Please login to " + "<a href=" + Consts.SettingsLoginInfoUrlLink + ">" + Consts.SettingsLoginInfoUrlLink + "</a>" + " set config remotely"));
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
            btnLogin.setText(getResources().getString(R.string.log_out));
        } else {
            txLoginStr.setVisibility(View.GONE);
            btnLogin.setText(getResources().getString(R.string.login));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CustomDashboardApplication.isCustomSettingScreenCall = false;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver6);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver2);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(fetchAppDetailsReceiver);

        BillingManager.getInstance().clearListener(purchaseListener);
    }

    private void showLoginToolTip() {
        /*loginToolTip = new Tooltip.Builder(btnLogin)
                .setText(getString(R.string.logged_in_msg_str))
                .setTextColor(getResources().getColor(R.color.white))
                .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .setPadding(8)
                .setCancelable(true)
                .setDismissOnClick(true)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        isLoginToolTip = false;
                        CustomDashboardApplication.setInt(context, Consts.isAppClick, 2);
                    }
                })
                .show();*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (keyCode == KeyEvent.KEYCODE_HOME) {
         /*   if (isMyLauncherDefault()) {
                // if (isHomeBtnClicked == 0) {
                resetPreferredLauncherAndOpenChooser(context);
                ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                activityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
                // }
            }*/

            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
           /* if (!isMyLauncherDefault()) {
                resetPreferredLauncherAndOpenChooser(context);
            }*/
            return false;
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

              /*  AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                int currentVolume = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                //mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {


             /*   AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                //mode.setStreamMute(AudioManager.STREAM_RING, true);
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
                try {
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

              /*  AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                //mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {
               /* final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.ADJUST_MUTE);*/

              /*  AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                //mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        /*if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }*/

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Android 6-12: Back arrives as a key event. Let the framework track it so it reaches
            // the OnBackPressedCallback registered in onCreate (finish when not dashboard-launched).
            return super.onKeyDown(keyCode, event);
        }

        return true;
    }

    // SHOW SETTING DIALOG
    void showSettingDialog() {
        try {
            final AppCompatDialog alertSettings = new AppCompatDialog(context);
            alertSettings.setContentView(R.layout.item_new_settings);

            LinearLayout llShortcuts = alertSettings.findViewById(R.id.ll_shortcuts);
            LinearLayout llVolumeControl = alertSettings.findViewById(R.id.ll_volume_control);
            LinearLayout llSecureBrowser = alertSettings.findViewById(R.id.ll_secure_browser);
            LinearLayout llDisableButton = alertSettings.findViewById(R.id.ll_disable_recent_buttons);
            LinearLayout llHardwareSettings = alertSettings.findViewById(R.id.ll_hardware_settings);
            Button btnSettingDone = alertSettings.findViewById(R.id.btn_settings_done);

            CheckBox cbShortCut = alertSettings.findViewById(R.id.cb_shortcut);
            CheckBox cbVolumeControl = alertSettings.findViewById(R.id.cb_volume_control);
            CheckBox cbSecureBrowser = alertSettings.findViewById(R.id.cb_secure_browser);
            CheckBox cbDisableButton = alertSettings.findViewById(R.id.cb_disable_recent_button);
            CheckBox cbLabels = alertSettings.findViewById(R.id.cb_labels);
            CheckBox cbHardwareSettings = alertSettings.findViewById(R.id.cb_hardware_settings);
            CheckBox cbWipeSettings = alertSettings.findViewById(R.id.cb_wipe_settings);

            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                llHardwareSettings.setVisibility(View.VISIBLE);
            } else {
                llHardwareSettings.setVisibility(View.GONE);
            }

            cbShortCut.setChecked(CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD) == 1);

            cbVolumeControl.setChecked(CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1);

            cbLabels.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alertSettings.dismiss();
                Intent intent = new Intent(context, Labels.class);
                startActivity(intent);
            });

            if (CustomDashboardApplication.getInt(context, Consts.SECURE_BROWSER_ENABLE) == 1) {
                CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 1);
                cbSecureBrowser.setChecked(true);
            } else {
                CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 0);
                cbSecureBrowser.setChecked(false);
            }

            cbDisableButton.setChecked(CustomDashboardApplication.getInt(context, Consts.DISABLE_RECENTS_BUTTON) == 1);

            cbShortCut.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 1);
                } else {
                    CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 0);
                }
            });

            cbVolumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 1);
                    try {
                        AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                        mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                        mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                        //mode.setStreamMute(AudioManager.STREAM_RING, false);
                        mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 0);
                    try {
                        AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                        mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                        mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        //mode.setStreamMute(AudioManager.STREAM_RING, true);
                        mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                if (NetConnectivity.isOnline(context))
                    apiManager.updateProfileSettings();
            });

            cbSecureBrowser.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 1);
                } else {
                    CustomDashboardApplication.setInt(context, Consts.SECURE_BROWSER_ENABLE, 0);
                }
            });

            cbDisableButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
                        Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission.");
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 1);
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
                }
            });

            cbHardwareSettings.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alertSettings.dismiss();
                showHardwareSettingsDialog();
            });

            cbWipeSettings.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alertSettings.dismiss();
                showWipeSettingsDialog();
            });

            btnSettingDone.setOnClickListener(v -> {
                alertSettings.dismiss();
                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    if (NetConnectivity.isOnline(context))
                        apiManager.updateProfileSettings();
                }
            });

            alertSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertSettings.getWindow().setGravity(Gravity.CENTER);
            alertSettings.getWindow().getDecorView().setLeft(50);
            alertSettings.getWindow().getDecorView().setRight(50);
            alertSettings.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW SET ALLOW/BLOCK VOLUME SETTING DIALOG
    void showSetVolumeSettingDialog() {
        try {
            alertVolumeControl = new AppCompatDialog(context);
            alertVolumeControl.setContentView(R.layout.item_volume_control);

            CheckBox switchTime = alertVolumeControl.findViewById(R.id.volume_switch);

            switchTime.setChecked(CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1);

            switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 1);
                    AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                    mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                    mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    //mode.setStreamMute(AudioManager.STREAM_RING, false);
                    mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                } else {
                    CustomDashboardApplication.setVolume(context, Consts.ALLOWVOLUME, 0);
                    AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                    mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                    mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    //mode.setStreamMute(AudioManager.STREAM_RING, true);
                    mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                }
                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                apiManager.updateProfileSettings();
            });


            alertVolumeControl.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertVolumeControl.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW SET ALLOW/BLOCK MARKET PLACE SETTING DIALOG
    void showSetMarketDialog() {
        try {
            alertDialogMarketPlace = new AppCompatDialog(context);
            alertDialogMarketPlace.setContentView(R.layout.item_market_place);

            CheckBox switchTime = alertDialogMarketPlace.findViewById(R.id.dt_time_switch);

            switchTime.setChecked(CustomDashboardApplication.getMarketPlace(context, Consts.PLAY_STORE_ENABLE) == 1);

            switchTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    CustomDashboardApplication.setMarketPlace(context, Consts.PLAY_STORE_ENABLE, 1);
                } else {
                    CustomDashboardApplication.setMarketPlace(context, Consts.PLAY_STORE_ENABLE, 0);
                }
                alertDialogMarketPlace.dismiss();
            });

            alertDialogMarketPlace.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogMarketPlace.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void showDisableRecentsButtonDialog() {
        try {
            alertDialogDisableRecentsButton = new AppCompatDialog(context);
            alertDialogDisableRecentsButton.setContentView(R.layout.item_block_recents_button);

            CheckBox cbDisableRecents = alertDialogDisableRecentsButton.findViewById(R.id.cb_disable_recents);
            ImageView ivDisableRecentsHelp = alertDialogDisableRecentsButton.findViewById(R.id.iv_disable_recents_help);
            final TextView txHelpMessage = alertDialogDisableRecentsButton.findViewById(R.id.tx_help_message);

            ivDisableRecentsHelp.setOnClickListener(v -> {
                if (txHelpMessage.getVisibility() == View.VISIBLE) {
                    txHelpMessage.setVisibility(View.GONE);
                } else {
                    txHelpMessage.setVisibility(View.VISIBLE);
                }
            });

            cbDisableRecents.setChecked(CustomDashboardApplication.getInt(context, Consts.DISABLE_RECENTS_BUTTON) == 1);

            cbDisableRecents.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!CustomDashboardApplication.isAccessibilitySettingsOn(context)) {
                        Utils.showAccessibilityPopup(context, "You must have to provide accessibility permission.");
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 1);
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
                }
                alertDialogDisableRecentsButton.dismiss();
            });

            alertDialogDisableRecentsButton.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogDisableRecentsButton.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showCallSmsLogsDialog() {
        try {
            alertDialogCallSmsLogs = new AppCompatDialog(context);
            alertDialogCallSmsLogs.setContentView(R.layout.dialog_call_sms_logs);

            CheckBox cbSyncCall = alertDialogCallSmsLogs.findViewById(R.id.cb_sync_call);
            CheckBox cbSyncSms = alertDialogCallSmsLogs.findViewById(R.id.cb_sync_sms);

            cbSyncCall.setChecked(CustomDashboardApplication.getInt(context, Consts.SYNC_CALL_LOGS) == 1);

            cbSyncSms.setChecked(CustomDashboardApplication.getInt(context, Consts.SYNC_SMS) == 1);

            cbSyncCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        /*if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                            CustomDashboardApplication.setInt(context, Consts.SYNC_CALL_LOGS, 1);
                            syncType = 1;
                            //CustomDashboardApplication.getCallLog(context);
                            //CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                            // apiManager.savePhoneHistory(context, 0);
                        } else {
                            requestReadLogsPermission();
                        }*/
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.SYNC_CALL_LOGS, 1);
                        // CustomDashboardApplication.getCallLog(context);
                        //CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                        //apiManager.savePhoneHistory(context, 0);
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.SYNC_CALL_LOGS, 0);
                }
            });

            cbSyncSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        /*if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                            CustomDashboardApplication.setInt(context, Consts.SYNC_SMS, 1);
                            syncType = 2;
                            CustomDashboardApplication.getAllSms(context);
                            //CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                            // apiManager.savePhoneHistory(context, 1);
                        } else {
                            requestReadLogsPermission();
                        }*/
                    } else {
                        CustomDashboardApplication.setInt(context, Consts.SYNC_SMS, 1);
                        CustomDashboardApplication.getAllSms(context);
                        //CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                        // apiManager.savePhoneHistory(context, 1);
                    }
                } else {
                    CustomDashboardApplication.setInt(context, Consts.SYNC_SMS, 0);
                }
            });

            alertDialogCallSmsLogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogCallSmsLogs.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW EXIT DIALOG
    public void showExitPopUp() {
        try {
            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(context.getResources().getString(R.string.exit_message));
            dialogBuilder1.setPositiveButton(getResources().getString(R.string.exit), (dialog, which) -> {
                //startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));

                //finishAndRemoveTask();

                PackageManager packageManager = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, FakeActivity.class);
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(selector);

                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                alertExitDialog.dismiss();
                //((Activity) context).finishAffinity();
                finish();
                System.exit(0);
                //finish();
            });
            dialogBuilder1.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> alertExitDialog.dismiss());
            alertExitDialog = dialogBuilder1.create();
            alertExitDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW EXIT DIALOG
    public void showLogOutPopUp() {
        try {
            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(context.getResources().getString(R.string.log_out_msg));
            dialogBuilder1.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                signOut();
                alertLogoutDialog.dismiss();
            });
            dialogBuilder1.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> alertLogoutDialog.dismiss());
            alertLogoutDialog = dialogBuilder1.create();
            alertLogoutDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signOut() {
        /*Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });*/
    }

    // GET FILE
    public void importFileData(String fileName) {
        try {

            // if (Consts.IS_BUSINESS_APP == 1) {

            ArrayList<AppModel> lstBook = new ArrayList<>();


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

                File imageFile = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.folder_name), "screen1.png");

                if (imageFile.exists()) {
                    final Uri contentURI = Uri.parse(imageFile.toString());
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, contentURI.toString());
                }

                File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.folder_name), fileName + ".txt");
                //File file = new File(fileName+".txt");

                if (file.exists()) {

                    br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        Log.i("Test", "text : " + text + " : end");
                        text.append('\n');
                    }
                    CustomDashboardApplication.setInt(context, Consts.isPreInstall, 1);
                }/* else {
                        file.getParentFile().mkdirs();
                    }*/

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

            // SET SECURE WEB VIEW


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

                    if (Utils.contains(dataObj, Consts.grid_row)) {
                        try {
                            appModel.setRow(dataObj.getInt(Consts.grid_row));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (Utils.contains(dataObj, Consts.grid_column)) {
                        try {
                            appModel.setColumn(dataObj.getInt(Consts.grid_column));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (appModel.getColumn() > 0 && appModel.getRow() > 0) {
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
                        appModel.setLocation(appModel.getRow() + "," + appModel.getColumn());
                    }

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

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    String ser = SerializeObject.objectToString(appModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.appHistoryFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.appHistoryFile);
                    }

                    // finish();
                }, 1000);
            }
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // OPEN SELECT IMAGE POP UP
    private void selectImage() {
        try {
            alertSelectImage = new AppCompatDialog(context);
            alertSelectImage.setContentView(R.layout.custom_select_image);

            TextView txCaptureCamera = alertSelectImage.findViewById(R.id.tx_capture_image);
            TextView txSelectGallery = alertSelectImage.findViewById(R.id.tx_select_gallery);
            ImageView ivDelete = alertSelectImage.findViewById(R.id.iv_delete);

            Button btnCancel = alertSelectImage.findViewById(R.id.btn_cancel);

            if (ivDelete != null) {
                ivDelete.setOnClickListener(v -> {
                    if (!TextUtils.isEmpty(CustomDashboardApplication.getString(context, Consts.IMG_FILE))) {
                        Toast.makeText(context, "Wallpaper deleted successfully.", Toast.LENGTH_SHORT).show();
                    }
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, "");
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                        apiManager.deleteWallpaper();
                    }
                    alertSelectImage.dismiss();
                });
            }

            if (txCaptureCamera != null) {
                txCaptureCamera.setOnClickListener(v -> {
                    userChoosenTask = getString(R.string.take_photo);
                    // CAMERA only. If not yet granted, checkPermission requests it and the camera
                    // is opened from onRequestPermissionsResult.
                    if (Utils.checkPermission(context)) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            cameraIntent();
                        } else {
                            Utils.openCameraIntent(context, CustomDashboardSettings.this);
                        }
                        alertSelectImage.dismiss();
                    }

                });
            }

            if (txSelectGallery != null) {
                txSelectGallery.setOnClickListener(v -> {
                    userChoosenTask = getString(R.string.choose_from_library);
                    // ACTION_PICK returns a content URI with a read grant: no permission needed.
                    galleryIntent();

                    alertSelectImage.dismiss();
                });
            }

            btnCancel.setOnClickListener(v -> alertSelectImage.dismiss());

            alertSelectImage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertSelectImage.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertSelectImage.getWindow().setGravity(Gravity.CENTER);
            alertSelectImage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
      /*  final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_library),
                getString(R.string.cancel)};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.wallpaper));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utils.checkPermission(context);

               if (items[item].equals(getString(R.string.take_photo))) {
                    userChoosenTask = getString(R.string.take_photo);
                    if (result){
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            cameraIntent();
                        } else {
                            Utils.openCameraIntent(context, CustomDashboardSettings.this);
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
        builder.show();*/
    }

    // SHOW ADD WEB SHORTCUTS POP UP
    void showAddWebShortcutDialog() {
        try {
            alertDialogAddWebShortcut = new AppCompatDialog(context);
            alertDialogAddWebShortcut.setContentView(R.layout.add_web_short_cuts_pop_up);

            final EditText edWebsiteName = alertDialogAddWebShortcut.findViewById(R.id.ed_web_shortcut_name);
            final EditText edWebsiteUrl = alertDialogAddWebShortcut.findViewById(R.id.ed_web_url_name);

            ImageView ivClose = alertDialogAddWebShortcut.findViewById(R.id.iv_close);

            Button btnDone = alertDialogAddWebShortcut.findViewById(R.id.done_btn);

            recAddedShortcuts = alertDialogAddWebShortcut.findViewById(R.id.rec_added_shortcuts);

            recAddedShortcuts.setHasFixedSize(true);
            recAddedShortcuts.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            addedWebShortCutsModels = new ArrayList<>();
            appModels = new ArrayList<>();

            String scheduleTimerStr = SerializeObject.ReadSettings(context, Consts.addedWebShortcutsFile);
            if (scheduleTimerStr != null && !scheduleTimerStr.equalsIgnoreCase("")) {
                Object scheduleModelObj = SerializeObject.stringToObject(scheduleTimerStr);
                if (scheduleModelObj instanceof ArrayList) {
                    addedWebShortCutsModels = (ArrayList<AddedWebShortcutModel>) scheduleModelObj;
                }
            }

            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                String dir1 = context.getFilesDir().getAbsolutePath();
                File f01 = new File(dir1, Consts.addedWebShortcutsFile);
                if (f01.exists()) {
                    f01.delete();
                }

                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.savedWebShortcutsFile);
                if (f0.exists()) {
                    f0.delete();
                }

                getWebShortCuts();
            }

            addedWebSiteShortcutAdapter = new AddedWebShortcutsAdapter(context, addedWebShortCutsModels, CustomDashboardSettings.this);
            recAddedShortcuts.setAdapter(addedWebSiteShortcutAdapter);

            ivClose.setOnClickListener(v -> {

                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    apiManager.saveWebShortcuts();
                }

                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.savedWebShortcutsFile);
                if (f0.exists()) {
                    f0.delete();
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
                alertDialogAddWebShortcut.dismiss();
            });

            btnDone.setOnClickListener(v -> {
                if (edWebsiteName.getText().toString().trim().length() == 0) {
                    edWebsiteName.setError(getResources().getString(R.string.enter_website_name));
                    edWebsiteName.setFocusable(true);
                } else if (edWebsiteUrl.getText().toString().trim().length() == 0) {
                    edWebsiteUrl.setError(getResources().getString(R.string.enter_website_url));
                    edWebsiteUrl.setFocusable(true);
                } else if (!Patterns.WEB_URL.matcher(edWebsiteUrl.getText().toString().trim()).matches()) {
                    edWebsiteUrl.setError(getResources().getString(R.string.enter_website_valid_url));
                    edWebsiteUrl.setFocusable(true);
                } else {
                    showWebShortcutGridSelection(edWebsiteName.getText().toString().trim(), edWebsiteUrl.getText().toString().trim());
                }
            });

            alertDialogAddWebShortcut.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogAddWebShortcut.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogAddWebShortcut.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showWebShortcutGridSelection(String name, String url) {
        final String[] webSiteName = {name};
        final String[] webSiteUrl = {url};

        final AppCompatDialog appCompatDialog = new AppCompatDialog(context);
        appCompatDialog.setContentView(R.layout.item_grid_selection);
        appCompatDialog.setCancelable(false);

        final Spinner rowSpinner = appCompatDialog.findViewById(R.id.spinner_row);
        final Spinner columnSpinner = appCompatDialog.findViewById(R.id.spinner_column);
        Button btnSelect = appCompatDialog.findViewById(R.id.btn_select);

        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> columns = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            rows.add(i);
        }

        for (int i = 1; i <= 3; i++) {
            columns.add(i);
        }

        ArrayAdapter<Integer> rowArray = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, rows);
        rowSpinner.setAdapter(rowArray);

        ArrayAdapter<Integer> columnArray = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, columns);
        columnSpinner.setAdapter(columnArray);

        btnSelect.setOnClickListener(v -> {
            int row = (int) rowSpinner.getSelectedItem();
            int column = (int) columnSpinner.getSelectedItem();
            String location = row + "," + column;
            int position = 0;
            if (row > 1) {
                position = ((row - 1) * 3 + column) - 1;
            } else {
                position = column - 1;
            }

            //String websiteName = edWebsiteUrl.getText().toString().trim();
            if (!webSiteUrl[0].startsWith("http")) {
                webSiteUrl[0] = "https://" + webSiteUrl[0];
            }

            if (addedWebShortCutsModels.size() > 0) {
                for (int k = 0; k < addedWebShortCutsModels.size(); k++) {
                    if (!addedWebShortCutsModels.get(k).getWebSiteUrl().equalsIgnoreCase(webSiteUrl[0])) {
                        // if (addedWebShortCutsModels.size() > 0) {
                        if (addedWebShortCutsModels.size() < 15) {

                            String dir = context.getFilesDir().getAbsolutePath();
                            File f0 = new File(dir, Consts.savedWebShortcutsFile);
                            if (f0.exists()) {
                                f0.delete();
                            }
                            String dir1 = context.getFilesDir().getAbsolutePath();
                            File f01 = new File(dir1, Consts.addedWebShortcutsFile);
                            if (f01.exists()) {
                                f01.delete();
                            }


                            addedWebShortCutsModels.add((addedWebShortCutsModels.size() - 1) + 1, new AddedWebShortcutModel(0, webSiteName[0], webSiteUrl[0], "", true, row, column, position, location));
                            String ser = SerializeObject.objectToString(addedWebShortCutsModels);
                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                SerializeObject.WriteSettings(context, ser, Consts.addedWebShortcutsFile);
                            }

                            if (addedWebShortCutsModels.size() > 0) {
                                for (int i = 0; i < addedWebShortCutsModels.size(); i++) {
                                    appModels.add(i, new AppModel(i, addedWebShortCutsModels.get(i).isSelected(), true, true,
                                            addedWebShortCutsModels.get(i).getWebSiteName(), "1.0", addedWebShortCutsModels.get(i).getWebSiteUrl(),
                                            "28", "", addedWebShortCutsModels.get(i).getRow(), addedWebShortCutsModels.get(i).getColumn(),
                                            addedWebShortCutsModels.get(i).getPosition(), addedWebShortCutsModels.get(i).getLocation()));
                                }

                                String ser1 = SerializeObject.objectToString(appModels);
                                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                                    SerializeObject.WriteSettings(context, ser1, Consts.savedWebShortcutsFile);
                                } else {
                                    SerializeObject.WriteSettings(context, "", Consts.savedWebShortcutsFile);
                                }
                            }

                            addedWebSiteShortcutAdapter = new AddedWebShortcutsAdapter(context, addedWebShortCutsModels, CustomDashboardSettings.this);
                            recAddedShortcuts.setAdapter(addedWebSiteShortcutAdapter);
                            break;
                        } else {
                            Toast.makeText(context, getResources().getString(R.string.max_web), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                //CustomDashboardApplication.getWebPage(edWebsiteUrl.getText().toString().trim());
                if (addedWebShortCutsModels.size() > 0) {
                    if (addedWebShortCutsModels.size() < 15)
                        addedWebShortCutsModels.add((addedWebShortCutsModels.size() - 1) + 1, new AddedWebShortcutModel(0, webSiteName[0], webSiteUrl[0], "", true, row, column, position, location));
                    else
                        Toast.makeText(context, getResources().getString(R.string.max_web), Toast.LENGTH_SHORT).show();
                } else {
                    if (addedWebShortCutsModels.size() < 15)
                        addedWebShortCutsModels.add(0, new AddedWebShortcutModel(0, webSiteName[0], webSiteUrl[0], "", true, row, column, position, location));
                    else
                        Toast.makeText(context, getResources().getString(R.string.max_web), Toast.LENGTH_SHORT).show();
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
                addedWebSiteShortcutAdapter = new AddedWebShortcutsAdapter(context, addedWebShortCutsModels, CustomDashboardSettings.this);
                recAddedShortcuts.setAdapter(addedWebSiteShortcutAdapter);
            }

            if (addedWebShortCutsModels.size() < 15) {
                alertDialogAddWebShortcut.dismiss();
            }

            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                apiManager.saveWebShortcuts();
            }

            appCompatDialog.dismiss();
        });

        appCompatDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        appCompatDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appCompatDialog.getWindow().setGravity(Gravity.CENTER);
        appCompatDialog.show();
    }

    public static void exitApplication(Context context) {
        Intent intent = new Intent(context, CustomDashboardSettings.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // OPEN CAMERA INTENT
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    // OPEN GALLERY INTENT
    private void galleryIntent() {
        /*Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(galleryIntent, SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }*/
        // Launch the picker immediately for demonstration
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
//                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build());
    }

    private void onCaptureImageResult(Intent data) {
        try {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            // App-specific Pictures dir (no storage permission needed; shared root is not writable on Android 10+).
            String paths = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "" + System.currentTimeMillis() + ".jpg";
            File destination = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");

            try {
                if (!destination.exists())
                    destination.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (destination.exists()) {
                // Get length of file in bytes
                long fileSizeInBytes = destination.length() * 1024 * 1024;
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                long fileSizeInKB = fileSizeInBytes / 1024;
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                //long fileSizeInMB = fileSizeInKB / 1024;
                long fileSizeInMB = 1024 * 1024 * 10;


                FileOutputStream fo = null;
                try {
                    destination.createNewFile();
                    /*fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();*/
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri tempUri = getImageUri(context, thumbnail);
                   /* Bitmap resizedImage = new Resizer(this)
                            .setTargetLength(1080)
                            .setSourceImage(destination)
                            .getResizedBitmap();

                    Uri newTempUri = getImageUri(context, resizedImage);
                    profileImageURI = newTempUri;*/
                profileImageURI = tempUri;
                profileImageString = getRealPathFromURI(tempUri);

                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    if (fileSizeInBytes < fileSizeInMB) {
                        updateProfileWallpaper();
                        CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                        Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.wallpaper_not_applied), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                    Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
//    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        try {
            if (data != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                    bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Uri tempUri = getImageUri(context, bm);

            profileImageURI = tempUri;
            profileImageString = getRealPathFromURI(tempUri);
            File file = new File(profileImageString);

         /*   Bitmap resizedImage = new Resizer(this)
                    .setTargetLength(1080)
                    .setSourceImage(file)
                    .getResizedBitmap();

            Uri newTempUri = getImageUri(context, resizedImage);
            profileImageURI = newTempUri;*/

            if (file.exists()) {
                // Get length of file in bytes
                long fileSizeInBytes = file.length();
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                long fileSizeInKB = fileSizeInBytes / 1024;
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                //long fileSizeInMB = fileSizeInKB / 1024;
                long fileSizeInMB = 1024 * 1024 * 10;

              /*  if (fileSizeInBytes < fileSizeInMB) {
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                        updateProfileWallpaper();
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                    Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.wallpaper_not_applied), Toast.LENGTH_SHORT).show();
                }*/

                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    if (fileSizeInBytes < fileSizeInMB) {
                        updateProfileWallpaper();
                        CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                        Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.wallpaper_not_applied), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                    Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                }
            }


            //String s = getRealPathFromURI(tempUri);
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
        if (path != null)
            return Uri.parse(path);
        else return null;
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            // Only CAMERA is requested with this code (Take photo).
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (getString(R.string.take_photo).equals(userChoosenTask)) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                        cameraIntent();
                    } else {
                        Utils.openCameraIntent(this, CustomDashboardSettings.this);
                    }
                }
            } else {
                //code for deny
            }
        }

        /*if (requestCode == readLogsRequest) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

                    if (syncType == 1) {
                        //CustomDashboardApplication.getCallLog(context);
                        //apiManager.savePhoneHistory(context, 0);
                    } else if (syncType == 2) {
                        //CustomDashboardApplication.getAllSms(context);
                        //apiManager.savePhoneHistory(context, 1);
                    }
                    //CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                } else {
                    requestReadLogsPermission();
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(context, context.getResources().getString(R.string.permission_denied_explanation_str), Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Consts.APP_SELECTION && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null && data.getExtras().getBoolean(Consts.is_launch_billing)) {
                startPurchase();
            }
        }

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String folderPath = null;
            if (data != null) {

                Uri uri = data.getData();
                File file = new File(uri.getPath());//create path from uri
                final String[] split = file.getPath().split(":");//split the path.
                folderPath = split[1];
                selectedPath = folderPath;
                edPath.setText(selectedPath);
                return;
            }
        }


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE)
                    onSelectFromGalleryResult(data);
                else if (requestCode == REQUEST_CAMERA)
                    onCaptureImageResult(data);
            }
        } else {
            if (requestCode == Utils.REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
           /*     profileImage.setVisibility(View.VISIBLE);
                iconText.setVisibility(View.GONE);
                profileImageString = Utils.imageFilePath;
                imageLoader.displayImage("file://"+profileImageString, profileImage, options);
                *//*imageLoader.displayImage(profileImageString, profilePic, options);*//*
                File file = new File(Utils.imageFilePath);
                profileImageURI = Uri.fromFile(file);
                uploadProfileImage();*/

                try {
                    File file = new File(Utils.imageFilePath);

                    if (file.exists()) {
                        // Get length of file in bytes
                        long fileSizeInBytes = file.length();
                        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                        //long fileSizeInMB = fileSizeInKB / 1024;
                        long fileSizeInMB = 1024 * 1024 * 10;

                        if (fileSizeInBytes < fileSizeInMB) {

                            profileImageURI = Uri.fromFile(file);
                           /* Bitmap resizedImage = new Resizer(this)
                                    .setTargetLength(1080)
                                    .setSourceImage(file)
                                    .getResizedBitmap();

                            profileImageURI = getImageUri(context, resizedImage);*/
                            profileImageString = Utils.imageFilePath;
                            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                                updateProfileWallpaper();
                            CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                            Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, getResources().getString(R.string.wallpaper_not_applied), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Bitmap bm = null;
                    try {
                        if (data != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 29) {
                                    bm = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), data.getData()));
                                } else {
                                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Uri tempUri = getImageUri(context, bm);
                        if (tempUri != null) {
                            profileImageURI = tempUri;

                            profileImageString = getRealPathFromURI(tempUri);
                        }

                        if (profileImageString != null) {
                            File file = new File(profileImageString);
                            if (file != null && file.exists()) {
                                // Get length of file in bytes
                                long fileSizeInBytes = file.length();
                                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                                long fileSizeInKB = fileSizeInBytes / 1024;
                                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                                //long fileSizeInMB = fileSizeInKB / 1024;
                                long fileSizeInMB = 1024 * 1024 * 10;

                               /* Bitmap resizedImage = new Resizer(this)
                                        .setTargetLength(1080)
                                        .setSourceImage(file)
                                        .getResizedBitmap();

                                profileImageURI = getImageUri(context, resizedImage);*/

                                if (fileSizeInBytes < fileSizeInMB) {
                                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                                        updateProfileWallpaper();
                                    CustomDashboardApplication.setString(context, Consts.IMG_FILE, String.valueOf(profileImageURI));
                                    Toast.makeText(context, getResources().getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, getResources().getString(R.string.wallpaper_not_applied), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.e("OnselectGallery->", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final BillingManager.PurchaseListener purchaseListener = new BillingManager.PurchaseListener() {
        @Override
        public void onPurchaseSuccess() {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Product purchased. You can use all premium features now.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPurchasePending() {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Payment is pending. Premium will activate once payment is complete.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPurchaseCancelled() {
            // Nothing to do; entitlement unchanged.
        }

        @Override
        public void onPurchaseError(int responseCode, @Nullable String message) {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Purchase could not be completed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    };

    /** Explicit Buy taps only: re-check entitlement first; launch the purchase flow only if not subscribed. */
    private void startPurchase() {
        BillingManager.getInstance().refreshEntitlement((queried, entitled) -> {
            if (isFinishing()) {
                return;
            }
            if (!(queried && entitled)) {
                BillingManager.getInstance().launchPurchase(CustomDashboardSettings.this, purchaseListener);
            }
        });
    }

    /** Website Block / App Block options: open the block screen (it refreshes entitlement itself). */
    private void openBlockDomainScreen(String from) {
        Intent intent = new Intent(CustomDashboardSettings.this, BlockDomainActivity.class);
        intent.putExtra(Consts.type, from);
        startActivity(intent);
    }

    @Override
    public void OnSelectItem(int pos, int type) {
        if (isllPassword == 1)
            return;

        isHomeApp = 1;
        if (type == 0) {
            if (pos == 0) {
                Intent intent = new Intent(context, AppSelection.class);
                startActivityForResult(intent, Consts.APP_SELECTION);
            } else if (pos == 1) {
                // Wallpaper: no permission needed to open the chooser. Take photo asks for CAMERA itself.
                selectImage();
            } else if (pos == 2) {
                //showPasswordSettingDialog();
                if (Consts.IS_BUSINESS_APP == 1) {
                    Intent intent = new Intent(context, ResetPin.class);
                    startActivity(intent);
                } else {
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                        Intent intent = new Intent(context, ResetPin.class);
                        startActivity(intent);
                    } else {
                        showLoggedInDialog();
                    }
                }
            } /*else if (pos == 3) {
                Intent intent = new Intent(context, Labels.class);
                startActivity(intent);
            }*/ else if (pos == 3) {
                showSetTimerDialog();
            } else if (pos == 4) {
                showAddWebShortcutDialog();
            } else if (pos == 5) {
                showSettingDialog();
            } else if (pos == 6) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    if (Consts.IS_BUSINESS_APP == 1) {
                        if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
                            if (intent != null) {
                                startActivity(intent);
                            }
                        } else {
                            showUnInstallApplicationDialog();
                        }
                    } else {
                        showUnInstallApplicationDialog();
                    }
                } else {
                    openBlockDomainScreen(getString(R.string.website_block));
                }
            } else if (pos == 7) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    if (Consts.IS_BUSINESS_APP == 1) {
                        if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                            Intent intent = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.myfiles");
                            if (intent != null) {
                                startActivity(intent);
                            }
                        } else {
                            showUnInstallApplicationDialog();
                        }
                    } else {
                        //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                        openBlockDomainScreen(getString(R.string.website_block));
                        //}
                    }
                } else {
                    openBlockDomainScreen(getString(R.string.app_block));
                }
            } else if (pos == 8) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    if (Consts.IS_BUSINESS_APP == 1) {
                        if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                            showUnInstallApplicationDialog();
                        }
                    } else {
                        if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                            openBlockDomainScreen(getString(R.string.app_block));
                        }
                    }
                }
            } else if (pos == 9) {
                if (CustomDashboardApplication.isDeviceSamsung()) {
                    if (Consts.IS_BUSINESS_APP == 1) {
                        if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                            showUnInstallApplicationDialog();
                        }
                    } else {
                        //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                        openBlockDomainScreen(getString(R.string.website_block));
                        //}
                    }
                }
            } else if (pos == 10) {
                //if (SuperLockState.getInstance(context).isESDKLicenseActivated()) {
                openBlockDomainScreen(getString(R.string.app_block));
                //}
            }
        } else if (type == 1) {
            if (pos == 0) {
                Intent intent = new Intent(context, ApplicationAnalytics.class);
                startActivity(intent);
            } else if (pos == 1) {
                //showGoogleLoggedInDialog();
                /*if (Consts.IS_BUSINESS_APP == 1) {
                    showImportExportDialog();
                } else {
                    showAdvanceSettingsDialog();
                }*/

                /*if (Utils.isMyServiceRunning(context, MediaProjectionService.class)) {
                    showStopScreenShareDialog();
                } else {
                    showScreenShareDialog();
                }*/
            } else if (pos == 2) {
                if (Consts.IS_BUSINESS_APP == 1) {
                    showImportExportDialog();
                } else {
                    showAdvanceSettingsDialog();
                }
            } else if (pos == 3) {
                showAdvanceSettingsDialog();
            }
        } else if (type == 2) {
            if (pos == 0) {
               /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.CONTACT_US));
                startActivity(browserIntent);*/
                CustomDashboardApplication.contactSupport(context);
            } else if (pos == 1) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                /*final ReviewManager manager = ReviewManagerFactory.create(this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                    @Override
                    public void onComplete(@NonNull Task<ReviewInfo> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "onComplete: Task Success");
                            ReviewInfo reviewInfo = task.getResult();
                            if (reviewInfo != null) {
                                Task<Void> flow = manager.launchReviewFlow(CustomDashboardSettings.this, reviewInfo);
                                flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.e(TAG, "onComplete: ");
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "onComplete: Task Fail");
                            showRateAppFallbackDialog();
                        }
                    }
                });*/
            } else if (pos == 2) {
                final String appPackageName = getPackageName();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out Kinder Safety app at: https://play.google.com/store/apps/details?id=" + appPackageName);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, ""));
            } else if (pos == 3) {
                //startActivity(new Intent(context, MyPlansActivity.class));
                Intent intent = new Intent(context, SubscriptionPlanInfoActivity.class);
                intent.putExtra(Consts.from_settings, true);
                intent.putExtra(Consts.is_launcher, false);
                startActivity(intent);
            }
        }
    }

    private void showUnInstallApplicationDialog() {
        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
        dialogBuilder1.setMessage(getResources().getString(R.string.delete_app));
        dialogBuilder1.setPositiveButton("Yes", (dialog, which) -> {
            //finish();
            adminDisable();
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:com.tabnova.aidashboard"));
            startActivity(intent);
            d.dismiss();
        });
        dialogBuilder1.setNegativeButton("No", (dialog, which) -> d.dismiss());
        d = dialogBuilder1.create();
        d.setOnShowListener(dialog -> {
            Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
            doneBtn.setAllCaps(false);
        });
        d.show();
    }

    String isMyLauncherDefaultstr() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.e("Package Name:", str);

        return str;
    }

    // UPDATE PROFILE SCHEDULE
    public void updateProfileSchedule() {
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
                jsonObject.addProperty(Consts.type, CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE));
                jsonObject.addProperty(Consts.daily_limit, dailyTimerModels.get(CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER)).getDailyTimeStr());

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
                                        CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    if (BuildConfig.DEBUG) {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        showApiErrorDialog();
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
                                        //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        showApiErrorDialog();
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


            } else {
                // ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // UPDATE WALL PAPER
    private void updateProfileWallpaper() {
        try {
            if (NetConnectivity.isOnline(context)) {
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                ProfileData profileData = new Gson().fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();

                // API CALL
                Call<JsonObject> calls = feedApi.updateProfile(prepareFilePart("file", profileImageString),
                        MultipartBody.Part.createFormData(Consts.profile_id, String.valueOf(profileId)),
                        MultipartBody.Part.createFormData(Consts.token, CustomDashboardApplication.getString(context, Consts.token)));

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        //Log.e("img_resp", response.body().toString());
                        if (response != null && response.body() != null)
                            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
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

    // GET SCHEDULE EVENT LIST
    private void getScheduleEventList() {
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
                                        scheduleEventModels = new ArrayList<>();
                                        JSONArray dataArray = jsonObject.getJSONArray(Consts.data);
                                        if (dataArray.length() > 0) {

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

                                            scheduleEventListAdapter = new ScheduleEventListAdapter(context, scheduleEventModels, CustomDashboardSettings.this);
                                            recScheduleEvent.setAdapter(scheduleEventListAdapter);

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
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        showApiErrorDialog();
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
                                        //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        showApiErrorDialog();
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


    @Override
    public void onSuccess(Response<JsonObject> response) {
        if (response != null) {
            if (response.code() == 200) {
                String responseString = response.body().toString();
                Log.e("resp", response.body().toString());
                try {
                    final JSONObject jsonObject = new JSONObject(responseString);
                    if (scheduleEventModels.size() > 0) {
                        scheduleEventModels.clear();
                        scheduleEventModels = new ArrayList<>();
                    }
                    //ShowMessage.showSuccess(context, jsonObject.getString(Consts.responseMessage));
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                        getScheduleEventList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    //dialogLoader.hideProgressDialog();
                    String resp = response.errorBody().string();
                    Log.d("tag", "onResponse - Status : " + response.code());
                    Log.e("regErr->", resp);
                    Gson gson = new Gson();
                    TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                    if (response.errorBody() != null) {
                        JsonObject registerResponse = adapter.fromJson(resp);
                        if (BuildConfig.DEBUG) {
                            //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                            showApiErrorDialog();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onFailed(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void OnSelectDay(int pos, boolean type) {
        scheduleDaysModels.get(pos).setSelected(type);
        scheduleAdapter.notifyDataSetChanged();

        if (type) {
            selectedDays.add(scheduleDaysModels.get(pos).getId());
        } else {
            for (int i = 0; i < scheduleDaysModels.size(); i++) {
                if (selectedDays.get(i) == scheduleDaysModels.get(pos).getId()) {
                    selectedDays.remove(i);
                    break;
                }
            }

        }
    }


    @Override
    public void OnSelectEvent(int pos, final String groupId, int isDelete) {

        if (isDelete == 1) {

            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(getResources().getString(R.string.delete_schedule_event));
            dialogBuilder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                    deleteScheduleEvent(groupId);
                    d.dismiss();
                }
            });
            dialogBuilder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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

        } else {

        }

    }

    // GET SCHEDULE EVENT LIST
    private void deleteScheduleEvent(String groupId) {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.group_id, groupId);
                String profileDataStr = CustomDashboardApplication.getString(context, Consts.profile_data);
                ProfileData profileData = new Gson().fromJson(profileDataStr, ProfileData.class);
                int profileId = profileData.getData().getUserProfiles().get(0).getId();
                jsonObject.addProperty(Consts.profile_id, profileId);
                // API CALL
                Call<JsonObject> calls = feedApi.deleteScheduleEvent(jsonObject);
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
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        if (scheduleEventModels.size() > 0) {
                                            scheduleEventModels.clear();
                                            scheduleEventModels = new ArrayList<>();
                                            if (scheduleEventListAdapter != null) {
                                                scheduleEventListAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1)
                                            getScheduleEventList();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    if (BuildConfig.DEBUG) {
                                        //ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        showApiErrorDialog();
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
                                        //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        showApiErrorDialog();
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
                    public void onResponse(@NonNull Call<JsonObject> call, Response<JsonObject> response) {

                        if (response.code() == 200) {
                            String responseString = response.body().toString();
                            Log.e("respCounter", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                //ShowMessage.showError(context, jsonObject.getString(Consts.error));
                                GoogleAuthManager.signOut(context, null);

                                CustomDashboardApplication.setInt(context, Consts.is_logged_in, 0);
                                CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, "0000");
                                CustomDashboardApplication.setInt(context, Consts.IS_PWD, 0);
                                Utils.clearLoginSession(context);
                                btnLogin.setText(getResources().getString(R.string.login));
                                txLoginStr.setVisibility(View.GONE);
                                Toast.makeText(context, "User logout successfully.", Toast.LENGTH_SHORT).show();

                                if (isProfileDeteted) {
                                    Utils.exitApp(CustomDashboardSettings.this);
                                }
                                isProfileDeteted = false;
                             /*   int status = jsonObject.getInt(Consts.error);
                                if (status == 1){
                                    try {
                                        ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                        btnLogin.setText(getResources().getString(R.string.login));
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                } else {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }*/
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
                                        //ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                        showApiErrorDialog();
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

    private void adminDisable() {
        ComponentName admin = new ComponentName(this, com.tabnova.aidashboard.Knox.startup.AdminReceiver.class);
        DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        DPM.removeActiveAdmin(admin);
    }


    @Override
    public void OnSelectShortcut(int pos, int isDelete) {
        if (addedWebShortCutsModels.size() > 0) {
            if (isDelete == 1) {
                showDeleteWebLinkAlert(pos);
            } else {
                for (int i = 0; i < addedWebShortCutsModels.size(); i++) {
                    addedWebShortCutsModels.get(i).setSelected(i != pos);
                }

                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.addedWebShortcutsFile);
                if (f0.exists()) {
                    f0.delete();
                }

                String ser = SerializeObject.objectToString(addedWebShortCutsModels);
                if (ser != null && !ser.equalsIgnoreCase("")) {
                    SerializeObject.WriteSettings(context, ser, Consts.addedWebShortcutsFile);
                }

                addedWebSiteShortcutAdapter.notifyDataSetChanged();
            }
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
                Log.e("serverReqUrl", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.body() != null) {
                            try {
                                Log.e("serverResp", response.body().toString());
                                JSONObject responseObj = new JSONObject(response.body().toString());

                                Object dataObj = responseObj.get(Consts.data);
                                if (dataObj instanceof JSONArray) {
                                    JSONArray dataArray = responseObj.getJSONArray(Consts.data);
                                    if (dataArray.length() > 0) {
                                        addedWebShortCutsModels = new ArrayList<>();
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject jsonObject = dataArray.getJSONObject(i);
                                            AddedWebShortcutModel addedWebShortcutModel = new AddedWebShortcutModel();
                                            addedWebShortcutModel.setId(jsonObject.getInt(Consts.id));
                                            addedWebShortcutModel.setWebSiteName(jsonObject.getString(Consts.website_name));
                                            addedWebShortcutModel.setWebSiteUrl(jsonObject.getString(Consts.website_url));
                                            addedWebShortcutModel.setWebsiteIcon(jsonObject.getString(Consts.website_icon));
                                            addedWebShortcutModel.setSelected(true);
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
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        addedWebSiteShortcutAdapter = new AddedWebShortcutsAdapter(context, addedWebShortCutsModels, CustomDashboardSettings.this);
                        recAddedShortcuts.setAdapter(addedWebSiteShortcutAdapter);
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

    public void deleteWebShortCuts(String id) {
        try {
            if (NetConnectivity.isOnline(context)) {

                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.deleteWebShortcut(CustomDashboardApplication.getString(context, Consts.token), id);
                Log.e("serverReq", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Log.e(TAG, "onResponse: deleteWebShortCuts : ");
                        if (response.body() != null) {
                            Log.e(TAG, "serverReq : deleteWebShortCuts : Body : " + response.body());
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    String error = response.errorBody().string();
                                    Log.e(TAG, "onResponse: deleteWebShortCuts : " + error);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "onFailure: deleteWebShortCuts : ");
                    }
                });
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SHOW UNINSTALL POP UP
    public void showUnInstallDialog() {
        try {
            alertDialogUninstall = new AppCompatDialog(context);
            alertDialogUninstall.setContentView(R.layout.uninstall_apps_list);

            // IMAGE VIEW
            ivUninstallSelectAll = alertDialogUninstall.findViewById(R.id.iv_select_all_selection);
            final ImageView ivShowAllApps = alertDialogUninstall.findViewById(R.id.iv_show_all_selection);
            ImageView ivUpdateTime = alertDialogUninstall.findViewById(R.id.iv_update_time);

            // SPINNER
            final Spinner selectTypeSpinner = alertDialogUninstall.findViewById(R.id.select_type_spinner);

            // TEXT VIEW
            TextView txCancel = alertDialogUninstall.findViewById(R.id.tx_cancel_uninstall);
            TextView txUninstallApps = alertDialogUninstall.findViewById(R.id.tx_uninstall_apps);
            TextView txUninstallAppsFrom = alertDialogUninstall.findViewById(R.id.tx_selected_time);

            // ARRAY ADAPTER
            ArrayAdapter<DateSelectionModel> selectTypeAdapter;

            String dateString = null;
            try {
                long firstTimeAppInstalledDate = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
                txUninstallAppsFrom.setText(dateString);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            long todayTime = CustomDashboardApplication.getLong(context, Consts.todayDateLong);
            long yesterdayTime = CustomDashboardApplication.getLong(context, Consts.yesterDayLong);
            long lastWeekTime = CustomDashboardApplication.getLong(context, Consts.lastWeekLong);
            long lastMonthTime = CustomDashboardApplication.getLong(context, Consts.lastMonthLong);

            // INIT ARRAY LIST
            uninstallAppList = new ArrayList();
            ArrayList<DateSelectionModel> selectedTypeList = new ArrayList();
            selectedTypeList.add(0, new DateSelectionModel(0, dateString, false));
            selectedTypeList.add(1, new DateSelectionModel(1, getResources().getString(R.string.today) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(todayTime)) + ")", false));
            selectedTypeList.add(2, new DateSelectionModel(2, getResources().getString(R.string.yesterday) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(yesterdayTime)) + ")", false));
            selectedTypeList.add(3, new DateSelectionModel(3, getResources().getString(R.string.lastweek) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(lastWeekTime)) + ")", false));
            selectedTypeList.add(4, new DateSelectionModel(4, getResources().getString(R.string.lastMonth) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(lastMonthTime)) + ")", false));
            selectedTypeList.add(5, new DateSelectionModel(5, getResources().getString(R.string.custom), false));

            // INIT SPINNER ADAPTER
            selectTypeAdapter = new ArrayAdapter(context, R.layout.view_spinner_item_white, selectedTypeList);
            selectTypeAdapter.setDropDownViewResource(R.layout.spinner_list);
            selectTypeSpinner.setAdapter(selectTypeAdapter);

            selectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    CustomDashboardApplication.setInt(context, Consts.selectedUnInstallWipeOutType, 0);
                    if (position == 0) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 0);
                        //sessionManager.selectedWipeOutType = 0
                        getUninstallList();
                    } else if (position == 1) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 1);
                        getUninstallList();
                        //sessionManager.selectedWipeOutType = 1
                    } else if (position == 2) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 2);
                        getUninstallList();
                        //sessionManager.selectedWipeOutType = 2
                    } else if (position == 3) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 3);
                        getUninstallList();
                        // sessionManager.selectedWipeOutType = 3
                    } else if (position == 4) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 4);
                        getUninstallList();
                        //sessionManager.selectedWipeOutType = 4
                    } else if (position == 5) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutType, 5);
                        //sessionManager.selectedWipeOutType = 5
                        showDatePickerDialog();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            ivUpdateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectTypeSpinner.performClick();
                }
            });

            // RECYCLER VIEW
            recUninstallAppsView = alertDialogUninstall.findViewById(R.id.rec_uninstall_view);
            recUninstallAppsView.setHasFixedSize(true);
            recUninstallAppsView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            getUninstallList();

            // SELECT ALL UNINSTALL BUTTON CLICK LISTENER
            ivUninstallSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isSelectAllSelected) {
                        ivUninstallSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < uninstallAppList.size(); i++) {
                            uninstallAppList.get(i).setSelected(false);
                        }
                        uninstallAppsListAdapter.notifyDataSetChanged();
                        isSelectAllSelected = false;
                    } else {
                        ivUninstallSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < uninstallAppList.size(); i++) {
                            uninstallAppList.get(i).setSelected(true);
                        }
                        uninstallAppsListAdapter.notifyDataSetChanged();
                        isSelectAllSelected = true;
                    }
                }
            });

            // SHOW ALL BUTTON CLICK LISTENER
            ivShowAllApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isShowAllSelected) {
                        ivShowAllApps.setImageResource(R.drawable.check_box_selected_icon);
                        isShowAllSelected = true;
                        uninstallAppList.clear();
                        uninstallAppList = new ArrayList();
                        for (int i = 0; i < tempList.size(); i++) {
                            if (!tempList.get(i).isSystemApp())
                                uninstallAppList.add(tempList.get(i));
                        }
                        uninstallAppsListAdapter = new UninstallAppsListAdapter(context, uninstallAppList, CustomDashboardSettings.this);
                        recUninstallAppsView.setAdapter(uninstallAppsListAdapter);
                    } else {
                        ivShowAllApps.setImageResource(R.drawable.check_box_unselected_icon);
                        isShowAllSelected = false;
                       /* uninstallAppList.clear();
                        uninstallAppList = new ArrayList();
                        for (int i = 0; i < tempList.size(); i ++){
                            if (tempList.get(i).isAfterDateInstall() && !tempList.get(i).isSystemApp())
                                uninstallAppList.add(tempList.get(i));

                        }

                        uninstallAppsListAdapter = new UninstallAppsListAdapter(context, uninstallAppList, CustomDashboardSettings.this);
                        recUninstallAppsView.setAdapter(uninstallAppsListAdapter);*/

                        getUninstallList();

                    }
                }
            });

            // CANCEL BUTTON CLICK LISTENER
            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogUninstall.dismiss();
                }
            });

            // UNINSTALL BUTTON CLICK LISTENER
            txUninstallApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        try {
                            SuperLockState superLockState = SuperLockState.getInstance(context);
                            if (superLockState.supportedActivation()) {
                                if (superLockState.isESDKLicenseActivated()) {
                                    for (int j = 0; j < uninstallAppList.size(); j++) {
                                        if (uninstallAppList.get(j).isSelected()) {
                                            superLockState.uninstallApplication(uninstallAppList.get(j).getPackageDetails(), false);
                                        }
                                    }
                                    getUninstallList();
                                } else {
                                    for (int j = 0; j < uninstallAppList.size(); j++) {
                                        if (uninstallAppList.get(j).isSelected()) {
                                            Intent intent = new Intent(Intent.ACTION_DELETE);
                                            intent.setData(Uri.parse("package:" + uninstallAppList.get(j).getPackageDetails()));
                                            startActivity(intent);
                                        }
                                    }
                                }
                            } else {

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            alertDialogUninstall.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogUninstall.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogUninstall.getWindow().setGravity(Gravity.CENTER);
            alertDialogUninstall.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET UN INSTALL LIST SELECTION
    public void getUninstallList() {
        uninstallAppList.clear();
        uninstallAppList = new ArrayList<>();
        tempList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();

        long todayTime = CustomDashboardApplication.getLong(context, Consts.todayDateLong);
        long yesterdayTime = CustomDashboardApplication.getLong(context, Consts.yesterDayLong);
        long lastWeekTime = CustomDashboardApplication.getLong(context, Consts.lastWeekLong);
        long lastMonthTime = CustomDashboardApplication.getLong(context, Consts.lastMonthLong);
        long customTime = CustomDashboardApplication.getLong(context, Consts.customDateLong);

        int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedWipeOutType);

        for (int i = 0; i < tempList.size(); i++) {
            Date appInstallDate = new Date(tempList.get(i).getAppInstallTime());
            if (currentSelection == 0) {
                if (tempList.get(i).isAfterDateInstall() && !tempList.get(i).isSystemApp()) {
                    uninstallAppList.add(tempList.get(i));
                }
            } else if (currentSelection == 1) {
                Date currentSelectionDate = new Date(todayTime);
                if (appInstallDate.after(currentSelectionDate) && !tempList.get(i).isSystemApp()) {
                    uninstallAppList.add(tempList.get(i));
                }
            } else if (currentSelection == 2) {
                Date currentSelectionDate = new Date(yesterdayTime);
                if (appInstallDate.after(currentSelectionDate) && !tempList.get(i).isSystemApp()) {
                    uninstallAppList.add(tempList.get(i));
                }
            } else if (currentSelection == 3) {
                Date currentSelectionDate = new Date(lastWeekTime);
                if (appInstallDate.after(currentSelectionDate) && !tempList.get(i).isSystemApp()) {
                    uninstallAppList.add(tempList.get(i));
                }
            } else if (currentSelection == 4) {
                Date currentSelectionDate = new Date(lastMonthTime);
                if (appInstallDate.after(currentSelectionDate) && !tempList.get(i).isSystemApp()) {
                    uninstallAppList.add(tempList.get(i));
                }
            } else if (currentSelection == 5) {
                Date currentSelectionDate = new Date(customTime);
                if (appInstallDate.after(currentSelectionDate) && !tempList.get(i).isSystemApp()) {
                    clearAppsDataList.add(tempList.get(i));
                }
            }

        }

        uninstallAppsListAdapter = new UninstallAppsListAdapter(context, uninstallAppList, this);
        recUninstallAppsView.setAdapter(uninstallAppsListAdapter);
    }

    // SHOW CLEAR APPS DATA DIALOG
    public void showClearAppsDataDialog() {
        try {
            alertClearAppsData = new AppCompatDialog(context);
            alertClearAppsData.setContentView(R.layout.clean_apps_data_list);

            // IMAGE VIEW
            ivClearAppsDataSelectAll = alertClearAppsData.findViewById(R.id.iv_select_all_clear_apps_data_selection);
            final ImageView ivShowAllApps = alertClearAppsData.findViewById(R.id.iv_show_all_clear_apps_data_selection);
            ImageView ivUpdateTime = alertClearAppsData.findViewById(R.id.iv_update_time);

            // SPINNER
            final Spinner selectTypeSpinner = alertClearAppsData.findViewById(R.id.select_type_spinner);

            // TEXT VIEW
            TextView txCancel = alertClearAppsData.findViewById(R.id.tx_cancel_clear_apps_data);
            TextView txClearAppsData = alertClearAppsData.findViewById(R.id.tx_clear_apps_data);
            TextView txClearAppsDataFrom = alertClearAppsData.findViewById(R.id.tx_selected_time);

            ArrayAdapter<DateSelectionModel> selectTypeAdapter;

            String dateString = null;
            try {
                long firstTimeAppInstalledDate = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            long todayTime = CustomDashboardApplication.getLong(context, Consts.todayDateLong);
            long yesterdayTime = CustomDashboardApplication.getLong(context, Consts.yesterDayLong);
            long lastWeekTime = CustomDashboardApplication.getLong(context, Consts.lastWeekLong);
            long lastMonthTime = CustomDashboardApplication.getLong(context, Consts.lastMonthLong);

            // INIT ARRAY LIST
            clearAppsDataList = new ArrayList();
            ArrayList<DateSelectionModel> selectedTypeList = new ArrayList();
            selectedTypeList.add(0, new DateSelectionModel(0, dateString, false));
            selectedTypeList.add(1, new DateSelectionModel(1, getResources().getString(R.string.today) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(todayTime)) + ")", false));
            selectedTypeList.add(2, new DateSelectionModel(2, getResources().getString(R.string.yesterday) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(yesterdayTime)) + ")", false));
            selectedTypeList.add(3, new DateSelectionModel(3, getResources().getString(R.string.lastweek) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(lastWeekTime)) + ")", false));
            selectedTypeList.add(4, new DateSelectionModel(4, getResources().getString(R.string.lastMonth) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(lastMonthTime)) + ")", false));
            selectedTypeList.add(5, new DateSelectionModel(5, getResources().getString(R.string.custom), false));

            // INIT SPINNER ADAPTER
            selectTypeAdapter = new ArrayAdapter(context, R.layout.view_spinner_item_white, selectedTypeList);
            selectTypeAdapter.setDropDownViewResource(R.layout.spinner_list);
            selectTypeSpinner.setAdapter(selectTypeAdapter);

            selectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if (position == 0) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 0);
                        //sessionManager.selectedWipeOutType = 0
                        getCleanAppsDataList();
                    } else if (position == 1) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 1);
                        //sessionManager.selectedWipeOutType = 1
                        getCleanAppsDataList();
                    } else if (position == 2) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 2);
                        // sessionManager.selectedWipeOutType = 2
                        getCleanAppsDataList();
                    } else if (position == 3) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 3);
                        //sessionManager.selectedWipeOutType = 3
                        getCleanAppsDataList();
                    } else if (position == 4) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 4);
                        //sessionManager.selectedWipeOutType = 4
                        getCleanAppsDataList();
                    } else if (position == 5) {
                        CustomDashboardApplication.setInt(context, Consts.selectedWipeOutTypeClnData, 5);
                        //sessionManager.selectedWipeOutType = 5
                        showDatePickerDialog();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            ivUpdateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectTypeSpinner.performClick();
                }
            });

            // RECYCLER VIEW
            recClearAppsDataView = alertClearAppsData.findViewById(R.id.rec_clear_apps_data_view);
            recClearAppsDataView.setHasFixedSize(true);
            recClearAppsDataView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            getCleanAppsDataList();

            // SELECT ALL BUTTON CLICK LISTENER
            ivClearAppsDataSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isSelectAllClearDataSelected) {
                        ivClearAppsDataSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < clearAppsDataList.size(); i++) {
                            clearAppsDataList.get(i).setSelected(false);
                        }
                        clearAppsDataListAdapter.notifyDataSetChanged();
                        isSelectAllClearDataSelected = false;
                    } else {
                        ivClearAppsDataSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < clearAppsDataList.size(); i++) {
                            clearAppsDataList.get(i).setSelected(true);
                        }
                        clearAppsDataListAdapter.notifyDataSetChanged();
                        isSelectAllClearDataSelected = true;
                    }
                }
            });

            // SHOW ALL BUTTON CLICK LISTENER
            ivShowAllApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isShowAllClearDataSelected) {
                        ivShowAllApps.setImageResource(R.drawable.check_box_selected_icon);
                        isShowAllClearDataSelected = true;
                        clearAppsDataList.clear();
                        clearAppsDataList = new ArrayList();
                        for (int i = 0; i < tempList.size(); i++) {
                            if (!tempList.get(i).isSystemApp())
                                clearAppsDataList.add(tempList.get(i));
                        }

                        clearAppsDataListAdapter = new ClearAppsDataListAdapter(context, clearAppsDataList, CustomDashboardSettings.this);
                        recClearAppsDataView.setAdapter(clearAppsDataListAdapter);
                    } else {
                        ivShowAllApps.setImageResource(R.drawable.check_box_unselected_icon);
                        isShowAllClearDataSelected = false;
                    /*    clearAppsDataList.clear();
                        clearAppsDataList = new ArrayList();
                        for (int i = 0; i < tempList.size(); i ++){
                            if (CustomDashboardApplication.getInt(context, Consts.selectedWipeOutTypeClnData) == 0){
                                if (tempList.get(i).isAfterDateInstall()){
                                    clearAppsDataList.add(tempList.get(i));
                                }
                            } else if (CustomDashboardApplication.getInt(context, Consts.selectedWipeOutTypeClnData) == 1) {
                                if (tempList.get(i).isAfterDateInstall()){
                                    clearAppsDataList.add(tempList.get(i));
                                }
                            }
                        }

                        clearAppsDataListAdapter = new ClearAppsDataListAdapter(context, clearAppsDataList, CustomDashboardSettings.this);
                        recClearAppsDataView.setAdapter(clearAppsDataListAdapter);*/
                        getCleanAppsDataList();
                    }
                }
            });


            // CANCEL BUTTON CLICK LISTENER
            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertClearAppsData.dismiss();
                }
            });


            // CLEAN  BUTTON CLICK LISTENER
            txClearAppsData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (CustomDashboardApplication.isDeviceSamsung()) {
                        try {
                            SuperLockState superLockState = SuperLockState.getInstance(context);
                            if (superLockState.supportedActivation()) {
                                if (superLockState.isESDKLicenseActivated()) {
                                    for (int i = 0; i < clearAppsDataList.size(); i++) {
                                        superLockState.wipeApplicationData(clearAppsDataList.get(i).getPackageDetails());
                                    }
                                }
                            } else {

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            });


            alertClearAppsData.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertClearAppsData.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertClearAppsData.getWindow().setGravity(Gravity.CENTER);
            alertClearAppsData.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET CLEAN APPS DATA LIST
    public void getCleanAppsDataList() {
        clearAppsDataList.clear();
        clearAppsDataList = new ArrayList<>();
        tempList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();

        long todayTime = CustomDashboardApplication.getLong(context, Consts.todayDateLong);
        long yesterdayTime = CustomDashboardApplication.getLong(context, Consts.yesterDayLong);
        long lastWeekTime = CustomDashboardApplication.getLong(context, Consts.lastWeekLong);
        long lastMonthTime = CustomDashboardApplication.getLong(context, Consts.lastMonthLong);
        long customTime = CustomDashboardApplication.getLong(context, Consts.customDateLong);

        int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedWipeOutTypeClnData);

        for (int i = 0; i < tempList.size(); i++) {
            Date appInstallDate = new Date(tempList.get(i).getAppInstallTime());
            if (currentSelection == 0) {
                if (tempList.get(i).isAfterDateInstall()) {
                    clearAppsDataList.add(tempList.get(i));
                }
            } else if (currentSelection == 1) {
                Date currentSelectionDate = new Date(todayTime);
                if (appInstallDate.after(currentSelectionDate)) {
                    clearAppsDataList.add(tempList.get(i));
                }
            } else if (currentSelection == 2) {
                Date currentSelectionDate = new Date(yesterdayTime);
                if (appInstallDate.after(currentSelectionDate)) {
                    clearAppsDataList.add(tempList.get(i));
                }
            } else if (currentSelection == 3) {
                Date currentSelectionDate = new Date(lastWeekTime);
                if (appInstallDate.after(currentSelectionDate)) {
                    clearAppsDataList.add(tempList.get(i));
                }
            } else if (currentSelection == 4) {
                Date currentSelectionDate = new Date(lastMonthTime);
                if (appInstallDate.after(currentSelectionDate)) {
                    clearAppsDataList.add(tempList.get(i));
                }
            } else if (currentSelection == 5) {
                Date currentSelectionDate = new Date(customTime);
                if (appInstallDate.after(currentSelectionDate)) {
                    clearAppsDataList.add(tempList.get(i));
                }
            }

        }

        clearAppsDataListAdapter = new ClearAppsDataListAdapter(context, clearAppsDataList, this);
        recClearAppsDataView.setAdapter(clearAppsDataListAdapter);
    }

    // CALL LOG DIALOG
    @SuppressLint("MissingPermission")
    private void showCallLogsDialog() {
        try {
            alertCallLogsDialog = new AppCompatDialog(context);
            alertCallLogsDialog.setContentView(R.layout.call_logs_list);
            alertCallLogsDialog.setCancelable(true);

            // IMAGE VIEW
            ivCallLogsSelectAll = alertCallLogsDialog.findViewById(R.id.iv_select_all_selection);
            ImageView ivUpdateTime = alertCallLogsDialog.findViewById(R.id.iv_update_time);

            // SPINNER
            final Spinner selectTypeSpinner = alertCallLogsDialog.findViewById(R.id.select_type_spinner);

            // TEXT VIEW
            TextView txCancel = alertCallLogsDialog.findViewById(R.id.tx_cancel_call_logs);
            TextView txClearCallLogs = alertCallLogsDialog.findViewById(R.id.tx_clear_call_logs);

            // RECYCLER VIEW
            recCallLogsView = alertCallLogsDialog.findViewById(R.id.rec_call_logs_view);
            recCallLogsView.setHasFixedSize(true);
            recCallLogsView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            ArrayAdapter<DateSelectionModel> selectTypeAdapter;

            String dateString = "";
            try {
                long firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // INIT ARRAY LIST
            callLogsModels = new ArrayList();
            ArrayList<DateSelectionModel> selectedTypeList = new ArrayList();
            selectedTypeList.add(0, new DateSelectionModel(0, dateString, false));
            selectedTypeList.add(
                    1,
                    new DateSelectionModel(
                            1,
                            getResources().getString(R.string.today) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(todayTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    2,
                    new DateSelectionModel(
                            2,
                            getResources().getString(R.string.yesterday) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(yesterdayTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    3,
                    new DateSelectionModel(
                            3,
                            getResources().getString(R.string.lastweek) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastWeekTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    4,
                    new DateSelectionModel(
                            4,
                            getResources().getString(R.string.lastMonth) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastMonthTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    5,
                    new DateSelectionModel(5, getResources().getString(R.string.custom), false)
            );

            getCallLogs();

            // INIT SPINNER ADAPTER
            selectTypeAdapter = new ArrayAdapter(context, R.layout.view_spinner_item, selectedTypeList);
            selectTypeAdapter.setDropDownViewResource(R.layout.spinner_list);
            selectTypeSpinner.setAdapter(selectTypeAdapter);

            selectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    CustomDashboardApplication.setInt(context, Consts.selectedRemoveCallLogType, position);
                    if (position == 5) {
                        showDatePickerDialog();
                    } else {
                        getCallLogs();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            selectTypeSpinner.setSelection(CustomDashboardApplication.getInt(context, Consts.selectedRemoveCallLogType));

            ivUpdateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTypeSpinner.performClick();
                }
            });

            ivCallLogsSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectAllCallLogsSelected) {
                        ivCallLogsSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < callLogsModels.size(); i++) {
                            callLogsModels.get(i).setSelected(false);
                        }
                        callLogsListAdapter.notifyDataSetChanged();
                        isSelectAllCallLogsSelected = false;
                    } else {
                        ivCallLogsSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < callLogsModels.size(); i++) {
                            callLogsModels.get(i).setSelected(true);
                        }
                        callLogsListAdapter.notifyDataSetChanged();
                        isSelectAllCallLogsSelected = true;
                    }
                }
            });

            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertCallLogsDialog.dismiss();
                }
            });

            txClearCallLogs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callLogsModels.size() > 0) {
                        List<CallLogsModel> selectedCallLogs = new ArrayList<>();

                        for (int i = 0; i < callLogsModels.size(); i++) {
                            if (callLogsModels.get(i).isSelected()) {
                                selectedCallLogs.add(callLogsModels.get(i));
                            }
                        }
                        if (selectedCallLogs.size() > 0) {
                            for (int i = 0; i < selectedCallLogs.size(); i++) {
                                // val queryString = "NUMBER='" + selectedCallLogs[i].number.toString() + "'"
                                String queryString = CallLog.Calls._ID + " = " + selectedCallLogs.get(i).getId();
                                String idd = String.valueOf(selectedCallLogs.get(i).getId());
                                /*if (ActivityCompat.checkSelfPermission(CustomDashboardSettings.this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }*/
                                /*context.getContentResolver().delete(
                                        CallLog.Calls.CONTENT_URI,
                                        CallLog.Calls._ID + " = ? ",
                                        new String[]{idd}
                                );*/
                            }
                            alertCallLogsDialog.dismiss();
                        }
                    }
                }
            });

            alertCallLogsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertCallLogsDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            alertCallLogsDialog.getWindow().setGravity(Gravity.CENTER);
            alertCallLogsDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET CALL LOGS
    private void getCallLogs() {
        try {
            callLogsModels.clear();
            callLogsModels = new ArrayList();
            //callLogsModels = AppCleanerApplication.getCallLogs(context)
            ArrayList<CallLogsModel> tempList = CustomDashboardApplication.getCallLogs(context);

            long customTime = CustomDashboardApplication.getCustomLong(context, Consts.callLogDateLong);

            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedRemoveCallLogType);

            long firstTimeAppInstalledDate = 0;
            try {
                firstTimeAppInstalledDate = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (tempList.size() > 0) {
                for (int i = 0; i < tempList.size(); i++) {
                    Date fileModifiedDate = new Date(tempList.get(i).getDateLong());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            callLogsModels.add(tempList.get(i));
                        }
                    }
                }
            }
            callLogsListAdapter = new CallLogsListAdapter(context, callLogsModels, this);
            if (recCallLogsView != null)
                recCallLogsView.setAdapter(callLogsListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // WEB COOKIE DIALOG DIALOG
    private void showWebCookieDialog() {
        try {
            alertClearWebCookies = new AppCompatDialog(context);
            alertClearWebCookies.setContentView(R.layout.web_cookies_list);
            alertClearWebCookies.setCancelable(true);

            // IMAGE VIEW
            ivWebCookieSelectAll = alertClearWebCookies.findViewById(R.id.iv_select_all_selection);
            ImageView ivUpdateTime = alertClearWebCookies.findViewById(R.id.iv_update_time);

            // SPINNER
            final Spinner selectTypeSpinner = alertClearWebCookies.findViewById(R.id.select_type_spinner);

            // TEXT VIEW
            TextView txCancel = alertClearWebCookies.findViewById(R.id.tx_cancel_web_cookies);
            TextView txUninstallApps =
                    alertClearWebCookies.findViewById(R.id.tx_clear_web_cookies);
            TextView txUninstallAppsFrom =
                    alertClearWebCookies.findViewById(R.id.tx_selected_time);

            // RECYCLER VIEW
            recWebCookieView =
                    alertClearWebCookies.findViewById(R.id.rec_sms_logs_view);
            recWebCookieView.setHasFixedSize(true);
            recWebCookieView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            ArrayAdapter<DateSelectionModel> selectTypeAdapter;

            String dateString = "";
            try {
                long firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // INIT ARRAY LIST
            webCookiesModels = new ArrayList();
            ArrayList<DateSelectionModel> selectedTypeList = new ArrayList();
            selectedTypeList.add(0, new DateSelectionModel(0, dateString, false));

            selectedTypeList.add(
                    1,
                    new DateSelectionModel(
                            1,
                            getResources().getString(R.string.today) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(todayTime)
                            ) + ")",
                            false
                    )
            );

            selectedTypeList.add(
                    2,
                    new DateSelectionModel(
                            2,
                            getResources().getString(R.string.yesterday) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(yesterdayTime)
                            ) + ")",
                            false
                    )
            );

            selectedTypeList.add(
                    3,
                    new DateSelectionModel(
                            3,
                            getResources().getString(R.string.lastweek) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastWeekTime)
                            ) + ")",
                            false
                    )
            );

            selectedTypeList.add(
                    4,
                    new DateSelectionModel(
                            4,
                            getResources().getString(R.string.lastMonth) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastMonthTime)
                            ) + ")",
                            false
                    )
            );

            selectedTypeList.add(
                    5,
                    new DateSelectionModel(5, getResources().getString(R.string.custom), false)
            );

            getWebCookieList();

            // INIT SPINNER ADAPTER
            selectTypeAdapter = new ArrayAdapter(context, R.layout.view_spinner_item_white, selectedTypeList);
            selectTypeAdapter.setDropDownViewResource(R.layout.spinner_list);
            selectTypeSpinner.setAdapter(selectTypeAdapter);

            selectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    CustomDashboardApplication.setInt(context, Consts.selectedClearWebCookiesType, position);
                    if (position == 5) {
                        showDatePickerDialog();
                    } else {
                        getWebCookieList();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            selectTypeSpinner.setSelection(CustomDashboardApplication.getInt(context, Consts.selectedClearWebCookiesType));

            ivUpdateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTypeSpinner.performClick();
                }
            });

            ivWebCookieSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectAllWebCookiesSelected) {
                        ivWebCookieSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < webCookiesModels.size(); i++) {
                            webCookiesModels.get(i).setSelected(false);
                        }
                        webCookiesListAdapter.notifyDataSetChanged();
                        isSelectAllWebCookiesSelected = false;
                    } else {
                        ivWebCookieSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < webCookiesModels.size(); i++) {
                            webCookiesModels.get(i).setSelected(true);
                        }
                        webCookiesListAdapter.notifyDataSetChanged();
                        isSelectAllWebCookiesSelected = true;
                    }
                }
            });

            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertClearWebCookies.dismiss();
                }
            });

            txUninstallApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<AppModel> clearAppsData = new ArrayList<>();
                    for (int i = 0; i < webCookiesModels.size(); i++) {
                        if (webCookiesModels.get(i).isSelected()) {
                            clearAppsData.add(webCookiesModels.get(i));
                        }
                    }

                    if (clearAppsData.size() > 0) {
                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            try {
                                SuperLockState superLockState =
                                        SuperLockState.getInstance(context);
                                if (superLockState.supportedActivation()) {
                                    if (superLockState.isESDKLicenseActivated()) {
                                        for (int i = 0; i < clearAppsData.size(); i++) {
                                            superLockState.wipeApplicationData(clearAppsData.get(i).getPackageDetails());
                                        }
                                    }
                                }
                                alertClearWebCookies.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(
                                context,
                                getResources().getString(R.string.no_apps),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });

            alertClearWebCookies.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertClearWebCookies.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            alertClearWebCookies.getWindow().setGravity(Gravity.CENTER);
            alertClearWebCookies.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // WIPE WEB COOKIES
    private ArrayList<AppModel> wipeWebCookiesData() {
        ArrayList<AppModel> finalList = new ArrayList();

        ArrayList<AppModel> cleanAppList = new ArrayList();

        cleanAppList.add(0, new AppModel(0, false, "com.sec.android.app.sbrowser", "Samsung Browser"));
        cleanAppList.add(1, new AppModel(1, false, "org.mozilla.firefox", "Firefox"));
        cleanAppList.add(
                2,
                new AppModel(2, false, "com.android.chrome", "Chrome")
        );

        if (cleanAppList.size() > 0) {
            finalList.addAll(cleanAppList);
            if (CustomDashboardApplication.isDeviceSamsung()) {
                try {
                    SuperLockState superLockState = SuperLockState.getInstance(context);
                    if (superLockState.supportedActivation()) {
                        if (superLockState.isESDKLicenseActivated()) {
                            for (int i = 0; i < cleanAppList.size(); i++) {
                                superLockState.wipeApplicationData(cleanAppList.get(i).getPackageDetails());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return finalList;
    }


    // GET WEB COOKIE LIST
    private void getWebCookieList() {
        try {
            webCookiesModels = new ArrayList();

            webCookiesModels.add(
                    0,
                    new AppModel(0, false, "com.sec.android.app.sbrowser", "Samsung Browser")
            );
            webCookiesModels.add(1, new AppModel(1, false, "org.mozilla.firefox", "Firefox"));
            webCookiesModels.add(
                    2,
                    new AppModel(2, false, "com.android.chrome", "Chrome")
            );

            webCookiesListAdapter = new WebCookiesListAdapter(context, webCookiesModels, this);
            if (recWebCookieView != null)
                recWebCookieView.setAdapter(webCookiesListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE FILE/FOLDERS DIALOG
    private void showDeleteFileFolderDialog() {
        try {

            alertDeleteFIleFolderDialog = new AppCompatDialog(context);
            alertDeleteFIleFolderDialog.setContentView(R.layout.delete_file_folders_list);

            // TEXT VIEW
            TextView txOpenFileManager =
                    alertDeleteFIleFolderDialog.findViewById(R.id.tx_add_file_folder);
            TextView txCancel =
                    alertDeleteFIleFolderDialog.findViewById(R.id.tx_cancel_delete_folder);
            TextView txDeleteFile =
                    alertDeleteFIleFolderDialog.findViewById(R.id.tx_delete_folder);
            ImageView ivUpdateTime =
                    alertDeleteFIleFolderDialog.findViewById(R.id.iv_update_time);

            // IMAGE VIEW
            ivDeleteFileSelectAll =
                    alertDeleteFIleFolderDialog.findViewById(R.id.iv_select_all_selection);

            // RECYCLER VIEW
            recDeleteFileFolderView =
                    alertDeleteFIleFolderDialog.findViewById(R.id.rec_delete_file_view);
            recDeleteFileFolderView.setHasFixedSize(true);
            recDeleteFileFolderView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            recPredefineDeleteFileFolderView =
                    alertDeleteFIleFolderDialog.findViewById(R.id.rec_delete_predefine_view);
            recPredefineDeleteFileFolderView.setHasFixedSize(true);
            recPredefineDeleteFileFolderView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));


            // SPINNER
            final Spinner selectTypeSpinner =
                    alertDeleteFIleFolderDialog.findViewById(R.id.select_type_spinner);

            ArrayAdapter<DateSelectionModel> selectTypeAdapter;

            String dateString = "";
            try {
                long firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // INIT ARRAY LIST
            deleteFileFolderModels = new ArrayList();
            ArrayList<DateSelectionModel> selectedTypeList = new ArrayList();
            selectedTypeList.add(0, new DateSelectionModel(0, dateString, false));
            selectedTypeList.add(
                    1,
                    new DateSelectionModel(
                            1,
                            getResources().getString(R.string.today) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(todayTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    2,
                    new DateSelectionModel(
                            2,
                            getResources().getString(R.string.yesterday) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(yesterdayTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    3,
                    new DateSelectionModel(
                            3,
                            getResources().getString(R.string.lastweek) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastWeekTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    4,
                    new DateSelectionModel(
                            4,
                            getResources().getString(R.string.lastMonth) + " (" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(
                                    new Date(lastMonthTime)
                            ) + ")",
                            false
                    )
            );
            selectedTypeList.add(
                    5,
                    new DateSelectionModel(5, getResources().getString(R.string.custom), false)
            );

            getDeleteFiles();

            // INIT SPINNER ADAPTER
            selectTypeAdapter = new ArrayAdapter(context, R.layout.view_spinner_item_white, selectedTypeList);
            selectTypeAdapter.setDropDownViewResource(R.layout.spinner_list);
            selectTypeSpinner.setAdapter(selectTypeAdapter);

            selectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    CustomDashboardApplication.setInt(context, Consts.selectedDeleteFileFolderType, position);
                    if (position == 5) {
                        showDatePickerDialog();
                    } else {
                        getDeleteFiles();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            selectTypeSpinner.setSelection(CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType));

            // OPEN FILE MANAGER DIALOG
            txOpenFileManager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFileManager();
                }
            });

            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDeleteFIleFolderDialog.dismiss();
                }
            });

            txDeleteFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFileFolders();
                }
            });

            ivUpdateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTypeSpinner.performClick();
                }
            });

            ivDeleteFileSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectAllDeleteFileSelected) {
                        ivDeleteFileSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < deleteFileFolderModels.size(); i++) {
                            deleteFileFolderModels.get(i).setSelected(false);
                        }
                        deleteFileFolderListAdapter.notifyDataSetChanged();

                        for (int j = 0; j < preDeleteFileFolderModels.size(); j++) {
                            preDeleteFileFolderModels.get(j).setSelected(false);
                        }
                        preDeleteFileFolderListAdapter.notifyDataSetChanged();
                        isSelectAllDeleteFileSelected = false;
                    } else {
                        ivDeleteFileSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < deleteFileFolderModels.size(); i++) {
                            deleteFileFolderModels.get(i).setSelected(true);
                        }
                        deleteFileFolderListAdapter.notifyDataSetChanged();

                        for (int j = 0; j < preDeleteFileFolderModels.size(); j++) {
                            preDeleteFileFolderModels.get(j).setSelected(true);
                        }
                        preDeleteFileFolderListAdapter.notifyDataSetChanged();
                        isSelectAllDeleteFileSelected = true;
                    }
                }
            });

            alertDeleteFIleFolderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDeleteFIleFolderDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            alertDeleteFIleFolderDialog.getWindow().setGravity(Gravity.CENTER);
            alertDeleteFIleFolderDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET DELETE FOLDER
    void getDeleteFiles() {
        try {

            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            deleteFileFolderModels = new ArrayList();
            preDeleteFileFolderModels = new ArrayList();

            long customTime = CustomDashboardApplication.getLong(context, Consts.deleteFilesDateLong);

            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            preDeleteFileFolderModels.add(
                    0,
                    new SelectedFileFolderModel(
                            0,
                            splitString[0] + "Download",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "Download"),
                            true,
                            true,
                            getDownloadFolderDetails()
                    )
            );
            preDeleteFileFolderModels.add(
                    1,
                    new SelectedFileFolderModel(
                            1,
                            splitString[0] + "Documents",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "Documents"),
                            true,
                            true,
                            getDocumentFolderDetails()
                    )
            );
            preDeleteFileFolderModels.add(
                    2,
                    new SelectedFileFolderModel(
                            2,
                            splitString[0] + "Pictures",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "Pictures"),
                            true,
                            true,
                            getPicturesFolderDetails()
                    )
            );
            preDeleteFileFolderModels.add(
                    3,
                    new SelectedFileFolderModel(
                            3,
                            splitString[0] + "Music",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "Music"),
                            true,
                            true,
                            getMusicFolderDetails()
                    )
            );
            preDeleteFileFolderModels.add(
                    4,
                    new SelectedFileFolderModel(
                            4,
                            splitString[0] + "Movies",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "Movies"),
                            true,
                            true,
                            getMoviesFolderDetails()
                    )
            );
            preDeleteFileFolderModels.add(
                    5,
                    new SelectedFileFolderModel(
                            5,
                            splitString[0] + "DCIM",
                            CustomDashboardApplication.getFileFolderModifiedDate(splitString[0] + "DCIM"),
                            true,
                            true,
                            getDCIMFolderDetails()
                    )
            );

            ArrayList<SelectedFileFolderModel> tempList = new ArrayList();
            String ser1 = SerializeObject.ReadSettings(context, Consts.fileManagerListFile);
            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                ArrayList<SelectedFileFolderModel> obj = (ArrayList<SelectedFileFolderModel>) SerializeObject.stringToObject(ser1);
                // Then cast it to your object and
                tempList = obj;
            }

            long firstTimeAppInstalledDate = 0;
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (tempList.size() > 0) {
                for (int i = 0; i < tempList.size(); i++) {
                    if (i < 5) {
                        deleteFileFolderModels.add(tempList.get(i));
                    } else {
                        Date fileModifiedDate = new Date(tempList.get(i).getFileCreateDate());
                        if (currentSelection == 0) {
                            Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        } else if (currentSelection == 1) {
                            Date currentSelectionDate = new Date(todayTime);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        } else if (currentSelection == 2) {
                            Date currentSelectionDate = new Date(yesterdayTime);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        } else if (currentSelection == 3) {
                            Date currentSelectionDate = new Date(lastWeekTime);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        } else if (currentSelection == 4) {
                            Date currentSelectionDate = new Date(lastMonthTime);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        } else if (currentSelection == 5) {
                            Date currentSelectionDate = new Date(customTime);
                            if (fileModifiedDate.after(currentSelectionDate)) {
                                deleteFileFolderModels.add(tempList.get(i));
                            }
                        }
                    }
                }
                //deleteFileFolderModels.addAll(tempList)
            } else {
                File isFileExist = new File(getFilesDir().getAbsolutePath(), Consts.fileManagerListFile);
                if (isFileExist.exists()) {
                    isFileExist.delete();
                }
                /*  deleteFileFolderModels.add(0, SelectedFileFolderModel(0,  splitString[0] + "Download", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "Download"),false, true))
                  deleteFileFolderModels.add(1, SelectedFileFolderModel(1,  splitString[0] + "Documents", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "Documents"), false, true))
                  deleteFileFolderModels.add(2, SelectedFileFolderModel(2,  splitString[0] + "Pictures", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "Pictures"), false, true))
                  deleteFileFolderModels.add(3, SelectedFileFolderModel(3,  splitString[0] + "Music", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "Music"), false, true))
                  deleteFileFolderModels.add(4, SelectedFileFolderModel(4,  splitString[0] + "Movies", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "Movies"), false, true))
                  deleteFileFolderModels.add(5, SelectedFileFolderModel(5,  splitString[0] + "DCIM", AppCleanerApplication.getFileFolderModifiedDate(splitString[0] + "DCIM"), false, true))*/
                for (int j = 0; j < tempList.size(); j++) {
                    Date fileModifiedDate = new Date(tempList.get(j).getFileCreateDate());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate)) {
                            deleteFileFolderModels.add(tempList.get(j));
                        }
                    }
                }

                String ser2 = SerializeObject.objectToString(deleteFileFolderModels);
                if (ser2 != null && !ser2.equals("")) {
                    SerializeObject.WriteSettings(context, ser2, Consts.fileManagerListFile);
                } else {
                    SerializeObject.WriteSettings(context, "", Consts.fileManagerListFile);
                }
            }

            // ADAPTER
            preDeleteFileFolderListAdapter =
                    new DeletePredefineFileFolderListAdapter(context, preDeleteFileFolderModels, this);
            if (recPredefineDeleteFileFolderView != null)
                recPredefineDeleteFileFolderView.setAdapter(preDeleteFileFolderListAdapter);
            deleteFileFolderListAdapter =
                    new DeleteFileFolderListAdapter(context, deleteFileFolderModels, this);
            if (recDeleteFileFolderView != null)
                recDeleteFileFolderView.setAdapter(deleteFileFolderListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET DOWNLOAD FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getDownloadFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> downloadFiles = new ArrayList();
        try {
            long customTime = CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            File downloadDir = new File(splitString[0] + "Download");
            File[] downloadDirFiles = downloadDir.listFiles();

            String dateString = "";
            long firstTimeAppInstalledDate = 0;
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (downloadDirFiles != null && downloadDirFiles.length > 0) {
                for (int i = 0; i < downloadDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(downloadDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            downloadFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            downloadDirFiles[i].getAbsolutePath(),
                                            true,
                                            downloadDirFiles[i].isDirectory()
                                    )
                            );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadFiles;
    }

    // GET DOCUMENT FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getDocumentFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> documentFiles = new ArrayList();
        try {

            long customTime =
                    CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            long firstTimeAppInstalledDate = 0;
            String dateString = "";
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            File documentDir = new File(splitString[0] + "Documents");
            File[] documentDirFiles = documentDir.listFiles();

            if (documentDirFiles != null && documentDirFiles.length > 0) {
                for (int i = 0; i < documentDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(documentDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            documentFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            documentDirFiles[i].getAbsolutePath(),
                                            true,
                                            documentDirFiles[i].isDirectory()
                                    )
                            );
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentFiles;
    }

    // GET PICTURES FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getPicturesFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> pictureFiles = new ArrayList();
        try {
            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            long customTime =
                    CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            long firstTimeAppInstalledDate = 0;
            String dateString = "";
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            File pictureDir = new File(splitString[0] + "Pictures");
            File[] pictureDirFiles = pictureDir.listFiles();

            if (pictureDirFiles != null && pictureDirFiles.length > 0) {
                for (int i = 0; i < pictureDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(pictureDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            pictureFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            pictureDirFiles[i].getAbsolutePath(),
                                            true,
                                            pictureDirFiles[i].isDirectory()
                                    )
                            );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pictureFiles;
    }

    // GET MUSIC FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getMusicFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> musicFiles = new ArrayList();
        try {
            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            long customTime =
                    CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            long firstTimeAppInstalledDate = 0;
            String dateString = "";
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            File musicDir = new File(splitString[0] + "Music");
            File[] musicDirFiles = musicDir.listFiles();

            if (musicDirFiles != null && musicDirFiles.length > 0) {
                for (int i = 0; i < musicDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(musicDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            musicFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            musicDirFiles[i].getAbsolutePath(),
                                            true,
                                            musicDirFiles[i].isDirectory()
                                    )
                            );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return musicFiles;
    }

    // GET MOVIES FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getMoviesFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> moviesFiles = new ArrayList();
        try {
            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            long customTime =
                    CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            long firstTimeAppInstalledDate = 0;
            String dateString = "";
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            File moviesDir = new File(splitString[0] + "Movies");
            File[] moviesDirFiles = moviesDir.listFiles();


            if (moviesDirFiles != null && moviesDirFiles.length > 0) {
                for (int i = 0; i < moviesDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(moviesDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            moviesFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            moviesDirFiles[i].getAbsolutePath(),
                                            true,
                                            moviesDirFiles[i].isDirectory()
                                    )
                            );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moviesFiles;
    }

    // GET DCIM FOLDER DETAILS
    ArrayList<SubSelectedFileFolderModel> getDCIMFolderDetails() {
        ArrayList<SubSelectedFileFolderModel> dcimFiles = new ArrayList();
        try {
            String fileString = getExternalFilesDir(null).toString();
            Log.e("fileString", fileString);
            String[] splitString = fileString.split("Android");

            long customTime =
                    CustomDashboardApplication.getCustomLong(context, Consts.deleteFilesDateLong);
            int currentSelection = CustomDashboardApplication.getInt(context, Consts.selectedDeleteFileFolderType);

            long firstTimeAppInstalledDate = 0;
            String dateString = "";
            try {
                firstTimeAppInstalledDate =
                        getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                dateString =
                        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(firstTimeAppInstalledDate));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            File dcimDir = new File(splitString[0] + "DCIM");
            File[] dcimDirFiles = dcimDir.listFiles();

            if (dcimDirFiles != null && dcimDirFiles.length > 0) {
                for (int i = 0; i < dcimDirFiles.length; i++) {
                    Date fileModifiedDate = new Date(dcimDirFiles[i].lastModified());
                    if (currentSelection == 0) {
                        Date currentSelectionDate = new Date(firstTimeAppInstalledDate);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 1) {
                        Date currentSelectionDate = new Date(todayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 2) {
                        Date currentSelectionDate = new Date(yesterdayTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 3) {
                        Date currentSelectionDate = new Date(lastWeekTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 4) {
                        Date currentSelectionDate = new Date(lastMonthTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    } else if (currentSelection == 5) {
                        Date currentSelectionDate = new Date(customTime);
                        if (fileModifiedDate.after(currentSelectionDate))
                            dcimFiles.add(
                                    i,
                                    new SubSelectedFileFolderModel(
                                            i,
                                            dcimDirFiles[i].getAbsolutePath(),
                                            true,
                                            dcimDirFiles[i].isDirectory()
                                    )
                            );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dcimFiles;
    }

    // DELETE FILE/FOLDER
    private void deleteFileFolders() {
        List<String> command = new ArrayList<>();

        try {
            ArrayList<SelectedFileFolderModel> selectedFileFolder = new ArrayList<>();

            for (int i = 0; i < deleteFileFolderModels.size(); i++) {
                if (deleteFileFolderModels.get(i).isSelected()) {
                    selectedFileFolder.add(deleteFileFolderModels.get(i));
                }
            }

            /*dialogLoader.initFileFolderDialog(selectedFileFolder as ArrayList<SelectedFileFolderModel>)
            dialogLoader.showProgressDialog()*/
            for (int j = 0; j < selectedFileFolder.size(); j++) {
                if (selectedFileFolder.get(j).isSelected()) {

                    command.clear();
                    command.add("/system/bin/rm");
                    command.add("-rf");
                    command.add(selectedFileFolder.get(j).getFileStr());

                    // start the sub process
                    ProcessBuilder pb = new ProcessBuilder(command);
                    Process process = pb.start();
                    process.waitFor();
                }
            }
            getDeleteFiles();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // UNINSTALL APPS POP UP SELECTION
    @Override
    public void onSelectApp(int position) {
        if (isSelectAllSelected) {
            ivUninstallSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
            isSelectAllSelected = false;
        }
        uninstallAppsListAdapter.notifyDataSetChanged();
    }

    // CLEAR APPS POP UP SELECTION
    @Override
    public void onSelectClearApp(int position) {
        if (isSelectAllClearDataSelected) {
            ivClearAppsDataSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
            isSelectAllClearDataSelected = false;
        }
        clearAppsDataListAdapter.notifyDataSetChanged();
    }

    // SHOW DATE PICKER DIALOG
    public void showDatePickerDialog() {
        try {
            Calendar c = Calendar.getInstance();
            final int year = c.get(Calendar.YEAR);
            final int month = c.get(Calendar.MONTH);
            final int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(Calendar.YEAR, year);
                    newDate.set(Calendar.MONTH, month);
                    newDate.set(Calendar.DAY_OF_MONTH, day);
                    //newDate.set(year, month, day);
                    String dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(newDate.getTimeInMillis()));

                    showTimePickerDialog(newDate);
                }

            }, year, month, day);

            datePicker.setTitle(getResources().getString(R.string.select_date));
            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // SHOW TIME PICKER DIALOG
    public void showTimePickerDialog(final Calendar instance) {
        try {
            int hour = instance.get(Calendar.HOUR_OF_DAY);
            int minute = instance.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    Calendar currentTime = Calendar.getInstance();
                    int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int currentDay = currentTime.get(Calendar.DAY_OF_MONTH);

                    Calendar newDate = Calendar.getInstance();
                    newDate.set(
                            instance.get(Calendar.YEAR),
                            instance.get(Calendar.MONTH),
                            instance.get(Calendar.DAY_OF_MONTH),
                            selectedHour,
                            selectedMinute
                    );
                    String dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date(newDate.getTimeInMillis()));
                    CustomDashboardApplication.setCustomLong(context, Consts.customDateLong, newDate.getTimeInMillis());
                    if (currentWipeSelection == 0) {
                        if (CustomDashboardApplication.isDeviceSamsung()) {
                            getUninstallList();
                        } else {
                            CustomDashboardApplication.setCustomLong(context, Consts.deleteFilesDateLong, newDate.getTimeInMillis());
                            getDeleteFiles();
                        }
                    } else if (currentWipeSelection == 1) {
                        getCleanAppsDataList();
                    } else if (currentWipeSelection == 2) {
                        CustomDashboardApplication.setCustomLong(context, Consts.deleteFilesDateLong, newDate.getTimeInMillis());
                    } else if (currentWipeSelection == 3) {
                        /*if (CustomDashboardApplication.isDeviceHasTelephonyService(context)) {
                            CustomDashboardApplication.setCustomLong(context, Consts.callLogDateLong, newDate.getTimeInMillis());
                        } else {*/
                        CustomDashboardApplication.setCustomLong(context, Consts.webCookiesDateLong, newDate.getTimeInMillis());
//                        }
                    } else if (currentWipeSelection == 4) {
                        CustomDashboardApplication.setCustomLong(context, Consts.webCookiesDateLong, newDate.getTimeInMillis());
                    }
                }

            }, hour, minute, false);
            timePickerDialog.setTitle(getResources().getString(R.string.select_time));
            timePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectCallLog(int pos) {
        if (isSelectAllCallLogsSelected) {
            ivCallLogsSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
            isSelectAllCallLogsSelected = false;
        }
        callLogsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectWebCookie(int position) {
        if (isSelectAllWebCookiesSelected) {
            ivWebCookieSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
            isSelectAllWebCookiesSelected = false;
        }
        webCookiesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeletePreFileFolder(int position, int isOpenFrom) {
        if (isOpenFrom == 0) {
            if (isSelectAllDeleteFileSelected) {
                ivDeleteFileSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                isSelectAllDeleteFileSelected = false;
            }
            preDeleteFileFolderListAdapter.notifyDataSetChanged();
        } else {
            deleteFileSelection = position;
            // getUpdatedDownloadFiles()
            openDeleteFileDetailsDialog();
        }
    }

    @Override
    public void onDeleteFileFolder(int position) {
        if (isSelectAllDeleteFileSelected) {
            ivDeleteFileSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
            isSelectAllDeleteFileSelected = false;
        }
        deleteFileFolderListAdapter.notifyDataSetChanged();
    }

    // OPEN DELETE FILE DETAILS DIALOG
    private void openDeleteFileDetailsDialog() {
        try {
            alertDeleteFileDetailsDialog = new AppCompatDialog(context);
            alertDeleteFileDetailsDialog.setContentView(R.layout.delete_folder_info_dialog);
            alertDeleteFileDetailsDialog.setCancelable(true);

            // TEXT VIEW
            TextView txOk = alertDeleteFileDetailsDialog.findViewById(R.id.tx_ok);

            // LINEAR LAYOUT
            llDeleteFileDetails = alertDeleteFileDetailsDialog.findViewById(R.id.ll_file_details);
            llDeleteFileDetails.removeAllViews();

            for (int i = 0; i < preDeleteFileFolderModels.get(deleteFileSelection).getFilePathList().size(); i++) {
                View dynamicView = LayoutInflater.from(context)
                        .inflate(R.layout.item_uninstall_apps, llDeleteFileDetails, false);

                // IMAGE VIEW
                ImageView ivSelectAll = dynamicView.findViewById(R.id.iv_select_selection);
                ImageView ivIcon = dynamicView.findViewById(R.id.iv_app_icon);

                // TEXT VIEW
                TextView txFileStr = dynamicView.findViewById(R.id.tx_app_name);

                ivSelectAll.setVisibility(View.GONE);

                txFileStr.setText(preDeleteFileFolderModels.get(deleteFileSelection).getFilePathList().get(i).getFileStr());

                File isDirectoryFile = new File(preDeleteFileFolderModels.get(deleteFileSelection).getFilePathList().get(i).getFileStr());

                if (isDirectoryFile.isDirectory()) {
                    ivIcon.setImageResource(R.drawable.ic_folder);
                } else {
                    ivIcon.setImageResource(R.drawable.ic_file);
                }

                llDeleteFileDetails.addView(dynamicView);
            }

            txOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDeleteFileDetailsDialog.dismiss();
                }
            });

            alertDeleteFileDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDeleteFileDetailsDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            alertDeleteFileDetailsDialog.getWindow().setGravity(Gravity.CENTER);
            alertDeleteFileDetailsDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // OPEN FILE MANAGER DIALOG
    private void openFileManager() {
        try {
            alertFileManagerDialog = new AppCompatDialog(context);
            alertFileManagerDialog.setContentView(R.layout.file_manager_selction_list);
            alertFileManagerDialog.setCancelable(true);

            // TEXT VIEW
            txCurrentPath = alertFileManagerDialog.findViewById(R.id.tx_current_path);
            TextView txCancel = alertFileManagerDialog.findViewById(R.id.tx_cancel_file_manager);
            TextView txSelect = alertFileManagerDialog.findViewById(R.id.tx_selected_item);

            // IMAGE VIEW
            ivUpFolder = alertFileManagerDialog.findViewById(R.id.iv_back);
            ivFileManagerSelectAll =
                    alertFileManagerDialog.findViewById(R.id.iv_select_all_selection);

            // RECYCLER VIEW
            recPreFileRecView =
                    alertFileManagerDialog.findViewById(R.id.rec_predefine_folder_view);
            recFileListRecView =
                    alertFileManagerDialog.findViewById(R.id.rec_file_view);

            recPreFileRecView.setHasFixedSize(true);
            recPreFileRecView.setLayoutManager(new GridLayoutManager(context, 3));
            recFileListRecView.setHasFixedSize(true);
            recFileListRecView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

            // ARRAY LIST
            predefineFoldersModels = new ArrayList();
            fileManagerFilesList = new ArrayList();

            predefineFoldersModels.add(
                    0,
                    new PredefineFoldersModel(
                            0,
                            R.drawable.ic_main_storage,
                            getResources().getString(R.string.main_storage)
                    )
            );
            predefineFoldersModels.add(
                    1,
                    new PredefineFoldersModel(
                            1,
                            R.drawable.ic_sd_card,
                            getResources().getString(R.string.sd_card)
                    )
            );
            predefineFoldersModels.add(
                    2,
                    new PredefineFoldersModel(
                            2,
                            R.drawable.ic_download_folder,
                            getResources().getString(R.string.downloads)
                    )
            );
            predefineFoldersModels.add(
                    3,
                    new PredefineFoldersModel(
                            3,
                            R.drawable.ic_document,
                            getResources().getString(R.string.documents)
                    )
            );
            predefineFoldersModels.add(
                    4,
                    new PredefineFoldersModel(4, R.drawable.ic_images, getResources().getString(R.string.images))
            );
            predefineFoldersModels.add(
                    5,
                    new PredefineFoldersModel(5, R.drawable.ic_music, getResources().getString(R.string.audio))
            );
            predefineFoldersModels.add(
                    6,
                    new PredefineFoldersModel(6, R.drawable.ic_videos, getResources().getString(R.string.video))
            );
            predefineFoldersModels.add(
                    7,
                    new PredefineFoldersModel(7, R.drawable.ic_camera, getResources().getString(R.string.dcim))
            );

            preDefineListAdapter = new PreDefineListAdapter(context, predefineFoldersModels, this);
            recPreFileRecView.setAdapter(preDefineListAdapter);

            refreshFileManagerList();
            setPath();

            ivUpFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upFolder();
                }
            });

            ivFileManagerSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectAllFileManagerSelected) {
                        ivFileManagerSelectAll.setImageResource(R.drawable.check_box_unselected_icon);
                        for (int i = 0; i < fileManagerFilesList.size(); i++) {
                            fileManagerFilesList.get(i).setSelected(false);
                        }
                        fileModelListAdapter.notifyDataSetChanged();
                        isSelectAllFileManagerSelected = false;
                    } else {
                        ivFileManagerSelectAll.setImageResource(R.drawable.check_box_selected_icon);
                        for (int i = 0; i < fileManagerFilesList.size(); i++) {
                            fileManagerFilesList.get(i).setSelected(true);
                        }
                        fileModelListAdapter.notifyDataSetChanged();
                        isSelectAllFileManagerSelected = true;
                    }
                }
            });

            txCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertFileManagerDialog.dismiss();
                }
            });

            txSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<FileModel> selectedPaths = new ArrayList<>();
                    for (int i = 0; i < fileManagerFilesList.size(); i++) {
                        if (fileManagerFilesList.get(i).isSelected()) {
                            selectedPaths.add(fileManagerFilesList.get(i));
                        }
                    }

                    selectedFilePaths = new ArrayList();

                    String ser1 = SerializeObject.ReadSettings(context, Consts.fileManagerListFile);
                    if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                        Object obj = SerializeObject.stringToObject(ser1);
                        // Then cast it to your object and
                        selectedFilePaths = (ArrayList<SelectedFileFolderModel>) obj;
                    }

                    File isFileExist = new File(getFilesDir().getAbsolutePath(), Consts.fileManagerListFile);
                    if (isFileExist.exists()) {
                        isFileExist.delete();
                    }

                    for (int i = 0; i < selectedPaths.size(); i++) {
                        if (selectedFilePaths.size() > 0) {
                            for (int j = 0; j < selectedFilePaths.size(); j++) {
                                if (!selectedPaths.get(i).getFilePath().equals(selectedFilePaths.get(j).getFileStr())) {
                                    selectedFilePaths.add(
                                            i,
                                            new SelectedFileFolderModel(
                                                    i,
                                                    selectedPaths.get(i).getFilePath(),
                                                    true,
                                                    selectedPaths.get(i).isFileDirectory()
                                            )
                                    );
                                }
                            }
                        } else {
                            selectedFilePaths.add(
                                    i,
                                    new SelectedFileFolderModel(
                                            i,
                                            selectedPaths.get(i).getFilePath(),
                                            true,
                                            selectedPaths.get(i).isFileDirectory()
                                    )
                            );
                        }
                    }

                    String ser2 = SerializeObject.objectToString(selectedFilePaths);
                    if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser2, Consts.fileManagerListFile);
                    } else {
                        SerializeObject.WriteSettings(context, "", Consts.fileManagerListFile);
                    }
                    getDeleteFiles();
                    alertFileManagerDialog.dismiss();
                }
            });

            alertFileManagerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertFileManagerDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            alertFileManagerDialog.getWindow().setGravity(Gravity.CENTER);
            alertFileManagerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // REFRESH FILE MANAGER LIST
    void refreshFileManagerList() {
        try {
            fileManagerFilesList.clear();
            fileManagerFilesList = new ArrayList();

            dir = new File(dirPath);
            File[] filelist = dir.listFiles();
            boolean isFileDirectory = false;
            if (filelist != null && filelist.length > 0) {
                for (int i = 0; i < filelist.length; i++) {
                    fileManagerFilesList.add(
                            i,
                            new FileModel(
                                    i,
                                    filelist[i].getName(),
                                    filelist[i].getAbsolutePath(),
                                    CustomDashboardApplication.getFileFolderModifiedDate(filelist[i].getAbsolutePath()),
                                    filelist[i].isDirectory(),
                                    false
                            )
                    );
                }
            }

            fileModelListAdapter = new FileModelListAdapter(context, fileManagerFilesList, this);
            recFileListRecView.setAdapter(fileModelListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreDefineButtonSelect(int pos) {
        String fileString = getExternalFilesDir(null).toString();
        Log.e("fileString", fileString);
        String[] splitString = fileString.split("Android");
        if (pos == 0) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                ///mounted
                dirPath = Environment.getRootDirectory().toString();
                refreshFileManagerList();
                setPath();
            }
        } else if (pos == 1) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                ///mounted
                dirPath = splitString[0];
                refreshFileManagerList();
                setPath();
            }
        } else if (pos == 2) {
            ///mounted
            dirPath = splitString[0] + "Download";
            refreshFileManagerList();
            setPath();
        } else if (pos == 3) {
            ///mounted
            dirPath = splitString[0] + "Documents";
            refreshFileManagerList();
            setPath();
        } else if (pos == 4) {
            ///mounted
            dirPath = splitString[0] + "Pictures";
            refreshFileManagerList();
            setPath();
        } else if (pos == 5) {
            ///mounted
            dirPath = splitString[0] + "Music";
            refreshFileManagerList();
            setPath();
        } else if (pos == 6) {
            ///mounted
            dirPath = splitString[0] + "Movies";
            refreshFileManagerList();
            setPath();
        } else if (pos == 7) {
            ///mounted
            dirPath = splitString[0] + "DCIM";
            refreshFileManagerList();
            setPath();
        }
    }

    void setPath() {
        txCurrentPath.setText(dirPath);
    }

    void upFolder() {
        if (!TextUtils.isEmpty(dirPath) && !dirPath.equalsIgnoreCase("/")) {
            String[] folders = dirPath.split("\\/");
            String[] folders2 = new String[folders.length - 1];
            dirPath = TextUtils.join("/", folders2);
        }

        if (TextUtils.isEmpty(dirPath)) {
            dirPath = "/";
        }

        refreshFileManagerList();
        setPath();
    }

    @Override
    public void onSelect(int pos) {
        try {
            parentDirPath = "$dirPath/..";
            dirPath = dirPath + "/" + fileManagerFilesList.get(pos).getFileName();
            File f = new File(dirPath);
            if (f.isDirectory()) {
                refreshFileManagerList();
                setPath();
            } else {
                Intent intent =
                        new Intent(Intent.ACTION_GET_CONTENT);
                intent.setAction(Intent.ACTION_VIEW);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                File file = new File(dirPath);

                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);
                intent.setDataAndType(Uri.fromFile(file), type);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void exitApp() {
        try {
            PackageManager pm = getApplicationContext().getPackageManager();
            ComponentName compName =
                    new ComponentName(getPackageName(), getPackageName() + ".Activity.FakeActivity");
            pm.setComponentEnabledSetting(compName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        WatchingAccessibilityService.hideReturnOverlay();

        SessionManager.getInstance(this).setIsParentMode(true);
        CustomDashboardApplication.setInt(context, Consts.is_from_dashboard, 0);
        CustomDashboardApplication.setInt(context, Consts.DISABLE_RECENTS_BUTTON, 0);
        CustomDashboardApplication.setInt(context, Consts.ENABLE_STATUS_BAR, 1);

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar beginCal = Calendar.getInstance();
        beginCal.setTimeInMillis(CustomDashboardApplication.startAppTime);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(System.currentTimeMillis());

        final List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        ArrayList<String> allPackageNameList = new ArrayList<>();
        ArrayList<String> finalBackgroundAppList = new ArrayList<>();

        for (int i = 0; i < queryUsageStats.size(); i++) {
            allPackageNameList.add(queryUsageStats.get(i).getPackageName());
        }

        if (allPackageNameList.size() > 0) {
            ArrayList<String> appModels = new ApkInfoExtractor(context).getAppsPackageNameList();
            for (int j = 0; j < allPackageNameList.size(); j++) {
                if (appModels.contains(allPackageNameList.get(j))) {
                    finalBackgroundAppList.add(allPackageNameList.get(j));
                }
            }
        }

        if (finalBackgroundAppList.size() > 0) {
            for (int k = 0; k < finalBackgroundAppList.size(); k++) {
                if (!finalBackgroundAppList.get(k).equals(getPackageName())) {
         /*           PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeActivity.class);
                    packageManager.setApplicationEnabledSetting(finalBackgroundAppList.get(k), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    packageManager.setApplicationEnabledSetting(finalBackgroundAppList.get(k),PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);*/
                }
            }
        }

        if (!(CustomDashboardApplication.isDeviceSamsung() && SuperLockState.getInstance(this).isESDKLicenseActivated())) {
            try {
                PackageManager packageManager = getPackageManager();
                packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

      /*  PackageManager packageManager = getPackageManager();
        packageManager.clearPackagePreferredActivities(Consts.PACKAGE_NAME);*/

        /*try {
            if (CustomDashboardApplication.getInt(context, Consts.IS_KNOX_SUPPORT) == 0 && CustomDashboardApplication.isDeviceSamsung()) {
                try {
                    //if (CustomDashboardApplication.isDeviceSamsung() && SuperLockState.getInstance(this).isESDKLicenseActivated())
                    try {
                        SuperLockState superLockState = SuperLockState.getInstance(CustomDashboardSettings.this);
                        if (superLockState != null)
                            if (!superLockState.isKioskModeEnabled(CustomDashboardSettings.this) && sessionManager.getIsDashboardLaunch()) {
                                superLockState.enableKioskMode(Consts.PACKAGE_NAME);
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

        try {
            if (sessionManager.getIsDashboardLaunch())
                resetPreferredLauncherAndOpenChooser(CustomDashboardSettings.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SessionManager.getInstance(context).setIsDashboardLaunch(false);

        try {
            if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                CustomDashboardApplication.exitSamsungKiosk(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isHomeApp = 1;

        Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("EXIT", true);
        try {
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSubscriptionAlert(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage("Your subscription is not active. Please enable subscription to use this feature.")
                .setCancelable(false)
                .setPositiveButton("Buy Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (alertDialogAdvanceSettings != null && alertDialogAdvanceSettings.isShowing()) {
                            alertDialogAdvanceSettings.dismiss();
                        }

                        startPurchase();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    private void uninstallDisabler() {
        adminDisable();
        //SuperLockState.getInstance().uninstallService(mContext);
        finish();
    }

    private PopupWindow showPopupMenu() {
        final PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = LayoutInflater.from(context).inflate(R.layout.item_setting_options, null);
        TextView tvAbout = view.findViewById(R.id.tv_about_us);
        TextView tvLoginRegister = view.findViewById(R.id.tv_login_register);

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            tvLoginRegister.setText(getResources().getString(R.string.log_out));
        } else {
            tvLoginRegister.setText(getResources().getString(R.string.login_register));
        }

        tvAbout.setOnClickListener(v -> {
            popupWindow.dismiss();
            showAboutUsDialog();
        });

        tvLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHomeApp = 1;
                popupWindow.dismiss();
                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) != 1) {
                    Intent intent = new Intent(context, LoginOrSignUpActivity.class);
                    intent.putExtra(Consts.is_redirect, true);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
                    dialogBuilder1.setMessage(getResources().getString(R.string.log_out_msg));
                    dialogBuilder1.setPositiveButton("Yes", (dialog, which) -> {
                        logoutUser();
                        CustomDashboardApplication.setInt(context, Consts.is_logged_in, 0);
                        d.dismiss();
                    });
                    dialogBuilder1.setNegativeButton("No", (dialog, which) -> d.dismiss());
                    d = dialogBuilder1.create();
                    d.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                            doneBtn.setAllCaps(false);
                        }
                    });
                    d.show();
                }
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setContentView(view);
        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

        return popupWindow;
    }

    // SHOW ENTER PASSWORD POP UP
    void showPasswordDialog(final int type) {
        try {
            isHomeApp = 1;
            llPwd.setVisibility(View.VISIBLE);
            isllPassword = 1;

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
                    isllPassword = 0;
                }
            }, 5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
//                    pointerCount = 0;
                    isllPassword = 0;
                    edPassword.setText(mainSb.toString());

                    if (edPassword.getText().toString().trim().length() > 0) {
                        String decryptStr = null;
                        try {
                            //decryptStr = AESCrypt.decrypt(context, CustomDashboardApplication.getString(context, Consts.PWD_ENCRY));
                            decryptStr = CustomDashboardApplication.getString(context, Consts.PWD_ENCRY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                            if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
//                                isVerify = 1;
                                edPassword.setText("");
                                llPwd.setVisibility(View.GONE);
//                                ivLogo.setVisibility(View.GONE);
//                                llLogo.setVisibility(View.GONE);
                               /* if (lstBook.size() == 0){
                                    showOpenSettingsDialog();
                                }*/
                            } else {
                                incorrectPinTapCount = incorrectPinTapCount + 1;
                                Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
//                                isVerify = 1;
                                edPassword.setText("");
                                llPwd.setVisibility(View.GONE);
//                                ivLogo.setVisibility(View.GONE);
//                                llLogo.setVisibility(View.GONE);
                               /* if (lstBook.size() == 0){
                                    showOpenSettingsDialog();
                                }*/
                            } else {
                                incorrectPinTapCount = incorrectPinTapCount + 1;
                                Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
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
            //ivContactUs.setVisibility(View.VISIBLE);
        } else {
            //ivContactUs.setVisibility(View.GONE);
        }

        edPassword.setText(mainSb.toString());
    }

    private void showApiErrorDialog() {
        try {
            errorDialog = new AppCompatDialog(context);
            errorDialog.setContentView(R.layout.item_api_error_dialog);

            Button btnOk = errorDialog.findViewById(R.id.btn_ok);
            Button btnCancel = errorDialog.findViewById(R.id.btn_cancel);

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorDialog.dismiss();
                    CustomDashboardApplication.contactSupport(context);
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorDialog.dismiss();
                }
            });

            errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            errorDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            errorDialog.getWindow().setGravity(Gravity.CENTER);
            errorDialog.getWindow().getDecorView().setLeft(20);
            errorDialog.getWindow().getDecorView().setRight(20);
            errorDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAboutUsDialog() {
        try {
            final AppCompatDialog aboutUsDialog = new AppCompatDialog(context);
            aboutUsDialog.setContentView(R.layout.item_about_us);

            TextView tvAboutUs = aboutUsDialog.findViewById(R.id.tv_message);
            Button btnOk = aboutUsDialog.findViewById(R.id.btn_ok);

            SpannableString ss = new SpannableString("Kids Dashboard is a powerful application with cloud support, to stop E-addiction. To know more, visit: https://www.kidsdashboard.co.uk");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View textView) {
                    aboutUsDialog.dismiss();
                    startActivity(new Intent(CustomDashboardSettings.this, WebViewActivity.class));
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                    ds.linkColor = ContextCompat.getColor(context, R.color.white);
                }
            };

            ss.setSpan(clickableSpan, 102, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (tvAboutUs != null) {
                tvAboutUs.setText(ss);
                tvAboutUs.setMovementMethod(LinkMovementMethod.getInstance());
            }

            btnOk.setOnClickListener(v -> aboutUsDialog.dismiss());

            aboutUsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            aboutUsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aboutUsDialog.getWindow().setGravity(Gravity.CENTER);
            aboutUsDialog.getWindow().getDecorView().setLeft(20);
            aboutUsDialog.getWindow().getDecorView().setRight(20);
            aboutUsDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRateAppFallbackDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Rate us")
                .setMessage("Liked us? Please rate us on play store")
                .setPositiveButton("Okay", (dialog, which) -> {

                })
                .setNegativeButton("Not now", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> {

                })
                .show();
    }

    private void showDeleteWebLinkAlert(int pos) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Web Shortcut")
                .setMessage("Are you sure you want to delete web shortcut ?")
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                        deleteWebShortCuts(String.valueOf(addedWebShortCutsModels.get(pos).getId()));
                    }

                    addedWebShortCutsModels.remove(pos);

                    String dir = context.getFilesDir().getAbsolutePath();
                    File f0 = new File(dir, Consts.addedWebShortcutsFile);
                    if (f0.exists()) {
                        f0.delete();
                    }

                    String ser = SerializeObject.objectToString(addedWebShortCutsModels);
                    if (ser != null && !ser.equalsIgnoreCase("")) {
                        SerializeObject.WriteSettings(context, ser, Consts.addedWebShortcutsFile);
                    }

                    addedWebSiteShortcutAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                })
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    String portNumber = "";

    private void showScreenShareDialog() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enter Room Id");
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setPadding(8, 0, 8, 8);
        editText.setTextColor(ContextCompat.getColor(context, R.color.black));
        editText.setEnabled(false);
        builder.setView(editText);

        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
            if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                portNumber = editText.getText().toString().trim();
                StartStopScreenShare.portNumber = portNumber;
                startScreenCapture();
            } else {
                Toast.makeText(context, "Please enter port number to start screen share.", Toast.LENGTH_SHORT).show();
            }
            dialogInterface.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setCancelable(false);

        /*if (SignallingClient.getInstance().roomName.equals("12345")) {
            String str_random_room_id = "" + ((int) (Math.random() * 9000) + 1000);
            editText.setText(str_random_room_id);
            SignallingClient.getInstance().roomName = str_random_room_id;
        } else {
            editText.setText(SignallingClient.getInstance().roomName);
        }*/

        editText.setText(CustomDashboardApplication.getSerialNumber());
        SignallingClient.getInstance().roomName = CustomDashboardApplication.getSerialNumber();

        builder.show();
    }

    private void showStopScreenShareDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Are You Sure?");
        builder.setMessage("Screen share is running. Your current room id is " + SignallingClient.roomName + "\n" + "Are you sure you want to stop screen share?");
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
            new Thread(() -> {
                try {
                    stopService(new Intent(context, MediaProjectionService.class));
                    if (SignallingClient.getInstance() != null) {
                        SignallingClient.getInstance().close();
                    }

                    StartStopScreenShare.hangup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            dialogInterface.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setCancelable(false);
        builder.show();
    }

    public void startScreenCapture() {
        // Consent first. MediaProjectionService is started only after RESULT_OK.
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        mediaProjectionResultLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    ActivityResultLauncher<Intent> mediaProjectionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // There are no request codes
                    Log.e(TAG, "Screen Share Started.");
                    StartStopScreenShare.portNumber = portNumber;
                    Intent intent = new Intent(getApplicationContext(), MediaProjectionService.class);
                    intent.putExtra(MediaProjectionService.EXTRA_RESULT_CODE, result.getResultCode());
                    intent.putExtra(MediaProjectionService.EXTRA_RESULT_DATA, result.getData());
                    ContextCompat.startForegroundService(getApplicationContext(), intent);
                } else {
                    StartStopScreenShare.clearConsent();
                }
            });
}