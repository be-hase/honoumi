package com.be_hase.honoumi.controller;

import com.be_hase.honoumi.controller.argument.PathParam;

public class InvalidTest1Controller {
	public void hoge() {
		
	}
	
	public void hoge(
			@PathParam("id")String id
			) {
	}
	
	public String bar() {
		return "";
	}
}
