package com.example.trackit;

import java.util.ArrayList;
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

import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;

public class TrackiTLocation extends Network {

	private Location location;
	private double latitude;
	private double longitude;
	private double accuracy;
	private double altitude; 
	private double bearing; 
	private double speed;
	
	public TrackiTLocation(Context context, Location location) {
		super(context);
		this.location = location;
		latitude = this.location.getLatitude();
		longitude = this.location.getLongitude();
		accuracy = this.location.getAccuracy();
		altitude = this.location.getAltitude();
		bearing = this.location.getBearing();
		speed = this.location.getSpeed();		
	}
	
	private void persistLocation(){
		HttpClient client = new MyHttpClient(thisContext);
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 1000);
		
		HttpPost post = new HttpPost(formatLocUrl());
		post.setHeader("User-Agent", "Custom Header");
		
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
		
		try{
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (Exception e){
			e.printStackTrace();
		}
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
