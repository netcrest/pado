package com.netcrest.pado.rpc.client.biz;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.IRpcContext;
import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryBiz
{	
	private IRpcContext rpcContext;
	
	public QueryBiz(IRpcContext rpcContext)
	{
		this.rpcContext = rpcContext;
	}
	
	public JsonLite executePql(String pql)
	{
		return executePql(pql, -1);
	}
	
	public JsonLite executePql(String pql, int fetchSize)
	{
		JsonLite params = new JsonLite();
		params.put("pql", pql);
		if (fetchSize > 0) {
			params.put("fetchSize", fetchSize);
		}
		return MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "executePql", params, 0);
	}
	
	public JsonLite nextResultSet(JsonLite result)
	{
		if (result == null) {
			return null;
		}
		result = (JsonLite) result.get("result");
		if (result == null) {
			return null;
		}
		String pql = result.getString("pql", null);
		if (pql == null) {
			return null;
		}
		int nextBatchIndexOnServer = result.getInt("nextBatchIndexOnServer", -1);
		if (nextBatchIndexOnServer < 0) {
			return null;
		}
		int totalSizeOnServer = result.getInt("totalSizeOnServer", -1);
		if (nextBatchIndexOnServer >= totalSizeOnServer) {
			return null;
		}
		int fetchSize = result.getInt("fetchSize", -1);
		int startIndex = nextBatchIndexOnServer;
		
		JsonLite params = new JsonLite();
		params.put("pql", pql);
		params.put("startIndex", startIndex);
		if (fetchSize > 0) {
			params.put("fetchSize", fetchSize);
		}
		System.out.println("QueryBiz.nextResultSet(): params=" + params.toJsonString());
		return MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "executePql", params, 0);	
	}
	
	public JsonLite prevResultSet(JsonLite result)
	{
		if (result == null) {
			return null;
		}
		String pql = result.getString("pql", null);
		if (pql == null) {
			return null;
		}
		int nextBatchIndexOnServer = result.getInt("nextBatchIndexOnServer", -1);
		int totalSizeOnServer = result.getInt("totalSizeOnServer", -1);
		if (nextBatchIndexOnServer >= totalSizeOnServer) {
			return null;
		}
		int fetchSize = result.getInt("fetchSize", 100);
		int startIndex = nextBatchIndexOnServer - 2*fetchSize;
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		JsonLite params = new JsonLite();
		params.put("pql", pql);
		params.put("startIndex", startIndex);
		params.put("fetchSize", fetchSize);
		return MqttJsonRpcClient.getRpcClient().execute(rpcContext, this, "executePql", params, 0);	
	}
}
