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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;
import com.netcrest.pado.gemfire.util.DataSerializerEx;
import com.netcrest.pado.temporal.TemporalKey;

@SuppressWarnings("rawtypes")
public class GemfireTemporalKey<K> extends TemporalKey<K> implements PartitionResolver, DataSerializable
{
	private static final long serialVersionUID = 1L;

	public GemfireTemporalKey()
	{
	}

	public GemfireTemporalKey(K identityKey, long startValidTime, long endValidTime, long writtenTime, String username)
	{
		super(identityKey, startValidTime, endValidTime, writtenTime, username);
	}
	
	public void toData(DataOutput out) throws IOException
	{
		DataSerializer.writeObject(this.identityKey, out);
		DataSerializer.writeObject(this.routingKey, out);
		out.writeLong(this.writtenTime);
		out.writeLong(this.startValidTime);
		out.writeLong(this.endValidTime);
		DataSerializerEx.writeUTF(username, out);
	}

	public void fromData(DataInput in) throws IOException, ClassNotFoundException
	{
		this.identityKey = DataSerializer.readObject(in);
		this.routingKey = DataSerializer.readObject(in);
		this.writtenTime = in.readLong();
		this.startValidTime = in.readLong();
		this.endValidTime = in.readLong();
		this.username = DataSerializerEx.readUTF(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		toData(out);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		fromData(in);
	}

	public String getName()
	{
		return "TemporalKey";
	}

	public Serializable getRoutingObject(EntryOperation opDetails)
	{
		return (Serializable)identityKey;
	}
	
	public void close()
	{
	}
}