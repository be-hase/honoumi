package com.be_hase.honoumi.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;


public class MonitoringResultSet {
	private static Logger logger = LoggerFactory.getLogger(MonitoringResultSet.class);
	private Map<String, BlockingQueue<ObjectNode>> results = new HashMap<String, BlockingQueue<ObjectNode>>();

	public void add(String queryName, int maxStoreCount, ObjectNode obj) {
		BlockingQueue<ObjectNode> result = results.get(queryName);
		if (result != null) {
			if (result.remainingCapacity() == 0) {
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
			result = new LinkedBlockingQueue<ObjectNode>(maxStoreCount);
			result.offer(obj); {
				logger.debug("store result data : {}", obj);
			}
			results.put(queryName, result);
		}
	}
	
	public List<ObjectNode> getByQueryName(String queryName) {
		BlockingQueue<ObjectNode> result = results.get(queryName);
		
		if (result == null) {
			return new ArrayList<ObjectNode>();
		}
		return getByQueryName(queryName, result.size());
	}
	public List<ObjectNode> getByQueryName(String queryName, int limit) {
		List<ObjectNode> rtn = Lists.newArrayList();
		if (limit <= 0) {
			return rtn;
		}
		
		BlockingQueue<ObjectNode> result = results.get(queryName);
		if (result == null) {
			return rtn;
		}
		
		rtn.addAll(result);
		if (rtn.isEmpty()) {
			return rtn;
		}

		int size = rtn.size();
		int cnt = new Long(limit).intValue();
		int fromIndex = size - cnt > 0 ? size - cnt : 0;
		int toIndex = fromIndex + cnt > size ? size : fromIndex + cnt;

		if (rtn.size() < toIndex) {
		} else {
			rtn = rtn.subList(fromIndex, toIndex);
		}
		
		Collections.reverse(rtn);
		return rtn;
	}
	
//	public Map<String, List<ObjectNode>> getAll() {
//		Map<String, List<ObjectNode>> rtn = Maps.newHashMap();
//		
//		for (Entry<String, BlockingQueue<ObjectNode>> e: results.entrySet()) {
//			String queryName = e.getKey();
//			rtn.put(queryName, getByQueryName(queryName));
//		}
//		
//		return rtn;
//	}
//	public Map<String, List<ObjectNode>> getAll(int limit) {
//		Map<String, List<ObjectNode>> rtn = Maps.newHashMap();
//		
//		for (Entry<String, BlockingQueue<ObjectNode>> e: results.entrySet()) {
//			String queryName = e.getKey();
//			rtn.put(queryName, getByQueryName(queryName, limit));
//		}
//		
//		return rtn;
//	}
	
	public void clear(String queryName) {
		results.remove(queryName);
	}
	public void clearAll() {
		results.clear();
	}
	
	public int getMaxStoreCount(String queryName) {
		BlockingQueue<ObjectNode> result = results.get(queryName);
		if (result == null) {
			return -1;
		}
		
		return result.remainingCapacity() + result.size();
	}
	
	public int getStoreCount(String queryName) {
		BlockingQueue<ObjectNode> result = results.get(queryName);
		if (result == null) {
			return 0;
		}
		
		return result.size();
	}
	
	
	public Map<String, BlockingQueue<ObjectNode>> getResults() {
		return results;
	}
	public void setResults(Map<String, BlockingQueue<ObjectNode>> results) {
		this.results = results;
	}
}
