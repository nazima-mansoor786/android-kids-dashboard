package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.FileModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class FileModelListAdapter extends RecyclerView.Adapter<FileModelListAdapter.FileModelListHolder> {

    private Context context;
    private ArrayList<FileModel> buttonListModels;
    private OnSelectListener onSelectListener;

    public FileModelListAdapter(Context context, ArrayList<FileModel> buttonListModels, OnSelectListener onSelectListener) {
        this.context = context;
        this.buttonListModels = buttonListModels;
        this.onSelectListener = onSelectListener;
    }

    @NonNull
    @Override
    public FileModelListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_uninstall_apps, parent, false);
        return new FileModelListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileModelListHolder buttonListHolder, final int position) {
        buttonListHolder.txButtonLabel.setText(buttonListModels.get(position).getFileName());

        if (buttonListModels.get(position).isSelected()) {
            buttonListHolder.ivSelection.setImageResource(R.drawable.check_box_selected_icon);
        } else {
            buttonListHolder.ivSelection.setImageResource(R.drawable.check_box_unselected_icon);
        }

        if (buttonListModels.get(position).isFileDirectory()) {
            buttonListHolder.ivButton.setImageResource(R.drawable.ic_folder);
        } else {
            buttonListHolder.ivButton.setImageResource(R.drawable.ic_file);
        }

        buttonListHolder.ivSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonListModels.get(position).isSelected()) {
                    buttonListModels.get(position).setSelected(false);
                    buttonListHolder.ivSelection.setImageResource(R.drawable.check_box_selected_icon);
                } else {
                    buttonListModels.get(position).setSelected(true);
                    buttonListHolder.ivSelection.setImageResource(R.drawable.check_box_unselected_icon);
                }
                notifyDataSetChanged();
            }
        });

        buttonListHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectListener.onSelect(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buttonListModels.size();
    }

    static class FileModelListHolder extends RecyclerView.ViewHolder {

        // TEXT VIEW
        TextView txButtonLabel;

        // IMAGE VIEW
        ImageView ivSelection;
        ImageView ivButton;

        FileModelListHolder(@NonNull View itemView) {
            super(itemView);

            txButtonLabel = itemView.findViewById(R.id.tx_app_name);

            ivSelection = itemView.findViewById(R.id.iv_select_selection);
            ivButton = itemView.findViewById(R.id.iv_app_icon);
        }
    }

    public interface OnSelectListener {
        void onSelect(int position);
    }
}
