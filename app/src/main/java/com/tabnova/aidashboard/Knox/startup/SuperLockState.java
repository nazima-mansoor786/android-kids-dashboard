package com.tabnova.aidashboard.Knox.startup;

import android.content.ComponentName;
import android.content.Context;

import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.application.ApplicationPolicy;
import com.samsung.android.knox.custom.CustomDeviceManager;
import com.samsung.android.knox.deviceinfo.DeviceInventory;
import com.samsung.android.knox.kiosk.KioskMode;
import com.samsung.android.knox.restriction.RestrictionPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osp on 17/01/15.
 */
public class SuperLockState {
    public static ComponentName compName;
    private static SuperLockState mInstance;
    public static EnterpriseDeviceManager edm = null;
    private ApplicationPolicy appPolicy = null;
    private RestrictionPolicy restrictionPolicy = null;
    private DeviceInventory deviceInventoryPolicy = null;
    // KioskMode kioskModeService = null;

    public static int LICESSE_STATUS_ESDK = 1;
    public static int LICESSE_STATUS_CSDK = 2;
    public static int LICESSE_STATUS_NOT_ACTIVATED = 0;

    public SuperLockState(Context context) {
        try {
            final String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer != null && manufacturer.equalsIgnoreCase("samsung")) {
                try {
                    edm = EnterpriseDeviceManager.getInstance(context);
                    if (edm != null) {
                        appPolicy = edm.getApplicationPolicy();
                        restrictionPolicy = edm.getRestrictionPolicy();
                        deviceInventoryPolicy = edm.getDeviceInventory();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // kioskModeService = KioskMode.getInstance(AppController.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SuperLockState getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SuperLockState(context);
        }
        return mInstance;
    }

    public int getLicenseStatus() {
        if (isESDKLicenseActivated() || isCSDKLicenseActivacted()) {
            return LICESSE_STATUS_CSDK;
        } else if (isESDKLicenseActivated()) {
            return LICESSE_STATUS_ESDK;
        }
        return LICESSE_STATUS_NOT_ACTIVATED;
    }

    public boolean supportedActivation() {

        try {
            CustomDeviceManager cdm = CustomDeviceManager.getInstance();
            int version = EnterpriseDeviceManager.getAPILevel();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isESDKLicenseActivated() {
        boolean status;

        try {
//            restrictionPolicy.allowStatusBarExpansion(true);
            restrictionPolicy.allowStatusBarExpansion(restrictionPolicy.isStatusBarExpansionAllowed());
            status = true;
        } catch (Exception e) {
            status = false;

        }
        return status;
    }

    public String allowFactoryReset(boolean allow) {
        try {
            return (restrictionPolicy.allowFactoryReset(allow) ? "OK" : "Failed");
        } catch (Exception e) {

        }
        return "Failed";
    }

    public String allowOTAUpgrade(boolean allow) {
        try {
            return (restrictionPolicy.allowOTAUpgrade(allow) ? "OK" : "Failed");
        } catch (Exception e) {

        }
        return "Failed";

    }

    public boolean isOTAUpgradeAllowed() {
        return restrictionPolicy.isOTAUpgradeAllowed();
    }

    public String allowSVoice(boolean allow) {
        try {
            return (restrictionPolicy.allowSVoice(allow) ? "OK" : "Failed");
        } catch (Exception e) {

        }
        return "Failed";
    }

    public String allowStatusBar(boolean allow) {
        try {
            return (restrictionPolicy.allowStatusBarExpansion(allow) ? "OK" : "Failed");
        } catch (Exception e) {

        }
        return "Failed";
    }


    public boolean uninstallApplication(String packageName, boolean keepDataAndCache) {
        try {
            return appPolicy.uninstallApplication(packageName, keepDataAndCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean wipeApplicationData(String packageName) {
        try {
            return appPolicy.wipeApplicationData(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getSerialNumber() {
        String srno = deviceInventoryPolicy.getSerialNumber();

        if (srno == null || srno.isEmpty() || srno.contains("0000")) {
            srno = "";
        }
        return srno;

    }


    public String startApp(String packageName, String className) {
        return (appPolicy.startApp(packageName, className) ? "OK" : "Failed");
    }

    public String stopApp(String packageName) {
        return (appPolicy.stopApp(packageName) ? "OK" : "Failed");
    }

    public String installApplication(String apkPath) {
        try {
            String res = appPolicy.installApplication(apkPath, false) ? "OK" : "Failed";
            if (res.contentEquals("OK")) {
                try {
                    File fdelete = new File(apkPath);
                    if (fdelete.exists()) {
                        fdelete.delete();
                    }
                } catch (Exception e) {

                }
            }
            return res;
        } catch (Exception e) {

        }
        return "Failed";
    }

    public String updateApplication(String apkPath) {
        try {
            String res = appPolicy.updateApplication(apkPath) ? "OK" : "Failed";
            if (res.contentEquals("OK")) {
                File fdelete = new File(apkPath);
                if (fdelete.exists()) {
                    fdelete.delete();
                }
            }
            return res;
        } catch (Exception e) {
        }
        return "Failed";
    }


    public boolean isApplicationInstalled(String packageName) {
        return appPolicy.isApplicationInstalled(packageName);
    }


    public int getApplicationVersionCode(String packageName) {
        return appPolicy.getApplicationVersionCode(packageName);
    }


    public boolean allowFirmwareRecovery(boolean allow) {
        try {
            return restrictionPolicy.allowFirmwareRecovery(allow);
        } catch (Exception e) {
        }
        return false;
    }


    public void removeAllShortcut() {
//
//        try {
//            ArrayList retList = appPolicy.getHomeShortcuts();
//            String getHomeShortcuts = "";
//            for (int index = 0 ; index < retList.size(); index ++) {
//                tmp = results.get(index);
//                getHomeShortcuts += "packagename: " + tmp.getPackageName() + " \n class name : " + tmp.getClassName() + "\n\n";
//            }
//        } catch (Exception e) {
//
//        }


        List<ComponentName> results;
        ComponentName tmp;

        results = appPolicy.getHomeShortcuts(null, true);

        String sb = "";
        for (int index = 0; index < results.size(); index++) {
            tmp = results.get(index);
            if (tmp != null) {
//                sb += "packagename: " + tmp.getPackageName() + " \n class name : "
//                        + tmp.getClassName() + "\n\n";

                appPolicy.deleteHomeShortcut(tmp.getPackageName(), "com.sec.android.kiosk");
            }
        }
    }

    public boolean isCSDKLicenseActivacted() {
        CustomDeviceManager cdm = CustomDeviceManager.getInstance();

        boolean status = false;
        try {
            status = cdm.checkEnterprisePermission("com.sec.enterprise.knox.permission.CUSTOM_SEALEDMODE");
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    public void enableKioskMode(String kioskPackage) {
        KioskMode kioskModeService = edm.getKioskMode();
        try {
            kioskModeService.enableKioskMode(kioskPackage);
        } catch (Exception se) {

        }
    }

    public void disableKioskMode() {
        KioskMode kioskModeService = edm.getKioskMode();
        try {
            kioskModeService.disableKioskMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String allowHardwareKeys(boolean status, int key, Context cont) {
        if (key < 0) {
            return "Failed";
        }
        List<Integer> res;
        boolean result = false;
        KioskMode kioskModeService = edm.getKioskMode();
        try {
            List availableHwKeys = new ArrayList();
            availableHwKeys.add(new Integer(key)); //Key code constant: Back key.

            res = kioskModeService.allowHardwareKeys(availableHwKeys, status);
            if (res != null) {
                result = true;
            }

        } catch (Exception e) {

        }
        return (result ? "OK" : "Failed");
    }

    public String hideStatusBar(boolean hide, Context context) {
        boolean res = false;

        try {
            KioskMode kioskModeService = edm.getKioskMode();
            res = kioskModeService.hideStatusBar(hide);

        } catch (Exception e) {

        }
        return (res ? "OK" : "Failed");
    }

    //Application package state
    public boolean setDisableApplication(String packageName) {
        try {
            //Toast.makeText(CustomDashboardApplication.getInstance(), "Disable App : " + packageName, Toast.LENGTH_SHORT).show();
            return appPolicy.setDisableApplication(packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setEnableApplication(String packageName) {
        try {
            return appPolicy.setEnableApplication(packageName);
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isKioskModeEnabled(Context context) {
        try {
            KioskMode kioskModeService = edm.getKioskMode();
            return kioskModeService.isKioskModeEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}