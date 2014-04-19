package com.be_hase.honoumi.domain;

public class ResponseError extends BaseDomain {
	private String error;
	private String message;
	
	public ResponseError() {
	}
	
	public ResponseError(String error, String description) {
		this.error = error;
		this.setMessage(description);
	}

	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
