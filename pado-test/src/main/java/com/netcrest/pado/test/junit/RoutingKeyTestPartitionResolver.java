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
package com.netcrest.pado.test.junit;

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;

@SuppressWarnings("rawtypes")
public class RoutingKeyTestPartitionResolver implements Declarable, PartitionResolver
{

	@Override
	public void close()
	{
	}

	@Override
	public Object getRoutingObject(EntryOperation opDetails)
	{
		Object key = opDetails.getKey();
		if (key instanceof CacheKey) {
			CacheKey ck = (CacheKey)key;
			return ck.getRoutingKey();
		} else {
			return key;
		}
	}

	@Override
	public String getName()
	{
		return "RoutingKeyTestPartitionResolver";
	}

	@Override
	public void init(Properties props)
	{
	}

}
