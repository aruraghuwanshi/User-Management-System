package com.appsdeveloperblog.app.ws.exceptions;

public class UserServiceException extends RuntimeException {

	private static final long serialVersionUID = 8818610030066768509L;
	
	public UserServiceException(String message)
	{
		super(message);
	}

}
