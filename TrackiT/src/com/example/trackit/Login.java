package com.example.trackit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Login extends Network {

	private String username;
	private String password;

	public Login(Context context, String username, String password) {
		super(context);
		this.username = username;
		this.password = password;
	}

	public HttpResponse authenticateLogin() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HttpResponse response = null;

		if (!isConnected()) {
			result.put("Connection status", false);
			result.put("Error cause", "Unable to connect to the internet");
		}

		HttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 1000);
		
		HttpPost post = new HttpPost(formatLoginURL());
		post.setHeader("User-Agent", "Custom Header");

		TelephonyManager telManager;
		telManager = (TelephonyManager) thisContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		//TODO use a JSON object instead
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", username));
		pairs.add(new BasicNameValuePair("password", password));
		pairs.add(new BasicNameValuePair("make", android.os.Build.MANUFACTURER));
		pairs.add(new BasicNameValuePair("model", android.os.Build.MODEL));
		pairs.add(new BasicNameValuePair("OS", "Android"));
		pairs.add(new BasicNameValuePair("phoneNumber", "0"));
		pairs.add(new BasicNameValuePair("deviceID", telManager.getDeviceId()));

		try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			response = client.execute(post);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	private String formatLoginURL() {
		String URL = thisContext.getString(R.string.server_root)
				+ thisContext.getString(R.string.login_url);

		return URL;
	}
	
	private boolean processResponse(HttpResponse response){
		String authToken = new String();
		boolean success = false;
		
		try {
			//Get the JSon response from the server
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			authToken = json.getString("authToken");
			success = json.getBoolean("loginSuccess");
		} catch (Exception e) {
			Log.e("Trackit Login", "Failed to parse Json object");
			e.printStackTrace();
		}
		
		//Did we logged in ok with the provided username and password?
		if(success && (authToken != new String())){
			//Save the auth token
			SharedPreferences prefs = thisContext.getSharedPreferences(thisContext.getString(R.string.authentication), 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("authToken", authToken);
			editor.putBoolean("authenticated", true);
			
			editor.commit();
		}
		
		return true;
	}

	protected String doInBackground(String... params) {
		HttpResponse response = authenticateLogin();
		String status = response.getStatusLine().toString();
		processResponse(response);
		
		return null;
	}
}

// protected HashMap<String, Object> formatResult(){
// HashMap<String, Object> result = new HashMap<String, Object>();
//
// //State of the connection to the target host
// result.put("Connection status", false);
// result.put("Error reason", "");
// result.put("Payload size", 0.0);
//
// return result;
// }