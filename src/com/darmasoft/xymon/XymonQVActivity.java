package com.darmasoft.xymon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XymonQVActivity extends Activity {
	
	private XymonQVReceiver m_receiver;
	private IntentFilter m_filter;
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
		unregisterReceiver(m_receiver);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		registerReceiver(m_receiver, m_filter);
	}

	SharedPreferences prefs;
	
	private static final String TAG = "XymonQVActivity";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.status);
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	m_receiver = new XymonQVReceiver();
    	m_filter = new IntentFilter("com.darmasoft.xymon.NEW_DATA");
    	
    	load_status();
    }

    public void load_status() {
       	XymonServer server = ((XymonQVApplication) getApplication()).xymon_server();
       	server.load_last_data();
       	update_view();
    }

    private void update_view() {
      	XymonServer server = ((XymonQVApplication) getApplication()).xymon_server();
        	String c = server.color();
       	Date d = server.last_updated();
       	String date = "NEVER";
       	if (d != null) {
           	date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(d);
       	}
       	((TextView) findViewById(R.id.hostname_line)).setText(server.host());
       	((TextView) findViewById(R.id.status_line)).setText(c.toUpperCase());
       	((TextView) findViewById(R.id.updated_line)).setText(date);
       	setBackgroundColor(c);
       	
       	ArrayList<XymonHost> hosts = server.hosts();

       	LinearLayout ll = (LinearLayout) findViewById(R.id.host_line_container);
       	ll.removeAllViews();
       	
       	for (XymonHost h : hosts) {       		
       		for (XymonService s : h.services()) {
       			XymonServiceView xsv = s.view(this);
       			ll.addView(xsv);	
       		}
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
    		startActivity(new Intent(this, PrefsActivity.class));
    		break;
    	case R.id.itemRefresh:
    		load_status();
    		break;
    	case R.id.itemStartService:
    		startService(new Intent(this, XymonQVService.class));
    		break;
    	case R.id.itemStopService:
    		stopService(new Intent(this, XymonQVService.class));
    		break;
    	case R.id.itemClearHistory:
    		XymonServer server = ((XymonQVApplication) getApplication()).xymon_server();
    		server.clear_history();
    		load_status();
    	}
    	return(true);
    }
    
    public void setBackgroundColor(String c) {
    	findViewById(R.id.color_indicator).setBackgroundColor(ColorHelper.colorForString(c));
    }

    class XymonQVReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.d("XymonQVReceiver", "onReceive()");
    	}
    }
}
