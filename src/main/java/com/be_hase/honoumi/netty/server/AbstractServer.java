package com.be_hase.honoumi.netty.server;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.routing.Router;
import com.google.inject.Injector;

public abstract class AbstractServer implements IServer {
	private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);
	
	protected String serverName;
	protected int port;
	protected String charsetStr;
	protected Charset charset;
	private boolean suppportKeepAlive;
	private boolean supportChunkAggregate;
	private int chunkAggregateMaxContentLength;
	private boolean supportContentCompress;
	
	protected Router router;
	protected Injector injector;
	protected ServerBootstrap serverBootstrap;
	
	protected void setBasicInfos(String serverName, int defaultPort, Router router) {
		// set server basic info
		this.serverName = serverName;
		this.port = ApplicationProperties.getInt(this.serverName + ".bind.port", defaultPort);
		this.charsetStr = ApplicationProperties.get(this.serverName + ".http.encoding", "UTF-8");
		this.charset = Charset.forName(this.charsetStr);
		this.suppportKeepAlive = ApplicationProperties.getBoolean(this.serverName + ".http.keepAlive", false);
		this.supportChunkAggregate = ApplicationProperties.getBoolean(this.serverName + ".http.chunkAggregate", false);
		this.chunkAggregateMaxContentLength = ApplicationProperties.getInt(this.serverName + ".http.chunkAggregate.maxContentLength", 65535);
		this.supportContentCompress = ApplicationProperties.getBoolean(this.serverName + ".http.contentCompress", false);
		logger.info("Create {} ...", this.serverName);
		logger.info("{}-port is {}", this.serverName, this.port);
		logger.info("{}-charset is {}", this.serverName, this.charsetStr);
		logger.info("{}-suppportKeepAlive is {}", this.serverName, this.suppportKeepAlive);
		logger.info("{}-supportChunkAggregate is {}", this.serverName, this.supportChunkAggregate);
		if (this.supportChunkAggregate) {
			logger.info("{}-chunkAggregateMaxContentLength is {}", this.serverName, this.chunkAggregateMaxContentLength);
		}
		logger.info("{}-supportContentCompress is {}", this.serverName, this.supportContentCompress);
		
		// compile server router
		this.router = router;
		logger.info("{} routes as follows.", this.serverName);
		this.router.compileRoutes();
	}
	
	protected void setNettyOptions() {
		String prefix = getServerName() + ".netty.options";
		List<String> keys = ApplicationProperties.getKeys(prefix);
		for (String key: keys) {
			String prefixWithDot = prefix + ".";
			if (key.startsWith(prefixWithDot)) {
				String optionKey = StringUtils.removeStart(key, prefixWithDot);
				String optionVal = ApplicationProperties.get(key);
				if (optionVal != null) {
					getServerBootstrap().setOption(optionKey, optionVal);
				}
			}
		}
	}
	
	public void start() {
		getServerBootstrap().bind(new InetSocketAddress(getPort()));
		logger.info("{} has started in {} mode. Port is {}", getServerName(), ApplicationProperties.getEnvironment(), getPort());
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Starting shutdown...");
				try {
					getServerBootstrap().shutdown();
					logger.info("Shutdown successfully.");
				} catch (Exception e) {
					logger.error("Shutdown unsafed.", e);
				}
			}
		});
	}
	
	public void stop() {
		getServerBootstrap().shutdown();
	}
	
	// getter
	public String getServerName() {
		return serverName;
	}
	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}
	public Injector getInjector() {
		return injector;
	}
	public String getCharsetStr() {
		return charsetStr;
	}
	public Charset getCharset() {
		return charset;
	}
	public int getPort() {
		return port;
	}
	public Router getRouter() {
		return router;
	}
	public boolean isSuppportKeepAlive() {
		return suppportKeepAlive;
	}
	public boolean isSupportChunkAggregate() {
		return supportChunkAggregate;
	}
	public int getChunkAggregateMaxContentLength() {
		return chunkAggregateMaxContentLength;
	}
	public boolean isSupportContentCompress() {
		return supportContentCompress;
	}
}
