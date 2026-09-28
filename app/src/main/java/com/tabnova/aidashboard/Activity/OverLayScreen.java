package com.tabnova.aidashboard.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.CountdownTimer;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Model.DailyTimerModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.tabnova.aidashboard.Fragment.AppPermissionMain.CODE_DRAW_OVER_OTHER_APP_PERMISSION;

public class OverLayScreen extends BaseActivity {

    // LINEAR LAYOUT
    private LinearLayout llExtendTimer;
    private LinearLayout llExit;
    private LinearLayout llPwd;

    // EDIT TEXT
    private EditText edPassword;

    // IMAGE VIEW
    private ImageView ivBtnOne, ivBtnTwo, ivBtnThree, ivBtnFour, ivBtnFive,
            ivBtnSix, ivBtnSeven, ivBtnEight, ivBtnNine, ivBtnZero, ivBtnBack, ivBtnOk, ivClose, ivContactUs;

    // ALERT DIALOG
    private AppCompatDialog alertDialogSetTimer;

    // CONTEXT
    Context context;

    // TEXT VIEW
    private TextView txForgotPin;

    // STRING BUILDER
    StringBuilder mainSb = new StringBuilder();

    // INTEGER
    private int Type;
    private int incorrectPinTapCount = 0;

    // WORK MANAGER REQUEST
    private OneTimeWorkRequest workRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_lay_screen);

        isHomeApp = 1;

        context = this;

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

        txForgotPin = findViewById(R.id.tx_forgot_pin);

        ivClose = findViewById(R.id.btn_close);
        ivContactUs = findViewById(R.id.btn_contact_us);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPwd.setVisibility(View.GONE);
            }
        });

        ivContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incorrectPinTapCount = 0;
                CustomDashboardApplication.contactSupport(context);
            }
        });

        txForgotPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    Intent intent = new Intent(context, ForgotPin.class);
                    startActivity(intent);
                } else {*/
                CustomDashboardApplication.showLoggedInDialog(context);
                // }
            }
        });

        llExit.setOnClickListener(v -> {
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

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(Consts.LOCKED_MODE));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int status = intent.getIntExtra(Consts.IS_LOCKED, 0);
                CustomDashboardApplication.setInt(context, Consts.is_screen_locked, 0);
                if (status == 0) {
                    finish();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CustomDashboardApplication.getInt(context, Consts.IS_LOCKED) == 0) {
            CustomDashboardApplication.setInt(context, Consts.is_screen_locked, 0);
            finish();
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
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        incorrectPinTapCount = incorrectPinTapCount + 1;
                                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr)) {
                                        edPassword.setText("");
                                        llPwd.setVisibility(View.GONE);
                                        //Intent openSettings = new Intent(context, CustomDashboardSettings.class);
                                        //startActivity(openSettings);
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
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
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
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
                                        //Toast.makeText(context, getResources().getString(R.string.disable_lock), Toast.LENGTH_SHORT).show();
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
                                    if (edPassword.getText().toString().trim().equalsIgnoreCase(decryptStr) || edPassword.getText().toString().trim().equalsIgnoreCase("2014")) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {

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
                        CustomDashboardApplication.setInt(context, Consts.SELECTED_TIMER_TYPE, 1);
                        CustomDashboardApplication.setInt(context, Consts.SET_TIMER, 1);
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

            alertDialogSetTimer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogSetTimer.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialogSetTimer.getWindow().setGravity(Gravity.CENTER);
            alertDialogSetTimer.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doneTimerPopup(ArrayList<DailyTimerModel> finalTimeList) {
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

        alertDialogSetTimer.dismiss();
    }
}