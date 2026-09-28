
package com.tabnova.aidashboard.Extra;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;


public class ShowMessage {
    private static AlertDialog d;
    // private static SpotsDialog dialog;

    public static void showError(Context context, String message) {
        try {
            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(message);
            dialogBuilder1.setNeutralButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    d.dismiss();
                }
            });
            d = dialogBuilder1.create();
            // d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                    doneBtn.setAllCaps(false);
                }
            });
            d.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showSuccess(Context context, String message) {

        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
        dialogBuilder1.setMessage(message);
        dialogBuilder1.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.dismiss();
            }
        });
        d = dialogBuilder1.create();
        //  d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button doneBtn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                doneBtn.setAllCaps(false);
            }
        });
        d.show();
    }

    public static void showSuccessFinish(final Context context, String message) {

        try {
            AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);
            dialogBuilder1.setMessage(message);
            dialogBuilder1.setNeutralButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    d.dismiss();
                    ((Activity) context).finish();
                }
            });
            d = dialogBuilder1.create();
            //   d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            d.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


      /*  public static void showBasic(Context context, String message) {
        AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(context);

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View dialogView1 = inflater.inflate(R.layout.custom_dialog_success, null);
        dialogBuilder1.setView(dialogView1);
        //d.setContentView(R.layout.custom_dialog_error);
        TextView titleTv=(TextView) dialogView1.findViewById(R.id.title);
        TextView messageTv=(TextView) dialogView1.findViewById(R.id.message);
        Button confirm = (Button) dialogView1.findViewById(R.id.confirm);
        titleTv.setText("");
        messageTv.setText(message);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });
        d = dialogBuilder1.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        d.show();
    }*/


}

