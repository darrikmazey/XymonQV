package com.darmasoft.xymon;

public class ConnectionErrorException extends XymonQVException {

	private static final long serialVersionUID = 1L;

	public ConnectionErrorException() {
		super("Connection Error");
		// TODO Auto-generated constructor stub
	}

	public ConnectionErrorException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ConnectionErrorException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public ConnectionErrorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
