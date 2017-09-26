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

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IBizLocal;
import com.netcrest.pado.annotation.BizClass;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.link.IIndexMatrixBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * IIndexMatrixBiz provides query execution service that returns scrollable
 * result sets.
 * <p>
 * <b>Arguments:
 * {@link IBizLocal#init(IBiz, com.netcrest.pado.IPado, Object...)}</b>
 * <p>
 * <blockquote> <b>boolean initialize</b> - true to initialize the grid query
 * service. This is an optional argument with the default value of false.
 * Typically, the query service is initialized once during the application life
 * cycle. It can however be initialized as needed in case the query service
 * needs to be reset. If initialized, the existing IIndexMatrixBiz instances may
 * be invalid depending on the underlying data grid configuration. </blockquote>
 * <p>
 * <b>OQL Example:</b>
 * 
 * <pre>
 * // Create a new instance of IIndexMatrixBiz with the initialize flag set to
 * // true
 * IIndexMatrixBiz imbiz = pado.newInstance(IIndexMatrixBiz.class, true);
 * // Set the query attributes
 * imbiz.setFetchSize(100);
 * imbiz.setQueryType(QueryType.OQL);
 * imbiz.setGridIds(&quot;mygrid&quot;);
 * // execute query
 * IScrollableResultSet rs = imbiz.execute(&quot;select * from /mygrid/account.keySet&quot;);
 * // Get the first page of the result set
 * List results = rs.toList();
 * // Get the next page
 * rs.nextSet();
 * results = rs.toList();
 * // Get the previous page
 * rs.previousSet();
 * results = rs.toList();
 * // Close the result set to immediately release resources
 * rs.close();
 * </pre>
 * 
 * <b>PQL Example:</b>
 * 
 * <pre>
 * // Create a new instance of IIndexMatrixBiz with the initialize flag set to
 * // true
 * IIndexMatrixBiz imbiz = pado.newInstance(IIndexMatrixBiz.class, true);
 * // Set the query attributes
 * imbiz.setFetchSize(100);
 * imbiz.setQueryType(QueryType.PQL); // Default is PQL
 * imbiz.setGridIds(&quot;mygrid&quot;);
 * // Order-by field can be any field name of the result set records.
 * // For example, WrittenTime or Username for temporal result set.
 * imbiz.setOrderByField(&quot;Username&quot;);
 * // Execute Lucene query
 * IScrollableResultSet rs = imbiz.execute(&quot;account?AccountId:acct_a&quot;);
 * // Execute OQL query
 * IScrollableResultSet rs = imbiz.execute(&quot;account.AccountId='acct_a'&quot;);
 * // Get the first page of the result set
 * List results = rs.toList();
 * // Get the next page
 * rs.nextSet();
 * results = rs.toList();
 * // Get the previous page
 * rs.previousSet();
 * results = rs.toList();
 * // Close the result set to immediately release resources
 * rs.close();
 * </pre>
 * 
 * @author dpark
 * 
 */
@BizClass
public interface IIndexMatrixBiz<T> extends IIndexMatrixBizLink<T>
{
	/**
	 * Do NOT directly invoke this method. It's a private method for internal
	 * use only. Submits the specified query criteria.
	 * 
	 * @param criteria
	 *            Query criteria
	 * @return Returns scrollable result set.
	 * @throws GridQueryException
	 *             Thrown if the query fails
	 */
	@BizMethod
	@OnServer
	IScrollableResultSet<T> __submitCriteria(GridQuery criteria) throws GridQueryException;

	/**
	 * Executes the specified query string based on the attributes set via the
	 * set methods. The content structure of the returned result set differs
	 * depending on the {@linkplain QueryType} as follows:
	 * <ul>
	 * <li><b>{@linkplain QueryType#PQL}</b> - For temporal Lucene query, it
	 * returns a result set with {@linkplain TemporalEntry} objects. For OQL
	 * query, it returns a result set with value objects. That means if temporal
	 * objects are stored in the path then it returns ITemporalData objects. To
	 * extract the actual object, invoke {@link ITemporalData#getValue()}</li>
	 * <p>
	 * <li><b>{@linkplain QueryType#OQL}</b> - An OQL query always begins with
	 * "select" and returns a result set that conforms to the OQL select
	 * projection. Because Pado creates and streams its own results, the OQL's
	 * "order by" is only performed on the local data set on each server and
	 * therefore should not be used. Instead use the
	 * {@linkplain #setOrderByField(String)} which performs order-by on the
	 * entire result set.</li>
	 * </ul>
	 * 
	 * <b>IMPORTANT:</b> <i>This method uses the query string as the unique ID
	 * to cache the returned result set in the grid. This means the result set
	 * can be shared across different clients.</i> If result-sharing is not
	 * desirable then invoke {@linkplain #execute(String, String)} instead.
	 * <p>
	 * 
	 * @param queryString
	 *            Query string. Supported query types are
	 *            {@linkplain QueryType#OQL} and {@linkplain QueryType#OQL}.
	 * @return Scrollable result set
	 * @throws GridQueryException
	 *             Thrown if the query fails
	 */
	public IScrollableResultSet<T> execute(String queryString) throws GridQueryException;

	/**
	 * Executes the specified query string based on the attributes set via the
	 * set methods. The content structure of the returned result set differs
	 * depending on the {@linkplain QueryType} as follows:
	 * <ul>
	 * <li><b>{@linkplain QueryType#PQL}</b> - For temporal Lucene query, it
	 * returns a result set with {@linkplain TemporalEntry} objects. For OQL
	 * query, it returns a result set with value objects. That means if temporal
	 * objects are stored in the path then it returns ITemporalData objects. To
	 * extract the actual object, invoke {@link ITemporalData#getValue()}</li>
	 * <p>
	 * <li><b>{@linkplain QueryType#OQL}</b> - An OQL query always begins with
	 * "select" and returns a result set that conforms to the OQL select
	 * projection. Because Pado creates and streams its own results, the OQL's
	 * "order by" is only performed on the local data set on each server and
	 * therefore should not be used. Instead use the
	 * {@linkplain #setOrderByField(String)} which performs order-by on the
	 * entire result set.</li>
	 * </ul>
	 * 
	 * @param queryString
	 *            Query string. Supported query types are
	 *            {@linkplain QueryType#OQL} and {@linkplain QueryType#OQL}.
	 * @param resultId
	 *            Uniquely identifies the returned result set. Pado keeps query
	 *            results in its L2 cache shared across client applications. If
	 *            result-sharing is not desirable then specify a client specific
	 *            ID. If null or the length is zero, then it sets resultId
	 *            to the query string instead.
	 * @return Scrollable result set
	 * @throws GridQueryException
	 *             Thrown if the query fails
	 */
	IScrollableResultSet<T> execute(String queryString, String resultId) throws GridQueryException;

	/**
	 * Sets the grid IDs. If null, then the normal IBiz grid selection process
	 * is performed.
	 * 
	 * @param gridIds
	 *            Grid Ids
	 */
	public void setGridIds(String... gridIds);

	/**
	 * Returns the grid IDs
	 */
	public String[] getGridIds();

	/**
	 * Sets the fetch size. Default: 1000.
	 * 
	 * @param fetchSize
	 *            Fetch size
	 */
	public void setFetchSize(int fetchSize);

	/**
	 * Returns the fetch size. Default: 1000.
	 */
	public int getFetchSize();

	/**
	 * Sets the result set size limit. Default: -1, i.e., no limit.
	 * 
	 * @param limit
	 *            Size limit. -1 if no limit.
	 */
	public void setLimit(int fetchSize);

	/**
	 * Returns the result set size limit. Default: 1000, i.e., no limit.
	 */
	public int getLimit();

	/**
	 * Returns the query type. Default: {@link QueryType#PQL}.
	 */
	public QueryType getQueryType();

	/**
	 * Sets the query type. Default: {@link QueryType#OQL}.
	 * 
	 * @param queryType
	 *            Query type
	 */
	public void setQueryType(QueryType queryType);

	/**
	 * Returns the object field to be sorted.
	 */
	public String getOrderByField();

	/**
	 * Sets the name of the object field to be ordered by. Order-by support is
	 * provided for only those query types that do not support order-by. For
	 * example, {@link QueryType#OQL} supports "order by" and therefore the
	 * order by attributes are ignored.
	 * 
	 * @param orderTypeField
	 *            The name of the object field to be ordered by
	 */
	public void setOrderByField(String orderTypeField);

	/**
	 * Returns true if order by ascending order, otherwise, unordered.
	 */
	public boolean isAscending();

	/**
	 * Sets the ascending order. The default value is true.
	 * 
	 * @param ascending
	 *            true to ascend, false for no ordering.
	 */
	public void setAscending(boolean ascending);

	/**
	 * Returns the start index for the first batch of the result set.
	 */
	public int getStartIndex();

	/**
	 * Sets the start index for the first batch of the result set.
	 */
	public void setStartIndex(int startIndex);

	/**
	 * Sets an additional parameter that may be required by the query execution
	 * provider.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            value
	 */
	public void setParameter(String name, Object value);

	/**
	 * Returns the additional parameter identified by the specified parameter
	 * name.
	 * 
	 * @param name
	 *            Parameter name
	 */
	public Object getParameter(String name);

	/**
	 * Returns true if the index is to be rebuilt. If false, then the index is
	 * created only if it does not exist. Note that the index eventually expires
	 * and it is removed from the grid.
	 */
	public boolean isForceRebuildIndex();

	/**
	 * Forces the index to be rebuild if even the index exists.
	 * 
	 * @param forceRebuildIndex
	 *            true to rebuild, false to use the existing index.
	 */
	public void setForceRebuildIndex(boolean forceRebuildIndex);

	/**
	 * Resets all of the attributes to the initialization (default) state.
	 */
	public void reset();
}
