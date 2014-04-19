package com.be_hase.honoumi.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.be_hase.honoumi.util.JacksonUtils;


public class BaseDomain {
	@Override
	public String toString() {
		return JacksonUtils.toJsonString(this);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
