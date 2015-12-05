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

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * TemporalInternalFactory creates data-grid product specific objects.
 * 
 * @author dpark
 * 
 * @param <K>
 * @param <V>
 */
public class TemporalInternalFactory<K, V>
{
	private static TemporalInternalFactory temporalInternalFactory;

	static {
		try {
			Class clazz = PadoUtil.getClass(Constants.PROP_CLASS_TEMPORAL_INTERNAL_FACTORY,
					Constants.DEFAULT_CLASS_TEMPORAL_INTERNAL_FACTORY);

			Method method = clazz.getMethod("getTemporalInternalFactory");
			try {
				temporalInternalFactory = (TemporalInternalFactory) method.invoke(null);
			} catch (Exception e) {
				Logger.severe("TemporalInternalFactory creation error", e);
			}
		} catch (Exception e) {
			Logger.severe("TemporalInternalFactory creation error", e);
		}
	}

	/**
	 * Returns the singleton TemoralInternalFactory object.
	 */
	public static TemporalInternalFactory getTemporalInternalFactory()
	{
		return temporalInternalFactory;
	}

	/**
	 * Returns a new empty AttachementResults object.
	 */
	public AttachmentResults<V> createAttachmentResults()
	{
		return temporalInternalFactory.createAttachmentResults();
	}

	/**
	 * Returns a new TemporalList object for the specified parameters.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param lastValue
	 *            Last value (or the latest value)
	 * @param temporalList
	 *            Temporal List
	 * @param bucketId
	 *            Bucket ID
	 * @param memberId
	 *            Member ID
	 * @param memberName
	 *            Member name
	 * @param host
	 *            Host name or IP address
	 * @param fullPath
	 *            Full path
	 */
	public TemporalDataList<K, V> createTemporalDataList(Object identityKey, TemporalEntry<K, V> lastValue,
			ArrayList<TemporalEntry<K, V>> temporalList, int bucketId, String memberId, String memberName, String host,
			String fullPath)
	{
		return temporalInternalFactory.createTemporalDataList(identityKey, lastValue, temporalList, bucketId, memberId,
				memberName, host, fullPath);
	}

	/**
	 * Returns a new TemporalEntry object for the specified parameters.
	 * 
	 * @param tkey
	 *            Temporal key
	 * @param data
	 *            Temporal data
	 */
	public TemporalEntry<K, V> createTemporalEntry(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		return temporalInternalFactory.createTemporalEntry(tkey, data);
	}

	/**
	 * Returns a new TemporalType object for the specified parameters.
	 * 
	 * @param fullPath
	 *            Full path
	 * @param identityKeyClassName
	 *            Identity key class name
	 * @param keyTypeClassName
	 *            KeyType class name if dataClassName is of KeyMap
	 * @param dataClassName
	 *            Data class name
	 * @param temporalEnabled
	 *            true to enable or false to temporal data
	 */
	public TemporalType createTemporalType(String fullPath, String identityKeyClassName, String keyTypeClassName,
			String dataClassName, boolean temporalEnabled)
	{
		return temporalInternalFactory.createTemporalType(fullPath, identityKeyClassName, keyTypeClassName, dataClassName,
				temporalEnabled);
	}
}
