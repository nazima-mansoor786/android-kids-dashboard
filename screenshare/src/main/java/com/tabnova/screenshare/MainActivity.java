package com.tabnova.screenshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SignallingClient.SignalingInterface {

    public static Context context;

    public static PeerConnectionFactory peerConnectionFactory;
    public static MediaConstraints audioConstraints;
    public static MediaConstraints videoConstraints;
    public static MediaConstraints sdpConstraints;
    public static VideoSource videoSource;
    public static VideoTrack localVideoTrack;
    public static AudioSource audioSource;
    public static AudioTrack localAudioTrack;
    public static SurfaceTextureHelper surfaceTextureHelper;

    //  SurfaceViewRenderer localVideoView;
    //  SurfaceViewRenderer remoteVideoView;

    public static Button hangup;
    public static Button startCall;
    public static EditText edt_room_id;
    public static TextView txt_room_id;
    public static TextView txt_status;
    public static PeerConnection localPeer;
    public static List<IceServer> iceServers;
    public static EglBase rootEglBase;

    public static boolean gotUserMedia;
    public static List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();

    final int ALL_PERMISSIONS_CODE = 1;

    private static final String TAG = "MainActivity";

    final int CAPTURE_PERMISSION_REQUEST_CODE = 100;
    public static int mMediaProjectionPermissionResultCode;
    public static int sDeviceWidth;
    public static int sDeviceHeight;
    public static Intent mMediaProjectionPermissionResultData;

    public static String roomName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main_scrren);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        sDeviceWidth = metrics.widthPixels;
        sDeviceHeight = metrics.heightPixels;

        context = this;

        initViews();

        edt_room_id.setText(roomName);

        startScreenCapture();
    }

/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ALL_PERMISSIONS_CODE
                && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // all permissions granted
            //  start();
            startScreenCapture();
        } else {
            finish();
        }
    }*/

    private void initViews() {
        hangup = findViewById(R.id.end_call);
        startCall = findViewById(R.id.start_call);
        edt_room_id = findViewById(R.id.edt_room_id);
        txt_status = findViewById(R.id.txt_status);
        txt_room_id = findViewById(R.id.txt_room_id);
        // localVideoView = findViewById(R.id.local_gl_surface_view);
        // remoteVideoView = findViewById(R.id.remote_gl_surface_view);
        hangup.setOnClickListener(this);
        startCall.setOnClickListener(this);
        /*if (SignallingClient.getInstance().roomName.equals("12345")) {
            String str_random_room_id = "" + ((int) (Math.random() * 9000) + 1000);
            edt_room_id.setText(str_random_room_id);
            txt_room_id.setText("Your room id: " + str_random_room_id);
            SignallingClient.getInstance().roomName = str_random_room_id;
        } else {
            edt_room_id.setText(SignallingClient.getInstance().roomName);
            txt_room_id.setText("Your room id: " + SignallingClient.getInstance().roomName);
        }*/

        SignallingClient.getInstance().roomName = roomName;

// Its better to have unique value since if the user didn't close the session next time we won't get room full
/*        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        String datetime = ft.format(dNow);
        System.out.println(datetime);*/

        if (isMyServiceRunning(MediaProjectionService.class)) {
            startCall.setEnabled(false);
            hangup.setEnabled(true);
        }

    }

    private void initVideos() {
        rootEglBase = EglBase.create();
        // localVideoView.init(rootEglBase.getEglBaseContext(), null);
        //  remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        //    localVideoView.setZOrderMediaOverlay(true);
        //    remoteVideoView.setZOrderMediaOverlay(true);
    }

    private void getIceServers() {
        //get Ice servers using xirsys
        byte[] data = new byte[0];
        try {
            data = ("<xirsys_ident>:<xirsys_secret>").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String authToken = "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
        Utils.getInstance().getRetrofitInstance().getIceCandidates(authToken).enqueue(new Callback<TurnServerPojo>() {
            @Override
            public void onResponse(@NonNull Call<TurnServerPojo> call, @NonNull Response<TurnServerPojo> response) {
                TurnServerPojo body = response.body();
                if (body != null) {
                    iceServers = body.iceServerList.iceServers;
                }
                for (IceServer iceServer : iceServers) {
                    if (iceServer.credential == null) {
                        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url).createIceServer();
                        peerIceServers.add(peerIceServer);
                    } else {
                        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url)
                                .setUsername(iceServer.username)
                                .setPassword(iceServer.credential)
                                .createIceServer();
                        peerIceServers.add(peerIceServer);
                    }
                }
                Log.d("onApiResponse", "IceServers\n" + iceServers.toString());
            }

            @Override
            public void onFailure(@NonNull Call<TurnServerPojo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void start() {
        if (!edt_room_id.getText().toString().equals("")) {
            SignallingClient.getInstance().initroomname(edt_room_id.getText().toString());
            txt_room_id.setText("Your room id: " + SignallingClient.getInstance().roomName);
        }
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initVideos();
        getIceServers();


        //Initialize PeerConnectionFactory globals.
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();

        //Now create a VideoCapturer instance.
        VideoCapturer videoCapturerAndroid;
        //    videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(false));
        videoCapturerAndroid = createScreenCapturer();

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
            videoCapturerAndroid.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        }
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        //   audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        //    localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(2048, 2048, 30);
        }


        // localVideoView.setVisibility(View.VISIBLE);
        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        // localVideoTrack.addSink(localVideoView);

        //   localVideoView.setMirror(true);
        //   remoteVideoView.setMirror(true);

        gotUserMedia = true;
        if (SignallingClient.getInstance().isInitiator) {
            onTryToStart();
        }


        Intent svc = new Intent(MainActivity.this, MediaProjectionService.class);
        // Intent svc = new Intent(this, mousemove.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }

        SignallingClient.getInstance().init(this);
        startCall.setEnabled(false);
        hangup.setEnabled(true);
    }

    private VideoCapturer createScreenCapturer() {
        if (mMediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            showToast("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mMediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                showToast("User revoked permission to capture the screen.");
            }
        });
    }

    private void startScreenCapture() {
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
                mMediaProjectionPermissionResultCode = result.getResultCode();
                mMediaProjectionPermissionResultData = result.getData();
                start();
            });

    /**
     * This method will be called directly by the app when it is the initiator and has got the local media
     * or when the remote peer sends a message through socket that it is ready to transmit AV data
     */
    @Override
    public void onTryToStart() {
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                SignallingClient.getInstance().isStarted = true;
                //  if (SignallingClient.getInstance().isInitiator) {
                doCall();
                //  }
            }
        });
    }

    /**
     * Creating the local peerconnection instance
     */
    private void createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(peerIceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                showToast("Received Remote stream");
                super.onAddStream(mediaStream);
                //   gotRemoteStream(mediaStream);
            }
        });

        addStreamToLocalPeer();
    }

    /**
     * Adding the stream to the localpeer
     */
    private void addStreamToLocalPeer() {
        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        //   stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localPeer.addStream(stream);
    }

    /**
     * This method is called when the app is the initiator - We generate the offer and send it over through socket
     * to remote peer
     */
    private void doCall() {
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        localPeer.createOffer(new CustomSdpObserver("localCreateOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                Log.d("onCreateSuccess", "SignallingClient emit ");
                SignallingClient.getInstance().emitMessage(sessionDescription);
            }
        }, sdpConstraints);
    }

    /**
     * Received remote peer's media stream. we will get the first video track and render it
     */
/*    private void gotRemoteStream(MediaStream stream) {
        //we have remote video stream. add to the renderer.
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        runOnUiThread(() -> {
            try {
                remoteVideoView.setVisibility(View.VISIBLE);
                videoTrack.addSink(remoteVideoView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }*/

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        SignallingClient.getInstance().emitIceCandidate(iceCandidate);
    }

    /**
     * SignallingCallback - called when the room is created - i.e. you are the initiator
     */
    @Override
    public void onCreatedRoom() {
        showToast("You created the room " + gotUserMedia);

        setStatus("You have created the room. Ask parents to join using same id");
        if (gotUserMedia) {
            SignallingClient.getInstance().emitMessage("got user media");
        }
    /*    if (SignallingClient.getInstance().isInitiator) {
            onTryToStart();
        }*/
    }

    /**
     * SignallingCallback - called when you join the room - you are a participant
     */
    @Override
    public void onJoinedRoom() {
        showToast("You joined the room " + gotUserMedia);
        if (gotUserMedia) {
            SignallingClient.getInstance().emitMessage("got user media");
        }
    }

    @Override
    public void onRoomFull() {
        showToast("Room full");
        setStatus("Room have more than two users. Please try with new room id");
    }

    @Override
    public void onNewPeerJoined() {
        showToast("Remote Peer Joined");
        setStatus("New User joined to the room");
    }

    @Override
    public void onRemoteHangUp(String msg) {
        //showToast("Remote Peer hungup");
        runOnUiThread(MainActivity::hangup);
    }

    /**
     * SignallingCallback - Called when remote peer sends offer
     */
    @Override
    public void onOfferReceived(final JSONObject data) {
        showToast("Received Offer");
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                onTryToStart();
            }

            try {
                localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                doAnswer();
                //   updateVideoViews(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void doAnswer() {
        localPeer.createAnswer(new CustomSdpObserver("localCreateAns") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocal"), sessionDescription);
                SignallingClient.getInstance().emitMessage(sessionDescription);
            }
        }, new MediaConstraints());
    }

    /**
     * SignallingCallback - Called when remote peer sends answer to your offer
     */

    @Override
    public void onAnswerReceived(JSONObject data) {
        showToast("Received Answer");
        try {
            localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
            //   updateVideoViews(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote IceCandidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        try {
            localPeer.addIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

/*    private void updateVideoViews(final boolean remoteVisible) {
        runOnUiThread(() -> {
            ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
            if (remoteVisible) {
                params.height = dpToPx(100);
                params.width = dpToPx(100);
            } else {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            localVideoView.setLayoutParams(params);
        });
    }*/

    /**
     * Closing up - normal hangup and app destroye
     */

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.end_call) {
            hangup();
        } else if (id == R.id.start_call) {//startCall();
            if (edt_room_id.getText().toString().equals("")) {
                showToast("Please enter room id to start the call");
            } else {
                startScreenCapture();
            }
        }
    }

    /*    private void startCall() {
     *//*      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        } else {
            // all permissions already granted
            // start();
            startScreenCapture();
        }*//*
        startScreenCapture();
    }*/

    public static void hangup() {
        try {
            if (isMyServiceRunning(MediaProjectionService.class)) {
                Intent myService = new Intent(context, MediaProjectionService.class);
                context.stopService(myService);
            }
            hangup.setEnabled(false);
            setStatus("Call closed");
            startCall.setEnabled(true);
            SignallingClient.getInstance().close();
            if (localPeer != null) {
                localPeer.close();
            }
            localPeer = null;
            //     updateVideoViews(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hiding On destroy since our app will work on background
/*    @Override
    protected void onDestroy() {
        SignallingClient.getInstance().close();
        super.onDestroy();

        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
    }*/

    /**
     * Util Methods
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    public static void setStatus(final String msg) {
        //runOnUiThread(() -> txt_status.setText(msg));
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
