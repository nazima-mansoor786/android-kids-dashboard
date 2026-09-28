package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.ScheduleDaysModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<ScheduleDaysModel> settingsModels;

    // APP COMPAT DIALOG
    private AppCompatDialog alertDialogEnable;

    // INTEGER
    private int currentType;

    public OnScheduleSelectListner onSettingSelectListner;


    public ScheduleAdapter(Context context, ArrayList<ScheduleDaysModel> settingsModels, OnScheduleSelectListner onSettingSelectListner) {
        this.context = context;
        this.settingsModels = settingsModels;
        this.onSettingSelectListner = onSettingSelectListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_schedule_days, viewGroup, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof SettingsViewHolder) {
            final SettingsViewHolder settingsViewHolder = (SettingsViewHolder) viewHolder;
            settingsViewHolder.txDayName.setText(settingsModels.get(position).getDayName().substring(0, 1).toUpperCase());


            if (settingsModels.get(position).isSelected()){
                settingsViewHolder.ivOverLay.setVisibility(View.VISIBLE);
            } else {
                settingsViewHolder.ivOverLay.setVisibility(View.GONE);
            }

            settingsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  /*  if (settingsModels.get(position).isSelected()){
                        settingsModels.get(position).setSelected(true);
                        settingsViewHolder.ivOverLay.setVisibility(View.VISIBLE);
                    } else {
                        settingsModels.get(position).setSelected(false);
                        settingsViewHolder.ivOverLay.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();*/
                  onSettingSelectListner.OnSelectDay(position, !settingsModels.get(position).isSelected());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return settingsModels.size();
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder {

        // IMAGE VIEW
        private ImageView ivOverLay;

        // TEXT VIEW
        private TextView txDayName;

        // RELATIVE LAYOUT
        private RelativeLayout relDay;


        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOverLay = itemView.findViewById(R.id.iv_over_lay);
            txDayName = itemView.findViewById(R.id.tx_day_name);
            relDay = itemView.findViewById(R.id.rel_day);
        }
    }

    public interface OnScheduleSelectListner{
        void OnSelectDay(int pos, boolean type);
    }

}
