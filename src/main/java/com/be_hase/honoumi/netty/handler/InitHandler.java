package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.util.Utils;

public class InitHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = LoggerFactory.getLogger(InitHandler.class);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		logger.debug("InitHandler.messageReceived called.");
		
		try {
			Channel channel = evt.getChannel();
			ChannelAttachment channelAttachment = new ChannelAttachment();
			channel.setAttachment(channelAttachment);
		} catch (Exception e) {
			logger.debug(Utils.stackTraceToStr(e));
		}
		
		ctx.sendUpstream(evt);
	}
}
