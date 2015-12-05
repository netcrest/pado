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
package com.netcrest.pado.index.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.Delta;
import com.gemstone.gemfire.InvalidDeltaException;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.ResultItem;

public class IndexMatrix implements DataSerializable, Delta
{
	private static final long serialVersionUID = 1L;
	private List<int[]> startIndexList;
	private List<int[]> batchList;
	private transient int[] deltaStartIndexes;
	private int[] bucketIds;
	// pageSize is always set with GridQuery.getAggregationPageSize() during
	// query
	private int pageSize = 1000;
	private int currentSize;
	private int totalSize;
	private boolean isComplete;
	private boolean hasDelta = false;
	private boolean isInProgress = true;

	public IndexMatrix()
	{
	}

	public IndexMatrix(int[] bucketIds, int totalSize)
	{
		this.bucketIds = bucketIds;
		this.totalSize = totalSize;
	}

	private List<int[]> getBatchList()
	{
		if (batchList == null) {
			batchList = new ArrayList<int[]>(2);
		}
		return batchList;
	}

	private int[] getDeltaStartIndexes()
	{
		return deltaStartIndexes;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	/**
	 * Returns the page index given the item index
	 * 
	 * @param itemIndex
	 *            Item or entry index of the entire result list
	 */
	public int getPageIndex(int itemIndex)
	{
		return (int) (itemIndex / pageSize);
	}

	/**
	 * Returns the start index of the entire result list for the specified item
	 * index. Note that this method merely calculates the start index based on
	 * the page size. The returned start index does not reflect an index of the
	 * actual result list.
	 * 
	 * @param itemIndex
	 *            Item or entry index of the entire result list.
	 */
	public int getStartIndex(int itemIndex)
	{
		return getPageIndex(itemIndex) * pageSize;
	}

	/**
	 * Returns the end index of the entire result list for the specified item
	 * index. Note that this method merely calculates the end index based on the
	 * page size. The returned start index does not reflect an index of the
	 * actual result list.
	 * 
	 * @param itemIndex
	 *            Item or entry index of the entire result list.
	 */
	public int getEndIndex(int itemIndex)
	{
		return getStartIndex(itemIndex) + pageSize - 1;
	}

	public int[] getBucketIds()
	{
		return bucketIds;
	}

	public List<int[]> getIndexList()
	{
		if (startIndexList == null) {
			startIndexList = new ArrayList<int[]>(10);
		}
		return startIndexList;
	}

	public int[] getStartIndexes(int pageIndex)
	{
		int[] startIndexes = null;
		List<int[]> startIndexList = getIndexList();
		if (0 <= pageIndex && pageIndex < startIndexList.size()) {
			startIndexes = startIndexList.get(pageIndex);
		}
		return startIndexes;
	}

	public boolean isInProgress()
	{
		return isInProgress;
	}

	public boolean isComplete()
	{
		if (isComplete == false) {
			return currentSize == getTotalSize();
		}
		return isComplete;
	}

	public void setComplete(boolean isComplete)
	{
		this.isComplete = isComplete;
		this.isInProgress = !isComplete;
	}

	public int getCurrentSize()
	{
		return currentSize;
	}

	public int getTotalSize()
	{
		return totalSize;
	}

	public void begin()
	{
		// System.out.println("IndexMatrix.begin(): deltaList.size()=" +
		// getDeltaList().size() + ", indexList.size()=" +
		// getIndexList().size());
		getBatchList().clear();
		deltaStartIndexes = null;
	}

	// public void add(int[] indexInfo)
	// {
	// getBatchList().add(indexInfo);
	// getDeltaList().add(indexInfo);
	// }

	// public void addAll(List<IndexInfo> indexInfos)
	// {
	// getBatchList().addAll(indexInfos);
	// getDeltaList().addAll(indexInfos);
	// }

	public void addStartIndexes(int[] startIndexes)
	{
		List<int[]> startIndexList = getIndexList();
		startIndexList.add(startIndexes);
		currentSize = startIndexList.size() * pageSize;
		if (currentSize > totalSize) {
			currentSize = totalSize;
		}
	}

	public void commit(boolean isComplete, final IGridResults<ResultItem<Object>> gridResults)
	{
		if (deltaStartIndexes != null) {
			hasDelta = true;
		}
		if (batchList != null) {
			List<int[]> indexList = getIndexList();
			indexList.addAll(batchList);
			batchList.clear();
		}

		setComplete(isComplete);

		// If complete, then wrap up the start indexes. Note that there
		// may be multiple sets of remaining startIndexes due to a server
		// that may have more than 1 page of remaining results.
		if (isComplete) {
			currentSize = totalSize;
			List<ResultItem<Object>> list = gridResults.getAggregatedSortedResults();
			// <bucketId, startIndex>
			HashMap<Integer, Integer> map = new HashMap(10);
			int bucketCount = this.bucketIds.length;
			int count = 0;
			for (ResultItem<Object> resultItem : list) {
				count++;
				int bucketId = resultItem.getBucketId();
				if (map.containsKey(bucketId) == false) {
					map.put(bucketId, resultItem.getResultIndex());
				}
				if (count == pageSize) {
					int[] startIndexes = createStartIndexes(map);
					addStartIndexes(startIndexes);
					map.clear();
					count = 0;
				}
			}

			if (count > 0) {
				int[] startIndexes = createStartIndexes(map);
				addStartIndexes(startIndexes);
				map.clear();
			}
		}
		
		// TODO: introduce clear() to remove the results. for gc.
		((GridResults)gridResults).setAggregatedSortedResults(null);
	}

	private int[] createStartIndexes(HashMap<Integer, Integer> map)
	{
		int bucketCount = bucketIds.length;
		int[] startIndexes = new int[bucketCount];
		for (int i = 0; i < bucketCount; i++) {
			Integer startIndex = map.get(this.bucketIds[i]);
			if (startIndex == null) {
				startIndexes[i] = -1;
			} else {
				startIndexes[i] = startIndex;
			}
		}
		return startIndexes;
	}

	/**
	 * Returns the bucket Id at the specified position in the index list. It
	 * returns -1 if the specified position is out of range.
	 * 
	 * @param position
	 *            The index position.
	 */
	// public int getBucketId(int position)
	// {
	// if (position < 0 || position >= indexList.size()) {
	// return -1;
	// }
	// return indexList.get(position).getBucketId();
	// }

	public void dump()
	{
		// Header: Index bucket_ids...
		System.out.print("Index ");
		for (int bucketId : bucketIds) {
			System.out.print(bucketId + "  ");
		}
		System.out.println();
		// Rows: start indexes
		for (int i = 0; i < startIndexList.size(); i++) {
			int[] indexes = startIndexList.get(9);
			System.out.print(i + 1 + ". ");
			for (int j = 0; i < indexes.length; i++) {
				System.out.print(indexes[j] + "  ");
			}
			System.out.println();
		}
		System.out.println("  Current count: " + currentSize);
		System.out.println("    Total count: " + totalSize);
		System.out.println();
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		pageSize = DataSerializer.readPrimitiveInt(input);
		currentSize = DataSerializer.readPrimitiveInt(input);
		totalSize = DataSerializer.readPrimitiveInt(input);
		isComplete = DataSerializer.readPrimitiveBoolean(input);
		isInProgress = DataSerializer.readPrimitiveBoolean(input);
		startIndexList = DataSerializer.readObject(input);
		bucketIds = DataSerializer.readIntArray(input);
	}

	/**
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 * 
	 * @gfcodegen This code is generated by gfcodegen.
	 */
	public void toData(DataOutput output) throws IOException
	{
		DataSerializer.writePrimitiveInt(pageSize, output);
		DataSerializer.writePrimitiveInt(currentSize, output);
		DataSerializer.writePrimitiveInt(totalSize, output);
		DataSerializer.writePrimitiveBoolean(isComplete, output);
		DataSerializer.writePrimitiveBoolean(isInProgress, output);
		DataSerializer.writeObject(startIndexList, output);
		DataSerializer.writeIntArray(bucketIds, output);

	}

	public boolean hasDelta()
	{
		return hasDelta;
	}

	public synchronized void toDelta(DataOutput out) throws IOException
	{
		DataSerializer.writePrimitiveBoolean(isComplete, out);
		DataSerializer.writeIntArray(deltaStartIndexes, out);
		deltaStartIndexes = null;
	}

	public synchronized void fromDelta(DataInput in) throws IOException, InvalidDeltaException
	{
		isComplete = DataSerializer.readPrimitiveBoolean(in);
		int nextStartIndex = DataSerializer.readPrimitiveInt(in);
		int[] startIndexes = DataSerializer.readIntArray(in);
		addStartIndexes(startIndexes);
	}
}
