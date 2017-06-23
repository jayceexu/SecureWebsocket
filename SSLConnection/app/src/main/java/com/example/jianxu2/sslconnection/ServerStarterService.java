package com.example.jianxu2.sslconnection;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ServerStarterService extends Service {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final String TAG = "ServerStarterService";
    private static final int PORT = 8887;


    public ServerStarterService() {


    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "ServerStarterService::onCreate...");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ServerStarterService::onStartCommand...");

        new StartServer().execute();
        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class StartServer extends AsyncTask<Void, Void, Void> {
        private ChatServer mChatServer;

        @Override
        protected Void doInBackground(Void... params) {
            startSSLServer();

            return null;
        }

        private void startServer() {
            try {
                WebSocketImpl.DEBUG = true;
                int port = PORT; // 843 flash policy port
                mChatServer = new ChatServer(port);
                mChatServer.start();
                Log.i(TAG, "ChatServer started on port: " + mChatServer.getPort());

                BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    //String in = sysin.readLine();
                    //s.sendToAll(in);
                    Thread.sleep(1000);
                    mChatServer.sendToAll("Hello, I'm the server");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void startSSLServer() {
            WebSocketImpl.DEBUG = true;

            try {
                ChatServer chatserver = new ChatServer(PORT); // Firefox does allow multible ssl connection only via port 443 //tested on FF16

                // load up the key store
                String KEYSTORE = "keystore.bks";
                String STOREPASSWORD = "storepass";
                String KEYPASSWORD = "storepass";
                File sdCardDir = Environment.getExternalStorageDirectory();
                Log.i(TAG, "Sdcard dir: " + sdCardDir.getAbsolutePath());


                KeyStore ks = KeyStore.getInstance("BKS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");

                // Jian: Either loading from sdcard or *raw* directory
                File kf = new File(sdCardDir, KEYSTORE);
                ks.load(getApplicationContext().getResources().openRawResource(R.raw.keystore), STOREPASSWORD.toCharArray());
                //ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

                kmf.init(ks, KEYPASSWORD.toCharArray());
                tmf.init(ks);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                chatserver.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));

                chatserver.start();
//                while (true) {
//                    //String in = sysin.readLine();
//                    //s.sendToAll(in);
//                    Thread.sleep(1000);
//                    //chatserver.sendToAll("Hello, I'm the server");
//                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "Stopping server.");
            try {
                if (mChatServer != null)
                    mChatServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
