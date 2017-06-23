package com.example.jianxu2.sslconnection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MainActivity extends AppCompatActivity {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    SSLContext mSslContext;
    WebSocketClient mWebSocketClient;
    private final static String TAG = "MainActivity";
    //private static final String SERVER = "ws://echo.websocket.org/";
    private static final String SERVER = "ws://localhost";
    private static int TIMEOUT = 6000;
    Button mStartBtn;
    EditText mServerText;
    EditText mClientText;
    Button mSendBtn;
    Button mConnBtn;
    EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate............");

        // Start server button
        mStartBtn = (Button) findViewById(R.id.launch_btn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Starting server.......");
                // Starting a background service to launch
                Intent intent = new Intent(getApplicationContext(), ServerStarterService.class);
                startService(intent);
            }
        });

        // Connect Button
        mConnBtn = (Button)findViewById(R.id.conn_btn);
        mConnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }

                Log.i(TAG, "Connecting the websocket........");
                try {
                    //WebSocket ws = connect();
                    //connectWebSocket();
                    connectSSLWebSocket();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mEditText = (EditText)findViewById(R.id.msg_txt);

        // Send Message Button
        mSendBtn = (Button)findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Sending message...........");
                String msg = mEditText.getText().toString();
                Log.i(TAG, "The message is " + msg);
                //sendMessage();
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                //ws.sendText(msg);
                mWebSocketClient.send(msg);
            }
        });
    }


    private void connectWebSocket() {
        URI uri;
        try {
            //uri = new URI(SERVER);
            uri = new URI("ws://localhost:8887");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");

            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                Log.i("WebSocket", "Receiving message: " + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(R.id.rcv_txt);
                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
        Log.i(TAG, "webSocket connected successfully.....");
    }


    private void connectSSLWebSocket() {
        WebSocketImpl.DEBUG = true;

        try {
            WebSocketChatClient chatclient = new WebSocketChatClient(new URI("wss://localhost:8887"));

//            // load up the key store
//            String STORETYPE = "BKS";
//            String KEYSTORE = "keystore.bks";
//            String STOREPASSWORD = "123456";
//            String KEYPASSWORD = "123456";
//            // Get the dir of SD Card
//            File sdCardDir = Environment.getExternalStorageDirectory();
//            Log.i(TAG, "Sdcard dir: " + sdCardDir.getAbsolutePath());
//
//            KeyStore ks = KeyStore.getInstance(STORETYPE);
//            File kf = new File(sdCardDir, KEYSTORE);
//            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());
//
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
//            kmf.init(ks, KEYPASSWORD.toCharArray());
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init(ks);
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//


            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            ks.load(getApplicationContext().getResources().openRawResource(R.raw.keystore), "storepass".toCharArray());
            kmf.init(ks, "storepass".toCharArray());


            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(getApplicationContext().getResources().openRawResource(R.raw.keystore), "storepass".toCharArray());
            tmf.init(ts);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);


            // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            chatclient.setSocket(sslContext.getSocketFactory().createSocket());

            chatclient.connectBlocking();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
//                String line = reader.readLine();
//                if (line.equals("close")) {
//                    chatclient.close();
//                } else {
//                    chatclient.send(line);
//                }
                Thread.sleep(1000);
                chatclient.send("Hello world");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage() {
        EditText editText = (EditText) findViewById(R.id.msg_txt);
        Log.i(TAG, "Sending message " + editText.getText().toString());
        mWebSocketClient.send(editText.getText().toString());

        editText.setText("");
    }

}
