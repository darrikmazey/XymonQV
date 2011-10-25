package com.darmasoft.xymon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class XymonQVBootReceiver extends BroadcastReceiver {

	private static final String TAG = "XymonQVBootReceiver";
	
	@Override
	public void onReceive(Context context, Intent incomingIntent) {
		Log.d(TAG, "onReceive()");
		Log.d(TAG, "ACTION: " + incomingIntent.getAction());
		
		XymonQVApplication app = ((XymonQVApplication) context.getApplicationContext());
		app.setIntentForCurrentInterval();
	}

}
