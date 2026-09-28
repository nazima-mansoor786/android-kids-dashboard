package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.AddedWebShortcutModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class AddedWebShortcutsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<AddedWebShortcutModel> addedWebShortcutsModels;

    public OnShortcutsSelectListener onShortcutsSelectListner;


    public AddedWebShortcutsAdapter(Context context, ArrayList<AddedWebShortcutModel> timerModels, OnShortcutsSelectListener onShortcutsSelectListner) {
        this.context = context;
        this.addedWebShortcutsModels = timerModels;
        this.onShortcutsSelectListner = onShortcutsSelectListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_added_web_shortcuts, viewGroup, false);
        return new AppTimerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int pos) {
        if (viewHolder instanceof AppTimerHolder) {
            final AppTimerHolder appTimerHolder = (AppTimerHolder) viewHolder;

            appTimerHolder.txWebSiteName.setText(addedWebShortcutsModels.get(pos).getWebSiteName());
            appTimerHolder.txWebSiteUrl.setText(addedWebShortcutsModels.get(pos).getWebSiteUrl());

            if (addedWebShortcutsModels.get(pos).isSelected()) {
                appTimerHolder.cbSelectedWebSite.setChecked(true);
            } else {
                appTimerHolder.cbSelectedWebSite.setChecked(false);
            }

            //appTimerHolder.itemView.setOnClickListener(v -> onShortcutsSelectListner.OnSelectShortcut(pos, 0));

            appTimerHolder.ivDeleteShortcut.setOnClickListener(v -> onShortcutsSelectListner.OnSelectShortcut(pos, 1));

        }
    }

    @Override
    public int getItemCount() {
        return addedWebShortcutsModels.size();
    }

    public class AppTimerHolder extends RecyclerView.ViewHolder {

        ImageView ivDeleteShortcut;
        TextView txWebSiteName, txWebSiteUrl;
        CheckBox cbSelectedWebSite;

        public AppTimerHolder(@NonNull View itemView) {
            super(itemView);
            ivDeleteShortcut = itemView.findViewById(R.id.iv_delete_web_shortcut);
            txWebSiteName = itemView.findViewById(R.id.tx_website_name);
            txWebSiteUrl = itemView.findViewById(R.id.tx_website_url);
            cbSelectedWebSite = itemView.findViewById(R.id.cb_web_site);
        }
    }

    public interface OnShortcutsSelectListener {
        void OnSelectShortcut(int pos, int isDelete);
    }
}
