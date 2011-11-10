package com.darmasoft.xymon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class XymonVersionAPI extends XymonVersion {

	public static final String TAG = "XymonVersionAPI";
	
	public XymonVersionAPI(String version) {
		super(version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String critical_url() {
		if (m_server.filter() == null) {
			return(String.format("%sxymon-cgi/appfeed-critical.sh", root_url()));
		} else {
			return(String.format("%sxymon-cgi/appfeed-critical.sh?filter=%s", root_url(), m_server.filter()));
		}
	}

	@Override
	public String non_green_url() {
		if (m_server.filter() == null) {
			return(String.format("%sxymon-cgi/appfeed.sh", root_url()));
		} else {
			return(String.format("%sxymon-cgi/appfeed.sh?filter=%s", root_url(), m_server.filter()));
		}
	}

	@Override
	public XymonQuery parse_non_green_body() {
		String body = m_server.last_non_green_body();
		XymonQuery q = new XymonQuery(m_server);
		q.set_last_updated(new Date());
		q.set_version(m_version);
		
		XymonXMLHandler handler = new XymonXMLHandler(q);
		try {
			byte[] bytes = body.getBytes("ISO-8859-1");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			Log.d(TAG, String.format("sax parser is validating: %b", sp.isValidating()));
			sp.parse(bais, handler);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		q.set_color(q.worst_color());
		q.set_was_ran(true);
		return(q);
	}
	
	@Override
	public XymonQuery parse_critical_body() {
		String body = m_server.last_critical_body();
		XymonQuery q = new XymonQuery(m_server);
		q.set_last_updated(new Date());
		q.set_version(m_version);
		
		XymonXMLHandler handler = new XymonXMLHandler(q);
		try {
			byte[] bytes = body.getBytes("ISO-8859-1");
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			Log.d(TAG, String.format("sax parser is validating: %b", sp.isValidating()));
			sp.parse(bais, handler);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		q.set_color(q.worst_color());
		q.set_was_ran(true);
		return(q);
	}
	
	@Override
	public String service_url(XymonService s) {
		return(String.format("%sxymon-cgi/svcstatus.sh?HOST=%s&SERVICE=%s", root_url(), s.host().hostname(), s.name()));
	}

}
