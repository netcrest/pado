package com.netcrest.pado.internal.pql.antlr4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netcrest.pado.index.exception.GridQueryResultSetExpiredException;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.log.Logger;

public class LocalSrollableResultSet<T> implements IScrollableResultSet<T>
{
	private static final long serialVersionUID = 1L;

	private String id;
	private List<T> valueResultList;
	private int fetchSize = 100;

	private transient int currentIndex = -1;

	// startIndex & endIndex are the current indexes of the pagination
	// result set.
	private int startIndex = -1;
	private int endIndex = -1;

	// prevStartIndex & prevEndIndex are set when the current cursor
	// reaches the beginning or the end of the whole result set.
	private transient int prevStartIndex = startIndex;
	private transient int prevEndIndex = endIndex;

	private GridQuery query;

	public LocalSrollableResultSet(GridQuery query, List<T> results)
	{
		this.query = query;
		this.valueResultList = results;
		if (valueResultList == null || valueResultList.size() == 0) {
			return;
		}
		goToSet(0);
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	@Override
	public boolean nextSet() throws GridQueryResultSetExpiredException
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
	public boolean previousSet() throws GridQueryResultSetExpiredException
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
			index = currentIndex - getFetchSize();
			if (currentIndex > 0 && index < 0) {
				index = 0;
			}
		}
		return goToSet(index);
	}

	@Override
	public boolean goToSet(int index) throws GridQueryResultSetExpiredException
	{
		if (index < 0) {
			prevStartIndex = startIndex;
			prevEndIndex = endIndex;
			startIndex = endIndex = -1;
			currentIndex = -1;
			return false;
		} else if (index >= getTotalSize()) {
			prevStartIndex = startIndex;
			prevEndIndex = endIndex;
			startIndex = endIndex = -1;
			currentIndex = -1;
			return false;
		}

		currentIndex = index;
		startIndex = currentIndex;
		int nextStartIndex = currentIndex + fetchSize;
		if (nextStartIndex < getTotalSize()) {
			endIndex = startIndex + fetchSize - 1;
		} else {
			endIndex = getTotalSize() - 1;
		}
		return true;
	}

	@Override
	public void setFetchSize(int fetchSize)
	{
		this.fetchSize = fetchSize;
	}

	@Override
	public int getFetchSize()
	{
		return fetchSize;
	}

	@Override
	public int getTotalSize()
	{
		return valueResultList.size();
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

	@Override
	public int getSetNumber()
	{
		int div = (getStartIndex() + 1) / getFetchSize();
		return div + 1;
	}

	@Override
	public int getSetSize()
	{
		if (startIndex == -1 || endIndex == -1) {
			return 0;
		}
		return endIndex - startIndex + 1;
	}

	@Override
	public int getStartIndex()
	{
		return startIndex;
	}

	@Override
	public int getEndIndex()
	{
		return endIndex;
	}

	@Override
	public int getViewStartIndex()
	{
		if (startIndex == -1) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public int getViewEndIndex()
	{
		if (endIndex == -1) {
			return -1;
		} else {
			return endIndex - startIndex;
		}
	}

	@Override
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	@Override
	public GridQuery getQuery()
	{
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> toList()
	{
		if (startIndex == -1) {
			return Collections.EMPTY_LIST;
		} else {
			ArrayList<T> list = new ArrayList<T>(fetchSize);
			for (int i = startIndex; i <= endIndex; i++) {
				list.add(valueResultList.get(i));
			}
			return list;
		}
	}

	@Override
	public List<T> toDomainList()
	{
		throw new UnsupportedOperationException("LocalScrollableResultSet.toDomainList() is not currently supported.");
	}

	@Override
	public List<T> toValueList()
	{
		throw new UnsupportedOperationException("LocalScrollableResultSet.toValueList() is not currently supported.");
	}

	@Override
	public void dump()
	{
		Logger.info("Total : " + valueResultList.size());
		for (Object value : valueResultList) {
			Logger.info(value.toString());
		}
	}

	@Override
	public void close()
	{
	}

	@Override
	public boolean isClosed()
	{
		// TODO: introduce proper closure 
		return false;
	}
}
