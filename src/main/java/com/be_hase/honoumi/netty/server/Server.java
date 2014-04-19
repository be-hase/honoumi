package com.be_hase.honoumi.netty.server;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.guice.CoreModule;
import com.be_hase.honoumi.netty.pipeline.ChannelPipelineFactoryImplForHttp;
import com.be_hase.honoumi.routing.Router;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class Server {
	private static Logger logger = LoggerFactory.getLogger(Server.class);

	private String serverName;
	private ServerBootstrap serverBootstrap;
	private Injector injector;
	private String charsetStr;
	private Charset charset;
	private int port;
	private Router router;

	private Server() {
	};

	public static Server create(String serverName, Router router) {
		return create(serverName, router, null, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));
	}

	public static Server create(String serverName, Router router, List<AbstractModule> modules) {
		return create(serverName, router, modules, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));
	}

	public static Server create(String serverName, Router router, ServerSocketChannelFactory serverSocketChannelFactory) {
		return create(serverName, router, null, serverSocketChannelFactory);
	}

	public static Server create(String serverName, Router router, List<AbstractModule> modules,
			ServerSocketChannelFactory serverSocketChannelFactory) {
		checkArgument(StringUtils.isNotBlank(serverName), "serverName is blank.");
		checkArgument(router != null, "router is null");

		Server server = new Server();

		server.serverName = serverName;
		server.port = ApplicationProperties.getInt(serverName + ".bind.port", 10080);
		server.charsetStr = ApplicationProperties.get(serverName + ".http.encoding", "UTF-8");
		server.charset = Charset.forName(server.charsetStr);
		server.router = router;
		logger.info("Create server...");
		logger.info("server-name is {}", server.serverName);
		logger.info("server-port is {}", server.port);
		logger.info("server-charset is {}", server.charsetStr);

		logger.info("server routes as follows.");
		server.router.compileRoutes();

		List<AbstractModule> modulesForCreate = Lists.newArrayList();
		modulesForCreate.add(new CoreModule(server));
		if (modules != null) {
			for (AbstractModule item: modules) {
				logger.info("server regists guice module... {}", item.getClass().getSimpleName());
			}
			modulesForCreate.addAll(modules);
		}
		server.injector = Guice.createInjector(Stage.PRODUCTION, modulesForCreate);

		server.serverBootstrap = new ServerBootstrap(serverSocketChannelFactory);

		ChannelPipelineFactoryImplForHttp pipeline = server.injector.getInstance(ChannelPipelineFactoryImplForHttp.class);
		server.serverBootstrap.setPipelineFactory(pipeline);

		List<String> keys = ApplicationProperties.getKeys(serverName + ".netty.options");
		for (String key: keys) {
			String prefix = serverName + ".netty.options.";
			if (key.startsWith(prefix)) {
				String optionKey = StringUtils.removeStart(key, prefix);
				String optionVal = ApplicationProperties.get(key);
				if (optionVal != null) {
					server.serverBootstrap.setOption(optionKey, optionVal);
				}
			}
		}
		logger.info("server-serverBootstrap option is {}", server.serverBootstrap.getOptions());

		return server;
	}

	public void start() {
		serverBootstrap.bind(new InetSocketAddress(port));
		logger.info("{} has started in {} mode. Port is {}", serverName, ApplicationProperties.getEnvironment(), port);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Starting shutdown...");
				try {
					serverBootstrap.shutdown();
					logger.info("Shutdown successfully.");
				} catch (Exception e) {
					logger.error("Shutdown unsafed.", e);
				}
			}
		});
	}

	public void stop() {
		serverBootstrap.shutdown();
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
