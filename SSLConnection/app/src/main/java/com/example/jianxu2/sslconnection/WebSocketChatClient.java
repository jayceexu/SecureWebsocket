package com.example.jianxu2.sslconnection;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

class WebSocketChatClient extends WebSocketClient {

	private static final String TAG = "WebSocketChatClient";

	private Activity mActivity;

	public WebSocketChatClient(URI serverUri, Activity activity) {
		super(serverUri);
		mActivity = activity;
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		Log.i(TAG, "Connected....." );

	}

	@Override
	public void onMessage( String message ) {
		final String msg = message;
		Log.i(TAG, "got: " + message );
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView textView = (TextView)mActivity.findViewById(R.id.rcv_txt);
				textView.setText(textView.getText() + "\n" + msg);
			}
		});
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		Log.i(TAG, "Disconnected...." );
		System.exit( 0 );
	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();

	}

}

