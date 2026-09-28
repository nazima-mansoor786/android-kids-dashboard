package com.tabnova.aidashboard.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.tabnova.aidashboard.CustomDashboardApplication;
import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.R;
import com.tabnova.screenshare.MediaProjectionService;
import com.tabnova.screenshare.StartStopScreenShare;

public class ScreenShareActivity extends AppCompatActivity {

    private static final String TAG = "ScreenShareActivity";
    private String serialNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_screen_share);

        StartStopScreenShare.context = this;

        serialNumber = CustomDashboardApplication.getSerialNumber();

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString(Consts.serial_number) != null) {
                serialNumber = getIntent().getExtras().getString(Consts.serial_number);
            }
        }

        startScreenCapture();
    }

    public void startScreenCapture() {
        Intent intent = new Intent(this, MediaProjectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        mediaProjectionResultLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    ActivityResultLauncher<Intent> mediaProjectionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Log.e(TAG, "Screen Share Started.");
                    StartStopScreenShare.mMediaProjectionPermissionResultCode = result.getResultCode();
                    StartStopScreenShare.mMediaProjectionPermissionResultData = result.getData();
                    StartStopScreenShare.context = ScreenShareActivity.this;
                    StartStopScreenShare.portNumber = serialNumber;
                    StartStopScreenShare.start();

                } else {
                    stopService(new Intent(getApplicationContext(), MediaProjectionService.class));
                }
                finish();
            });
}