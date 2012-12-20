package com.example.trackit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.commonsware.cwac.locpoll.LocationPollerParameter;

public class LocationService extends Service {

	private NotificationManager notifManager;
	private Notification notif;
	private NotificationCompat.Builder notifBuilder;

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

		AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, LocationPoller.class);

		Bundle bundle = new Bundle();
		LocationPollerParameter param = new LocationPollerParameter(bundle);
		param.setIntentToBroadcastOnCompletion(new Intent(this,
				LocationReciever.class));

		// Try GPS and then fall back to Network
		param.setProviders(new String[] { LocationManager.GPS_PROVIDER,
				LocationManager.NETWORK_PROVIDER });

		param.setTimeout(60 * 1000);
		intent.putExtras(bundle);

		PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, 0);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), 120 * 1000, pending);
	}

	@Override
	public void onDestroy() {
		Toast msg = Toast.makeText(this, "Location service DESTROYED!",
				Toast.LENGTH_LONG);
		msg.show();
		// Push something to the location server here to say updates have been
		// disabled
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
}
