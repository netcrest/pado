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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.biz.impl.gemfire.GridMapBulkLoaderImpl;
import com.netcrest.pado.exception.PathUndefinedException;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBulkLoader<K, V> extends GridMapBulkLoaderImpl<K, V>implements ITemporalBulkLoader<K, V>
{
	private ITemporalBizLink temporalBiz;
	private TemporalClientFactory clientFactory;
	private boolean diffEnabled = false;
	private Map<Object, TemporalEntry> temporalEntryMap;
	private long diffTemporalTime = System.currentTimeMillis();

	/**
	 * Constructs a BulkLoader instance. The default batch size of 1000 is used.
	 * 
	 * @param gridPath
	 *            The temporal grid path.
	 */
	public TemporalBulkLoader(String gridPath, ICatalog catalog)
	{
		this(gridPath, 1000, catalog);
	}

	/**
	 * Constructs a BulkLoader instance.
	 * 
	 * @param gridPath
	 *            The temporal grid path.
	 * @param batchSize
	 *            The bulk load batch size. The default is 1000.
	 */
	public TemporalBulkLoader(String gridPath, int batchSize, ICatalog catalog)
	{
		super(null, batchSize);
		this.batchSize = batchSize;
		ITemporalAdminBizLink temporalAdminBiz = (ITemporalAdminBizLink) catalog
				.newInstanceLocal("com.netcrest.pado.biz.ITemporalAdminBiz", gridPath);
		this.temporalBiz = (ITemporalBizLink) catalog.newInstance("com.netcrest.pado.biz.ITemporalBiz", gridPath);
		this.gridMapBiz = temporalAdminBiz.getGridMapBiz();
		this.gridMapBiz.setGridPath(gridPath);
		init(temporalAdminBiz);
	}

	public TemporalBulkLoader(ITemporalAdminBizLink temporalAdminBiz, int batchSize, ICatalog catalog)
	{
		super(null, batchSize);
		this.temporalBiz = (ITemporalBizLink) catalog.newInstance("com.netcrest.pado.biz.ITemporalBiz",
				temporalAdminBiz.getGridPath());
		this.gridMapBiz = temporalAdminBiz.getGridMapBiz();
		this.batchSize = batchSize;
		this.map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(batchSize + 1, 1f);
		this.clientFactory = temporalAdminBiz.getTemporalClientFactory();
		this.temporalEntryMap = new HashMap<Object, TemporalEntry>(batchSize + 1, 1f);
	}

	/**
	 * Initializes this object.
	 */
	private void init(ITemporalAdminBizLink temporalAdminBiz)
	{
		map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(batchSize + 1, 1f);
		clientFactory = temporalAdminBiz.getTemporalClientFactory();
	}

	/**
	 * Puts the specified key and value in the form of temporal key and data.
	 * 
	 * @param key
	 *            Identity key
	 * @parm value The actual value, not ITemporalData
	 */
	@Override
	public void put(K key, V value) throws PathUndefinedException
	{
		put(key, value, null, TemporalUtil.MIN_TIME, TemporalUtil.MAX_TIME, System.currentTimeMillis(), false);
	}
	
	/**
	 * Puts the specified temporal key and value into the temporal path using
	 * the current server time. Note that the key has the type ITemporalKey and
	 * the value is the actual value, not ITemporalValue.
	 * 
	 * @param tkey
	 *            The temporal key.
	 * @param value The actual value, not ITemporalData.
	 * @param attachmentMap
	 *            A map of attachment sets containing attachments to be part of
	 *            the value. If none, pass in null.
	 * 
	 * @return Returns the corresponding ITemporalValue object stored in the
	 *         temporal path.
	 */
	@Override
	public ITemporalData<K> put(ITemporalKey<K> tkey, V value, Map<String, AttachmentSet<K>> attachmentMap)
	{
		try {
			ITemporalData<K> data;
			if (value instanceof ITemporalData) {
				data = (ITemporalData<K>) value;
			} else {
				data = clientFactory.createTemporalData(tkey, value);
			}
			data.__getTemporalValue().setTemporalKey(tkey);
			data.__getTemporalValue().setAttachmentMap(attachmentMap);
			map.put(tkey, data);

			if (map.size() % batchSize == 0) {
				flush();
			}
			return data;
		} catch (Exception ex) {
			Logger.error(ex);
			return null;
		}
	}

	/**
	 * {@docRoot}
	 */
	public ITemporalData put(K identityKey, V value, Map<String, AttachmentSet<K>> attachmentMap, long startValidTime,
			long endValidTime, boolean isDelta)
	{
		return put(identityKey, value, attachmentMap, startValidTime, endValidTime, System.currentTimeMillis(),
				isDelta);
	}

	/**
	 * {@docRoot}
	 */
	public ITemporalData put(K identityKey, V value, Map<String, AttachmentSet<K>> attachmentMap, long startValidTime,
			long endValidTime, long writtenTime, boolean isDelta)
	{
		try {
			ITemporalKey tkey = clientFactory.createTemporalKey(identityKey, startValidTime, endValidTime, writtenTime,
					gridMapBiz.getBizContext().getUserContext().getUsername());
			ITemporalData<K> data;
			if (value instanceof ITemporalData) {
				data = (ITemporalData<K>) value;
				// ITemporalValue<K> tvalue =
				// clientFactory.createTemporalValue(tkey, value);
				ITemporalValue<K> tvalue = new GemfireTemporalValue(tkey, data);
				tvalue.setDelta(isDelta);
				data.__setTemporalValue(tvalue);
			} else {
				data = clientFactory.createTemporalData(tkey, value);
			}
			data.__getTemporalValue().setTemporalKey(tkey);
			data.__getTemporalValue().setAttachmentMap(attachmentMap);
			return put(tkey, data);
		} catch (Exception ex) {
			Logger.error(ex);
			return null;
		}
	}

	/**
	 * Flushes the remaining batch. This method must be invoked at the end of
	 * the load.
	 */
	public void flush()
	{
		if (map.size() > 0) {
			if (diffEnabled) {
				handleTemporalDiff();
			}
		}
		super.flush();
	}

	private ITemporalKey lastTemporalKey;

	@Override
	public ITemporalData<K> put(ITemporalKey<K> tkey, ITemporalData<K> data)
	{
		if (diffEnabled) {
			TemporalEntry te = TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tkey, data);
			temporalEntryMap.put(tkey.getIdentityKey(), te);
			lastTemporalKey = tkey;
		}

		map.put(tkey, data);
		if (map.size() % batchSize == 0) {
			flush();
		}
		return data;
	}

	private void handleTemporalDiff()
	{
		if (temporalEntryMap.size() == 0) {
			return;
		}

		// First, remove all duplicates in the batch
		// Set<Map.Entry<Object, TemporalEntry>> set2 =
		// temporalEntryMap.entrySet();
		// Iterator<Map.Entry<Object, TemporalEntry>> iterator2 =
		// set2.iterator();
		// while (iterator2.hasNext()) {
		// Map.Entry<Object, TemporalEntry> entry = iterator2.next();
		// TemporalEntry te = entry.getValue();
		// }
		//

		// Now, compare with the server data
		AttachmentSetFactory factory = new AttachmentSetFactory();
		AttachmentSet as = factory.createAttachmentSet(temporalEntryMap.keySet());
		as.setGridPath(temporalBiz.getGridPath());
		Map<ITemporalKey<K>, ITemporalData<K>> searchedResultMap = temporalBiz.getAttachmentsEntries(as,
				lastTemporalKey.getStartValidTime(), lastTemporalKey.getWrittenTime() + 1);
		Set<Map.Entry<ITemporalKey<K>, ITemporalData<K>>> set = searchedResultMap.entrySet();
		Iterator<Map.Entry<ITemporalKey<K>, ITemporalData<K>>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<ITemporalKey<K>, ITemporalData<K>> entry = iterator.next();
			ITemporalKey<K> tk2 = entry.getKey();
			ITemporalData<K> tv2 = entry.getValue();
			Object value;
			if (tv2 instanceof GemfireTemporalData) {
				value = ((GemfireTemporalData) tv2).getValue();
			} else {
				value = tv2;
			}
			TemporalEntry te = temporalEntryMap.remove(tk2.getIdentityKey());
			if (te != null) {
				ITemporalKey teTk = te.getTemporalKey();
				this.map.remove(te.getTemporalKey());
				if (value.equals(te.getValue()) == false) {
					ITemporalKey newTkWithDiffTemporalTime = new GemfireTemporalKey(teTk.getIdentityKey(),
							diffTemporalTime, teTk.getEndValidTime(), diffTemporalTime, teTk.getUsername());
					this.map.put(newTkWithDiffTemporalTime, te.getTemporalData());
				}
			}
		}
		temporalEntryMap.clear();
		lastTemporalKey = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDiffEnabled(boolean diffEnabled)
	{
		this.diffEnabled = diffEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDiffEnabled()
	{
		return this.diffEnabled;
	}

	@Override
	public void setDiffTemporalTime(long temporalTime)
	{
		if (temporalTime < 0) {
			this.diffTemporalTime = System.currentTimeMillis();
		} else {
			this.diffTemporalTime = temporalTime;
		}
	}

	@Override
	public long getDiffTemporalTime()
	{
		return diffTemporalTime;
	}
}
