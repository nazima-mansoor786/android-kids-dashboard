package com.tabnova.aidashboard.Activity;

import android.os.Bundle;

import com.tabnova.aidashboard.R;

public class SelectTimer extends BaseActivity {

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
        setContentView(R.layout.activity_select_timer);
    }
}
