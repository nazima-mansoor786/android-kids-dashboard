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

public class DeletePredefineFileFolderListAdapter extends RecyclerView.Adapter<DeletePredefineFileFolderListAdapter.DeletePredefineFileFolderListHolder> {

    private Context context;
    private ArrayList<SelectedFileFolderModel> buttonListModels;
    private OnDeletePreFileFolderListener onSelectListener;

    public DeletePredefineFileFolderListAdapter(Context context, ArrayList<SelectedFileFolderModel> buttonListModels, OnDeletePreFileFolderListener onSelectListener) {
        this.context = context;
        this.buttonListModels = buttonListModels;
        this.onSelectListener = onSelectListener;
    }

    @NonNull
    @Override
    public DeletePredefineFileFolderListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_uninstall_apps, viewGroup, false);
        return new DeletePredefineFileFolderListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeletePredefineFileFolderListHolder buttonListHolder, final int position) {

        buttonListHolder.txAppName.setText(buttonListModels.get(position).getFileStr());

        if (buttonListModels.get(position).isSelected()){
            buttonListHolder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
        } else {
            buttonListHolder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
        }

        if (buttonListModels.get(position).isFileDirectory()){
            buttonListHolder.ivAppIcon.setImageResource(R.drawable.ic_folder);
        }  else {
            buttonListHolder.ivAppIcon.setImageResource(R.drawable.ic_file);
        }

        buttonListHolder.ivSelectedApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonListModels.get(position).isSelected()){
                    buttonListModels.get(position).setSelected(false);
                    buttonListHolder.ivSelectedApp.setImageResource(R.drawable.check_box_selected_icon);
                } else {
                    buttonListModels.get(position).setSelected(true);
                    buttonListHolder.ivSelectedApp.setImageResource(R.drawable.check_box_unselected_icon);
                }
                //notifyDataSetChanged()
                onSelectListener.onDeletePreFileFolder(position, 0);
            }
        });

        buttonListHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectListener.onDeletePreFileFolder(position, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class DeletePredefineFileFolderListHolder extends RecyclerView.ViewHolder {

        // TEXT VIEW
        TextView txAppName;

        // IMAGE VIEW
        ImageView ivSelectedApp;
        ImageView ivAppIcon;

        DeletePredefineFileFolderListHolder(@NonNull View itemView) {
            super(itemView);

            txAppName = itemView.findViewById(R.id.tx_app_name);

            ivSelectedApp = itemView.findViewById(R.id.iv_select_selection);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
        }
    }

    public interface OnDeletePreFileFolderListener {
        void onDeletePreFileFolder(int position, int isOpenFrom);
    }
}
