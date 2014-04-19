package com.be_hase.honoumi.routing;

import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.be_hase.honoumi.controller.TestController;

public class RouteTest extends TestCase {
	//private static final Logger logger = LoggerFactory.getLogger(RouteTest.class);

	@Before
	public void setUp() {
		System.setProperty("application.environment", "local");
		System.setProperty("application.properties", "test1.properties,test2.properties,server.properties");
	}
	
	@Test
	public void test_matches() {
		RouteBuilder builder1 = new RouteBuilder();
		builder1.GET().route("/hoge").with(TestController.class, "hoge");
		Route route1 = builder1.buildRoute();
		
		assertTrue(route1.matches("get", "/hoge"));
		assertFalse(route1.matches("get", "/bar"));
		assertFalse(route1.matches("post", "/hoge"));
		
		RouteBuilder builder2 = new RouteBuilder();
		builder2.GET().route("/hoge/{bar}").with(TestController.class, "hoge");
		Route route2 = builder2.buildRoute();
		
		assertTrue(route2.matches("get", "/hoge/hoge"));
		assertFalse(route2.matches("get", "/hoge"));
		assertFalse(route2.matches("post", "/hoge/hoge"));
		
		RouteBuilder builder3 = new RouteBuilder();
		builder3.GET().route("/hoge/hoge/.*").with(TestController.class, "hoge");
		Route route3 = builder3.buildRoute();
		
		assertTrue(route3.matches("get", "/hoge/hoge/vbuaoevbe"));
		assertFalse(route3.matches("get", "/bar"));
		assertFalse(route3.matches("post", "/hoge/hoge/vbuaoevbe"));
	}
	
	@Test
	public void test_getPathParametersEncoded() {
		RouteBuilder builder1 = new RouteBuilder();
		builder1.GET().route("/hoge/{bar}").with(TestController.class, "hoge");
		Route route1 = builder1.buildRoute();
		
		Map<String, String> params1 = route1.getPathParametersEncoded("/hoge");
		Map<String, String> params2 = route1.getPathParametersEncoded("/hoge/hasebe");
		Map<String, String> params3 = route1.getPathParametersEncoded("/fuga/hasebe");
		
		assertTrue(params1.size() == 0);
		assertTrue(params2.size() == 1);
		assertTrue(params2.get("bar").equals("hasebe"));
		assertTrue(params3.size() == 0);
		
		RouteBuilder builder2 = new RouteBuilder();
		builder2.GET().route("/{appId}/{userId}/{service}").with(TestController.class, "hoge");
		Route route2 = builder2.buildRoute();
		Map<String, String> params4 = route2.getPathParametersEncoded("/app/1234/login?hoge=hoge");
		assertTrue(params4.get("appId").equals("app"));
		assertTrue(params4.get("userId").equals("1234"));
		assertTrue(params4.get("service").equals("login"));
	}
}
