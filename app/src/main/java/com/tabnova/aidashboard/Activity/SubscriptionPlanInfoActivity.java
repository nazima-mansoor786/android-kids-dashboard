package com.tabnova.aidashboard.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.BillingManager;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.R;

public class SubscriptionPlanInfoActivity extends AppCompatActivity {

    private static final String TAG = "SubscriptionPlanInfo";

    //CONTEXT
    private Context context;

    //BUTTON
    private TextView btnSubscribe;
    private Button btnContinue;

    //TEXT VIEW
    private TextView tvPremiumPlan;
    private TextView tvCancellationPolicy;
    private TextView tvTrial;
    private TextView tvBuy;
    private TextView tvPlansTitle;

    //IMAGE VIEW
    private ImageView ivBack;

    //LINEAR LAYOUT
    private LinearLayout llSubscriptionDialog;
    private LinearLayout llPlanInfo;

    //STRING
    private String serialNumber = "";

    //SESSION MANAGER
    private SessionManager sessionManager;

    //BOOLEAN
    private boolean isFromSettings = false;
    private boolean isScreenShare = false;
    private boolean isLauncher = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_plan_info);

        //INIT CONTEXT
        context = this;

        //INIT SESSION MANAGER
        sessionManager = SessionManager.getInstance(context);

        getDataFromIntent();
        initViews();
        initClickListener();

        if (CustomDashboardApplication.getInt(context, Consts.IS_FIRST_INSTALLED) == 1) {
            if (!isFromSettings && !isScreenShare) {
                if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SubscriptionPlanInfoActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
                } else {
                    Intent intent;
                    if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                            !CustomDashboardApplication.isDeviceSamsungActivated()) {
                        intent = new Intent(context, ActivateAdminActivity.class);
                    } else {
                        intent = new Intent(context, MainActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            } else {
                if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SubscriptionPlanInfoActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
                }
                checkEntitlementAndPrice();
            }
        } else {
            if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SubscriptionPlanInfoActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
            }
            checkEntitlementAndPrice();
        }

        tvPlansTitle.setVisibility(View.VISIBLE);

        if (isFromSettings) {
            ivBack.setVisibility(View.VISIBLE);
        } else {
            ivBack.setVisibility(View.GONE);
        }

        tvPremiumPlan.setPaintFlags(tvPremiumPlan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        if (isScreenShare) {
            ivBack.setVisibility(View.VISIBLE);
            tvPlansTitle.setVisibility(View.INVISIBLE);
            llPlanInfo.setVisibility(View.GONE);
            llSubscriptionDialog.setVisibility(View.VISIBLE);

            if (sessionManager.getFreeTrialScreenShareCount() >= 5) {
                tvTrial.setVisibility(View.GONE);
            } else {
                tvTrial.setVisibility(View.VISIBLE);
            }
        } else {
            llPlanInfo.setVisibility(View.VISIBLE);
            llSubscriptionDialog.setVisibility(View.GONE);
        }
    }

    private void getDataFromIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            isFromSettings = getIntent().getExtras().getBoolean(Consts.from_settings);
            isScreenShare = getIntent().getExtras().getBoolean(Consts.is_screen_share);
            isLauncher = getIntent().getExtras().getBoolean(Consts.is_launcher);

            if (getIntent().getExtras().getString(Consts.serial_number) != null) {
                serialNumber = getIntent().getExtras().getString(Consts.serial_number);
            }
        }
    }

    private void initViews() {
        //INIT BUTTONS
        btnSubscribe = findViewById(R.id.btn_subscribe);
        btnContinue = findViewById(R.id.btn_continue);

        //INIT TEXT VIEW
        tvPremiumPlan = findViewById(R.id.tv_premium_plan);
        tvCancellationPolicy = findViewById(R.id.tv_cancellation_policy);
        tvTrial = findViewById(R.id.tx_trial);
        tvBuy = findViewById(R.id.tx_buy);
        tvPlansTitle = findViewById(R.id.tv_plans_title);

        //INIT LINEAR LAYOUT
        llPlanInfo = findViewById(R.id.ll_plan_info);
        llSubscriptionDialog = findViewById(R.id.ll_subscription_dialog);

        //INIT IMAGE VIEW
        ivBack = findViewById(R.id.iv_back);

        tvCancellationPolicy.setPaintFlags(tvCancellationPolicy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void initClickListener() {
        tvCancellationPolicy.setOnClickListener(view -> {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra(Consts.web_url, Consts.cancellationPolicyUrl);
            intent.putExtra(Consts.name, getString(R.string.cancellation_policy));
            startActivity(intent);
        });

        btnSubscribe.setOnClickListener(view -> {
            if (sessionManager.getIsSubscriptionEnabled()) {
                if (btnSubscribe.getText().toString().trim().equalsIgnoreCase(getString(R.string.continue_str))) {
                    if (isLauncher && !isScreenShare) {
                        CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);
                        Intent intent = new Intent(context, LoginOrSignUpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        finish();
                    }
                } else {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
                    if (launchIntent != null) {
                        try {
                            startActivity(launchIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            tvCancellationPolicy.performClick();
                        }
                    } else {
                        tvCancellationPolicy.performClick();
                    }
                }
            } else {
                startPurchase();
            }
        });

        btnContinue.setOnClickListener(view -> {
            if (!isFromSettings) {
                Intent intent = new Intent(context, LoginOrSignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }
        });

        ivBack.setOnClickListener(view -> finish());

        tvTrial.setOnClickListener(view -> {
            int count = sessionManager.getFreeTrialScreenShareCount();
            count++;
            sessionManager.setFreeTrialScreenShareCount(count);
            Intent intent = new Intent(getApplicationContext(), ScreenShareActivity.class);
            intent.putExtra(Consts.serial_number, serialNumber);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        tvBuy.setOnClickListener(view -> startPurchase());
    }

    private final BillingManager.PurchaseListener purchaseListener = new BillingManager.PurchaseListener() {
        @Override
        public void onPurchaseSuccess() {
            Toast.makeText(context, "Product purchased. You can use all premium features now.", Toast.LENGTH_SHORT).show();
            onSubscriptionActive();
        }

        @Override
        public void onPurchasePending() {
            Toast.makeText(context, "Payment is pending. Premium will activate once payment is complete.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPurchaseCancelled() {
            // Nothing to do; entitlement unchanged.
        }

        @Override
        public void onPurchaseError(int responseCode, @Nullable String message) {
            Toast.makeText(context, "Purchase could not be completed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    };

    /** Launch path: refresh entitlement and price; redirect only if the subscription is active. */
    private void checkEntitlementAndPrice() {
        BillingManager.getInstance().queryPrice(price -> {
            if (isFinishing() || price == null) {
                return;
            }
            tvPremiumPlan.setText(getString(R.string.premium_plan_title) + " (" + price + "/" + getString(R.string.year) + ")");
            tvPremiumPlan.setPaintFlags(tvPremiumPlan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            if (!sessionManager.getIsSubscriptionEnabled()) {
                btnSubscribe.setText(getString(R.string.try_premium_for));
            }
        });
        BillingManager.getInstance().refreshEntitlement((queried, entitled) -> {
            if (!isFinishing() && queried && entitled) {
                onSubscriptionActive();
            }
        });
    }

    /** Buy: re-check entitlement first; launch the purchase flow only if not already subscribed. */
    private void startPurchase() {
        BillingManager.getInstance().refreshEntitlement((queried, entitled) -> {
            if (isFinishing()) {
                return;
            }
            if (queried && entitled) {
                onSubscriptionActive();
            } else {
                BillingManager.getInstance().launchPurchase(SubscriptionPlanInfoActivity.this, purchaseListener);
            }
        });
    }

    /** Same UI and redirects as the former "subscription found" / purchase-success paths. */
    private void onSubscriptionActive() {
        btnSubscribe.setVisibility(View.VISIBLE);
        if (isLauncher) {
            btnSubscribe.setText(getString(R.string.continue_str));
        } else {
            btnSubscribe.setText(getString(R.string.cancel_subscription));
        }

        redirectOnScreenShare();

        if (isLauncher && !isScreenShare) {
            CustomDashboardApplication.setInt(context, Consts.IS_FIRST_INSTALLED, 1);
            Intent intent = new Intent(context, LoginOrSignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BillingManager.getInstance().clearListener(purchaseListener);
    }

    private void redirectOnScreenShare() {
        if (isScreenShare) {
            Intent intent = new Intent(getApplicationContext(), ScreenShareActivity.class);
            intent.putExtra(Consts.serial_number, serialNumber);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            Intent intent;
            if (CustomDashboardApplication.isDeviceSamsungAndSupportActivation() &&
                    !CustomDashboardApplication.isDeviceSamsungActivated()) {
                intent = new Intent(context, ActivateAdminActivity.class);
            } else {
                intent = new Intent(context, MainActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}