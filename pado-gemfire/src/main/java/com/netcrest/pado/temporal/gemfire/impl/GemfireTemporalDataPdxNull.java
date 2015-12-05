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
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.gemfire.ITemporalPdxSerializable;
import com.netcrest.pado.temporal.gemfire.TemporalDataPdxNull;

/**
 * TemporalDataNull used as a null value in temporal regions.
 * @author dpark
 *
 */
public class GemfireTemporalDataPdxNull<K, V> extends TemporalDataPdxNull<K, V> implements ITemporalPdxSerializable<K>, PdxSerializable
{
	private static final long serialVersionUID = 1L;

	public GemfireTemporalDataPdxNull()
	{
	}
	
	public GemfireTemporalDataPdxNull(ITemporalKey<K> temporalKey, V value)
	{
		this.temporalValue = new GemfireTemporalValue<K>(temporalKey, this);
	}
	
	public GemfireTemporalDataPdxNull(GemfireTemporalDataPdxNull<K, V> data, LinkedList<byte[]> deltaList)
	{
		temporalValue = new GemfireTemporalValue<K>(this, data, deltaList);
	}
	
	public void toData(PdxWriter writer)
	{
		writer.writeObject("temporalValue", temporalValue);
	}

	@SuppressWarnings("unchecked")
	public void fromData(PdxReader reader)
	{
		temporalValue = (ITemporalValue<K>)reader.readObject("temporalValue");
		if (temporalValue != null) {
			temporalValue.setData(this);
		}
	}

	public void readTemporal(PdxReader pdxReader) throws IOException, ClassNotFoundException
	{
	}

	public void writeTemporal(PdxWriter pdxWriter) throws IOException
	{
	}
}
