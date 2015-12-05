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
package com.netcrest.pado.gemfire.biz.biz;

import java.util.List;
import java.util.Map;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.Pool;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.annotation.BizClass;

@BizClass(name="IGemfireBiz")
public interface IGemfireBiz extends IBiz
{
	/**
	 * Returns the app ID of this IBiz instance.
	 */
	String getAppId();
	
	/**
	 * Returns the Gemfire cache instance.
	 */
	Cache getCache();
	
	/**
	 * Returns the pool used to connect to Pado.
	 */
	Pool getPadoPool();
	
	/**
	 * Returns a list with all of the pools used to connect to the specified
	 * grid. It always returns a non-empty immutable list that contains
	 * pools returned by {@link #getPool(String)} and {@link #getSharedPool(String)}.
	 * @param gridId Grid ID
	 */
	List<Pool> getPoolList(String gridId);
	
	/**
	 * Returns the pool used to authenticate the user in the specified grid.
	 * The returned pool has {@link Pool#getMultiuserAuthentication()} == true
	 * if Pado has multi-user authentication enabled. Otherwise, it is same
	 * as the pool returned by {@link #getSharedPool(String)}.
	 * @param gridId Grid ID
	 */
	Pool getPool(String gridId);
	
	/**
	 * Returns the shared pool. The returned pool has {@link Pool#getMultiuserAuthentication()} == false, 
	 * allowing non-empty local regions. The returned pool may be same as the
	 * pool returned by {@link #getPool(String)} if multi-user authentication
	 * is disabled in Pado.
	 * @param gridId Grid ID
	 * @return
	 */
	Pool getSharedPool(String gridId);
	
	/**
	 * Returns the RegionService object used exclusively to connect to the
	 * specified grid. It returns null if multi-user authentication is disabled
	 * in Pado.
	 * @param gridId Grid ID
	 * @return
	 */
	RegionService getRegionService(String gridId);
	
	/**
	 * Returns the root path for the specified grid ID. Note that if 
	 * the root path is not defined then Pado automatically assigns
	 * the root path to the grid ID.
	 * @param gridId Grid ID
	 */
	String getRootPath(String gridId);
	
	/**
	 * Returns the specified gridId's region. I returns null if the region
	 * is not found.
	 * @param gridId Grid ID
	 * @param relativePath relative to the root path
	 */
	Region getRegion(String gridId, String gridPath);
	
	/**
	 * Returns IDs of grids that contain the specified grid path. It
	 * always returns a non-null array.
	 * @param gridPath relative to the root path
	 */
	String[] getGridIds(String gridPath);
	
	/**
	 * Returns a map of &lt;gridId, Region&gt; that contains all
	 * grids that have the specified grid path. It always returns a non-null
	 * immutable map.
	 * @param gridPath relative to the root path
	 */
	Map<String, Region> getGridRegionMap(String gridPath);
}
