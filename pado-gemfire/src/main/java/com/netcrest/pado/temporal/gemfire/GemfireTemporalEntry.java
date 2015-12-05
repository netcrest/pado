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
package com.netcrest.pado.temporal.gemfire;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

public class GemfireTemporalEntry<K, V> extends TemporalEntry<K, V> implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	public GemfireTemporalEntry()
	{}
	
	public GemfireTemporalEntry(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		super(tkey, data);
	}
	
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		tkey = DataSerializer.readObject(input);
		data = DataSerializer.readObject(input);
	}

	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeObject(tkey, output);
		DataSerializer.writeObject(data, output);
	}

}
