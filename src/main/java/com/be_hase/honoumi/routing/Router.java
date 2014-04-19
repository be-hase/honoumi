package com.be_hase.honoumi.routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Router {
	private static final Logger logger = LoggerFactory.getLogger(Router.class);
	private List<Route> routes;
	private final List<RouteBuilder> allRouteBuilders = new ArrayList<RouteBuilder>();

	public Route getRouteFor(String httpMethod, String uri) {
		if (routes == null) {
			throw new IllegalStateException(
					"Attempt to get route when routes not compiled");
		}

		for (Route route : routes) {
			if (route.matches(httpMethod, uri)) {
				return route;
			}
		}

		return null;
	}

	public String getReverseRoute(Class<?> controllerClass,
			String controllerMethodName) {

		Map<String, Object> map = Maps.newHashMap();
		return getReverseRoute(controllerClass, controllerMethodName, map);

	}

	public String getReverseRoute(Class<?> controllerClass,
			String controllerMethodName, Map<String, Object> parameterMap) {
		if (routes == null) {
			throw new IllegalStateException(
					"Attempt to get route when routes not compiled");
		}

		for (Route route : routes) {
			if (route.getControllerClass() != null
					&& route.getControllerClass().equals(controllerClass)
					&& route.getControllerMethod().getName()
							.equals(controllerMethodName)) {

				String urlWithReplacedPlaceholders = route.getUri();

				Map<String, Object> queryParameterMap = Maps.newHashMap();

				for (Entry<String, Object> parameterPair : parameterMap
						.entrySet()) {
					String originalRegex = String.format("{%s}",
							parameterPair.getKey());
					String originalRegexEscaped = String.format("\\{%s\\}",
							parameterPair.getKey());
					String resultingRegexReplacement = parameterPair.getValue()
							.toString();

					if (urlWithReplacedPlaceholders.contains(originalRegex)) {
						urlWithReplacedPlaceholders = urlWithReplacedPlaceholders
								.replaceAll(originalRegexEscaped,
										resultingRegexReplacement);
					} else {
						queryParameterMap.put(parameterPair.getKey(),
								parameterPair.getValue());
					}
				}

				if (queryParameterMap.entrySet().size() > 0) {
					StringBuffer queryParameterStringBuffer = new StringBuffer();
					for (Iterator<Entry<String, Object>> iterator = queryParameterMap
							.entrySet().iterator(); iterator.hasNext();) {
						Entry<String, Object> queryParameterEntry = iterator
								.next();
						queryParameterStringBuffer.append(queryParameterEntry
								.getKey());
						queryParameterStringBuffer.append("=");
						queryParameterStringBuffer.append(queryParameterEntry
								.getValue());

						if (iterator.hasNext()) {
							queryParameterStringBuffer.append("&");
						}
					}

					urlWithReplacedPlaceholders = urlWithReplacedPlaceholders
							+ "?" + queryParameterStringBuffer.toString();

				}

				return urlWithReplacedPlaceholders;
			}
		}

		return null;
	}

	public void compileRoutes() {
		if (routes != null) {
			throw new IllegalStateException("Routes already compiled");
		}
		if (allRouteBuilders.size() == 0) {
			throw new IllegalStateException("There is no Routes.");
		}
		
		List<Route> routes = new ArrayList<Route>();
		for (RouteBuilder routeBuilder : allRouteBuilders) {
			Route route = routeBuilder.buildRoute();
			logger.info("'{}' TO {}.{}", route.getUri(), route.getControllerClass().getName(), route.getControllerMethod().getName());
			routes.add(route);
		}
		this.routes = ImmutableList.copyOf(routes);
	}

	public RouteBuilder GET() {

		RouteBuilder routeBuilder = new RouteBuilder().GET();
		allRouteBuilders.add(routeBuilder);

		return routeBuilder;
	}

	public RouteBuilder POST() {
		RouteBuilder routeBuilder = new RouteBuilder().POST();
		allRouteBuilders.add(routeBuilder);

		return routeBuilder;
	}

	public RouteBuilder PUT() {
		RouteBuilder routeBuilder = new RouteBuilder().PUT();
		allRouteBuilders.add(routeBuilder);

		return routeBuilder;
	}

	public RouteBuilder DELETE() {
		RouteBuilder routeBuilder = new RouteBuilder().DELETE();
		allRouteBuilders.add(routeBuilder);

		return routeBuilder;
	}

	public List<Route> getRoutes() {
		return routes;
	}
}
