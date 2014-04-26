package com.be_hase.honoumi.controller;

import java.util.Map;

import org.jboss.netty.channel.MessageEvent;

import com.be_hase.honoumi.netty.server.MonitoringServer;
import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.util.JacksonUtils;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.util.EventTypePropertyPair;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

public class MonitoringController {
	
	@Inject
	private MonitoringServer monitoringServer;
	
	public void statusesAllServer(
			MessageEvent evt
			) {
		ArrayNode response = JacksonUtils.createArrayNode();
		for (String monitoredServerName: monitoringServer.getMonitoredServerNames()) {
			ObjectNode obj = JacksonUtils.createObjectNode();
			Server monitoredServer = monitoringServer.getMonitoredServer(monitoredServerName);
			EventType[] eventTypes = monitoredServer.getEpService().getEPAdministrator().getConfiguration().getEventTypes();
		}
	}
	
	public void editStatusesAllServer(
			MessageEvent evt
			) {
	}
	
	public void status(
			MessageEvent evt
			) {
	}
	
	public void editStatus(
			MessageEvent evt
			) {
	}
	
	public void queriesAllServer(
			MessageEvent evt
			) {
	}
	
	public void deleteQueriesAllServer(
			MessageEvent evt
			) {
	}
	
	public void queries(
			MessageEvent evt
			) {
	}
	
	public void deleteQueries(
			MessageEvent evt
			) {
	}
	
	public void queryAllServer(
			MessageEvent evt
			) {
	}
	
	public void deleteQueryAllServer(
			MessageEvent evt
			) {
	}

	public void query(
			MessageEvent evt
			) {
	}

	
	public void saveQuery(
			MessageEvent evt
			) {
	}

	public void deleteQuery(
			MessageEvent evt
			) {
	}
}
