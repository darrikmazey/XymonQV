package com.darmasoft.xymon;

public class NotFoundException extends XymonQVException {

	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super("URL Not Found");
	}

	public NotFoundException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public NotFoundException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
