package com.darmasoft.xymon;

import java.util.ArrayList;

public class XymonHost {

	private String m_hostname;
	private ArrayList<XymonService> m_services = new ArrayList<XymonService>();
	private XymonServer server = null;
	
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
		svc.set_server(server);
		m_services.add(svc);
	}
	
	public ArrayList<XymonService> services() {
		return(m_services);
	}
	
	public void setServer(XymonServer s) {
		server = s;
	}
	
	public XymonServer server() {
		return(server);
	}
	
	public int service_count_by_color(String c) {
		int count = 0;
		for (XymonService s : m_services) {
			if (s.color().equals(c)) {
				count++;
			}
		}
		return(count);
	}
	
	public int service_count() {
		return(m_services.size());
	}
	
	public String worst_color() {
		String worst = "green";
		for (XymonService s : services()) {
			String c = s.color();
			if (c.equals("red")) {
				worst = "red";
			} else if (c.equals("yellow") && (!worst.equals("red"))) {
				worst = "yellow";
			} else if (c.equals("purple") && (!(worst.equals("yellow") || worst.equals("red")))) {
				worst = "purple";
			}
		}
		return(worst);
	}
}
