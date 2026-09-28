package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.CallLogsModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class CallLogsListAdapter extends RecyclerView.Adapter<CallLogsListAdapter.CallLogListHolder> {

    // CONTEXT
    public Context context;
    // ARRAY LIST
    private ArrayList<CallLogsModel> callLogsModels;
    // APP SELECT LISTENER
    private OnCallLogSelectListener onAppSelect;

    public CallLogsListAdapter(Context context, ArrayList<CallLogsModel> callLogsModels, OnCallLogSelectListener onAppSelect) {
        this.context = context;
        this.callLogsModels = callLogsModels;
        this.onAppSelect = onAppSelect;
    }

    @NonNull
    @Override
    public CallLogListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_call_logs, viewGroup, false);
        return new CallLogListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CallLogListHolder viewHolder, final int position) {
        viewHolder.txName.setText(callLogsModels.get(position).getName());
        viewHolder.txDuration.setText(callLogsModels.get(position).getDuration());
        viewHolder.txDate.setText(callLogsModels.get(position).getDate());

        String type = callLogsModels.get(position).getType();
        if (type.equalsIgnoreCase("Incoming")) {
            viewHolder.ivAppIcon.setImageResource(R.drawable.ic_incoming_calls);
        } else if (type.equalsIgnoreCase("Outgoing")) {
            viewHolder.ivAppIcon.setImageResource(R.drawable.ic_dialed_calls);
        } else if (type.equalsIgnoreCase("Missed")) {
            viewHolder.ivAppIcon.setImageResource(R.drawable.ic_missed_calls);
        } else if (type.equalsIgnoreCase("Rejected")) {
            viewHolder.ivAppIcon.setImageResource(R.drawable.ic_blocked_calls);
        } else if (type.equalsIgnoreCase("Blocked")) {
            viewHolder.ivAppIcon.setImageResource(R.drawable.ic_blocked_calls);
        }


        if (callLogsModels.get(position).isSelected()) {
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
                callLogsModels.get(position).setSelected(!callLogsModels.get(position).isSelected());
                onAppSelect.onSelectCallLog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return callLogsModels.size();
    }

    static class CallLogListHolder extends RecyclerView.ViewHolder {
        // TEXT VIEW
        TextView txName;
        TextView txDuration;
        TextView txDate;

        // IMAGE VIEW
        ImageView ivSelectedApp;
        ImageView ivAppIcon;

        CallLogListHolder(@NonNull View itemView) {
            super(itemView);
            // TEXT VIEW
            txName = itemView.findViewById(R.id.tx_app_name);
            txDuration = itemView.findViewById(R.id.tx_call_duration);
            txDate = itemView.findViewById(R.id.tx_date);

            // IMAGE VIEW
            ivSelectedApp = itemView.findViewById(R.id.iv_select_selection);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
        }
    }

    public interface OnCallLogSelectListener {
        void onSelectCallLog(int pos);
    }
}
