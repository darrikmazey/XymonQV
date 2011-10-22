package com.darmasoft.xymon;

import java.util.ArrayList;

public class XymonHost {

	private String m_hostname;
	private ArrayList<XymonService> m_services = new ArrayList<XymonService>();
	
	public XymonHost(String hostname) {
		super();
		m_hostname = hostname;
	}
	
	public String hostname() {
		return(m_hostname);
	}
	
	public void clear_services() {
		m_services.clear();
	}
	
	public void add_service(XymonService svc) {
		svc.setHost(this);
		m_services.add(svc);
	}
	
	public ArrayList<XymonService> services() {
		return(m_services);
	}
	
}
