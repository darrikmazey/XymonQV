package com.darmasoft.xymon;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class XymonVersion434 extends XymonVersion {

	private static final String TAG = "XymonVersion434";
	
	public XymonVersion434(String version) {
		super("4.3.4");
		Log.d(TAG, "XymonVersion434()");
	}

	public static ArrayList<String> supported_versions() {
		ArrayList<String> al = new ArrayList<String>();
		al.add("4.3.4");
		al.add("4.3.5");
		return al;
	}
	
	public static boolean sufficient_for_version(String version) {
//		return(true);
		return(supported_versions().contains(version));
	}	 

	@Override
	public String non_green_url() {
		return(String.format("%snongreen.html", root_url()));
	}

	@Override
	public XymonQuery parse_non_green_body(String body) {
		Log.d(TAG, "parse_non_green_body()");
		
		XymonQuery q = new XymonQuery(m_server);
		q.set_last_updated(new Date());
		q.set_version(m_version);

		try {
			CleanerProperties props = new CleanerProperties();
			props.setAllowHtmlInsideAttributes(true);
			props.setAllowMultiWordAttributes(true);
			props.setRecognizeUnicodeChars(true);
			props.setOmitComments(true);

			TagNode doc = new HtmlCleaner(props).clean(body);

			TagNode doc_body = ((TagNode) doc.evaluateXPath("body")[0]);

			q.set_color(doc_body.getAttributeByName("class"));

			Object[] non_green_lines = doc.evaluateXPath("//tr[@class='line']");
			Log.d(TAG, "found " + Integer.toString(non_green_lines.length) + " non green lines");
			for (Object o : non_green_lines) {
				TagNode e = (TagNode) o;
				Object[] anchors = e.evaluateXPath("//td[@nowrap]/a[@name]");
				if (anchors.length > 0) {
					String hostname = ((TagNode) anchors[0]).getAttributeByName("name");
					Log.d(TAG, "found hostname: " + hostname);
					XymonHost host = new XymonHost(hostname);
					Object[] non_green_services = e.evaluateXPath("//td[@align]/a/img[@src]");
					Log.d(TAG, "found " + Integer.toString(non_green_services.length) + " non-green services");
					for (Object svc_o : non_green_services) {
						TagNode svc_e = (TagNode) svc_o;

						String svc_src = svc_e.getAttributeByName("src");
						Pattern p = Pattern.compile("(?i)(red|yellow|blue|purple|clear)");
						Matcher m = p.matcher(svc_src);

						if (m.find()) {
							String svcinfo = svc_e.getAttributeByName("title");
							Log.d(TAG, "svcinfo: [" + svcinfo + "]");
							if (svcinfo != null){ 
								String[] parts = svcinfo.split(":");
								XymonService s = new XymonService(parts);
								host.add_service(s);
							} else {
								Log.d(TAG, "skipped: " + svc_e.toString());
							}
						}
					}
					host.setServer(m_server);
					q.add_host(host);
				}
			}
		} catch (XPatherException e) {
			Log.d(TAG, e.getMessage());
			Log.printStackTrace(e);
		}
		
		q.set_was_ran(true);
		return(q);
	}
		
	@Override
	public String service_url(XymonService s) {
		return(String.format("%sxymon-cgi/svcstatus.sh?HOST=%s&SERVICE=%s", root_url(), s.host().hostname(), s.name()));
	}

}
