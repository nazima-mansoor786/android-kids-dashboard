package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.SettingsAppsModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class SettingsAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<SettingsAppsModel> settingsModels;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogEnable;

    // INTEGER
    private int currentType;

    public OnSettingSelectListner onSettingSelectListner;


    public SettingsAppsAdapter(Context context, ArrayList<SettingsAppsModel> settingsModels, OnSettingSelectListner onSettingSelectListner, int type) {
        this.context = context;
        this.settingsModels = settingsModels;
        this.onSettingSelectListner = onSettingSelectListner;
        this.currentType = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_settings_dynamic, viewGroup, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof SettingsViewHolder) {
            final SettingsViewHolder settingsViewHolder = (SettingsViewHolder) viewHolder;
            settingsViewHolder.txSettingName.setText(settingsModels.get(i).getSettingName());
            settingsViewHolder.ivIcon.setImageResource(settingsModels.get(i).getDrawableId());


            settingsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSettingSelectListner.OnSelectItem(i, currentType);
                }
            });

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


        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            txSettingName = itemView.findViewById(R.id.tx_setting);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }

    public interface OnSettingSelectListner {
        void OnSelectItem(int pos, int type);
    }

}
