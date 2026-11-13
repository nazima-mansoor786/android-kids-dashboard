package com.rsb.vpnpoc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static android.os.Build.VERSION.SDK_INT;

public class MySingletonClass {

    private static MySingletonClass instance;
    public static String build = "release"; //debug

    public static MySingletonClass getInstance() {
        if (instance == null)
            instance = new MySingletonClass();
        return instance;
    }

    private MySingletonClass() {
    }

    private ArrayList<String> val = new ArrayList<>();

    public ArrayList<String> getValue() {
        return val;
    }

    public void setValue(String value) {
        this.val.add(value);
    }

    public void setValueList(List<String> value) {
        this.val.addAll(value);
    }

    public boolean isBlocked(String host) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Predicate<String> predicate = s -> s.contains(host) || host.contains(s);
            return blockedHosts.get().stream().anyMatch(predicate);
        } else*/
        return blockedHosts.get().contains(host);
    }

    final AtomicReference<HashSet<String>> blockedHosts = new AtomicReference<>(new HashSet<>());

    HashSet<String> nextBlockedHosts = null;
    HashSet<String> blockedHostsFile = null;

    public void setBlockedHostsValue(Context context, boolean isAllowList) {
        nextBlockedHosts = new HashSet<>(blockedHosts.get().size());
        if (blockedHostsFile == null) {
            blockedHostsFile = new HashSet<>();
        }

        if (val != null && val.size() > 0) {
            Log.e("Singleton", "setBlockedHostsValue: VAL : " + val.size());
            nextBlockedHosts.addAll(val);
        } else {
            Log.e("Singleton", "setBlockedHostsValue: VAL : EMPTY");
        }

        new ReadFileAsyncTask(context, isAllowList).execute();
    }

    private void startVPNService(Context context) {
        if (nextBlockedHosts != null && nextBlockedHosts.size() > 0) {
            blockedHosts.set(nextBlockedHosts);
        } else {
            Log.e("Singleton", "setBlockedHostsValue: nextBlockedHosts : EMPTY");
        }
        Runtime.getRuntime().gc();

        Log.e("Singleton", "already have VPN permission");
        if (blockedHosts.get().size() > 0) {
            Intent vpnService = new Intent(context, VPNService.class);
            if (VPNService.Companion.getInstance() == null) {
                if (SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(vpnService);
                } else {
                    context.startService(vpnService);
                }
            }
        }
    }

    public String readFromFile(Context context, String nameFile) {
        if (nextBlockedHosts == null) {
            nextBlockedHosts = new HashSet<>();
        }
        blockedHostsFile = new HashSet<>();
        String aBuffer = "";
        int count = 0;
        try {
            File myFile = new File(nameFile);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                //     aBuffer += aDataRow;
                String host = parseLine(aDataRow);
                if (host != null) {
                    count += 1;
                    nextBlockedHosts.add(host);
                    blockedHostsFile.add(host);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            Log.e("Singleton", "readFromFile: FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Singleton", "readFromFile: IOException");
            e.printStackTrace();
        }
        return aBuffer;
    }

    @Nullable
    static String parseLine(String line) {
        int endOfLine = line.indexOf('#');

        if (endOfLine == -1)
            endOfLine = line.length();

        // Trim spaces
        while (endOfLine > 0 && Character.isWhitespace(line.charAt(endOfLine - 1)))
            endOfLine--;

        // The line is empty.
        if (endOfLine <= 0)
            return null;

        // Find beginning of host field
        int startOfHost = 0;

        if (line.regionMatches(0, "127.0.0.1", 0, 9) && (endOfLine <= 9 || Character.isWhitespace(line.charAt(9))))
            startOfHost += 10;
        else if (line.regionMatches(0, "::1", 0, 3) && (endOfLine <= 3 || Character.isWhitespace(line.charAt(3))))
            startOfHost += 4;
        else if (line.regionMatches(0, "0.0.0.0", 0, 7) && (endOfLine <= 7 || Character.isWhitespace(line.charAt(7))))
            startOfHost += 8;

        // Trim of space at the beginning of the host.
        while (startOfHost < endOfLine && Character.isWhitespace(line.charAt(startOfHost)))
            startOfHost++;

        // Reject lines containing a space
        for (int i = startOfHost; i < endOfLine; i++) {
            if (Character.isWhitespace(line.charAt(i)))
                return null;
        }

        if (startOfHost >= endOfLine)
            return null;

        return line.substring(startOfHost, endOfLine).toLowerCase(Locale.ENGLISH);
    }

    class ReadFileAsyncTask extends AsyncTask<String, String, String> {
        Context context;
        ProgressDialog progressDialog;
        boolean isAllowFile;

        public ReadFileAsyncTask(Context context, boolean isAllowFile) {
            this.context = context;
            this.isAllowFile = isAllowFile;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String readFile = "";

            if (isAllowFile) {
                if (blockedHostsFile != null && blockedHostsFile.size() > 0) {
                    nextBlockedHosts.addAll(blockedHostsFile);
                } else {
                    String host = "" + context.getExternalFilesDir(null) + "/Dashboard/block_list.txt";
                    readFile = readFromFile(context, host);
                }
            }

            return readFile;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                progressDialog.dismiss();
            }

            startVPNService(context);
        }
    }
}
