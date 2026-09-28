package com.tabnova.aidashboard.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
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

public class MyPlansActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final String TAG = "MyPlansActivity";

    //CONTEXT
    private Context context;

    //IMAGE VIEW
    private ImageView ivClose;

    //TEXT VIEW
    private TextView tvPremiumPlan;
    private TextView tvBuyPlan;

    //BUTTON
    private Button btnBuyPlan;

    // BILLING CLIENT
    private BillingClient billingClient;

    //STRING
    private String purchaseToken = "";
    private String purchaseTime = "";

    //SESSION MANAGER
    private SessionManager sessionManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plans);

        //INIT CONTEXT
        context = this;

        //INIT SESSION MANAGER
        sessionManager = SessionManager.getInstance(context);

        //INIT TEXT VIEW
        tvPremiumPlan = findViewById(R.id.tv_premium_plan);
        tvBuyPlan = findViewById(R.id.tv_buy_plan);

        //INIT IMAGE VIEW
        ivClose = findViewById(R.id.iv_close);

        //INIT BUTTON
        btnBuyPlan = findViewById(R.id.btn_buy_plan);

        initClickListener();

        if (sessionManager.getIsSubscriptionEnabled()) {
            tvPremiumPlan.setText("1. Premium Plan (Purchased)");
            btnBuyPlan.setVisibility(View.GONE);
        } else {
            tvPremiumPlan.setText("1. Premium Plan");
            btnBuyPlan.setVisibility(View.VISIBLE);
        }

        setupBillingClient(false);
    }

    private void initClickListener() {
        tvBuyPlan.setOnClickListener(view -> setUpBillingConnection());

        ivClose.setOnClickListener(view -> finish());

        btnBuyPlan.setOnClickListener(view -> setUpBillingConnection());
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
                    //runOnUiThread(() -> Toast.makeText(context, "Success to connect billing", Toast.LENGTH_SHORT).show());
                    if (billingClient.isReady()) {
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

                if (isProductFound) {
                    tvPremiumPlan.setText(getString(R.string.premium_plan_purchased));
                    btnBuyPlan.setVisibility(View.GONE);
                    sessionManager.setIsSubscriptionEnabled(true);
                } else {
                    tvPremiumPlan.setText(getString(R.string.premium_plan));
                    btnBuyPlan.setVisibility(View.VISIBLE);
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
        try {
            runOnUiThread(() -> Toast.makeText(MyPlansActivity.this, "Billing process is under development.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(Consts.subscriptionId).setProductType(BillingClient.ProductType.SUBS).build();
        skuList.add(product);
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(skuList).build();
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, skuDetailsList) -> {
            Log.e(TAG, "onSkuDetailsResponse: ");
            if (skuDetailsList.size() > 0) {
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
                    billingClient.launchBillingFlow(MyPlansActivity.this, billingFlowParams);
                }
            }
        });
    }


    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null && purchases.size() > 0) {
            Toast.makeText(context, "Purchase item: " + purchases.size(), Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(true);
            tvPremiumPlan.setText(getString(R.string.premium_plan_purchased));
            btnBuyPlan.setVisibility(View.GONE);
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "onPurchasesUpdated: USER_CANCELED");
            Toast.makeText(context, "Purchase cancelled.", Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(false);
            tvPremiumPlan.setText(getString(R.string.premium_plan));
            btnBuyPlan.setVisibility(View.VISIBLE);
            CustomDashboardApplication.isBillingLaunched = false;
            if (billingClient != null) {
                billingClient.endConnection();
                billingClient = null;
            }
        } else {
            Log.e(TAG, "onPurchasesUpdated" + billingResult.getResponseCode());
            sessionManager.setIsSubscriptionEnabled(false);
            tvPremiumPlan.setText(getString(R.string.premium_plan));
            btnBuyPlan.setVisibility(View.VISIBLE);
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
                    tvPremiumPlan.setText(getString(R.string.premium_plan_purchased));
                    btnBuyPlan.setVisibility(View.GONE);

                    runOnUiThread(() -> Toast.makeText(context, "Product purchased. You can use block domains now.", Toast.LENGTH_SHORT).show());

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
                                Log.e(TAG, "handlePurchase: Product Acknowledged");
                            }
                        });
                    };

                    billingClient.consumeAsync(consumeParams, listener);
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
                                Log.e(TAG, "handlePurchase: Product Acknowledged");
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
                            Log.e(TAG, "handlePurchase: Product Acknowledged");
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }
}