package com.be_hase.honoumi.routing;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteBuilder {
	private static final Logger log = LoggerFactory
			.getLogger(RouteBuilder.class);

	private String httpMethod;
	private String uri;
	private Class<?> controller;
	private Method controllerMethod;

	public RouteBuilder GET() {
		httpMethod = "GET";
		return this;
	}

	public RouteBuilder POST() {
		httpMethod = "POST";
		return this;
	}

	public RouteBuilder PUT() {
		httpMethod = "PUT";
		return this;
	}

	public RouteBuilder DELETE() {
		httpMethod = "DELETE";
		return this;
	}

	public RouteBuilder route(String uri) {
		this.uri = uri;
		return this;
	}

	public void with(Class<?> controller, String controllerMethod) {
		this.controller = controller;
		this.controllerMethod = verifyThatControllerAndMethodExists(controller,
				controllerMethod);
	}

	private Method verifyThatControllerAndMethodExists(Class<?> controller,
			String controllerMethod) {
		try {
			Method methodFromQueryingClass = null;

			for (Method method : controller.getMethods()) {
				if (method.getName().equals(controllerMethod)) {
					if (methodFromQueryingClass == null) {
						methodFromQueryingClass = method;
					} else {
						throw new NoSuchMethodException("Not allow more than one method with the same name!");
					}
				}
			}

			if (methodFromQueryingClass == null) {
				throw new NoSuchMethodException("Can not find Controller " + controller.getName() + " and method " + controllerMethod);
			}

			if (methodFromQueryingClass.getReturnType() == void.class) {
				return methodFromQueryingClass;
			} else {
				throw new NoSuchMethodException("Make sure the controller returns void");
			}
		} catch (SecurityException e) {
			log.error("Error while checking for valid Controller / controllerMethod combination", e);
		} catch (NoSuchMethodException e) {
			log.error("Error in route configuration!!!");
			log.error("Reason : " + e.getMessage());
		}
		
		return null;
	}

	public Route buildRoute() {
		if (httpMethod == null || uri == null ||  controller == null || controllerMethod == null) {
			log.error("Error in build route.");
			throw new IllegalStateException("Error in build route.");
		}
	
		return new Route(httpMethod, uri, controller, controllerMethod);
	}
}
