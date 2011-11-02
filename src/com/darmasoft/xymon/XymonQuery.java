package com.darmasoft.xymon;

import java.util.ArrayList;
import java.util.Date;

public class XymonQuery {

	private static final String TAG = "XymonQuery";
	
	private String m_color;
	private String m_version;
	private Date m_date_ran;
	private boolean m_ran;

	private ArrayList<XymonHost> m_hosts;
	
	public XymonQuery(XymonServer s) {
		
	}
	
	public void add_host(XymonHost h) {
		m_hosts.add(h);
	}
	
	public ArrayList<XymonHost> hosts() {
		return m_hosts;
	}
	
	public void insert() {
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
	
	public boolean was_ran() {
		return m_ran;
	}
	
	public void set_was_ran(boolean ran) {
		m_ran = ran;
	}
}
