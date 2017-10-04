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
package com.netcrest.pado.temporal.gemfire.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalKey;
import com.netcrest.pado.temporal.TemporalListFactory;
import com.netcrest.pado.temporal.TemporalUtil;

/**
 * TemporalList contains a complete history of updates made via temporal
 * operations, namely, put, invalid, and remove. It is maintained by
 * TemporalCacheListener in real-time.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class GemfireTemporalList implements ITemporalList
{
	private static final long serialVersionUID = 1L;

	private static final int NOT_FOUND = -1;

	private String temporalName;
	protected Region<ITemporalKey, ITemporalData> region;
	protected Object identityKey;
	private TemporalListFactory temporalListFactory;
	private ITemporalKey removedKey;

	/**
	 * lastKey is the last key in the temporal list. It is important to note
	 * that the last key is not necessarily now-relative as the written time
	 * could be set to a future time from now. The following check must be
	 * performed on the last key to determine whether it is now-relative:
	 * <p>
	 * 
	 * <pre>
	 * startValidTime &lt;= currentTime &lt;= endValidTime &amp;&amp; currentTime &gt;= writtenTime
	 * </pre>
	 */
	protected volatile ITemporalKey lastKey;

	/**
	 * List of temporal keys in the temporal list
	 */
	protected volatile ArrayList<ITemporalKey> keyList;

	public GemfireTemporalList(String temporalName, Object identityKey, String fullPath)
	{
		this.temporalName = temporalName;
		this.identityKey = identityKey;
		this.region = CacheFactory.getAnyInstance().getRegion(fullPath);
		temporalListFactory = TemporalListFactory.getTemporalListFactory(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return temporalName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int add(ITemporalKey tkey)
	{
		synchronized (this) {

			// handle removed key or request
			if (removedKey != null) {
				return -1;
			} else {
				if (tkey.getStartValidTime() == -1 && tkey.getEndValidTime() == -1) {
					return remove(tkey);
				}
			}

			ArrayList<ITemporalKey> currentList = this.keyList;
			if (currentList == null) {
				ArrayList<ITemporalKey> tmp = new ArrayList<ITemporalKey>(1);
				tmp.add(tkey);
				this.keyList = tmp;
				this.lastKey = tkey;
				return tmp.size() - 1;
			}

			// never modify in place - obsolete due to synchronization
			// int curSize = currentList.size();
			// ArrayList<ITemporalKey> tmp = new ArrayList<ITemporalKey>(curSize
			// + 1);
			// tmp.addAll(currentList);
			ArrayList<ITemporalKey> tmp = keyList;
			int index = binarySearch_addKey(tmp, tkey.getStartValidTime(), tkey.getWrittenTime());

			if (index == NOT_FOUND) {

				index = 0;
				tmp.add(index, tkey);

			} else {
				ITemporalKey foundKey = tmp.get(index);

				// If the found key's written time is larger then the
				// new key must be added above the found key.
				if (foundKey.getStartValidTime() == tkey.getStartValidTime() || foundKey == removedKey) {
					if (foundKey.getWrittenTime() > tkey.getWrittenTime()) {
						index--;
						if (index < 0) {
							foundKey = null;
						} else {
							foundKey = tmp.get(index);
						}
					}
				}
				// If all of the times are same then return. Duplicates can
				// occur when class versions are changed (KeyMap) or simply user
				// inputs keys with the same times.
				if (foundKey != null && foundKey.getStartValidTime() == tkey.getStartValidTime()
						&& foundKey.getEndValidTime() == tkey.getEndValidTime()
						&& foundKey.getWrittenTime() == tkey.getWrittenTime()) {
					tmp.set(index, tkey);
				} else {
					index++;
					tmp.add(index, tkey);
				}

			}
			this.lastKey = tmp.get(tmp.size() - 1);
			this.keyList = tmp;

			// Update the previous and next end written times
			ITemporalKey prevKey = getTemporalKey(index - 1);
			if (prevKey != null) {
				((TemporalKey) prevKey).setEndWrittenTime(tkey.getWrittenTime());
			}
			ITemporalKey nextKey = getTemporalKey(index + 1);
			if (nextKey != null) {
				((TemporalKey) tkey).setEndWrittenTime(nextKey.getWrittenTime());
			} else {
				((TemporalKey) tkey).setEndWrittenTime(TemporalUtil.MAX_TIME);
			}

			return index;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(ITemporalKey tkey)
	{
		return keyList.contains(tkey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIndex(ITemporalKey tkey)
	{
		return keyList.indexOf(tkey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getEndWrittenTime(ITemporalKey tkey)
	{
		ArrayList<ITemporalKey> currentList = keyList;
		int index = currentList.indexOf(tkey);
		if (index == -1) {
			return -1;
		}
		index++;
		if (index >= currentList.size()) {
			return TemporalUtil.MAX_TIME;
		}
		ITemporalKey nextKey = currentList.get(index);
		return nextKey.getWrittenTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalKey getTemporalKey(int index)
	{
		ArrayList<ITemporalKey> currentList = keyList;
		if (index < 0 || index >= currentList.size()) {
			return null;
		}
		return currentList.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalData getTemporalData(int index)
	{
		TemporalEntry entry = getTemporalEntry(index);
		if (entry == null) {
			return null;
		}
		return entry.getTemporalData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry getTemporalEntry(int index)
	{
		ArrayList<ITemporalKey> kl = keyList;
		if (index < 0 || index >= kl.size()) {
			return null;
		}
		ITemporalKey tk = kl.get(index);
		ITemporalData data = region.get(tk);
		ITemporalKey baseKey = null;
		ITemporalData base = null;
		LinkedList<byte[]> deltaList = null;
		if (data != null) {
			if (data.__getTemporalValue().isDelta()) {
				deltaList = new LinkedList<byte[]>();
				base = collectDeltas(kl, index, deltaList);
				if (base != null) {
					baseKey = tk;
				}
			} else {
				base = data;
				baseKey = tk;
				base.__getTemporalValue().setTemporalKey(baseKey);
			}
		}

		// if base is found then construct the whole object by applying
		// the deltas.
		if (baseKey == null || base == null) {
			return null;
		} else {
			try {
				return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(baseKey,
						temporalListFactory.createTemporalData(base, deltaList));
			} catch (Exception ex) {
				Logger.error(ex);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return keyList.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int addNowRelative(ITemporalKey tkey, String username)
	{
		try {
			long writtenTime = System.currentTimeMillis();
			ITemporalKey newKey = temporalListFactory.createTemporalKey(tkey.getIdentityKey(), tkey.getStartValidTime(),
					tkey.getEndValidTime(), writtenTime, username);
			return add(newKey);
		} catch (Exception ex) {
			Logger.error(ex);
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry getNowRelativeEntry()
	{
		if (removedKey != null) {
			return null;
		}
		if (lastKey == null) {
			return null;
		}
		TemporalEntry entry = null;
		long currentTime = System.currentTimeMillis();
		if (lastKey.getStartValidTime() <= currentTime && currentTime < lastKey.getEndValidTime()
				&& currentTime > lastKey.getWrittenTime()) {
			entry = getLastEntry();
		}
		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry getFirstEntry()
	{
		if (lastKey == null) {
			return null;
		}
		TemporalEntry entry = null;
		ArrayList<ITemporalKey> currentList = keyList;
		if (currentList.size() > 0) {
			ITemporalKey firstKey = currentList.get(0);
			ITemporalData data = region.get(firstKey);
			entry = TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(firstKey, data);
		}
		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry getLastEntry()
	{
		if (lastKey == null) {
			return null;
		}
		TemporalEntry entry = null;
		ITemporalData data = region.get(lastKey);
		if (data != null) {
			data.__getTemporalValue().setTemporalKey(lastKey);
			ITemporalData base = null;
			LinkedList<byte[]> deltaList = null;
			if (data != null) {
				if (data.__getTemporalValue().isDelta()) {
					ArrayList<ITemporalKey> currentList = this.keyList;
					deltaList = new LinkedList<byte[]>();
					data = collectDeltas(currentList, currentList.size() - 1, deltaList);
				}
			}
			try {
				entry = TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(lastKey,
						temporalListFactory.createTemporalData(data, deltaList));
			} catch (Exception ex) {
				Logger.error(ex);
			}
		}
		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry getValidAt(long validAtTime)
	{
		return getAsOf(validAtTime, -1);
	}

	/**
	 * Returns the temporal entry that falls in validAtTime and asOfTime. It
	 * returns the first temporal entry that falls in validAtTime if asOfTime is
	 * -1. If both validAtTime and asOfTime are -1 then it returns the
	 * now-relative entry.
	 * 
	 * @param validAtTime
	 *            Valid-at time.
	 * @param asOfTime
	 * @see #getValidAt(long)
	 */
	@Override
	public TemporalEntry<ITemporalKey, ITemporalData> getAsOf(long validAtTime, long asOfTime)
	{
		if (validAtTime == -1 && asOfTime == -1) {
			return getNowRelativeEntry();
		}
		if (removedKey != null) {
			return null;
		}

		// if (asOfTime == -1) {
		// asOfTime = System.currentTimeMillis();
		// }
		ArrayList<ITemporalKey> currentList = this.keyList;
		if (currentList == null) {
			return null;
		}

		int lastIndex = currentList.size() - 1;
		if (lastIndex < 0) {
			return null;
		}
		int asOfIndex;
		if (asOfTime == -1) {
			asOfIndex = binarySearch_validAt(currentList, validAtTime);
			asOfTime = System.currentTimeMillis();
		} else {
			asOfIndex = binarySearch(currentList, validAtTime, asOfTime);
		}
		if (asOfIndex == NOT_FOUND) {
			return null;
		}

		// Iterate the temporal list bottom-up to find the first valid record
		ITemporalKey baseKey = null;
		ITemporalData base = null;
		LinkedList<byte[]> deltaList = null;
		for (int index = asOfIndex; index >= 0; index--) {
			ITemporalKey curElem = currentList.get(index);

			// if validAtTime is less than the start valid time then
			// there are no valid records. break immediately.
			if (curElem.getStartValidTime() > validAtTime) {
				break;
			}

			// find the first record that is valid and break
			if (validAtTime < curElem.getEndValidTime()) {
				// asOfTime must fall in between this and next record
				boolean found = false;
				if (index < currentList.size() - 1) {
					ITemporalKey nextElem = currentList.get(index + 1);
					found = curElem.getWrittenTime() <= asOfTime && asOfTime < nextElem.getWrittenTime();
				} else {
					// last item
					found = curElem.getWrittenTime() < asOfTime;
				}

				if (found) {
					// find the base object
					ITemporalData data = region.get(curElem);
					if (data != null) {
						if (data.__getTemporalValue().isDelta()) {

							deltaList = new LinkedList<byte[]>();
							base = collectDeltas(currentList, index, deltaList);

							if (base != null) {
								baseKey = curElem;
							}
						} else {
							base = data;
							baseKey = curElem;
							base.__getTemporalValue().setTemporalKey(baseKey);
						}
					}
					break;
				}
			}
		}

		// if base is found then construct the whole object by applying
		// the deltas.
		if (baseKey == null || base == null) {
			return null;
		} else {
			try {
				return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(baseKey,
						temporalListFactory.createTemporalData(base, deltaList));
			} catch (Exception ex) {
				Logger.error(ex);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TemporalEntry<ITemporalKey, ITemporalData> getWrttenTimeRange(long validAtTime, long fromWrittenTime,
			long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			return getNowRelativeEntry();
		}
		if (removedKey != null) {
			return null;
		}

		if (fromWrittenTime == -1) {
			fromWrittenTime = System.currentTimeMillis();
		}
		if (toWrittenTime == -1) {
			toWrittenTime = System.currentTimeMillis();
		}
		ArrayList<ITemporalKey> currentList = this.keyList;
		if (currentList == null) {
			return null;
		}

		int lastIndex = currentList.size() - 1;
		if (lastIndex < 0) {
			return null;
		}
		int asOfIndex;
		if (fromWrittenTime == -1 && toWrittenTime == -1) {
			asOfIndex = binarySearch_validAt(currentList, validAtTime);
		} else {
			asOfIndex = binarySearchWrittenTimeRange(currentList, validAtTime, fromWrittenTime, toWrittenTime);
		}
		if (asOfIndex == NOT_FOUND) {
			return null;
		}

		// Iterate the temporal list bottom-up to find the first valid record
		ITemporalKey baseKey = null;
		ITemporalData base = null;
		LinkedList<byte[]> deltaList = null;
		for (int index = asOfIndex; index >= 0; index--) {

			ITemporalKey tk = currentList.get(index);
			if (tk.getStartValidTime() <= validAtTime) {
				if (validAtTime < tk.getEndValidTime() && fromWrittenTime <= tk.getWrittenTime()
						&& tk.getWrittenTime() < toWrittenTime) {
					ITemporalData data = region.get(tk);
					if (data != null) {
						if (data.__getTemporalValue().isDelta()) {

							deltaList = new LinkedList<byte[]>();
							base = collectDeltas(currentList, index, deltaList);

							if (base != null) {
								baseKey = tk;
							}
						} else {
							base = data;
							baseKey = tk;
							base.__getTemporalValue().setTemporalKey(baseKey);
						}
					}
				} else {
					break;
				}
				break;
			}

			ITemporalKey curElem = currentList.get(index);

			// // if validAtTime is less than the start valid time then
			// // there are no valid records. break immediately.
			// if (curElem.getStartValidTime() > validAtTime) {
			// break;
			// }
			//
			// // find the first record that is valid and break
			// if (validAtTime < curElem.getEndValidTime()) {
			// // asOfTime must fall in between this and next record
			// boolean found = fromWrittenTime <= curElem.getWrittenTime() &&
			// curElem.getWrittenTime() < toWrittenTime;
			//
			// if (found) {
			// // find the base object
			// ITemporalData data = region.get(curElem);
			// if (data != null) {
			// if (data.__getTemporalValue().isDelta()) {
			//
			// deltaList = new LinkedList<byte[]>();
			// base = collectDeltas(currentList, index, deltaList);
			//
			// if (base != null) {
			// baseKey = curElem;
			// }
			// } else {
			// base = data;
			// baseKey = curElem;
			// base.__getTemporalValue().setTemporalKey(baseKey);
			// }
			// }
			// break;
			// }
			// }
		}

		// if base is found then construct the whole object by applying
		// the deltas.
		if (baseKey == null || base == null) {
			return null;
		} else {
			try {
				return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(baseKey,
						temporalListFactory.createTemporalData(base, deltaList));
			} catch (Exception ex) {
				Logger.error(ex);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TemporalEntry<ITemporalKey, ITemporalData>> getHistoryWrttenTimeRange(long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		if (validAtTime == -1 && fromWrittenTime == -1 && toWrittenTime == -1) {
			TemporalEntry<ITemporalKey, ITemporalData> te = getNowRelativeEntry();
			if (te == null) {
				return null;
			} else {
				return Collections.singletonList(te);
			}
		}
		if (removedKey != null) {
			return null;
		}

		if (fromWrittenTime == -1) {
			fromWrittenTime = System.currentTimeMillis();
		}
		if (toWrittenTime == -1) {
			toWrittenTime = System.currentTimeMillis();
		}
		ArrayList<ITemporalKey> currentList = this.keyList;
		if (currentList == null) {
			return null;
		}

		int lastIndex = currentList.size() - 1;
		if (lastIndex < 0) {
			return null;
		}
		int asOfIndex;
		if (fromWrittenTime == -1 && toWrittenTime == -1) {
			asOfIndex = binarySearch_validAt(currentList, validAtTime);
		} else {
			asOfIndex = binarySearchWrittenTimeRange(currentList, validAtTime, fromWrittenTime, toWrittenTime);
		}
		if (asOfIndex == NOT_FOUND) {
			return null;
		}

		// Iterate the temporal list bottom-up to find all valid records
		ITemporalKey baseKey = null;
		ITemporalData base = null;
		LinkedList<byte[]> deltaList = null;
		List<TemporalEntry<ITemporalKey, ITemporalData>> list = new ArrayList<TemporalEntry<ITemporalKey, ITemporalData>>();
		for (int index = asOfIndex; index >= 0; index--) {
			ITemporalKey tk = currentList.get(index);
			if (tk.getStartValidTime() <= validAtTime && validAtTime < tk.getEndValidTime()
					&& fromWrittenTime <= tk.getWrittenTime() && tk.getWrittenTime() < toWrittenTime) {
				ITemporalData data = region.get(tk);
				if (data != null) {
					if (data.__getTemporalValue().isDelta()) {

						deltaList = new LinkedList<byte[]>();
						base = collectDeltas(currentList, index, deltaList);

						if (base != null) {
							baseKey = tk;
						}
					} else {
						base = data;
						baseKey = tk;
						base.__getTemporalValue().setTemporalKey(baseKey);
					}
					// Construct the whole object by applying the deltas if exist
					try {
						ITemporalData td = temporalListFactory.createTemporalData(base, deltaList);
						list.add(TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tk, td));
					} catch (Exception ex) {
						Logger.error(ex);
						return null;
					}
				}
			} else {
				break;
			}
		}
		
		return list;
	}

	/**
	 * Returns all entries that have the valid range of startValidTime <=
	 * validAt < endValidTime. It returns null if the temporal list list
	 * removed.
	 * 
	 * @param validAt
	 *            The valid-at time
	 */
	@Override
	public Map<ITemporalKey, ITemporalData> getAllValidAt(long validAtTime)
	{
		if (removedKey != null) {
			return null;
		}
		// startValidTime <= validAt < endValidTime
		if (validAtTime < 0) {
			validAtTime = System.currentTimeMillis();
		}
		ArrayList<ITemporalKey> currentList = this.keyList;
		HashMap<ITemporalKey, ITemporalData> map = new HashMap<ITemporalKey, ITemporalData>(currentList.size());
		for (ITemporalKey key : currentList) {
			if (key.getStartValidTime() <= validAtTime && validAtTime < key.getEndValidTime()) {
				map.put(key, region.get(key));
			}
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<TemporalEntry<ITemporalKey, ITemporalData>> getAllAsOfEntrySet(long validAtTime, long asOfTime)
	{
		if (removedKey != null) {
			return null;
		}
		// startValidTime <= validAt < endValidTime
		if (validAtTime < 0) {
			validAtTime = System.currentTimeMillis();
		}
		if (asOfTime < 0) {
			asOfTime = System.currentTimeMillis();
		}
		ArrayList<ITemporalKey> currentList = this.keyList;
		TreeSet<TemporalEntry<ITemporalKey, ITemporalData>> set = new TreeSet();
		for (ITemporalKey key : currentList) {
			if (key.getStartValidTime() <= validAtTime && validAtTime < key.getEndValidTime()) {
				// asOfTime <= writtenTime, if -1, includes all
				if (asOfTime >= key.getWrittenTime()) {
					set.add(TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(key,
							region.get(key)));
				}
			}
		}
		return set;
	}

	/**
	 * Returns the base object and a linked list of delta objects. The caller is
	 * expected to apply the delta objects to the base object to obtain the
	 * complete object. The returned base object is the actual reference to the
	 * base object in the cache and therefore in-place modification must NOT be
	 * performed. Always copy the base object if modification is desired.
	 * 
	 * @param temporalKeyList
	 * @param index
	 * @param deltaList
	 */
	private ITemporalData collectDeltas(ArrayList<ITemporalKey> temporalKeyList, int index,
			LinkedList<byte[]> deltaList)
	{
		if (index < 0) {
			return null;
		}
		ITemporalKey key = temporalKeyList.get(index);
		ITemporalData data = region.get(key);
		data.__getTemporalValue().setTemporalKey(key);
		if (data != null && data.__getTemporalValue().isDelta()) {
			deltaList.addFirst(data.__getTemporalValue().getSerializedData());
			index--;
			data = collectDeltas(temporalKeyList, index, deltaList);
		}
		return data;
	}

	public void invalidate(ITemporalKey tkey)
	{
		if (tkey == null) {
			return;
		}
		if (removedKey != null) {
			return;
		}

		long endValidTime = tkey.getEndValidTime();
		long writtenTime = tkey.getWrittenTime();

		// copy-on-write
		synchronized (this) {
			ArrayList<ITemporalKey> currentList = this.keyList;
			ITemporalKey key;
			long startValidTime = 0;
			int lastIndex = currentList.size() - 1;
			if (lastIndex == 1) {
				return;
			}

			// copy-on-write
			ArrayList<ITemporalKey> tmp = new ArrayList<ITemporalKey>(currentList);
			int index;
			for (index = lastIndex; index >= 0; index--) {
				key = tmp.get(index);
				if (key.getStartValidTime() <= endValidTime && endValidTime < key.getEndValidTime()
						&& key.getWrittenTime() <= writtenTime) {
					startValidTime = key.getStartValidTime();
					break;
				}
			}

			// GemfireTemporalKey tk = new GemfireTemporalKey(identityKey,
			// startValidTime, endValidTime, writtenTime);
			// ITemporalValue invalidatedEntry = new TemporalValue(tk, null);

			try {
				// Must insert after the found record. Increment the index for
				// insertion
				index++;
				tmp.add(index, tkey);
				lastKey = tmp.get(tmp.size() - 1);
				this.keyList = tmp;
			} catch (Exception ex) {
				Logger.error(ex);
			}
		}
	}

	public ITemporalKey invalidate(long endValidTime, long writtenTime, String username)
	{
		if (removedKey != null) {
			return null;
		}

		synchronized (this) {

			// If the temporal list is empty then nothing to invalidate. Return
			// immediately.
			ArrayList<ITemporalKey> currentList = this.keyList;
			long startValidTime = 0;
			int lastIndex = currentList.size() - 1;
			if (lastIndex == 1) {
				return null;
			}

			// Find the last record in the temporal list
			for (int index = lastIndex; index >= 0; index--) {
				ITemporalKey key = currentList.get(index);
				if (key.getStartValidTime() <= endValidTime && endValidTime <= key.getEndValidTime()
						&& key.getWrittenTime() <= writtenTime) {
					startValidTime = key.getStartValidTime();
					break;
				}
			}

			ITemporalKey tk = null;
			try {
				tk = temporalListFactory.createTemporalKey(identityKey, startValidTime, endValidTime, writtenTime,
						username);
				region.put(tk, TemporalClientFactory.getTemporalClientFactory().createTemporalData(tk, null));
			} catch (Exception ex) {
				Logger.error(ex);
			}
			return tk;
		}
	}

	public ITemporalKey invalidateNowRelative(long endValidTime, String username)
	{
		return invalidate(endValidTime, System.currentTimeMillis(), username);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(ITemporalKey rkey)
	{
		if (rkey == null) {
			return -1;
		}
		if (rkey.getStartValidTime() != -1 && rkey.getEndValidTime() != -1) {
			return -1;
		}
		// copy-on-write
		synchronized (this) {
			if (removedKey != null) {
				return -1;
			}

			// copy-on-write
			ArrayList<ITemporalKey> currentList = this.keyList;
			ArrayList<ITemporalKey> tmp = new ArrayList<ITemporalKey>(currentList);

			// Add the removed key at the end of the temporal list.
			// Once removed, the temporal list cannot be searched by valid-at
			// and as-of.
			tmp.add(rkey);

			// Note that lastKey is not updated. The "removed" marker
			// cannot be the last key. The last key is always a valid entry.
			this.removedKey = rkey;
			this.keyList = tmp;
			return tmp.size() - 1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(String username)
	{
		if (removedKey != null) {
			return;
		}
		try {
			ITemporalKey tkey = temporalListFactory.createTemporalKey(identityKey, -1, -1, System.currentTimeMillis(),
					username);
			region.put(tkey, TemporalClientFactory.getTemporalClientFactory().createTemporalData(tkey, null));
		} catch (Exception ex) {
			Logger.error(ex);
		}
	}

	ArrayList<ITemporalKey> removePermanentlyKeyList = new ArrayList<ITemporalKey>(3);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITemporalData removePermanently(ITemporalKey removeKey)
	{
		if (removeKey == null) {
			return null;
		}

		// TODO: The following logic is broken. A race condition
		// will require keyList to be synchronized. This means
		// keyList must be synchronized throughout this class causing
		// performance degradation.
		ITemporalData td = region.remove(removeKey);

		synchronized (this) {
			ArrayList<ITemporalKey> currentList = this.keyList;
			Iterator<ITemporalKey> iterator = currentList.iterator();
			ITemporalKey tk = null;
			while (iterator.hasNext()) {
				tk = iterator.next();
				if (tk.equals(removeKey)) {
					iterator.remove();
					break;
				}
			}
			if (lastKey == tk) {
				if (currentList.size() == 0) {
					lastKey = null;
				} else {
					lastKey = currentList.get(currentList.size() - 1);
				}
			}
			return td;
		}
	}

	/**
	 * Returns the index of the specified list that has the written time greater
	 * than equal to the specified asOfTime. Returns NOT_FOUND if the specified
	 * asOfTime is less than the first writtenTime.
	 * 
	 * @param list
	 * @param asOfTime
	 */
	private static int binarySearch(ArrayList<ITemporalKey> list, long asOfTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;

		while (low <= high) {
			mid = (low + high) / 2;
			ITemporalKey tk = list.get(mid);
			if (tk.getStartValidTime() < asOfTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > asOfTime)
				high = mid - 1;
			else
				return mid;
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			return mid;
		}
	}

	private static int binarySearch_addKey(ArrayList<ITemporalKey> list, long validAtTime, long writtenTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		ITemporalKey tk = null;

		while (low <= high) {
			mid = (low + high) / 2;
			tk = list.get(mid);
			if (tk.getStartValidTime() < validAtTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > validAtTime)
				high = mid - 1;
			else {

				// startValidTime is same.
				// find the last index with the same startValidTime.
				//
				int size = list.size();
				int index = mid;
				int i;
				for (i = mid; i < size; i++) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime) {
						index = i;
					} else {
						break;
					}
				}

				// traverse up until the first index that does not match
				// startValidTime and writtenTime.
				for (i = index; i >= 0; i--) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime && tk.getWrittenTime() > writtenTime) {
						index = i;
					} else {
						break;
					}
				}

				return index;
			}
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			if (tk != null) {
				if (tk.getStartValidTime() > validAtTime) {
					mid--;
				}
			}
			return mid;
		}
	}

	/**
	 * Returns the index of the last temporal key that has the startValidTime
	 * less than or equal to the startValidTime of the specified validAtTime and
	 * the writtenTime greater than the specified writtenTime.
	 * 
	 * @param list
	 *            The temporal list.
	 * @param validAtTime
	 *            Valid-at time.
	 * @return Returns the index that represents the row that matches the
	 *         specified validAtTime and writtenTime. It returns NOT_FOUND if
	 *         the search fails. In that case, the new key must be appended at
	 *         the end of the temporal list.
	 */
	private static int binarySearch(ArrayList<ITemporalKey> list, long validAtTime, long asOfTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		ITemporalKey tk = null;

		while (low <= high) {
			mid = (low + high) / 2;
			tk = list.get(mid);
			if (tk.getStartValidTime() < validAtTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > validAtTime)
				high = mid - 1;
			else {

				// startValidTime is same.
				// find the last index with the same startValidTime.
				int size = list.size();
				int index = mid;
				int i;
				for (i = mid; i < size; i++) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime) {
						index = i;
					} else {
						break;
					}
				}

				// traverse up until the first index that does not match
				// validAtTime and asOfTime.
				// TODO: Validate this logic.
				for (i = index; i >= 0; i--) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime && tk.getWrittenTime() > asOfTime) {
						index = i;
					} else {
						break;
					}
				}

				return index;
			}
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			if (tk != null) {
				if (tk.getStartValidTime() > validAtTime) {
					mid--;
				}
			}
			return mid;
		}
	}

	private static int binarySearch_validAtKey(ArrayList<ITemporalKey> list, long validAtTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		ITemporalKey tk = null;

		while (low <= high) {
			mid = (low + high) / 2;
			tk = list.get(mid);
			if (tk.getStartValidTime() < validAtTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > validAtTime)
				high = mid - 1;
			else {
				if (validAtTime > tk.getEndValidTime()) {
					return NOT_FOUND;
				}

				// startValidTime is same.

				// found the mid index. must sweep up and down to determine
				// the index by searching the following order:
				// startValidTime, writtenTime, operation-time

				// find the last index with the same startValidTime.
				int size = list.size();
				int index = mid;
				int i;
				for (i = mid; i < size; i++) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime && validAtTime < tk.getEndValidTime()) {
						index = i;
					} else {
						break;
					}
				}
				return index;
			}
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			if (tk != null) {
				if (tk.getStartValidTime() > validAtTime) {
					mid--;
				}
			}
			if (validAtTime > tk.getEndValidTime()) {
				return NOT_FOUND;
			}
			return mid;
		}
	}

	private static int binarySearch_validAt(ArrayList<ITemporalKey> list, long validAtTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		ITemporalKey tk = null;

		while (low <= high) {
			mid = (low + high) / 2;
			tk = list.get(mid);
			if (tk.getStartValidTime() < validAtTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > validAtTime)
				high = mid - 1;
			else {
				if (validAtTime >= tk.getEndValidTime()) {
					return NOT_FOUND;
				}

				// startValidTime is same.

				// found the mid index. must sweep up and down to determine
				// the index by searching the following order:
				// startValidTime, writtenTime, operation-time

				// find the last index with the same startValidTime.
				int size = list.size();
				int index = mid;
				int i;
				for (i = mid; i < size; i++) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime && validAtTime < tk.getEndValidTime()) {
						index = i;
					} else {
						break;
					}
				}
				return index;
			}
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			if (tk != null) {
				if (tk.getStartValidTime() > validAtTime) {
					mid--;
				}
			}
			if (validAtTime >= tk.getEndValidTime()) {
				return NOT_FOUND;
			}
			return mid;
		}
	}

	/**
	 * Returns the index of the last temporal key that has the startValidTime
	 * less than or equal to the startValidTime of the specified validAtTime and
	 * the writtenTime is in the range of the specified fromWrittenTime and
	 * toWrittenTime.
	 * 
	 * @param list
	 *            The temporal list.
	 * @param validAtTime
	 *            Valid-at time.
	 * @param fromWrittenTime
	 *            Start of the written time range, inclusive.
	 * @param toWrittenTime
	 *            End of the written time range, exclusive.
	 * @return Returns the index that represents the row that matches the
	 *         specified validAtTime and writtenTime. It returns NOT_FOUND if
	 *         the search fails. In that case, the new key must be appended at
	 *         the end of the temporal list.
	 */
	private static int binarySearchWrittenTimeRange(ArrayList<ITemporalKey> list, long validAtTime,
			long fromWrittenTime, long toWrittenTime)
	{
		int low = 0;
		int high = list.size() - 1;
		int mid = 0;
		ITemporalKey tk = null;

		while (low <= high) {
			mid = (low + high) / 2;
			tk = list.get(mid);
			if (tk.getStartValidTime() < validAtTime)
				low = mid + 1;
			else if (tk.getStartValidTime() > validAtTime)
				high = mid - 1;
			else {

				// startValidTime is same.

				// find the last index with the same startValidTime.
				int size = list.size();
				int startIndex = mid;
				int i;
				for (i = startIndex; i < size; i++) {
					tk = list.get(i);
					if (tk.getStartValidTime() == validAtTime) {
						mid = i;
					} else {
						break;
					}
				}
				break;

				// // traverse up until the first index that does not match
				// // validAtTime, fromWrittenTime and toWrittenTime.
				// // TODO: Validate this logic.
				// for (i = index; i >= 0; i--) {
				// tk = list.get(i);
				// if (tk.getStartValidTime() <= validAtTime && validAtTime <
				// tk.getEndValidTime() && fromWrittenTime <=
				// tk.getWrittenTime()
				// && tk.getWrittenTime() < toWrittenTime) {
				// return i;
				// }
				// }
			}
		}
		if (high < 0) {
			return NOT_FOUND;
		} else {
			// if (tk != null) {
			// // traverse up the list from the bottom to find the last index
			// that
			// // has the valid range
			// for (int i = high; i >= mid; i--) {
			// tk = list.get(i);
			// if (tk.getStartValidTime() <= validAtTime && validAtTime <
			// tk.getEndValidTime() && fromWrittenTime <= tk.getWrittenTime()
			// && tk.getWrittenTime() < toWrittenTime) {
			// return i;
			// }
			// }
			// if (tk.getStartValidTime() > validAtTime) {
			// mid--;
			// }
			// }
			return mid;
		}
	}

	public void dump()
	{
		synchronized (this) {
			TemporalEntry entry = getLastEntry();
			ITemporalData data = entry.getTemporalData();
			data.__getTemporalValue().deserializeData();
			// data.setAttachmentSets(data.getTemporalValue().getAttachmentIdentityKeySets());
			System.out.println();
			System.out.println("=====================================================");
			System.out.println("   IdentityKey = " + identityKey);
			System.out.println("   Last value = " + getLastEntry());

			// now do temporal data
			ArrayList<ITemporalKey> cur = this.keyList;
			if (cur == null) {
				System.out.println("   TemporalList empty");
			} else {
				// never modify in place
				int curSize = cur.size();
				String sv, ev;
				System.out.println("   IdentityKey  StartValid  EndValidTime  WrittenTime  Value");
				System.out.println("   -----------  ----------  ------------  -----------  -----");
				for (int i = 0; i < curSize; i++) {
					ITemporalKey curElem = cur.get(i);
					if (curElem.getStartValidTime() >= TemporalUtil.MAX_TIME) {
						sv = "&";
					} else {
						sv = curElem.getStartValidTime() + "";
					}
					if (curElem.getEndValidTime() >= TemporalUtil.MAX_TIME) {
						ev = "&";
					} else {
						ev = curElem.getEndValidTime() + "";
					}
					System.out.println("   " + curElem.getIdentityKey() + "  " + sv + "  " + ev + "  "
							+ curElem.getWrittenTime() + "  " + curElem);
				}
			}
			System.out.println("=====================================================");
			System.out.println();
		}
	}

	private static final SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	static {
		iso8601DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void dumpAll(FileWriter writer, char delimiter) throws IOException
	{
		synchronized (this) {
			ArrayList<ITemporalKey> cur = this.keyList;
			int curSize = cur.size();
			String sv, ev, wt;
			for (int i = 0; i < curSize; i++) {
				ITemporalKey tk = cur.get(i);
				ITemporalData td = region.get(tk);
				sv = iso8601DateFormat.format(new Date(tk.getStartValidTime()));
				ev = iso8601DateFormat.format(new Date(tk.getEndValidTime()));
				wt = iso8601DateFormat.format(new Date(tk.getWrittenTime()));

				// write key first
				// writer.write(tk.getIdentityKey());
				writer.write(delimiter);
				writer.write(sv);
				writer.write(delimiter);
				writer.write(ev);
				writer.write(delimiter);
				writer.write(wt);
				writer.write(delimiter);
				writer.write(tk.getUsername());
				writer.write(delimiter);
				// writer.write(td);
			}
		}
	}

	public TemporalDataList getTemporalDataList()
	{
		TemporalEntry lastDataEntry = getLastEntry();
		ArrayList entryList = new ArrayList();
		ArrayList<ITemporalKey> cur = this.keyList;
		for (ITemporalKey tk : cur) {
			ITemporalData data = region.get(tk);
			entryList.add(TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tk, data));
		}
		PartitionedRegion pr = (PartitionedRegion) region;
		int bucketId = -1;
		if (lastDataEntry != null) {
			BucketRegion br = pr.getBucketRegion(lastDataEntry.getTemporalKey());
			bucketId = br.getId();
		}
		String memberId = pr.getCache().getDistributedSystem().getMemberId();
		String memberName = pr.getCache().getName();
		String host = pr.getCache().getDistributedSystem().getDistributedMember().getHost();
		return TemporalInternalFactory.getTemporalInternalFactory().createTemporalDataList(identityKey, lastDataEntry,
				entryList, bucketId, memberId, memberName, host, pr.getFullPath());
	}

	public Region getRegion()
	{
		return region;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemoved(long asOfTime)
	{
		if (removedKey == null) {
			return false;
		}
		return asOfTime > removedKey.getWrittenTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemoved()
	{
		return removedKey != null;
	}
}