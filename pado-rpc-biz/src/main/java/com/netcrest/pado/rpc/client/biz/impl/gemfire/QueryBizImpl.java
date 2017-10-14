package com.netcrest.pado.rpc.client.biz.impl.gemfire;

import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.provider.lucene.LuceneTemporalQueryFunction;
import com.netcrest.pado.index.result.IMemberResults;
import com.netcrest.pado.index.result.ValueInfo;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.rpc.IRpc;
import com.netcrest.pado.rpc.client.IRpcContext;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class QueryBizImpl implements IRpc
{
	protected IRpcContext rpcContext;

	public void init(IRpcContext rpcContext)
	{
		this.rpcContext = rpcContext;
	}

	public JsonLite executePql(JsonLite params) throws Exception
	{
		String pql = params.getString("pql", null);
		int startIndex = params.getInt("startIndex", 0);
		boolean forceRebuildIndex = params.getBoolean("forceRebuildIndex", false);
		int fetchSize = params.getInt("fetchSize", -1);
		GridQuery criteria = new GridQuery();
		criteria.setQueryString(pql);
		criteria.setStartIndex(startIndex);
		criteria.setForceRebuildIndex(forceRebuildIndex);
		criteria.setId(pql);
		criteria.setOrdered(false);
		if (fetchSize > 0) {
			criteria.setFetchSize(fetchSize);
			criteria.setAggregationPageSize(fetchSize);
		}
		criteria.setProviderKey(Constants.TEMPORAL_ENTRY_PROVIDER_KEY);
		// BizIndexServerResultsCollector resultCollector = new
		// BizIndexServerResultsCollector(criteria);
		Execution exec = FunctionService
				.onMember(CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember())
				.withArgs(criteria);
		ResultCollector rs = exec.execute(LuceneTemporalQueryFunction.Id);
		JsonLite result = new JsonLite();
		List<JsonLite> resultList = null;
		try {
			Object obj = rs.getResult();
			List<IMemberResults> memberResultsList = (List<IMemberResults>) obj;
			if (memberResultsList != null) {
				resultList = new ArrayList<JsonLite>(memberResultsList.size());
				for (IMemberResults memberResults : memberResultsList) {
					result.put("pql", pql);
					result.put("nextBatchIndexOnServer", memberResults.getNextBatchIndexOnServer());
					result.put("totalSizeOnServer", memberResults.getTotalSizeOnServer());
					if (fetchSize > 0) {
						result.put("fetchSize", fetchSize);
					}
					List<ValueInfo> valueInfoList = (List<ValueInfo>) memberResults.getResults();
					for (ValueInfo valueInfo : valueInfoList) {
						TemporalEntry te = (TemporalEntry) valueInfo.getValue();
						ITemporalKey tk = te.getTemporalKey();
						ITemporalData td = te.getTemporalData();
						JsonLite jl = new JsonLite();
						jl.put("key", tk);
						jl.put("value", td.getValue());
						resultList.add(jl);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		result.put("dataList", resultList);
		return result;
	}
}
