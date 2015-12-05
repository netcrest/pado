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

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.IncompatibleTypeException;
import com.netcrest.pado.exception.NestedPathExistsException;
import com.netcrest.pado.gemfire.exception.NestedRegionExistsException;
import com.netcrest.pado.gemfire.util.RegionUtil;
import com.netcrest.pado.info.PathInfo;

public class PathBizImplLocal implements IPathBiz, IBizLocal
{
	@Resource
	IPathBiz biz;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	private String setGridId(String gridId)
	{
		if (gridId == null) {
			gridId = biz.getBizContext().getGridService().getDefaultGridId();
		}
		biz.getBizContext().getGridContextClient().setGridIds(gridId);
		return gridId;
	}

	@SuppressWarnings("rawtypes")
	private Region getRegion(String gridId, String gridPath)
	{
		String fullPath = getBizContext().getGridService().getFullPath(gridId, gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		return region;
	}

	private boolean __createPath(String gridId, String gridPath, String pathType, boolean recursive)
	{
		gridId = setGridId(gridId);
		return biz.__createPath(gridId, gridPath, pathType, null, null, null, 1, 113, recursive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createPath(String gridId, String gridPath, PathType pathType, boolean recursive)
	{
		if (pathType == null) {
			pathType = PathType.REPLICATE;
		}
		return __createPath(gridId, gridPath, pathType.name(), recursive);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void clear(String gridId, String gridPath)
	{
		Region region = getRegion(gridId, gridPath);
		if (region != null) {
			region.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(String gridId, String path, boolean force)
	{
		if (force) {
			gridId = setGridId(gridId);
			biz.clear(gridId, path, force);
		} else {
			clear(gridId, path);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void remove(String gridId, String gridPath, boolean recursive) throws NestedPathExistsException
	{
		String fullPath = biz.getBizContext().getGridService().getFullPath(gridId, gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		try {
			RegionUtil.removeRegionLocal(region, recursive);
		} catch (NestedRegionExistsException ex) {
			throw new NestedPathExistsException(ex);
		}
		setGridId(gridId);
		biz.remove(gridId, gridPath, recursive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PathInfo> getAllTemporalPathInfoList(String gridId)
	{
		gridId = setGridId(gridId);
		return biz.getAllTemporalPathInfoList(gridId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createPath(String gridId, String gridPath, String refid, boolean isTemporal, boolean isDynamicLucene, boolean recursive)
	{
		gridId = setGridId(gridId);
		return biz.createPath(gridId, gridPath, refid, isTemporal, isDynamicLucene, recursive);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean __createPath(String gridId, String gridPath, String pathType, String diskStoreName,
			String gatewaySenderIds, String colocatedWithGridPath, int redundantCopies, int totalBucketCount,
			boolean recursive)
	{
		gridId = setGridId(gridId);
		if (pathType != null) {
			pathType = pathType.toUpperCase();
		}
		return biz.__createPath(gridId, gridPath, pathType, diskStoreName, gatewaySenderIds, colocatedWithGridPath, redundantCopies,
				totalBucketCount, recursive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean createPath(String gridId, String gridPath, PathType pathType, String diskStoreName,
			String gatewaySenderIds, String colocatedWithGridPath, int redundantCopies, int totalBucketCount,
			boolean recursive)
	{
		return __createPath(gridId, gridPath, pathType.name(), diskStoreName, gatewaySenderIds, colocatedWithGridPath,
				redundantCopies, totalBucketCount, recursive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getSize(String gridId, String gridPath)
	{
		setGridId(gridId);
		return biz.getSize(gridId, gridPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(String gridId, String sourceGridPath, String targetGridPath) throws IncompatibleTypeException
	{
		setGridId(gridId);
		biz.copy(gridId, sourceGridPath, targetGridPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathType getPathType(String gridId, String gridPath)
	{
		setGridId(gridId);
		return biz.getPathType(gridId, gridPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(String gridId, String gridPath)
	{
		setGridId(gridId);
		return biz.exists(gridId, gridPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public JsonLite rebalance(String gridId, Set<String> includeGridPathSet, Set<String> excludeGridPathSet,
			long timeout, boolean isSimulate)
	{
		setGridId(gridId);
		return biz.rebalance(gridId, includeGridPathSet, excludeGridPathSet, timeout, isSimulate);
	}
}
