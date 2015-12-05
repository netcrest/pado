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
package com.netcrest.pado.temporal;

import java.lang.reflect.InvocationTargetException;

/**
 * TemporalListFactory extends {@link TemporalClientFactory}
 * to create {@link ITemporalList} objects that are specific to the underlying
 * data grids.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class TemporalListFactory extends TemporalClientFactory
{
	/**
	 * Temporal list implementation class
	 */
	private Class temporalListClass;

	/**
	 * Returns the TemporalListFactory object initialized with the default data
	 * grid.
	 * 
	 * @throws ClassNotFoundException
	 *             Thrown if any of the default temporal classes are undefined
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws NoSuchMethodException
	 *             Thrown if any of the default temporal class constructors does
	 *             not exist
	 */
	public static TemporalListFactory getTemporalListFactory() throws ClassNotFoundException, SecurityException,
			NoSuchMethodException
	{
		return getTemporalListFactory("temporal", "com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalList");
	}

	/**
	 * Returns the TemporalListFactory object pertaining to the specified
	 * temporal list.
	 * 
	 * @param temporalList
	 *            Temporal list
	 */
	public static TemporalListFactory getTemporalListFactory(ITemporalList temporalList)
	{
		if (temporalList == null) {
			return null;
		}
		return (TemporalListFactory) temporalClientFactoryMap.get(temporalList.getName());
	}

	/**
	 * Returns the TemporalListFactory instance pertaining to the specified
	 * temporal name and the temporal class names.
	 * 
	 * @param temporalName
	 *            Temporal name
	 * @param temporalKeyClassName
	 *            Fully-qualified temporal key class name
	 * @param temporalValueClassName
	 *            Fully-qualified temporal value class name
	 * @param temporalDataClassName
	 *            Fully-qualified data class name
	 * @param temporalListClassName
	 *            Fully-qualified temporal list class name
	 * @throws ClassNotFoundException
	 *             Thrown if any of the specified class names is undefined
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws NoSuchMethodException
	 *             Thrown if the constructor of any of the classes does not
	 *             exist
	 */
	public static TemporalListFactory getTemporalListFactory(String temporalName, String temporalKeyClassName,
			String temporalValueClassName, String temporalDataClassName, String temporalListClassName)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException
	{
		TemporalListFactory temporalListFactory = (TemporalListFactory) temporalClientFactoryMap.get(temporalName);
		if (temporalListFactory == null) {
			temporalListFactory = new TemporalListFactory(temporalName, temporalKeyClassName, temporalValueClassName,
					temporalDataClassName, temporalListClassName);
			temporalClientFactoryMap.put(temporalName, temporalListFactory);
		}
		return temporalListFactory;
	}

	/**
	 * Constructs a new TemporalListFactory instance pertaining to the specified
	 * temporal name and temporal classes.
	 * 
	 * @param temporalName
	 *            Temporal name
	 * @param temporalKeyClassName
	 *            Fully-qualified temporal key class name
	 * @param temporalValueClassName
	 *            Fully-qualified temporal value class name
	 * @param temporalDataClassName
	 *            Fully-qualified data class name
	 * @param temporalListClassName
	 *            Fully-qualified temporal list class name
	 * @throws ClassNotFoundException
	 *             Thrown if any of the specified class names is undefined
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws NoSuchMethodException
	 *             Thrown if the constructor of any of the classes does not
	 *             exist
	 */
	protected TemporalListFactory(String temporalName, String temporalKeyClassName, String temporalValueClassName,
			String temporalDataClassName, String temporalListClassName) throws ClassNotFoundException,
			SecurityException, NoSuchMethodException
	{
		super(temporalName, temporalKeyClassName, temporalValueClassName, temporalDataClassName);

		// ITemporalList
		temporalListClass = Class.forName(temporalListClassName);
		// check to see if the default constructor exists
		temporalListClass.getConstructor(String.class, Object.class, String.class);
	}

	/**
	 * Constructs a new ITemporalList instance with the specified parameters.
	 * 
	 * @param temporalName
	 *            Temporal name
	 * @param identityKey
	 *            Identity key
	 * @param fullPath
	 *            Full path
	 * @throws InstantiationException
	 *             Thrown if the key class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the key class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the temporal list class' constructor argument list
	 *             does not conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the temporal list class' constructor throws an
	 *             exception
	 * @throws NoSuchMethodException
	 *             Thrown if the temporal list class' constructor does not exist
	 */
	public ITemporalList createTemporalList(String temporalName, Object identityKey, String fullPath)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException
	{
		return (ITemporalList) temporalListClass.getConstructor(String.class, Object.class, String.class).newInstance(
				temporalName, identityKey, fullPath);
	}
}
