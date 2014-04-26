package com.be_hase.honoumi.domain;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


public class MonitoringResult {
	private static Logger logger = LoggerFactory.getLogger(MonitoringResult.class);
	private Map<String, BlockingQueue<Object>> results = Maps.newLinkedHashMap();

	public void add(String queryName, int storeCount, Object obj) {
		BlockingQueue<Object> result = results.get(queryName);
		if (result != null) {
			if (result.size() >= result.remainingCapacity()) {
				try {
					result.take();
					logger.debug("Deleting stored data. with LRU");
				} catch (Exception e) {
					logger.error("queue stored taking failed.", e);
				}
			}

			if (result.offer(obj)) {
				logger.debug("store result data : {}", obj);
			}
		} else {
			result = new LinkedBlockingQueue<Object>(storeCount);
			result.offer(obj); {
				logger.debug("store result data : {}", obj);
			}
			results.put(queryName, result);
		}
	}
	
	
	public Map<String, BlockingQueue<Object>> getResults() {
		return results;
	}
	public void setResults(Map<String, BlockingQueue<Object>> results) {
		this.results = results;
	}
}
