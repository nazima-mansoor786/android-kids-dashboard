package com.tabnova.aidashboard.Knox.license;

public class Const {

    protected static final String URL_BASE = "https://b2b.tabnova.com/api/v1";

    public static final String AUTHENTICATE = URL_BASE + "/authenticate/";
    public static final String DEVICE_DEVICE_DETAILS = URL_BASE + "/setdevicedetails/";
    public static final String DEVICE_PROVISION_STATUS = URL_BASE + "/setdeviceprovisionstatus/";
    public static final String DEVICE_LOG_TO_CLOUD = URL_BASE + "/setdevicelog/";

    ///api/v1/register/{deviceserialnumber}/{devicepassword}/{suscriptioncode}/{profileid}
    public static final String REGISTER = URL_BASE + "/register/";

    public static final String BASIC_PASSWORD = "qwerty";

    public static final String DEVICE_PROFILE_LICENCES = URL_BASE + "/deviceprofilelicences/";


    public static final String GENERAL_SERIAL_NUMBER = "PDP_WEB_APP_SRNO";
    public static final String PDP_WEB_LICENCE = "PDP_WEB_LICENCE";
    public static final String IOS = "alpine";
    public static final String PDP_WEB_PASS = "PDP_WEB_PASS";
    public static final String HARD_CODE_SUB_CODE = "179-1559219867";
    public static String ask_profile_name = "ask_profile_name";

    public static String getUrlBase() {
        return URL_BASE;
    }

    public static final int MAX_RETRY_COUNT = 3;

    public static final int PROFILE_ID = 179; //Custom Dashboard

    public static final String IGNORE_KNOX_LICENSE = "ignore_knox_samsung_license";

    public static final String KNOX_LICENSE_SDK_KEY = "KLM09-N85U9-EGSNV-HCZEW-P1ULV-QR8VI";
//    public static final String KNOX_LICENSE_SDK_KEY = "KLM09-N0B4M-TQAGR-JGD18-U3X1C-QKPRA";

}
