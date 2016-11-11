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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.impl.IdentityKeyPartitionResolver;
import com.netcrest.pado.temporal.gemfire.impl.TemporalCacheListener;
import com.netcrest.pado.temporal.gemfire.impl.TemporalStatistics;

/**
 * GemfireTemporalManager extends TemporalManager to provide GemFire specific
 * temporal management services.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class GemfireTemporalManager extends TemporalManager
{
	private final String fullPath;
	private TemporalCacheListener temporalCacheListener;
	private final TemporalStatistics stats;
	private final boolean statsEnabled;

	public GemfireTemporalManager(String fullPath)
	{
		this.fullPath = fullPath;
		statsEnabled = PadoUtil.isProperty(Constants.PROP_STATISTICS_ENABLED);
		if (statsEnabled) {
			this.stats = new TemporalStatistics(CacheFactory.getAnyInstance().getDistributedSystem(), fullPath);
		} else {
			this.stats = null;
		}
	}

	public static GemfireTemporalManager getTemporalManager(String fullPath)
	{
		synchronized (managerMap) {
			GemfireTemporalManager manager = (GemfireTemporalManager) managerMap.get(fullPath);
			if (manager == null) {
				Region region = CacheFactory.getAnyInstance().getRegion(fullPath);

				// All temporal regions must be configured with
				// TemporalCacheListener. IdentityKeyPartitionResolver is
				// required only if the application introduced its own
				// ITemporalKey implementation without implementing
				// PartitionResolver.
				if (region != null) {
					CacheListener listeners[] = region.getAttributes().getCacheListeners();
					TemporalCacheListener temporalCacheListener = null;
					for (int i = 0; i < listeners.length; i++) {
						if (listeners[i] instanceof TemporalCacheListener) {
							temporalCacheListener = (TemporalCacheListener) listeners[i];
						}
					}
					if (temporalCacheListener != null) {
						manager = new GemfireTemporalManager(fullPath);
						manager.temporalCacheListener = temporalCacheListener;
						manager.temporalManager = manager;
						managerMap.put(fullPath, manager);
					}
				}
			}
			return manager;
		}
	}

	/**
	 * Non-synchronized method for quick access to the temporal listener.
	 * 
	 * @param fullPath
	 *            The region path
	 * @return Returns the listener for the specified region. Returns null if
	 *         the TemporalManager is not registered for the specified region.
	 */
	public static TemporalCacheListener getTemporalCacheListener(String fullPath)
	{
		GemfireTemporalManager manager = (GemfireTemporalManager) managerMap.get(fullPath);
		if (manager == null) {
			return null;
		}
		return manager.getTemporalCacheListener();
	}

	/**
	 * Creates a new temporal region if the specified full path does not exist.
	 * It registers the newly created temporal region to TemporalManager and
	 * returns the TemporalManager instance. If the full path already exists
	 * then it registers the full path to TemporalManager if it has not been
	 * done already. Note that TemporalManager registration occurs only if the
	 * full path satisfies the temporal region criteria. If the full path is
	 * invalid, then it returns null. A valid full path must honor the following
	 * temporal region criteria:
	 * <ul>
	 * <li>If the region does not exist then its parent region must exist</li>
	 * <li>If the region exists then the existing region must registered
	 * PartitionListenerImpl.</li>
	 * </ul>
	 * 
	 * @param fullPath
	 *            Full path
	 * @param regionShortcut
	 *            Valid path types for partitioned path are PARTITION,
	 *            PARTITION_PERSISTENT, PARTITION_PERSISTENT_OVERFLOW. Invalid
	 *            path type is converted to PARTITION. Default is PARTITION.
	 * @param diskStoreName
	 *            Disk store name defined in the grid. If null or undefined,
	 *            then the default disk store name is used. This applies to
	 *            persistent paths only.
	 * @param colocatedWithFullPath
	 *            Colocated region full path
	 * @param redunantCopies
	 *            Redundant copies. If < 0 then 0 is assigned.
	 * @param totalBucketCount
	 *            Total number of buckets. If < 0 then the default number of
	 *            buckets (113) is assigned.
	 */
	@SuppressWarnings("unchecked")
	public static TemporalManager createTemporalManager(String fullPath, boolean isLuceneDynamic, RegionShortcut regionShortcut,
			String diskStoreName, String colocatedWithFullPath, int redunantCopies, int totalBucketCount)
	{
		if (fullPath == null) {
			return null;
		}
		TemporalManager tm = getTemporalManager(fullPath);
		if (tm != null) {
			return tm;
		}

		// find the parent region
		Cache cache = CacheFactory.getAnyInstance();
		Region region = cache.getRegion(fullPath);
		if (region == null) {
			String parentPath = null;
			String regionName = null;
			int index = fullPath.lastIndexOf("/");
			if (index >= 0) {
				parentPath = fullPath.substring(0, index);
				regionName = fullPath.substring(index + 1);
			} else {
				regionName = fullPath;
			}

			// If the fullPath's parent region doesn't exist then the region
			// cannot be created. Immediately return.
			Region parentRegion = null;
			if (parentPath != null) {
				parentRegion = cache.getRegion(parentPath);
				if (parentRegion == null) {
					return null;
				}
			}

			switch (regionShortcut) {
			case PARTITION_PERSISTENT:
			case PARTITION_PERSISTENT_OVERFLOW:
			case PARTITION_OVERFLOW:
			case PARTITION:
				break;
			default:
				regionShortcut = RegionShortcut.PARTITION;
				break;
			}
			RegionFactory rf = cache.createRegionFactory(regionShortcut);
			switch (regionShortcut) {
			case PARTITION_PERSISTENT:
			case PARTITION_PERSISTENT_OVERFLOW:
				if (diskStoreName != null) {
					if (diskStoreName != null) {
						if (cache.findDiskStore(diskStoreName) != null) {
							rf.setDiskStoreName(diskStoreName);
						}
					}
				}
				break;
			default:
				break;
			}
			PartitionAttributesFactory paf = new PartitionAttributesFactory();
			if (colocatedWithFullPath != null) {
				paf.setColocatedWith(colocatedWithFullPath);
			}
			paf.setRedundantCopies(redunantCopies);
			paf.setTotalNumBuckets(totalBucketCount);
			rf.setPartitionAttributes(paf.create());
			addTemporalAttributes(fullPath, isLuceneDynamic, rf, paf);
			if (parentRegion == null) {
				region = rf.create(regionName);
			} else {
				region = rf.createSubregion(parentRegion, regionName);
			}

			tm = getTemporalManager(fullPath);
		}
		return tm;
	}

	/**
	 * Adds temporal attributes to the specified region and partition attributes
	 * factories. After this call the specified factories include all of the
	 * required temporal attributes for creating a temporal region. If either rf
	 * or paf is null then no attributes are set. This method essentially adds
	 * event listeners pertaining to managing temporal data. As such, the
	 * specified factories must not include any temporal data specific event
	 * listeners, otherwise, duplicate events may occur.
	 * 
	 * @param fullPath
	 *            Full path of region
	 * @param rf
	 *            Region factory
	 * @param paf
	 *            Partition attributes factory
	 */
	@SuppressWarnings("unchecked")
	public static void addTemporalAttributes(String fullPath, boolean isLuceneDynamic, RegionFactory rf, PartitionAttributesFactory paf)
	{
		if (rf == null || paf == null) {
			return;
		}

		TemporalCacheListener temporalCacheListener = new TemporalCacheListener();
		Properties props = new Properties();
		props.setProperty("fullPath", fullPath);
		props.setProperty("luceneEnabled", Boolean.toString(isLuceneDynamic));
		temporalCacheListener.init(props);
		rf.addCacheListener(temporalCacheListener);
		paf.setPartitionResolver(new IdentityKeyPartitionResolver());
	}

	private TemporalCacheListener getListener(Region region)
	{
		if (region instanceof PartitionedRegion == false) {
			return null;
		}
		PartitionedRegion pr = (PartitionedRegion) region;

		CacheListener[] cls = pr.getCacheListeners();
		if (cls != null) {
			for (int i = 0; i < cls.length; i++) {
				if (cls[i] instanceof TemporalCacheListener) {
					return (TemporalCacheListener) cls[i];
				}
			}
		}
		return null;
	}

	private void removeListener(Region region, TemporalCacheListener listener)
	{
		if (region instanceof PartitionedRegion == false) {
			return;
		}
		PartitionedRegion pr = (PartitionedRegion) region;
		pr.removeCacheListener(listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized void setEnabled(boolean enabled, boolean buildLucene, boolean spawnThread)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			return;
		}
		TemporalCacheListener listener = getListener(region);
		if (enabled) {
			Logger.info("Temporal list enablement started: fullPath=" + fullPath);
			if (listener == null) {
				region.getAttributesMutator().addCacheListener(temporalCacheListener);
			}
			// Rebuild the temporal lists
			temporalCacheListener.initTemporalLists(region, buildLucene, spawnThread);
			Logger.info("Temporal list enablement complete: fullPath=" + fullPath);
		} else {
			removeListener(region, temporalCacheListener);
			temporalCacheListener.close();
			Logger.info("Temporal list disabled (removed): fullPath=" + fullPath);
		}
	}

	public boolean isEnabled()
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		return getListener(region) != null;
	}

	public TemporalCacheListener getTemporalCacheListener()
	{
		return temporalCacheListener;
	}

	public Set getIdentityKeySet()
	{
		TemporalCacheListener listener = getTemporalCacheListener();
		return listener.getIdentityKeySet();
	}

	public List getIdentityKeyList()
	{
		TemporalCacheListener listener = getTemporalCacheListener();
		return listener.getIdentityKeyList();
	}

	@Override
	public List<TemporalEntry> getNowRelativeTemporalEntryList()
	{
		TemporalCacheListener listener = getTemporalCacheListener();
		return listener.getNowRelativeTemporalEntryList();
	}

	@Override
	public List<TemporalEntry> getLastTemporalEntryList()
	{
		TemporalCacheListener listener = getTemporalCacheListener();
		return listener.getLastTemporalEntryList();
	}
	
	@Override
	public List<TemporalEntry> getLastTemporalEntryList(int limit)
	{
		TemporalCacheListener listener = getTemporalCacheListener();
		return listener.getLastTemporalEntryList(limit);
	}

	@Override
	public TemporalDataList getTemporalDataList(Object identityKey)
	{
		return temporalCacheListener.getTemporalDataList(identityKey);
	}

	public ITemporalList getTemporalList(Object identityKey)
	{
		return temporalCacheListener.getTemporalList(identityKey);
	}

	public void update(EntryEvent<ITemporalKey, ITemporalData> event)
	{
		temporalCacheListener.update(event);
	}

	public void dump(Object identityKey)
	{
		temporalCacheListener.dump(identityKey);
	}

	public void dumpAll(String filePath, boolean includeDeltas)
	{
		temporalCacheListener.dumpAll(filePath, includeDeltas);
	}

	public void clearTemporalList(Object identityKey)
	{
		temporalCacheListener.clearTemporalList(identityKey);
	}

	public TemporalEntry getNowRelativeEntry(Object identityKey)
	{
		if (statsEnabled) {
			long startTime = stats.startAsOfEntitySearchCount();
			TemporalEntry entry = temporalCacheListener.getNowRelativeEntry(identityKey);
			stats.endAsOfEntitySearchCount(startTime);
			return entry;
		} else {
			return temporalCacheListener.getNowRelativeEntry(identityKey);
		}
	}

	public TemporalEntry getLastEntry(Object identityKey)
	{
		if (statsEnabled) {
			long startTime = stats.startAsOfEntitySearchCount();
			TemporalEntry entry = temporalCacheListener.getLastEntry(identityKey);
			stats.endAsOfEntitySearchCount(startTime);
			return entry;
		} else {
			return temporalCacheListener.getLastEntry(identityKey);
		}
	}

	public TemporalEntry getAsOf(Object idKey, long validTime, long asOfTime)
	{
		if (statsEnabled) {
			long startTime = stats.startAsOfEntitySearchCount();
			TemporalEntry entry = temporalCacheListener.getAsOf(idKey, validTime, asOfTime);
			stats.endAsOfEntitySearchCount(startTime);
			return entry;
		} else {
			return temporalCacheListener.getAsOf(idKey, validTime, asOfTime);
		}
	}

	public TemporalEntry getAsOf(Object idKey, long validTime)
	{
		if (statsEnabled) {
			long startTime = stats.startAsOfEntitySearchCount();
			TemporalEntry entry = temporalCacheListener.getAsOf(idKey, validTime, -1);
			stats.endAsOfEntitySearchCount(startTime);
			return entry;
		} else {
			return temporalCacheListener.getAsOf(idKey, validTime, -1);
		}
	}

	/**
	 * Returns the number of temporal lists. This number represents the total
	 * number of temporal lists of this particular region currently maintained
	 * by this server.
	 */
	public int getTemporalListCount()
	{
		return temporalCacheListener.getTemporalListCount();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTemporalListCount(long validAtTime, long asOfTime)
	{
		return temporalCacheListener.getTemporalListCount(validAtTime, asOfTime);
	}

	public TemporalStatistics getStatistics()
	{
		return stats;
	}

	public boolean isStatisticsEnabled()
	{
		return statsEnabled;
	}

	public static TemporalType[] getAllTemporalTypes()
	{
		Cache cache = CacheFactory.getAnyInstance();
		Set<Region<?, ?>> set = cache.rootRegions();
		List<TemporalType> typeList = new ArrayList<TemporalType>();
		for (Region<?, ?> region : set) {
			if (region.isDestroyed()) {
				continue;
			}
			typeList = getTemporalTypes(region, typeList);
		}
		Collections.sort(typeList);
		return typeList.toArray(new TemporalType[0]);
	}

	private static List<TemporalType> getTemporalTypes(Region region, List<TemporalType> typeList)
	{
		TemporalType type = getTemporalType(region);
		if (type != null) {
			typeList.add(type);
		}
		Set<Region> set = region.subregions(false);
		for (Region region2 : set) {
			typeList = getTemporalTypes(region2, typeList);
		}
		return typeList;
	}

	/**
	 * Returns the temporal type object if the specified region contains
	 * temporal objects. Returns null, otherwise.
	 * 
	 * @param region
	 *            The region to interrogate
	 */
	@SuppressWarnings("unchecked")
	public static TemporalType getTemporalType(Region region)
	{
		if (region instanceof PartitionedRegion == false) {
			return null;
		}

		GemfireTemporalManager tm = getTemporalManager(region.getFullPath());
		if (tm == null) {
			return null;
		}

		// Look up the first non-null entry in the region to determine
		// TemporalType.
		TemporalType type = null;
		Set<Map.Entry> set = null;
		if (region instanceof PartitionedRegion) {
			// Must get the local entry set to make sure it doesn't block
			// in case the partitioned region's co-located regions are
			// being recovered.
			PartitionedRegion pr = (PartitionedRegion) region;
			Region r = PartitionRegionHelper.getLocalData(pr);
			set = r.entrySet();
		} else {
			set = region.entrySet();
		}

		if (set != null) {
			for (Map.Entry entry : set) {
				Object key = entry.getKey();
				Object data = entry.getValue();
				if (key instanceof ITemporalKey && data instanceof ITemporalData) {
					String dataClassName = null;
					String keyTypeClassName = null;
					if (data instanceof TemporalData) {
						Object value = ((TemporalData) data).getValue();
						if (value != null) {
							dataClassName = value.getClass().getName();
							if (value instanceof KeyMap) {
								KeyType keyType = ((KeyMap) value).getKeyType();
								if (keyType != null) {
									keyType = KeyTypeManager.getLatestKeyTypeVersion(keyType);
									keyTypeClassName = keyType.getClass().getName();
								}
							}
						}
					} else if (data instanceof ITemporalData) {
						dataClassName = data.getClass().getName();
					}
					if (dataClassName != null) {
						String identityKeyClassName = ((ITemporalKey) key).getIdentityKey().getClass().getName();
						type = TemporalInternalFactory.getTemporalInternalFactory().createTemporalType(
								region.getFullPath(), identityKeyClassName, keyTypeClassName, dataClassName,
								tm.isEnabled());
						break;
					}
				}
			}
		}
		if (type == null) {
			type = TemporalInternalFactory.getTemporalInternalFactory().createTemporalType(region.getFullPath(), null,
					null, null, tm.isEnabled());
		}
		return type;
	}

	public int getIndex(ITemporalKey tk)
	{
		return temporalCacheListener.getIndex(tk);
	}

	public long getEndWrittenTime(ITemporalKey tk)
	{
		return temporalCacheListener.getEndWrittenTime(tk);
	}
	
	public ITemporalData removePermanently(ITemporalKey temporalKey)
	{
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			return null;
		}
		return temporalCacheListener.removePermanently(temporalKey, region);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pause()
	{
		temporalCacheListener.pause();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush()
	{
		temporalCacheListener.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resume()
	{
		temporalCacheListener.resume();
	}
}
