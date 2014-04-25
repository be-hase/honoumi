package com.be_hase.honoumi.controller;

import org.jboss.netty.channel.MessageEvent;

import com.be_hase.honoumi.netty.server.MonitoringServer;
import com.google.inject.Inject;

public class MonitoringController {
	@Inject
	private MonitoringServer monitoringServer;
	
	public void statuses(
			MessageEvent evt
			) {
	}
	
	public void editStatuses(
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
	
	public void queries(
			MessageEvent evt
			) {
	}
	
	public void deleteQueries(
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
