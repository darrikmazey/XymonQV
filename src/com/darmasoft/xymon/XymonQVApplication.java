package com.darmasoft.xymon;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class XymonQVApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = "XymonQVApplication";
	public static final String CRITICAL = "critical";
	public static final String NON_GREEN = "non_green";
	
	public static int INTERVAL_NEVER = 0;
	
	public SharedPreferences prefs;
	private XymonServer m_server = null;
	
	public XymonQVApplication() {
	}

	public long getInterval() {
		String sint = prefs.getString("update_interval", "0");
		long interval = Long.parseLong(sint);
		return(interval);
	}
	
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		
		boolean dbg = prefs.getBoolean("enable_debug_log", false);
		Log.set_debug_mode(dbg);
		
		Log.i(TAG, "created");
	}
	
	public int update_interval() {
		return(Integer.valueOf(prefs.getString("update_interval", "0")));
	}
	
	public XymonServer xymon_server() throws UnsupportedVersionException {
		Log.d(TAG, "xymon_server()");
		if (m_server == null) {
			
			String hostname = prefs.getString("hostname", "www.xymon.org");
			boolean ssl = prefs.getBoolean("use_ssl", false);
			String username = prefs.getString("username", "");
			String password = prefs.getString("password", "");
			String view = prefs.getString("use_view", NON_GREEN);
			String filter_string = prefs.getString("appfeed_filter", "");
			String port_string = prefs.getString("port", "0");
			int port = 0;
			try {
				port = Integer.valueOf(prefs.getString("port", "0"));
			} catch (NumberFormatException e) {
				port = 0;
			}
			
			try {
				m_server = new XymonServer(hostname, port, ssl, username, password, view, this);
				m_server.set_filter(filter_string);
			} catch (UnsupportedVersionException e) {
				throw e;
			}
		}
		return(m_server);
	}
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "terminated");
	}
	
	public PendingIntent pendingIntent() {
		Intent intent = new Intent(this, XymonQVService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, -1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return(pendingIntent);
	}
	
	public void setIntentForCurrentInterval() {
		long interval = getInterval();
		Log.d(TAG, String.format("setting intent: %d", interval));
		if (interval == XymonQVApplication.INTERVAL_NEVER){
			return;
		}
		PendingIntent pe = pendingIntent();
		
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, pe);
	}
	
	public void cancelIntent() {
		PendingIntent pe = pendingIntent();
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pe);
	}
	
	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("update_interval")) {
			int ui = update_interval();
			Log.d(TAG, String.format("SETTING: update interval: %d", ui));
			setIntentForCurrentInterval();
		}
		if (key.equals("enable_debug_log")) {
			boolean dbg = prefs.getBoolean("enable_debug_log", false);
			Log.d(TAG, String.format("SETTING: debug log: %b", dbg));
			Log.set_debug_mode(dbg);
		}
		if (key.equals("hostname")) {
			String hostname = prefs.getString("hostname", "");
			Log.d(TAG, String.format("SETTING: hostname: %s", hostname));
			if (m_server != null) {
				m_server.clear_history();
			}
		}
		m_server = null;
	}

}
