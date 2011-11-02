package com.darmasoft.xymon;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.util.Log;

public class XymonVersion423 extends XymonVersion {

	private static final String TAG = "XymonVersion423";

	public XymonVersion423(String version) {
		super("4.2.3");
	}
	
	public static ArrayList<String> supported_versions() {
		ArrayList<String> al = new ArrayList<String>();
		al.add("4.2.3");
		return al;
	}

	@Override
	public String non_green_url() {
		return(String.format("%s/bb2.html", root_url()));
	}

	@Override
	public XymonQuery parse_non_green_body(String body) {
		Log.d(TAG, "parse_non_green_body_4_2_3()");
		
		XymonQuery q = new XymonQuery(m_server);
		
		try {
			CleanerProperties props = new CleanerProperties();
			props.setAllowHtmlInsideAttributes(true);
			props.setAllowMultiWordAttributes(true);
			props.setRecognizeUnicodeChars(true);
			props.setOmitComments(true);
			
			TagNode doc = new HtmlCleaner(props).clean(body);
				
			TagNode doc_body = ((TagNode) doc.evaluateXPath("//body")[0]);
			
			q.set_color(doc_body.getAttributeByName("bgcolor"));
			
			Object[] non_green_lines = doc.evaluateXPath("//table[@summary]//td[@nowrap]");
			Log.d(TAG, String.format("found %d non green lines", non_green_lines.length));
			
			for (Object o : non_green_lines) {
				TagNode e = (TagNode) o;
				String hostname = ((TagNode) e.evaluateXPath("//a[@name]")[0]).getAttributeByName("name");
				XymonHost host = new XymonHost(hostname);
				Object[] red_services = doc.evaluateXPath("//table[@summary]//td[@align]/a/img[@src]");
				Log.d(TAG, String.format("found %d non green services", red_services.length));
				for (Object svc_o : red_services) {
					TagNode svc_e = (TagNode) svc_o;
					
					String svc_src = svc_e.getAttributeByName("src");
					Pattern p = Pattern.compile("(?i)(red|yellow|blue|purple)");
					Matcher m = p.matcher(svc_src);
					
					if (m.find()) {
						String svcinfo = svc_e.getAttributeByName("alt");
						String[] parts = svcinfo.split(":");
						XymonService s = new XymonService(parts);
						host.add_service(s);
					}
				}
				host.setServer(m_server);
				q.add_host(host);
			}
		} catch (XPatherException e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return(q);

	}

	@Override
	public String root_url() {
		String scheme = (ssl() ? "https://" : "http://");
		return(String.format("%s%s/", scheme, host()));
	}

	@Override
	public String service_url(XymonService s) {
		return(String.format("%sxymon-cgi/bb-hostsvc.sh?HOST=%s&SERVICE=%s", root_url(), host(), s.name()));
	}

}