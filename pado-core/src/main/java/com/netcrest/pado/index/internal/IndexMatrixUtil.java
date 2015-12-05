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
package com.netcrest.pado.index.internal;

import java.util.Properties;

public class IndexMatrixUtil implements Constants
{
	private static Properties indexProperties = new Properties();

	static {
		// Default values
		indexProperties.setProperty(PROP_REGION_SYSTEM, "/__system");
		indexProperties.setProperty(PROP_REGION_INDEX, "/__index");
		indexProperties.setProperty(PROP_REGION_RESULTS, "/__results");
		indexProperties.setProperty(PROP_REGION_LUCENE, "/__lucene");
	}
	
	/**
	 * Returns the property value by first checking the system property. If the
	 * system property exists then it returns its value, otherwise, it returns
	 * the property value found in the specified properties. Returns the
	 * specified default value if not found.
	 * 
	 * @param props
	 *            Properties to look up
	 * @param prop
	 *            Property
	 * @param defaultValue
	 *            The default value if not found.
	 */
	public static String getProperty(Properties props, String prop, String defaultValue)
	{
		if (prop == null) {
			return null;
		}
		String val = System.getProperty(prop);
		if (val == null) {
			if (props == null) {
				val = defaultValue;
			} else {
				val = props.getProperty(prop, defaultValue);
			}
		}
		return val;
	}
	
	/**
	 * Returns the system property. Analogous to invoking
	 * {@link #getProperty(Properties, String, String)} with null for the
	 * properties argument.
	 * 
	 * @param prop
	 *            Property
	 * @param defaultValue
	 *            The default property value
	 */
	public static String getProperty(String prop, String defaultValue)
	{
		return getProperty(null, prop, defaultValue);
	}

	/**
	 * Returns the IndexMatrix property. The system properties always override
	 * IndexMatrix properties. It returns the system property value if defined.
	 * 
	 * @param prop
	 *            A Pado property name
	 */
	public static String getProperty(String prop)
	{
		if (prop == null) {
			return null;
		}
		String value = getProperty(prop, null);
		if (value == null) {
			value = indexProperties.getProperty(prop);
		}
		return value;
	}

	/**
	 * Returns true if the specified property has the value of "true". The value
	 * is case-insensitive.
	 * 
	 * @param prop
	 *            Property
	 */
	public static boolean isProperty(String prop)
	{
		String value = getProperty(prop);
		return value != null && value.equalsIgnoreCase("true") ? true : false;
	}

	public static Properties getIndexMatrixProperties()
	{
		return indexProperties;
	}
}
