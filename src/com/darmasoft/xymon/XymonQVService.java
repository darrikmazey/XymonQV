package com.darmasoft.xymon;

import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class XymonQVService extends IntentService {

	static final String TAG = "XymonQVService";
	private XymonServer server;
	
	public static final String RECEIVE_DATA_NOTIFICATION = "com.darmasoft.xymon.RECEIVE_DATA_NOTIFICATION";
	
	private DBHelper dbHelper;

	public XymonQVService() {
		super(TAG);
		
		Log.d(TAG, "constructor");	
	}
	
	@Override
	protected void onHandleIntent(Intent incoming_intent) {
		Log.d(TAG, "Updater running");
		
		this.dbHelper = new DBHelper(this);
		
		SharedPreferences prefs = ((XymonQVApplication) getApplication()).prefs;
		
		String hostname = prefs.getString("hostname", "www.xymon.org");
		boolean ssl = prefs.getBoolean("use_ssl", true);
		String username = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		
		server = new XymonServer(hostname, ssl, username, password, this);

		server.refresh();
		Date last_updated = server.last_updated();
		String last_color = server.color();

		for (XymonHost host : server.hosts()) {

			dbHelper.insert(host, last_updated);

			for (XymonService s : host.services()) {
				dbHelper.insert(s, last_updated);
			}
		}

		dbHelper.insert_run(last_updated, last_color);

		Intent intent = new Intent("com.darmasoft.xymon.NEW_DATA");
		sendBroadcast(intent, RECEIVE_DATA_NOTIFICATION);

		Log.d(TAG, "Updater ran");
	}
}
