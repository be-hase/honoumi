package com.be_hase.honoumi.controller;

import org.jboss.netty.channel.MessageEvent;

import com.be_hase.honoumi.netty.server.Server;
import com.google.inject.Inject;

public class MonitoringController {
	@Inject
	private Server monitoringServer;
	
	public void status(
			MessageEvent evt
			) {
	}
}
