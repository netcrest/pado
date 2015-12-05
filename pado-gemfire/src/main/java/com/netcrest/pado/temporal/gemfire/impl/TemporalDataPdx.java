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
package com.netcrest.pado.temporal.gemfire.impl;

import java.io.IOException;
import java.util.LinkedList;

import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.gemfire.ITemporalPdxSerializable;

/**
 * TemporalDataPdx is a temporal wrapper class that contains the value only 
 * and does not contain attachments. This class transforms non-temporal 
 * data classes into temporal data classes. The value is always kept serialized
 * in the server. It is deserialized when getValue() is invoked.
 * <p>
 * <b>TemporalDataPdx is not currently supported.</b>
 * @author dpark
 */
public class TemporalDataPdx<K, V> implements ITemporalPdxSerializable<K>
{
	private static final long serialVersionUID = 1L;

	private ITemporalValue<K> temporalValue;
	private V value;

	public TemporalDataPdx()
	{
	}
	
	public TemporalDataPdx(ITemporalKey<K> temporalKey, V value)
	{
		this.temporalValue = new TemporalValuePdx<K>(this, temporalKey);
		this.value = value;
	}
	
	public TemporalDataPdx(TemporalDataPdx<K, V> data, LinkedList<byte[]> deltaList)
	{
		temporalValue = new TemporalValuePdx<K>(this, data, deltaList);
	}
	
	public ITemporalValue<K> __getTemporalValue()
	{
		return temporalValue;
	}

	public void __setTemporalValue(ITemporalValue<K> temporalValue)
	{
		this.temporalValue = temporalValue;
	}
	
	public V getValue()
	{
		deserializeData();
		return value;
	}
	
	private void deserializeData()
	{
		if (temporalValue != null) {
			temporalValue.deserializeData();
		}
	}
	
	public String toString()
	{
		return new StringBuffer(40).append("TemporalData[value=").append(value).append("]").toString();
	}

	public void toData(PdxWriter writer)
	{
		writer.writeObject("temporalValue", temporalValue);
	}

	@SuppressWarnings("unchecked")
	public void fromData(PdxReader reader)
	{
		temporalValue = (ITemporalValue<K>)reader.readObject("temporalValue");
		temporalValue.setData(this);
		
	}

	@SuppressWarnings("unchecked")
	public void readTemporal(PdxReader pdxReader) throws IOException, ClassNotFoundException
	{
		value = (V)pdxReader.readObject("value");
	}

	public void writeTemporal(PdxWriter pdxWriter) throws IOException
	{
		pdxWriter.writeObject("value", value);
	}
}
