package com.example.trackit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public GCMIntentService() {
		super("714729619832");
	}

	private static final String TAG = "===GCMIntentService===";
	public static final String DEVICE_DELETE_KEY = "Delete";

	@Override
	protected void onRegistered(Context arg0, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "unregistered = " + arg1);
	}

	@Override
	protected void onMessage(Context context, Intent arg1) {
		try {
			Log.i(TAG, "new message= ");

			Bundle bundle = arg1.getExtras();
			Log.i(TAG, "new message= ");

			String deviceDelete = (String) bundle.get(DEVICE_DELETE_KEY);
			if (deviceDelete != null) {
				Intent deleteIntent = new Intent(
						context.getString(R.string.device_delete_broadcast_action));
				deleteIntent.putExtras(bundle);
				context.sendBroadcast(deleteIntent);

				Intent serviceIntent = new Intent(context,
						LocationService.class);
				stopService(serviceIntent);
				
				Intent serviceIntent2 = new Intent(context,
						com.commonsware.cwac.locpoll.LocationPollerService.class);
				stopService(serviceIntent2);
			}

			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = lm.getBestProvider(crit, true);
			Location loc = lm.getLastKnownLocation(provider);

			if (loc != null) {
				TrackiTLocation tiloc = new TrackiTLocation(context, loc);
				tiloc.execute(new String());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onError(Context arg0, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}
}
