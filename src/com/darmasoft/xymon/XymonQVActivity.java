package com.darmasoft.xymon;

import java.util.ArrayList;
import java.util.Date;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class XymonQVActivity extends Activity {
	
	private XymonQVUpdateReceiver m_receiver;
	private IntentFilter m_filter;
	private ProgressDialog prog_dialog = null;
	
	Cursor m_cursor;
	ListView m_listview;
	SimpleCursorAdapter m_adapter;
	static final String[] HOST_FROM = { DBHelper.C_HOST_ID, DBHelper.C_STATUS_COUNT };
	static final int[] HOST_TO = { R.id.hostname, R.id.status_count };
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
		unregisterReceiver(m_receiver);
		XymonQVApplication app = ((XymonQVApplication) getApplication());
		app.cancelIntent();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		registerReceiver(m_receiver, m_filter, "com.darmasoft.xymon.SEND_DATA_NOTIFICATION", null);
		XymonQVApplication app = ((XymonQVApplication) getApplication());
		app.setIntentForCurrentInterval();
		if (app.update_interval() == 0) {
    		prog_dialog = ProgressDialog.show(this, "", "Loading data...");
    		startService(new Intent(this, XymonQVService.class));
		}
		
		DBHelper db_helper = new DBHelper(this);
		m_cursor = db_helper.get_hosts_cursor();
		
		m_adapter = new SimpleCursorAdapter(this, R.layout.service_row, m_cursor, HOST_FROM, HOST_TO);
		m_listview.setAdapter(m_adapter);
		
		load_status();
	}

	SharedPreferences prefs;
	
	private static final String TAG = "XymonQVActivity";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.listview);
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	m_receiver = new XymonQVUpdateReceiver(this);
    	m_filter = new IntentFilter("com.darmasoft.xymon.NEW_DATA");

    	m_listview = (ListView) findViewById(R.id.listview);
    	
    	load_status();
    }
    
    public void load_status() {
    	Log.d(TAG, "load_status()");
    	if (prog_dialog != null) {
    		prog_dialog.dismiss();
    		prog_dialog = null;
    	}
    	
    	try {
    	XymonServer server = ((XymonQVApplication) getApplication()).xymon_server();
       	server.load_last_data();
       	update_view();
    	} catch (UnsupportedVersionException e) {
    		update_header_from_settings(e.version());
    	}
    }

    private void update_header_from_settings(String version) {
    	String hostname = this.prefs.getString("hostname", "www.xymon.org");
		((TextView) findViewById(R.id.hostname_line)).setText(String.format("%s (%s)", hostname, version));	
		((TextView) findViewById(R.id.status_line)).setText("UNSUPPORTED");
		Date date = new Date();
		((TextView) findViewById(R.id.updated_line)).setText(String.format("%tF %tT", date, date));
    }
    
    private void update_view() {

    	try {
    		DBHelper db_helper = new DBHelper(this);
    		if (m_cursor != null) {
    			m_cursor.close();
    		}
    		m_cursor = db_helper.get_hosts_cursor();
    		
    		m_adapter = new SimpleCursorAdapter(this, R.layout.host_row, m_cursor, HOST_FROM, HOST_TO);
    		m_listview.setAdapter(m_adapter);
    		
    		XymonServer server = ((XymonQVApplication) getApplication()).xymon_server();
    		String c = server.color();
    		if (c == null) {
    			c = "unknown";
    		}
    		
    		String ds;
    		Date d = server.last_updated();
    		if (d != null) {
    			ds = String.format("%tF %tT", d, d);
    		} else {
    			ds = "NEVER";
    		}
    		
    		((TextView) findViewById(R.id.hostname_line)).setText(String.format("%s", server.host()));
    		((TextView) findViewById(R.id.hostname_updated)).setText(ds);

    		setBackgroundColor(c);

    	} catch (UnsupportedVersionException e) {
    		// noop
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return(true);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.itemPrefs:
    		Log.d(TAG, "options item : preferences");
    		startActivity(new Intent(this, PrefsActivity.class));
    		break;
    	case R.id.itemRefresh:
    		Log.d(TAG, "options item : start service");
    		prog_dialog = ProgressDialog.show(this, "", "Loading data...");
    		startService(new Intent(this, XymonQVService.class));
    		break;
    	case R.id.itemClearHistory:
    		Log.d(TAG, "options item : clear history");
    		DBHelper dbHelper = new DBHelper(this);
    		dbHelper.delete_all_statuses();
    		dbHelper.delete_all_hosts();
    		dbHelper.delete_all_runs();
    		Log.delete_debug_log();
    		load_status();
    		break;
    	case R.id.itemAbout:
    		Log.d(TAG, "options item : about");
    		String app_ver;
			try {
				app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				app_ver = "8.6.7";
			}
    		Toast.makeText(this, String.format("XymonQV v%s\n\n(c)2011 DarmaSoft, LLC.\nandroid@darmasoft.com", app_ver), Toast.LENGTH_LONG).show();
    		break;
    	}
    	return(true);
    }
    
    public void setBackgroundColor(String c) {
    	findViewById(R.id.hostname_color).setBackgroundColor(ColorHelper.colorForString(c));
    }

    class XymonQVReceiver extends BroadcastReceiver {
    	
    	private XymonQVActivity m_act;
    	
    	public XymonQVReceiver(XymonQVActivity activity) {
    		super();
    		m_act = activity;
    	}
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.d("XymonQVReceiver", "onReceive()");
    		m_act.load_status();
    	}
    }
}
