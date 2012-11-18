package com.example.trackit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener{

	private NotificationManager notifManager;
	private Notification notif;
	private NotificationCompat.Builder notifBuilder;
	
	protected LocationManager locManager;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		Toast.makeText(this, "Location service CREATED!", Toast.LENGTH_LONG).show();
		Log.e("LocationService", "Hello from the service!!");
		setupNotif();
		super.onCreate();

		setupLocationListener();	
	}
	
	@Override
	public void onDestroy(){
		Toast msg = Toast.makeText(this, "Location service DESTROYED!", Toast.LENGTH_LONG);
		msg.show();
		//Push something to the location server here to say updates have been disabled
	}
	
	private void setupLocationListener(){
		locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	}
	
	private void setupNotif(){
		Log.e("LocationService", "Setting up notifs...");
		notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifBuilder = new NotificationCompat.Builder(this);
		
		notifBuilder.setContentTitle("TrackiT Location");
		notifBuilder.setContentText("Some application information");
		notifBuilder.setSmallIcon(R.drawable.notif_icon);
		notifBuilder.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000 });
		
		
		notif = notifBuilder.build();
		notifManager.notify(0, notif);
		
		Log.e("LocationService", "...done!");
	}

	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		Toast.makeText(this, "Lat: " + lat + " Long: " + lng, Toast.LENGTH_LONG).show();
		
		Intent intent = new Intent("TrackiTLoc");
		Bundle b = new Bundle();
		b.putDouble("GEO_LONG", lng);
		b.putDouble("GEO_LAT", lat);
		
		intent.putExtras(b);
		sendBroadcast(intent);
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "TrackiT location services disabled - alerting parents!", Toast.LENGTH_LONG).show();
		//TODO Alert parents
	}

	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "New TrackiT location service enabled", Toast.LENGTH_LONG).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub	
	}

}
