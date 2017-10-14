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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.gemfire.IGemfireGridContextServer;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.gemfire.info.GemfireGridInfo;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.index.helper.ComparatorFactory;
import com.netcrest.pado.index.provider.gemfire.OqlSearch;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.pql.PqlParser;
import com.netcrest.pado.pql.PqlParser.OrderByQueryString;
import com.netcrest.pado.pql.VirtualCompiledUnit2;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.AttachmentResults;
import com.netcrest.pado.temporal.AttachmentSet;
import com.netcrest.pado.temporal.IFilter;
import com.netcrest.pado.temporal.ITemporalAdminBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.ITemporalValue;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalInternalFactory;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalValue;
import com.netcrest.pado.temporal.gemfire.impl.TemporalCacheListener;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBizImpl<K, V> implements ITemporalBiz<K, V>
{
	@Resource
	IBizContextServer bizContext;

	private ComparatorFactory comparatorFactory = new ComparatorFactory();

	private ITemporalList<K, V> getTemporalList(K identityKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		return tm.getTemporalList(identityKey);
	}

	private Object getKeyMapReferenceId()
	{
		Object keyMapReferenceId;
		Object[] td = bizContext.getGridContextServer().getTransientData();
		if (td != null && td.length > 0) {
			keyMapReferenceId = td[0];
		} else {
			keyMapReferenceId = Thread.currentThread().getId();
		}
		return keyMapReferenceId;
	}

	@Override
	public IBizContextClient getBizContext()
	{
		// do nothing
		return null;
	}

	@Override
	public void setGridPath(String gridPath)
	{
		// client only
	}

	@Override
	public String getGridPath()
	{
		// client only
		return null;
	}

	@Override
	public void setReference(boolean isReference)
	{
		// client only
	}

	@Override
	public boolean isReference()
	{
		// client only
		return false;
	}

	@Override
	public void __setVirtualEntityPath(String virtualEntityPath)
	{
		// client only
	}

	@Override
	public String __getVirtualEntityPath()
	{
		// client only
		return null;
	}

	@Override
	public void setDepth(int depth)
	{
		// client only
	}

	@Override
	public int getDepth()
	{
		// client only
		return 0;
	}

	@Override
	public void setPutExceptionEnabled(boolean isPutExceptionEnabled)
	{
		// client only
	}

	@Override
	public boolean isPutExceptionEnabled()
	{
		// client only
		return false;
	}

	@Override
	public ITemporalAdminBizLink<K, V> getTemporalAdminBiz()
	{
		// client only
		return null;
	}

	// @SuppressWarnings({ "rawtypes", "unchecked" })
	// @BizMethod
	// public V get(K identityKey, int depth)
	// {
	// TemporalEntry entry = getEntry(identityKey);
	// if (entry == null) {
	// return null;
	// }
	// Object value = entry.getValue();
	// if (s) {
	// // supports KeyMap only
	// if (value instanceof KeyMap) {
	// ReferenceFinder finder = new ReferenceFinder();
	// value = finder.getReferences((KeyMap) value, depth, -1, -1);
	// }
	// }
	// return (V) value;
	// }

	@BizMethod
	@Override
	public V get(K identityKey)
	{
		TemporalEntry entry = getEntry(identityKey);
		if (entry == null) {
			return null;
		}
		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		Object[] args = bizContext.getGridContextServer().getAdditionalArguments();
		if (args != null && args.length > 0) {
			isReference = (Boolean) args[0];
			if (args.length > 1) {
				depth = (Integer) args[1];
			}
			if (args.length > 2) {
				virtualEntityPath = (String) args[2];
			}
		}
		Object value = entry.getValue();
		if (isReference) {
			if (depth != 0) {
				// supports KeyMap only
				if (value instanceof KeyMap) {
					ReferenceFinder finder = new ReferenceFinder();
					RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
							.getRegionFunctionContext();
					value = finder.getReferences((KeyMap) value, depth, -1, -1, getKeyMapReferenceId());
				}
			}
		}
		return (V) value;
	}

	@BizMethod
	@Override
	public V get(K identityKey, long validAtTime)
	{
		return get(identityKey, validAtTime, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public V get(K identityKey, long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		TemporalEntry entry = tm.getAsOf(identityKey, validAtTime, asOfTime);
		if (entry == null) {
			return null;
		}
		ITemporalData data = entry.getTemporalData();
		Object value;
		if (data instanceof GemfireTemporalData) {
			value = ((GemfireTemporalData) data).getValue();
		} else {
			value = data;
		}

		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		Object[] args = bizContext.getGridContextServer().getAdditionalArguments();
		if (args != null && args.length > 0) {
			isReference = (Boolean) args[0];
			if (args.length > 1) {
				depth = (Integer) args[1];
			}
			if (args.length > 2) {
				virtualEntityPath = (String) args[2];
			}
		}
		if (isReference) {
			if (depth != 0) {
				// supports KeyMap only
				if (value instanceof KeyMap) {
					ReferenceFinder finder = new ReferenceFinder();
					value = finder.getReferences((KeyMap) value, depth, validAtTime, asOfTime, getKeyMapReferenceId());
				}
			}
		}

		return (V) value;
	}

	@BizMethod
	@Override
	public TemporalEntry<K, V> getEntry(K identityKey)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		TemporalEntry<K, V> entry = tm.getNowRelativeEntry(identityKey);
		if (entry != null) {
			Object value = entry.getValue();
			if (value instanceof KeyMap) {
				Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
				boolean isReference = false;
				int depth = -1;
				String virtualEntityPath = null;
				if (args != null) {
					if (args.length > 0) {
						isReference = (Boolean) args[0];
					}
					if (args.length > 1) {
						depth = (Integer) args[1];
					}
					if (args.length > 2) {
						virtualEntityPath = (String) args[2];
					}
				}

				if (isReference) {
					if (depth != 0) {
						ReferenceFinder finder = new ReferenceFinder();
						finder.getReferences((KeyMap) value, depth, -1, -1, getKeyMapReferenceId());
					}
				}
			}
		}
		return entry;
	}

	@BizMethod
	@Override
	public TemporalEntry<K, V> getEntry(K identityKey, long validAtTime)
	{
		return getEntry(identityKey, validAtTime, System.currentTimeMillis());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	@Override
	public TemporalEntry<K, V> getEntry(K identityKey, long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		TemporalEntry<K, V> entry = tm.getAsOf(identityKey, validAtTime, asOfTime);

		if (entry != null) {
			Object value = entry.getValue();
			if (value instanceof KeyMap) {
				Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
				boolean isReference = false;
				String virtualEntityPath = null;
				int depth = -1;
				if (args != null) {
					if (args.length > 0) {
						isReference = (Boolean) args[0];
					}
					if (args.length > 1) {
						depth = (Integer) args[1];
					}
					if (args.length > 2) {
						virtualEntityPath = (String) args[2];
					}
				}

				if (isReference) {
					if (depth != 0) {
						ReferenceFinder finder = new ReferenceFinder();
						finder.getReferences((KeyMap) value, depth, validAtTime, asOfTime, getKeyMapReferenceId());
					}
				}
			}
		}
		return entry;
	}

	@BizMethod
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime)
	{
		return getEntries(validAtTime, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getEntries(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}

		Map<ITemporalKey<K>, ITemporalData<K>> map = tm.getTemporalCacheListener().getAsOfEntryMap(validAtTime,
				asOfTime);

		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		if (args != null) {
			if (args.length > 0) {
				isReference = (Boolean) args[0];
			}
			if (args.length > 1) {
				depth = (Integer) args[1];
			}
			if (args.length > 2) {
				virtualEntityPath = (String) args[2];
			}
		}

		if (isReference) {
			if (depth != 0) {
				ReferenceFinder finder = new ReferenceFinder();
				finder.getMapReferences(map, depth, validAtTime, asOfTime, getKeyMapReferenceId());
			}
		}

		return map;
	}

	@Override
	public List<V> getQueryValues(String queryStatement, long validAtTime, long asOfTime)
	{
		// client only
		return null;
	}

	@Override
	public List<TemporalEntry<K, V>> getQueryEntries(String queryStatement, long validAtTime, long asOfTime)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, long validAtTime, long asOfTime, String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime,
			String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex)
	{
		// client only
		return null;
	}

	@Override
	public IScrollableResultSet<V> getValueWrittenTimeRangeResultSet(String queryStatement, long validAtTime,
			long fromWrittenTime, long toWrittenTime, String orderBy, boolean orderAscending, int batchSize,
			boolean forceRebuildIndex, int limit)
	{
		// client only
		return null;
	}
	
	@BizMethod
	@Override
	public List<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryHistoryWrittenTimeRangeList(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		Set<ITemporalKey> temporalKeySet = null;
		CompiledUnit cu = new CompiledUnit(queryStatement);
		String[] fullPaths = cu.getFullPaths();
		if (fullPaths == null || fullPaths.length != 1) {
			return null;
		}

		List<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> resultList = null;
		String fullPath = fullPaths[0];
		if (cu.getQueryLanguage() == QueryLanguage.LUCENE) {
			GemfireTemporalManager tm = GemfireTemporalManager.getTemporalManager(fullPath);
			if (tm != null) {
				String qs;
				LuceneSearch luceneSearch = LuceneSearch.getLuceneSearch(fullPath);
				qs = luceneSearch.getWrittenTimeRangeQuery(validAtTime, fromWrittenTime, toWrittenTime,
						cu.getCompiledQuery());
				temporalKeySet = luceneSearch.getTemporalKeySet(fullPath, qs, -1);
				resultList = tm.getTemporalCacheListener().getTemporalEntryList(temporalKeySet);
			}
		}
		return resultList;
	}

	@BizMethod
	@Override
	public List<V> getValueHistoryWrittenTimeRangeList(String queryStatement, long validAtTime, long fromWrittenTime,
			long toWrittenTime)
	{
		Set<ITemporalKey> temporalKeySet = null;
		CompiledUnit cu = new CompiledUnit(queryStatement);
		String[] fullPaths = cu.getFullPaths();
		if (fullPaths == null || fullPaths.length != 1) {
			return null;
		}

		List resultList = null;
		String fullPath = fullPaths[0];
		if (cu.getQueryLanguage() == QueryLanguage.LUCENE) {
			GemfireTemporalManager tm = GemfireTemporalManager.getTemporalManager(fullPath);
			if (tm != null) {
				String qs;
				LuceneSearch luceneSearch = LuceneSearch.getLuceneSearch(fullPath);
				qs = luceneSearch.getWrittenTimeRangeQuery(validAtTime, fromWrittenTime, toWrittenTime,
						cu.getCompiledQuery());
				temporalKeySet = luceneSearch.getTemporalKeySet(fullPath, qs, -1);
				resultList = tm.getTemporalCacheListener().getValueList(temporalKeySet);
			}
		}
		return resultList;
	}

	@BizMethod
	@Override
	public ITemporalKey<K> getKey(K identityKey)
	{
		TemporalEntry entry = getEntry(identityKey);
		if (entry == null) {
			return null;
		}
		return entry.getTemporalKey();
	}

	@BizMethod
	@Override
	public ITemporalKey<K> getKey(K identityKey, long validAtTime)
	{
		return getKey(identityKey, validAtTime, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public ITemporalKey<K> getKey(K identityKey, long validAtTime, long asOfTime)
	{
		TemporalEntry<K, V> entry = getEntry(identityKey, validAtTime, asOfTime);
		return entry.getTemporalKey();
	}

	@BizMethod
	@Override
	public Set<ITemporalKey<K>> getKeySet(long validAtTime, long asOfTime)
	{
		Map<ITemporalKey<K>, ITemporalData<K>> map = getEntries(validAtTime, asOfTime);
		HashSet set = new HashSet(map.size() + 1);
		set.addAll(map.keySet());
		return set;
	}

	@BizMethod
	@Override
	public Set<ITemporalKey<K>> getKeySet(long validAtTime)
	{
		return getKeySet(validAtTime, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public Set<V> get(long validAtTime)
	{
		return get(validAtTime, System.currentTimeMillis());
	}

	@SuppressWarnings("unchecked")
	@BizMethod
	@Override
	public Set<V> get(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}
		return tm.getTemporalCacheListener().getAsOfValueSet(validAtTime, asOfTime);
	}

	@BizMethod
	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey)
	{
		return getAllEntrySet(identityKey, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime)
	{
		return getAllEntrySet(identityKey, validAtTime, System.currentTimeMillis());
	}

	@BizMethod
	@Override
	public Set<TemporalEntry<K, V>> getAllEntrySet(K identityKey, long validAtTime, long asOfTime)
	{
		ITemporalList tl = getTemporalList(identityKey);
		if (tl == null) {
			return null;
		}
		Set<TemporalEntry<K, V>> set = tl.getAllAsOfEntrySet(validAtTime, asOfTime);

		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		if (args != null) {
			if (args.length > 0) {
				isReference = (Boolean) args[0];
			}
			if (args.length > 1) {
				depth = (Integer) args[1];
			}
			if (args.length > 2) {
				virtualEntityPath = (String) args[2];
			}
		}

		if (isReference) {
			if (depth != 0) {
				ReferenceFinder finder = new ReferenceFinder();
				finder.getCollectionReferences(set, depth, validAtTime, asOfTime, getKeyMapReferenceId());
			}
		}
		return set;
	}

	/**
	 * Not used. ILocalBiz (HPIM) handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<K, V>> getAllLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz (HPIM) handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<K, V>> getLastTemporalEntries(String orderBy, boolean orderAcending,
			int batchSize, boolean forceRebuildIndex, int limit)
	{
		return null;
	}

	// @BizMethod
	// @Override
	// public void invalidate(K identityKey, long endValidTime)
	// {
	// ITemporalList tl = getTemporalList(identityKey);
	// if (tl == null) {
	// return;
	// }
	// // tl.invalidate(endValidTime);
	// }
	//
	// @Override
	// public void invalidate(K identityKey, long endValidTime, long
	// writtenTime)
	// {
	// ITemporalList tl = getTemporalList(identityKey);
	// if (tl == null) {
	// return;
	// }
	// tl.invalidate(endValidTime, writtenTime);
	// }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset()
	{
		// not used
	}

	/**
	 * Not used. ILocalBiz handles it by invoking IGridMap.put(). System time a
	 * problem with the local call.
	 */
	@Override
	public TemporalEntry<K, V> put(K identityKey, V value)
	{
		return put(identityKey, value, false);
	}

	/**
	 * Not used. ILocalBiz handles it by invoking IGridMap.put(). System time a
	 * problem with the local call.
	 */
	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, boolean isDelta)
	{
		long currentTime = System.currentTimeMillis();
		return put(identityKey, value, currentTime, TemporalUtil.MAX_TIME, currentTime, isDelta);
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * IGridMap.put().
	 */
	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, boolean isDelta)
	{
		long currentTime = System.currentTimeMillis();
		return put(identityKey, value, currentTime, endValidTime, currentTime, isDelta);
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * IGridMap.put().
	 */
	@Override
	public TemporalEntry<K, V> put(K identityKey, V value, long startValidTime, long endValidTime, long writtenTime,
			boolean isDelta)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		ITemporalKey<K> tkey = new GemfireTemporalKey<K>(identityKey, startValidTime, endValidTime, writtenTime,
				PadoServerManager.getPadoServerManager().getUsername(bizContext.getUserContext().getToken()));
		ITemporalData<K> data;
		ITemporalValue<K> tvalue;
		if (value instanceof ITemporalData) {
			data = (ITemporalData<K>) value;
		} else {
			data = new GemfireTemporalData(tkey, value);
		}

		tvalue = data.__getTemporalValue();
		if (tvalue == null) {
			tvalue = new GemfireTemporalValue(tkey, (ITemporalData<K>) value);
			data.__setTemporalValue(tvalue);
		}
		tvalue.setDelta(isDelta);
		rfc.getDataSet().put(tkey, data);
		return TemporalInternalFactory.getTemporalInternalFactory().createTemporalEntry(tkey, data);
	}

	/**
	 * Not used. ILocalBiz handles it by invoking IGridMap.putAttachments().
	 * System time is a problem with the local call.
	 */
	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySet)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it by invoking IGridMap.putAttachments().
	 * System time is a problem with the local call.
	 */
	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySet,
			boolean isDelta)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * IGridMap.putAttachments().
	 */
	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, boolean isDelta)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * IGridMap.putAttachments().
	 */
	@Override
	public TemporalEntry<K, V> putAttachments(K identityKey, V value, AttachmentSet<K>[] attachmentIdentityKeySets,
			long startValidTime, long endValidTime, long writtenTime, boolean isDelta)
	{
		return null;
	}

	@BizMethod
	@Override
	public void remove(K identityKey)
	{
		ITemporalList tl = getTemporalList(identityKey);
		if (tl != null) {
			tl.remove(bizContext.getUserContext().getUsername());
		}
	}

	@BizMethod
	@Override
	public boolean isRemoved(K identityKey)
	{
		ITemporalList tl = getTemporalList(identityKey);
		if (tl == null) {
			return true;
		} else {
			return tl.isRemoved();
		}
	}

	@BizMethod
	@Override
	public boolean isExist(K identityKey)
	{
		ITemporalList tl = getTemporalList(identityKey);
		return tl != null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public void dump(K identityKey)
	{
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public void dump(K identityKey, PrintStream printStream, SimpleDateFormat formatter)
	{
	}

	public void dumpAll(boolean includeDeltas)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		String filePath = System.getProperty("pado.temporal.dump.file",
				"data/temporal/" + rfc.getDataSet().getName() + ".txt");
		tm.dumpAll(filePath, includeDeltas);
	}

	/**
	 * This used?
	 */
	@BizMethod
	@Override
	public AttachmentResults<V> getAttachments(K identityKey)
	{
		// TODO: Not sure this method is being used. Remove it otherwise.

		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}

		// Start recording stats
		long startTime = 0;
		if (tm.isStatisticsEnabled()) {
			startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
		}

		TemporalCacheListener cl = tm.getTemporalCacheListener();
		ArrayList<ITemporalData> list = new ArrayList<ITemporalData>();
		Set filterSet = rfc.getFilter();
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		IFilter filter = null;
		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		String name = "default";
		if (args != null) {
			if (args.length > 0) {
				name = (String) args[0];
			}
			if (args.length > 1) {
				filter = (IFilter) args[1];
			}
			if (args.length > 2) {
				isReference = (Boolean) args[2];
			}
			if (args.length > 3) {
				depth = (Integer) args[3];
			}
			if (args.length > 4) {
				virtualEntityPath = (String) args[4];
			}
		}
		if (filterSet == null) {
			Map<ITemporalKey, ITemporalData> map = cl.getNowRelativeMap();
			if (map != null) {
				if (filter == null) {
					list.addAll(map.values());
				} else {
					Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
					for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
						if (filter.isValid(entry.getKey(), entry.getValue())) {
							list.add(entry.getValue());
						}
					}
				}
			}
		} else {
			if (filter == null) {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getNowRelativeEntry(identityKey2);
					if (asof != null) {
						list.add(asof.getTemporalData());
					}
				}
			} else {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getNowRelativeEntry(identityKey2);
					if (asof != null) {
						if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
							list.add(asof.getTemporalData());
						}
					}
				}
			}
		}

		// Return results in AttachementResults. Only one map entry.
		List resultList = null;
		if (list.size() > 0) {
			ITemporalData data = list.get(0);
			if (data instanceof GemfireTemporalData) {
				resultList = new ArrayList<V>(list.size());
				for (ITemporalData data2 : list) {
					resultList.add(((GemfireTemporalData) data2).getValue());
				}
			} else {
				resultList = list;
			}
			if (isReference) {
				if (depth != 0) {
					ReferenceFinder finder = new ReferenceFinder();
					if (list.get(0) instanceof KeyMap) {
						finder.getKeyMapCollectionReferences((List<KeyMap>) resultList, depth, -1, -1,
								getKeyMapReferenceId());
					}
				}
			}
		}
		AttachmentResults results = TemporalInternalFactory.getTemporalInternalFactory().createAttachmentResults();
		HashMap map = new HashMap(1, 1f);
		map.put(name, resultList);
		results.setAttachmentValues(map);

		// Stop recording stats
		if (tm.isStatisticsEnabled()) {
			tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
		}
		return results;
	}

	/**
	 * Not used.
	 */
	@BizMethod
	@Override
	public AttachmentResults<V> getAttachments(K identityKey, long validAtTime)
	{
		return getAttachments(identityKey, validAtTime, System.currentTimeMillis());
	}

	/**
	 * Not used.
	 */
	@Override
	public AttachmentResults<V> getAttachments(K identityKey, long validAtTime, long asOfTime)
	{
		return null;
	}

	@BizMethod
	@Override
	public List<V> __getAttachments(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}

		// Start recording stats
		long startTime = 0;
		if (tm.isStatisticsEnabled()) {
			startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
		}

		TemporalCacheListener cl = tm.getTemporalCacheListener();
		ArrayList<ITemporalData> list = new ArrayList<ITemporalData>();
		Set filterSet = rfc.getFilter();
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		IFilter filter = null;
		int depth = -1;
		boolean isReference = false;
		String virtualEntityPath = null;
		if (args != null) {
			if (args.length > 0) {
				filter = (IFilter) args[0];
			}
			if (args.length > 1) {
				isReference = (Boolean) args[1];
			}
			if (args.length > 2) {
				depth = (Integer) args[2];
			}
			if (args.length > 3) {
				virtualEntityPath = (String) args[3];
			}
		}
		if (filterSet == null) {
			if (filter == null) {
				Map<ITemporalKey, ITemporalData> map = cl.getAsOfEntryMap(validAtTime, asOfTime);
				if (map != null) {
					list.addAll(map.values());
				}
			} else {
				Map<ITemporalKey, ITemporalData> map = cl.getAsOfEntryMap(validAtTime, asOfTime);
				if (map != null) {
					Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
					for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
						if (filter.isValid(entry.getKey(), entry.getValue())) {
							list.add(entry.getValue());
						}
					}
				}
			}
		} else {
			if (filter == null) {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						list.add(asof.getTemporalData());
					}
				}
			} else {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
							list.add(asof.getTemporalData());
						}
					}
				}
			}
		}

		// Return results in AttachementResults. Only one map entry.
		List resultList = null;
		if (list.size() > 0) {
			ITemporalData data = list.get(0);
			if (data instanceof GemfireTemporalData) {
				resultList = new ArrayList<V>(list.size());
				for (ITemporalData data2 : list) {
					resultList.add(((GemfireTemporalData) data2).getValue());
				}
			} else {
				resultList = list;
			}
			if (isReference) {
				if (depth != 0) {
					ReferenceFinder finder = new ReferenceFinder();
					if (resultList.get(0) instanceof KeyMap) {
						finder.getKeyMapCollectionReferences((List<KeyMap>) resultList, depth, validAtTime, asOfTime,
								getKeyMapReferenceId());
					}
				}
			}
		}
		// Stop recording stats
		if (tm.isStatisticsEnabled()) {
			tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
		}
		return resultList;
	}

	@BizMethod
	@Override
	public Map<String, List<V>> __getAttachmentsOnServer(long validAtTime, long asOfTime)
	{
		return __getAttachmentsBroadcast(validAtTime, asOfTime);
	}

	@BizMethod
	@Override
	public Map<String, List<V>> __getAttachmentsBroadcast(long validAtTime, long asOfTime)
	{
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		AttachmentSet attSets[] = null;
		int depth = -1;
		boolean isReference = false;
		Object serverId = null;
		if (args != null) {
			if (args.length > 0) {
				attSets = (AttachmentSet[]) args[0];
			}
			if (args.length > 1) {
				isReference = (Boolean) args[1];
			}
			if (args.length > 2) {
				depth = (Integer) args[2];
			}
			if (args.length > 3) {
				serverId = args[3];
			}
		}

		HashMap<String, List<V>> map = new HashMap<String, List<V>>(attSets.length, 1f);
		if (attSets != null) {
			if (validAtTime == -1) {
				validAtTime = System.currentTimeMillis();
			}
			if (asOfTime == -1) {
				asOfTime = System.currentTimeMillis();
			}
			for (AttachmentSet attSet : attSets) {
				String identityKeyQueryStatement = attSet.getQueryStatement();
				if (VirtualPathEngine.getVirtualPathEngine().isVirtualPath(attSet.getGridPath())) {
					String virtualPath = attSet.getGridPath();
					VirtualCompiledUnit2 vcu = VirtualPathEngine.getVirtualPathEngine()
							.getVirtualCompiledUnit(virtualPath);

					List list = null;
					if (vcu != null && vcu.isEntity()) {
						String entityGridPath = vcu.getEntityGridPath();
						list = getLocalResults(entityGridPath, null /* identityKeySet */, identityKeyQueryStatement,
								null /* filter */, validAtTime, asOfTime);
						if (list != null && depth != 0) {
							// supports KeyMap only
							VirtualPathEntityFinder finder = new VirtualPathEntityFinder();
							// TODO: Support bulk reference
							for (Object object : list) {
								KeyMap keyMap = (KeyMap) object;
								finder.getReferences(PadoServerManager.getPadoServerManager().getCatalog(), virtualPath,
										keyMap, depth, validAtTime, asOfTime, null);
							}
						}
						// } else if
						// (PadoServerManager.getPadoServerManager().isMaster())
						// {
					} else if (serverId == null || serverId.equals(GemfireGridUtil.getDistributedMember().getId())) {
						identityKeyQueryStatement = virtualPath + "?" + identityKeyQueryStatement;
						list = VirtualPathEngine.getVirtualPathEngine().execute(identityKeyQueryStatement, validAtTime,
								asOfTime);
					}
					if (list != null) {
						map.put(attSet.getName(), list);
					}
				} else {

					Set identityKeySet = attSet.getAttachments();
					IFilter filter = attSet.getFilter();
					List list = getLocalResults(attSet.getGridPath(), identityKeySet, identityKeyQueryStatement, filter,
							validAtTime, asOfTime);
					map.put(attSet.getName(), list);

				}
			}
		}

		if (isReference && depth != 0) {

			// Aggregate all KeyMap objects with the same KeyType into one
			// list
			Map<KeyType, List<KeyMap>> mapOfKeyMapLists = new HashMap();
			Set<Map.Entry<String, List<V>>> set = map.entrySet();
			for (Map.Entry<String, List<V>> entry : set) {
				entry.getKey();
				List list = entry.getValue();
				if (list.size() > 0) {
					if (list.get(0) instanceof KeyMap) {
						KeyMap keyMap = (KeyMap) list.get(0);
						List<KeyMap> list2 = mapOfKeyMapLists.get(keyMap.getKeyType());
						if (list2 == null) {
							list2 = new ArrayList(list);
							mapOfKeyMapLists.put(keyMap.getKeyType(), list2);
						} else {
							list2.addAll(list);
						}
					}
				}
			}

			// Find references per aggregated list.
			Object keyMapReferenceId = getKeyMapReferenceId();
			Collection<List<KeyMap>> col = mapOfKeyMapLists.values();
			for (List<KeyMap> keyMapList : col) {
				ReferenceFinder finder = new ReferenceFinder();
				finder.getKeyMapCollectionReferences(keyMapList, depth, validAtTime, asOfTime, keyMapReferenceId);
			}
		}
		return map;
	}

	private List getLocalResults(String gridPath, Set identityKeySet, String identityKeyQueryStatement, IFilter filter,
			long validAtTime, long asOfTime)
	{
		String fullPath = GridUtil.getFullPath(gridPath);
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager.getTemporalManager(fullPath);
		if (tm == null) {
			return null;
		}

		// Start recording stats
		long startTime = 0;
		if (tm.isStatisticsEnabled()) {
			startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
		}

		OrderByQueryString orderByQueryString = null;
		TemporalCacheListener cl = tm.getTemporalCacheListener();
		if (identityKeyQueryStatement.startsWith("select")) {
			// OQL
			try {
				OqlSearch os = OqlSearch.getOqlSearch();
				String qs = os.getTimePredicate(fullPath, validAtTime, asOfTime) + " AND (" + identityKeyQueryStatement
						+ ")";
				List<Object> localList = os.searchLocal(qs);
				// List<Object> localList =
				// os.searchLocal(identityKeyQueryStatement);
				if (localList != null) {
					if (identityKeySet != null) {
						identityKeySet.addAll(localList);
					} else {
						identityKeySet = new HashSet(localList);
					}
				}
			} catch (Exception e) {
				// if query error, log and skip this attribute
				// set
				Logger.error(e);
				return null;
			}
		} else {
			LuceneSearch ls = LuceneSearch.getLuceneSearch(fullPath);
			String qs = ls.getTimePredicate(validAtTime, asOfTime);
			if (identityKeyQueryStatement != null && identityKeyQueryStatement.length() > 0) {
				orderByQueryString = PqlParser.getOrderBy(identityKeyQueryStatement);
				if (orderByQueryString.queryString != null && orderByQueryString.queryString.length() > 0) {
					qs += " AND (" + orderByQueryString.queryString + ")";
				}
			}
			Set identityKeySearchedSet = ls.getIdentityKeySet(fullPath, qs, -1);
			// Set identityKeySearchedSet =
			// ls.getIdentityKeySet(fullPath,
			// identityKeyQueryStatement);
			if (identityKeySet != null) {
				if (identityKeySearchedSet != null) {
					identityKeySet.addAll(identityKeySearchedSet);
				}
			} else {
				identityKeySet = identityKeySearchedSet;
			}
		}
		List list = new ArrayList();

		if (identityKeySet != null) {
			if (filter == null) {
				for (Object identityKey : identityKeySet) {
					TemporalEntry asof = cl.getAsOf(identityKey, validAtTime, asOfTime);
					if (asof != null) {
						if (asof.getTemporalData() instanceof GemfireTemporalData) {
							list.add(((GemfireTemporalData) asof.getTemporalData()).getValue());
						} else {
							list.add(asof.getTemporalData());
						}
					}
				}
			} else {
				for (Object identityKey : identityKeySet) {
					TemporalEntry asof = cl.getAsOf(identityKey, validAtTime, asOfTime);
					if (asof != null) {
						if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
							if (asof.getTemporalData() instanceof GemfireTemporalData) {
								list.add(((GemfireTemporalData) asof.getTemporalData()).getValue());
							} else {
								list.add(asof.getTemporalData());
							}
						}
					}
				}
			}
		}

		if (list.size() > 0 && orderByQueryString != null && orderByQueryString.orderBy != null) {
			Comparator comparator = comparatorFactory.getComparator(list.get(0), orderByQueryString.orderBy,
					orderByQueryString.isAscending, false);
			Collections.sort(list, comparator);
		}

		// Stop recording stats
		if (tm.isStatisticsEnabled()) {
			tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
		}
		return list;

	}

	@BizMethod
	@Override
	public Map<String, List<TemporalEntry<K, V>>> __getAttachmentsEntriesBroadcast(long validAtTime, long asOfTime)
	{
		boolean isReference = false;
		int depth = -1;
		String virtualEntityPath = null;
		AttachmentSet attSets[] = null;
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		if (args != null && args.length > 0) {
			attSets = (AttachmentSet[]) args[0];
			if (args.length > 1) {
				isReference = (Boolean) args[1];
			}
			if (args.length > 2) {
				depth = (Integer) args[2];
			}
			if (args.length > 3) {
				virtualEntityPath = (String) args[3];
			}
		}

		HashMap<String, List<TemporalEntry<K, V>>> map = new HashMap<String, List<TemporalEntry<K, V>>>(attSets.length,
				1f);
		if (attSets != null) {
			if (validAtTime == -1) {
				validAtTime = System.currentTimeMillis();
			}
			if (asOfTime == -1) {
				asOfTime = System.currentTimeMillis();
			}
			for (AttachmentSet attSet : attSets) {
				String rootPath = ((GemfireGridInfo) PadoServerManager.getPadoServerManager().getGridInfo())
						.getGridRootPath();
				String fullPath = rootPath + "/" + attSet.getGridPath();
				GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager.getTemporalManager(fullPath);
				if (tm == null) {
					continue;
				}

				// Start recording stats
				long startTime = 0;
				if (tm.isStatisticsEnabled()) {
					startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
				}

				TemporalCacheListener cl = tm.getTemporalCacheListener();

				Set identityKeySet = attSet.getAttachments();
				String queryStatement = attSet.getQueryStatement();
				OrderByQueryString orderByQueryString = null;
				if (queryStatement != null && queryStatement.length() > 0) {
					LuceneSearch ls = LuceneSearch.getLuceneSearch(fullPath);
					String qs = ls.getTimePredicate(validAtTime, asOfTime);
					orderByQueryString = PqlParser.getOrderBy(queryStatement);
					if (orderByQueryString.queryString != null && orderByQueryString.queryString.length() > 0) {
						qs += " AND (" + orderByQueryString.queryString + ")";
					}
					Set identityKeySearchedSet = ls.getIdentityKeySet(fullPath, qs, -1);
					if (identityKeySearchedSet != null) {
						if (identityKeySet == null) {
							identityKeySet = identityKeySearchedSet;
						} else {
							identityKeySet.addAll(identityKeySearchedSet);
						}
					}
				}
				IFilter filter = attSet.getFilter();
				ArrayList<TemporalEntry<K, V>> list = new ArrayList<TemporalEntry<K, V>>();
				map.put(attSet.getName(), list);

				if (identityKeySet != null) {
					if (filter == null) {
						for (Object identityKey : identityKeySet) {
							TemporalEntry asof = cl.getAsOf(identityKey, validAtTime, asOfTime);
							if (asof != null) {
								list.add(asof);
							}
						}
					} else {
						for (Object identityKey : identityKeySet) {
							TemporalEntry asof = cl.getAsOf(identityKey, validAtTime, asOfTime);
							if (asof != null) {
								if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
									list.add(asof);
								}
							}
						}
					}

					if (isReference) {
						if (depth != 0) {
							// supports KeyMap only
							ReferenceFinder finder = new ReferenceFinder();
							finder.getCollectionReferences(list, depth, validAtTime, asOfTime, getKeyMapReferenceId());
						}
					}
				}
				if (list.size() > 0 && orderByQueryString != null && orderByQueryString.orderBy != null) {
					boolean sortByKey = orderByQueryString.orderBy.equals("IdentityKey")
							|| orderByQueryString.orderBy.equals("Username")
							|| orderByQueryString.orderBy.equals("WrittenTime")
							|| orderByQueryString.orderBy.equals("EndWrittenTime")
							|| orderByQueryString.orderBy.equals("StartValidTime")
							|| orderByQueryString.orderBy.equals("EndValidTime");
					Comparator comparator = comparatorFactory.getComparator(list.get(0), orderByQueryString.orderBy,
							orderByQueryString.isAscending, sortByKey);
					Collections.sort(list, comparator);
				}

				// Stop recording stats
				if (tm.isStatisticsEnabled()) {
					tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
				}
			}
		}

		return map;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V> getAttachments(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)} or
	 * {@link #__getAttachmentsBroadcast(long, long)}.
	 */
	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)} or
	 * {@link #__getAttachmentsBroadcast(long, long)}.
	 */
	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)} or
	 * {@link #__getAttachmentsBroadcast(long, long)}.
	 */
	@Override
	public AttachmentResults<V> getAttachmentsEntries(K identityKey, long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachments(long, long)}.
	 */
	@Override
	public List<V>[] getAttachments(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime, long asOfTime)
	{
		return null;
	}

	@BizMethod
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> __getAttachmentsEntries(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}

		// Start recording stats
		long startTime = 0;
		if (tm.isStatisticsEnabled()) {
			startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
		}

		TemporalCacheListener cl = tm.getTemporalCacheListener();
		ArrayList<ITemporalData> list = new ArrayList<ITemporalData>();
		Set filterSet = rfc.getFilter();
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		boolean isReference = false;
		String virtualEntityPath = null;
		int depth = -1;
		IFilter filter = null;
		if (args != null && args.length > 0) {
			filter = (IFilter) args[0];
			if (args.length > 1) {
				isReference = (Boolean) args[1];
			}
			if (args.length > 2) {
				depth = (Integer) args[2];
			}
			if (args.length > 3) {
				virtualEntityPath = (String) args[3];
			}
		}

		Map<ITemporalKey<K>, ITemporalData<K>> resultMap = null;
		if (filterSet == null) {
			Map<ITemporalKey<K>, ITemporalData<K>> map = cl.getAsOfEntryMap(validAtTime, asOfTime);
			if (filter == null) {
				resultMap = map;
			} else {
				if (map != null) {
					Set<Map.Entry<ITemporalKey<K>, ITemporalData<K>>> set = map.entrySet();
					resultMap = new HashMap();
					for (Map.Entry<ITemporalKey<K>, ITemporalData<K>> entry : set) {
						if (filter.isValid(entry.getKey(), entry.getValue())) {
							resultMap.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
		} else {
			resultMap = new HashMap();
			if (filter == null) {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						resultMap.put(asof.getTemporalKey(), asof.getTemporalData());
					}
				}
			} else {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
							resultMap.put(asof.getTemporalKey(), asof.getTemporalData());
						}
					}
				}
			}
		}

		if (isReference) {
			if (depth != 0) {
				ReferenceFinder finder = new ReferenceFinder();
				finder.getMapReferences(resultMap, depth, validAtTime, asOfTime, getKeyMapReferenceId());
			}
		}

		// Stop recording stats
		if (tm.isStatisticsEnabled()) {
			tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
		}

		return resultMap;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>> getAttachmentsEntries(AttachmentSet<K> attachmentIdentityKeySet,
			long validAtTime, long asOfTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsEntries(long, long)}.
	 */
	@Override
	public Map<ITemporalKey<K>, ITemporalData<K>>[] getAttachmentsEntries(AttachmentSet<K>[] attachmentIdentityKeySets,
			long validAtTime, long asOfTime)
	{
		return null;
	}

	@BizMethod
	@Override
	public Set<ITemporalKey<K>> __getAttachmentsKeys(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		GemfireTemporalManager tm = (GemfireTemporalManager) TemporalManager
				.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return null;
		}

		// Start recording stats
		long startTime = 0;
		if (tm.isStatisticsEnabled()) {
			startTime = tm.getStatistics().startAsOfEntityAttachmentsSearchCount();
		}

		TemporalCacheListener cl = tm.getTemporalCacheListener();
		HashSet<ITemporalKey<K>> resultSet = new HashSet<ITemporalKey<K>>();
		Set filterSet = rfc.getFilter();
		Object args[] = bizContext.getGridContextServer().getAdditionalArguments();
		IFilter filter = null;
		if (args != null) {
			if (args.length > 0) {
				filter = (IFilter) args[0];
			}
		}
		if (filterSet == null) {
			if (filter == null) {
				Map<ITemporalKey<K>, ITemporalData<K>> map = cl.getAsOfEntryMap(validAtTime, asOfTime);
				if (map != null) {
					resultSet.addAll(map.keySet());
				}
			} else {
				Map<ITemporalKey, ITemporalData> map = cl.getAsOfEntryMap(validAtTime, asOfTime);
				if (map != null) {
					Set<Map.Entry<ITemporalKey, ITemporalData>> set = map.entrySet();
					for (Map.Entry<ITemporalKey, ITemporalData> entry : set) {
						if (filter.isValid(entry.getKey(), entry.getValue())) {
							resultSet.add(entry.getKey());
						}
					}
				}
			}
		} else {
			if (filter == null) {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						resultSet.add(asof.getTemporalKey());
					}
				}
			} else {
				for (Object identityKey2 : filterSet) {
					TemporalEntry asof = cl.getAsOf(identityKey2, validAtTime, asOfTime);
					if (asof != null) {
						if (filter.isValid(asof.getTemporalKey(), asof.getTemporalData())) {
							resultSet.add(asof.getTemporalKey());
						}
					}
				}
			}
		}

		// Stop recording stats
		if (tm.isStatisticsEnabled()) {
			tm.getStatistics().endAsOfEntityAttachmentsSearchCount(startTime);
		}

		return resultSet;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>> getAttachmentsKeys(AttachmentSet<K> attachmentIdentityKeySet, long validAtTime,
			long asOfTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it. ILocalBiz handles it by invoking
	 * {@link #__getAttachmentsKeys(long, long)}.
	 */
	@Override
	public Set<ITemporalKey<K>>[] getAttachmentsKeys(AttachmentSet<K>[] attachmentIdentityKeySets, long validAtTime,
			long asOfTime)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(long validAtTime,
			long asOfTime, String orderBy, boolean orderAcending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryResultSet(
			String queryStatement, long validAtTime, long asOfTime, String orderBy, boolean orderAscending,
			int batchSize, boolean forceRebuildIndex, int limit)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<TemporalEntry<ITemporalKey<K>, ITemporalData<K>>> getEntryWrittenTimeRangeResultSet(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime, String orderBy,
			boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		return null;
	}

	/**
	 * Not used. ILocalBiz handles it.
	 */
	@Override
	public IScrollableResultSet<V> getValueResultSet(String queryStatement, long validAtTime, long asOfTime,
			String orderBy, boolean orderAscending, int batchSize, boolean forceRebuildIndex, int limit)
	{
		return null;
	}

	@Override
	public int size()
	{
		return size(-1, -1);
	}

	@Override
	@BizMethod
	public int size(long validAtTime, long asOfTime)
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return 0;
		}
		return tm.getTemporalListCount(validAtTime, asOfTime);
	}

	@Override
	@BizMethod
	public int getTemporalListCount()
	{
		RegionFunctionContext rfc = ((IGemfireGridContextServer) (bizContext.getGridContextServer()))
				.getRegionFunctionContext();
		TemporalManager tm = TemporalManager.getTemporalManager(rfc.getDataSet().getFullPath());
		if (tm == null) {
			return 0;
		}
		return tm.getTemporalListCount();
	}
}
