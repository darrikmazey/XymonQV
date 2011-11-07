package com.darmasoft.xymon;

import java.util.ArrayList;

abstract public class XymonVersion {

	 protected XymonServer m_server = null;
	 protected String m_version;
	 
	 public XymonVersion(String version) {
		 m_version = version;
	 }

	 public void set_server(XymonServer s) {
		 m_server = s;
	 }
	 
	 public String host() {
		 if (m_server == null) {
			 return "";
		 }
		 return(m_server.host());
	 }
	 
	 public boolean ssl() {
		 if (m_server == null) {
			 return(false);
		 }
		 return(m_server.ssl());
	 }
	 
	 public String version() {
		 return(m_version);
	 }
	 
	 public static boolean sufficient_for_version(String version) {
		 return false;
	 }
	 
	 abstract public String service_url(XymonService s);
	 abstract public String non_green_url();
	 abstract public String critical_url();
	 
	 public String root_url() {
		 String scheme = (ssl() ? "https://" : "http://");
		 return(String.format("%s%s/", scheme, host()));
	 }
	 
	 abstract public XymonQuery parse_non_green_body();
	 abstract public XymonQuery parse_critical_body();
	 
	 public static ArrayList<String> supported_versions() {
		 return(new ArrayList<String>());
	 }
}
