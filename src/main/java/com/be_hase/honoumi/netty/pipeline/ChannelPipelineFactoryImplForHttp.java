package com.be_hase.honoumi.netty.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.netty.handler.HttpKeepAliveHandler;
import com.be_hase.honoumi.netty.handler.HttpRequestHandler;
import com.be_hase.honoumi.netty.server.Server;
import com.google.inject.Inject;

public class ChannelPipelineFactoryImplForHttp implements ChannelPipelineFactory {
	private static Logger logger = LoggerFactory.getLogger(ChannelPipelineFactoryImplForHttp.class);

	@Inject
	private HttpRequestHandler httpRequestHandler;
	
	@Inject
	private Server server;
	
	public ChannelPipeline getPipeline() throws Exception {
		String serverName = server.getServerName();
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new HttpRequestDecoder());
		
		if (ApplicationProperties.getBoolean(serverName + ".http.chunkAggregate", true)) {
			logger.info(serverName + ".http.chunkAggregate = true");
			pipeline.addLast("aggregator", new HttpChunkAggregator(ApplicationProperties.getInt(serverName + ".http.chunkAggregate.maxContentLength", 65535)));
		} else {
			logger.info(serverName + ".http.chunkAggregate = false");
		}
		
		pipeline.addLast("encoder", new HttpResponseEncoder());
		
		if (ApplicationProperties.getBoolean(serverName + ".http.contentCompress", false)) {
			logger.info(serverName + ".http.contentCompress = true");
			pipeline.addLast("deflater", new HttpContentCompressor());
		} else {
			logger.info(serverName + ".http.contentCompress = false");
		}
		
		pipeline.addLast("httpKeepAlive", new HttpKeepAliveHandler(ApplicationProperties.getBoolean(serverName + ".http.keepAlive", false)));
		
		pipeline.addLast("httpRequestHandler", httpRequestHandler);
		
		return pipeline;
	}
}
