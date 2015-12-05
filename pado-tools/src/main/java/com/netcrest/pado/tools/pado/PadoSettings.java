/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.tools.pado;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Pado configuration property setting wrapper object
 * 
 */
public class PadoSettings {
	/**
	 * SYS_PROPERTY="pado-config.properties"
	 */
	public static final String SYS_PROPERTY = "config.properties";

	/**
	 * RESOURCE_BUNDLE_NAME = "config"
	 */
	public static final String RESOURCE_BUNDLE_NAME = "config";

	private PadoSettings() {
		loadProperties();
	}// ------------------------------------------------

	public static PadoSettings getInstance() {

		return instance;
	}// ------------------------------------------------

	/**
	 * 
	 * @param property
	 * @return results
	 */
	public final String getProperty(String property) {
		// get from system
		String results = System.getProperty(property);

		if (results == null) {
			try {
				// check resource
				results = this.properties.getProperty(property);
			} catch (MissingResourceException e) {
			}
		}
		return results;
	}// ------------------------------------------------

	/**
	 * 
	 * @param property
	 *            the name of the property
	 * @param defaultValue
	 * @return
	 */
	public final String getProperty(String property, String defaultValue) {
		if (property == null)
			return defaultValue;

		// get from system
		String results = getProperty(property);

		if (results == null || results.length() == 0)
			return defaultValue;

		return results;
	}// ------------------------------------------------

	/**
	 * Get an boolean property from config.properties resource bundle
	 * 
	 * @param property
	 *            the property name
	 * @param defaultValue
	 *            the default value if the property does not exist
	 * @return the resource or default value
	 */
	public final boolean getPropertyBoolean(String property,
			boolean defaultValue) {
		String results = getProperty(property, String.valueOf(defaultValue));

		if (results == null || results.length() == 0)
			return Boolean.valueOf(defaultValue).booleanValue();

		return Boolean.valueOf(results).booleanValue();
	}// ------------------------------------------------

	/**
	 * Get an integer property from config.properties resource bundle
	 * 
	 * @param property
	 *            the property name
	 * @param defaultValue
	 *            the default value if the property does not exist
	 * @return the resource or default value
	 */
	public final int getPropertyInteger(String property, int defaultValue) {
		String results = getProperty(property, String.valueOf(defaultValue));

		if (results == null || results.length() == 0)
			return Integer.valueOf(defaultValue).intValue();

		return Integer.valueOf(results).intValue();
	}// ------------------------------------------------

	/**
	 * Load the configuration properties from the properties file.
	 * <p/>
	 * <p/>
	 * <p/>
	 * Caller must test to ensure that properties is Non-null.
	 * 
	 * @throws IllegalArgumentException
	 *             Translates an IOException from reading
	 *             <p/>
	 *             the properties file into a run time exception.
	 */

	private synchronized void loadProperties() {
		// If multiple threads are waiting to invoke this method only allow
		// the first one to do so. The rest should just return since the first
		// thread through took care of loading the properties.
		try {
			String file = getSystemPropertyFile();
			if (file != null && file.length() > 0) {

				FileInputStream fis = null;

				try {
					fis = new FileInputStream(file);
					properties = new Properties();
					// Load the properties object from the properties file
					properties.load(fis);

					location = file;

				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.toString());
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							// do nothing
						} // Always close the file, even on exception
					}
				}

			} else {

				// try to get properties from resource bundle

				ResourceBundle rb = ResourceBundle
						.getBundle(RESOURCE_BUNDLE_NAME);

				location = String.valueOf(PadoSettings.class
						.getResource(RESOURCE_BUNDLE_NAME + ".properties"));

				Enumeration<String> keys = rb.getKeys();

				String k = null;

				// String v = null;

				properties = new Properties();

				while (keys.hasMoreElements()) {

					k = keys.nextElement();

					properties.put(k, rb.getString(k + ""));

				}
			}// end els load from resource bundle

		} catch (MissingResourceException e) {
			this.properties = new Properties();
		}
	}// ------------------------------------------------------------

	/**
	 * @return the system property file
	 */

	private static String getSystemPropertyFile() {
		String file = System.getProperty(SYS_PROPERTY);
		return file;

	}// -----------------------------------------------------------

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PadoSettings [properties=" + properties + ", location="
				+ location + "]";
	}// --------------------------------------------------------

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}// -----------------------------------------------

	/**
	 * 
	 * @return the configuration location
	 */
	public String getLocation() {
		return location;
	}// --------------------------------------------------------

	private Properties properties = null; // configuration properties
	private static PadoSettings instance = new PadoSettings();
	private String location = null;
}
