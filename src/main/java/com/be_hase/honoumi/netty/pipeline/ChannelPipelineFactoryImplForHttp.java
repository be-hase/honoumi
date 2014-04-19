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
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new HttpRequestDecoder());
		
		if (ApplicationProperties.getBoolean(server.getServerName() + ".netty.httpChunkAggregator.isEnabled", true)) {
			logger.info(server.getServerName() + ".netty.httpChunkAggregator.isEnabled = true");
			pipeline.addLast("aggregator", new HttpChunkAggregator(ApplicationProperties.getInt(server.getServerName() + ".netty.httpChunkAggregator.maxContentLength", 65535)));
		} else {
			logger.info(server.getServerName() + ".netty.httpChunkAggregator.isEnabled = false");
		}
		
		pipeline.addLast("encoder", new HttpResponseEncoder());
		
		if (ApplicationProperties.getBoolean(server.getServerName() + ".netty.httpContentCompressor.isEnabled", true)) {
			logger.info(server.getServerName() + ".netty.httpContentCompressor.isEnabled = true");
			pipeline.addLast("deflater", new HttpContentCompressor());
		} else {
			logger.info(server.getServerName() + ".netty.httpContentCompressor.isEnabled = false");
		}
		
		pipeline.addLast("httpRequestHandler", httpRequestHandler);
		
		return pipeline;
	}
}
