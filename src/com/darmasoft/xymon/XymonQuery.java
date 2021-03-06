package com.darmasoft.xymon;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;

public class XymonQuery {

	private static final String TAG = "XymonQuery";
	
	private String m_color = "black";
	private String m_version;
	private Date m_date_ran = null;
	private boolean m_ran;
	private XymonServer m_server;

	private ArrayList<XymonHost> m_hosts = new ArrayList<XymonHost>();
	
	public XymonQuery(XymonServer s) {
		m_server = s;
	}
	
	public XymonServer server() {
		return(m_server);
	}
	
	public void set_hosts(ArrayList<XymonHost> hosts) {
		m_hosts = hosts;
	}
	
	public void add_host(XymonHost h) {
		m_hosts.add(h);
	}
	
	public ArrayList<XymonHost> hosts() {
		return m_hosts;
	}
	
	public XymonHost get_host_by_hostname(String hostname) {
		for (XymonHost h : hosts()) {
			if (h.hostname().equals(hostname)) {
				return h;
			}
		}
		return null;
	}

	public String worst_color() {
		String worst = "green";
		for (XymonHost h : hosts()) {
			for (XymonService s : h.services()) {
				String c = s.color();
				if (c.equals("red")) {
					worst = "red";
				} else if (c.equals("yellow") && (!worst.equals("red"))) {
					worst = "yellow";
				} else if (c.equals("purple") && (!(worst.equals("yellow") || worst.equals("red")))) {
					worst = "purple";
				}
			}
		}
		return(worst);
	}
	
	public void insert(Context ctx) {
		
		DBHelper dbHelper = new DBHelper(ctx);

		Date last_updated = m_date_ran;
		String last_color = m_color;
		String version = m_version;

		for (XymonHost host : this.hosts()) {

			dbHelper.insert(host, last_updated);

			for (XymonService s : host.services()) {
				dbHelper.insert(s, last_updated);
			}
		}

		dbHelper.insert_run(last_updated, last_color, version);
	}
	
	public String color() {
		return m_color;
	}
	
	public void set_color(String color) {
		m_color = color;
	}
	
	public Date last_updated() {
		return m_date_ran;
	}
	
	public void set_last_updated(Date date) {
		m_date_ran = date;
	}
	
	public String version() {
		return m_version;
	}
	
	public void set_version(String version) {
		m_version = version;
	}
	
	public boolean was_ran() {
		return m_ran;
	}
	
	public void set_was_ran(boolean ran) {
		m_ran = ran;
	}
}
