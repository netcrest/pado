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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalClientMetadata;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalException;
import com.netcrest.pado.temporal.TemporalType;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue;
import com.netcrest.pado.temporal.gemfire.impl.TemporalBulkLoader;

public class TemporalAdminBizImplLocal<K, V> implements ITemporalAdminBiz<K, V>, IBizLocal
{
	// Temporal metadata is retrieved from the grid only once during
	// the first instance of ITemporalAdminBiz.
	private static TemporalClientMetadata metadata;
	
	private IPado pado;
	private ITemporalAdminBiz<K, V> biz;
	private IGridMapBiz<ITemporalKey<K>, ITemporalData<K>> gridMapBiz;
	private String gridPath;
	private TemporalClientFactory<K, V> clientFactory;

	public TemporalAdminBizImplLocal()
	{
	}

	public TemporalAdminBizImplLocal(String gridPath)
	{
		this.gridPath = gridPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.pado = pado;
		this.biz = (ITemporalAdminBiz<K, V>) biz;
		this.gridMapBiz = (IGridMapBiz<ITemporalKey<K>, ITemporalData<K>>) pado.getCatalog().newInstance(
				IGridMapBiz.class);
		if (args != null && args.length > 0) {
			this.gridPath = (String) args[0];
		}
		if (gridPath != null) {
			gridMapBiz.setGridPath(gridPath);
			this.biz.getBizContext().getGridContextClient().setGridPath(gridPath);
			loadMetadata();
		}
	}

	private void resetBizContext()
	{
		this.biz.getBizContext().reset();
		String gridIds[] = this.biz.getBizContext().getGridService().getGridIds(gridPath);
		if (gridIds != null && gridIds.length > 0) {
			this.biz.getBizContext().getGridContextClient().setGridIds(gridIds[0]);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadMetadata() throws TemporalException
	{
		TemporalClientMetadata metadata = getMetadata();
		if (metadata == null) {
			throw new TemporalException(
					"TemporalAdminBizLocalImpl.loadMetadata(): Unable to load temporal metadata for the grid path: " + gridPath
							+ ". The grid path must be configured to store temporal data. Please verify Pado configuration.");
		}
		try {
			clientFactory = TemporalClientFactory.getTemporalClientFactory(metadata);
		} catch (Exception ex) {
			throw new TemporalException("TemporalAdminBizLocalImpl.loadMetadata()", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalClientMetadata getMetadata()
	{
		// Get temporal metadata. Only once is enough.
		if (metadata == null) {
			resetBizContext();
			metadata = biz.getMetadata(gridPath);
		} 
		return metadata;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalClientMetadata getMetadata(String regionPath)
	{
		resetBizContext();
		return biz.getMetadata(regionPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalType[] getAllTemporalTypes()
	{
		resetBizContext();
		return biz.getAllTemporalTypes();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGridPath()
	{
		return biz.getBizContext().getGridContextClient().getGridPath();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridPath(String gridPath)
	{
		biz.getBizContext().getGridContextClient().setGridPath(gridPath);
		gridMapBiz.setGridPath(gridPath);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalData<K> get(ITemporalKey<K> temporalKey)
	{
		return gridMapBiz.get(temporalKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAll(Set<ITemporalKey<K>> keys)
	{
		return gridMapBiz.getAll(keys);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalData<K> put(ITemporalKey<K> temporalKey, ITemporalData<K> data)
	{
		return gridMapBiz.put(temporalKey, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<ITemporalKey<K>, ITemporalData<K>> map)
	{
		gridMapBiz.putAll(map);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void deepCopy(ITemporalData fromData, ITemporalData toData)
	{
		// toDeltaData.serializedData is delta. Set it to deltaList and
		// copy formData.getSerializedData() as its data. This allows
		// the delta to be applied to the copied fromData content.
		byte[] delta = toData.__getTemporalValue().getSerializedData();
		LinkedList<byte[]> deltaList = new LinkedList<byte[]>();
		GemfireTemporalValue toTemporalValue = (GemfireTemporalValue) toData.__getTemporalValue();
		deltaList.add(delta);
		toTemporalValue.setSerializedData(fromData.__getTemporalValue().getSerializedData());
		toTemporalValue.setDeltaList(deltaList);
		toTemporalValue.deserializeAll();
	}

	@SuppressWarnings("rawtypes")
	private TemporalDataList<K, V> deserialize(TemporalDataList<K, V> dataList)
	{
		if (dataList != null) {
			ArrayList<TemporalEntry<K, V>> list = dataList.getTemporalList();
			ITemporalData prevData = null;
			for (TemporalEntry<K, V> entry : list) {
				V value = entry.getValue();
				if (value instanceof ITemporalData) {
					ITemporalData data = (ITemporalData) value;
					if (data.__getTemporalValue().isDelta()) {
						// data contains delta
						deepCopy(prevData, data);
					}
					data.__getTemporalValue().deserializeAll();
					prevData = data;
				}
			}
		}
		return dataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalDataList<K, V> getTemporalDataList(K identityKey)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		return deserialize(biz.getTemporalDataList(identityKey));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dumpServer(K identityKey)
	{
		biz.getBizContext().reset();
		biz.dumpServer(identityKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ITemporalBulkLoader<K, V> createBulkLoader(int batchSize)
	{
		return new TemporalBulkLoader(this, batchSize, pado.getCatalog());
//		try {
//			Class claz = this.getClass().getClassLoader().loadClass("com.netcrest.pado.temporal.gemfire.impl.TemporalBulkLoader");
//			Class adminClaz = this.getClass().getClassLoader().loadClass("com.netcrest.pado.biz.ITemporalAdminBiz");
//			Class padoClaz = this.getClass().getClassLoader().loadClass("com.netcrest.pado.IPado");
//			Constructor constructor = claz.getConstructor(adminClaz, int.class, padoClaz);
//			return (ITemporalBulkLoader<K, V>)constructor.newInstance(this, batchSize, pado.getCatalog());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return null;
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalClientFactory<K, V> getTemporalClientFactory()
	{
		return clientFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IGridMapBizLink<ITemporalKey<K>, ITemporalData<K>> getGridMapBiz()
	{
		return gridMapBiz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearTemporalList(K identityKey)
	{
		biz.clearTemporalList(identityKey);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(boolean enabled, boolean spawnThread)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setAdditionalArguments(getGridPath());
		biz.setEnabled(enabled, spawnThread);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled()
	{
		biz.getBizContext().reset();
		return biz.isEnabled();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabledAll(boolean enabled, boolean spawnThread)
	{
		biz.getBizContext().reset();
		biz.setEnabledAll(enabled, spawnThread);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabledAll()
	{
		biz.getBizContext().reset();
		return biz.isEnabledAll();
	}
}
