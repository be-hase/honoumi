package com.be_hase.honoumi.routing;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Route {
	//private static final Logger logger = LoggerFactory.getLogger(Route.class);
	private static final Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern
			.compile("\\{(.*?)(:\\s(.*))?\\}");
	private static final String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";

	private final String httpMethod;
	private final String uri;
	private final Class<?> controllerClass;
	private final Method controllerMethod;

	private final List<String> parameterNames;
	private final Pattern regex;

	public Route(String httpMethod, String uri, Class<?> controllerClass,
			Method controllerMethod) {
		this.httpMethod = httpMethod;
		this.uri = uri;
		this.controllerClass = controllerClass;
		this.controllerMethod = controllerMethod;

		parameterNames = ImmutableList.copyOf(doParseParameters(uri));
		regex = Pattern.compile(convertRawUriToRegex(uri));
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public Method getControllerMethod() {
		return controllerMethod;
	}

	public boolean matches(String httpMethod, String uri) {
		//uri = uri.replaceFirst("\\?.*$", "");
		uri = uri.split("\\?")[0];
		if (this.httpMethod.equalsIgnoreCase(httpMethod)) {
			Matcher matcher = regex.matcher(uri);
			return matcher.matches();
		} else {
			return false;
		}
	}

	public Map<String, String> getPathParametersEncoded(String uri) {
		uri = uri.split("\\?")[0];
		Map<String, String> map = Maps.newHashMap();
		Matcher m = regex.matcher(uri);

		if (m.matches()) {
			for (int i = 1; i < m.groupCount() + 1; i++) {
				String value = m.group(i);
				map.put(parameterNames.get(i - 1), value);
			}
		}

		return map;
	}

	public Map<String, String> getPathParametersDecoded(String uri) {
		uri = uri.split("\\?")[0];
		Map<String, String> map = Maps.newHashMap();
		Matcher m = regex.matcher(uri);

		if (m.matches()) {
			for (int i = 1; i < m.groupCount() + 1; i++) {
				try {
					String value = m.group(i);
					map.put(parameterNames.get(i - 1), URLDecoder.decode(value,"utf-8"));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

		return map;
	}

	private static List<String> doParseParameters(String rawRoute) {
		List<String> list = new ArrayList<String>();
		Matcher m = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawRoute);

		while (m.find()) {
			list.add(m.group(1));
		}

		return list;
	}

	protected static String convertRawUriToRegex(String rawUri) {
		Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawUri);
		StringBuffer stringBuffer = new StringBuffer();

		while (matcher.find()) {
			String namedVariablePartOfRoute = matcher.group(3);
			String namedVariablePartOfORouteReplacedWithRegex;

			if (namedVariablePartOfRoute != null) {
				namedVariablePartOfORouteReplacedWithRegex = "("
						+ namedVariablePartOfRoute + ")";
			} else {
				namedVariablePartOfORouteReplacedWithRegex = VARIABLE_ROUTES_DEFAULT_REGEX;
			}

			matcher.appendReplacement(stringBuffer,
					namedVariablePartOfORouteReplacedWithRegex);
		}

		matcher.appendTail(stringBuffer);

		return stringBuffer.toString();
	}
}
