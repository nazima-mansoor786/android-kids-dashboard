package com.tabnova.screenshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
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

public class StartStopScreenShare {

    public static Context context;

    static PeerConnectionFactory peerConnectionFactory;
    static MediaConstraints audioConstraints;
    static MediaConstraints videoConstraints;
    static MediaConstraints sdpConstraints;
    static VideoSource videoSource;
    static VideoTrack localVideoTrack;
    static AudioSource audioSource;
    static AudioTrack localAudioTrack;
    static SurfaceTextureHelper surfaceTextureHelper;
    static VideoCapturer videoCapturer;
    private static volatile boolean sessionActive;
    private static volatile int captureSessionId;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static PeerConnection localPeer;
    static List<IceServer> iceServers;
    static EglBase rootEglBase;

    static boolean gotUserMedia;
    static List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();

    final int ALL_PERMISSIONS_CODE = 1;

    private static final String TAG = "MainActivity";

    final int CAPTURE_PERMISSION_REQUEST_CODE = 100;
    public static int mMediaProjectionPermissionResultCode;
    int sDeviceWidth;
    int sDeviceHeight;
    public static Intent mMediaProjectionPermissionResultData;

    public static String portNumber = "";

    public static Handler applicationHandler;

    public StartStopScreenShare() {
    }

    private static void initVideos() {
        rootEglBase = EglBase.create();
        // localVideoView.init(rootEglBase.getEglBaseContext(), null);
        //  remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        //    localVideoView.setZOrderMediaOverlay(true);
        //    remoteVideoView.setZOrderMediaOverlay(true);
    }

    private static void getIceServers() {
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
            }

            @Override
            public void onFailure(@NonNull Call<TurnServerPojo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Starts a screen-share session. Called only by MediaProjectionService after it is in the
     * foreground with type mediaProjection, using a fresh consent result (single use).
     */
    static void start(int resultCode, Intent resultData) {
        mMediaProjectionPermissionResultCode = resultCode;
        mMediaProjectionPermissionResultData = resultData;
        sessionActive = true;
        Log.e(TAG, "start: " + portNumber);
        SignallingClient.getInstance().initroomname(portNumber);
        initVideos();
        getIceServers();

        //Initialize PeerConnectionFactory globals.
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
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
        videoCapturerAndroid = createScreenCapture();
        videoCapturer = videoCapturerAndroid;

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
            videoCapturerAndroid.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
        }
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        //   audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        //    localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(2048, 2048, 30);
        }

        gotUserMedia = true;
        if (SignallingClient.getInstance().isInitiator) {
            signalingInterface.onTryToStart();
        }

        SignallingClient.getInstance().init(signalingInterface);
    }

    private static VideoCapturer createScreenCapture() {
        if (mMediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            showToast("User didn't give permission to capture the screen.");
            return null;
        }
        final int sessionId = ++captureSessionId;
        Intent resultData = mMediaProjectionPermissionResultData;
        // The consent token is single use: clear it as soon as it is handed to the capturer.
        clearConsent();
        return new ScreenCapturerAndroid(
                resultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                showToast("User revoked permission to capture the screen.");
                // Only end the session when the current projection was stopped by the user or system,
                // not when closeSession() stopped it on purpose. Off the capture thread, because
                // hangup() blocks while signalling closes.
                if (sessionActive && sessionId == captureSessionId) {
                    new Thread(StartStopScreenShare::hangup).start();
                }
            }
        });
    }

    private static void doAnswer() {
        Log.e(TAG, "doAnswer: ");
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
     * Creating the local peer connection instance
     */
    private static void createPeerConnection() {
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
    private static void addStreamToLocalPeer() {
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
    private static void doCall() {
        Log.e(TAG, "doCall: ");
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

    public static void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        SignallingClient.getInstance().emitIceCandidate(iceCandidate);
    }

    public static synchronized void hangup() {
        try {
            /*context.stopService(new Intent(context, MediaProjectionService.class));
            setStatus("Call closed");
            */

            closeSession();
            if (context != null && isMyServiceRunning(MediaProjectionService.class)) {
                context.stopService(new Intent(context, MediaProjectionService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** True between start() and closeSession(). */
    public static boolean isSessionActive() {
        return sessionActive;
    }

    /**
     * Ends the current session: stops capture, then closes signalling and the peer connection.
     * Does not stop MediaProjectionService. Safe to call more than once.
     */
    public static synchronized void closeSession() {
        if (!sessionActive) {
            return;
        }
        sessionActive = false;
        releaseCapture();
        final PeerConnection peer = localPeer;
        try {
            if (SignallingClient.getInstance() != null) {
                SignallingClient.getInstance().isStarted = false;
                SignallingClient.getInstance().close();
            }
            if (localPeer != null) {
                localPeer.close();
            }
            localPeer = null;
            //     updateVideoViews(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        disposeMedia(peer);
    }

    /**
     * Frees the per-session WebRTC objects created in start(), once, in dependency order:
     * peer connection (which also disposes its local stream and track), the track if no peer
     * owned it, the video source, the factory, then the EGL context.
     */
    private static void disposeMedia(PeerConnection peer) {
        try {
            if (peer != null) {
                peer.dispose();
            } else if (localVideoTrack != null) {
                localVideoTrack.dispose();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        localPeer = null;
        localVideoTrack = null;
        try {
            if (videoSource != null) {
                videoSource.dispose();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        videoSource = null;
        try {
            if (peerConnectionFactory != null) {
                peerConnectionFactory.dispose();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        peerConnectionFactory = null;
        try {
            if (rootEglBase != null) {
                rootEglBase.release();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        rootEglBase = null;
    }

    /** Stops and disposes the screen capturer (ends the MediaProjection) and clears the consent token. */
    private static void releaseCapture() {
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        clearConsent();
    }

    /** Discards any stored MediaProjection consent result (deny, hangup, or after single use). */
    public static void clearConsent() {
        mMediaProjectionPermissionResultCode = 0;
        mMediaProjectionPermissionResultData = null;
    }

    public static void showToast(final String msg) {
        if (context == null) {
            return;
        }
        mainHandler.post(() -> {
            Log.e(TAG, "showToast: " + msg);
            //if (BuildConfig.DEBUG) {
                Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
           // }
        });
    }

    public static void setStatus(final String msg) {
        Log.e(TAG, "setStatus: " + msg);
    }

    static SignallingClient.SignalingInterface signalingInterface = new SignallingClient.SignalingInterface() {
        @Override
        public void onRemoteHangUp(String msg) {
            Log.e(TAG, "onRemoteHangUp: " + msg);
            // Off the main thread: SignallingClient.close() sleeps while it says "bye".
            new Thread(StartStopScreenShare::hangup).start();
        }

        @Override
        public void onOfferReceived(JSONObject data) {
            Log.e(TAG, "onOfferReceived: ");
            showToast("Received Offer");
            mainHandler.post(() -> {
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

        @Override
        public void onAnswerReceived(JSONObject data) {
            Log.e(TAG, "onAnswerReceived: ");
            showToast("Received Answer");
            try {
                localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
                //   updateVideoViews(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidateReceived(JSONObject data) {
            Log.e(TAG, "onIceCandidateReceived: ");
            try {
                localPeer.addIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTryToStart() {
            Log.e(TAG, "onTryToStart: ");
            mainHandler.post(() -> {
                if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                    createPeerConnection();
                    SignallingClient.getInstance().isStarted = true;
                    //  if (SignallingClient.getInstance().isInitiator) {
                    doCall();
                    //  }
                } else {
                    if (SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                        doCall();
                    }
                }
            });
        }

        @Override
        public void onCreatedRoom() {
            Log.e(TAG, "onCreatedRoom: ");
            showToast("You created the room " + gotUserMedia);

            setStatus("You have created the room. Ask parents to join using same id");
            if (gotUserMedia) {
                SignallingClient.getInstance().emitMessage("got user media");
            }
        }

        @Override
        public void onJoinedRoom() {
            Log.e(TAG, "onJoinedRoom: ");
            showToast("You joined the room " + gotUserMedia);
            if (gotUserMedia) {
                SignallingClient.getInstance().emitMessage("got user media");
            }
        }

        @Override
        public void onRoomFull() {
            Log.e(TAG, "onRoomFull: ");
            showToast("Room full");
            setStatus("Room have more than two users. Please try with new room id");
        }

        @Override
        public void onNewPeerJoined() {
            Log.e(TAG, "onNewPeerJoined: ");
            showToast("Remote Peer Joined");
            setStatus("New User joined to the room");
        }
    };

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
    }
}
