package com.tabnova.aidashboard.Extra;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.tabnova.aidashboard.R;


/**
 * DialogLoader will be used to show and hide Dialog with ProgressBar
 **/

public class DialogLoader {
    private Context context;
    private AlertDialog alertDialog;
    private AlertDialog.Builder dialog;
    private LayoutInflater layoutInflater;

    public DialogLoader(Context context) {
        try {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);

            initDialog();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public DialogLoader(Context context, int Type) {
        try {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);

            initWithMessageDialog();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initDialog() {
        dialog = new AlertDialog.Builder(context);
        View dialogView = layoutInflater.inflate(R.layout.layout_progress_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        ProgressBar dialog_progressBar = (ProgressBar) dialogView.findViewById(R.id.dialog_progressBar);
        dialog_progressBar.setIndeterminate(true);

        alertDialog = dialog.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initWithMessageDialog() {
        dialog = new AlertDialog.Builder(context);
        View dialogView = layoutInflater.inflate(R.layout.layout_progress_message_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        ProgressBar dialog_progressBar = (ProgressBar) dialogView.findViewById(R.id.dialog_progressBar);
        dialog_progressBar.setIndeterminate(true);

        alertDialog = dialog.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    public void showProgressDialog() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    public void hideProgressDialog() {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

