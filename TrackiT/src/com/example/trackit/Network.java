package com.example.trackit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class Network extends AsyncTask<String, Void, String> {
	protected Context thisContext;

	public Network(Context context) {
		thisContext = context;
	}

	// Check if the device is connection to the network - either WiFi or
	// ceulluar (3G/Edge/GPRS)
	protected boolean isConnected() {
		ConnectivityManager connManager = (ConnectivityManager) thisContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}

	private void testDownload() {
		InputStream is = null;
		int len = 500;
		if (isConnected()) {
			try {
				URL url = new URL("http://5.126.59.12:2610/device/child/location");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				
				conn.connect();
				int response = conn.getResponseCode();
				Log.d(this.getClass().getName(), "The response is: " + response);
				is = conn.getInputStream();
				
				String contentAsString = readIt(is, len);
				System.out.println(contentAsString);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "UTF-8");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    return new String(buffer);
	}

	@Override
	protected String doInBackground(String... params) {
		testDownload();

		return null;
	}
	
}
