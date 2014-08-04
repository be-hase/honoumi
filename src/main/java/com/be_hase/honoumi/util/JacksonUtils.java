package com.be_hase.honoumi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;


public class JacksonUtils {
	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new AfterburnerModule());
	}

	public static String toJsonString(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			//logger.warn("Exception on json converting.", e);
		}

		return null;
	}

	public static <T> T toObject(String jsonString, Class<T> clazz) {
		try {
			return objectMapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			//logger.warn("Exception on json converting.", e);
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T toObject(String jsonString, TypeReference<T> typeReference) {
		try {
			return (T)objectMapper.readValue(jsonString, typeReference);
		} catch (Exception e) {
			//logger.warn("Exception on json converting.", e);
		}

		return null;
	}
	
	public static ObjectNode createObjectNode() {
		return objectMapper.createObjectNode();
	}
	
	public static ObjectNode createObjectNode(Object object) {
		return objectMapper.convertValue(object, ObjectNode.class);
	}
	
	public static ArrayNode createArrayNode() {
		return objectMapper.createArrayNode();
	}
	
	public static ArrayNode createArrayNode(Object object) {
		return objectMapper.convertValue(object, ArrayNode.class);
	}
}
