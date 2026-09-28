package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class BlockDomainListAdapter extends RecyclerView.Adapter<BlockDomainListAdapter.BlockDomainListHolder> {
    private Context context;
    private ArrayList<String> blockDomainsList;
    private BlockDomainInterface blockDomainInterface;

    public BlockDomainListAdapter(Context context, ArrayList<String> blockDomainsList, BlockDomainInterface blockDomainInterface) {
        this.context = context;
        this.blockDomainsList = blockDomainsList;
        this.blockDomainInterface = blockDomainInterface;
    }

    @NonNull
    @Override
    public BlockDomainListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_block_domain_website, parent, false);
        return new BlockDomainListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockDomainListAdapter.BlockDomainListHolder holder, int position) {
        holder.tvDomainName.setText(blockDomainsList.get(position));
        holder.ivRemoveDomain.setOnClickListener(view -> blockDomainInterface.onRemoveBlockDomain(position, blockDomainsList.get(position)));
    }

    @Override
    public int getItemCount() {
        return blockDomainsList.size();
    }

    class BlockDomainListHolder extends RecyclerView.ViewHolder {

        TextView tvDomainName;
        ImageView ivRemoveDomain;

        public BlockDomainListHolder(@NonNull View itemView) {
            super(itemView);
            tvDomainName = itemView.findViewById(R.id.tv_domain_name);
            ivRemoveDomain = itemView.findViewById(R.id.iv_remove_domain);
        }
    }

    public interface BlockDomainInterface {
        void onRemoveBlockDomain(int position, String blockDomain);
    }
}
