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
package com.netcrest.pado.gemfire.factory;

import com.netcrest.pado.internal.factory.TemporalFactory;
import com.netcrest.pado.temporal.ITemporalDataNull;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientMetadata;
import com.netcrest.pado.temporal.gemfire.ITemporalPdxSerializable;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalClientMetadata;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalDataNull;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalDataPdxNull;

/**
 * GemfireTemporalFactory extends TemporalFactory to provide static delegation
 * service for creating temporal internal objects.
 * 
 * @author dpark
 *
 */
public class GemfireTemporalFactory extends TemporalFactory
{
	private static GemfireTemporalFactory temporalFactory;
	
	private final static String POGO = "pogo";
	private final static String PDX = "pdx";
	
	public static GemfireTemporalFactory getTemporalFactory()
	{
		if (temporalFactory == null) {
			temporalFactory = new GemfireTemporalFactory();
		}
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
		return new GemfireTemporalClientMetadata(temporalName, temporalKeyClassName, temporalValueClassName, temporalDataClassName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalDataNull createTemporalDataNull(ITemporalKey tk, String dataClassType)
	{
		if (dataClassType == PDX) {
			return new GemfireTemporalDataPdxNull(tk, null); 
		} else {
			return new GemfireTemporalDataNull(tk, null);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataClassType(Class dataClass)
	{
		if (isTemporalPdxDataClass(dataClass)) {
			return PDX;
		} else {
			return POGO;
		}
	}
	
	/**
	 * Returns true if the specified class is a PDX data class.
	 * @param dataClass Data class to verify
	 */
	private boolean isTemporalPdxDataClass(Class<?> temporalDataClass)
	{
		boolean isPdxDataClass = false;
		Class dataClass = temporalDataClass;
		while (dataClass != null) {
			Class[] interfaces = dataClass.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				if (interfaces[i] == ITemporalPdxSerializable.class) {
					isPdxDataClass = true;
					break;
				}
			}
			if (isPdxDataClass) {
				dataClass = null;
			} else {
				dataClass = dataClass.getSuperclass();
			}
		}
		return isPdxDataClass;
	}
}
