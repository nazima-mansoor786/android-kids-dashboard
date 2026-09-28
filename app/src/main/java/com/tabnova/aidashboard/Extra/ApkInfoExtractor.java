package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Model.AppCategoryModel;
import com.tabnova.aidashboard.Model.AppModel;
import com.tabnova.aidashboard.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApkInfoExtractor {
    Context context1;
    ArrayList<AppModel> apkPackageCategories = new ArrayList<>();
    ArrayList<String> packageNameList = new ArrayList<>();

    public ApkInfoExtractor(Context context2) {
        context1 = context2;
    }

    public ArrayList<AppModel> GetAllInstalledApkInfo() {

        ArrayList<AppModel> ApkPackageName = new ArrayList<>();
        packageNameList = new ArrayList<>();

        ArrayList<AppCategoryModel> appCategoryModels = new ArrayList<>();

        String ser = SerializeObject.ReadSettings(context1, Consts.appCategoryFile);
        if (ser != null && !ser.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                appCategoryModels = (ArrayList<AppCategoryModel>) obj;
            }
        }

        ArrayList<AppModel> selectedAppList = new ArrayList<>();
        String ser1 = SerializeObject.ReadSettings(context1, Consts.blocksAppsFile);
        if (ser1 != null && !ser1.equalsIgnoreCase("")) {
            Object obj = SerializeObject.stringToObject(ser1);
            // Then cast it to your object and
            if (obj instanceof ArrayList) {
                // Do something
                selectedAppList = (ArrayList<AppModel>) obj;
            }
        }

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        List<ResolveInfo> resolveInfoList = context1.getPackageManager().queryIntentActivities(intent, 0);

        //TelephonyManager telephonyManager = (TelephonyManager) context1.getSystemService(Context.TELEPHONY_SERVICE);

        //if(telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE){
        AppModel appModel = new AppModel();
        appModel.setId(0);
        appModel.setPackageDetails("com.dialer");
        appModel.setAppVersion("1.0");
        appModel.setTargetSdk("");
        appModel.setDateInstalled("");
        appModel.setAppInfo("");
        appModel.setSelected(false);
        appModel.setSystemApp(true);
        appModel.setUpgradableApp(false);
        appModel.setAppCategory("System");
        // }

       /* for (int k = 0; k < selectedAppList.size(); k++){
            if (selectedAppList.get(k).getPackageDetails().equals("com.dialer")){
                if (selectedAppList.get(k).isLocked()){
                    appModel.setLocked(true);
                } else {
                    appModel.setLocked(false);
                }
                break;
            }
        }*/
        appModel.setLocked(false);
        ApkPackageName.add(appModel);

        for (int i = 1; i < resolveInfoList.size(); i++) {
            ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
            AppModel appModel1 = new AppModel();
            appModel1.setId(i);
            appModel1.setPackageDetails(activityInfo.applicationInfo.packageName);

            // if(!isSystemPackage(activityInfo.applicationInfo)){
            appModel1.setSystemApp(isSystemPackage(activityInfo.applicationInfo));
            //  } else {
            //    appModel1.setSystemApp(true);
            //   }

            appModel1.setUpgradableApp(isUpgradableApp(activityInfo.applicationInfo));

            //appModel1.setLocked(true);
            appModel1.setSelected(false);
            appModel1.setTargetSdk(activityInfo.applicationInfo.targetSdkVersion + "");
            //appModel1.setAppInfo(activityInfo.applicationInfo.);
            CustomDashboardApplication.getAppVersion(context1, activityInfo.applicationInfo.packageName);

            try {
                long time = context1.getPackageManager().getPackageInfo(activityInfo.applicationInfo.packageName, 0).firstInstallTime;
                appModel1.setAppInstallTime(time);
                String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                appModel1.setDateInstalled(dateString);
                Date allAppInstallDate = new Date(time);
                Long appInstalledDate = context1.getPackageManager().getPackageInfo(context1.getPackageName(), 0).firstInstallTime;
                Date appInstallDate = new Date(appInstalledDate);
                appModel1.setAfterDateInstall(allAppInstallDate.after(appInstallDate));

                //Log.e("date", dateString);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            for (int k = 0; k < appCategoryModels.size(); k++) {
                if (appCategoryModels.get(k).getPackageName().equalsIgnoreCase(activityInfo.applicationInfo.packageName)) {
                    appModel1.setAppCategory(appCategoryModels.get(k).getAppCategory());
                }
            }

            if (selectedAppList.size() > 0) {
                for (int k = 0; k < selectedAppList.size(); k++) {
                    if (selectedAppList.get(k).getPackageDetails().equals(activityInfo.applicationInfo.packageName)) {
                        if (selectedAppList.get(k).isLocked()) {
                            appModel1.setLocked(true);
                        } else {
                            appModel1.setLocked(false);
                        }
                    }
                }
            } else {
                appModel1.setLocked(false);
            }

            //ApkPackageName.add(appModel1);

            //if (isMobileAvailable(context1)){
            if (!resolveInfoList.get(i).activityInfo.applicationInfo.packageName.equals(context1.getPackageName())) {
                try {
                    // if (!resolveInfoList.get(i).activityInfo.taskAffinity.contains("android.task.dialtact")) {
                    //if(telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
                    ApkPackageName.add(appModel1);
                    // }
                    //}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // }

           /* String dir = context1.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.categoryWiseAppsFile);
            if (f0.exists()) {
                f0.delete();
            }

            String ser2 = SerializeObject.objectToString(ApkPackageName);
            if (ser2 != null && !ser2.equalsIgnoreCase("")) {
                SerializeObject.WriteSettings(context1, ser2, Consts.categoryWiseAppsFile);
            } else {
                SerializeObject.WriteSettings(context1, "", Consts.categoryWiseAppsFile);
            }*/
        }

        for (int h = 0; h < selectedAppList.size(); h++) {
            int temp = 0;
            for (int n = 0; n < ApkPackageName.size(); n++) {
                if (ApkPackageName.get(n).getPackageDetails().equalsIgnoreCase(selectedAppList.get(h).getPackageDetails())) {
                    temp = 0;
                    break;
                } else {
                    temp++;
                }
            }

            if (temp > 0) {
                ApkPackageName.add(selectedAppList.get(h));
            }
        }


        //Toast.makeText(context1, "ApkPackageName Size : " + ApkPackageName.size(), Toast.LENGTH_SHORT).show();
        return ApkPackageName;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public ArrayList<String> getAppsPackageNameList() {
        ArrayList<String> packageNameList = new ArrayList<>();
        packageNameList.add("com.dialer");

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        List<ResolveInfo> resolveInfoList = context1.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 1; i < resolveInfoList.size(); i++) {
            ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
            packageNameList.add(activityInfo.applicationInfo.packageName);
        }

        return packageNameList;
    }

    public void getAppsCategoryWise() {
        try {
            apkPackageCategories = new ArrayList<>();
            apkPackageCategories.clear();
            ArrayList<AppModel> defaultAppsList = GetAllInstalledApkInfo();
            ArrayList<String> appCategoryModels = new ArrayList<>();
            AppModel appModel;
            for (int i = 0; i < defaultAppsList.size(); i++) {
                appModel = new AppModel();
                appModel.setId(i);

                AppModel categoryModel = new AppModel();
                categoryModel.setViewType(0);
                categoryModel.setAppCategory(defaultAppsList.get(i).getAppCategory());

                if (appCategoryModels.contains(defaultAppsList.get(i).getAppCategory())) {

                } else {
                    appCategoryModels.add(defaultAppsList.get(i).getAppCategory());
                    categoryModel.setViewType(0);
                    defaultAppsList.add(categoryModel);
                }
                appModel.setViewType(1);
                appModel.setAppCategory(defaultAppsList.get(i).getAppCategory());
                appModel.setAppInfo(defaultAppsList.get(i).getAppInfo());
                appModel.setDateInstalled(defaultAppsList.get(i).getDateInstalled());
                appModel.setTargetSdk(defaultAppsList.get(i).getTargetSdk());
                appModel.setAppVersion(defaultAppsList.get(i).getAppVersion());
                appModel.setSystemApp(defaultAppsList.get(i).isSystemApp());
                appModel.setPackageDetails(defaultAppsList.get(i).getPackageDetails());
                appModel.setSelected(defaultAppsList.get(i).isSelected());

                if (defaultAppsList.get(i).isLocked()) {
                    appModel.setLocked(true);
                } else {
                    appModel.setLocked(false);
                }
                defaultAppsList.add(appModel);
            }

            apkPackageCategories.addAll(defaultAppsList);
            String dir = context1.getFilesDir().getAbsolutePath();
            File f0 = new File(dir, Consts.categoryWiseAppsFile);
            if (f0.exists()) {
                f0.delete();
            }

            String ser1 = SerializeObject.objectToString(apkPackageCategories);
            if (ser1 != null && !ser1.equalsIgnoreCase("")) {
                SerializeObject.WriteSettings(context1, ser1, Consts.categoryWiseAppsFile);
            } else {
                SerializeObject.WriteSettings(context1, "", Consts.categoryWiseAppsFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isTablet() {
        try {
            // Compute screen size

            DisplayMetrics dm = context1.getResources().getDisplayMetrics();
            float screenWidth = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) +
                    Math.pow(screenHeight, 2));
            // Tablet devices have a screen size greater than 6 inches
            return size >= 6;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isMobileAvailable(Context acontext) {
        TelephonyManager tel = (TelephonyManager) acontext.getSystemService(Context.TELEPHONY_SERVICE);
        return (tel.getNetworkOperator() != null && !tel.getNetworkOperator().equals(""));
    }

    public boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public boolean isUpgradableApp(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public Drawable getAppIconByPackageName(String ApkTempPackageName) {
        Drawable drawable;
        try {
            drawable = context1.getPackageManager().getApplicationIcon(ApkTempPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            drawable = ContextCompat.getDrawable(context1, R.mipmap.ic_launcher);
        }
        return drawable;
    }

    public String GetAppName(String ApkPackageName) {

        String Name = "";

        ApplicationInfo applicationInfo;

        PackageManager packageManager = context1.getPackageManager();

        try {

            applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0);

            if (applicationInfo != null) {
                Name = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return Name;
    }

    public String getAppVersion(String packageName) {
        PackageInfo pinfo = null;
        String versionName = null;
        try {
            pinfo = context1.getPackageManager().getPackageInfo(packageName, 0);
            int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }
}
