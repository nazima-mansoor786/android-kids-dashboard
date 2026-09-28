package com.rsb.vpnpoc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.rsb.vpnpoc.packetRebuild.PCapFileWriter;
import com.rsb.vpnpoc.socket.IProtectSocket;
import com.rsb.vpnpoc.socket.IReceivePacket;
import com.rsb.vpnpoc.socket.SocketDataPublisher;
import com.rsb.vpnpoc.socket.SocketNIODataService;
import com.rsb.vpnpoc.socket.SocketProtector;
import com.rsb.vpnpoc.transport.tcp.PacketHeaderException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class VPNService extends VpnService implements Handler.Callback,
        Runnable, IProtectSocket, IReceivePacket {
    private static final String TAG = "VPNService";
    private static final int MAX_PACKET_LEN = 1500;

    private Handler mHandler;
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    private boolean serviceValid;
    private SocketNIODataService dataService;
    private Thread dataServiceThread;
    private SocketDataPublisher packetbgWriter;
    private Thread packetQueueThread;
    private PCapFileWriter pcapOutput;
    private FileOutputStream timeStream;

    //
    public static final Companion Companion = new Companion();
    private static VPNService instance;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Companion.setInstance(this);
        String CHANNEL_ONE_ID = "com.rsb.vpnpoc";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Kids Dashboard")
                .setContentText("filtering blocked website")
                .setSmallIcon(R.drawable.filter)
                .setLargeIcon(icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notification.setChannelId(CHANNEL_ONE_ID);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        startForeground(100, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
            int reps = 0;
            while (mThread.isAlive()) {
                Log.i(TAG, "Waiting to exit " + ++reps);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Start a new session by creating a new thread.
        mThread = new Thread(this, "CaptureThread");
        mThread.start();
        return START_STICKY;
    }

    private void unregisterAnalyzerCloseCmdReceiver() {
        Log.d(TAG, "inside unregisterAnalyzerCloseCmdReceiver()");
        try {
            if (serviceCloseCmdReceiver != null) {
                unregisterReceiver(serviceCloseCmdReceiver);
                serviceCloseCmdReceiver = null;
                Log.d(TAG, "successfully unregistered serviceCloseCmdReceiver");
            }
        } catch (Exception e) {
            Log.d(TAG, "Ignoring exception in serviceCloseCmdReceiver", e);
        }
    }

    @Override
    public void onRevoke() {
        Log.i(TAG, "revoked!, user has turned off VPN");
        super.onRevoke();
    }

    /**
     * receive message to trigger termination of collection
     */
    private BroadcastReceiver serviceCloseCmdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            Log.d(TAG, "received service close cmd intent at " + System.currentTimeMillis());
            unregisterAnalyzerCloseCmdReceiver();
            serviceValid = false;
            stopSelf();
        }
    };

    @Override
    public ComponentName startService(Intent service) {
        Log.i(TAG, "startService(...)");
        return super.startService(service);
    }


    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG, "stopService(...)");

        serviceValid = false;
        //	closeTraceFiles();
        return super.stopService(name);
    }

    @Override
    public void protectSocket(Socket socket) {
        this.protect(socket);
    }

    @Override
    public void protectSocket(int socket) {
        this.protect(socket);
    }

    /**
     * called back from background thread when new packet arrived
     */
    @Override
    public void receive(byte[] packet) {
        if (pcapOutput != null) {
            try {
                pcapOutput.addPacket(packet, 0, packet.length, System.currentTimeMillis() * 1000000);
            } catch (IOException e) {
                Log.e(TAG, "pcapOutput.addPacket IOException :" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "overrun from capture: length:" + packet.length);
        }
    }

    /**
     * Close the packet trace file
     */
    private void closePcapTrace() {
        Log.i(TAG, "closePcapTrace()");
        if (pcapOutput != null) {
            pcapOutput.close();
            pcapOutput = null;
            Log.i(TAG, "closePcapTrace() closed");
        }
    }

    /**
     * onDestroy is invoked when user disconnects the VPN
     */
    @Override
    public void onDestroy() {
        Companion.setInstance(null);
        Log.i(TAG, "onDestroy()");
        serviceValid = false;

        unregisterAnalyzerCloseCmdReceiver();

        if (dataService != null)
            dataService.setShutdown(true);

        if (packetbgWriter != null)
            packetbgWriter.setShuttingDown(true);

        //	closeTraceFiles();

        if (dataServiceThread != null) {
            dataServiceThread.interrupt();
        }
        if (packetQueueThread != null) {
            packetQueueThread.interrupt();
        }

        try {
            if (mInterface != null) {
                Log.i(TAG, "mInterface.close()");
                mInterface.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "mInterface.close():" + e.getMessage());
            e.printStackTrace();
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
            int reps = 0;
            while (mThread.isAlive()) {
                Log.i(TAG, "Waiting to exit " + ++reps);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (reps > 5) {
                    break;
                }
            }
            mThread = null;
        }
        stopForeground(true);

    }

    @Override
    public void run() {
        Log.i(TAG, "running vpnService");
        SocketProtector protector = SocketProtector.getInstance();
        protector.setProtector(this);

        try {
            if (startVpnService()) {
                startCapture();
                Log.i(TAG, "Capture completed");
            } else {
                Log.e(TAG, "Failed to start VPN Service!");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "Closing Capture files");
    }

    /**
     * setup VPN interface.
     *
     * @return boolean
     * @throws IOException
     */
    boolean startVpnService() throws IOException {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null) {
            Log.i(TAG, "Using the previous interface");
            return false;
        }

        Log.i(TAG, "startVpnService => create builder");
        // Configure a builder while parsing the parameters.
        VpnService.Builder builder = new VpnService.Builder()
                .addAddress("10.120.0.1", 32)
                .addRoute("0.0.0.0", 0)
                .setSession("VPNPOC");
        mInterface = builder.establish();

        if (mInterface != null) {
            Log.i(TAG, "VPN Established:interface = " + mInterface.getFileDescriptor().toString());
            return true;
        } else {
            Log.d(TAG, "mInterface is null");
            return false;
        }
    }

    /**
     * start background thread to handle client's socket, handle incoming and outgoing packet from VPN interface
     *
     * @throws IOException
     */
    void startCapture() throws IOException {

        Log.i(TAG, "startCapture() :capture starting");

        // Packets to be sent are queued in this input stream.
        FileInputStream clientReader = new FileInputStream(mInterface.getFileDescriptor());

        // Packets received need to be written to this output stream.
        FileOutputStream clientWriter = new FileOutputStream(mInterface.getFileDescriptor());

        // Allocate the buffer for a single packet.
        ByteBuffer packet = ByteBuffer.allocate(MAX_PACKET_LEN);
        IClientPacketWriter clientPacketWriter = new ClientPacketWriterImpl(clientWriter);

        SessionHandler handler = SessionHandler.getInstance();
        handler.setWriter(clientPacketWriter);

        //background task for non-blocking socket
        dataService = new SocketNIODataService(clientPacketWriter);
        dataServiceThread = new Thread(dataService);
        dataServiceThread.start();

        //background task for writing packet data to pcap file
        packetbgWriter = new SocketDataPublisher();
        packetbgWriter.subscribe(this);
        packetQueueThread = new Thread(packetbgWriter);
        packetQueueThread.start();

        byte[] data;
        int length;
        serviceValid = true;
        while (serviceValid) {
            //read packet from vpn client
            data = packet.array();
            length = clientReader.read(data);
            if (length > 0) {
                //Log.d(TAG, "received packet from vpn client: "+length);
                try {
                    packet.limit(length);

                    handler.handlePacket(packet);
                } catch (PacketHeaderException e) {
                    Log.e(TAG, e.getMessage());
                }

                packet.clear();
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Failed to sleep: " + e.getMessage());
                }
            }
        }
        Log.i(TAG, "capture finished: serviceValid = " + serviceValid);
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Log.d(TAG, "handleMessage:" + getString(message.what));
            Toast.makeText(this.getApplicationContext(), message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void stopvpnService() {
        try {
            if (mInterface != null) {
                Log.i(TAG, "mInterface.close()");
                mInterface.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "mInterface.close():" + e.getMessage());
            e.printStackTrace();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void protectSocket(DatagramSocket socket) {
        this.protect(socket);
    }

    public static final class Companion {
        public final VPNService getInstance() {
            return VPNService.instance;
        }

        public final void setInstance(VPNService vpnService) {
            VPNService.instance = vpnService;
        }

        public final void stopService() {
            if (VPNService.instance != null)
                VPNService.instance.stopvpnService();
        }

    }
}
