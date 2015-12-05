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
package com.netcrest.pado.index.gemfire.function;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.netcrest.pado.util.GridUtil;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.provider.ITextSearchProvider;
import com.netcrest.pado.index.provider.TextSearchProviderFactory;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;

/**
 * The TemporalEntrySearchFunction called by GridSearch server to build
 * IndexMatrix for a ITemporalBiz.getEntryResultSet() query.
 * 
 */
public class TemporalEntrySearchFunction extends AbstractEntitySearchFunction implements Function, Declarable,
		IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;

	public final static String Id = "TemporalEntrySearchFunction";

	@Override
	public String getId()
	{
		return Id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		List resultList = null;
		BizManager bizManager = PadoServerManager.getPadoServerManager().getAppBizManager(
				"com.netcrest.pado.biz.ITemporalBiz");
		try {
			String fullPath = criteria.getFullPath();
			String gridPath = GridUtil.getChildPath(fullPath);
			GemfireTemporalManager tm = GemfireTemporalManager.getTemporalManager(fullPath);
			boolean isVirtualPath = false;
			if (tm == null) {
				isVirtualPath = VirtualPathEngine.getVirtualPathEngine().isVirtualPath(gridPath);
			}

			if (tm != null || isVirtualPath) {
				boolean isReference = false;
				int depth = -1;
				long validAtTime = -1;
				long asOfTime = System.currentTimeMillis();
				boolean isWrittenTimeRange = false;
				ITemporalBizLink.ResultSetType type = ITemporalBizLink.ResultSetType.TEMPORAL_ENTRY;
				String queryString = criteria.getQueryString();
				if (queryString != null) {
					queryString = queryString.trim();
				}
				Object parameter = criteria.getParam("isReference");
				if (parameter != null && parameter instanceof Boolean) {
					isReference = (Boolean) parameter;
				}
				parameter = criteria.getParam("depth");
				if (parameter != null && parameter instanceof Integer) {
					depth = (Integer) parameter;
				}
				parameter = criteria.getParam("validAtTime");
				if (parameter != null && parameter instanceof Number) {
					validAtTime = (Long) parameter;
				}
				parameter = criteria.getParam("asOfTime");
				if (parameter != null && parameter instanceof Number) {
					asOfTime = (Long) parameter;
				}
				parameter = criteria.getParam("type");
				if (parameter != null && parameter instanceof ITemporalBizLink.ResultSetType) {
					type = (ITemporalBizLink.ResultSetType) parameter;
				}
				parameter = criteria.getParam("isWrittenTimeRange");
				if (parameter != null && parameter instanceof Boolean) {
					isWrittenTimeRange = (Boolean) parameter;
				}
				long fromWrittenTime = -1;
				long toWrittenTime = -1;
				parameter = criteria.getParam("fromWrittenTime");
				if (parameter != null && parameter instanceof Number) {
					fromWrittenTime = (Long) parameter;
				}
				parameter = criteria.getParam("toWrittenTime");
				if (parameter != null && parameter instanceof Number) {
					toWrittenTime = (Long) parameter;
				}

				if (isVirtualPath) {
					resultList = VirtualPathEngine.getVirtualPathEngine().execute(criteria.getQueryString(),
							validAtTime, asOfTime);
				} else {
					if (queryString != null && queryString.length() > 0) {
						// Search Lucene + temporal
						// Set identityKeySet = null;
						Set<ITemporalKey> temporalKeySet = null;
						if (queryString.toLowerCase().startsWith("select")) {
							// for backward compatibility, replace remove
							// IdentityKey from the select projection.
							String temporalKeyOqlQueryString = queryString.replaceFirst("\\.IdentityKey", "");
							QueryService qs = CacheFactory.getAnyInstance().getQueryService();
							DefaultQuery query = (DefaultQuery) qs.newQuery(temporalKeyOqlQueryString);
							Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
							if (region == null) {
								throw new QueryException("Invalid query. Region not found. " + queryString);
							}
							LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
							SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);
							temporalKeySet = sr.asSet();
						} else {
							CompiledUnit cu = new CompiledUnit(queryString);
							
							if (cu.getQueryLanguage() == QueryLanguage.LUCENE) {
								
								//LuceneSearch luceneSearch = LuceneSearch.getLuceneSearch(criteria.getFullPath());
								ITextSearchProvider provider = TextSearchProviderFactory.getInstance().getProvider(cu.getQueryLanguage(), criteria);
								String qs;
								if (provider instanceof LuceneSearch) {
									LuceneSearch luceneSearch = (LuceneSearch) provider;
									if (isWrittenTimeRange) {
										qs = luceneSearch.getWrittenTimeRangeQuery(validAtTime, fromWrittenTime, toWrittenTime,
												cu.getCompiledQuery());
									} else {
										qs = luceneSearch.getTimeQuery(validAtTime, asOfTime, cu.getCompiledQuery());
									}
								temporalKeySet = luceneSearch.getTemporalKeySet(criteria.getFullPath(), qs);
								}
							} else {
								String temporalKeyOqlQueryString = cu.getTemporalKeyQuery(validAtTime, asOfTime);
								Region region = IndexMatrixOperationUtility.getRegionFromQuery(
										temporalKeyOqlQueryString, null);
								if (region == null) {
									throw new GridQueryException("Invalid query. Path undefined: " + queryString);
								}
								QueryService qs = CacheFactory.getAnyInstance().getQueryService();
								DefaultQuery query = (DefaultQuery) qs.newQuery(temporalKeyOqlQueryString);
								LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
								SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);
								temporalKeySet = sr.asSet();
							}
						}

						switch (type) {
						case KEY:
							resultList = tm.getTemporalCacheListener().getIdentityKeyList(temporalKeySet);
							break;

						case VALUE:
							resultList = tm.getTemporalCacheListener().getValueList_TemporalKeys(
										temporalKeySet);
							break;

						case TEMPORAL_KEY:
							resultList = new ArrayList(temporalKeySet);
							break;

						case TEMPORAL_DATA:
							resultList = tm.getTemporalCacheListener()
									.getTemporalDataList_TemporalKeys(temporalKeySet);
							break;

						case TEMPORAL_ENTRY:
						default:
							resultList = tm.getTemporalCacheListener()
									.getTemporalEntryList_TemporalKeys(temporalKeySet);

							break;
						}
					} else {
						switch (type) {
						case KEY:
							resultList = tm.getTemporalCacheListener().getAsOfKeyList(validAtTime, asOfTime);
							break;

						case VALUE:
							resultList = tm.getTemporalCacheListener().getWrittenTimeRangeValueList(validAtTime,
									fromWrittenTime, toWrittenTime);
							break;

						case TEMPORAL_KEY:
							resultList = tm.getTemporalCacheListener().getAsOfTemporalKeyList(validAtTime, asOfTime);
							break;

						case TEMPORAL_DATA:
							resultList = tm.getTemporalCacheListener().getAsOfTemporalDataList(validAtTime, asOfTime);
							break;

						case TEMPORAL_ENTRY:
						default:
							// Search temporal
							resultList = tm.getTemporalCacheListener().getWrittenTimeRangeTemporalEntryList(
									validAtTime, fromWrittenTime, toWrittenTime);
							break;
						}

					}
					if (type != ITemporalBizLink.ResultSetType.KEY && isReference) {
						if (depth != 0) {
							Object finder = bizManager.getTargetClass().getClassLoader()
									.loadClass("com.netcrest.pado.biz.impl.gemfire.ReferenceFinder").newInstance();
							Method method;
							switch (type) {
							case VALUE:
								method = finder.getClass().getMethod("getCollectionValueReferences", Collection.class,
										int.class, long.class, long.class, Object.class);
								break;

							case TEMPORAL_ENTRY:
							default:
								method = finder.getClass().getMethod("getCollectionReferences", Collection.class,
										int.class, long.class, long.class, Object.class);
								break;
							}
							method.invoke(finder, resultList, depth, validAtTime, asOfTime, Thread.currentThread()
									.getId());
						}
					}
				}
			}
		} catch (Exception e) {
			getLogger().warning("TemporalEntrySearchFunction.queryLocal(): ", e);

			// TODO: Add support for throwing exception. This code needs
			// to be moved to Pado to take advantage of its support for
			// throwing and catching remote exceptions.
			// throw new GridQueryException(e);
		}

		return resultList;
	}
}
