package com.example.trackit;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class LocationBoot extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i("Autostart", "**********started************");
		
		SharedPreferences auth = context.getSharedPreferences(
				context.getString(R.string.authentication), 0);

		boolean authenticated = auth.getBoolean("authenticated", false);

		if (authenticated) {
			Toast msg = Toast.makeText(context, "Device authenticated attempting to start service", Toast.LENGTH_LONG);
			msg.show();
			Intent gpsService = new Intent();
			gpsService.setAction("com.example.trackit.LocationService");
			ComponentName service = context.startService(gpsService);

			if (service == null)
				Log.e("Location Boot", "Failed to start GPS Service");
		}

		Log.w("Location Boot",
				"Device is not authenticated, the service will not be started");
		
		Toast msg = Toast.makeText(context, "Device NOT authenticated, service will not start", Toast.LENGTH_LONG);
		msg.show();
	}

}
