package com.tabnova.aidashboard.Knox.startup;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.rsb.vpnpoc.VPNService;
import com.tabnova.aidashboard.Activity.BlockDomainActivity;
import com.tabnova.aidashboard.Activity.MainActivity;

import java.io.File;

/**
 * Created by osp on 24/01/15.
 */
public class CommonUtility {

    private static final String TAG = "OSP";
    public static final String OSPPREFERENCES = "OspPrefs";
    public static final String KIOSKPIN = "KioskPin";
    public static final String OSPLICENSE = "licenseKey";
    public static ProgressDialog progressDialog;
    public Context context;

    public CommonUtility(Context context) {
        this.context = context;
    }

    public static void saveBooleanInSP(String key, boolean value, Context ctx) {
        SharedPreferences preferences = ctx.getApplicationContext().getSharedPreferences(OSPPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }//savePWDInSP()

    public void downloadFile() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading File...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
        String url = "https://www.tabnova.com/block.list";
        //String fileName = url.substring(url.lastIndexOf('/') + 1);
        Uri downloadUri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDescription("Downloading a file");
        downloadManager.enqueue(request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("File Downloading...")
                .setDescription("File Downloaded")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, "Dashboard", "block_list.txt"));
    }

    public static BroadcastReceiver fileDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(
                        intent.getLongExtra(
                                DownloadManager.EXTRA_DOWNLOAD_ID,
                                0
                        )
                );
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = manager.query(query);
                if (cursor.moveToFirst()) {
                    if (cursor.getCount() > 0) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        String downloadFilePath = "";
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            String downloadFileLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            if (downloadFileLocalUri != null) {
                                File mFile = new File(Uri.parse(downloadFileLocalUri).getPath());
                                downloadFilePath = mFile.getAbsolutePath();
                                //String filename = downloadFilePath.substring(downloadFilePath.lastIndexOf("/") + 1);
                                if (mFile.exists()) {
                                    Toast.makeText(context, "Block domains list downloaded.", Toast.LENGTH_SHORT).show();
                                    if (VPNService.Companion.getInstance() == null) {
                                        //Toast.makeText(getBaseContext(), "Service Already stopped", Toast.LENGTH_SHORT).show();
                                    } else {
                                        try {
                                            VPNService.Companion.stopService();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (context != null && context instanceof MainActivity) {
                                        ((MainActivity) context).startVPN();
                                    }
                                }
                            }
                        }
                    }
                }
                cursor.close();
            }

            try {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}