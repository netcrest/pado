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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

/**
 * TemporalManager manages all temporal lists hosted by a VM.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class TemporalManager
{
	/**
	 * Temporal manager class
	 */
	private static Class temporalManagerClass;

	/**
	 * Contains &lt;full-path, TemporalManager&gt; pairs.
	 */
	protected final static HashMap<String, TemporalManager> managerMap = new HashMap<String, TemporalManager>(30);

	// Loads the temporal manager class.
	static {
		try {
			temporalManagerClass = PadoUtil.getClass(Constants.PROP_CLASS_TEMPORAL_MANAGER,
					Constants.DEFAULT_CLASS_TEMPORAL_MANAGER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Temporal manager instance
	 */
	protected TemporalManager temporalManager;

	private boolean isLuceneEnabled = false;

	/**
	 * Returns the temporal manager instance pertaining to the specified full
	 * path. Each full path has a dedicated temporal manager.
	 * 
	 * @param fullPath
	 *            Full path
	 */
	public static TemporalManager getTemporalManager(String fullPath)
	{
		synchronized (managerMap) {
			TemporalManager manager = managerMap.get(fullPath);
			if (manager == null) {
				try {
					Method method = temporalManagerClass.getMethod("getTemporalManager", String.class);
					manager = (TemporalManager) method.invoke(null, fullPath);
					if (manager != null) {
						managerMap.put(fullPath, manager);
						manager.temporalManager = manager;
					}
				} catch (Exception e) {
					Logger.severe(e);
				}
			}
			return manager;
		}
	}

	/**
	 * Removes the temporal manager for the specified full path if it exists. It
	 * permanently disables and removes the temporal manager.
	 * 
	 * @param fullPath
	 *            return Removed temporal manager or null if it does not exist
	 */
	public static TemporalManager remove(String fullPath)
	{
		TemporalManager manager;
		synchronized (managerMap) {
			manager = managerMap.remove(fullPath);
			if (manager != null) {
				// Block till done
				manager.setEnabled(false, false, false /* spawnThread */);
			}
		}
		return manager;
	}

	/**
	 * Returns a non-null set of all temporal full paths. The returned set
	 * includes both enabled and disabled paths.
	 */
	public static Set<String> getAllTemporalFullPaths()
	{
		return Collections.unmodifiableSet(managerMap.keySet());
	}

	/**
	 * Returns a set of identity keys that contains all of identity keys that
	 * this temporal manager manages.
	 */
	public Set getIdentityKeySet()
	{
		return temporalManager.getIdentityKeySet();
	}

	/**
	 * Returns a list of identity keys that contains all of identity keys that
	 * this temporal manager manages.
	 */
	public List getIdentityKeyList()
	{
		return temporalManager.getIdentityKeyList();
	}

	/**
	 * Returns a list of now-relative temporal entries extracted from temporal
	 * lists.
	 */
	public List<TemporalEntry> getNowRelativeTemporalEntryList()
	{
		return temporalManager.getNowRelativeTemporalEntryList();
	}

	/**
	 * Returns a list of last temporal entries extracted from temporal lists.
	 */
	public List<TemporalEntry> getLastTemporalEntryList()
	{
		return temporalManager.getLastTemporalEntryList();
	}

	/**
	 * Returns the temporal data list pertaining to the specified identity key.
	 * The returned temporal data list can be safely shipped over the network.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public TemporalDataList getTemporalDataList(Object identityKey)
	{
		return temporalManager.getTemporalDataList(identityKey);
	}

	/**
	 * Returns the temporal list pertaining to the specified identity key. The
	 * returned temporal list is the actual temporal list managed by the
	 * temporal manager. It cannot be shipped over the network.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public ITemporalList getTemporalList(Object identityKey)
	{
		return temporalManager.getTemporalList(identityKey);
	}

	/**
	 * Prints the temporal list of the specified identity key in the log file.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public void dump(Object identityKey)
	{
		temporalManager.dump(identityKey);
	}

	/**
	 * Clears the temporal list of the specified identity key. This method
	 * actually deletes the temporal list from the temporal manager. After this
	 * call, the temporal list no longer exists.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public void clearTemporalList(Object identityKey)
	{
		temporalManager.clearTemporalList(identityKey);
	}

	/**
	 * Returns the now-relative entry of the specified identity key.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public TemporalEntry getNowRelativeEntry(Object identityKey)
	{
		return temporalManager.getNowRelativeEntry(identityKey);
	}

	/**
	 * Returns the first entry in the temporal list of the specified identity
	 * key. It returns null if the temporal list does not exist.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public TemporalEntry getFirstEntry(Object identityKey)
	{
		return temporalManager.getFirstEntry(identityKey);
	}

	/**
	 * Returns the last temporal entry in the temporal list. The last entry is
	 * not necessarily now-relative as it might have been expired as of now. It
	 * is simply the last entry in the temporal list. Note that some "get"
	 * methods, it carries out the search operation even if the temporal list is
	 * removed. It returns null if the temporal list is empty.
	 * 
	 * @param identityKey
	 *            Identity key
	 */
	public TemporalEntry getLastEntry(Object identityKey)
	{
		return temporalManager.getLastEntry(identityKey);
	}

	/**
	 * Returns the as-of entry of the specified identity key. It returns null if
	 * the entry is not found.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param validTime
	 *            Valid-at time in msec
	 * @param asOfTime
	 *            As-of time in msec
	 */
	public TemporalEntry getAsOf(Object identityKey, long validTime, long asOfTime)
	{
		return temporalManager.getAsOf(identityKey, validTime, asOfTime);
	}

	/**
	 * Returns the now-relative (as of now) entry of the specified identity key.
	 * It returns null if the entry is not found.
	 * 
	 * @param identityKey
	 *            Identity key
	 * @param validTime
	 *            Valid-at time in msec
	 */
	public TemporalEntry getAsOf(Object identityKey, long validTime)
	{
		return temporalManager.getAsOf(identityKey, validTime);
	}

	/**
	 * Returns the index of the specified key in the temporal list. It returns
	 * -1 if the key is not found in the temporal list.
	 * 
	 * @param tk
	 *            Temporal key.
	 */
	public int getIndex(ITemporalKey tk)
	{
		return temporalManager.getIndex(tk);
	}

	/**
	 * Returns the end written time. The end written time is the written time of
	 * the next key after the specified key in the temporal list.
	 * 
	 * @param tk
	 *            Temporal key
	 * @return Return the next key's written time or Long.MAX_VALUE if the
	 *         specified key is the last key in the temporal list.
	 */
	public long getEndWrittenTime(ITemporalKey tk)
	{
		return temporalManager.getEndWrittenTime(tk);
	}

	/**
	 * Enables/disables the temporal data management mechanics. If Enabled
	 * (true), then it freshly builds all of the temporal lists pertaining to
	 * this temporal manager's path regardless of whether it is already enabled
	 * and the temporal lists have already been built. If disabled (false), then
	 * it clears all of the temporal lists and detaches the temporal data update
	 * listener such that it completely stops producing temporal data for this
	 * temporal manager's path.
	 * 
	 * @param enabled
	 *            true to enable, false to disable.
	 * @param buildLucene
	 *            true to build Lucene indexes. This option has no effect if
	 *            enabled is false.
	 * @param spawnThread
	 *            true to enable in thread, false to block till done.
	 */
	public void setEnabled(boolean enabled, boolean buildLucene, boolean spawnThread)
	{
		temporalManager.setEnabled(enabled, buildLucene, spawnThread);
	}

	/**
	 * Returns true if the temporal data management mechanics is enabled, false,
	 */
	public boolean isEnabled()
	{
		return temporalManager.isEnabled();
	}

	/**
	 * Enables/disables all temporal paths.
	 * 
	 * @param enabled
	 *            true to enable, false to disable.
	 * @param buildLucene
	 *            true to build Lucene indexes. This option has no effect if
	 *            enabled is false.
	 * @param spawnThread
	 *            true to enable in thread, false to block till done.
	 */
	public synchronized static void setEnabledAll(boolean enabled, boolean buildLucene, boolean spawnThread)
	{
		Set<Map.Entry<String, TemporalManager>> set = managerMap.entrySet();
		for (Map.Entry<String, TemporalManager> entry : set) {
			TemporalManager tm = entry.getValue();
			tm.setEnabled(enabled, buildLucene, spawnThread);
		}
	}
	
	/**
	 * Re-enables temporal managers that are already enabled. This method
	 * may be useful during failover when temporal and Lucene indexes need
	 * to be rebuilt due to a primary promotion of redundant data.
	 */
	public synchronized static void resetEnabledAll()
	{
		Set<Map.Entry<String, TemporalManager>> set = managerMap.entrySet();
		for (Map.Entry<String, TemporalManager> entry : set) {
			TemporalManager tm = entry.getValue();
			if (tm.isEnabled()) {
				tm.setEnabled(true, true, false);
			}
		}
	}

	/**
	 * Returns true if all of the temporal paths are enabled. It returns false
	 * if at least one of the temporal paths is disabled.
	 */
	public static boolean isEnabledAll()
	{
		Set<Map.Entry<String, TemporalManager>> set = managerMap.entrySet();
		for (Map.Entry<String, TemporalManager> entry : set) {
			TemporalManager tm = entry.getValue();
			if (tm.isEnabled() == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the number of temporal lists. This number represents the total
	 * number of temporal lists of this particular region currently maintained
	 * by this server.
	 */
	public int getTemporalListCount()
	{
		return temporalManager.getTemporalListCount();
	}

	/**
	 * Enables/disables Lucene indexing for all temporal paths. Note that
	 * enabling disabled paths requires rebuilding of Lucene indexes from
	 * scratch in order to synchronize with dynamic indexes.
	 * 
	 * @param enabled
	 *            true to enable, false to disable
	 * @param buildLucene
	 *            true to build Lucene indexes. This option has no effect if
	 *            enabled is false.
	 */
	public static void setLuceneEnabledAll(boolean enabled, boolean buildLucene)
	{
		Set<Map.Entry<String, TemporalManager>> set = managerMap.entrySet();
		for (Map.Entry<String, TemporalManager> entry : set) {
			TemporalManager tm = entry.getValue();
			tm.setLuceneEnabled(enabled, buildLucene);
		}
	}

	/**
	 * Returns true if all of the temporal paths are enabled. It returns false
	 * if at least one of the temporal paths is disabled.
	 */
	public static boolean isLuceneEnabledAll()
	{
		Set<Map.Entry<String, TemporalManager>> set = managerMap.entrySet();
		for (Map.Entry<String, TemporalManager> entry : set) {
			TemporalManager tm = entry.getValue();
			if (tm.isLuceneEnabled() == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Enables/disables Lucene indexing for this temporal path.
	 * 
	 * @param enabled
	 *            true to enable or false to disable Lucene indexing. If true
	 *            and previously false, then Lucene indexes are rebuilt for this
	 *            temporal path.
	 * @param buildLucene
	 *            true to build Lucene indexes. This option has no effect if
	 *            enabled is false.
	 */
	public void setLuceneEnabled(boolean enabled, boolean buildLucene)
	{
		if (enabled) {
			if (this.isLuceneEnabled == false) {
				// Rebuild Lucene indexes
				setEnabled(true, buildLucene, false);
			}
		}
		this.isLuceneEnabled = enabled;
	}

	/**
	 * Returns true if Lucene indexing is enabled.
	 */
	public boolean isLuceneEnabled()
	{
		return isLuceneEnabled;
	}

	/**
	 * Pauses the temporal event dispatcher. Note that during the pause period,
	 * underlying data grid events continue to accumulate in the dispatcher
	 * queue. It is recommended that the pause period should be brief. Invoke
	 * {@link #resume()} to exit the pause mode.
	 */
	public abstract void pause();

	/**
	 * Flushes the temporal event dispatcher queue by dispatching all of the
	 * temporal events in the queue.
	 */
	public abstract void flush();

	/**
	 * Resumes the dispatcher by lifting the pause mode. The dispatcher works in
	 * the normal mode after this call.
	 */
	public abstract void resume();
}
