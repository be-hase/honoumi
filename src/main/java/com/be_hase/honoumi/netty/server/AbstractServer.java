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
import com.be_hase.honoumi.util.Utils;
import com.google.inject.Injector;

public abstract class AbstractServer implements IServer {
	private static Logger logger = LoggerFactory.getLogger(AbstractServer.class);
	
	private static final int DEFAULT_MAX_CONTENT_LENGTH = 65535;
	
	protected String serverName;
	protected int port;
	protected String charsetStr;
	protected Charset charset;
	protected boolean suppportKeepAlive;
	protected boolean supportChunkAggregate;
	protected int chunkAggregateMaxContentLength;
	protected boolean supportContentCompress;
	
	protected Router router;
	protected Injector injector;
	protected ServerBootstrap serverBootstrap;
	
	protected void setBasicInfos(String serverName, int defaultPort, Router router) {
		// set server basic info
		this.serverName = serverName;
		port = ApplicationProperties.getInt(serverName + ".bind.port", defaultPort);
		charsetStr = ApplicationProperties.get(serverName + ".http.encoding", "UTF-8");
		charset = Charset.forName(charsetStr);
		suppportKeepAlive = ApplicationProperties.getBoolean(serverName + ".http.keepAlive", false);
		supportChunkAggregate = ApplicationProperties.getBoolean(serverName + ".http.chunkAggregate", false);
		chunkAggregateMaxContentLength = ApplicationProperties.getInt(serverName + ".http.chunkAggregate.maxContentLength", DEFAULT_MAX_CONTENT_LENGTH);
		supportContentCompress = ApplicationProperties.getBoolean(serverName + ".http.contentCompress", false);
		logger.info("Create {} ...", serverName);
		logger.info("{}-port is {}", serverName, port);
		logger.info("{}-charset is {}", serverName, charsetStr);
		logger.info("{}-suppportKeepAlive is {}", serverName, suppportKeepAlive);
		logger.info("{}-supportChunkAggregate is {}", serverName, supportChunkAggregate);
		if (this.supportChunkAggregate) {
			logger.info("{}-chunkAggregateMaxContentLength is {}", serverName, chunkAggregateMaxContentLength);
		}
		logger.info("{}-supportContentCompress is {}", serverName, supportContentCompress);
		
		// compile server router
		this.router = router;
		logger.info("{} routes as follows.", serverName);
		this.router.compileRoutes();
	}
	
	protected void setNettyOptions() {
		String prefix = serverName + ".netty.options";
		List<String> keys = ApplicationProperties.getKeys(prefix);
		for (String key: keys) {
			String prefixWithDot = prefix + ".";
			if (key.startsWith(prefixWithDot)) {
				String optionKey = StringUtils.removeStart(key, prefixWithDot);
				String optionVal = ApplicationProperties.get(key);
				if (optionVal != null) {
					serverBootstrap.setOption(optionKey, optionVal);
				}
			}
		}
	}
	
	public void start() {
		serverBootstrap.bind(new InetSocketAddress(getPort()));
		logger.info("{} has started in {} mode. Port is {}", serverName, ApplicationProperties.getEnvironment(), port);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Starting shutdown {} ...", serverName);
				try {
					serverBootstrap.shutdown();
					logger.info("Shutdown {} successfully.", serverName);
				} catch (Exception e) {
					logger.error("Shutdown {} unsafed.", serverName);
					logger.error("Stacktrace is {}", Utils.stackTraceToStr(e));
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
