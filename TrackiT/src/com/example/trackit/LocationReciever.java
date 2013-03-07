package com.example.trackit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.commonsware.cwac.locpoll.LocationPollerResult;

public class LocationReciever extends BroadcastReceiver{
	
	private Location currentBestLocation;
	private static final int ONE_MINUTE = 1000 * 60;

	@Override
	public void onReceive(Context context, Intent intent) {

		try{
		Toast msg1 = Toast.makeText(context, "Poller loc received",
				Toast.LENGTH_LONG);
		msg1.show();
		
		Log.i("Location poller", "Location recieved, attempting to push to server");
		
		Bundle b = intent.getExtras();
		
		LocationPollerResult locationResult = new LocationPollerResult(b);

		  Location loc=locationResult.getLocation();
		  String msg;

		  if (loc==null) {
		    loc=locationResult.getLastKnownLocation();

		    if (loc==null) {
		      msg=locationResult.getError();
		    }
		    else {
		      msg="TIMEOUT, lastKnown="+loc.toString();
		    }
		  }
		  else {
		    msg=loc.toString();
		  }

		  if (msg==null) {
		    msg="Invalid broadcast received!";
		  }

		  Log.i("LocationReciever", msg);
		  
		  //Process the location update
		  onLocationChanged(loc, context);
		} catch(Exception e){
			Log.e("Location poller", "An unhandled exception occured: " + e.getMessage());
		}
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

	private void onLocationChanged(Location location, Context context) {

		if (isBetterLocation(location)) {
			currentBestLocation = location;

			// Create a new location and attempt to push it to the server. This
			// is a network op so it must run in a new thread to avoid hanging
			// the UI
			TrackiTLocation newLoc = new TrackiTLocation(context, location);
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
			context.sendBroadcast(intent);
		}
	}

}
