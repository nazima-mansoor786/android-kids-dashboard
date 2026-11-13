package com.tabnova.aidashboard.Knox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.tabnova.aidashboard.CustomDashboardApplication;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by on 04/11/2016.
 */
public class SysUtils {
    /*
                /api/v1/setdevicedetails/{device_battery_level}/{device_activation_date}/{device_osp_client_version}/
            {device_operating_system}/{device_model_number}/{device_mac_address}/{device_ip_address}/
            {device_location}/{device_gateway_ip}/{device_profile_counter}/{token}
     */
    public static String getVersionName(Context context, String packageName) {
        String version = "Unknown";
        try {
            final PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            version = pInfo.versionName;
        } catch(final Throwable e) {

        }
        return version;
    }

    public static String getLocalIPAddress(final Context context) {
        final StringBuilder ipAddress = new StringBuilder();
        try {
            for(final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                final NetworkInterface intf = en.nextElement();
                for(final Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    final InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress() && inetAddress.getClass() == Inet4Address.class) {
                        if(ipAddress.length() > 0) {
                            ipAddress.append("\n");
                        }
                        ipAddress.append(inetAddress.getHostAddress());
                    }
                }
            }
        } catch(Exception e) {
            // Do nothing.
        }
        if(ipAddress.length() == 0) {
            return "Unknown";
        }
        return ipAddress.toString().trim();
    }


    public static float getBatteryLevel(Context context) {
        int level = -1;
        int scale = -1;
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }catch (Exception e){
            e.printStackTrace();
        }

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }


    public static String getOSVersion() {
       String version = "Unknown" ;
        try {
            version = Build.VERSION.RELEASE + " - " + Build.VERSION.INCREMENTAL + " - " +Build.VERSION.SDK_INT;
        } catch (Exception e) {

        }
        return version;
    }
    public static String getDeviceModel() {
        String deviceModel = "Unknown" ;
        try {
            deviceModel = Build.MANUFACTURER  + " - " + Build.MODEL ;
        } catch (Exception e) {

        }
        return deviceModel;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


    public static String getLocation(Context context) {
        // Get the location manager
        String location =  CustomDashboardApplication.getString(context, "Location");
        if(location == null || location.isEmpty()) {
            location = "51.503324, -0.119543";
        }
        return location;

    }



    public static String getSystemDetails() {
        String  details = "";
        try {
            details = "VERSION.RELEASE : "+ Build.VERSION.RELEASE
                    +"\nVERSION.INCREMENTAL : "+Build.VERSION.INCREMENTAL
                    +"\nVERSION.SDK.NUMBER : "+Build.VERSION.SDK_INT
                    +"\nBOARD : "+Build.BOARD
                    +"\nBOOTLOADER : "+Build.BOOTLOADER
                    +"\nBRAND : "+Build.BRAND
                    +"\nCPU_ABI : "+Build.CPU_ABI
                    +"\nCPU_ABI2 : "+Build.CPU_ABI2
                    +"\nDISPLAY : "+Build.DISPLAY
                    +"\nFINGERPRINT : "+Build.FINGERPRINT
                    +"\nHARDWARE : "+Build.HARDWARE
                    +"\nHOST : "+Build.HOST
                    +"\nID : "+Build.ID
                    +"\nMANUFACTURER : "+Build.MANUFACTURER
                    +"\nMODEL : "+Build.MODEL
                    +"\nPRODUCT : "+Build.PRODUCT
                    +"\nSERIAL : "+Build.SERIAL
                    +"\nTAGS : "+Build.TAGS
                    +"\nTIME : "+Build.TIME
                    +"\nTYPE : "+Build.TYPE
                    +"\nUNKNOWN : "+Build.UNKNOWN
                    +"\nUSER : "+Build.USER;
        } catch (Exception e) {

        }

        return details;
    }

    public static String getBaseDomain(String url) {
        String host = getHost(url);

        int startIndex = 0;
        int nextIndex = host.indexOf('.');
        int lastIndex = host.lastIndexOf('.');
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf('.', startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }

    public static String getHost(String url){
        if(url == null || url.length() == 0)
            return "";

        int doubleslash = url.indexOf("//");
        if(doubleslash == -1)
            doubleslash = 0;
        else
            doubleslash += 2;

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        int port = url.indexOf(':', doubleslash);
        end = (port > 0 && port < end) ? port : end;

        return url.substring(doubleslash, end);
    }


    public static boolean ScreenBrightness(int level, Context context) {
        if(level < 0 || level > 100 ) {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            return false;
        }

        level = (level * 255 ) / 100;
        try {
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, level);


            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    level);


            return true;
        }

        catch (Exception e) {
            return false;
        }
    }

    public static String getFunctionName(Context context) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(context.getClass().getName())) {
                continue;
            }

            return "[" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId()
                    + "): " + st.getFileName() + ":" + st.getMethodName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    public static void deleteFolder(String folder) {
        try {
            File dir = new File(com.tabnova.aidashboard.CustomDashboardApplication.getInstance().getExternalFilesDir(null) + folder);
            rmdir(dir);
//            if (dir.isDirectory())
//            {
//                String[] children = dir.list();
//                if(children == null) {
//                    return;
//                }
//                for (int i = 0; i < children.length; i++)
//                {
//                    new File(dir, children[i]).delete();
//                }
//            }
        } catch (Exception e) {

        }
    }

    public static void rmdir(final File folder) {
        // check if folder file is a real folder
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    File tmpF = list[i];
                    if (tmpF.isDirectory()) {
                        rmdir(tmpF);
                    }
                    tmpF.delete();
                }
            }
            if (!folder.delete()) {

            }
        }
    }


//    public static void restart(Context context) {
//        PendingIntent myActivity = PendingIntent.getActivity(AppController.getInstance().getBaseContext(),
//                192837, new Intent(AppController.getInstance().getBaseContext(), MainActivity.class),
//                PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmManager;
//        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                1000, myActivity );
//        System.exit(2);
//    }

    public static String getApplicationName(String packageName, Context context) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
            return context.getPackageManager().getApplicationLabel(app).toString();
         } catch (Exception e) {

        }
        return "Unknown";
    }

//    synchronized public static  void addInstalledApps(Context context) {
//        InstalledAppsPref sharedPreference = new InstalledAppsPref();
//        ArrayList<String> appList = sharedPreference.getPackageList(context);
//        if(appList == null) {
//            return;
//        }
////Remove duplicate
//        Set<String> hs = new HashSet<>();
//        hs.addAll(appList);
//        appList.clear();
//        appList.addAll(hs);
//        int playStorePage = AppController.getInt(context, ProfileManager.SAVED_LAUNCHER_PAGE_PLAYSTORE);
//        if(playStorePage < 1 ) {
//           return;
//        }
//        int columnNo = AppController.getInt(context, ProfileManager.SAVED_LAUNCHER_NO_COLUMN);
//        if(columnNo < 1 ) {
//            columnNo = 3;
//        }
//
//        int count = 0;
//        int maxRow = appList.size() / columnNo +  ((appList.size() %  columnNo) > 0 ? 1 :0 );
//
//        for (int row = 1; row <= maxRow ; ++row) {
//            for (int column = 1; column<= columnNo; ++column) {
//                try {
//                    String pName = appList.get(count++);
//                    if(pName == null) {
//                       continue;
//                    }
//                    boolean res = ContentManager.addAppToDashboard(playStorePage,
//                            row,
//                            column,
//                            pName, context);
//                    if(! res ) {
//                        appList.remove(pName);
//                    }
//                } catch (Exception e) {
//
//                }
//
//            }
//        }
//        sharedPreference.savePackageList(context, appList);
//    }
    public static String getFileExtension(String fileName) {
        if(fileName == null || fileName.isEmpty()) {
            return "";
        }
        String filenameArray[] = fileName.split("\\.");
        if(filenameArray.length < 1) {
            return "";
        }
        return filenameArray[filenameArray.length-1];
    }

//    public static void sentSyncMessage(Context context, int syncValue, String msg) {
//        Intent intent = new Intent(context, LoginService.class);
//        intent.putExtra(Const.SYNC_TYPE_EXTRA, syncValue);
//        intent.putExtra(Const.SYNC_MESSAGE_EXTRA, msg);
//        context.startService(intent);
//    }

    public static int getInt(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getInt(key, -1);
    }
}
