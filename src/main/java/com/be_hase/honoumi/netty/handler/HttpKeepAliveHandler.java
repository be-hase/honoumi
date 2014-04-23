package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.be_hase.honoumi.domain.ChannelAttachment;

public class HttpKeepAliveHandler extends SimpleChannelUpstreamHandler {
	//private static Logger logger = LoggerFactory.getLogger(HttpKeepAliveHandler.class);

	private final boolean isKeepAliveSupported;
	
	public HttpKeepAliveHandler(boolean isKeepAliveSupported) {
		this.isKeepAliveSupported = isKeepAliveSupported;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		Channel channel = evt.getChannel();
		
		ChannelAttachment channelAttachment = (ChannelAttachment)channel.getAttachment();
		if (channelAttachment == null) {
			channelAttachment = new ChannelAttachment();
		}
		channelAttachment.setKeepAliveSupported(isKeepAliveSupported);
		channel.setAttachment(channelAttachment);
		
		ctx.sendUpstream(evt);
	}
}
