package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.PredefineFoldersModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class PreDefineListAdapter extends RecyclerView.Adapter<PreDefineListAdapter.PreDefinedListHolder> {
    private Context context;
    private ArrayList<PredefineFoldersModel> buttonListModels;
    private OnPreDefineSelectListener onButtonSelectListener;

    public PreDefineListAdapter(Context context, ArrayList<PredefineFoldersModel> buttonListModels, OnPreDefineSelectListener onButtonSelectListener) {
        this.context = context;
        this.buttonListModels = buttonListModels;
        this.onButtonSelectListener = onButtonSelectListener;
    }

    @NonNull
    @Override
    public PreDefinedListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_manager, parent, false);
        return new PreDefinedListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreDefinedListHolder buttonListHolder, final int position) {
        buttonListHolder.txButtonLabel.setText(buttonListModels.get(position).getFolderName());
        buttonListHolder.ivButton.setImageResource(buttonListModels.get(position).getDrawableId());

        buttonListHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonSelectListener.onPreDefineButtonSelect(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buttonListModels.size();
    }

    static class PreDefinedListHolder extends RecyclerView.ViewHolder {

        ImageView ivButton;
        TextView txButtonLabel;

        PreDefinedListHolder(@NonNull View itemView) {
            super(itemView);
            ivButton = itemView.findViewById(R.id.iv_icon);
            txButtonLabel = itemView.findViewById(R.id.tx_button_name);
        }
    }

    public interface OnPreDefineSelectListener {
        void onPreDefineButtonSelect(int position);
    }
}