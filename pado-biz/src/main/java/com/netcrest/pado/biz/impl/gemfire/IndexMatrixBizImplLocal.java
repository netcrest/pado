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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IIndexMatrixBiz;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.gemfire.GemfireGridService;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.impl.BizGridQueryService;
import com.netcrest.pado.index.gemfire.impl.BizGridQueryServiceImpl;
import com.netcrest.pado.index.gemfire.impl.BizIndexMatrixCollector;
import com.netcrest.pado.index.gemfire.service.GridQueryService;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.GridQueryFactory;
import com.netcrest.pado.index.service.IGridQueryService;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.pql.PqlParser.OrderByQueryString;

public class IndexMatrixBizImplLocal<T> implements IIndexMatrixBiz<T>, IBizLocal
{
	private static final int DEFAULT_FETCH_SIZE = 1000;

	@Resource
	IIndexMatrixBiz<T> biz;

	private String[] gridIds;

	private QueryType queryType = QueryType.PQL;

	private int fetchSize = 1000;

	private int startIndex = 0;

	private String orderByField;

	private boolean ascending = true;

	private boolean forceRebuildIndex = false;

	private Map<String, Object> parameterMap = new HashMap<String, Object>();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(IBiz biz, IPado pado, Object... args)
	{
		this.biz = (IIndexMatrixBiz<T>) biz;
		if (args != null && args.length > 0) {
			if (args[0] != null && args[0].getClass() == Boolean.class) {
				boolean initialize = (Boolean) args[0];
				if (initialize) {
					initializeGridQueryService();
				}
			}
		}
		reset();
	}

	private IGridQueryService initializeGridQueryService()
	{
		GemfireGridService gridService = ((GemfireGridService) biz.getBizContext().getGridService());
		return GridQueryService.initialize(gridService.getIndexMatrixPool(), gridService.getPadoRegionService());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBizContextClient getBizContext()
	{
		return biz.getBizContext();
	}

	private String createId(String queryString)
	{
		String id;
		switch (queryType) {
		case PQL:
			id = "pql://" + queryString;
			break;
		case OQL:
		default:
			id = "oql://" + queryString;
		}
		return id;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public IScrollableResultSet<T> __submitCriteria(GridQuery criteria) throws GridQueryException
	{
		BizGridQueryServiceImpl gridQueryService = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();
		biz.getBizContext().getGridContextClient().setGridCollector(new BizIndexMatrixCollector(criteria, gridQueryService));
		return biz.__submitCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IScrollableResultSet<T> execute(String queryString) throws GridQueryException
	{
		IPado pado = Pado.getPado(biz.getBizContext().getGridService().getToken());
		if (pado == null) {
			throw new PadoException(
					"Access denied. Invalid session token: " + " [token="
							+ biz.getBizContext().getGridService().getToken() + "]");
		}
		if (pado.isLoggedOut()) {
			throw new PadoException(
					"Access denied. Invalid session token: " + " [token="
							+ biz.getBizContext().getGridService().getToken() + "]");
		}
		if (queryString == null) {
			return null;
		}
		queryString = queryString.trim();
		GridQuery criteria = GridQueryFactory.createGridQuery();
		OrderByQueryString orderByQueryString = PqlParser.getOrderBy(queryString);
		criteria.setId(createId(orderByQueryString.queryString));
		criteria.setAscending(ascending);
		criteria.setFetchSize(fetchSize);
		criteria.setSortField(orderByField);
		criteria.setForceRebuildIndex(forceRebuildIndex);
		Boolean isServerQuery = (Boolean) parameterMap.get("IsServerQuery");
		Integer bucketId = (Integer)parameterMap.get("BucketId");
		if ( isServerQuery != null && isServerQuery || bucketId != null) {
			queryType = QueryType.OQL;
			criteria.setProviderKey(Constants.SERVER_PROVIDER_KEY);
			criteria.setQueryString(queryString);
		} else if (queryString.toLowerCase().startsWith("select ")) {
			queryType = QueryType.OQL;
			criteria.setProviderKey(Constants.OQL_PROVIDER_KEY);
			criteria.setQueryString(queryString);
		} else {
			queryType = QueryType.PQL;
			criteria.setProviderKey(Constants.PQL_PROVIDER_KEY);
			criteria.setQueryString(orderByQueryString.queryString);
		}
		criteria.setParameter(parameterMap);
		boolean sortKey = orderByField == null || 
				orderByField.equals("WrittenTime") || 
				orderByField.equals("EndWrittenTime") || 
				orderByField.equals("StartValidTime") || 
				orderByField.equals("EndValidTime") || 
				orderByField.equals("Username") || 
				orderByField.equals("IdentityKey");
		criteria.setSortKey(sortKey);
		if (gridIds == null || gridIds.length == 0) {
			criteria.setGridIds(getBizContext().getGridService().getGridIds());
		} else {
			criteria.setGridIds(gridIds);
		}
		criteria.setGridService(getBizContext().getGridService());

		if (orderByQueryString.isAscending == false) {
			criteria.setAscending(false);
		}
		if (orderByQueryString.orderBy != null) {
			criteria.setSortField(orderByQueryString.orderBy);
		}
		criteria.setOrdered(criteria.getSortField() != null);
		//Check if it is topN type, it must be the last statement
		//for creating GridQuery
		if (queryType == QueryType.PQL) {
			PqlParser.processTopN(queryString, criteria);
		}
		
		IGridQueryService qs = GridQueryService.getGridQueryService();
		return (IScrollableResultSet<T>)qs.query(criteria);

		// TODO: Replace the above with the following
//		BizGridQueryServiceImpl qs = (BizGridQueryServiceImpl)BizGridQueryService.getGridQueryService();
//		return (IScrollableResultSet<T>)qs.query(this, criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridIds(String... gridIds)
	{
		this.gridIds = gridIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getGridIds()
	{
		return gridIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFetchSize(int fetchSize)
	{
		if (fetchSize <= 0) {
			this.fetchSize = DEFAULT_FETCH_SIZE;
		} else {
			this.fetchSize = fetchSize;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFetchSize()
	{
		if (fetchSize == 0) {
			fetchSize = DEFAULT_FETCH_SIZE;
		}
		return fetchSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryType getQueryType()
	{
		return queryType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setQueryType(QueryType queryType)
	{
		this.queryType = queryType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOrderByField()
	{
		return orderByField;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOrderByField(String sortField)
	{
		this.orderByField = sortField;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAscending()
	{
		return ascending;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStartIndex()
	{
		return startIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParameter(String name, Object value)
	{
		parameterMap.put(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParameter(String name)
	{
		return parameterMap.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isForceRebuildIndex()
	{
		return forceRebuildIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setForceRebuildIndex(boolean forceRebuildIndex)
	{
		this.forceRebuildIndex = forceRebuildIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset()
	{
		gridIds = null;
		queryType = QueryType.OQL;
		fetchSize = DEFAULT_FETCH_SIZE;
		startIndex = 0;
		orderByField = null;
		ascending = false;
		forceRebuildIndex = false;
		parameterMap.clear();
	}
}
