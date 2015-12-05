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
package com.netcrest.pado.temporal.test.gemfire;

import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.PartitionResolver;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalData;

public class MockPartitionResolver implements PartitionResolver<ITemporalKey<String>, ITemporalData<String>>
{

	@Override
	public void close()
	{
	}

	@Override
	public Object getRoutingObject(EntryOperation<ITemporalKey<String>, ITemporalData<String>> opDetails)
	{
		Region<ITemporalKey<String>, ITemporalData<String>> region = opDetails.getRegion();
		ITemporalKey<String> key = opDetails.getKey();
		ITemporalData<String> data = opDetails.getNewValue();
		String regionName = region.getName();
		Object routingKey = key;
		if (regionName.equals("account") || regionName.equals("portfolio")) {
			if (data instanceof TemporalData) {
				KeyMap keyMap = (KeyMap)((TemporalData)data).getValue();
				routingKey = keyMap.get("AccountId");
			}
		}
		return routingKey;
	}

	@Override
	public String getName()
	{
		return "MockPartitionResolver";
	}

}
