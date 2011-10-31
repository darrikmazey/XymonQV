package com.darmasoft.xymon;

public class InvalidUsernameOrPasswordException extends XymonQVException {

	private static final long serialVersionUID = 1L;

	public InvalidUsernameOrPasswordException() {
		super("Invalid Username or Password");
		// TODO Auto-generated constructor stub
	}

	public InvalidUsernameOrPasswordException(String detailMessage,
			Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public InvalidUsernameOrPasswordException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public InvalidUsernameOrPasswordException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
