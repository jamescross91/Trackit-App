package com.example.trackit;

import android.content.Context;
import android.net.ConnectivityManager;

public class Network {	
	private Context thisContext;
	
	public Network(Context context){
		thisContext = context;
	}
	//Check if the device is connection to the network - either WiFi or ceulluar (3G/Edge/GPRS)
	private void isConnected(){
		ConnectivityManager connManager = (ConnectivityManager) thisContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
}
