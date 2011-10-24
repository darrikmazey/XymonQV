package com.darmasoft.xymon;

import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class XymonQVService extends Service {

	static final String TAG = "XymonQVService";
	private boolean running = false;
	private Updater updater;
	private XymonServer server;
	
	private DBHelper dbHelper;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		this.updater = new Updater();
		
		this.dbHelper = new DBHelper(this);
				
		SharedPreferences prefs = ((XymonQVApplication) getApplication()).prefs;
		
		String hostname = prefs.getString("hostname", "www.xymon.org");
		boolean ssl = prefs.getBoolean("use_ssl", true);
		String username = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		
		server = new XymonServer(hostname, ssl, username, password, this);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Starting");
		super.onStartCommand(intent, flags, startId);
		if (!running) {
			running = true;
			this.updater.start();
		} else {
			Log.d(TAG, "Already running");
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "Stopping");
		this.updater.interrupt();
		super.onDestroy();
	}
	
	private class Updater extends Thread {
		public Updater() {
			super("Updater");
		}
		
		@Override
		public void run() {
			XymonQVService svc = XymonQVService.this;
			
			while (svc.running) {
				Log.d(TAG, "Updater running");
				try {
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
			       	svc.sendBroadcast(intent, "com.darmasoft.xymon.RECEIVE_DATA_NOTIFICATION");
			       	
					Log.d(TAG, "Updater ran");
					int delay = ((XymonQVApplication) getApplication()).update_interval();
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					svc.running = false;
				}
			}
		}
		
	}
}
