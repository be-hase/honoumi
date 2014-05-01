package com.be_hase.honoumi.listener;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.util.JacksonUtils;
import com.be_hase.honoumi.util.Utils;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MonitoringListener implements UpdateListener {
	private static Logger logger = LoggerFactory.getLogger(MonitoringListener.class);
	
	private Server server;
	private String queryName;
	private int maxStoreCount;
	
	public MonitoringListener(Server server, String queryName, int maxStoreCount) {
		this.server = server;
		this.queryName = queryName;
		this.maxStoreCount = maxStoreCount;
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		logger.debug("called");
		
		if (newEvents == null || newEvents.length == 0) {
			return;
		}
		
		DateTime dt = new DateTime();
		for (EventBean event : newEvents) {
			try {
				Object underlying =  event.getUnderlying();
				if (underlying == null) {
					continue;
				}
				
				ObjectNode data = JacksonUtils.createObjectNode(underlying);
				data.put("_time", dt.toString());
				data.put("_id", UUID.randomUUID().toString());
				
				server.getMonitoringResultSet().add(queryName, maxStoreCount, data);
			} catch (Exception e) {
				logger.debug("Failed to add event result : {}", Utils.stackTraceToStr(e));
			}
		}
	}
}
