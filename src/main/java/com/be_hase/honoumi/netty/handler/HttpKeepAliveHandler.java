package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.netty.server.IServer;
import com.be_hase.honoumi.util.Utils;
import com.google.inject.Inject;

public class HttpKeepAliveHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = LoggerFactory.getLogger(HttpKeepAliveHandler.class);

	@Inject
	private IServer server;
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		logger.debug("HttpKeepAliveHandler.messageReceived called.");
		
		Channel channel = evt.getChannel();
		try {
			ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(channel);
			channelAttachment.setKeepAliveSupported(server.isSuppportKeepAlive());
		} catch (Exception e) {
			logger.error(Utils.stackTraceToStr(e));
		}
		
		ctx.sendUpstream(evt);
	}
}
