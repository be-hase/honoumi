package com.be_hase.honoumi.domain;


public class ChannelAttachment extends BaseDomain {
	private boolean isKeepAliveSupported = false;
	
	private boolean isMonitoring = false;
	private long startTime = 0;

	public boolean isKeepAliveSupported() {
		return isKeepAliveSupported;
	}
	public void setKeepAliveSupported(boolean isKeepAliveSupported) {
		this.isKeepAliveSupported = isKeepAliveSupported;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public boolean isMonitoring() {
		return isMonitoring;
	}
	public void setMonitoring(boolean isMonitoring) {
		this.isMonitoring = isMonitoring;
	}
}