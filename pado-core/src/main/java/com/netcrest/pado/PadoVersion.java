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
package com.netcrest.pado;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * PadoVersion provides Pado build information extracted from the manifest file.
 * 
 * @author dpark
 *
 */
public class PadoVersion
{
	public static int major;
	public static int minor;
	public static int update;
	public static String build;

	public static String buildJdk;
	public static String projectName;
	public static String title;
	public static String VERSION;
	public static String vendorId;
	public static String REPOSITORY_TAG;
	public static String buildDate;
	public static String builderName;

	static {
		Manifest manifest = getManifest(PadoVersion.class);
		Attributes attrs = manifest.getMainAttributes();
		buildJdk = attrs.getValue("Build-Jdk");
		projectName = attrs.getValue("Project-Name");
		title = attrs.getValue("Specification-Title");
		VERSION = attrs.getValue("Specification-Version");
		vendorId = attrs.getValue("Implementation-Vendor-Id");
		builderName = attrs.getValue("Built-By");
		REPOSITORY_TAG = attrs.getValue("Repository-Tag");
		buildDate = attrs.getValue("Build-Date");

		// 0.4.0-B1
		if (VERSION != null) {
			String split[] = VERSION.split("\\.");
			if (split.length > 0) {
				major = Integer.parseInt(split[0]);
			}
			if (split.length > 1) {
				minor = Integer.parseInt(split[1]);
			}
			if (split.length > 2) {
				String split2[] = split[2].split("-B");
				if (split2.length > 0) {
					update = Integer.parseInt(split2[0]);
				}
				if (split.length > 1) {
					build = split2[1];
				}
			}
		}
	}

	public final static String getVersion()
	{
		return VERSION;
	}

	public final static String getProjectName()
	{
		return projectName;
	}

	public final static String getBuilderName()
	{
		return builderName;
	}

	public final static String getDistributionName()
	{
		return projectName + "_" + getVersion();
	}

	public final static String getVersionDate()
	{
		return VERSION + " " + buildDate;
	}

	public final static String getRepositoryTag()
	{
		return REPOSITORY_TAG;
	}

	public static Manifest getManifest(Class<?> cl)
	{
		InputStream inputStream = null;
		try {
			URLClassLoader classLoader = (URLClassLoader) cl.getClassLoader();
			String classFilePath = cl.getName().replace('.', '/') + ".class";
			URL classUrl = classLoader.getResource(classFilePath);
			if (classUrl == null)
				return null;
			String classUri = classUrl.toString();
			if (!classUri.startsWith("jar:"))
				return null;
			int separatorIndex = classUri.lastIndexOf('!');
			if (separatorIndex <= 0)
				return null;
			String manifestUri = classUri.substring(0, separatorIndex + 2) + "META-INF/MANIFEST.MF";
			URL url = new URL(manifestUri);
			inputStream = url.openStream();
			return new Manifest(inputStream);
		} catch (Throwable e) {
			// handle errors
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable e) {
					// ignore
				}
			}
		}
	}

	public final static void main(String args[])
	{
		System.out.println("       Project: " + getProjectName());
		System.out.println("       Version: " + getVersionDate());
		System.out.println("Repository Tag: " + getRepositoryTag());
		System.out.println("      Built by: " + getBuilderName());
	}
}
