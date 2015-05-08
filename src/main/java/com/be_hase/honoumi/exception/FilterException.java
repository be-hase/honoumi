package com.be_hase.honoumi.exception;

import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class FilterException extends AbstractErrorResponseException {
	private static final long serialVersionUID = -5307206415430855120L;

	public FilterException(Object response) {
		super(HttpResponseStatus.BAD_REQUEST, null, response, null, null);
	}

	public FilterException(Object response, String message, Throwable cause) {
		super(HttpResponseStatus.BAD_REQUEST, null, response, message, cause);
	}

	public FilterException(HttpResponseStatus status, Object response) {
		super(status, null, response, null, null);
	}

	public FilterException(HttpResponseStatus status, Object response, String message, Throwable cause) {
		super(status, null, response, message, cause);
	}

	public FilterException(Map<String, String> headers, Object response) {
		super(HttpResponseStatus.BAD_REQUEST, headers, response, null, null);
	}

	public FilterException(Map<String, String> headers, Object response,
			String message, Throwable cause) {
		super(HttpResponseStatus.BAD_REQUEST, headers, response, message, cause);
	}

	public FilterException(HttpResponseStatus status, Map<String, String> headers, Object response) {
		super(status, headers, response, null, null);
	}

	public FilterException(HttpResponseStatus status, Map<String, String> headers, Object response,
			String message, Throwable cause) {
		super(status, headers, response, message, cause);
	}
}
