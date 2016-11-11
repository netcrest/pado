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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TemporalList contains a complete history of updates made via temporal
 * operations, namely, put, invalid, and remove. It is maintained by
 * TemporalCacheListener in real-time.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public interface ITemporalList<K, V>
{
	/**
	 * Returns the temporal list name. The implementing class should return the
	 * default name, "temporal", if undefined.
	 */
	public String getName();

	/**
	 * Adds the specified temporal key to the temporal list. Note that the
	 * temporal key must have the same identity key as the ones that are already
	 * in the temporal list.
	 * 
	 * @param tkey
	 *            Temporal key
	 * @return The index at which the specified temporal key is added. It
	 *         returns -1 if the temporal key cannot be added. This method also
	 *         handles removals, i.e., both tkey.getStartValidTime() and
	 *         tkey.getStartEndValidTime() are -1.
	 */
	public int add(ITemporalKey<K> tkey);

	/**
	 * Returns true if the specified temporal key exists in this temporal list.
	 * Note that some "get" methods, it carries out the search operation even if
	 * the temporal list is removed.
	 * 
	 * @param tkey
	 *            Temporal key
	 */
	public boolean contains(ITemporalKey<K> tkey);

	/**
	 * Returns the index of the specified key in the temporal list. It returns
	 * -1 if the key is not found in the temporal list.
	 * 
	 * @param tk
	 *            Temporal key.
	 */
	public int getIndex(ITemporalKey<K> tkey);

	/**
	 * Returns the end written time. The end written time is the written time of
	 * the next key after the specified key in the temporal list.
	 * 
	 * @param tk
	 *            Temporal key
	 * @return Return the next key's written time or Long.MAX_VALUE if the
	 *         specified key is the last key in the temporal list.
	 */
	public long getEndWrittenTime(ITemporalKey<K> tkey);

	/**
	 * Returns the temporal key at the specified index position in the temporal
	 * list.
	 * 
	 * @param index
	 *            Temporal list index position
	 * @return null if index is less than 0 or greater than or equal to the
	 *         temporal list size.
	 */
	public ITemporalKey<K> getTemporalKey(int index);

	/**
	 * Returns the temporal data at the specified index position in the temporal
	 * list.
	 * 
	 * @param index
	 *            Temporal list index position
	 * @return null if index is less than 0 or greater than or equal to the
	 *         temporal list size.
	 */
	public ITemporalData getTemporalData(int index);

	/**
	 * Returns the temporal entry at the specified index position in the
	 * temporal list.
	 * 
	 * @param index
	 *            Temporal list index position
	 * @return null if index is less than 0 or greater than or equal to the
	 *         temporal list size.
	 */
	public TemporalEntry<K, V> getTemporalEntry(int index);

	/**
	 * Returns the temporal list size.
	 */
	public int size();

	/**
	 * Adds the specified value using the current system time as the
	 * writtenTime. It assigns the temporal key with the system time into the
	 * temporal value.
	 * 
	 * @param tkey
	 *            The temporal key.
	 * @param username
	 *            The name of the user who is adding the temporal entity.
	 * @return The index at which the specified temporal key is added. It
	 *         returns -1 if the temporal key cannot be added. This method also
	 *         handles removals, i.e., both tkey.getStartValidTime() and
	 *         tkey.getStartEndValidTime() are -1.
	 */
	public int addNowRelative(ITemporalKey<K> tkey, String username);

	/**
	 * Returns the now-relative temporal entry. The now-relative entry is the
	 * entry as of now or current system time. It returns null if the temporal
	 * list is removed.
	 */
	public TemporalEntry<K, V> getNowRelativeEntry();

	/**
	 * Returns the first entry in the temporal list. The first entry is normally
	 * the first checkpoint containing the full object if delta is enabled. Note
	 * that some "get" methods, it returns the first entry even if the temporal
	 * list is removed. It returns null if the temporal list is empty.
	 */
	public TemporalEntry<K, V> getFirstEntry();

	/**
	 * Returns the last temporal entry in the temporal list. The last entry is
	 * not necessarily now-relative as it might have been expired as of now. It
	 * is simply the last entry in the temporal list. Note that some "get"
	 * methods, it carries out the search operation even if the temporal list is
	 * removed. It returns null if the temporal list is empty.
	 */
	public TemporalEntry<K, V> getLastEntry();

	/**
	 * Returns the first temporal entry that falls in the specified valid time.
	 * It returns null if the temporal list is removed.
	 * 
	 * @param validAtTime
	 *            The valit-at time.
	 */
	public TemporalEntry<K, V> getValidAt(long validAtTime);

	/**
	 * Returns the temporal entry that falls in validAtTime and asOfTime. It
	 * returns the first temporal entry that falls in validAtTime if asOfTime is
	 * -1. If both validAtTime and asOfTime are -1, then it returns the
	 * now-relative entry. It returns null if the temporal list is removed.
	 * 
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param asOfTime
	 *            As-of time in msec
	 * @see #getValidAt(long)
	 */
	public TemporalEntry<K, V> getAsOf(long validAtTime, long asOfTime);

	/**
	 * Returns all temporal entries that fall in validAtTime and asOfTime. It
	 * returns the first temporal entries that fall in validAtTime if asOfTime
	 * is -1. If both validAtTime and asOfTime are -1, then it returns the
	 * now-relative entries. It returns null if the temporal list is removed.
	 * 
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param asOfTime
	 *            As-of time in msec
	 */
	public Set<TemporalEntry<K, V>> getAllAsOfEntrySet(long validAtTime, long asOfTime);

	/**
	 * Returns the temporal entry that falls in validAtTime and the written time
	 * range. It returns the last temporal entry that falls in validAtTime if
	 * fromWrittenTime and toWrittenTime are -1. If all arguments are -1, then
	 * it returns the now-relative entry. It returns null if the temporal list
	 * is removed, does not exist, or temporal entries meeting the match time
	 * search criteria are not found.
	 * 
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param fromWrittenTime
	 *            Start of the written time range, inclusive.
	 * @param toWrittenTime
	 *            End of the written time range, exclusive.
	 */
	public TemporalEntry<ITemporalKey, ITemporalData> getWrttenTimeRange(long validAtTime, long fromWrittenTime,
			long toWrittenTime);

	/**
	 * Returns a list of temporal entries that fall in validAtTime and the
	 * written time range. Note that this method returns one or more temporal
	 * entries with the same identity key. It returns returns a list of that
	 * contains the last temporal entry that falls in validAtTime if
	 * fromWrittenTime and toWrittenTime are -1. If all arguments are -1, then
	 * it returns the new-relative entry. It returns null if the temporal list
	 * is removed, does not exist, or temporal entries meeting the match time
	 * search criteria are not found.
	 * 
	 * @param validAtTime
	 *            Valid-at time in msec
	 * @param fromWrittenTime
	 *            Start of the written time range, inclusive.
	 * @param toWrittenTime
	 *            End of the written time range, exclusive.
	 * @return
	 */
	public List<TemporalEntry<ITemporalKey, ITemporalData>> getHistoryWrttenTimeRange(long validAtTime,
			long fromWrittenTime, long toWrittenTime);

	/**
	 * Returns all temporal entries that have the valid range of startValidTime
	 * <= validAt < endValidTime. It returns null if the temporal list is
	 * removed.
	 * 
	 * @param validAt
	 *            Valid-at time in msec
	 */
	public Map<ITemporalKey<K>, ITemporalData<K>> getAllValidAt(long validAt);

	/**
	 * Marks the temporal list as "removed" by adding the specified key.
	 * 
	 * @param removeKey
	 *            removeKey must have -1 for both start and end valid time
	 *            values, otherwise, remove is not performed.
	 * @return The index at which the specified temporal key is placed in the
	 *         temporal list. It returns -1 if the temporal key is invalid,
	 *         i.e., startVadlidTime and endValidTime are not -1, or has already
	 *         been removed.
	 */
	public int remove(ITemporalKey<K> removeKey);

	/**
	 * Permanently removes the specified temporal key and its mapping data from
	 * the temporal list. Unlike {@link #remove(ITemporalKey)}, this method does
	 * not mark as remove but actually removes the entry from the temporal list
	 * and the underlying store. Data is not recoverable after this call.
	 * 
	 * @param removeKey
	 *            removeKey.
	 * @return ITemporalData<K> Data that has been removed. null if not found.
	 */
	public ITemporalData<K> removePermanently(ITemporalKey<K> removeKey);

	/**
	 * Marks the temporal list as "removed". Unlike
	 * {@link #remove(ITemporalKey)}, this method may trigger an event to notify
	 * the grid and clients of its removal.
	 * 
	 * @param username
	 *            The name of the user who is removing the temporal list.
	 */
	public void remove(String username);

	/**
	 * Returns true if the temporal list was removed as of the specified as-of
	 * time. Note that once removed, the temporal list is removed from all
	 * temporal searches.
	 * 
	 * @param asOfTime
	 */
	public boolean isRemoved(long asOfTime);

	/**
	 * Returns true if the temporal list is marked as "removed". Removed
	 * temporal lists do not participate in temporal search.
	 */
	public boolean isRemoved();

	/**
	 * Dumps the temporal list to the log file.
	 */
	public void dump();

	/**
	 * Returns a TemporalDataList object that can be sent over the network.
	 */
	public TemporalDataList getTemporalDataList();
}