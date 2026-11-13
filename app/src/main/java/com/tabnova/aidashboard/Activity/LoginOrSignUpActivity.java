package com.tabnova.aidashboard.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.R;

public class LoginOrSignUpActivity extends AppCompatActivity {

    //CONTEXT
    Context context;

    //BUTTON
    Button btnLogin;
    Button btnRegister;

    //CHECK BOX
    CheckBox cbTermsCondition;

    //TEXT VIEW
    TextView tvSkipLogin;

    //BOOLEAN
    private boolean isRedirect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_sign_up);
        // Edge-to-edge: purple root, default (white) bar icons.
        Utils.applyEdgeToEdge(this, null, false, false, false, false);

        //INIT CONTEXT
        context = this;

        //INIT BUTTONS
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        //INIT CHECKBOX
        cbTermsCondition = findViewById(R.id.cb_terms_condition);

        //INIT TEXT VIEW
        tvSkipLogin = findViewById(R.id.tv_skip_login);

        cbTermsCondition.setChecked(true);

        if (getIntent() != null && getIntent().getExtras() != null) {
            isRedirect = getIntent().getExtras().getBoolean(Consts.is_redirect);

            if (getIntent().getExtras().getBoolean(Consts.exit_app)) {
                finish();
                return;
            }
        }

        btnLogin.setOnClickListener(v -> {
            if (cbTermsCondition.isChecked()) {
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            } else {
                Toast.makeText(context, R.string.please_accept_terms_conditions, Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            if (cbTermsCondition.isChecked()) {
                Intent intent = new Intent(context, Register.class);
                startActivity(intent);
            } else {
                Toast.makeText(context, R.string.please_accept_terms_conditions, Toast.LENGTH_SHORT).show();
            }
        });

        tvSkipLogin.setOnClickListener(v -> {
            if (cbTermsCondition.isChecked()) {
                if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                        !CustomDashboardApplication.isDeviceSamsungActivated()) {
                    //Toast.makeText(context, "Condition 1", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, ActivateAdminActivity.class);
                    startActivity(intent);
                    finish();
                } else if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                    //Toast.makeText(context, "Condition 2", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, AppPermissionsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.app_permission_done));
                    CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);
                    //Toast.makeText(context, "Condition 3 @104", Toast.LENGTH_SHORT).show();
                    Log.e(LoginOrSignUpActivity.class.getSimpleName(), "onClick: Condition 3 @104");
                    CustomDashboardApplication.redirectOnActivity(context, true);
                }
            } else {
                Toast.makeText(context, R.string.please_accept_terms_conditions, Toast.LENGTH_SHORT).show();
            }
        });

        if (!isRedirect && CustomDashboardApplication.getInt(context, Consts.IS_FIRST_INSTALLED) == 1) {
            if (cbTermsCondition.isChecked()) {
                if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                        !CustomDashboardApplication.isDeviceSamsungActivated()) {
                    Intent intent = new Intent(context, ActivateAdminActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (!CustomDashboardApplication.isDeviceSamsungActivated() && !CustomDashboardApplication.checkAllPermissions(context)) {
                    Intent intent = new Intent(context, AppPermissionsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.app_permission_done));
                    CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);

                    //Toast.makeText(context, "Condition 3 @135", Toast.LENGTH_SHORT).show();
                    Log.e(LoginOrSignUpActivity.class.getSimpleName(), "Condition 3 @135");
                    CustomDashboardApplication.redirectOnActivity(context, true);
                }
            } else {
                Toast.makeText(context, R.string.please_accept_terms_conditions, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                try {
                    Intent intent;
                    if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                            !CustomDashboardApplication.isDeviceSamsungActivated()) {
                        intent = new Intent(context, ActivateAdminActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        CustomDashboardApplication.redirectOnActivity(this, true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.e(LoginOrSignUpActivity.class.getSimpleName(), "onUserLeaveHint: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}