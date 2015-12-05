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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.netcrest.pado.log.Logger;

public class ServiceFinder
{
	/*
	 * Try to find provider using Jar Service Provider Mechanism
	 * 
	 * @param factoryId The interface name to search in "META-INF/services/"
	 * 
	 * @return all implementation class names that implements the
	 * <code>factoryId</code>
	 */
	public static List<String> findJarServiceProvider(String factoryId) throws ConfigurationError
	{
		List<String> serviceClasses = new ArrayList<String>();
		String serviceId = "META-INF/services/" + factoryId;
		List<InputStream> iss = null;

		// First try the Context ClassLoader
		ClassLoader cl = getContextClassLoader();
		if (cl != null) {
			iss = getResourcesAsStream(cl, serviceId);

			// If no provider found then try the current ClassLoader
			if (iss == null) {
				cl = ServiceFinder.class.getClassLoader();
				iss = getResourcesAsStream(cl, serviceId);
			}
		} else {
			// No Context ClassLoader, try the current ClassLoader
			cl = ServiceFinder.class.getClassLoader();
			iss = getResourcesAsStream(cl, serviceId);
		}

		if (iss == null) {
			// No provider found
			return null;
		}
		for (InputStream is : iss) {
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			} catch (java.io.UnsupportedEncodingException e) {
				rd = new BufferedReader(new InputStreamReader(is));
			}

			String factoryClassName = null;
			try {
				// XXX Does not handle all possible input as specified by the
				// Jar Service Provider specification
				factoryClassName = rd.readLine();
				while (factoryClassName != null) {
					Logger.info("ServiceFinder.findJarServiceProvider(): factoryClassName=" + factoryClassName);
					if (factoryClassName.length() != 0) {

						// Note: here we do not want to fall back to the current
						// ClassLoader because we want to avoid the case where
						// the
						// resource file was found using one ClassLoader and the
						// provider class was instantiated using a different
						// one.
						serviceClasses.add(factoryClassName);
					}
					factoryClassName = rd.readLine();
				}
				rd.close();
			} catch (IOException x) {
				// No provider found
				return null;
			}

		}

		Logger.info("ServiceFinder.findJarServiceProvider(): " + serviceClasses);
		// No provider found
		return serviceClasses;
	}

	static class ConfigurationError extends Error
	{
		private Exception exception;

		/**
		 * Construct a new instance with the specified detail string and
		 * exception.
		 */
		ConfigurationError(String msg, Exception x)
		{
			super(msg);
			this.exception = x;
		}

		Exception getException()
		{
			return exception;
		}
	}

	private static ClassLoader getContextClassLoader()
	{
		ClassLoader cl = null;
		// try {
		cl = Thread.currentThread().getContextClassLoader();
		// } catch (SecurityException ex) { }

		if (cl == null)
			cl = ClassLoader.getSystemClassLoader();

		return cl;
	}

	private static List<InputStream> getResourcesAsStream(final ClassLoader cl, final String name)
	{
		Enumeration<URL> urls = null;
		List<InputStream> iss = new ArrayList<InputStream>();
		InputStream ris;
		try {
			if (cl == null) {
				urls = ClassLoader.getSystemResources(name);
			} else {
				urls = cl.getResources(name);
			}
		} catch (Exception ex) {
			return null;
		}
		iss = new ArrayList<InputStream>();
		while (urls.hasMoreElements()) {
			if (iss == null) {
				iss = new ArrayList<InputStream>();
			}
			InputStream is = null;
			try {
				is = urls.nextElement().openStream();
				iss.add(is);
			} catch (Exception ex) {

			}

		}
		return iss;
	}

}
