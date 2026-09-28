package com.rsb.vpnpoc.socket;

import android.os.Environment;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.rsb.vpnpoc.IClientPacketWriter;
import com.rsb.vpnpoc.MySingletonClass;
import com.rsb.vpnpoc.Session;
import com.rsb.vpnpoc.SessionManager;
import com.rsb.vpnpoc.transport.tcp.TCPPacketFactory;
import com.rsb.vpnpoc.util.PacketUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;

import static com.rsb.vpnpoc.MySingletonClass.build;

public class SocketDataWriterWorker implements Runnable {
    private static final String TAG = "SocketDataWriterWorker";

    private static IClientPacketWriter writer;
    @NonNull
    private String sessionKey;

    SocketDataWriterWorker(IClientPacketWriter writer, @NonNull String sessionKey) {
        this.writer = writer;
        this.sessionKey = sessionKey;
    }

    @Override
    public void run() {
        final Session session = SessionManager.INSTANCE.getSessionByKey(sessionKey);
        if (session == null) {
            Log.d(TAG, "No session related to " + sessionKey + "for write");
            return;
        }

        session.setBusywrite(true);

        AbstractSelectableChannel channel = session.getChannel();
        if (channel instanceof SocketChannel) {
            writeTCP(session);
        } else if (channel instanceof DatagramChannel) {
            writeUDP(session);
        } else {
            return;
        }
        session.setBusywrite(false);

        if (session.isAbortingConnection()) {
            Log.d(TAG, "removing aborted connection -> " + sessionKey);
            session.getSelectionKey().cancel();

            if (channel instanceof SocketChannel) {
                try {
                    SocketChannel socketChannel = (SocketChannel) channel;
                    if (socketChannel.isConnected()) {
                        socketChannel.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (channel instanceof DatagramChannel) {
                try {
                    DatagramChannel datagramChannel = (DatagramChannel) channel;
                    if (datagramChannel.isConnected()) {
                        datagramChannel.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            SessionManager.INSTANCE.closeSession(session);
        }
    }

    private void writeUDP(Session session) {
        if (!session.hasDataToSend()) {
            return;
        }
        DatagramChannel channel = (DatagramChannel) session.getChannel();
        String name = PacketUtil.intToIPAddress(session.getDestIp()) + ":" + session.getDestPort() +
                "-" + PacketUtil.intToIPAddress(session.getSourceIp()) + ":" + session.getSourcePort();
        byte[] data = session.getSendingData();
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data);
        buffer.flip();
        try {
            String str = new String(data);
            Log.d(TAG, "****** data write to server ********");
            Log.d(TAG, str);
            Log.d(TAG, "***** end writing to server *******");
            Log.d(TAG, "writing data to remote UDP: " + name);
            channel.write(buffer);
            Date dt = new Date();
            session.connectionStartTime = dt.getTime();
        } catch (NotYetConnectedException ex2) {
            session.setAbortingConnection(true);
            Log.e(TAG, "Error writing to unconnected-UDP server, will abort current connection: " + ex2.getMessage());
        } catch (IOException e) {
            session.setAbortingConnection(true);
            e.printStackTrace();
            Log.e(TAG, "Error writing to UDP server, will abort connection: " + e.getMessage());
        }
    }

    private void writeTCP(Session session) {
        SocketChannel channel = (SocketChannel) session.getChannel();
        String name = PacketUtil.intToIPAddress(session.getDestIp()) + ":" + session.getDestPort() +
                "-" + PacketUtil.intToIPAddress(session.getSourceIp()) + ":" + session.getSourcePort();
        byte[] data = session.getSendingData();

        //Code to filter host
        String url = new String(data);
        System.out.println(TAG + "writeTCP-----> " + url);
	/*	boolean valid = DomainUtils.isValidDomainName(url); //check if domain name is valid or not
		System.out.println("Valid domain name : " + valid);
	InetAddress addr = null;
		try {
			addr = InetAddress.getByName(PacketUtil.intToIPAddress(session.getDestIp()));
			String host = addr.getHostName();
			System.out.println(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}*/

		/*		if(url.contains("google.com")){
			return;
		}*/

		/*
		//ByteBuffer packet_buffer = packet.backingBuffer;
		stream.mark();
		byte[] tmp_bytes = new byte[stream.remaining()];
		stream.get(tmp_bytes);
		stream.reset();
		Message message = null;
		try {
			message = new Message(tmp_bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Name query_domain = message.getQuestion().getName();
		String query_string = query_domain.toString();

*/
	/*		ArrayList<String> num_words = MySingletonClass.getInstance().getValue();
		appendLog("VPNPOC number of blocked host " +  num_words.size());
		appendLog((java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) +"VPNPOC URL access by device " +  url.replaceAll("[^a-zA-Z0-9]", " ")  ));
	for (int i = 0; i < num_words.size(); i++) {
		//	System.out.println(TAG+"host checked-----> " + num_words.get(i));
			Log.d(TAG,"VPNPOC checking blocked host " +  num_words.get(i));
			appendLog("VPNPOC checking blocked host " +  num_words.get(i));
			if(url.contains(num_words.get(i)))
			{
				Log.d(TAG,"VPNPOC detected blocked host " + name);
				appendLog("VPNPOC detected blocked host " + name);
				return;
			}
		}*/

        //	List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(url);
        while (m.find()) {
            String url1 = m.group();
            Log.e(TAG, "VPNPOC url " + url1);
			/*String url = m.group();
			Log.d(TAG, "URL extracted: " + url);
			links.add(url);*/
            if (MySingletonClass.getInstance().isBlocked(url1)) {
                Log.e(TAG, "VPNPOC url detected blocked host" + url1);
                //	appendLog("VPNPOC detected blocked host " + url1);
                return;
            }
        }

        ArrayList<String> num_words = MySingletonClass.getInstance().getValue();
        appendLog("VPNPOC number of blocked host " + num_words.size());
        appendLog((java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "VPNPOC URL access by device " + url.replaceAll("[^a-zA-Z0-9]", " ")));
        for (int i = 0; i < num_words.size(); i++) {
            //	System.out.println(TAG+"host checked-----> " + num_words.get(i));
            Log.d(TAG, "VPNPOC checking blocked host " + num_words.get(i));
            appendLog("VPNPOC checking blocked host " + num_words.get(i));
            if (url.toLowerCase().contains(num_words.get(i).toLowerCase())) {
                Log.e(TAG, "VPNPOC detected blocked host " + name);
                appendLog("VPNPOC detected blocked host " + name);
                return;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data);
        buffer.flip();

        try {
            Log.d(TAG, "writing TCP data to: " + name);
            channel.write(buffer);
            //Log.d(TAG,"finished writing data to: "+name);
        } catch (NotYetConnectedException ex) {
            Log.e(TAG, "failed to write to unconnected socket: " + ex.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error writing to server: " + e.getMessage());

            //close connection with vpn client
            byte[] rstData = TCPPacketFactory.createRstData(
                    session.getLastIpHeader(), session.getLastTcpHeader(), 0);
            try {
                writer.write(rstData);
                SocketData socketData = SocketData.getInstance();
                socketData.addData(rstData);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //remove session
            Log.e(TAG, "failed to write to remote socket, aborting connection");
            session.setAbortingConnection(true);
        }
    }

    public void appendLog(String text) {

        if (build.equals("debug")) {

            //File logFile = new File("Android/log.file");
            File logFile = new File(Environment.getExternalStorageDirectory(), "Android/VPNPOC_log.txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
