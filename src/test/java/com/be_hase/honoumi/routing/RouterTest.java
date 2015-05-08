package com.be_hase.honoumi.routing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.be_hase.honoumi.controller.TestController;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

public class RouterTest extends TestCase {
	//private static final Logger logger = LoggerFactory.getLogger(RouterTest.class);

	@Before
	public void setUp() {
		System.setProperty("application.environment", "local");
		System.setProperty("application.properties", "test1.properties,test2.properties,server.properties");
	}

	@Test
	public void test_getRouteFor() {
		Router router = new Router();
		router.GET().route("/etc/privacy").with(TestController.class, "hoge");
		router.POST().route("/blog/{blogId}/edit").with(TestController.class, "bar");
		router.GET().route("/events/.*").with(TestController.class, "fuga");
		router.compileRoutes();

		Route route1 = router.getRouteFor("GET", "/etc/privacy?hoge=hoge&bar=bar");
		Route route2 = router.getRouteFor("POST", "/blog/1234/edit");
		Route route3 = router.getRouteFor("GET", "/events/sdvnwbgp/vsjgowe/dsjv");

		assertTrue(route1.getControllerClass().getSimpleName().equals("TestController"));
		assertTrue(route1.getControllerMethod().getName().equals("hoge"));
		assertTrue(route2.getControllerClass().getSimpleName().equals("TestController"));
		assertTrue(route2.getControllerMethod().getName().equals("bar"));
		assertTrue(route3.getControllerClass().getSimpleName().equals("TestController"));
		assertTrue(route3.getControllerMethod().getName().equals("fuga"));
	}

	@Test
	public void test_getReverseRoute() {
		Router router = new Router();
		router.GET().route("/etc/privacy").with(TestController.class, "hoge");
		router.POST().route("/blog/{blogId}/edit").with(TestController.class, "bar");
		router.GET().route("/events/.*").with(TestController.class, "fuga");
		router.compileRoutes();

		Map<String, Object> map = Maps.newHashMap();
		map.put("blogId", "1234");
		map.put("huga", "hughuga");

		String uri1 = router.getReverseRoute(TestController.class, "hoge");
		String uri2 = router.getReverseRoute(TestController.class, "hoge", map);
		String uri3 = router.getReverseRoute(TestController.class, "bar");
		String uri4 = router.getReverseRoute(TestController.class, "bar", map);
		String uri5 = router.getReverseRoute(TestController.class, "fuga");
		String uri6 = router.getReverseRoute(TestController.class, "fuga", map);

		assertThat(uri1, is("/etc/privacy"));
		assertThat(uri2, is("/etc/privacy?huga=hughuga&blogId=1234"));

		assertThat(uri3, is("/blog/{blogId}/edit"));
		assertThat(uri4, is("/blog/1234/edit?huga=hughuga"));

		assertThat(uri5, is("/events/.*"));
		assertThat(uri6, is("/events/.*?huga=hughuga&blogId=1234"));
	}
}
