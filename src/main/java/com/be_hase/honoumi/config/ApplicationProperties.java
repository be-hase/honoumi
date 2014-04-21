package com.be_hase.honoumi.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ApplicationProperties {
	private ApplicationProperties() {};

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationProperties.class);
	
	
	private static final String ENVIRONMENT = "application.environment";
	private static final String PROPERTIES = "application.properties";

	private static CompositeConfiguration compositeConfiguration;
	private static String environment;

	static {
		compositeConfiguration = new CompositeConfiguration();
		environment = System.getProperty(ENVIRONMENT);
		String propertiesFiles = System.getProperty(PROPERTIES);
		
		if (StringUtils.isNotBlank(propertiesFiles)) {
			List<String> propertiesFilesList = Arrays.asList(propertiesFiles.split(","));
			Collections.reverse(propertiesFilesList);
			
			for (String propertiesFile: propertiesFilesList) {
				if (StringUtils.isNotBlank(propertiesFile)) {
					PropertiesConfiguration defaultConfiguration = loadConfigurationInUtf8(propertiesFile);
					
					Configuration prefixedDefaultConfiguration = null;
					if (StringUtils.isNotBlank(environment) && defaultConfiguration != null) {
						prefixedDefaultConfiguration = defaultConfiguration.subset("%" + environment);
					}
					
					if (prefixedDefaultConfiguration != null) {
						compositeConfiguration.addConfiguration(prefixedDefaultConfiguration);
					}
					if (defaultConfiguration != null) {
						compositeConfiguration.addConfiguration(defaultConfiguration);
					}
				}
			}
		}
		
		logger.debug("Init application propeties");
	}

	private static PropertiesConfiguration loadConfigurationInUtf8(
			String fileOrUrlOrClasspathUrl) {
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setEncoding("utf-8");
		propertiesConfiguration.setDelimiterParsingDisabled(true);
		propertiesConfiguration.setFileName(fileOrUrlOrClasspathUrl);

		try {
			propertiesConfiguration.load(fileOrUrlOrClasspathUrl);
		} catch (ConfigurationException e) {
			logger.info("Could not load file " + fileOrUrlOrClasspathUrl
					+ " (not a bad thing necessarily, but I am returing null)");
			return null;
		}

		return propertiesConfiguration;
	}

	/**
	 * Get a string associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated string.
	 *
	 * @throws ConversionException is thrown if the key maps to an object that
	 *         is not a String.
	 */
	public static String get(String key) {
		return compositeConfiguration.getString(key);
	}
	/**
	 * Get a string associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated string if key is found and has valid
	 *         format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an object that
	 *         is not a String.
	 */
	public static String get(String key, String defaultValue) {
		return compositeConfiguration.getString(key, defaultValue);
	}

	/**
	 * Get an array of strings associated with the given configuration key.
	 * If the key doesn't map to an existing object an empty array is returned
	 *
	 * @param key The configuration key.
	 * @return The associated string array if key is found.
	 *
	 */
	public static String[] getStringArray(String key) {
		String value = compositeConfiguration.getString(key);
		if (value != null) {
			return Iterables.toArray(Splitter.on(",").trimResults()
					.omitEmptyStrings().split(value), String.class);
		} else {
			return null;
		}
	}
	/**
	 * Get an array of strings associated with the given configuration key.
	 * If the key doesn't map to an existing object an empty array is returned
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated string array if key is found.
	 *
	 */
	public static String[] getStringArray(String key, String[] defaultValue) {
		String[] value = getStringArray(key);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Get a boolean associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated boolean.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Boolean.
	 */
	public static boolean getBoolean(String key) {
		return compositeConfiguration.getBoolean(key);
	}
	/**
	 * Get a boolean associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated boolean.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Boolean.
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		return compositeConfiguration.getBoolean(key, defaultValue);
	}
	/**
	 * Get a {@link Boolean} associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated boolean if key is found and has valid
	 *         format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Boolean.
	 */
	public static Boolean getBoolean(String key, Boolean defaultValue) {
		return compositeConfiguration.getBoolean(key, defaultValue);
	}
	
	/**
	 * Get a int associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated int.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Integer.
	 */
	public static int getInt(String key) {
		return compositeConfiguration.getInt(key);
	}
	/**
	 * Get a int associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated int.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Integer.
	 */
	public static int getInt(String key, int defaultValue) {
		return compositeConfiguration.getInt(key, defaultValue);
	}
	/**
	 * Get an {@link Integer} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated int if key is found and has valid format, default
	 *         value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an object that
	 *         is not a Integer.
	 */
	public static Integer getInteger(String key, Integer defaultValue) {
		return compositeConfiguration.getInteger(key, defaultValue);
	}
	
	/**
	 * Get a double associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated double.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Double.
	 */
	public static double getDouble(String key) {
		return compositeConfiguration.getDouble(key);
	}
	/**
	 * Get a double associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated double.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Double.
	 */
	public static double getDouble(String key, double defaultValue) {
		return compositeConfiguration.getDouble(key, defaultValue);
	}
	/**
	 * Get a {@link Double} associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated double if key is found and has valid
	 *         format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Double.
	 */
	public static Double getDouble(String key, Double defaultValue) {
		return compositeConfiguration.getDouble(key, defaultValue);
	}
	
	/**
	 * Get a float associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated float.
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Float.
	 */
	public static float getFloat(String key) {
		return compositeConfiguration.getFloat(key);
	}
	/**
	 * Get a float associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated float.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Float.
	 */
	public static float getFloat(String key, float defaultValue) {
		return compositeConfiguration.getFloat(key, defaultValue);
	}
	
	/**
	 * Get a {@link Float} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated float if key is found and has valid
	 *         format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Float.
	 */
	public static Float getFloat(String key, Float defaultValue) {
		return compositeConfiguration.getFloat(key, defaultValue);
	}
	
	/**
	 * Get a long associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated long.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Long.
	 */
	public static long getLong(String key) {
		return compositeConfiguration.getLong(key);
	}
	
	/**
	 * Get a long associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated long.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Long.
	 */
	public static long getLong(String key, long defaultValue) {
		return compositeConfiguration.getLong(key, defaultValue);
	}
	
	/**
	 * Get a {@link Long} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated long if key is found and has valid
	 * format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Long.
	 */
	public static Long getLong(String key, Long defaultValue) {
		return compositeConfiguration.getLong(key, defaultValue);
	}
	
	/**
	 * Get a short associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated short.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Short.
	 */
	public static short getShort(String key) {
		return compositeConfiguration.getShort(key);
	}
	
	/**
	 * Get a short associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated short.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Short.
	 */
	public static short getShort(String key, short defaultValue) {
		return compositeConfiguration.getShort(key, defaultValue);
	}
	
	/**
	 * Get a {@link Short} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key The configuration key.
	 * @param defaultValue The default value.
	 * @return The associated short if key is found and has valid
	 *         format, default value otherwise.
	 *
	 * @throws ConversionException is thrown if the key maps to an
	 *         object that is not a Short.
	 */
	public static Short getShort(String key, Short defaultValue) {
		return compositeConfiguration.getShort(key, defaultValue);
	}
	
	/**
	 * Get a {@link BigDecimal} associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 * @return The associated BigDecimal if key is found and has valid format
	 */
	public static BigDecimal getBigDecimal(String key) {
		return compositeConfiguration.getBigDecimal(key);
	}
	
	/**
	 * Get a {@link BigDecimal} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key          The configuration key.
	 * @param defaultValue The default value.
	 *
	 * @return The associated BigDecimal if key is found and has valid
	 *         format, default value otherwise.
	 */
	public static BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		return compositeConfiguration.getBigDecimal(key, defaultValue);
	}
	
	/**
	 * Get a {@link BigInteger} associated with the given configuration key.
	 *
	 * @param key The configuration key.
	 *
	 * @return The associated BigInteger if key is found and has valid format
	 */
	public static BigInteger getBigInteger(String key) {
		return compositeConfiguration.getBigInteger(key);
	}
	
	/**
	 * Get a {@link BigInteger} associated with the given configuration key.
	 * If the key doesn't map to an existing object, the default value
	 * is returned.
	 *
	 * @param key          The configuration key.
	 * @param defaultValue The default value.
	 *
	 * @return The associated BigInteger if key is found and has valid
	 *         format, default value otherwise.
	 */
	public static BigInteger getBigInteger(String key, BigInteger defaultValue) {
		return compositeConfiguration.getBigInteger(key, defaultValue);
	}

	/**
	 * Get the list of the keys contained in the configuration. The returned
	 * iterator can be used to obtain all defined keys. Note that the exact
	 * behavior of the iterator's {@code remove()} method is specific to
	 * a concrete implementation. It <em>may</em> remove the corresponding
	 * property from the configuration, but this is not guaranteed. In any case
	 * it is no replacement for calling
	 * {@link #clearProperty(String)} for this property. So it is
	 * highly recommended to avoid using the iterator's {@code remove()}
	 * method.
	 *
	 * @return An List.
	 */
	public static List<String> getKeys(String key) {
		List<String> result = Lists.newArrayList();
		
		Iterator<String> i = compositeConfiguration.getKeys(key);
		while (i.hasNext()) {
			result.add(i.next());
		}
		
		return result;
	}
	/**
	 * Get the list of the keys contained in the configuration. The returned
	 * iterator can be used to obtain all defined keys. Note that the exact
	 * behavior of the iterator's {@code remove()} method is specific to
	 * a concrete implementation. It <em>may</em> remove the corresponding
	 * property from the configuration, but this is not guaranteed. In any case
	 * it is no replacement for calling
	 * {@link #clearProperty(String)} for this property. So it is
	 * highly recommended to avoid using the iterator's {@code remove()}
	 * method.
	 *
	 * @return An List.
	 */
	public static List<String> getKeys() {
		List<String> result = Lists.newArrayList();
		
		Iterator<String> i = compositeConfiguration.getKeys();
		while (i.hasNext()) {
			result.add(i.next());
		}
		
		return result;
	}
	
	/**
	 * Get environment value you specified when start application.
	 * 
	 * @return
	 */
	public static String getEnvironment() {
		return environment;
	}

	/**
	 * Get java.util.Properties
	 * 
	 * @return
	 */
	public static java.util.Properties getAllCurrentProperties() {
		return ConfigurationConverter.getProperties(compositeConfiguration);
	}
}
