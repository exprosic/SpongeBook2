package com.example.exprosic.spongebook2.test;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exprosic.spongebook2.LoginActivity;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.URLManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TestEntryActivity extends Activity {
    private static final String TAG = TestEntryActivity.class.getSimpleName();
    private static final int PORT = 12345;
    private static final int TIMEOUT_MS = 50;
    private static final String REQUEST_MSG = "searching for server";
    private static final String RESPONSE_MSG = "i've heard you";

    @Bind(R.id.the_text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_entry);
        ButterKnife.bind(this);
//        BookInfoActivity.start(this, "jd,10592815");
//        MultiscanActivity.startAlone(this);
//        findServer();
        LoginActivity.startAlone(this);
//        sendBrodcast();
    }

    private void findServer() {
        final InetAddress broadcastAddress;
        final DatagramSocket senderSocket;
        final DatagramSocket receiverSocket;
        try {
            broadcastAddress = getBroadcastAddress();
            senderSocket = new DatagramSocket();
            senderSocket.setSoTimeout(TIMEOUT_MS);
            receiverSocket = new DatagramSocket(PORT);
        } catch (Exception e) {
            mTextView.setText(e.getMessage());
            return;
        }

        final Thread senderThread = new Thread() {
            @Override
            public void run() {
                final int tryTimes = 100;
                final int netMask = getNetMask();
                final int netAddress = getIpAddress() & ~netMask;
                byte[] data = REQUEST_MSG.getBytes();
                for (int i=0; i<tryTimes; ++i) {
                    for (int j=1; j<netMask; ++j) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, intToInetAddress(netAddress+j), PORT);
                        try {
                            senderSocket.send(packet);
                        } catch (IOException e) {
                            Log.e(TAG, "error while sending packet", e);
                        }
                    }
                }
            }
        };

        Thread receiverThread = new Thread() {
            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                byte[] data = new byte[1000];
                for (;;) {
                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        receiverSocket.receive(packet);
                        String msg = new String(packet.getData());
                        if (msg.equals(RESPONSE_MSG)) {
                            senderThread.stop();
                            URLManager.HOST = String.format(Locale.US, "http://%s:8000/", inet4ToString(packet.getAddress()));
                            break;
                        }
                    } catch (IOException e) {
                        /* pass */
                    }
                }
            }
        };
    }

    private void sendMulticast() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.acquire();

        try {
            MulticastSocket socket = new MulticastSocket();

        } catch (IOException e) {
            Log.e(TAG, "something went wrong", e);
        } finally {
            multicastLock.release();
        }
    }

    private void sendBrodcast() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String myIp = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        mTextView.setText(String.format(Locale.US, "my ip: %s", myIp));
        final InetAddress broadcastAddress;
        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255");
//            broadcastAddress = InetAddress.getByName("10.0.44.64");
//            broadcastAddress = InetAddress.getByName("10.0.47.255");
//            broadcastAddress = getBroadcastAddress();
            mTextView.setText(mTextView.getText()+", "+broadcastAddress.toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mTextView.setText("unknown host");
            return;
        }

        new Thread() {
            @Override
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                try {
                    final DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    String msg = "a message";
                    byte[] data = msg.getBytes();
                    for (; ; ) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, PORT);
                        Log.d(TAG, "packet created");
                        socket.send(packet);
                        Log.d(TAG, "data sent");
                        TestEntryActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "sent from "+socket.getLocalSocketAddress().toString();
                                Toast.makeText(TestEntryActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "something's wrong", e);
                }
            }
        }.start();
    }

    int getNetMask() {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        return dhcp.netmask;
    }

    int getIpAddress() {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        return dhcp.ipAddress;
    }


    private InetAddress intToInetAddress(int x) {
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((x >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            Log.e(TAG, "unknown host", e);
            return null;
        }
    }

    private int inetAddressToInt(InetAddress address) {
        byte[] bytes = address.getAddress();
        int val = 0;
        for (int i=0; i<bytes.length; ++i) {
            val <<= 8;
            val |= bytes[i] & 0xff;
        }
        return val;
    }

    private String inet4ToString(InetAddress address) {
        byte[] bytes = address.getAddress();
        return String.format(Locale.US, "%d.%d.%d.%d", bytes[3], bytes[2], bytes[1], bytes[0]);
    }
    InetAddress getBroadcastAddress() throws UnknownHostException {
        int ip = getIpAddress(), mask = getNetMask();
        int broadcast = (ip & mask) | ~mask;
        return intToInetAddress(broadcast);
    }
}
