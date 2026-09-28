package com.tabnova.aidashboard.Adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tabnova.aidashboard.Activity.SecureWebView;
import com.tabnova.aidashboard.Extra.ApkInfoExtractor;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private List<AppModel> mData;

    public OnAppSelectListner onAppSelectListner;
    private View myView;
    private WindowManager wm;

    // ALERT DIALOG
    private AppCompatDialog alertDialogPwd;

    public RecyclerViewAdapter(Context mContext, List<AppModel> mData, OnAppSelectListner onAppSelectListner) {
        this.context = mContext;
        this.mData = mData;
        this.onAppSelectListner = onAppSelectListner;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(context);
        view = mInflater.inflate(R.layout.cardveiw_item_book, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context);

        if (mData.get(position).isSelected() && !TextUtils.isEmpty(mData.get(position).getPackageDetails())) {
            holder.llAppItem.setVisibility(View.VISIBLE);
            if (mData.get(position).isWebShortCut()) {
                holder.tv_book_title.setText(mData.get(position).getPackageDetails());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, SecureWebView.class);
                        intent.putExtra(Consts.web_url, mData.get(position).getAppInfo());
                        intent.putExtra(Consts.is_shortcut, 1);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
                holder.img_book_thumbnail.setImageResource(R.drawable.ic_domain);
            } else {
                final String ApplicationPackageName = (String) mData.get(position).getPackageDetails();
                if (mData.get(position).getPackageDetails().equals("com.dialer")) {
                    holder.tv_book_title.setText("Dialer");
                    holder.img_book_thumbnail.setImageResource(R.drawable.ic_dia);
                } else if (mData.get(position).getPackageDetails().equals("com.securewebview")) {
                    holder.tv_book_title.setText("Secure Webview");
                    holder.img_book_thumbnail.setImageResource(R.drawable.ic_sec_webview);
                } else {
                    holder.tv_book_title.setText(apkInfoExtractor.GetAppName(ApplicationPackageName));
                    Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);
                    holder.img_book_thumbnail.setImageDrawable(drawable);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAppSelectListner.OnSelectItem(position);
                    }
                });
            }
        } else {
            holder.llAppItem.setVisibility(View.GONE);
        }
    }

    public void notifyData() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_book_title;
        ImageView img_book_thumbnail;
        CardView cardView;
        LinearLayout llAppItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_book_title = (TextView) itemView.findViewById(R.id.book_title_id);
            img_book_thumbnail = (ImageView) itemView.findViewById(R.id.book_img_id);
            cardView = (CardView) itemView.findViewById(R.id.cardview_id);
            llAppItem = itemView.findViewById(R.id.ll_app_item);
        }
    }


    public interface OnAppSelectListner {
        void OnSelectItem(int pos);
    }


    public static void startApp(String packageName, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(packageName, getMainActivity(packageName, context)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMainActivity(String packageName, Context context) {
        try {
            Intent mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (mIntent != null) {
                if (mIntent.getComponent() != null) {
                    return mIntent.getComponent().getClassName();
                }
            }
        } catch (Exception e) {

        }
        return "";
    }
}