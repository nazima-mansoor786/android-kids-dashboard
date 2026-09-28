package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Knox.startup.SuperLockState;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.util.List;

public class DisableAppsAdapter extends RecyclerView.Adapter<DisableAppsAdapter.DisableAppsHolder> {

    private Context context;
    private List<AppModel> appsList;
    private DisableAppsInterface disableAppsInterface;

    public DisableAppsAdapter(Context context, List<AppModel> appsList, DisableAppsInterface disableAppsInterface) {
        this.context = context;
        this.appsList = appsList;
        this.disableAppsInterface = disableAppsInterface;
    }

    @NonNull
    @Override
    public DisableAppsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_disable_apps, parent, false);
        return new DisableAppsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisableAppsAdapter.DisableAppsHolder holder, int position) {
        AppModel appModel = appsList.get(position);
        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

        final String applicationPackageName = appModel.getPackageDetails();
        if (appModel.getPackageDetails().equals("com.dialer")) {
            holder.tvAppName.setText("Phone");
            holder.ivAppIcon.setImageResource(R.drawable.ic_dia);
        } else {
            try {
                String applicationLabelName = apkInfoExtractor.GetAppName(applicationPackageName);
                holder.tvAppName.setText(applicationLabelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Drawable drawable = apkInfoExtractor.getAppIconByPackageName(applicationPackageName);
                holder.ivAppIcon.setImageDrawable(drawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.tvPackageName.setText(applicationPackageName);

        if (appModel.isLocked()) {
            holder.ivDisable.setImageResource(R.drawable.ic_locked_new);
        } else {
            holder.ivDisable.setImageResource(R.drawable.ic_unlocked_new);
        }

        holder.ivDisable.setOnClickListener(view -> {
            disableAppsInterface.onDisableApp(position, appModel, !appModel.isLocked());
        });
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    class DisableAppsHolder extends RecyclerView.ViewHolder {
        ImageView ivDisable;
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvPackageName;

        public DisableAppsHolder(@NonNull View itemView) {
            super(itemView);
            ivDisable = itemView.findViewById(R.id.iv_disable);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvPackageName = itemView.findViewById(R.id.tv_app_package_name);
        }
    }

    public interface DisableAppsInterface {
        void onDisableApp(int position, AppModel appModel, boolean isDisable);
    }
}
