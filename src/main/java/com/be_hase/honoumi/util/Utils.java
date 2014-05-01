package com.be_hase.honoumi.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
	private Utils(){}
	
	public static String stackTraceToStr(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
