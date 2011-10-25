package com.darmasoft.xymon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class XymonQVUpdateReceiver extends BroadcastReceiver {

	private static final String TAG = "XymonQVUpdateReceiver";
	
	private XymonQVActivity m_context;
	
	public XymonQVUpdateReceiver(XymonQVActivity context) {
		Log.d(TAG, "constructor");
		m_context = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		m_context.load_status();
	}

}
