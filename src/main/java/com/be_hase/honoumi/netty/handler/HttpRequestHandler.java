package com.be_hase.honoumi.netty.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.controller.Response;
import com.be_hase.honoumi.controller.argument.ArgumentResolver;
import com.be_hase.honoumi.controller.argument.Body;
import com.be_hase.honoumi.controller.argument.FormParam;
import com.be_hase.honoumi.controller.argument.FormParams;
import com.be_hase.honoumi.controller.argument.Header;
import com.be_hase.honoumi.controller.argument.Headers;
import com.be_hase.honoumi.controller.argument.PathParam;
import com.be_hase.honoumi.controller.argument.PathParams;
import com.be_hase.honoumi.controller.argument.QueryParam;
import com.be_hase.honoumi.controller.argument.QueryParams;
import com.be_hase.honoumi.controller.argument.WithArgumentResolver;
import com.be_hase.honoumi.controller.filter.Filter;
import com.be_hase.honoumi.controller.filter.WithFilter;
import com.be_hase.honoumi.domain.ChannelAttachment;
import com.be_hase.honoumi.domain.ResponseError;
import com.be_hase.honoumi.exception.AbstractErrorResponseException;
import com.be_hase.honoumi.exception.ArgumentResolveException;
import com.be_hase.honoumi.netty.server.IServer;
import com.be_hase.honoumi.routing.Route;
import com.be_hase.honoumi.util.JacksonUtils;
import com.be_hase.honoumi.util.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

	private static final String X_HTTP_METHOD_OVERRIDE = "X-Http-Method-Override";

	@Inject
	private IServer server;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
		logger.debug("HttpRequestHandler.messageReceived called.");

		if (!(evt.getMessage() instanceof HttpRequest)) {
			logger.debug("[n/a] received message is illegal.");
			DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			ctx.getChannel().write(res).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		try {
			HttpRequest request = (HttpRequest)evt.getMessage();
			String uri = request.getUri();
			String httpMethod = getHttpMethod(request);
			logger.debug("Request [{}] '{}'", httpMethod.toUpperCase(), uri);

			ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(evt.getChannel());
			channelAttachment.setUrlPath(request.getUri());
			channelAttachment.setHttpMethod(HttpRequestHandler.getHttpMethod(request));
			channelAttachment.setRequestHeaders(HttpRequestHandler.getRequestHeaders(request));

			Route route = server.getRouter().getRouteFor(httpMethod, uri);
			if (route == null) {
				Response.execute(evt, HttpResponseStatus.NOT_FOUND, null,
					JacksonUtils.toJsonString(createResponseError("Not Found", "We don't have this uri")));
				return;
			}

			Class<?> clazz = route.getControllerClass();
			Method method = route.getControllerMethod();

			List<Class<? extends Filter>> filterClasses = Lists.newArrayList();
			WithFilter classWithFilter = clazz.getAnnotation(WithFilter.class);
			if (classWithFilter != null) {
				filterClasses.addAll(Arrays.asList(classWithFilter.value()));
			}
			WithFilter methodWithFilter = method.getAnnotation(WithFilter.class);
			if (methodWithFilter != null) {
				filterClasses.addAll(Arrays.asList(methodWithFilter.value()));
			}

			logger.debug("Registed filters : {}", filterClasses);
			for (Class<? extends Filter> filterClass : filterClasses) {
				Filter filter = server.getInjector().getInstance(filterClass);

				logger.debug("Invoke filter : {}", filterClass.getSimpleName());
				boolean filterResult = filter.filter(ctx, evt);

				if (!filterResult) {
					break;
				}
			}

			List<Object> args = parseInvokingMethodArguments(ctx, evt, request, clazz, method, route.getPathParametersDecoded(uri));

			logger.debug("Invoke controller method : {}.{}", clazz.getSimpleName(), method.getName());

			method.invoke(server.getInjector().getInstance(clazz), args.toArray());
		} catch (AbstractErrorResponseException e) {
			logger.debug("[AbstractErrorResponseException] : {}", e.getResponse(), e);

			String response;
			if (e.getResponse() instanceof String) {
				response = (String)e.getResponse();
			} else {
				response = JacksonUtils.toJsonString(e.getResponse());
			}
			Response.execute(evt, e.getStatus(), e.getHeaders(), response);
		} catch (Exception e) {
			logger.error(Utils.stackTraceToStr(e));
			Response.execute(evt, HttpResponseStatus.INTERNAL_SERVER_ERROR, null,
				JacksonUtils.toJsonString(createResponseError("Internal Server Error", e.getMessage())));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Channel channel = ctx.getChannel();

		logger.error(
			"[channel exception] {} \n"
				+
				"[channel status] channelIsOpen={}, channelIsBound={}, channelIsWriteable={}, channelIsReadable={}, channelIsConnected={} \n"
				+
				"[stacktrace] {}",
			e,
			channel.isOpen(), channel.isBound(), channel.isWritable(), channel.isReadable(), channel.isConnected(),
			Utils.stackTraceToStr(e.getCause())
			);

		if (ctx.getChannel().isOpen()) {
			ctx.getChannel().close();
		}
	}

	private static ResponseError createResponseError(String error, String message) {
		return new ResponseError(error, message);
	}

	private List<Object> parseInvokingMethodArguments(ChannelHandlerContext ctx, MessageEvent evt, HttpRequest request,
			Class<?> clazz, Method method, Map<String, String> pathParams) throws ArgumentResolveException {
		List<Object> args = Lists.newArrayList();
		List<Object> annotationArgs = Lists.newArrayList();

		final Class<?>[] paramTypes = method.getParameterTypes();
		final Annotation[][] paramAnnotations = method.getParameterAnnotations();

		ChannelBuffer content = request.getContent();
		String bodyStr = content.toString(server.getCharset());
		logger.debug("body : {}", bodyStr);

		final Map<String, String> headers = getRequestHeaders(request);
		logger.debug("headers : {}", headers);

		Map<String, String> queryParams = Maps.newHashMap();
		Map<String, List<String>> tmpQueryParams = new QueryStringDecoder(request.getUri()).getParameters();
		for (Map.Entry<String, List<String>> e : tmpQueryParams.entrySet()) {
			queryParams.put(e.getKey(), StringUtils.join(e.getValue(), ","));
		}
		logger.debug("queryParams : {}", queryParams);

		Map<String, String> formParams = Maps.newHashMap();
		try {
			String[] keyAndVals = bodyStr.split("&");
			for (String keyAndVal : keyAndVals) {
				if (keyAndVal.contains("=")) {
					final String[] keyAndValArray = keyAndVal.split("=");
					formParams.put(URLDecoder.decode(keyAndValArray[0], server.getCharsetStr()),
						URLDecoder.decode(keyAndValArray.length >= 2 ? keyAndValArray[1] : "", server.getCharsetStr()));
				}
			}
		} catch (Exception e) {
		}
		logger.debug("formParams : {}", formParams);

		logger.debug("pathParams : {}", pathParams);

		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> type = paramTypes[i];

			if (type.isAssignableFrom(ChannelHandlerContext.class)) {
				args.add(ctx);
			} else if (type.isAssignableFrom(MessageEvent.class)) {
				args.add(evt);
			} else if (type.isAssignableFrom(HttpRequest.class)) {
				args.add(request);
			} else {
				Annotation annotation = null;
				for (int j = 0; j < paramAnnotations[i].length; j++) {
					if (isValidParameterAnnotation(paramAnnotations[i][j])) {
						annotation = paramAnnotations[i][j];
						continue;
					}
				}

				if (annotation == null) {
					throw new IllegalArgumentException("There is no valid annotation on controller method.");
				}

				if (annotation instanceof Body) {
					if (!type.isAssignableFrom(String.class)) {
						throw new IllegalArgumentException("@Body support String.");
					}
					Object arg = bodyStr;
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof FormParam) {
					if (!type.isAssignableFrom(String.class)) {
						throw new IllegalArgumentException("@FormParam support String.");
					}
					String key = ((FormParam)annotation).value();
					Object arg = formParams.get(key);
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof FormParams) {
					if (!type.isAssignableFrom(Map.class)) {
						throw new IllegalArgumentException("@FormParams support Map<String, String>.");
					}
					Object arg = formParams;
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof Header) {
					if (!type.isAssignableFrom(String.class)) {
						throw new IllegalArgumentException("@Header support String.");
					}
					final String key = ((Header)annotation).value();
					Object arg = headers.get(key);
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof Headers) {
					if (!type.isAssignableFrom(Map.class)) {
						throw new IllegalArgumentException("@Headers support Map<String, String>.");
					}
					Object arg = headers;
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof PathParam) {
					if (!type.isAssignableFrom(String.class)) {
						throw new IllegalArgumentException("@PathParam support String.");
					}
					String key = ((PathParam)annotation).value();
					Object arg = pathParams.get(key);
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof PathParams) {
					if (!type.isAssignableFrom(Map.class)) {
						throw new IllegalArgumentException("@PathParams support Map<String, String>.");
					}
					Object arg = pathParams;
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof QueryParam) {
					if (!type.isAssignableFrom(String.class)) {
						throw new IllegalArgumentException("@QueryParam support String.");
					}
					String key = ((QueryParam)annotation).value();
					Object arg = queryParams.get(key);
					args.add(arg);
					annotationArgs.add(arg);
				} else if (annotation instanceof QueryParams) {
					if (!type.isAssignableFrom(Map.class)) {
						throw new IllegalArgumentException("@QueryParams support Map<String, String>.");
					}
					Object arg = queryParams;
					args.add(arg);
					annotationArgs.add(arg);
				} else {
					WithArgumentResolver withAnno = annotation.annotationType().getAnnotation(WithArgumentResolver.class);
					if (withAnno == null) {
						continue;
					}

					ArgumentResolver<?> resolver = (ArgumentResolver<?>)server.getInjector().getInstance(withAnno.value());
					if (!resolver.supportedType(type)) {
						throw new IllegalArgumentException("@" + annotation.annotationType().getSimpleName()
							+ " does not support " + type.getSimpleName() + ".");
					}
					Object arg = resolver.resolveArgument(ctx, evt);
					args.add(arg);
					annotationArgs.add(arg);
				}
			}
		}

		//monitoring
		ChannelAttachment channelAttachment = ChannelAttachment.getByChannel(evt.getChannel());
		if (channelAttachment.isNowMonitoring()) {
			logger.debug("server is monitoring.");

			String eventTypeName = clazz.getSimpleName() + "_" + method.getName();
			channelAttachment.setEventTypeName(eventTypeName);
			Map<String, Object> event = channelAttachment.getEvent();
			int index = 0;
			for (Object arg : annotationArgs) {
				event.put("annotationArg" + index, arg);
				index++;
			}

			logger.debug("store event to channelAttachment. eventTypeName : {}, event : {}", eventTypeName, event);
		} else {
			logger.debug("server is NOT monitoring.");
		}

		return args;
	}

	public static String getHttpMethod(HttpRequest request) {
		String headerHttpMethod = request.headers().get(X_HTTP_METHOD_OVERRIDE);
		String httpMethod = request.getMethod().getName();
		if (StringUtils.isNotBlank(headerHttpMethod) && "post".equalsIgnoreCase(httpMethod)) {
			if ("put".equalsIgnoreCase(headerHttpMethod)) {
				httpMethod = "PUT";
			} else if ("delete".equalsIgnoreCase(headerHttpMethod)) {
				httpMethod = "DELETE";
			}
		}

		return StringUtils.upperCase(httpMethod);
	}

	public static Map<String, String> getRequestHeaders(HttpRequest request) {
		final Map<String, String> headers = Maps.newHashMap();
		final HttpHeaders httpHeaders = request.headers();
		for (String headerName : httpHeaders.names()) {
			headers.put(headerName, httpHeaders.get(headerName));
		}
		return headers;
	}

	public static boolean isValidParameterAnnotation(Annotation annotation) {
		if (annotation instanceof Body) {
			return true;
		}
		if (annotation instanceof FormParam) {
			return true;
		}
		if (annotation instanceof FormParams) {
			return true;
		}
		if (annotation instanceof Header) {
			return true;
		}
		if (annotation instanceof Headers) {
			return true;
		}
		if (annotation instanceof PathParam) {
			return true;
		}
		if (annotation instanceof PathParams) {
			return true;
		}
		if (annotation instanceof QueryParam) {
			return true;
		}
		if (annotation instanceof QueryParams) {
			return true;
		}
		if (annotation.annotationType().getAnnotation(WithArgumentResolver.class) != null) {
			return true;
		}
		return false;
	}
}
