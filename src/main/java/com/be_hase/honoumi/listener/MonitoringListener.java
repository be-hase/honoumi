package com.be_hase.honoumi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.netty.server.Server;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class MonitoringListener implements UpdateListener {
	private static Logger logger = LoggerFactory.getLogger(MonitoringListener.class);
	
	private Server server;
	private String queryName;
	private int storeCount;
	
	public MonitoringListener(Server server, String queryName, int storeCount) {
		this.server = server;
		this.queryName = queryName;
		this.storeCount = storeCount;
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		logger.debug("called");
		
		if (newEvents == null) {
			return;
		}
		for (EventBean event : newEvents) {
			Object underlying =  event.getUnderlying();
			server.getMonitoringResult().add(queryName, storeCount, underlying);
		}
	}
}
