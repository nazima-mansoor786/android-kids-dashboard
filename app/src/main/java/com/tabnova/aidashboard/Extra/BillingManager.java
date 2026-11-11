package com.tabnova.aidashboard.Extra;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.UnfetchedProduct;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Application-scoped Google Play Billing wrapper for the Consts.subscriptionId subscription.
 * <p>
 * All public methods may be called from any thread; all callbacks are delivered on the main thread.
 * Targets Play Billing Library 8.x (automatic service reconnection, QueryProductDetailsResult).
 * Never consumes subscriptions and never logs purchase tokens or purchase JSON.
 */
public final class BillingManager implements PurchasesUpdatedListener {

    /** Result of {@link #refreshEntitlement(EntitlementCallback)}. */
    public interface EntitlementCallback {
        /**
         * @param queried  true if Play was queried successfully; false if billing was unavailable
         *                 or the query failed (the stored entitlement was left unchanged).
         * @param entitled the entitlement after the call (the stored value when queried is false).
         */
        void onEntitlement(boolean queried, boolean entitled);
    }

    /** Result of {@link #queryPrice(PriceCallback)}. */
    public interface PriceCallback {
        /** @param formattedPrice base plan price, e.g. "£9.99", or null if unavailable. */
        void onPrice(@Nullable String formattedPrice);
    }

    /** Outcome of {@link #launchPurchase(Activity, PurchaseListener)}. */
    public interface PurchaseListener {
        /** Purchase completed (or was already owned) and premium is now granted. */
        void onPurchaseSuccess();

        /** Payment is pending; premium is NOT granted until it completes. */
        void onPurchasePending();

        /** User cancelled the purchase flow; entitlement unchanged. */
        void onPurchaseCancelled();

        /** Purchase failed; entitlement unchanged. responseCode is a BillingClient.BillingResponseCode. */
        void onPurchaseError(int responseCode, @Nullable String message);
    }

    private interface ProductDetailsCallback {
        void onResult(@Nullable ProductDetails details);
    }

    private static final class PendingRequest {
        final Runnable onReady;
        final Runnable onUnavailable;

        PendingRequest(Runnable onReady, Runnable onUnavailable) {
            this.onReady = onReady;
            this.onUnavailable = onUnavailable;
        }
    }

    private static final String TAG = "BillingManager";

    private static final BillingManager INSTANCE = new BillingManager();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<PendingRequest> pendingRequests = new ArrayList<>();

    private BillingClient billingClient;
    private SessionManager sessionManager;
    private boolean debug;
    private boolean connecting;
    private WeakReference<PurchaseListener> listenerRef = new WeakReference<>(null);

    private BillingManager() {
    }

    public static BillingManager getInstance() {
        return INSTANCE;
    }

    /** Creates the BillingClient. Call once from Application.onCreate; later calls are ignored. */
    public void init(@NonNull Application application) {
        runOnMain(() -> {
            if (billingClient != null) {
                return;
            }
            // App's own debuggable flag (equivalent to the app BuildConfig.DEBUG, which is not generated).
            debug = (application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            sessionManager = SessionManager.getInstance(application);
            billingClient = BillingClient.newBuilder(application)
                    .setListener(this)
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts()
                            .build())
                    .enableAutoServiceReconnection()
                    .build();
        });
    }

    /**
     * Queries active subscriptions. Entitled only if a PURCHASED purchase contains Consts.subscriptionId.
     * Acknowledges any PURCHASED purchase that is not yet acknowledged. Updates the stored entitlement
     * only when the query succeeds.
     */
    public void refreshEntitlement(@Nullable EntitlementCallback callback) {
        runOnMain(() -> executeWhenReady(
                () -> billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                        (result, purchases) -> mainHandler.post(() -> onPurchasesQueried(result, purchases, callback))),
                () -> deliverEntitlement(callback, false, storedEntitlement())));
    }

    /** Returns the formatted price of the base plan's last pricing phase, or null. */
    public void queryPrice(@NonNull PriceCallback callback) {
        runOnMain(() -> executeWhenReady(
                () -> queryProductDetails(details -> {
                    String price = null;
                    ProductDetails.SubscriptionOfferDetails offer = details != null ? findBasePlanOffer(details) : null;
                    if (offer != null && offer.getPricingPhases() != null) {
                        List<ProductDetails.PricingPhase> phases = offer.getPricingPhases().getPricingPhaseList();
                        if (phases != null && !phases.isEmpty()) {
                            price = phases.get(phases.size() - 1).getFormattedPrice();
                        }
                    }
                    callback.onPrice(price);
                }),
                () -> callback.onPrice(null)));
    }

    /**
     * Launches the purchase flow for the base plan offer (getOfferId() == null, and matching
     * Consts.BASE_PLAN_ID when set). Never launches without an offer token. The listener is held
     * weakly; clear it with {@link #clearListener(PurchaseListener)} in onDestroy.
     */
    public void launchPurchase(@NonNull Activity activity, @Nullable PurchaseListener listener) {
        runOnMain(() -> {
            listenerRef = new WeakReference<>(listener);
            final WeakReference<Activity> activityRef = new WeakReference<>(activity);
            executeWhenReady(
                    () -> queryProductDetails(details -> {
                        Activity target = activityRef.get();
                        if (target == null || target.isFinishing() || target.isDestroyed()) {
                            return;
                        }
                        if (details == null) {
                            notifyError(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Subscription product not available");
                            return;
                        }
                        ProductDetails.SubscriptionOfferDetails offer = findBasePlanOffer(details);
                        if (offer == null) {
                            notifyError(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "No base plan offer available");
                            return;
                        }
                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(Collections.singletonList(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(details)
                                                .setOfferToken(offer.getOfferToken())
                                                .build()))
                                .build();
                        BillingResult result = billingClient.launchBillingFlow(target, flowParams);
                        logDebug("launchBillingFlow: " + result.getResponseCode());
                        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                            notifyError(result.getResponseCode(), result.getDebugMessage());
                        }
                    }),
                    () -> notifyError(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, "Billing service unavailable"));
        });
    }

    /** Clears the purchase listener if it is the one currently registered. Call from onDestroy. */
    public void clearListener(@Nullable PurchaseListener listener) {
        runOnMain(() -> {
            if (listener != null && listenerRef.get() == listener) {
                listenerRef.clear();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        runOnMain(() -> handlePurchasesUpdated(billingResult, purchases));
    }

    // ---------------------------------------------------------------------------------------------

    private void handlePurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int code = billingResult.getResponseCode();
        logDebug("onPurchasesUpdated: " + code);

        if (code == BillingClient.BillingResponseCode.OK) {
            if (purchases == null || purchases.isEmpty()) {
                refreshAndNotify();
                return;
            }
            List<Purchase> purchased = new ArrayList<>();
            boolean pending = false;
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    purchased.add(purchase);
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    pending = true;
                }
            }
            if (!purchased.isEmpty()) {
                acknowledgeThenGrant(purchased);
            } else if (pending) {
                PurchaseListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onPurchasePending();
                }
            } else {
                notifyError(code, "Purchase state unspecified");
            }
        } else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            PurchaseListener listener = listenerRef.get();
            if (listener != null) {
                listener.onPurchaseCancelled();
            }
        } else if (code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            refreshAndNotify();
        } else {
            notifyError(code, billingResult.getDebugMessage());
        }
    }

    /** Acknowledges purchases that need it, then grants premium if the subscription is among them. */
    private void acknowledgeThenGrant(@NonNull List<Purchase> purchases) {
        boolean containsSubscription = false;
        List<Purchase> toAcknowledge = new ArrayList<>();
        for (Purchase purchase : purchases) {
            if (purchase.getProducts().contains(Consts.subscriptionId)) {
                containsSubscription = true;
            }
            if (!purchase.isAcknowledged()) {
                toAcknowledge.add(purchase);
            }
        }

        final boolean grant = containsSubscription;
        final Runnable finish = () -> {
            if (grant) {
                sessionManager.setIsSubscriptionEnabled(true);
                PurchaseListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onPurchaseSuccess();
                }
            } else {
                notifyError(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Purchased item is not the subscription");
            }
        };

        if (toAcknowledge.isEmpty()) {
            finish.run();
            return;
        }
        // Acknowledge callbacks are posted to the main thread, so this counter is single-threaded.
        final int[] remaining = {toAcknowledge.size()};
        for (Purchase purchase : toAcknowledge) {
            acknowledge(purchase, () -> {
                remaining[0]--;
                if (remaining[0] == 0) {
                    finish.run();
                }
            });
        }
    }

    /** ITEM_ALREADY_OWNED (or OK with no purchases): re-query and report the result. */
    private void refreshAndNotify() {
        refreshEntitlement((queried, entitled) -> {
            if (entitled) {
                PurchaseListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onPurchaseSuccess();
                }
            } else {
                notifyError(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
                        queried ? "Item already owned but not active" : "Billing service unavailable");
            }
        });
    }

    private void onPurchasesQueried(@NonNull BillingResult result, @Nullable List<Purchase> purchases,
                                    @Nullable EntitlementCallback callback) {
        logDebug("queryPurchasesAsync: " + result.getResponseCode());
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            deliverEntitlement(callback, false, storedEntitlement());
            return;
        }
        boolean entitled = false;
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
                    continue;
                }
                if (!purchase.isAcknowledged()) {
                    acknowledge(purchase, null);
                }
                if (purchase.getProducts().contains(Consts.subscriptionId)) {
                    entitled = true;
                }
            }
        }
        sessionManager.setIsSubscriptionEnabled(entitled);
        deliverEntitlement(callback, true, entitled);
    }

    private void acknowledge(@NonNull Purchase purchase, @Nullable Runnable onDone) {
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(params, result -> mainHandler.post(() -> {
            // A failed acknowledgement is retried by the next refreshEntitlement() call.
            logDebug("acknowledgePurchase: " + result.getResponseCode());
            if (onDone != null) {
                onDone.run();
            }
        }));
    }

    /**
     * Billing 8 ProductDetailsResponseListener: (BillingResult, QueryProductDetailsResult).
     */
    private void queryProductDetails(@NonNull ProductDetailsCallback callback) {
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(Consts.subscriptionId)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()))
                .build();
        billingClient.queryProductDetailsAsync(params, (billingResult, queryResult) -> {
            ProductDetails found = null;
            List<ProductDetails> productDetailsList = queryResult != null ? queryResult.getProductDetailsList() : null;
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                for (ProductDetails details : productDetailsList) {
                    if (Consts.subscriptionId.equals(details.getProductId())) {
                        found = details;
                        break;
                    }
                }
            }
            logDebug("queryProductDetailsAsync: " + billingResult.getResponseCode() + ", found=" + (found != null));
            if (debug && queryResult != null) {
                List<UnfetchedProduct> unfetched = queryResult.getUnfetchedProductList();
                if (unfetched != null && !unfetched.isEmpty()) {
                    StringBuilder ids = new StringBuilder();
                    for (UnfetchedProduct product : unfetched) {
                        if (ids.length() > 0) {
                            ids.append(", ");
                        }
                        ids.append(product.getProductId());
                    }
                    logDebug("queryProductDetailsAsync: unfetched=" + unfetched.size() + " [" + ids + "]");
                }
            }
            final ProductDetails result = found;
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    /** Base plan offer: getOfferId() == null, matching Consts.BASE_PLAN_ID when it is set. */
    @Nullable
    private static ProductDetails.SubscriptionOfferDetails findBasePlanOffer(@NonNull ProductDetails details) {
        List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
        if (offers == null) {
            return null;
        }
        for (ProductDetails.SubscriptionOfferDetails offer : offers) {
            if (offer.getOfferId() != null) {
                continue;
            }
            if (!TextUtils.isEmpty(Consts.BASE_PLAN_ID) && !Consts.BASE_PLAN_ID.equals(offer.getBasePlanId())) {
                continue;
            }
            if (TextUtils.isEmpty(offer.getOfferToken())) {
                continue;
            }
            return offer;
        }
        return null;
    }

    // --- Connection handling -----------------------------------------------------------------------

    private void executeWhenReady(@NonNull Runnable onReady, @NonNull Runnable onUnavailable) {
        if (billingClient == null) {
            onUnavailable.run();
            return;
        }
        if (billingClient.isReady()) {
            onReady.run();
            return;
        }
        pendingRequests.add(new PendingRequest(onReady, onUnavailable));
        if (!connecting) {
            connect();
        }
    }

    private void connect() {
        connecting = true;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                mainHandler.post(() -> onSetupFinished(billingResult));
            }

            @Override
            public void onBillingServiceDisconnected() {
                mainHandler.post(() -> onDisconnected());
            }
        });
    }

    private void onSetupFinished(@NonNull BillingResult billingResult) {
        connecting = false;
        logDebug("onBillingSetupFinished: " + billingResult.getResponseCode());
        drainPendingRequests(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK);
    }

    private void onDisconnected() {
        // Automatic service reconnection is enabled: do NOT call startConnection() here (per Play
        // Billing docs). Fail anything still queued; later API calls reconnect automatically.
        logDebug("onBillingServiceDisconnected");
        connecting = false;
        drainPendingRequests(false);
    }

    private void drainPendingRequests(boolean ready) {
        List<PendingRequest> requests = new ArrayList<>(pendingRequests);
        pendingRequests.clear();
        for (PendingRequest request : requests) {
            if (ready && billingClient.isReady()) {
                request.onReady.run();
            } else {
                request.onUnavailable.run();
            }
        }
    }

    // --- Helpers -----------------------------------------------------------------------------------

    private void notifyError(int responseCode, @Nullable String message) {
        logDebug("purchase error: " + responseCode);
        PurchaseListener listener = listenerRef.get();
        if (listener != null) {
            listener.onPurchaseError(responseCode, message);
        }
    }

    private void deliverEntitlement(@Nullable EntitlementCallback callback, boolean queried, boolean entitled) {
        if (callback != null) {
            callback.onEntitlement(queried, entitled);
        }
    }

    private boolean storedEntitlement() {
        return sessionManager != null && sessionManager.getIsSubscriptionEnabled();
    }

    private void runOnMain(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    private void logDebug(@NonNull String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }
}
