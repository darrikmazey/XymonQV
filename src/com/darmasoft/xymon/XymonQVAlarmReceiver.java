package com.darmasoft.xymon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class XymonQVAlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "XymonQVAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		context.startService(new Intent(context, XymonQVService.class));
	}

}
