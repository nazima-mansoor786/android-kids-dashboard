package com.tabnova.aidashboard.Knox.startup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tabnova.aidashboard.R;


public class AlertDialogManager {
	/**
	 * Function to display simple Alert Dialog
	 * 
	 * @param context
	 *            - application context
	 * @param title
	 *            - alert dialog title
	 * @param message
	 *            - alert message
	 * @param status
	 *            - success/failure (used to set icon) - pass null if you don't
	 *            want icon
	 * */
	@SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message, Boolean status) {

		View customView =  LayoutInflater.from(context).inflate(R.layout.custom_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(customView);
		TextView tv = (TextView) customView.findViewById(R.id.textView_header);
		tv.setText(title);
		tv = (TextView) customView.findViewById(R.id.TextView_error_msg);
		tv.setText(message);

		builder.setPositiveButton(R.string.alert_close, null);

		builder.create().show();

	}

	public void showAlertPassword(Context context, String title, String message, Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		// Setting Dialog Title
		alertDialog.setTitle(title);
		// Setting Dialog Message
		alertDialog.setMessage(message);
		if (status != null)
			// Setting alert dialog icon
			alertDialog.setIcon((status) ? R.drawable.success  : R.drawable.fail);
		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}
}
