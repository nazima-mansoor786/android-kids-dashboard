package com.tabnova.aidashboard.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.tabnova.aidashboard.Adapter.BlockDomainListAdapter;
import com.tabnova.aidashboard.Adapter.DisableAppsAdapter;
//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.startup.CommonUtility;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockDomainActivity extends AppCompatActivity implements BlockDomainListAdapter.BlockDomainInterface,
        PurchasesUpdatedListener, DisableAppsAdapter.DisableAppsInterface {

    private static final String TAG = "BlockDomainActivity";

    //CONTEXT
    private Context context;

    //ARRAY LIST
    private ArrayList<String> blockDomainWebsitesList = new ArrayList<>();

    //IMAGE VIEW
    private ImageView ivClose;
    private ImageView ivRefresh;

    //BUTTON
    private Button btnBlock;

    //RECYCLER VIEW
    private RecyclerView rvBlockList;
    private RecyclerView rvDisableApps;

    //TEXT VIEW
    private TextView tvNoBlockData;
    private TextView tvRemoveAll;
    private TextView tvBlockDomainTitle;
    private TextView tvDisableApps;

    //EDIT TEXT
    private EditText etDomainName;

    private CheckBox cbAllowToBlockDomains;

    //LINEAR LAYOUT
    private LinearLayout llBlockDomains;
    private LinearLayout llBlockApplications;

    //ADAPTER
    private BlockDomainListAdapter blockDomainListAdapter;
    private DisableAppsAdapter disableAppsAdapter;

    //SESSION MANAGER
    SessionManager sessionManager;

    // BILLING CLIENT
    private BillingClient billingClient;

    //STRING
    private String purchaseToken = "";
    private String purchaseTime = "";
    private String type = "";

    //ARRAY LIST
    private List<AppModel> disableAppsModels;
    private List<AppModel> applicationsList;

    //PROGRESS DIALOG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_domain);

        this.context = BlockDomainActivity.this;

        //INIT PROGRESS DIALOG

        //INIT SESSION MANAGER
        sessionManager = SessionManager.getInstance(context);

        disableAppsModels = new ArrayList<>();

        type = getString(R.string.website_block);

        getDataFromIntent();

        initViews();

        initClickListener();

        blockDomainWebsitesList = new ArrayList<>();
        String blockDomainsList = SerializeObject.ReadSettings(context, Consts.blockDomainWebsiteFile);
        if (!blockDomainsList.equalsIgnoreCase("")) {
            Object blockDomainsObj = SerializeObject.stringToObject(blockDomainsList);
            if (blockDomainsObj instanceof ArrayList) {
                blockDomainWebsitesList = (ArrayList<String>) blockDomainsObj;
            }
        }

        if (Utils.checkIsTablet(context)) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false);
            rvBlockList.setLayoutManager(gridLayoutManager);
        } else {
            //LAYOUT MANAGER
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            rvBlockList.setLayoutManager(linearLayoutManager);
        }

        LinearLayoutManager llBlockAppsLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        rvDisableApps.setLayoutManager(llBlockAppsLayoutManager);

        blockDomainListAdapter = new BlockDomainListAdapter(context, blockDomainWebsitesList, BlockDomainActivity.this);
        rvBlockList.setAdapter(blockDomainListAdapter);

        if (blockDomainWebsitesList.size() > 0) {
            rvBlockList.setVisibility(View.VISIBLE);
            tvNoBlockData.setVisibility(View.GONE);
        } else {
            rvBlockList.setVisibility(View.GONE);
            tvNoBlockData.setVisibility(View.VISIBLE);
        }

        tvBlockDomainTitle.performClick();

        applicationsList = new ArrayList<>();
        setUpBlockAppsList();

        if (Utils.isTiramisu()) {
            registerReceiver(
                    CommonUtility.fileDownloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    CommonUtility.fileDownloadReceiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        }

        if (!sessionManager.getIsSubscriptionEnabled()) {
            cbAllowToBlockDomains.setChecked(false);
        }

        resetTabs();
        if (type.equalsIgnoreCase(getString(R.string.website_block))) {
            tvBlockDomainTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tvBlockDomainTitle.setTextColor(ContextCompat.getColor(context, R.color.white));

            llBlockDomains.setVisibility(View.VISIBLE);
            llBlockApplications.setVisibility(View.GONE);
        } else {
            tvDisableApps.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tvDisableApps.setTextColor(ContextCompat.getColor(context, R.color.white));

            llBlockDomains.setVisibility(View.GONE);
            llBlockApplications.setVisibility(View.VISIBLE);

            ivRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void getDataFromIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString(Consts.type) != null) {
                type = getIntent().getExtras().getString(Consts.type);
            }
        }
    }

    private void initViews() {
        //INIT IMAGE VIEW
        ivClose = findViewById(R.id.iv_close);
        ivRefresh = findViewById(R.id.iv_refresh_apps_list);

        //INIT BUTTONS
        btnBlock = findViewById(R.id.btn_block);

        //INIT RECYCLER VIEW
        rvBlockList = findViewById(R.id.rv_block_domain_list);
        rvDisableApps = findViewById(R.id.rv_disable_apps);

        //INIT EDIT TEXT
        etDomainName = findViewById(R.id.et_domain_website);

        //INIT TEXT VIEW
        tvNoBlockData = findViewById(R.id.tv_no_data_exist);
        tvRemoveAll = findViewById(R.id.tv_remove_all);
        tvBlockDomainTitle = findViewById(R.id.tv_block_domain_title);
        tvDisableApps = findViewById(R.id.tv_disable_apps_title);

        //INIT CHECK BOX
        //CHECK BOX
        cbAllowToBlockDomains = findViewById(R.id.cb_allow_to_block_domains);

        //INIT LINEAR LAYOUT
        llBlockApplications = findViewById(R.id.ll_block_applications);
        llBlockDomains = findViewById(R.id.ll_block_domains);

        cbAllowToBlockDomains.setChecked(sessionManager.isApplyBlockDomains());
    }

    private void initClickListener() {
        ivClose.setOnClickListener(view -> finish());

        ivRefresh.setOnClickListener(view -> {
            writeDisableAppsFile();

            applicationsList = new ArrayList<>();
            setUpBlockAppsList();
        });

        btnBlock.setOnClickListener(view -> {
            if (blockDomainWebsitesList.size() >= 5) {
                if (sessionManager.getIsSubscriptionEnabled()) {
                    addDomainInList();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.app_name))
                            .setMessage("If you want to add more domains, Please purchase subscription first.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setNeutralButton("Purchase", (dialog, which) -> {
                                dialog.dismiss();
                                if (billingClient != null && billingClient.isReady()) {
                                    launchBillingFlow();
                                } else {
                                    setupBillingClient();
                                }
                            })
                            .setIcon(R.mipmap.ic_launcher)
                            .show();
                }
            } else {
                addDomainInList();
            }
        });

        tvRemoveAll.setOnClickListener(view -> showClearDomainListDialog("Are you sure you want to remove all block domains / websites ?", true, ""));

        tvBlockDomainTitle.setOnClickListener(view -> {
            resetTabs();
            tvBlockDomainTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tvBlockDomainTitle.setTextColor(ContextCompat.getColor(context, R.color.white));

            llBlockDomains.setVisibility(View.VISIBLE);
            llBlockApplications.setVisibility(View.GONE);
        });

        tvDisableApps.setOnClickListener(view -> {
            resetTabs();
            tvDisableApps.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tvDisableApps.setTextColor(ContextCompat.getColor(context, R.color.white));

            llBlockDomains.setVisibility(View.GONE);
            llBlockApplications.setVisibility(View.VISIBLE);

            ivRefresh.setVisibility(View.VISIBLE);
        });

        //cbApplyDomains.setOnCheckedChangeListener((compoundButton, b) -> sessionManager.setApplyBlockDomains(b));

        cbAllowToBlockDomains.setOnCheckedChangeListener((compoundButton, b) -> {
            sessionManager.setApplyBlockDomains(false);
            if (b) {
                if (sessionManager.getIsSubscriptionEnabled()) {
                    sessionManager.setApplyBlockDomains(true);
                    File file = new File(this.getExternalFilesDir(null) + "/Dashboard/block_list.txt");

                    if (!file.exists()) {
                        new CommonUtility(context).downloadFile();
                    }
                } else {
                    cbAllowToBlockDomains.setChecked(false);
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.app_name))
                            .setMessage("If you want to block millions of domains, Please purchase subscription first.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setNeutralButton("Purchase", (dialog, which) -> {
                                dialog.dismiss();
                                if (billingClient != null && billingClient.isReady()) {
                                    launchBillingFlow();
                                } else {
                                    setupBillingClient();
                                }
                            })
                            .setIcon(R.mipmap.ic_launcher)
                            .show();
                }
            }
        });
    }

    private void resetTabs() {
        tvBlockDomainTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        tvDisableApps.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

        tvBlockDomainTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
        tvDisableApps.setTextColor(ContextCompat.getColor(context, R.color.black));

        ivRefresh.setVisibility(View.GONE);
    }

    private void addDomainInList() {
        if (!TextUtils.isEmpty(etDomainName.getText().toString().trim())) {
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                Log.d(TAG, "ask user for VPN permission");
                vpnActivityResultLauncher.launch(intent);
            } else {
                if (!blockDomainWebsitesList.contains(etDomainName.getText().toString().trim())) {
                    blockDomainWebsitesList.add(etDomainName.getText().toString().trim());
                }

                etDomainName.setText("");

                readWriteBlockDomainFile();

                blockDomainListAdapter = new BlockDomainListAdapter(context, blockDomainWebsitesList, BlockDomainActivity.this);
                rvBlockList.setAdapter(blockDomainListAdapter);

                if (blockDomainWebsitesList.size() > 0) {
                    rvBlockList.setVisibility(View.VISIBLE);
                    tvNoBlockData.setVisibility(View.GONE);
                } else {
                    rvBlockList.setVisibility(View.GONE);
                    tvNoBlockData.setVisibility(View.VISIBLE);
                }
            }
        } else {
            etDomainName.setError("Please enter domain / website");
            etDomainName.requestFocus();
        }
    }

    private void setUpBlockAppsList() {
        String disableApps = SerializeObject.ReadSettings(context, Consts.disableAppsFile);
        if (!disableApps.equalsIgnoreCase("")) {
            Object blockObj = SerializeObject.stringToObject(disableApps);
            if (blockObj instanceof ArrayList) {
                disableAppsModels = (ArrayList<AppModel>) blockObj;
            }
        }

        if (applicationsList == null || applicationsList.isEmpty()) {
            getAllInstalledApplications();
        }

        if (disableAppsModels != null && disableAppsModels.size() > 0) {
            for (int k = 0; k < disableAppsModels.size(); k++) {
                boolean isAppFound = false;
                for (int n = 0; n < applicationsList.size(); n++) {
                    if (applicationsList.get(n).getPackageDetails().equalsIgnoreCase(disableAppsModels.get(k).getPackageDetails())) {
                        applicationsList.get(n).setLocked(true);
                        isAppFound = true;
                    }
                }
                if (!isAppFound) {
                    applicationsList.add(disableAppsModels.get(k));
                }
            }
        }

        Collections.sort(applicationsList, (appModel1, appModel2) -> Boolean.compare(appModel2.isLocked(), appModel1.isLocked()));

        if (applicationsList != null && applicationsList.size() > 0) {
            disableAppsAdapter = new DisableAppsAdapter(context, applicationsList, this);
            rvDisableApps.setAdapter(disableAppsAdapter);
        }
    }

    private void getAllInstalledApplications() {
        ArrayList<AppModel> appsList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();
        if (appsList != null && appsList.size() > 0) {
            applicationsList = new ArrayList<>();
            applicationsList.addAll(appsList);
        }
    }

    private void readWriteBlockDomainFile() {
        String dir1 = context.getFilesDir().getAbsolutePath();
        File f01 = new File(dir1, Consts.blockDomainWebsiteFile);
        if (f01.exists()) {
            if (f01.delete()) {
                Log.e(TAG, "readWriteBlockDomainFile: File Delete Success");
            }
        }

        if (blockDomainWebsitesList.isEmpty()) {
            SerializeObject.WriteSettings(context, "", Consts.blockDomainWebsiteFile);
        } else {
            String ser1 = SerializeObject.objectToString(blockDomainWebsitesList);
            if (ser1 != null && !ser1.equalsIgnoreCase("") && !TextUtils.isEmpty(ser1)) {
                SerializeObject.WriteSettings(context, ser1, Consts.blockDomainWebsiteFile);
            } else {
                SerializeObject.WriteSettings(context, "", Consts.blockDomainWebsiteFile);
            }
        }
    }

    private void showClearDomainListDialog(String message, boolean clearAll, String blockName) {
        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
        dialogBuilder1.setCancelable(false);
        dialogBuilder1.setTitle(getResources().getString(R.string.app_name));
        dialogBuilder1.setIcon(R.mipmap.ic_launcher);
        dialogBuilder1.setMessage(message);
        dialogBuilder1.setPositiveButton("Yes", (dialog, which) -> {
            if (clearAll) {
                //new BlockDomains(context).removeDomainRulesForChrome(new ArrayList<>(), blockDomainWebsitesList, true);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    blockDomainWebsitesList = new ArrayList<>();
                    if (blockDomainListAdapter != null) {
                        blockDomainListAdapter.notifyDataSetChanged();
                    } else {
                        blockDomainListAdapter = new BlockDomainListAdapter(context, blockDomainWebsitesList, BlockDomainActivity.this);
                        rvBlockList.setAdapter(blockDomainListAdapter);
                    }

                    readWriteBlockDomainFile();
                }, 2000);

                rvBlockList.setVisibility(View.GONE);
                tvNoBlockData.setVisibility(View.VISIBLE);
            } else {
                if (blockDomainWebsitesList.size() > 0) {
                    blockDomainWebsitesList.remove(blockName);
                }

                if (blockDomainListAdapter != null) {
                    blockDomainListAdapter.notifyDataSetChanged();
                } else {
                    blockDomainListAdapter = new BlockDomainListAdapter(context, blockDomainWebsitesList, BlockDomainActivity.this);
                    rvBlockList.setAdapter(blockDomainListAdapter);
                }

                if (blockDomainWebsitesList.size() > 0) {
                    rvBlockList.setVisibility(View.VISIBLE);
                    tvNoBlockData.setVisibility(View.GONE);
                } else {
                    rvBlockList.setVisibility(View.GONE);
                    tvNoBlockData.setVisibility(View.VISIBLE);
                    //new BlockDomains(context).removeDomainRulesForChrome(new ArrayList<>(), blockDomainWebsitesList, true);
                }

                readWriteBlockDomainFile();
            }

            dialog.dismiss();
        });
        dialogBuilder1.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog d = dialogBuilder1.create();
        d.setOnShowListener(dialog -> {
            Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
            doneBtn.setAllCaps(false);
        });
        d.show();
    }

    @Override
    public void onRemoveBlockDomain(int position, String blockDomain) {
        showClearDomainListDialog("Are you sure you want to remove this block domain / website ?", false, blockDomain);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null && purchases.size() > 0) {
            Toast.makeText(context, "Purchase item: " + purchases.size(), Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(true);
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "onPurchasesUpdated: USER_CANCELED");
            Toast.makeText(context, "Purchase cancelled.", Toast.LENGTH_SHORT).show();
            sessionManager.setIsSubscriptionEnabled(false);
            CustomDashboardApplication.isBillingLaunched = false;
            if (billingClient != null) {
                billingClient.endConnection();
                billingClient = null;
            }
        } else {
            Log.e(TAG, "onPurchasesUpdated" + billingResult.getResponseCode());
            sessionManager.setIsSubscriptionEnabled(false);
            CustomDashboardApplication.isBillingLaunched = false;
            if (billingClient != null) {
                billingClient.endConnection();
                billingClient = null;
            }
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

                    runOnUiThread(() -> Toast.makeText(context, "Subscription purchased. Now you can add more websites/domains.", Toast.LENGTH_SHORT).show());

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
                                Log.e(TAG, "handlePurchase: Product Acknowledgement Success.");
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
                                Log.e(TAG, "handlePurchase: Product Acknowledgement Success.");
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
                            Log.e(TAG, "handlePurchase: Product Acknowledgement Success.");
                        }
                    });
                }
            }
        }
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (billingClient.isReady()) {
                        runOnUiThread(() -> queryPurchaseHistory());
                    } else {
                        CustomDashboardApplication.isBillingLaunched = false;
                    }
                } else {
                    CustomDashboardApplication.isBillingLaunched = false;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                CustomDashboardApplication.isBillingLaunched = false;
            }
        });
    }

    private void launchBillingFlow() {
        try {
            runOnUiThread(() -> Toast.makeText(BlockDomainActivity.this, "Billing process is under development.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(Consts.subscriptionId).setProductType(BillingClient.ProductType.SUBS).build();
        skuList.add(product);
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(skuList).build();
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, skuDetailsList) -> {
            Log.e(TAG, "onSkuDetailsResponse: ");
            if (!skuDetailsList.isEmpty()) {
                if (skuDetailsList.get(0).getProductId().equalsIgnoreCase(Consts.subscriptionId)) {
                    Log.e(TAG, "launchBillingFlow: Product ID Same. Process for billing.");
                    List<BillingFlowParams.ProductDetailsParams> productDetailsParams = new ArrayList<>();
                    if (skuDetailsList.get(0).getSubscriptionOfferDetails() != null && !skuDetailsList.get(0).getSubscriptionOfferDetails().isEmpty() &&
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
                    billingClient.launchBillingFlow(BlockDomainActivity.this, billingFlowParams);
                }
            }
        });
    }


    private void queryPurchaseHistory() {
        QueryPurchaseHistoryParams queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build();
        billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams, (client, purchasesList) -> {
            if (client.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchasesList != null) {
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

                if (!isProductFound) {
                    sessionManager.setIsSubscriptionEnabled(false);
                    launchBillingFlow();
                } else {
                    sessionManager.setIsSubscriptionEnabled(true);
                    runOnUiThread(() -> Toast.makeText(context, "Subscription purchased. Now you can add more websites/domains.", Toast.LENGTH_SHORT).show());
                }
            } else {
                launchBillingFlow();
            }
        });
    }

    private void writeDisableAppsFile() {
        Log.e(TAG, "writeDisableAppsFile: ");
        String dir1 = context.getFilesDir().getAbsolutePath();
        File f01 = new File(dir1, Consts.disableAppsFile);
        if (f01.exists()) {
            if (f01.delete()) {
                Log.e(TAG, "writeDisableAppsFile: File delete success");
            }
        }

        ArrayList<AppModel> disableApps = new ArrayList<>();

        if (disableAppsModels != null && disableAppsModels.size() > 0) {
            disableApps.addAll(disableAppsModels);
        } else {
            if (applicationsList != null && applicationsList.size() > 0) {
                for (int i = 0; i < applicationsList.size(); i++) {
                    if (applicationsList.get(i).isLocked()) {
                        disableApps.add(applicationsList.get(i));
                    }
                }
            }
        }

        if (disableApps.isEmpty()) {
            SerializeObject.WriteSettings(context, "", Consts.disableAppsFile);
        } else {
            String ser1 = SerializeObject.objectToString(disableApps);
            if (ser1 != null && !ser1.equalsIgnoreCase("") && !TextUtils.isEmpty(ser1)) {
                SerializeObject.WriteSettings(context, ser1, Consts.disableAppsFile);
            } else {
                SerializeObject.WriteSettings(context, "", Consts.disableAppsFile);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        writeDisableAppsFile();

        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }

        unregisterReceiver(CommonUtility.fileDownloadReceiver);
    }

    @Override
    public void onDisableApp(int position, AppModel appModel, boolean isDisableApp) {
        if (!isDisableApp) {
            //applicationsList.get(position).setLocked(isDisableApp);
            removeAppFromList(appModel);
            setDisableApp(appModel, false, position);

            if (disableAppsAdapter != null) {
                disableAppsAdapter.notifyDataSetChanged();
            } else {
                disableAppsAdapter = new DisableAppsAdapter(context, applicationsList, this);
                rvDisableApps.setAdapter(disableAppsAdapter);
            }
        } else {
            if (disableAppsModels != null && disableAppsModels.size() >= 5) {
                if (sessionManager.getIsSubscriptionEnabled()) {
                    //applicationsList.get(position).setLocked(isDisableApp);
                    setDisableApp(appModel, true, position);
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.app_name))
                            .setMessage("If you want to add more apps to disable list, Please purchase subscription first.")
                            .setCancelable(false)
                            .setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setNeutralButton("Purchase", (dialog, which) -> {
                                dialog.dismiss();
                                if (billingClient != null && billingClient.isReady()) {
                                    launchBillingFlow();
                                } else {
                                    setupBillingClient();
                                }
                            })
                            .setIcon(R.mipmap.ic_launcher)
                            .show();
                }
            } else {
                setDisableApp(appModel, true, position);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setDisableApp(AppModel appModel, boolean isDisableApp, int pos) {

        if (isDisableApp) {
            if (!sessionManager.getIsSubscriptionEnabled()) {
                if (!TextUtils.isEmpty(appModel.getPackageDetails()) &&
                        (appModel.getPackageDetails().equalsIgnoreCase("com.android.vending") ||
                                appModel.getPackageDetails().equalsIgnoreCase("com.google.android.gms"))) {
                    Utils.showSingleButtonAlert(context, "You can not disable this app. Some functionality will not work proper after disable this app. ");
                    return;
                }
            }
        }

        appModel.setLocked(isDisableApp);

        if (CustomDashboardApplication.isDeviceSamsung()) {
            try {
                SuperLockState superLockState = SuperLockState.getInstance(context);
                if (superLockState.supportedActivation()) {
                    if (superLockState.isESDKLicenseActivated()) {
                        if (appModel.isLocked()) {
                            boolean isDisable = false;
                            try {
                                isDisable = superLockState.setDisableApplication(appModel.getPackageDetails());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!isDisable) {
                                applicationsList.get(pos).setLocked(false);
                                appModel.setLocked(false);
                                Toast.makeText(context, "This app cannot be disabled.", Toast.LENGTH_SHORT).show();
                            } else {
                                applicationsList.get(pos).setLocked(isDisableApp);
                                if (SessionManager.getInstance(context).getIsShowDisableAppDialog())
                                    showEnableDisableApplicationDialog(context, "Disabling a app will stop the application and remove the icon from the device.", false);

                                if (appModel.isSelected()) {
                                    appModel.setSelected(false);
                                }
                            }
                        } else {
                            applicationsList.get(pos).setLocked(isDisableApp);
                            superLockState.setEnableApplication(appModel.getPackageDetails());
                            if (SessionManager.getInstance(context).getIsShowEnableAppDialog())
                                showEnableDisableApplicationDialog(context, "Enabling the app will add icon on the device. It will be the last app icon.", true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (disableAppsAdapter != null) {
            disableAppsAdapter.notifyDataSetChanged();
        } else {
            disableAppsAdapter = new DisableAppsAdapter(context, applicationsList, this);
            rvDisableApps.setAdapter(disableAppsAdapter);
        }

        boolean isAppFound = false;
        int position = -1;
        if (disableAppsModels != null && disableAppsModels.size() > 0) {
            for (int i = 0; i < disableAppsModels.size(); i++) {
                if (disableAppsModels.get(i).getPackageDetails().equalsIgnoreCase(appModel.getPackageDetails())) {
                    isAppFound = true;
                    position = i;
                }
            }

            if (isDisableApp) {
                if (!isAppFound) {
                    disableAppsModels.add(appModel);
                }
            } else {
                if (isAppFound) {
                    disableAppsModels.remove(position);
                }
            }
        } else {
            if (isDisableApp) {
                if (disableAppsModels != null) {
                    disableAppsModels.add(appModel);
                }
            }
        }
    }

    private void removeAppFromList(AppModel appModel) {
        boolean isAppFound = false;
        int position = -1;
        if (disableAppsModels != null && disableAppsModels.size() > 0) {
            for (int i = 0; i < disableAppsModels.size(); i++) {
                if (disableAppsModels.get(i).getPackageDetails().equalsIgnoreCase(appModel.getPackageDetails())) {
                    isAppFound = true;
                    position = i;
                }
            }

            if (isAppFound) {
                disableAppsModels.remove(position);
            }
        }
    }

    public void showEnableDisableApplicationDialog(final Context context, String message, final boolean isEnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.app_name));
        builder.setMessage(message);
        builder.setIcon(R.drawable.ic_launcher);
        // set the custom layout
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.item_do_not_show, null);
        final CheckBox checkBox = customLayout.findViewById(R.id.cb_do_now_show);
        builder.setView(customLayout);
        // add a button

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (checkBox.isChecked()) {
                if (isEnable) {
                    SessionManager.getInstance(context).setIsShowEnableAppDialog(false);
                } else {
                    SessionManager.getInstance(context).setIsShowDisableAppDialog(false);
                }
            }

            dialog.dismiss();
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    ActivityResultLauncher<Intent> vpnActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    addDomainInList();
                }
            });
}