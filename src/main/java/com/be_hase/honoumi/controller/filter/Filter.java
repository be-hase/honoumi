package com.be_hase.honoumi.controller.filter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public interface Filter {
	boolean filter(ChannelHandlerContext ctx, MessageEvent evt);
}
