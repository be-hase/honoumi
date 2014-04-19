package com.be_hase.honoumi;

import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.be_hase.honoumi.domain.BaseDomain;

public class HttpUtilForTest {
private static Logger log = LoggerFactory.getLogger(HttpUtilForTest.class);
	
	public static HttpUtilResult get(String url, Map<String, String> headers) {
		log.debug("API GET : {}", url);
		log.debug("headers : {}", headers);
		
		HttpUtilResult result = new HttpUtilResult();
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		
		try {
			HttpParams httpParams = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000);
			HttpConnectionParams.setSoTimeout(httpParams, 10*1000);
			
			if (headers != null) {
				for (Map.Entry<String, String> e: headers.entrySet()) {
					request.setHeader(e.getKey(), e.getValue());
				}
			}
			
			HttpResponse httpResponse = httpClient.execute(request);
			
			result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
			
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				result.setResponse(EntityUtils.toString(entity, "UTF-8"));
				EntityUtils.consume(entity);
			}
			
			log.debug("API Result : {}", result);
		} catch (Exception e) {
			e.getStackTrace();
			log.warn("API FAIL : {}", e.getMessage());
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return result;
	};
	
	public static HttpUtilResult post(String url, String inputData, Map<String, String> headers) {
		log.debug("API POST : {}", url);
		log.debug("headers : {}", headers);
		log.debug("Input data : {}", inputData);
		
		HttpUtilResult result = new HttpUtilResult();
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		try {
			HttpParams httpParams = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10*1000);
			HttpConnectionParams.setSoTimeout(httpParams, 10*1000);
			
			if (headers != null) {
				for (Map.Entry<String, String> e: headers.entrySet()) {
					request.setHeader(e.getKey(), e.getValue());
				}
			}
			request.setEntity(new StringEntity(inputData, "UTF-8"));
			
			HttpResponse httpResponse = httpClient.execute(request);
			
			result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
			
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				result.setResponse(EntityUtils.toString(entity, "UTF-8"));
				EntityUtils.consume(entity);
			}
			
			log.debug("API Result : {}", result);
		} catch (Exception e) {
			log.warn("API FAIL : {}", e.getMessage());
			result = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return result;
	}
	
	public static class HttpUtilResult extends BaseDomain {
		private int statusCode;
		private boolean isSuccess;
		private String response;
		
		public boolean isSuccess() {
			return isSuccess;
		}
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
			if (statusCode >= 200 && statusCode < 300 || statusCode == 304) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String response) {
			this.response = response;
		}
	}
}
