package com.netcrest.pado.test.rpc.client;

import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.QueryBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * QueryBizTest tests the RPC {@link QueryBiz} "executePql" method via
 * {@link RpcInvoker}. This test case requires the "nw" data distributed with
 * Pado. Make sure to first load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryBizPql extends AbstractTest
{
	public QueryBizPql(String lang)
	{
		super(lang);
	}
	
	@Test
	public void testExecutePql()
	{
		System.out.println("QueryBizTest.testExecutePql()");
		System.out.println("-----------------------------");
		JsonLite request = createBizRequest(QueryBiz.class.getName(), "executePql");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams("nw/orders?VICTE", 1000);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	private JsonLite createRpcParams(String pql, int fetchSize)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("pql", pql);
		rpcParams.put("fetchSize", fetchSize);
		return rpcParams;
	}
}
