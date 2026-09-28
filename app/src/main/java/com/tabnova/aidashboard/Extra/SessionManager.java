package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    private static SessionManager sessionManager;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "KidsDashboardRef";

    // All Shared Preferences Keys
    private static final String IS_SUBSCRIPTION_ENABLED = "is_subscription_enable";
    private static final String PURCHASE_TOKEN = "purchase_token";
    private static final String IS_LICENSE_ACTIVATE = "is_license_activate";
    private static final String IS_PARENT_MODE = "is_parent_mode";
    private static final String IS_DISABLE_APP_DIALOG = "is_disable_app_dialog";
    private static final String IS_ENABLE_APP_DIALOG = "is_enable_app_dialog";
    private static final String IS_DASHBOARD_LAUNCH = "is_dashboard_launch";
    private static final String LAST_EXIT_APP_TIME = "last_exit_app_time";
    private static final String PURCHASE_TIME = "purchase_time";
    private static final String APPLY_BLOCK_DOMAINS = "apply_block_domains_list";
    private static final String APP_USAGE_DATES_LIST = "app_usage_dates_list";
    private static final String FREE_TRIAL_SCREEN_SHARE_COUNT = "free_trial_screen_share_count";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public synchronized static SessionManager getInstance(Context context) {
        if (sessionManager == null) {
            /**
             * Use application Context to prevent leak.
             * **/
            sessionManager = new SessionManager(context.getApplicationContext());
        }
        return sessionManager;
    }

    public void setIsSubscriptionEnabled(boolean isSubscriptionEnabled) {
        editor.putBoolean(IS_SUBSCRIPTION_ENABLED, isSubscriptionEnabled);
        editor.commit();
    }

    public boolean getIsSubscriptionEnabled() {
        return pref.getBoolean(IS_SUBSCRIPTION_ENABLED, false);
    }

    public void setPurchaseToken(String purchaseToken) {
        editor.putString(PURCHASE_TOKEN, purchaseToken);
        editor.commit();
    }

    public String getPurchaseToken() {
        return pref.getString(PURCHASE_TOKEN, "");
    }

    public boolean getIsLicenseEnabled() {
        return pref.getBoolean(IS_LICENSE_ACTIVATE, false);
    }

    public void setIsLicenseEnabled(boolean isLicenseEnabled) {
        editor.putBoolean(IS_LICENSE_ACTIVATE, isLicenseEnabled);
        editor.commit();
    }

    public boolean getIsParentMode() {
        return pref.getBoolean(IS_PARENT_MODE, true);
    }

    public void setIsParentMode(boolean isParentMode) {
        editor.putBoolean(IS_PARENT_MODE, isParentMode);
        editor.commit();
    }

    public boolean getIsShowDisableAppDialog() {
        return pref.getBoolean(IS_DISABLE_APP_DIALOG, true);
    }

    public void setIsShowDisableAppDialog(boolean isShowDialog) {
        editor.putBoolean(IS_DISABLE_APP_DIALOG, isShowDialog);
        editor.commit();
    }

    public boolean getIsShowEnableAppDialog() {
        return pref.getBoolean(IS_ENABLE_APP_DIALOG, true);
    }

    public void setIsShowEnableAppDialog(boolean isShowDialog) {
        editor.putBoolean(IS_ENABLE_APP_DIALOG, isShowDialog);
        editor.commit();
    }

    public boolean getIsDashboardLaunch() {
        return pref.getBoolean(IS_DASHBOARD_LAUNCH, false);
    }

    public void setIsDashboardLaunch(boolean isDashboardLaunch) {
        editor.putBoolean(IS_DASHBOARD_LAUNCH, isDashboardLaunch);
        editor.commit();
    }

    public long getLastExitAppTime() {
        String lastAppTime = pref.getString(LAST_EXIT_APP_TIME, "");
        if (!TextUtils.isEmpty(lastAppTime.trim())) {
            return Long.parseLong(lastAppTime);
        } else {
            return 0;
        }
    }

    public void setLastExitAppTime(long exitAppTime) {
        editor.putString(LAST_EXIT_APP_TIME, String.valueOf(exitAppTime));
        editor.commit();
    }

    public void setPurchaseTime(String purchaseTime) {
        editor.putString(PURCHASE_TIME, purchaseTime);
        editor.commit();
    }

    public String getPurchaseTime() {
        return pref.getString(PURCHASE_TIME, "");
    }

    public void setFreeTrialScreenShareCount(int screenShareCount) {
        editor.putInt(FREE_TRIAL_SCREEN_SHARE_COUNT, screenShareCount);
        editor.commit();
    }

    public int getFreeTrialScreenShareCount() {
        return pref.getInt(FREE_TRIAL_SCREEN_SHARE_COUNT, 0);
    }

    public boolean isApplyBlockDomains() {
        return pref.getBoolean(APPLY_BLOCK_DOMAINS, false);
    }

    public void setApplyBlockDomains(boolean applyBlockDomains) {
        editor.putBoolean(APPLY_BLOCK_DOMAINS, applyBlockDomains);
        editor.commit();
    }

    public List<String> getAppUsageDates() {
        String appUsageDates = pref.getString(APP_USAGE_DATES_LIST, "");
        if (!TextUtils.isEmpty(appUsageDates)) {
            List<String> datesList = new Gson().fromJson(appUsageDates, new TypeToken<ArrayList<String>>() {
            }.getType());
            return datesList;
        } else {
            return new ArrayList<>();
        }
    }

    public void setAppUsageDatesList(String datesList) {
        editor.putString(APP_USAGE_DATES_LIST, datesList);
        editor.commit();
    }


    public void clearData() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
}