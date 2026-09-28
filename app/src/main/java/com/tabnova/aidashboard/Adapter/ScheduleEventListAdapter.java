package com.tabnova.aidashboard.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Model.ScheduleEventModel;
import com.tabnova.aidashboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScheduleEventListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // CONTEXT
    Context context;

    // ARRAY LIST
    ArrayList<ScheduleEventModel> scheduleEventModels;

    public OnEventSelectListner onAppSelectListner;


    public ScheduleEventListAdapter(Context context, ArrayList<ScheduleEventModel> timerModels, OnEventSelectListner onEventSelectListner) {
        this.context = context;
        this.scheduleEventModels = timerModels;
        this.onAppSelectListner = onEventSelectListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_schedule_event, viewGroup, false);
        return new ScheduleEventViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int pos) {
        if (viewHolder instanceof ScheduleEventViewHolder) {
            ScheduleEventViewHolder scheduleEventViewHolder = (ScheduleEventViewHolder) viewHolder;
            scheduleEventViewHolder.txEventName.setText(scheduleEventModels.get(pos).getEventName());
            scheduleEventViewHolder.txEventTime.setText(scheduleEventModels.get(pos).getStartTime() + " - " + scheduleEventModels.get(pos).getEndTime());

            List<String> myList = new ArrayList<String>(Arrays.asList(scheduleEventModels.get(pos).getDay().split(",")));

            for (int i = 0; i < myList.size(); i++) {
                if (myList.get(i).contains("1")) {
                    scheduleEventViewHolder.ivSunday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("2")) {
                    scheduleEventViewHolder.ivMonday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("3")) {
                    scheduleEventViewHolder.ivTuesday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("4")) {
                    scheduleEventViewHolder.ivWednsday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("5")) {
                    scheduleEventViewHolder.ivThursday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("6")) {
                    scheduleEventViewHolder.ivFriday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else if (myList.get(i).contains("7")) {
                    scheduleEventViewHolder.ivSaturday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing));
                } else {
                    scheduleEventViewHolder.ivSunday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivMonday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivTuesday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivWednsday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivThursday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivFriday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                    scheduleEventViewHolder.ivSaturday.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_bg_drawable_listing_transparent));
                }


            }

            scheduleEventViewHolder.relDeleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scheduleEventModels.size() > 0)
                        onAppSelectListner.OnSelectEvent(pos, scheduleEventModels.get(pos).getGroupId(), 1);
                }
            });

            scheduleEventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scheduleEventModels.size() > 0)
                        onAppSelectListner.OnSelectEvent(pos, scheduleEventModels.get(pos).getGroupId(), 0);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return scheduleEventModels.size();
    }

    public class ScheduleEventViewHolder extends RecyclerView.ViewHolder {


        TextView txEventName, txEventTime;
        ImageView ivSunday, ivMonday, ivTuesday, ivWednsday, ivThursday, ivFriday, ivSaturday, ivDeleteEvent;
        RelativeLayout relDeleteEvent;


        public ScheduleEventViewHolder(@NonNull View view) {
            super(view);
            txEventName = view.findViewById(R.id.tx_event_name);
            txEventTime = view.findViewById(R.id.tx_event_time);
            ivSunday = view.findViewById(R.id.iv_sunday);
            ivMonday = view.findViewById(R.id.iv_monday);
            ivTuesday = view.findViewById(R.id.iv_tuesday);
            ivWednsday = view.findViewById(R.id.iv_wednsday);
            ivThursday = view.findViewById(R.id.iv_thursday);
            ivFriday = view.findViewById(R.id.iv_friday);
            ivSaturday = view.findViewById(R.id.iv_saturday);
            ivDeleteEvent = view.findViewById(R.id.iv_delete);
            relDeleteEvent = view.findViewById(R.id.rel_delete);
        }
    }

    public interface OnEventSelectListner {
        void OnSelectEvent(int pos, String groupId, int isDelete);
    }
}
