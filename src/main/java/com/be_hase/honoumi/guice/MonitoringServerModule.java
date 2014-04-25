package com.be_hase.honoumi.guice;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.netty.server.AbstractServer;
import com.be_hase.honoumi.netty.server.IServer;
import com.be_hase.honoumi.netty.server.MonitoringServer;
import com.be_hase.honoumi.routing.Route;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class MonitoringServerModule extends AbstractModule {
	private final MonitoringServer monitoringServer;
	
	public MonitoringServerModule(MonitoringServer monitoringServer) {
		this.monitoringServer = monitoringServer;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), ApplicationProperties.getAllCurrentProperties());
		
		bind(IServer.class).toInstance(monitoringServer);
		bind(AbstractServer.class).toInstance(monitoringServer);
		bind(MonitoringServer.class).toInstance(monitoringServer);
		
		for (Route route: monitoringServer.getRouter().getRoutes()) {
			bind(route.getControllerClass()).in(Singleton.class);
		}
	}

}
