package com.tabnova.aidashboard.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.tabnova.aidashboard.Adapter.AppsAdapter;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.APIManager;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.DialogLoader;
import com.tabnova.aidashboard.Extra.NetConnectivity;
import com.tabnova.aidashboard.Extra.RetrofitExtra;
import com.tabnova.aidashboard.Extra.RetrofitService;
import com.tabnova.aidashboard.Extra.SerializeObject;
import com.tabnova.aidashboard.Extra.ShowMessage;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.Knox.utils.PackageUtils;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AppSelection extends BaseActivity implements AppsAdapter.OnItemSelect {


    RecyclerView recyclerView;
    AppsAdapter adapter;
    RecyclerView.LayoutManager recyclerViewLayoutManager;

    // ARRAY LIST
    private ArrayList<AppModel> selectedAppModels;
    private ArrayList<AppModel> appModels;
    private ArrayList<AppModel> systemAppsModels = new ArrayList<>();
    private ArrayList<AppModel> copySystemAppsModels = new ArrayList<>();
    private ArrayList<AppModel> selectedAppList;
    private ArrayList<String> blockAppSelectionModels = new ArrayList<>();
    private ArrayList<AppModel> blockAppsModels = new ArrayList<>();

    // BUTTON
    private Button doneBtn;
    private Button dashboardBtn;

    // IMAGE VIEW
    private ImageView ivSearch;
    private ImageView ivDelete;
    private ImageView ivHelp;
    private ImageView closeHelpBtn;
    private ImageView ivFilter;
    private ImageView ivBack;
    private ImageView ivRefresh;

    //CARD VIEW
    private CardView cvSearchApps;

    // EDIT TEXT
    private EditText edSearchApps;

    // TEXT VIEW
    private TextView txEmpty;
    private TextView txHelpMessage;
    private TextView txHelpMessage1;
    private TextView tvEnableDisable;

    // INTEGER
    private int isSearchVisible = 0;
    private int isVolumeEnabled;
    public int totalAppsSize = 0;

    // PROGRESS DIALOG
    private DialogLoader dialogLoader;

    // STRING
    private String currentCategory;

    // ALERT DIALOG
    private AlertDialog d;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogSettings;

    // API MANAGER
    APIManager apiManager;

    // LINEAR LAYOUT
    LinearLayout llCustomHelp;

    // CONTEXT
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        context = this;

        apiManager = new APIManager(context);

        dialogLoader = new DialogLoader(context);
        recyclerView = findViewById(R.id.recycler_view);
        doneBtn = findViewById(R.id.select_btn);
        dashboardBtn = findViewById(R.id.dashboard_btn);
        cvSearchApps = findViewById(R.id.cv_search);
        edSearchApps = findViewById(R.id.ed_search_app);
        ivSearch = findViewById(R.id.iv_search);
        ivDelete = findViewById(R.id.iv_delete);
        ivHelp = findViewById(R.id.iv_help);
        ivFilter = findViewById(R.id.iv_filter);
        llCustomHelp = findViewById(R.id.ll_custom_help);
        txHelpMessage = findViewById(R.id.tx_help_message);
        txHelpMessage1 = findViewById(R.id.tx_help_message1);
        closeHelpBtn = findViewById(R.id.btn_close);
        txEmpty = findViewById(R.id.tx_empty);
        tvEnableDisable = findViewById(R.id.tv_enable_disable);
        ivBack = findViewById(R.id.iv_back);
        ivRefresh = findViewById(R.id.iv_refresh);

        // Passing the column number 1 to show online one column in each row.
        recyclerViewLayoutManager = new GridLayoutManager(context, 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        selectedAppModels = new ArrayList<>();
        selectedAppList = new ArrayList<>();
        appModels = new ArrayList<>();

        tvEnableDisable.setVisibility(View.INVISIBLE);

        /*if (CustomDashboardApplication.isSamsungAndActivated(this)) {
            tvEnableDisable.setVisibility(View.VISIBLE);
        } else {
            tvEnableDisable.setVisibility(View.GONE);
        }*/

        String blockApps = SerializeObject.ReadSettings(context, Consts.blocksAppsFile);
        if (blockApps != null && !blockApps.equalsIgnoreCase("")) {
            Object blockObj = SerializeObject.stringToObject(blockApps);
            if (blockObj instanceof ArrayList) {
                blockAppsModels = (ArrayList<AppModel>) blockObj;
            }
        }

//        ArrayList<AppModel> appsList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();
//        appModels.addAll(appsList);

        String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                appModels = (ArrayList<AppModel>) obj;
            }
        }

        dashboardBtn.setOnClickListener(v -> getSelectedAppsList(1));

        ivBack.setOnClickListener(v -> finish());

        ivFilter.setOnClickListener(v -> showFilterPopup());

        ivHelp.setOnClickListener(v -> {
            llCustomHelp.setVisibility(View.VISIBLE);
            SpannableString spannableString = new SpannableString(getResources().getString(R.string.add_dashboard_txt));
            /*Drawable d = getResources().getDrawable(R.drawable.check_box_selected_icon_teal_small);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
            spannableString.setSpan(span, spannableString.toString().indexOf(getResources().getString(R.string.add_dashboard_txt)), spannableString.toString().indexOf(getResources().getString(R.string.add_dashboard_txt)) + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);*/
            txHelpMessage.setText(spannableString);

            SpannableString spannableString1 = new SpannableString(getResources().getString(R.string.block_app_txt));
            /*Drawable d1 = getResources().getDrawable(R.drawable.ic_locked_new);
            d1.setTint(getResources().getColor(R.color.teal));
            d1.setBounds(0, 0, d1.getIntrinsicWidth(), d1.getIntrinsicHeight());
            ImageSpan span1 = new ImageSpan(d1, ImageSpan.ALIGN_BOTTOM);
            spannableString1.setSpan(span1, spannableString1.toString().indexOf(getResources().getString(R.string.block_app_txt)), spannableString1.toString().indexOf(getResources().getString(R.string.block_app_txt)) + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);*/
            txHelpMessage1.setText(spannableString1);
        });

        closeHelpBtn.setOnClickListener(v -> llCustomHelp.setVisibility(View.GONE));

        refreshApplicationsLst();

        //OLD CODE FOR SET APPLICATION LIST
        /*if (appModels.size() > 0) {

            //Toast.makeText(context, "Apps model Size > 0", Toast.LENGTH_SHORT).show();
            ivDelete.setVisibility(View.VISIBLE);

            systemAppsModels = new ArrayList<>();
            copySystemAppsModels = new ArrayList<>();

            String ser1 = SerializeObject.ReadSettings(context, Consts.categoryWiseAppsFile);
            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                Object obj = SerializeObject.stringToObject(ser1);
                // Then cast it to your object and
                if (obj instanceof ArrayList) {

                    // Do something
                    //systemAppsModels = (ArrayList<AppModel>) obj;
                    systemAppsModels = (ArrayList<AppModel>) obj;
                }
            }

            int position = -1;

            if (systemAppsModels.size() > 0) {
                for (int i = 0; i < systemAppsModels.size(); i++) {
                    if (systemAppsModels.get(i).getViewType() == 1) {
                        final String text = systemAppsModels.get(i).getPackageDetails();
                        if (text.equalsIgnoreCase("com.dialer")) {
                            position = i;
                        }

                        for (int j = 0; j < appModels.size(); j++) {
                            if (appModels.get(j).getRow() > 0 && appModels.get(j).getColumn() > 0) {
                                int pos = 0;
                                if (appModels.get(j).getColumn() > 3) {
                                    appModels.get(j).setColumn(3);
                                }
                                if (appModels.get(j).getRow() > 1) {
                                    pos = ((appModels.get(j).getRow() - 1) * 3 + appModels.get(j).getColumn()) - 1;
                                } else {
                                    pos = appModels.get(j).getColumn() - 1;
                                }

                                appModels.get(j).setPosition(pos);
                                appModels.get(j).setLocation(appModels.get(j).getRow() + "," + appModels.get(j).getColumn());
                            }

                            if (systemAppsModels.get(i).getPackageDetails().equalsIgnoreCase(appModels.get(j).getPackageDetails())) {
                                if (PackageUtils.isPackageExisted(context, appModels.get(j).getPackageDetails())) {
                                    if (appModels.get(j).isSelected()) {
                                        AppModel appModel1 = systemAppsModels.get(i);
                                        appModel1.setId(i);
                                        appModel1.setSelected(true);
                                        appModel1.setPackageDetails(text);
                                        appModel1.setWebShortCut(false);
                                        appModel1.setLocked(appModels.get(j).isLocked());
                                        appModel1.setRow(appModels.get(j).getRow());
                                        appModel1.setColumn(appModels.get(j).getColumn());
                                        appModel1.setPosition(appModels.get(j).getPosition());
                                        appModel1.setLocation(appModels.get(j).getRow() + "," + appModels.get(j).getColumn());
                                        appModel1.setViewType(1);
                                        if (systemAppsModels.get(i).getAppCategory() == null) {
                                            appModel1.setAppCategory("");
                                        } else {
                                            appModel1.setAppCategory(systemAppsModels.get(i).getAppCategory());
                                        }
                                        appModel1.setDateInstalled(systemAppsModels.get(i).getDateInstalled());
                                        appModel1.setTargetSdk(systemAppsModels.get(i).getTargetSdk());
                                        appModel1.setAppVersion(systemAppsModels.get(i).getAppVersion());
                                        systemAppsModels.set(i, appModel1);
                                    }
                                }
                            }
                        }
                    }
                }

                if (position > 0) {
                    if (!CustomDashboardApplication.isDeviceHasTelephonyService(this)) {
                        systemAppsModels.remove(position);
                    }
                }

                for (int k = 0; k < systemAppsModels.size(); k++) {
                    if (systemAppsModels.get(k).getViewType() == 1) {
                        final String packageName = systemAppsModels.get(k).getPackageDetails();
                        for (int l = 0; l < blockAppsModels.size(); l++) {
                            if (systemAppsModels.get(k).getPackageDetails().equals(blockAppsModels.get(l).getPackageDetails())) {
                                if (PackageUtils.isPackageExisted(context, systemAppsModels.get(k).getPackageDetails())) {
                                    if (!blockAppsModels.get(l).isSelected()) {
                                        AppModel appModel1 = systemAppsModels.get(k);
                                        appModel1.setId(k);
                                        appModel1.setSelected(systemAppsModels.get(k).isSelected());
                                        appModel1.setPackageDetails(packageName);
                                        appModel1.setWebShortCut(false);
                                        appModel1.setLocked(blockAppsModels.get(l).isLocked());
                                        appModel1.setViewType(1);
                                        if (systemAppsModels.get(k).getAppCategory() == null) {
                                            appModel1.setAppCategory("");
                                        } else {
                                            appModel1.setAppCategory(systemAppsModels.get(k).getAppCategory());
                                        }
                                        //appModel1.setAppCategory(systemAppsModels.get(k).getAppCategory());
                                        appModel1.setDateInstalled(systemAppsModels.get(k).getDateInstalled());
                                        appModel1.setTargetSdk(systemAppsModels.get(k).getTargetSdk());
                                        appModel1.setAppVersion(systemAppsModels.get(k).getAppVersion());
                                        systemAppsModels.set(k, appModel1);
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    Collections.sort(systemAppsModels, new Comparator<AppModel>() {
                        @Override
                        public int compare(AppModel A1, AppModel A2) {
                            if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                                return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                            else return 0;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {

                //Toast.makeText(context, "Apps model Size ==== 0", Toast.LENGTH_SHORT).show();
                ArrayList<AppModel> defaultAppsList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();
                ArrayList<String> appCategoryModels = new ArrayList<>();
                //AppModel appModel;
                appCategoryModels.clear();
                HashMap<String, ArrayList<AppModel>> mainList = new HashMap<>();

                for (int i = 0; i < defaultAppsList.size(); i++) {
                    ArrayList<AppModel> modelList = new ArrayList<>();
                    currentCategory = defaultAppsList.get(i).getAppCategory();
                    if (mainList.containsKey(currentCategory)) {
                        modelList = mainList.get(currentCategory);
                    }
                    AppModel appModel = new AppModel();
                    appModel = defaultAppsList.get(i);
                    appModel.setViewType(1);
                    if (defaultAppsList.get(i).getPackageDetails().equalsIgnoreCase("com.dialer")) {
                        if (CustomDashboardApplication.isDeviceHasTelephonyService(this)) {
                            modelList.add(appModel);
                            mainList.put(currentCategory, modelList);
                        }
                    } else {
                        modelList.add(appModel);
                        mainList.put(currentCategory, modelList);
                    }
                }

                Iterator iterator = mainList.entrySet().iterator();
                systemAppsModels = new ArrayList<>();
                while (iterator.hasNext()) {
                    Map.Entry<String, ArrayList<AppModel>> entry = (Map.Entry<String, ArrayList<AppModel>>) iterator.next();
                    AppModel categoryModel = new AppModel();
                    categoryModel.setViewType(0);
                    if (entry.getKey() == null) {
                        categoryModel.setAppCategory("");
                    } else {
                        categoryModel.setAppCategory(entry.getKey());
                    }
                    categoryModel.setCategoryAppsSize(entry.getValue().size() + "");
                    systemAppsModels.add(categoryModel);
                    systemAppsModels.addAll(entry.getValue());
                }

                try {
                    Collections.sort(systemAppsModels, (A1, A2) -> {
                        if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                            return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                        else return 0;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                adapter = new AppsAdapter(context, systemAppsModels, this);
                recyclerView.setAdapter(adapter);

                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.categoryWiseAppsFile);
                if (f0.exists()) {
                    f0.delete();
                }

                String ser2 = SerializeObject.objectToString(systemAppsModels);
                if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                    SerializeObject.WriteSettings(context, ser2, Consts.categoryWiseAppsFile);
                } else {
                    SerializeObject.WriteSettings(context, "", Consts.categoryWiseAppsFile);
                }

            }

            adapter = new AppsAdapter(context, systemAppsModels, this);
            recyclerView.setAdapter(adapter);
        } else {
            ivDelete.setVisibility(View.GONE);
            ArrayList<AppModel> defaultAppsList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();
            ArrayList<String> appCategoryModels = new ArrayList<>();
            //AppModel appModel;
            appCategoryModels.clear();
            HashMap<String, ArrayList<AppModel>> mainList = new HashMap<>();

            for (int i = 0; i < defaultAppsList.size(); i++) {
                ArrayList<AppModel> modelList = new ArrayList<>();
                currentCategory = defaultAppsList.get(i).getAppCategory();
                if (mainList.containsKey(currentCategory)) {
                    modelList = mainList.get(currentCategory);
                }
                AppModel appModel = new AppModel();
                appModel = defaultAppsList.get(i);
                appModel.setViewType(1);
                if (defaultAppsList.get(i).getPackageDetails().equalsIgnoreCase("com.dialer")) {
                    if (CustomDashboardApplication.isDeviceHasTelephonyService(this)) {
                        modelList.add(appModel);
                        mainList.put(currentCategory, modelList);
                    }
                } else {
                    modelList.add(appModel);
                    mainList.put(currentCategory, modelList);
                }
            }


            Iterator iterator = mainList.entrySet().iterator();
            systemAppsModels = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<String, ArrayList<AppModel>> entry = (Map.Entry<String, ArrayList<AppModel>>) iterator.next();
                AppModel categoryModel = new AppModel();
                categoryModel.setViewType(0);
                if (entry.getKey() == null) {
                    categoryModel.setAppCategory("");
                } else {
                    categoryModel.setAppCategory(entry.getKey());
                }
                categoryModel.setCategoryAppsSize(entry.getValue().size() + "");
                systemAppsModels.add(categoryModel);
                systemAppsModels.addAll(entry.getValue());
            }

            try {
                Collections.sort(systemAppsModels, (A1, A2) -> {
                    if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                        return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                    else return 0;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            adapter = new AppsAdapter(context, systemAppsModels, this);
            recyclerView.setAdapter(adapter);

            String dir = context.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.categoryWiseAppsFile);
            if (f0.exists()) {
                f0.delete();
            }

            String ser1 = SerializeObject.objectToString(systemAppsModels);
            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                SerializeObject.WriteSettings(context, ser1, Consts.categoryWiseAppsFile);
            } else {
                SerializeObject.WriteSettings(context, "", Consts.categoryWiseAppsFile);
            }
        }*/

        // }

        if (systemAppsModels.size() > 0) {
            CustomDashboardApplication.setInt(context, Consts.isAppSelected, 1);
        } else {
            CustomDashboardApplication.setInt(context, Consts.isAppSelected, 0);
        }

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            getApplicationList();
        }


       /* for (int k = 0; k < systemAppsModels.size(); k++){

        }*/


        //adapter = new AppsAdapter(context, systemAppsModels);

        // DELETE BUTTON CLICK EVENT
        ivDelete.setOnClickListener(v -> {
            try {
                boolean isAppDeleted = false;
                for (int i = 0; i < systemAppsModels.size(); i++) {
                    final String text = systemAppsModels.get(i).getPackageDetails();
                    for (int j = 0; j < appModels.size(); j++) {
                        if (systemAppsModels.get(i).getViewType() == 1) {
                            if (PackageUtils.isPackageExisted(context, systemAppsModels.get(i).getPackageDetails())) {
                                if (systemAppsModels.get(i).getPackageDetails().equals(appModels.get(j).getPackageDetails())) {
                                    isAppDeleted = true;
                                    AppModel appModel = systemAppsModels.get(i);
                                    appModel.setId(i);
                                    appModel.setSelected(false);
                                    if (systemAppsModels.get(i).getAppCategory() == null) {
                                        appModel.setAppCategory("");
                                    } else {
                                        appModel.setAppCategory(systemAppsModels.get(i).getAppCategory());
                                    }
                                    appModel.setPackageDetails(text);
                                    appModel.setWebShortCut(false);
                                    appModel.setLocked(systemAppsModels.get(i).isLocked());
                                    systemAppsModels.set(i, appModel);
                                    adapter.notifyItemChanged(i);
                                }
                            }
                        }
                    }
                }

                String dir = getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.appHistoryFile);
                if (f0.exists()) {
                    f0.delete();
                }

                CustomDashboardApplication.setInt(context, Consts.isAppSelected, 0);

                if (isAppDeleted) {
                    ivDelete.setVisibility(View.GONE);
                    Toast.makeText(context, "Dashboard added app deleted successfully.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //recyclerView.setAdapter(adapter);

        doneBtn.setOnClickListener(v -> {
            /*if (CustomDashboardApplication.getInt(context, Consts.isAppClick) != 2) {
                CustomDashboardApplication.setInt(context, Consts.isAppClick, 1);
            }*/
            getSelectedAppsList(0);
        });

        // SEARCH BUTTON CLICK EVENT
        ivSearch.setOnClickListener(v -> {
            if (isSearchVisible == 0) {
                isSearchVisible = 1;
                cvSearchApps.setVisibility(View.VISIBLE);
            } else {
                isSearchVisible = 0;
                cvSearchApps.setVisibility(View.GONE);
            }
        });

        ivRefresh.setOnClickListener(view -> refreshApplicationsLst());

        // SEARCH APPS EDIT TEXT LISTENER
        edSearchApps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<AppModel> appModels = ((AppsAdapter) adapter).getSelectedAppsList();
                if (appModels.size() > 0) {
                    for (int i = 0; i < appModels.size(); i++) {
                        if (appModels.get(i).getViewType() == 1) {
                            if (appModels.get(i).isSelected()) {
                                if (selectedAppList.size() > 0) {
                                    for (int j = 0; j < selectedAppList.size(); j++) {
                                        if (!selectedAppList.contains(appModels.get(i))) {
                                            selectedAppList.add(appModels.get(i));
                                        }
                                    }
                                } else {
                                    selectedAppList.add(appModels.get(i));
                                }
                            } else {

                            }
                        }
                    }
                }
                if (s.length() > 1) {
                    adapter.getFilter().filter(s.toString().toLowerCase());
                    //getSelectedAppsList(1);
                    recyclerView.setVisibility(View.VISIBLE);
                    txEmpty.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    txEmpty.setVisibility(View.GONE);
                    adapter = new AppsAdapter(context, systemAppsModels, AppSelection.this);
                    recyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void refreshApplicationsLst() {
        dialogLoader.showProgressDialog();

        ArrayList<AppModel> defaultAppsList = new ApkInfoExtractor(context).GetAllInstalledApkInfo();
        ArrayList<String> appCategoryModels = new ArrayList<>();
        //AppModel appModel;
        appCategoryModels.clear();
        HashMap<String, ArrayList<AppModel>> mainList = new HashMap<>();

        for (int i = 0; i < defaultAppsList.size(); i++) {
            ArrayList<AppModel> modelList = new ArrayList<>();
            currentCategory = defaultAppsList.get(i).getAppCategory();
            if (mainList.containsKey(currentCategory)) {
                modelList = mainList.get(currentCategory);
            }
            AppModel appModel = new AppModel();
            appModel = defaultAppsList.get(i);
            appModel.setViewType(1);
            if (defaultAppsList.get(i).getPackageDetails().equalsIgnoreCase("com.dialer")) {
                if (CustomDashboardApplication.isDeviceHasTelephonyService(this)) {
                    modelList.add(appModel);
                    mainList.put(currentCategory, modelList);
                }
            } else {
                modelList.add(appModel);
                mainList.put(currentCategory, modelList);
            }
        }

        Iterator iterator = mainList.entrySet().iterator();
        systemAppsModels = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList<AppModel>> entry = (Map.Entry<String, ArrayList<AppModel>>) iterator.next();
            AppModel categoryModel = new AppModel();
            categoryModel.setViewType(0);
            if (entry.getKey() == null) {
                categoryModel.setAppCategory("");
            } else {
                categoryModel.setAppCategory(entry.getKey());
            }
            categoryModel.setCategoryAppsSize(entry.getValue().size() + "");
            systemAppsModels.add(categoryModel);
            systemAppsModels.addAll(entry.getValue());
        }

        try {
            Collections.sort(systemAppsModels, (A1, A2) -> {
                if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                    return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                else return 0;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        int position = -1;

        String ser = SerializeObject.ReadSettings(context, Consts.appHistoryFile);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                appModels = (ArrayList<AppModel>) obj;
            }
        }

        if (appModels.size() > 0) {
            ivDelete.setVisibility(View.VISIBLE);
            if (systemAppsModels.size() > 0) {
                for (int i = 0; i < systemAppsModels.size(); i++) {
                    if (systemAppsModels.get(i).getViewType() == 1) {
                        final String text = systemAppsModels.get(i).getPackageDetails();
                        if (text.equalsIgnoreCase("com.dialer")) {
                            position = i;
                        }

                        for (int j = 0; j < appModels.size(); j++) {
                            if (appModels.get(j).getRow() > 0 && appModels.get(j).getColumn() > 0) {
                                int pos = 0;
                                if (appModels.get(j).getColumn() > 3) {
                                    appModels.get(j).setColumn(3);
                                }
                                if (appModels.get(j).getRow() > 1) {
                                    pos = ((appModels.get(j).getRow() - 1) * 3 + appModels.get(j).getColumn()) - 1;
                                } else {
                                    pos = appModels.get(j).getColumn() - 1;
                                }

                                appModels.get(j).setPosition(pos);
                                appModels.get(j).setLocation(appModels.get(j).getRow() + "," + appModels.get(j).getColumn());
                            }

                            if (systemAppsModels.get(i).getPackageDetails().equalsIgnoreCase(appModels.get(j).getPackageDetails())) {
                                if (PackageUtils.isPackageExisted(context, appModels.get(j).getPackageDetails())) {
                                    if (appModels.get(j).isSelected()) {
                                        AppModel appModel1 = systemAppsModels.get(i);
                                        appModel1.setId(i);
                                        appModel1.setSelected(true);
                                        appModel1.setPackageDetails(text);
                                        appModel1.setWebShortCut(false);
                                        appModel1.setLocked(appModels.get(j).isLocked());
                                        appModel1.setRow(appModels.get(j).getRow());
                                        appModel1.setColumn(appModels.get(j).getColumn());
                                        appModel1.setPosition(appModels.get(j).getPosition());
                                        appModel1.setLocation(appModels.get(j).getRow() + "," + appModels.get(j).getColumn());
                                        appModel1.setViewType(1);
                                        if (systemAppsModels.get(i).getAppCategory() == null) {
                                            appModel1.setAppCategory("");
                                        } else {
                                            appModel1.setAppCategory(systemAppsModels.get(i).getAppCategory());
                                        }
                                        appModel1.setDateInstalled(systemAppsModels.get(i).getDateInstalled());
                                        appModel1.setTargetSdk(systemAppsModels.get(i).getTargetSdk());
                                        appModel1.setAppVersion(systemAppsModels.get(i).getAppVersion());
                                        systemAppsModels.set(i, appModel1);
                                    }
                                }
                            }
                        }
                    }
                }

                if (position > 0) {
                    if (!CustomDashboardApplication.isDeviceHasTelephonyService(this)) {
                        systemAppsModels.remove(position);
                    }
                }

                for (int k = 0; k < systemAppsModels.size(); k++) {
                    if (systemAppsModels.get(k).getViewType() == 1) {
                        final String packageName = systemAppsModels.get(k).getPackageDetails();
                        for (int l = 0; l < blockAppsModels.size(); l++) {
                            if (systemAppsModels.get(k).getPackageDetails().equals(blockAppsModels.get(l).getPackageDetails())) {
                                if (PackageUtils.isPackageExisted(context, systemAppsModels.get(k).getPackageDetails())) {
                                    if (!blockAppsModels.get(l).isSelected()) {
                                        AppModel appModel1 = systemAppsModels.get(k);
                                        appModel1.setId(k);
                                        appModel1.setSelected(systemAppsModels.get(k).isSelected());
                                        appModel1.setPackageDetails(packageName);
                                        appModel1.setWebShortCut(false);
                                        appModel1.setLocked(blockAppsModels.get(l).isLocked());
                                        appModel1.setViewType(1);
                                        if (systemAppsModels.get(k).getAppCategory() == null) {
                                            appModel1.setAppCategory("");
                                        } else {
                                            appModel1.setAppCategory(systemAppsModels.get(k).getAppCategory());
                                        }
                                        //appModel1.setAppCategory(systemAppsModels.get(k).getAppCategory());
                                        appModel1.setDateInstalled(systemAppsModels.get(k).getDateInstalled());
                                        appModel1.setTargetSdk(systemAppsModels.get(k).getTargetSdk());
                                        appModel1.setAppVersion(systemAppsModels.get(k).getAppVersion());
                                        systemAppsModels.set(k, appModel1);
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    Collections.sort(systemAppsModels, (A1, A2) -> {
                        if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                            return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                        else return 0;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            ivDelete.setVisibility(View.GONE);
        }

        totalAppsSize = systemAppsModels.size();

        adapter = new AppsAdapter(context, systemAppsModels, this);
        recyclerView.setAdapter(adapter);

        String dir = context.getFilesDir().getAbsolutePath();
        File f0 = new File(dir, Consts.categoryWiseAppsFile);
        if (f0.exists()) {
            f0.delete();
        }

        String ser1 = SerializeObject.objectToString(systemAppsModels);
        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
            SerializeObject.WriteSettings(context, ser1, Consts.categoryWiseAppsFile);
        } else {
            SerializeObject.WriteSettings(context, "", Consts.categoryWiseAppsFile);
        }

        dialogLoader.hideProgressDialog();
    }

    private ArrayList<AppModel> filter(ArrayList<AppModel> models, String query) {
        //final String lowerCaseQuery = query.toLowerCase();
        final ArrayList<AppModel> filteredModelList = new ArrayList<>();
        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);
        for (AppModel model : models) {
            final String text = model.getPackageDetails();
            final String appName = apkInfoExtractor.GetAppName(text);
            if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(appName)) {
                if (text.trim().contains(query.trim()) || appName.trim().contains(query.trim())) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    public void getSelectedAppsList(final int type) {
        ArrayList<AppModel> appModels = adapter.getSelectedAppsList();

        final ArrayList<AppModel> blockedAppsList = new ArrayList<>();

        if (appModels.size() > 0) {
            for (int i = 0; i < appModels.size(); i++) {
                if (appModels.get(i).isSelected()) {
                    if (selectedAppList.size() > 0) {
                        for (int j = 0; j < selectedAppList.size(); j++) {
                            if (selectedAppList.get(j).getRow() > 0 && selectedAppList.get(j).getColumn() > 0) {
                                int position = 0;
                                if (selectedAppList.get(j).getColumn() > 3) {
                                    selectedAppList.get(j).setColumn(3);
                                }
                                if (selectedAppList.get(j).getRow() > 1) {
                                    position = ((selectedAppList.get(j).getRow() - 1) * 3 + selectedAppList.get(j).getColumn()) - 1;
                                } else {
                                    position = selectedAppList.get(j).getColumn() - 1;
                                }

                                selectedAppList.get(j).setPosition(position);
                                selectedAppList.get(j).setLocation(selectedAppList.get(j).getRow() + "," + selectedAppList.get(j).getColumn());
                            }

                            if (!selectedAppList.contains(appModels.get(i))) {
                                if (appModels.get(i).getRow() > 0 && appModels.get(i).getColumn() > 0) {
                                    int position = 0;
                                    if (appModels.get(i).getColumn() > 3) {
                                        appModels.get(i).setColumn(3);
                                    }
                                    if (appModels.get(i).getRow() > 1) {
                                        position = ((appModels.get(i).getRow() - 1) * 3 + appModels.get(i).getColumn()) - 1;
                                    } else {
                                        position = appModels.get(i).getColumn() - 1;
                                    }

                                    appModels.get(i).setPosition(position);
                                    appModels.get(i).setLocation(appModels.get(i).getRow() + "," + appModels.get(i).getColumn());
                                }
                                selectedAppList.add(appModels.get(i));
                            }
                        }
                    } else {
                        int position = 0;
                        if (appModels.get(i).getColumn() > 3) {
                            appModels.get(i).setColumn(3);
                        }
                        if (appModels.get(i).getRow() > 1) {
                            position = ((appModels.get(i).getRow() - 1) * 3 + appModels.get(i).getColumn()) - 1;
                        } else {
                            position = appModels.get(i).getColumn() - 1;
                        }

                        appModels.get(i).setPosition(position);
                        appModels.get(i).setLocation(appModels.get(i).getRow() + "," + appModels.get(i).getColumn());
                        selectedAppList.add(appModels.get(i));
                    }
                } else {
                    if (selectedAppList.size() > 0) {
                        selectedAppList.remove(appModels.get(i));
                    }
                }

                if (appModels.get(i).isLocked()) {
                    int position = 0;
                    if (appModels.get(i).getColumn() > 3) {
                        appModels.get(i).setColumn(3);
                    }
                    if (appModels.get(i).getRow() > 1) {
                        position = ((appModels.get(i).getRow() - 1) * 3 + appModels.get(i).getColumn()) - 1;
                    } else {
                        position = appModels.get(i).getColumn() - 1;
                    }

                    appModels.get(i).setPosition(position);
                    appModels.get(i).setLocation(appModels.get(i).getRow() + "," + appModels.get(i).getColumn());
                    blockedAppsList.add(appModels.get(i));
                }
            }

            CustomDashboardApplication.setInt(context, Consts.device_counter, CustomDashboardApplication.getInt(context, Consts.device_counter) + 1);

            Handler handler = new Handler();
            handler.postDelayed(() -> {

                ArrayList<AppModel> lastBlockApps = new ArrayList<>();
                String ser2 = SerializeObject.ReadSettings(context, Consts.blocksAppsFile);
                if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                    Object obj = SerializeObject.stringToObject(ser2);
                    // Then cast it to your object and
                    if (obj instanceof ArrayList) {
                        // Do something
                        lastBlockApps = (ArrayList<AppModel>) obj;
                    }
                }

                String dir = context.getFilesDir().getAbsolutePath();
                File f0 = new File(dir, Consts.blocksAppsFile);
                if (f0.exists()) {
                    f0.delete();
                }

                String ser1 = SerializeObject.objectToString(blockedAppsList);
                if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                    SerializeObject.WriteSettings(context, ser1, Consts.blocksAppsFile);
                } else {
                    SerializeObject.WriteSettings(context, "", Consts.blocksAppsFile);
                }

                for (int l = 0; l < selectedAppList.size(); l++) {
                    if (selectedAppList.get(l).getRow() > 0 && selectedAppList.get(l).getColumn() > 0) {
                        int position = 0;
                        if (selectedAppList.get(l).getColumn() > 3) {
                            selectedAppList.get(l).setColumn(3);
                        }
                        if (selectedAppList.get(l).getRow() > 1) {
                            position = ((selectedAppList.get(l).getRow() - 1) * 3 + selectedAppList.get(l).getColumn()) - 1;
                        } else {
                            position = selectedAppList.get(l).getColumn() - 1;
                        }

                        selectedAppList.get(l).setPosition(position);
                        selectedAppList.get(l).setLocation(selectedAppList.get(l).getRow() + "," + selectedAppList.get(l).getColumn());
                    }
                    if (selectedAppList.get(l).getAppCategory() == null) {
                        selectedAppList.get(l).setAppCategory("");
                    }
                }

                String ser = SerializeObject.objectToString(selectedAppList);
                if (ser != null && !ser.equalsIgnoreCase("")) {
                    SerializeObject.WriteSettings(context, ser, Consts.appHistoryFile);
                } else {
                    SerializeObject.WriteSettings(context, "", Consts.appHistoryFile);
                }

                if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
                    apiManager.uploadAppsToServer();
                }

                if (selectedAppList.size() > 0) {
                    ivDelete.setVisibility(View.VISIBLE);
                    CustomDashboardApplication.setInt(context, Consts.isAppSelected, 1);
                } else {
                    ivDelete.setVisibility(View.GONE);
                    CustomDashboardApplication.setInt(context, Consts.isAppSelected, 0);
                }

                String tempBlockdir = getApplicationContext().getFilesDir().getAbsolutePath();
                File tempBlockApps = new File(tempBlockdir, Consts.tempBlockAppFile);
                if (tempBlockApps.exists()) {
                    tempBlockApps.delete();
                }

                if (type == 1) {
                    Log.e("TAG", "run: @720");
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else if (type == 0) {
                    finish();
                }
            }, 1000);

        } else {
            Toast.makeText(context, getResources().getString(R.string.select_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CustomDashboardApplication.getInt(context, Consts.is_logged_in) == 1) {
            apiManager.getProfileCounter(CustomDashboardApplication.getString(context, Consts.token));
        }

        if (CustomDashboardApplication.getVolume(context, Consts.ALLOWVOLUME) == 1) {
            isVolumeEnabled = 1;
        } else {
            isVolumeEnabled = 0;
        }
    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, getPackageName());

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // SHOW OPEN SETTINGS DIALOG
    void showOpenSettingsDialog() {
        alertDialogSettings = new AppCompatDialog(context);
        alertDialogSettings.setContentView(R.layout.launcher_pop_up);

        Button btnDone = alertDialogSettings.findViewById(R.id.done_btn);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (isMyLauncherDefault()) {
                finish();
                // } else {
                //   resetPreferredLauncherAndOpenChooser(context);
                // }
                alertDialogSettings.dismiss();
            }
        });

        alertDialogSettings.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogSettings.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogSettings.getWindow().setGravity(Gravity.CENTER);
        alertDialogSettings.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isVolumeEnabled == 1) {
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

                AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                try {
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

              /*  AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {
               /* final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.ADJUST_MUTE);*/

          /*      AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isVolumeEnabled == 1) {
                /*final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);*/

                AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                // Get the music current volume level
                int music_volume_level = mode.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Get the device music maximum volume level
                int max = mode.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                try {
                    mode.setStreamVolume(AudioManager.STREAM_MUSIC, music_volume_level, max);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

            /*    AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                mode.setStreamMute(AudioManager.STREAM_ALARM, false);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, false);
                mode.setStreamMute(AudioManager.STREAM_RING, false);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, false);*/

                return true;
            } else {
               /* final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                mode.setRingerMode(AudioManager.ADJUST_MUTE);*/

            /*    AudioManager mode=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mode.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mode.setStreamMute(AudioManager.STREAM_ALARM, true);
                mode.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mode.setStreamMute(AudioManager.STREAM_RING, true);
                mode.setStreamMute(AudioManager.STREAM_SYSTEM, true);*/
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (llCustomHelp.getVisibility() == View.VISIBLE) {
                llCustomHelp.setVisibility(View.GONE);
            } else {
                //CustomDashboardApplication.setInt(context, Consts.isAppClick, 1);
                finish();
            }
            return true;
        }
        return true;
    }


    public void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            if (isMyLauncherDefaultstr().contains("oppo.launcher")) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            } else if (isMyLauncherDefaultstr().contains("huawei.android.launcher")) {
                Intent localObject = new Intent("com.android.settings.PREFERRED_SETTINGS");
                startActivity(localObject);
            } else {
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                startActivity(selector);
            }

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String isMyLauncherDefaultstr() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.e("Package Name:", str);

        return str;
    }

    public void getApplicationList() {

        try {

            final ArrayList<String> appCategoryModels = new ArrayList<>();
            ArrayList<AppModel> resultCategoryModels = new ArrayList<>();


            if (NetConnectivity.isOnline(context)) {
                //  dialogLoader.showProgressDialog();


                //Retrofit retrofit = RetrofitExtra.getInstance();
                Retrofit retrofit = RetrofitExtra.getInstanceV2();
                RetrofitService feedApi = retrofit.create(RetrofitService.class);

                // API CALL
                Call<JsonObject> calls = feedApi.getAllApplicationList(CustomDashboardApplication.getString(context, Consts.token));
                Log.e("url", calls.request().toString());

                calls.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        //dialogLoader.hideProgressDialog();

                        if (response.code() == 200) {
                            final String responseString = response.body().toString();
                            Log.e("resp", response.body().toString());
                            try {
                                final JSONObject jsonObject = new JSONObject(responseString);
                                int status = jsonObject.getInt(Consts.status);
                                if (status == 1) {
                                    try {
                                        ArrayList<AppModel> tempList = new ArrayList<>();

                                        JSONObject dataObject = jsonObject.getJSONObject(Consts.data);
                                        JSONArray categoryArray = dataObject.getJSONArray(Consts.categories);

                                        //AppModel appModel = new AppModel();
                                        //CustomDashboardApplication.setInt(context, Consts.serverAppListCount, dataArray.length());
                                        if (categoryArray.length() > 0) {
                                            for (int i = 0; i < categoryArray.length(); i++) {
                                                JSONObject dataObj = categoryArray.getJSONObject(i);

                                                JSONArray dataArray = dataObj.getJSONArray(Consts.data);

                                                if (dataArray.length() > 0) {
                                                    for (int k = 0; k < dataArray.length(); k++) {
                                                        JSONObject appObj = dataArray.getJSONObject(k);
                                                        AppModel appModel = new AppModel();
                                                        appModel.setId(k);

                                                      /*  AppModel categoryModel = new AppModel();
                                                        categoryModel.setViewType(0);*/
                                                        // appModel.setAppCategory(dataObj.getString(Consts.name));


                                                    /*    if (appCategoryModels.contains( dataObj.getString(Consts.name))){

                                                        } else {
                                                            appCategoryModels.add(dataObj.getString(Consts.name));
                                                            categoryModel.setViewType(0);
                                                            tempList.add(categoryModel);
                                                        }
                                                        appModel.setViewType(1);*/
                                                        appModel.setAppCategory(dataObj.getString(Consts.name));
                                                        appModel.setAppInfo(appObj.getString(Consts.device_application_additional_info));
                                                        appModel.setDateInstalled(appObj.getString(Consts.device_application_date_installed));
                                                        appModel.setTargetSdk(appObj.getString(Consts.device_application_sdk_version));
                                                        appModel.setAppVersion(appObj.getString(Consts.device_application_version));
                                                        if (appObj.getInt(Consts.device_application_type) == 1) {
                                                            appModel.setSystemApp(true);
                                                        }
                                                        appModel.setPackageDetails(appObj.getString(Consts.device_application_package_name));

                                                        if (appObj.getInt(Consts.added_to_dashboard) == 1) {
                                                            appModel.setSelected(true);
                                                        } else {
                                                            appModel.setSelected(false);
                                                        }

                                                        if (Utils.contains(appObj, Consts.is_app_locked)) {
                                                            if (appObj.getInt(Consts.is_app_locked) == 2) {
                                                                appModel.setLocked(true);
                                                            } else {
                                                                appModel.setLocked(false);
                                                            }
                                                        }

                                                        if (Utils.contains(appObj, Consts.grid_row)) {
                                                            if (!TextUtils.isEmpty(appObj.getString(Consts.grid_row))) {
                                                                try {
                                                                    appModel.setRow(Integer.parseInt(appObj.getString(Consts.grid_row)));
                                                                } catch (NumberFormatException e) {
                                                                    e.printStackTrace();
                                                                    appModel.setRow(1);
                                                                }
                                                            } else {
                                                                appModel.setRow(1);
                                                            }
                                                        } else {
                                                            appModel.setRow(1);
                                                        }

                                                        if (Utils.contains(appObj, Consts.grid_column)) {
                                                            if (!TextUtils.isEmpty(appObj.getString(Consts.grid_column))) {
                                                                try {
                                                                    appModel.setColumn(Integer.parseInt(appObj.getString(Consts.grid_column)));
                                                                } catch (NumberFormatException e) {
                                                                    e.printStackTrace();
                                                                    appModel.setColumn(1);
                                                                }
                                                            } else {
                                                                appModel.setColumn(1);
                                                            }
                                                        } else {
                                                            appModel.setColumn(1);
                                                        }

                                                        if (appModel.getRow() > 0 && appModel.getColumn() > 0) {
                                                            int position = 0;
                                                            if (appModel.getColumn() > 3) {
                                                                appModel.setColumn(3);
                                                            }
                                                            if (appModel.getRow() > 1) {
                                                                position = ((appModel.getRow() - 1) * 3 + appModel.getColumn()) - 1;
                                                            } else {
                                                                position = appModel.getColumn() - 1;
                                                            }

                                                            appModel.setPosition(position);
                                                        } else {
                                                            appModel.setPosition(0);
                                                        }

                                                        appModel.setLocation(appModel.getRow() + "," + appModel.getColumn());

                                                        if (appModel.getPackageDetails() != null && PackageUtils.isPackageExisted(AppSelection.this, appModel.getPackageDetails()))
                                                            tempList.add(appModel);
                                                    }
                                                }
                                            }

                                            ArrayList<AppModel> defaultAppsList = new ArrayList<>();
                                            defaultAppsList.addAll(tempList);
                                            ArrayList<String> appCategoryModels = new ArrayList<>();
                                            appCategoryModels.clear();
                                            HashMap<String, ArrayList<AppModel>> mainList = new HashMap<>();
                                            String currentCategory;

                                            for (int i = 0; i < defaultAppsList.size(); i++) {
                                                ArrayList<AppModel> modelList = new ArrayList<>();
                                                currentCategory = defaultAppsList.get(i).getAppCategory();
                                                if (mainList.containsKey(currentCategory)) {
                                                    modelList = mainList.get(currentCategory);
                                                }
                                                AppModel appModel = new AppModel();
                                                appModel = defaultAppsList.get(i);
                                                appModel.setViewType(1);
                                                modelList.add(appModel);
                                                mainList.put(currentCategory, modelList);
                                            }

                                            Iterator iterator = mainList.entrySet().iterator();
                                            systemAppsModels = new ArrayList<>();
                                            systemAppsModels.clear();
                                            while (iterator.hasNext()) {
                                                Map.Entry<String, ArrayList<AppModel>> entry = (Map.Entry<String, ArrayList<AppModel>>) iterator.next();
                                                AppModel categoryModel = new AppModel();
                                                categoryModel.setViewType(0);
                                                if (entry.getKey() == null) {
                                                    categoryModel.setAppCategory("");
                                                } else {
                                                    categoryModel.setAppCategory(entry.getKey());
                                                }
                                                categoryModel.setCategoryAppsSize(entry.getValue().size() + "");
                                                systemAppsModels.add(categoryModel);
                                                systemAppsModels.addAll(entry.getValue());
                                            }

                                            try {
                                                Collections.sort(systemAppsModels, new Comparator<AppModel>() {
                                                    @Override
                                                    public int compare(AppModel A1, AppModel A2) {
                                                        if (A1.getAppCategory() != null && A2.getAppCategory() != null)
                                                            return A1.getAppCategory().compareToIgnoreCase(A2.getAppCategory());
                                                        else return 0;
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            //systemAppsModels.addAll(tempList);

                                            adapter = new AppsAdapter(context, systemAppsModels, AppSelection.this);
                                            recyclerView.setAdapter(adapter);
                              /*              String ser = SerializeObject.objectToString(tempList);
                                            if (ser != null && !ser.equalsIgnoreCase("")) {
                                                SerializeObject.WriteSettings(context, ser,Consts.appHistoryFile);
                                            } else {
                                                SerializeObject.WriteSettings(context, "", Consts.appHistoryFile);
                                            }

                                            Intent intent = new Intent(Consts.listUpdate);
                                            intent.putExtra(Consts.listUpdateCount , 1);
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    ShowMessage.showError(context, jsonObject.getString(Consts.responseMessage));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                String resp = response.errorBody().string();
                                Log.d("tag", "onResponse - Status : " + response.code());
                                Log.e("regErr->", resp);
                                Gson gson = new Gson();
                                TypeAdapter<JsonObject> adapter = gson.getAdapter(JsonObject.class);

                                if (response.errorBody() != null) {
                                    JsonObject registerResponse = adapter.fromJson(resp);
                                    ShowMessage.showError(context, registerResponse.get(Consts.responseMessage) + "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                //ShowMessage.showError(context, context.getResources().getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showFilterPopup() {
        final ArrayList<AppModel> appList = new ArrayList<>();

        String[] filters;
        /*if (CustomDashboardApplication.isDeviceSamsungActivated()) {
            filters = new String[]{"Disabled Applications", "Dashboard Applications", "All Applications", "Cancel"};
        } else {
            filters = new String[]{"Dashboard Applications", "All Applications", "Cancel"};
        }*/

        filters = new String[]{"Dashboard Applications", "All Applications", "Cancel"};

        new AlertDialog.Builder(context)
                .setTitle("Select filter option")
                .setCancelable(false)
                .setItems(filters, (dialog, which) -> {
                    if (which == 0) {
                        if (systemAppsModels != null && systemAppsModels.size() > 0) {
                            for (int i = 0; i < systemAppsModels.size(); i++) {
                                if (systemAppsModels.get(i).isSelected()) {
                                    appList.add(systemAppsModels.get(i));
                                }
                            }

                            adapter = new AppsAdapter(context, appList, AppSelection.this);
                            recyclerView.setAdapter(adapter);
                        }
                        /*if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                            if (systemAppsModels != null && systemAppsModels.size() > 0) {
                                for (int i = 0; i < systemAppsModels.size(); i++) {
                                    if (systemAppsModels.get(i).isLocked()) {
                                        appList.add(systemAppsModels.get(i));
                                    }
                                }

                                adapter = new AppsAdapter(context, appList, AppSelection.this);
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            if (systemAppsModels != null && systemAppsModels.size() > 0) {
                                for (int i = 0; i < systemAppsModels.size(); i++) {
                                    if (systemAppsModels.get(i).isSelected()) {
                                        appList.add(systemAppsModels.get(i));
                                    }
                                }

                                adapter = new AppsAdapter(context, appList, AppSelection.this);
                                recyclerView.setAdapter(adapter);
                            }
                        }*/
                    } else if (which == 1) {
                        adapter = new AppsAdapter(context, systemAppsModels, AppSelection.this);
                        recyclerView.setAdapter(adapter);
                        /*if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                            if (systemAppsModels != null && systemAppsModels.size() > 0) {
                                for (int i = 0; i < systemAppsModels.size(); i++) {
                                    if (systemAppsModels.get(i).isSelected()) {
                                        appList.add(systemAppsModels.get(i));
                                    }
                                }

                                adapter = new AppsAdapter(context, appList, AppSelection.this);
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            adapter = new AppsAdapter(context, systemAppsModels, AppSelection.this);
                            recyclerView.setAdapter(adapter);
                        }*/
                    } else if (which == 2) {
                        /*if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                            adapter = new AppsAdapter(context, systemAppsModels, AppSelection.this);
                            recyclerView.setAdapter(adapter);
                        }*/
                    }
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onAppSelected(int pos, int clickType) {
        getSelectedAppsList(2);
    }
}
