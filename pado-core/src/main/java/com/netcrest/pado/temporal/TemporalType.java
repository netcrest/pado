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
 * TemporalType provides temporal type information that can be sent to clients
 * and tools to understand the temporal configuration for a given full path.
 * 
 * @author dpark
 * 
 */
public abstract class TemporalType implements Comparable<TemporalType>
{
	/**
	 * Full path
	 */
	protected String fullPath;

	/**
	 * Fully-qualified identity key class name
	 */
	protected String identityKeyClassName;

	/**
	 * Fully-qualified KeyType class name
	 */
	protected String keyTypeClassName;

	/**
	 * Fully-qualified data class name
	 */
	protected String dataClassName;

	/**
	 * Temporal enabled flag. Default: false
	 */
	protected boolean temporalEnabled;

	/**
	 * Constructs an empty TemoralType object
	 */
	public TemporalType()
	{
	}

	/**
	 * Constructs a TemporalType object with the specified parameters.
	 * 
	 * @param fullPath
	 *            Full path
	 * @param identityKeyClassName
	 *            Fully-qualified identity key class name
	 * @param keyType
	 *            Fully-qualified KeyType class name. May be nulll if the data
	 *            class is not of KeyMap.
	 * @param dataClassName
	 *            Fully-qualified data class name
	 * @param temporalEnabled
	 *            true to enable temporal data
	 */
	public TemporalType(String fullPath, String identityKeyClassName, String keyTypeClassName, String dataClassName,
			boolean temporalEnabled)
	{
		this.fullPath = fullPath;
		this.identityKeyClassName = identityKeyClassName;
		this.keyTypeClassName = keyTypeClassName;
		this.dataClassName = dataClassName;
		this.temporalEnabled = temporalEnabled;
	}

	/**
	 * Returns the full path to the temporal data
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * Sets the full path to the temporal data
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public void setFullPath(String fullPath)
	{
		this.fullPath = fullPath;
	}

	/**
	 * Returns the fully-qualifed identity key class name.
	 */
	public String getIdentityKeyClassName()
	{
		return identityKeyClassName;
	}

	/**
	 * Sets the fully-qualified identity key class name.
	 * 
	 * @param identityKeyClassName
	 *            Fully qualified identity key class name
	 */
	public void setIdentityKeyClassName(String identityKeyClassName)
	{
		this.identityKeyClassName = identityKeyClassName;
	}

	/**
	 * Returns the key type class name. If it returns null then the data class
	 * is not of KeyMap.
	 */
	public String getKeyTypeClassName()
	{
		return keyTypeClassName;
	}

	/**
	 * Sets the key type class name.
	 * 
	 * @param keyTypeClassName
	 *            KeyType class name.
	 */
	public void setKeyTypeClassName(String keyTypeClassName)
	{
		this.keyTypeClassName = keyTypeClassName;
	}

	/**
	 * Returns the fully-qualifed data class name.
	 */
	public String getDataClassName()
	{
		return dataClassName;
	}

	/**
	 * Sets the fully-qualified data class name.
	 * 
	 * @param dataClassName
	 *            Fully qualified data class name
	 */
	public void setDataClassName(String dataClassName)
	{
		this.dataClassName = dataClassName;
	}

	/**
	 * Returns true if temporal data is enabled.
	 */
	public boolean isTemporalEnabled()
	{
		return temporalEnabled;
	}

	/**
	 * Enables or disables the temporal data.
	 * 
	 * @param temporalEnabled
	 *            true to enable, false to disable
	 */
	public void setTemporalEnabled(boolean temporalEnabled)
	{
		this.temporalEnabled = temporalEnabled;
	}

	/**
	 * Compares the full path.
	 * 
	 * @param otherObject
	 *            A TemporalType object to compare
	 */
	public int compareTo(TemporalType otherObject)
	{
		if (fullPath == null) {
			return -1;
		}
		if (otherObject.fullPath == null) {
			return 1;
		}
		return this.fullPath.compareTo(otherObject.fullPath);
	}
}
