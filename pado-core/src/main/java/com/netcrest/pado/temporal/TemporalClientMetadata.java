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

/**
 * TemporalClientMetadata contains temporal metadata required by clients to
 * properly initialize the temporal API. The metadata is retrieved from the grid
 * during the temporal API initialization time.
 * 
 * @author dpark
 * 
 */
public abstract class TemporalClientMetadata
{
	/**
	 * Unique temporal name representing an instance of TemporalClientFactory
	 */
	protected String temporalName;

	/**
	 * Fully-qualified temporal key class name
	 */
	protected String temporalKeyClassName;

	/**
	 * Fully-qualified temporal value class name
	 */
	protected String temporalValueClassName;

	/**
	 * Fully-qualified temporal data class name
	 */
	protected String temporalDataClassName;

	/**
	 * Constructs an empty TemporalClientMetadata object
	 */
	public TemporalClientMetadata()
	{
	}

	/**
	 * Constructs a TemporalClientMetatdata with the specified parameters.
	 * 
	 * @param temporalName
	 *            Unique temporal name representing an instance of
	 *            TemporalClientFactory
	 * @param temporalKeyClassName
	 *            Fully-qualified temporal key class name
	 * @param temporalValueClassName
	 *            Fully-qualified temporal value class name
	 * @param temporalDataClassName
	 *            Fully-qualified temporal data class name
	 */
	public TemporalClientMetadata(String temporalName, String temporalKeyClassName, String temporalValueClassName,
			String temporalDataClassName)
	{
		this.temporalName = temporalName;
		this.temporalKeyClassName = temporalKeyClassName;
		this.temporalValueClassName = temporalValueClassName;
		this.temporalDataClassName = temporalDataClassName;
	}

	/**
	 * Returns the unique temporal name representing an instance of
	 * TemporalClientFactory
	 */
	public String getTemporalName()
	{
		return temporalName;
	}

	/**
	 * Sets the unique temporal name representing an instance of
	 * TemporalClientFactory
	 * 
	 * @param temporalName
	 *            Temporal name
	 */
	public void setTemporalName(String temporalName)
	{
		this.temporalName = temporalName;
	}

	/**
	 * Returns the full-qualified temporal key class name.
	 */
	public String getTemporalKeyClassName()
	{
		return temporalKeyClassName;
	}

	/**
	 * Sets the fully-qualified temporal key class name.
	 * 
	 * @param temporalKeyClassName
	 *            Fully-qualified key class name
	 */
	public void setTemporalKeyClassName(String temporalKeyClassName)
	{
		this.temporalKeyClassName = temporalKeyClassName;
	}

	/**
	 * Returns the full-qualified temporal value class name.
	 */
	public String getTemporalValueClassName()
	{
		return temporalValueClassName;
	}

	/**
	 * Sets the fully-qualified temporal value class name.
	 * 
	 * @param temporalValueClassName
	 *            Fully-qualified value class name
	 */
	public void setTemporalValueClassName(String temporalValueClassName)
	{
		this.temporalValueClassName = temporalValueClassName;
	}

	/**
	 * Returns the full-qualified temporal data class name.
	 */
	public String getTemporalDataClassName()
	{
		return temporalDataClassName;
	}

	/**
	 * Sets the fully-qualified temporal data class name.
	 * 
	 * @param temporalDataClassName
	 *            Fully-qualified data class name
	 */
	public void setTemporalDataClassName(String temporalDataClassName)
	{
		this.temporalDataClassName = temporalDataClassName;
	}
}
