package com.be_hase.honoumi.controller;

import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.be_hase.honoumi.controller.argument.PathParam;
import com.be_hase.honoumi.controller.argument.PathParams;
import com.be_hase.honoumi.util.JacksonUtils;

public class TestController {
	public void hoge(
			MessageEvent evt
			) {
		Response.execute(evt, HttpResponseStatus.OK, null, "hoge");
	}
	
	public void bar(
			MessageEvent evt,
			@PathParam("path1") CharSequence path1,
			@PathParam("path2") String path2
			) {
		Response.execute(evt, HttpResponseStatus.OK, null, (String)path1 + path2);
	}
	public void fuga(
			MessageEvent evt,
			@PathParams Map<String, String> pathParams
			) {
		Response.execute(evt, HttpResponseStatus.OK, null, JacksonUtils.toJsonString(pathParams));
	}
}
