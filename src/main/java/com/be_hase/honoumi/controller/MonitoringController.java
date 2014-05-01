package com.be_hase.honoumi.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.be_hase.honoumi.controller.argument.FormParam;
import com.be_hase.honoumi.controller.argument.PathParam;
import com.be_hase.honoumi.controller.argument.QueryParam;
import com.be_hase.honoumi.listener.MonitoringListener;
import com.be_hase.honoumi.netty.server.MonitoringServer;
import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.util.JacksonUtils;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

//TODO: separate the Service class properly.

public class MonitoringController {
	//private static Logger logger = LoggerFactory.getLogger(MonitoringController.class);
	
	@Inject
	private MonitoringServer monitoringServer;
	
	private static final String NOT_EXIST_SERVER_NAME = "This server-name does not exist.";
	private static final String NOT_EXIST_QUERY_NAME = "This query-name does not exist.";
	
	private static final String STATUS_START = "start";
	private static final String STATUS_STOP = "stop";
	
	private static final Map<String, String> RESPONSE_HEADERS = Maps.newHashMap();
	static {
		RESPONSE_HEADERS.put("Access-Control-Allow-Origin", "*");
	}
	
	public void statusesAllServer(
			MessageEvent evt
			) {
		responseStatusesAllServer(evt);
	}
	
	public void editStatusesAllServer(
			MessageEvent evt,
			@FormParam("isNowMonitoring") String isNowMonitoring
			) {
		boolean setNowMonitoring;
		if ("true".equals(isNowMonitoring)) {
			setNowMonitoring = true;
		} else if ("false".equals(isNowMonitoring)) {
			setNowMonitoring = false;
		} else  {
			error(evt, "isNowMonitoring must be true or false");
			return;
		}
		
		List<Server> servers = monitoringServer.getMonitoredServersList();
		for (Server server: servers) {
			server.setNowMonitoring(setNowMonitoring);
		}

		responseStatusesAllServer(evt);
	}
	
	public void status(
			MessageEvent evt,
			@PathParam("serverName") String serverName
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		
		responseStatus(evt, server);
	}
	
	
	public void editStatus(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@FormParam("isNowMonitoring") String isNowMonitoring
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		boolean setNowMonitoring;
		if ("true".equals(isNowMonitoring)) {
			setNowMonitoring = true;
		} else if ("false".equals(isNowMonitoring)) {
			setNowMonitoring = false;
		} else  {
			error(evt, "isNowMonitoring must be true or false");
			return;
		}
		server.setNowMonitoring(setNowMonitoring);
		
		responseStatus(evt, server);
	}
	
	public void queriesAllServer(
			MessageEvent evt,
			@QueryParam("limit") String limitStr
			) {
		int limit = checkAndConvertLimit(evt, limitStr);
		if (limit == -1) {
			return;
		}
		
		responseQueriesAllServer(evt, limit);
	}

	public void editQueriesAllServer(
			MessageEvent evt,
			@FormParam("status") String status
			) {
		if (status == null) {
			error(evt, "status is empty.");
			return;
		}
		if (STATUS_START.equals(status) || STATUS_STOP.equals(status)) {
		} else {
			error(evt, String.format("status must be %s or %s.", STATUS_START, STATUS_STOP));
			return;
		}
		
		List<Server> servers = monitoringServer.getMonitoredServersList();
		for (Server server: servers) {
			try {
				if (STATUS_STOP.equals(status)) {
					server.getEpService().getEPAdministrator().stopAllStatements();;
				} else if (STATUS_START.equals(status)) {
					server.getEpService().getEPAdministrator().startAllStatements();;
				}
			} catch (Exception e) {
			}
		}
		
		responseQueriesAllServer(evt, -1);
	}
	
	public void deleteQueriesAllServer(
			MessageEvent evt
			) {
		List<Server> servers = monitoringServer.getMonitoredServersList();
		for (Server server: servers) {
			server.getEpService().getEPAdministrator().destroyAllStatements();
		}
		
		successOK(evt);
	}
	
	public void queries(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@QueryParam("limit") String limitStr
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		int limit = checkAndConvertLimit(evt, limitStr);
		if (limit == -1) {
			return;
		}
		
		responseQueries(evt, server, limit);
	}
	
	public void editQueries(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@FormParam("status") String status
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		if (status == null) {
			error(evt, "status is empty.");
			return;
		}
		if (STATUS_START.equals(status) || STATUS_STOP.equals(status)) {
		} else {
			error(evt, String.format("status must be %s or %s.", STATUS_START, STATUS_STOP));
			return;
		}
		
		try {
			if (STATUS_STOP.equals(status)) {
				server.getEpService().getEPAdministrator().stopAllStatements();;
			} else if (STATUS_START.equals(status)) {
				server.getEpService().getEPAdministrator().startAllStatements();;
			}
		} catch (Exception e) {
		}
		
		responseQueries(evt, server, -1);
	}
	
	public void deleteQueries(
			MessageEvent evt,
			@PathParam("serverName") String serverName
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		server.getEpService().getEPAdministrator().destroyAllStatements();
		
		successOK(evt);
	}
	
	public void query(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@PathParam("queryName") String queryName,
			@QueryParam("limit") String limitStr
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		int limit = checkAndConvertLimit(evt, limitStr);
		if (limit == -1) {
			return;
		}
		
		responseQuery(evt, server, queryName, limit);
	}

	
	public void addQuery(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@PathParam("queryName") String queryName,
			@FormParam("query") String query,
			@FormParam("maxStoreCount") String maxStoreCountStr,
			@FormParam("status") String status
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		EPStatement epStatement = server.getEpService().getEPAdministrator().getStatement(queryName);
		if (epStatement != null) {
			error(evt, "This query-name already exists.");
			return;
		}
		
		if (status != null) {
			if (STATUS_START.equals(status) || STATUS_STOP.equals(status)) {
			} else {
				error(evt, String.format("status must be %s or %s.", STATUS_START, STATUS_STOP));
				return;
			}
		}
		
		if (StringUtils.isBlank(query)) {
			error(evt, "query is empty.");
			return;
		}
		
		int maxStoreCount = 500;
		if (maxStoreCountStr != null) {
			if (!StringUtils.isNumeric(maxStoreCountStr)) {
				error(evt, "maxStoreCount be numeric.");
				return;
			}
			maxStoreCount = Integer.valueOf(maxStoreCountStr);
			if (maxStoreCount <= 0) {
				error(evt, "maxStoreCount must be greater then 0.");
				return;
			}
		}
		
		try {
			epStatement = server.getEpService().getEPAdministrator().createEPL(query, queryName);
			if (status != null) {
				try {
					if (STATUS_STOP.equals(status)) {
						epStatement.stop();
					}
				} catch (Exception e) {
				}
			}
			epStatement.addListener(new MonitoringListener(server, queryName, maxStoreCount));
		} catch (EPException e) {
			error(evt, e.getMessage());
			return;
		}
		
		responseQuery(evt, server, queryName, -1);
	}

	public void editQuery(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@PathParam("queryName") String queryName,
			@FormParam("status") String status
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		if (status == null) {
			error(evt, "status is empty.");
			return;
		}
		if (STATUS_START.equals(status) || STATUS_STOP.equals(status)) {
		} else {
			error(evt, String.format("status must be %s or %s.", STATUS_START, STATUS_STOP));
			return;
		}
		
		EPStatement epStatement = server.getEpService().getEPAdministrator().getStatement(queryName);
		if (epStatement == null) {
			error(evt, NOT_EXIST_QUERY_NAME);
		}
		
		try {
			if (STATUS_STOP.equals(status)) {
				epStatement.stop();
			} else if (STATUS_START.equals(status)) {
				epStatement.start();
			}
		} catch (Exception e) {
		}
		
		responseQuery(evt, server, queryName, -1);
	}
	
	public void deleteQuery(
			MessageEvent evt,
			@PathParam("serverName") String serverName,
			@PathParam("queryName") String queryName
			) {
		Server server = monitoringServer.getMonitoredServer(serverName);
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		EPStatement epStatement = server.getEpService().getEPAdministrator().getStatement(queryName);
		if (epStatement == null) {
			error(evt, NOT_EXIST_QUERY_NAME);
			return;
		}
		
		epStatement.destroy();
		
		successOK(evt);
	}
	
	private void error(MessageEvent evt, String message) {
		ObjectNode response = JacksonUtils.createObjectNode();
		response.put("error", message);
		Response.execute(evt, HttpResponseStatus.BAD_REQUEST, RESPONSE_HEADERS, JacksonUtils.toJsonString(response));
	}
	
	private void success(MessageEvent evt, Object response) {
		Response.execute(evt, HttpResponseStatus.OK, RESPONSE_HEADERS, JacksonUtils.toJsonString(response));
	}
	
	private void successOK(MessageEvent evt) {
		ObjectNode response = JacksonUtils.createObjectNode();
		response.put("result", "OK");
		Response.execute(evt, HttpResponseStatus.OK, RESPONSE_HEADERS, JacksonUtils.toJsonString(response));
	}
	
	private void responseStatusesAllServer(MessageEvent evt) {
		List<Map<String, Object>> response = Lists.newArrayList();
		
		List<Server> servers = monitoringServer.getMonitoredServersList();
		for (Server server: servers) {
			Map<String, Object> serverMap = Maps.newLinkedHashMap();

			serverMap.put("isNowMonitoring", server.isNowMonitoring());
			serverMap.put("eventTypes", getEventTypeMapList(server));
			serverMap.put("queries", getQueryMapList(server, -1));
			response.add(serverMap);
		}
		
		success(evt, response);
	}
	
	private void responseStatus(MessageEvent evt, Server server) {
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		Map<String, Object> response = Maps.newLinkedHashMap();
		
		response.put("isNowMonitoring", server.isNowMonitoring());
		response.put("eventTypes", getEventTypeMapList(server));
		response.put("queries", getQueryMapList(server, -1));
		success(evt, response);
	}
	
	private void responseQueriesAllServer(MessageEvent evt, int limit) {
		Map<String, Object> response = Maps.newHashMap();
		
		List<Server> servers = monitoringServer.getMonitoredServersList();
		for (Server server: servers) {
			response.put(server.getServerName(), getQueryMapList(server, limit));
		}
		
		success(evt, response);
	}
	
	private void responseQueries(MessageEvent evt, Server server, int limit) {
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		success(evt, getQueryMapList(server, limit));
	}
	
	private void responseQuery(MessageEvent evt, Server server, String queryName, int limit) {
		if (server == null) {
			error(evt, NOT_EXIST_SERVER_NAME);
			return;
		}
		
		EPStatement epStatement = server.getEpService().getEPAdministrator().getStatement(queryName);
		if (epStatement == null) {
			error(evt, NOT_EXIST_QUERY_NAME);
			return;
		}
		
		success(evt, getQueryMap(server, epStatement, limit));
	}
	
	
	private List<Map<String, Object>> getEventTypeMapList(Server server) {
		List<Map<String, Object>> eventTypesMap = Lists.newArrayList();
		EventType[] eventTypes = server.getEpService().getEPAdministrator().getConfiguration().getEventTypes();
		
		for (EventType eventType: eventTypes) {
			Map<String, Object> eventTypeMap = Maps.newLinkedHashMap();
			
			eventTypeMap.put("name", eventType.getName());
			eventTypeMap.put("properties", getEventPropertyMapList(eventType));
			eventTypesMap.add(eventTypeMap);
		}
		
		return eventTypesMap;
	}
	
	private List<Map<String, Object>> getEventPropertyMapList(EventType eventType) {
		List<Map<String, Object>> eventPropertiesMap = Lists.newArrayList();
		EventPropertyDescriptor[] descriptors = eventType.getPropertyDescriptors();
		
		for (EventPropertyDescriptor descriptor: descriptors) {
			Map<String, Object> eventPropertyMap = Maps.newLinkedHashMap();
			
			eventPropertyMap.put("name", descriptor.getPropertyName());
			eventPropertyMap.put("type", descriptor.getPropertyType());
			eventPropertiesMap.add(eventPropertyMap);
		}
		
		return eventPropertiesMap;
	}
	
	private List<Map<String, Object>> getQueryMapList(Server server, int limit) {
		List<Map<String, Object>> queriesMap = Lists.newArrayList();
		String[] statementNames = server.getEpService().getEPAdministrator().getStatementNames();
		
		for (String statementName: statementNames) {
			EPStatement statement = server.getEpService().getEPAdministrator().getStatement(statementName);
			queriesMap.add(getQueryMap(server, statement, limit));
		}
		
		return queriesMap;
	}
	
	private Map<String, Object> getQueryMap(Server server, EPStatement statement, int limit) {
		Map<String, Object> queryMap = Maps.newLinkedHashMap();
		
		queryMap.put("name", statement.getName());
		queryMap.put("text", statement.getText());
		queryMap.put("status", StringUtils.lowerCase(statement.getState().toString()));
		queryMap.put("storeCount", server.getMonitoringResultSet().getStoreCount(statement.getName()));
		queryMap.put("maxStoreCount", server.getMonitoringResultSet().getMaxStoreCount(statement.getName()));
		if (limit > 0) {
			queryMap.put("results", server.getMonitoringResultSet().getByQueryName(statement.getName(), limit));
		} else if (limit == 0) {
			queryMap.put("results", server.getMonitoringResultSet().getByQueryName(statement.getName()));
		}
		
		return queryMap;
	}
	
	private int checkAndConvertLimit(MessageEvent evt, String limitStr) {
		int limit;
		if (limitStr == null || "all".equals(limitStr)) {
			limit = 0;
		} else {
			if (!StringUtils.isNumeric(limitStr)) {
				error(evt, "limit must be numeric.");
				return -1;
			}
			limit = Integer.valueOf(limitStr);
			if (limit <= 0) {
				error(evt, "limit must be greater then 0.");
				return -1;
			}
		}
		return limit;
	}
}
