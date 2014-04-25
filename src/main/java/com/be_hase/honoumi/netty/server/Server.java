package com.be_hase.honoumi.netty.server;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.guice.ServerModule;
import com.be_hase.honoumi.netty.pipeline.ChannelPipelineFactoryImplForHttp;
import com.be_hase.honoumi.routing.Router;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;

public class Server extends AbstractServer {
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	
	private boolean supportMonitoring;
	private boolean nowMonitoring = false;

	private Server() {
	};

	/**
	 * create server.<br>
	 * <br>
	 * @param serverName
	 * @param router
	 * @return
	 */
	public static Server create(String serverName, Router router) {
		return create(serverName, router, null, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));
	}

	/**
	 * create server.<br>
	 * <br>
	 * @param serverName
	 * @param router
	 * @param modules
	 * @return
	 */
	public static Server create(String serverName, Router router, List<AbstractModule> modules) {
		return create(serverName, router, modules, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));
	}

	/**
	 * create server.<br>
	 * <br>
	 * @param serverName
	 * @param router
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static Server create(String serverName, Router router, ServerSocketChannelFactory serverSocketChannelFactory) {
		return create(serverName, router, null, serverSocketChannelFactory);
	}

	/**
	 * create server.<br>
	 * <br>
	 * @param serverName
	 * @param router
	 * @param modules
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static Server create(String serverName, Router router, List<AbstractModule> modules,
			ServerSocketChannelFactory serverSocketChannelFactory) {
		checkArgument(StringUtils.isNotBlank(serverName), "serverName is blank.");
		checkArgument(router != null, "router is null");
		
		// create server
		Server server = new Server();

		// set server basic info
		server.setBasicInfos(serverName, router);
		
		// set support monitoring
		server.supportMonitoring = ApplicationProperties.getBoolean(server.serverName + ".monitoring", false);
		logger.info("{}-supportMonitoring is {}", server.serverName, server.supportMonitoring);

		// create injector
		List<AbstractModule> modulesForCreate = Lists.newArrayList();
		modulesForCreate.add(new ServerModule(server));
		if (modules != null) {
			for (AbstractModule item: modules) {
				logger.info("{} regists guice module... {}", server.serverName, item.getClass().getSimpleName());
			}
			modulesForCreate.addAll(modules);
		}
		server.injector = Guice.createInjector(Stage.PRODUCTION, modulesForCreate);

		// create and set serverBootstrap
		server.serverBootstrap = new ServerBootstrap(serverSocketChannelFactory);
		server.serverBootstrap.setPipelineFactory(server.injector.getInstance(ChannelPipelineFactoryImplForHttp.class));
		server.setNettyOptions();
		logger.info("{}-serverBootstrap option is {}", server.serverName, server.serverBootstrap.getOptions());
		
		return server;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Server)) {
			return false;
		}
		return serverName.equals(((Server) obj).getServerName());
	}
	
	// getter
	public boolean isSupportMonitoring() {
		return supportMonitoring;
	}
	public boolean isNowMonitoring() {
		return nowMonitoring;
	}
}