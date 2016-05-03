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
package com.netcrest.pado.index.service;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;
import com.netcrest.pado.internal.impl.GridService;
import com.netcrest.pado.internal.impl.PadoClientManager;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.server.PadoServerManager;

/**
 * GridQuery contains query criteria for executing the specified query string in
 * the grid and forming the scrollable result set the way the client wants to
 * receive.
 * 
 */
public abstract class GridQuery implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Default fetch size
	 */
	protected static final int DEFAULT_FETCH_SIZE = 1000;

	/**
	 * Grid service
	 */
	protected transient GridService gridService;

	/**
	 * Target grid IDs
	 */
	protected String[] gridIds;

	/**
	 * Provider key
	 */
	protected String providerKey;

	/**
	 * Query string to be executed in the grid
	 */
	protected String queryString;

	/**
	 * Full path. This is required for those providers that cannot include the
	 * full path in the query string.
	 */
	protected String fullPath;

	/**
	 * Fetch size. Default is 1000.
	 */
	protected int fetchSize;

	/**
	 * The actual size of the page that internally queries from each server
	 * during aggregation.
	 */
	protected int aggregationPageSize = PadoUtil.getInteger("index.pageSize", DEFAULT_FETCH_SIZE);

	/**
	 * Start index. Default is 0
	 */
	protected int startIndex = 0;

	/**
	 * Start page index. Retrieves page(s) of results in the range of
	 * [startPageIndex, endPageIndex]
	 */
	protected int startPageIndex = 0;

	/**
	 * End page index. Retrieves page(s) of results in the range of
	 * [startPageIndex, endPageIndex]
	 */
	protected int endPageIndex = 0;

	/**
	 * Immutable for this class Internal subclass mutates the value in the index
	 * matrix building process
	 */
	protected boolean returnKey = false;

	/**
	 * Order by flag
	 */
	protected boolean ordered;

	/**
	 * Transient query flag
	 */
	protected boolean transientQuery = false;

	/**
	 * Name of the object field to be sorted
	 */
	protected String sortField;

	/**
	 * Result set ascending flag
	 */
	protected boolean ascending;

	/**
	 * If results are (key,value) pairs then having sortKey=true sorts keys.
	 */
	protected boolean sortKey;

	/**
	 * Unique ID for this query
	 */
	protected String id;

	/**
	 * Force rebuild index flag. true to force the grid to rebuild the index
	 */
	protected boolean forceRebuildIndex = false;

	/**
	 * If enabled, then an exception is thrown when the index expires
	 */
	protected boolean throwExceptionOnExpire = false;

	/**
	 * Map of additional parameters that the provider may needed
	 */
	protected Map<String, Object> parameterMap = new HashMap<String, Object>();

	/**
	 * Size limit of the result set. This field is ignored if the query supports
	 * the limit token. For example, the limit value is ignored for GemFire OQL
	 * which already supports 'limit'. Default is -1, i.e., no limit.
	 */
	protected int limit = -1;

	/**
	 * Constructs an empty GridQuery object
	 */
	public GridQuery()
	{
	}

	/**
	 * Returns the result class type variable.
	 */
	public TypeVariable<?>[] getResultClass()
	{
		TypeVariable<?>[] variables = getClass().getTypeParameters();
		return variables;
	}

	/**
	 * Returns the ID of this query. This ID must be unique per distinct query
	 * as it identifies the result set in the grid which serves as an L2 cache.
	 */
	public String getId()
	{
		if (id == null) {
			id = queryString + (sortField == null ? "" : sortField) + ascending;
		}
		return id;
	}

	/**
	 * Sets the query ID.
	 * 
	 * @param id
	 *            A unique ID that identifies this query
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Sets the target grid IDs. This is a required attribute.
	 * 
	 * @param gridIds
	 *            Grid IDs
	 */
	public void setGridIds(String... gridIds)
	{
		this.gridIds = gridIds;
	}

	/**
	 * Returns the target grid IDs.
	 */
	public String[] getGridIds()
	{
		return gridIds;
	}

	/**
	 * Sets the query string. This is a required attribute.
	 * 
	 * @param queryString
	 *            Query string specific to the provider query language
	 */
	public void setQueryString(String queryString)
	{
		this.queryString = queryString;
	}

	/**
	 * Sets the fetch size. The default size is 1000.
	 * 
	 * @param fetchSize
	 *            Fetch size
	 */
	public void setFetchSize(int fetchSize)
	{
		if (fetchSize <= 0) {
			this.fetchSize = DEFAULT_FETCH_SIZE;
		} else {
			this.fetchSize = fetchSize;
		}
	}

	/**
	 * Enables or disables "order by" for those providers that do not support
	 * order by natively. This flag has no effect for the providers that
	 * natively support order by.
	 * 
	 * @param ordered
	 *            true to enable order by, false to disable order by
	 */
	public void setOrdered(boolean ordered)
	{
		this.ordered = ordered;
	}

	/**
	 * Returns the query string.
	 */
	public String getQueryString()
	{
		return queryString;
	}

	/**
	 * Returns the fetch size. The default is 1000.
	 */
	public int getFetchSize()
	{
		if (fetchSize == 0) {
			fetchSize = DEFAULT_FETCH_SIZE;
		}
		return fetchSize;
	}

	/**
	 * Returns the aggregation page size that is used when aggregating the
	 * remaining results from each server.
	 */
	public int getAggregationPageSize()
	{
		return aggregationPageSize;
	}

	/**
	 * Sets the aggregation page size that is used when aggregating the
	 * remaining results from each server. This value maybe increased by the
	 * underlying result set provider in order to improve the result set
	 * aggregation performance.
	 * 
	 * @param aggregationPageSize
	 *            The aggregation page size
	 */
	public void setAggregationPageSize(int aggregationPageSize)
	{
		this.aggregationPageSize = aggregationPageSize;
	}

	/**
	 * Returns the full path. This is a required attribute for providers that do
	 * not natively support the concept of path.
	 */
	public String getFullPath()
	{
		return fullPath;
	}

	/**
	 * Sets the full path.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public void setFullPath(String fullPath)
	{
		this.fullPath = fullPath;
	}

	/**
	 * Returns true if "order by" is enabled.
	 */
	public boolean isOrdered()
	{
		return ordered;
	}

	/**
	 * Returns the provider key.
	 */
	public String getProviderKey()
	{
		return providerKey;
	}

	/**
	 * The provider key to identify the plug-in provider.
	 */
	public void setProviderKey(String providerKey)
	{
		this.providerKey = providerKey;
	}

	/**
	 * Returns the name of the object field to be sorted.
	 */
	public String getSortField()
	{
		return sortField;
	}

	/**
	 * Sets the object field to sort.
	 * 
	 * @param sortField
	 *            Object field name
	 */
	public void setSortField(String sortField)
	{
		this.sortField = sortField;
	}

	/**
	 * Returns true if the result set is to be sorted by key. This is meaningful
	 * only if the results are key/value pairs.
	 */
	public boolean isSortKey()
	{
		return sortKey;
	}

	/**
	 * Sets the sort by key flag. This is meaningful only if the results are
	 * key/value pairs.
	 * 
	 * @param sortKey
	 *            true to sort by key, false to sort by value.
	 */
	public void setSortKey(boolean sortKey)
	{
		this.sortKey = sortKey;
	}

	/**
	 * Returns true if the result set is to be sorted in ascending order.
	 */
	public boolean isAscending()
	{
		return ascending;
	}

	/**
	 * Sets the ascending order flag.
	 * 
	 * @param ascending
	 *            true to ascend, false to no ordering
	 */
	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	/**
	 * Returns the start index of the first page in the result set. The default
	 * is 0 or the first row.
	 */
	public int getStartIndex()
	{
		return startIndex;
	}

	/**
	 * Sets the start index of the first page in the result set. The default is
	 * 0 or the first row.
	 * 
	 * @param startIndex
	 *            Start index
	 */
	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	public void setPageRange(int startPageIndex, int endPageIndex)
	{
		this.startPageIndex = startPageIndex;
		this.endPageIndex = endPageIndex;
	}

	public int getStartPageIndex()
	{
		return this.startPageIndex;
	}

	public int getEndPageIndex()
	{
		return this.endPageIndex;
	}

	/**
	 * Returns the return key.
	 */
	public boolean isReturnKey()
	{
		return returnKey;
	}

	/**
	 * Returns the map of &lt;name, value&gt; pairs of additional provider
	 * specific parameters.
	 */
	public Map<String, Object> getParams()
	{
		return parameterMap;
	}

	/**
	 * Sets the map of &lt;name, value&gt; pairs of additional provider specific
	 * parameters.
	 * 
	 * @param params
	 *            Parameter map
	 */
	public void setParameter(Map<String, Object> params)
	{
		this.parameterMap = params;
	}

	/**
	 * Sets the specified parameter name and value.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public void setParameter(String name, Object value)
	{
		if (parameterMap == null) {
			parameterMap = new HashMap<String, Object>();
		}
		parameterMap.put(name, value);
	}

	/**
	 * Returns the value of the specified parameter name.
	 * 
	 * @param name
	 *            Parameter name
	 */
	public Object getParam(String name)
	{
		if (parameterMap == null) {
			return null;
		}
		return parameterMap.get(name);
	}

	/**
	 * Returns the force-build-index flag. If true, then the forces the grid to
	 * rebuild the index.
	 */
	public boolean isForceRebuildIndex()
	{
		return forceRebuildIndex;
	}

	/**
	 * Sets the force-build-index flag.
	 * 
	 * @param forceRebuildIndex
	 *            true to force the grid to rebuild the index, false to use the
	 *            existing index from the L2 cache
	 */
	public void setForceRebuildIndex(boolean forceRebuildIndex)
	{
		this.forceRebuildIndex = forceRebuildIndex;
	}

	/**
	 * Returns GridService for IBiz-based invocation.
	 */
	public GridService getGridService()
	{
		return gridService;
	}

	/**
	 * Sets GridService. GridService is typically obtained by invoking
	 * {@link Pado#getCatalog()#getGridService()}. This is a required attribute
	 * if the grid service is initialized via IBiz.
	 * 
	 * @param gridService
	 *            GridService for retrieving region information.
	 */
	public void setGridService(GridService gridService)
	{
		this.gridService = gridService;
	}

	/**
	 * Returns the result set size limit. The size limit only applies to queries
	 * that do not support limit. For example, if the query string is GemFire
	 * OQL, this property is ignored as OQL already supports 'limit'. Default is
	 * -1, i.e., no limit.
	 */
	public int getLimit()
	{
		return limit;
	}

	/**
	 * Sets the result set size limit. -1 to set no limit. The limit only
	 * applies to queries that do not support limit. For example, if the query
	 * string is GemFire OQL, this property is ignored as OQL already supports
	 * 'limit'.
	 * 
	 * @param limit
	 *            Result set size limit. Default is -1, i.e., no limit.
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	/**
	 * Returns the result set limit for a single server. The server limit is
	 * determined by dividing the limit by the number of running servers. This
	 * method is for server only. If a pure client invokes this then it returns
	 * the {@linkplain #getLimit()} value.
	 */
	public int getServerLimit()
	{
		if (PadoUtil.isPureClient()) {
			return getLimit();
		}
		if (limit > 0) {
			return (int) Math.ceil(limit / PadoServerManager.getPadoServerManager().getServerCount());
		} else {
			return limit;
		}
	}

	/**
	 * Shallow copies this grid query to the specified grid query.
	 * 
	 * @param another
	 *            Copy-to query
	 */
	public void copyTo(GridQuery another)
	{
		another.id = this.id;
		another.providerKey = this.providerKey;
		another.queryString = this.queryString;
		another.fetchSize = this.fetchSize;
		another.aggregationPageSize = this.aggregationPageSize;
		another.startIndex = this.startIndex;
		another.returnKey = this.returnKey;
		another.ordered = this.ordered;
		another.transientQuery = this.transientQuery;
		another.sortField = this.sortField;
		another.sortKey = this.sortKey;
		another.ascending = this.ascending;
		another.parameterMap = this.parameterMap;
		another.forceRebuildIndex = this.forceRebuildIndex;
		another.throwExceptionOnExpire = this.throwExceptionOnExpire;
		another.fullPath = this.fullPath;
		another.gridIds = this.gridIds;
		another.gridService = this.gridService;
		another.limit = this.limit;
	}

	/**
	 * Returns true if this query is transient. TransientQuery is a fast
	 * changing query such that the result set L2 cache has a shorter ITTL.
	 */
	public boolean isTransientQuery()
	{
		return transientQuery;
	}

	/**
	 * TransientQuery is a fast changing query that result set L2 cache has a
	 * shorter ITTL.
	 */
	public void setTransientQuery(boolean transientQuery)
	{
		this.transientQuery = transientQuery;
	}

	/**
	 * Returns true if the result set throws an exception when it expires.
	 */
	public boolean isThrowExceptionOnExpire()
	{
		return throwExceptionOnExpire;
	}

	/**
	 * Sets the exception flag. The default is false.
	 * <p>
	 * If true then {@link GridQueryResultSetExpiredException} may be thrown on
	 * an {@link IScrollableResultSet} scrolling operation. Otherwise, the query
	 * is automatically re-executed if the result set has been expired, allowing
	 * the user to continuously paginate the result set after a long idle time
	 * period without explicitly executing the query.
	 * 
	 * @param throwExceptionOnExpire
	 *            true to throw exception, false to re-execute query upon
	 *            expiration
	 */
	public void setThrowExceptionOnExpire(boolean throwExceptionOnExpire)
	{
		this.throwExceptionOnExpire = throwExceptionOnExpire;
	}

}
