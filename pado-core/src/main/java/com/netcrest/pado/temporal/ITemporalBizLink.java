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
package com.netcrest.pado.temporal;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.IBiz;
import com.netcrest.pado.IGridContextClient;
import com.netcrest.pado.annotation.WithGridCollector;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.index.service.IScrollableResultSet;

/**
 * ITemporalBizLink provides temporal data services for the specified grid path.
 * The grid path can be changed as needed before invoking ITemporalBizLink
 * methods. It is a class loader link class that links the main class loader to
 * an IBiz class loader.
 * 
 * @author dpark
 * 
 * @param <K>
 * @param <V>
 */
public interface ITemporalBizLink<K, V> extends IBiz
{

	/**
	 * ENTRY not supported currently. 10/5/2014
	 * 
	 * @author dpark
	 * 
	 */
	public enum ResultSetType
	{
		TEMPORAL_ENTRY, TEMPORAL_KEY, TEMPORAL_DATA, ENTRY, KEY, VALUE
	};

	/**
	 * Sets the grid path.
	 * 
	 * @param gridPath
	 *            Grid path.
	 */
	void setGridPath(String gridPath);

	/**
	 * Returns the grid path.
	 */
	String getGridPath();

	/**
	 * Sets the reference flag.
	 * 
	 * @param isReference
	 *            If true, then the query results return object references.
	 *            Default is false.
	 */
	void setReference(boolean isReference);

	/**
	 * Returns true if the query results include object references. The default
	 * value is false, i.e., the query results do not include object references
	 * and include only identity keys.
	 */
	boolean isReference();

	/**
	 * Sets the maximum depth of the object graph to traverse when searching the
	 * object references. This parameter is provided to handle circular
	 * references.
	 * <p>
	 * If -1, then it defaults to the depths specified in the object schema such
	 * as {@link KeyType}. If 0, then object references are not searched. It is
	 * same as setting {@link #isReference()} to false. Note that the depth is
	 * always initiated from the top-level object and decremented onwards. The
	 * nested objects' depths are never used during search. The default value is
	 * -1.
	 * 
	 * @param depth
	 *            The object reference depth. -1 defaults to the depths defined
	 *            in the object schema, i.e., {@link KeyType}. 0 indicates no
	 *            depth, 1 indicates A->B, 2 indicates A->B->C, etc. where A is
	 *            the top object, B is A's nested object, C is B's nested
	 *            object. Default is -1.
	 */
	void setDepth(int depth);

	/**
	 * Returns the maximum depth of the object graph to traverse when searching
	 * the object references. This parameter is provided to handle circular
	 * references.
	 * <p>
	 * If -1, then it defaults to the depths specified in the object schema such
	 * as {@link KeyType}. If 0, then object references are not searched. It is
	 * same as setting {@link #isReference()} to false. Note that the depth is
	 * always initiated from the top-level object and decremented onwards. The
	 * nested objects' depths are never used during search. The default value is
	 * -1.
	 */
	int getDepth();

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
	void setPutExceptionEnabled(boolean isPutExceptionEnabled);

	/**
	 * Returns true if put throws TemporalException upon failure. Returns false
	 * otherwise.
	 * 
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #put(Object, Object, long, long, long, boolean)
	 */
	boolean isPutExceptionEnabled();

	/**
	 * Returns the admin object for accessing temporal data in the form of
	 * internal data structures, i.e., ITemporalKey and ITemporalData.
	 */
	ITemporalAdminBizLink<K, V> getTemporalAdminBiz();

	/**
	 * Returns the latest value as of now mapped by the specified identity key.
	 * It returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	V get(K identityKey);

	/**
	 * Returns the value that satisfy the specified valid-at time. It returns
	 * null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	V get(K identityKey, long validAtTime);

	/**
	 * Returns the value that satisfy the specified valid-at and as-of times. It
	 * returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 * 
	 */
	V get(K identityKey, long validAtTime, long asOfTime);

	/**
	 * Returns the latest temporal entry as of now mapped by the specified
	 * identity key. It returns null if the value is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	TemporalEntry<K, V> getEntry(K identityKey);

	/**
	 * Returns the temporal entry that satisfies the specified valid-at. It
	 * returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 * 
	 */
	TemporalEntry<K, V> getEntry(K identityKey, long validAtTime);

	/**
	 * Returns the temporal entry that satisfies the specified valid-at and
	 * as-of times. It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	TemporalEntry<K, V> getEntry(K identityKey, long validAtTime, long asOfTime);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at time.
	 * Note that it does not take the identity key. It returns null if the entry
	 * is not found.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and
	 * as-of times. Note that it does not take the identity key. It returns null
	 * if the entries are not found.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime, long asOfTime);

	/**
	 * Returns the temporal values that satisfy the specified valid-at and as-of
	 * times for the specified query. Note that it does not take the identity
	 * key. It returns null if the values are not found.
	 * 
	 * @param queryStatement
	 *            Query statement native to the underlying data grid product(s),
	 *            i.e., LUCENE, GemFire OQL, etc.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 */
	List<V> getQueryValues(String queryStatement, long validAtTime, long asOfTime);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and
	 * as-of times for the specified query. Note that it does not take the
	 * identity key. It returns null if the entries are not found.
	 * 
	 * @param queryStatement
	 *            Query statement native to the underlying data grid product(s),
	 *            i.e., LUCENE, GemFire OQL, etc.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 */
	List<TemporalEntry<K, V>> getQueryEntries(String queryStatement, long validAtTime, long asOfTime);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and
	 * as-of times. Note that it does not take the identity key. It returns null
	 * if the entry is not found. The returned result set is scrollable as it
	 * may contain a large number of entries. <i>For pure clients only.</i>
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 * 
	 */
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and
	 * as-of times. Note that it does not take the identity key. It returns null
	 * if the entry is not found. The returned result set is scrollable as it
	 * may contain a large number of entries. <i>For pure clients only.</i>
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 * 
	 * 
	 */
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the now-relative temporal entries for the given PQL query string.
	 * It returns null if the entry is not found. The returned result set is
	 * scrollable as it may contain a large number of entries. <i>For pure
	 * clients only.</i>
	 * 
	 * @param queryStatement
	 *            PQL query string. If null or empty, then this method call is
	 *            equivalent to {@link #getEntryResultSet(long, long)}.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 * 
	 * 
	 */
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(String queryStatement,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the temporal entries that satisfy the specified valid-at and
	 * as-of times for the given PQL query string. It returns null if the entry
	 * is not found. The returned result set is scrollable as it may contain a
	 * large number of entries. <i>For pure clients only.</i>
	 * 
	 * @param queryStatement
	 *            PQL query string. If null or empty, then this method call is
	 *            equivalent to {@link #getEntryResultSet(long, long)}.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 * 
	 * 
	 */
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(String queryStatement,
			long validAtTime, long asOfTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex);

	/**
	 * Returns the now-relative temporal entries for the given PQL query string.
	 * It returns null if the entry is not found. The returned result set is
	 * scrollable as it may contain a large number of entries. <i>For pure
	 * clients only.</i>
	 * 
	 * @param queryStatement
	 *            PQL query string. If null or empty, then this method call is
	 *            equivalent to {@link #getEntryResultSet(long, long)}.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 * 
	 */
	IScrollableResultSet<V> getValueResultSet(String queryStatement, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the temporal values that satisfy the specified valid-at and as-of
	 * times for the given PQL or OQL query string. If OQL, the select project
	 * must be identity key. It returns null if the entry is not found. The
	 * returned result set is scrollable as it may contain a large number of
	 * entries. <i>For pure clients only.</i>
	 * 
	 * @param queryStatement
	 *            PQL query string. If null or empty, then this method call is
	 *            equivalent to {@link #getEntryResultSet(long, long)}.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 */
	IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the temporal values that satisfy the specified valid-at and end
	 * written time range for the given PQL or OQL query string. Note that this
	 * method retrieves not more than one valid object per temporal list. If
	 * OQL, the select project must be identity key. It searches temporal values
	 * that fall in the specified written time range. It returns null if the
	 * values are not found. The returned result set is scrollable as it may
	 * contain a large number of entries. <i>For pure clients only.</i>
	 * 
	 * @param queryStatement
	 *            PQL and OQL query string. If OQL, i.e., begins with "select"
	 *            then the select projection must be identity key. For example,
	 *            "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e where e.value.value['PortfolioId']='port_a'"
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param fromWrittenTime
	 *            start of the written time range. -1 for current time
	 * @param toWrittenTime
	 *            end of the written time range. -1 for current time.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 */
	IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex);

	/**
	 * Returns the temporal values that satisfy the specified valid-at and end
	 * written time range for the given PQL query string. Note that this method
	 * retrieves not more than one valid object per temporal list. It searches
	 * temporal values that fall in the specified written time range. It returns
	 * null if the entries are not found. The returned result set is scrollable
	 * as it may contain a large number of entries. <i>For pure clients
	 * only.</i>
	 * 
	 * @param queryStatement
	 *            PQL and OQL query string. If OQL, i.e., begins with "select"
	 *            then the select projection must be identity key. For example,
	 *            "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e where e.value.value['PortfolioId']='port_a'"
	 * @param validAtTime
	 *            The time at which the value is valid. -1 for current time.
	 * @param fromWrittenTime
	 *            start of the written time range. -1 for current time
	 * @param toWrittenTime
	 *            end of the written time range. -1 for current time.
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 */
	IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex);

	/**
	 * Returns the latest temporal key that satisfies the specified identity
	 * key. It returns null if the key is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	ITemporalKey<K> getKey(K identityKey);

	/**
	 * Returns the temporal key that satisfies the specified valid-at time. It
	 * returns null if the key is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	ITemporalKey<K> getKey(K identityKey, long validAtTime);

	/**
	 * Returns the temporal key that satisfies the specified valid-at and as-of
	 * times. It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	ITemporalKey<K> getKey(K identityKey, long validAtTime, long asOfTime);

	/**
	 * Returns the temporal keys that satisfy the specified valid-at time. Note
	 * that this method does not take the identity key. It returns null if the
	 * entry is not found.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	Set<ITemporalKey<K>> getKeySet(long validAtTime);

	/**
	 * Returns the temporal keys that satisfy the specified valid-at and as-of
	 * times. Note that this method does not take the identity key. It returns
	 * null if the entry is not found.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	Set<ITemporalKey<K>> getKeySet(long validAtTime, long asOfTime);

	/**
	 * Returns all temporal values across the grids that satisfy the specified
	 * valid-at time. Note that this method does not take the identity key. It
	 * is executed on all distributed servers of each grid. It returns null if
	 * there no values that satisfy the passed-in valid-at time.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	public Set<V> get(long validAtTime);

	/**
	 * Returns all temporal values across the grids that satisfy the specified
	 * valid-at and as-of times. Note that this method does not take the
	 * identity key. It is executed on all distributed servers of each grid. It
	 * returns null if there no values that satisfy the passed-in time values.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	Set<V> get(long validAtTime, long asOfTime);

	/**
	 * Returns all temporal entries of the specified identity key as of now. It
	 * returns a chronologically ordered set providing a history of changes that
	 * fall in the valid-at time. The entries are ordered by start-valid and
	 * written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey);

	/**
	 * Returns all temporal entries of the specified identity key that satisfy
	 * the specified valid-at time. It returns a chronologically ordered set
	 * providing a history of changes that fall in the valid-at time. The
	 * entries are ordered by start-valid and written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime);

	/**
	 * Returns all temporal entries of the specified identity key that satisfy
	 * the specified valid-at and as-of times. It returns a chronologically
	 * ordered set providing a history of changes that fall in the valid-at and
	 * as-of times. The entries are ordered by start-valid and written times.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime, long asOfTime);

	/**
	 * Returns all last temporal entries including expired and deleted entries.
	 * It returns null if the specified grid path is not defined. Prior to
	 * invoking this method, the grid path must be set by invoking
	 * {@link #setGridPath(String)}. <i>For pure clients only.</i>
	 * 
	 * @param orderBy
	 *            Order-by field name. If null, then no order takes place.
	 * @param orderAscending
	 *            true for ascending, false for descending result set.
	 * @param batchSize
	 *            Fetch batch size.
	 * @param forceRebuildIndex
	 *            true to force the servers to rebuild the result set index,
	 *            false to use the cached result set if exists.
	 * @throws PadoException
	 *             Thrown if this method is invoked by a server. Only pure
	 *             clients are supported.
	 */
	IScrollableResultSet<TemporalEntry<K, V>> getAllLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex);

	/**
	 * Resets all TemporalBiz specifics and biz context attributes. This method
	 * invokes the methods under the "See Also:" tag to revert to the original
	 * state defined by the application.
	 * 
	 * @see #setReference(boolean)
	 * @see #setDepth(int)
	 * @see #setPutExceptionEnabled(boolean)
	 * @see IGridContextClient#reset()
	 * 
	 */
	void reset();

	/**
	 * Puts temporal data along with attachment identity keys if defined in the
	 * value object into Pado using the current system time as the written time.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> put(K identityKey, V value);

	/**
	 * Puts temporal data along with attachment identity keys if defined in the
	 * value object into Pado using the current system time as the written time.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> put(K identityKey, V value, boolean isDelta);

	/**
	 * Puts temporal data along with attachment identity keys if defined in the
	 * value object into Pado using the current system time as the written time.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param startValidTime
	 *            The start valid-time.
	 * @param endValidTime
	 *            The end valid-time.
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, boolean isDelta);

	/**
	 * Puts temporal data along with attachment identity keys if defined in the
	 * value object into Pado.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param startValidTime
	 *            The start valid-time.
	 * @param endValidTime
	 *            The end valid-time.
	 * @param writtenTime
	 *            The written time (transaction time)
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, long writtenTime,
			boolean isDelta);

	/**
	 * Puts temporal data along with attachment identity keys into Pado using
	 * the current system time as the written time.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param attachmentIdentityKeySets
	 *            Array of attachment identity key sets. null is treated as no
	 *            attachments.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * Puts temporal attachment identity keys into Pado using the current system
	 * time as the written time. The value is set to null.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param attachmentIdentityKeySets
	 *            Array of attachment identity key sets. null is treated as no
	 *            attachments.
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			boolean isDelta);

	/**
	 * Puts temporal data into Pado.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param value
	 *            The value to put into Pado.
	 * @param startValidTime
	 *            The start valid-time.
	 * @param endValidTime
	 *            The end valid-time.
	 * @param writtenTime
	 *            The written time (transaction time)
	 * @param isDelta
	 *            If true, then it stores only the changes made in the value
	 *            object. Otherwise, the entire value object is stored as a
	 *            whole.
	 * 
	 * @return Returns the corresponding temporal entry in Pado. Returns null if
	 *         {@link #isPutExceptionEnabled()} is false. and the put operation
	 *         fails. Put could fail if the specified key already exists.
	 * 
	 * @throws TemporalException
	 *             Thrown if the specified key already exists. To bypass the
	 *             exception, use {@link #setPutExceptionEnabled(boolean)} to
	 *             disable put exception or set the system property
	 *             "temporal.put.exception.enabled" to false. If false, the
	 *             duplicate key is silently ignored. The default is true.
	 * @see #setPutExceptionEnabled(boolean)
	 * @see #isPutExceptionEnabled()
	 */
	TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, long writtenTime, boolean isDelta);

	/**
	 * Removes the specified identity key from the temporal list. Note that the
	 * entity is never actually removed from the data grid. Removed entities
	 * cannot be searched by valid-at and as-of times.
	 * 
	 * @param identityKey
	 *            The identity key.
	 */
	void remove(K identityKey);

	/**
	 * Returns true if the specified identity key has been removed or does not
	 * exist. Note that temporal data is never physically removed.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	boolean isRemoved(K identityKey);

	/**
	 * Returns true if the specified identity key exists.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	boolean isExist(K identityKey);

	/**
	 * Dumps the temporal list of the specified identity key to the console in
	 * raw time format (msec).
	 * 
	 * @param identityKey
	 *            The identity key.
	 */
	void dump(K identityKey);

	/**
	 * Dumps the temporal list of the specified identity key to the specified
	 * output stream in specified date format.
	 * 
	 * @param identityKey
	 *            The identity key.
	 * @param printStream
	 *            Output stream. If null, then it prints to the console.
	 * @param formatter
	 *            Date formatter. If null, it prints in raw time (msec).
	 */
	void dump(K identityKey, PrintStream printStream, SimpleDateFormat formatter);

	/**
	 * Returns the latest value and attachments as of now mapped by the
	 * specified identity key. It returns null if the values are not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * 
	 */
	AttachmentResults<V> getAttachments(K identityKey);

	/**
	 * Returns the value and attachments that satisfy the specified valid-at
	 * time. It returns null if the values are not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	AttachmentResults<V> getAttachments(K identityKey, long validAtTime);

	/**
	 * Returns the value and attachments that satisfy the specified valid-at and
	 * as-of times. It returns null if the values are not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	AttachmentResults<V> getAttachments(K identityKey, long validAtTime, long asOfTime);

	/**
	 * Returns the temporal entry of the specified identity key and valid-at
	 * along with all attachments in the form of {@linkplain TemporalEntry} that
	 * are valid as of now. It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 */
	AttachmentResults<V> getAttachmentsEntries(K identityKey);

	/**
	 * Returns the temporal entry of the specified identity key and valid-at
	 * along with all attachments in the form of {@linkplain TemporalEntry} as
	 * of now. It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 */
	AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime);

	/**
	 * Returns the temporal entry of the specified identity key, valid-at and
	 * as-of along with all attachments in the form of
	 * {@linkplain TemporalEntry} It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            The identity key that identifies the value in search.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 */
	AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * @return Returns the attachment values that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	List<V> __getAttachments(long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment values as of now mapped by the identity
	 * keys in the specified attachment set. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * 
	 */
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * Returns the attachment values that satisfy the specified valid-at time.
	 * Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime);

	/**
	 * Returns the attachment values that satisfy the specified valid-at and
	 * as-of times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment value lists as of now in array mapped by
	 * the identity keys in the specified attachment set. Returns null if not
	 * found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * 
	 */
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * Returns the attachment value lists in array that satisfy the specified
	 * valid-at time. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime);

	/**
	 * Returns the attachment value lists in array that satisfy the specified
	 * valid-at and as-of times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * @return Returns the attachment entries that satisfy the specified
	 *         valid-at and as-of times. Returns null if not found.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> __getAttachmentsEntries(long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment entries that satisfy the specified identity
	 * key. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * Returns the attachment entries that satisfy the specified valid-at time.
	 * Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime);

	/**
	 * Returns the attachment entries that satisfy the specified valid-at and
	 * as-of times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment entries that satisfy the specified identity
	 * key. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * Returns the attachment entries that satisfy the specified valid-at time.
	 * Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime);

	/**
	 * Returns the attachment entries that satisfy the specified valid-at and
	 * as-of times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	@WithGridCollector(gridCollectorClass = "com.netcrest.pado.biz.collector.MapCollector")
	Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * @return Returns the attachment keys that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	Set<ITemporalKey<K>> __getAttachmentsKeys(long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment keys that satisfy the specified identity
	 * key. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * 
	 */
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * Returns attachments by invoking a single server. This method is strictly
	 * reserved for virtual path executions.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @return Returns the attachment values that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	Map<String, List<V>> __getAttachmentsOnServer(long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * Returns attachments by invoking all servers in all participating grids.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @return Returns the attachment values that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	Map<String, List<V>> __getAttachmentsBroadcast(long validAtTime, long asOfTime);

	/**
	 * <b>This is a private method that must not be invoked by applications.</b>
	 * Returns attachment entries by invoking all servers in all participating
	 * grids.
	 * 
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times. If -1, then
	 *            it returns the now-relative (as-of-now) value.
	 * @return Returns the attachment values that satisfy the specified valid-at
	 *         and as-of times. Returns null if not found.
	 * 
	 */
	Map<String, List<TemporalEntry<K, V>>> __getAttachmentsEntriesBroadcast(long validAtTime, long asOfTime);

	/**
	 * Returns the attachment keys that satisfy the specified valid-at time.
	 * Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime);

	/**
	 * Returns the attachment keys that satisfy the specified valid-at and as-of
	 * times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySet
	 *            The attachment identity key set.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime);

	/**
	 * Returns the latest attachment keys that satisfy the specified identity
	 * key. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * 
	 */
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets);

	/**
	 * Returns the attachment keys that satisfy the specified valid-at time.
	 * Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * 
	 */
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime);

	/**
	 * Returns the attachment keys that satisfy the specified valid-at and as-of
	 * times. Returns null if not found.
	 * 
	 * @param attachmentIdentityKeySets
	 *            The attachment identity key sets.
	 * @param validAtTime
	 *            The time at which the value is valid.
	 * @param asOfTime
	 *            The as-of time compared against the written times.
	 * 
	 */
	Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime,
			long asOfTime);
}
