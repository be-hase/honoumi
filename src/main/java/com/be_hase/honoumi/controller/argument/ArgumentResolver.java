package com.be_hase.honoumi.controller.argument;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.be_hase.honoumi.exception.ArgumentResolveException;

public interface ArgumentResolver<T> {
	public T resolveArgument(ChannelHandlerContext ctx, MessageEvent evt) throws ArgumentResolveException;

	public boolean supportedType(Class<?> klass);
}