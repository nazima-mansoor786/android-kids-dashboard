package com.tabnova.aidashboard.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//import com.tabnova.aidashboard.BuildConfig;
import com.tabnova.aidashboard.Extra.BillingManager;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.R;

public class MyPlansActivity extends AppCompatActivity {

    //CONTEXT
    private Context context;

    //IMAGE VIEW
    private ImageView ivClose;

    //TEXT VIEW
    private TextView tvPremiumPlan;
    private TextView tvBuyPlan;

    //BUTTON
    private Button btnBuyPlan;

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

        // Refresh from Play; only update the UI when the query succeeded. Never auto-launch a purchase.
        BillingManager.getInstance().refreshEntitlement((queried, entitled) -> {
            if (!isFinishing() && queried) {
                showPlanState(entitled);
            }
        });
    }

    private void initClickListener() {
        tvBuyPlan.setOnClickListener(view -> startPurchase());

        ivClose.setOnClickListener(view -> finish());

        btnBuyPlan.setOnClickListener(view -> startPurchase());
    }

    private final BillingManager.PurchaseListener purchaseListener = new BillingManager.PurchaseListener() {
        @Override
        public void onPurchaseSuccess() {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Product purchased. You can use all premium features now.", Toast.LENGTH_SHORT).show();
            showPlanState(true);
        }

        @Override
        public void onPurchasePending() {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Payment is pending. Premium will activate once payment is complete.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPurchaseCancelled() {
            // Nothing to do; entitlement and UI unchanged.
        }

        @Override
        public void onPurchaseError(int responseCode, @Nullable String message) {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(context, "Purchase could not be completed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    };

    /** Buy: re-check entitlement first; launch the purchase flow only if not already subscribed. */
    private void startPurchase() {
        BillingManager.getInstance().refreshEntitlement((queried, entitled) -> {
            if (isFinishing()) {
                return;
            }
            if (queried && entitled) {
                showPlanState(true);
            } else {
                BillingManager.getInstance().launchPurchase(MyPlansActivity.this, purchaseListener);
            }
        });
    }

    /** Same plan UI the former purchase-history and purchase-update paths used. */
    private void showPlanState(boolean entitled) {
        if (entitled) {
            tvPremiumPlan.setText(getString(R.string.premium_plan_purchased));
            btnBuyPlan.setVisibility(View.GONE);
        } else {
            tvPremiumPlan.setText(getString(R.string.premium_plan));
            btnBuyPlan.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BillingManager.getInstance().clearListener(purchaseListener);
    }
}