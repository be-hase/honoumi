package com.be_hase.honoumi.routing;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.be_hase.honoumi.controller.InvalidTest1Controller;
import com.be_hase.honoumi.controller.TestController;

public class RouteBuilderTest extends TestCase {
	@Before
	public void setUp() {
		System.setProperty("application.environment", "local");
		System.setProperty("application.properties", "test1.properties,test2.properties,server.properties");
	}
	
	@Test
	public void test_buildRoute() {
		try {
			RouteBuilder builder = new RouteBuilder();
			builder.GET().route("/hoge").with(TestController.class, "hoge");
			builder.buildRoute();
			assertTrue(true);
		} catch (IllegalStateException e) {
			assertTrue(false);
		}
		
		try {
			RouteBuilder builder = new RouteBuilder();
			builder.GET().route("/hoge").with(TestController.class, "notExist");
			builder.buildRoute();
			assertTrue(false);
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
		
		try {
			RouteBuilder builder = new RouteBuilder();
			builder.route("/hoge").with(TestController.class, "hoge");
			builder.buildRoute();
			assertTrue(false);
		} catch (IllegalStateException e) {
			assertTrue(true);
		}

		try {
			RouteBuilder builder = new RouteBuilder();
			builder.GET().with(TestController.class, "hoge");
			builder.buildRoute();
			assertTrue(false);
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
		
		try {
			RouteBuilder builder = new RouteBuilder();
			builder.GET().route("/hoge").with(InvalidTest1Controller.class, "hoge");
			builder.buildRoute();
			assertTrue(false);
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
		
		try {
			RouteBuilder builder = new RouteBuilder();
			builder.GET().route("/bar").with(InvalidTest1Controller.class, "bar");
			builder.buildRoute();
			assertTrue(false);
		} catch (IllegalStateException e) {
			assertTrue(true);
		}
	}
}
