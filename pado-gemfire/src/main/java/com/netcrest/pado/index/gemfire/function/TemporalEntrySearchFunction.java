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
import com.gemstone.gemfire.pdx.internal.PdxInstanceEnum;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.provider.ITextSearchProvider;
import com.netcrest.pado.index.provider.TextSearchProviderFactory;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.util.GridUtil;

/**
 * The TemporalEntrySearchFunction called by GridSearch server to build
 * IndexMatrix for a ITemporalBiz.getEntryResultSet() query.
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalEntrySearchFunction extends AbstractEntitySearchFunction
		implements Function, Declarable, IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;

	public final static String Id = "TemporalEntrySearchFunction";

	@Override
	public String getId()
	{
		return Id;
	}

	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		List resultList = null;
		BizManager bizManager = PadoServerManager.getPadoServerManager()
				.getAppBizManager("com.netcrest.pado.biz.ITemporalBiz");
		try {
			String fullPath = criteria.getFullPath();
			String gridPath = GridUtil.getChildPath(fullPath);
			String queryString = criteria.getQueryString();
			if (queryString != null) {
				queryString = queryString.trim();
			}
			GemfireTemporalManager tm = GemfireTemporalManager.getTemporalManager(fullPath);
			boolean isVirtualPath = VirtualPathEngine.getVirtualPathEngine().isVirtualPath(gridPath);

			int depth = -1;
			long validAtTime = -1;
			long asOfTime = System.currentTimeMillis();
			ITemporalBizLink.ResultSetType type = ITemporalBizLink.ResultSetType.TEMPORAL_ENTRY;
			String memberId = null;
			Object parameter = criteria.getParam("validAtTime");
			if (parameter != null && parameter instanceof Number) {
				validAtTime = (Long) parameter;
			}
			parameter = criteria.getParam("asOfTime");
			if (parameter != null && parameter instanceof Number) {
				asOfTime = (Long) parameter;
			}
			parameter = criteria.getParam("depth");
			if (parameter != null && parameter instanceof Integer) {
				depth = (Integer) parameter;
			}
			parameter = criteria.getParam("type");
			if (parameter != null) {
				if (parameter instanceof ITemporalBizLink.ResultSetType) {
					type = (ITemporalBizLink.ResultSetType) parameter;
				} else {
					type = ITemporalBizLink.ResultSetType.valueOf(parameter.toString());
				}
			}
			parameter = criteria.getParam("memberId");
			if (parameter != null && parameter instanceof String) {
				memberId = (String) parameter;
			}
			
			if (isVirtualPath) {

				String virtualPath = gridPath;
				VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine().getVirtualCompiledUnit(virtualPath);
				if (vcu != null && vcu.isEntity()) {
					String entityGridPath = vcu.getEntityGridPath();
					PqlParser pqlParser = new PqlParser(queryString);
					queryString = vcu.getEntityGridPath() + "?" + pqlParser.getParsedQuery();
					resultList = getLocalResults(criteria, entityGridPath, queryString);

					if (resultList != null && depth != 0) {
						// supports KeyMap only
						Object finder = bizManager.getTargetClass().getClassLoader()
								.loadClass("com.netcrest.pado.biz.impl.gemfire.VirtualPathEntityFinder").newInstance();
						
						Method method;
						switch (type) {
						case VALUE:
							// TODO: Support bulk reference
							method = finder.getClass().getMethod("getReferences", ICatalog.class, String.class,
									KeyMap.class, int.class, long.class, long.class, Object.class);
							for (Object object : resultList) {
								KeyMap keyMap = (KeyMap) object;
								method.invoke(finder, PadoServerManager.getPadoServerManager().getCatalog(), virtualPath, keyMap,
										depth, validAtTime, asOfTime, null);
							}
							break;

						case TEMPORAL_ENTRY:
						default:
							method = finder.getClass().getMethod("getCollectionReferences", ICatalog.class, VirtualCompiledUnit2.class,
									Collection.class, int.class, long.class, long.class, Object.class);
							
//							public Collection<TemporalEntry<K, V>> getCollectionReferences(ICatalog catalog, String virtualPath,
//									Collection<TemporalEntry<K, V>> collection, int depth, long validAtTime, long asOfTime,
//									Object keyMapReferenceId)
							
//							private Collection<TemporalEntry<K, V>> getCollectionReferences(ICatalog catalog, VirtualCompiledUnit2 vcu,
//									Collection<TemporalEntry<K, V>> collection, int depth, long validAtTime, long asOfTime,
//									Object keyMapReferenceId)
							method.invoke(finder, PadoServerManager.getPadoServerManager().getCatalog(), vcu, resultList,
									depth, validAtTime, asOfTime, null);
							break;
						}
					}
					
				} else {
					// TODO : Verify
					// TODO: This puts load on the master. Find a way to pick one server.
//					if (PadoServerManager.getPadoServerManager().isMaster()) {
					if (memberId == null || memberId.equals(GemfireGridUtil.getDistributedMember().getId())) {
						resultList = VirtualPathEngine.getVirtualPathEngine().execute(criteria.getQueryString(),
								validAtTime, asOfTime);
					}
				}

			} else if (tm != null) {

				resultList = getLocalResults(criteria, gridPath, queryString);

				boolean isReference = false;
				parameter = criteria.getParam("isReference");
				if (parameter != null && parameter instanceof Boolean) {
					isReference = (Boolean) parameter;
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
							method = finder.getClass().getMethod("getCollectionReferences", Collection.class, int.class,
									long.class, long.class, Object.class);
							break;
						}
						method.invoke(finder, resultList, depth, validAtTime, asOfTime, Thread.currentThread().getId());
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

	private List getLocalResults(GridQuery criteria, String gridPath, String queryString) throws QueryException
	{
		List resultList = null;
		String fullPath = GridUtil.getFullPath(gridPath);
		GemfireTemporalManager tm = GemfireTemporalManager.getTemporalManager(fullPath);

		long validAtTime = -1;
		long asOfTime = System.currentTimeMillis();
		boolean isWrittenTimeRange = false;
		int limit = criteria.getServerLimit();
		ITemporalBizLink.ResultSetType type = ITemporalBizLink.ResultSetType.TEMPORAL_ENTRY;
		Object parameter = criteria.getParam("validAtTime");
		if (parameter != null && parameter instanceof Number) {
			validAtTime = (Long) parameter;
		}
		parameter = criteria.getParam("asOfTime");
		if (parameter != null && parameter instanceof Number) {
			asOfTime = (Long) parameter;
		}
		parameter = criteria.getParam("type");
		if (parameter != null) {
			if (parameter instanceof ITemporalBizLink.ResultSetType) {
				type = (ITemporalBizLink.ResultSetType) parameter;
			} else {
				type = ITemporalBizLink.ResultSetType.valueOf(parameter.toString());
			}
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

		// Result set limit is applied per server. Note that limit
		// only approximates the result set size. For example, if one or more
		// servers have a number of entries that is less than the
		// server limit then the final aggregate resize will be less
		// then the total limit.
		if (queryString != null && queryString.length() > 0) {
			// Search Lucene + temporal
			// Set identityKeySet = null;
			Set<ITemporalKey> temporalKeySet = null;
			if (queryString.toLowerCase().startsWith("select")) {
				// for backward compatibility, replace remove
				// IdentityKey from the select projection.
				String temporalKeyOqlQueryString = queryString.replaceFirst("\\.IdentityKey", "");
				if (limit > 0) {
					temporalKeyOqlQueryString += " limit " + limit;
				}
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

					// LuceneSearch luceneSearch =
					// LuceneSearch.getLuceneSearch(fullPath);
					ITextSearchProvider provider = TextSearchProviderFactory.getInstance()
							.getProvider(cu.getQueryLanguage(), criteria);
					String qs;
					if (provider instanceof LuceneSearch) {
						LuceneSearch luceneSearch = (LuceneSearch) provider;
						if (isWrittenTimeRange) {
							qs = luceneSearch.getWrittenTimeRangeQuery(validAtTime, fromWrittenTime, toWrittenTime,
									cu.getCompiledQuery());
						} else {
							qs = luceneSearch.getTimeQuery(validAtTime, asOfTime, cu.getCompiledQuery());
						}
						temporalKeySet = luceneSearch.getTemporalKeySet(fullPath, qs, limit);
					}
				} else {
					String temporalKeyOqlQueryString = cu.getTemporalKeyQuery(validAtTime, asOfTime);
					if (limit > 0) {
						temporalKeyOqlQueryString += " limit " + limit;
					}
					Region region = IndexMatrixOperationUtility.getRegionFromQuery(temporalKeyOqlQueryString, null);
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
				resultList = tm.getTemporalCacheListener().getValueList_TemporalKeys(temporalKeySet);
				break;

			case TEMPORAL_KEY:
				resultList = new ArrayList(temporalKeySet);
				break;

			case TEMPORAL_DATA:
				resultList = tm.getTemporalCacheListener().getTemporalDataList_TemporalKeys(temporalKeySet);
				break;

			case TEMPORAL_ENTRY:
			default:
				resultList = tm.getTemporalCacheListener().getTemporalEntryList_TemporalKeys(temporalKeySet);

				break;
			}
		} else {
			switch (type) {
			case KEY:
				resultList = tm.getTemporalCacheListener().getAsOfKeyList(validAtTime, asOfTime);
				break;

			case VALUE:
				resultList = tm.getTemporalCacheListener().getWrittenTimeRangeValueList(validAtTime, fromWrittenTime,
						toWrittenTime);
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
				resultList = tm.getTemporalCacheListener().getWrittenTimeRangeTemporalEntryList(validAtTime,
						fromWrittenTime, toWrittenTime);
				break;
			}

		}
		return resultList;
	}
}
