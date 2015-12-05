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
package com.netcrest.pado.gemfire.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;

public class GemfireRegionInfo extends PathInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;
	
	private List<BucketInfo> primaryBucketInfoList;
	private List<BucketInfo> redundantBucketInfoList;

	/**
	 * Constructs an empty RegionInfo object. The caller is responsible for
	 * supplying RegionInfo attributes.
	 */
	public GemfireRegionInfo()
	{
	}
	
//	public GemfireRegionInfo(boolean isReal)
//	{
//		this.isReal = isReal;
//	}

	/**
	 * Constructs a RegionInfo object without the parent information. The caller
	 * must supply the parent information. It does not build the child list.
	 * 
	 * @param region
	 */
	public GemfireRegionInfo(Region region)
	{
		this(region, false);
	}
	
	/**
	 * Constructs a RegionInfo object without the parent information.
	 * 
	 * @param region The region from which RegionInfo attributes to be extracted.
	 * @param recursive true to include all children RegionInfo objects or false
	 *            to include none. false is same as invoking {@link GemfireRegionInfo#RegionInfo(Region)}.
	 */
	public GemfireRegionInfo(Region region, boolean recursive)
	{
		init(region);
		if (recursive) {
			Set<Region<?, ?>> regionSet = region.subregions(false);
			for (Region<?, ?> region2 : regionSet) {
				createChild(region2, this);
			}
		}
	}
	
	/**
	 * Initializes without children.
	 * @param region The region from which RegionInfo is extracted.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(Region region)
	{
		if (region == null) {
			return;
		}
		DistributedMember member = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember();
		setName(region.getName());
		setFullPath(region.getFullPath());
		GemfireRegionAttributeInfo attrInfo = new GemfireRegionAttributeInfo();
		RegionAttributes<?, ?> attr = region.getAttributes();
		attrInfo.setAttribute(GemfireRegionAttributeInfo.DATA_POLICY, attr.getDataPolicy().toString());
		attrInfo.setAttribute(GemfireRegionAttributeInfo.SCOPE, attr.getScope().toString());
		if (region instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) region;
			PartitionAttributes pattr = pr.getPartitionAttributes();
			attrInfo.setAttribute(GemfireRegionAttributeInfo.LOCAL_MAX_MEMORY, pattr.getLocalMaxMemory() + "");
			attrInfo.setAttribute(GemfireRegionAttributeInfo.REDUNDANT_COPIES, pattr.getRedundantCopies() + "");
			attrInfo.setAttribute(GemfireRegionAttributeInfo.TOTAL_MAX_MEMORY, pattr.getTotalMaxMemory() + "");
			attrInfo.setAttribute(GemfireRegionAttributeInfo.TOTAL_NUM_BUCKETS, pattr.getTotalNumBuckets() + "");

			// data store is null if it's a proxy, i.e., LOCAL_MAX_MEMORY=0
			if (pr.getDataStore() != null) {
				Set<BucketRegion> localtBuketSet = pr.getDataStore().getAllLocalBucketRegions();
				List<BucketInfo> primaryList = new ArrayList<BucketInfo>();
				List<BucketInfo> redundantList = new ArrayList<BucketInfo>();
				this.size = 0;
				for (BucketRegion br : localtBuketSet) {
					BucketInfo bucketInfo = new GemfireBucketInfo(br.getId(), br.getBucketAdvisor().isPrimary(), br.size(), br.getTotalBytes());
//					InternalDistributedMember m = pr.getBucketPrimary(br.getId());
//					if (m.getId().equals(member.getId())) {
					if (bucketInfo.isPrimary()) {
						primaryList.add(bucketInfo);
						this.size += bucketInfo.getSize();
					} else {
						redundantList.add(bucketInfo);
					}
				}
				Collections.sort(primaryList);
				Collections.sort(redundantList);
				setPrimaryBucketInfoList(primaryList);
				setRedundantBucketInfoList(redundantList);
			}
		} else {
			this.size = region.size();
		}
		setAttrInfo(attrInfo);
		temporalType = GemfireTemporalManager.getTemporalType(region);
		if (region.isDestroyed() == false && region.isEmpty() == false) {
			Set<Map.Entry> regionEntrySet = region.entrySet();
			for (Map.Entry entry : regionEntrySet) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				keyTypeName = key.getClass().getName();
				valueTypeName = value.getClass().getName();
				break;
			}
		}
	}

	/**
	 * Creates and returns child RegionInfo with all of its children sorted.
	 * @param region  The region from which RegionInfo is extracted.
	 * @param parentInfo The parent RegionInfo object to update with
	 *             the region's children information.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GemfireRegionInfo createChild(Region region, GemfireRegionInfo parentInfo)
	{
		GemfireRegionInfo regionInfo = new GemfireRegionInfo(region, false);
		if (parentInfo != null) {
			parentInfo.setParent(parentInfo);
			parentInfo.getChildList().add(regionInfo);
		}
		Set<Region<?, ?>> regionSet = region.subregions(false);
		for (Region<?, ?> region2 : regionSet) {
			createChild(region2, regionInfo);
		}
		Collections.sort(regionInfo.getChildList());
		return regionInfo;
	}
	
	/**
	 * Returns grid region relative path, i.e., without the
	 * top-level region name. The returned path never begins
	 * with "/".
	 */
	@Override
	public String getGridRelativePath()
	{
		if (fullPath == null) {
			return null;
		}
		return GemfireGridUtil.getChildPath(fullPath);
	}

	/**
	 * Returns the sorted list of primary BucketInfo objects if this region is partitioned.
	 * If returns null if this region is not partitioned or partitioned as proxy,
	 * i.e., local max memory is configured to 0.
	 */
	public List<BucketInfo> getPrimaryBucketInfoList()
	{
		return primaryBucketInfoList;
	}

	public void setPrimaryBucketInfoList(List<BucketInfo> primaryBucketInfoList)
	{
		this.primaryBucketInfoList = primaryBucketInfoList;
	}
	
	/**
	 * Returns the total number of buckets assigned to this partitioned region.
	 * It returns 0 if it's not a partitioned region.
	 */
	public int getTotalBucketCount()
	{
		if (primaryBucketInfoList == null) {
			return 0;
		} else {
			return primaryBucketInfoList.size();
		}
	}

	/**
	 * Returns the sorted list of redundant BucketInfo objects if this region is partitioned.
	 * If returns null if this region is not partitioned or partitioned as proxy,
	 * i.e., local max memory is configured to 0.
	 */
	public List<BucketInfo> getRedundantBucketInfoList()
	{
		return redundantBucketInfoList;
	}

	public void setRedundantBucketInfoList(List<BucketInfo> redundantBucketInfoList)
	{
		this.redundantBucketInfoList = redundantBucketInfoList;
	}

	public boolean isDataPolicyPartitionedRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.PARTITION.toString())
				|| dataPolicy.equals(DataPolicy.PERSISTENT_PARTITION.toString())
				|| dataPolicy.equals(DataPolicy.PERSISTENT_REPLICATE.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyPartitionedRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyNormalRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.NORMAL.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyNormalRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyPreloadedRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.PRELOADED.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyPreloadedRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyReplicateRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.REPLICATE.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyReplicateRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyPersistentPartitionRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.PERSISTENT_PARTITION.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyPersistentPartitionRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyPersistentReplicateRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.PERSISTENT_REPLICATE.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyPersistentReplicateRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isDataPolicyEmptyRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String dataPolicy = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.DATA_POLICY);
		boolean flag = dataPolicy != null && dataPolicy.equals(DataPolicy.EMPTY.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isDataPolicyEmptyRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isScopeLocalRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String scope = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.SCOPE);
		boolean flag = scope != null && scope.equals(Scope.LOCAL.toString()) || scope.equals(Scope.LOCAL.toString());

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isScopeLocalRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isScopeGlobalRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String scope = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.SCOPE);
		boolean flag = scope != null && scope.equals(Scope.GLOBAL.toString())
				|| scope.equals(Scope.GLOBAL.toString());

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isScopeGlobalRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isScopeAckRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String scope = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.SCOPE);
		boolean flag = scope != null && scope.equals(Scope.DISTRIBUTED_ACK.toString())
				|| scope.equals(Scope.DISTRIBUTED_ACK.toString());

		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isScopeAckRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	public boolean isScopeNoAckRegion(boolean recursive)
	{
		if (attrInfo == null) {
			return false;
		}
		String scope = ((GemfireRegionAttributeInfo)attrInfo).getAttribute(GemfireRegionAttributeInfo.SCOPE);
		boolean flag = scope != null && scope.equals(Scope.DISTRIBUTED_NO_ACK.toString())
				|| scope.equals(Scope.DISTRIBUTED_NO_ACK.toString());
		
		if (flag == false && recursive) {
			for (PathInfo pathInfo : childList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)pathInfo;
				flag = regionInfo.isScopeNoAckRegion(recursive);
				if (flag) {
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		name = DataSerializer.readString(input);
//		isReal = DataSerializer.readBoolean(input);
		fullPath = DataSerializer.readString(input);
		childList = DataSerializer.readObject(input);
		attrInfo = DataSerializer.readObject(input);
		temporalType = DataSerializer.readObject(input);
		keyTypeName = DataSerializer.readString(input);
		valueTypeName = DataSerializer.readString(input);
		size = DataSerializer.readPrimitiveInt(input);
		primaryBucketInfoList = DataSerializer.readObject(input);
		redundantBucketInfoList = DataSerializer.readObject(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writeString(name, output);
//		DataSerializer.writeBoolean(isReal, output);
		DataSerializer.writeString(fullPath, output);
		DataSerializer.writeObject(childList, output);
		DataSerializer.writeObject(attrInfo, output);
		DataSerializer.writeObject(temporalType, output);
		DataSerializer.writeString(keyTypeName, output);
		DataSerializer.writeString(valueTypeName, output);
		DataSerializer.writePrimitiveInt(size, output);
		DataSerializer.writeObject(primaryBucketInfoList, output);
		DataSerializer.writeObject(redundantBucketInfoList, output);
	}

}
