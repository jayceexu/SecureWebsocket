package com.example.jianxu2.sslconnection;

import android.util.Log;

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

	public WebSocketChatClient( URI serverUri ) {
		super( serverUri );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		Log.i(TAG, "Connected....." );

	}

	@Override
	public void onMessage( String message ) {
		Log.i(TAG, "got: " + message );
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
	/*
	 * Keystore with certificate created like so (in JKS format):
	 *
	 *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
	 */

}

