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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.index.gemfire.lucene.TemporalLuceneDynamicIndexing;
import com.netcrest.pado.index.provider.lucene.LuceneBuilder;
import com.netcrest.pado.internal.util.QueueDispatcherListener;
import com.netcrest.pado.internal.util.QueueDispatcherMultiplexer;
import com.netcrest.pado.internal.util.QueueDispatcherMultiplexerPool;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalListFactory;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalEntry;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.util.GridUtil;

/**
 * TemporalCacheListener is invoked in the server for all temporal data update
 * operations, i.e., put, get, invalidate, and remove. It builds and keeps track
 * of temporal lists.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalCacheListener<K, V> extends CacheListenerAdapter implements Declarable, QueueDispatcherListener
{
	protected String temporalName;
	private boolean cacheWriterEnabled;
	// private QueueDispatcher dispatcher = new QueueDispatcher(500, 500);
	private QueueDispatcherMultiplexer temporalThread;

	private final boolean noObjectFormCached = Boolean.getBoolean("gemfire.PREFER_SERIALIZED")
			&& !Boolean.getBoolean("gemfire.STORE_ALL_VALUE_FORMS");

	private final ConcurrentHashMap<Object, ITemporalList> temporalListMap = new ConcurrentHashMap<Object, ITemporalList>(
			100000);

	protected TemporalListFactory temporalListFactory;

	// private LuceneTemporalEntryBuilder luceneBuilder;

	@SuppressWarnings("unused")
	private static final boolean DEBUG = Boolean.getBoolean("debug");

	private String fullPath;
	private boolean isLuceneEnabled = false;
	private TemporalLuceneDynamicIndexing luceneDynamicIndexing;

	/**
	 * If true, temporal list uses Collections.synchronizedList for faster
	 * write, slower read if false (default), it uses CopyOnWriteArrayList for
	 * faster read, slower write
	 */
	private boolean optimizedForWrite = false;

	public TemporalCacheListener()
	{
	}

	public void init(Properties p)
	{
		if (p == null) {
			p = new Properties();
		}
		this.temporalName = p.getProperty("temporalName", "temporal");
		String temporalKeyClassName = p.getProperty("temporalKey.class",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey");
		String temporalValueClassName = p.getProperty("temporalValue.class",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue");
		String temporalDataClassName = p.getProperty("temporalData.class",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData");
		String temporalListClassName = p.getProperty("temporalList.class",
				"com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalList");
		cacheWriterEnabled = p.getProperty("cacheWriterEnabled", "false").equals("true");

		try {
			temporalListFactory = TemporalListFactory.getTemporalListFactory(temporalName, temporalKeyClassName,
					temporalValueClassName, temporalDataClassName, temporalListClassName);

			// create the client factory in case the server needs to use the
			// client API.
			TemporalClientFactory temporalClientFactory = TemporalClientFactory.getTemporalClientFactory(temporalName,
					temporalKeyClassName, temporalValueClassName, temporalDataClassName);
		} catch (Exception ex) {
			CacheFactory.getAnyInstance().getLogger().error(ex);
		}

		// Initialize Lucene dynamic indexing
		this.fullPath = p.getProperty("fullPath");
		String pvalue = p.getProperty("luceneEnabled");
		isLuceneEnabled = pvalue != null && pvalue.equalsIgnoreCase("true");
		if (isLuceneEnabled) {
			luceneDynamicIndexing = new TemporalLuceneDynamicIndexing(fullPath);
		}

		// Start the dispatcher
		// dispatcher.setName("TemporalDispatcher: " + fullPath);
		// dispatcher.setQueueDispatcherListener(this);
		// dispatcher.start();

		QueueDispatcherMultiplexerPool.getQueueDispatcherMultiplexerPool().addQueueDispatcherListener(fullPath, this);
		temporalThread = QueueDispatcherMultiplexerPool.getQueueDispatcherMultiplexerPool().getMultiplexer(fullPath);
		temporalThread.setBatchSize(500);
		temporalThread.setTimeInterval(500);
	}

	@Override
	public void close()
	{
		// this.temporalListMap.clear();
		// this.dispatcher.clear();
		// this.temporalThread.clear(fullPath);

		if (luceneDynamicIndexing != null) {
			luceneDynamicIndexing.close();
		}
	}

	public boolean isLuceneDynamicIndexingEnabled()
	{
		return luceneDynamicIndexing != null && luceneDynamicIndexing.isClosed() == false;
	}

	public void afterRegionCreate(RegionEvent event)
	{
		Region region = event.getRegion();

		// Add cache writer
		if (cacheWriterEnabled) {
			region.getAttributesMutator().setCacheWriter(new TemporalCacheWriter());
		}
	}

	/**
	 * Initializes the temporal lists belong to this cache listener.
	 * 
	 * @param region
	 *            This cache listener's region.
	 * @param buildLucene
	 *            true to build Lucene indexes. This option has no effect if
	 *            Lucene is not enabled for this cache listener's region.
	 * @param spawnThread
	 *            true to perform initialization in thread, false to block till
	 *            done.
	 */
	public synchronized void initTemporalLists(final Region region, final boolean buildLucene, boolean spawnThread)
	{
		if (spawnThread) {
			Executors.newSingleThreadExecutor(new ThreadFactory() {
				public Thread newThread(Runnable r)
				{
					Thread t = new Thread(r, "Pado-TemporalCacheListener");
					t.setDaemon(true);
					return t;
				}
			}).execute(new Runnable() {

				public void run()
				{
					initTemporalLists(region, buildLucene);
				}
			});
		} else {
			initTemporalLists(region, buildLucene);
		}
	}

	/**
	 * Initializes all of temporal lists under the specified region.
	 * 
	 * @param region
	 *            Region
	 * @param buildLucene
	 *            true to build Lucene indexes if luceneDynamicIndexing is not
	 *            null.
	 */
	private void initTemporalLists(final Region region, boolean buildLucene)
	{
		// Updates/inserts could occur while temporal lists are
		// being built. Pause the dispatcher during the build period
		// and resume once the build is done. Resuming the dispatcher
		// will potentially dispatch the entries that have already
		// been processed during the build. Those entries must be
		// handled appropriately by the temporal list.
		// dispatcher.pause();
		temporalThread.pause(fullPath);
		try {
			long startTime = System.currentTimeMillis();
			Logger.info(region.getFullPath() + " - Temporal lists initialization started");
			temporalListMap.clear();
			PartitionedRegion pr = (PartitionedRegion) region;
			Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
			for (BucketRegion bucketRegion : set) {
				Set<Region.Entry<ITemporalKey, ITemporalData>> set2 = bucketRegion.entrySet();
				for (Region.Entry<ITemporalKey, ITemporalData> entry : set2) {
					ITemporalKey tk = entry.getKey();
					update(tk, region);
				}
			}
			long elapsedTime = System.currentTimeMillis() - startTime;
			Logger.info(region.getFullPath() + " - Temporal lists initialized. Number of lists: "
					+ temporalListMap.size() + ", time took (msec): " + elapsedTime);
			if (buildLucene) {
				if (luceneDynamicIndexing != null) {
					startTime = System.currentTimeMillis();
					luceneDynamicIndexing.close();
					LuceneBuilder.getLuceneBuilder().buildIndexes(GridUtil.getChildPath(fullPath));
					luceneDynamicIndexing.open();
					elapsedTime = System.currentTimeMillis() - startTime;
					Logger.info(region.getFullPath() + " - Lucene indexes rebuilt. Time took (msec): " + elapsedTime);
				}
			}
		} finally {
			// dispatcher.resume();
			temporalThread.resume(fullPath);
		}
	}

	public void afterCreate(EntryEvent event)
	{
		// dispatcher.enqueue(event);
		temporalThread.enqueue(fullPath, event);
	}

	public void afterUpdate(EntryEvent event)
	{
		// dispatcher.enqueue(event);
		temporalThread.enqueue(fullPath, event);
	}

	@Override
	public void afterDestroy(EntryEvent event)
	{

	}

	public void objectDispatched(Object obj)
	{
		List<EntryEvent<ITemporalKey, ITemporalData>> list = (List<EntryEvent<ITemporalKey, ITemporalData>>) obj;
		for (EntryEvent<ITemporalKey, ITemporalData> entryEvent : list) {
			update(entryEvent);
		}
		if (luceneDynamicIndexing != null) {
			luceneDynamicIndexing.processEvents(list);
		}
	}

	public void update(EntryEvent<ITemporalKey, ITemporalData> event)
	{
		ITemporalKey tk = event.getKey();
		ITemporalData data = event.getNewValue();
		if (data == null) {
			throw new IllegalArgumentException("Unexpected null for event=" + event);
		}
		update(tk, event.getRegion());
	}

	/**
	 * Updates the matching temporal list with the specified temporal key.
	 * 
	 * @param tk
	 *            If tk.getStartValidTime() and tk.getEndValidTime() are -1,
	 *            then the record is marked as removed from the temporal list.
	 *            It is appended at the end of the temporal list. If only
	 *            tk.getStartValidTime() is -1, then it is ignored. This case
	 *            should be for invalidating the record but it is currently not
	 *            supported.
	 * @param region
	 *            Temporal region that the specified temporal key belongs.
	 * @return The index at which the specified temporal key is
	 *         updated/added/removed. This method also handles removals, i.e.,
	 *         both tkey.getStartValidTime() and tkey.getStartEndValidTime() are
	 *         -1.
	 */
	int update(ITemporalKey tk, Region region)
	{
		int index = -1;
		try {
			Object identityKey = tk.getIdentityKey();

			// Create a new temporal list up front so that locking is
			// not required when calling the concurrent map's put operation.
			ITemporalList newTdl = temporalListFactory.createTemporalList(temporalName, tk.getIdentityKey(),
					region.getFullPath());

			ITemporalList existingTdl = this.temporalListMap.putIfAbsent(identityKey, newTdl);
			if (tk.getStartValidTime() == -1) {
				if (existingTdl != null) {
					if (tk.getEndValidTime() == -1) {
						GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
								.getTemporalManager(region.getFullPath());
						if (tm.isStatisticsEnabled()) {
							long startTime = tm.getStatistics().startRemoveCount();
							index = existingTdl.remove(tk);
							tm.getStatistics().endRemoveCount(startTime);
						} else {
							index = existingTdl.remove(tk);
						}
					} else {
						// invalidation not supported
						// existingTdl.invalidate(tk);
						index = -1;
					}
				}
			} else {
				GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
						.getTemporalManager(region.getFullPath());
				if (tm.isStatisticsEnabled()) {
					long startTime = tm.getStatistics().startPutCount();
					if (existingTdl != null) {
						index = existingTdl.add(tk);
					} else {
						index = newTdl.add(tk);
					}
					tm.getStatistics().endPutCount(startTime);
				} else {
					if (existingTdl != null) {
						index = existingTdl.add(tk);
					} else {
						index = newTdl.add(tk);
					}
				}
			}
		} catch (Exception ex) {
			CacheFactory.getAnyInstance().getLogger().error("Unable to create TemporalList", ex);
		}
		return index;
	}

	/**
	 * Pauses the temporal event dispatcher. Note that during the pause period
	 * region events continue to accumulate in the dispatcher queue. It is
	 * recommended that the pause period should be brief. Invoke
	 * {@link #resume()}.
	 */
	public void pause()
	{
		// dispatcher.pause();
		temporalThread.pause(fullPath);
		if (luceneDynamicIndexing != null) {
			luceneDynamicIndexing.close();
		}
	}

	/**
	 * Flushes the temporal event dispatcher queue by dispatching all of the
	 * temporal events in the queue.
	 */
	public void flush()
	{
		// dispatcher.flush();
		temporalThread.flush(fullPath);
	}

	/**
	 * Resumes the dispatcher by lifting the pause mode. The dispatcher works in
	 * the normal mode after this call.
	 */
	public void resume()
	{
		if (luceneDynamicIndexing != null) {
			luceneDynamicIndexing.open();
		}
		// dispatcher.resume();
		temporalThread.resume(fullPath);
	}

	public Set getIdentityKeySet()
	{
		return temporalListMap.keySet();
	}

	public List getIdentityKeyList()
	{
		return new ArrayList<Object>(temporalListMap.keySet());
	}

	/**
	 * Returns a list containing unique identity keys retrieved from the
	 * specified temporal key set.
	 * 
	 * @param temporalKeySet
	 *            Temporal key set that may contain duplicate identity keys
	 */
	public List getIdentityKeyList(Collection<ITemporalKey> temporalKeySet)
	{
		if (temporalKeySet == null) {
			return null;
		}
		ArrayList list = new ArrayList(temporalKeySet.size());
		for (ITemporalKey tk : temporalKeySet) {
			list.add(tk.getIdentityKey());
		}
		return list;
	}

	public List<TemporalEntry> getNowRelativeTemporalEntryList()
	{
		Collection<ITemporalList> col = temporalListMap.values();
		ArrayList list = new ArrayList(col.size() + 1);
		for (ITemporalList temporalList : col) {
			TemporalEntry entry = temporalList.getNowRelativeEntry();
			if (entry != null) {
				list.add(entry);
			}
		}
		return list;
	}

	public List<TemporalEntry> getLastTemporalEntryList()
	{
		return getLastTemporalEntryList(-1);
	}

	public List<TemporalEntry> getLastTemporalEntryList(int limit)
	{
		ArrayList list;
		Collection<ITemporalList> col = temporalListMap.values();
		if (limit < 0) {
			limit = col.size();
		} else if (limit > col.size()) {
			limit = col.size();
		}
		list = new ArrayList(limit);
		if (limit > 0) {
			int count = 0;
			for (ITemporalList temporalList : col) {
				count++;
				TemporalEntry entry = temporalList.getLastEntry();
				if (entry != null) {
					list.add(entry);
				}
				if (count >= limit) {
					break;
				}
			}
		}
		return list;
	}

	public TemporalDataList getTemporalDataList(Object identityKey)
	{
		if (identityKey == null) {
			return null;
		}
		ITemporalList list = temporalListMap.get(identityKey);
		if (list == null) {
			return null;
		}
		return list.getTemporalDataList();
	}

	public ITemporalList getTemporalList(Object identityKey)
	{
		if (identityKey == null) {
			return null;
		}
		return temporalListMap.get(identityKey);
	}

	public void dump(Object identityKey)
	{
		ITemporalList list = null;
		if (identityKey != null) {
			list = temporalListMap.get(identityKey);
		}
		if (list != null) {
			list.dump();
		} else {
			System.out.println();
			System.out.println("=====================================================");
			System.out.println("   IdentityKey = " + identityKey);
			System.out.println("   TemporalList does not exist");
			System.out.println("=====================================================");
			System.out.println();
		}
	}

	public void dumpAll(String filePath, boolean includeDeltas)
	{
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}

		if (includeDeltas) {

		} else {

		}
	}

	public void clearTemporalList(Object identityKey)
	{
		if (identityKey != null) {
			temporalListMap.remove(identityKey);
		}
	}

	public ITemporalData removePermanently(ITemporalKey tk, Region region)
	{
		Object identityKey = tk.getIdentityKey();
		ITemporalList existingTdl = this.temporalListMap.get(identityKey);
		if (existingTdl == null) {
			return null;
		}
		return existingTdl.removePermanently(tk);
	}

	public TemporalEntry getNowRelativeEntry(Object idKey)
	{
		if (idKey == null) {
			return null;
		}
		TemporalEntry result = null;
		ITemporalList tdl = this.temporalListMap.get(idKey);
		if (tdl != null) {
			result = tdl.getNowRelativeEntry();
		}
		return result;
	}

	/**
	 * Return all now-relative temporal entities.
	 */
	public Map<ITemporalKey<K>, ITemporalData<K>> getNowRelativeMap()
	{
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		HashMap<ITemporalKey<K>, ITemporalData<K>> map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			TemporalEntry entry2 = tdl.getNowRelativeEntry();
			if (entry2 != null) {
				map.put(entry2.getTemporalKey(), entry2.getTemporalData());
			}
		}
		return map;
	}

	/**
	 * Return all now-relative temporal entities for the specified identity key
	 * set.
	 * 
	 * @param identityKeySet
	 *            Identity key set.
	 */
	public Map<ITemporalKey<K>, ITemporalData<K>> getNowRelativeMap(Set identityKeySet)
	{
		HashMap<ITemporalKey<K>, ITemporalData<K>> map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(
				identityKeySet.size(), 1f);
		for (Object identityKey : identityKeySet) {
			ITemporalList tdl = getTemporalList(identityKey);
			TemporalEntry entry2 = tdl.getNowRelativeEntry();
			if (entry2 != null) {
				map.put(entry2.getTemporalKey(), entry2.getTemporalData());
			}
		}
		return map;
	}

	public List<TemporalEntry> getNowRelativeList()
	{
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			TemporalEntry entry2 = tdl.getNowRelativeEntry();
			if (entry2 != null) {
				list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
			}
		}
		return list;
	}

	public List<TemporalEntry> getNowRelativeList(Collection<K> identityKeyCollection)
	{
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Object identityKey : identityKeyCollection) {
			ITemporalList tdl = getTemporalList(identityKey);
			if (tdl != null) {
				TemporalEntry entry2 = tdl.getNowRelativeEntry();
				if (entry2 != null) {
					list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
				}
			}
		}
		return list;
	}

	public TemporalEntry getLastEntry(Object idKey)
	{
		if (idKey == null) {
			return null;
		}
		TemporalEntry result = null;
		ITemporalList tdl = this.temporalListMap.get(idKey);
		if (tdl != null) {
			result = tdl.getLastEntry();
		}
		return result;
	}

	public TemporalEntry getAsOf(Object idKey, long validAtTime, long asOfTime)
	{
		if (idKey == null) {
			return null;
		}
		TemporalEntry entry = null;
		ITemporalList tdl = this.temporalListMap.get(idKey);
		if (tdl != null) {
			entry = tdl.getAsOf(validAtTime, asOfTime);
		}
		return entry;
	}

	/**
	 * Return all temporal entities that satisfy the specified valid-at and
	 * as-of times.
	 * 
	 * @param validAtTime
	 * @param asOfTime
	 */
	public Map<ITemporalKey<K>, ITemporalData<K>> getAsOfEntryMap(long validAtTime, long asOfTime)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			return getNowRelativeMap();
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		HashMap<ITemporalKey<K>, ITemporalData<K>> map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			if (entry2 != null) {
				map.put(entry2.getTemporalKey(), entry2.getTemporalData());
			}
		}
		return map;
	}

	/**
	 * Return all temporal entities that satisfy the specified valid-at and
	 * as-of times for the specified identity key set.
	 * 
	 * @param identityKeySet
	 *            Identity key set
	 * @param validAtTime
	 * @param asOfTime
	 */
	public Map<ITemporalKey<K>, ITemporalData<K>> getAsOfEntryMap(Set identityKeySet, long validAtTime, long asOfTime)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			return getNowRelativeMap(identityKeySet);
		}

		HashMap<ITemporalKey<K>, ITemporalData<K>> map = new HashMap<ITemporalKey<K>, ITemporalData<K>>(
				identityKeySet.size(), 1f);
		for (Object identityKey : identityKeySet) {
			ITemporalList tdl = getTemporalList(identityKey);
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			if (entry2 != null) {
				map.put(entry2.getTemporalKey(), entry2.getTemporalData());
			}
		}
		return map;
	}

	public List<TemporalEntry> getAsOfTemporalEntryList(long validAtTime, long asOfTime)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			return getNowRelativeList();
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			if (entry2 != null) {
				list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
			}
		}
		return list;
	}

	public List<TemporalEntry> getAsOfTemporalEntryList(Set identityKeySet, long validAtTime, long asOfTime)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			return getNowRelativeList(identityKeySet);
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Object identityKey : identityKeySet) {
			ITemporalList tdl = getTemporalList(identityKey);
			if (tdl != null) {
				TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
				if (entry2 != null) {
					list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
				}
			}
		}
		return list;
	}

	public List<TemporalEntry> getWrittenTimeRangeTemporalEntryList(long validAtTime, long fromWrittenTime,
			long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			return getNowRelativeList();
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			TemporalEntry entry2 = tdl.getWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
			if (entry2 != null) {
				list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
			}
		}
		return list;
	}

	public List<TemporalEntry> getWrittenTimeRangeTemporalEntryList(Set identityKeySet, long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			return getNowRelativeList(identityKeySet);
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Object identityKey : identityKeySet) {
			ITemporalList tdl = getTemporalList(identityKey);
			if (tdl != null) {
				TemporalEntry entry2 = tdl.getWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
				if (entry2 != null) {
					list.add(new GemfireTemporalEntry(entry2.getTemporalKey(), entry2.getTemporalData()));
				}
			}
		}
		return list;
	}

	/**
	 * Returns temporal entries that fall in the specified validAtTime and
	 * written time range. It may return one ore more temporal entries with the
	 * same identity key.
	 * 
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param fromWrittenTime
	 *            Start of the written time range, inclusive.
	 * @param toWrittenTime
	 *            End of the written time range, exclusive.
	 * @return null if not found
	 */
	public List<TemporalEntry> getHistoryWrittenTimeRangeTemporalEntryList(long validAtTime, long fromWrittenTime,
			long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			return getNowRelativeList();
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tdl = entry.getValue();
			List<TemporalEntry> list2 = tdl.getHistoryWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
			if (list2 != null) {
				list.addAll(list2);
			}
		}
		return list;
	}

	/**
	 * Returns temporal entries that fall in the specified validAtTime and
	 * written time range. It may return one ore more temporal entries with the
	 * same identity key.
	 * 
	 * @param identityKeySet
	 *            Identity key set
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param fromWrittenTime
	 *            Start of the written time range, inclusive.
	 * @param toWrittenTime
	 *            End of the written time range, exclusive.
	 * @return null if not found
	 */
	public List<TemporalEntry> getHistoryWrittenTimeRangeTemporalEntryList(Set identityKeySet, long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			return getNowRelativeList(identityKeySet);
		}
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		List<TemporalEntry> list = new ArrayList<TemporalEntry>(set.size() / 2);
		for (Object identityKey : identityKeySet) {
			ITemporalList tdl = getTemporalList(identityKey);
			if (tdl != null) {
				List<TemporalEntry> list2 = tdl.getHistoryWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
				if (list2 != null) {
					list.addAll(list2);
				}
			}
		}
		return list;
	}

	/**
	 * Returns a list of temporal entries with unique identity keys and that
	 * have the latest written times in their temporal lists.
	 * 
	 * @param temporalKeySet
	 *            Temporal key set
	 */
	public List<TemporalEntry> getTemporalEntryList_TemporalKeys(Set<ITemporalKey> temporalKeySet)
	{
		Map<Object, ITemporalKey> keyMap = getWrittenTimeRangeTemporalKeyMap(temporalKeySet);
		List list = new ArrayList(keyMap.size());
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		for (ITemporalKey tk : keyMap.values()) {
			ITemporalData td = (ITemporalData) region.get(tk);
			if (td != null) {
				list.add(new GemfireTemporalEntry(tk, td));
			}
		}
		return list;
	}

	/**
	 * Returns a list of temporal values, i.e., ITemporalData.getValue()), that
	 * mapped to unique identity keys in the specified temporal key set and that
	 * have the latest written times in their temporal lists.
	 * 
	 * @param temporalKeySet
	 *            Temporal key set that may contain duplicate identity keys
	 */
	public List<V> getValueList_TemporalKeys(Set<ITemporalKey> temporalKeySet)
	{
		Map<Object, ITemporalKey> keyMap = getWrittenTimeRangeTemporalKeyMap(temporalKeySet);
		List<V> list = new ArrayList<V>(keyMap.size());
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		for (ITemporalKey tk : keyMap.values()) {
			ITemporalData td = (ITemporalData) region.get(tk);
			if (td != null) {
				list.add((V) td.getValue());
			}
		}
		return list;
	}

	/**
	 * Returns a list of temporal data objects that mapped to unique identity
	 * keys in the specified temporal key set and that have the latest written
	 * times in their temporal lists.
	 * 
	 * @param temporalKeySet
	 *            Temporal key set that may contain duplicate identity keys
	 */
	public List<ITemporalData> getTemporalDataList_TemporalKeys(Set<ITemporalKey> temporalKeySet)
	{
		Map<Object, ITemporalKey> keyMap = getWrittenTimeRangeTemporalKeyMap(temporalKeySet);
		List<ITemporalData> list = new ArrayList(keyMap.size());
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		for (ITemporalKey tk : keyMap.values()) {
			ITemporalData td = (ITemporalData) region.get(tk);
			if (td != null) {
				list.add(td);
			}
		}
		return list;
	}

	/**
	 * Returns a map of <IdentityKey, ITemporalKey> entries that have the latest
	 * written times in their temporal lists.
	 * 
	 * @param temporalKeySet
	 *            Temporal key set that may contain duplicate identity keys
	 */
	private Map<Object, ITemporalKey> getWrittenTimeRangeTemporalKeyMap(Set<ITemporalKey> temporalKeySet)
	{
		HashMap<Object, ITemporalKey> keyMap = new HashMap(temporalKeySet.size(), 1f);
		for (ITemporalKey tk : temporalKeySet) {
			ITemporalKey tk2 = keyMap.get(tk.getIdentityKey());
			if (tk2 == null) {
				keyMap.put(tk.getIdentityKey(), tk);
			} else {
				if (tk.getWrittenTime() >= tk2.getWrittenTime()) {
					keyMap.put(tk.getIdentityKey(), tk);
				}
			}
		}
		return keyMap;
	}

	public Set<K> getAsOfKeySet(long validAtTime, long asOfTime)
	{
		Set<K> set = new HashSet<K>(temporalListMap.size() / 2);
		return (Set<K>) getAsOfKeyCollection(validAtTime, asOfTime, set);
	}

	public List<K> getAsOfKeyList(long validAtTime, long asOfTime)
	{
		List<K> list = new ArrayList<K>(temporalListMap.size() / 2);
		return (List<K>) getAsOfKeyCollection(validAtTime, asOfTime, list);
	}

	private Collection<K> getAsOfKeyCollection(long validAtTime, long asOfTime, Collection<K> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			col.add((K) entry2.getTemporalKey().getIdentityKey());
		}
		return col;
	}

	public Set<ITemporalKey> getAsOfTemporalKeySet(long validAtTime, long asOfTime)
	{
		Set<ITemporalKey> set = new HashSet<ITemporalKey>(temporalListMap.size() / 2);
		return (Set<ITemporalKey>) getAsOfTemporalKeyCollection(validAtTime, asOfTime, set);
	}

	public List<ITemporalKey> getAsOfTemporalKeyList(long validAtTime, long asOfTime)
	{
		List<ITemporalKey> list = new ArrayList<ITemporalKey>(temporalListMap.size() / 2);
		return (List<ITemporalKey>) getAsOfTemporalKeyCollection(validAtTime, asOfTime, list);
	}

	private Collection<ITemporalKey> getAsOfTemporalKeyCollection(long validAtTime, long asOfTime,
			Collection<ITemporalKey> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			col.add(entry2.getTemporalKey());
		}
		return col;
	}

	/**
	 * Returns all temporal values that satisfy the specified valid-at and as-of
	 * times.
	 * 
	 * @param validAtTime
	 * @param asOfTime
	 */
	public Set<V> getAsOfValueSet(long validAtTime, long asOfTime)
	{
		Set<V> set = new HashSet<V>(temporalListMap.size() / 2);
		return (Set<V>) getAsOfValueCollection(validAtTime, asOfTime, set);
	}

	public List<V> getAsOfValueList(long validAtTime, long asOfTime)
	{
		List<V> list = new ArrayList<V>(temporalListMap.size() / 2);
		return (List<V>) getAsOfValueCollection(validAtTime, asOfTime, list);
	}

	private Collection<V> getAsOfValueCollection(long validAtTime, long asOfTime, Collection<V> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			if (entry2 != null) {
				col.add((V) entry2.getTemporalData().getValue());
			}
		}
		return col;
	}

	public List<V> getWrittenTimeRangeValueList(long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		List<V> list = new ArrayList<V>(temporalListMap.size() / 2);
		return (List<V>) getWrittenTimeRangeCollection(validAtTime, fromWrittenTime, toWrittenTime, list);
	}

	private Collection<V> getWrittenTimeRangeCollection(long validAtTime, long fromWrittenTime, long toWrittenTime,
			Collection<V> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			TemporalEntry entry2 = tdl.getWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
			if (entry2 != null) {
				col.add((V) entry2.getTemporalData().getValue());
			}
		}
		return col;
	}

	public List<V> getHistoryWrittenTimeRangeValueList(long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		List<V> list = new ArrayList<V>(temporalListMap.size() / 2);
		return (List<V>) getHistoryWrittenTimeRangeCollection(validAtTime, fromWrittenTime, toWrittenTime, list);
	}

	private Collection<V> getHistoryWrittenTimeRangeCollection(long validAtTime, long fromWrittenTime,
			long toWrittenTime, Collection<V> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			List<TemporalEntry> list2 = tdl.getHistoryWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
			if (list2 != null) {
				for (TemporalEntry entry2 : list2) {
					col.add((V) entry2.getTemporalData().getValue());
				}
			}
		}
		return col;
	}

	public Set<ITemporalKey> getAsOfTemporalKeySet(Collection<K> identityKeyCollection, long validAtTime, long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (Set<ITemporalKey>) getAsOfTemporalKeyCollection(identityKeyCollection, validAtTime, asOfTime,
				new HashSet<ITemporalKey>(identityKeyCollection.size()));
	}

	public List<ITemporalKey> getAsOfTemporalKeyList(Collection<K> identityKeyCollection, long validAtTime,
			long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (List<ITemporalKey>) getAsOfTemporalKeyCollection(identityKeyCollection, validAtTime, asOfTime,
				new ArrayList<ITemporalKey>(identityKeyCollection.size()));
	}

	private Collection<ITemporalKey> getAsOfTemporalKeyCollection(Collection<K> identityKeyCollection, long validAtTime,
			long asOfTime, Collection<ITemporalKey> col)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			List<TemporalEntry> list = getNowRelativeList(identityKeyCollection);
			for (TemporalEntry entry : list) {
				col.add(entry.getTemporalKey());
			}
		} else {
			for (Object identityKey : identityKeyCollection) {
				ITemporalList tdl = getTemporalList(identityKey);
				if (tdl != null) {
					TemporalEntry entry = tdl.getAsOf(validAtTime, asOfTime);
					col.add(entry.getTemporalKey());
				}
			}
		}
		return col;
	}

	/**
	 * Returns a set of values that satisfy the specified valid-at and as-of
	 * times.
	 * 
	 * @param identityKeyCollection
	 *            Identity key collection
	 * @param validAtTime
	 *            Valid-at time
	 * @param asOfTime
	 *            As-of time
	 * @return
	 */
	public Set<V> getAsOfValueSet(Collection<K> identityKeyCollection, long validAtTime, long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (Set<V>) getAsOfValueCollection(identityKeyCollection, validAtTime, asOfTime,
				new HashSet<V>(identityKeyCollection.size()));
	}

	public List<V> getAsOfValueList(Collection<K> identityKeyCollection, long validAtTime, long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (List<V>) getAsOfValueCollection(identityKeyCollection, validAtTime, asOfTime,
				new ArrayList<V>(identityKeyCollection.size()));
	}

	private Collection<V> getAsOfValueCollection(Collection<K> identityKeyCollection, long validAtTime, long asOfTime,
			Collection<V> col)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			List<TemporalEntry> list = getNowRelativeList(identityKeyCollection);
			for (TemporalEntry entry : list) {
				if (entry.getTemporalData() instanceof GemfireTemporalData) {
					col.add((V) ((GemfireTemporalData) (entry.getTemporalData())).getValue());
				} else {
					col.add((V) entry.getTemporalData());
				}
			}
		} else {
			for (Object identityKey : identityKeyCollection) {
				ITemporalList tdl = getTemporalList(identityKey);
				if (tdl != null) {
					TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
					if (entry2 != null) {
						if (entry2.getTemporalData() instanceof GemfireTemporalData) {
							col.add((V) ((GemfireTemporalData) (entry2.getTemporalData())).getValue());
						} else {
							col.add((V) entry2.getTemporalData());
						}
					}
				}
			}
		}
		return col;
	}

	public List<V> getWrittenTimeRangeValueList(Collection<K> identityKeyCollection, long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (List<V>) getWrittenTimeRangeValueCollection(identityKeyCollection, validAtTime, fromWrittenTime,
				toWrittenTime, new ArrayList<V>(identityKeyCollection.size()));
	}

	public List<V> getValueList(Collection<ITemporalKey> temporalKeyCollection)
	{
		if (temporalKeyCollection == null) {
			return null;
		}
		return (List<V>) getValueCollection(temporalKeyCollection, new ArrayList<V>(temporalKeyCollection.size()));
	}

	private Collection<V> getWrittenTimeRangeValueCollection(Collection<K> identityKeyCollection, long validAtTime,
			long fromWrittenTime, long toWrittenTime, Collection<V> col)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			List<TemporalEntry> list = getNowRelativeList(identityKeyCollection);
			for (TemporalEntry entry : list) {
				if (entry.getTemporalData() instanceof GemfireTemporalData) {
					col.add((V) ((GemfireTemporalData) (entry.getTemporalData())).getValue());
				} else {
					col.add((V) entry.getTemporalData());
				}
			}
		} else {
			for (Object identityKey : identityKeyCollection) {
				ITemporalList tdl = getTemporalList(identityKey);
				if (tdl != null) {
					TemporalEntry entry2 = tdl.getWrttenTimeRange(validAtTime, fromWrittenTime, toWrittenTime);
					if (entry2 != null) {
						if (entry2.getTemporalData() instanceof GemfireTemporalData) {
							col.add((V) ((GemfireTemporalData) (entry2.getTemporalData())).getValue());
						} else {
							col.add((V) entry2.getTemporalData());
						}
					}
				}
			}
		}
		return col;
	}

	public List<V> getHistoryWrittenTimeRangeValueList(Collection<K> identityKeyCollection, long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (List<V>) getHistoryWrittenTimeRangeValueCollection(identityKeyCollection, validAtTime, fromWrittenTime,
				toWrittenTime, new ArrayList<V>(identityKeyCollection.size()));
	}

	private Collection<V> getHistoryWrittenTimeRangeValueCollection(Collection<K> identityKeyCollection,
			long validAtTime, long fromWrittenTime, long toWrittenTime, Collection<V> col)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			List<TemporalEntry> list = getNowRelativeList(identityKeyCollection);
			for (TemporalEntry entry : list) {
				col.add((V) entry.getTemporalData().getValue());
			}
		} else {
			for (Object identityKey : identityKeyCollection) {
				ITemporalList tdl = getTemporalList(identityKey);
				if (tdl != null) {
					List<TemporalEntry> list2 = tdl.getHistoryWrttenTimeRange(validAtTime, fromWrittenTime,
							toWrittenTime);
					for (TemporalEntry entry2 : list2) {
						if (entry2 != null) {
							col.add((V) entry2.getTemporalData().getValue());
						}
					}
				}
			}
		}
		return col;
	}

	private Collection<V> getValueCollection(Collection<ITemporalKey> temporalKeyCollection, Collection<V> col)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
		Map<ITemporalKey, ITemporalData> map = localDS.getAll(temporalKeyCollection);
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			col.add((V) entry.getValue().getValue());
		}
		return col;
	}

	public Set<ITemporalData> getAsOfTemporalDataSet(Collection<K> identityKeyCollection, long validAtTime,
			long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (Set<ITemporalData>) getAsOfTemporalDataCollection(identityKeyCollection, validAtTime, asOfTime,
				new HashSet<ITemporalData>(identityKeyCollection.size()));
	}

	public List<ITemporalData> getAsOfTemporalDataList(Collection<K> identityKeyCollection, long validAtTime,
			long asOfTime)
	{
		if (identityKeyCollection == null) {
			return null;
		}
		return (List<ITemporalData>) getAsOfTemporalDataCollection(identityKeyCollection, validAtTime, asOfTime,
				new ArrayList<ITemporalData>(identityKeyCollection.size()));
	}

	public List<ITemporalData> getTemporalDataList(Collection<ITemporalKey> temporalKeyCollection)
	{
		if (temporalKeyCollection == null) {
			return null;
		}
		return (List<ITemporalData>) getTemporalDataCollection(temporalKeyCollection,
				new ArrayList<ITemporalData>(temporalKeyCollection.size()));
	}

	private Collection<ITemporalData> getAsOfTemporalDataCollection(Collection<K> identityKeyCollection,
			long validAtTime, long asOfTime, Collection<ITemporalData> col)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			List<TemporalEntry> list = getNowRelativeList(identityKeyCollection);
			for (TemporalEntry entry : list) {
				col.add(entry.getTemporalData());
			}
		} else {
			for (Object identityKey : identityKeyCollection) {
				ITemporalList tdl = getTemporalList(identityKey);
				if (tdl != null) {
					TemporalEntry entry = tdl.getAsOf(validAtTime, asOfTime);
					if (entry != null) {
						col.add(entry.getTemporalData());
					}
				}
			}
		}
		return col;
	}

	private Collection<ITemporalData> getTemporalDataCollection(Collection<ITemporalKey> temporalKeyCollection,
			Collection<ITemporalData> col)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
		Map<ITemporalKey, ITemporalData> map = localDS.getAll(temporalKeyCollection);
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			col.add(entry.getValue());
		}
		return col;
	}
	
	public List<TemporalEntry<ITemporalKey, ITemporalData>> getTemporalEntryList(Collection<ITemporalKey> temporalKeyCollection)
	{
		if (temporalKeyCollection == null) {
			return null;
		}
		return (List<TemporalEntry<ITemporalKey, ITemporalData>>) getTemporalEntryCollection(temporalKeyCollection,
				new ArrayList<ITemporalData>(temporalKeyCollection.size()));
	}
	
	private Collection<TemporalEntry<ITemporalKey, ITemporalData>> getTemporalEntryCollection(Collection<ITemporalKey> temporalKeyCollection,
			Collection<ITemporalData> col)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
		Map<ITemporalKey, ITemporalData> map = localDS.getAll(temporalKeyCollection);
		List<TemporalEntry<ITemporalKey, ITemporalData>> list = new ArrayList(map.size());
		for (Map.Entry<ITemporalKey, ITemporalData> entry : map.entrySet()) {
			list.add(new GemfireTemporalEntry(entry.getKey(), entry.getValue()));
		}
		return list;
	}

	public Set<ITemporalData<K>> getAsOfTemporalDataSet(long validAtTime, long asOfTime)
	{
		Set<ITemporalData<K>> set = new HashSet<ITemporalData<K>>(temporalListMap.size() / 2);
		return (Set<ITemporalData<K>>) getAsOfTemporalDataCollection(validAtTime, asOfTime, set);
	}

	public List<ITemporalData<K>> getAsOfTemporalDataList(long validAtTime, long asOfTime)
	{
		List<ITemporalData<K>> set = new ArrayList<ITemporalData<K>>(temporalListMap.size() / 2);
		return (List<ITemporalData<K>>) getAsOfTemporalDataCollection(validAtTime, asOfTime, set);
	}

	private Collection<ITemporalData<K>> getAsOfTemporalDataCollection(long validAtTime, long asOfTime,
			Collection<ITemporalData<K>> col)
	{
		Collection<ITemporalList> temporalListCollection = temporalListMap.values();
		for (ITemporalList tdl : temporalListCollection) {
			TemporalEntry entry2 = tdl.getAsOf(validAtTime, asOfTime);
			if (entry2 != null) {
				col.add(entry2.getTemporalData());
			}
		}
		return col;
	}

	/**
	 * Returns the index of the specified key in the temporal list. It returns
	 * -1 if the key is not found in the temporal list.
	 * 
	 * @param tk
	 *            Temporal key.
	 */
	public int getIndex(ITemporalKey tk)
	{
		if (tk == null) {
			return -1;
		}
		ITemporalList tdl = this.temporalListMap.get(tk.getIdentityKey());
		if (tdl == null) {
			return -1;
		}
		return tdl.getIndex(tk);
	}

	public long getEndWrittenTime(ITemporalKey tkey)
	{
		if (tkey == null) {
			return -1;
		}
		ITemporalList tdl = this.temporalListMap.get(tkey.getIdentityKey());
		if (tdl == null) {
			return -1;
		}
		return tdl.getEndWrittenTime(tkey);
	}

	public TemporalListFactory getTemporalListFactory()
	{
		return temporalListFactory;
	}

	public Map getTemporalListMap()
	{
		return temporalListMap;
	}

	public int getTemporalListCount()
	{
		return temporalListMap.size();
	}

	/**
	 * Returns the total count of entries that satisfy the specified valid-at
	 * and as-of times.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 */
	public int getTemporalListCount(long validAtTime, long asOfTime)
	{
		Set<Map.Entry<Object, ITemporalList>> set = temporalListMap.entrySet();
		int count = 0;
		for (Map.Entry<Object, ITemporalList> entry : set) {
			ITemporalList tl = entry.getValue();
			if (tl.getAsOf(validAtTime, asOfTime) != null) {
				count++;
			}
		}
		return count;
	}
}
