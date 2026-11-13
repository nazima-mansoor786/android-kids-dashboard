package com.tabnova.aidashboard.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.activity.OnBackPressedCallback;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.R;

public class Labels extends BaseActivity {

    // EDIT TEXT
    private EditText edLabel;

    // RADIO GROUP
    private RadioGroup rgDateTimeSettings;

    // BUTTON
    private Button btnSaveSettings;

    // CHECK BOX
    private CheckBox cbBatteryStatus;

    //IMAGE VIEW
    private ImageView ivBack;

    // CONTEXT
    private Context context;

    // API MANAGER
    APIManager apiManager;

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
        setContentView(R.layout.activity_labels);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back closes Labels (previously super.onBackPressed() did nothing).
                finish();
            }
        });

        context = this;

        apiManager = new APIManager(context);

        edLabel = findViewById(R.id.ed_label);
        rgDateTimeSettings = findViewById(R.id.rg_setting);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        cbBatteryStatus = findViewById(R.id.battery_switch);
        ivBack = findViewById(R.id.iv_back);

        int selectedSetting = CustomDashboardApplication.getClockSetting(context, Consts.SELECTED_TIME_SETTING);

        if (selectedSetting == 0) {
            rgDateTimeSettings.check(R.id.rb_none);
            CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 0);
        } else if (selectedSetting == 1) {
            rgDateTimeSettings.check(R.id.rb_time);
            CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
        } else if (selectedSetting == 2) {
            rgDateTimeSettings.check(R.id.rb_date);
            CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
        } else if (selectedSetting == 3) {
            rgDateTimeSettings.check(R.id.rb_date_time);
            CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
        }

        rgDateTimeSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_none) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 0);
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 0);
                } else if (checkedId == R.id.rb_time) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 1);
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
                } else if (checkedId == R.id.rb_date) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 2);
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
                } else if (checkedId == R.id.rb_date_time) {
                    CustomDashboardApplication.setClockSetting(context, Consts.SELECTED_TIME_SETTING, 3);
                    CustomDashboardApplication.setClockEnabled(context, Consts.CLOCK_WIDGET, 1);
                }
            }
        });

        ivBack.setOnClickListener(view -> finish());

        if (!CustomDashboardApplication.getString(context, Consts.APP_INFO_STR).equalsIgnoreCase("")) {
            edLabel.setText(CustomDashboardApplication.getString(context, Consts.APP_INFO_STR));
        }

        if (CustomDashboardApplication.getBatteryStatus(context, Consts.BATTERY_STATUS) == 0) {
            cbBatteryStatus.setChecked(false);
        } else {
            cbBatteryStatus.setChecked(true);
        }

        cbBatteryStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                CustomDashboardApplication.setBatteryStatus(context, Consts.BATTERY_STATUS, 1);
            } else {
                CustomDashboardApplication.setBatteryStatus(context, Consts.BATTERY_STATUS, 0);
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edLabel.getText().toString().trim().length() > 0) {
                    CustomDashboardApplication.setInt(context, Consts.APP_INFO, 1);
                    CustomDashboardApplication.setString(context, Consts.APP_INFO_STR, edLabel.getText().toString());
                } else {
                    CustomDashboardApplication.setInt(context, Consts.APP_INFO, 0);
                    CustomDashboardApplication.setString(context, Consts.APP_INFO_STR, "");
                }

                CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);
                apiManager.updateProfileSettings();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
        }
    }
}