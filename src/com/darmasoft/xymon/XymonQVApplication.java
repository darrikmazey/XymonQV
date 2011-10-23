package com.darmasoft.xymon;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class XymonQVApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = "XymonQVApplication";
	
	public SharedPreferences prefs;
	private XymonServer m_server = null;
	
	public XymonQVApplication() {
	}

	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "created");
	}
	
	public int update_interval() {
		return(Integer.valueOf(prefs.getString("update_interval", "0")));
	}
	
	public XymonServer xymon_server() {
		Log.d(TAG, "xymon_server()");
		if (m_server == null) {
			
			String hostname = prefs.getString("hostname", "www.xymon.org");
			boolean ssl = prefs.getBoolean("use_ssl", true);
			String username = prefs.getString("username", "");
			String password = prefs.getString("password", "");
			
			m_server = new XymonServer(hostname, ssl, username, password, this);
		}
		return(m_server);
	}
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "terminated");
	}
	
	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		m_server = null;
	}

}
