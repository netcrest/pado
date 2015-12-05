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

import java.util.Map;

import com.netcrest.pado.util.IBulkLoader;

/**
 * Bulk-loads data into the temporal path.
 * 
 * @author dpark
 * 
 * @param <K>
 *            Identity key type
 * @param <V>
 *            Value type
 */
public interface ITemporalBulkLoader<K, V> extends IBulkLoader<K, V>
{
	/**
	 * Puts temporal data into the temporal path. A bulk-put is done when it
	 * reaches the batch size.
	 * 
	 * @param key
	 *            The temporal key.
	 * @param value
	 *            The value to put into the cache.
	 * @param attachmentMap
	 *            A map of attachment sets containing attachments to be part of
	 *            the value. If none, pass in null.
	 * 
	 * @return Returns the corresponding temporal value that the path holds.
	 */
	ITemporalData<K> put(ITemporalKey<K> key, V value, Map<String, AttachmentSet<K>> attachmentMap);

	/**
	 * Puts temporal data into the temporal path. A bulk-put is done when it
	 * reaches the batch size.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into the cache.
	 * @param attachmentMap
	 *            A map of attachment sets containing attachments to be part of
	 *            the value. If none, pass in null.
	 * @param startValidTime
	 *            The start valid-time.
	 * @param endValidTime
	 *            The end valid-time.
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal value that the path holds.
	 */
	ITemporalData<K> put(K identityKey, V value, Map<String, AttachmentSet<K>> attachmentMap, long startValidTime,
			long endValidTime, boolean isDelta);

	/**
	 * Puts temporal data into the temporal path. A bulk-put is done when it
	 * reaches the batch size.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into the cache.
	 * @param attachmentMap
	 *            A map of attachment sets containing attachments to be part of
	 *            the value. If none, pass in null.
	 * @param startValidTime
	 *            The start valid-time.
	 * @param endValidTime
	 *            The end valid-time.
	 * @param writtenTime
	 *            The written time (transaction time)
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal value that the path holds.
	 */
	ITemporalData<K> put(K identityKey, V value, Map<String, AttachmentSet<K>> attachmentMap, long startValidTime,
			long endValidTime, long writtenTime, boolean isDelta);

	/**
	 * Puts the specified temporal entry into the temporal path.
	 * 
	 * @param tkey
	 *            Temporal key
	 * @param data
	 *            Temporal data object
	 * @return
	 */
	ITemporalData<K> put(ITemporalKey<K> tkey, ITemporalData<K> data);

	/**
	 * Flushes the remaining batch. This method must be invoked at the end of
	 * the load.
	 */
	void flush();

	/**
	 * Enables or disables dff.
	 * 
	 * @param diff
	 *            If true then diff is performed against the existing data in
	 *            the grid path and inserted in the delta form only if there is
	 *            a difference.
	 */
	void setDiffEnabled(boolean diff);

	/**
	 * Returns true if diff is enabled. Otherwise, false. Default is false.
	 */
	boolean isDiffEnabled();

	/**
	 * Sets the temporal time to be used if diff is enabled. If objects are
	 * different then this time overwrites startValidTime and writtenTime.
	 * Otherwise, the times are untouched.
	 * 
	 * @param temporalTime
	 *            temporal time that overwrites startValidTime and writtenTime
	 *            only if diff is enabled.
	 */
	void setDiffTemporalTime(long temporalTime);

	/**
	 * Returns the temporal time that overwrites startValidTime and writtenTime.
	 * Default is the current time.
	 */
	long getDiffTemporalTime();

	/**
	 * Sets the batch size. The default size is 1000.
	 * 
	 * @param batchSize
	 *            Batch size
	 */
	void setBatchSize(int batchSize);

	/**
	 * Returns the batch size. The default size is 1000.
	 */
	int getBatchSize();

}