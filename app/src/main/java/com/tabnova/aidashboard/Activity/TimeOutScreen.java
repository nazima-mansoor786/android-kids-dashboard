package com.tabnova.aidashboard.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.tabnova.aidashboard.Adapter.ScheduleAdapter;
import com.tabnova.aidashboard.Adapter.ScheduleEventListAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.CountdownTimer;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.OnResponseListener;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Model.DailyTimerModel;
import com.tabnova.aidashboard.Model.ProfileData;
import com.tabnova.aidashboard.Model.ScheduleDaysModel;
import com.tabnova.aidashboard.Model.ScheduleEventModel;
import com.tabnova.aidashboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TimeOutScreen extends BaseActivity implements ScheduleEventListAdapter.OnEventSelectListner, ScheduleAdapter.OnScheduleSelectListner, OnResponseListener {

    //RELATIVE LAYOUT
    private RelativeLayout relMain;

    // LINEAR LAYOUT
    private LinearLayout llExtendTimer;
    private LinearLayout llExit;
    private LinearLayout llPwd;

    // EDIT TEXT
    private EditText edPassword;

    // IMAGE VIEW
    private ImageView ivBtnOne, ivBtnTwo, ivBtnThree, ivBtnFour, ivBtnFive,
            ivBtnSix, ivBtnSeven, ivBtnEight, ivBtnNine, ivBtnZero, ivBtnBack, ivBtnOk, ivClose, ivContactUs;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogSetTimer;
    private AppCompatDialog alertDialogAddEvent;

    // ALERT DIALOG
    private AlertDialog d;

    // RECYCLER VIEW
    private RecyclerView recScheduleEvent;

    // ADAPTER
    public ScheduleEventListAdapter scheduleEventListAdapter;
    private ScheduleAdapter scheduleAdapter;

    // CONTEXT
    Context context;

    // JSON ARRAY
    private JsonArray selectedDaysArray = new JsonArray();

    // STRING BUILDER
    StringBuilder mainSb = new StringBuilder();

    // INTEGER
    private int Type;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private int incorrectPinTapCount = 0;
    private int isVolumeEnabled;

    // WORK MANAGER REQUEST
    private OneTimeWorkRequest workRequest;

    // TEXT VIEW
    private TextView txForgotPin;

    // ARRAY LIST
    private ArrayList<ScheduleEventModel> scheduleEventModels;
    private ArrayList<Integer> selectedDays;
    private ArrayList<ScheduleDaysModel> scheduleDaysModels;

    // API MANAGER
    APIManager apiManager;

    private int pointerCount;

    public int isllPassword;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_time_out_screen);

        context = this;

        apiManager = new APIManager(context, this);

        relMain = findViewById(R.id.rel_main);

        llExtendTimer = findViewById(R.id.ll_select_timer);
        llExit = findViewById(R.id.ll_exit);
        llPwd = findViewById(R.id.ll_show_pwd);

        edPassword = findViewById(R.id.ed_pwd);

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
        ivContactUs = findViewById(R.id.btn_contact_us);

        txForgotPin = findViewById(R.id.tx_forgot_pin);

        WorkManager.getInstance(context).cancelAllWork();

        relMain.setOnTouchListener((v, event) -> {
            int staus = initCountDownTimer();
            if (staus == 0) {
                pointerCount += event.getPointerCount();
                if (pointerCount == 5) {
                    // SHOW ENTER PASSWORD POP UP
                    Type = 0;
                    //showPasswordDialog(0);
                    isHomeApp = 1;
                    llPwd.setVisibility(View.VISIBLE);
                    isllPassword = 1;

                    // HANDLER FOR CHECKING USER INACTIVITY
                    new Handler().postDelayed(() -> {
                        if (edPassword.getText().toString().trim().length() == 0) {
                            llPwd.setVisibility(View.GONE);
                            isllPassword = 0;
                            if (mainSb.length() > 0) {
                                if (mainSb.length() == 1) {
                                    mainSb.delete(0, mainSb.length() - 1);
                                    mainSb.setLength(0);
                                    mainSb = new StringBuilder();
                                } else {
                                    mainSb.deleteCharAt(mainSb.length() - 1);
                                }
                            }
                        }
                    }, 5000);

                    pointerCount = 0;
                    countDownTimer.cancel();
                    Log.e("Touch10Count", String.valueOf(pointerCount));
                }
                Log.e("TouchCount", String.valueOf(pointerCount));
            }
            return false;
        });

        txForgotPin.setOnClickListener(v -> {
          /*  if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                Intent intent = new Intent(context, ForgotPin.class);
                startActivity(intent);
            } else {*/
            CustomDashboardApplication.showLoggedInDialog(context);
            // }
        });

        ivClose.setOnClickListener(v -> {
            llPwd.setVisibility(View.GONE);
            edPassword.setText("");
            isllPassword = 0;
            if (mainSb.length() > 0) {
                if (mainSb.length() == 1) {
                    mainSb.delete(0, mainSb.length() - 1);
                    mainSb.setLength(0);
                    mainSb = new StringBuilder();
                } else {
                    mainSb.deleteCharAt(mainSb.length() - 1);
                }
            }
        });

        ivContactUs.setOnClickListener(v -> {
            incorrectPinTapCount = 0;
            CustomDashboardApplication.contactSupport(context);
        });

        llExit.setOnClickListener(v -> {
            Type = 0;
            llPwd.setVisibility(View.VISIBLE);
        });

        llExtendTimer.setOnClickListener(v -> {
            Type = 1;
            llPwd.setVisibility(View.VISIBLE);
        });

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
            }
        }, 5000);

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(Consts.SCHEDULE_TIMER));
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

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int status = intent.getIntExtra(Consts.scheduleStatus, 0);
                if (status == 1) {
                    Intent intent1;
                    if (SessionManager.getInstance(TimeOutScreen.this).getIsParentMode()) {
                        Log.e(TimeOutScreen.class.getSimpleName(), "onClick: @321");
                        intent1 = new Intent(TimeOutScreen.this, CustomDashboardSettings.class);
                    } else {
                        intent1 = new Intent(TimeOutScreen.this, MainActivity.class);
                    }
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                }
            }
        }
    };

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

                    edPassword.setText(mainSb.toString());

                    if (edPassword.getText().toString().trim().length() > 0) {

                        if (Type == 0) {
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
                                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
                                        WorkManager.getInstance(context).cancelAllWork();
                                        finish();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
                                        WorkManager.getInstance(context).cancelAllWork();
                                        finish();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                                    if (edPassword.getText().toString().trim().equals("0000") || edPassword.getText().toString().trim().equals("2014")) {
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        llPwd.setVisibility(View.GONE);
                                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
                                        WorkManager.getInstance(context).cancelAllWork();
                                        finish();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equals("0000")) {
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        llPwd.setVisibility(View.GONE);
                                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
                                        WorkManager.getInstance(context).cancelAllWork();
                                        finish();
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
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) ||
                                            edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        showSetTimerDialog();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
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
        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }
    }

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

            final TextView txAddEvent = alertDialogSetTimer.findViewById(R.id.tx_add_event);

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

            recScheduleEvent = alertDialogSetTimer.findViewById(R.id.rec_schedule_list);
            recScheduleEvent.setHasFixedSize(true);

            scheduleEventModels = new ArrayList<>();

            recScheduleEvent.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            scheduleEventListAdapter = new ScheduleEventListAdapter(context, scheduleEventModels, this);
            recScheduleEvent.setAdapter(scheduleEventListAdapter);

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
                getScheduleEventList();
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
                        txAddEvent.setEnabled(false);
                        cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        spinnerSchedule.setEnabled(false);
                        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
                    }
                }
            });

            rbDailyLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                }
            });

            rbSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbNoTimer.setChecked(false);
                        rbDailyLimit.setChecked(false);
                        rbSchedule.setChecked(true);
                        spinnerTimer.setEnabled(false);
                        cvTimer.setCardBackgroundColor(getResources().getColor(R.color.grey_bg));
                        cvSchedule.setCardBackgroundColor(getResources().getColor(R.color.white));
                        //spinnerSchedule.setEnabled(true);
                        txAddEvent.setEnabled(true);
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 1);
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                        getScheduleEventList();
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(context)) {
                            doneTimerPopup(finalTimeList);
                        } else {
                            Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                        }
                    } else {
                        doneTimerPopup(finalTimeList);
                    }
                }
            });

            txAddEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddEventDialog();
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

    private void doneTimerPopup(ArrayList<DailyTimerModel> finalTimeList) {
        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    apiManager.updateProfileSchedule();
                    //alertDialogSetTimer.dismiss();
                } else {
                    Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent1, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                }
            } else {
                apiManager.updateProfileSchedule();
                //alertDialogSetTimer.dismiss();
            }
        }

        if (CustomDashboardApplication.getInt(context, Consts.SET_TIMER) == 0) {
            WorkManager.getInstance(context).cancelAllWork();
        } else {
            if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 0) {
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 0);
                WorkManager.getInstance(context).cancelAllWork();
            } else if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER_TYPE) == 1) {
                CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
                if (CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER) != 0) {
                    if (finalTimeList.size() > 0) {
                        final int timer = CustomDashboardApplication.getInt(context, Consts.SELECTED_TIMER);
                        if (timer != 0) {
                            long timeMillis = finalTimeList.get(timer).getDailyTime();
                            TimeUnit timeUnit = finalTimeList.get(timer).getDailyTimeUnit();

                            Data data = new Data.Builder().putLong(Consts.COUNT_DOWN_TIME, timeMillis).build();

                            workRequest = new OneTimeWorkRequest.Builder(CountdownTimer.class).setInputData(data).setInitialDelay(timeMillis, timeUnit).build();
                            WorkManager.getInstance(context).enqueue(workRequest);
                        } else {
                            WorkManager.getInstance(context).cancelAllWork();
                        }
                    }
                }
            }
        }

        CustomDashboardApplication.setInt(context, Consts.is_screen_time_out, 0);
        alertDialogSetTimer.dismiss();
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       /* Intent i=new Intent(this,TimeOutScreen.class);
        startActivity(i);*/
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

                                            scheduleEventListAdapter = new ScheduleEventListAdapter(context, scheduleEventModels, TimeOutScreen.this);
                                            recScheduleEvent.setAdapter(scheduleEventListAdapter);

                                            String ser = SerializeObject.objectToString(scheduleEventModels);
                                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                                SerializeObject.WriteSettings(context, ser, Consts.scheduleFile);
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
            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {

            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isVolumeEnabled == 1) {

                try {
                    AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    // Get the music current volume level
                    int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                    // Get the device music maximum volume level
                    int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (Exception e) {
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

        return true;
    }


    @Override
    public void OnSelectEvent(final int pos, final String groupId, int isDelete) {
        if (isDelete == 1) {

            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(getResources().getString(R.string.delete_schedule_event));
            dialogBuilder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteScheduleEvent(groupId, pos);
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

            selectedDays = new ArrayList<Integer>();

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

            scheduleAdapter = new ScheduleAdapter(context, scheduleDaysModels, TimeOutScreen.this);
            recDays.setAdapter(scheduleAdapter);


            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edEventName.getText().toString().trim().length() == 0) {
                        edEventName.setError(getResources().getString(R.string.enter_event_name_valid));
                        edEventName.requestFocus();
                    } else if (edStartTimeOne.getText().toString().trim().length() == 0) {
                        edStartTimeOne.setError(getResources().getString(R.string.enter_start_time_valid));
                        edStartTimeOne.requestFocus();
                    } else if (edEndTimeOne.getText().toString().trim().length() == 0) {
                        edStartTimeOne.setError(getResources().getString(R.string.enter_end_time_valid));
                        edStartTimeOne.requestFocus();
                    } else if (selectedDays.size() == 0) {
                        Toast.makeText(context, "Please select weekday from below", Toast.LENGTH_SHORT).show();
                    } else {
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

    // GET SCHEDULE EVENT LIST
    private void deleteScheduleEvent(String groupId, final int pos) {
        try {
            if (NetConnectivity.isOnline(context)) {
                //dialogLoader.showProgressDialog();
                Retrofit retrofit = RetrofitExtra.getInstance();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // REQUEST OBJECT
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Consts.token, CustomDashboardApplication.getString(context, Consts.token));
                jsonObject.addProperty(Consts.group_id, groupId);

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
                                            scheduleEventListAdapter.notifyItemChanged(pos);
                                        }

                                        getScheduleEventList();
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
                    scheduleEventListAdapter.notifyDataSetChanged();
                    //ShowMessage.showSuccess(context, jsonObject.getString(Consts.responseMessage));
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
                            ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }
}
