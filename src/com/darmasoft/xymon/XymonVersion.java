package com.darmasoft.xymon;

import java.util.ArrayList;

abstract public class XymonVersion {

	 protected XymonServer m_server;
	 protected String m_version;
	 
	 public XymonVersion(String version) {
		 m_version = version;
	 }

	 public void set_server(XymonServer s) {
		 m_server = s;
	 }
	 
	 public String host() {
		 return(m_server.host());
	 }
	 
	 public boolean ssl() {
		 return(m_server.ssl());
	 }
	 
	 public String version() {
		 return(m_version);
	 }
	 
	 public static boolean sufficient_for_version(String version) {
		 return(supported_versions().contains(version));
	 }
	 
	 abstract public String service_url(XymonService s);
	 abstract public String root_url();
	 abstract public String non_green_url();
	 
	 abstract public XymonQuery parse_non_green_body(String body);
	 
	 public static ArrayList<String> supported_versions() {
		 return(new ArrayList<String>());
	 }
}
