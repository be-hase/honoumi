package com.be_hase.honoumi.guice;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.routing.Route;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class CoreModule extends AbstractModule {
	private final Server server;
	
	public CoreModule(Server server) {
		this.server = server;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), ApplicationProperties.getAllCurrentProperties());
		
		bind(Server.class).toInstance(server);
		
		for (Route route: server.getRouter().getRoutes()) {
			bind(route.getControllerClass()).in(Singleton.class);
		}
	}
}
