package com.be_hase.honoumi.domain;

public class ChannelAttachment extends BaseDomain {
	private boolean isKeepAliveSupported = false;

	public boolean isKeepAliveSupported() {
		return isKeepAliveSupported;
	}
	public void setKeepAliveSupported(boolean isKeepAliveSupported) {
		this.isKeepAliveSupported = isKeepAliveSupported;
	}
}
