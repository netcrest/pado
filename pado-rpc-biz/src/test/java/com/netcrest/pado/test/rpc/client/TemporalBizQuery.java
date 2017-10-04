package com.netcrest.pado.test.rpc.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.rpc.client.biz.TemporalBiz;
import com.netcrest.pado.rpc.client.dna.RpcInvoker;
import com.netcrest.pado.rpc.mqtt.RequestKey;

/**
 * TemporalBizQuery tests the RPC {@link TemporalBiz} query methods via
 * {@link RpcInvoker}. This test case requires the "nw" data distributed with
 * Pado. Make sure to first load that set of data in the "mygrid" environment.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TemporalBizQuery extends AbstractTest
{
	private final String gridPath = "nw/orders";

	public TemporalBizQuery(String lang)
	{
		super(lang);
	}
	
	@Test
	public void testGetAllEntries()
	{
		System.out.println("TemporalBizTest.testGetAllEntries()");
		System.out.println("-----------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "getAllEntries");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, "10251", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		printRequest(request);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testGetEntry()
	{
		System.out.println("TemporalBizTest.testGetEntry()");
		System.out.println("------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "getEntry");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, "10251", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testGet()
	{
		System.out.println("TemporalBizTest.testGet()");
		System.out.println("-------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "get");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, "10251", -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testSize()
	{
		System.out.println("TemporalBizTest.testSize()");
		System.out.println("--------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "size");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath, -1, -1);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testGetTemporalListCount()
	{
		System.out.println("TemporalBizTest.testGetTemporalListCount()");
		System.out.println("------------------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "getTemporalListCount");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		JsonLite rpcParams = createRpcParams(gridPath);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	@Test
	public void testGetEntryHistoryWrittenTimeRangeList() throws ParseException
	{
		System.out.println("TemporalBizTest.testGetEntryHistoryWrittenTimeRangeList()");
		System.out.println("---------------------------------------------------------");
		JsonLite request = createBizRequest(TemporalBiz.class.getName(), "getEntryHistoryWrittenTimeRangeList");
		JsonLite params = (JsonLite) request.get(RequestKey.params.name());
		SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd");
		long fromWrittenTime = format.parse("1980/01/01").getTime();
		long toWrittenTime = System.currentTimeMillis();
		JsonLite rpcParams = createRpcParams("nw/orders?10251", -1, fromWrittenTime, toWrittenTime);
		params.put(RequestKey.params.name(), rpcParams);

		System.out.println("Reply:");
		List<JsonLite> reply = rpcBiz.broadcast(request);
		printReply(reply);
	}

	private JsonLite createRpcParams(String gridPath)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		return rpcParams;
	}

	private JsonLite createRpcParams(String gridPath, long validAtTime, long asOfTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("asOfTime", asOfTime);
		return rpcParams;
	}

	private JsonLite createRpcParams(String gridPath, String identityKey, long validAtTime, long asOfTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("gridPath", gridPath);
		rpcParams.put("identityKey", identityKey);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("asOfTime", asOfTime);
		return rpcParams;
	}

	private JsonLite createRpcParams(String queryStatement, long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		JsonLite rpcParams = new JsonLite();
		rpcParams.put("queryStatement", queryStatement);
		rpcParams.put("validAtTime", validAtTime);
		rpcParams.put("fromWrittenTime", fromWrittenTime);
		rpcParams.put("toWrittenTime", toWrittenTime);
		return rpcParams;
	}
}
