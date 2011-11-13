package com.darmasoft.xymon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class XymonService {

	private static final String TAG = "XymonService";
	
	private String m_svc_name;
	private String m_svc_color;
	private boolean m_svc_acked;
	private int m_svc_ack_time;
	private String m_svc_ack_text;
	private String m_svc_ack_by;
	private int m_svc_duration;
	private String m_url;
	private XymonServer m_server;
	
	private XymonHost m_svc_host = null;
	
	public XymonService(String[] parts) {
		super();
		if (parts.length == 3) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_duration = parse_duration_part(parts[2]);
			m_svc_acked = false;
		} else if (parts.length == 4) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_acked = true;
			m_svc_duration = parse_duration_part(parts[3]);
		} else if (parts.length == 2) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_duration = 0;
			m_svc_acked = false;
		}
	}
	
	private int parse_duration_part(String part) {
		Pattern p = Pattern.compile("(\\d+)h(\\d+)m");
		Matcher m = p.matcher(part);
		
		if (m.find()) {
			int hours = Integer.valueOf(m.group(1));
			int minutes = Integer.valueOf(m.group(2));
			return((int)(System.currentTimeMillis() / 1000.0 - (hours * 60 * 60) - (minutes * 60)));
		}

		return((int) (System.currentTimeMillis() / 1000.0));
	}
	
	public XymonService(String svc_name, String svc_color, boolean svc_acked, int svc_ack_time, String svc_ack_text, String svc_ack_by, int svc_duration, String url) {
		super();
		m_svc_name = svc_name;
		m_svc_color = svc_color;
		m_svc_duration = svc_duration;
		m_svc_acked = svc_acked;
		m_svc_ack_time = svc_ack_time;
		m_svc_ack_text = svc_ack_text;
		m_svc_ack_by = svc_ack_by;
		m_url = url;
	}
	
	public XymonServer server() {
		return(m_server);
	}
	
	public void set_server(XymonServer s) {
		m_server = s;
	}
	
	public XymonHost host() {
		return(m_svc_host);
	}
	
	public void setHost(XymonHost h) {
		m_svc_host = h;
	}
	
	public String ack_by() {
		return(m_svc_ack_by);
	}
	
	public int ack_time() {
		return(m_svc_ack_time);
	}
	
	public String ack_text() {
		return(m_svc_ack_text);
	}
	
	public String url() {
		if (m_url != null) {
			return(m_url);
		} else {
			return m_svc_host.server().service_url(this);
		}
	}
	
	public void set_url(String url) {
		m_url = url;
	}
	
	public String name() {
		return(m_svc_name);
	}
	
	public String color() {
		return(m_svc_color);
	}
	
	public boolean acked() {
		return(m_svc_acked);
	}
	
	public int duration() {
		return(m_svc_duration);
	}
	
	public XymonServiceView view(Context context) {
		return(new XymonServiceView(this, context));
	}
}
