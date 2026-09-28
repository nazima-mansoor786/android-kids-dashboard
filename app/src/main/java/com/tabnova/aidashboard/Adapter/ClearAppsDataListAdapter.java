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
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class ClearAppsDataListAdapter  extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    public Context context;
    // ARRAY LIST
    public ArrayList<AppModel> appsModels;
    // APP SELECT LISTENER
    public OnClearAppSelectListener onAppSelect;

    public ClearAppsDataListAdapter(Context context, ArrayList<AppModel> appsModels, OnClearAppSelectListener onAppSelect) {
        this.context = context;
        this.appsModels = appsModels;
        this.onAppSelect = onAppSelect;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_uninstall_apps, viewGroup, false);
        return new appsListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof appsListHolder) {
            final appsListHolder viewHolder = (appsListHolder) holder;

            ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

            String  applicationPackageName = appsModels.get(position).getPackageDetails();
            String applicationLabelName = apkInfoExtractor.GetAppName(applicationPackageName);
            viewHolder.txAppName.setText(applicationLabelName);
            Drawable drawable = apkInfoExtractor.getAppIconByPackageName(applicationPackageName);
            viewHolder.ivAppIcon.setImageDrawable(drawable);

            if (appsModels.get(position).isSelected()){
                viewHolder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
            } else {
                viewHolder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
            }

            viewHolder.ivSelectedApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.itemView.performClick();
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (appsModels.get(position).isSelected()){
                        appsModels.get(position).setSelected(false);
                    } else {
                        appsModels.get(position).setSelected(true);
                    }
                    onAppSelect.onSelectClearApp(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return appsModels.size();
    }

    public interface OnClearAppSelectListener{
        void onSelectClearApp(int pos);
    }

    class appsListHolder extends RecyclerView.ViewHolder{

        // TEXT VIEW
        TextView txAppName;

        // IMAGE VIEW
        ImageView ivSelectedApp;
        ImageView ivAppIcon;

        appsListHolder(@NonNull View itemView) {
            super(itemView);

            txAppName = itemView.findViewById(R.id.tx_app_name);

            ivSelectedApp = itemView.findViewById(R.id.iv_select_selection);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
        }

    }


}
