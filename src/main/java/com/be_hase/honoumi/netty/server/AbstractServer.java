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
	protected Router router;
	protected Injector injector;
	protected ServerBootstrap serverBootstrap;
	
	protected void setBasicInfos(String serverName, Router router) {
		// set server basic info
		this.serverName = serverName;
		this.port = ApplicationProperties.getInt(this.serverName + ".bind.port", 10080);
		this.charsetStr = ApplicationProperties.get(this.serverName + ".http.encoding", "UTF-8");
		this.charset = Charset.forName(this.charsetStr);
		logger.info("Create {} ...", this.serverName);
		logger.info("{}-port is {}", this.serverName, this.port);
		logger.info("{}-charset is {}", this.serverName, this.charsetStr);
		
		// compile server router
		this.router = router;
		logger.info("{} routes as follows.", this.serverName);
		this.router.compileRoutes();
	}
	
	protected void setNettyOptions() {
		List<String> keys = ApplicationProperties.getKeys(getServerName() + ".netty.options");
		for (String key: keys) {
			String prefix = getServerName() + ".netty.options.";
			if (key.startsWith(prefix)) {
				String optionKey = StringUtils.removeStart(key, prefix);
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
}
