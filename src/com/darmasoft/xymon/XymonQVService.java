package com.darmasoft.xymon;

import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class XymonQVService extends IntentService {

	static final String TAG = "XymonQVService";
	private XymonServer server;
	private Handler handler;
	
	public static final String RECEIVE_DATA_NOTIFICATION = "com.darmasoft.xymon.RECEIVE_DATA_NOTIFICATION";
	
	public XymonQVService() {
		super(TAG);
		
		Log.d(TAG, "constructor");
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	
		handler = new Handler();
	}

	private class ToastMessage implements Runnable {
		String m_text;
		
		public ToastMessage(String text) {
			Log.d(TAG, String.format("ToastMessage('%s')", text));
			m_text = text;
		}
		
		public void run() {
			Toast.makeText(XymonQVService.this, m_text, Toast.LENGTH_LONG).show();
		}
	}
	

	@Override
	protected void onHandleIntent(Intent incoming_intent) {
		Date runtime = new Date();
		Log.d(TAG, String.format("Updater running: %tF %tT", runtime, runtime));
		
		SharedPreferences prefs = ((XymonQVApplication) getApplication()).prefs;
		
		String hostname = prefs.getString("hostname", "www.xymon.org");
		boolean ssl = prefs.getBoolean("use_ssl", false);
		String username = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		
		try {
			server = new XymonServer(hostname, ssl, username, password, this);

			XymonQuery q = server.refresh();
			q.insert(this);
		} catch (XymonQVException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			handler.post(new ToastMessage(e.getMessage()));
		}			
		
		Intent intent = new Intent("com.darmasoft.xymon.NEW_DATA");
		sendBroadcast(intent, RECEIVE_DATA_NOTIFICATION);

		Log.d(TAG, "Updater ran");
		

	}
}
