package com.tabnova.aidashboard.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tabnova.aidashboard.Model.HistoryUrlModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;

public class HistoryListAdapter extends ArrayAdapter<HistoryUrlModel> {

    // CONTEXT
    public Context context;

    // ARRAY LIST
    public ArrayList<HistoryUrlModel> stringList;
    public ArrayList<HistoryUrlModel> stringFilterList;

    public HistoryListAdapter(Context context, ArrayList<HistoryUrlModel> list){
        super(context, 0, list);
        this.context = context;
        this.stringList = list;

    }

   /* public HistoryListAdapter(Context context, ArrayList<HistoryUrlModel> stringList, OnUrlSelectListner onSettingSelectListner) {
        super(context, 0, list);
        this.context = context;
        this.stringList = stringList;
        this.onSettingSelectListner = onSettingSelectListner;
    }*/

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.result_text_view, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.tx_label);
        HistoryUrlModel historyUrlModel = getItem(position);

        if (historyUrlModel != null) {
            textViewName.setText(historyUrlModel.getUrl());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                if (charSequence != null) {
                    String charStr = charSequence.toString().trim();
                    FilterResults filterResults = new FilterResults();
                    ArrayList<HistoryUrlModel> historyUrlModels = new ArrayList<>();

                    if (charStr.isEmpty() && charStr.length() == 0) {
                        historyUrlModels.addAll(stringList);
                    } else {
                        for (HistoryUrlModel model : stringList) {
                            if (model.getUrl().trim().contains(charStr.trim().toLowerCase())) {
                                historyUrlModels.add(model);
                            }
                        }
                        //stringFilterList = appModels;
                        filterResults.values = historyUrlModels;
                        filterResults.count = historyUrlModels.size();
                    }

                    return filterResults;
                } else {
                    return null;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.values != null) {
                    clear();
                    addAll((ArrayList<HistoryUrlModel>) results.values);
                    //stringList = (ArrayList<HistoryUrlModel>) results.values;
                    notifyDataSetChanged();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((HistoryUrlModel) resultValue).getUrl();
            }
        };
    }


}
