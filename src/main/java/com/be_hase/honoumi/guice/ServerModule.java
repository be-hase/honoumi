package com.be_hase.honoumi.guice;

import org.jboss.netty.channel.ChannelPipelineFactory;

import com.be_hase.honoumi.config.ApplicationProperties;
import com.be_hase.honoumi.netty.pipeline.ServerChannelPipelineFactory;
import com.be_hase.honoumi.netty.server.AbstractServer;
import com.be_hase.honoumi.netty.server.IServer;
import com.be_hase.honoumi.netty.server.Server;
import com.be_hase.honoumi.routing.Route;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class ServerModule extends AbstractModule {
	private final Server server;
	
	public ServerModule(Server server) {
		this.server = server;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), ApplicationProperties.getAllCurrentProperties());
		
		bind(IServer.class).toInstance(server);
		bind(AbstractServer.class).toInstance(server);
		bind(Server.class).toInstance(server);
		
		bind(ChannelPipelineFactory.class).to(ServerChannelPipelineFactory.class);
		
		for (Route route: server.getRouter().getRoutes()) {
			bind(route.getControllerClass()).in(Singleton.class);
		}
	}
}
