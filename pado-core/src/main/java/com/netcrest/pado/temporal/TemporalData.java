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
 * TemporalData is a temporal wrapper class that contains the value only and
 * does not contain attachments. This class transforms non-temporal data classes
 * into temporal data classes. The value is always kept serialized in the
 * server. It is deserialized when {@link #getValue()} is invoked.
 * 
 * @author dpark
 */
public abstract class TemporalData<K, V>
{
	/**
	 * Temporal value object
	 */
	protected ITemporalValue<K> temporalValue;

	/**
	 * The actual value (or data) object
	 */
	protected V value;

	/**
	 * Constructs an empty TemporalData object.
	 */
	public TemporalData()
	{
	}

	/**
	 * Returns the temporal value object. This method is for internal use only.
	 */
	public ITemporalValue<K> __getTemporalValue()
	{
		return temporalValue;
	}

	/**
	 * Sets the temporal value object. This method is for internal use only.
	 * 
	 * @param temporalValue
	 */
	public void __setTemporalValue(ITemporalValue<K> temporalValue)
	{
		this.temporalValue = temporalValue;
	}

	/**
	 * Return the value object.
	 */
	public V getValue()
	{
		deserializeData();
		return value;
	}

	/**
	 * Deserializes the temporal value object
	 */
	private void deserializeData()
	{
		if (temporalValue != null) {
			temporalValue.deserializeData();
		}
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString()
	{
		return new StringBuffer(40).append("TemporalData[value=").append(value).append("]").toString();
	}
}
