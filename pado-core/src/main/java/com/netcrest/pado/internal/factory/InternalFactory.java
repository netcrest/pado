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
package com.netcrest.pado.internal.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.TemporalClientMetadata;

/**
 * InternalFactory creates internal objects used by Pado. It adheres to the
 * static delegation pattern.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class InternalFactory
{
	private static InternalFactory internalFactory;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_INTERNAL_FACTORY,
					Constants.DEFAULT_CLASS_INTERNAL_FACTORY);
			Method method = clazz.getMethod("getInternalFactory");
			try {
				internalFactory = (InternalFactory) method.invoke(null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			Logger.severe("InternalFactory creation error", e);
		}
	}

	/**
	 * Returns the singleton InternalFactory object that delegates static method
	 * calls to the underlying data grid factory implementation object.
	 */
	public static InternalFactory getInternalFactory()
	{
		return internalFactory;
	}

	/**
	 * Creates a GridService object.
	 * 
	 * @param gridId
	 *            Grid ID
	 * @param appId
	 *            App ID
	 * @param credentials
	 *            Properties containing crendential information
	 * @param token
	 *            Session token
	 * @param isGridParent
	 *            true if the grid is a parent to other grids
	 */
	public GridService createGridService(String gridId, String appId, Properties credentials, Object token,
			String username, boolean isGridParent)
	{
		return internalFactory.createGridService(gridId, appId, credentials, token, username, isGridParent);
	}

	/**
	 * Creates a TemporalClientMetadata object.
	 * 
	 * @param temporalName
	 *            Temporal name
	 * @param temporalKeyClassName
	 *            Temporal key class name
	 * @param temporalValueClassName
	 *            Temporal value class name
	 * @param temporalDataClassName
	 *            Temporal data class name
	 */
	public TemporalClientMetadata createTemporalClientMetadata(String temporalName, String temporalKeyClassName,
			String temporalValueClassName, String temporalDataClassName)
	{
		return internalFactory.createTemporalClientMetadata(temporalName, temporalKeyClassName, temporalValueClassName,
				temporalDataClassName);
	}
	
	/**
	 * Returns the default temporal client metadata specific to the underlying
	 * data grid product.
	 */
	public abstract TemporalClientMetadata getDefaultTemporalClientMetadata();

}
