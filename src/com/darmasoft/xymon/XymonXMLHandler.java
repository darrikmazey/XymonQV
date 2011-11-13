package com.darmasoft.xymon;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XymonXMLHandler extends DefaultHandler {

	private static final String TAG = "XymonXMLHandler";
	
	private XymonQuery m_query = null;
	private XymonHost m_host = null;
	private String m_current_character_content = null;
	private boolean m_in_element = false;
	private String m_svc_name = null;
	private String m_svc_color = null;
	private boolean m_svc_acked = false;
	private String m_svc_ack_text = null;
	private int m_svc_ack_time = 0;
	private int m_svc_duration = 0;
	private String m_svc_url = null;
	
	public XymonXMLHandler(XymonQuery q) {
		super();
		m_query = q;
	}
	
	public XymonQuery query() {
		return(m_query);
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
//		Log.d(TAG, String.format("characters(%s)", new String(ch).substring(start, length)));
		if (m_in_element) {
			m_current_character_content = new String(ch).substring(start, length);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
//		Log.d(TAG, "endDocument()");
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
//		Log.d(TAG, String.format("endElement(%s, %s, %s", uri, localName, qName));
		if (localName.equals("Servername")) {
			Log.d(TAG, String.format("found server name: %s", m_current_character_content));
			m_host = m_query.get_host_by_hostname(m_current_character_content);
			if (m_host == null) {
				m_host = new XymonHost(m_current_character_content);
				m_host.setServer(m_query.server());
				m_query.add_host(m_host);
			}
		} else if (localName.equals("Type")) {
			m_svc_name = m_current_character_content;
		} else if (localName.equals("Status")) {
			m_svc_color = m_current_character_content;
		} else if (localName.equals("AckTime")) {
			m_svc_acked = true;
			m_svc_ack_time = Integer.valueOf(m_current_character_content);
		} else if (localName.equals("AckText")) {
			m_svc_acked = true;
			m_svc_ack_text = m_current_character_content;
		} else if (localName.equals("LastChange")) {
			m_svc_duration = Integer.valueOf(m_current_character_content);
			Log.d(TAG, String.format("duration: %d", m_svc_duration));
		} else if (localName.equals("DetailURL")) {
			m_svc_url = String.format("%s%s", m_query.server().root_url_stripped(), m_current_character_content);
		} else if (localName.equals("ServerStatus")) {
			XymonService s = new XymonService(m_svc_name, m_svc_color, m_svc_acked, m_svc_ack_time, m_svc_ack_text, m_svc_duration, m_svc_url);
			m_host.add_service(s);
		}
		m_in_element = false;
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		super.endPrefixMapping(prefix);
//		Log.d(TAG, String.format("endPrefixMapping(%s)", prefix));
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		Log.printStackTrace(e);
		super.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		Log.printStackTrace(e);
		super.fatalError(e);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		super.ignorableWhitespace(ch, start, length);
//		Log.d(TAG, String.format("ignorableWhitespace(%s, %d, %d)", new String(ch).substring(start, length), start, length));
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		super.notationDecl(name, publicId, systemId);
//		Log.d(TAG, String.format("notationDecl(%s, %s, %s)", name, publicId, systemId));
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		super.processingInstruction(target, data);
//		Log.d(TAG, String.format("processingInstruction(%s, %s)", target, data));
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
//		Log.d(TAG, String.format("resolveEntity(%s, %s)", publicId, systemId));
		return super.resolveEntity(publicId, systemId);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator(locator);
//		Log.d(TAG, "setDocumentLocator()");
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		super.skippedEntity(name);
//		Log.d(TAG, String.format("skippedEntity(%s)", name));
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
//		Log.d(TAG, "startDocument()");
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
//		Log.d(TAG, String.format("startElement(%s, %s, %s)", uri, localName, qName));
		m_in_element = true;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		super.startPrefixMapping(prefix, uri);
//		Log.d(TAG, String.format("startPrefixMapping(%s, %s)", prefix, uri));
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
//		Log.d(TAG, String.format("unparsedEntityDecl(%s, %s, %s, %s)", name, publicId, systemId, notationName));
		super.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		Log.printStackTrace(e);
		super.warning(e);
	}

}
