package com.darmasoft.xymon;

import android.content.Context;

public class XymonService {

	private String m_svc_name;
	private String m_svc_color;
	private boolean m_svc_acked;
	private String m_svc_duration;
	private XymonHost m_svc_host = null;
	
	public XymonService(String[] parts) {
		super();
		if (parts.length == 3) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_duration = parts[2];
			m_svc_acked = false;
		} else if (parts.length == 4) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_acked = true;
			m_svc_duration = parts[3];
		} else if (parts.length == 2) {
			m_svc_name = parts[0];
			m_svc_color = parts[1];
			m_svc_duration = "";
			m_svc_acked = false;
		}
	}
	
	public XymonService(String svc_name, String svc_color, boolean svc_acked, String svc_duration) {
		super();
		m_svc_name = svc_name;
		m_svc_color = svc_color;
		m_svc_duration = svc_duration;
		m_svc_acked = svc_acked;
	}
	
	public XymonHost host() {
		return(m_svc_host);
	}
	
	public void setHost(XymonHost h) {
		m_svc_host = h;
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
	
	public String duration() {
		return(m_svc_duration);
	}
	
	public XymonServiceView view(Context context) {
		return(new XymonServiceView(this, context));
	}
}
