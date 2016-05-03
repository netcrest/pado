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
package com.netcrest.pado.index.provider.lucene;

import java.util.List;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.function.AbstractEntitySearchFunction;
import com.netcrest.pado.index.gemfire.function.IEntitySearchFunction;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * LuceneTemporalQueryFunction queries the local temporal data set and caches
 * the results in the server.
 * 
 */
public class LuceneTemporalQueryFunction extends AbstractEntitySearchFunction implements Function, Declarable,
		IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;
	public final static String Id = LuceneTemporalQueryFunction.class.getSimpleName();

	public LuceneTemporalQueryFunction()
	{
	}

	@Override
	public String getId()
	{
		return Id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		List<TemporalEntry> list = null;
		String pql = criteria.getQueryString();
		CompiledUnit cu = new CompiledUnit(pql);
		switch (cu.getQueryLanguage()) {
		case OQL:
			String queryString = cu.getCompiledQuery();
			Region region = IndexMatrixOperationUtility.getRegionFromQuery(queryString, null);
			if (region == null) {
				break;
			}

			QueryService qs = CacheFactory.getAnyInstance().getQueryService();
			DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
			LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
			try {
				SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);
				list = sr.asList();
			} catch (Exception ex) {
				Logger.warning(ex);
				throw new GridQueryException(ex);
			}
			break;

		case LUCENE:
			LuceneSearch search = LuceneSearch.getLuceneSearch(criteria.getFullPath());
			list = search.searchTemporal(criteria);
			break;
		default:
			break;
		}

		return list;
	}
}
