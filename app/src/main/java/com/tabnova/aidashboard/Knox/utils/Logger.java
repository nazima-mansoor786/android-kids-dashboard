package com.tabnova.aidashboard.Knox.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by  on 24/10/2016.
 */
public class Logger {
    /**
     * log TAG
     */
    private static String TAG = "samsung_dashboard";
    // App-specific external files dir (the shared storage root is not writable on Android 10+).
    private static final String ROOT = appFilesRoot();
    private static String FILE_NAME = "samsung_dashboard.txt";
    private static String logFile = ROOT + FILE_NAME;

    /**
     * debug or not
     */
    private static boolean debug = true;
    private static boolean write2Sdcard = true;
    private static Logger instance = new Logger();

    private static String appFilesRoot() {
        android.content.Context app = com.tabnova.aidashboard.CustomDashboardApplication.getInstance();
        File dir = app != null ? app.getExternalFilesDir(null) : null;
        return (dir != null ? dir.getAbsolutePath() : "") + File.separatorChar;
    }

    private Logger() {

    }

    public static Logger getLogger() {
        return instance;
    }

    public static Logger getLogger(Context context, String fileName) {
        TAG = context.getPackageName();
        FILE_NAME = fileName;
        return instance;
    }


    public static void setDebug(boolean status) {
        debug = status;
    }

    public static void setPath(String path) {
        if (path.endsWith("/")) {
            FILE_NAME = path + "ApplicationLog.txt";
        } else if (!path.endsWith(".txt")) {
            FILE_NAME = path + ".txt";
        } else {
            FILE_NAME = path;
        }
    }

    /**
     * 获取函数名称
     */
    private String getFunctionName() {
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

            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            return "[" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId()
                    + "): " + st.getFileName() + ":" + st.getMethodName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    private String createMessage(String msg) {
        String functionName = getFunctionName();
        String message = (functionName == null ? msg : (functionName + " - " + msg));
        return message;
    }

    /**
     * log.i
     */
    public void info(String msg) {
        String message = createMessage(msg);

        if (debug) {
            Log.i(TAG, message);
        }
        if (write2Sdcard) {
            instance.writeLog(message);
        }
    }

    public static void i(String msg) {
        instance.info(msg);
    }

    public static void i(Exception e) {
        instance.info(e != null ? e.toString() : "null");
    }

    /**
     * log.v
     */
    public void verbose(String msg) {
        String message = createMessage(msg);

        if (debug) {
            Log.v(TAG, message);
        }

        if (write2Sdcard) {
            instance.writeLog(message);
        }
    }

    public void v(String msg) {

        if (debug) {
            instance.verbose(msg);
        }

        if (write2Sdcard) {
            instance.writeLog(msg);
        }

    }

    public void v(Exception e) {

        if (debug) {
            instance.verbose(e != null ? e.toString() : "null");
        }

        if (write2Sdcard) {
            instance.writeLog(e != null ? e.toString() : "null");
        }

    }

    /**
     * log.d
     */
    public void debug(String msg) {

        if (debug) {
            String message = createMessage(msg);
            Log.d(TAG, message);
        }

        if (write2Sdcard) {
            instance.writeLog(msg);
        }
    }

    /**
     * log.e
     */
    public void error(String msg) {
        String message = createMessage(msg);
        if (debug) {
            Log.e(TAG, message);
        }
        if (write2Sdcard) {
            instance.writeLog(message);
        }
    }

    /**
     * log.error
     */
    public void error(Exception e) {
        StringBuilder sb = new StringBuilder();
        String name = getFunctionName();
        StackTraceElement[] sts = e.getStackTrace();

        if (name != null) {
            sb.append(name).append(" - ").append(e).append("\r\n");
        } else {
            sb.append(e).append("\r\n");
        }

        if (sts != null && sts.length > 0) {
            for (StackTraceElement st : sts) {
                if (st != null) {
                    sb.append("[ ").append(st.getFileName()).append(":").append(st.getLineNumber()).append(" ]\r\n");
                }
            }
        }

        if (debug) {
            Log.e(TAG, sb.toString());
        }

        if (write2Sdcard) {
            instance.writeLog(sb.toString());
        }
    }

    /**
     * log.warn
     */
    public void warn(String msg) {
        String message = createMessage(msg);
        if (debug) {
            Log.w(TAG, message);
        }
        if (write2Sdcard) {
            instance.writeLog(message);
        }
    }

    public static void d(String msg) {
        instance.debug(msg);

    }

    public static void d(Exception e) {
        instance.debug(e != null ? e.toString() : "null");
    }

    /**
     * log.e
     */
    public static void e(String msg) {
        instance.error(msg);
    }

    public static void e(Exception e) {
        instance.error(e);
    }

    /**
     * log.w
     */
    public static void w(String msg) {
        instance.warn(msg);
    }

    public static void w(Exception e) {
        instance.warn(e != null ? e.toString() : "null");
    }

    public static void resetLogFile() {
        File file = new File(logFile);
        boolean delete = file.delete();

        try {
            boolean newFile = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeLog(String content) {

        try {
            File file = new File(logFile);

            if (!file.exists()) {
                boolean newFile = file.createNewFile();
            }
            if (file.length() > 2097152) { // 2 MB
                resetLogFile();
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            FileWriter write = new FileWriter(file, true);
            write.write(format.format(new Date()) + "   " +
                    content + "\n");
            write.flush();
            write.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
