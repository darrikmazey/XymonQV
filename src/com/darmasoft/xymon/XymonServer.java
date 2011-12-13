package com.darmasoft.xymon;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class XymonServer {
	private boolean m_ssl = false;
	private String m_host;
	private int m_port;
	private String m_path;
	private String m_username;
	private String m_password;
	private String m_version = "unknown";
	private String m_last_non_green_body = null;
	private String m_last_critical_body = null;
	private Context m_context;
	private String m_view = "non_green";
	private boolean m_has_api_target = false;
	private XymonQuery m_last_query = null;
	private XymonVersion m_xv = null;
	private String m_filter = null;
	
	private ClientConnectionManager m_conn_manager;
	private HttpContext m_http_context;
	private HttpParams m_http_params;
	
	private static final String TAG = "XymonServer";

	public static final String CRITICAL = "critical";
	public static final String NON_GREEN = "non_green";

	public XymonServer(String host, int port, String path, boolean ssl, String username, String password, String view, Context context) throws UnsupportedVersionException {
		super();
		m_host = host;
		if (port == 0) {
			if (ssl) {
				m_port = 443;
			} else {
				m_port = 80;
			}
		} else {
			m_port = port;
		}
		m_path = path;
		m_ssl = ssl;
		m_username = username;
		m_password = password;
		m_view = view;
		new ArrayList<XymonHost>();
		m_context = context;
		
		SchemeRegistry scheme_registry = new SchemeRegistry();
		
		scheme_registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		scheme_registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		
		m_http_params = new BasicHttpParams();
		m_http_params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 3);
		m_http_params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		m_http_params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		m_http_params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		HttpConnectionParams.setConnectionTimeout(m_http_params, 3000);
		HttpConnectionParams.setSoTimeout(m_http_params, 5000);
		HttpProtocolParams.setVersion(m_http_params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(m_http_params, "utf8");
		
		CredentialsProvider credentials_provider = new BasicCredentialsProvider();
		credentials_provider.setCredentials(new AuthScope(m_host, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username, password));
		m_conn_manager = new ThreadSafeClientConnManager(m_http_params, scheme_registry);
		m_http_context = new BasicHttpContext();
		m_http_context.setAttribute("http.auth.credentials-provider", credentials_provider);

		try {
			m_version = fetch_version();
			m_has_api_target = check_api_target();
			
			String force_version = ((XymonQVApplication) context.getApplicationContext()).prefs.getString("force_version", "");
			if (force_version.length() > 0) {
				m_xv = XymonVersionFactory.for_version(force_version);
			} else if (m_has_api_target) {
				m_xv = XymonVersionFactory.api_parser(m_version);
			} else {
				m_xv = XymonVersionFactory.for_version(m_version);
			}
			m_xv.set_server(this);
		} catch (UnsupportedVersionException e) {
			throw e;
		} catch (XymonQVException e) {
			Log.printStackTrace(e);
		}
	}

	public String path() {
		String p = m_path;
		if (!p.startsWith("/")) {
			p = '/' + p;
		}
		if (!p.endsWith("/")) {
			p += "/";
		}
		Pattern pat = Pattern.compile("//");
		while (pat.matcher(p).find()) {
			p = p.replace("//", "/");
		}
		return(p);
	}
	
	public int port() {
		return(m_port);
	}
	
	public void set_port(int p) {
		m_port = p;
	}
	
	public void set_filter(String f) {
		m_filter = f;
	}
	
	public String filter() {
		return(m_filter);
	}
	
	public String api_target_url() {
		XymonVersion xv = XymonVersionFactory.api_parser("API");
		xv.set_server(this);
		
		if (m_view.equals(NON_GREEN)) {
			return(xv.non_green_url());
		} else if (m_view.equals(CRITICAL)) {
			return(xv.critical_url());
		}
		return("");
	}
	
	public boolean check_api_target() throws XymonQVException {
		Log.d(TAG, "checking for api target");
		String url = api_target_url();
		try {
			fetch(url);
		} catch (NotFoundException e) {
			Log.printStackTrace(e);
			Log.d(TAG, "no api target found");
			return(false);
		}
		Log.d(TAG, String.format("found api target: %s", url));
		return(true);
	}
	
	public String root_url() {
		String scheme = (ssl() ? "https://" : "http://");
		if (m_port != 0) {
			return(String.format("%s%s:%d%s", scheme, host(), port(), path()));
		} else {
			return(String.format("%s%s%s", scheme, host(), path()));
		}
	}

	public String root_url_stripped() {
		String scheme = (ssl() ? "https://" : "http://");
		if (m_port != 0) {
			return(String.format("%s%s:%d%s", scheme, host(), port(), path()));
		} else {
			return(String.format("%s%s%s", scheme, host(), path()));
		}
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
	
		m_last_query = dbHelper.load_last_query(this);
		
		return(true);
	}
	
	public String last_non_green_body() {
		return(m_last_non_green_body);
	}
	
	public String last_critical_body() {
		return(m_last_critical_body);
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
			return("unknown");
		}
		return(m_version);
	}
	
	public String service_url(XymonService s) {
		return m_xv.service_url(s);
	}	
	
	public String fetch_version() throws XymonQVException {
		Log.d(TAG, "fetching version");
		String body = null;
		body = fetch(this.root_url());
		if (body == null) {
			Log.d(TAG, "null body on fetch(root_url())");
			return("unknown");
		}

		CleanerProperties props = new CleanerProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);
		
		TagNode node = new HtmlCleaner(props).clean(body);
			
		Object links[];
		TagNode link;
		
		try {
			links = node.evaluateXPath("//table//tr/td/font/b/a");
			if (links.length > 0) {
				link = ((TagNode) links[0]);
			} else {
				link = null;
			}
		} catch (XPatherException e) {
			Log.printStackTrace(e);
			Log.d(TAG, "XPatherException: " + e.getMessage());
			// TODO Auto-generated catch block
			return(try_regexp_version(body));
		}
		
		if (link == null) {
			Log.d(TAG, "null link");
			return(try_regexp_version(body));
		}
		String lt = link.getText().toString();
		Log.d(TAG, "link text: " + lt);
		Pattern p = Pattern.compile("Xymon \\d.\\d.\\d.*");
		Matcher m = p.matcher(lt);
		if (m.find()) {
			Pattern pv = Pattern.compile("\\d\\.\\d\\.\\d");
			Matcher mv = pv.matcher(m.group());
			if (mv.find()) {
				Log.d(TAG, "found version: " + mv.group());
				return(mv.group());
			} else {
				Log.d(TAG, "couldn't find version");
				return(try_regexp_version(body));
			}
		} else {
			Log.d(TAG, "couldn't find Xymon");
			return(try_regexp_version(body));
		}
	}
	
	public String try_regexp_version(String body) {
		Log.d(TAG, "trying regexp");
		Pattern p = Pattern.compile("Xymon \\d.\\d.\\d.*");
		Matcher m = p.matcher(body);
		if (m.find()) {
			Pattern pv = Pattern.compile("\\d\\.\\d\\.\\d");
			Matcher mv = pv.matcher(m.group());
			if (mv.find()) {
				Log.d(TAG, "found version: " + mv.group());
				return(mv.group());
			} else {
				Log.d(TAG, "couldn't find version");
				return("unknown");
			}
		} else {
			Log.d(TAG, "couldn't find Xymon");
			return("unknown");
		}

	}
	
	public void fetch_non_green_view() throws XymonQVException {
		String url = m_xv.non_green_url();
		Log.d(TAG, String.format("fetch_non_green_view(%s)", url));
		m_last_non_green_body = fetch(url);
		m_last_query = m_xv.parse_non_green_body();
	}
	
	public void fetch_target() throws XymonQVException {
		String url = "";
		if (m_view == CRITICAL) {
			url = m_xv.critical_url();
			m_last_critical_body = fetch(url);
			m_last_query = m_xv.parse_critical_body();
		} else {
			url = m_xv.non_green_url();
			m_last_non_green_body = fetch(url);
			m_last_query = m_xv.parse_non_green_body();
		}
		Log.d(TAG, String.format("found %d hosts", m_last_query.hosts().size()));
	}
	
	public XymonQuery refresh() throws XymonQVException {
		Log.d(TAG, "refresh()");
		try {
			if (m_version.length() == 0 || m_version.equals("unknown")) {
				m_version = fetch_version();
				m_has_api_target = check_api_target();
				
				String force_version = ((XymonQVApplication) m_context.getApplicationContext()).prefs.getString("force_version", "");
				if (force_version.length() > 0) {
					m_xv = XymonVersionFactory.for_version(force_version);
				} else if (m_has_api_target) {
					m_xv = XymonVersionFactory.api_parser(m_version);
				} else {
					m_xv = XymonVersionFactory.for_version(m_version);
				}
				m_xv.set_server(this);
			} else {
				Log.d(TAG, String.format("using cached version: %s", m_version));
			}
			Log.d(TAG, String.format("found version: %s", m_version));
			fetch_target();
		} catch (XymonQVException e) {
			throw e;
		}
		return(m_last_query);
	}

	public String color() {
		return(m_last_query.color());
	}
	
	public ArrayList<XymonHost> hosts() {
		return(m_last_query.hosts());
	}
	
	public XymonHost get_host_by_hostname(String hostname) {
		return (m_last_query.get_host_by_hostname(hostname));
	}
	
	public Date last_updated() {
		return(m_last_query.last_updated());
	}
	
	public String fetch(String url) throws XymonQVException {
		Log.d(TAG, "fetching: " + url);
		String app_ver;
		try {
			app_ver = m_context.getPackageManager().getPackageInfo(m_context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			app_ver = "8.6.7";
		}
		
		HttpClient client = new DefaultHttpClient(m_conn_manager, m_http_params);

		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			Log.printStackTrace(e);
			throw new XymonQVException(String.format("URI Syntax Error: %s", url));
		}
		
		Log.d(TAG, String.format("scheme: %s", uri.getScheme()));
		Log.d(TAG, String.format("host: %s", uri.getHost()));
		Log.d(TAG, String.format("port: %d", uri.getPort()));
		Log.d(TAG, String.format("path: %s", uri.getPath()));
		
		HttpGet get = new HttpGet(uri);
		get.setHeader("User-Agent", String.format("XymonQV v%s", app_ver));
		HttpResponse res = null;
		try {
			res = client.execute(get, m_http_context);
		} catch(ConnectTimeoutException e) {
			Log.printStackTrace(e);
			ConnectionErrorException ne = new ConnectionErrorException("Connection Error: " + e.getMessage());
			throw ne;
		} catch (HttpHostConnectException e) {
			Log.printStackTrace(e);
			ConnectionErrorException ne = new ConnectionErrorException("Connection Error: " + e.getMessage());
			throw ne;
		} catch (UnknownHostException e) {
			Log.printStackTrace(e);
			ConnectionErrorException ne = new ConnectionErrorException("Unknown Host: " + e.getMessage());
			throw ne;
		} catch (ClientProtocolException e) {
			Log.printStackTrace(e);
			throw new XymonQVException(e.getCause().getMessage());
		} catch (IOException e) {
			Log.printStackTrace(e);
			throw new XymonQVException("Error:\n" + e.getClass() + "\n" + e.getMessage());
		}
		String body = "";
		try {
			body = HttpHelper.readBody(res);
		} catch (NotFoundException e) {
			NotFoundException ne = new NotFoundException(String.format("URL Not Found: %s", url));
			throw ne;
		}
		return(body);
	}
}
