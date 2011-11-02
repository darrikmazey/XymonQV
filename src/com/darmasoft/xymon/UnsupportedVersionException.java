package com.darmasoft.xymon;

public class UnsupportedVersionException extends XymonQVException {

	private static final long serialVersionUID = 1L;

	private String m_version;
	
	public UnsupportedVersionException() {
		// TODO Auto-generated constructor stub
	}

	public UnsupportedVersionException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public UnsupportedVersionException(String version, String detailMessage) {
		super(detailMessage);
		m_version = version;
	}
	
	public String version() {
		return(m_version);
	}
	
	public UnsupportedVersionException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public UnsupportedVersionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
