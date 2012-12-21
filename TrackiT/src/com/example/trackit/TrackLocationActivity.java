package com.example.trackit;

import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		
		setContentView(R.layout.activity_track_location);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		mapOverlays = mapView.getOverlays();

		drawable = this.getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new MapOverlay(drawable, this);

		//GeoPoint point = new GeoPoint(19240000, -99120000);
		//OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!",
		//		"I'm in Mexico City!");

		//itemizedOverlay.addOverlay(overlayitem);
		//mapOverlays.add(itemizedOverlay);
		
		registerReceiver(receiver, new IntentFilter("TrackiTLoc"));
	}

	@Override
	protected void onPause() {
		super.onPause();
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

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("TrackiTLoc")) {

				double lng = intent.getExtras().getDouble("GEO_LONG");
				double lat = intent.getExtras().getDouble("GEO_LAT");

				GeoPoint point = new GeoPoint((int) (lat * 1E6),
						(int) (lng * 1E6));
				
				Date d = new Date();
				CharSequence s  = DateFormat.format("kk:mm on EEEE, MMMM d, yyyy ", d.getTime());
				
				OverlayItem overlayitem = new OverlayItem(point,
						"TrackiT Location",  "Sent to server at: " + s);

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);

				mapView.invalidate();

				Toast.makeText(context, "New location added to the map",
						Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	protected boolean isRouteDisplayed() {

		return false;
	}
}
