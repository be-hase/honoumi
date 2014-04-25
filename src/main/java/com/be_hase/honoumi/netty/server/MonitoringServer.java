package com.be_hase.honoumi.netty.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.controller.MonitoringController;
import com.be_hase.honoumi.guice.MonitoringServerModule;
import com.be_hase.honoumi.routing.Router;
import com.espertech.esper.client.EPServiceProviderManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Stage;

public class MonitoringServer extends AbstractServer {
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	private static final Object LOCK = new Object();
	private final static String SERVER_NAME = "monitoring";
	
	private static MonitoringServer monitoringServer;
	
	private Map<String, Server> servers;
	
	/**
	 * create server<br>
	 * <br>
	 * @param server
	 * @return
	 */
	public static MonitoringServer create(Server server) {
		Set<Server> servers = Sets.newHashSet();
		servers.add(server);
		return create(servers, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
	}
	
	/**
	 * create server<br>
	 * <br>
	 * @param server
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static MonitoringServer create(Server server, ServerSocketChannelFactory serverSocketChannelFactory) {
		Set<Server> servers = Sets.newHashSet();
		servers.add(server);
		return create(servers, serverSocketChannelFactory);
	}
	
	/**
	 * create server<br>
	 * <br>
	 * @param servers
	 * @return
	 */
	public static MonitoringServer create(Set<Server> servers) {
		return create(servers, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
	}
	
	/**
	 * create server<br>
	 * <br>
	 * @param servers
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static MonitoringServer create(Set<Server> servers, ServerSocketChannelFactory serverSocketChannelFactory) {
		synchronized (LOCK) {
			if (monitoringServer != null) {
				return monitoringServer;
			}
			
			// monitoring router
			Router router = new Router();
			router.GET().route("/monitor/statuses").with(MonitoringController.class, "statuses");
			router.PUT().route("/monitor/statuses").with(MonitoringController.class, "editStatuses");
			router.GET().route("/monitor/{serverName}/status").with(MonitoringController.class, "status");
			router.PUT().route("/monitor/{serverName}/status").with(MonitoringController.class, "editStatus");
			router.GET().route("/monitor/queries").with(MonitoringController.class, "queries");
			router.DELETE().route("/monitor/queries").with(MonitoringController.class, "deleteQueries");
			router.GET().route("/monitor/query/{queryName}").with(MonitoringController.class, "query");
			router.POST().route("/monitor/query/{queryName}").with(MonitoringController.class, "saveQuery");
			router.DELETE().route("/monitor/query/{queryName}").with(MonitoringController.class, "deleteQuery");
			
			// create server
			monitoringServer = new MonitoringServer();
			
			// set server basic info
			monitoringServer.setBasicInfos(SERVER_NAME, 10081, router);
			
			// set monitoring server
			monitoringServer.servers = Maps.newHashMap();
			for (Server server: servers) {
				logger.info("Monitor {}", server.getServerName());
				server.setSupportMonitoring(true);
				server.setEpService(EPServiceProviderManager.getDefaultProvider());
				monitoringServer.servers.put(server.getServerName(), server);
			}
			
			// create injector
			monitoringServer.injector = Guice.createInjector(Stage.PRODUCTION, new MonitoringServerModule(monitoringServer));
			
			// create and set serverBootstrap
			monitoringServer.serverBootstrap = new ServerBootstrap(serverSocketChannelFactory);
			monitoringServer.serverBootstrap.setPipelineFactory(monitoringServer.injector.getInstance(ChannelPipelineFactory.class));
			monitoringServer.setNettyOptions();
			logger.info("{}-serverBootstrap option is {}", monitoringServer.serverName, monitoringServer.serverBootstrap.getOptions());
			
			return monitoringServer;
		}
	}
	
	/**
	 * get instance.
	 * @return
	 */
	public static MonitoringServer getInstance() {
		synchronized (LOCK) {
			return monitoringServer;
		}
	}
	
	// getter
	public Map<String, Server> getServers() {
		return servers;
	}
}