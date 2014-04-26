package com.be_hase.honoumi.netty.handler;

import java.util.HashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.util.Utils;
import com.google.inject.Inject;

public class MonitoringHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = LoggerFactory.getLogger(HttpKeepAliveHandler.class);
	
	@Inject
	private Server server;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		Channel channel = evt.getChannel();
		
		try {
			if (server.isNowMonitoring()) {
				ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(channel);
				channelAttachment.setNowMonitoring(true);
				channelAttachment.setServer(server);
				channelAttachment.setStartTime(System.currentTimeMillis());
			}
		} catch (Exception e) {
			logger.error(Utils.stackTraceToStr(e));
		}
		
		ctx.sendUpstream(evt);
	}

}
