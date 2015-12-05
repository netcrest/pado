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
package com.netcrest.pado.data.jsonlite.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.netcrest.pado.log.Logger;


/**
 * Given (1) URLs to set of Jar Files and (2) package prefixes to include this class
 * will determine the IBiz classes that are qualified to be hot deployable
 *
 */
public class JarIntrospector {
	
	private String[] keyTypeNames = new String[0];
	private boolean isKeyTypeSuffixFound = false;
	private URL [] jarFiles;
	private String [] packageRoots;
//	private final LogWriter logger = CacheFactory.getAnyInstance().getLogger();
	
	/**
	 * Constructor
	 * @param jarFiles
	 * @param packageRoots space separated list of package prefixes
	 */
	public JarIntrospector(URL[] jarFiles, String packageRoots) {
		this(jarFiles,packageRoots.split(" "));
	}
	
	/**
	 * Constructor
	 * @param jarFiles
	 * @param packageName
	 */	
	public JarIntrospector(URL[] jarFiles, String [] packageRoots) {
		this.jarFiles = jarFiles;
		this.packageRoots = packageRoots;
		if (packageRoots.length == 0) {
			// We always expect at least on package root.  Even it is simply "com'
			Logger.warning("No package roots are configured. No IBiz classes will be loaded");
			return;
		}
		FilterBuilder filterBuilder = new FilterBuilder();
		for (String packageRoot : packageRoots) {
			filterBuilder.include(FilterBuilder.prefix(packageRoot));
		}
		
		ConfigurationBuilder cb = new ConfigurationBuilder().filterInputsBy(filterBuilder).setUrls(java.util.Arrays.asList(jarFiles));
		Reflections r = new Reflections(cb);
		
		// KeyType
		Set<String> keyTypeNamesSet = r.getStore().getSubTypesOf("com.netcrest.pado.data.KeyType");
		for (String keyTypeName : keyTypeNamesSet) {
			if (keyTypeName.matches(".*_v\\d++$")) {
				isKeyTypeSuffixFound = true;
				break;
			}
		}
		keyTypeNames = keyTypeNamesSet.toArray(new String[keyTypeNamesSet.size()]);		
	}
	
	/**
	 * KeyType names that are qualified to be hot deployable
	 * @return
	 */
	public String [] getKeyTypes() {
		return keyTypeNames;
	}

	/**
	 * Returns true if KeyTypes are found
	 */
	public boolean isKeyTypesFound()
	{
		return keyTypeNames.length > 0;
	}

	/**
	 * Returns true if one or more KeyType class names end with the
	 * with the "_v#" suffix.
	 */
	public boolean isKeyTypeSuffixFound()
	{
		return isKeyTypeSuffixFound;
	}
	
	/**
	 * Return Jar Files Used
	 * @return jar files used
	 */
	public URL [] getJarFiles() {
		return jarFiles;
	}
	
	/**
	 * Return Package Roots
	 * @return package roots
	 */
	public String [] getPackageRoots() {
		return packageRoots;
	}

	public static void main(String [] args) {
		
		URL[] urls = null;
		URL[] urls2 = null;
		try {
			urls = new URL [] {new File("plugins/portfolio_v1.jar").toURI().toURL()};
			urls2 = new URL [] {new File("plugins/position_v9.jar").toURI().toURL()};
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		JarIntrospector funcs = 
				new JarIntrospector(urls,"junk com junk");
		
		JarIntrospector funcs2 = 
				new JarIntrospector(urls2,"junk com junk");
		
		for (String s : funcs2.getKeyTypes()) {
			System.out.println("Hot Deployable Pado KeyType: " + s);
		}
	}
}
