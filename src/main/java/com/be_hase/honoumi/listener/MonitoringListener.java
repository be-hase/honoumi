package com.be_hase.honoumi.listener;

import com.be_hase.honoumi.netty.server.Server;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class MonitoringListener implements UpdateListener {
	
	private Server server;
	private String queryName;
	private int storeCount;
	
	public MonitoringListener(Server server, String queryName, int storeCount) {
		this.server = server;
		this.queryName = queryName;
		this.storeCount = storeCount;
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents == null) {
			return;
		}
		for (EventBean event : newEvents) {
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {}
			Object underlying =  event.getUnderlying();
			server.getMonitoringResult().add(queryName, storeCount, underlying);
		}
	}
}
