package com.tabnova.aidashboard.Knox.startup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.samsung.android.knox.license.EnterpriseLicenseManager;
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager;
import com.tabnova.aidashboard.Extra.Utils;
import com.tabnova.aidashboard.R;


public class LicenseActivity extends Activity {

    private SuperLockState mSuperLockState;
    private LicenseActivateReceiver mLicenseActivateReceiver;
    private Context mContext;
    ProgressDialog mProgress;

    Button retryButton;
    TextView retryTestView;


    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        // Edge-to-edge: light screen (no action bar), dark status and navigation bar icons.
        Utils.applyEdgeToEdge(this, null, false, false, true, true);

        mContext = this;
        ConnectionDetector cd = new ConnectionDetector(mContext);
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(LicenseActivity.this, getString(R.string.neterror), getString(R.string.neterrormessage), false);
            // stop executing code by return
            return;
        }
        mContext =this;

        retryButton  = (Button) findViewById(R.id.button_retry);
        retryTestView = (TextView)findViewById(R.id.textView_retry);
        retryButton.setVisibility(View.INVISIBLE);
        retryTestView.setVisibility(View.INVISIBLE);

        mLicenseActivateReceiver=new LicenseActivateReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(EnterpriseLicenseManager.ACTION_LICENSE_STATUS);
        filter.addAction(KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS);


        ContextCompat.registerReceiver(this, mLicenseActivateReceiver, filter,
                ContextCompat.RECEIVER_EXPORTED);



        mSuperLockState =  SuperLockState.getInstance(this);

        mProgress= ProgressDialog.show(LicenseActivity.this, getString(R.string.licenseprogress), getString(R.string.progresschecking), true);
      //  mProgress.setCancelable(true);

        timerDelayRemoveDialog(20*1000, mProgress); //15 sec
        //mSuperLockState.setProgressbarDialog(mProgress);

        // mSuperLockState.activateLicense();



    }

//    @Override
//    protected void onDestroy()
//    {
//        if(null !=mLicenseActivateReceiver)
//            unregisterReceiver(mLicenseActivateReceiver);
//
//        super.onStop();
//    }

    @Override
    protected void onStop()
    {
        try {
            if(null !=mLicenseActivateReceiver)
                unregisterReceiver(mLicenseActivateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    public void onClickRetry(View view) {
        try {
            Intent i = new Intent(getApplicationContext(), StartupActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    d.dismiss();
                }

                retryButton.setVisibility(View.VISIBLE);
                retryTestView.setVisibility(View.VISIBLE);
            }
        }, time);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_license, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    protected class LicenseActivateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction.equals( EnterpriseLicenseManager.ACTION_LICENSE_STATUS ) ){
                String str = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
               // mSuperLockState.cancelProgressbarDialog();
                mProgress.dismiss();

                if (str != null && !TextUtils.isEmpty(str) && str.equalsIgnoreCase("success")) {

//                    Intent i = new Intent(mContext, OspDownloadService.class);
//                    intent.setData(Uri.parse("http://mantraplanet.com/orthik/tabnova/installapp/apps.zip"));
//                    startService(i);

                    if ( mSuperLockState.isESDKLicenseActivated() ) {
                        Intent i = new Intent(getApplicationContext(), StartupActivity.class);
                        mContext.startActivity(i);

                        finish();
                    }

                }

            }

            if (intentAction.equals( KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS ) ){
                String str = intent.getStringExtra(KnoxEnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
                // mSuperLockState.cancelProgressbarDialog();
                mProgress.dismiss();

                if (str.equals("success")) {

//                    Intent i = new Intent(mContext, OspDownloadService.class);
//                    intent.setData(Uri.parse("http://mantraplanet.com/orthik/tabnova/installapp/apps.zip"));
//                    startService(i);

                    if ( mSuperLockState.isCSDKLicenseActivacted() ) {
                        Intent i = new Intent(getApplicationContext(), StartupActivity.class);
                        mContext.startActivity(i);

                        finish();
                    }

                }

            }

        }


    }

}
