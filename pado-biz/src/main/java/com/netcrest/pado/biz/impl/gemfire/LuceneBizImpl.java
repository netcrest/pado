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

import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.biz.gemfire.IGemfireGridContextServer;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.provider.lucene.LuceneBuilder;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;

@SuppressWarnings("rawtypes")
public class LuceneBizImpl extends AbstractEntitySearch implements ILuceneBiz
{
	@Resource
	IBizContextServer bizContext;

	@Override
	public IBizContextClient getBizContext()
	{
		// not used
		return null;
	}

	@BizMethod
	@Override
	public void buildAllIndexes()
	{
		LuceneBuilder manager = LuceneBuilder.getLuceneBuilder();
		manager.buildAll();
	}

	@BizMethod
	@Override
	public void buildAllGridIndexes(String... gridPaths)
	{
		if (gridPaths == null || gridPaths.length == 0) {
			buildAllIndexes();
		} else {
			LuceneBuilder manager = LuceneBuilder.getLuceneBuilder();
			manager.buildIndexes(gridPaths);
		}
	}

	@BizMethod
	@Override
	public void buildAllPathIndexes(String gridId)
	{
		buildAllIndexes();
	}

	@BizMethod
	@Override
	public void buildIndexes(String gridId, String... gridPaths)
	{
		buildAllGridIndexes(gridPaths);
	}

	@BizMethod
	@Override
	public void buildTemporalIndexes(String gridId, String... gridPaths)
	{
		if (gridPaths == null || gridPaths.length == 0) {
			TemporalManager.setEnabledAll(true, true, false);
			
			// If Lucene is enabled (configured) then it is built by the above call
			// (TemporalManager.setEnabeldAll()).
			// If it is not enabled then build it here.
			Set<String> set = TemporalManager.getAllTemporalFullPaths();
			for (String fullPath : set) {
				TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
				if (tm.isLuceneEnabled() == false) {
					buildAllGridIndexes(GridUtil.getChildPath(fullPath));
				}
			}
		} else {
			for (String gridPath : gridPaths) {
				TemporalManager tm = TemporalManager.getTemporalManager(GridUtil.getFullPath(gridPath));
				if (tm != null) {
					tm.setEnabled(true, true, false);

					// If Lucene is enabled (configured) then it is built by the above call
					// (TemporalManager.setEnabeld()).
					// If it is not enabled then build it here.
					if (tm.isLuceneEnabled() == false) {
						buildAllGridIndexes(gridPath);
					}
				}
			}
		}

	}

	@BizMethod
	@Override
	public IMemberResults query(GridQuery criteria)
	{
		return execute(criteria, ((IGemfireGridContextServer) (bizContext.getGridContextServer())).getFunctionContext());
	}

	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		LuceneSearch search = LuceneSearch.getLuceneSearch(criteria.getFullPath());
		return search.searchTemporal(criteria);
	}

	@Override
	@BizMethod
	public List<TemporalEntry> searchTemporal(GridQuery criteria)
	{
		return LuceneSearch.getLuceneSearch(criteria.getFullPath()).searchTemporal(criteria);
	}

	@Override
	@BizMethod
	public Set getTemporalIdentityKeySet(String gridPath, String queryString)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		return LuceneSearch.getLuceneSearch(fullPath).getIdentityKeySet(fullPath, queryString, -1);
	}

	@Override
	@BizMethod
	public Set<ITemporalKey> getTemporalKeySet(String gridPath, String queryString)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		return LuceneSearch.getLuceneSearch(fullPath).getTemporalKeySet(fullPath, queryString, -1);
	}

	@Override
	@BizMethod
	public void setLuceneEnabled(String gridPath, boolean enabled)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		if (tm != null) {
			tm.setLuceneEnabled(enabled, true);
		}
	}

	@Override
	@BizMethod
	public boolean isLuceneEnabled(String gridPath)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		if (tm != null) {
			return tm.isLuceneEnabled();
		} else {
			return false;
		}
	}

	@Override
	@BizMethod
	public void setLuceneEnabledAll(boolean enabled)
	{
		TemporalManager.setLuceneEnabledAll(enabled, true);
	}

	@Override
	@BizMethod
	public boolean isLuceneEnabledAll()
	{
		return TemporalManager.isLuceneEnabledAll();
	}
}
