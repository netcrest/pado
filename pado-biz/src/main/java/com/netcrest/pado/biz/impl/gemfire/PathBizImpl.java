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
package com.netcrest.pado.biz.impl.gemfire;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributesFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.control.RebalanceFactory;
import com.gemstone.gemfire.cache.control.RebalanceOperation;
import com.gemstone.gemfire.cache.control.RebalanceResults;
import com.gemstone.gemfire.cache.control.ResourceManager;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IPathBiz.PathType;
import com.netcrest.pado.data.Entry;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.IncompatibleTypeException;
import com.netcrest.pado.exception.NestedPathExistsException;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.GemfireEntryImpl;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.info.GemfireRegionInfo;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.util.GridUtil;

public class PathBizImpl
{
	@Resource
	IBizContextServer bizContext;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BizMethod
	public boolean createPath(String gridId, String gridPath, String refId, boolean isTemporal, boolean isLuceneDynamic,
			boolean recursive)
	{

		String fullPath = GridUtil.getFullPath(gridPath);
		Cache cache = CacheFactory.getAnyInstance();
		Region region = cache.getRegion(fullPath);
		if (region != null) {
			return true;
		}

		Region parentRegion = RegionUtil.getParentRegion(fullPath);
		if (parentRegion == null) {
			if (recursive) {
				// Create REPLICATE type parent regions
				parentRegion = RegionUtil.createNestedParentReplicatedRegions(fullPath);
				if (parentRegion == null) {
					return false;
				}
			} else {
				return false;
			}
		}
		String regionName = RegionUtil.getRegionName(fullPath);

		PartitionAttributesFactory paf = null;
		RegionFactory rf = null;
		if (refId == null) {
			throw new PadoServerException("Reference ID is not defined: refid=" + refId);
		}
		RegionAttributes ra = cache.getRegionAttributes(refId);
		if (ra == null) {
			throw new PadoServerException("Reference ID is not defined: refid=" + refId);
		}
		PartitionAttributes pa = ra.getPartitionAttributes();
		if (pa != null) {
			paf = new PartitionAttributesFactory(pa);
		}
		// Create a region factory with the region
		// attributes defined in server.xml.
		rf = cache.createRegionFactory(ra);
		if (isTemporal) {
			GemfireTemporalManager.addTemporalAttributes(fullPath, isLuceneDynamic, rf, paf);
		}
		if (paf != null) {
			rf.setPartitionAttributes(paf.create());
		}
		region = rf.createSubregion(parentRegion, regionName);
		if (region != null) {
			if (PadoServerManager.getPadoServerManager().isMaster()) {
				PadoServerManager.getPadoServerManager()
						.updateAppInfo(PadoServerManager.getPadoServerManager().createGridInfo());
			}
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	public boolean __createPath(String gridId, String gridPath, String pathType, String diskStoreName,
			String gatewaySenderIds, String colocatedWithGridPath, int redundantCopies, int totalBucketCount,
			boolean recursive)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		Cache cache = CacheFactory.getAnyInstance();
		Region region = cache.getRegion(fullPath);
		if (region != null) {
			return true;
		}

		Region parentRegion = RegionUtil.getParentRegion(fullPath);
		if (parentRegion == null) {
			if (recursive) {
				// Create REPLICATE type parent regions
				parentRegion = RegionUtil.createNestedParentReplicatedRegions(fullPath);
				if (parentRegion == null) {
					return false;
				}
			} else {
				return false;
			}
		}
		String regionName = RegionUtil.getRegionName(fullPath);

		if (pathType == null) {
			pathType = PathType.PARTITION.name();
		}
		PathType pt = PathType.valueOf(pathType);
		if (pt == null) {
			pt = PathType.TEMPORAL;
		}

		// Create temporal path
		RegionShortcut rs = null;

		switch (pt) {
		case TEMPORAL_PERSISTENT:
		case TEMPORAL_LUCENE_PERSISTENT:
			rs = RegionShortcut.PARTITION_PERSISTENT;
			break;
		case TEMPORAL_PERSISTENT_OVERFLOW:
		case TEMPORAL_LUCENE_PERSISTENT_OVERFLOW:
			rs = RegionShortcut.PARTITION_PERSISTENT_OVERFLOW;
			break;
		case TEMPORAL_OVERFLOW:
		case TEMPORAL_LUCENE_OVERFLOW:
			rs = RegionShortcut.PARTITION_OVERFLOW;
			break;
		case TEMPORAL:
		case TEMPORAL_LUCENE:
			rs = RegionShortcut.PARTITION;
			break;
		default:
			break;
		}
		String colocatedWithFullPath = null;
		if (colocatedWithGridPath != null) {
			colocatedWithFullPath = GridUtil.getFullPath(colocatedWithGridPath);
		}

		if (rs != null) {
			boolean isLuceneDynamic = pt == PathType.TEMPORAL_LUCENE_PERSISTENT
					|| pt == PathType.TEMPORAL_LUCENE_PERSISTENT_OVERFLOW || pt == PathType.TEMPORAL_LUCENE_OVERFLOW
					|| pt == PathType.TEMPORAL_LUCENE;
			TemporalManager tm = GemfireTemporalManager.createTemporalManager(fullPath, isLuceneDynamic, rs,
					diskStoreName, colocatedWithFullPath, redundantCopies, totalBucketCount);
			if (tm != null) {
				if (PadoServerManager.getPadoServerManager().isMaster()) {
					PadoServerManager.getPadoServerManager()
							.updateAppInfo(PadoServerManager.getPadoServerManager().createGridInfo());
				}
			}
			return tm != null;
		}

		// Create non-temporal path
		RegionFactory rf;
		switch (pt) {
		case LOCAL:
			rf = cache.createRegionFactory(RegionShortcut.LOCAL);
			break;
		case LOCAL_PERSISTENT:
			rf = cache.createRegionFactory(RegionShortcut.LOCAL_PERSISTENT);
			break;
		case LOCAL_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.LOCAL_OVERFLOW);
			break;
		case LOCAL_PERSISTENT_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.LOCAL_PERSISTENT_OVERFLOW);
			break;
		case NORMAL:
			rf = cache.createRegionFactory();
			rf.setDataPolicy(DataPolicy.NORMAL);
			break;
		case PRELOADED:
			rf = cache.createRegionFactory();
			rf.setDataPolicy(DataPolicy.PRELOADED);
			break;
		case GLOBAL:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE);
			rf.setScope(Scope.GLOBAL);
			break;
		case REPLICATE:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE);
			break;
		case REPLICATE_PERSISTENT:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE_PERSISTENT);
			break;
		case REPLICATE_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE_OVERFLOW);
			break;
		case REPLICATE_PERSISTENT_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE_PERSISTENT_OVERFLOW);
			break;
		case REPLICATE_EMPTY:
			rf = cache.createRegionFactory(RegionShortcut.REPLICATE_PROXY);
			break;
		case PARTITION_PERSISTENT:
			rf = cache.createRegionFactory(RegionShortcut.PARTITION_PERSISTENT);
			break;
		case PARTITION_PERSISTENT_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.PARTITION_PERSISTENT_OVERFLOW);
			break;
		case PARTITION_OVERFLOW:
			rf = cache.createRegionFactory(RegionShortcut.PARTITION_OVERFLOW);
			break;
		case PARTITION:
		default:
			rf = cache.createRegionFactory(RegionShortcut.PARTITION);
			break;
		}

		// Set partition attributes
		switch (pt) {
		case PARTITION_PERSISTENT:
		case PARTITION_PERSISTENT_OVERFLOW:
		case PARTITION_OVERFLOW:
		case PARTITION:
			PartitionAttributesFactory paf = new PartitionAttributesFactory();
			if (colocatedWithFullPath != null) {
				paf.setColocatedWith(colocatedWithFullPath);
			}
			if (redundantCopies > 0) {
				paf.setRedundantCopies(redundantCopies);
			}
			if (totalBucketCount > 0) {
				paf.setTotalNumBuckets(totalBucketCount);
			}
			rf.setPartitionAttributes(paf.create());
			break;
		default:
			break;
		}

		// Set diskStoreName
		switch (pt) {
		case LOCAL_PERSISTENT:
		case LOCAL_PERSISTENT_OVERFLOW:
		case REPLICATE_PERSISTENT:
		case REPLICATE_PERSISTENT_OVERFLOW:
		case PARTITION_PERSISTENT:
		case PARTITION_PERSISTENT_OVERFLOW:
			if (diskStoreName != null) {
				if (cache.findDiskStore(diskStoreName) != null) {
					rf.setDiskStoreName(diskStoreName);
				}
			}
			break;
		default:
			break;
		}

		if (gatewaySenderIds != null && gatewaySenderIds.trim().length() > 0) {
			String split[] = gatewaySenderIds.split(",");
			for (String id : split) {
				rf.addGatewaySenderId(id.trim());
			}
		}
		region = rf.createSubregion(parentRegion, regionName);
		if (region != null) {
			if (PadoServerManager.getPadoServerManager().isMaster()) {
				PadoServerManager.getPadoServerManager()
						.updateAppInfo(PadoServerManager.getPadoServerManager().createGridInfo());
			}
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public void clear(String gridId, String gridPath, boolean force)
	{
		if (force) {
			String fullPath = GridUtil.getFullPath(gridPath);
			Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
			if (region == null) {
				return;
			}

			// TODO: ExecutorService to use a thread pool for concurrent
			// removes.
			if (region instanceof PartitionedRegion) {
				PartitionedRegion pr = (PartitionedRegion) region;
				Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
				for (BucketRegion bucketRegion : set) {
					final BucketRegion br = bucketRegion;
					Set keySet = br.keySet();
					for (Object key : keySet) {
						br.remove(key);
					}
				}
				TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
				if (tm != null) {
					// Reset the temporal list
					if (tm.isEnabled()) {
						// Block till done
						tm.setEnabled(false, false, false /* spawnThread */);
						tm.setEnabled(true, true, false /* spawnThread */);
					}
				}
			} else {
				region.clear();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public void remove(String gridId, String gridPath, boolean recursive) throws NestedPathExistsException
	{
		if (gridPath == null) {
			return;
		}

		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager.remove(fullPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			return;
		}
		RegionUtil.removeRegionLocal(region, recursive);
		if (PadoServerManager.getPadoServerManager().isMaster()) {
			PadoServerManager.getPadoServerManager()
					.updateAppInfo(PadoServerManager.getPadoServerManager().createGridInfo());
		}
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public List<PathInfo> getAllTemporalPathInfoList(String gridId)
	{
		Cache cache = CacheFactory.getAnyInstance();
		Set<String> set = TemporalManager.getAllTemporalFullPaths();
		List<PathInfo> list = new ArrayList<PathInfo>(set.size());
		for (String fullPath : set) {
			Region region = cache.getRegion(fullPath);
			GemfireRegionInfo pathInfo = new GemfireRegionInfo(region);
			// Overwrite the local size with the total size. Region.size()
			// is a distributed call for partitioned regions.
			pathInfo.setSize(region.size());
			list.add(pathInfo);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public int getSize(String gridId, String gridPath)
	{
		Cache cache = CacheFactory.getAnyInstance();
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = cache.getRegion(fullPath);
		if (region == null) {
			return 0;
		}
		int size = 0;
		if (region instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) region;
			if (pr.getDataStore() != null) {
				Set<BucketRegion> primaryBucketList = pr.getDataStore().getAllLocalPrimaryBucketRegions();
				for (BucketRegion br : primaryBucketList) {
					size += br.size();
				}
			}
		} else {
			size = region.size();
		}
		return size;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	public void copy(String gridId, String sourceGridPath, String targetGridPath) throws IncompatibleTypeException
	{
		Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
		Region sourceRegion = rootRegion.getSubregion(sourceGridPath);
		if (sourceRegion == null) {
			return;
		}
		Region targetRegion = rootRegion.getSubregion(targetGridPath);

		if (targetRegion == null) {
			// If the targetRegion does not exist then create it using
			// the same region attributes as the sourceRegion.
			String targetFullPath = rootRegion.getFullPath() + "/" + targetGridPath;
			Region targetParentRegion = RegionUtil.getParentRegion(targetFullPath);
			if (targetParentRegion == null) {
				return;
			}
			String regionName = targetFullPath.substring(targetFullPath.lastIndexOf("/") + 1);
			RegionFactory factory = CacheFactory.getAnyInstance().createRegionFactory(sourceRegion.getAttributes());
			targetRegion = factory.createSubregion(targetParentRegion, regionName);
			if (targetRegion != null) {
				PadoServerManager.getPadoServerManager()
						.updateAppInfo(PadoServerManager.getPadoServerManager().createGridInfo());
			}
		} else {
			// Make sure the targetRegion is compatible with the sourceRegion
			Set<Map.Entry> sourceSet = sourceRegion.entrySet();
			Set<Map.Entry> targetSet = targetRegion.entrySet();
			Object sourceKey = null;
			Object sourceValue = null;
			Object targetKey = null;
			Object targetValue = null;
			for (Map.Entry entry : sourceSet) {
				sourceKey = entry.getKey();
				sourceValue = entry.getValue();
				break;
			}
			if (sourceKey == null) {
				return;
			}
			for (Map.Entry entry : targetSet) {
				targetKey = entry.getKey();
				targetValue = entry.getValue();
				break;
			}

			// If the target region is not empty then make sure the source
			// and target paths have the same entry types.
			if (targetKey != null) {

				// Temporal identity keys must have the same type.
				if (sourceKey instanceof ITemporalKey && targetKey instanceof ITemporalKey == false
						|| targetKey instanceof ITemporalKey == false && targetKey instanceof ITemporalKey) {
					throw new IncompatibleTypeException("Incompatible temporal key types");
				}
				if (sourceKey instanceof ITemporalKey && targetKey instanceof ITemporalKey) {
					sourceKey = ((ITemporalKey) sourceKey).getIdentityKey();
					targetKey = ((ITemporalKey) targetKey).getIdentityKey();
				}

				// Keys (or identity keys) must have the same type.
				if (targetKey.getClass() != sourceKey.getClass()) {
					throw new IncompatibleTypeException("Incompatible key types");
				}

				// Temporal values must have the same type.
				if (sourceValue instanceof ITemporalData && targetValue instanceof ITemporalData == false
						|| sourceValue instanceof ITemporalData == false && targetValue instanceof ITemporalData) {
					throw new IncompatibleTypeException("Incompatible temporal data types");
				}
				if (targetValue instanceof ITemporalData && sourceValue instanceof ITemporalData) {
					targetValue = ((ITemporalData) targetValue).getValue();
					sourceValue = ((ITemporalData) sourceValue).getValue();
				}

				// Values (temporal values) must have the same type.
				if (sourceValue instanceof KeyMap && targetValue instanceof KeyMap == false
						|| sourceValue instanceof KeyMap == false && targetValue instanceof KeyMap) {
					throw new IncompatibleTypeException("Incompatible temporal data (KeyMap) types");
				}
				if (sourceValue instanceof KeyMap && targetValue instanceof KeyMap) {
					KeyType sourceKeyType = ((KeyMap) sourceValue).getKeyType();
					KeyType targetKeyType = ((KeyMap) targetValue).getKeyType();
					if (sourceKeyType.getId().equals(targetKeyType.getId()) == false) {
						throw new IncompatibleTypeException("Incompatible KeyMap KeyTypes");
					}
				} else {
					// Values (or temporal values) must have the same type.
					if (sourceValue.getClass() != targetValue.getClass()) {
						throw new IncompatibleTypeException("Incompatible temporal data types");
					}
				}
			}
		}

		// Copy all entries
		if (sourceRegion instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) sourceRegion;
			Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
			for (BucketRegion bucketRegion : set) {
				targetRegion.putAll(bucketRegion);
			}
		} else {
			if (PadoServerManager.getPadoServerManager().isMaster()) {
				targetRegion.putAll(sourceRegion);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public PathType getPathType(String gridId, String gridPath)
	{
		Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
		Region region = rootRegion.getSubregion(gridPath);
		return getPathType(region);
	}

	@SuppressWarnings("rawtypes")
	public static PathType getPathType(Region region)
	{
		if (region == null) {
			return null;
		}

		RegionAttributes ra = region.getAttributes();
		EvictionAttributes ea = ra.getEvictionAttributes();
		boolean isLocal = ra.getScope().isLocal();
		boolean isGlobal = ra.getScope().isGlobal();
		boolean isEmpty = !ra.getDataPolicy().withStorage();
		boolean isPersistent = ra.getDataPolicy().withPersistence();
		boolean isReplicate = ra.getDataPolicy().withReplication();
		boolean isPartition = ra.getDataPolicy().withPartitioning();
		boolean isOverflow = ea != null && ea.getAlgorithm().isNone() == false;
		boolean isNormal = ra.getDataPolicy().isNormal();
		boolean isPreloaded = ra.getDataPolicy().withPreloaded();

		PathType pathType;
		if (isLocal) {
			if (isPersistent) {
				if (isOverflow) {
					pathType = PathType.LOCAL_PERSISTENT_OVERFLOW;
				} else {
					pathType = PathType.LOCAL_PERSISTENT;
				}
			} else if (isOverflow) {
				pathType = PathType.LOCAL_OVERFLOW;
			} else {
				pathType = PathType.LOCAL;
			}
		} else if (isPreloaded) {
			pathType = PathType.PRELOADED;
		} else if (isNormal) {
			if (isOverflow) {
				if (ea.getAlgorithm().isLRUHeap() && ea.getAction().isOverflowToDisk()) {
					if (isPersistent) {
						pathType = PathType.LOCAL_PERSISTENT_OVERFLOW;
					} else {
						pathType = PathType.LOCAL_OVERFLOW;
					}
				} else {
					pathType = PathType.LOCAL_OVERFLOW;
				}
			} else if (isPersistent) {
				pathType = PathType.LOCAL_PERSISTENT;
			} else {
				pathType = PathType.NORMAL;
			}
		} else if (isEmpty) {
			pathType = PathType.REPLICATE_EMPTY;
		} else if (isGlobal) {
			pathType = PathType.GLOBAL;
		} else if (isReplicate) {
			if (isPersistent) {
				if (isOverflow) {
					pathType = PathType.REPLICATE_PERSISTENT_OVERFLOW;
				} else {
					pathType = PathType.REPLICATE_PERSISTENT;
				}
			} else if (isOverflow) {
				pathType = PathType.REPLICATE_OVERFLOW;
			} else {
				pathType = PathType.REPLICATE;
			}
		} else if (isPartition) {
			TemporalManager tm = TemporalManager.getTemporalManager(region.getFullPath());
			if (tm != null) {
				// temporal
				if (isPersistent) {
					if (isOverflow) {
						pathType = PathType.TEMPORAL_PERSISTENT_OVERFLOW;
					} else {
						pathType = PathType.TEMPORAL_PERSISTENT;
					}
				} else if (isOverflow) {
					pathType = PathType.TEMPORAL_OVERFLOW;
				} else {
					pathType = PathType.TEMPORAL;
				}
			} else {
				// partition
				if (isPersistent) {
					if (isOverflow) {
						pathType = PathType.PARTITION_PERSISTENT_OVERFLOW;
					} else {
						pathType = PathType.PARTITION_PERSISTENT;
					}
				} else if (isOverflow) {
					pathType = PathType.PARTITION_OVERFLOW;
				} else {
					pathType = PathType.PARTITION;
				}
			}

		} else {
			pathType = PathType.NOT_SUPPORTED;
		}

		return pathType;
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public boolean exists(String gridId, String gridPath)
	{
		Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
		Region region = rootRegion.getSubregion(gridPath);
		return region != null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BizMethod
	public Entry getRandomEntry(String gridId, String gridPath)
	{
		Cache cache = CacheFactory.getAnyInstance();
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = cache.getRegion(fullPath);
		if (region == null) {
			return null;
		}
		Entry entry = null;
		Set<Map.Entry> set = region.entrySet();
		for (Map.Entry e : set) {
			entry = new GemfireEntryImpl(e.getKey(), e.getValue());
		}
		return entry;
	}

	@SuppressWarnings("rawtypes")
	@BizMethod
	public JsonLite rebalance(String gridId, Set<String> includeGridPathSet, Set<String> excludeGridPathSet,
			long timeout, boolean isSimulate)
	{
		Cache cache = CacheFactory.getAnyInstance();
		ResourceManager manager = cache.getResourceManager();
		RebalanceFactory factory = manager.createRebalanceFactory();
		Set<String> includeRegionSet = createValidFullPathSet(includeGridPathSet);
		Set<String> excludeRegionSet = createValidFullPathSet(excludeGridPathSet);
		factory.includeRegions(includeRegionSet);
		factory.excludeRegions(excludeRegionSet);
		RebalanceOperation op;
		if (isSimulate) {
			op = factory.simulate();
		} else {
			op = factory.start();
		}
		try {
			RebalanceResults results;
			if (isSimulate) {
				results = op.getResults();
			} else {
				// Timeout if it's taking too long. Rebalancing will still
				// complete.
				results = op.getResults(timeout, TimeUnit.MILLISECONDS);
			}
			return toJsonLite(results);
		} catch (Exception ex) {
			return null;
		}
	}

	private Set<String> createValidFullPathSet(Set<String> gridPathSet)
	{
		Set<String> fullPathSet = null;
		Cache cache = CacheFactory.getAnyInstance();
		if (gridPathSet != null) {
			fullPathSet = new HashSet<String>(gridPathSet.size(), 1f);
			for (String gridPath : gridPathSet) {
				Region region = cache.getRegion(GridUtil.getFullPath(gridPath));
				if (region != null) {
					if (region instanceof PartitionedRegion) {
						fullPathSet.add(region.getFullPath());
					}
				}
			}
		}
		return fullPathSet;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JsonLite toJsonLite(RebalanceResults results)
	{
		if (results == null) {
			return null;
		}
		JsonLite jl = new JsonLite();
		// if (results.getPartitionRebalanceDetails() != null) {
		// jl.put("RebalanceDetails", results.getPartitionRebalanceDetails());
		// }
		// Set<PartitionRebalanceInfo> set =
		// results.getPartitionRebalanceDetails();
		// if (set != null) {
		// for (PartitionRebalanceInfo info : set) {
		// info.
		// }
		// }
		jl.put("TotalBucketCreateBytes", results.getTotalBucketCreateBytes());
		jl.put("TotalBucketCreatesCompleted", results.getTotalBucketCreatesCompleted());
		jl.put("TotalBucketCreateTime", results.getTotalBucketCreateTime());
		jl.put("TotalBucketTransferBytes", results.getTotalBucketTransferBytes());
		jl.put("TotalBucketTransfersCompleted", results.getTotalBucketTransfersCompleted());
		jl.put("TotalBucketTransferTime", results.getTotalBucketTransferTime());
		jl.put("TotalPrimaryTransfersCompleted", results.getTotalPrimaryTransfersCompleted());
		jl.put("TotalPrimaryTransferTime", results.getTotalPrimaryTransferTime());
		jl.put("TotalTime", results.getTotalTime());
		return jl;
	}

}
