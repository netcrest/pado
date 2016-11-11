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
package com.netcrest.pado.index.gemfire.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;
import com.netcrest.pado.index.gemfire.service.GridQueryService;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrix;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.provider.IIndexMatrixProvider;
import com.netcrest.pado.index.provider.IndexMatrixProviderFactory;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.GridQueryFactory;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;

/**
 * ClientResults implements IResultSet allows user to scroll to any place in the
 * results
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BizScrollableResultSetImpl<T> implements Serializable, IScrollableResultSet<T>
{
	private static final long serialVersionUID = 7432723057L;
	private final GridQuery query;
	private final IIndexMatrixProvider provider;
	private final String id;
	private List<Object> valueResultList;
	private BizGridQueryServiceImpl serviceImpl;
	private int totalSize = -1;
	private int fetchSize = 100;

	// startIndex & endIndex are the current indexes of the pagination
	// result set.
	private int startIndex = -1;
	private int endIndex = -1;

	// prevStartIndex & prevEndIndex are set when the current cursor
	// reaches the beginning or the end of the whole result set.
	private transient int prevStartIndex = startIndex;
	private transient int prevEndIndex = endIndex;

	// viewStartIndex and viewEndIndex are the current indexes of the partial
	// result set. A partial result set is the paginated result set in
	// the local VM.
	private transient int viewStartIndex = 0;
	private transient int viewEndIndex = -1;

	// startPage & endPageIndex are the current indexes of the pages
	// retrieved from the grid.
	private int startPageIndex = -1;
	private int endPageIndex = -1;

	private final boolean throwExceptionOnExpire;

	public BizScrollableResultSetImpl(GridQuery query, BizGridQueryServiceImpl serviceImpl)
	{
		this.query = query;
		this.id = query.getId();
		this.fetchSize = query.getFetchSize();
		provider = IndexMatrixProviderFactory.getInstance().getProviderInstance(query.getProviderKey());
		throwExceptionOnExpire = query.isThrowExceptionOnExpire();
		setGridQueryService(serviceImpl);
	}

	public Object getId()
	{
		return id;
	}

	public List getValueResultList()
	{
		return valueResultList;
	}

	public void setValueResultList(List valueResultList)
	{
		this.valueResultList = valueResultList;
	}

	public int getFetchSize()
	{
		return fetchSize;
	}

	public void setFetchSize(int fetchSize)
	{
		this.fetchSize = fetchSize;
	}

	public int getCurrentIndex()
	{
		if (viewStartIndex == -1) {
			return -1;
		}
		return viewStartIndex + startIndex;
	}

	public int getViewStartIndex()
	{
		return viewStartIndex;
	}

	public int getViewEndIndex()
	{
		return viewEndIndex;
	}

	@Override
	public int getStartIndex()
	{
		return startIndex;
	}

	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	public int getEndIndex()
	{
		return endIndex;
	}

	public void setEndIndex(int endIndex)
	{
		this.endIndex = endIndex;
	}

	public void setGridQueryService(BizGridQueryServiceImpl serviceImpl)
	{
		// TODO: Used by client accessing Pado. Other grids required their own
		// region services. Note that there is only 1 region service instance
		// for all Pado logins. Need to fix this.
		this.serviceImpl = serviceImpl;
		if (serviceImpl != null) {
			provider.setRegionService(serviceImpl.getRegionService());
		}
	}

	public void commit()
	{
		if (valueResultList != null) {
			endIndex = valueResultList.size() - 1;
			if (endIndex >= 0) {
				startIndex = 0;
			}
		}
	}

	public void clear()
	{
		if (valueResultList != null) {
			valueResultList.clear();
		}
		viewStartIndex = 0;
		viewEndIndex = -1;
	}

	private void fetchValues(IndexMatrix indexMatrix, int nextItemStartIndex, int nextItemEndIndex)
	{
		int nextStartPageIndex = indexMatrix.getPageIndex(nextItemStartIndex);
		int nextEndPageIndex = indexMatrix.getPageIndex(nextItemEndIndex);

		// Get the actual start and end indexes of the data to retrieve
		int nextStartIndex = indexMatrix.getStartIndex(nextItemStartIndex);
		int nextEndIndex = indexMatrix.getEndIndex(nextItemEndIndex);

		// If the next start page range is within the current page range than
		// we already have the results.
		if (this.startPageIndex <= nextStartPageIndex && nextStartPageIndex <= this.endPageIndex
				&& this.startPageIndex <= nextEndPageIndex && nextEndPageIndex <= this.endPageIndex) {
			startIndex = nextStartIndex;
			endIndex = nextEndIndex;
			this.viewStartIndex = nextItemStartIndex - startIndex;
			return;
		}

		clear();

		GridQuery newQuery = getCriteria(nextStartIndex, nextEndIndex, nextStartPageIndex, nextEndPageIndex);
		IGridResults<Object> gridResults = provider.retrieveEntities(newQuery, indexMatrix);
		valueResultList = gridResults.getAggregatedSortedResults();
		setTotalSize(indexMatrix.getTotalSize());

		this.startIndex = nextStartIndex;
		if (nextEndIndex >= indexMatrix.getTotalSize()) {
			this.endIndex = indexMatrix.getTotalSize();
		} else {
			this.endIndex = nextEndIndex;
		}
		this.startPageIndex = nextStartPageIndex;
		this.endPageIndex = nextEndPageIndex;
		this.viewStartIndex = nextItemStartIndex - startIndex;
		if (valueResultList.size() < fetchSize) {
			this.viewEndIndex = valueResultList.size() - 1;
		} else {
			this.viewEndIndex = viewStartIndex + fetchSize - 1;
		}
	}

	private GridQuery getCriteria(int nextStartIndex, int nextEndIndex, int nextStartPageIndex, int nextEndPageIndex)
	{
		GridQuery newQuery = GridQueryFactory.createGridQuery();
		query.copyTo(newQuery);
		newQuery.setFetchSize(nextEndIndex - nextStartIndex + 1);
		newQuery.setStartIndex(nextStartIndex);
		newQuery.setPageRange(nextStartPageIndex, nextEndPageIndex);
		return newQuery;
	}

	/**
	 * After the first batch of result is streamed to the client, the client may
	 * do nextSet or goToSet which may require the indexMatrix build.
	 * 
	 * @param endIndex
	 *            The end index of the targetSet.
	 *            <p>
	 *            If indexMatrix build is not complete and the endIndex >
	 *            indexMatrix's current total size, it will keep waiting till
	 *            indexMatrix build is complete or the target data set is
	 *            available
	 *            </p>
	 * @param timeout
	 *            Timeout to wait in milliseconds, if -1, it will wait forever
	 * 
	 * @return <code>true</code> Ready to process IndexMatrix;
	 *         <code>false</code> IndexMatrix is not available;
	 */
	private synchronized boolean waitForIndexMatrixToBuild(int endIndex, long timeOut)
			throws GridQueryResultSetExpiredException
	{
		long waitTill = -1;
		if (timeOut > 0) {
			waitTill = System.currentTimeMillis() + timeOut;
		}
		IndexMatrix indexMatrix = getIndexMatrix(query);
		if (indexMatrix == null) {
			// check the query is being processed or not
			if (serviceImpl != null && serviceImpl.isIndexBuildingInProgress(query.getId())) {
				while (indexMatrix == null && (waitTill < 0 || System.currentTimeMillis() < waitTill)) {
					indexMatrix = getIndexMatrix(query);
					if (indexMatrix != null) {
						break;
					}
					try {
						Thread.sleep(100);
						indexMatrix = getIndexMatrix(query);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (indexMatrix == null) {
					getLogger().warning("IndexMatrix for :" + id + " can not be built");
					return false;
				}
			} else {
				// no query is being processed
				throw new GridQueryResultSetExpiredException("The result set for :" + query.getQueryString()
						+ " is expired");
			}
		}

		// TODO: race condition exists...
		if (indexMatrix == null) {
			// possibly evicted
			return false;
		}

		// Wait till indexMatrix to be built to reach the data set desired.
		while (endIndex > indexMatrix.getCurrentSize() && !indexMatrix.isComplete()
				&& (waitTill < 0 || System.currentTimeMillis() < waitTill)) {
			try {
				Thread.sleep(100);
				indexMatrix = getIndexMatrix(query);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// IndexMatrix is complete or data is ready
		if (indexMatrix.isComplete() || endIndex < indexMatrix.getIndexList().size()) {
			return true;
		} else {
			if (!indexMatrix.isComplete()) {
				getLogger().warning(
						"IndexMatrix for :" + id + " incomplete: " + indexMatrix.isComplete()
								+ ". The indexMatrix total size is : " + indexMatrix.getIndexList().size());
				return false;
			} else
				return true;
		}

	}

	@Override
	public synchronized boolean nextSet()
	{
		int index;
		int currentIndex = getCurrentIndex();
		if (currentIndex == -1) {
			// fetch the previously viewed batch which may have the size
			// that is less than the fetch size.
			index = prevStartIndex;
			startIndex = prevStartIndex;
			endIndex = prevEndIndex;
		} else {
			index = currentIndex + getFetchSize();
		}
		return goToSet(index);
	}

	@Override
	public synchronized boolean previousSet()
	{
		int index;
		int viewEndIndexIfBeginningReached = -1;
		int currentIndex = getCurrentIndex();
		if (currentIndex == -1) {
			// fetch the previously viewed batch which may have the size
			// that is less than the fetch size.
			index = prevStartIndex;
			startIndex = prevStartIndex;
			endIndex = prevEndIndex;
		} else {
			index = currentIndex - getFetchSize();
			if (currentIndex > 0 && index < 0) {
				index = 0;
				viewEndIndexIfBeginningReached = currentIndex - 1;
			}
		}
		boolean canScroll = goToSet(index);
		if (viewEndIndexIfBeginningReached != -1) {
			viewEndIndex = viewEndIndexIfBeginningReached;
		}
		return canScroll;
	}

	@Override
	public boolean goToSet(int index)
	{
		if (index < 0) {
			prevStartIndex = startIndex;
			prevEndIndex = endIndex;
			startIndex = endIndex = -1;
			viewStartIndex = 0;
			viewEndIndex = -1;
			return false;
		} else if (totalSize != -1 && index >= totalSize) {
			prevStartIndex = startIndex;
			prevEndIndex = endIndex;
			startIndex = endIndex = -1;
			viewStartIndex = viewEndIndex = -1;
			return false;
		}
		boolean canScroll = true;
		if (index < 0) {
			index = 0;
		}
		if (startIndex <= index && index <= endIndex) {
			if (index >= totalSize) {
				viewStartIndex = viewEndIndex = totalSize;
				return false;
			}
			int newNextEndIndex = index + fetchSize - 1;
			if (newNextEndIndex <= endIndex) {
				viewStartIndex = index - startIndex;
				viewEndIndex = viewStartIndex + fetchSize - 1;
				return true;
			} else if (newNextEndIndex >= totalSize) {
				viewStartIndex = index - startIndex;
				viewEndIndex = totalSize - 1;
				return true;
			}
		}

		int nextStartIndex = index;
		int nextEndIndex = nextStartIndex + fetchSize - 1;

		try {
			// waitForIndexMatrixToBuild(nextEndIndex, -1);
			waitForIndexMatrixToBuild(nextEndIndex, 10000 /* time out */);

			IndexMatrix indexMatrix = getIndexMatrix(query);

			// TODO: race condition exists...
			if (indexMatrix == null) {
				// possibly evicted
				return false;
			}
			// No next set
			if (nextStartIndex >= indexMatrix.getCurrentSize()) {
				clear();
				return false;
			}
			fetchValues(indexMatrix, nextStartIndex, nextEndIndex);
			return canScroll;
		} catch (GridQueryResultSetExpiredException expiredEx) {
			if (throwExceptionOnExpire) {
				throw expiredEx;
			} else {
				// Retry the query
				BizScrollableResultSetImpl newResultSet = (BizScrollableResultSetImpl) GridQueryService.getGridQueryService().query(query);
				boolean result = newResultSet.goToSet(index);
				newResultSet.copyTo(this);
				return result;
			}
		}
	}

	@Override
	public int getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(int total)
	{
		totalSize = total;
	}

	@Override
	public int getSetCount()
	{
		int count = getTotalSize() / getFetchSize();
		if (getTotalSize() % getFetchSize() > 0) {
			count++;
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSetNumber()
	{
		int div = (getStartIndex() + getViewStartIndex() + 1) / getFetchSize();
		return div + 1;
	}

	@Override
	public int getSetSize()
	{
		return viewEndIndex - viewStartIndex + 1;
	}

	public boolean isIndexMatrixComplete()
	{
		IndexMatrix indexMatrix = getIndexMatrix(query);
		if (indexMatrix == null) {
			return false;
		}
		return indexMatrix.isComplete();
	}

	@Override
	public List<T> toList()
	{
		if (startIndex == -1) {
			return Collections.EMPTY_LIST;
		} else {
			if (viewEndIndex == -1) {
				viewEndIndex = fetchSize - 1;
			}
			if (viewEndIndex >= valueResultList.size()) {
				viewEndIndex = valueResultList.size() - 1;
			}

			if (viewEndIndex < viewStartIndex) {
				return Collections.EMPTY_LIST;
			}
			// int startIndex = this.getReferenceIndex(this.startIndex);
			// int endIndex = this.getReferenceIndex(this.endIndex);

			List newList = new ArrayList(viewEndIndex - viewStartIndex + 1);
			// endIndex = startIndex + fetchSize;

			for (int i = viewStartIndex; i <= viewEndIndex; i++) {
				newList.add(valueResultList.get(i));
			}
			return newList;
		}
	}

	private Object createDomainObject(Class domainObjectClass, Object obj)
	{
		Object domain = obj;
		if (domainObjectClass != null && obj instanceof JsonLite) {
			try {
				IJsonLiteWrapper wrapper = (IJsonLiteWrapper) domainObjectClass.newInstance();
				wrapper.fromJsonLite((JsonLite) obj);
				domain = wrapper;
			} catch (Exception e) {
				// ignore
			}
		}
		return domain;
	}

	private List<T> toObjectList(boolean isDomainObject)
	{
		List list = toList();
		Object obj = null;
		if (list.size() > 0) {
			obj = list.get(0);
		}
		if (obj == null) {
			return list;
		}
		Class domainObjectClass = null;
		if (isDomainObject) {
			Object value = obj;
			if (obj instanceof TemporalEntry) {
				value = ((TemporalEntry) obj).getValue();
			} else if (obj instanceof ITemporalData) {
				ITemporalData data = (ITemporalData) obj;
				if (data instanceof GemfireTemporalData) {
					value = ((GemfireTemporalData) data).getValue();
				}
			}
			if (value instanceof KeyMap) {
				KeyType keyType = ((KeyMap) value).getKeyType();
				if (keyType != null) {
					domainObjectClass = keyType.getDomainClass();
				}
			}
		}
		List domainObjectList = list;
		if (obj instanceof TemporalEntry) {
			domainObjectList = new ArrayList(list.size());
			for (Object object : list) {
				TemporalEntry te = (TemporalEntry) object;
				obj = te.getValue();
				if (isDomainObject) {
					obj = createDomainObject(domainObjectClass, obj);
				}
				domainObjectList.add(obj);
			}
		} else if (obj instanceof ITemporalData) {
			domainObjectList = new ArrayList(list.size());
			for (Object object : list) {
				ITemporalData data = (ITemporalData) object;
				if (data instanceof GemfireTemporalData) {
					obj = ((GemfireTemporalData) data).getValue();
				}
				if (isDomainObject) {
					obj = createDomainObject(domainObjectClass, obj);
				}
				domainObjectList.add(obj);
			}
		} else if (obj instanceof ITemporalKey) {
			domainObjectList = new ArrayList(list.size());
			for (Object object : list) {
				ITemporalKey tk = (ITemporalKey) object;
				obj = tk.getIdentityKey();
				domainObjectList.add(obj);
			}
		}
		return domainObjectList;
	}

	public List<T> toDomainList()
	{
		return toObjectList(true);
	}

	public List<T> toValueList()
	{
		return toObjectList(false);
	}

	@Override
	public void dump()
	{
		getLogger().info("Total : " + valueResultList.size());
		for (Object value : valueResultList) {
			getLogger().info(value.toString());
		}
	}

	protected synchronized IndexMatrix getIndexMatrix(GridQuery query)
	{
		Region<Object, IndexMatrix> indexRegion = CacheFactory.getAnyInstance().getRegion(
				IndexMatrixUtil.getProperty(Constants.PROP_REGION_INDEX));
		IndexMatrix indexMatrix = indexRegion.get(query.getId());
		return indexMatrix;
	}

	protected LogWriter getLogger()
	{

		return CacheFactory.getAnyInstance().getLogger();
	}

	public void close()
	{
		if (serviceImpl != null) {
			serviceImpl.close(id);
		}
	}
	
	public boolean isClosed()
	{
		if (serviceImpl == null) {
			return true;
		} else {
			return serviceImpl.isClosed(id);
		}
	}

	public GridQuery getQuery()
	{
		return query;
	}

	private void copyTo(BizScrollableResultSetImpl clientResults)
	{
		clientResults.valueResultList = this.valueResultList;
		clientResults.totalSize = this.totalSize;
		clientResults.fetchSize = this.fetchSize;
		clientResults.startIndex = this.startIndex;
		clientResults.endIndex = this.endIndex;
	}

}
