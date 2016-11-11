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
package com.netcrest.pado.biz;

import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalClientMetadata;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalType;

/**
 * ITemporalAdminBiz provides administrative methods for managing temporal
 * data.
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>String gridPath</b> - Grid path
 * </blockquote>
 * 
 * @author dpark
 *
 * @param <K> Key class
 * @param <V> Value class
 */
@BizClass(name="ITemporalAdminBiz")
public interface ITemporalAdminBiz<K, V> extends IBiz, ITemporalAdminBizLink<K, V>
{
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	TemporalClientMetadata getMetadata(String targetPath);
	
	/**
	 * {@inheritDoc}
	 */
	TemporalClientMetadata getMetadata();
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer
	TemporalType[] getAllTemporalTypes();
	
	/**
	 * {@inheritDoc}
	 */
	public String getGridPath();
	
	/**
	 * {@inheritDoc}
	 */
	public void setGridPath(String gridPath);

	/**
	 * {@inheritDoc}
	 */
	@Override
	ITemporalData<K> get(ITemporalKey<K> temporalKey);
	
	/**
	 * {@inheritDoc}
	 */
	Map<ITemporalKey<K>, ITemporalData<K>> getAll(Set<ITemporalKey<K>> keys);
	
	/**
	 * {@inheritDoc}
	 */
	ITemporalData<K> put(ITemporalKey<K> temporalKey, ITemporalData<K> data);
	
	/**
	 * {@inheritDoc}
	 */
	void putAll(Map<ITemporalKey<K>, ITemporalData<K>> map);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	TemporalDataList<K, V> getTemporalDataList(K identityKey);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	void clearTemporalList(K identityKey);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	ITemporalData<K> removePermanently(ITemporalKey<K> temporalKey);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	void dumpServer(K identityKey);
	
	/**
	 * {@inheritDoc}
	 */
	ITemporalBulkLoader<K, V> createBulkLoader(int batchSize);
	
	/**
	 *{@inheritDoc}
	 */
	TemporalClientFactory<K, V> getTemporalClientFactory();
	
	/**
	 * {@inheritDoc}
	 */
	IGridMapBizLink<ITemporalKey<K>, ITemporalData<K>> getGridMapBiz();
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast=true)
	@Override
	void setEnabled(boolean enabled, boolean spawnThread);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnPath
	@Override
	boolean isEnabled();
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast=true)
	@Override
	void setEnabledAll(boolean enabled, boolean spawnThread);
	
	/**
	 * {@inheritDoc}
	 */
	@BizMethod
	@OnServer(broadcast=true)
	@WithGridCollector(gridCollectorClass="com.netcrest.pado.biz.collector.BooleanAndCollector")
	@Override
	boolean isEnabledAll();
}
