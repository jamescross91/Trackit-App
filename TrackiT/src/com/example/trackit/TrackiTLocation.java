package com.example.trackit;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TrackiTLocation extends Network {

	private Location location;
	private double latitude;
	private double longitude;
	private double accuracy;
	private double altitude; 
	private double bearing; 
	private double speed;
	private String authToken;
	
	public TrackiTLocation(Context context, Location location) {
		super(context);
		this.location = location;
		latitude = this.location.getLatitude();
		longitude = this.location.getLongitude();
		accuracy = this.location.getAccuracy();
		altitude = this.location.getAltitude();
		bearing = this.location.getBearing();
		speed = this.location.getSpeed();
		SharedPreferences auth = context.getSharedPreferences(
				context.getString(R.string.authentication), 0);
		 authToken = auth.getString("authToken", "");
	}
	
	public JSONObject toJSON(){
		JSONObject object = new JSONObject();
		try {
			object.put("latitude", latitude);
			object.put("longitude", longitude);
			object.put("accuracy", accuracy);
			object.put("altitude", altitude);
			object.put("bearing", bearing);
			object.put("speed", speed);
		} catch (JSONException e) {
			Log.e("JSON Conversion", e.getMessage());
		}
		
		return object;
	}
	
	private void persistLocation(){
		
		TelephonyManager telManager;
		telManager = (TelephonyManager) thisContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
		pairs.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
		pairs.add(new BasicNameValuePair("accuracy", String.valueOf(accuracy)));
		pairs.add(new BasicNameValuePair("altitude", String.valueOf(altitude)));
		pairs.add(new BasicNameValuePair("bearing", String.valueOf(bearing)));
		pairs.add(new BasicNameValuePair("speed", String.valueOf(speed)));		
		pairs.add(new BasicNameValuePair("deviceID", telManager.getDeviceId()));
		pairs.add(new BasicNameValuePair("authToken", authToken));
		
		networkExec(formatLocUrl(), pairs);
	}
	
	private String formatLocUrl(){
		String URL = thisContext.getString(R.string.server_root)
				+ thisContext.getString(R.string.location_url);

		return URL;
	}
	
	protected String doInBackground(String... params) {
		if(!isConnected())
			return null;
		
		persistLocation();
		return null;
	}
}
