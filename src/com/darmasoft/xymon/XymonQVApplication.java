package com.darmasoft.xymon;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class XymonQVApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = "XymonQVApplication";
	
	private SharedPreferences m_prefs;
	private XymonServer m_server = null;
	
	public XymonQVApplication() {
	}

	public void onCreate() {
		super.onCreate();
		this.m_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.m_prefs.registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "created");
	}
	
	public XymonServer xymon_server() {
		if (m_server == null) {
			
			String hostname = m_prefs.getString("hostname", "www.xymon.org");
			boolean ssl = m_prefs.getBoolean("use_ssl", true);
			String username = m_prefs.getString("username", "");
			String password = m_prefs.getString("password", "");
			
			m_server = new XymonServer(hostname, ssl, username, password);
		}
		return(m_server);
	}
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "terminated");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		m_server = null;
	}

}
