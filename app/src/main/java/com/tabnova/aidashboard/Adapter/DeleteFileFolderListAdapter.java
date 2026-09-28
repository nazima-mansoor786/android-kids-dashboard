package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.SelectedFileFolderModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class DeleteFileFolderListAdapter extends RecyclerView.Adapter<DeleteFileFolderListAdapter.DeleteFileFolderListHolder> {
    private Context context;
    private ArrayList<SelectedFileFolderModel> fileFoldersList;
    private OnDeleteFileFolderListener onDeleteFileFolderListener;

    public DeleteFileFolderListAdapter(Context context, ArrayList<SelectedFileFolderModel> fileFoldersList, OnDeleteFileFolderListener onDeleteFileFolderListener) {
        this.context = context;
        this.fileFoldersList = fileFoldersList;
        this.onDeleteFileFolderListener = onDeleteFileFolderListener;
    }

    @NonNull
    @Override
    public DeleteFileFolderListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_uninstall_apps, viewGroup, false);
        return new DeleteFileFolderListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeleteFileFolderListHolder holder, final int position) {
        holder.txAppName.setText(fileFoldersList.get(position).getFileStr());

        if (fileFoldersList.get(position).isSelected()) {
            holder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
        } else {
            holder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
        }


        if (fileFoldersList.get(position).isFileDirectory()) {
            holder.ivAppIcon.setImageResource(R.drawable.ic_folder);
        } else {
            holder.ivAppIcon.setImageResource(R.drawable.ic_file);
        }

        holder.ivSelectedApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.ivSelectedApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileFoldersList.get(position).isSelected()) {
                    fileFoldersList.get(position).setSelected(false);
                    holder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
                } else {
                    fileFoldersList.get(position).setSelected(true);
                    holder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
                }
                notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteFileFolderListener.onDeleteFileFolder(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileFoldersList.size();
    }

    static class DeleteFileFolderListHolder extends RecyclerView.ViewHolder {
        // TEXT VIEW
        TextView txAppName;

        // IMAGE VIEW
        ImageView ivSelectedApp;
        ImageView ivAppIcon;

        DeleteFileFolderListHolder(@NonNull View itemView) {
            super(itemView);
            txAppName = itemView.findViewById(R.id.tx_app_name);

            ivSelectedApp = itemView.findViewById(R.id.iv_select_selection);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
        }
    }

    public interface OnDeleteFileFolderListener {
        void onDeleteFileFolder(int position);
    }
}
