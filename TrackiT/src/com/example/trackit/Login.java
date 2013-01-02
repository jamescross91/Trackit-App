package com.example.trackit;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gcm.GCMRegistrar;

public class Login extends Network {

	private String username;
	private String password;
	private ProgressBar bar;
	private EditText userField;
	private EditText passField;
	private Button loginButton;

	public Login(Context context, String username, String password,
			ProgressBar bar, EditText userField, EditText passField,
			Button loginButton) {
		super(context);
		this.username = username;
		this.password = password;
		this.bar = bar;
		this.userField = userField;
		this.passField = passField;
		this.loginButton = loginButton;
	}

	public HttpResponse authenticateLogin() {
		TelephonyManager telManager;
		telManager = (TelephonyManager) thisContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		GCMRegistrar.checkDevice(thisContext);
		GCMRegistrar.checkManifest(thisContext);
		final String regId = GCMRegistrar.getRegistrationId(thisContext);
		if (regId.equals("")) {
		  GCMRegistrar.register(thisContext, thisContext.getString(R.string.gcm_project_id));
		} else {
		  Log.v("GCM", "Already registered");
		}
		
		String reg = GCMRegistrar.getRegistrationId(thisContext);
		
		// TODO use a JSON object instead
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", username));
		pairs.add(new BasicNameValuePair("password", password));
		pairs.add(new BasicNameValuePair("make", android.os.Build.MANUFACTURER));
		pairs.add(new BasicNameValuePair("model", android.os.Build.MODEL));
		pairs.add(new BasicNameValuePair("OS", "Android"));
		pairs.add(new BasicNameValuePair("phoneNumber", "0"));
		pairs.add(new BasicNameValuePair("deviceID", telManager.getDeviceId()));
		pairs.add(new BasicNameValuePair("gcm_token", reg));
		
		return networkExec(formatLoginURL(), pairs);
	}

	private String formatLoginURL() {
		String URL = thisContext.getString(R.string.server_root)
				+ thisContext.getString(R.string.login_url);

		return URL;
	}

	private boolean processResponse(HttpResponse response) {
		String authToken = new String();
		boolean success = false;

		try {
			// Get the JSon response from the server
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject json = new JSONObject(responseBody);
			authToken = json.getString("authToken");
			success = json.getBoolean("loginSuccess");
		} catch (Exception e) {
			Log.e("Trackit Login", "Failed to parse Json object");
			e.printStackTrace();
		}

		// Did we log in ok with the provided username and password?
		if (success && (authToken.compareTo("") != 0)) {
			// Save the auth token
			SharedPreferences prefs = thisContext.getSharedPreferences(
					thisContext.getString(R.string.authentication), 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("authToken", authToken);
			editor.putBoolean("authenticated", true);

			editor.commit();
		} else {
			SharedPreferences prefs = thisContext.getSharedPreferences(
					thisContext.getString(R.string.authentication), 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("authenticated", false);

			editor.commit();
		}

		return true;
	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) thisContext
				.getSystemService(Context.ACTIVITY_SERVICE);

		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (LocationService.class.getName().equals(
					service.service.getClassName()))
				return true;
		}

		return false;
	}

	@Override
	protected void onPreExecute() {
		userField.setVisibility(View.INVISIBLE);
		passField.setVisibility(View.INVISIBLE);
		loginButton.setVisibility(View.INVISIBLE);
		bar.setVisibility(View.VISIBLE);
	}

	@Override
	protected String doInBackground(String... params) {
		HttpResponse response = authenticateLogin();
		// String status = response.getStatusLine().toString();
		processResponse(response);

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		SharedPreferences auth = thisContext.getSharedPreferences(
				thisContext.getString(R.string.authentication), 0);
		boolean authenticated = auth.getBoolean("authenticated", false);

		if (authenticated) {
			// If the location service is not already running, run it!
			if (!isServiceRunning()) {
				Intent serviceIntent = new Intent(thisContext,
						LocationService.class);
				thisContext.startService(serviceIntent);
			}

			Intent intent = new Intent(thisContext, TrackLocationActivity.class);
			thisContext.startActivity(intent);
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					thisContext);

			alertDialogBuilder.setTitle("Invalid Username or Password");
			alertDialogBuilder.setMessage("Please check and try again.");
			// alertDialogBuilder.setCancelable(true);
			alertDialogBuilder.setNegativeButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

			userField.setVisibility(View.VISIBLE);
			passField.setVisibility(View.VISIBLE);
			loginButton.setVisibility(View.VISIBLE);
			bar.setVisibility(View.INVISIBLE);
		}
	}
}
