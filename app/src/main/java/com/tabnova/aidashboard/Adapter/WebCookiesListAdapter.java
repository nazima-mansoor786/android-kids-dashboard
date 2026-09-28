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

public class WebCookiesListAdapter extends RecyclerView.Adapter<WebCookiesListAdapter.WebCookiesListHolder> {
    private Context context;
    private ArrayList<AppModel> appsModels;
    private OnWebCookieSelectListener webCookieSelectListener;

    public WebCookiesListAdapter(Context context, ArrayList<AppModel> appsModels, OnWebCookieSelectListener webCookieSelectListener) {
        this.context = context;
        this.appsModels = appsModels;
        this.webCookieSelectListener = webCookieSelectListener;
    }

    @NonNull
    @Override
    public WebCookiesListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_uninstall_apps, viewGroup, false);
        return new WebCookiesListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WebCookiesListHolder viewHolder, final int position) {
        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

        if (position == 0) {
            viewHolder.txAppName.setText(appsModels.get(position).getAppInfo());
            viewHolder.ivAppIcon.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.samsung_internet_logo)
            );
        } else if (position == appsModels.size() - 2) {
            viewHolder.txAppName.setText(appsModels.get(position).getAppInfo());
            viewHolder.ivAppIcon.setImageDrawable(
                    context.getResources().getDrawable(R.drawable.fire_fox_logo)
            );
        } else {
            String applicationPackageName = appsModels.get(position).getPackageDetails();
            String applicationLabelName = apkInfoExtractor.GetAppName(applicationPackageName);
            viewHolder.txAppName.setText(applicationLabelName);
            Drawable drawable = apkInfoExtractor.getAppIconByPackageName(applicationPackageName);
            viewHolder.ivAppIcon.setImageDrawable(drawable);
        }

        if (appsModels.get(position).isSelected()) {
            viewHolder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
        } else {
            viewHolder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
        }

        viewHolder.ivSelectedApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.performClick();
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appsModels.get(position).setSelected(!appsModels.get(position).isSelected());
                webCookieSelectListener.onSelectWebCookie(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appsModels.size();
    }

    static class WebCookiesListHolder extends RecyclerView.ViewHolder {
        // TEXT VIEW
        TextView txAppName;

        // IMAGE VIEW
        ImageView ivSelectedApp;
        ImageView ivAppIcon;

        WebCookiesListHolder(@NonNull View itemView) {
            super(itemView);
            txAppName = itemView.findViewById(R.id.tx_app_name);

            ivSelectedApp = itemView.findViewById(R.id.iv_select_selection);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
        }
    }

    public interface OnWebCookieSelectListener {
        void onSelectWebCookie(int position);
    }
}
