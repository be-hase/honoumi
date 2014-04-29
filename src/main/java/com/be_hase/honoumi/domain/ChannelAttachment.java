package com.be_hase.honoumi.domain;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.be_hase.honoumi.netty.server.Server;


public class ChannelAttachment extends BaseDomain {
	private boolean isKeepAliveSupported = false;
	
	private boolean isNowMonitoring = false;
	private Server server = null;
	private long startTime = 0;
	private String urlPath = "";
	private String httpMethod = "";
	private Map<String, String> requestHeaders = new HashMap<String, String>();
	private Map<String, Object> event = new HashMap<String, Object>();
	private String eventTypeName = "";
	
	public static ChannelAttachment getByChannel(Channel channel) {
		ChannelAttachment channelAttachment = (ChannelAttachment)channel.getAttachment();
		if (channelAttachment == null) {
			channelAttachment = new ChannelAttachment();
			channel.setAttachment(channelAttachment);
		}
		return channelAttachment;
	}

	public boolean isKeepAliveSupported() {
		return isKeepAliveSupported;
	}
	public void setKeepAliveSupported(boolean isKeepAliveSupported) {
		this.isKeepAliveSupported = isKeepAliveSupported;
	}
	public boolean isNowMonitoring() {
		return isNowMonitoring;
	}
	public void setNowMonitoring(boolean isMonitoring) {
		this.isNowMonitoring = isMonitoring;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public Server getServer() {
		return server;
	}
	public void setServer(Server server) {
		this.server = server;
	}
	public Map<String, Object> getEvent() {
		return event;
	}
	public void setEvent(Map<String, Object> event) {
		this.event = event;
	}
	public String getEventTypeName() {
		return eventTypeName;
	}
	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	public String getUrlPath() {
		return urlPath;
	}
	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}
	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}
}