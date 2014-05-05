package com.be_hase.honoumi.netty.server;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.controller.MonitoringController;
import com.be_hase.honoumi.domain.MonitoringResultSet;
import com.be_hase.honoumi.guice.MonitoringServerModule;
import com.be_hase.honoumi.listener.MonitoringListener;
import com.be_hase.honoumi.netty.handler.HttpRequestHandler;
import com.be_hase.honoumi.routing.Route;
import com.be_hase.honoumi.routing.Router;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Stage;

public class MonitoringServer extends AbstractServer {
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	private static final Object LOCK = new Object();
	
	public final static String SERVER_NAME = "monitoring";
	public final static String ACCESS_EVENT_TYPE_NAME = "access";
	public final static String DEFAULT_STORE_COUNT = "1000";
	
	private static MonitoringServer monitoringServer;
	
	private Map<String, Server> monitoredServers;
	
	/**
	 * create server for monitoring<br>
	 * <br>
	 * @param monitoredServer
	 * @return
	 */
	public static MonitoringServer create(Server monitoredServer) {
		Set<Server> monitoredServers = Sets.newHashSet();
		monitoredServers.add(monitoredServer);
		return create(monitoredServers, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
	}
	
	/**
	 * create server for monitoring<br>
	 * <br>
	 * @param monitoredServer
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static MonitoringServer create(Server monitoredServer, ServerSocketChannelFactory serverSocketChannelFactory) {
		Set<Server> monitoredServers = Sets.newHashSet();
		monitoredServers.add(monitoredServer);
		return create(monitoredServers, serverSocketChannelFactory);
	}
	
	/**
	 * create server<br>
	 * <br>
	 * @param monitoredServers
	 * @return
	 */
	public static MonitoringServer create(Set<Server> monitoredServers) {
		return create(monitoredServers, new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
	}
	
	/**
	 * create server<br>
	 * <br>
	 * @param monitoredServers
	 * @param serverSocketChannelFactory
	 * @return
	 */
	public static MonitoringServer create(Set<Server> monitoredServers, ServerSocketChannelFactory serverSocketChannelFactory) {
		synchronized (LOCK) {
			if (monitoringServer != null) {
				return monitoringServer;
			}
			
			// monitoring router
			Router router = createMonitoringRouter();
			
			// create server
			monitoringServer = new MonitoringServer();
			
			// set server basic info
			monitoringServer.setBasicInfos(SERVER_NAME, 10081, router);
			
			// set monitored server
			setMonitoredServer(monitoredServers, monitoringServer);
			
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
	
	private static Router createMonitoringRouter() {
		Router router = new Router();
		router.GET().route("/monitor/statuses").with(MonitoringController.class, "statusesAllServer");
		router.PUT().route("/monitor/statuses").with(MonitoringController.class, "editStatusesAllServer");
		router.POST().route("/monitor/statuses/edit").with(MonitoringController.class, "editStatusesAllServer");
		
		router.GET().route("/monitor/{serverName}/status").with(MonitoringController.class, "status");
		router.PUT().route("/monitor/{serverName}/status").with(MonitoringController.class, "editStatus");
		router.POST().route("/monitor/{serverName}/status/edit").with(MonitoringController.class, "editStatus");
		
		router.GET().route("/monitor/queries").with(MonitoringController.class, "queriesAllServer");
		router.PUT().route("/monitor/queries").with(MonitoringController.class, "editQueriesAllServer");
		router.POST().route("/monitor/queries/edit").with(MonitoringController.class, "editQueriesAllServer");
		router.DELETE().route("/monitor/queries").with(MonitoringController.class, "deleteQueriesAllServer");
		router.POST().route("/monitor/queries/delete").with(MonitoringController.class, "deleteQueriesAllServer");
		
		router.GET().route("/monitor/{serverName}/queries").with(MonitoringController.class, "queries");
		router.PUT().route("/monitor/{serverName}/queries").with(MonitoringController.class, "editQueries");
		router.POST().route("/monitor/{serverName}/queries/edit").with(MonitoringController.class, "editQueries");
		router.DELETE().route("/monitor/{serverName}/queries").with(MonitoringController.class, "deleteQueries");
		router.POST().route("/monitor/{serverName}/queries/delete").with(MonitoringController.class, "deleteQueries");
		
		router.GET().route("/monitor/{serverName}/query/{queryName}").with(MonitoringController.class, "query");
		router.POST().route("/monitor/{serverName}/query/{queryName}").with(MonitoringController.class, "addQuery");
		router.PUT().route("/monitor/{serverName}/query/{queryName}").with(MonitoringController.class, "editQuery");
		router.POST().route("/monitor/{serverName}/query/{queryName}/edit").with(MonitoringController.class, "editQuery");
		router.DELETE().route("/monitor/{serverName}/query/{queryName}").with(MonitoringController.class, "deleteQuery");
		router.POST().route("/monitor/{serverName}/query/{queryName}/delete").with(MonitoringController.class, "deleteQuery");
		
		return router;
	}
	
	private static void setMonitoredServer(Set<Server> servers, MonitoringServer monitoringServer) {
		monitoringServer.monitoredServers = Maps.newHashMap();
		for (Server server: servers) {
			logger.info("Monitor {}", server.getServerName());
			
			EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
			
			server.setSupportMonitoring(true);
			server.setNowMonitoring(true);
			server.setMonitoringResult(new MonitoringResultSet());
			server.setEpService(epService);
			
			addEventTypesFromRouter(server);
			createEPLAndAddListener(server);
			
			monitoringServer.monitoredServers.put(server.getServerName(), server);
		}
	}
	
	private static void addEventTypesFromRouter(Server server) {
		EPServiceProvider epService = server.getEpService();
		
		Map<String, Object> accessDef = Maps.newHashMap();
		accessDef.put("urlPath", String.class);
		accessDef.put("httpMethod", String.class);
		accessDef.put("requestHeaders", Map.class);
		accessDef.put("httpStatusCode", int.class);
		accessDef.put("time", long.class);
		accessDef.put("responseTime", long.class);
		epService.getEPAdministrator().getConfiguration().addEventType(ACCESS_EVENT_TYPE_NAME, accessDef);
		logger.info("addEventType. eventTypeName : {}, def : {}", ACCESS_EVENT_TYPE_NAME, accessDef);
		
		for (Route route: server.getRouter().getRoutes()) {
			Map<String, Object> def = Maps.newHashMap();
			Method method = route.getControllerMethod();
			final Class<?>[] paramTypes = method.getParameterTypes();
			final Annotation[][] paramAnnotations = method.getParameterAnnotations();
			int index = 0;
			
			for (int i = 0; i < paramTypes.length; i++) {
				Annotation annotation = null;
				for (int j = 0; j < paramAnnotations[i].length; j++) {
					if (HttpRequestHandler.isValidParameterAnnotation(paramAnnotations[i][j])) {
						annotation = paramAnnotations[i][j];
						continue;
					}
				}
				if (annotation != null) {
					def.put("annotationArg" + index, paramTypes[i]);
					index++;
				}
			}
			
			def.putAll(accessDef);
			String eventTypeName = route.getControllerClass().getSimpleName() + "_" + method.getName();
			epService.getEPAdministrator().getConfiguration().addEventType(eventTypeName, def);
			logger.info("addEventType. eventTypeName : {}, def : {}", eventTypeName, def);
		}
	}
	
	private static void createEPLAndAddListener(Server server) {
		EPServiceProvider epService = server.getEpService();
		
		String prefix = SERVER_NAME + "." + server.getServerName() + ".query";
		Map<String, Map<String, String>> queries = Maps.newHashMap();
		List<String> keys = ApplicationProperties.getKeys(prefix);
		for (String key: keys) {
			String prefixWithDot = prefix + ".";
			if (key.startsWith(prefixWithDot)) {
				String cutKey = StringUtils.removeStart(key, prefixWithDot);
				String[] cutKeyArray = StringUtils.split(cutKey, ".");
				
				if (cutKeyArray.length == 2) {
					String queryName = cutKeyArray[0];
					String queryOption = cutKeyArray[1];
					
					Map<String, String> query = queries.get(queryName);
					if (query == null) {
						query = Maps.newHashMap();
						queries.put(queryName, query);
					}
					query.put(queryOption, ApplicationProperties.get(key));
				}
			}
		}
		for (Entry<String, Map<String, String>> e: queries.entrySet()) {
			String queryName = e.getKey();
			Map<String, String> query = e.getValue();
			
			String queryStatement = query.get("statement");
			checkArgument(StringUtils.isNotBlank(queryStatement), queryName + " statement is blank.");
			
			String queryMaxStoreCount = query.get("maxStoreCount");
			if (queryMaxStoreCount == null) {
				queryMaxStoreCount = DEFAULT_STORE_COUNT;
			}
			checkArgument(StringUtils.isNumeric(queryMaxStoreCount), queryName + " maxStoreCount is not numeric.");
			int queryMaxStoreCountInt = Integer.parseInt(queryMaxStoreCount);
			checkArgument(queryMaxStoreCountInt >= 0, queryName + " maxStoreCount is not numeric.");
			
			EPStatement statement = epService.getEPAdministrator().createEPL(queryStatement, queryName);
			statement.addListener(new MonitoringListener(server, queryName, Integer.parseInt(queryMaxStoreCount)));
			logger.info("add query. queryName : {}, statement : {}, maxStoreCount: {}", queryName, queryStatement, queryMaxStoreCount);
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
	
	public Set<String> getMonitoredServerNames() {
		return monitoredServers.keySet();
	}
	
	public List<Server> getMonitoredServersList() {
		List<Server> result = Lists.newArrayList();
		result.addAll(monitoredServers.values());
		return result;
	}
	
	public Server getMonitoredServer(String monitoredServerName) {
		return monitoredServers.get(monitoredServerName);
	}
	
	// getter
	public Map<String, Server> getMonitoredServers() {
		return monitoredServers;
	}
}