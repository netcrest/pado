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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.IGridRouter;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalAdminBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.service.GridQueryService;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.GridQueryFactory;
import com.netcrest.pado.index.service.IGridQueryService;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.pql.PqlParser.OrderByQueryString;
import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.AttachmentSetFactory;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalException;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBizImplLocal<K, V> implements ITemporalBiz<K, V>, IBizLocal
{
	@Resource
	ITemporalBiz<K, V> biz;

	private IPado pado;

	private IGridMapBizLink<ITemporalKey<K>, ITemporalData<K>> gridMapBiz;
	private boolean isReference = false;
	private int depth = -1;

	private boolean isPutExceptionEnabled;

	/**
	 * The temporal administration object for directly accessing data in terms
	 * of ITemporalKey and ITemporalData.
	 */
	private ITemporalAdminBizLink<K, V> temporalAdminBiz;

	/**
	 * The client factory for creating temporal objects. The underlying temporal
	 * class types are assigned by the server.
	 */
	private TemporalClientFactory<K, V> clientFactory;

	public TemporalBizImplLocal()
	{
	}

	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (ITemporalBiz) biz;
		this.pado = pado;
		String gridPath = null;
		if (args != null && args.length > 0) {
			gridPath = (String) args[0];
		}

		// if (PadoServerManager.getPadoServerManager() == null) {
		temporalAdminBiz = pado.getCatalog().newInstance(ITemporalAdminBiz.class, gridPath);
		// } else {

		// If server, then it must be created via the IBiz class loader
		// temporalAdminBiz = (ITemporalAdminBizLink<K, V>) PadoServerManager
		// .getPadoServerManager()
		// .getCatalog()
		// .newInstanceLocal("com.netcrest.pado.biz.ITemporalAdminBiz",
		// "com.netcrest.pado.biz.impl.gemfire.TemporalAdminBizImplLocal",
		// gridPath);
		// }

		gridMapBiz = temporalAdminBiz.getGridMapBiz();
		clientFactory = temporalAdminBiz.getTemporalClientFactory();
		biz.getBizContext().getGridContextClient().setGridPath(gridMapBiz.getGridPath());
	}

	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	@Override
	public String getGridPath()
	{
		return biz.getBizContext().getGridContextClient().getGridPath();
	}

	@Override
	public void setGridPath(String gridPath)
	{
		biz.getBizContext().getGridContextClient().setGridPath(gridPath);
		temporalAdminBiz.setGridPath(gridPath);
		// No need to set gridMapBiz. Its grid path is set by
		// temporalAdminBiz.setGridPath().
	}

	@Override
	public void setReference(boolean isReference)
	{
		this.isReference = isReference;
	}

	@Override
	public boolean isReference()
	{
		return isReference;
	}

	@Override
	public void setDepth(int depth)
	{
		this.depth = depth;
	}

	@Override
	public int getDepth()
	{
		return depth;
	}

	/**
	 * Enables/disables TemporalException thrown by the put methods. If true,
	 * put() throws a TemporalException if it fails - typically due to duplicate
	 * keys. If false, the failed put call is silently ignored. The put() method
	 * in that case returns null.
	 * 
	 * @param isPutExceptionEnabled
	 *            true to enable, false to disable. The default value is true.
	 *            This overwrites the system property
	 *            "temporal.put.exception.enabled".
	 */
	@Override
	public void setPutExceptionEnabled(boolean isPutExceptionEnabled)
	{
		this.isPutExceptionEnabled = isPutExceptionEnabled;
	}

	/**
	 * Returns true if put throws TemporalException upon failure. Returns false
	 * otherwise.
	 * 
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #put(Object, Object, long, long, long, boolean)
	 */
	@Override
	public boolean isPutExceptionEnabled()
	{
		return this.isPutExceptionEnabled;
	}

	@Override
	public ITemporalAdminBizLink<K, V> getTemporalAdminBiz()
	{
		return temporalAdminBiz;
	}

	private V deserialize(V value)
	{
		if (value != null && value instanceof ITemporalData) {
			((ITemporalData) value).__getTemporalValue().deserializeAll();
		}
		return value;
	}

	@Override
	public V get(K identityKey)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.get(identityKey));
	}

	@Override
	public V get(K identityKey, long validAtTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.get(identityKey, validAtTime));
	}

	@Override
	public V get(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.get(identityKey, validAtTime, asOfTime));
	}

	private TemporalEntry<K, V> deserialize(TemporalEntry<K, V> entry)
	{
		if (entry != null && entry.getTemporalData() != null) {
			entry.getTemporalData().__getTemporalValue().deserializeAll();
		}
		return entry;
	}

	@Override
	public TemporalEntry<K, V> getEntry(K identityKey)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.getEntry(identityKey));
	}

	@Override
	public TemporalEntry<K, V> getEntry(K identityKey, long validAtTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.getEntry(identityKey, validAtTime));
	}

	@Override
	public TemporalEntry<K, V> getEntry(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.getEntry(identityKey, validAtTime, asOfTime));
	}

	private Map<ITemporalKey<K>, ITemporalData<K>> deserialize(Map<ITemporalKey<K>, ITemporalData<K>> map)
	{
		if (map != null) {
			Collection<ITemporalData<K>> col = map.values();
			for (ITemporalData<K> data : col) {
				data.__getTemporalValue().deserializeAll();
			}
		}
		return map;
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.getEntries(validAtTime));
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserialize(biz.getEntries(validAtTime, asOfTime));
	}

	@Override
	public List<V> getQueryValues(String queryStatement, long validAtTime, long asOfTime)
	{
		String gridPath = getGridPath();
		PqlParser pqlParser = new PqlParser(this.biz.getBizContext().getGridService(), queryStatement, gridPath);
		String gridPaths[] = pqlParser.getPaths();
		if (gridPaths != null && gridPaths.length > 0) {
			gridPath = gridPaths[0];
		}
		boolean isVirtualPath = pado.isVirtualPath(gridPath);
		String gridIds[];
		String queryString;
		if (isVirtualPath) {
			gridIds = new String[] { this.biz.getBizContext().getGridService().getDefaultGridId() };
			queryString = pqlParser.getPql();
		} else {
			gridIds = pqlParser.getGridIds();
			queryString = pqlParser.getParsedQuery();
		}

		AttachmentSet as = new AttachmentSetFactory().createAttachmentSet();
		as.setQueryStatement(queryString);
		as.setGridPath(gridPath);
		biz.getBizContext().getGridContextClient().setGridIds(gridIds);
		AttachmentSet<K>[] attSets = new AttachmentSet[] { as };
		biz.getBizContext().getGridContextClient().setAdditionalArguments(attSets, isReference, depth);

		Map<String, List<V>> map;
		if (isVirtualPath) {
			// Invoke on a single server
			biz.getBizContext().getGridContextClient().setGridIds(gridIds);
			map = biz.__getAttachmentsOnServer(validAtTime, asOfTime);
		} else {
			map = biz.__getAttachmentsBroadcast(validAtTime, asOfTime);
		}
		if (map == null) {
			return null;
		}
		deserializeAttachmentValueListMap(map);
		if (map.values().isEmpty()) {
			return null;
		} else {
			return new ArrayList(map.values().iterator().next());
		}
	}

	@Override
	public List<TemporalEntry<K, V>> getQueryEntries(String queryStatement, long validAtTime, long asOfTime)
	{
		String gridPath = getGridPath();
		PqlParser pqlParser = new PqlParser(this.biz.getBizContext().getGridService(), queryStatement, gridPath);
		String gridPaths[] = pqlParser.getPaths();
		if (gridPaths != null && gridPaths.length > 0) {
			gridPath = gridPaths[0];
		}
		boolean isVirtualPath = pado.isVirtualPath(gridPath);
		String gridIds[];
		String queryString;
		if (isVirtualPath) {
			gridIds = new String[] { this.biz.getBizContext().getGridService().getDefaultGridId() };
			queryString = pqlParser.getPql();
		} else {
			gridIds = pqlParser.getGridIds();
			queryString = pqlParser.getParsedQuery();
		}

		AttachmentSet as = new AttachmentSetFactory().createAttachmentSet();
		as.setQueryStatement(queryString);
		as.setGridPath(gridPath);
		biz.getBizContext().getGridContextClient().setGridIds(gridIds);
		AttachmentSet<K>[] attSets = new AttachmentSet[] { as };
		biz.getBizContext().getGridContextClient().setAdditionalArguments(attSets, isReference, depth);

		// <String, List<V>>
		Map<String, List<TemporalEntry<K, V>>> map = biz.__getAttachmentsEntriesBroadcast(validAtTime, asOfTime);
		if (map == null) {
			return null;
		}
		deserializeAttachementMap(map);
		if (map.values().isEmpty()) {
			return null;
		} else {
			return new ArrayList(map.values().iterator().next());
		}
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex)
	{
		return getEntryResultSet(validAtTime, System.currentTimeMillis(), orderBy, orderAcending, batchSize,
				forceRebuildIndex, -1);
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex)
	{
		return getEntryResultSet(validAtTime, asOfTime, orderBy, orderAcending, batchSize, forceRebuildIndex, -1);
	}
	
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		if (biz.getBizContext().getGridService().isPureClient() == false) {
			throw new PadoException(
					"Unsupported method. This method is supported for pure clients only.");
		} else if (Pado.getPado(biz.getBizContext().getGridService().getToken()) == null) {
			throw new PadoException(
					"Access denied. Invalid session token: " + " [token="
							+ biz.getBizContext().getGridService().getToken() + "]");
		}
		String gridPath = getGridPath();
		String gridIds[] = this.biz.getBizContext().getGridService().getGridIds(gridPath);
		String gridId = null;
		if (gridIds.length == 0) {
			gridId = this.biz.getBizContext().getGridService().getDefaultGridId();
		} else {
			gridId = gridIds[0];
		}
		if (gridId == null) {
			return null;
		}
		
		GridQuery criteria = GridQueryFactory.createGridQuery();
		criteria.setId("pql://" + gridPath + "&validAt=" + validAtTime + "&asOf=" + asOfTime + " limit " + limit);
		criteria.setAscending(orderAcending);
		criteria.setFetchSize(100);
		criteria.setOrdered(orderBy != null);
		criteria.setSortField(orderBy);
		boolean sortKey = orderBy == null || orderBy.equals("WrittenTime") || orderBy.equals("EndWrittenTime")
				|| orderBy.equals("StartValidTime") || orderBy.equals("EndValidTime") || orderBy.equals("Username")
				|| orderBy.equals("IdentityKey");
		criteria.setSortKey(sortKey);
		criteria.setQueryString(null);
		criteria.setForceRebuildIndex(forceRebuildIndex);
		criteria.setGridIds(gridId);
		criteria.setGridService(this.biz.getBizContext().getGridService());
		criteria.setFullPath(this.biz.getBizContext().getGridService().getFullPath(gridId, gridPath));
		criteria.setProviderKey(Constants.TEMPORAL_ENTRY_PROVIDER_KEY);
		criteria.setLimit(limit);
		criteria.setParameter("validAtTime", validAtTime);
		criteria.setParameter("asOfTime", asOfTime);
		criteria.setParameter("isReference", isReference);
		criteria.setParameter("depth", depth);
		
		IGridQueryService qs = GridQueryService.getGridQueryService();	
		IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> rs = ((IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) qs
				.query(criteria));
		return rs;
		
		// TODO: Replace the above with the following
//		BizGridQueryServiceImpl qs = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();	
//		IIndexMatrixBiz imBiz = pado.getCatalog().newInstance(IIndexMatrixBiz.class);
//		return qs.query(imBiz, criteria);
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) getResultSet(queryStatement,
				-1, -1, orderBy, orderAscending, batchSize, forceRebuildIndex, -1, ResultSetType.TEMPORAL_ENTRY);
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, long validAtTime, long asOfTime, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) getResultSet(queryStatement,
				validAtTime, asOfTime, orderBy, orderAscending, batchSize, forceRebuildIndex, -1,
				ResultSetType.TEMPORAL_ENTRY);
	}
	
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, long validAtTime, long asOfTime, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex, int limit)
	{
		return (IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) getResultSet(queryStatement,
				validAtTime, asOfTime, orderBy, orderAscending, batchSize, forceRebuildIndex, -1,
				ResultSetType.TEMPORAL_ENTRY);
	}
	
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) getResultSetWrittenTimeRange(queryStatement,
				validAtTime, fromWrittenTime, toWrittenTime, orderBy, orderAscending, batchSize, forceRebuildIndex, -1, 
				ResultSetType.TEMPORAL_ENTRY);
	}
	
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		return (IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>>) getResultSetWrittenTimeRange(queryStatement,
				validAtTime, fromWrittenTime, toWrittenTime, orderBy, orderAscending, batchSize, forceRebuildIndex, limit,
				ResultSetType.TEMPORAL_ENTRY);
	}

	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<V>) getResultSet(queryStatement, -1, -1, orderBy, orderAscending, batchSize,
				forceRebuildIndex, -1, ResultSetType.VALUE);
	}

	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<V>) getResultSet(queryStatement, validAtTime, asOfTime, orderBy, orderAscending,
				batchSize, forceRebuildIndex, -1, ResultSetType.VALUE);
	}
	
	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		return (IScrollableResultSet<V>) getResultSet(queryStatement, validAtTime, asOfTime, orderBy, orderAscending,
				batchSize, forceRebuildIndex, limit, ResultSetType.VALUE);
	}
	
	@Override
	public IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex)
	{
		return (IScrollableResultSet<V>) getResultSetWrittenTimeRange(queryStatement, validAtTime, fromWrittenTime,
				toWrittenTime, orderBy, orderAscending, batchSize, forceRebuildIndex, -1, ResultSetType.VALUE);
	}
	
	@Override
	public IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex, int limit)
	{
		return (IScrollableResultSet<V>) getResultSetWrittenTimeRange(queryStatement, validAtTime, fromWrittenTime,
				toWrittenTime, orderBy, orderAscending, batchSize, forceRebuildIndex, limit, ResultSetType.VALUE);
	}

	private IScrollableResultSet getResultSet(String queryStatement, long validAtTime, long asOfTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit, ResultSetType type)
	{
		if (queryStatement == null) {
			new NullPointerException("queryStatement is null");
		}
		GridQuery criteria = getGridQuery(queryStatement, validAtTime, orderBy, orderAscending, batchSize, forceRebuildIndex, type);
		criteria.setParameter("asOfTime", asOfTime);
		criteria.setId(criteria.getId() + "&asOf=" + asOfTime + " limit " + limit);
		criteria.setLimit(limit);
		IGridQueryService qs = GridQueryService.getGridQueryService();
		IScrollableResultSet rs = qs.query(criteria);
		return rs;
		
		// TODO: Replace the above with the following
//		BizGridQueryServiceImpl qs = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();
//		IIndexMatrixBiz imBiz = pado.getCatalog().newInstance(IIndexMatrixBiz.class);
//		return qs.query(imBiz, criteria);
	}
	
	private IScrollableResultSet getResultSetWrittenTimeRange(String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit, ResultSetType type)
	{
		if (queryStatement == null) {
			new NullPointerException("queryStatement is null");
		}
		GridQuery criteria = getGridQuery(queryStatement, validAtTime, orderBy, orderAscending, batchSize, forceRebuildIndex, type);
		criteria.setParameter("isWrittenTimeRange", true);
		criteria.setParameter("fromWrittenTime", fromWrittenTime);
		criteria.setParameter("toWrittenTime", toWrittenTime);
		criteria.setId(criteria.getId() + "&fromWrittenTime=" + fromWrittenTime + "&toWrittenTime=" + toWrittenTime + " limit " + limit);
		criteria.setLimit(limit);
		IGridQueryService qs = GridQueryService.getGridQueryService();
		IScrollableResultSet rs = qs.query(criteria);
		return rs;
		
		// TODO: Replace the above with the following
//		BizGridQueryServiceImpl qs = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();
//		IIndexMatrixBiz imBiz = pado.getCatalog().newInstance(IIndexMatrixBiz.class);
//		return qs.query(imBiz, criteria);
	}
	
	private GridQuery getGridQuery(String queryStatement, long validAtTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, ResultSetType type)
	{
		if (queryStatement == null) {
			return null;
		}
		if (biz.getBizContext().getGridService().isPureClient() == false) {
			throw new PadoException(
					"Unsupported method. This method is supported for pure clients only.");
		} else if (Pado.getPado(biz.getBizContext().getGridService().getToken()) == null) {
			throw new PadoException(
					"Access denied. Invalid session token: " + " [token="
							+ biz.getBizContext().getGridService().getToken() + "]");
		}
		String gridPath = getGridPath();
		queryStatement = queryStatement.trim();
		OrderByQueryString orderByQueryString = PqlParser.getOrderBy(queryStatement);
		
		String gridIds[];
		String queryString;
		String fullPath = null;
		GridQuery criteria = GridQueryFactory.createGridQuery();
		if (queryStatement.toLowerCase().startsWith("select")) {
			queryString = queryStatement;
			QueryService qs = CacheFactory.getAnyInstance().getQueryService();
			DefaultQuery query = (DefaultQuery) qs.newQuery(queryStatement);
			Set<String> set = query.getRegionsInQuery(null);
			for (String regionPath : set) {
				fullPath = regionPath;
			}
			if (fullPath == null) {
				throw new GridQueryException("Invalid query. Path undefined: " + queryString);
			}
			gridIds = getBizContext().getGridService().getGridIds();
			criteria.setId("pql://" + queryString + "&validAt=" + validAtTime);
		} else {
		
			PqlParser pqlParser = new PqlParser(this.biz.getBizContext().getGridService(), orderByQueryString.queryString,
					gridPath);
			String gridPaths[] = pqlParser.getPaths();
			if (gridPaths != null && gridPaths.length > 0) {
				gridPath = gridPaths[0];
			}
			boolean isVirtualPath = pado.isVirtualPath(gridPath);
			
			if (isVirtualPath) {
				gridIds = new String[] { this.biz.getBizContext().getGridService().getDefaultGridId() };
			} else {
				gridIds = pqlParser.getGridIds();
			}
			queryString = pqlParser.getPql();
			fullPath = pqlParser.getFullPath();
			gridIds = pqlParser.getGridIds();
			criteria.setId("pql://" + pqlParser.getFullPql() + "&validAt=" + validAtTime);
		}

		
		
		if (orderByQueryString.isAscending == false) {
			criteria.setAscending(false);
		} else {
			criteria.setAscending(orderAscending);
		}
		criteria.setFetchSize(batchSize);
		if (orderByQueryString.orderBy == null) {
			criteria.setSortField(orderBy);
		} else {
			criteria.setSortField(orderByQueryString.orderBy);
		}
		
		criteria.setOrdered(criteria.getSortField() != null);
		criteria.setForceRebuildIndex(forceRebuildIndex);
		criteria.setGridIds(gridIds);
		criteria.setGridService(this.biz.getBizContext().getGridService());
		criteria.setFullPath(fullPath);
		criteria.setQueryString(queryString);
		criteria.setProviderKey(Constants.TEMPORAL_ENTRY_PROVIDER_KEY);
		criteria.setParameter("validAtTime", validAtTime);
		criteria.setParameter("isReference", isReference);
		criteria.setParameter("depth", depth);
		criteria.setParameter("type", type);
		//ProcessTopN at last
		PqlParser.processTopN(queryString, criteria);
		return criteria;
	}

	@Override
	public Set<ITemporalKey<K>> getKeySet(long validAtTime)
	{
		biz.getBizContext().reset();
		return biz.getKeySet(validAtTime);
	}

	@Override
	public Set<ITemporalKey<K>> getKeySet(long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		return biz.getKeySet(validAtTime, asOfTime);
	}

	@Override
	public ITemporalKey<K> getKey(K identityKey)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		return biz.getKey(identityKey);
	}

	@Override
	public ITemporalKey<K> getKey(K identityKey, long validAtTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		return biz.getKey(identityKey, validAtTime);
	}

	@Override
	public ITemporalKey<K> getKey(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		return biz.getKey(identityKey, validAtTime, asOfTime);
	}

	private Set<V> deserialize(Set<V> valueSet)
	{
		if (valueSet != null) {
			for (V value : valueSet) {
				if (value instanceof ITemporalData) {
					((ITemporalData) value).__getTemporalValue().deserializeAll();
				}
			}
		}
		return valueSet;
	}

	@Override
	public Set<V> get(long validAtTime)
	{
		biz.getBizContext().reset();
		return deserialize(biz.get(validAtTime));
	}

	@Override
	public Set<V> get(long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		return deserialize(biz.get(validAtTime, asOfTime));
	}

	private Set<TemporalEntry<K, V>> deserializeTemporalEntrySet(Set<TemporalEntry<K, V>> set)
	{
		if (set != null) {
			for (TemporalEntry<K, V> entry : set) {
				V value = entry.getValue();
				if (value instanceof ITemporalData) {
					((ITemporalData) value).__getTemporalValue().deserializeAll();
				}
			}
		}
		return set;
	}

	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserializeTemporalEntrySet(biz.getAllEntrySet(identityKey));
	}

	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserializeTemporalEntrySet(biz.getAllEntrySet(identityKey, validAtTime));
	}

	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		biz.getBizContext().getGridContextClient().setAdditionalArguments(isReference, depth);
		return deserializeTemporalEntrySet(biz.getAllEntrySet(identityKey, validAtTime, asOfTime));
	}

	@Override
	public IScrollableResultSet<TemporalEntry<K, V>> getAllLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex)
	{
		return getLastTemporalEntries(orderBy, orderAcending, batchSize, forceRebuildIndex, -1);
	}
	
	@Override
	public IScrollableResultSet<TemporalEntry<K, V>> getLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex, int limit)
	{
		if (biz.getBizContext().getGridService().isPureClient() == false) {
			throw new PadoException(
					"Unsupported method. This method is supported for pure clients only.");
		} else if (Pado.getPado(biz.getBizContext().getGridService().getToken()) == null) {
			throw new PadoException(
					"Access denied. Invalid session token: " + " [token="
							+ biz.getBizContext().getGridService().getToken() + "]");
		}
		String gridPath = getGridPath();
		String gridIds[] = this.biz.getBizContext().getGridService().getGridIds(gridPath);
		String gridId = null;
		if (gridIds.length == 0) {
			gridId = this.biz.getBizContext().getGridService().getDefaultGridId();
		} else {
			gridId = gridIds[0];
		}
		if (gridId == null) {
			return null;
		}
		String fullPath = this.biz.getBizContext().getGridService().getFullPath(gridId, gridPath);
		
		GridQuery criteria = GridQueryFactory.createGridQuery();
		criteria.setId("pql://" + gridId + fullPath + " limit " + limit);
		criteria.setAscending(orderAcending);
		criteria.setFetchSize(batchSize);
		criteria.setOrdered(true);
		criteria.setProviderKey(Constants.TEMPORAL_PROVIDER_KEY);
		criteria.setQueryString(".*");
		criteria.setGridIds(gridId);
		criteria.setGridService(biz.getBizContext().getGridService());
		criteria.setFullPath(fullPath);
		criteria.setSortField(orderBy);
		criteria.setForceRebuildIndex(forceRebuildIndex);
		criteria.setLimit(limit);
		
		IGridQueryService qs = GridQueryService.getGridQueryService();	
		IScrollableResultSet<TemporalEntry<K, V>> rs = (IScrollableResultSet) qs.query(criteria);
		return rs;
		
		// TODO: Replace the above with the following
//		BizGridQueryServiceImpl qs = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();	
//		IIndexMatrixBiz imBiz = pado.getCatalog().newInstance(IIndexMatrixBiz.class);
//		return qs.query(imBiz, criteria);
	}

	@Override
	public void reset()
	{
		biz.getBizContext().getGridContextClient().reset();
		setReference(false);
		setPutExceptionEnabled(false);
		setDepth(-1);
	}

	@Override
	public TemporalEntry<K, V> put(K identityKey, V value)
	{
		return put(identityKey, value, false);
	}

	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, boolean isDelta)
	{
		long currentTime = System.currentTimeMillis();
		return put(identityKey, value, currentTime, TemporalUtil.MAX_TIME, currentTime, isDelta);
	}

	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, boolean isDelta)
	{
		long currentTime = System.currentTimeMillis();
		return put(identityKey, value, startValidTime, endValidTime, currentTime, isDelta);
	}

	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, long writtenTime,
			boolean isDelta)
	{
		if (writtenTime < 0) {
			writtenTime = System.currentTimeMillis();
		}
		ITemporalKey<K> tkey = new GemfireTemporalKey<K>(identityKey, startValidTime, endValidTime, writtenTime, biz
				.getBizContext().getUserContext().getUsername());
		ITemporalData<K> data;
		ITemporalValue<K> tvalue;
		if (value instanceof ITemporalData) {
			data = (ITemporalData<K>) value;
		} else {
			data = new GemfireTemporalData(tkey, value);
		}

		tvalue = data.__getTemporalValue();
		if (tvalue == null) {
			tvalue = new GemfireTemporalValue(tkey, (ITemporalData<K>) value);
			data.__setTemporalValue(tvalue);
		}
		tvalue.setDelta(isDelta);
		gridMapBiz.put(tkey, data);
		return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tkey, data);
	}

	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySet)
	{
		return putAttachments(identityKey, value, attachmentIdentityKeySet, false);
	}

	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySet,
			boolean isDelta)
	{
		long currentTime = System.currentTimeMillis();
		return putAttachments(identityKey, value, attachmentIdentityKeySet, currentTime, TemporalUtil.MAX_TIME,
				currentTime, isDelta);
	}

	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySet,
			long startValidTime, long endValidTime, boolean isDelta)
	{
		return putAttachments(identityKey, value, attachmentIdentityKeySet, startValidTime, endValidTime,
				System.currentTimeMillis(), isDelta);
	}

	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, long writtenTime, boolean isDelta)
	{
		ITemporalKey<K> tkey;
		try {
			tkey = clientFactory.createTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, biz
					.getBizContext().getUserContext().getUsername());
			ITemporalData<K> data;
			ITemporalValue<K> tvalue;
			if (value instanceof ITemporalData) {
				data = (ITemporalData<K>) value;
			} else {
				data = clientFactory.createTemporalData(tkey, value);
			}
			tvalue = data.__getTemporalValue();
			if (tvalue == null) {
				tvalue = clientFactory.createTemporalValue((ITemporalData<K>) value, tkey);
				data.__setTemporalValue(tvalue);
			}
			tvalue.setDelta(isDelta);

			// if attachmentIdentityKeySets is specified, it bypasses
			// the attachments defined in the value. Note that it does NOT
			// change the attachment keys in the value.
			if (attachmentIdentityKeySets != null) {
				Map<String, AttachmentSet<K>> map = new HashMap<String, AttachmentSet<K>>(
						attachmentIdentityKeySets.length + 1);
				for (int i = 0; i < attachmentIdentityKeySets.length; i++) {
					map.put(attachmentIdentityKeySets[i].getName(), attachmentIdentityKeySets[i]);
				}
				tvalue.setAttachmentMap(map);
			}
			gridMapBiz.put(tkey, data);
			return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tkey, data);
		} catch (Exception ex) {
			TemporalException te = new TemporalException(ex);
			Logger.error(ex);
			if (isPutExceptionEnabled) {
				throw te;
			}
			return null;
		}
	}

	@Override
	public void remove(K identityKey)
	{
		biz.remove(identityKey);
	}
	
	@Override
	public boolean isRemoved(K identityKey)
	{
		return biz.isRemoved(identityKey);
	}

	@Override
	public boolean isExist(K identityKey)
	{
		return biz.isExist(identityKey);
	}

	@Override
	public void dump(K identityKey)
	{
		dump(identityKey, System.out, null);
	}

	@Override
	public void dump(K identityKey, PrintStream printStream, SimpleDateFormat formatter)
	{
		biz.getBizContext().reset();
		biz.getBizContext().getGridContextClient().setRoutingKeys(Collections.singleton(identityKey));
		TemporalDataList tdl = temporalAdminBiz.getTemporalDataList(identityKey);
		if (tdl == null) {
			System.out.println("Temporal list not found: identityKey=" + identityKey);
		} else {
			tdl.dump(printStream, formatter);
		}
	}

	@Override
	public AttachmentResults<V> getAttachments(K identityKey)
	{
		return __getAttachments(identityKey, -1, -1);
	}

	@Override
	public AttachmentResults<V> getAttachments(K identityKey, long validAtTime)
	{
		return __getAttachments(identityKey, validAtTime, -1);
	}

	/**
	 * Two hop operation: 1) get TemporalEntry, 2) get attachments for the entry
	 */
	@Override
	public AttachmentResults<V> getAttachments(K identityKey, long validAtTime, long asOfTime)
	{
		return __getAttachments(identityKey, validAtTime, asOfTime);
	}

	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey)
	{
		return __getAttachmentsEntries(identityKey, -1, -1);
	}

	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime)
	{
		return __getAttachmentsEntries(identityKey, validAtTime, -1);
	}

	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime, long asOfTime)
	{
		return __getAttachmentsEntries(identityKey, validAtTime, asOfTime);
	}

	private AttachmentResults<V> __getAttachments(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();

		TemporalEntry<K, V> temporalEntry;
		if (validAtTime == -1 && asOfTime == -1) {
			temporalEntry = getEntry(identityKey);
		} else {
			temporalEntry = getEntry(identityKey, validAtTime, asOfTime);
		}
		if (temporalEntry == null) {
			return null;
		}
		ITemporalData data = temporalEntry.getTemporalData();
		data.__getTemporalValue().deserializeAttachments();
		Map<String, AttachmentSet<K>> attachmentMap = data.__getTemporalValue().getAttachmentMap();

		// list to return
		AttachmentResults<V> results = TemporalInternalFactory.getTemporalInternalFactory().createAttachmentResults();
		results.setValue(temporalEntry.getValue());

		// Another hop... Get the attachment values.
		// FlowbackCacheListener could give better performance. It eliminates
		// the additional hop by having each server to send their results
		// directly to the client but complicates the code. Use this for now.
		if (attachmentMap != null) {
			Map<String, List<V>> valueMap = new HashMap<String, List<V>>(attachmentMap.size() + 1);
			results.setAttachmentValues(valueMap);
			Set<Map.Entry<String, AttachmentSet<K>>> set = attachmentMap.entrySet();

			boolean useOnServers = false;
			String gridPath = getGridPath();
			for (Map.Entry<String, AttachmentSet<K>> entry : set) {
				AttachmentSet<K> attachmentSet = entry.getValue();
				String attGridPath = attachmentSet.getGridPath();
				if (attGridPath != null) {
					attGridPath = attGridPath.trim();
					if (attGridPath.equals(gridPath) == false) {
						// grid paths are different. must use onServers.
						useOnServers = true;
						break;
					}
				}

				// query statement must be executed on all servers since
				// it is ad-hoc, i.e., co-location info is not available.
				String queryStatement = attachmentSet.getQueryStatement();
				if (queryStatement != null && queryStatement.trim().length() > 0) {
					useOnServers = true;
					break;
				}
			}

			if (useOnServers) {

				// Broadcast. One request.
				Collection<AttachmentSet<K>> col = attachmentMap.values();
				AttachmentSet<K>[] attSets = col.toArray(new AttachmentSet[col.size()]);
				biz.getBizContext().getGridContextClient().setAdditionalArguments(attSets, isReference, depth);

				Map<String, List<V>> map = biz.__getAttachmentsBroadcast(validAtTime, asOfTime);
				deserializeAttachmentValueListMap(map);
				results.setAttachmentValues(map);

			} else {
				for (Map.Entry<String, AttachmentSet<K>> entry : set) {
					String name = entry.getKey();
					AttachmentSet<K> attachmentSet = entry.getValue();
					biz.getBizContext().getGridContextClient().setRoutingKeys(attachmentSet.getAttachments());
					biz.getBizContext().getGridContextClient()
							.setAdditionalArguments(attachmentSet.getFilter(), isReference, depth);
					biz.getBizContext().getGridContextClient().setGridPath(attachmentSet.getGridPath());
					List<V> list = biz.__getAttachments(validAtTime, asOfTime);
					deserialize(list);
					valueMap.put(name, list);
				}
			}
		}

		return results;
	}

	private List<V> deserialize(List<V> list)
	{
		if (list != null) {
			for (V value : list) {
				deserialize(value);
			}
		}
		return list;
	}

	private List<TemporalEntry<K, V>> deserializeTemporalEntryList(List<TemporalEntry<K, V>> list)
	{
		if (list != null) {
			for (TemporalEntry entry : list) {
				deserialize((V) entry.getValue());
			}
		}
		return list;
	}

	private Map<String, List<V>> deserializeAttachmentValueListMap(Map<String, List<V>> map)
	{
		if (map != null) {
			Collection<List<V>> col = map.values();
			for (List<V> list : col) {
				deserialize(list);
			}
		}
		return map;
	}

	private Map<String, List<TemporalEntry<K, V>>> deserializeAttachementMap(Map<String, List<TemporalEntry<K, V>>> map)
	{
		if (map != null) {
			Collection<List<TemporalEntry<K, V>>> col = map.values();
			for (List<TemporalEntry<K, V>> list : col) {
				deserializeTemporalEntryList(list);
			}
		}
		return map;
	}

	private AttachmentResults<V> __getAttachmentsEntries(K identityKey, long validAtTime, long asOfTime)
	{
		biz.getBizContext().reset();

		TemporalEntry temporalEntry;
		if (validAtTime == -1 && asOfTime == -1) {
			temporalEntry = getEntry(identityKey);
		} else {
			temporalEntry = getEntry(identityKey, validAtTime, asOfTime);
		}
		if (temporalEntry == null) {
			return null;
		}
		ITemporalData data = temporalEntry.getTemporalData();
		data.__getTemporalValue().deserializeAttachments();
		Map<String, AttachmentSet<K>> attachmentMap = data.__getTemporalValue().getAttachmentMap();

		// list to return
		AttachmentResults results = TemporalInternalFactory.getTemporalInternalFactory().createAttachmentResults();
		results.setValue(temporalEntry);

		// Another hop... Get the attachment values.
		// FlowbackCacheListener could give better performance. It eliminates
		// the additional hop by having each server to send their results
		// directly to the client but complicates the code. Use this for now.
		if (attachmentMap != null) {
			Map<String, List<V>> valueMap = new HashMap<String, List<V>>(attachmentMap.size() + 1);
			results.setAttachmentValues(valueMap);
			Set<Map.Entry<String, AttachmentSet<K>>> set = attachmentMap.entrySet();

			boolean useOnServers = false;
			String gridPath = getGridPath();
			HashSet<String> gridIdSet = new HashSet(set.size());
			for (Map.Entry<String, AttachmentSet<K>> entry : set) {
				AttachmentSet<K> attachmentSet = entry.getValue();
				String attGridPath = attachmentSet.getGridPath();
				if (attGridPath != null) {
					attGridPath = attGridPath.trim();
					if (attGridPath.equals(gridPath) == false) {
						IGridRouter router = biz.getBizContext().getGridService().getGridRouter(attGridPath);
						if (router != null) {
							gridIdSet.addAll(router.getAllowedGridIdSet());
						} else {
							String gridIds[] = biz.getBizContext().getGridService().getGridIds(attGridPath);
							if (gridIds != null) {
								for (String gid : gridIds) {
									gridIdSet.add(gid);
								}
							}
						}
					}
				}
				useOnServers = gridIdSet.size() > 0;

				if (useOnServers == false) {
					// query statement must be executed on all servers since
					// it is ad-hoc, i.e., co-location info is not available.
					String queryStatement = attachmentSet.getQueryStatement();
					if (queryStatement != null && queryStatement.trim().length() > 0) {
						useOnServers = true;
					}
				}
			}

			if (useOnServers) {

				// Broadcast. One request.
				biz.getBizContext().getGridContextClient().setGridIds(gridIdSet.toArray(new String[gridIdSet.size()]));
				Collection<AttachmentSet<K>> col = attachmentMap.values();
				AttachmentSet<K>[] attSets = col.toArray(new AttachmentSet[col.size()]);
				biz.getBizContext().getGridContextClient().setAdditionalArguments(attSets, isReference, depth);

				// <String, List<V>>
				Map<String, List<TemporalEntry<K, V>>> map = biz
						.__getAttachmentsEntriesBroadcast(validAtTime, asOfTime);
				deserializeAttachementMap(map);
				results.setAttachmentValues(map);

			} else {
				for (Map.Entry<String, AttachmentSet<K>> entry : set) {
					String name = entry.getKey();
					AttachmentSet<K> attachmentSet = entry.getValue();
					biz.getBizContext().getGridContextClient().setRoutingKeys(attachmentSet.getAttachments());
					biz.getBizContext().getGridContextClient()
							.setAdditionalArguments(attachmentSet.getFilter(), isReference, depth);
					biz.getBizContext().getGridContextClient().setGridPath(attachmentSet.getGridPath());
					Map<ITemporalKey<K>, ITemporalData<K>> map = biz.__getAttachmentsEntries(validAtTime, asOfTime);
					map = deserialize(map);
					List list = new ArrayList(map.size());
					for (Map.Entry<ITemporalKey<K>, ITemporalData<K>> entry2 : map.entrySet()) {
						TemporalEntry te = TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(
								entry2.getKey(), entry2.getValue());
						list.add(te);
					}
					valueMap.put(name, list);
				}
			}
		}

		return results;
	}

	/**
	 * Never invoked
	 */
	@Override
	public List<V> __getAttachments(long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Never invoked
	 */
	@Override
	public Map<String, List<V>> __getAttachmentsOnServer(long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Never invoked
	 */
	@Override
	public Map<String, List<V>> __getAttachmentsBroadcast(long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Never invoked
	 */
	@Override
	public Map<String, List<TemporalEntry<K, V>>> __getAttachmentsEntriesBroadcast(long validAtTime, long asOfTime)
	{
		return null;
	}

	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return getAttachments(attachmentIdentityKeySet, -1, -1);
	}

	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime)
	{
		return getAttachments(attachmentIdentityKeySet, validAtTime, -1);
	}

	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime)
	{
		List<V>[] lists = getAttachments(new AttachmentSet[] { attachmentIdentityKeySet }, validAtTime, asOfTime);
		if (lists == null) {
			return null;
		}
		return lists[0];
	}

	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return getAttachments(attachmentIdentityKeySets, -1, -1);
	}

	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime)
	{
		return getAttachments(attachmentIdentityKeySets, validAtTime, -1);
	}

	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime, long asOfTime)
	{
		if (attachmentIdentityKeySets == null) {
			return null;
		}

		// Another hop... Get the attachment values.
		// FlowbackCacheListener could give better performance. It eliminates
		// the additional hop by having each server to send their results
		// directly to the client but complicates the code. Use this for now.
		List<V>[] attachmentValues = new List[attachmentIdentityKeySets.length];
		for (int i = 0; i < attachmentIdentityKeySets.length; i++) {
			AttachmentSet attachmentSet = attachmentIdentityKeySets[i];
			if (attachmentSet == null || attachmentSet.getAttachments() == null
					|| attachmentSet.getAttachments().size() == 0) {
				continue;
			}
			biz.getBizContext().reset();
			biz.getBizContext().getGridContextClient().setRoutingKeys(attachmentSet.getAttachments());
			biz.getBizContext().getGridContextClient()
					.setAdditionalArguments(attachmentSet.getFilter(), isReference, depth);
			biz.getBizContext().getGridContextClient().setGridPath(attachmentSet.getGridPath());
			attachmentValues[i] = biz.__getAttachments(validAtTime, asOfTime);
			deserialize(attachmentValues[i]);
		}

		return attachmentValues;
	}

	/**
	 * Never invoked
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> __getAttachmentsEntries(long validAtTime, long asOfTime)
	{
		return null;
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return getAttachmentsEntries(attachmentIdentityKeySet, -1, -1);
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime)
	{
		return getAttachmentsEntries(attachmentIdentityKeySet, validAtTime, -1);
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime, long asOfTime)
	{
		Map<ITemporalKey<K>, ITemporalData<K>>[] map = getAttachmentsEntries(
				new AttachmentSet[] { attachmentIdentityKeySet }, validAtTime, asOfTime);
		if (map == null) {
			return null;
		}
		return map[0];
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return getAttachmentsEntries(attachmentIdentityKeySets, -1, -1);
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime)
	{
		return getAttachmentsEntries(attachmentIdentityKeySets, validAtTime, -1);
	}

	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime, long asOfTime)
	{
		if (attachmentIdentityKeySets == null) {
			return null;
		}

		// Another hop... Get the attachment values.
		// FlowbackCacheListener could give better performance. It eliminates
		// the additional hop by having each server to send their results
		// directly to the client but complicates the code. Use this for now.
		Map<ITemporalKey<K>, ITemporalData<K>>[] map = new Map[attachmentIdentityKeySets.length];
		for (int i = 0; i < attachmentIdentityKeySets.length; i++) {
			AttachmentSet attachmentSet = attachmentIdentityKeySets[i];
			if (attachmentSet == null || attachmentSet.getAttachments() == null) {
				continue;
			}
			biz.getBizContext().getGridContextClient().setRoutingKeys(attachmentSet.getAttachments());
			biz.getBizContext().getGridContextClient()
					.setAdditionalArguments(attachmentSet.getFilter(), isReference, depth);
			biz.getBizContext().getGridContextClient().setGridPath(attachmentSet.getGridPath());
			map[i] = biz.__getAttachmentsEntries(validAtTime, asOfTime);
			deserialize(map[i]);
		}

		return map;
	}

	/**
	 * Never invoked
	 */
	@Override
	public Set<ITemporalKey<K>> __getAttachmentsKeys(long validAtTime, long asOfTime)
	{
		return null;
	}

	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return getAttachmentsKeys(attachmentIdentityKeySet, -1, -1);
	}

	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime)
	{
		return getAttachmentsKeys(attachmentIdentityKeySet, validAtTime, -1);
	}

	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime,
			long asOfTime)
	{
		Set<ITemporalKey<K>>[] sets = getAttachmentsKeys(new AttachmentSet[] { attachmentIdentityKeySet }, validAtTime,
				asOfTime);
		if (sets == null) {
			return null;
		}
		return sets[0];
	}

	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return getAttachmentsKeys(attachmentIdentityKeySets, -1, -1);
	}

	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime)
	{
		return getAttachmentsKeys(attachmentIdentityKeySets, validAtTime, -1);
	}

	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime,
			long asOfTime)
	{
		if (attachmentIdentityKeySets == null) {
			return null;
		}

		// Another hop... Get the attachment values.
		// FlowbackCacheListener could give better performance. It eliminates
		// the additional hop by having each server to send their results
		// directly to the client but complicates the code. Use this for now.
		Set<ITemporalKey<K>>[] sets = new Set[attachmentIdentityKeySets.length];
		for (int i = 0; i < attachmentIdentityKeySets.length; i++) {
			AttachmentSet attachmentSet = attachmentIdentityKeySets[i];
			if (attachmentSet == null || attachmentSet.getAttachments() == null) {
				continue;
			}
			biz.getBizContext().reset();
			biz.getBizContext().getGridContextClient().setRoutingKeys(attachmentSet.getAttachments());
			biz.getBizContext().getGridContextClient().setAdditionalArguments(attachmentSet.getFilter());
			biz.getBizContext().getGridContextClient().setGridPath(attachmentSet.getGridPath());
			sets[i] = biz.__getAttachmentsKeys(validAtTime, asOfTime);
		}

		return sets;
	}
}
