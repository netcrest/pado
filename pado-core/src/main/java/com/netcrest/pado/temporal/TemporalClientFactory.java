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
import java.util.HashMap;
import java.util.LinkedList;

import com.netcrest.pado.internal.factory.InternalFactory;
import com.netcrest.pado.internal.factory.TemporalFactory;

/**
 * A TemporalClientFactory is a named factory object that creates ITemporalValue
 * and ITemporalData objects that may be specific to applications and the
 * underlying data grid. Each instance of TemporalClientFactory may have a
 * unique set of temporal classes, i.e., ITemporalKey, ITemporalValue, and
 * ITemporalData, that may store temporal data in different ways to satisfy
 * business requirements and/or optimize the data store.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Key object
 * @param <V>
 *            Data object
 */
public class TemporalClientFactory<K, V>
{
	protected final static HashMap<String, TemporalClientFactory> temporalClientFactoryMap = new HashMap<String, TemporalClientFactory>(
			5);

	/**
	 * Client metadata required by clients to initialize the temporal API before
	 * accessing the grid.
	 */
	protected TemporalClientMetadata clientMetadata;

	/**
	 * Temporal key class that implements ITemporalKey
	 */
	protected Class<?> temporalKeyClass;

	/**
	 * Temporal value class that implements ITemporalValue
	 */
	protected Class<?> temporalValueClass;

	/**
	 * Temporal data class that implements ITemporalData
	 */
	protected Class<?> temporalDataClass;

	/**
	 * Data class type, i.e., "pogo", "pdx", etc.
	 */
	private String dataClassType;

	/**
	 * Returns the default singleton TemporalClientFactory object with the
	 * temporal name, "temporal".
	 * 
	 * @throws ClassNotFoundException
	 *             Thrown if the default temporal classes are not found
	 */
	public static TemporalClientFactory getTemporalClientFactory() throws ClassNotFoundException
	{
		return getTemporalClientFactory("temporal", "com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue", "com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData");
	}

	/**
	 * Returns the TemporalClientFactory instance pertaining to the specified
	 * temporal name. It returns null if the factory object has not be created
	 * for the specified temporal name.
	 * 
	 * @param temporalName
	 *            TemporalClientFactory name
	 */
	public static TemporalClientFactory getTemporalClientFactory(String temporalName)
	{
		return temporalClientFactoryMap.get(temporalName);
	}

	/**
	 * Returns the TemporalClientFactory object pertaining to the specified
	 * metadata. It creates a new TemporalClientFactory if it does not exist.
	 * 
	 * @param metadata
	 *            Temporal client metadata
	 * @throws ClassNotFoundException
	 *             Thrown if any of the metata specified temporal classes is
	 *             undefined
	 */
	public static TemporalClientFactory getTemporalClientFactory(TemporalClientMetadata metadata)
			throws ClassNotFoundException
	{
		return getTemporalClientFactory(metadata.getTemporalName(), metadata.getTemporalKeyClassName(),
				metadata.getTemporalValueClassName(), metadata.getTemporalDataClassName());
	}

	/**
	 * Returns the TemporalClientFactory object pertaining to the specified
	 * parameters. It creates a new TemporalClientFactory if it does not exist.
	 * 
	 * @param temporalName
	 *            Temporal client metadata
	 * @param temporalKeyClassName
	 *            Fully-qualified temporal key class name
	 * @param temporalValueClassName
	 *            Fully-qualified temporal value class name
	 * @param temporalDataClassName
	 *            Fully-qualified data class name
	 * @throws ClassNotFoundException
	 *             Thrown if any of the specified temporal classes is undefined
	 */
	public static TemporalClientFactory getTemporalClientFactory(String temporalName, String temporalKeyClassName,
			String temporalValueClassName, String temporalDataClassName) throws ClassNotFoundException
	{
		TemporalClientFactory temporalClientFactory = temporalClientFactoryMap.get(temporalName);
		if (temporalClientFactory == null) {
			temporalClientFactory = new TemporalClientFactory(temporalName, temporalKeyClassName,
					temporalValueClassName, temporalDataClassName);
			temporalClientFactoryMap.put(temporalName, temporalClientFactory);
		}
		return temporalClientFactory;
	}

	/**
	 * Constructs a new TemporalClientFactory object pertaining to the specified
	 * parameters.
	 * 
	 * @param temporalName
	 *            Temporal client metadata
	 * @param temporalKeyClassName
	 *            Fully-qualified temporal key class name
	 * @param temporalValueClassName
	 *            Fully-qualified temporal value class name
	 * @param temporalDataClassName
	 *            Fully-qualified data class name
	 * @throws ClassNotFoundException
	 *             Thrown if any of the specified temporal classes is undefined
	 */
	protected TemporalClientFactory(String temporalName, String temporalKeyClassName, String temporalValueClassName,
			String temporalDataClassName) throws ClassNotFoundException
	{
		clientMetadata = InternalFactory.getInternalFactory().createTemporalClientMetadata(temporalName,
				temporalKeyClassName, temporalValueClassName, temporalDataClassName);

		// ITemporalKey
		temporalKeyClass = Class.forName(temporalKeyClassName);
		// ITemporalValue
		temporalValueClass = Class.forName(temporalValueClassName);
		// ITemporalData
		temporalDataClass = Class.forName(temporalDataClassName);

		dataClassType = TemporalFactory.getTemporalFactory().getDataClassType(temporalDataClass);
	}

	/**
	 * Returns the temporal client metadata. The returned object can be sent
	 * over the network.
	 */
	public TemporalClientMetadata getClientMetadata()
	{
		return clientMetadata;
	}

	/**
	 * Returns a new ITemporalKey instance with the specified parameters.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param startValidTime
	 *            Start valid time in msec
	 * @param endValidTime
	 *            End valid time in msec
	 * @param writtenTime
	 *            Written time in msec
	 * @param username
	 *            User name
	 * 
	 * @throws InstantiationException
	 *             Thrown if the key class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the key class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the key class' constructor argument list does not
	 *             conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the key class' constructor throws an exception
	 * @throws NoSuchMethodException
	 *             Thrown if the key class' constructor does not exist
	 */
	public ITemporalKey<K> createTemporalKey(K identityKey, long startValidTime, long endValidTime, long writtenTime,
			String username) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			SecurityException, InvocationTargetException, NoSuchMethodException
	{
		return (ITemporalKey<K>) temporalKeyClass.getConstructor(Object.class, long.class, long.class, long.class,
				String.class).newInstance(identityKey, startValidTime, endValidTime, writtenTime, username);
	}

	/**
	 * Returns a new empty ITemporalValue instance.
	 * 
	 * @throws InstantiationException
	 *             Thrown if the value class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the value class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the key class' constructor argument list does not
	 *             conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the value class' constructor throws an exception
	 * @throws NoSuchMethodException
	 *             Thrown if the value class' constructor does not exist
	 */
	public ITemporalValue<K> createTemporalValue() throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException
	{
		return (ITemporalValue<K>) temporalValueClass.newInstance();
	}

	/**
	 * Returns a new ITemporalValue instance with the specified parameters.
	 * 
	 * @param data
	 *            Temporal data object
	 * @param tkey
	 *            Temporal key object
	 * @throws InstantiationException
	 *             Thrown if the value class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the value class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the value class' constructor argument list does not
	 *             conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the value class' constructor throws an exception
	 * @throws NoSuchMethodException
	 *             Thrown if the value class' constructor does not exist
	 */
	public ITemporalValue<K> createTemporalValue(ITemporalData data, ITemporalKey<K> tkey)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException
	{
		return (ITemporalValue<K>) temporalValueClass.getConstructor(ITemporalData.class, ITemporalKey.class)
				.newInstance(data, tkey);
	}

	/**
	 * Returns a new ITemporalValue object by configuring with the specified
	 * data, copyFromData and deltaList objects.
	 * 
	 * @param newData
	 *            Temporal data object
	 * @param copyFromData
	 *            Temporal data object from which attributes are copied
	 * @param deltaList
	 *            Delta list
	 * @throws InstantiationException
	 *             Thrown if the value class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the value class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the value class' constructor argument list does not
	 *             conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the value class' constructor throws an exception
	 * @throws NoSuchMethodException
	 *             Thrown if the value class' constructor does not exist
	 */
	public ITemporalValue createTemporalValue(ITemporalData newData, ITemporalData copyFromData,
			LinkedList<byte[]> deltaList) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException
	{
		return (ITemporalValue) temporalValueClass.getConstructor(ITemporalData.class, ITemporalData.class,
				LinkedList.class).newInstance(newData, copyFromData, deltaList);
	}

	/**
	 * Returns a new ITemporalData object with the specified temporal key and
	 * value.
	 * 
	 * @param tkey
	 *            Temporal key
	 * @param value
	 *            Value
	 * @throws InstantiationException
	 *             Thrown if the data class that declares the underlying
	 *             constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the data class constructor object enforces Java
	 *             language access control and the underlying constructor is
	 *             inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the data class' constructor argument list does not
	 *             conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the data class' constructor throws an exception
	 * @throws NoSuchMethodException
	 *             Thrown if the data class' constructor does not exist
	 */
	public ITemporalData<K> createTemporalData(ITemporalKey<K> tkey, V value) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException,
			NoSuchMethodException
	{
		if (value == null) {
			return TemporalFactory.getTemporalFactory().createTemporalDataNull(tkey, dataClassType);
		} else {
			return (ITemporalData<K>) temporalDataClass.getConstructor(ITemporalKey.class, Object.class).newInstance(
					tkey, value);
		}
	}

	/**
	 * Returns a new ITemporalData object that has the specified base and all
	 * deltas applied.
	 * 
	 * @param base
	 *            Base data object
	 * @param deltaList
	 *            Ordered delta list
	 * @throws InstantiationException
	 *             Thrown if the data or value class that declares the
	 *             underlying constructor represents an abstract class
	 * @throws IllegalAccessException
	 *             Thrown if the data or value class constructor object enforces
	 *             Java language access control and the underlying constructor
	 *             is inaccessible
	 * @throws IllegalArgumentException
	 *             Thrown if the data or value class' constructor argument list
	 *             does not conform
	 * @throws SecurityException
	 *             Thrown if constructor invocation violates security
	 * @throws InvocationTargetException
	 *             Thrown if the data or value class' constructor throws an
	 *             exception
	 * @throws NoSuchMethodException
	 *             Thrown if the data or value class' constructor does not exist
	 */
	public ITemporalData createTemporalData(ITemporalData base, LinkedList<byte[]> deltaList)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException
	{
		if (deltaList == null) {
			return base;
		}
		ITemporalData newData;
		if (base instanceof TemporalData == false) {
			newData = base.getClass().newInstance();
		} else {
			newData = (ITemporalData) temporalDataClass.newInstance();
		}
		// ITemporalValue temporalValue = createTemporalValue(newData,
		// base.__getTemporalValue().getTemporalKey());
		ITemporalValue temporalValue = createTemporalValue(newData, base, deltaList);
		newData.__setTemporalValue(temporalValue);
		return newData;
	}

}
