package com.tabnova.aidashboard.CallRejectionReciver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;

import java.lang.reflect.Method;

public class IncomingCallReceiver extends BroadcastReceiver {

    String incomingNumber="";
    //AudioManager audioManager;
    TelephonyManager telephonyManager;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (CustomDashboardApplication.getAllowCalls(context, Consts.ALLOWCALLS) == 1) {
            // Get AudioManager
            //audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            // Get TelephonyManager
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // Get incoming number
                    incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                }
            }

            if (!incomingNumber.equals("")) {
                // Get an instance of ContentResolver
                ContentResolver cr = context.getContentResolver();
                // Fetch the matching number
                Cursor numbers = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.NUMBER + "=?", new String[]{incomingNumber}, null);
                if(numbers.getCount()<=0){ // The incoming number is not  found in the contacts list
                    // Turn on the mute
                    //audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    // Reject the call
                    endCall(context);
                    //disconnectCall(context);
                    // Send the rejected message ton app
                    //startApp(context,incomingNumber);
                }

            }

        }
    }


   /* private void rejectCall(){

        try {
            // Get the getITelephony() method
            Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method method = classTelephony.getDeclaredMethod("getITelephony");
            // Disable access check
            method.setAccessible(true);
            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = method.invoke(telephonyManager);
            // Get the endCall method from ITelephony
            Class<?> telephonyInterfaceClass =Class.forName(telephonyInterface.getClass().getName());
            Method m2 = telephonyInterface.getClass().getDeclaredMethod("silenceRinger");
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
            // Invoke endCall()
            m2.invoke(telephonyInterface);
            method.invoke(telephonyInterface);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
*/

    public void endCall(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {

            try {
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
           /* Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm);

            c = Class.forName(telephonyService.getClass().getName());
            m = c.getDeclaredMethod("endCall");
            m.setAccessible(true);
            m.invoke(telephonyService);*/


            /*TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = Class.forName(telephonyManager.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
            telephonyService.endCall();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectCall(Context context){
        try {

            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("",
                    "FATAL ERROR: could not connect to telephony subsystem");
            Log.e("", "Exception object: " + e);
        }
    }


}
