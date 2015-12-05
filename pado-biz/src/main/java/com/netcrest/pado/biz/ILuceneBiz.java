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

import java.util.List;
import java.util.Set;

import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.link.ILuceneBizLink;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * ILuceneBiz provides methods to build Lucene indexes and execute Lucene
 * queries.
 * 
 * <p>
 * <b>Arguments: None</b>
 * <p>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes" })
@BizClass(name = "ILuceneBiz")
public interface ILuceneBiz extends ILuceneBizLink
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	void buildAllIndexes();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	void buildAllGridIndexes(String... gridPaths);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true)
	void buildAllPathIndexes(String gridId);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true)
	void buildIndexes(String gridId, String... gridPaths);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true)
	void buildTemporalIndexes(String gridId, String... gridPaths);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer
	IMemberResults query(GridQuery criteria);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Use {@linkplain ITemporalBiz} instead.
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	List<TemporalEntry> searchTemporal(GridQuery criteria);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Use {@linkplain ITemporalBiz} instead.
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	Set getTemporalIdentityKeySet(String gridPath, String queryString);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Use {@linkplain ITemporalBiz} instead.
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true, broadcastGrids = true)
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.CollectionCollector")
	Set<ITemporalKey> getTemporalKeySet(String gridPath, String queryString);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true)
	void setLuceneEnabled(String gridPath, boolean enabled);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer
	boolean isLuceneEnabled(String gridPath);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer(broadcast = true)
	void setLuceneEnabledAll(boolean enabled);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@BizMethod
	@OnServer
	boolean isLuceneEnabledAll();
}
