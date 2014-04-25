package com.be_hase.honoumi.netty.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.be_hase.honoumi.netty.handler.HttpKeepAliveHandler;
import com.be_hase.honoumi.netty.handler.HttpRequestHandler;
import com.be_hase.honoumi.netty.handler.MonitoringHandler;
import com.be_hase.honoumi.netty.server.Server;
import com.google.inject.Inject;

public class ServerChannelPipelineFactory implements ChannelPipelineFactory {
	//private static Logger logger = LoggerFactory.getLogger(ServerChannelPipelineFactory.class);

	@Inject
	private Server server;
	
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("monitoringHandler", server.getInjector().getInstance(MonitoringHandler.class));
		
		// http decoder
		pipeline.addLast("decoder", new HttpRequestDecoder());
		
		// http chunk aggregate
		if (server.isSupportChunkAggregate()) {
			pipeline.addLast("aggregator", new HttpChunkAggregator(server.getChunkAggregateMaxContentLength()));
		}
		
		// http encode
		pipeline.addLast("encoder", new HttpResponseEncoder());
		
		// http conent compress
		if (server.isSupportContentCompress()) {
			pipeline.addLast("deflater", new HttpContentCompressor());
		}
		
		// http keepAlive
		pipeline.addLast("httpKeepAlive", new HttpKeepAliveHandler(server.isSuppportKeepAlive()));
		
		// http request handler
		pipeline.addLast("httpRequestHandler", server.getInjector().getInstance(HttpRequestHandler.class));
		
		return pipeline;
	}
}
