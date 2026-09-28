package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Model.ApplicationTimerModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class ApplicationTimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<ApplicationTimerModel> timerModels;


    public ApplicationTimerAdapter(Context context, ArrayList<ApplicationTimerModel> timerModels) {
        this.context = context;
        this.timerModels = timerModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_app_analytics, viewGroup, false);
        return new AppTimerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
        if (viewHolder instanceof  AppTimerHolder){
            AppTimerHolder appTimerHolder = (AppTimerHolder) viewHolder;

            ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);
            final String ApplicationPackageName = (String) timerModels.get(pos).getAppPackageName();
            if (timerModels.get(pos).getAppName().equals("com.dialer")){
                appTimerHolder.appName.setText("Phone");
                appTimerHolder.appPackageName.setText(timerModels.get(pos).getAppName());
                appTimerHolder.ivAppIcon.setImageResource(R.drawable.ic_dia);
            } else {
                String ApplicationLabelName = apkInfoExtractor.GetAppName(ApplicationPackageName);
                appTimerHolder.appName.setText(ApplicationLabelName);
                appTimerHolder.appPackageName.setText(ApplicationPackageName);
                Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);
                appTimerHolder.ivAppIcon.setImageDrawable(drawable);
            }

            appTimerHolder.date.setText(timerModels.get(pos).getDate());
            appTimerHolder.startTime.setText(timerModels.get(pos).getStartTime());
            appTimerHolder.endTime.setText(timerModels.get(pos).getEndTime());
        }
    }

    @Override
    public int getItemCount() {
        return timerModels.size();
    }

    public class AppTimerHolder extends RecyclerView.ViewHolder {

        ImageView ivAppIcon;
        TextView appName, appPackageName, date, startTime, endTime;

        public AppTimerHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
            appName = itemView.findViewById(R.id.Apk_Name);
            appPackageName = itemView.findViewById(R.id.Apk_Package_Name);
            date = itemView.findViewById(R.id.tx_date);
            startTime = itemView.findViewById(R.id.tx_start_time);
            endTime = itemView.findViewById(R.id.tx_end_time);
        }
    }
}
