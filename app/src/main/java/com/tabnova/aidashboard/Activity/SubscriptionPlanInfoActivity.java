package com.tabnova.aidashboard.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanInfoActivity extends AppCompatActivity implements PurchasesUpdatedListener {

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

    // BILLING CLIENT
    private BillingClient billingClient;

    //STRING
    private String purchaseToken = "";
    private String purchaseTime = "";
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
                setupBillingClient(false);
            }
        } else {
            if (Utils.isTiramisu() && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SubscriptionPlanInfoActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
            }
            setupBillingClient(false);
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
                setUpBillingConnection();
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

        tvBuy.setOnClickListener(view -> setUpBillingConnection());
    }

    private void setUpBillingConnection() {
        if (billingClient != null && billingClient.isReady()) {
            queryPurchaseHistory(true);
        } else {
            setupBillingClient(true);
        }
    }

    private void setupBillingClient(boolean isLaunchFlow) {
        CustomDashboardApplication.isBillingLaunched = true;
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingClient.isReady()) {
                        if (!isLaunchFlow) {
                            getSubscriptionPriceDetails();
                        }
                        queryPurchaseHistory(isLaunchFlow);
                    } else {
                        CustomDashboardApplication.isBillingLaunched = false;
                        runOnUiThread(() -> Toast.makeText(context, "Billing client not ready", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    CustomDashboardApplication.isBillingLaunched = false;

                    runOnUiThread(() -> Toast.makeText(context, "Failed to connect billing" + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                CustomDashboardApplication.isBillingLaunched = false;
                runOnUiThread(() -> Toast.makeText(context, "You are disconnect from billing", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void queryPurchaseHistory(boolean isLaunchFlow) {
        QueryPurchaseHistoryParams queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build();
        billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams, (client, purchasesList) -> {
            Log.e(TAG, "queryPurchaseHistory : Status : " + client.getResponseCode());
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
                                    purchaseToken = purchase.getPurchaseToken();
                                    purchaseTime = String.valueOf(purchase.getPurchaseTime());

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

                btnSubscribe.setVisibility(View.VISIBLE);

                Log.e(TAG, "queryPurchaseHistory: Is Product Found : " + isProductFound);

                if (isProductFound) {
                    if (isLauncher) {
                        btnSubscribe.setText(getString(R.string.continue_str));
                    } else {
                        btnSubscribe.setText(getString(R.string.cancel_subscription));
                    }
                    sessionManager.setIsSubscriptionEnabled(true);

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
                } else {
                    btnSubscribe.setText(getString(R.string.continue_str));
                    sessionManager.setIsSubscriptionEnabled(false);

                    if (isLaunchFlow)
                        launchBillingFlow();
                }
            } else {
                if (isLaunchFlow)
                    launchBillingFlow();
            }
        });
    }

    private void launchBillingFlow() {
        Log.e(TAG, "launchBillingFlow: ");
        try {
            runOnUiThread(() -> Toast.makeText(SubscriptionPlanInfoActivity.this, "Billing process is under development.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(Consts.subscriptionId).setProductType(BillingClient.ProductType.SUBS).build();
        skuList.add(product);
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(skuList).build();
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, skuDetailsList) -> {
            Log.e(TAG, "launchBillingFlow: Response : " + skuDetailsList.size() + " : Status : " + billingResult.getResponseCode());
            if (skuDetailsList.size() > 0) {
                Log.e(TAG, "launchBillingFlow: Product ID : " + skuDetailsList.get(0).getProductId());
                if (skuDetailsList.get(0).getProductId().equalsIgnoreCase(Consts.subscriptionId)) {
                    Log.e(TAG, "launchBillingFlow: Product ID Same. Process for billing.");
                    List<BillingFlowParams.ProductDetailsParams> productDetailsParams = new ArrayList<>();
                    if (skuDetailsList.get(0).getSubscriptionOfferDetails() != null && skuDetailsList.get(0).getSubscriptionOfferDetails().size() > 0 &&
                            !TextUtils.isEmpty(skuDetailsList.get(0).getSubscriptionOfferDetails().get(0).getOfferToken())) {
                        Log.e(TAG, "launchBillingFlow: Offer Token : " + skuDetailsList.get(0).getSubscriptionOfferDetails().get(0).getOfferToken());
                        productDetailsParams.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(skuDetailsList.get(0))
                                .setOfferToken(skuDetailsList.get(0).getSubscriptionOfferDetails().get(0).getOfferToken())
                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                // for a list of offers that are available to the user
                                .build());
                    } else {
                        productDetailsParams.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(skuDetailsList.get(0))
                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                // for a list of offers that are available to the user
                                .build());
                    }

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParams)
                            .build();

                    Log.e(TAG, "launchBillingFlow: Launching billing flow.");
                    billingClient.launchBillingFlow(SubscriptionPlanInfoActivity.this, billingFlowParams);
                }
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null && purchases.size() > 0) {
            Toast.makeText(context, "Purchase item: " + purchases.size(), Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(true);
            //btnSubscribe.setVisibility(View.GONE);
            btnSubscribe.setVisibility(View.VISIBLE);
            if (isLauncher) {
                btnSubscribe.setText(getString(R.string.continue_str));
            } else {
                btnSubscribe.setText(getString(R.string.cancel_subscription));
            }
            if (purchases.size() > 0) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else {
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
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "onPurchasesUpdated: USER_CANCELED");
            Toast.makeText(context, "Purchase cancelled.", Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(false);
            //btnSubscribe.setVisibility(View.VISIBLE);

            btnSubscribe.setVisibility(View.VISIBLE);
            btnSubscribe.setText(getString(R.string.try_premium_for));

            CustomDashboardApplication.isBillingLaunched = false;
            if (billingClient != null) {
                billingClient.endConnection();
                billingClient = null;
            }
        } else {
            Log.e(TAG, "onPurchasesUpdated" + billingResult.getResponseCode());
            sessionManager.setIsSubscriptionEnabled(false);
            btnSubscribe.setVisibility(View.VISIBLE);
            btnSubscribe.setText(getString(R.string.try_premium_for));

            CustomDashboardApplication.isBillingLaunched = false;
            if (billingClient != null) {
                billingClient.endConnection();
                billingClient = null;
            }
            // Toast.makeText(context, "onPurchasesUpdated: " + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (BuildConfig.DEBUG) {
            runOnUiThread(() -> Toast.makeText(context, "handlePurchase: " + purchase.getPackageName(), Toast.LENGTH_SHORT).show());
        }
        Log.e(TAG, "handlePurchase: " + purchase.getPackageName() + " : " + purchase.getPurchaseToken());
        Log.e(TAG, "handlePurchase: Purchase Time : " + purchase.getPurchaseTime());
        Log.e(TAG, "handlePurchase: Purchase JSON : " + new Gson().toJson(purchase));

        if (!TextUtils.isEmpty(purchase.getOriginalJson())) {
            try {
                JSONObject jObj = new JSONObject(purchase.getOriginalJson());
                if (Utils.contains(jObj, Consts.productId) && jObj.getString(Consts.productId).equalsIgnoreCase(Consts.subscriptionId)) {
                    purchaseToken = purchase.getPurchaseToken();
                    purchaseTime = String.valueOf(purchase.getPurchaseTime());

                    sessionManager.setPurchaseToken(purchase.getPurchaseToken());
                    sessionManager.setPurchaseTime(String.valueOf(purchase.getPurchaseTime()));
                    sessionManager.setIsSubscriptionEnabled(true);

                    btnSubscribe.setVisibility(View.VISIBLE);
                    if (isLauncher) {
                        btnSubscribe.setText(getString(R.string.continue_str));
                    } else {
                        btnSubscribe.setText(getString(R.string.cancel_subscription));
                    }

                    runOnUiThread(() -> Toast.makeText(context, "Product purchased. You can use all premium features now.", Toast.LENGTH_SHORT).show());

                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();

                    ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
                        Log.e(TAG, "onConsumeResponse: " + billingResult.getResponseCode());
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
                            Log.e(TAG, "handlePurchase: ");
                            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "handlePurchase: Result Success");
                            }
                        });
                    };

                    billingClient.consumeAsync(consumeParams, listener);

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
            } catch (JSONException e) {
                e.printStackTrace();

                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
                            Log.e(TAG, "handlePurchase: ");
                            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "handlePurchase: Result Success");
                            }
                        });
                    }
                }
            }
        } else {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, result -> {
                        Log.e(TAG, "handlePurchase: ");
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.e(TAG, "handlePurchase: Result Success");
                        }
                    });
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private void getSubscriptionPriceDetails() {
        List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(Consts.subscriptionId).setProductType(BillingClient.ProductType.SUBS).build();
        skuList.add(product);
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(skuList).build();
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, skuDetailsList) -> {
            Log.e(TAG, "onSkuDetailsResponse: ");
            if (!skuDetailsList.isEmpty()) {
                runOnUiThread(() -> {
                    for (int i = 0; i < skuDetailsList.size(); i++) {
                        Log.e(TAG, "getSubscriptionDetails: Sku : " + skuDetailsList.get(i).getProductId());
                        if (!TextUtils.isEmpty(skuDetailsList.get(i).getProductId())) {
                            if (skuDetailsList.get(i).getProductId().equalsIgnoreCase(Consts.subscriptionId)) {
                                if (skuDetailsList.get(i).getOneTimePurchaseOfferDetails() != null) {
                                    if (skuDetailsList.get(i).getOneTimePurchaseOfferDetails() != null && skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice() != null &&
                                            !TextUtils.isEmpty(skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice())) {
                                        Log.e(TAG, "getSubscriptionDetails: Price : " + skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice());
                                        tvPremiumPlan.setText(getString(R.string.premium_plan_title) + " (" + skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice() + "/" + getString(R.string.year) + ")");
                                        tvPremiumPlan.setPaintFlags(tvPremiumPlan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                        btnSubscribe.setText(getString(R.string.try_premium_for) + " " + skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice() + "/" + getString(R.string.year));
                                    } else {
                                        tvPremiumPlan.setText(getString(R.string.premium_plan_title));
                                        tvPremiumPlan.setPaintFlags(tvPremiumPlan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                        btnSubscribe.setText(getString(R.string.try_premium_for));
                                    }
                                } else {
                                    tvPremiumPlan.setText(getString(R.string.premium_plan_title));
                                    tvPremiumPlan.setPaintFlags(tvPremiumPlan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                    btnSubscribe.setText(getString(R.string.try_premium_for));

                                    if (skuDetailsList.get(i).getSubscriptionOfferDetails() != null && skuDetailsList.get(i).getSubscriptionOfferDetails().size() > 0) {
                                        for (int k = 0; k < skuDetailsList.get(i).getSubscriptionOfferDetails().size(); k++) {
                                            ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails = skuDetailsList.get(i).getSubscriptionOfferDetails().get(k);
                                            if (subscriptionOfferDetails != null && subscriptionOfferDetails.getPricingPhases() != null &&
                                                    subscriptionOfferDetails.getPricingPhases().getPricingPhaseList() != null && subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().size() > 0) {
                                                for (int m = 0; m < subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().size(); m++) {
                                                    if (subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(m) != null &&
                                                            subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(m).getFormattedPrice() != null &&
                                                            !TextUtils.isEmpty(subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(m).getFormattedPrice())) {
                                                        tvPremiumPlan.setText(getString(R.string.premium_plan_title) + " (" + subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(m).getFormattedPrice() + "/" + getString(R.string.year) + ")");
                                                        if (skuDetailsList.get(i).getOneTimePurchaseOfferDetails() != null &&
                                                                skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice() != null &&
                                                                !TextUtils.isEmpty(skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice())) {
                                                            btnSubscribe.setText(getString(R.string.try_premium_for) + " " + skuDetailsList.get(i).getOneTimePurchaseOfferDetails().getFormattedPrice() + "/" + getString(R.string.year));
                                                        } else {
                                                            btnSubscribe.setText(getString(R.string.try_premium_for));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                Log.e(TAG, "getSubscriptionDetails: No Sku Details Found");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
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