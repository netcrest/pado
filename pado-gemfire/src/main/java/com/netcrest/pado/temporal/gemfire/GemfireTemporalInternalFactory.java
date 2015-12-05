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

import java.util.ArrayList;

import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalType;

public class GemfireTemporalInternalFactory<K, V> extends TemporalInternalFactory<K, V>
{
	private static GemfireTemporalInternalFactory temporalInternalFactory = new GemfireTemporalInternalFactory();
	
	public static GemfireTemporalInternalFactory getTemporalInternalFactory()
	{
		return temporalInternalFactory;
	}
	
	@Override
	public AttachmentResults<V> createAttachmentResults()
	{
		return new GemfireAttachmentResults<V>();
	}
	
	@Override
	public TemporalDataList<K, V> createTemporalDataList(Object identityKey, TemporalEntry<K, V> lastValue, ArrayList<TemporalEntry<K, V>> temporalList,
			int bucketId, String memberId, String memberName, String host, String fullPath)
	{
		return new GemfireTemporalDataList<K, V>(identityKey, lastValue, temporalList, bucketId, memberId, memberName, host, fullPath);
	}
	
	@Override
	public TemporalEntry<K, V> createTemporalEntry(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		return new GemfireTemporalEntry<K, V>(tkey, data);
	}
	
	@Override
	public TemporalType createTemporalType(String regionPath, String identityKeyClassName, String keyTypeClassName, String dataClassName, boolean temporalEnabled)
	{
		return new GemfireTemporalType(regionPath, identityKeyClassName, keyTypeClassName, dataClassName, temporalEnabled);
	}
}
