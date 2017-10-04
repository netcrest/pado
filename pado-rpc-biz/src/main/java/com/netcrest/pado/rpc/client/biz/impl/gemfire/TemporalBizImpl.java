package com.netcrest.pado.rpc.client.biz.impl.gemfire;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.rpc.IRpc;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalBizImpl implements IRpc
{
	private TemporalEntry __getEntry(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String identityKey = params.getString("identityKey", null);
		long validAtTime = params.getInt("validAtTime", -1);
		long asOfTime = params.getInt("asOfTime", -1);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (identityKey == null) {
			throw new InvalidParameterException("identityKey undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);

		return tm.getAsOf(identityKey, validAtTime, asOfTime);
	}

	private Set<TemporalEntry<ITemporalKey, ITemporalData>> __getAllEntrySet(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String identityKey = params.getString("identityKey", null);
		long validAtTime = params.getInt("validAtTime", -1);
		long asOfTime = params.getInt("asOfTime", -1);

		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (identityKey == null) {
			throw new InvalidParameterException("identityKey undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		if (tm == null) {
			return null;
		}
		ITemporalList<ITemporalKey, ITemporalData> tl =tm.getTemporalList(identityKey);
		if (tl == null) {
			return null;
		}
		return tl.getAllAsOfEntrySet(validAtTime, asOfTime);
	}

	private JsonLite toJsonLite(ITemporalKey tk)
	{
		JsonLite jl = new JsonLite();
		jl.put("identityKey", tk.getIdentityKey());
		jl.put("username", tk.getUsername());
		jl.put("startValidTime", tk.getStartValidTime());
		jl.put("endValidTime", tk.getEndValidTime());
		jl.put("writtenTime", tk.getWrittenTime());
		return jl;
	}
	
	private JsonLite[] __getTemporalEntries(Collection<TemporalEntry<ITemporalKey, ITemporalData>> col)
	{
		JsonLite[] retArray = null;
		if (col != null) {
			retArray = new JsonLite[col.size()];
			int i = 0;
			for (TemporalEntry te : col) {
				JsonLite pair = new JsonLite();
				pair.put("key", toJsonLite(te.getTemporalKey()));
				pair.put("value", te.getTemporalData().getValue());
				retArray[i++] = pair;
			}
		}
		return retArray;
	}
	
	private int __size(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		long validAtTime = params.getInt("validAtTime", -1);
		long asOfTime = params.getInt("asOfTime", -1);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		if (tm == null) {
			return 0;
		}
		return tm.getTemporalListCount(validAtTime, asOfTime);
	}

	public JsonLite get(JsonLite params) throws Exception
	{
		JsonLite retJl = null;
		TemporalEntry te = __getEntry(params);
		if (te != null) {
			retJl = (JsonLite) te.getTemporalData().getValue();
		}
		return retJl;
	}

	public JsonLite getEntry(JsonLite params) throws Exception
	{
		JsonLite retJl = null;
		TemporalEntry te = __getEntry(params);
		if (te != null) {
			retJl = new JsonLite();
			retJl.put("key", toJsonLite(te.getTemporalKey()));
			retJl.put("value", te.getTemporalData().getValue());
		}
		return retJl;
	}

	public JsonLite[] getAllEntries(JsonLite params) throws Exception
	{
		Set<TemporalEntry<ITemporalKey, ITemporalData>> set = __getAllEntrySet(params);
		return __getTemporalEntries(set);
	}
	
	public List<TemporalEntry<ITemporalKey, ITemporalData>> __getEntryHistoryWrittenTimeRangeList(
			String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		Set<ITemporalKey> temporalKeySet = null;
		CompiledUnit cu = new CompiledUnit(queryStatement);
		String[] fullPaths = cu.getFullPaths();
		if (fullPaths == null || fullPaths.length != 1) {
			return null;
		}

		List<TemporalEntry<ITemporalKey, ITemporalData>> resultList = null;
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

	public JsonLite[] getEntryHistoryWrittenTimeRangeList(JsonLite params) throws Exception
	{
		String queryStatement = params.getString("queryStatement", null);
		long validAtTime = params.getInt("validAtTime", -1);
		long fromWrittenTime = params.getInt("fromWrittenTime", -1);
		long toWrittenTime = params.getInt("toWrittenTime", -1);
		
		if (queryStatement == null) {
			throw new InvalidParameterException("queryStatement undefined");
		}
		List<TemporalEntry<ITemporalKey, ITemporalData>> list = __getEntryHistoryWrittenTimeRangeList(queryStatement, validAtTime, fromWrittenTime, toWrittenTime);
		return __getTemporalEntries(list);
	}
	
	public int size(JsonLite params)
	{
		return __size(params);
	}
	
	public int getTemporalListCount(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		return tm.getTemporalListCount();
	}
}
