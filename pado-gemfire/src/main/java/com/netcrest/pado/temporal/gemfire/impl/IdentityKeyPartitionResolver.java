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

import java.util.Properties;
import java.util.regex.Pattern;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;
import com.netcrest.pado.IRoutingKey;
import com.netcrest.pado.biz.file.CompositeKeyInfo;
import com.netcrest.pado.temporal.ITemporalKey;

@SuppressWarnings("rawtypes")
public class IdentityKeyPartitionResolver implements Declarable, PartitionResolver
{
	private int[] routingKeyIndexes;
	private String delimiter = ".";
	private int largestIndex = -1;
	private boolean routingKeyIndexesDefined = false;

	public IdentityKeyPartitionResolver()
	{
	}

	public Object getRoutingObject(EntryOperation opDetails)
	{
		Object key = opDetails.getKey();
		if (key instanceof IRoutingKey) {
			key = ((IRoutingKey) key).getRoutingKey();
		} else if (key instanceof ITemporalKey) {
			key = ((ITemporalKey) key).getIdentityKey();
			key = getRoutingKey(key.toString());
		} else {
			key = getRoutingKey(key.toString());
		}
		return key;
	}

	private String getRoutingKey(String key)
	{
		if (routingKeyIndexesDefined == false) {
			return key;
		}
		
		// Non-regex delimiter
		String tokens[] = key.toString().split(Pattern.quote(delimiter));
		
		// If the number of tokens is less than the number of allowed tokens then
		// return the passed-in key. This enables any key to be used as a routing key.
		if (tokens.length <= routingKeyIndexes.length || tokens.length <= largestIndex) {
			return key;
		}
		StringBuffer buffer = new StringBuffer(20);
		for (int index : routingKeyIndexes) {
			buffer.append(tokens[index]);
		}
		key = buffer.toString();
		return key;
	}

	public void init(Properties p)
	{
	}

	public void close()
	{
	}

	public String getName()
	{
		return "IdentityKeyPartitionResolver";
	}
	
	public void setCompositeKeyInfo(CompositeKeyInfo compositeKeyInfo)
	{
		if (compositeKeyInfo != null) {
			setRoutingKeyIndexs(compositeKeyInfo.getRoutingKeyIndexes());
			setCompositeKeyDelimiter(compositeKeyInfo.getCompositeKeyDelimiter());
		}
	}
	
	public CompositeKeyInfo getCompositeKeyInfo()
	{
		return new CompositeKeyInfo(this.routingKeyIndexes, this.delimiter);
	}

	public void setRoutingKeyIndexs(int[] routingKeyIndexes)
	{
		this.routingKeyIndexes = routingKeyIndexes;
		routingKeyIndexesDefined = routingKeyIndexes != null && routingKeyIndexes.length > 0;
		largestIndex = -1;
		if (routingKeyIndexesDefined) {
			for (int i : routingKeyIndexes) {
				if (largestIndex < i) {
					largestIndex = i;
				}
			}
		}
	}
	
	public int[] getRoutingKeyIndexes()
	{
		return this.routingKeyIndexes;
	}
	
	public void setCompositeKeyDelimiter(String delimiter)
	{
		this.delimiter = delimiter;
	}
	
	public String getCompositeKeyDelimiter()
	{
		return this.delimiter;
	}
}
