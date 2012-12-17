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

public class LocationService extends Service implements LocationListener {

	private NotificationManager notifManager;
	private Notification notif;
	private NotificationCompat.Builder notifBuilder;
	private Location currentBestLocation;

	private static final int ONE_MINUTE = 1000 * 60 * 2;

	protected LocationManager locManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "Location service CREATED!", Toast.LENGTH_LONG)
				.show();
		Log.e("LocationService", "Hello from the service!!");
		setupNotif();
		super.onCreate();

		setupLocationListener();
	}

	@Override
	public void onDestroy() {
		Toast msg = Toast.makeText(this, "Location service DESTROYED!",
				Toast.LENGTH_LONG);
		msg.show();
		// Push something to the location server here to say updates have been
		// disabled
	}

	//Check the location providers are available and if they are register listeners for each of them
	private void setupLocationListener() {
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					60000, 50, this);

		if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					60000, 50, this);
	}

	private void setupNotif() {
		Log.e("LocationService", "Setting up notifs...");
		notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifBuilder = new NotificationCompat.Builder(this);

		notifBuilder.setContentTitle("TrackiT Location");
		notifBuilder.setContentText("Some application information");
		notifBuilder.setSmallIcon(R.drawable.icon);
		notifBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

		notif = notifBuilder.build();
		notifManager.notify(0, notif);

		Log.e("LocationService", "...done!");
	}

	private boolean isBetterLocation(Location newLocation) {
		if (currentBestLocation == null)
			return true;

		// Check the timing of the new location fix
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
		boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
		boolean isNewer = timeDelta > 0;

		// If it's more than one minute since the current best location use the
		// new one as a user has likely moved
		if (isSignificantlyNewer)
			return true;
		// If the new location is more than a minute older than the current best
		// then it must be worse
		else if (isSignificantlyOlder)
			return false;

		// Check whether the new location is more or less accurate than the
		// current best
		int accuracyDelta = (int) newLocation.getAccuracy()
				- (int) currentBestLocation.getAccuracy();
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the locations are from the same provider
		boolean isSameProvider = isSameProvider(newLocation.getProvider(),
				currentBestLocation.getProvider());

		// Determine quality taking timeliness and accuracy into account
		if (isMoreAccurate)
			return true;
		else if (isNewer && !isLessAccurate)
			return true;
		else if (isNewer && !isSignificantlyLessAccurate && isSameProvider)
			return true;

		return false;
	}

	private boolean isSameProvider(String prov1, String prov2) {
		if (prov1 == null)
			return prov2 == null;

		return prov1.equals(prov2);
	}

	public void onLocationChanged(Location location) {

		if (isBetterLocation(location)) {
			currentBestLocation = location;

			// Create a new location and attempt to push it to the server. This
			// is a network op so it must run in a new thread to avoid hanging
			// the UI
			TrackiTLocation newLoc = new TrackiTLocation(this, location);
			String[] array = new String[10];
			newLoc.execute(array);

			double lat = location.getLatitude();
			double lng = location.getLongitude();

			// Send out a broadcast, the application will pick this up
			Intent intent = new Intent("TrackiTLoc");
			Bundle b = new Bundle();
			b.putDouble("GEO_LONG", lng);
			b.putDouble("GEO_LAT", lat);

			intent.putExtras(b);
			sendBroadcast(intent);
		}
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(this,
				"TrackiT location services disabled - alerting parents!",
				Toast.LENGTH_LONG).show();
		// TODO Alert parents
	}

	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "New TrackiT location service enabled",
				Toast.LENGTH_LONG).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

}
