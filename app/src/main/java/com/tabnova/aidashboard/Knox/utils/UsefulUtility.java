package com.tabnova.aidashboard.Knox.utils;

import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tabnova.aidashboard.Knox.startup.CommonUtility;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by  on 02/01/16.
 */
public class UsefulUtility {
    private static final String JSON_NULL_STR = "null";

    public static void cleanDirectory(String path) {
        File dir = new File(Environment.getExternalStorageDirectory() + path);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public static void toast(String message, Context cont) {
        Toast.makeText(cont, message, Toast.LENGTH_LONG).show();
    }

    public static PackageInfo getPackageNameByAPK(String _strAPKPath, Context _activityORservice) {
        String strRetVal = "";
        PackageManager packMan = null;
        PackageInfo packInfo = null;

        try {

            if (_strAPKPath == null) return null;

            Log.d("K.ATAG", "getPackageInfoFromAPK(): " + _strAPKPath);
            if (_activityORservice == null) {
                Log.e("K.ATAG", "Context is null");
                return null;
            }

            packMan = _activityORservice.getPackageManager();
            packInfo = packMan.getPackageArchiveInfo(_strAPKPath, 0);


        } catch (Exception e) {
            Log.e("K.ATAG", e.toString() + "");
        }

        Log.d("K.ATAG", "RetVal: " + strRetVal);
        return packInfo;
    }


    public static void cleanDirectory(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
        }
    }

    public static void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
            try {
                dir.delete();
            } catch (Exception e) {
                //errorReport += "\nRemove file " + dir.getName() + " failed";
            }
        } else {
            try {
                dir.delete();
            } catch (Exception e) {
                //errorReport += "\nRemove file " + dir.getName() + " failed";
            }
        }
    }


    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.isEmpty());
    }


    public static String getIpFromIntSigned(int ip_int) {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    /*
     * wrapper for JSONObject.optString() which handles "null" values
     */
    public static String getString(JSONObject json, String name) {
        String value = null;
        try {
            value = json.optString(name);
        } catch (Exception e) {
            return "";
        }
        // return empty string for "null"
        if (JSON_NULL_STR.equals(value))
            return "";
        return value;
    }

    public static void setWallPaper(Context cont, String path) {
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(cont.getApplicationContext());
        try {
            InputStream inputStream = new FileInputStream(path);
            myWallpaperManager.setStream(inputStream);
        } catch (Exception e) {

        }

    }


    public static void unlockScreen(Context context) {

    }

//    public static String getSerialNumber() {
//        String serial = null;
//        try {
//            Class<?> c = Class.forName("android.os.SystemProperties");
//            Method get = c.getMethod("get", String.class, String.class);
//            serial = (String) get.invoke(c, "ril.serialnumber", "unknown");
//        } catch (Exception ignored) {}
//        return serial;
//    }


    public static String getSerialNumber() {
        String serial = null;
        Class<?> c;
        try {
            c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ril.serialnumber");

            if (TextUtils.isEmpty(serial) || TextUtils.isEmpty(serial.replaceAll("0", ""))) {
                serial = (String) get.invoke(c, "ro.lenovosn2");
            }

            if (TextUtils.isEmpty(serial) || TextUtils.isEmpty(serial.replaceAll("0", ""))) {
                serial = (String) get.invoke(c, "sys.serialnumber");
            }

            if (TextUtils.isEmpty(serial) || TextUtils.isEmpty(serial.replaceAll("0", ""))) {
                serial = (String) get.invoke(c, "ro.serialno");
            }

            if (TextUtils.isEmpty(serial) || TextUtils.isEmpty(serial.replaceAll("0", ""))) {
                serial = Build.SERIAL;
            }

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {

        }

        return serial + "_KNOX_DASH";
        //return "R52J4107ZTY";
        //return "R52J90FPNLD";
        // return "R52J509F4JL";

        //return "R52GA0QS5GE";
    }

    public static void downloadFilesFromCloud(String fileurl, String outputFile, Context context) {

        File direct;
        direct = new File(Environment.getExternalStorageDirectory()
                + "/" + outputFile);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        // Uri downloadUri = Uri.parse(AppConfig.APK_PATH_URL + user +"/"+ app);
        Uri downloadUri = Uri.parse(fileurl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE
        )
                .setAllowedOverRoaming(false).setTitle("Application Install")
                .setDescription("Files download install")
                .setDestinationInExternalPublicDir("/" + outputFile, fileurl.substring(fileurl.lastIndexOf("/") + 1));

        mgr.enqueue(request);


    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }


    public static int getApplicationVersionCode(String pname, Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(pname, 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
        }

        return 0;
    }


    public static String copyAssetFileToSD(String asset, String path, Context context) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String sdcardpath = Environment.getExternalStorageDirectory().getPath() + "/" + path + "/" + asset;
        boolean res = false;

        try {
            in = assetManager.open(asset);
            out = new FileOutputStream(sdcardpath);

            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

        } catch (Exception e) {

        }
        return sdcardpath;
    }
//    public static boolean LogToFile(String fcontent){
//        try {
//            SimpleDateFormat formatter = new SimpleDateFormat("dd_HH_mm_ss_SS");
//            Date now = new Date();
//            String fpath = "/sdcard/"+"mdm"+".txt";
//
//            File file = new File(fpath);
//
//            // If file does not exists, then create it
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(formatter.format(now) + " : " + fcontent +"\n");
//            bw.close();
//
//            Log.d("Suceess","Sucess");
//            return true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void showErrorDialog(Context context, String title, String message) {
        Intent i = new Intent(context.getApplicationContext(), ErrorDialogActivity.class);
        i.putExtra("title", title);
        i.putExtra("message", message);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public static void setKioskPin(String newPin, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CommonUtility.OSPPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(CommonUtility.KIOSKPIN, newPin);
        editor.commit();

    }

    public static String getKioskPin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CommonUtility.OSPPREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(CommonUtility.KIOSKPIN, "0000");
    }
}



