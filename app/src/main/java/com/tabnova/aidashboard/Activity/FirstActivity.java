package com.tabnova.aidashboard.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.R;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            try {
                if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                    CustomDashboardApplication.exitSamsungKiosk(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finishAndRemoveTask();
            System.exit(0);
        }
    }
}