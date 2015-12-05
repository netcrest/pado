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

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalDataNull;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientMetadata;

/**
 * TemporalFactory creates temporal data objects. It adheres to the static
 * delegation pattern.
 * 
 * @author dpark
 *
 */
public abstract class TemporalFactory
{
	private static TemporalFactory temporalFactory;
	
	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_TEMPORAL_FACTORY, Constants.DEFAULT_CLASS_TEMPORAL_FACTORY);

			Method method = clazz.getMethod("getTemporalFactory");
			try {
				temporalFactory = (TemporalFactory) method.invoke(null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			Logger.severe("TemporalFactory creation error", e);
		}
	}
	
	/**
	 * Returns the singleton TemporalFactory object that delegates static method
	 * calls to the underlying data grid factory implementation object.
	 */
	public static TemporalFactory getTemporalFactory()
	{
		return temporalFactory;
	}

	/**
	 * Creates a TemporalClientMetadata object.
	 * @param temporalName Temporal name
	 * @param temporalKeyClassName Temporal key class name
	 * @param temporalValueClassName Temporal value class name
	 * @param temporalDataClassName Temporal data class name
	 */
	public TemporalClientMetadata createTemporalClientMetadata(String temporalName, String temporalKeyClassName, String temporalValueClassName, String temporalDataClassName)
	{
		return temporalFactory.createTemporalClientMetadata(temporalName, temporalKeyClassName, temporalValueClassName, temporalDataClassName);
	}
	
	/**
	 * Returns the data class type that determines the temporal null type.
	 * @param dataClass Data class
	 */
	public String getDataClassType(Class dataClass)
	{
		return temporalFactory.getDataClassType(dataClass);
	}
	
	/**
	 * Creates a TemplralDataNull object.
	 */
	public ITemporalDataNull createTemporalDataNull(ITemporalKey tk, String classType)
	{
		return temporalFactory.createTemporalDataNull(tk, classType);
	}
}
