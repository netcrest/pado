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
package com.netcrest.pado.gemfire.util;

import java.io.IOException;

import com.gemstone.gemfire.internal.util.BlobHelper;
import com.netcrest.pado.io.IObjectSerializer;

/**
 * GemfireObjectSerializer serializes objects in the GemFire native wire format.
 * 
 * @author dpark
 * 
 */
public class GemfireSerializer implements IObjectSerializer
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] serialize(Object object) throws IOException
	{
		return BlobHelper.serializeToBlob(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object deserialize(byte[] blob) throws IOException, ClassNotFoundException
	{
		return BlobHelper.deserializeBlob(blob);
	}

	/**
	 * Returns the hash code. Only one instance is allowed in hash lookup and
	 * therefore the hash code is always the same for all instances.
	 */
	@Override
	public int hashCode()
	{
		return getClass().getName().hashCode();
	}

	/**
	 * Only one isntance is allowed in hash lookup and therefore it returns true
	 * if the specified object has the same type.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
