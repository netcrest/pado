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
package com.netcrest.pado.internal.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.log.Logger;


/**
 * Given (1) URLs to set of Jar Files and (2) package prefixes to include this class
 * will determine the IBiz classes that are qualified to be hot deployable
 *
 */
public class HotDeploymentBizClasses {
	
	private String[] keyTypeNames = new String[0];
	private String[] bizNames = new String[0];
	private boolean isKeyTypeSuffixFound = false;
	private URL [] jarFiles;
	private String [] packageRoots;
	private Reflections r;
	private ClassLoader classLoader;
	
	/**
	 * Constructor
	 * @param jarFiles
	 * @param packageRoots space separated list of package prefixes
	 */
	public HotDeploymentBizClasses(URL[] jarFiles, String packageRoots) {
		this(jarFiles,packageRoots.split(" "));
	}
	
	/**
	 * Constructor
	 * @param jarFiles
	 * @param packageName
	 */	
	public HotDeploymentBizClasses(URL[] jarFiles, String [] packageRoots) {
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
		r = new Reflections(cb);
		
		// KeyType
		Set<String> keyTypeNamesSet = r.getStore().getSubTypesOf("com.netcrest.pado.data.KeyType");
		for (String keyTypeName : keyTypeNamesSet) {
			if (keyTypeName.matches(".*_v\\d++$")) {
				isKeyTypeSuffixFound = true;
				break;
			}
		}
		keyTypeNames = keyTypeNamesSet.toArray(new String[keyTypeNamesSet.size()]);		
		
		// BizClass
		Set<String> bizNamesSet = r.getStore().getTypesAnnotatedWith(BizClass.class.getName());
		bizNames = bizNamesSet.toArray(new String[bizNamesSet.size()]);	
	}
	
	/**
	 * BizClass names that are qualified to be hot deployable
	 * @return
	 */
	public String [] getBizNames() {
		return bizNames;
	}

	/**
	 * IBiz classes Found?
	 * @return true or false
	 */
	public boolean isBizFound() {
		return bizNames.length > 0;
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
	
	/**
	 * Returns reflections
	 */
	public Reflections getReflections()
	{
		return r;
	}
	
	/**
	 * Sets the class loader that loaded the hot-deployed classes
	 * @param classLoader Class loader
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
	
	/**
	 * Returns the class loader that hot-deployed classes.
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	public static void main(String [] args) {
		
		URL[] urls = null;
		URL[] urls2 = null;
		try {
			urls = new URL [] {new URL("file:/C:/Work/Netcrest/projects/pado-dash/tmp/biz-test.jar")};
			urls2 = new URL [] {new URL("file:/C:/Work/Netcrest/projects/pado-dash/tmp/temporal-test.jar")};
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		HotDeploymentBizClasses funcs = 
				new HotDeploymentBizClasses(urls,"junk com junk");
		
		for (String s : funcs.getBizNames()) {
			System.out.println("Hot Deployable Pado Biz: " + s);
		}
		
		HotDeploymentBizClasses funcs2 = 
				new HotDeploymentBizClasses(urls2,"junk com junk");
		
		for (String s : funcs2.getKeyTypes()) {
			System.out.println("Hot Deployable Pado Biz: " + s);
		}
	}
}
