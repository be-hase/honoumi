package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.util.Utils;

public class HttpKeepAliveHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = LoggerFactory.getLogger(HttpKeepAliveHandler.class);

	private final boolean isKeepAliveSupported;
	
	public HttpKeepAliveHandler(boolean isKeepAliveSupported) {
		this.isKeepAliveSupported = isKeepAliveSupported;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		Channel channel = evt.getChannel();
		
		try {
			ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(channel);
			channelAttachment.setKeepAliveSupported(isKeepAliveSupported);
		} catch (Exception e) {
			logger.error(Utils.stackTraceToStr(e));
		}
		
		ctx.sendUpstream(evt);
	}
}
