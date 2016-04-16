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

    @Bind(R.id.the_text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_entry);
        ButterKnife.bind(this);
//        BookInfoActivity.startWithBookId(this, "jd,10592815");
//        MultiscanActivity.startAlone(this);
        LoginActivity.startAlone(this);
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
//            broadcastAddress = InetAddress.getByName("255.255.255.255");
            broadcastAddress = InetAddress.getByName("10.0.44.64");
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

    private void findHost() {
        Thread sender = new Thread() {
            @Override
            public void run() {

            }
        };
    }

    InetAddress getBroadcastAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
