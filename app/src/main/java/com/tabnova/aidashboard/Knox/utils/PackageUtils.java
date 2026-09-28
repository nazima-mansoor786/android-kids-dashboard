package com.tabnova.aidashboard.Knox.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by on 29/08/16.
 */
public class PackageUtils {


    public static boolean LogToFile(String fcontent){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd_HH_mm_ss_SS");
            Date now = new Date();
            String fpath = "/sdcard/"+"samsungservice"+".txt";

            File file = new File(fpath);

            // If file does not exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(formatter.format(now) + " : " + fcontent +"\n");
            bw.close();

            Log.d("Suceess","Sucess");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static int getApkVersionCode(Context context, String apkPath) {
        try {
            final PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, 0);
            return info.versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getApkPackageName(Context context, String apkPath) {
        try {
            final PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, 0);
            return info.packageName;
        } catch (Exception e) {
            return "";
        }
    }

    public static int getVersionCode (Context context, String packageName){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static boolean isPackageExisted(Context context, String targetPackage){
        PackageManager pm= context.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
//2017-02-17 09:38:02 date format
    public static String getAppInstallDate(Context context, String packageName)  {
        try {
            if (packageName == null ||packageName.isEmpty() ){
                return "00-00-00 0:0:0";
            }
            long installTime = context.getPackageManager()
                    .getPackageInfo(packageName, 0)
                    .firstInstallTime
                    ;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            return sdf.format(new Date(installTime));
        } catch (Exception e) {

        }
        return "00-00-00 0:0:0";
    }

}
