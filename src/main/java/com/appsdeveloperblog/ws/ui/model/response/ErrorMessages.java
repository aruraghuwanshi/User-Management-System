package com.appsdeveloperblog.ws.ui.model.response;

public enum ErrorMessages {
	MISSING_REQUIRED_FIELDS("Missing required fields. Please check documentation"),
	RECORD_ALREADY_EXISTS("Record already Exists"),
	INTERNAL_SERVER_ERROR("Internal Server Error"),
	NO_RECORD_FOUND("Record with provided id is not found"),
	AUTHENTICATION_FAILED("Authentication Failed"),
	COULD_NOT_UPDATE_RECORD("Could not update record"),
	COULD_NOT_DELETE_RECORD("Could not delete record"),
	EMAIL_ADDRESS_NOT_VERIFIED("Email address not verified");
	
	private String errorMessage;
	
	ErrorMessages(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
