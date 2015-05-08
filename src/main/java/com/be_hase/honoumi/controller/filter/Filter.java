package com.be_hase.honoumi.controller.filter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.be_hase.honoumi.exception.FilterException;

public interface Filter {
	boolean filter(ChannelHandlerContext ctx, MessageEvent evt) throws FilterException;
}
