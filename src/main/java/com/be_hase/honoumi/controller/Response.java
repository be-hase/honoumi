package com.be_hase.honoumi.controller;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.netty.server.MonitoringServer;
import com.be_hase.honoumi.util.Utils;
import com.espertech.esper.client.EPRuntime;
import com.google.common.collect.Maps;

public class Response {
	private static Logger logger = LoggerFactory.getLogger(Response.class);
	
	private Response(){};
	
	public static void execute(MessageEvent evt, HttpResponseStatus status, Map<String, String> headers, String content) {
		HttpRequest request = (HttpRequest)evt.getMessage();
		
		if (HttpHeaders.is100ContinueExpected(request)) {
			send100Continue(evt);
		}
		
		ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(evt.getChannel());
		
		boolean isKeepAlive = channelAttachment.isKeepAliveSupported() && HttpHeaders.isKeepAlive(request);
		
		DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		res.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
		res.headers().add(CONTENT_TYPE, "application/json");
		if (isKeepAlive) {
			res.headers().add(HttpHeaders.Names.CONTENT_LENGTH, res.getContent().readableBytes());
			res.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
		if (headers != null) {
			for (Map.Entry<String, String> e: headers.entrySet()) {
				// can override
				res.headers().set(e.getKey(), e.getValue());
			}
		}
		
		ChannelFuture future = evt.getChannel().write(res);
		if (!isKeepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
		
		// if now monitoring, sendEvent to Esper
		if (channelAttachment.isNowMonitoring()) {
			logger.debug("server is now monitoring.");
			
			try {
				EPRuntime epRuntime = channelAttachment.getServer().getEpService().getEPRuntime();
				
				Map<String, Object> accessEvent = Maps.newHashMap();
				accessEvent.put("urlPath", channelAttachment.getUrlPath());
				accessEvent.put("httpMethod", channelAttachment.getHttpMethod());
				accessEvent.put("requestHeaders", channelAttachment.getRequestHeaders());
				accessEvent.put("httpStatusCode", status.getCode());
				accessEvent.put("time", channelAttachment.getStartTime());
				accessEvent.put("responseTime", System.currentTimeMillis() - channelAttachment.getStartTime());
				epRuntime.sendEvent(accessEvent, MonitoringServer.ACCESS_EVENT_TYPE_NAME);
				logger.debug("sendEvent. eventTypeName : {}, event : {}", MonitoringServer.ACCESS_EVENT_TYPE_NAME, accessEvent);
				
				String eventTypeName = channelAttachment.getEventTypeName();
				if (StringUtils.isNotBlank(eventTypeName)) {
					Map<String, Object> event = channelAttachment.getEvent();
					event.putAll(accessEvent);
					epRuntime.sendEvent(event, eventTypeName);
					logger.debug("sendEvent. eventTypeName : {}, event : {}", eventTypeName, event);
				}
			} catch (Exception e) {
				logger.debug(Utils.stackTraceToStr(e));
			}
		} else {
			logger.debug("server is NOT now monitoring.");
		}
	}
	
	private static void send100Continue(MessageEvent e) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		e.getChannel().write(response);
	}
}
