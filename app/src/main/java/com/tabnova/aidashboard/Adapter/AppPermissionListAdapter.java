package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.AppPermissionModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class AppPermissionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<AppPermissionModel> appPermissionModels;

    public OnPermissionSelectListener onPermissionSelectListener;


    public AppPermissionListAdapter(Context context, ArrayList<AppPermissionModel> timerModels, OnPermissionSelectListener onPermissionSelectListener ) {
        this.context = context;
        this.appPermissionModels = timerModels;
        this.onPermissionSelectListener = onPermissionSelectListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_app_permission_list, viewGroup, false);
        return new AppPermissionListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int pos) {
        if (viewHolder instanceof AppPermissionListHolder){
            final AppPermissionListHolder appPermissionListHolder = (AppPermissionListHolder) viewHolder;

            appPermissionListHolder.txLabel.setText(appPermissionModels.get(pos).getPermissionName());

            if (appPermissionModels.get(pos).isPermissionGiven()){
               appPermissionListHolder.ivPermissionError.setImageResource(R.drawable.ic_check_mark);
            } else {
                appPermissionListHolder.ivPermissionError.setImageResource(R.drawable.ic_permission_error);
            }

            appPermissionListHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPermissionSelectListener.OnSelectPermission(pos, appPermissionModels.get(pos).isPermissionGiven());
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return appPermissionModels.size();
    }

    public class AppPermissionListHolder extends RecyclerView.ViewHolder {

        // CARD VIEW
        CardView cvPermission;

        // IMAGE VIEW
        ImageView ivPermissionError;

        // TEXT VIEW
        TextView txLabel;

        public AppPermissionListHolder(@NonNull View itemView) {
            super(itemView);
            cvPermission = itemView.findViewById(R.id.cv_permission);
            txLabel = itemView.findViewById(R.id.tx_label);
            ivPermissionError = itemView.findViewById(R.id.iv_permission_error);
        }
    }

    public interface OnPermissionSelectListener{
        void OnSelectPermission(int pos, boolean isPermissionGiven);
    }
}
