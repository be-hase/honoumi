package com.be_hase.honoumi.netty.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.netty.server.Server;
import com.google.inject.Inject;

public class MonitoringHandler extends SimpleChannelUpstreamHandler {
	//private static Logger logger = LoggerFactory.getLogger(HttpKeepAliveHandler.class);
	
	@Inject
	private Server server;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		Channel channel = evt.getChannel();
		
		if (server.isSupportMonitoring() && server.isNowMonitoring()) {
			ChannelAttachment channelAttachment = (ChannelAttachment)channel.getAttachment();
			if (channelAttachment == null) {
				channelAttachment = new ChannelAttachment();
			}
			channelAttachment.setMonitoring(true);
			channelAttachment.setStartTime(System.currentTimeMillis());
			channel.setAttachment(channelAttachment);
		}
		
		ctx.sendUpstream(evt);
	}

}
