package com.tabnova.aidashboard.Knox.license;


/**
 * Created by  on 31/10/2016.
 */
public class TokenManager {


    private static TokenManager instance;
    private static int mCount = 0;
    private static String token = "";


    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        mCount++;
        return instance;
    }

    public static synchronized void releaseTokenRef() {
        --mCount;

    }

    public static String getToken() {
        return token;
    }

    public static synchronized void setToken(String token) {
        TokenManager.token = token;
    }
}
