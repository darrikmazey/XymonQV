package com.darmasoft.xymon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.content.Context;
import android.util.Log;

public class XymonServer {
	private boolean m_ssl = false;
	private String m_host;
	private String m_username;
	private String m_password;
	private String m_version = null;
	private String m_color = null;
	private String m_last_non_green_body = null;
	private Date m_last_updated = null;
	private ArrayList<XymonHost> m_hosts = new ArrayList<XymonHost>();
	private Context m_context;
	
	private ClientConnectionManager m_conn_manager;
	private HttpContext m_http_context;
	private HttpParams m_http_params;
	
	private static final String TAG = "XymonServer";

	public XymonServer(String host, boolean ssl, String username, String password, Context context) {
		super();
		Log.d(TAG, "constructor(" + host + ", " + Boolean.toString(ssl) + ", " + username + ", " + password + ")");
		m_host = host;
		m_ssl = ssl;
		m_username = username;
		m_password = password;
		m_hosts = new ArrayList<XymonHost>();
		m_context = context;
		
		SchemeRegistry scheme_registry = new SchemeRegistry();
		
		scheme_registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		scheme_registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		
		m_http_params = new BasicHttpParams();
		m_http_params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		m_http_params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		m_http_params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(m_http_params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(m_http_params, "utf8");
		
		CredentialsProvider credentials_provider = new BasicCredentialsProvider();
		credentials_provider.setCredentials(new AuthScope(m_host, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username, password));
		m_conn_manager = new ThreadSafeClientConnManager(m_http_params, scheme_registry);
		m_http_context = new BasicHttpContext();
		m_http_context.setAttribute("http.auth.credentials-provider", credentials_provider);
		
	}
	
	public synchronized boolean clear_history() {
		Log.d(TAG, "clear_history()");
		DBHelper dbHelper = new DBHelper(m_context);
		dbHelper.delete_all_statuses();
		dbHelper.delete_all_hosts();
		dbHelper.delete_all_runs();
		return(true);
	}

	public boolean load_last_data() {
		Log.d(TAG, "load_last_data()");
		DBHelper dbHelper = new DBHelper(m_context);
		
		m_hosts = dbHelper.load_last_hosts(this);
		m_color = dbHelper.load_last_color();
		m_last_updated = dbHelper.load_last_updated();
		
		return(true);
	}
	public String host() {
		return(m_host);
	}
	
	public boolean ssl() {
		return(m_ssl);
	}
	
	public String username() {
		return(m_username);
	}
	
	public String password() {
		return(m_password);
	}
	
	public String version() {
		if (m_version == null || m_version == "unknown") {
			m_version = fetch_version();
			Log.d(TAG, "FOUND VERSION: " + m_version);
		}
		return(m_version);
	}
	
	public String service_url(XymonService s) {
		if (version().equals("4.3.0")) {
			return(service_url_4_3_0(s));
		} else if (version().equals("4.2.3")) {
			return(service_url_4_3_0(s));
		} else {
			return(service_url_4_3_0(s));
		}
	}
	
	public String service_url_4_3_0(XymonService s) {
		String hostname = s.host().hostname();
		String svcname = s.name();
		return(String.format("%sxymon-cgi/bb-hostsvc.sh?HOST=%s&SERVICE=%s", root_url(), hostname, svcname));
	}
	
	
	public String fetch_version() {
		String body = fetch(root_url());
		if (body == null) {
			return("unknown");
		}
		
		CleanerProperties props = new CleanerProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);
		
		TagNode node = new HtmlCleaner(props).clean(body);
			
		TagNode link;
		
		try {
			link = ((TagNode) node.evaluateXPath("//table//tr/td/font/b/a")[0]);
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			return("unknown");
		}
		
		if (link == null) {
			return("unknown");
		}
		String lt = link.getText().toString();
		Pattern p = Pattern.compile("Xymon \\d.\\d.\\d.*");
		Matcher m = p.matcher(lt);
		if (m.find()) {
			Pattern pv = Pattern.compile("\\d\\.\\d\\.\\d");
			Matcher mv = pv.matcher(m.group());
			if (mv.find()) {
				return(mv.group());
			} else {
				return("unknown");
			}
		} else {
			return("unknown");
		}
	}
	
	public String scheme() {
		if (m_ssl) {
			return("https://");
		} else {
			return("http://");
		}
	}
	
	public String root_url() {
		return(scheme() + m_host + "/");
	}
	
	public String non_green_url_for_version(String version) {
		String postfix = "/";
		// TODO: should these be based on minor version, and not release?
		if (version.equals("4.3.4")) {
			postfix = "/nongreen.html";
		} else if (version.equals("4.3.5")) {
			postfix = "/nongreen.html";
		} else {
			postfix = "/bb2.html";
		}
		return(scheme() + m_host + postfix);
	}
	
	public String fetch_non_green_view() {
		m_last_non_green_body = fetch(non_green_url_for_version(version()));
		parse_non_green_body(version());
		return(m_last_non_green_body);
	}
	
	public boolean refresh() {
		Log.d(TAG, "refresh()");
		fetch_non_green_view();
		return(true);
	}

	public String color() {
		if (m_color == null) {
			refresh();
		}
		return(m_color);
	}
	
	public ArrayList<XymonHost> hosts() {
		return(m_hosts);
	}
	
	public boolean parse_non_green_body(String version) {
		Log.d(TAG, "parse_non_green_body(" + version + ")");
		if (m_last_non_green_body == null) {
			m_color = "unknown";
			return(false);
		}
		m_hosts.clear();		

		/* TODO: should these be based on minor version, and not release?
		 * if so, then the parse methods should reflect that (ie, 4_3 and 4_2).
		 */
		//if (version.substring(0,3).equals("4.3")) { //Supports any 4.3 minor version
		if (version.equals("4.3.0")) {
			parse_non_green_body_4_3_0();
		} else if (version.equals("4.2.3")) {
			parse_non_green_body_4_2_3();
		} else {
			// unknown version
			m_color = "unknown";
		}
		
		m_last_updated = new Date();
		return(true);
	}
	
	public boolean parse_non_green_body_4_2_3() {
		Log.d(TAG, "parse_non_green_body_4_2_3()");
		
		try {
			CleanerProperties props = new CleanerProperties();
			props.setAllowHtmlInsideAttributes(true);
			props.setAllowMultiWordAttributes(true);
			props.setRecognizeUnicodeChars(true);
			props.setOmitComments(true);
			
			TagNode doc = new HtmlCleaner(props).clean(m_last_non_green_body);
				
			TagNode doc_body = ((TagNode) doc.evaluateXPath("//body")[0]);
			
			m_color = doc_body.getAttributeByName("bgcolor");
			
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
				m_hosts.add(host);
			}
		} catch (XPatherException e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return(true);
	}
	
	public boolean parse_non_green_body_4_3_0() {

		try {
			CleanerProperties props = new CleanerProperties();
			props.setAllowHtmlInsideAttributes(true);
			props.setAllowMultiWordAttributes(true);
			props.setRecognizeUnicodeChars(true);
			props.setOmitComments(true);
			
			TagNode doc = new HtmlCleaner(props).clean(m_last_non_green_body);

			TagNode doc_body = ((TagNode) doc.evaluateXPath("body")[0]);
			
			m_color = doc_body.getAttributeByName("bgcolor");
			
			Object[] non_green_lines = doc.evaluateXPath("//tr[@class='line']");
			Log.d(TAG, "found " + Integer.toString(non_green_lines.length) + " non green lines");
			for (Object o : non_green_lines) {
				TagNode e = (TagNode) o;
				String hostname = ((TagNode) e.evaluateXPath("//td[@nowrap]/a[@name]")[0]).getAttributeByName("name");
				Log.d(TAG, "found hostname: " + hostname);
				XymonHost host = new XymonHost(hostname);
				Object[] non_green_services = e.evaluateXPath("//td[@align]/a/img[@src]");
				Log.d(TAG, "found " + Integer.toString(non_green_services.length) + " non-green services");
				for (Object svc_o : non_green_services) {
					TagNode svc_e = (TagNode) svc_o;
					
					String svc_src = svc_e.getAttributeByName("src");
					Pattern p = Pattern.compile("(?i)(red|yellow|blue|purple)");
					Matcher m = p.matcher(svc_src);
					
					if (m.find()) {
						String svcinfo = svc_e.getAttributeByName("alt");
						Log.d(TAG, "svcinfo: [" + svcinfo + "]");
						String[] parts = svcinfo.split(":");
						XymonService s = new XymonService(parts);
						host.add_service(s);
					}
				}
				
				m_hosts.add(host);
			}
		} catch (XPatherException e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return(true);
	}
	
	public Date last_updated() {
		return(m_last_updated);
	}
	
	public String fetch(String url) {
		HttpClient client = new DefaultHttpClient(m_conn_manager, m_http_params);
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse res = client.execute(get, m_http_context);
			return(HttpHelper.readBody(res));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString() + " : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString() + " : " + e.getMessage());
			e.printStackTrace();
		}
		return(null);
	}
}
