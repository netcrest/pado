package com.netcrest.pado.rpc.client.biz.impl.gemfire;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.index.provider.lucene.LuceneSearch;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.CompiledUnit.QueryLanguage;
import com.netcrest.pado.rpc.IRpc;
import com.netcrest.pado.rpc.client.IRpcContext;
import com.netcrest.pado.rpc.util.DataNodeUtil;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.ITemporalList;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.gemfire.GemfireTemporalManager;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalBizImpl implements IRpc
{
	protected IRpcContext rpcContext;
	
	public void init(IRpcContext rpcContext)
	{
		this.rpcContext = rpcContext;
	}
	
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
		ITemporalList<ITemporalKey, ITemporalData> tl = tm.getTemporalList(identityKey);
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

	public List<TemporalEntry<ITemporalKey, ITemporalData>> __getEntryHistoryWrittenTimeRangeList(String queryStatement,
			long validAtTime, long fromWrittenTime, long toWrittenTime)
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
		List<TemporalEntry<ITemporalKey, ITemporalData>> list = __getEntryHistoryWrittenTimeRangeList(queryStatement,
				validAtTime, fromWrittenTime, toWrittenTime);
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

	public JsonLite[] getTemporalList(JsonLite params)
	{
		String gridPath = params.getString("gridPath", null);
		String identityKey = params.getString("identityKey", null);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (identityKey == null) {
			throw new InvalidParameterException("identityKey undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
		ITemporalList list = tm.getTemporalList(identityKey);
		TemporalDataList tdl = list.getTemporalDataList();
		List<TemporalEntry<ITemporalKey, ITemporalData>> tl = tdl.getTemporalList();
		return __getTemporalEntries(tl);
	}
	
	private JsonLite dumpTemporalLists(JsonLite params, boolean isAll)
	{
		String gridPath = params.getString("gridPath", null);
		String dumpDir = params.getString("dumpDir", null);
		List identityKeyList = null;
		if (isAll == false) {
			identityKeyList = params.getList("identityKeys");
			if (identityKeyList == null) {
				throw new InvalidParameterException("identityKeys undefined");
			}
		}
		boolean isIncludeColumnHeader = params.getBoolean("includeColumnHeader", false);
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}

		String fileNamePostfix = "dna";
		String fullPath = GridUtil.getFullPath(gridPath);
		File topDir;
		if (dumpDir == null) {
			topDir = new File(dumpDir, "dna");
		} else {
			topDir = new File(dumpDir);
		}
		JsonLite retJl = new JsonLite();
		retJl.put("dumpDir", topDir.getAbsolutePath());
		JsonLite fileMap = new JsonLite();
		retJl.put("fileMap", fileMap);

		Collection col;
		if (isAll) {
			TemporalManager tm = TemporalManager.getTemporalManager(fullPath);
			col = tm.getIdentityKeySet();
		} else {
			col = identityKeyList;
		}
		for (Object identityKey : col) {
			// dump
			File filePath = DataNodeUtil.dumpTemporalList(topDir, fileNamePostfix, gridPath, identityKey.toString(),
					isIncludeColumnHeader);
			if (filePath != null) {
				fileMap.put(identityKey.toString(), filePath.getName());
			}
		}

		return retJl;
	}

	public JsonLite dumpTemporalLists(JsonLite params)
	{
		return dumpTemporalLists(params, false);
	}

	public JsonLite dumpAllTemporalLists(JsonLite params)
	{
		return dumpTemporalLists(params, true);
	}
	
	public void put(JsonLite params) throws Exception
	{
		String gridPath = params.getString("gridPath", null);
		String identityKey = params.getString("identityKey", null);
		long startValidTime = params.getInt("startValidTime", -1);
		long endValidTime = params.getInt("endValidTime", -1);
		long writtenTime = params.getInt("writtenTime", -1);
		JsonLite value = (JsonLite)params.get("value", -1);
		Object token = rpcContext.getToken();
		String username = rpcContext.getUsername();
		if (token != null) {
			username = PadoServerManager.getPadoServerManager().getUsername(token);
		}
		if (username == null) {
			username = "dna";
		}
		if (gridPath == null) {
			throw new InvalidParameterException("gridPath undefined");
		}
		if (identityKey == null) {
			throw new InvalidParameterException("identityKey undefined");
		}
		if (value == null) {
			throw new InvalidParameterException("value undefined");
		}
		String fullPath = GridUtil.getFullPath(gridPath);
		Region region = CacheFactory.getAnyInstance().getRegion(fullPath);
		if (region == null) {
			throw new InvalidParameterException("gridPath does not exist: " + gridPath);
		}
		long now = System.currentTimeMillis();
		if (startValidTime < 0) {
			startValidTime = now;
		}
		if (endValidTime < 0) {
			endValidTime = TemporalUtil.MAX_TIME;;
		}
		if (writtenTime < 0) {
			writtenTime = now;
		}
		
		ITemporalKey tk = TemporalClientFactory.getTemporalClientFactory().createTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, username);
		ITemporalData td = TemporalClientFactory.getTemporalClientFactory().createTemporalData(tk, value);
		region.put(tk, td);
	}
}
