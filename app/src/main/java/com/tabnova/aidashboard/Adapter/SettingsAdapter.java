package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Model.SettingsModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<SettingsModel> settingsModels;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogEnable;


    public OnSettingSelectListner onSettingSelectListner;


    public SettingsAdapter(Context context, ArrayList<SettingsModel> settingsModels, OnSettingSelectListner onSettingSelectListner) {
        this.context = context;
        this.settingsModels = settingsModels;
        this.onSettingSelectListner = onSettingSelectListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_settings, viewGroup, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof SettingsViewHolder) {
            final SettingsViewHolder settingsViewHolder = (SettingsViewHolder) viewHolder;
            settingsViewHolder.txSettingName.setText(settingsModels.get(i).getSettingName());
           // settingsViewHolder.ivIcon.setImageResource(settingsModels.get(i).getDrawableId());

            settingsViewHolder.llDynamic.removeAllViews();
            RecyclerView recyclerView = new RecyclerView(context);
            RecyclerView.LayoutParams layoutParams = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            recyclerView.setLayoutParams(layoutParams);
            int currentOrientation = context.getResources().getConfiguration().orientation;
            if (!CustomDashboardApplication.isTablet(context)){
                recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, 6));
            }
            SettingsAppsAdapter settingsAppsAdapter = new SettingsAppsAdapter(context, settingsModels.get(i).getSettingsAppsModels(), (SettingsAppsAdapter.OnSettingSelectListner) onSettingSelectListner, i);
            recyclerView.setAdapter(settingsAppsAdapter);
            settingsViewHolder.llDynamic.addView(recyclerView);

           /* for (int j = 0; j < settingsModels.get(i).getSettingsAppsModels().size(); j++){
                final View view = LayoutInflater.from(context).inflate(R.layout.item_settings_dynamic, settingsViewHolder.llDynamic, false);
                TextView txSettingName = view.findViewById(R.id.tx_setting);
                ImageView ivIcon = view.findViewById(R.id.iv_icon);

                txSettingName.setText(settingsModels.get(i).getSettingsAppsModels().get(j).getSettingName());
                ivIcon.setImageResource(settingsModels.get(i).getSettingsAppsModels().get(j).getDrawableId());

                settingsViewHolder.llDynamic.addView(view);

            }*/


          /*  if (i == 5) {
                settingsViewHolder.faceRecSwitch.setVisibility(View.VISIBLE);
                if (CustomDashboardApplication.getHelpEnabled(context, Consts.IS_HELP_ENABLED) == 1) {
                    settingsViewHolder.faceRecSwitch.setChecked(true);
                } else {
                    settingsViewHolder.faceRecSwitch.setChecked(false);
                }
            } else if (i == 6) {
                settingsViewHolder.faceRecSwitch.setVisibility(View.VISIBLE);
                if (CustomDashboardApplication.getInt(context, Consts.PASSWORD_AT_LAUNCH) == 1) {
                    settingsViewHolder.faceRecSwitch.setChecked(true);
                } else {
                    settingsViewHolder.faceRecSwitch.setChecked(false);
                }
            } else if (i == 7) {
                settingsViewHolder.faceRecSwitch.setVisibility(View.VISIBLE);
                if (CustomDashboardApplication.getShortCut(context, Consts.SHORTCUT_DASHBOARD) == 1) {
                    settingsViewHolder.faceRecSwitch.setChecked(true);
                } else {
                    settingsViewHolder.faceRecSwitch.setChecked(false);
                }
            } else if (i == 10) {
                settingsViewHolder.faceRecSwitch.setVisibility(View.VISIBLE);
                if (CustomDashboardApplication.getAllowCalls(context, Consts.ALLOWCALLS) == 1) {
                    settingsViewHolder.faceRecSwitch.setChecked(true);
                } else {
                    settingsViewHolder.faceRecSwitch.setChecked(false);
                }
            } else {
                settingsViewHolder.faceRecSwitch.setVisibility(View.GONE);
            }
*/

          /*  settingsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (i == 5){
                        settingsViewHolder.faceRecSwitch.performClick();
                    } else if (i == 6){
                        settingsViewHolder.faceRecSwitch.performClick();
                    } else if (i == 7){
                        settingsViewHolder.faceRecSwitch.performClick();
                    } else if (i == 10) {
                        settingsViewHolder.faceRecSwitch.performClick();
                    } else {
                        onSettingSelectListner.OnSelectItem(i);
                    }
                }
            });
*/
/*
            settingsViewHolder.faceRecSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {
                        if (i == 6){
                            CustomDashboardApplication.setHelpEnabled(context, Consts.IS_HELP_ENABLED, 1);
                        } else if (i == 7){
                            if (CustomDashboardApplication.getInt(context, Consts.IS_PWD) == 1) {
                                CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 1);
                            } else {
                                settingsViewHolder.faceRecSwitch.setChecked(false);
                                showPasswordDialog();
                            }
                        } else if (i == 8){
                            CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 1);
                        } else if (i == 11){
                            CustomDashboardApplication.setAllowCalls(context, Consts.ALLOWCALLS, 1);
                        }
                    } else {
                        if (i == 6){
                            CustomDashboardApplication.setHelpEnabled(context, Consts.IS_HELP_ENABLED, 0);
                        } else if (i == 7){
                            CustomDashboardApplication.setInt(context, Consts.PASSWORD_AT_LAUNCH, 0);
                        } else if (i == 8){
                            CustomDashboardApplication.setShortCut(context, Consts.SHORTCUT_DASHBOARD, 0);
                        } else if (i == 11){
                            CustomDashboardApplication.setAllowCalls(context, Consts.ALLOWCALLS, 0);
                        }

                    }
                }
            });
*/
        }
    }

    @Override
    public int getItemCount() {
        return settingsModels.size();
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder {

        // IMAGE VIEW
        private ImageView ivIcon;

        // TEXT VIEW
        private TextView txSettingName;

        // SWITCH
        private Switch faceRecSwitch;

        // LINEAR LAYOUT
        private LinearLayout llDynamic;

        // GRID LAYOUT MANAGER
        private GridLayoutManager gridLayoutManager;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            txSettingName = itemView.findViewById(R.id.tx_setting_name);
            ivIcon = itemView.findViewById(R.id.iv_icon);
//            faceRecSwitch = itemView.findViewById(R.id.face_rec_switch);
            llDynamic = itemView.findViewById(R.id.ll_dynamic);

        }
    }

    public interface OnSettingSelectListner{
        void OnSelectItem(int pos);
    }


    // SHOW ENTER PASSWORD DIALOG
    void showPasswordDialog() {
        alertDialogEnable = new AppCompatDialog(context);
        alertDialogEnable.setContentView(R.layout.enable_pwd_pop_up);

        final EditText edNewPassword = alertDialogEnable.findViewById(R.id.ed_new_password);
        Button btnDone = alertDialogEnable.findViewById(R.id.done_btn);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogEnable.dismiss();
            }
        });
        alertDialogEnable.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogEnable.show();
    }
}