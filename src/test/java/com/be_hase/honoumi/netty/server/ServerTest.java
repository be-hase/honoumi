package com.be_hase.honoumi.netty.server;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.be_hase.honoumi.HttpUtilForTest;
import com.be_hase.honoumi.controller.TestController;
import com.be_hase.honoumi.guice.TestModule;
import com.be_hase.honoumi.routing.Router;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;

public class ServerTest extends TestCase {
	@Before
	public void setUp() {
		System.setProperty("application.environment", "local");
		System.setProperty("application.properties", "test1.properties,test2.properties,server.properties");
	}
	
	@Test
	public void test_create() {
		Router router = new Router();
		router.GET().route("/hoge").with(TestController.class, "hoge");
		router.GET().route("/bar/{path1}/{path2}").with(TestController.class, "bar");
		router.GET().route("/fuga/{path1}/{path2}").with(TestController.class, "fuga");
		List<AbstractModule> modules = Lists.newArrayList();
		modules.add(new TestModule());
		Server testServer = Server.create("testServer", router, modules);
		testServer.start();
		
		MonitoringServer monitoringServer = MonitoringServer.create(testServer);
		monitoringServer.start();
		
		assertEquals("testServer", testServer.getServerName());
		assertEquals(22222, testServer.getPort());
		assertEquals("UTF-8", testServer.getCharsetStr());
		assertEquals("true", testServer.getServerBootstrap().getOption("reuseAddress").toString());
		assertEquals("true", testServer.getServerBootstrap().getOption("child.keepAlive").toString());
		assertEquals("true", testServer.getServerBootstrap().getOption("child.tcpNoDelay").toString());
		
		assertEquals("hoge", HttpUtilForTest.get("http://localhost:22222/hoge", null).getResponse());
		assertEquals("12", HttpUtilForTest.get("http://localhost:22222/bar/1/2", null).getResponse());
		assertEquals("{\"path2\":\"2\",\"path1\":\"1\"}", HttpUtilForTest.get("http://localhost:22222/fuga/1/2", null).getResponse());
	}
}
