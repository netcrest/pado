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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.netcrest.pado.info.BucketInfo;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.CacheServerInfo;
import com.netcrest.pado.info.GridInfo;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.info.ServerInfo;

public class GemfireGridInfo extends GridInfo implements DataSerializable
{
	private static final long serialVersionUID = 1L;

	private boolean clientConnectionSingleHopEnabled;
	private boolean clientConnectionMultiuserAuthenticationEnabled;

	public GemfireGridInfo()
	{
	}

	/**
	 * Updates this object with contents of the specified GridInfo object
	 * 
	 * @param gridInfo
	 * @return Returns true if the passed in GridInfo object is different than
	 *         this object and this object has been updated.
	 */
	public boolean update(GridInfo gridInfo)
	{
		boolean different = false;
		if (gridInfo != null) {
			different = equals(gridInfo) == false;
			if (different) {
				setGridId(gridInfo.getGridId());
				setLocation(gridInfo.getLocation());
				setChildGridIds(gridInfo.getChildGridIds());
				setGridRootPath(((GemfireGridInfo) gridInfo).getGridRootPath());
				setParentGridIds(gridInfo.getParentGridIds());
				setConnectionName(gridInfo.getConnectionName());
				setSharedConnectionName(gridInfo.getSharedConnectionName());
				setIndexMatrixConnectionName(gridInfo.getIndexMatrixConnectionName());
				setLocators(gridInfo.getLocators());
				setClientIndexMatrixConnectionName(gridInfo.getClientIndexMatrixConnectionName());
				setClientConnectionName(gridInfo.getClientConnectionName());
				setClientSharedConnectionName(gridInfo.getClientSharedConnectionName());
				setClientConnectionSingleHopEnabled(((GemfireGridInfo) gridInfo).isClientConnectionSingleHopEnabled());
				setClientConnectionMultiuserAuthenticationEnabled(((GemfireGridInfo) gridInfo)
						.isClientConnectionMultiuserAuthenticationEnabled());
				setClientLocators(gridInfo.getClientLocators());
				rootPathInfo = ((GemfireCacheInfo) gridInfo.getCacheInfo()).getPathInfo(getGridRootPath());
			}
		}
		return different;
	}

	public boolean isClientConnectionSingleHopEnabled()
	{
		return clientConnectionSingleHopEnabled;
	}

	public void setClientConnectionSingleHopEnabled(boolean clientConnectionSingleHopEnabled)
	{
		this.clientConnectionSingleHopEnabled = clientConnectionSingleHopEnabled;
	}

	public boolean isClientConnectionMultiuserAuthenticationEnabled()
	{
		return clientConnectionMultiuserAuthenticationEnabled;
	}

	public void setClientConnectionMultiuserAuthenticationEnabled(boolean clientConnectionMultiuserAuthenticationEnabled)
	{
		this.clientConnectionMultiuserAuthenticationEnabled = clientConnectionMultiuserAuthenticationEnabled;
	}

	public List<ServerInfo> getServerInfoList(String regionPath)
	{
		List<ServerInfo> serverInfoList = serverInfoMap.get(regionPath);
		if (serverInfoList == null) {
			if (cacheInfoList != null) {
				serverInfoList = new ArrayList(cacheInfoList.size() + 1);
				for (CacheInfo cacheInfo : cacheInfoList) {
					for (CacheServerInfo cacheServerInfo : cacheInfo.getCacheServerInfoList()) {
						ServerInfo serverInfo = new GemfireServerInfo(this, (GemfireCacheInfo) cacheInfo,
								(GemfireCacheServerInfo) cacheServerInfo, regionPath);
						serverInfoList.add(serverInfo);
					}
				}
				Collections.sort(serverInfoList);
			}
			serverInfoMap.put(regionPath, serverInfoList);
		}
		return serverInfoList;
	}

	public PathInfo getGridRegionInfo()
	{
		return cacheInfo.getPathInfo(getGridRootPath() + "/__pado/grid");
	}

	public PathInfo getServerRegionInfo()
	{
		return cacheInfo.getPathInfo(getGridRootPath() + "/__pado/server");
	}

	private List<BucketInfo> getBucketInfoList(String regionPath, boolean isPrimary)
	{
		List<BucketInfo> bucketInfoList = null;
		if (cacheInfoList != null) {
			bucketInfoList = new ArrayList();
			for (CacheInfo cacheInfo : cacheInfoList) {
				GemfireRegionInfo regionInfo = (GemfireRegionInfo)cacheInfo.getPathInfo(regionPath);
				if (regionInfo == null) {
					continue;
				}
				List<BucketInfo> list;
				if (isPrimary) {
					list = regionInfo.getPrimaryBucketInfoList();
				} else {
					list = regionInfo.getRedundantBucketInfoList();
				}
				if (list == null) {
					continue;
				}
				for (BucketInfo bucketInfo : list) {
					bucketInfo.setServerName(cacheInfo.getName());
					bucketInfo.setServerId(cacheInfo.getId());
				}
				bucketInfoList.addAll(list);
			}
		}
		return bucketInfoList;
	}

	public List<BucketInfo> getPrimaryBucketInfoList(String regionPath)
	{
		return getBucketInfoList(regionPath, true);
	}

	public List<BucketInfo> getRedundantBucketInfoList(String regionPath)
	{
		return getBucketInfoList(regionPath, false);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(childGridIds);
		result = prime * result + ((clientLocators == null) ? 0 : clientLocators.hashCode());
		result = prime * result + ((gridId == null) ? 0 : gridId.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((locators == null) ? 0 : locators.hashCode());
		result = prime * result + ((clientIndexMatrixConnectionName == null) ? 0 : clientIndexMatrixConnectionName.hashCode());
		result = prime * result + ((clientConnectionName == null) ? 0 : clientConnectionName.hashCode());
		result = prime * result + Arrays.hashCode(parentGridIds);
		result = prime * result + ((clientSharedConnectionName == null) ? 0 : clientSharedConnectionName.hashCode());
		result = prime * result + ((indexMatrixConnectionName == null) ? 0 : indexMatrixConnectionName.hashCode());
		result = prime * result + ((connectionName == null) ? 0 : connectionName.hashCode());
		result = prime * result + ((sharedConnectionName == null) ? 0 : sharedConnectionName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof GemfireAppGridInfo == false) // @dpark
			return false;
		GemfireGridInfo other = (GemfireGridInfo) obj;
		if (!Arrays.equals(childGridIds, other.childGridIds))
			return false;
		if (clientLocators == null) {
			if (other.clientLocators != null)
				return false;
		} else if (!clientLocators.equals(other.clientLocators))
			return false;
		if (gridId == null) {
			if (other.gridId != null)
				return false;
		} else if (!gridId.equals(other.gridId))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (locators == null) {
			if (other.locators != null)
				return false;
		} else if (!locators.equals(other.locators))
			return false;
		if (!Arrays.equals(parentGridIds, other.parentGridIds))
			return false;
		if (clientIndexMatrixConnectionName == null) {
			if (other.clientIndexMatrixConnectionName != null)
				return false;
		} else if (!clientIndexMatrixConnectionName.equals(other.clientIndexMatrixConnectionName))
			return false;
		if (clientConnectionName == null) {
			if (other.clientConnectionName != null)
				return false;
		} else if (!clientConnectionName.equals(other.clientConnectionName))
			return false;
		if (clientSharedConnectionName == null) {
			if (other.clientSharedConnectionName != null)
				return false;
		} else if (!clientSharedConnectionName.equals(other.clientSharedConnectionName))
			return false;
		if (indexMatrixConnectionName == null) {
			if (other.indexMatrixConnectionName != null)
				return false;
		} else if (!indexMatrixConnectionName.equals(other.indexMatrixConnectionName))
			return false;
		if (connectionName == null) {
			if (other.connectionName != null)
				return false;
		} else if (!connectionName.equals(other.connectionName))
			return false;
		if (sharedConnectionName == null) {
			if (other.sharedConnectionName != null)
				return false;
		} else if (!sharedConnectionName.equals(other.sharedConnectionName))
			return false;
		return true;
	}
	
	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		// AppGridInfo
		gridId = DataSerializer.readString(input);
		location = DataSerializer.readString(input);
		gridRootPath = DataSerializer.readString(input);
		connectionName = DataSerializer.readString(input);
		sharedConnectionName = DataSerializer.readString(input);
		indexMatrixConnectionName = DataSerializer.readString(input);
		locators = DataSerializer.readString(input);
		parentGridIds = DataSerializer.readStringArray(input);
		childGridIds = DataSerializer.readStringArray(input);
		clientIndexMatrixConnectionName = DataSerializer.readString(input);
		clientConnectionName = DataSerializer.readString(input);
		clientSharedConnectionName = DataSerializer.readString(input);
		clientLocators = DataSerializer.readString(input);
		clientConnectionSingleHopEnabled = DataSerializer.readPrimitiveBoolean(input);
		clientConnectionMultiuserAuthenticationEnabled = DataSerializer.readPrimitiveBoolean(input);
		rootPathInfo = DataSerializer.readObject(input);

		// GridInfo
		bizSet = DataSerializer.readObject(input);
		cacheInfo = DataSerializer.readObject(input);
		cacheInfoList = DataSerializer.readObject(input);
		if (cacheInfo != null) {
			this.rootPathInfo = ((GemfireCacheInfo) cacheInfo).getPathInfo(getGridRootPath());
		}
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		// AppGridInfo
		DataSerializer.writeString(gridId, output);
		DataSerializer.writeString(location, output);
		DataSerializer.writeString(gridRootPath, output);
		DataSerializer.writeString(connectionName, output);
		DataSerializer.writeString(sharedConnectionName, output);
		DataSerializer.writeString(indexMatrixConnectionName, output);
		DataSerializer.writeString(locators, output);
		DataSerializer.writeStringArray(parentGridIds, output);
		DataSerializer.writeStringArray(childGridIds, output);
		DataSerializer.writeString(clientIndexMatrixConnectionName, output);
		DataSerializer.writeString(clientConnectionName, output);
		DataSerializer.writeString(clientSharedConnectionName, output);
		DataSerializer.writeString(clientLocators, output);
		DataSerializer.writePrimitiveBoolean(clientConnectionSingleHopEnabled, output);
		DataSerializer.writePrimitiveBoolean(clientConnectionMultiuserAuthenticationEnabled, output);
		DataSerializer.writeObject(rootPathInfo, output);
		
		// GridInfo
		DataSerializer.writeObject(bizSet, output);
		DataSerializer.writeObject(cacheInfo, output);
		DataSerializer.writeObject(cacheInfoList, output);
	}

}
