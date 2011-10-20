package com.darmasoft.xymon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
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
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class XymonServer {
	private String m_host;
	private String m_version = null;
	private String m_color = null;
	private String m_last_non_green_body = null;
	
	private ClientConnectionManager m_conn_manager;
	private HttpContext m_http_context;
	private HttpParams m_http_params;
	
	private static final String TAG = "XymonServer";

	public XymonServer(String host) {
		super();
		m_host = host;
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
		credentials_provider.setCredentials(new AuthScope(m_host, AuthScope.ANY_PORT), new UsernamePasswordCredentials("darrik", "wiscons9n"));
		m_conn_manager = new ThreadSafeClientConnManager(m_http_params, scheme_registry);
		m_http_context = new BasicHttpContext();
		m_http_context.setAttribute("http.auth.credentials-provider", credentials_provider);
	}

	public String host() {
		return(m_host);
	}
	
	public String version() {
		if (m_version == null) {
			m_version = fetch_version();
			Log.d(TAG, "FOUND VERSION: " + m_version);
		}
		return(m_version);
	}
	
	public String fetch_version() {
		String body = fetch(root_url());
		Pattern p = Pattern.compile(">Xymon \\d.\\d.\\d.*<\\/[Aa]");
		Matcher m = p.matcher(body);
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
	
	public String root_url() {
		return("https://" + m_host + "/");
	}
	public String non_green_url_for_version(String version) {
		String postfix = "/";
		if (version == "4.3.4") {
			postfix = "/nongreen.html";
		} else if (version == "4.3.5") {
			postfix = "/nongreen.html";
		} else {
			postfix = "/bb2.html";
		}
		return("https://" + m_host + postfix);
	}
	
	public String fetch_non_green_view() {
		m_last_non_green_body = fetch(non_green_url_for_version(version()));
		parse_non_green_body();
		return(m_last_non_green_body);
	}
	
	public String color() {
		if (m_color == null) {
			fetch_non_green_view();
		}
		return(m_color);
	}
	
	public boolean parse_non_green_body()
	{
		Pattern p = Pattern.compile("body bgcolor=\"(.*?)\"",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(m_last_non_green_body);
		if (m.find()) {
			String match = m.group().toLowerCase();
			m_color = match.replace("body bgcolor=\"", "").replace("\"", "");
			return(true);
		}
		return(false);
	}
	public String fetch(String url) {
		Log.d(TAG, "fetching: " + url);
		HttpClient client = new DefaultHttpClient(m_conn_manager, m_http_params);
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse res = client.execute(get, m_http_context);
			return(HttpHelper.readBody(res));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return(null);
	}
}
