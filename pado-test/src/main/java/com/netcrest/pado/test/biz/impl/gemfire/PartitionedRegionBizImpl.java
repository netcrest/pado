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
package com.netcrest.pado.test.biz.impl.gemfire;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.ForceReattemptException;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IUserPrincipal;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.gemfire.IGemfireGridContextServer;
import com.netcrest.pado.context.IDataInfo;
import com.netcrest.pado.context.ISimpleDataContext;
import com.netcrest.pado.context.ISimpleUserContext;
import com.netcrest.pado.context.IUserInfo;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.test.biz.TestException;

public class PartitionedRegionBizImpl
{
	@Resource
	private IBizContextServer bizContext;

	public PartitionedRegionBizImpl()
	{
	}

	@BizMethod
	public int[] getBucketIds()
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		if (rfc == null) {
			return null;
		}
		
		// Log if the user has admin rights.
		boolean isAdmin = false;
		IUserPrincipal userPrincipal = bizContext.getUserPrincipal();
		if (userPrincipal != null) {
			isAdmin = userPrincipal.isMemberOf("Admin");
		}
		if (isAdmin) {
			Logger.info("User has admin rights: " + userPrincipal.getUsername());
		}
		
		Region region = rfc.getDataSet();
		if (region instanceof PartitionedRegion == false) {
			return null;
		}
		Set<String> filterSet = (Set<String>) rfc.getFilter();

		PartitionedRegion pr = (PartitionedRegion) region;
		int bucketIds[];
		if (filterSet == null) {
			List<Integer> list = pr.getLocalPrimaryBucketsListTestOnly();
			bucketIds = new int[list.size()];
			for (int i = 0; i < bucketIds.length; i++) {
				bucketIds[i] = list.get(i);
			}
			return bucketIds;
		} else {
			List<Integer> bucketIdList = new ArrayList(filterSet.size() + 1);
			for (String routingKey : filterSet) {
				BucketRegion bucketRegion = pr.getBucketRegion(routingKey);
				bucketIdList.add(bucketRegion.getId());
			}
			bucketIds = new int[bucketIdList.size()];
			for (int i = 0; i < bucketIds.length; i++) {
				bucketIds[i] = bucketIdList.get(i);
			}
		}
		return bucketIds;
	}

	@BizMethod
	public Map<String, String> getBucketMap(int bucketId)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		PartitionedRegion region = (PartitionedRegion) rfc.getDataSet();
		Map map = null;
		try {
			Region<String, String> bucketRegion = region.getDataStore().getInitializedBucketForId(null, bucketId);
			map = new HashMap(bucketRegion.size(), 1f);
			for (Map.Entry<String, String> entry : bucketRegion.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
		} catch (ForceReattemptException e) {
			e.printStackTrace();
		}
		return map;
	}

	@BizMethod
	public void putEntry(String key, String value)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		PartitionedRegion region = (PartitionedRegion) rfc.getDataSet();
		region.put(key, value);
	}

	@BizMethod
	public String getServerIdList()
	{
		// fc should be null since this is onServer call
		FunctionContext fc = ((IGemfireGridContextServer) (bizContext.getGridContextServer())).getFunctionContext();
		ISimpleUserContext userContext = (ISimpleUserContext) bizContext.getUserContext();
		IUserInfo userInfo = userContext.getUserInfo();
		String userId = (String) userInfo.getAttribute("FUserId");
		String location = (String) userInfo.getAttribute("FLocation");
		String org = (String) userInfo.getAttribute("FOrg");
		Boolean isOnBehalf = (Boolean) userInfo.getAttribute("FIsOnBehalf");
		Logger.info("PartitionedRegionBizImpl.getServerIdList(): userId=" + userId + ", " + "location=" + location
				+ ", org=" + org + ", isOnBehalf=" + isOnBehalf);

		ISimpleDataContext dataContext = (ISimpleDataContext) bizContext.getDataContext();
		IDataInfo dataInfo = dataContext.getDataInfo();
		Byte businessConfidentialType = (Byte) dataInfo.getAttribute("FBusinessConfidentialType");
		Byte medication = (Byte) dataInfo.getAttribute("FMedication");
		Integer orderNumber = (Integer) dataInfo.getAttribute("FOrderNumber");
		Integer rxNumber = (Integer) dataInfo.getAttribute("FRxNumber");
		Logger.info("PartitionedRegionBizImpl.getServerIdList(): businessConfidentialType=" + businessConfidentialType
				+ ", " + "medication=" + medication + ", orderNumber=" + orderNumber + ", rxNumber=" + rxNumber);

		Cache cache = CacheFactory.getAnyInstance();
		String memberId = cache.getDistributedSystem().getDistributedMember().getId();
		return memberId;
	}
	
	@BizMethod
	public String getServerIdMap()
	{
		return getServerIdList();
	}

	@BizMethod
	public int testPadoException(String message) throws TestException
	{
		if (message == null) {
			throw new TestException("Server error: message cannot be null");
		}
		return message.length();
	}

	@BizMethod
	public int testNonPadoException(String message) throws InvalidObjectException
	{
		if (message == null) {
			throw new InvalidObjectException("Server error: message cannot be null");
		}
		return message.length();
	}

	@BizMethod
	public int add(int x, int y)
	{
		return x + y;
	}
}
