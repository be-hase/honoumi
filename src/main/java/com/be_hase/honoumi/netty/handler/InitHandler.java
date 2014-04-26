package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.be_hase.honoumi.domain.ChannelAttachment;

public class InitHandler extends SimpleChannelUpstreamHandler {
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		Channel channel = evt.getChannel();
		
		ChannelAttachment channelAttachment = new ChannelAttachment();
		channel.setAttachment(channelAttachment);
		
		ctx.sendUpstream(evt);
	}

}
