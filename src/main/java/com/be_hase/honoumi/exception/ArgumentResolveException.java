package com.be_hase.honoumi.exception;

import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.be_hase.honoumi.domain.ResponseError;

public class ArgumentResolveException extends Exception {
	private static final long serialVersionUID = -8047985686021013861L;
	private HttpResponseStatus status;
	private Map<String, String> headers;
	private Object response;
	
	public ArgumentResolveException() {
		this(HttpResponseStatus.BAD_REQUEST, null, new ResponseError("Argument Resolve Error", "Check your paramer."));
	}
	public ArgumentResolveException(Object response) {
		this(HttpResponseStatus.BAD_REQUEST, null, response);
	}
	public ArgumentResolveException(HttpResponseStatus status, Object response) {
		this(status, null, response);
	}
	public ArgumentResolveException(Map<String, String> headers, Object response) {
		this(HttpResponseStatus.BAD_REQUEST, headers, response);
	}
	public ArgumentResolveException(HttpResponseStatus status, Map<String, String> headers, Object response) {
		this.setStatus(status);
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