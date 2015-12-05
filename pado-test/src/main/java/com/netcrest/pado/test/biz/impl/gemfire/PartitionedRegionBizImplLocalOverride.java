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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;
import com.netcrest.pado.test.biz.TestException;

public class PartitionedRegionBizImplLocalOverride implements IPartitionedRegionBiz, IBizLocal
{
	@Resource
	private IPartitionedRegionBiz biz;
	
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IPartitionedRegionBiz)biz;
	}
	
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public int[] getBucketIds()
	{
		return biz.getBucketIds();
	}

	@Override
	public Map<String, String> getBucketMap(int bucketId)
	{
		if (bucketId < 0) {
			return null;
		}
		return biz.getBucketMap(bucketId);
	}

	@Override
	public void putEntry(String key, String value)
	{
		if (key == null || value == null) {
			return;
		}
		biz.putEntry(key, value);
	}

	@Override
	public List<String> getServerIdList()
	{
		return biz.getServerIdList();
	}

	@Override
	public int testPadoException(String message) throws TestException
	{
		if (message == null) {
			throw new TestException("message cannot be null");
		}
		return biz.testPadoException(message);
	}

	@Override
	public int testNonPadoException(String message) throws InvalidObjectException
	{
		if (message == null) {
			throw new InvalidObjectException("message cannot be null");
		}
		return biz.testNonPadoException(message);
	}

	@Override
	public int add(int x, int y)
	{
		// Change x to alter the final value
		x += 10;
		return biz.add(x, y);
	}

	@Override
	public Map<String, List<String>> getServerIdMap()
	{
		return biz.getServerIdMap();
	}
}
