package com.be_hase.honoumi.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConversionException;
import org.junit.Before;
import org.junit.Test;

public class ApplicationPropertiesTest extends TestCase {
	@Before
	public void setUp() {
		System.setProperty("application.environment", "local");
		System.setProperty("application.properties", "test1.properties,test2.properties,server.properties");
	}

	@Test
	public void test_get() {
		assertEquals("hoge", ApplicationProperties.get("test.hoge"));
		assertEquals("bar", ApplicationProperties.get("test.bar"));
		assertEquals("1", ApplicationProperties.get("test.int"));
		assertEquals("case", ApplicationProperties.get("test.case"));
		assertEquals("Case", ApplicationProperties.get("test.Case"));
		assertNull(ApplicationProperties.get("test.notExist"));
		assertEquals("default", ApplicationProperties.get("test.notExist", "default"));
	}
	
	@Test
	public void test_getStringArray() {
		String[] stringArray = ApplicationProperties.getStringArray("test.stringArray");
		assertEquals("1", stringArray[0]);
		assertEquals("2", stringArray[1]);
		assertEquals("3", stringArray[2]);

		String[] stringArrayNull = ApplicationProperties.getStringArray("test.stringArray.null");
		assertNull(stringArrayNull);

		String[] stringArrayDefault = ApplicationProperties.getStringArray("test.notExist", new String[]{"1","2","3"});
		assertEquals("1", stringArrayDefault[0]);
		assertEquals("2", stringArrayDefault[1]);
		assertEquals("3", stringArrayDefault[2]);
	}
	
	@Test
	public void test_getBoolean() {
		assertEquals(true, ApplicationProperties.getBoolean("test.booleanTrue"));
		assertEquals(false, ApplicationProperties.getBoolean("test.booleanFalse"));
		try {
			ApplicationProperties.getBoolean("test.notExist");
			fail();
		} catch (Exception e) {
		}
		assertEquals(true, ApplicationProperties.getBoolean("test.booleanTrue", true));
		assertEquals(false, ApplicationProperties.getBoolean("test.booleanFalse", true));
		assertEquals(true, ApplicationProperties.getBoolean("test.notExist", true));
	}
	
	@Test
	public void test_getInt() {
		assertEquals(1, ApplicationProperties.getInt("test.int"));
		try {
			assertEquals(1, ApplicationProperties.getInt("test.notExist"));
			fail();
		} catch (Exception e) {
		}
		try {
			ApplicationProperties.getInt("test.double");
			fail();
		} catch (Exception e) {
		}
		assertEquals(1, ApplicationProperties.getInt("test.notExist", 1));
	}
	
	@Test
	public void test_getInteger() {
		assertEquals(new Integer(1), ApplicationProperties.getInteger("test.notExist", 1));
	}
	
	@Test
	public void test_getDouble() {
		assertEquals(1.1, ApplicationProperties.getDouble("test.double"));
		try {
			ApplicationProperties.getDouble("test.notExist");
			fail();
		} catch (Exception e) {
		}
		assertEquals(1.0, ApplicationProperties.getDouble("test.int"));
		assertEquals(1.0, ApplicationProperties.getDouble("test.notExist", (double)1));
	}

	@Test
	public void test_getFloat() {
		assertEquals(1.1f, ApplicationProperties.getFloat("test.float"));
		try {
			ApplicationProperties.getFloat("test.notExist");
			fail();
		} catch (Exception e) {
		}
		assertEquals(1.0f, ApplicationProperties.getFloat("test.int"));
		assertEquals(1.0f, ApplicationProperties.getFloat("test.notExist", (float)1));
	}
	
	@Test
	public void test_getLong() {
		assertEquals(1, ApplicationProperties.getLong("test.long"));
		try {
			ApplicationProperties.getLong("test.notExist");
			fail();
		} catch (Exception e) {
		}
		assertEquals(1, ApplicationProperties.getLong("test.int"));
		assertEquals(1, ApplicationProperties.getLong("test.notExist", (long)1));
	}
	
	@Test
	public void test_getShort() {
		assertEquals(1, ApplicationProperties.getShort("test.short"));
		try {
			ApplicationProperties.getShort("test.notExist");
			fail();
		} catch (Exception e) {
		}
		assertEquals(1, ApplicationProperties.getShort("test.int"));
		assertEquals(1, ApplicationProperties.getShort("test.notExist", (short)1));
	}
	
	@Test
	public void test_getBigDecimal() {
		assertEquals(1.1, ApplicationProperties.getBigDecimal("test.bigDecimal").doubleValue());
		assertEquals(1, ApplicationProperties.getBigDecimal("test.int").intValue());
		assertNull(ApplicationProperties.getBigDecimal("test.notExist"));
		try {
			ApplicationProperties.getBigDecimal("test.hoge");
			fail();
		} catch (ConversionException e) {
		}
		assertEquals(1.1, ApplicationProperties.getBigDecimal("test.notExist", new BigDecimal(1.1)).doubleValue());
	}
	
	@Test
	public void test_getBigInteger() {
		assertEquals(1, ApplicationProperties.getBigInteger("test.bigInteger").intValue());
		assertEquals(1, ApplicationProperties.getBigInteger("test.int").intValue());
		assertNull(ApplicationProperties.getBigInteger("test.notExist"));
		try {
			ApplicationProperties.getBigInteger("test.hoge");
			fail();
		} catch (ConversionException e) {
		}
		assertEquals(1, ApplicationProperties.getBigInteger("test.notExist", new BigInteger("1")).intValue());
	}
	
	@Test
	public void test_getKeys() {
		List<String> keys = ApplicationProperties.getKeys("test.keys");
		assertEquals("test.keys.1", keys.get(0));
		assertEquals("test.keys.6", keys.get(1));
		assertEquals("test.keys.2", keys.get(2));
		assertEquals("test.keys.3", keys.get(3));
	}
	
	@Test
	public void test_getEnvironment() {
		assertEquals("local", ApplicationProperties.getEnvironment());
	}
	
	@Test
	public void test_env() {
		assertEquals("local", ApplicationProperties.get("test.env"));
		assertEquals("local", ApplicationProperties.get("%local.test.env"));
		assertEquals("alpha", ApplicationProperties.get("%alpha.test.env"));
		assertEquals("release", ApplicationProperties.get("%release.test.env"));
	}
	
	@Test
	public void test_override() {
		assertEquals("test2", ApplicationProperties.get("test.override"));
		
		assertEquals("local2", ApplicationProperties.get("test.override.env"));
		assertEquals("local2", ApplicationProperties.get("%local.test.override.env"));
		assertEquals("alpha2", ApplicationProperties.get("%alpha.test.override.env"));
		assertEquals("release2", ApplicationProperties.get("%release.test.override.env"));
	}
}
