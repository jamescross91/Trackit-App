package com.example.trackit;

import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TrackLocationActivity extends MapActivity {

	protected Drawable drawable;
	protected MapOverlay itemizedOverlay;
	protected List<Overlay> mapOverlays;
	protected MapView mapView;
	private MapActivity thisActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.activity_track_location);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.icon);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		mapOverlays = mapView.getOverlays();

		drawable = this.getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new MapOverlay(drawable, this);

		registerReceiver(receiver, new IntentFilter("TrackiTLoc"));
		registerReceiver(receiver, new IntentFilter("DeviceDelete"));
		thisActivity = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter("TrackiTLoc"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_track_location, menu);
		return true;
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
	
	private boolean isServiceRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(LocationService.class.getName().equals(service.service.getClassName()))
				return true;
		}
		
		return false;
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("TrackiTLoc")) {

				mapOverlays.clear();
				itemizedOverlay.clearOverlays();
				double lng = intent.getExtras().getDouble("GEO_LONG");
				double lat = intent.getExtras().getDouble("GEO_LAT");

				GeoPoint point = new GeoPoint((int) (lat * 1E6),
						(int) (lng * 1E6));

				Date d = new Date();
				CharSequence s = DateFormat.format(
						"kk:mm on EEEE, MMMM d, yyyy ", d.getTime());

				OverlayItem overlayitem = new OverlayItem(point,
						"TrackiT Location", "Sent to server at: " + s);

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);

				mapView.invalidate();

				Toast.makeText(context, "New location added to the map",
						Toast.LENGTH_LONG).show();
			}

			if (intent.getAction().compareTo(
					context.getString(R.string.device_delete_broadcast_action)) == 0) {

				SharedPreferences prefs = context.getSharedPreferences(
						context.getString(R.string.authentication), 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("authToken");
				editor.remove("device_id");
				editor.remove("authenticated");
				editor.commit();
				
				if(!isServiceRunning()){
					Intent serviceIntent = new Intent(thisActivity, LocationService.class);
					startService(serviceIntent);
				}

				Intent loginIntent = new Intent(context, MainActivity.class);
				startActivity(loginIntent);
			}
		}
	};

	@Override
	protected boolean isRouteDisplayed() {

		return false;
	}
}
