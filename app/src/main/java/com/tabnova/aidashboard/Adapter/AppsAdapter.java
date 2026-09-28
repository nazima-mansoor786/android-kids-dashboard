package com.tabnova.aidashboard.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Activity.AppSelection;
import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Knox.utils.PackageUtils;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class AppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    // CONTEXT
    public Context context;

    // ARRAY LIST
    public ArrayList<AppModel> stringList;
    public ArrayList<AppModel> stringFilterList;

    public OnItemSelect onItemSelect;

    private AppCompatDialog alertDialogBatteryOptimization;

    public AppsAdapter(Context context, ArrayList<AppModel> list, OnItemSelect onItemSelect) {
        this.context = context;
        this.stringList = list;
        this.onItemSelect = onItemSelect;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charStr = charSequence.toString().trim();
                FilterResults filterResults = new FilterResults();
                if (charStr.isEmpty()) {
                    stringFilterList = stringList;
                } else {
                    ArrayList<AppModel> appModels = new ArrayList<>();

                    ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);
                    for (AppModel model : stringList) {
                        if (model.getViewType() == 1) {
                            final String text = model.getPackageDetails();
                            final String appName = apkInfoExtractor.GetAppName(text);
                            if (!TextUtils.isEmpty(appName.toLowerCase())) {
                                if (appName.trim().toLowerCase().startsWith(charStr.trim().toLowerCase())) {
                                    appModels.add(model);
                                }
                            }
                        }

                    }

                    stringFilterList = appModels;
                    filterResults.values = stringFilterList;
                    filterResults.count = stringFilterList.size();

                }


                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                stringList = (ArrayList<AppModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public ImageView imageView, ivLockApp;
        ImageView ivSelection;
        public TextView textView_App_Name;
        public TextView textView_App_Package_Name;

        public ViewHolder(View view) {

            super(view);

            cardView = view.findViewById(R.id.card_view);
            imageView = view.findViewById(R.id.imageview);
            textView_App_Name = view.findViewById(R.id.Apk_Name);
            textView_App_Package_Name = view.findViewById(R.id.Apk_Package_Name);
            ivSelection = view.findViewById(R.id.iv_select);
            ivLockApp = view.findViewById(R.id.iv_lock);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public TextView tx_category_name;

        public SectionViewHolder(View view) {

            super(view);
            tx_category_name = view.findViewById(R.id.category_name);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View sectionView = LayoutInflater.from(context).inflate(R.layout.item_category_header_section, parent, false);
            return new SectionViewHolder(sectionView);
        } else if (viewType == 1) {
            View dataView = LayoutInflater.from(context).inflate(R.layout.card_view_layout, parent, false);
            return new ViewHolder(dataView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

            final String ApplicationPackageName = stringList.get(position).getPackageDetails();
            if (stringList.get(position).getPackageDetails().equals("com.dialer")) {
                viewHolder.textView_App_Name.setText("Phone");
                viewHolder.imageView.setImageResource(R.drawable.ic_dia);
            } else {
                String ApplicationLabelName = apkInfoExtractor.GetAppName(ApplicationPackageName);
                viewHolder.textView_App_Name.setText(ApplicationLabelName);
                Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);
                viewHolder.imageView.setImageDrawable(drawable);
            }


            //viewHolder.textView_App_Name.setText(ApplicationLabelName);

            viewHolder.textView_App_Package_Name.setText(ApplicationPackageName);

            if (PackageUtils.isPackageExisted(context, stringList.get(position).getPackageDetails())) {
                viewHolder.ivSelection.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivSelection.setVisibility(View.GONE);
            }

            if (stringList.get(position).isSelected()) {
                viewHolder.ivSelection.setImageResource(R.drawable.check_box_selected_icon_teal);
            } else {
                viewHolder.ivSelection.setImageResource(R.drawable.check_box_unselected_icon_teal);
            }

            if (stringList.get(position).isLocked()) {
                viewHolder.ivLockApp.setImageResource(R.drawable.ic_locked_new);
            } else {
                viewHolder.ivLockApp.setImageResource(R.drawable.ic_unlocked_new);
            }

            viewHolder.ivLockApp.setVisibility(View.GONE);

            /*if (CustomDashboardApplication.isDeviceSamsungActivated()) {
                if (PackageUtils.isPackageExisted(context, stringList.get(position).getPackageDetails())) {
                    viewHolder.ivLockApp.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.ivLockApp.setVisibility(View.GONE);
                }
            } else {
                viewHolder.ivLockApp.setVisibility(View.GONE);
            }*/

            viewHolder.ivSelection.setOnClickListener(v -> {
                // onItemSelect.onAppSelected(position, 1);
                viewHolder.itemView.performClick();
            });

            //Adding click listener on CardView to open clicked application directly from here .
            viewHolder.itemView.setOnClickListener(view -> {
                if (!stringList.get(position).isLocked()) {
                    if (stringList.get(position).isSelected()) {
                        stringList.get(position).setSelected(false);
                        stringList.get(position).setPosition(-1);
                        notifyDataSetChanged();
                        onItemSelect.onAppSelected(position, 1);
                    } else {
                        showGridSelection(position);
                        stringList.get(position).setSelected(true);
                    }
                } else {
                    Toast.makeText(context, "Please enable application for add to dashboard.", Toast.LENGTH_SHORT).show();
                }
/*
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(ApplicationPackageName);
            if(intent != null){
                context.startActivity(intent);
            } else {
                Toast.makeText(context,ApplicationPackageName + " Error, Please Try Again.", Toast.LENGTH_LONG).show();
            }*/
            });

            viewHolder.ivLockApp.setOnClickListener(v -> {
               /* if (CustomDashboardApplication.getInt(context, Consts.is_power_optimize) == 0) {
                    try {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            showBatteryOptimizationDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/

//                    if (SessionManager.getInstance(context).getIsSubscriptionEnabled()) {
                if (stringList.get(position).isLocked()) {
                    stringList.get(position).setLocked(false);
                } else {
                    stringList.get(position).setLocked(true);
                }

                if (CustomDashboardApplication.isDeviceSamsung()) {
                    try {
                        SuperLockState superLockState = SuperLockState.getInstance(context);
                        if (superLockState.supportedActivation()) {
                            if (superLockState.isESDKLicenseActivated()) {
                                if (stringList.get(position).isLocked()) {
                                    boolean isDisable = false;
                                    try {
                                        isDisable = superLockState.setDisableApplication(stringList.get(position).getPackageDetails());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!isDisable) {
                                        Toast.makeText(context, "This app cannot be disabled.", Toast.LENGTH_SHORT).show();
                                        stringList.get(position).setLocked(false);
                                    } else {
                                        if (SessionManager.getInstance(context).getIsShowDisableAppDialog())
                                            showEnableDisableApplicationDialog(context, "Disabling a app will stop the application and remove the icon from the device.", false);

                                        if (stringList.get(position).isSelected()) {
                                            stringList.get(position).setSelected(false);
                                        }
                                    }
                                } else {
                                    superLockState.setEnableApplication(stringList.get(position).getPackageDetails());
                                    if (SessionManager.getInstance(context).getIsShowEnableAppDialog())
                                        showEnableDisableApplicationDialog(context, "Enabling the app will add icon on the device. It will be the last app icon.", true);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                notifyItemChanged(position);
                onItemSelect.onAppSelected(position, 1);
               /* } else {
                    showSubscriptionAlert(context);
                }*/
            });
        } else if (holder instanceof SectionViewHolder) {
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.tx_category_name.setText(stringList.get(position).getAppCategory() + " (" + stringList.get(position).getCategoryAppsSize() + ")");
        }
    }

    private void showSubscriptionAlert(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage("Your subscription is not active. Please enable subscription to use this feature.")
                .setCancelable(false)
                .setPositiveButton("Buy Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (context instanceof AppSelection) {
                            Intent intent = new Intent();
                            intent.putExtra(Consts.is_launch_billing, true);
                            ((AppSelection) context).setResult(Activity.RESULT_OK, intent);
                            ((AppSelection) context).finish();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .show();
    }

    public interface OnItemSelect {
        void onAppSelected(int pos, int clickType);
    }

    @Override
    public int getItemViewType(int position) {
        if (stringList.get(position).getViewType() == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return (stringList != null) ? stringList.size() : 0;
    }

    public ArrayList<AppModel> getSelectedAppsList() {
        return stringList;
    }


    void showBatteryOptimizationDialog() {
        alertDialogBatteryOptimization = new AppCompatDialog(context);
        alertDialogBatteryOptimization.setContentView(R.layout.custom_popup_message);

        Button btnOk = alertDialogBatteryOptimization.findViewById(R.id.btn_ok);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    context.startActivity(intent);
                    CustomDashboardApplication.setInt(context, Consts.is_power_optimize, 1);
                }

                alertDialogBatteryOptimization.dismiss();
            }
        });

        alertDialogBatteryOptimization.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogBatteryOptimization.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDialogBatteryOptimization.getWindow().setGravity(Gravity.CENTER);
        alertDialogBatteryOptimization.show();
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

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    if (isEnable) {
                        SessionManager.getInstance(context).setIsShowEnableAppDialog(false);
                    } else {
                        SessionManager.getInstance(context).setIsShowDisableAppDialog(false);
                    }
                }

                dialog.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showGridSelection(final int pos) {
        final AppCompatDialog appCompatDialog = new AppCompatDialog(context);
        appCompatDialog.setContentView(R.layout.item_grid_selection);
        appCompatDialog.setCancelable(false);

        final Spinner rowSpinner = appCompatDialog.findViewById(R.id.spinner_row);
        final Spinner columnSpinner = appCompatDialog.findViewById(R.id.spinner_column);
        Button btnSelect = appCompatDialog.findViewById(R.id.btn_select);

        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> columns = new ArrayList<>();

        int size = stringList.size();

        if (context instanceof AppSelection) {
            if (((AppSelection) context).totalAppsSize > 0) {
                size = ((AppSelection) context).totalAppsSize;
            }
        }

        if (size <= 3) {
            size = 30;
        }

        for (int i = 1; i <= Math.ceil(size / 3); i++) {
            rows.add(i);
        }

        for (int i = 1; i <= 3; i++) {
            columns.add(i);
        }

        ArrayAdapter<Integer> rowArray = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, rows);
        rowSpinner.setAdapter(rowArray);

        ArrayAdapter<Integer> columnArray = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, columns);
        columnSpinner.setAdapter(columnArray);

        btnSelect.setOnClickListener(v -> {
            int row = 1;
            try {
                row = (int) rowSpinner.getSelectedItem();
            } catch (Exception e) {
                e.printStackTrace();
            }
            int column = 1;
            try {
                column = (int) columnSpinner.getSelectedItem();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String location = row + "," + column;
            int position = 0;
            if (row > 1) {
                position = ((row - 1) * 3 + column) - 1;
            } else {
                position = column - 1;
            }

            stringList.get(pos).setPosition(position);
            stringList.get(pos).setRow(row);
            stringList.get(pos).setColumn(column);
            stringList.get(pos).setLocation(location);

            notifyDataSetChanged();
            onItemSelect.onAppSelected(pos, 1);
            appCompatDialog.dismiss();
        });

        appCompatDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        appCompatDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appCompatDialog.getWindow().setGravity(Gravity.CENTER);
        appCompatDialog.show();
    }
}
