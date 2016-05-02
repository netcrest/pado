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
package com.netcrest.pado.biz;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * ITemporalBiz provides access to temporal data stored in a grid path.
 * 
 * <p>
 * <b>Arguments:</b>
 * 
 * <pre>
 *     ICatalog.newInstance(ITemporalBiz.class, String gridPath)
 * </pre>
 * 
 * <blockquote> <b>gridPath</b> Grid path </blockquote>
 * 
 * @param validAtTime
 *            The time at which the value is valid. This time must fall within
 *            the valid time range. It is the valid-at time represented by the
 *            calendar time, i.e., the milliseconds since January 1, 1970,
 *            00:00:00 GMT.
 * @param asOfTime
 *            The as-of time compared against the written times. If -1, then it
 *            returns the now-relative (as-of-now) value. The value in search
 *            must have the written time that is greater than or equal to the
 *            specified as-of time. As-of time represented by the calendar time,
 *            i.e., the milliseconds since January 1, 1970, 00:00:00 GMT.
 * @param <K>
 *            The identity key type. This is the actual identity key that serves
 *            as the primary key. It is <i>not</i> ITemporalKey, which is the
 *            internal composite key that contains the identity key and time
 *            parameters.
 * @param <V>
 *            The value type. This is the actual value that is mapped with the
 *            identity key. It typically implements ITemporalData. If not,
 *            ITemporalBiz internally transforms it into ITemporalData.
 * @see java.lang.System#currentTimeMillis()
 * 
 * @author dpark
 */
@BizClass(name = "ITemporalBiz", path = "temporal")
public interface ITemporalBiz<K, V> extends IBiz, ITemporalBizLink<K, V>
{
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	void setPutExceptionEnabled(boolean isPutExceptionEnabled);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	boolean isPutExceptionEnabled();

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	ITemporalAdminBizLink<K, V> getTemporalAdminBiz();

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	V get(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	V get(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	V get(K identityKey, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> getEntry(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> getEntry(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> getEntry(K identityKey, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	List<V> getQueryValues(String queryStatement, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	List<TemporalEntry<K, V>> getQueryEntries(String queryStatement, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex, int limit);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(String queryStatement,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(String queryStatement,
			long validAtTime, long asOfTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(String queryStatement,
			long validAtTime, long asOfTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex, int limit);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit);

	/**
	 * {@inheritDoc}
	 */
	IScrollableResultSet<V> getValueResultSet(String queryStatement, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex, int limit);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	ITemporalKey<K> getKey(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	ITemporalKey<K> getKey(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	ITemporalKey<K> getKey(K identityKey, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> getKeySet(long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> getKeySet(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	public Set<V> get(long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<V> get(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<K, V>> getAllLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IScrollableResultSet<TemporalEntry<K, V>> getLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex, int limitSize);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void reset();

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> put(K identityKey, V value);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> put(K identityKey, V value, boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, long writtenTime,
			boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, long writtenTime, boolean isDelta);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	void remove(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	boolean isRemoved(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	boolean isExist(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void dump(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void dump(K identityKey, PrintStream printStream, SimpleDateFormat formatter);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachments(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachments(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachments(K identityKey, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachmentsEntries(K identityKey);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * Returns attachments by targeting the specific grid path.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @return Returns the attachment values that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V> __getAttachments(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapOfListsCollector")
	@Override
	Map<String, List<V>> __getAttachmentsOnServer(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapOfListsCollector")
	@Override
	Map<String, List<V>> __getAttachmentsBroadcast(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapOfListsCollector")
	@Override
	Map<String, List<TemporalEntry<K, V>>> __getAttachmentsEntriesBroadcast(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * {@inheritDoc}
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> __getAttachmentsEntries(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	@Override
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> __getAttachmentsKeys(long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime);

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	@Override
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime,
			long asOfTime);
}
