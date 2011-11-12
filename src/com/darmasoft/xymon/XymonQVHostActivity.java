package com.darmasoft.xymon;

import java.util.Date;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XymonQVHostActivity extends Activity {

	private static final String TAG = "XymonQVHostActivity";
	private XymonHost m_host = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.listview);
    	    	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		String hostname = getIntent().getStringExtra("hostname");
		
		try {
			m_host = ((XymonQVApplication) getApplication()).xymon_server().get_host_by_hostname(hostname);
		} catch (UnsupportedVersionException e) {
			Log.printStackTrace(e);
			finish();
		}
		
		update_view();
	}

	protected void update_view() {
		String color = m_host.worst_color();

		setBackgroundColor(color);

		Date d = m_host.server().last_updated();
		String date = "NEVER";
		if (d != null) {
			date = String.format("%tF %tT", d, d);
		}

		((TextView) findViewById(R.id.hostname_line)).setText(m_host.hostname());
		((TextView) findViewById(R.id.hostname_updated)).setText(date);
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.host_line_container);
		ll.removeAllViews();

		for (XymonService s : m_host.services()) {       		
			XymonServiceView xsv = new XymonServiceView(s, this);
			ll.addView(xsv);	
		}

	}

	public void setBackgroundColor(String c) {
    	findViewById(R.id.hostname_color).setBackgroundColor(ColorHelper.colorForString(c));
    }

}
