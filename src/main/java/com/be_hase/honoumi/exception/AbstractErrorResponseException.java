package com.be_hase.honoumi.exception;

import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class AbstractErrorResponseException extends Exception {
	private static final long serialVersionUID = -8047985686021013861L;
	private HttpResponseStatus status;
	private Map<String, String> headers;
	private Object response;

	public AbstractErrorResponseException(HttpResponseStatus status, Map<String, String> headers, Object response,
			String message, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.headers = headers;
		this.response = response;
	}

	public Object getResponse() {
		return this.response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
}
