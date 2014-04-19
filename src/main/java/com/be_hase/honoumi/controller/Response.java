package com.be_hase.honoumi.controller;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class Response {
	
	private Response(){};
	
	public static void execute(MessageEvent evt, HttpResponseStatus status, Map<String, String> headers, String content) {
		HttpRequest request = (HttpRequest)evt.getMessage();
		
		if (HttpHeaders.is100ContinueExpected(request)) {
			send100Continue(evt);
		}
		
		boolean isKeepAlive = HttpHeaders.isKeepAlive(request);
		
		DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		res.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
		res.headers().add(CONTENT_TYPE, "application/json");
		if (isKeepAlive) {
			res.headers().add(HttpHeaders.Names.CONTENT_LENGTH, res
					.getContent().readableBytes());
			res.headers().add(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		}
		if (headers != null) {
			for (Map.Entry<String, String> e: headers.entrySet()) {
				// can override
				res.headers().set(e.getKey(), e.getValue());
			}
		}
		
		// Encode the cookie.
		String cookieString = request.headers().get(HttpHeaders.Names.COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (Cookie cookie : cookies) {
					cookieEncoder.addCookie(cookie);
					res.headers().add(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
				}
			}
		}

		ChannelFuture future = evt.getChannel().write(res);
		if (!isKeepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private static void send100Continue(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		e.getChannel().write(response);
	}
}
