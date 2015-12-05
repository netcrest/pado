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
package com.netcrest.pado.internal.biz.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizFuture;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizClass.BizStateType;
import com.netcrest.pado.annotation.BizFuture;
import com.netcrest.pado.annotation.BizType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;

/**
 * BizUtil provides IBiz utility convenience methods.
 * 
 * @author dpark
 * 
 */
public class BizUtil
{
	/**
	 * Returns the IBiz name that uniquely represents the specified class.
	 * 
	 * @param ibizClass
	 *            A class annotated with {@link BizClass}.
	 * 
	 * @return Returns {@link BizClass#name()} if defined, otherwise the class
	 *         name. It returns null if the class is not annotated with
	 *         {@link BizClass}.
	 * @param allInterfaces
	 *            If true, it iterates all interfaces that the specified class
	 *            implements. Otherwise, it checks only the specified class.
	 */
	public static String getBizName(Class<?> ibizClass, boolean allInterfaces)
	{
		if (ibizClass == null) {
			return null;
		}
		String id = null;
		BizClass bizClass = ibizClass.getAnnotation(BizClass.class);
		if (bizClass == null) {
			if (allInterfaces) {
				Class<?> interaces[] = ibizClass.getInterfaces();
				for (Class<?> class1 : interaces) {
					bizClass = class1.getAnnotation(BizClass.class);
					if (bizClass != null) {
						id = bizClass.name();
						break;
					}
				}
				if (id == null) {
					id = ibizClass.getSimpleName();
				}
			}
		} else {
			id = bizClass.name();
			if (id.length() == 0) {
				id = ibizClass.getSimpleName();
			}
		}
		return id;
	}

	/**
	 * Returns a map of all methods found in the specified IBiz class. The
	 * returned map contains &lt;Method Name, Method&gt; entries.
	 * 
	 * @param ibizClass
	 * @return null if the specified IBiz class is null, otherwise, a non-null
	 *         Map object.
	 */
	public static Map<String, Method[]> getMethodMap(Class<?> ibizClass)
	{
		if (ibizClass == null) {
			return null;
		}
		Method methods[] = ibizClass.getMethods();
		HashMap<String, Method[]> map = new HashMap(methods.length, 1f);
		for (Method method : methods) {
			Method[] bizMethods = map.get(method.getName());
			if (bizMethods == null) {
				bizMethods = new Method[1];
				bizMethods[0] = method;
				map.put(method.getName(), bizMethods);
			} else {
				Method[] bizMethods2 = new Method[bizMethods.length + 1];
				System.arraycopy(bizMethods, 0, bizMethods2, 0, bizMethods.length);
				bizMethods2[bizMethods.length] = method;
				map.put(method.getName(), bizMethods2);
			}
		}
		return map;
	}

	/**
	 * Returns @{@link BizClass#path()} of the specified biz class. It returns
	 * null if the specified class is null or is not annotated with
	 * {@link BizClass}.
	 * 
	 * @param bizClass
	 * @{@link BizClass} annotated class
	 */
	public static String getPath(Class<?> bizClass)
	{
		if (bizClass == null) {
			return null;
		}
		BizClass bc = bizClass.getAnnotation(BizClass.class);
		if (bc == null) {
			return null;
		}
		return bc.path();
	}

	/**
	 * Returns the @{@link BizClass#bizType()} of the specified biz class. It
	 * returns null if the specified class is null or is not annotated with @
	 * {@link BizClass}.
	 * 
	 * @param bizClass
	 * @{@link BizClass} annotated class
	 */
	public static BizType getBizType(Class<?> bizClass)
	{
		if (bizClass == null) {
			return null;
		}
		BizClass bc = bizClass.getAnnotation(BizClass.class);
		if (bc == null) {
			return null;
		}
		return bc.bizType();
	}

	/**
	 * Returns the @{@link BizClass#bizStateType} of the specified biz class. It
	 * returns null if the specified class is null or is not annotated with @
	 * {@link BizClass}.
	 * 
	 * @param bizClass
	 * @{@link BizClass} annotated class
	 */
	public static BizStateType getBizStateType(Class<?> bizClass)
	{
		if (bizClass == null) {
			return null;
		}
		BizClass bc = bizClass.getAnnotation(BizClass.class);
		if (bc == null) {
			return null;
		}
		return bc.bizStateType();
	}

	/**
	 * Returns the name of the specified biz class. It returns
	 * {@link BizClass#name()} if defined, otherwise, it returns the fully
	 * qualified class name. It returns null if the specified class is null or
	 * is not annotated with @ {@link BizClass}.
	 * 
	 * @param bizClass
	 * @{@link BizClass} annotated class
	 */
	public static String getBizClassName(Class<?> bizClass)
	{
		if (bizClass == null) {
			return null;
		}
		BizClass bc = bizClass.getAnnotation(BizClass.class);
		if (bc == null) {
			return null;
		}
		String bizClassName = bc.name().trim();
		return bizClassName.length() == 0 ? bizClass.getName() : bizClassName;
	}

	/**
	 * Returns the standard implementation class name. It returns null if the
	 * specified class is null or is not annotated with @ {@link BizClass}. All
	 * {@link IBiz} implementation classes must follow the following naming
	 * conventions enforced by this method:
	 * <ul>
	 * <li>The package name must be suffixed with impl.&lt;prefix&gt;, where
	 * &lt;prefix&gt is the underlying data grid product name, i.e., gemfire.</li>
	 * <li>The class name must begin with the interface name without the "I" and
	 * suffixed with "Impl".</li>
	 * </ul>
	 * For example, given com.foo.IPortfolioBiz, the implementation class must
	 * be com.foo.impl.gemfire.PorfolioBizImpl.
	 * <p>
	 * 
	 * @param ibizClass
	 *            {@link IBiz} interface with {@link BizClass} annotation.
	 */
	public static String getBizImplClassName(Class<?> ibizClass)
	{
		if (ibizClass == null) {
			return null;
		}
		BizClass bizClass = (BizClass) ibizClass.getAnnotation(BizClass.class);
		if (bizClass == null) {
			return null;
		}
		String bizImpl = bizClass.remoteImpl().trim();
		if (bizImpl.length() == 0) {
			bizImpl = getCommonBizClassName(ibizClass) + "Impl";
		}
		return bizImpl;
	}

	/**
	 * Returns the fully-qualified common (default) server-side implementation
	 * class name
	 * 
	 * @param ibizClass
	 *            IBiz class
	 */
	private static String getCommonBizClassName(Class<?> ibizClass)
	{
		// Interface with the "I" prefix
		String bizImpl = ibizClass.getSimpleName();
		if (bizImpl.length() >= 2 && bizImpl.startsWith("I") && 65 <= bizImpl.charAt(1) && bizImpl.charAt(1) <= 90) {
			bizImpl = bizImpl.substring(1);
		}
		// Package can be null if the class loader has not created the
		// package object
		String packageImplPrefix = PadoUtil.getProperty(Constants.PROP_DATA_GRID, Constants.DEFAULT_DAGA_GRID);
		if (ibizClass.getPackage() == null) {
			String className = ibizClass.getName();
			String packageName = className.substring(0, className.lastIndexOf('.'));
			bizImpl = packageName + ".impl." + packageImplPrefix + "." + bizImpl;
		} else {
			bizImpl = ibizClass.getPackage().getName() + ".impl." + packageImplPrefix + "." + bizImpl;
		}
		return bizImpl;
	}

	/**
	 * Returns the explicitly annotated local implementation class name. It
	 * returns an empty string if the local implementation class name is not
	 * annotated.
	 * 
	 * @param ibizClass
	 *            {@link IBiz} interface with {@link BizClass} annotation.
	 */
	public static String getExplicitLocalImplClassName(Class<?> ibizClass)
	{
		String bizImpl;
		if (isFuture(ibizClass)) {
			BizFuture bizFuture = (BizFuture) ibizClass.getAnnotation(BizFuture.class);
			bizImpl = bizFuture.localImpl().trim();
		} else {
			BizClass bizClass = (BizClass) ibizClass.getAnnotation(BizClass.class);
			bizImpl = bizClass.localImpl().trim();
		}

		return bizImpl;
	}

	/**
	 * Returns the standard local or future implementation class name. All
	 * {@link IBizLocal} and {@link IBizFuture} implementation classes must
	 * follow the following naming conventions enforced by this method:
	 * <ul>
	 * <li>The package name must be suffixed with impl.&lt;prefix&gt;, where
	 * &lt;prefix&gt is the underlying data grid product name, i.e., gemfire.</li>
	 * <li>For {@link IBizLocal}, the class name must begin with the interface
	 * name without the "I" and suffixed with "ImplLocal".</li>
	 * <li>For {@link IBizFuture}, the class name must begin with the interface
	 * name without the "I" and suffixed with "ImplFuture".</li>
	 * </ul>
	 * For example, given com.foo.IPortfolioBiz, the local implementation class
	 * must be com.foo.impl.gemfire.PorfolioBizImplLocal and the future
	 * implemation class must be com.foo.impl.gemfire.PorfolioBizImplFuture.
	 * 
	 * @param ibizClass
	 *            Class name
	 */
	public static String getImplicitBizLocalImplClassName(Class<?> ibizClass)
	{
		String bizImpl;
		if (isFuture(ibizClass)) {
			BizFuture bizFuture = (BizFuture) ibizClass.getAnnotation(BizFuture.class);
			bizImpl = bizFuture.localImpl().trim();
			if (bizImpl.length() == 0) {
				bizImpl = getCommonBizClassName(ibizClass) + "ImplFuture";
			}
		} else {
			BizClass bizClass = (BizClass) ibizClass.getAnnotation(BizClass.class);
			bizImpl = bizClass.localImpl().trim();
			if (bizImpl.length() == 0) {
				bizImpl = getCommonBizClassName(ibizClass) + "ImplLocal";
			}
		}

		return bizImpl;
	}

	/**
	 * Returns true if the specified class name ends with the suffix, "Future".
	 * 
	 * @param ibizClassName
	 *            Class name
	 */
	public static boolean isFuture(String ibizClassName)
	{
		return ibizClassName == null ? false : ibizClassName.endsWith("Future");
	}

	/**
	 * Returns the IBiz class name for the specified future class name. It
	 * assumes all Future class names end with the suffix, "Future". It
	 * essentially returns the class name without the Future suffix. It returns
	 * null if className is null. It returns the same className value if
	 * className does not end with the "Future" suffix.
	 * 
	 * @param className
	 *            The future class name.
	 */
	public static String getBizInterfaceNameOfFuture(String className)
	{
		if (className == null) {
			return null;
		}
		int index = className.lastIndexOf("Future");
		if (index == -1) {
			return className;
		}
		return className.substring(0, index + 1);
	}

	/**
	 * Returns true if the specified class is an {@link IBizFuture} class.
	 * 
	 * @param theClass
	 * @return
	 */
	public static boolean isFuture(Class<?> theClass)
	{
		return IBizFuture.class.isAssignableFrom(theClass);
	}

	/**
	 * Returns @{@link BizFuture#bizInterface} of the specified biz future
	 * class. It returns null if the specified class is null or is not annotated
	 * with @{@link BizFuture}.
	 * 
	 * @param theFutureClass
	 * @{@link BizFuture} class
	 * @return
	 */
	public static String getBizInterfaceName(Class<?> futureClass)
	{
		if (futureClass == null) {
			return null;
		}
		BizFuture fc = (BizFuture) futureClass.getAnnotation(BizFuture.class);
		if (fc == null) {
			return null;
		}
		return fc.bizInterface().trim();
	}
}
