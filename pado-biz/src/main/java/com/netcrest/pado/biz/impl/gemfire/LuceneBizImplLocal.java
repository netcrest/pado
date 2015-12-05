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

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings("rawtypes")
public class LuceneBizImplLocal implements ILuceneBiz, IBizLocal
{
	@Resource
	ILuceneBiz biz;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (ILuceneBiz) biz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public void buildAllIndexes()
	{
		biz.buildAllIndexes();
	}

	@Override
	public void buildAllGridIndexes(String... gridPaths)
	{
		biz.buildAllGridIndexes(gridPaths);
	}
	
	@Override
	public void buildAllPathIndexes(String gridId)
	{
		String[] prevGridIds = this.biz.getBizContext().getGridContextClient().getGridIds();
		this.biz.getBizContext().getGridContextClient().setGridIds(gridId);
		this.biz.buildAllPathIndexes(gridId);
		biz.getBizContext().getGridContextClient().setGridIds(prevGridIds);
	}

	@Override
	public void buildIndexes(String gridId, String... gridPaths)
	{
		String[] prevGridIds = this.biz.getBizContext().getGridContextClient().getGridIds();
		this.biz.getBizContext().getGridContextClient().setGridIds(gridId);
		this.biz.buildIndexes(gridId, gridPaths);
		biz.getBizContext().getGridContextClient().setGridIds(prevGridIds);
	}
	
	@Override
	public void buildTemporalIndexes(String gridId, String... gridPaths)
	{
		String[] prevGridIds = this.biz.getBizContext().getGridContextClient().getGridIds();
		this.biz.getBizContext().getGridContextClient().setGridIds(gridId);
		this.biz.buildTemporalIndexes(gridId, gridPaths);
		biz.getBizContext().getGridContextClient().setGridIds(prevGridIds);
	}

	@Override
	public IMemberResults query(GridQuery criteria)
	{
		return biz.query(criteria);
	}

	@Override
	public List<TemporalEntry> searchTemporal(GridQuery criteria)
	{
		return biz.searchTemporal(criteria);
	}

	@Override
	public Set getTemporalIdentityKeySet(String gridPath, String queryString)
	{
		return biz.getTemporalIdentityKeySet(gridPath, queryString);
	}

	@Override
	public Set<ITemporalKey> getTemporalKeySet(String gridPath, String queryString)
	{
		return biz.getTemporalKeySet(gridPath, queryString);
	}

	@Override
	public void setLuceneEnabled(String gridPath, boolean enabled)
	{
		biz.setLuceneEnabled(gridPath, enabled);
	}

	@Override
	public boolean isLuceneEnabled(String gridPath)
	{
		return biz.isLuceneEnabled(gridPath);
	}
	
	@Override
	public void setLuceneEnabledAll(boolean enabled)
	{
		biz.setLuceneEnabledAll(enabled);
	}

	@Override
	public boolean isLuceneEnabledAll()
	{
		return biz.isLuceneEnabledAll();
	}
}
