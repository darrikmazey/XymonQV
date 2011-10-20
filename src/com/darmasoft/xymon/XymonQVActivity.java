package com.darmasoft.xymon;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class XymonQVActivity extends Activity implements OnSharedPreferenceChangeListener {
	
	SharedPreferences prefs;
	
	private static final String TAG = "XymonQVActivity";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.status);
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	prefs.registerOnSharedPreferenceChangeListener(this);
    	
    	String hostname = prefs.getString("hostname", "www.xymon.org");
    	
    	XymonServer server = new XymonServer(hostname);
    	ScrollView sv = (ScrollView) findViewById(R.id.status);
    	TextView tv = new TextView(getApplicationContext());
    	String c = server.color();
    	tv.setText(c);
    	Log.d(TAG, "COLOR [" + c + "]");
    	if (c.equals("red")) {
    		Log.d(TAG, "GOT HERE");
    		findViewById(R.id.status_layout).setBackgroundColor(Color.RED);
    	} else {
    		findViewById(R.id.status_layout).setBackgroundColor(Color.GREEN);
    	}
    	sv.addView(tv);
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
    	}
    	return(true);
    }
    
    // shared preferences methods
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    	Log.d(TAG, "pref set: " + key + " : " + prefs.getString(key, ""));
    }
}