package com.example.trackit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService{
	public GCMIntentService() {
		super("714729619832");
		}
		 
		private static final String TAG = "===GCMIntentService===";
		 
		 
		@Override
		protected void onRegistered(Context arg0, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		}
		 
		@Override
		protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "unregistered = "+arg1);
		}
		 
		@Override
		protected void onMessage(Context arg0, Intent arg1) {
		Log.i(TAG, "new message= ");
		
		Bundle bundle = arg1.getExtras();
		Log.i(TAG, "new message= ");
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
