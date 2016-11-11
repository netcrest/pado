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

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.gemfire.IGemfireGridContextServer;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.internal.factory.InternalFactory;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalClientMetadata;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalClientMetadata;
import com.netcrest.pado.temporal.gemfire.impl.TemporalCacheListener;
import com.netcrest.pado.util.GridUtil;

public class TemporalAdminBizImpl<K, V> implements ITemporalAdminBiz<K, V>
{
	@Resource
	IBizContextServer bizContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@BizMethod
	@Override
	public TemporalClientMetadata getMetadata(String gridPath)
	{
		TemporalCacheListener cl;
		if (gridPath.startsWith("/")) {
			cl = GemfireTemporalManager.getTemporalCacheListener(gridPath);
		} else {
			Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
			cl = GemfireTemporalManager.getTemporalCacheListener(rootRegion.getFullPath() + "/" + gridPath);
			if (cl == null) {
				return InternalFactory.getInternalFactory().getDefaultTemporalClientMetadata();
			}
		}
		TemporalClientFactory clientFactory = cl.getTemporalListFactory();
		TemporalClientMetadata clientMetadata = clientFactory.getClientMetadata();
		return clientMetadata;
	}

	/**
	 * Not used. Local biz invokes {@link #getMetadata(String)}
	 */
	@BizMethod
	@Override
	public GemfireTemporalClientMetadata getMetadata()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public TemporalType[] getAllTemporalTypes()
	{
		return GemfireTemporalManager.getAllTemporalTypes();
	}

	/**
	 * Not used.
	 */
	@Override
	public String getGridPath()
	{
		return null;
	}

	/**
	 * Not used.
	 */
	@Override
	public void setGridPath(String gridPath)
	{
	}

	/**
	 * Not used. Local biz invokes {@link IGridMapBiz#get(K)}.
	 */
	@BizMethod
	@Override
	public ITemporalData<K> get(ITemporalKey<K> temporalKey)
	{
		return null;
	}

	/**
	 * Not used. Local biz invokes {@link IGridMapBiz#getAll(Set)}.
	 */
	@BizMethod
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAll(Set<ITemporalKey<K>> keys)
	{
		return null;
	}

	/**
	 * Not used. Local biz invokes {@link IGridMapBiz#put(K, V)}.
	 */
	@BizMethod
	@Override
	public ITemporalData<K> put(ITemporalKey<K> temporalKey, ITemporalData<K> data)
	{
		return null;
	}

	/**
	 * Not used. Local biz invokes {@link IGridMapBiz#putAll(Map)}.
	 */
	@BizMethod
	@Override
	public void putAll(Map<ITemporalKey<K>, ITemporalData<K>> map)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BizMethod
	@Override
	public TemporalDataList getTemporalDataList(K identityKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		return tm.getTemporalDataList(identityKey);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public void clearTemporalList(K identityKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return;
		}
		tm.clearTemporalList(identityKey);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public ITemporalData<K> removePermanently(ITemporalKey<K> temporalKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		return tm.removePermanently(temporalKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@Override
	public void dumpServer(K identityKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return;
		}
		tm.dump(identityKey);
	}

	/**
	 * Not used. Local biz creates BulkLoader.
	 */
	@Override
	public ITemporalBulkLoader createBulkLoader(int batchSize)
	{
		return null;
	}

	/**
	 * Not used. Local biz provides client factory.
	 */
	@Override
	public TemporalClientFactory<K, V> getTemporalClientFactory()
	{
		return null;
	}

	/**
	 * Not used. Local biz provides {@link IGridMapBiz}.
	 */
	@Override
	public IGridMapBizLink<ITemporalKey<K>, ITemporalData<K>> getGridMapBiz()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	public void setEnabled(boolean enabled, boolean spawnThread)
	{
		Object obj[] = bizContext.getGridContextServer().getAdditionalArguments();
		if (obj == null || obj.length == 0) {
			return;
		}
		String gridPath = (String)obj[0];
		TemporalManager tm = TemporalManager.getTemporalManager(GridUtil.getFullPath(gridPath));
		if (tm == null) {
			return;
		}
		tm.setEnabled(enabled, true, spawnThread);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	public boolean isEnabled()
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		if (rfc == null) {
			return false;
		}
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return false;
		}
		return tm.isEnabled();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	public void setEnabledAll(boolean enabled, boolean spawnThread)
	{
		TemporalManager.setEnabledAll(enabled, true, spawnThread);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	public boolean isEnabledAll()
	{
		return TemporalManager.isEnabledAll();
	}
}
